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
package net.humbleprogrammer.e4;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.e4.documents.GameDocument;
import net.humbleprogrammer.e4.gui.dialogs.DialogManager;
import net.humbleprogrammer.e4.gui.helpers.Command;
import net.humbleprogrammer.e4.gui.helpers.ResourceManager;
import net.humbleprogrammer.e4.interfaces.IBoardController;
import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.maxx.Board;
import net.humbleprogrammer.maxx.Parser;

import static net.humbleprogrammer.maxx.Constants.*;

public class Workspace extends Observable implements IBoardController
	{

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	private static final Logger s_log = LoggerFactory.getLogger( Workspace.class );

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Current document. */
	private GameDocument _document = new GameDocument();

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	Workspace()
		{
		s_log.debug( "Workspace()" );
		/*
		**	CODE
		*/
		App.addCommand( Command.ID.QUICK_GAME_WHITE, new QuickGameWhiteCommand() );
		App.addCommand( Command.ID.QUICK_GAME_BLACK, new QuickGameBlackCommand() );
		App.addCommand( Command.ID.QUICK_GAME_RANDOM, new QuickGameRandomCommand() );
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
	//	INTERFACE: IBoardController
	//	-----------------------------------------------------------------------

	/**
	 * Gets the current position.
	 *
	 * @return Board object.
	 */
	@Override
	public Board getPosition()
		{
		return _document.getPosition();
		}

	/**
	 * Adds an observer.
	 *
	 * @param observer
	 * 	Observer to add.
	 */
	@Override
	public void registerObserver( Observer observer )
		{
		DBC.requireNotNull( observer, "Observer" );
		/*
		**	CODE
		*/
		addObserver( observer );
		}

	/**
	 * Deletes an observer.
	 *
	 * @param observer
	 * 	Observer object to delete.
	 */
	public void unregisterObserver( Observer observer )
		{
		deleteObserver( observer );
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------


	//  -----------------------------------------------------------------------
	//	NESTED CLASS: QuickGameWhiteCommand
	//	-----------------------------------------------------------------------

	public class QuickGameWhiteCommand extends Command
		{
		public QuickGameWhiteCommand()
			{
			putValue( NAME, "Play White" );
			putValue( LONG_DESCRIPTION, "Play white against the default engine." );
			putValue( MNEMONIC_KEY, KeyEvent.VK_W );

			Image img = ResourceManager.getImage( "White-16x16.png" );
			if (img != null)
				putValue( SMALL_ICON, new ImageIcon( img ) );
			}

		@Override
		public void run()
			{
			s_log.debug( "QuickGameWhiteCommand" );
			/*
			**  CODE
            */
			Command cmd = App.getCommand( ID.WHITE_ON_TOP );

			_document = new GameDocument();

			if (cmd != null)
				cmd.run();
			//
			//	And tell the world.
			//
			setChanged();
			notifyObservers();
			}
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: QuickGameBlackCommand
	//	-----------------------------------------------------------------------

	public class QuickGameBlackCommand extends Command
		{
		public QuickGameBlackCommand()
			{
			putValue( NAME, "Play Black" );
			putValue( LONG_DESCRIPTION, "Play black against the default engine." );
			putValue( MNEMONIC_KEY, KeyEvent.VK_B );

			Image img = ResourceManager.getImage( "Black-16x16.png" );
			if (img != null)
				putValue( SMALL_ICON, new ImageIcon( img ) );
			}

		@Override
		public void run()
			{
			s_log.debug( "QuickGameBlackCommand" );
			/*
			**  CODE
            */
			Command cmd = App.getCommand( ID.BLACK_ON_TOP );

			_document = new GameDocument();

			if (cmd != null)
				cmd.run();
			//
			//	And tell the world.
			//
			setChanged();
			notifyObservers();
			}
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: QuickGameRandomCommand
	//	-----------------------------------------------------------------------

	public class QuickGameRandomCommand extends Command
		{
		public QuickGameRandomCommand()
			{
			putValue( NAME, "Play Random" );
			putValue( LONG_DESCRIPTION, "Play a random side against the default engine." );
			putValue( MNEMONIC_KEY, KeyEvent.VK_W );

			Image img = ResourceManager.getImage( "Random-16x16.png" );
			if (img != null)
				putValue( SMALL_ICON, new ImageIcon( img ) );
			}

		@Override
		public void run()
			{
			s_log.debug( "QuickGameRandomCommand" );
			/*
			**  CODE
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
			Command cmd = App.getCommand( (pc == WHITE)
										  ? ID.BLACK_ON_TOP
										  : ID.WHITE_ON_TOP );

			if (cmd != null)
				cmd.run();
			}
		}
	}   /* end of class Workspace */
