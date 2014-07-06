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
package net.humbleprogrammer.maxx.pgn;

import net.humbleprogrammer.maxx.Parser;

public class PgnStream extends Parser
    {

    //  -----------------------------------------------------------------------
    //	CONSTANTS
    //	-----------------------------------------------------------------------

    public enum PgnToken
        {
            Annotation,
            Comment,
            Move,
            MoveNumber,
            MovePlaceholder,
            Result,
            TagName,
            TagValue,
            VariationClose,
            VariationOpen
        }

    /** Period, full stop, dot, decimal point, whatever... */
    private static final char DOT_SYM    = '.';
    /** Marks internal escaped lines. */
    private static final char ESCAPE_SYM = '%';
    /** Marks start of Numeric Annotation Glyph (NAG) */
    private static final char NAG_SYM    = '$';
    /** Indeterminate result. */
    private static final char SPLAT_SYM  = '*';
    /** Used to delimit tag values. */
    private static final char QUOTE_SYM  = '"';
    /** Used to escape special characters in tag values. */
    private static final char SLASH_SYM  = '\\';

    /** Comment close marker */
    private static final char COMMENT_CLOSE   = '}';
    /** Comment open marker */
    private static final char COMMENT_OPEN    = '{';
    /** Tag close marker */
    private static final char TAG_CLOSE       = ']';
    /** Tag open marker */
    private static final char TAG_OPEN        = '[';
    /** Variation close marker */
    private static final char VARIATION_CLOSE = ')';
    /** Variation open marker */
    private static final char VARIATION_OPEN  = '(';

    /** Inclusion set for move strings. */
    private static final String MOVE_STR = "abcdefgh12345678BKNQRxO-=+#!?";
    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Length of the input string. */
    private final int    _iLength;
    /** Input string. */
    private final String _strPGN;
    /** Current token. */
    private final StringBuilder _sbToken = new StringBuilder();

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
    public PgnStream( String strPGN )
        {
        _strPGN = (strPGN != null) ? strPGN.trim() : "";
        _iLength = _strPGN.length();
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Reads the next token from the input stream.
     *
     * @return Token type.
     */
    public PgnToken nextToken() throws PgnException
        {
        int ch;

        _sbToken.setLength( 0 );

        while ( (ch = readNextChar()) != 0 )
            {
            //
            //  Moves start with a letter
            //
            if (Character.isLetter( ch ))
                return parseMove( ch );
            //
            //  Move numbers (and some results) start with a digit.
            //
            if (Character.isDigit( ch ))
                return parseNumber( ch );
            //
            //  Everything else starts with a special character.
            //
            switch (ch)
                {
                case DOT_SYM:
                    return parseMovePlaceholder();

                case SPLAT_SYM:
                    _sbToken.append( SPLAT_SYM );
                    return PgnToken.Result;

                case COMMENT_OPEN:
                    return parseComment();

                case ESCAPE_SYM:
                    parseEscapedLine();
                    break;

                case NAG_SYM:
                    return parseAnnotation();

                case QUOTE_SYM:
                    return parseTagValue();

                case TAG_OPEN:
                    return parseTagName();

                case VARIATION_OPEN:
                    return PgnToken.VariationOpen;

                case VARIATION_CLOSE:
                    return PgnToken.VariationClose;

                default:
                    throw new PgnException( _index,
                                            String.format( "Unexpected '%c' character.",
                                                           (char) ch ) );
                }
            }

        return null;
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Tests for end of stream condition.
     *
     * @return <c>true</c> if no more tokens, <c>false</c> otherwise.
     */
    public boolean atEnd()
        { return _indexNext >= _iLength; }

    /**
     * Gets the last token read.
     *
     * @return Token string.
     */
    public String getToken()
        { return _sbToken.toString(); }
    //  -----------------------------------------------------------------------
    //	IMPLEMENTATION
    //	-----------------------------------------------------------------------

    /**
     * Parses an annotation.  The opening '$' is included in the token.
     *
     * @return Always <c>PgnToken.Comment</c>
     */
    private PgnToken parseAnnotation()
        {
        assert peekChar( 0 ) == '$';
        /*
        **  CODE
        */
        int ch;

        while ( (ch = readChar()) != 0 && Character.isDigit( ch ) )
            _sbToken.appendCodePoint( ch );

        if (ch != 0)
            undoRead();

        return PgnToken.Annotation;
        }

    /**
     * Parses an in-line comment.  The comment open/close markers are not included, and all
     * whitespace is compressed to a single space.
     *
     * @return Always <c>PgnToken.Comment</c>
     */
    private PgnToken parseComment()
        {
        boolean bNeedSpace = false;
        int ch;

        while ( (ch = readChar()) != 0 && ch != COMMENT_CLOSE )
            {
            if (ch <= ' ' || Character.isWhitespace( ch ))
                bNeedSpace = (_sbToken.length() > 0);
            else
                {
                if (bNeedSpace)
                    {
                    bNeedSpace = false;
                    _sbToken.append( ' ' );
                    }

                _sbToken.appendCodePoint( ch );
                }
            }

        return PgnToken.Comment;
        }

    /**
     * Parses an escaped line.  The contents are added to the token for diagnostic purposes.
     */
    private void parseEscapedLine() throws PgnException
        {
        int ch;

        //  Only valid if at beginning of line.
        if (_index > 0 && ((ch = peekChar( -1 )) == '\n' || ch == '\r'))
            {
            throw new PgnException( _index,
                                    "Unexpected escape character ('%')." );
            }

        //  Consume the rest of the line until the next CR/LF character.
        while ( (ch = readChar()) != 0 && !(ch == '\n' || ch == '\r') )
            _sbToken.appendCodePoint( ch );
        }

    /**
     * Parses a move out of the stream.
     *
     * @param ch
     *     First character.
     *
     * @return Token type.
     */
    private PgnToken parseMove( int ch )
        {
        assert Character.isLetter( ch );
        /*
        **  CODE
        */
        do
            {
            _sbToken.appendCodePoint( ch );
            }
        while ( (ch = readChar()) != 0 && MOVE_STR.indexOf( ch ) >= 0 );

        return PgnToken.Move;
        }

    /**
     * Parses a move placeholder ("..").
     *
     * @return Always <c>PgnToken.MovePlaceholder</c>.
     */
    private PgnToken parseMovePlaceholder() throws PgnException
        {
        assert peekChar( 0 ) == DOT_SYM;
        /*
        **  CODE
        */
        int ch;

        if ((ch = readNextChar()) != DOT_SYM)
            {
            throw new PgnException( _index,
                                    String.format( "Invalid move place holder: '%c' .",
                                                   (char) ch ) );
            }

        _sbToken.append( ".." );
        return PgnToken.MovePlaceholder;
        }

    /**
     * Parses a number out of the stream.
     *
     * @param ch
     *     First character.
     *
     * @return Token type.
     */
    private PgnToken parseNumber( int ch ) throws PgnException
        {
        assert Character.isDigit( ch );
        /*
        **  CODE
        */
        PgnToken token;

        if ((ch == '0' || ch == '1') && (token = parseResult()) != null)
            return token;

        // Can't start move numbers with a zero.
        if (ch == '0')
            {
            throw new PgnException( _index,
                                    "Move numbers cannot start with a zero." );
            }
        //
        //  Not a result, so build a move number.
        //
        do
            {
            _sbToken.appendCodePoint( ch );
            }
        while ( (ch = readChar()) != 0 && Character.isDigit( ch ) );
        //
        //  Now deal with potential dots after the Move number.  If there's exactly two, then
        //  it must be a move placeholder ('..') and the current dot is pushed back into the
        //  stream.  A count of 1 or 3 means that there was a dot after the move number, so it
        //  is consumed, but not added to the token.
        //
        if (ch == DOT_SYM)
            {
            int iDots = 1;

            for ( int idx = 1; idx < 3; ++idx )
                if (peekChar( idx ) == DOT_SYM)
                    iDots++;
                else
                    break;

            if (iDots == 2)
                undoRead();
            }
        else if (ch != 0)
            undoRead(); // push the character back into the input stream

        return PgnToken.MoveNumber;
        }

    /**
     * Parses a result.
     *
     * @return <c>PgnToken.Result</c> or null if not found.
     */
    private PgnToken parseResult()
        {
        final String[] strResults = { "1-0", "0-1", "1/2-1/2" };

        for ( String str : strResults )
            if (_strPGN.regionMatches( _index, str, 0, str.length() ))
                {
                _sbToken.append( str );
                return PgnToken.Result;
                }

        return null;
        }

    /**
     * Parses a tag name.
     *
     * The tag name includes the "tag open marker" ('['), but the marker is not added to the token
     * string.
     *
     * @return Always <c>PgnToken.TagName</c>.
     */
    private PgnToken parseTagName() throws PgnException
        {
        int ch = readNextChar();

        if (Character.isLetter( ch ) && Character.isUpperCase( ch ))
            {
            do
                {
                _sbToken.appendCodePoint( ch );
                }
            while ( (ch = readChar()) == '_' || Character.isLetterOrDigit( ch ) );
            }
        else
            {
            throw new PgnException( _index,
                                    "Tag names must start with a capital letter." );
            }

        if (!Parser.isValidTagName( _sbToken.toString() ))
            {
            throw new PgnException( _index,
                                    String.format( "Invalid tag name '%s'.",
                                                   _sbToken ) );
            }

        return PgnToken.TagName;
        }

    /**
     * Parses a tag value.
     *
     * The tag value includes the "tag close marker" (']'), but the marker is not added to the token
     * string.
     *
     * @return Always <c>PgnToken.TagValue</c>.
     */
    private PgnToken parseTagValue() throws PgnException
        {
        boolean bEscaped = false;
        int ch;

        while ( (ch = readNextChar()) != 0 )
            {
            if (bEscaped)
                {
                bEscaped = false;
                if (ch == QUOTE_SYM || ch == SLASH_SYM)
                    _sbToken.appendCodePoint( ch );
                else
                    {
                    throw new PgnException( _index,
                                            String.format( "Invalid special character '%c'.",
                                                           (char) ch ) );
                    }
                }
            else if (ch == SLASH_SYM)
                bEscaped = true;
            else if (ch >= ' ' && ch != QUOTE_SYM)
                _sbToken.appendCodePoint( ch );
            else
                break;
            }

        if (_sbToken.length() > 255)
            {
            throw new PgnException( _index,
                                    String.format( "Invalid tag value '%s'.",
                                                   _sbToken ) );
            }

        if (readNextChar() != TAG_CLOSE)
            {
            throw new PgnException( _index,
                                    String.format( "Missing tag close marker ('%c').",
                                                   TAG_CLOSE ) );
            }

        return PgnToken.TagValue;
        }

    /**
     * Looks at a character
     *
     * @param iOffset
     *     Offset from current position: positive to look ahead, negative to look behind.
     *
     * @return Character at offset, or zero if beyond front/back of string.
     */
    private int peekChar( int iOffset )
        {
        iOffset += _index;

        return (iOffset >= 0 && iOffset < _iLength)
               ? _strPGN.codePointAt( iOffset )
               : '\0';
        }

    /**
     * Reads the next character from the input.
     *
     * @return Input character, or zero if no more characters.
     */
    private int readChar()
        {
        int ch = 0;

        if (_indexNext < _iLength)
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

        while ( _indexNext < _iLength )
            {
            _index = _indexNext++;
            ch = _strPGN.codePointAt( _index );
            if (Character.isSupplementaryCodePoint( ch ))
                _indexNext++;

            if (!Character.isWhitespace( ch ))
                return ch;
            }

        return 0;
        }

    /**
     * Pushes the current character back into the stream.
     */
    private void undoRead()
        { _indexNext = _index; }
    }   /* end of nested class PgnStream */
