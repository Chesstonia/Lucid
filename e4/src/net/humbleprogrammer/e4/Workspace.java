/*****************************************************************************
 **
 ** @since 1.0
 **
 ******************************************************************************/
package net.humbleprogrammer.e4;

import java.awt.event.KeyEvent;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.e4.documents.GameDocument;
import net.humbleprogrammer.e4.gui.dialogs.DialogManager;
import net.humbleprogrammer.e4.gui.helpers.Command;
import net.humbleprogrammer.e4.gui.players.PlayerHuman;
import net.humbleprogrammer.e4.interfaces.*;
import net.humbleprogrammer.maxx.Parser;

import static net.humbleprogrammer.maxx.Constants.*;

public class Workspace
	{

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	private static final Logger s_log = LoggerFactory.getLogger( Workspace.class );

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Players */
	private final IPlayer[] _players = new IPlayer[ 2 ];

	/** Current document. */
	private GameDocument _document;
	/** Moving player. */
	private IPlayer      _playerMoving;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 */
	Workspace()
		{
		s_log.debug( "Workspace()" );
		/*
		**	EMPTY CTOR
		*/
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	public void dispose()
		{
		s_log.debug( "dispose()" );
		/*
		**	STUB METHOD
		*/
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Starts a new game.
	 */
	private void startNewGame()
		{
		IBoardPresenter presenter = App.getFrame().getBoardPresenter();

		if (_document != null)
			_document.deleteObserver( presenter );

		_document = new GameDocument();
		_document.addObserver( presenter );

		_players[ WHITE ] = new PlayerHuman( WHITE, _document.getPosition() );
		_players[ BLACK ] = new PlayerHuman( BLACK, _document.getPosition() );
		//
		//	Get the moving player and connect it to the board.
		//
		_playerMoving = _players[ _document.getPosition().getMovingPlayer() ];
		}

	//  -----------------------------------------------------------------------
	//	COMMAND: QuickGameWhite
	//	-----------------------------------------------------------------------

	private final Command cmdQuickGameWhite = new Command( Command.ID.QUICK_GAME_WHITE,
														   "Play White",
														   "Play white against the default engine.",
														   "White-16x16.png",
														   KeyEvent.VK_W )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdQuickGameWhite" );
		/*
		**	CODE
		*/
		startNewGame();
		//
		//	Set the board so that white is at the bottom.
		//
		Command.execute( ID.BLACK_ON_TOP );
		cmdRequestMove.run();
		}
	};

	//  -----------------------------------------------------------------------
	//	COMMAND: QuickGameBlack
	//	-----------------------------------------------------------------------

	private final Command cmdQuickGameBlack = new Command( Command.ID.QUICK_GAME_BLACK,
														   "Play Black",
														   "Play black against the default engine.",
														   "Black-16x16.png",
														   KeyEvent.VK_B )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdQuickGameBlack" );
		/*
		**	CODE
		*/
		startNewGame();
		//
		//	Set the board so that black is at the bottom.
		//
		Command.execute( ID.WHITE_ON_TOP );
		cmdRequestMove.run();
		}
	};

	//  -----------------------------------------------------------------------
	//	COMMAND: QuickGameRandom
	//	-----------------------------------------------------------------------
	@SuppressWarnings( "unused" )
	private final Command cmdQuickGameRandom = new Command( Command.ID.QUICK_GAME_RANDOM,
															"Play Random",
															"Play a random side against the default engine.",
															"Random-16x16.png",
															KeyEvent.VK_R )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdQuickGameRandom" );
		/*
		**	CODE
		*/
		Random rnd = new Random();
		int pc = rnd.nextBoolean() ? WHITE : BLACK;
		//
		//	Tell the user which side they're playing.
		//
		String strMessage = String.format( "You will be playing %s this game.",
										   Parser.playerToString( pc ) );

		DialogManager.advise( strMessage );
		//
		//	Now set the board.
		//
		if (pc == WHITE)
			cmdQuickGameWhite.run();
		else
			cmdQuickGameBlack.run();
		}
	};

	//  -----------------------------------------------------------------------
	//	Command: RequestMove
	//	-----------------------------------------------------------------------

	private final Command cmdRequestMove = new Command( Command.ID.REQUEST_MOVE,
														"Move Now" )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdRequestMove" );
		/*
		**	CODE
		*/
		if (_playerMoving instanceof IBoardController)
			{
			App.getFrame()
			   .getBoardPresenter()
			   .setBoardController( (IBoardController) _playerMoving );
			}

		_playerMoving.startThinking();
		}
	};

	}   /* end of class Workspace */
/*****************************************************************************
 **
 ** @author Lee Neuse (coder@humbleprogrammer.net)
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
