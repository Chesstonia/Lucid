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

import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.humble.StrUtil;
import net.humbleprogrammer.maxx.*;
import net.humbleprogrammer.maxx.factories.*;
import net.humbleprogrammer.maxx.pgn.*;

import static net.humbleprogrammer.maxx.Constants.*;

@SuppressWarnings( "unused" )
public class Mater extends ToolboxApp
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	private static final boolean EXACT_ONLY      = false;
	private static final int     MATE_IN_X       = 4;
	private static final long    REPORT_INTERVAL = 60L * 1000;    // 60 seconds
	private static final int     STOP_AFTER      = 0;

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Array of PGN files. */
	private final List<Path> _listPGN;

	/** .T. to display parser errors; .F. to ignore them */
	private boolean _bShowErrors;
	/** Number of results found so far. */
	private int     _iResultsFound;
	/** Hash of the previous solution seen. */
	private long _hashPrevious = HASH_INVALID;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 *
	 * @param strArgs
	 * 	Command-line arguments.
	 */
	private Mater( String[] strArgs )
		{
		assert strArgs != null;
		//	-----------------------------------------------------------------
		String strPath = (strArgs.length > 0)
						 ? strArgs[ 0 ]
						 : "P:\\Chess\\PGN\\Assorted"; // "P:\\Chess\\PGN\\TWIC";

		_listPGN = getPGN( strPath );

		printLine( "# Found %,d *.pgn %s",
				   _listPGN.size(),
				   StrUtil.pluralize( _listPGN.size(), "file", null ) );

		if (_listPGN.isEmpty()) throw new RuntimeException( "No *.pgn files found." );
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
			new Mater( strArgs ).run();
			}
		catch (Exception ex)
			{
			s_log.warn( "Caught fatal exception.", ex );
			}
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	private void run()
		{
		try
			{
			MaterListener listener = new MaterListener( MATE_IN_X, EXACT_ONLY );

			for ( Path path : _listPGN )
				{
				try (PgnReader pgn = new PgnReader( new FileReader( path.toFile() ) ))
					{
					String strPGN;

					printLine( "# " + path.toString() );

					while ( (strPGN = pgn.readGame()) != null )
						if (PgnParser.parse( listener, strPGN ))
							{
							//noinspection ConstantConditions
							if (STOP_AFTER > 0 && _iResultsFound >= STOP_AFTER)
								{
								printLine( "# Stopped after %,d results.", _iResultsFound );
								return;
								}
							}
						else if (_bShowErrors)
							{
							s_log.warn( String.format( "%s:\n%s", path.toFile(), strPGN ) );
							}
					}
				}
			}
		catch (IOException ex)
			{
			s_log.error( ex.getMessage() );
			}
		}

	private void saveSolutions( final Board bdStart, final List<PV> solutions )
		{
		assert bdStart != null;
		assert solutions != null;

		if (solutions.isEmpty() || bdStart.getZobristHash() == _hashPrevious) return;
		//	-----------------------------------------------------------------
		String strPrefix = BoardFactory.exportEPD( bdStart );

		_hashPrevious = bdStart.getZobristHash();
		_iResultsFound += solutions.size();

		for ( PV pv : solutions )
			{
			boolean bFirst = true;
			int iMoves = (pv.size() + 1) / 2;
			Board bd = new Board( bdStart );

			print( strPrefix );
			print( "; dm %d; bm ", iMoves );

			for ( Move mv : pv )
				{
				if (bFirst)
					bFirst = false;
				else
					print( " " );

				print( MoveFactory.toSAN( bd, mv, true ) );
				bd.makeMove( mv );
				}

			printLine( "" );
			}
		}

	//  -----------------------------------------------------------------------
	//	INTERFACE: IPgnListener
	//	-----------------------------------------------------------------------

	private class MaterListener extends PgnValidator
		{
		private final boolean _bExactDepth;
		private final int     _iMaxMoves;
		private       long    _deadline;

		MaterListener( int iMaxMoves, boolean bExactDepth )
			{
			DBC.requireGreaterThanZero( iMaxMoves, "Mate Depth" );
			//	-------------------------------------------------------------
			_bExactDepth = bExactDepth;
			_iMaxMoves = iMaxMoves;
			}

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

			saveSolutions( bd, Evaluator.findMateIn( bd, _iMaxMoves, _bExactDepth ) );
			//
			//	See if it's time to report our progress...
			//
			if (System.currentTimeMillis() >= _deadline)
				{
				s_log.info( String.format( "Nodes: %,16d  NPS: %,12d",
										   Evaluator.getNodeCount(),
										   Evaluator.getNPS() ) );
				_deadline = System.currentTimeMillis() + REPORT_INTERVAL;
				}

			return true;
			}
		}
	} /* end of class App */
