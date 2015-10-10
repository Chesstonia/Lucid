/*****************************************************************************
 * * * @author Lee Neuse (coder@humbleprogrammer.net) * @since 1.0 *
 * *	---------------------------- [License] ---------------------------------- *	This work is
 * licensed under the Creative Commons Attribution-NonCommercial- *	ShareAlike 3.0 Unported
 * License. To view a copy of this license, visit *				http://creativecommons.org/licenses/by-nc-sa/3.0/
 * *	or send a letter to Creative Commons, 444 Castro Street Suite 900, Mountain *	View,
 * California, 94041, USA. *	--------------------- [Disclaimer of Warranty]
 * -------------------------- *	There is no warranty for the program, to the extent permitted by
 * applicable *	law.  Except when otherwise stated in writing the copyright holders and/or
 * *	other parties provide the program “as is” without warranty of any kind, *	either expressed
 * or implied, including, but not limited to, the implied *	warranties of merchantability and
 * fitness for a particular purpose.  The *	entire risk as to the quality and performance of the
 * program is with you. *	Should the program prove defective, you assume the cost of all
 * necessary *	servicing, repair or correction. *	-------------------- [Limitation of Liability]
 * -------------------------- *	In no event unless required by applicable law or agreed to in
 * writing will *	any copyright holder, or any other party who modifies and/or conveys the
 * *	program as permitted above, be liable to you for damages, including any *	general, special,
 * incidental or consequential damages arising out of the *	use or inability to use the program
 * (including but not limited to loss of *	data or data being rendered inaccurate or losses
 * sustained by you or third *	parties or a failure of the program to operate with any other
 * programs), *	even if such holder or other party has been advised of the possibility of *	such
 * damages. *
 ******************************************************************************/
package net.humbleprogrammer.maxx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.humble.Stopwatch;

import static net.humbleprogrammer.maxx.Constants.*;

/**
 * Magic Bitboards class.
 *
 * Portions derived from Carballo Chess by Alonso Ruibal. Downloaded from
 * http://sourceforge.net/p/carballo/code/HEAD/tree/core/src/main/java/com/alonsoruibal/chess/
 */
