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
package net.humbleprogrammer.mater;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.humble.StrUtil;
import net.humbleprogrammer.maxx.*;
import net.humbleprogrammer.maxx.factories.BoardFactory;
import net.humbleprogrammer.maxx.factories.MoveFactory;
import net.humbleprogrammer.maxx.pgn.*;

@SuppressWarnings( "unused" )
public class App extends PgnValidator
	{

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	private static final Logger s_log = LoggerFactory.getLogger( App.class );

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Array of PGN files. */
	private final List<Path> _listPGN = new ArrayList<>();

	/** Number of positions found so far. */
	private int _iCount;
	/** Last position */
	private Board _board;
	/** Best move string */
	private String _strBM;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 *
	 * @param strArgs
	 * 	Command-line arguments.
	 */
	private App( String[] strArgs )
		{
		assert strArgs != null;
		//	-----------------------------------------------------------------
		String strPath = (strArgs.length > 0)
						 ? strArgs[ 0 ]
						 : "P:\\Chess\\PGN\\TWIC";

		if (getPGN( strPath ) <= 0)
			throw new RuntimeException( "No *.pgn files found." );
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Entry point for the application.
	 *
	 * @param strArgs
	 * 	Command-line parameters.
	 */
	public static void main( String[] strArgs )
		{
		try
			{
			new App( strArgs ).run( 10 );
			}
		catch (Exception ex)
			{
			s_log.warn( "Caught fatal exception.", ex );
			}
		}

	/**
	 * Output a text string without a following CR/LF.
	 *
	 * @param message
	 * 	Text to output.
	 */
	private static void print( String message )
		{
		if (message != null)
			System.out.print( message );
		}

	/**
	 * Output a formatted text string without a following CR/LF.
	 *
	 * @param format
	 * 	Text to output.
	 * @param args
	 * 	Optional arguments.
	 */
	private static void print( String format, Object... args )
		{
		try { print( String.format( format, args ) ); }
		catch (IllegalFormatException ex)
			{ /* EMPTY CATCH BLOCK */ }
		}

	/**
	 * Output a text string with a CR/LF.
	 *
	 * @param message
	 * 	Text to output.
	 */
	private static void printLine( String message )
		{
		if (message != null)
			System.out.println( message );
		}

	/**
	 * Output a formatted text string with a following CR/LF.
	 *
	 * @param format
	 * 	Text to output.
	 * @param args
	 * 	Optional arguments.
	 */
	private static void printLine( String format, Object... args )
		{
		try { printLine( String.format( format, args ) ); }
		catch (IllegalFormatException ex)
			{ /* EMPTY CATCH BLOCK */ }
		}

	//  -----------------------------------------------------------------------
	//	INTERFACE: IPgnListener
	//	-----------------------------------------------------------------------

	/**
	 * A move has been parsed.
	 *
	 * @param strSAN
	 * 	Move string.
	 * @param strSuffix
	 * 	Optional suffix string.
	 *
	 * @return .T. if parsing is to continue; .F. to abort parsing.
	 */
	@Override
	public boolean onMove( final String strSAN, final String strSuffix )
		{
		if (!super.onMove( strSAN, strSuffix )) return false;
		//	-----------------------------------------------------------------
		final Board bd = _pv.getCurrentPosition();
		final MoveList moves = MoveList.generate( bd );

		for ( Move mv : moves )
			{
			Board bdNew = BoardFactory.createCopy( bd, mv);

			if (Arbiter.isMated( bdNew ))
				savePosition( bd, mv );
			}

		return true;
		}

	/**
	 * A new game is being started.
	 */
	@Override
	public void onGameStart()
		{
		super.onGameStart();
		//	-----------------------------------------------------------------
		}

	/**
	 * The current game has ended.
	 */
	@Override
	public void onGameOver()
		{
		super.onGameOver();
		//	-----------------------------------------------------------------
		displayResults();
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	private void displayResults()
		{
		if (_board == null) return;
		//	-----------------------------------------------------------------
		print( BoardFactory.toString( _board ) );
		printLine( _strBM );

		_board = null;
		_strBM = null;
		}

	/**
	 * Builds a set of *.pgn files.
	 *
	 * @return Count of files found.
	 */
	private int getPGN( String strPath )
		{
		if (StrUtil.isBlank( strPath )) return 0;
		//	-----------------------------------------------------------------
		try
			{
			Path pathPgnRoot = Paths.get( strPath );
			DirectoryStream<Path> stream = Files.newDirectoryStream( pathPgnRoot, "*.pgn" );

			for ( Path path : stream )
				_listPGN.add( path );

			stream.close();
			}
		catch (IOException ex)
			{
			s_log.warn( "Failed to find PGN files: {}",
						ex.getMessage() );
			}

		s_log.debug( "Found {} PGN files.", _listPGN.size() );
		Collections.sort( _listPGN );

		return _listPGN.size();
		}

	private void savePosition( Board bd, Move move )
		{
		assert bd != null;
		assert move != null;
		//	-----------------------------------------------------------------
		String strMove = move.toString();

		if (_board != null && _board.equals( bd ))
			_strBM += " " + MoveFactory.toSAN( bd, move, true);
		else
			{
			displayResults();

			_board = BoardFactory.createCopy( bd );
			_strBM = "; bm " + MoveFactory.toSAN( bd, move, true);
			_iCount++;
			}
		}

	private void run(int iMaxCount)
		{
		try
			{
			for ( Path path : _listPGN )
				{
				try (PgnReader pgn = new PgnReader( new FileReader( path.toFile() ) ))
					{
					String strPGN;

					while ( (strPGN = pgn.readGame()) != null )
						if (PgnParser.parse( this, strPGN ))
							{
							if (iMaxCount > 0 && _iCount >= iMaxCount)
								return;
							}
						else
							{
							s_log.warn( String.format( "%s:\n%s",
													   path.toFile(),
													   strPGN ) );
							}
					}
				}
			}
		catch (IOException ex)
			{
			s_log.error( ex.getMessage() );
			}
		}
	}   /* end of class App */
