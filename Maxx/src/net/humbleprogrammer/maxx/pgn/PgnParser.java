/* ****************************************************************************
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
 **	other parties provide the program "as is" without warranty of any kind,
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

import net.humbleprogrammer.humble.*;
import net.humbleprogrammer.maxx.*;
import net.humbleprogrammer.maxx.interfaces.IPgnListener;

import org.slf4j.*;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings( { "PointlessBitwiseExpression", "WeakerAccess" } )
public class PgnParser extends Parser
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	/** PGN Tag name of starting position. */
	public static final String TAG_FEN = "FEN";

	/** Period, full stop, dot, whatever. */
	private static final char SYM_DOT     = '.';
	/** Numeric Annotation Glyph (NAG) */
	private static final char SYM_NAG     = '$';
	/** Marks an escaped line, but only in column 1. */
	private static final char SYM_PERCENT = '%';
	/** Marks an escaped character in a text literal. */
	private static final char SYM_SLASH   = '\\';
	/** Indeterminate result. */
	private static final char SYM_STAR    = '*';

	/** Marks the start of an in-line comment. */
	private static final int COMMENT_BEGIN   = '{';
	/** Marks the end of an in-line comment. */
	private static final int COMMENT_END     = '}';
	/** Marks the start of a tag name/value pair. */
	private static final int TAG_BEGIN       = '[';
	/** Marks the end of a tag name/value pair. */
	private static final int TAG_END         = ']';
	/** Marks the start of a variation. */
	private static final int VARIATION_BEGIN = '(';
	/** Marks the end of a variation. */
	private static final int VARIATION_END   = ')';

	/** Characters allowed in a move suffix. */
	private static final String STR_MOVE_SUFFIX = "!?+#";
	/** Regex to validate tag names. */
	private static final String STR_TAG_NAME    = "^[A-Z]\\w{0,254}$";

	/** Result 0-1 */
	private static final String STR_RESULT_BLACK_WIN = Result.toString( Result.WON_BY_BLACK );
	/** Result 1-0 */
	private static final String STR_RESULT_WHITE_WIN = Result.toString( Result.WON_BY_WHITE );
	/** Result 1/2-1/2 */
	private static final String STR_RESULT_DRAW      = Result.toString( Result.DRAW );
	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** The mandatory "Seven Tag Roster". */
	private static final List<String> s_listTags =
		Arrays.asList( "Event", "Site", "Date", "Round",
					   "White", "Black", "Result" );

	/** Logger */
	private static final Logger s_log = LoggerFactory.getLogger( PgnParser.class );
	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Receives notification of tokens. */
	private final IPgnListener _listener;
	/** Used for building tokens. */
	private final StringBuilder _sb = new StringBuilder();
	/** Used for parsing the input string. */
	private final StrLexer _lexer;

	/** Numer of moves seen since the last move number. */
	private int _iMovesSeen;
	/** Valid token types for the next token. */
	private int _iValidTokens = TT_TAG_PAIR;
	/** Variation flags. */
	private int _iVariations  = TT_VARIATION_BEGIN;
	/** Variation nesting level, or zero for the main line. */
	private int _iVariationDepth;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * CTOR
	 *
	 * @param listener
	 * 	Listener that will receive notifications.
	 * @param strPGN
	 * 	Strng to parse.
	 */
	private PgnParser( IPgnListener listener, final String strPGN )
		{
		assert listener != null;
		assert strPGN != null;
		//  -----------------------------------------------------------------
		_lexer = new StrLexer( strPGN );
		_listener = listener;
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Validates a PGN string.
	 *
	 * @param strPGN
	 * 	String to validate.
	 *
	 * @return .T. if valid PGN; .F. otherwise.
	 */
	public static boolean isValid( final String strPGN )
		{
		return parse( new PgnValidator(), strPGN );
		}

	/**
	 * Tests a tag name for validity.
	 *
	 * @param strName
	 * 	Name to test.
	 *
	 * @return <code>.T.</code> if valid; <code>.F.</code> otherwise.
	 */
	public static boolean isValidTagName( String strName )
		{
		return (strName != null && Pattern.matches( STR_TAG_NAME, strName ));
		}

	/**
	 * Tests a tag value for validity.
	 *
	 * @param strValue
	 * 	Value to test.
	 *
	 * @return <code>.T.</code> if valid; <code>.F.</code> otherwise.
	 */
	public static boolean isValidTagValue( String strValue )
		{
		return (strValue != null && strValue.length() < 256);
		}

	/**
	 * Parses a PGN string.
	 *
	 * @param listener
	 * 	Listener to receive PGN tokens.
	 * @param strPGN
	 * 	String to parse.
	 *
	 * @return .T. if parsed successfully; .F. on error.
	 */
	public static boolean parse( IPgnListener listener, String strPGN )
		{
		DBC.requireNotNull( listener, "PGN Listener" );
		if (StrUtil.isBlank( strPGN )) return false;
		//  -----------------------------------------------------------------
		PgnParser parser = new PgnParser( listener, strPGN );

		try
			{
			listener.onGameStart();

			//noinspection StatementWithEmptyBody
			while ( parser.nextToken() )
				{ /* EMPTY LOOP */ }

			listener.onGameOver();
			}
		catch (ParseException ex)
			{
			s_strError = ex.getMessage();

			s_log.debug( parser.getCurrentLine() );
			s_log.warn( "Parsing failed: {}", s_strError );
			return false;
			}

		return true;
		}

//  -----------------------------------------------------------------------
//	PUBLIC GETTERS & SETTERS
//	-----------------------------------------------------------------------

	public static String getLastError()
		{ return s_strError; }

	/**
	 * Gets the mandatory tags.
	 *
	 * @return Read-only list of tags.
	 */
	public static List<String> getMandatoryTags()
		{
		return Collections.unmodifiableList( s_listTags );
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Gets the current input line.
	 *
	 * @return Input line that contains the current offset.
	 */
	private String getCurrentLine()
		{
		return _lexer.extractLine( _lexer.getOffset() );
		}

	/**
	 * Reads and parses the next token.
	 *
	 * @return .T. if token found; .F. at end of input.
	 *
	 * @throws ParseException
	 * 	if parsing encounters a syntax error.
	 */
	private boolean nextToken() throws ParseException
		{
		if (_iValidTokens == 0 || _lexer.atEnd()) return false;
		//	-------------------------------------------------------------
		int ch;

		_sb.setLength( 0 );

		if ((ch = _lexer.readNextChar()) == 0) return false;

		// Moves all start with a letter.
		if (Character.isLetter( ch ))
			{
			parseMove();
			return true;
			}

		// Digits are either a move number, or a result.
		if (Character.isDigit( ch ))
			{
			if (ch <= '1' && parseResult()) return (_iVariationDepth > 0);

			parseMoveNumber();
			return true;
			}

		//  Handle everything else
		switch (ch)
			{
			case COMMENT_BEGIN:
				parseComment();
				return true;

			case SYM_DASH:
				parseNullMove();
				return true;

			case SYM_DOT:
				parseMovePlaceholder();
				return true;

			case SYM_NAG:
				parseAnnotation();
				return true;

			case SYM_PERCENT:
				if (_lexer.getColumn() == 1)
					{
					_sb.append( _lexer.readLine() );
					return true;
					}
				break;

			case SYM_STAR:
				parseResult();
				return (_iVariationDepth > 0);

			case TAG_BEGIN:
				parseTag();
				return true;

			case VARIATION_BEGIN:
				parseVariationBegin();
				return true;

			case VARIATION_END:
				parseVariationEnd();
				return true;
			}
		//
		//  If we get this far, then we didn't recognize the character.
		//
		String strWhat = String.format( "Unexpected character '%c' in column %,d", (char) ch,
										_lexer.getColumn() );
		throw new ParseException( strWhat, _lexer.getOffset() );
		}

	/**
	 * Parses a "Numeric Annotation Glyph" (NAG)
	 */
	private void parseAnnotation() throws ParseException
		{
		assert _lexer.peek( 0 ) == SYM_NAG;

		if ((_iValidTokens & TT_ANNOTATION) == 0)
			throw new ParseException( "Unexpected annotation.", _lexer.getOffset() );
		//	-------------------------------------------------------------
		int ch = _lexer.peek( 0 );

		do
			{
			_sb.appendCodePoint( ch );
			}
		while ( (ch = _lexer.readChar()) != 0 && Character.isDigit( ch ) );

		if (!(ch == 0 || Character.isWhitespace( ch ))) _lexer.undoRead();
		//
		//  Pass the NAG to the listener
		//
		_listener.onAnnotation( _sb.toString() );
		}

	/**
	 * Parses a comment.
	 */
	private void parseComment() throws ParseException
		{
		assert _lexer.peek( 0 ) == COMMENT_BEGIN;

		if ((_iValidTokens & TT_COMMENT) == 0)
			throw new ParseException( "Unexpected comment.", _lexer.getOffset() );
		//	-------------------------------------------------------------
		boolean bNeedSpace = false;
		int ch;

		while ( (ch = _lexer.readChar()) != 0 && ch != COMMENT_END )
			if (Character.isWhitespace( ch ))
				bNeedSpace = (_sb.length() > 0);
			else if (bNeedSpace)
				{
				bNeedSpace = false;
				_sb.append( ' ' );
				_sb.appendCodePoint( ch );
				}
			else
				_sb.appendCodePoint( ch );
		//
		//  Pass the comment to the listener.
		//
		_listener.onComment( _sb.toString() );

		_iValidTokens = _iVariations | TT_COMMENT | TT_MOVE | TT_MOVE_NUMBER | TT_RESULT;
		}

	/**
	 * Parses a move, including the suffix.
	 */
	private void parseMove() throws ParseException
		{
		assert Character.isLetter( _lexer.peek( 0 ) );
		//	-------------------------------------------------------------
		final int iMoveCol = _lexer.getColumn();

		int ch;
		String strSuffix;

		for ( ch = _lexer.peek( 0 ); STR_MOVE.indexOf( ch ) >= 0; ch = _lexer.readChar() )
			_sb.appendCodePoint( ch );
		//
		//  Build the move suffix, if any. These are typically ver short (2-3 characters
		//  at most) so any performance hit from string concantenation is acceptable.
		//
		for ( strSuffix = ""; STR_MOVE_SUFFIX.indexOf( ch ) >= 0; ch = _lexer.readChar() )
			strSuffix += (char) ch;

		if (!(ch == 0 || Character.isWhitespace( ch ))) _lexer.undoRead();
		//
		//  Pass the move to the listener.
		//
		if ((_iValidTokens & TT_MOVE) != 0 && _listener.onMove( _sb.toString(), strSuffix ))
			{
			_iMovesSeen++;
			_iValidTokens = _iVariations | TT_ANNOTATION | TT_COMMENT | TT_RESULT
							| ((_iMovesSeen == 1) ? TT_MOVE : TT_MOVE_NUMBER);
			}
		else
			{
			String strWhat =
				String.format( "Invalid move '%s' in column %,d.", _sb.toString(), iMoveCol );

			throw new ParseException( strWhat, _lexer.getOffset() );
			}
		}

	/**
	 * Parses a move number. S single trailing dot is consumed, but not added to
	 * the token.
	 */
	private void parseMoveNumber() throws ParseException
		{
		assert Character.isDigit( _lexer.peek( 0 ) );

		if ((_iValidTokens & TT_MOVE_NUMBER) == 0)
			throw new ParseException( "Unexpected move number.", _lexer.getOffset() );
		//	-------------------------------------------------------------
		int ch;
		int iValue = 0;

		for ( ch = _lexer.peek( 0 ); Character.isDigit( ch ); ch = _lexer.readChar() )
			{
			_sb.appendCodePoint( ch );
			iValue = (iValue * 10) + (ch - '0');
			}

		if (ch == SYM_DOT)
			{
			int iDots;

			for ( iDots = 0; iDots < 3; ++iDots )
				if (_lexer.peek( iDots ) != SYM_DOT) break;

			if (iDots == 2) _lexer.undoRead();
			}
		else if (!(ch == 0 || Character.isWhitespace( ch ))) _lexer.undoRead();
		//
		//  Pass the move number to the listener
		//
		if (iValue > 0 && _listener.onMoveNumber( iValue ))
			{
			_iMovesSeen = 0;
			_iValidTokens = TT_MOVE | TT_MOVE_PLACEHOLDER;
			}
		else
			{
			String strWhat =
				String.format( "Invalid move number '%s' in column %,d.", _sb.toString(),
							   (_lexer.getColumn() - _sb.length()) );
			throw new ParseException( strWhat, _lexer.getOffset() );
			}
		}

	/**
	 * Parses a move placeholder.
	 */
	private void parseMovePlaceholder() throws ParseException
		{
		assert _lexer.peek( 0 ) == SYM_DOT;
		//	-------------------------------------------------------------
		if (_lexer.readChar() != SYM_DOT)
			{
			String strWhat =
				String.format( "Unexpected '.' in column %,d.", _lexer.getColumn() );
			throw new ParseException( strWhat, _lexer.getOffset() );
			}
		//
		//  Inform the listener.
		//
		if ((_iValidTokens & TT_MOVE_PLACEHOLDER) != 0 && _listener.onMovePlaceholder())
			{
			_iMovesSeen++;
			_iValidTokens = TT_MOVE;
			}
		else
			throw new ParseException( "Unexpected move placeholder.", _lexer.getOffset() );
		}

	/**
	 * Parses a null Move.
	 */
	private void parseNullMove() throws ParseException
		{
		assert _lexer.peek( 0 ) == SYM_DASH;
		//	-------------------------------------------------------------
		if (_lexer.readChar() != SYM_DASH)
			{
			String strWhat =
				String.format( "Unexpected '-' in column %,d.", _lexer.getColumn() );
			throw new ParseException( strWhat, _lexer.getOffset() );
			}
		//
		//  Inform the listener.
		//
		if ((_iValidTokens & TT_MOVE) != 0 && _listener.onNullMove())
			{
			_iMovesSeen++;
			_iValidTokens = _iVariations | TT_ANNOTATION | TT_COMMENT | TT_RESULT
							| ((_iMovesSeen == 1) ? TT_MOVE : TT_MOVE_NUMBER);
			}
		else
			throw new ParseException( "Unexpected null move.", _lexer.getOffset() );
		}

	/**
	 * Parses a result.
	 */
	private boolean parseResult() throws ParseException
		{
		final int ch = _lexer.peek( 0 );
		final int cNext = _lexer.peek( +1 );
		Result result = null;

		if (ch == SYM_STAR)
			result = Result.INDETERMINATE;
		else if (cNext == '-')
			{
			if (_lexer.readString( STR_RESULT_BLACK_WIN ))
				result = Result.WON_BY_BLACK;
			else if (_lexer.readString( STR_RESULT_WHITE_WIN )) result = Result.WON_BY_WHITE;
			}
		else if (cNext == '/')
			{
			if (_lexer.readString( STR_RESULT_DRAW )) result = Result.DRAW;
			}

		if (result == null) return false;

		if ((_iValidTokens & TT_RESULT) != 0 && _listener.onResult( result ))
			{
			_iValidTokens = (_iVariationDepth > 0) ? TT_VARIATION_END : 0;
			}
		else
			throw new ParseException( "Unexpected result.", _lexer.getOffset() );

		return true;
		}

	/**
	 * Parses a tag name/value pair.
	 */
	private void parseTag() throws ParseException
		{
		assert _lexer.peek( 0 ) == TAG_BEGIN;
		//	-------------------------------------------------------------
		if ((_iValidTokens & TT_TAG_PAIR) != 0 &&
			_listener.onTag( parseTagName(), parseTagValue() ))
			{
			_iValidTokens = TT_COMMENT | TT_TAG_PAIR | TT_MOVE_NUMBER | TT_RESULT;
			}
		else
			throw new ParseException( "Invalid tag marker.", _lexer.getOffset() );
		}

	/**
	 * Parses a tag name.
	 *
	 * @return Tag name.
	 */
	private String parseTagName() throws ParseException
		{
		for ( int ch = _lexer.readNextChar(); ch != 0; ch = _lexer.readChar() )
			if (Character.isLetterOrDigit( ch ) || ch == '_')
				_sb.appendCodePoint( ch );
			else
				break;
		//
		//  Validate the name.
		//
		final String strName = _sb.toString();

		if (isValidTagName( strName )) return strName;

		String strWhat = String.format( "Invalid tag name '%s'.", _sb.toString() );
		throw new ParseException( strWhat, _lexer.getOffset() - strName.length() );
		}

	/**
	 * Parses a tag value.
	 *
	 * @return Tag value.
	 */
	private String parseTagValue() throws ParseException
		{
		if (_lexer.readNextChar() != SYM_QUOTE)
			throw new ParseException( "Tag values must be in quotes.", _lexer.getOffset() );
		//	-------------------------------------------------------------
		boolean bEscaped = false;
		int ch;

		_sb.setLength( 0 );

		while ( (ch = _lexer.readChar()) != 0 )
			{
			if (bEscaped)
				{
				bEscaped = false;
				if (ch == SYM_QUOTE || ch == SYM_SLASH)
					_sb.appendCodePoint( ch );
				else
					{
					_sb.append( SYM_SLASH );
					_sb.appendCodePoint( ch );
					}
				}
			else if (ch == SYM_SLASH)
				bEscaped = true;
			else if (ch != SYM_QUOTE && Parser.STR_CRLF.indexOf( ch ) < 0)
				{
				_sb.appendCodePoint( ch );
				}
			else
				break;
			}

		if (_lexer.readNextChar() != TAG_END)
			throw new ParseException( "Tag close marker ']' not found.", _lexer.getOffset() );
		//
		//  Validate the value.
		//
		final String strValue = _sb.toString();

		if (!isValidTagValue( strValue ))
			{
			String strWhat = String.format( "Invalid tag value '%s'.", strValue );
			throw new ParseException( strWhat, _lexer.getOffset() - strValue.length() );
			}

		return strValue;
		}

	/**
	 * Parses a variation open marker.
	 */
	private void parseVariationBegin() throws ParseException
		{
		assert _lexer.peek( 0 ) == VARIATION_BEGIN;

		if ((_iValidTokens & TT_VARIATION_BEGIN) == 0)
			throw new ParseException( "Unexpected '(' marker.", _lexer.getOffset() );
		//	-------------------------------------------------------------
		_listener.onVariationEnter();

		_iVariationDepth++;
		_iVariations = TT_VARIATION_BEGIN | TT_VARIATION_END;
		_iValidTokens = _iVariations | TT_COMMENT | TT_MOVE_NUMBER;
		}

	/**
	 * Parses a variation close marker.
	 */
	private void parseVariationEnd() throws ParseException
		{
		assert _lexer.peek( 0 ) == VARIATION_END;

		if ((_iValidTokens & TT_VARIATION_END) == 0)
			throw new ParseException( "Unexpected ')' marker.", _lexer.getOffset() );
		//	-------------------------------------------------------------
		_listener.onVariationExit();

		_iVariationDepth--;
		_iVariations = (_iVariationDepth <= 0) ? TT_VARIATION_BEGIN
											   : (TT_VARIATION_BEGIN | TT_VARIATION_END);
		_iValidTokens = _iVariations | TT_COMMENT | TT_MOVE_NUMBER | TT_MOVE | TT_RESULT;
		}

	//  -----------------------------------------------------------------------
	//	TOKEN TYPE CONSTANTS (TT_*)
	//	-----------------------------------------------------------------------

	private static final int TT_ANNOTATION       = 1 << 0;
	private static final int TT_COMMENT          = 1 << 1;
	private static final int TT_MOVE             = 1 << 2;
	private static final int TT_MOVE_NUMBER      = 1 << 3;
	private static final int TT_MOVE_PLACEHOLDER = 1 << 4;
	private static final int TT_RESULT           = 1 << 5;
	private static final int TT_TAG_PAIR         = 1 << 6;
	private static final int TT_VARIATION_BEGIN  = 1 << 7;
	private static final int TT_VARIATION_END    = 1 << 8;

	} /* end of class PgnParser() */
