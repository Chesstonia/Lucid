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

import net.humbleprogrammer.humble.BitUtil;
import net.humbleprogrammer.humble.DBC;

import static net.humbleprogrammer.maxx.Constants.*;

class MoveGenerator
	{

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Maximum possible moves in a single position. */
	private static final int MAX_MOVE_COUNT = 224;

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Number of moves in {@link #_moves}. */
	int _iCount;
	/** Current position. */
	final Board _board;
	/** Array of packed moves. */
	final int[] _moves = new int[ MAX_MOVE_COUNT ];

	/** Color of opposing player. */
	private final int  _opponent;
	/** Color of moving player. */
	private final int  _player;
	/** Square occupied by the moving player's King. */
	private final int  _iSqKing;
	/** Bitboard of all pieces. */
	private final long _bbAll;
	/** Bitboard of pieces threatening the moving player's King. */
	private final long _bbCheckers;
	/** Bitboard of opposing pieces. */
	private final long _bbOpponent;
	/** Bitboard of moving pieces. */
	private final long _bbPlayer;
	/** Saved copy of the board maps. */
	private final long[] _map = new long[ MAP_LENGTH ];

	/** Bitboard of all pieces that are NOT pinned. */
	private long _bbPinned;
	/** Bitboard of potential "From" squares. */
	private long _bbSqFrom;
	/** Bitboard of potential "To" squares. */
	private long _bbSqTo;

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
	 * 	if board is null.
	 */
	MoveGenerator( Board bd )
		{
		DBC.requireNotNull( bd, "Board" );
		//	-----------------------------------------------------------------
		_board = bd;
		System.arraycopy( _board.map, 0, _map, 0, MAP_LENGTH );

		_player = _board.getMovingPlayer();
		_opponent = _player ^ 1;

		_bbOpponent = _map[ _opponent ];
		_bbPlayer = _map[ _player ];
		_bbAll = _bbPlayer | _bbOpponent;
		//
		//  Find out if the moving player is in check.
		//
		_iSqKing = BitUtil.first( _map[ MAP_W_KING + _player ] );

		_bbCheckers = Bitboards.getAttackedBy( _map, _iSqKing, _opponent );
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Generates all legal moves.
	 */
	void generateAll()
		{
		initBitboards();
		generate( MAX_MOVE_COUNT );
		}

	/**
	 * Generates the fewest legal moves.
	 *
	 * @return .T. if at least one move available; .F. otherwise.
	 */
	@SuppressWarnings( "WeakerAccess" )
	boolean generateFirst()
		{
		initBitboards();
		generate( 1 );

		return (_iCount > 0);
		}

	/**
	 * Generates a subset of legal moves.
	 *
	 * @param bbFromMask
	 * 	Bitboard of desired "From" squares.
	 * @param bbToMask
	 * 	Bitboard of desired "To" squares.
	 */
	void generateSome( long bbFromMask, long bbToMask )
		{
		initBitboards();

		_bbSqFrom &= bbFromMask;
		_bbSqTo &= bbToMask;

		generate( MAX_MOVE_COUNT );
		//
		//  Remove all moves that don't reach the "From" or "To" squares.  This may be
		//	computationally-intensive, but is usally applied to short move lists.
		//
		for ( int index = 0; index < _iCount; ++index )
			{
			int packed = _moves[ index ];

			if (BitUtil.isSet( bbFromMask, Move.unpackFromSq( packed ) ) &&
				BitUtil.isSet( bbToMask, Move.unpackToSq( packed ) ))
				{
				continue;
				}
			//
			//	Remove the unwanted move by copying the last move on top of the "bad" move.
			//	The current index is decremented so that the next iteration of the loop will
			//	test the copied move, which is now in the same element.
			//
			if (--_iCount > index)
				_moves[ index-- ] = _moves[ _iCount ];
			}
		}

	/**
	 * Checks for a legal mvoe.
	 *
	 * @param bd
	 * 	Position to check.
	 *
	 * @return .T. if at least one legal move found; .F. otherwise.
	 */
	static boolean hasLegalMove( Board bd )
		{
		return (bd != null &&
				new MoveGenerator( bd ).generateFirst());
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
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
		boolean bIsEP = (iMoveType == Move.Type.EN_PASSANT);
		long bbSqFrom = 1L << iSqFrom;

		if (bIsEP ||
			_bbCheckers != 0L ||
			(_bbPinned & bbSqFrom) != 0L)
			{
			int piece = _board.sq[ iSqFrom ];
			long bbSqBoth = bbSqFrom | (1L << iSqTo);

			_map[ piece ] ^= bbSqBoth;
			_map[ _player ] ^= bbSqBoth;
			//
			//	See if this is a capturing move.  We have to remove the victim
			//	from the bitboards, so that it doesn't still generate attacks.
			//	If this is an e.p. capture, adjust the "victim" square to the
			//	pawn being captured (which is different from the "To" square
			//	of the moving pawn).
			//
			int iSqVictim = bIsEP
							? ((iSqFrom & 0x38) | (iSqTo & 0x07))
							: iSqTo;

			if ((piece = _board.sq[ iSqVictim ]) != EMPTY)
				{
				long bbSqVictim = 1L << iSqVictim;

				_map[ piece ] ^= bbSqVictim;
				_map[ _opponent ] ^= bbSqVictim;
				}

			boolean bInCheck = (_player == WHITE)
							   ? Bitboards.isAttackedByBlack( _map, _iSqKing )
							   : Bitboards.isAttackedByWhite( _map, _iSqKing );

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
		for ( long bb = bbFrom & _bbSqFrom; bb != 0L; bb &= (bb - 1) )
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
		for ( long bb = bbTo & _bbSqTo; bb != 0L; bb &= (bb - 1) )
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
	private void addPawnMoves( int iDelta, long bbTo, int iType )
		{
		if ((bbTo &= _bbSqTo) == 0) return;
		//	-----------------------------------------------------------------

		//	Promotion moves/captures
		for ( long bb = bbTo & Square.NO_PAWN_ZONE; bb != 0L; bb &= (bb - 1) )
			{
			int iSqTo = BitUtil.first( bb );
			int iSqFrom = iSqTo + iDelta;

			if (addMoveIfLegal( iSqFrom, iSqTo, Move.Type.PROMOTION ))
				{
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTE_KNIGHT );
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTE_BISHOP );
				_moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, Move.Type.PROMOTE_ROOK );
				}
			}

		//	Normal moves/captures
		for ( long bb = bbTo & Square.PAWN_ZONE; bb != 0L; bb &= (bb - 1) )
			{
			int iSqTo = BitUtil.first( bb );
			int iSqFrom = iSqTo + iDelta;

			addMoveIfLegal( iSqFrom, iSqTo, iType );
			}
		}

	/**
	 * Generate all legal moves.
	 *
	 * @param iMaxMoves
	 * 	Maximum number of moves to generate.
	 */
	private void generate( int iMaxMoves )
		{
		long bbPawns = _map[MAP_W_PAWN + _player];

		_iCount = 0;

		for ( long bb = _bbSqFrom & ~bbPawns; bb != 0L; bb &= (bb - 1L) )
			{
			int iSq = BitUtil.first( bb );

			switch (_board.sq[ iSq ])
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
					addMovesTo( iSq, Bitboards.getSlidingMovesFrom( iSq, _bbAll ) );
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

			if (_iCount >= iMaxMoves)
				return;
			}
		//
		//	Now move all the pawns at once.
		//
		if ((bbPawns &= _bbSqFrom) != 0L)
			{
			if (_player == WHITE)
				generatePawnMovesWhite( bbPawns );
			else
				generatePawnMovesBlack( bbPawns );
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
		assert _board.sq[ iSq ] == MAP_B_KING;
		//	-----------------------------------------------------------------
		long bbKingMoves = _bbSqTo & Bitboards.king[ iSq ];

		if (bbKingMoves != 0L)
			{
			_map[ MAP_B_ALL ] ^= _map[ MAP_B_KING ];

			for ( long bb = bbKingMoves; bb != 0L; bb &= (bb - 1) )
				{
				int iSqTo = BitUtil.first( bb );
				if (!Bitboards.isAttackedByWhite( _map, iSqTo ))
					_moves[ _iCount++ ] = Move.pack( iSq, iSqTo, Move.Type.NORMAL );
				}

			_map[ MAP_B_ALL ] ^= _map[ MAP_B_KING ];
			}
		//
		//	Check for castling moves.
		//
		int castling;

		if (_bbCheckers == 0L &&
			iSq == Square.E8 &&
			(castling = _board.getCastlingFlags()) != 0)
			{
			if ((_bbAll & Board.CastlingFlags.BLACK_SHORT_MASK) == 0 &&
				(castling & Board.CastlingFlags.BLACK_SHORT) != 0 &&
				!Bitboards.isAttackedByWhite( _map, Square.F8 ) &&
				!Bitboards.isAttackedByWhite( _map, Square.G8 ))
				{
				_moves[ _iCount++ ] = Move.BLACK_CASTLE_SHORT; // Black O-O
				}

			if ((_bbAll & Board.CastlingFlags.BLACK_LONG_MASK) == 0 &&
				(castling & Board.CastlingFlags.BLACK_LONG) != 0 &&
				!Bitboards.isAttackedByWhite( _map, Square.D8 ) &&
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
		assert _board.sq[ iSq ] == MAP_W_KING;
		//	-----------------------------------------------------------------
		long bbKingMoves = _bbSqTo & Bitboards.king[ iSq ];

		if (bbKingMoves != 0L)
			{
			_map[ MAP_W_ALL ] ^= _map[ MAP_W_KING ];

			for ( long bb = bbKingMoves; bb != 0L; bb &= (bb - 1) )
				{
				int iSqTo = BitUtil.first( bb );
				if (!Bitboards.isAttackedByBlack( _map, iSqTo ))
					_moves[ _iCount++ ] = Move.pack( iSq, iSqTo, Move.Type.NORMAL );
				}

			_map[ MAP_W_ALL ] ^= _map[ MAP_W_KING ];
			}
		//
		//	Check for castling moves.
		//
		int castling;

		if (_bbCheckers == 0L &&
			iSq == Square.E1 &&
			(castling = _board.getCastlingFlags()) != 0)
			{
			if ((_bbAll & Board.CastlingFlags.WHITE_SHORT_MASK) == 0 &&
				(castling & Board.CastlingFlags.WHITE_SHORT) != 0 &&
				!Bitboards.isAttackedByBlack( _map, Square.F1 ) &&
				!Bitboards.isAttackedByBlack( _map, Square.G1 ))
				{
				_moves[ _iCount++ ] = Move.WHITE_CASTLE_SHORT; // White O-O
				}

			if ((_bbAll & Board.CastlingFlags.WHITE_LONG_MASK) == 0 &&
				(castling & Board.CastlingFlags.WHITE_LONG) != 0 &&
				!Bitboards.isAttackedByBlack( _map, Square.D1 ) &&
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
		addPawnMoves( 7,
					  (((bbPawns & 0x7F7F7F7F7F7F7F7FL) >>> 7) & _bbOpponent),
					  Move.Type.NORMAL );

		//	Captures to the SE.
		addPawnMoves( 9,
					  (((bbPawns & 0xFEFEFEFEFEFEFEFEL) >>> 9) & _bbOpponent),
					  Move.Type.NORMAL );

		//	En passant captures
		int iSqEP = _board.getEnPassantSquare();

		if (Square.isValid( iSqEP ))
			{
			addMovesFrom( (bbPawns & Bitboards.pawnUpwards[ iSqEP ]),
						  iSqEP,
						  Move.Type.EN_PASSANT );
			}

		//	Normal moves
		long bbEmpty = ~_bbAll;
		long bbUnblocked = (bbPawns >>> 8) & bbEmpty;

		if (bbUnblocked != 0L)
			{
			addPawnMoves( 8, bbUnblocked, Move.Type.NORMAL );
			//	Pawn Advances (double moves)
			addPawnMoves( 16,
						  (((bbUnblocked & Bitboards.rankMask[ 5 ]) >>> 8) & bbEmpty),
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
		//	Captures to the NW.
		addPawnMoves( -9,
					  (((bbPawns & 0x7F7F7F7F7F7F7F7FL) << 9) & _bbOpponent),
					  Move.Type.NORMAL );

		//	Captures to the NE.
		addPawnMoves( -7,
					  (((bbPawns & 0xFEFEFEFEFEFEFEFEL) << 7) & _bbOpponent),
					  Move.Type.NORMAL );

		//	En passant captures
		int iSqEP = _board.getEnPassantSquare();

		if (Square.isValid( iSqEP ))
			{
			addMovesFrom( (bbPawns & Bitboards.pawnDownwards[ iSqEP ]),
						  iSqEP,
						  Move.Type.EN_PASSANT );
			}

		//	Normal moves.
		long bbEmpty = ~_bbAll;
		long bbUnblocked = (bbPawns << 8) & bbEmpty;

		if (bbUnblocked != 0L)
			{
			addPawnMoves( -8, bbUnblocked, Move.Type.NORMAL );
			//	Pawn Advances (double moves)
			addPawnMoves( -16,
						  (((bbUnblocked & Bitboards.rankMask[ 2 ]) << 8) & bbEmpty),
						  Move.Type.PAWN_PUSH );
			}
		}

	/**
	 * Initializes the internal bitboards.
	 */
	private void initBitboards()
		{
		if (!Square.isValid( _iSqKing )) return;
		//	-----------------------------------------------------------------
		_bbPinned = 0L;
		_bbSqFrom = _bbPlayer;
		_bbSqTo = ~_bbPlayer;
		//
		//  If the moving player is in check, that affects possible "From" and "To" squares.
		//	If the player is NOT in check, build a bitboard of pinned pieces.
		//
		if (_bbCheckers == 0L)
			{
			final long bbQueen = _map[ MAP_W_QUEEN + _opponent ];
			final long bbBishops = _map[ MAP_W_BISHOP + _opponent ];
			final long bbRooks = _map[ MAP_W_ROOK + _opponent ];
			//
			//  Find pinned pieces.  This is done by finding all of the opposing pieces that
			//  could attack the King if the moving player's pieces were removed.
			//
			long bbPinners =
				Bitboards.getDiagonalAttackers( _iSqKing, (bbQueen | bbBishops), _bbOpponent ) |
				Bitboards.getLateralAttackers( _iSqKing, (bbQueen | bbRooks), _bbOpponent );

			for ( long bb = bbPinners & ~Bitboards.king[ _iSqKing ]; bb != 0L; bb &= (bb - 1) )
				{
				//
				//  If there is one (and only one) moving piece that lies on the path between a
				//	threatening piece (the "pinner") and the King, then it is pinned. Pinned
				//	pieces may still be able to move (except for Knights) but need to test for
				//	check when they do so.
				//
				long bbBetween = _bbPlayer &
								 Bitboards.getSquaresBetween( _iSqKing, BitUtil.first( bb ) );

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
				_bbSqTo &= (_bbCheckers | bbXRays | Bitboards.king[ _iSqKing ]);
			else
				{
				long bbFrom = (1L << _iSqKing) |
							  Bitboards.getAttackedBy( _map, iSqChecker, _player );
				//
				//	See if there are any e.p. captures possible. This is used as
				//	possible evasion moves if the King is in check to handle the edge
				//	case where the only legal move is an e.p. capture.
				//
				int iSqEP = _board.getEnPassantSquare();

				if (Square.isValid( iSqEP ))
					{
					bbFrom |= (_player == WHITE)
							  ? (Bitboards.pawnDownwards[ iSqEP ] & _map[ MAP_W_PAWN ])
							  : (Bitboards.pawnUpwards[ iSqEP ] & _map[ MAP_B_PAWN ]);
					}

				_bbSqFrom &= bbFrom;
				_bbSqTo &= _bbCheckers |
						   Square.getMask( iSqEP ) |
						   Bitboards.king[ _iSqKing ];
				}
			}
		//
		//  The King is being threatened by more than one attacker (double check).  The only
		//	possible evasion is to move the King.
		//
		else
			{
			_bbSqFrom &= Square.getMask(_iSqKing );
			_bbSqTo &= Bitboards.king[ _iSqKing ];
			}
		}

	} /* end of class MoveGenerator */
