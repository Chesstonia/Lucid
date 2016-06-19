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

public class Piece
    {
    //  -----------------------------------------------------------------------
    //	PUBLIC DECLARATIONS
    //	-----------------------------------------------------------------------
    /** index = 2 */
    public static final int W_PAWN   = MAP_W_PAWN;
    /** index = 3 */
    public static final int B_PAWN   = MAP_B_PAWN;
    /** index = 4 */
    public static final int W_KNIGHT = MAP_W_KNIGHT;
    /** index = 5 */
    public static final int B_KNIGHT = MAP_B_KNIGHT;
    /** index = 6 */
    public static final int W_BISHOP = MAP_W_BISHOP;
    /** index = 7 */
    public static final int B_BISHOP = MAP_B_BISHOP;
    /** index = 8 */
    public static final int W_ROOK   = MAP_W_ROOK;
    /** index = 9 */
    public static final int B_ROOK   = MAP_B_ROOK;
    /** index = 10 */
    public static final int W_QUEEN  = MAP_W_QUEEN;
    /** index = 11 */
    public static final int B_QUEEN  = MAP_B_QUEEN;
    /** index = 12 */
    public static final int W_KING   = MAP_W_KING;
    /** index = 13 */
    public static final int B_KING   = MAP_B_KING;
    
    /** Lowest piece index */
    public static final int FIRST = W_PAWN;
    /** Highest piece index */
    public static final int LAST = B_KING;

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Returns a piece of the given type and color.
     *
     * @param pc
     *     Piece color [BLACK|WHITE]
     * @param pt
     *     Piece type [PAWN..KING]
     *
     * @return Piece.*, or EMPTY if either type or color are invalid.
     */
    static int create( int pc, int pt )
        {
        return (pt >= PAWN && pt <= KING && (pc == WHITE || pc == BLACK))
               ? ((pt << 1) + pc)
               : EMPTY;
        }

    /**
     * Extracts the piece color from a Piece.* constant.
     *
     * @param piece
     *     Piece.* constant.
     *
     * @return [WHITE|BLACK] if piece is valid; EMPTY otherwise.
     */
    public static int getColor( int piece )
        { return isValid( piece ) ? (piece & 1) : INVALID; }

    /**
     * Extracts the piece color from a Piece.* constant.
     *
     * @param piece
     *     Piece.* constant.
     *
     * @return Piece type (PAWN, KNIGHT, BISHOP, etc.)  if piece is valid; EMPTY otherwise.
     */
    public static int getType( int piece )
        { return isValid( piece ) ? (piece >> 1) : INVALID; }

    /**
     * Tests a piece value for validity.
     *
     * @param piece
     *     Piece.* constant.
     *
     * @return .T. if valid; .F. otherwise.
     */
    public static boolean isValid( int piece )
        { return (piece >= W_PAWN && piece <= B_KING); }

    }   /* end of enum Piece */
