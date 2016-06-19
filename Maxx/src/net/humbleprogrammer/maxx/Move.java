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

import static net.humbleprogrammer.maxx.Constants.*;

public class Move {
	// -----------------------------------------------------------------------
	// CONSTANTS
	// -----------------------------------------------------------------------

	static final int MASK_FROM_SQ = 0x003F00;
	static final int MASK_TO_SQ = 0x3F0000;
	static final int MASK_TYPE = 0x000007;
	static final int MASK_ALL = MASK_FROM_SQ | MASK_TO_SQ | MASK_TYPE;

	/** Black castling O-O-O */
	static final int BLACK_CASTLE_LONG = pack(Square.E8, Square.C8, Type.CASTLING);
	/** Black castling O-O */
	static final int BLACK_CASTLE_SHORT = pack(Square.E8, Square.G8, Type.CASTLING);
	/** White castling O-O-O */
	static final int WHITE_CASTLE_LONG = pack(Square.E1, Square.C1, Type.CASTLING);
	/** White castling O-O */
	static final int WHITE_CASTLE_SHORT = pack(Square.E1, Square.G1, Type.CASTLING);

	// -----------------------------------------------------------------------
	// DECLARATIONS
	// -----------------------------------------------------------------------

	/** "From" square iLength, in 8x8 format. */
	public final int iSqFrom;
	/** "To" square iLength, in 8x8 format. */
	public final int iSqTo;
	/** Move type */
	public final int iType;

	/** Zobrist hash BEFORE move was made. */
	final long hashBefore;

	// -----------------------------------------------------------------------
	// CTOR
	// -----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 *
	 * @param iPacked
	 *            Packed move.
	 * @param hash
	 *            Zobrist hash of the board prior to the move being made.
	 */
	Move(int iPacked, long hash) {
		hashBefore = hash;

		iSqFrom = (iPacked >>> 8) & 0x3F;
		iSqTo = (iPacked >>> 16) & 0x3F;
		iType = iPacked & MASK_TYPE;
	}

	// -----------------------------------------------------------------------
	// IMPLEMENTATION
	// -----------------------------------------------------------------------

	/**
	 * Packs the "From" square, "To" square, and move type into a 32-bit
	 * integer.
	 *
	 * @param iSqFrom
	 *            "From" square in 8x8 format.
	 * @param iSqTo
	 *            "To" square in 8x8 format.
	 *
	 * @return packed move.
	 */

	static int pack(int iSqFrom, int iSqTo) {
		return ((iSqTo << 16) | (iSqFrom << 8)) & MASK_ALL;
	}

	/**
	 * Packs the "From" square, "To" square, and move type into a 32-bit
	 * integer.
	 *
	 * @param iSqFrom
	 *            "From" square in 8x8 format.
	 * @param iSqTo
	 *            "To" square in 8x8 format.
	 * @param iMoveType
	 *            Move type.
	 *
	 * @return packed move.
	 */
	static int pack(int iSqFrom, int iSqTo, int iMoveType) {
		return ((iSqTo << 16) | (iSqFrom << 8) | iMoveType) & MASK_ALL;
	}

	/**
	 * Unpacks the "From" square from a packed move.
	 *
	 * @param packed
	 *            Packed move.
	 *
	 * @return "From" square, in 8x8 format.
	 */
	static int unpackFromSq(int packed) {
		return (packed >>> 8) & 0x3F;
	}

	/**
	 * Unpacks the "To" square from a packed move.
	 *
	 * @param packed
	 *            Packed move.
	 *
	 * @return "To" square, in 8x8 format.
	 */
	static int unpackToSq(int packed) {
		return (packed >>> 16) & 0x3F;
	}

	// -----------------------------------------------------------------------
	// OVERRIDES
	// -----------------------------------------------------------------------

	public boolean equals(Move move) {
		return (move != null && move.iSqFrom == iSqFrom && move.iSqTo == iSqTo && move.iType == iType);
	}

	@Override
	public boolean equals(Object obj) {
		return ((obj instanceof Board) && equals((Board) obj));
	}

	@Override
	public String toString() {
		String str = Square.toString(iSqFrom) + Square.toString(iSqTo);

		if (iType >= Type.PROMOTION) {
			if (iType == Type.PROMOTION)
				str += Character.toLowerCase(Parser.pieceTypeToGlyph(QUEEN));
			else if (iType == Type.PROMOTE_KNIGHT)
				str += Character.toLowerCase(Parser.pieceTypeToGlyph(KNIGHT));
			else if (iType == Type.PROMOTE_BISHOP)
				str += Character.toLowerCase(Parser.pieceTypeToGlyph(BISHOP));
			else
				str += Character.toLowerCase(Parser.pieceTypeToGlyph(ROOK));
		}

		return str;

	}

	// -----------------------------------------------------------------------
	// NESTED CLASS: Type
	// -----------------------------------------------------------------------

	public static class Type {
		/** Normal move. */
		public static final int NORMAL = 0;
		/** Initial pawn advance. */
		public static final int PAWN_PUSH = 1;
		/** Castling move (O-O or O-O-O). */
		public static final int EN_PASSANT = 2;
		/** En Passant capture. */
		public static final int CASTLING = 3;
		/** Promote to Queen. */
		public static final int PROMOTION = 4;
		/** Under-promote to a Rook. */
		public static final int PROMOTE_ROOK = 5;
		/** Under-promote to a Bishop. */
		public static final int PROMOTE_BISHOP = 6;
		/** Under-promote to a Knight. */
		public static final int PROMOTE_KNIGHT = 7;
	} /* end of nested class Type */

} /* end of class Move */
