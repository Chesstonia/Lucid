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
package net.humbleprogrammer.e4.gui.helpers;

import javax.swing.*;

import net.humbleprogrammer.humble.DBC;

@SuppressWarnings( "unused" )
public abstract class Command extends javax.swing.AbstractAction implements Runnable
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	public enum ID
		{
			EXIT_APP,
			QUICK_GAME_BLACK,
			QUICK_GAME_RANDOM,
			QUICK_GAME_WHITE
		}

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Optional menu item created for the command. */
	protected JMenuItem _item;

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Creates a menu item for the command.
	 *
	 * @return JMenuItem.
	 */
	public JMenuItem createMenuItem( boolean bCheckable )
		{
		if (_item != null)
			return _item;
		/*
		**  CODE
        */
		Object obj;

		_item = bCheckable
				? new JCheckBoxMenuItem( this )
				: new JMenuItem( this );

		// Add the optional icon
		if ((obj = getValue( SMALL_ICON )) != null)
			_item.setIcon( (Icon) obj );

		// Add the accelerator key
		if ((obj = getValue( MNEMONIC_KEY )) != null)
			_item.setMnemonic( (int) obj );

		// Add the optional tool tip text
		if ((obj = getValue( LONG_DESCRIPTION )) != null)
			_item.setToolTipText( (String) obj );

		update();    // let the command update it's initial state.

		return _item;
		}

	/**
	 * Undoes the command.
	 */
	public void undo()
		{
		throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
		}

	/**
	 * Updates the current state of the command.
	 */
	public void update()
		{
		/*
		**  STUB METHOD
        */
		}

	//  -----------------------------------------------------------------------
	//	INTERFACE: AbstractAction
	//	-----------------------------------------------------------------------

	/**
	 * Invokes the command.
	 *
	 * @param ae
	 * 	Event arguments.
	 */
	@Override
	public void actionPerformed( java.awt.event.ActionEvent ae )
		{
		run();
		}

	//  -----------------------------------------------------------------------
	//	INTERFACE: Runnable
	//	-----------------------------------------------------------------------

	/**
	 * Performs the command.
	 */
	public abstract void run();

	}   /* end of class Command */
