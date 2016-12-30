package net.humbleprogrammer.maxx.epd;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.humble.StrUtil;
import net.humbleprogrammer.maxx.Board;
import net.humbleprogrammer.maxx.Parser;
import net.humbleprogrammer.maxx.factories.BoardFactory;

public class EPD extends Parser
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	/** Regular expression for an EPD operator. */
	private static final String RX_EPD_OP = "(\\w{1,14})\\s+(\\S+|\"[^\"]*\");";

	/** FEN string pattern. */
	private static final Pattern s_rxEPD = Pattern.compile
		(
			// group[1] -- position
			RX_POSITION +
			// group[2] -- player
			"\\s+" + RX_PLAYER +
			// group[3] -- castling flags
			"\\s+" + RX_CASTLING +
			// group[4] -- e.p. square
			"\\s+" + RX_EP_SQUARE +
			// End of string or white space
			RX_EOS
		);

	/** EPD string pattern. */
	private static final Pattern s_rxOpCodes = Pattern.compile( RX_EPD_OP );

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Dictionary of op codes and operands. */
	private final HashMap<String, String> _opCodes = new HashMap<>();

	/** Position */
	private Board _board;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	public EPD( String strIn )
		{
		DBC.requireNotBlank( strIn, "EPD String" );
		//	-----------------------------------------------------------------
		Matcher match = s_rxEPD.matcher( strIn );

		if (match.lookingAt())
			{
			_board = BoardFactory.createFromFEN( match.group( 0 ) );

			String strArgs = strIn.substring( match.end() );

			match = s_rxOpCodes.matcher( strArgs );

			for ( int iStart = 0;
				  iStart < strArgs.length() && match.find( iStart );
				  iStart = match.end() + 1 )
				{
				String strOpCode = match.group( 1 );
				String strOperand = match.group( 2 );

				if (!StrUtil.isBlank( strOpCode ) && strOperand != null)
					{
					_opCodes.put( strOpCode.toLowerCase(),
								  StrUtil.stripDelimiters( strOperand, SYM_QUOTE ) );
					}
				}
			}
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------


	/**
	 * Searches a string for a valid EPD string.
	 *
	 * @param strEPD
	 * 	String to search.
	 *
	 * @return {@link java.util.regex.Matcher} if found; null otherwise.
	 */
	public static Matcher matchEPD( final String strEPD )
		{
		if (StrUtil.isBlank( strEPD )) return null;
		//	-----------------------------------------------------------------
		Matcher match = s_rxEPD.matcher( strEPD );

		return match.lookingAt() ? match : null;
		}


	//  -----------------------------------------------------------------------
	//	PUBLIC GETTERS & SETTERS
	//	-----------------------------------------------------------------------

	public String getOperand( String strOpCode )
		{
		return (strOpCode != null)
			   ? _opCodes.get( strOpCode.toLowerCase() )
			   : null;
		}

	public Board getPosition() { return _board; }

	public boolean hasOpCode( String strOpCode )
		{
		return (!StrUtil.isBlank( strOpCode ) &&
				_opCodes.containsKey( strOpCode.toLowerCase() ));
		}

	public boolean isValid() { return !(_board == null || _opCodes.isEmpty()); }

	}