public class Bitboards
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	// Board borders
	private static final long MASK_DOWN  = 0x00000000000000FFL;
	private static final long MASK_UP    = 0xFF00000000000000L;
	private static final long MASK_RIGHT = 0x0101010101010101L;
	private static final long MASK_LEFT  = 0x8080808080808080L;

	// Board borders (2 squares),for the knight
	private static final long MASK_DOWN2X  = 0x000000000000FFFFL;
	private static final long MASK_UP2X    = 0xFFFF000000000000L;
	private static final long MASK_RIGHT2X = 0x0303030303030303L;
	private static final long MASK_LEFT2X  = 0xC0C0C0C0C0C0C0C0L;

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	private static final Logger s_log = LoggerFactory.getLogger( "MAXX" );

	private final static byte rookShiftBits[] =
		{
			12, 11, 11, 11, 11, 11, 11, 12,
			11, 10, 10, 10, 10, 10, 10, 11,
			11, 10, 10, 10, 10, 10, 10, 11,
			11, 10, 10, 10, 10, 10, 10, 11,
			11, 10, 10, 10, 10, 10, 10, 11,
			11, 10, 10, 10, 10, 10, 10, 11,
			11, 10, 10, 10, 10, 10, 10, 11,
			12, 11, 11, 11, 11, 11, 11, 12
		};

	private final static byte bishopShiftBits[] =
		{
			6, 5, 5, 5, 5, 5, 5, 6,
			5, 5, 5, 5, 5, 5, 5, 5,
			5, 5, 7, 7, 7, 7, 5, 5,
			5, 5, 7, 9, 9, 7, 5, 5,
			5, 5, 7, 9, 9, 7, 5, 5,
			5, 5, 7, 7, 7, 7, 5, 5,
			5, 5, 5, 5, 5, 5, 5, 5,
			6, 5, 5, 5, 5, 5, 5, 6
		};

	// Magic numbers generated with MagicNumbersGen
	private final static long rookMagicNumber[] =
		{
			0x1080108000400020L, 0x0040200010004000L, 0x0100082000441100L, 0x0480041000080080L,
			0x0100080005000210L, 0x0100020801000400L, 0x0280010000800200L, 0x0100008020420100L,
			0x0400800080400020L, 0x0000401000402000L, 0x0100801000200080L, 0x0000801000800800L,
			0x0000800400080080L, 0x0000800200800400L, 0x0001000200040100L, 0x4840800041000080L,
			0x0020008080004000L, 0x0000404010002000L, 0x0000808010002000L, 0x0000828010000800L,
			0x0000808004000800L, 0x0014008002000480L, 0x0000040002100801L, 0x0000020001004084L,
			0x0000802080004000L, 0x0000200080400080L, 0x0810001080200080L, 0x0010008080080010L,
			0x4000080080040080L, 0x0000040080020080L, 0x0001000100040200L, 0x0080008200004124L,
			0x0000804000800020L, 0x0000804000802000L, 0x0000801000802000L, 0x2000801000800804L,
			0x0000080080800400L, 0x0080040080800200L, 0x0000800100800200L, 0x0000008042000104L,
			0x0000208040008008L, 0x0010500020004000L, 0x0000100020008080L, 0x2000100008008080L,
			0x0200040008008080L, 0x0008020004008080L, 0x0001000200010004L, 0x0100040080420001L,
			0x0080004000200040L, 0x0000200040100140L, 0x0020004800100040L, 0x0000100080080280L,
			0x8100800400080080L, 0x8004020080040080L, 0x9001000402000100L, 0x0000040080410200L,
			0x0000208040110202L, 0x0800810022004012L, 0x0001000820004011L, 0x0001002004100009L,
			0x0041001002480005L, 0x0081000208040001L, 0x4000008201100804L, 0x0000002841008402L
		};

	private final static long bishopMagicNumber[] =
		{
			0x1020041000484080L, 0x0020204010a0000L, 0x0008020420240000L, 0x0404040085006400L,
			0x0804242000000108L, 0x008901008800000L, 0x0001010110400080L, 0x0000402401084004L,
			0x1000200810208082L, 0x000020802208200L, 0x4200100102082000L, 0x0001024081040020L,
			0x0000020210000000L, 0x000008210400100L, 0x0000010110022000L, 0x0080090088010820L,
			0x0008001002480800L, 0x008102082008200L, 0x0041001000408100L, 0x0088000082004000L,
			0x0204000200940000L, 0x000410201100100L, 0x0002000101012000L, 0x040201008200c200L,
			0x0010100004204200L, 0x002080020010440L, 0x0000480004002400L, 0x0002008008008202L,
			0x0001010080104000L, 0x001020001004106L, 0x0001040200520800L, 0x0008410000840101L,
			0x0001201000200400L, 0x002029000021000L, 0x0004002400080840L, 0x5000020080080080L,
			0x0001080200002200L, 0x004008202028800L, 0x0002080210010080L, 0x0800809200008200L,
			0x0001082004001000L, 0x001080202411080L, 0x0000840048010101L, 0x0040004010400200L,
			0x0500811020800400L, 0x020200040800040L, 0x1008012800830A00L, 0x0001041102001040L,
			0x0011010120200000L, 0x002020222020c00L, 0x0400002402080800L, 0x0000000020880000L,
			0x0000001122020400L, 0x011100248084000L, 0x0210111000908000L, 0x0002048102020080L,
			0x1000108208024000L, 0x001004100882000L, 0x0000000041044100L, 0x0000000000840400L,
			0x0000000004208204L, 0x80000200282020CL, 0x000008A001240100L, 0x0002040104040080L
		};


	static final long[] all           = new long[ 64 ];
	static final long[] bishop        = new long[ 64 ];
	static final long[] knight        = new long[ 64 ];
	static final long[] king          = new long[ 64 ];
	static final long[] pawnDownwards = new long[ 64 ];
	static final long[] pawnUpwards   = new long[ 64 ];
	static final long[] rook          = new long[ 64 ];

	/** Bitboard masks for individual files. */
	static final long[] fileMask = new long[]
		{
			0x0101010101010101L,
			0x0202020202020202L,
			0x0404040404040404L,
			0x0808080808080808L,
			0x1010101010101010L,
			0x2020202020202020L,
			0x4040404040404040L,
			0x8080808080808080L
		};

	/** Bitboard masks for individual ranks. */
	static final long[] rankMask = new long[]
		{
			0x00000000000000FFL,
			0x000000000000FF00L,
			0x0000000000FF0000L,
			0x00000000FF000000L,
			0x000000FF00000000L,
			0x0000FF0000000000L,
			0x00FF000000000000L,
			0xFF00000000000000L
		};

	/** Array of intervening ("between") squares. */
	private static final long[][] between     = new long[ 64 ][ 64 ];
	@SuppressWarnings( "MismatchedReadAndWriteOfArray" )
	private static final long[]   bishopMask  = new long[ 64 ];
	private static final long[][] bishopMagic = new long[ 64 ][];

	@SuppressWarnings( "MismatchedReadAndWriteOfArray" )
	private static final long[]   rookMask  = new long[ 64 ];
	private static final long[][] rookMagic = new long[ 64 ][];

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	static
		{
		int iByteCount = 0;
		Stopwatch swatch = Stopwatch.startNew();

		for ( int iSq = 0; iSq < 64; ++iSq )
			{
			long bbMask = 1L << iSq;

			pawnUpwards[ iSq ] = squareAttacked( bbMask, 7, MASK_UP | MASK_RIGHT ) |
								 squareAttacked( bbMask, 9, MASK_UP | MASK_LEFT );

			pawnDownwards[ iSq ] = squareAttacked( bbMask, -7, MASK_DOWN | MASK_LEFT ) |
								   squareAttacked( bbMask, -9, MASK_DOWN | MASK_RIGHT );

			rook[ iSq ] = squareAttackedSlider( bbMask, +8, MASK_UP ) |
						  squareAttackedSlider( bbMask, -8, MASK_DOWN ) |
						  squareAttackedSlider( bbMask, -1, MASK_RIGHT ) |
						  squareAttackedSlider( bbMask, +1, MASK_LEFT );

			rookMask[ iSq ] = squareAttackedSliderMask( bbMask, +8, MASK_UP ) |
							  squareAttackedSliderMask( bbMask, -8, MASK_DOWN ) |
							  squareAttackedSliderMask( bbMask, -1, MASK_RIGHT ) |
							  squareAttackedSliderMask( bbMask, +1, MASK_LEFT );

			bishop[ iSq ] = squareAttackedSlider( bbMask, +9, MASK_UP | MASK_LEFT ) |
							squareAttackedSlider( bbMask, +7, MASK_UP | MASK_RIGHT ) |
							squareAttackedSlider( bbMask, -7, MASK_DOWN | MASK_LEFT ) |
							squareAttackedSlider( bbMask, -9, MASK_DOWN | MASK_RIGHT );

			bishopMask[ iSq ] = squareAttackedSliderMask( bbMask, +9, MASK_UP | MASK_LEFT ) |
								squareAttackedSliderMask( bbMask, +7, MASK_UP | MASK_RIGHT ) |
								squareAttackedSliderMask( bbMask, -7, MASK_DOWN | MASK_LEFT ) |
								squareAttackedSliderMask( bbMask, -9, MASK_DOWN | MASK_RIGHT );

			knight[ iSq ] = squareAttacked( bbMask, +17, MASK_UP2X | MASK_LEFT ) |
							squareAttacked( bbMask, +15, MASK_UP2X | MASK_RIGHT ) |
							squareAttacked( bbMask, -15, MASK_DOWN2X | MASK_LEFT ) |
							squareAttacked( bbMask, -17, MASK_DOWN2X | MASK_RIGHT ) |
							squareAttacked( bbMask, +10, MASK_UP | MASK_LEFT2X ) |
							squareAttacked( bbMask, +6, MASK_UP | MASK_RIGHT2X ) |
							squareAttacked( bbMask, -6, MASK_DOWN | MASK_LEFT2X ) |
							squareAttacked( bbMask, -10, MASK_DOWN | MASK_RIGHT2X );

			king[ iSq ] = squareAttacked( bbMask, +8, MASK_UP ) |
						  squareAttacked( bbMask, -8, MASK_DOWN ) |
						  squareAttacked( bbMask, -1, MASK_RIGHT ) |
						  squareAttacked( bbMask, +1, MASK_LEFT ) |
						  squareAttacked( bbMask, +9, MASK_UP | MASK_LEFT ) |
						  squareAttacked( bbMask, +7, MASK_UP | MASK_RIGHT ) |
						  squareAttacked( bbMask, -7, MASK_DOWN | MASK_LEFT ) |
						  squareAttacked( bbMask, -9, MASK_DOWN | MASK_RIGHT );

			all[ iSq ] = bishop[ iSq ] | rook[ iSq ] | knight[ iSq ];
			//
			// Bishops & Queens
			//
			int bishopPositions = 1 << bishopShiftBits[ iSq ];

			iByteCount += bishopPositions * 8;
			bishopMagic[ iSq ] = new long[ bishopPositions ];

			for ( int idx = 0; idx < bishopPositions; idx++ )
				{
				long pieces = generatePieces( idx,
											  bishopShiftBits[ iSq ],
											  bishopMask[ iSq ] );
				int index = magicTransform( pieces,
											bishopMagicNumber[ iSq ],
											bishopShiftBits[ iSq ] );
				bishopMagic[ iSq ][ index ] = getBishopShiftAttacks( bbMask, pieces );
				}
			//
			//  Rooks & Queens
			//
			int rookPositions = 1 << rookShiftBits[ iSq ];

			iByteCount += rookPositions * 8;
			rookMagic[ iSq ] = new long[ rookPositions ];

			for ( int idx = 0; idx < rookPositions; ++idx )
				{
				long pieces = generatePieces( idx,
											  rookShiftBits[ iSq ],
											  rookMask[ iSq ] );
				int index = magicTransform( pieces,
											rookMagicNumber[ iSq ],
											rookShiftBits[ iSq ] );
				rookMagic[ iSq ][ index ] = getRookShiftAttacks( bbMask, pieces );
				}
			//
			//  Compute the intervening squares.  Source:
			//  http://chessprogramming.wikispaces.com/Square+Attacked+By#Legality Test-InBetween-Pure Calculation
			//
			for ( int iSqRHS = 0; iSqRHS < 64; ++iSqRHS )
				{
				long bbBetween = (~0L << iSq) ^ (~0L << iSqRHS);
				long bbFile = (iSqRHS & 0x07) - (iSq & 0x07);
				long bbLine = ((bbFile & 0x07) - 1) & 0x0001010101010100L;
				long bbRank = ((iSqRHS | 0x07) - iSq) >>> 3;

				iByteCount += 8;

				bbLine += 2 * (((bbRank & 0x07) - 1) >> 58);
				bbLine += (((bbRank - bbFile) & 0x0F) - 1) & 0x0040201008040200L;
				bbLine += (((bbRank + bbFile) & 0x0F) - 1) & 0x0002040810204080L;
				bbLine *= bbBetween & -bbBetween;

				between[ iSq ][ iSqRHS ] = bbLine & bbBetween;
				}
			}

		swatch.stop();

		iByteCount += (8 * all.length) +
					  (8 * bishop.length) +
					  (8 * bishopMask.length) +
					  (8 * rook.length) +
					  (8 * rookMask.length) +
					  (8 * knight.length) +
					  (8 * king.length) +
					  (8 * pawnDownwards.length) +
					  (8 * pawnUpwards.length);

		if (s_log.isDebugEnabled())
			{
			s_log.debug( String.format( "Generated %,.1fKB attack tables in %s.",
										(iByteCount / 1024.0),
										swatch ) );
			}
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Gets the mask for a given file.
	 *
	 * @param iFile
	 * 	Square.
	 *
	 * @return Rank mask.
	 */
	public static long getFileMask( int iFile )
		{
		return ((iFile & ~0x07) == 0)
			   ? fileMask[ iFile ]
			   : 0L;
		}

	/**
	 * Gets the mask for a given rank.
	 *
	 * @param iRank
	 * 	Square.
	 *
	 * @return Rank mask.
	 */
	public static long getRankMask( int iRank )
		{
		return ((iRank & ~0x07) == 0)
			   ? rankMask[ iRank ]
			   : 0L;
		}

	/**
	 * Gets a bitboard of pieces that attack a given square.
	 *
	 * @param map
	 * 	Array of piece bitboards.
	 * @param iSq
	 * 	Square index, in 8x8 format.
	 * @param player
	 * 	Color of attacking player [WHITE|BLACK].
	 *
	 * @return Bitboard of pieces that attack the square.
	 */
	static long getAttackedBy( long[] map, int iSq, int player )
		{
		assert map != null;
		if ((iSq & ~0x3F) != 0) return 0L;
		//	-----------------------------------------------------------------
		long bbAll = map[ MAP_W_ALL ] | map[ MAP_B_ALL ];
		long bbPawns = (player == WHITE)
					   ? (map[ MAP_W_PAWN ] & pawnDownwards[ iSq ])
					   : (map[ MAP_B_PAWN ] & pawnUpwards[ iSq ]);

		return (bbPawns |
				(king[ iSq ] & map[ MAP_W_KING + player ]) |
				(knight[ iSq ] & map[ MAP_W_KNIGHT + player ]) |
				getDiagonalAttackers( iSq, (map[ MAP_W_BISHOP + player ] |
											map[ MAP_W_QUEEN + player ]), bbAll ) |
				getLateralAttackers( iSq,
									 (map[ MAP_W_ROOK + player ] | map[ MAP_W_QUEEN + player ]),
									 bbAll ));
		}

	/**
	 * Computes all diagonally-reachable squares from a given square.
	 *
	 * @param iSq
	 * 	Origin square, in 8x8 format.
	 * @param bbAttackers
	 * 	Bitboard of potential attackers.
	 * @param bbAll
	 * 	Bitboard of all pieces on the board.
	 *
	 * @return Bitboard of all squares that can reach the origin square.
	 */
	static long getDiagonalAttackers( int iSq, long bbAttackers, long bbAll )
		{
		if ((iSq & ~0x3F) != 0 || (bbAttackers &= bishop[ iSq ]) == 0L) return 0L;
		//	-----------------------------------------------------------------
		int idx = magicTransform( bbAll & bishopMask[ iSq ],
								  bishopMagicNumber[ iSq ],
								  bishopShiftBits[ iSq ] );

		return bbAttackers & bishopMagic[ iSq ][ idx ];
		}

	/**
	 * Computes all diagonally-reachable squares from a given square.
	 *
	 * @param iSq
	 * 	Origin square, in 8x8 format.
	 * @param bbAll
	 * 	Bitboard of all pieces on the board.
	 *
	 * @return Bitboard of all squares that can reach the origin square.
	 */
	static long getDiagonalMovesFrom( int iSq, long bbAll )
		{
		if ((iSq & ~0x3F) != 0) return 0L;
		//	-----------------------------------------------------------------
		int idx = magicTransform( bbAll & bishopMask[ iSq ],
								  bishopMagicNumber[ iSq ],
								  bishopShiftBits[ iSq ] );

		return bishopMagic[ iSq ][ idx ];
		}

	/**
	 * Computes all pieces that attack a square laterally.
	 *
	 * @param iSq
	 * 	Origin square, in 8x8 format.
	 * @param bbAttackers
	 * 	Bitboard of potential attackers.
	 * @param bbAll
	 * 	Bitboard of all pieces on the board.
	 *
	 * @return Bitboard of all squares that can reach the origin square.
	 */
	static long getLateralAttackers( int iSq, long bbAttackers, long bbAll )
		{
		if ((iSq & ~0x3F) != 0 || (bbAttackers &= rook[ iSq ]) == 0L) return 0L;
		//	-----------------------------------------------------------------
		int idx = magicTransform( bbAll & rookMask[ iSq ],
								  rookMagicNumber[ iSq ],
								  rookShiftBits[ iSq ] );

		return bbAttackers & rookMagic[ iSq ][ idx ];
		}

	/**
	 * Computes all laterally-reachable squares from a given square.
	 *
	 * @param iSq
	 * 	Origin square, in 8x8 format.
	 * @param bbAll
	 * 	Bitboard of potential attackers on the board.
	 *
	 * @return Bitboard of all squares that can reach the origin square.
	 */
	static long getLateralMovesFrom( int iSq, long bbAll )
		{
		if ((iSq & ~0x3F) != 0) return 0L;
		//	-----------------------------------------------------------------
		int idx = magicTransform( bbAll & rookMask[ iSq ],
								  rookMagicNumber[ iSq ],
								  rookShiftBits[ iSq ] );
		return rookMagic[ iSq ][ idx ];
		}

	/**
	 * Computes all squares reachable by a queen from a given square.
	 *
	 * @param iSq
	 * 	Origin square, in 8x8 format.
	 * @param bbAll
	 * 	Bitboard of all pieces on the board.
	 *
	 * @return Bitboard of all squares that can reach the origin square.
	 */
	static long getQueenMovesFrom( int iSq, long bbAll )
		{
		if ((iSq & ~0x3F) != 0) return 0L;
		//	-----------------------------------------------------------------
		int iDiagonal = magicTransform( bbAll & bishopMask[ iSq ],
										bishopMagicNumber[ iSq ],
										bishopShiftBits[ iSq ] );
		int iLateral = magicTransform( bbAll & rookMask[ iSq ],
									   rookMagicNumber[ iSq ],
									   rookShiftBits[ iSq ] );

		return bishopMagic[ iSq ][ iDiagonal ] | rookMagic[ iSq ][ iLateral ];
		}

	/**
	 * Finds the squares between to squares on the board.
	 *
	 * @param iSqLHS
	 * 	First square.
	 * @param iSqRHS
	 * 	Second square.
	 *
	 * @return Bitboard of intervening squares.
	 */
	static long getSquaresBetween( int iSqLHS, int iSqRHS )
		{
		return (((iSqLHS | iSqRHS) & ~0x3F) == 0)
			   ? between[ iSqLHS ][ iSqRHS ]
			   : 0L;
		}

	/**
	 * Determines if a square is attacked by a given player.
	 *
	 * @param map
	 * 	Array of piece bitboards.
	 * @param iSq
	 * 	Square index, in 8x8 format.
	 * @param player
	 * 	Attacking player color [WHITE|BLACK].
	 *
	 * @return <c>true</c> if attacked; <c>false</c> otherwise.
	 */
	static boolean isAttackedBy( long[] map, int iSq, int player )
		{
		assert map != null;
		if ((iSq & ~0x3F) != 0 || (all[ iSq ] & map[ player ]) == 0L) return false;
		//	-----------------------------------------------------------------
		if ((king[ iSq ] & map[ MAP_W_QUEEN + player ]) != 0L)
			return true;

		return (player == WHITE)
			   ? isAttackedByWhite( map, iSq )
			   : isAttackedByBlack( map, iSq );
		}

	/**
	 * Determines if a square is attacked by the Black player.
	 *
	 * @param map
	 * 	Array of piece bitboards.
	 * @param iSq
	 * 	Square index, in 8x8 format.
	 *
	 * @return <c>true</c> if attacked; <c>false</c> otherwise.
	 */
	static boolean isAttackedByBlack( long[] map, int iSq )
		{
		assert map != null;
		if ((iSq & ~0x3F) != 0) return false;
		//	-----------------------------------------------------------------
		if ((map[ MAP_B_PAWN ] & pawnUpwards[ iSq ]) != 0L ||
			(knight[ iSq ] & map[ MAP_B_KNIGHT ]) != 0L ||
			(king[ iSq ] & map[ MAP_B_KING ]) != 0L)
			{
			return true;
			}

		final long bbAll = map[ MAP_W_ALL ] | map[ MAP_B_ALL ];

		return (getDiagonalAttackers( iSq, (map[ MAP_B_QUEEN ] | map[ MAP_B_BISHOP ]), bbAll ) != 0L ||
				getLateralAttackers( iSq, (map[ MAP_B_QUEEN ] | map[ MAP_B_ROOK ]), bbAll ) != 0L);
		}

	/**
	 * Determines if a square is attacked by a given player.
	 *
	 * @param map
	 * 	Array of piece bitboards.
	 * @param iSq
	 * 	Square index, in 8x8 format.
	 *
	 * @return <c>true</c> if attacked; <c>false</c> otherwise.
	 */
	static boolean isAttackedByWhite( long[] map, int iSq )
		{
		assert map != null;
		if ((iSq & ~0x3F) != 0) return false;
		//	-----------------------------------------------------------------
		if ((map[ MAP_W_PAWN ] & pawnDownwards[ iSq ]) != 0L ||
			(knight[ iSq ] & map[ MAP_W_KNIGHT ]) != 0L ||
			(king[ iSq ] & map[ MAP_W_KING ]) != 0L)
			{
			return true;
			}

		long bbAll = map[ MAP_W_ALL ] | map[ MAP_B_ALL ];

		return (getDiagonalAttackers( iSq, (map[ MAP_W_QUEEN ] | map[ MAP_W_BISHOP ]), bbAll ) != 0L ||
				getLateralAttackers( iSq, (map[ MAP_W_QUEEN ] | map[ MAP_W_ROOK ]), bbAll ) != 0L);
		}

	//  -----------------------------------------------------------------------
	//	METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Attacks for sliding pieces
	 *
	 * @param bbSqMask
	 * 	Square mask.
	 * @param bbAll
	 * 	Bitboard of all pieces.
	 * @param iShift
	 * 	Shift value.
	 * @param bbBorder
	 * 	Bitboard of border squares.
	 *
	 * @return Bitboard of squares reachable from square.
	 */
	private static long checkSquareAttacked( long bbSqMask, long bbAll, int iShift, long bbBorder )
		{
		assert iShift != 0;
		//	-----------------------------------------------------------------
		long bbResult = 0;

		while ( (bbSqMask & bbBorder) == 0L )
			{
			if (iShift > 0)
				bbSqMask <<= iShift;
			else
				bbSqMask >>>= -iShift;

			bbResult |= bbSqMask;

			if ((bbSqMask & bbAll) != 0L)
				break;    // Stop if we collide with another piece
			}

		return bbResult;
		}

	/**
	 * Fills pieces from a mask. Neccesary for magic generation variable bits is the mask bytes
	 * number index goes from 0 to 2^bits
	 */
	private static long generatePieces( int index, int iBits, long bbMask )
		{
		long bbResult = 0L;

		for ( int idx = 0; idx < iBits; ++idx )
			{
			long bbLSB = bbMask & -bbMask;

			bbMask ^= bbLSB; // Deactivates lsb bit of the mask to get next bit next time

			if ((index & (1 << idx)) != 0)
				bbResult |= bbLSB; // if bit is set to 1
			}

		return bbResult;
		}

	private static int magicTransform( long bb, long magic, int iBits )
		{
		return (int) ((bb * magic) >>> (64 - iBits));
		}

	private static long squareAttacked( long bbSqMask, int iShift, long bbBorder )
		{
		assert iShift != 0;
		//	-----------------------------------------------------------------
		if ((bbSqMask & bbBorder) != 0L)
			return 0L;

		return (iShift > 0)
			   ? (bbSqMask << iShift)
			   : (bbSqMask >>> -iShift);
		}

	private static long squareAttackedSlider( long bbSqMask, int iShift, long bbBorder )
		{
		assert iShift != 0;
		//	-----------------------------------------------------------------
		long bbAttacked = 0L;

		while ( (bbSqMask & bbBorder) == 0L )
			{
			if (iShift > 0)
				bbSqMask <<= iShift;
			else
				bbSqMask >>>= -iShift;

			bbAttacked |= bbSqMask;
			}

		return bbAttacked;
		}

	private static long squareAttackedSliderMask( long bbSqMask, int iShift, long bbBorder )
		{
		assert iShift != 0;
		//	-----------------------------------------------------------------
		long bbAttacked = 0L;

		while ( (bbSqMask & bbBorder) == 0 )
			{
			if (iShift > 0)
				bbSqMask <<= iShift;
			else
				bbSqMask >>>= -iShift;

			if ((bbSqMask & bbBorder) == 0)
				bbAttacked |= bbSqMask;
			}

		return bbAttacked;
		}


	//  -----------------------------------------------------------------------
	//	GETTERS & SETTERS
	//	-----------------------------------------------------------------------

	private static long getRookShiftAttacks( long bbSqMask, long bbAll )
		{
		return checkSquareAttacked( bbSqMask, bbAll, +8, MASK_UP ) |
			   checkSquareAttacked( bbSqMask, bbAll, -8, MASK_DOWN ) |
			   checkSquareAttacked( bbSqMask, bbAll, -1, MASK_RIGHT ) |
			   checkSquareAttacked( bbSqMask, bbAll, +1, MASK_LEFT );
		}

	private static long getBishopShiftAttacks( long bbSqMask, long bbAll )
		{
		return checkSquareAttacked( bbSqMask, bbAll, +9, MASK_UP | MASK_LEFT ) |
			   checkSquareAttacked( bbSqMask, bbAll, +7, MASK_UP | MASK_RIGHT ) |
			   checkSquareAttacked( bbSqMask, bbAll, -7, MASK_DOWN | MASK_LEFT ) |
			   checkSquareAttacked( bbSqMask, bbAll, -9, MASK_DOWN | MASK_RIGHT );
		}

	}   /* end of class Bitboards */
