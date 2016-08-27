/* ****************************************************************************
 *
 *	@author Lee Neuse (coder@humbleprogrammer.net)
 *	@since 1.0
 *
 *	---------------------------- [License] ----------------------------------
 *	This work is licensed under the Creative Commons Attribution-NonCommercial-
 *	ShareAlike 3.0 Unported License. To view a copy of this license, visit
 *			http://creativecommons.org/licenses/by-nc-sa/3.0/
 *	or send a letter to Creative Commons, 444 Castro Street Suite 900, Mountain
 *	View, California, 94041, USA.
 *	--------------------- [Disclaimer of Warranty] --------------------------
 *	There is no warranty for the program, to the extent permitted by applicable
 *	law.  Except when otherwise stated in writing the copyright holders and/or
 *	other parties provide the program "as is" without warranty of any kind,
 *	either expressed or implied, including, but not limited to, the implied
 *	warranties of merchantability and fitness for a particular purpose.  The
 *	entire risk as to the quality and performance of the program is with you.
 *	Should the program prove defective, you assume the cost of all necessary
 *	servicing, repair or correction.
 *	-------------------- [Limitation of Liability] --------------------------
 *	In no event unless required by applicable law or agreed to in writing will
 *	any copyright holder, or any other party who modifies and/or conveys the
 *	program as permitted above, be liable to you for damages, including any
 *	general, special, incidental or consequential damages arising out of the
 *	use or inability to use the program (including but not limited to loss of
 *	data or data being rendered inaccurate or losses sustained by you or third
 *	parties or a failure of the program to operate with any other programs),
 *	even if such holder or other party has been advised of the possibility of
 *	such damages.
 *
 ******************************************************************************/
package net.humbleprogrammer.maxx;

import java.util.Iterator;

import net.humbleprogrammer.humble.BitUtil;
import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.maxx.interfaces.IMoveScorer;

import static net.humbleprogrammer.maxx.Constants.*;

