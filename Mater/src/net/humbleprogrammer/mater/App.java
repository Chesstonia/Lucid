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
 *	other parties provide the program “as is” without warranty of any kind,
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

import net.humbleprogrammer.maxx.*;
import net.humbleprogrammer.maxx.factories.BoardFactory;
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


	/** Output file */
	private FileWriter _writer;
	/** Array of PGN files. */
	private List<Path> _listPGN = new ArrayList<>();
	/** Best move string */
	private String _strBM;
	/** Last position */
	private String _strFEN;

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
			new App( strArgs ).run();
			}
		catch (Exception ex)
			{
			s_log.warn( "Caught fatal exception.", ex );
			}
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
			if (MoveList.isMate( BoardFactory.createCopy( bd, mv ) ))
				{
				try { outputPosition( bd, mv.toString() ); }
				catch (IOException ex) { return false; }
				}

		return true;
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Builds a set of *.pgn files.
	 *
	 * @return Count of files found.
	 */
	private int getPGN( String strPath )
		{
		if (strPath == null || strPath.length() <= 0) return 0;
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

	private void outputPosition( Board bd, String strSAN ) throws IOException
		{
		assert bd != null;
		assert strSAN != null;
		//	-----------------------------------------------------------------
		if (_strFEN != null && _strFEN.equals( bd.toString() ))
			_strBM += " " + strSAN;
		else
			{
			if (_strFEN != null)
				{
				_writer.write( _strFEN );
				_writer.write( _strBM );
				_writer.write( System.lineSeparator() ); // CR/LF
				}

			_strFEN = bd.toString();
			_strBM = "; bm " + strSAN;
			}
		}

	private void run()
		{
		try
			{
			_writer = new FileWriter( new File( "Mater.epd" ) );

			for ( Path path : _listPGN )
				{
				try (PgnReader pgn = new PgnReader( new FileReader( path.toFile() ) ))
					{
					String strPGN;

					while ( (strPGN = pgn.readGame()) != null )
						if (!PgnParser.parse( this, strPGN ))
							{
							s_log.warn( String.format( "%s:\n%s",
													   path.toFile(),
													   strPGN ) );
							}
					}
				}

			if (_strFEN != null)
				{
				_writer.write( _strFEN );
				_writer.write( _strBM );
				_writer.write( System.lineSeparator() ); // CR/LF
				}

			_writer.close();
			}
		catch (IOException ex)
			{
			s_log.error( ex.getMessage() );
			}
		}
	}   /* end of class App */
/*****************************************************************************
 * * * @author Lee Neuse (coder@humbleprogrammer.net) * *	---------------------------- [License]
 * ---------------------------------- *	This work is licensed under the Creative Commons
 * Attribution-NonCommercial- *	ShareAlike 3.0 Unported License. To view a copy of this license,
 * visit *				http://creativecommons.org/licenses/by-nc-sa/3.0/ *	or send a letter to Creative
 * Commons, 444 Castro Street Suite 900, Mountain *	View, California, 94041, USA.
 * *	--------------------- [Disclaimer of Warranty] -------------------------- *	There is no
 * warranty for the program, to the extent permitted by applicable *	law.  Except when otherwise
 * stated in writing the copyright holders and/or *	other parties provide the program “as is”
 * without warranty of any kind, *	either expressed or implied, including, but not limited to,
 * the implied *	warranties of merchantability and fitness for a particular purpose.  The
 * *	entire risk as to the quality and performance of the program is with you. *	Should the
 * program prove defective, you assume the cost of all necessary *	servicing, repair or
 * correction. *	-------------------- [Limitation of Liability] -------------------------- *	In
 * no event unless required by applicable law or agreed to in writing will *	any copyright
 * holder, or any other party who modifies and/or conveys the *	program as permitted above, be
 * liable to you for damages, including any *	general, special, incidental or consequential
 * damages arising out of the *	use or inability to use the program (including but not limited
 * to loss of *	data or data being rendered inaccurate or losses sustained by you or third
 * *	parties or a failure of the program to operate with any other programs), *	even if such
 * holder or other party has been advised of the possibility of *	such damages. *
 ******************************************************************************/
