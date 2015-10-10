/*****************************************************************************
 **
 ** @author Lee Neuse (coder@humbleprogrammer.net)
 ** @since 1.0
 **
 **	---------------------------- [License] ----------------------------------
 **	This work is licensed under the Creative Commons Attribution-NonCommercial-
 **	ShareAlike 3.0 Unported License. To view a copy of this license, visit
 **				http://creativecommons.org/licenses/by-nc-sa/3.0/
 **	or send a letter to Creative Commons, 444 Castro Street Suite 900, Mountain
 **	View, California, 94041, USA.
 **	--------------------- [Disclaimer of Warranty] --------------------------
 **	There is no warranty for the program, to the extent permitted by applicable
 **	law.  Except when otherwise stated in writing the copyright holders and/or
 **	other parties provide the program “as is” without warranty of any kind,
 **	either expressed or implied, including, but not limited to, the implied
 **	warranties of merchantability and fitness for a particular purpose.  The
 **	entire risk as to the quality and performance of the program is with you.
 **	Should the program prove defective, you assume the cost of all necessary
 **	servicing, repair or correction.
 **	-------------------- [Limitation of Liability] --------------------------
 **	In no event unless required by applicable law or agreed to in writing will
 **	any copyright holder, or any other party who modifies and/or conveys the
 **	program as permitted above, be liable to you for damages, including any
 **	general, special, incidental or consequential damages arising out of the
 **	use or inability to use the program (including but not limited to loss of
 **	data or data being rendered inaccurate or losses sustained by you or third
 **	parties or a failure of the program to operate with any other programs),
 **	even if such holder or other party has been advised of the possibility of
 **	such damages.
 **
 ******************************************************************************/
package net.humbleprogrammer.maxx;

import java.util.Iterator;

import net.humbleprogrammer.humble.BitUtil;
import net.humbleprogrammer.humble.DBC;

import static net.humbleprogrammer.maxx.Constants.*;

