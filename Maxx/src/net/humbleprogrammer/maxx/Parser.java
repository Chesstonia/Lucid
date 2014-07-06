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

import net.humbleprogrammer.humble.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.humbleprogrammer.maxx.Constants.*;

public class Parser
    {

    //  -----------------------------------------------------------------------
    //	CONSTANTS
    //	-----------------------------------------------------------------------

    /** CR/LF seqquence. */
    public static final String STR_CRLF = System.getProperty( "line.separator" );
    /** Placeholder in EPD/FEN strings. */
    public static final String STR_DASH = "-";

    /** Regular expresion for whitespace or end of string. */
    static final String RX_EOS = "(?:\\z|\\s+)";
    /** Regular expresion for an EPD string. */
    static final String RX_EPD =
        // group[1] -- position
        "([BbKkNnQqRr1-8]{1,8}(?:/[BbKkNnPpQqRr1-8]{1,8}){6}/[BbKkNnQqRr1-8]{1,8})" +
        // group[2] -- player
        "\\s+(w|b)" +
        // group[3] -- castling flags
        "\\s+(-|[KkQq]{1,4})" +
        // group[4] -- e.p. square
        "\\s+(-|[a-h][36])";

    /** FEN string pattern. */
    private static final Pattern s_rxFEN = Pattern.compile
        (
            RX_EPD +
            // group[5] -- half move clock
            "\\s+(0|[1-9]\\d*)" +
            // group[6] -- full move clock
            "\\s+([1-9]\\d*)" +
            // End of string or white space
            RX_EOS
        );

    /** Piece glyphs */
    private static final String PIECE_GLYPHS  = "PpNnBbRrQqKk";
    /** Player glyphs */
    private static final String PLAYER_GLYPHS = "wb";

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------


    /** Logger. */
    static final Logger s_log = LoggerFactory.getLogger( "MAXX" );
    /** Last error encountered. */
    static String s_strError;

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Tests a PGN Tag Name for validity.
     *
     * Tag names must start with a capital letter, and contain only letters, digits, or the
     * underscore ('_').  They cannot exceed 255 characters in length.
     *
     * @param strName
     *     Name to test.
     *
     * @return <c>true</c> if valid; <c>false</c> otherwise.
     */
    public static boolean isValidTagName( String strName )
        {
        if (strName == null || strName.isEmpty() || strName.length() > 255)
            return false;
        /*
        **  CODE
        */
        int ch = strName.codePointAt( 0 );

        if (!(Character.isLetter( ch ) && Character.isUpperCase( ch )))
            return false;

        for ( int index = 1; index < strName.length(); ++index )
            {
            if (Character.isSupplementaryCodePoint( ch ))
                index++;

            ch = strName.codePointAt( index );
            if (!(ch == '_' || Character.isLetter( ch ) || Character.isDigit( ch )))
                return false;
            }

        return true;
        }


    /**
     * Tests a PGN Tag Value for validity.
     *
     * Tag values cannot exceed 255 characters in length.
     *
     * @param strValue
     *     Value to test.
     *
     * @return <c>true</c> if valid; <c>false</c> otherwise.
     */
    public static boolean isValidTagValue( String strValue )
        { return (strValue != null && strValue.length() < 256); }

    /**
     * Searches a string for a valid FEN string.
     *
     * @param strFEN
     *     String to search.
     *
     * @return {@link java.util.regex.Matcher} if found; null otherwise.
     */
    public static Matcher matchFEN( final String strFEN )
        {
        if (StrUtil.isBlank( strFEN ))
            return null;
        /*
        **  CODE
        */
        Matcher match = s_rxFEN.matcher( strFEN );

        return match.lookingAt() ? match : null;
        }

    /**
     * Gets the piece represented by a single character.
     *
     * @param ch
     *     Character (code point)
     *
     * @return {@link net.humbleprogrammer.maxx.Piece} if recognized; <c>null</c> otherwise.
     */
    public static Piece pieceFromGlyph( int ch )
        {
        int iPos = PIECE_GLYPHS.indexOf( ch );

        return (iPos >= 0)
               ? Piece.values()[ iPos ]
               : null;
        }

    /**
     * Gets the character representing a piece.
     *
     * @param piece
     *     Piece.
     *
     * @return Character, or zero if piece is invalid.
     */
    public static char pieceToGlyph( Piece piece )
        {
        return (piece != null)
               ? PIECE_GLYPHS.charAt( piece.ordinal() )
               : '\0';
        }

    /**
     * Gets the piece type represented by a single character.
     *
     * @param ch
     *     Character (code point)
     *
     * @return Piece type [PAWN..KING] if recognized; <c>INVALID</c> otherwise.
     */
    @SuppressWarnings( "unused" )
    public static int pieceTypeFromGlyph( int ch )
        {
        int iPos = PIECE_GLYPHS.indexOf( ch );

        return (iPos >= 0)
               ? Piece.values()[ iPos ].type
               : INVALID;
        }

    /**
     * Gets the piece type represented by a single character.
     *
     * @param pt
     *     Piece type [PAWN..KING]
     *
     * @return Character representing the piece.
     */
    public static char pieceTypeToGlyph( int pt )
        {
        return (pt >= PAWN && pt <= KING)
               ? PIECE_GLYPHS.charAt( pt << 1 )
               : 0;
        }

    /**
     * Gets the player represented by a single character.
     *
     * @param ch
     *     Character (code point)
     *
     * @return [WHITE|BLACK] if recognized; <c>INVALID</c> otherwise.
     */
    public static int playerFromGlyph( final int ch )
        {
        int iPos = PLAYER_GLYPHS.indexOf( ch );

        return (iPos >= 0) ? iPos : INVALID;
        }

    /**
     * Gets the glyph for a player color.
     *
     * @param player
     *     Player color [WHITE|BLACK]
     *
     * @return Single character if recognized; zero otherwise.
     */
    public static char playerToGlyph( int player )
        {
        return (player == WHITE || player == BLACK)
               ? PLAYER_GLYPHS.charAt( player )
               : '\0';
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Gets the language setting for the parser.
     *
     * @return Language code.
     */
    @SuppressWarnings("SameReturnValue")
    public static String getLanguage()
        { return "en-US"; }

    }   /* end of class Parser */
