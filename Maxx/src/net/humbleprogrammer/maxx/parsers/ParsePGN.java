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

import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.maxx.*;

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

    /** Game being populated. */
    private final Game          _gm;
    /** Input string. */
    private final String        _strPGN;
    /** Used for building strings. */
    private final StringBuilder _sb;

    /** Previous offset into input string. */
    private int _index     = 0;
    /** Current offset into input string. */
    private int _indexNext = 0;
    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    /**
     * Default CTOR.
     *
     * @param strPGN
     *     Input string.
     */
    public ParsePGN( String strPGN )
        {
        DBC.requireNotNull( strPGN, "PGN String" );
        /*
        **  CODE
        */
        _gm = GameFactory.createBlank();
        _sb = new StringBuilder();
        _strPGN = strPGN;
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    public static void exportMoves( Game gm, StringBuilder sb )
        {
        DBC.requireNotNull( gm, "Game" );
        DBC.requireNotNull( sb, "StringBuilder" );
        /*
        **  CODE
        */


        }

    public static void exportTags( Game gm, StringBuilder sb )
        {
        DBC.requireNotNull( gm, "Game" );
        DBC.requireNotNull( sb, "StringBuilder" );
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
     * Imports the moves section.
     */
    public void importMoves() throws ParseException
        {

        }

    /**
     * Imports the tags section.
     *
     * <tag-section> ::=    <tag-pair> <tag-section> <empty> <tag-pair> ::= [ <tag-name> <tag-value>
     * ]
     *
     * @return <code>.T.</code> if at least one tag pair imported, <code>.F.</code> otherwise.
     */
    public boolean importTags() throws ParseException
        {
        int ch;
        int iTags;

        for ( iTags = 0; (ch = readNextChar()) == '['; ++iTags )
            {
            _gm.setTag( importTagName(), importTagValue() );

            if (readNextChar() != ']')
                throw new ParseException( "Tag close marker (']') not found.", _index );
            }

        if (ch != 0)
            undoRead();

        return (iTags > 0);
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

    /**
     * Imports a single tag name.
     *
     * <tag-name> ::= <identifier>
     *
     * @return Tag name.
     */
    private String importTagName() throws ParseException
        {
        int iStart = _index;
        int ch = readNextChar();

        if (Character.isLetter( ch ) && Character.isDigit( ch ))
            {
            for ( _sb.setLength( 0 ); ch > ' '; ch = readChar() )
                _sb.appendCodePoint( ch );

            if (!isValidTagName( _sb.toString() ))
                throw new ParseException( "Invalid tag name.", iStart );
            }
        else
            throw new ParseException( "Tag name must start with a capital letter.", iStart );

        return _sb.toString();
        }

    /**
     * <tag-value> ::= '"' ('\\\\' | '\\"' | ~[\\"])* '"'
     *
     * @return Tag value.
     */
    private String importTagValue() throws ParseException
        {
        int iStart = _index;
        int ch = readNextChar();

        if (ch != '"')
            throw new ParseException( "Expected quote at start of tag value.", iStart );

        //  Capture the value, which is everything between the '"' marks.
        for ( _sb.setLength( 0 ); (ch = readChar()) != '"'; _sb.appendCodePoint( ch ) )
            {
            if (ch == '\\')
                ch = readChar();

            if (ch < ' ')
                throw new ParseException( "Invalid tag character.", _index );
            }

        if (!isValidTagValue( _sb.toString() ))
            throw new ParseException( "Invalid tag value.", iStart );

        return _sb.toString();
        }
    //  -----------------------------------------------------------------------
    //	IMPLEMENTATION
    //	-----------------------------------------------------------------------

    /**
     * Reads the next character from the input.
     *
     * @return Input character, or zero if no more characters.
     */
    private int readChar()
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
    private int readNextChar()
        {
        int ch;

        while ( (ch = readChar()) != 0 )
            if (!Character.isWhitespace( ch ))
                return ch;

        return 0;
        }

    /**
     * Pushes the current character back into the stream.
     */
    private void undoRead()
        { _indexNext = _index; }
    }   /* end of nested class ParsePGN */
