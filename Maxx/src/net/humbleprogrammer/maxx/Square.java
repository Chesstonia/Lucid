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

import static net.humbleprogrammer.maxx.Constants.INVALID;

public class Square
	{
	//  -----------------------------------------------------------------------
	//	SQUARE CONSTANTS
	//	-----------------------------------------------------------------------
	//
	//		   a    b    c    d    e    f    g    h
	//		+----+----+----+----+----+----+----+----+
	//   8	| 56 | 57 | 58 | 59 | 60 | 61 | 62 | 63 |  8
	//		+----+----+----+----+----+----+----+----+
	//   7	| 48 | 49 | 50 | 51 | 52 | 53 | 54 | 55 |  7
	//		+----+----+----+----+----+----+----+----+
	//   6	| 40 | 41 | 42 | 43 | 44 | 45 | 46 | 47 |  6
	//		+----+----+----+----+----+----+----+----+
	//   5	| 32 | 33 | 34 | 35 | 36 | 37 | 38 | 39 |  5
	//		+----+----+----+----+----+----+----+----+
	//   4	| 24 | 25 | 26 | 27 | 28 | 29 | 30 | 31 |  4
	//		+----+----+----+----+----+----+----+----+
	//   3	| 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |  3
	//		+----+----+----+----+----+----+----+----+
	//   2	| 08 | 09 | 10 | 11 | 12 | 13 | 14 | 15 |  2
	//		+----+----+----+----+----+----+----+----+
	//   1	| 00 | 01 | 02 | 03 | 04 | 05 | 06 | 07 |  1
	//		+----+----+----+----+----+----+----+----+
	//		   a    b    c    d    e    f    g    h

	public static final int	A1				= 0;
	public static final int	B1				= 1;
	public static final int	C1				= 2;
	public static final int	D1				= 3;
	public static final int	E1				= 4;
	public static final int	F1				= 5;
	public static final int	G1				= 6;
	public static final int	H1				= 7;

	public static final int	A2				= 8;
	public static final int	B2				= 9;
	public static final int	C2				= 10;
	public static final int	D2				= 11;
	public static final int	E2				= 12;
	public static final int	F2				= 13;
	public static final int	G2				= 14;
	public static final int	H2				= 15;

	public static final int	A3				= 16;
	public static final int	B3				= 17;
	public static final int	C3				= 18;
	public static final int	D3				= 19;
	public static final int	E3				= 20;
	public static final int	F3				= 21;
	public static final int	G3				= 22;
	public static final int	H3				= 23;

	public static final int	A4				= 24;
	public static final int	B4				= 25;
	public static final int	C4				= 26;
	public static final int	D4				= 27;
	public static final int	E4				= 28;
	public static final int	F4				= 29;
	public static final int	G4				= 30;
	public static final int	H4				= 31;

	public static final int	A5				= 32;
	public static final int	B5				= 33;
	public static final int	C5				= 34;
	public static final int	D5				= 35;
	public static final int	E5				= 36;
	public static final int	F5				= 37;
	public static final int	G5				= 38;
	public static final int	H5				= 39;

	public static final int	A6				= 40;
	public static final int	B6				= 41;
	public static final int	C6				= 42;
	public static final int	D6				= 43;
	public static final int	E6				= 44;
	public static final int	F6				= 45;
	public static final int	G6				= 46;
	public static final int	H6				= 47;

	public static final int	A7				= 48;
	public static final int	B7				= 49;
	public static final int	C7				= 50;
	public static final int	D7				= 51;
	public static final int	E7				= 52;
	public static final int	F7				= 53;
	public static final int	G7				= 54;
	public static final int	H7				= 55;

	public static final int	A8				= 56;
	public static final int	B8				= 57;
	public static final int	C8				= 58;
	public static final int	D8				= 59;
	public static final int	E8				= 60;
	public static final int	F8				= 61;
	public static final int	G8				= 62;
	public static final int	H8				= 63;

	/** Pre-defined square masks, mostly used in castling logic. */

	static final long		A1_MASK			= 1L << A1;
	static final long		B1_MASK			= 1L << B1;
	static final long		C1_MASK			= 1L << C1;
	static final long		D1_MASK			= 1L << D1;
	static final long		E1_MASK			= 1L << E1;
	static final long		F1_MASK			= 1L << F1;
	static final long		G1_MASK			= 1L << G1;
	static final long		H1_MASK			= 1L << H1;

	static final long		A8_MASK			= 1L << A8;
	static final long		B8_MASK			= 1L << B8;
	static final long		C8_MASK			= 1L << C8;
	static final long		D8_MASK			= 1L << D8;
	static final long		E8_MASK			= 1L << E8;
	static final long		F8_MASK			= 1L << F8;
	static final long		G8_MASK			= 1L << G8;
	static final long		H8_MASK			= 1L << H8;

	/** Bitboard where pawns are not allowed (first & last ranks). */
	static final long		NO_PAWN_ZONE	= 0xFF000000000000FFL;
	/** Bitboard where pawns are allowed (2nd - 7th ranks). */
	static final long		PAWN_ZONE		= 0x00FFFFFFFFFFFF00L;

	/** Square masks. */
	static final long[]		mask			= new long[64];

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	static
		{
		for ( int iSq = 0; iSq < 64; ++iSq )
			mask[iSq] = (1L << iSq);
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Returns the distance from one square to another.
	 * 
	 * @param iSqFrom
	 *            "From" square, in 8x8 format.
	 * @param iSqTo
	 *            "To" square, in 8x8 format.
	 * @return Distance.
	 */
	public static int distance( int iSqFrom, int iSqTo )
		{
		if (iSqFrom == iSqTo || ((iSqFrom | iSqTo) & ~0x3F) != 0) return 0;
		//	-----------------------------------------------------------------
		int iFileDist = Math.abs(getFile(iSqFrom) - getFile(iSqTo));
		int iRankDist = Math.abs(getRank(iSqFrom) - getRank(iSqTo));
		//  Chebyshev distance
		return Math.max(iFileDist, iRankDist);
		}

	/**
	 * Imports a square in algebraic format.
	 *
	 * @param strIn
	 *            String to convert.
	 * @return Square index in 8x8 format, or <c>INVALID</c> if str isn't valid.
	 */
	public static int fromString( final String strIn )
		{
		if (strIn == null || strIn.length() < 2) return INVALID;
		//  -----------------------------------------------------------------
		int iFile = Character.toLowerCase(strIn.charAt(0)) - 'a';
		int iRank = strIn.charAt(1) - '1';

		return (iRank << 3) + iFile;
		}

	/**
	 * Imports a square in algebraic format.
	 *
	 * @param strIn
	 *            String to convert.
	 * @param iOffset
	 *            Starting offset within the string.
	 * @return Square index in 8x8 format, or <c>INVALID</c> if str isn't valid.
	 */
	public static int fromString( final String strIn, int iOffset )
		{
		if (strIn == null || iOffset < 0 || (iOffset + 2) > strIn.length()) return INVALID;
		//  -----------------------------------------------------------------
		int iFile = Character.toLowerCase(strIn.charAt(iOffset)) - 'a';
		int iRank = strIn.charAt(iOffset + 1) - '1';

		return toIndex(iRank, iFile);
		}

	/**
	 * Tests a square index to determine if the square is light or dark.
	 *
	 * @param iSq
	 *            Square index in 8x8 format.
	 * @return .T. if square is dark; .F. if light or off the board.
	 */
	public static boolean isDark( final int iSq )
		{
		return ((iSq & ~0x3F) == 0 && ((iSq * 9) & 0x08) == 0);
		}

	/**
	 * Tests a square index for validity.
	 *
	 * @param index
	 *            Square index in 8x8 format.
	 * @return .T. if index is on the board; .F. otherwise.
	 */
	public static boolean isValid( final int index )
		{
		return ((index & ~0x3F) == 0);
		}

	/**
	 * Tests a rank or file for validity.
	 *
	 * @param index
	 *            Rank or file value in 8x8 format.
	 * @return .T. if value is in the range [0..7]; .F. otherwise.
	 */
	public static boolean isValidRankOrFile( final int index )
		{
		return ((index & ~0x07) == 0);
		}

	/**
	 * Converts a rank and file into the Square index.
	 *
	 * @param iRank
	 *            Rank [0..7]
	 * @param iFile
	 *            File [0..7]
	 * @return Square index in 8x8 format, or <c>INVALID</c> if rank or file are
	 *         invalid.
	 */
	public static int toIndex( int iRank, int iFile )
		{
		return (((iRank | iFile) & ~0x07) == 0) ? (iRank << 3) + iFile : INVALID;
		}

	/**
	 * Converts a square to it's inverse.
	 *
	 * @param iSq
	 *            Square.
	 * @return Inverse.
	 */
	public static int toMirror( int iSq )
		{
		return ((iSq & ~0x3F) == 0) ? ((7 - (iSq >>> 3)) << 3) | (iSq & 0x07) : INVALID;
		}

	/**
	 * Exports a square in algebraic format.
	 *
	 * @param iSq
	 *            Square index in 8x8 format.
	 * @return String, or <c>null</c> if index isn't valid.
	 */
	public static String toString( final int iSq )
		{
		if ((iSq & ~0x3F) != 0) return null;
		//  -----------------------------------------------------------------
		return String.format("%c%c", (char) ('a' + (iSq & 0x07)), (char) ('1' + (iSq >>> 3)));
		}

	/**
	 * Exports a square in algebraic format.
	 *
	 * @param iSqFrom
	 *            "From" square index in 8x8 format.
	 * @param iSqTo
	 *            "To" square index in 8x8 format.
	 * @return String, or an empty string if either square isn't valid.
	 */
	public static String toString( int iSqFrom, int iSqTo )
		{
		if (((iSqFrom | iSqTo) & ~0x3F) != 0) return "";
		//  -----------------------------------------------------------------
		return String.format("%c%c%c%c", (char) ('a' + (iSqFrom & 0x07)), (char) ('1' + (iSqFrom >>> 3)),
				(char) ('a' + (iSqTo & 0x07)), (char) ('1' + (iSqTo >>> 3)));
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC GETTERS & SETTERS
	//	-----------------------------------------------------------------------

	/**
	 * Gets the mask for a given square.
	 *
	 * @param iSq
	 *            Square.
	 * @return Square mask.
	 */
	public static long getMask( int iSq )
		{
		return ((iSq & ~0x3F) == 0) ? mask[iSq] : 0L;
		}

	/**
	 * Extracts the file from a square index.
	 *
	 * @param iSq
	 *            Square index in 8x8 format.
	 * @return File [0..7] if square is valid, INVALID otherwise.
	 */
	public static int getFile( final int iSq )
		{
		return ((iSq & ~0x3F) == 0) ? (iSq & 0x07) : INVALID;
		}

	/**
	 * Extracts the rank from a square index.
	 *
	 * @param iSq
	 *            Square index in 8x8 format.
	 * @return Rank [0..7] if square is valid, INVALID otherwise.
	 */
	public static int getRank( final int iSq )
		{
		return ((iSq & ~0x3F) == 0) ? (iSq >>> 3) : INVALID;
		}

	} /* end of class Square */
