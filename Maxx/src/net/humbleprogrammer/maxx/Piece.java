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

import static net.humbleprogrammer.maxx.Constants.*;

public enum Piece
    {
    //  -----------------------------------------------------------------------
    //	PUBLIC DECLARATIONS
    //	-----------------------------------------------------------------------
        /** index = 2 */
        W_PAWN( WHITE, PAWN ),
        /** index = 3 */
        B_PAWN( BLACK, PAWN ),
        /** index = 4 */
        W_KNIGHT( WHITE, KNIGHT ),
        /** index = 5 */
        B_KNIGHT( BLACK, KNIGHT ),
        /** index = 6 */
        W_BISHOP( WHITE, BISHOP ),
        /** index = 7 */
        B_BISHOP( BLACK, BISHOP ),
        /** index = 8 */
        W_ROOK( WHITE, ROOK ),
        /** index = 9 */
        B_ROOK( BLACK, ROOK ),
        /** index = 10 */
        W_QUEEN( WHITE, QUEEN ),
        /** index = 11 */
        B_QUEEN( BLACK, QUEEN ),
        /** index = 12 */
        W_KING( WHITE, KING ),
        /** index = 13 */
        B_KING( BLACK, KING );

    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** The piece color [BLACK|WHITE]. */
    public final int color;
    /** The piece type [PAWN|KNIGHT|BISHOP|ROOK|QUEEN|KING]. */
    public final int type;

    /** The index into the board bitmaps. */
    final int index;

    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    /**
     * CTOR
     *
     * @param pc
     *     Piece color [BLACK|WHITE]
     * @param pt
     *     Piece type [PAWN|KNIGHT|BISHOP|ROOK|QUEEN|KING]
     */
    Piece( final int pc, final int pt )
        {
        assert (pc == WHITE || pc == BLACK);
        assert (pt >= PAWN && pt <= KING);
        /*
        **  CODE
        */
        index = (pt << 1) | pc;

        color = pc;
        type = pt;
        }

    /**
     * Returns a piece of the given type and color.
     *
     * @param pt
     *     Piece type [PAWN..KING]
     * @param pc
     *     Piece color [BLACK|WHITE]
     *
     * @return Piece, or <code>null</code> if either type or color are invalid.
     */
    public static Piece makePiece( int pt, int pc )
        {
        assert pt >= PAWN && pt <= KING;
        assert pc == WHITE || pc == BLACK;
        /*
        **  CODE
        */
        switch (pt)
            {
            case PAWN:
                return (pc == WHITE) ? W_PAWN : B_PAWN;
            case KNIGHT:
                return (pc == WHITE) ? W_KNIGHT : B_KNIGHT;
            case BISHOP:
                return (pc == WHITE) ? W_BISHOP : B_BISHOP;
            case ROOK:
                return (pc == WHITE) ? W_ROOK : B_ROOK;
            case QUEEN:
                return (pc == WHITE) ? W_QUEEN : B_QUEEN;
            case KING:
                return (pc == WHITE) ? W_KING : B_KING;
            }

        return null;
        }
    }   /* end of enum Piece */
