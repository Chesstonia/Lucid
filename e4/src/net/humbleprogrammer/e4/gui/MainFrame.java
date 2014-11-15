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
package net.humbleprogrammer.e4.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.e4.App;
import net.humbleprogrammer.e4.gui.helpers.Command;
import net.humbleprogrammer.e4.gui.helpers.ResourceManager;
import net.humbleprogrammer.e4.gui.views.BoardView;

public class MainFrame extends JFrame
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	/** Minimum window height, in pixels. */
	private static final int MIN_HEIGHT = 320;
	/** Minimum window width, in pixels. */
	private static final int MIN_WIDTH  = 240;

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	private static final Logger s_log = LoggerFactory.getLogger( MainFrame.class );

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Board view. */
	private BoardView _viewBoard;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 */
	public MainFrame()
		{
		s_log.debug( "MainFrame::ctor" );
		/*
		**  CODE
        */
		createUI( getContentPane() );
		pack();
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC GETTERS & SETTERS
	//	-----------------------------------------------------------------------

	/**
	 * Exposes the board view.
	 *
	 * @return BoardView object.
	 */
	public BoardView getBoardView()
		{
		return _viewBoard;
		}
	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Creates and populates the menu bar.
	 *
	 * @return Menu bar object.
	 */
	private JMenuBar createMenuBar()
		{
		/* Menu bar. */
		JMenuBar menuBar = new JMenuBar();
		//
		//  File menu
		//
		JMenu menu = new JMenu( "File" );
		menu.setMnemonic( KeyEvent.VK_F );

		//	Quick Game sub-menu
		JMenu menuSub = new JMenu( "Quick Game" );
		menuSub.setMnemonic( KeyEvent.VK_Q );
		menuSub.add( App.getCommand( Command.ID.QUICK_GAME_WHITE ).createMenuItem( false ) );
		menuSub.add( App.getCommand( Command.ID.QUICK_GAME_BLACK ).createMenuItem( false ) );
		menuSub.add( App.getCommand( Command.ID.QUICK_GAME_RANDOM ).createMenuItem( false ) );

		menu.add( menuSub );
		menu.addSeparator();
		menu.add( App.getCommand( Command.ID.EXIT_APP ).createMenuItem( false ) );

		menuBar.add( menu );
		//
		//	Edit menu
		//
		menu = new JMenu( "Edit" );
		menu.setMnemonic( KeyEvent.VK_E );

		menu.add( new DummyCommand( "Undo", KeyEvent.VK_U ).createMenuItem( false ) );
		menu.add( new DummyCommand( "Redo", KeyEvent.VK_R ).createMenuItem( false ) );
		menu.addSeparator();
		menu.add( new DummyCommand( "Cut", KeyEvent.VK_T ).createMenuItem( false ) );
		menu.add( new DummyCommand( "Copy", KeyEvent.VK_C ).createMenuItem( false ) );
		menu.add( new DummyCommand( "Paste", KeyEvent.VK_P ).createMenuItem( false ) );
		menu.add( new DummyCommand( "Delete", KeyEvent.VK_DELETE ).createMenuItem( false ) );

		menuBar.add( menu );
		//
		//	Tools menu
		//
		menu = new JMenu( "Tools" );
		menu.setMnemonic( KeyEvent.VK_T );

		menu.add( new DummyCommand( "Benchmarks", KeyEvent.VK_B ).createMenuItem( false ) );

		menuBar.add( menu );
		return menuBar;
		}

	/**
	 * Populates the toolbar.
	 *
	 * @return Toolbar object.
	 */
	private JToolBar createToolBar()
		{
		JToolBar toolBar = new JToolBar( "Main" );

		toolBar.setRollover( true );

		return toolBar;
		}

	/**
	 * Creates all of the user interface components.
	 *
	 * @param content
	 * 	Root container.
	 */
	private void createUI( Container content )
		{
		assert content != null;
		/*
		**  CODE
        */
		setJMenuBar( createMenuBar() );
		content.add( createToolBar(), BorderLayout.NORTH );

		_viewBoard = new BoardView();
		content.add( _viewBoard, BorderLayout.CENTER );

		setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
		setMinimumSize( new Dimension( MIN_WIDTH, MIN_HEIGHT ) );
		setRootPaneCheckingEnabled( true );
		setTitle( App.getName() );
		//
		//	Add the logo; on Windows, this will appear in the task bar.
		//
		Image imgLogo = ResourceManager.getImage( "Logo-48x48.png" );

		if (imgLogo != null)
			setIconImage( imgLogo );
		//
		//  Intercept the activation and closing commands.
		//
		addWindowListener( new WindowAdapter()
		{
		@Override public void windowActivated( WindowEvent we )
			{
			App.updateAllCommands();
			}

		@Override public void windowClosing( WindowEvent we )
			{
			App.execute( Command.ID.EXIT_APP );
			}

		} );
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: DummyCommand
	//	-----------------------------------------------------------------------
	public class DummyCommand extends Command
		{
		DummyCommand( String strLabel, int iHotKey )
			{
			putValue( NAME, strLabel );
			if (iHotKey > 0)
				putValue( MNEMONIC_KEY, iHotKey );

			setEnabled( false );
			}

		@Override
		public void run()
			{
			throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
			}
		}

	}   /* end of class MainFrame */
