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

import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.maxx.Parser;

import java.io.*;

public class PgnReader implements AutoCloseable
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	/** Event tag prefix. */
	private static final String		EVENT_TAG	= "[Event ";

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Input source. */
	private final BufferedReader	_reader;
	/** Used for building PGN strings. */
	private final StringBuilder		_sb			= new StringBuilder();

	/** <code>.T</code> if end of file reached; <code>.F.</code> otherwise. */
	private boolean					_bAtEOF;
	/** Stores event tag lines between calls. */
	private String					_strCached;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 *
	 * @param reader
	 *            Input stream.
	 */
	public PgnReader(Reader reader)
		{
		DBC.requireNotNull(reader, "Input reader");
		//	-------------------------------------------------------------
		_reader = new BufferedReader(reader);
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Reads the next PGN game from the stream.
	 *
	 * @return PGN game, or <code>null</code> if no more games.
	 */
	public String readGame()
		{
		if (_bAtEOF) return null;
		//	-------------------------------------------------------------
		try
			{
			int iPos;
			String str = (_strCached != null) ? _strCached : _reader.readLine();

			_sb.setLength(0);
			//
			//  Read in lines one-by-one until an Event tag is found.
			//
			for ( _strCached = null; str != null; str = _reader.readLine() )
				if ((iPos = str.indexOf(EVENT_TAG)) >= 0)
					{
					if (iPos > 0) str = str.substring(iPos);

					_sb.append(str.trim());
					break;
					}

			_strCached = null; // clear the cached line, because it's been consumed.
			//
			//  If the end of the stream is hit, then fail.  This will happen if the file is
			//  empty, or doesn't contain any PGN games.
			//
			if (str == null)
				{
				_bAtEOF = true;
				return null;
				}
			//
			//  Continue reading in lines until another event tag is hit.
			//
			while ( (str = _reader.readLine()) != null )
				{
				if ((iPos = str.indexOf(EVENT_TAG)) < 0)
					{
					_sb.append(Parser.STR_CRLF);
					_sb.append(str.trim());
					}
				else
					{
					if (iPos == 0)
						_strCached = str;
					else
						{
						_strCached = str.substring(iPos);

						_sb.append(Parser.STR_CRLF);
						_sb.append(str.substring(0, iPos - 1).trim());
						}

					break;
					}
				}
			}
		catch ( IOException ex )
			{
			_bAtEOF = true;
			_sb.setLength(0);
			}

		return _sb.toString();
		}

	//  -----------------------------------------------------------------------
	//	OVERRIDES
	//	-----------------------------------------------------------------------

	@Override
	public void close()
		{
		try
			{
			_reader.close();
			}
		catch ( IOException ex )
			{
			/*
			**  EMPTY CATCH BLOCK
			*/
			}
		}
	} /* end of class PgnReader() */
