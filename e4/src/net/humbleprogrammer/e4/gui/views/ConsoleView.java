/*****************************************************************************
 **
 ** @since 1.0
 **
 ******************************************************************************/
package net.humbleprogrammer.e4.gui.views;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;

import net.humbleprogrammer.e4.App;
import net.humbleprogrammer.e4.gui.SwingUtil;
import net.humbleprogrammer.e4.gui.helpers.Command;
import net.humbleprogrammer.humble.GfxUtil;

public class ConsoleView extends JDialog
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	/** Minimum window height, in pixels. */
	private static final int MIN_HEIGHT = 128;
	/** Minimum window width, in pixels. */
	private static final int MIN_WIDTH  = 196;

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** .T. if window location needs to be set; .F. otherwise. */
	private boolean _bNeedLocation = true;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 *
	 * @param owner
	 * 	Frame that owns this view.
	 */
	public ConsoleView( Frame owner )
		{
		super( owner );
		/*
		**	CODE
		*/
		createUI( getContentPane() );

		pack();
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Creates all of the UI elements.
	 *
	 * @param content
	 * 	Content pane that all the elements will be added to.
	 */
	private void createUI( Container content )
		{
		assert content != null;
		/*
		**	CODE
		*/
		setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
		setMinimumSize( new Dimension( MIN_WIDTH, MIN_HEIGHT ) );
		setRootPaneCheckingEnabled( true );
		setTitle( App.getName() + " Console" );

		pack();
		}

	//  -----------------------------------------------------------------------
	//	COMMAND: ToggleConsole
	//	-----------------------------------------------------------------------

	@SuppressWarnings( "unused" )
	private final Command cmdToggleConsole = new Command( Command.ID.TOGGLE_CONSOLE,
														  "Console Window",
														  "Shows or hides the Console window.",
														  null, KeyEvent.VK_C )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdToggleConsole" );
		/*
		**	CODE
		*/
		boolean bShow = !isVisible();

		if (bShow && _bNeedLocation)
			{
			Rectangle rFrame = App.getFrame().getBounds();

			setBounds( (int) (rFrame.getMaxX() + GfxUtil.MARGIN_THICK),
					   (int) rFrame.getMinY(),
					   rFrame.width, (rFrame.height / 2) );

			if (!SwingUtil.getDesktopBounds().contains( rFrame ))
				setLocationByPlatform( true );

			_bNeedLocation = false;
			}

		setVisible( bShow );
		}

	@Override
	public void update()
		{
		setChecked( isVisible() );
		}
	};
	}	/* end of class ConsoleView */
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