@SuppressWarnings( "unused" )
public class MoveList implements Iterable<Move>
	{

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Maximum possible moves in a single position. */
	private static final int MAX_MOVE_COUNT = 224;

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** .T. if move list is all legal mvoes; .F. otherwise. */
	private final boolean _bAllMoves;
	/** Color of opposing player. */
	private final int     _opponent;
	/** Color of moving player. */
	private final int     _player;
	/** Square occupied by the moving player's King. */
	private final int     _iSqKing;
	/** Bitboard of all pieces. */
	private final long    _bbAll;
	/** Bitboard of opposing pieces. */
	private final long    _bbOpponent;
	/** Bitboard of moving pieces. */
	private final long    _bbPlayer;
	/** Zobrist hash of board that moves were generated for. */
	private final long    _hashZobrist;
	/** Current position. */
	private final Board   _board;
	/** Array of packed moves. */
	private final int[]  _moves = new int[ MAX_MOVE_COUNT ];
	/** Saved copy of the board maps. */
	private final long[] _map   = new long[ MAP_LENGTH ];

	/** Number of moves in {@link #_moves}. */
	private int  _iCount;
	/** Bitboard of pieces threatening the moving player's King. */
	private long _bbCheckers;
	/** Bitboard of pawns that can capture via e.p. */
	private long _bbEP;
	/** Bitboard of potential "From" squares. */
	private long _bbFromSq;
	/** Bitboard of potential "To" squares. */
	private long _bbToSq;
	/** Bitboard of all pieces that are NOT pinned. */
	private long _bbUnpinned;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 *
	 * @param bd
	 * 	Board to generate moves for.
	 *
	 * @throws java.lang.IllegalArgumentException
	 * 	if board is <code>null</code>.
	 */
	public MoveList( Board bd )
		{
		DBC.requireNotNull( bd, "bd" );
		//	-----------------------------------------------------------------
		_board = bd;

		_player = _board.getMovingPlayer();
		_opponent = _player ^ 1;

		_bbOpponent = _board.map[ _opponent ];
		_bbPlayer = _board.map[ _player ];
		_bbAll = _bbPlayer | _bbOpponent;

		_bbFromSq = _bbPlayer;
		_bbToSq = ~_bbPlayer;

		_bAllMoves = true;
		_hashZobrist = bd.getZobristHash();
		_iSqKing = bd.getKingSquare( _player );

		if (Square.isValid( _iSqKing ))
			{
			initBitboards();
			generate();
			}
		}

	/**
	 * Generate a subset of legal moves.
	 *
	 * @param bd
	 * 	Position to generate moves for.
	 * @param bbCandidates
	 * 	Bitboard of moving pieces.
	 * @param iSqTo
	 * 	Target square, in 8x8 format.
	 */
	public MoveList( Board bd, long bbCandidates, int iSqTo )
		{
		DBC.requireNotNull( bd, "bd" );
		//	-----------------------------------------------------------------
		_board = bd;

		_player = _board.getMovingPlayer();
		_opponent = _player ^ 1;

		_bbOpponent = _board.map[ _opponent ];
		_bbPlayer = _board.map[ _player ];
		_bbAll = _bbPlayer | _bbOpponent;

		_bbFromSq = bbCandidates & _bbPlayer;
		_bbToSq = (1L << iSqTo);

		_bAllMoves = false;
		_hashZobrist = bd.getZobristHash();
		_iSqKing = bd.getKingSquare( _player );

		if (Square.isValid( _iSqKing ))
			{
			initBitboards();
			generate();
			//
			//  Take out all moves that don't reach the "To" square.  Do this by copying the last
			//  move on top of the "bad" move.  The current index is decremented so that the next
			//  iteration of the loop will test the copied move, which is now in the same element.
			//
			int iToMask = Move.pack( 0, iSqTo );

			for ( int index = 0; index < _iCount; ++index )
				if ((_moves[ index ] & Move.MASK_TO_SQ) != iToMask && --_iCount > index)
					_moves[ index-- ] = _moves[ _iCount ];
			}
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC GETTERS & SETTERS
	//	-----------------------------------------------------------------------

	/**
	 * Gets the move at a given index.
	 *
	 * @param index
	 * 	Zero-based index of move.
	 *
	 * @return Move if valid index; <c>null/c> otherwise.
	 */
	public Move getAt( int index )
		{
		return (index >= 0 && index < _iCount)
			   ? new Move( _moves[ index ], _board.getZobristHash() )
			   : null;
		}

	/**
	 * Gets the Zobrist hash for the position moves were generated for.
	 *
	 * @return 64-bit hash value.
	 */
	public long getZobristHash()
		{
		return _bAllMoves ? _hashZobrist : HASH_INVALID;
		}

	/**
	 * Gets the empty / not empty state of the move list.
	 *
	 * @return <code>.T.</code> if no legal moves available; <code>.F.</code> otherwise.
	 */
	public boolean hasLegalMove()
		{
		return (_iCount > 0);
		}

	/**
	 * Gets the number of legal moves found.
	 *
	 * @return Move count.
	 */
	public int size()
		{
		return _iCount;
		}

	//  -----------------------------------------------------------------------
	//	METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Adds a set of moves to the move list, testing it for legality if necessary.
	 *
	 * @param iSqFrom
	 * 	"From" square in 8x8 format.
	 * @param bbTo
	 * 	Bitboard of potential "To" squares.
	 * @param iType
	 * 	Type of move (Type.*)
	 */
	private void addMoves( int iSqFrom, long bbTo, int iType )
		{
		int iSqTo;

		for ( bbTo &= _bbToSq; bbTo != 0L; bbTo ^= (1L << iSqTo) )
			{
			iSqTo = BitUtil.first( bbTo );
			if (testMove( iSqFrom, iSqTo, iType ))
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, iType );
			}
		}

	/**
	 * Adds a set of moves to the move list, testing it for legality if necessary.
	 *
	 * @param bbFrom
	 * 	Bitboard of potential "From" squares.
	 * @param iSqTo
	 * 	"To" square in 8x8 format.
	 * @param iType
	 * 	Type of move (Type.*)
	 */
	private void addMoves( long bbFrom, int iSqTo, int iType )
		{
		int iSqFrom;

		for ( bbFrom &= _bbFromSq; bbFrom != 0L; bbFrom ^= (1L << iSqFrom) )
			{
			iSqFrom = BitUtil.first( bbFrom );
			if (testMove( iSqFrom, iSqTo, iType ))
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, iType );
			}
		}

	/**
	 * Adds a set of moves to the move list, testing it for legality if necessary.
	 *
	 * @param iDelta
	 * 	Mathematical distance between "From" and "To" squares.
	 * @param bbTo
	 * 	Bitboard of potential "To" squares.
	 * @param iType
	 * 	Type of move (Type.*)
	 */
	private void addPawnMoves( int iDelta, long bbTo, int iType )
		{
		if ((bbTo &= _bbToSq) == 0) return;
		//	-----------------------------------------------------------------
		int iSqTo;

		//	Promotions first.
		for ( long bb = bbTo & Square.NO_PAWN_ZONE; bb != 0; bb ^= (1L << iSqTo) )
			{
			iSqTo = BitUtil.first( bb );
			int iSqFrom = iSqTo + iDelta;

			if (testMove( iSqFrom, iSqTo, Move.Type.PROMOTION ))
				{
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTION );
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTE_KNIGHT );
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTE_BISHOP );
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTE_ROOK );
				}
			}

		//	Then regular captures/moves.
		for ( long bb = bbTo & ~Square.NO_PAWN_ZONE; bb != 0; bb ^= (1L << iSqTo) )
			{
			iSqTo = BitUtil.first( bb );
			int iSqFrom = iSqTo + iDelta;

			if (testMove( iSqFrom, iSqTo, iType ))
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, iType );
			}
		}

	/**
	 * Generate all legal moves.
	 */
	private void generate()
		{
		long bbPawns;
		long bbPieces = _bbFromSq;
		//
		//	Generate pawn captures/moves.
		//
		if (_player == WHITE)
			{
			bbPawns = bbPieces & _map[ MAP_W_PAWN ];
			generatePawnMovesWhite( bbPawns );
			}
		else
			{
			bbPawns = bbPieces & _map[ MAP_B_PAWN ];
			generatePawnMovesBlack( bbPawns );
			}
		//
		//	Generate remaining moves.
		//
		int iSq;

		for ( bbPieces &= ~bbPawns; bbPieces != 0L; bbPieces ^= (1L << iSq) )
			{
			iSq = BitUtil.first( bbPieces );

			switch (_board.sq[ iSq ])
				{
				case MAP_W_KNIGHT:
				case MAP_B_KNIGHT:
					addMoves( iSq,
							  Bitboards.knight[ iSq ],
							  Move.Type.NORMAL );
					break;

				case MAP_W_BISHOP:
				case MAP_B_BISHOP:
					addMoves( iSq,
							  Bitboards.getDiagonalMovesFrom( iSq, _bbAll ),
							  Move.Type.NORMAL );
					break;

				case MAP_W_ROOK:
				case MAP_B_ROOK:
					addMoves( iSq,
							  Bitboards.getLateralMovesFrom( iSq, _bbAll ),
							  Move.Type.NORMAL );
					break;

				case MAP_W_QUEEN:
				case MAP_B_QUEEN:
					addMoves( iSq,
							  Bitboards.getQueenMovesFrom( iSq, _bbAll ),
							  Move.Type.NORMAL );
					break;

				case MAP_W_KING:
					generateKingMovesWhite( iSq,
											(_board.getCastlingFlags() & Board.CastlingFlags.WHITE_BOTH) );
					break;

				case MAP_B_KING:
					generateKingMovesBlack( iSq,
											(_board.getCastlingFlags() & Board.CastlingFlags.BLACK_BOTH) );
					break;

				default:
					throw new RuntimeException( "Invalid piece type." );
				}
			}

		}

	/**
	 * Generate all legal castling moves for Black.
	 *
	 * @param iSq
	 * 	"From" square, in 8x8 format.
	 * @param castling
	 * 	Black's castling rights.
	 */
	private void generateKingMovesBlack( int iSq, int castling )
		{
		assert Square.isValid( iSq );
		assert _board.sq[ iSq ] == Piece.B_KING;
		//	-----------------------------------------------------------------
		addMoves( iSq, Bitboards.king[ iSq ], Move.Type.NORMAL );

		//	Can't castle out of check.
		if (_bbCheckers != 0)
			return;

		// Black  O-O
		if ((castling & Board.CastlingFlags.BLACK_SHORT) != 0 &&
			(_bbAll & Board.CastlingFlags.BLACK_SHORT_MASK) == 0 &&
			!Bitboards.isAttackedByWhite( _board.map, Square.F8 ) &&
			testMove( Square.E8, Square.G8, Move.Type.CASTLING ))
			{
			_moves[ _iCount++ ] = Move.BLACK_CASTLE_SHORT;
			}

		// Black O-O-O
		if ((castling & Board.CastlingFlags.BLACK_LONG) != 0 &&
			(_bbAll & Board.CastlingFlags.BLACK_LONG_MASK) == 0 &&
			!Bitboards.isAttackedByWhite( _board.map, Square.D8 ) &&
			testMove( Square.E8, Square.C8, Move.Type.CASTLING ))
			{
			_moves[ _iCount++ ] = Move.BLACK_CASTLE_LONG;
			}
		}

	/**
	 * Generate all legal castling moves for White.
	 *
	 * @param iSq
	 * 	"From" square, in 8x8 format.
	 * @param castling
	 * 	White's castling rights.
	 */
	private void generateKingMovesWhite( int iSq, int castling )
		{
		assert Square.isValid( iSq );
		assert _board.sq[ iSq ] == Piece.W_KING;
		//	-----------------------------------------------------------------
		addMoves( iSq, Bitboards.king[ iSq ], Move.Type.NORMAL );

		//	Can't castle out of check.
		if (_bbCheckers != 0)
			return;

		// White O-O
		if ((castling & Board.CastlingFlags.WHITE_SHORT) != 0 &&
			(_bbAll & Board.CastlingFlags.WHITE_SHORT_MASK) == 0 &&
			!Bitboards.isAttackedByBlack( _board.map, Square.F1 ) &&
			testMove( Square.E1, Square.G1, Move.Type.CASTLING ))
			{
			_moves[ _iCount++ ] = Move.WHITE_CASTLE_SHORT;
			}

		// White O-O-O
		if ((castling & Board.CastlingFlags.WHITE_LONG) != 0 &&
			(_bbAll & Board.CastlingFlags.WHITE_LONG_MASK) == 0 &&
			!Bitboards.isAttackedByBlack( _board.map, Square.D1 ) &&
			testMove( Square.E1, Square.C1, Move.Type.CASTLING ))
			{
			_moves[ _iCount++ ] = Move.WHITE_CASTLE_LONG;
			}
		}

	/**
	 * Generate all moves for a set of Black pawns.
	 *
	 * @param bbPawns
	 * 	Bitboard of pawns to moves.
	 */
	private void generatePawnMovesBlack( long bbPawns )
		{
		if (bbPawns == 0) return;
		//	-----------------------------------------------------------------

		//	Captures to the SW.
		addPawnMoves( 7,
					  (((bbPawns & 0x7F7F7F7F7F7F7F7FL) >>> 7) & _bbOpponent),
					  Move.Type.NORMAL );

		//	Captures to the SE.
		addPawnMoves( 9,
					  (((bbPawns & 0xFEFEFEFEFEFEFEFEL) >>> 9) & _bbOpponent),
					  Move.Type.NORMAL );

		//	All e.p. captures.
		if (_bbEP != 0L)
			addMoves( _bbEP, _board.getEnPassantSquare(), Move.Type.EN_PASSANT );

		//	Normal moves.
		long bbEmpty = ~_bbAll;
		long bbUnblocked = (bbPawns >>> 8) & bbEmpty;

		addPawnMoves( 8, bbUnblocked, Move.Type.NORMAL );

		//	Pawn Advances (double moves)
		if ((bbUnblocked &= Bitboards.rankMask[ 5 ]) != 0)
			{
			addPawnMoves( 16,
						  ((bbUnblocked >>> 8) & bbEmpty),
						  Move.Type.PAWN_PUSH );
			}
		}

	/**
	 * Generate all moves for a set of White pawns.
	 *
	 * @param bbPawns
	 * 	Bitboard of pawns to moves.
	 */
	private void generatePawnMovesWhite( long bbPawns )
		{
		if (bbPawns == 0) return;
		//	-----------------------------------------------------------------
		long bbEmpty = ~_bbAll;

		//	Captures to the NW.
		addPawnMoves( -9,
					  (((bbPawns & 0x7F7F7F7F7F7F7F7FL) << 9) & _bbOpponent),
					  Move.Type.NORMAL );

		//	Captures to the NE.
		addPawnMoves( -7,
					  (((bbPawns & 0xFEFEFEFEFEFEFEFEL) << 7) & _bbOpponent),
					  Move.Type.NORMAL );

		//	All e.p. captures.
		if (_bbEP != 0L)
			addMoves( _bbEP, _board.getEnPassantSquare(), Move.Type.EN_PASSANT );

		//	Normal moves.
		long bbUnblocked = (bbPawns << 8) & bbEmpty;

		addPawnMoves( -8, bbUnblocked, Move.Type.NORMAL );

		//	Pawn Advances (double moves)
		if ((bbUnblocked &= Bitboards.rankMask[ 2 ]) != 0)
			{
			addPawnMoves( -16,
						  ((bbUnblocked << 8) & bbEmpty),
						  Move.Type.PAWN_PUSH );
			}
		}

	/**
	 * Initializes the internal bitboards.
	 */
	private void initBitboards()
		{
		assert Square.isValid( _iSqKing );
		//	-----------------------------------------------------------------
		System.arraycopy( _board.map, 0, _map, 0, MAP_LENGTH );
		//
		//	See if there are any e.p. captures possible.
		//
		int iSq = _board.getEnPassantSquare();

		if (Square.isValid( iSq ))
			{
			_bbEP = (_player == WHITE)
					? (Bitboards.pawnDownwards[ iSq ] & _map[ MAP_W_PAWN ])
					: (Bitboards.pawnUpwards[ iSq ] & _map[ MAP_B_PAWN ]);
			}
		//
		//  Find out if the moving player is in check, because that affects the possible "From"
		//	and "To" squares.  If the player is NOT in check, build a bitboard of pinned pieces.
		//
		_bbCheckers = Bitboards.getAttackedBy( _map, _iSqKing, _opponent );
		_bbUnpinned = _bbPlayer;

		if (_bbCheckers == 0L)
			{
			//
			//  Find pinned pieces.  This is done by finding all of the opposing pieces that
			//  could attack the King if the moving player's pieces were removed.
			//
			long bbDiagonal = _map[ MAP_W_QUEEN + _opponent ] | _map[ MAP_W_BISHOP + _opponent ];
			long bbLateral = _map[ MAP_W_QUEEN + _opponent ] | _map[ MAP_W_ROOK + _opponent ];
			long bbPinners = Bitboards.getDiagonalAttackers( _iSqKing, bbDiagonal, _bbOpponent ) |
							 Bitboards.getLateralAttackers( _iSqKing, bbLateral, _bbOpponent );

			for ( bbPinners &= ~Bitboards.king[ _iSqKing ];
				  bbPinners != 0L;
				  bbPinners ^= (1L << iSq) )
				{
				iSq = BitUtil.first( bbPinners );
				//
				//  Now see if there is one (and only one) moving piece that lies on the path
				//	between a threatening piece (the "pinner") and the King, then it is pinned.
				//	Pinned pieces may still be able to move (except for Knights) but need to
				//	test for check when they do so.
				//
				long bbBetween = _bbPlayer & Bitboards.getSquaresBetween( _iSqKing, iSq );

				if (BitUtil.singleton( bbBetween ))
					_bbUnpinned ^= bbBetween;
				}
			}
		//
		//  The King is being threatened by a single attacker.  The possible evasions are
		//  (1) capture the attacker, (2) move the King, or (3) interpose another piece. If
		//  the attacker is a Knight or Pawn--or is adjacent to the King--then the bitboard
		//  of potential interposition squares (bbXRays) will be zero.
		//
		else if (BitUtil.singleton( _bbCheckers ))
			{
			int iSqChecker = BitUtil.first( _bbCheckers );
			long bbXRays = Bitboards.getSquaresBetween( _iSqKing, iSqChecker );
			//
			//  If the King is being checked by a Knight or Pawn--or the attacker is adjacent
			//  to the King--the only possible "From" squares are the King's square, plus any
			//  of the moving player's pieces that can capture the checking piece.
			//
			if (bbXRays != 0L)
				_bbToSq &= (_bbCheckers | bbXRays | Bitboards.king[ _iSqKing ]);
			else
				{
				_bbFromSq &= _bbEP |
							 (1L << _iSqKing) |
							 Bitboards.getAttackedBy( _map, iSqChecker, _player );
				_bbToSq &= (_bbCheckers | Bitboards.king[ _iSqKing ]);
				}
			}
		//
		//  The King is being threatened by more than one attacker (double check).  The only
		//	possible evasion is to move the King.
		//
		else
			{
			_bbFromSq &= (1L << _iSqKing);
			_bbToSq &= Bitboards.king[ _iSqKing ];
			}
		}

	/**
	 * Tests a move to see if the King is left exposed to check.
	 *
	 * @param iSqFrom
	 * 	"From" square in 8x8 format.
	 * @param iSqTo
	 * 	"To" square in 8x8 format.
	 * @param iMoveType
	 * 	Move type (Move.Type.*)
	 *
	 * @return <code>.T.</code> if move is legal; <code>.F.</code> if king left in check.
	 */
	private boolean testMove( int iSqFrom, int iSqTo, int iMoveType )
		{
		assert Square.isValid( iSqFrom );
		assert Square.isValid( iSqTo );
		//	-----------------------------------------------------------------
		if (iMoveType != Move.Type.EN_PASSANT &&
			_bbCheckers == 0L &&
			iSqFrom != _iSqKing &&
			(_bbUnpinned & (1L << iSqFrom)) != 0L)
			{
			return true;
			}
		//
		//	Now do it the hard way.
		//
		int piece = _board.sq[ iSqFrom ];
		long bbSqFrom = 1L << iSqFrom;
		long bbSqTo = 1L << iSqTo;

		_map[ piece ] ^= bbSqFrom | bbSqTo;
		_map[ _player ] ^= bbSqFrom | bbSqTo;

		switch (iMoveType)
			{
			case Move.Type.CASTLING:
			case Move.Type.PAWN_PUSH:
				break;

			case Move.Type.EN_PASSANT:
				long bbTarget = 1L << ((iSqFrom & 0x38) | (iSqTo & 0x07));

				_map[ _opponent ] ^= bbTarget;
				_map[ MAP_W_PAWN + _opponent ] ^= bbTarget;
				break;

			default:
				if ((piece = _board.sq[ iSqTo ]) != EMPTY)
					{
					_map[ piece ] ^= bbSqTo;
					_map[ _opponent ] ^= bbSqTo;
					}
				break;
			}
		//
		//	See if the King is exposed to check, and reset the _map[] array back to match the
		//	board.
		//
		int iSqKing = (iSqFrom == _iSqKing) ? iSqTo : _iSqKing;
		boolean bInCheck = Bitboards.isAttackedBy( _map, iSqKing, _opponent );

		System.arraycopy( _board.map, 0, _map, 0, MAP_LENGTH );

		return !bInCheck;
		}

	//  -----------------------------------------------------------------------
	//	INTERFACE: Iterable<Move>
	//	-----------------------------------------------------------------------

	@Override
	public Iterator<Move> iterator()
		{
		return new MoveListIterator();
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: MoveListIterator
	//	-----------------------------------------------------------------------

	private class MoveListIterator implements Iterator<Move>
		{
		/** Next element in the _moves[] array. */
		private int _iNext = 0;

		/**
		 * Returns true if the move list has more moves.
		 *
		 * @return <code>.T.</code> if more moves available; <code>.F.</code> otherwise.
		 */
		@Override
		public boolean hasNext()
			{
			return _iNext < _iCount;
			}

		/**
		 * Returns the next move in the move list.
		 *
		 * @return Next move, or <code>null</code> if no more moves available.
		 */
		@Override
		public Move next()
			{
			if (_iNext >= _iCount) throw new java.util.NoSuchElementException();
			//	-------------------------------------------------------------
			return new Move( _moves[ _iNext++ ], _hashZobrist );
			}

		@Override
		public void remove()
			{
			throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
			}
		}   /* end of nested class MoveListIterator */

	}   /* end of class MoveList */
