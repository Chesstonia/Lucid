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
package net.humbleprogrammer.toolbox;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import net.humbleprogrammer.humble.Stopwatch;
import net.humbleprogrammer.humble.TimeUtil;
import net.humbleprogrammer.maxx.interfaces.IPgnListener;
import net.humbleprogrammer.maxx.pgn.*;

public class Cleaner extends ToolboxApp
	{

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Array of PGN files. */
	private final List<Path>	_listPGN;

	/** Number of errors found so far. */
	private int					_iErrorsFound;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 *
	 * @param strArgs
	 *            Command-line arguments.
	 */
	private Cleaner(String[] strArgs)
		{
		assert strArgs != null;
		//	-----------------------------------------------------------------
		String strPath = (strArgs.length > 0) ? strArgs[0] : "P:\\Chess\\PGN\\TWIC";

		_listPGN = getPGN(strPath);

		printLine("Found %,d *.pgn %s", _listPGN.size(), ((_listPGN.size() == 1) ? "file" : "files"));

		if (_listPGN.isEmpty()) throw new RuntimeException("No *.pgn files found.");
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Entry point for the application.
	 *
	 * @param strArgs
	 *            Command-line parameters.
	 */
	public static void main( String[] strArgs )
		{
		try
			{
			new Cleaner(strArgs).run(50);
			}
		catch ( Exception ex )
			{
			s_log.warn("Caught fatal exception.", ex);
			}
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	private void displayError( String strPGN, String strError, int iGame )
		{
		printLine("Game #%,d:", (iGame + 1));
		printLine(strPGN);
		printLine(strError);
		printLine("");
		}

	private void run( int iMaxCount )
		{
		assert iMaxCount >= 0;
		//	-----------------------------------------------------------------
		try
			{
			int iNetGames = 0;
			IPgnListener listener = new PgnValidator();
			Stopwatch swatch = Stopwatch.startNew();

			for ( Path path : _listPGN )
				{
				try (PgnReader pgn = new PgnReader(new FileReader(path.toFile())))
					{
					int iGames;
					String strPGN;

					printLine(path.toString());

					for ( iGames = 0; (strPGN = pgn.readGame()) != null; ++iGames )
						if (!PgnParser.parse(listener, strPGN))
							{
							displayError(strPGN, PgnParser.getLastError(), iGames);

							if (iMaxCount > 0 && ++_iErrorsFound >= iMaxCount)
								{
								printLine("Stopped after %,d errors.", _iErrorsFound);
								return;
								}
							}

					iNetGames += iGames;
					}
				}

			swatch.stop();

			long lMillisecs = Math.max(1L, swatch.getElapsedMillisecs());
			String strFiles = String.format("%,d %s", _listPGN.size(), ((_listPGN.size() == 1) ? "file" : "files"));
			String strGames = String.format("%,d %s", iNetGames, ((iNetGames == 1) ? "game" : "games"));

			print("Validated %s containing %s in %s ", strFiles, strGames, TimeUtil.formatMillisecs(lMillisecs, false));
			printLine("(%,d gps)", ((iNetGames * 1000L) / lMillisecs));
			}
		catch ( IOException ex )
			{
			s_log.error(ex.getMessage());
			}
		}
	} /* end of class Cleaner */
