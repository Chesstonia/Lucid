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

@SuppressWarnings( "WeakerAccess" )
public final class Constants
    {

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Square index/offset that is off the board. */
    public static final int INVALID = -1;
    //
    //	Piece/Player colors
    //
    /** The light side...we have chocolate-chip cookies. */
    public static final int WHITE   = 0;
    /** The dark side...no cookies for you! */
    public static final int BLACK   = 1;
    //
    //	Piece types
    //
    /** Empty square */
    public static final int EMPTY   = 0;
    /** The foot soldier of the chess board. */
    public static final int PAWN    = 1;
    /** Please don't call it a Horsey. */
    public static final int KNIGHT  = 2;
    /** (Insert funny comment here.) */
    public static final int BISHOP  = 3;
    /** A man's rook is his castle? */
    public static final int ROOK    = 4;
    /** She who must be obeyed. */
    public static final int QUEEN   = 5;
    /** It's good to be the King! */
    public static final int KING    = 6;
    //
    //	Score values
    //
	/** Maximum allowable score. */
	public static final int	MAX_SCORE		= 32767;
	/** Minimum allowable score. */
	public static final int	MIN_SCORE		= -32767;
	/** Maximum mate in X depth */
	static final int		MAX_MATE_DEPTH	= 512;
    //
    //  Special Zobrist hash values
    //
    /** Zobrist hash for an empty board. */
    public static final long   HASH_BLANK   = 0L;
    /** Zobrist hash value of the initial (starting) position. */
    public static final long   HASH_INITIAL = 0xBEEDB0B2B9B67995L;
    /** Invalid Zobrist hash value (all ones). */
    public static final long   HASH_INVALID = ~0L;
    //
    //  Elements of the _map[] array
    //
    public static final int MAP_W_ALL    = 0;
    public static final int MAP_B_ALL    = 1;
    public static final int MAP_W_PAWN   = 2;
    public static final int MAP_B_PAWN   = 3;
    public static final int MAP_W_KNIGHT = 4;
    public static final int MAP_B_KNIGHT = 5;
    public static final int MAP_W_BISHOP = 6;
    public static final int MAP_B_BISHOP = 7;
    public static final int MAP_W_ROOK   = 8;
    public static final int MAP_B_ROOK   = 9;
    public static final int MAP_W_QUEEN  = 10;
    public static final int MAP_B_QUEEN  = 11;
    public static final int MAP_W_KING   = 12;
    public static final int MAP_B_KING   = 13;
    public static final int MAP_LENGTH   = 14;

    /** FEN string of blank board. */
    public static final String FEN_BLANK    = "8/8/8/8/8/8/8/8 w - - 0 1";
    /** FEN string of starting position. */
    public static final String FEN_INITIAL  = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    }	/* end of class Constants */
