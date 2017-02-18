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

import net.humbleprogrammer.humble.*;
import net.humbleprogrammer.maxx.*;
import net.humbleprogrammer.maxx.factories.BoardFactory;
import net.humbleprogrammer.maxx.factories.MoveFactory;
import net.humbleprogrammer.maxx.interfaces.IPgnListener;
import net.humbleprogrammer.maxx.pgn.*;

import static net.humbleprogrammer.maxx.Constants.*;

public class Checker extends ToolboxApp
	{
	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	private static final int STOP_AFTER = 25;

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Array of PGN files. */
	private final List<Path> _listPGN;

	/** Number of errors found so far. */
	private int _iFound;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 *
	 * @param strArgs
	 * 	Command-line arguments.
	 */
	private Checker( String[] strArgs )
		{
		assert strArgs != null;
		//	-----------------------------------------------------------------
		String strPath = (strArgs.length > 0)
						 ? strArgs[ 0 ]
						 : "P:\\Chess\\PGN\\TWIC"; // "P:\\Chess\\PGN\\Assorted";

		_listPGN = getPGN( strPath );

		printLine( "Found %,d *.pgn %s",
				   _listPGN.size(),
				   StrUtil.pluralize( _listPGN.size(), "file", null ) );

		if (_listPGN.isEmpty())
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
			new Checker( strArgs ).run( STOP_AFTER );
			}
		catch (Exception ex)
			{
			s_log.warn( "Caught fatal exception.", ex );
			}
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	private void run( int iMaxCount )
		{
		assert iMaxCount >= 0;
		//	-----------------------------------------------------------------
		try
			{
			IPgnListener listener = new ForkListener();

			for ( Path path : _listPGN )
				{
				String strPGN;

				try (PgnReader pgn = new PgnReader( new FileReader( path.toFile() ) ))
					{
					printLine( "# " + path.toString() );

					for ( int iGames = 0; (strPGN = pgn.readGame()) != null; ++iGames )
						if (PgnParser.parse( listener, strPGN ))
							{
							if (iMaxCount > 0 && _iFound >= iMaxCount)
								{
								printLine( "Stopped after %,d %s.",
										   _iFound,
										   StrUtil.pluralize( _iFound, "result", null ) );
								return;
								}
							}
						else
							{
							printLine( "Game #%,d:", (iGames + 1) );
							printLine( strPGN );
							printLine( PgnParser.getLastError() );
							printLine( "" );
							}

					}
				}
			}
		catch (IOException ex)
			{
			s_log.error( ex.getMessage() );
			}
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: CheckFinderListener
	//	-----------------------------------------------------------------------

	@SuppressWarnings( "unused" )
	private class CheckFinderListener extends PgnValidator
		{
		/** Longest sequence of moves found so far. */
		private int _iLongest = 0;

		/**
		 * Display all the moves for a given position.
		 *
		 * @param bd
		 * 	Position.
		 * @param moves
		 * 	List of moves.
		 */
		private void display( Board bd, List<Move> moves )
			{
			if (bd == null || moves.size() <= _iLongest) return;
			//	-----------------------------------------------------------------
			_iFound++;
			_iLongest = moves.size();

			print( BoardFactory.exportEPD( bd ) );
			print( "; bm" );
			for ( Move move : moves )
				print( ' ' + MoveFactory.toSAN( bd, move, true ) );

			printLine( "" );

			moves.clear();
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
			//	-------------------------------------------------------------
			if (_pv != null)
				{
				final Board bd = new Board( _pv.getCurrentPosition() );
				final List<Move> checks = new ArrayList<>();
				final MoveList moves = new MoveList( bd );

				for ( Move mv : moves )
					{
					Board bdNew = new Board( bd );

					bdNew.makeMove( mv );
					if (bdNew.isInCheck())
						checks.add( mv );
					}

				if (!checks.isEmpty())
					display( bd, checks );
				}

			return true;
			}
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: ForkListener
	//	-----------------------------------------------------------------------

	@SuppressWarnings( "unused" )
	private class ForkListener extends PgnValidator
		{
		/**
		 * Display all the moves for a given position.
		 */
		private void display( Board bd, Move move, long bbAttacks )
			{
			assert bd != null;
			assert move != null;
			//	-------------------------------------------------------------
			Board bdAfter = new Board( bd );
			String message = "";

			bdAfter.makeMove( move );

			for ( long bb = bbAttacks; bb != 0L; bb &= (bb - 1) )
				{
				int sqTo = BitUtil.first( bb );

				if (message.length() > 0) message += ", ";

				message += String.format( "%s (%s)",
										  Parser.pieceToString( bd.get( sqTo ) ),
										  Square.toString( sqTo ) );
				}

			_iFound++;

			printLine( "%s; bm %s; c0 \"%s\"",
					   BoardFactory.exportEPD( bd ),
					   MoveFactory.toSAN( bd, move, true ),
					   message );
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
			if (_pv == null)
				return super.onMove( strSAN, strSuffix );

			final Board bdBefore = _pv.getCurrentPosition();
			final Move move = MoveFactory.fromSAN( bdBefore, strSAN );

			if (!super.onMove( strSAN, strSuffix ))
				return false;
			//	-------------------------------------------------------------
			final Board bdAfter = _pv.getCurrentPosition();

			if (bdAfter.isInCheck())
				return true;

			bdAfter.setMovingPlayer( bdBefore.getMovingPlayer() );

			final int piece = bdAfter.get( move.iSqTo );
			final MoveList moves = new MoveList( bdAfter, Square.getMask( move.iSqTo ), ~0L );

			long bbAttacks = 0L;

			for ( Move mv : moves )
				{
				int victim = bdAfter.get( mv.iSqTo );

				if (victim != EMPTY &&
					!(mv.isPromotion() && mv.getPromotionPiece() != QUEEN) &&
					Piece.getType( victim ) > Piece.getType( piece ))
					{
					bbAttacks |= Square.getMask( mv.iSqTo );
					}
				}

			if (BitUtil.count( bbAttacks ) > 1)
				display( bdBefore, move, bbAttacks );

			return true;
			}
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: PinListener
	//	-----------------------------------------------------------------------

	@SuppressWarnings( "unused" )
	private class PinListener extends PgnValidator
		{
		/**
		 * Display all the moves for a given position.
		 */
		private void display( Board bd, int sqPinned )
			{
			_iFound++;

			printLine( "%s; c0 \"%s on %s is pinned\"",
					   BoardFactory.exportEPD( bd ),
					   Parser.pieceToString( bd.get( sqPinned ) ),
					   Square.toString( sqPinned ) );
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
			if (_pv == null) return true;
			//	-------------------------------------------------------------
			final Board bd = _pv.getCurrentPosition();
			final int player = bd.getMovingPlayer();
			final int opponent = player ^ 1;

			long bbPinned = 0L;
			long bbOpponent = bd.getPieceMap( opponent );
			long bbPlayer = bd.getPieceMap( player );
			long bbAll = bbPlayer | bbOpponent;
			//
			//  Find pinned pieces.  This is done by finding all of the player's pieces that
			//  could attack the King if the opposing player's pieces were removed.
			//
			final int sqKing = bd.getOpposingKingSquare();
			final long bbQueen = bd.getPieceMap( MAP_W_QUEEN + player );
			final long bbBishops = bd.getPieceMap( MAP_W_BISHOP + player );
			final long bbRooks = bd.getPieceMap( MAP_W_ROOK + player );

			long bbPinners = Bitboards.getDiagonalAttackers( sqKing, (bbQueen | bbBishops), bbPlayer ) |
							 Bitboards.getLateralAttackers( sqKing, (bbQueen | bbRooks), bbPlayer );

			for ( long bb = bbPinners; bb != 0L; bb &= (bb - 1) )
				{
				int sqFrom = BitUtil.first( bb );
				//
				//  If there is one (and only one) moving piece that lies on the path between a
				//	threatening piece (the "pinner") and the King, then it is pinned. Pinned
				//	pieces may still be able to move (except for Knights) but need to test for
				//	check when they do so.
				//
				long bbBetween = bbOpponent &
								 Bitboards.getSquaresBetween( sqKing, sqFrom );

				if (BitUtil.singleton( bbBetween ))
					{
					int sqTo = BitUtil.first( bbBetween );

					if (bd.getPieceType( sqTo ) > bd.getPieceType( sqFrom ))
						display( bd, sqTo );
					}
				}

			return true;
			}
		}

//  -----------------------------------------------------------------------
//	NESTED CLASS: MateListener
//	-----------------------------------------------------------------------

	@SuppressWarnings( "unused" )
	private class MateListener extends PgnValidator
		{
		private final List<Move> _moves = new ArrayList<>();

		private Board _board;

		/**
		 * Display all the moves for a given position.
		 */
		private void display( Board bd, Move move )
			{
			if (_board != null && _board.equals( bd ))
				_moves.add( move );
			else
				{
				if (_board != null)
					{
					String message =
						String.format( "%s; bm", BoardFactory.exportEPD( _board ) );

					for ( Move mv : _moves )
						message +=
							String.format( " %s", MoveFactory.toSAN( _board, mv, true ) );

					_iFound++;

					printLine( message );
					}

				_board = new Board( bd );
				_moves.clear();
				_moves.add( move );
				}
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
			if (_pv == null) return true;
			//	-------------------------------------------------------------
			final Board bd = _pv.getCurrentPosition();
			final MoveList moves = new MoveList( bd );

			for ( Move mv : moves )
				{
				Board bdNew = new Board( bd );
				bdNew.makeMove( mv );

				if (Arbiter.isMated( bdNew ))
					display( bd, mv );
				}

			return true;
			}
		}

//  -----------------------------------------------------------------------
//	NESTED CLASS: TestListener
//	-----------------------------------------------------------------------

	@SuppressWarnings( "unused" )
	private class TestListener extends PgnValidator
		{
		private int _iLongest = 64;


		/**
		 * Display all the moves for a given position.
		 */
		private void display( Board bd )
			{
			printLine( "%s; %,d",
					   BoardFactory.exportEPD( bd ),
					   _iLongest );
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
			//	-------------------------------------------------------------
			if (_pv != null)
				{
				final MoveList moves = new MoveList( _pv.getCurrentPosition() );

				if (moves.size() > _iLongest)
					{
					_iLongest = moves.size();
					display( _pv.getCurrentPosition() );
					}
				}
			return true;
			}
		}

//  -----------------------------------------------------------------------
//	NESTED CLASS: SkewerListener
//	-----------------------------------------------------------------------

	@SuppressWarnings( "unused" )
	private class SkewerListener extends PgnValidator
		{
		/**
		 * Display all the moves for a given position.
		 */
		private void display( Board bd )
			{
			_iFound++;

			printLine( "%s;",
					   BoardFactory.exportEPD( bd ) );
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
			if (_pv == null) return true;
			//	-------------------------------------------------------------
			final Board bd = _pv.getCurrentPosition();
			final long bbCheckers = bd.getCheckers();

			for ( long bb = bbCheckers; bb != 0L; bb &= (bb - 1) )
				{
				final int sqFrom = BitUtil.first( bb );
				final int piece = bd.get( sqFrom );

				if (Piece.getType( piece ) == PAWN || Piece.getType( piece ) == KNIGHT)
					continue;

				}

			return true;

			}
		}

	} /* end of class Checker */
