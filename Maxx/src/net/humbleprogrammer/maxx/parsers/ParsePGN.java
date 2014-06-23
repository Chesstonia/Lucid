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
package net.humbleprogrammer.maxx.parsers;

import net.humbleprogrammer.humble.StrUtil;
import net.humbleprogrammer.maxx.Game;

import java.text.ParseException;

public class ParsePGN extends Parser
    {

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** The mandatory "Seven Tag Roster". */
    private static final String[] s_strTags = new String[]
        {
            "Event", "Site", "Date", "Round", "White", "Black", "Result"
        };

    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Current offset into input string. */
    private int _indexNext = 0;
    /** Previous offset into input string. */
    private int _index     = 0;

    /** Game being populated. */
    private final Game _gm = new Game();
    /** Input string. */
    private final String _strPGN;
    /** Used for building strings. */
    private final StringBuilder _sb = new StringBuilder();
    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    /**
     * Default CTOR.
     *
     * @param strPGN
     *     Input string.
     */
    private ParsePGN( String strPGN )
        {
        _strPGN = strPGN;
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Converts a PGN string to a Game.
     *
     * <PGN-game> ::= <tag-section> <movetext-section>
     *
     * @param strPGN
     *     String to parse.
     *
     * @return {@link Game} object if parsed, <c>null</c> otherwise.
     */
    public static Game fromString( final String strPGN )
        {
        if (StrUtil.isBlank( strPGN ))
            return null;
        /*
        **  CODE
        */
        s_strError = null;

        try
            {
            ParsePGN parser = new ParsePGN( strPGN );

            // Import tags section
            int iTags = 0;

            while ( parser.importTagPair() )
                iTags++;

            if (iTags <= 0)
                return null;    // didn't find any valid tags?

            }
        catch (ParseException ex)
            {
            s_strError = ex.getMessage();
            s_log.debug( s_strError, ex );
            }

        return null;
        }

    /**
     * Exports a game as a PGN string.
     *
     * @param gm
     *     Game to export.
     *
     * @return PGN string.
     */
    public static String toString( Game gm )
        {
        if (gm == null)
            return null;
        /*
        **  CODE
        */
        StringBuilder sb = new StringBuilder();

        exportTags( gm, sb );
        exportMoves( gm, sb );

        return sb.toString();
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTERS
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
     * @return <code>.T.</code> if valid; <code>.F.</code> otherwise.
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
     * @return <code>.T.</code> if valid; <code>.F.</code> otherwise.
     */
    public static boolean isValidTagValue( String strValue )
        { return (strValue != null && strValue.length() < 256); }

    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    private static void exportMoves( Game gm, StringBuilder sb )
        {
        assert gm != null;
        assert sb != null;
        /*
        **  CODE
        */


        }

    private static void exportTags( Game gm, StringBuilder sb )
        {
        assert gm != null;
        assert sb != null;
        /*
        **  CODE
        */

        for ( String strName : s_strTags )
            {
            String strValue = gm.getTag( strName );

            if (strValue == null)
                strValue = "";

            sb.append( String.format( "[%s \"%s\"]" + STR_CRLF,
                                      strName,
                                      strValue ) );
            }
        //
        //  TODO: export optional tags.
        //
        sb.append( STR_CRLF );  // empty line after tag section
        }

    /**
     * <tag-name> ::= <identifier>
     */
    private String importTagName() throws ParseException
        {
        int ch = getNextNonWhiteChar();

        if (!(Character.isLetter( ch ) && Character.isDigit( ch )))
            {
            throw new ParseException( String.format( "Unexpected '%c'.",
                                                     (char) ch ),
                                      _index
            );
            }

        _sb.setLength( 0 );

        do
            {
            _sb.append( (char) ch );
            }
        while ( (ch = getNextChar()) != 0 && !Character.isWhitespace( ch ) );

        String strName = _sb.toString();
        if (!isValidTagName( strName ))
            {
            throw new ParseException( String.format( "Invalid tag name: '%s'.",
                                                     strName ),
                                      _index
            );
            }

        return strName;
        }

    /**
     * <tag-pair> ::= [ <tag-name> <tag-value> ]
     *
     * @return <code>.T.</code> if tag pair imported, <code>.F.</code> otherwise.
     */
    private boolean importTagPair() throws ParseException
        {
        int ch = getNextNonWhiteChar();

        if (ch != '[')
            {
            ungetChar();
            return false;
            }

        String strName = importTagName();
        String strValue = importTagValue();

        if ((ch = getNextNonWhiteChar()) != ']')
            {
            throw new ParseException( String.format( "Unexpected '%c'.",
                                                     (char) ch ),
                                      _index
            );
            }

        _gm.setTag( strName, strValue );

        return true;
        }

    /**
     * <tag-value> ::= '"' ('\\\\' | '\\"' | ~[\\"])* '"'
     *
     * @return Tag value.
     */
    private String importTagValue() throws ParseException
        {
        int ch = getNextNonWhiteChar();

        if (ch != '"')
            {
            throw new ParseException( String.format( "Unexpected '%c'.",
                                                     (char) ch ),
                                      _index
            );
            }
        //
        //  Capture the value, which is everything between the '"' marks.
        //
        _sb.setLength( 0 );

        while ( (ch = getNextChar()) != 0 && ch != '"' )
            {
            if (ch == '\\')
                ch = getNextChar();

            if (ch == 0 || ch == '\r' || ch == '\n')
                {
                throw new ParseException( "Unexpected EOL.",
                                          _index );
                }

            _sb.append( (char) ch );
            }
        //
        //  Validate the value.
        //
        if (_sb.length() > 255)
            {
            s_log.warn( "Truncating tag value '{}'.", _sb );
            _sb.setLength( 255 );
            }

        return _sb.toString();
        }

    /**
     * Pushes the current character back into the stream.
     */
    private void ungetChar()
        { _indexNext = _index; }
    //  -----------------------------------------------------------------------
    //	GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Reads the next character from the input.
     *
     * @return Input character, or zero if no more characters.
     */
    private int getNextChar()
        {
        int ch = 0;

        if (_indexNext < _strPGN.length())
            {
            _index = _indexNext++;
            ch = _strPGN.codePointAt( _index );

            if (Character.isSupplementaryCodePoint( ch ))
                _indexNext++;
            }

        return ch;
        }

    /**
     * Advances past all whitespace in the input stream.
     *
     * @return Next non-whitespace character.
     */
    private int getNextNonWhiteChar()
        {
        int ch;

        while ( (ch = getNextChar()) != 0 )
            if (!Character.isWhitespace( ch ))
                return ch;

        return 0;
        }
    }   /* end of nested class ParsePGN */
