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
import java.util.concurrent.CountDownLatch;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.e4.documents.GameDocument;
import net.humbleprogrammer.e4.gui.MainFrame;
import net.humbleprogrammer.e4.gui.dialogs.DialogManager;
import net.humbleprogrammer.e4.gui.helpers.Command;
import net.humbleprogrammer.e4.gui.helpers.ResourceManager;
import net.humbleprogrammer.humble.DBC;

@SuppressWarnings( "unused" )
public class App implements Runnable
	{

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Countdown latch. */
	static final CountDownLatch s_signalExit = new CountDownLatch( 1 );

	/** Logger */
	private static final Logger s_log = LoggerFactory.getLogger( App.class );
	/** Singleton instance of the workspace. */
	private static App s_self;

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Current document. */
	private final GameDocument             _document = new GameDocument();
	/** Commands. */
	private final Map<Command.ID, Command> _commands = new EnumMap<>( Command.ID.class );

	/** Top-level frame. */
	private MainFrame _frame;

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
		assert s_self == null;
		/*
		**  CODE
        */
		s_self = this;
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Executes a command.
	 *
	 * @param id
	 * 	Command ID
	 */
	public static void execute( Command.ID id )
		{
		DBC.requireNotNull( id, "Comand ID" );

		s_log.debug( "App::execute( {} )", id );
		/*
		**  CODE
        */
		Command cmd = getCommand( id );

		if (cmd != null)
			try
				{
				cmd.run();
				updateAllCommands();
				}
			catch (Exception ex)
				{
				s_log.error( String.format( "Command %s failed", id ), ex );
				}
		}

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
			SwingUtilities.invokeLater( new App( strArgs ) );

			s_signalExit.await();   // wait for the "all clear" from ExitAppCommand
			}
		catch (InterruptedException ex)
			{
			s_log.warn( "Timed out waiting for exit signal.", ex );
			}
		}

	/**
	 * Updates the state of the application.
	 */
	public static void updateAllCommands()
		{
		for ( Command cmd : getAllCommands() )
			cmd.update();
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC GETTERS & SETTERS
	//	-----------------------------------------------------------------------


	/**
	 * Adds a global command to the application.
	 *
	 * @param id
	 * 	Command ID
	 * @param cmd
	 * 	Command
	 */
	public static void addCommand( Command.ID id, Command cmd )
		{
		DBC.requireNotNull( id, "Command ID" );
		DBC.requireNotNull( cmd, "Command" );
		/*
		**  CODE
        */
		assert !s_self._commands.containsKey( id );
		assert !s_self._commands.containsValue( cmd );

		s_self._commands.put( id, cmd );
		}

	/**
	 * Exposes all of the command objects.
	 *
	 * @return Command object.
	 */
	public static Collection<Command> getAllCommands()
		{
		return Collections.unmodifiableCollection( s_self._commands.values() );
		}

	/**
	 * Gets the command object for a given command.
	 *
	 * @param id
	 * 	Command ID
	 *
	 * @return Command object.
	 */
	public static Command getCommand( Command.ID id )
		{
		DBC.requireNotNull( id, "Command ID" );
		/*
		**  CODE
        */
		return s_self._commands.get( id );
		}

	/**
	 * Gets the document.
	 *
	 * @return Document object.
	 */
	public static GameDocument getDocument()
		{
		return s_self._document;
		}

	/**
	 * Gets the top-level frame.
	 *
	 * @return JFrame component.
	 */
	public static JFrame getFrame()
		{
		return s_self._frame;
		}

	/**
	 * Gets the singleton instance of the workspace.
	 *
	 * @return App.
	 */
	public static App getInstance()
		{
		return s_self;
		}

	/**
	 * Gets the application name.
	 *
	 * @return Short name.
	 */
	public static String getName()
		{
		return "e4";
		}

	//  -----------------------------------------------------------------------
	//	INTEFACE: Runnable
	//	-----------------------------------------------------------------------

	@Override
	public void run()
		{
		s_log.debug( "App::run()" );
		/*
		**  CODE
        */
		try
			{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

			if (initCommands() && initGUI() && initPlayers())
				_frame.setVisible( true );
			else
				{
				DialogManager.warn( "Initialization failed." );
				s_signalExit.countDown();
				}

			s_log.debug( "App::run() ended." );
			}
		catch (Exception ex)
			{
			s_log.error( "App::run() aborted.", ex );
			}
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Initializes all of the commands.
	 *
	 * @return .T. on success; .F. otherwise.
	 */
	private boolean initCommands()
		{
		_commands.put( Command.ID.EXIT_APP, new ExitAppCommand() );
		_commands.put( Command.ID.QUICK_GAME_RANDOM, new QuickGameRandomCommand() );
		_commands.put( Command.ID.QUICK_GAME_WHITE, new QuickGameWhiteCommand() );
		_commands.put( Command.ID.QUICK_GAME_BLACK, new QuickGameBlackCommand() );

		return true;
		}

	/**
	 * Builds the User Interface.
	 *
	 * @return .T. on success; .F. otherwise.
	 */
	private boolean initGUI()
		{
		_frame = new MainFrame();
		//
		//	Wire up the document to the views.
		//
		_frame.getBoardView().setDocument( _document );

		return true;
		}

	/**
	 * Initializes all of the players.
	 *
	 * @return .T. on success; .F. otherwise.
	 */
	private boolean initPlayers()
		{
		return true;
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: ExitAppCommand
	//	-----------------------------------------------------------------------

	public class ExitAppCommand extends Command
		{
		public ExitAppCommand()
			{
			putValue( NAME, "Exit" );
			putValue( LONG_DESCRIPTION, "Exit the application." );
			putValue( MNEMONIC_KEY, KeyEvent.VK_X );
			}

		@Override
		public void run()
			{
			s_log.debug( "ExitAppCommand" );
			/*
			**  CODE
            */
			if (!DialogManager.confirm( "Really exit?" ))
				return;

			if (_frame != null)
				{
				_frame.setVisible( false );
				_frame.dispose();
				}

			//  And pull the plug.
			s_signalExit.countDown();
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
			}
		}

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
			}
		}

	}   /* end of class App */