@SuppressWarnings( { "WeakerAccess", "unused" } )
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

	/** Color of opposing player. */
	private final int   _opponent;
	/** Color of moving player. */
	private final int   _player;
	/** Square occupied by the moving player's King. */
	private final int   _iSqKing;
	/** Bitboard of all pieces. */
	private final long  _bbAll;
	/** Bitboard of the moving player's King. */
	private final long  _bbKing;
	/** Bitboard of opposing pieces. */
	private final long  _bbOpponent;
	/** Bitboard of moving pieces. */
	private final long  _bbPlayer;
	/** Zobrist hash of board that moves were generated for. */
	private final long  _hashZobrist;
	/** Current position. */
	private final Board _board;
	/** Array of packed moves. */
	private final int[]  _moves = new int[ MAX_MOVE_COUNT ];
	/** Saved copy of the board maps. */
	private final long[] _map   = new long[ MAP_LENGTH ];

	/** .T. if move list is all legal moves; .F. otherwise. */
	private boolean _bAllMoves;
	/** Number of moves in {@link #_moves}. */
	private int     _iCount;
	/** Bitboard of pieces threatening the moving player's King. */
	private long    _bbCheckers;
	/** Bitboard of pawns that can capture via e.p. */
	private long    _bbEP;
	/** Bitboard of potential "From" squares. */
	private long    _bbFromSq;
	/** Bitboard of potential "To" squares. */
	private long    _bbToSq;
	/** Bitboard of all pieces that are NOT pinned. */
	private long    _bbPinned;

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
	private MoveList( Board bd )
		{
		assert bd != null;
		//	-----------------------------------------------------------------
		_bAllMoves = true;
		_board = bd;
		System.arraycopy( _board.map, 0, _map, 0, MAP_LENGTH );

		_player = _board.getMovingPlayer();
		_opponent = _player ^ 1;

		_bbOpponent = _map[ _opponent ];
		_bbPlayer = _map[ _player ];
		_bbAll = _bbPlayer | _bbOpponent;
		_bbKing = _map[ MAP_W_KING + _player ];

		_hashZobrist = _board.getZobristHash();
		_iSqKing = BitUtil.first( _bbKing );

		if (Square.isValid( _iSqKing )) initBitboards();
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Generate all legal moves for a given position.
	 *
	 * @param bd
	 * 	Position to generate moves for.
	 *
	 * @return Move list.
	 */
	public static MoveList generate( Board bd )
		{
		DBC.requireNotNull( bd, "Board" );
		//	-----------------------------------------------------------------
		MoveList moves = new MoveList( bd );

		moves.generate();

		return moves;
		}

	/**
	 * Generate all legal moves from a square for a given position.
	 *
	 * @param bd
	 * 	Position to generate moves for.
	 * @param iSqFrom
	 * 	"From" square, in 8x8 format.
	 *
	 * @return Move list.
	 */
	public static MoveList generateMovesFrom( Board bd, int iSqFrom )
		{
		DBC.requireNotNull( bd, "Board" );
		assert Square.isValid( iSqFrom );
		//	-----------------------------------------------------------------
		MoveList moves = new MoveList( bd );

		moves._bAllMoves = false;
		moves._bbFromSq &= Square.getMask( iSqFrom );
		moves.generate();

		return moves;
		}

	/**
	 * Generate all legal moves for a specific set of pieces and target square.
	 *
	 * @param bd
	 * 	Position.
	 * @param bbFromSq
	 * 	Bitboard of candidate pieces.
	 * @param iToSq
	 * 	Target square, in 8x8 format.
	 *
	 * @return Move list.
	 */
	public static MoveList generateMovesTo( Board bd, long bbFromSq, int iToSq )
		{
		DBC.requireNotNull( bd, "Board" );
		//	-----------------------------------------------------------------
		MoveList moves = new MoveList( bd );

		moves.generate( bbFromSq, iToSq );

		return moves;
		}

	/**
	 * Generate all legal capture moves for a given position.
	 *
	 * @param bd
	 * 	Position to generate captures for.
	 * @param iToSq
	 * 	Square to generate captures for, in 8x8 format.
	 *
	 * @return Move list.
	 */
	public static MoveList generateCaptures( Board bd, int iToSq )
		{
		DBC.requireNotNull( bd, "Board" );
		//	-----------------------------------------------------------------
		MoveList moves = new MoveList( bd );

		moves.generate( moves._bbPlayer, iToSq );

		return moves;
		}

	public static boolean hasLegalMove( Board bd )
		{
		DBC.requireNotNull( bd, "Board" );
		//	-----------------------------------------------------------------
		MoveList moves = new MoveList( bd );

		moves.generate();

		return (moves.size() > 0);
		}

	/**
	 * Tests the move list to see if it is empty, i.e., has no moves.
	 *
	 * @return <code>.T.</code> if empty; <code>.F.</code> if one or more moves present.
	 */
	public boolean isEmpty()
		{
		return (_iCount <= 0);
		}

	/**
	 * Sort all the moves into place based on the score supplied.
	 *
	 * @param scorer
	 * 	Object to provide scores.
	 *
	 * @return Always this
	 */
	public MoveList sort( IMoveScorer scorer )
		{
		DBC.requireNotNull( scorer, "Move Scorer" );

		if (_iCount <= 1) return this;
		//	-----------------------------------------------------------------
		int index = 0;
		int[] scores = new int[ _iCount ];

		for ( Move move : this )
			scores[ index++ ] = scorer.scoreMove( _board, move );
		//
		//	Now do a simple selection sort, using the scores[] array as the
		//	determining factor.
		//
		for ( index = 0; index < _iCount; ++index )
			{
			int best = index;

			for ( int idx = index + 1; idx < _iCount; ++idx )
				if (scores[ idx ] > scores[ best ])
					best = idx;

			if (best == index) continue; // got lucky...no change

			int tmp = scores[ index ];
			scores[ index ] = scores[ best ];
			scores[ best ] = tmp;

			tmp = _moves[ index ];
			_moves[ index ] = _moves[ best ];
			_moves[ best ] = tmp;
			}

		return this;
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
			   ? new Move( _moves[ index ], _hashZobrist )
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
	 * Tests a move to see if the King is left exposed to check. If not, the
	 * move is legal, and is added to the move list.
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
	private boolean addMoveIfLegal( int iSqFrom, int iSqTo, int iMoveType )
		{
		assert Square.isValid( iSqFrom | iSqTo );
		assert iSqFrom != _iSqKing;
		//	-----------------------------------------------------------------
		long bbSqFrom = 1L << iSqFrom;

		if (iMoveType == Move.Type.EN_PASSANT ||
			(_bbPinned & bbSqFrom) != 0L)
			{
			long bbSqBoth = bbSqFrom | (1L << iSqTo);
			//
			//	Move the piece to the new square.  If this is an e.p. capture,
			//	adjust the "To" square to the pawn being captured (which is
			//	different from the final square of the moving pawn).
			//
			int piece = _board.sq[ iSqFrom ];
			int iSqVictim = (iMoveType == Move.Type.EN_PASSANT)
				? ((iSqFrom & 0x38) | (iSqTo & 0x07))
				: iSqTo;

			_map[ piece ] ^= bbSqBoth;
			_map[ _player ] ^= bbSqBoth;
			//
			//	See if this is a capturing move.  We have to remove the victim
			//	from the bitboards, so that it doesn't still generate attacks.
			//
			int victim = _board.sq[ iSqVictim ];

			if (victim != EMPTY)
				{
				long bbSqVictim = 1L << iSqVictim;

				_map[ victim ] ^= bbSqVictim;
				_map[ _opponent ] ^= bbSqVictim;
				}

			boolean bInCheck = Bitboards.isAttackedBy( _map, _iSqKing, _opponent );

			System.arraycopy( _board.map, 0, _map, 0, MAP_LENGTH );

			if (bInCheck) return false;
			}

		_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, iMoveType );

		return true;
		}

	/**
	 * Adds a set of moves to the move list, testing for legality.
	 *
	 * @param bbFrom
	 * 	Bitboard of potential "From" squares.
	 * @param iSqTo
	 * 	"To" square in 8x8 format.
	 * @param iType
	 * 	Type of move (Type.*)
	 */
	private void addMovesFrom( long bbFrom, int iSqTo, int iType )
		{
		for ( long bb = bbFrom & _bbFromSq; bb != 0L; bb &= (bb - 1) )
			addMoveIfLegal( BitUtil.first( bb ), iSqTo, iType );
		}

	/**
	 * Adds a set of moves to the move list, testing for legality.
	 *
	 * @param iSqFrom
	 * 	"From" square in 8x8 format.
	 * @param bbTo
	 * 	Bitboard of potential "To" squares.
	 */
	private void addMovesTo( int iSqFrom, long bbTo )
		{
		for ( long bb = bbTo & _bbToSq; bb != 0L; bb &= (bb - 1) )
			addMoveIfLegal( iSqFrom, BitUtil.first( bb ), Move.Type.NORMAL );
		}

	/**
	 * Adds a set of moves to the move list, testing each one for legality.
	 *
	 * @param iDelta
	 * 	Mathematical distance between "From" and "To" squares.
	 * @param bbTo
	 * 	Bitboard of potential "To" squares.
	 */
	private void addPawnMoves( int iDelta, long bbTo )
		{
		if ((bbTo &= _bbToSq) == 0) return;
		//	-----------------------------------------------------------------
		int iType = (iDelta > -16 && iDelta < 16) ? Move.Type.NORMAL : Move.Type.PAWN_PUSH;

		for ( long bb = bbTo; bb != 0L; bb &= (bb - 1) )
			{
			int iSqTo = BitUtil.first( bb );
			int iSqFrom = iSqTo + iDelta;

			if (iSqTo > Square.H1 && iSqTo < Square.A8)
				addMoveIfLegal( iSqFrom, iSqTo, iType );
			else if (addMoveIfLegal( iSqFrom, iSqTo, Move.Type.PROMOTION ))
				{
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTE_KNIGHT );
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTE_BISHOP );
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTE_ROOK );
				}
			}
		}

	/**
	 * Generate all legal moves.
	 */
	private void generate()
		{
		long bbPawns;
		long bbPieces = _bbFromSq;

		_bAllMoves = true;
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
		//	Generate e.p. captures.  These ignore the "From" and "To"
		//	restrictions because of the edge case where the capture
		//	removes a pawn checking the King.
		//
		if (_bbEP != 0L)
			addMovesFrom( _bbEP, _board.getEnPassantSquare(), Move.Type.EN_PASSANT );
		//
		//	Generate remaining moves.
		//
		for ( long bb = bbPieces & ~bbPawns; bb != 0L; bb &= (bb - 1L) )
			{
			int iSq = BitUtil.first( bb );

			switch (_board.get( iSq ))
				{
				case MAP_W_KNIGHT:
				case MAP_B_KNIGHT:
					addMovesTo( iSq, Bitboards.knight[ iSq ] );
					break;

				case MAP_W_BISHOP:
				case MAP_B_BISHOP:
					addMovesTo( iSq, Bitboards.getDiagonalMovesFrom( iSq, _bbAll ) );
					break;

				case MAP_W_ROOK:
				case MAP_B_ROOK:
					addMovesTo( iSq, Bitboards.getLateralMovesFrom( iSq, _bbAll ) );
					break;

				case MAP_W_QUEEN:
				case MAP_B_QUEEN:
					addMovesTo( iSq, Bitboards.getQueenMovesFrom( iSq, _bbAll ) );
					break;

				case MAP_W_KING:
					generateKingMovesWhite( iSq );
					break;

				case MAP_B_KING:
					generateKingMovesBlack( iSq );
					break;

				default:
					throw new RuntimeException( "Invalid piece type." );
				}
			}
		}

	/**
	 * Generate a subset of legal moves.
	 *
	 * @param bbCandidates
	 * 	Bitboard of "From" pieces.
	 * @param iSqTo
	 * 	"To" square, in 8x8 format.
	 */
	private void generate( long bbCandidates, int iSqTo )
		{
		_bAllMoves = false;
		_bbFromSq &= bbCandidates;
		_bbToSq &= Square.getMask( iSqTo );

		if (_bbFromSq != 0L && _bbToSq != 0L)
			{
			generate();
			//
			//  Take out all moves that don't reach the "To" square.  Do this by copying the last
			//  move on top of the "bad" move.  The current index is decremented so that the next
			//  iteration of the loop will test the copied move, which is now in the same element.
			//
			for ( int index = 0; index < _iCount; ++index )
				if (Move.unpackToSq( _moves[ index ] ) != iSqTo && --_iCount > index)
					_moves[ index-- ] = _moves[ _iCount ];
			}
		}

	/**
	 * Generate all legal moves for a King.
	 *
	 * @param iSq
	 * 	"From" square, in 8x8 format.
	 */
	private void generateKingMovesBlack( int iSq )
		{
		assert Square.isValid( iSq );
		assert Piece.getType( _board.get( iSq ) ) == KING;
		//	-----------------------------------------------------------------
		long bbKingMoves = _bbToSq & Bitboards.king[ iSq ];

		if (bbKingMoves != 0L)
			{
			_map[ MAP_B_ALL ] ^= _bbKing;

			for ( long bb = bbKingMoves; bb != 0L; bb &= (bb - 1) )
				{
				int iSqTo = BitUtil.first( bb );
				if (!Bitboards.isAttackedByWhite( _map, iSqTo ))
					_moves[ _iCount++ ] = Move.pack( iSq, iSqTo, Move.Type.NORMAL );
				}

			_map[ MAP_B_ALL ] ^= _bbKing;
			}
		//
		//	Check for castling moves.
		//
		if (_bbCheckers == 0L && iSq == Square.E8)
			{
			if ((_bbAll & Board.CastlingFlags.BLACK_SHORT_MASK) == 0
				&& (_board.getCastlingFlags() & Board.CastlingFlags.BLACK_SHORT) != 0
				&& !Bitboards.isAttackedByWhite( _map, Square.F8 ) &&
				!Bitboards.isAttackedByWhite( _map, Square.G8 ))
				{
				_moves[ _iCount++ ] = Move.BLACK_CASTLE_SHORT; // Black O-O
				}

			if ((_bbAll & Board.CastlingFlags.BLACK_LONG_MASK) == 0
				&& (_board.getCastlingFlags() & Board.CastlingFlags.BLACK_LONG) != 0
				&& !Bitboards.isAttackedByWhite( _map, Square.D8 ) &&
				!Bitboards.isAttackedByWhite( _map, Square.C8 ))
				{
				_moves[ _iCount++ ] = Move.BLACK_CASTLE_LONG; // Black O-O-O
				}
			}
		}

	/**
	 * Generate all legal moves for a King.
	 *
	 * @param iSq
	 * 	"From" square, in 8x8 format.
	 */
	private void generateKingMovesWhite( int iSq )
		{
		assert Square.isValid( iSq );
		assert Piece.getType( _board.get( iSq ) ) == KING;
		//	-----------------------------------------------------------------
		long bbKingMoves = _bbToSq & Bitboards.king[ iSq ];

		if (bbKingMoves != 0L)
			{
			_map[ MAP_W_ALL ] ^= _bbKing;

			for ( long bb = bbKingMoves; bb != 0L; bb &= (bb - 1) )
				{
				int iSqTo = BitUtil.first( bb );
				if (!Bitboards.isAttackedByBlack( _map, iSqTo ))
					_moves[ _iCount++ ] = Move.pack( iSq, iSqTo, Move.Type.NORMAL );
				}

			_map[ MAP_W_ALL ] ^= _bbKing;
			}
		//
		//	Check for castling moves.
		//
		if (_bbCheckers == 0L && iSq == Square.E1)
			{
			if ((_bbAll & Board.CastlingFlags.WHITE_SHORT_MASK) == 0
				&& (_board.getCastlingFlags() & Board.CastlingFlags.WHITE_SHORT) != 0
				&& !Bitboards.isAttackedByBlack( _map, Square.F1 ) &&
				!Bitboards.isAttackedByBlack( _map, Square.G1 ))
				{
				_moves[ _iCount++ ] = Move.WHITE_CASTLE_SHORT; // White O-O
				}

			if ((_bbAll & Board.CastlingFlags.WHITE_LONG_MASK) == 0
				&& (_board.getCastlingFlags() & Board.CastlingFlags.WHITE_LONG) != 0
				&& !Bitboards.isAttackedByBlack( _map, Square.D1 ) &&
				!Bitboards.isAttackedByBlack( _map, Square.C1 ))
				{
				_moves[ _iCount++ ] = Move.WHITE_CASTLE_LONG; // White O-O-O
				}
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
		addPawnMoves( 7, (((bbPawns & 0x7F7F7F7F7F7F7F7FL) >>> 7) & _bbOpponent) );

		//	Captures to the SE.
		addPawnMoves( 9, (((bbPawns & 0xFEFEFEFEFEFEFEFEL) >>> 9) & _bbOpponent) );

		//	Normal moves.
		long bbUnblocked = (bbPawns >>> 8) & ~_bbAll;

		addPawnMoves( 8, bbUnblocked );

		//	Pawn Advances (double moves)
		if ((bbUnblocked &= Bitboards.rankMask[ 5 ]) != 0)
			addPawnMoves( 16, ((bbUnblocked >>> 8) & ~_bbAll) );
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
		//	Captures to the NW.
		addPawnMoves( -9, (((bbPawns & 0x7F7F7F7F7F7F7F7FL) << 9) & _bbOpponent) );

		//	Captures to the NE.
		addPawnMoves( -7, (((bbPawns & 0xFEFEFEFEFEFEFEFEL) << 7) & _bbOpponent) );

		//	Normal moves.
		long bbUnblocked = (bbPawns << 8) & ~_bbAll;

		addPawnMoves( -8, bbUnblocked );

		//	Pawn Advances (double moves)
		if ((bbUnblocked &= Bitboards.rankMask[ 2 ]) != 0)
			addPawnMoves( -16, ((bbUnblocked << 8) & ~_bbAll) );
		}

	/**
	 * Initializes the internal bitboards.
	 */
	private void initBitboards()
		{
		_bbFromSq = _bbPlayer;
		_bbToSq = ~_bbPlayer;
		_bbPinned = 0;
		//
		//	See if there are any e.p. captures possible.
		//
		int iSqEP = _board.getEnPassantSquare();

		if (Square.isValid( iSqEP ))
			{
			_bbEP = (_player == WHITE) ? (Bitboards.pawnDownwards[ iSqEP ] & _map[ MAP_W_PAWN ])
									   : (Bitboards.pawnUpwards[ iSqEP ] & _map[ MAP_B_PAWN ]);
			}
		//
		//  Find out if the moving player is in check, because that affects the possible "From"
		//	and "To" squares.  If the player is NOT in check, build a bitboard of pinned pieces.
		//
		_bbCheckers = Bitboards.getAttackedBy( _map, _iSqKing, _opponent );

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

			for ( long bb = bbPinners & ~Bitboards.king[ _iSqKing ]; bb != 0L; bb &= (bb - 1) )
				{
				//
				//  Now see if there is one (and only one) moving piece that lies on the path
				//	between a threatening piece (the "pinner") and the King, then it is pinned.
				//	Pinned pieces may still be able to move (except for Knights) but need to
				//	test for check when they do so.
				//
				long bbBetween = _bbPlayer & Bitboards.getSquaresBetween( _iSqKing, BitUtil.first( bb ) );

				if (BitUtil.singleton( bbBetween ))
					_bbPinned |= bbBetween;
				}
			}
		//
		//  The King is being threatened by a single attacker.  The possible evasions are
		//  (1) capture the attacker, (2) move the King, or (3) interpose another piece. If
		//  the attacker is a Knight -- or is adjacent to the King -- then the bitboard of
		//  potential interposition squares (bbXRays) will be zero.
		//
		else if (BitUtil.singleton( _bbCheckers ))
			{
			int iSqChecker = BitUtil.first( _bbCheckers );
			long bbXRays = Bitboards.getSquaresBetween( _iSqKing, iSqChecker );
			//
			//  If the King is being checked by a Knight -- or the attacker is adjacent
			//  to the King--the only possible "From" squares are the King's square,
			//	plus any of the moving player's pieces that can capture the checking piece.
			//
			if (bbXRays != 0L)
				_bbToSq &= (_bbCheckers | bbXRays | Bitboards.king[ _iSqKing ]);
			else
				{
				_bbFromSq &=
					_bbEP | _bbKing | Bitboards.getAttackedBy( _map, iSqChecker, _player );
				_bbToSq &= _bbCheckers | Bitboards.king[ _iSqKing ] | Square.getMask( iSqEP );
				}

			_bbPinned = _bbPlayer;	// treat all pieces as if they're pinned
			}
		//
		//  The King is being threatened by more than one attacker (double check).  The only
		//	possible evasion is to move the King.
		//
		else
			{
			_bbFromSq &= _bbKing;
			_bbToSq &= Bitboards.king[ _iSqKing ];
			_bbPinned = _bbPlayer;	// treat all pieces as if they're pinned
			}
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
		} /* end of nested class MoveListIterator */

	} /* end of class MoveList */
