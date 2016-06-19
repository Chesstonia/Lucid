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
package net.humbleprogrammer.e4;

import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.e4.gui.MainFrame;
import net.humbleprogrammer.e4.gui.dialogs.DialogManager;
import net.humbleprogrammer.e4.gui.helpers.Command;

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
	private final Workspace _workspace;

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

		_workspace = new Workspace();
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
			SwingUtilities.invokeLater( new App( strArgs ) );

			s_signalExit.await();   // wait for the "all clear" from ExitAppCommand
			}
		catch (InterruptedException ex)
			{
			s_log.warn( "Timed out waiting for exit signal.", ex );
			}
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC GETTERS & SETTERS
	//	-----------------------------------------------------------------------


	/**
	 * Gets the workspace.
	 *
	 * @return Workspace object.
	 */
	public static Workspace getWorkspace()
		{
		return s_self._workspace;
		}

	/**
	 * Gets the top-level frame.
	 *
	 * @return JFrame component.
	 */
	public static MainFrame getFrame()
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
		//	-----------------------------------------------------------------
		try
			{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

			_frame = new MainFrame();
			_frame.setVisible( true );
			Command.execute( Command.ID.QUICK_GAME_RANDOM );

			s_log.debug( "App::run() ended." );
			}
		catch (Exception ex)
			{
			s_log.error( "App::run() aborted.", ex );

			DialogManager.warn( "Initialization failed." );
			s_signalExit.countDown();
			}
		}

	//  -----------------------------------------------------------------------
	//	COMMAND: ExitApp
	//	-----------------------------------------------------------------------

	@SuppressWarnings("unused")
	private final Command cmdExitApp = new Command( Command.ID.EXIT_APP,
													"Exit",
													"Exits the application.",
													null,
													KeyEvent.VK_X )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdExitApp" );
		//	-----------------------------------------------------------------
		if (!DialogManager.confirm( "Really exit?" ))
			return;

		if (_frame != null)
			{
			_frame.setVisible( false );
			_frame.dispose();
			}

		_workspace.dispose();

		s_signalExit.countDown();    //  And pull the plug.
		}
	};

	}   /* end of class App */
