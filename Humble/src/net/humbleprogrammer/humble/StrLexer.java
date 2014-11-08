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
package net.humbleprogrammer.humble;

public class StrLexer
    {

    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Input string. */
    private final String _strIn;

    /** Offset to the current character. */
    private int _index;
    /** Current column value, which is one-based. */
    private int _iColumn = 1;
    /** Offset to the next character */
    private int _iNext;

    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    /**
     * Default CTOR.
     *
     * @param strIn
     *     String to analyze.
     */
    public StrLexer( final String strIn )
        {
        _strIn = (strIn != null) ? strIn : "";
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Gets the line containing a given offset.
     *
     * @param iOffset
     *     Offset.
     *
     * @return Line containing the offset.
     */
    public String extractLine( int iOffset )
        {
        int iFirst = 0;
        int iLast = _strIn.length();

        for ( int index = iOffset - 1; index >= 0; --index )
            if (_strIn.charAt( index ) < ' ')
                {
                iFirst = index + 1;
                break;
                }

        for ( int index = iOffset + 1; index < iLast; ++index )
            if (_strIn.charAt( index ) < ' ')
                {
                iLast = index;
                break;
                }

        return (iLast > iFirst)
               ? _strIn.substring( iFirst, iLast )
               : "";
        }

    /**
     * Peeks at the next character in the input stream.
     *
     * @param iOffset
     *     Distance (in characters) before/after current offset.
     *
     * @return Character at offset, or zero if outside the string bounds.
     */
    public int peek( int iOffset )
        {
        iOffset += _index;
        return (iOffset >= 0 && iOffset < _strIn.length())
               ? _strIn.codePointAt( iOffset )
               : 0;
        }

    /**
     * Reads the next character in the input stream.
     *
     * @return Next character, or zero if no more characters.
     */
    public int readChar()
        {
        int ch = 0;
        if ((_index = _iNext) < _strIn.length())
            {
            ch = _strIn.codePointAt( _iNext++ );
            if (Character.isSupplementaryCodePoint( ch ))
                _iNext++;

            if (ch == '\n' || ch == '\r')
                _iColumn = 0;
            else
                _iColumn++;
            }

        return ch;
        }

    /**
     * Reads to the end of the current line.
     *
     * @return Current line, not including the trailing CR/LF.
     */
    public String readLine()
        {
        int ch;
        int iFirst = _index;
        int iLast = _strIn.length();

        while ( (ch = readChar()) != 0 )
            if (ch == '\n' || ch == '\r')
                {
                iLast = _index;
                break;
                }

        return (iLast > iFirst)
               ? _strIn.substring( iFirst, iLast )
               : "";
        }

    /**
     * Reads in the next character, advancing past any/all whitespace.
     */
    public int readNextChar()
        {
        int ch;

        while ( (ch = readChar()) != 0 )
            if (!Character.isWhitespace( ch ))
                return ch;

        return 0;
        }

    /**
     * Reads a string from the input stream.
     *
     * @param strMatch
     *     String to read.
     *
     * @return .T. if string read in; .F. otherwise.
     */
    public boolean readString( final String strMatch )
        {
        if (strMatch == null || strMatch.isEmpty())
            return false;
        /*
        **  CODE
        */
        if (_index < _strIn.length() && _strIn.startsWith( strMatch, _index ))
            {
            _iNext = _index + strMatch.length();
            return true;
            }

        return false;
        }

    /**
     * Pushes the last character or string read back into the input stream.
     */
    public void undoRead()
        {
        _iNext = _index;
        }
    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Test for end of input.
     *
     * @return .T. if at end of the input; .F. if more data available.
     */
    public boolean atEnd()
        {
        return (_iNext >= _strIn.length());
        }

    /**
     * Gets the current column number.
     *
     * @return Column number, which starts at 1.
     */
    public int getColumn()
        {
        return _iColumn;
        }

    /**
     * Gets the offset of the last character read in.
     *
     * @return Offset, or -1 if no characters read in yet.
     */
    public int getOffset()
        {
        return (_index < _iNext) ? _index : -1;
        }
    }   /* end of class StrLexer */
