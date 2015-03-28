/*****************************************************************************
 **
 ** @since 1.0
 **
 ******************************************************************************/
package net.humbleprogrammer.e4.gui.helpers;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.humble.StrUtil;

@SuppressWarnings( "unused" )
public abstract class Command extends javax.swing.AbstractAction implements Runnable
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	public enum ID
		{
			BLACK_ON_TOP,
			WHITE_ON_TOP,
			TOGGLE_ON_TOP,

			EXIT_APP,

			QUICK_GAME_BLACK,
			QUICK_GAME_RANDOM,
			QUICK_GAME_WHITE,

			REQUEST_MOVE,

			TOGGLE_CONSOLE,
			TOGGLE_ENGINE_LOG
		}

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	protected static final Logger           s_log = LoggerFactory.getLogger( Command.class );
	/** Commands. */
	private static final   Map<ID, Command> s_commands = new EnumMap<>( ID.class );

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Optional menu item created for the command. */
	protected JMenuItem _item;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR
	 *
	 * @param id
	 * 	Command ID
	 * @param strName
	 * 	Command label
	 */
	public Command( ID id, String strName )
		{ this( id, strName, null, null, 0 ); }

	/**
	 * Alternate CTOR
	 *
	 * @param id
	 * 	Command ID (may be null for local commands)
	 * @param strName
	 * 	Command label
	 * @param strDescription
	 * 	Human-readable description of the command.
	 */
	public Command( ID id, String strName, String strDescription )
		{ this( id, strName, strDescription, null, 0 ); }


	/**
	 * Alternate CTOR
	 *
	 * @param id
	 * 	Command ID (may be null for local commands)
	 * @param strName
	 * 	Command label
	 * @param strDescription
	 * 	Human-readable description of the command.
	 * @param strIconName
	 * 	Optional icon resource
	 */
	public Command( ID id, String strName, String strDescription, String strIconName )
		{ this( id, strName, strDescription, strIconName, 0 ); }

	/**
	 * Full CTOR
	 *
	 * @param id
	 * 	Command ID (may be null for local commands)
	 * @param strName
	 * 	Command label
	 * @param strDescription
	 * 	Human-readable description of the command.
	 * @param strIconName
	 * 	Optional icon resource
	 * @param iHotKey
	 * 	Optional mnemonic key
	 */
	public Command( ID id, String strName, String strDescription, String strIconName, int iHotKey )
		{
		DBC.requireNotBlank( strName, "Command Name" );
		/*
		**	CODE
		*/
		putValue( NAME, strName );

		if (!StrUtil.isBlank( strDescription ))
			putValue( LONG_DESCRIPTION, strDescription );

		if (iHotKey != 0)
			putValue( MNEMONIC_KEY, iHotKey );

		if (!StrUtil.isBlank( strIconName ))
			{
			Image img = ResourceManager.getImage( strIconName );

			if (img != null)
				putValue( SMALL_ICON, new ImageIcon( img ) );
			}

		if (!(id == null || s_commands.containsKey( id )))
			s_commands.put( id, this );
		}
	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Creates a menu item for the command.
	 *
	 * @return JMenuItem.
	 */
	public JMenuItem createMenuItem()
		{
		return (_item == null)
			   ? buildMenuItem( new JMenuItem( this ) )
			   : _item;
		}

	/**
	 * Creates a checkable menu item for the command.
	 *
	 * @return JMenuItem.
	 */
	public JMenuItem createCheckedMenuItem()
		{
		return (_item == null)
			   ? buildMenuItem( new JCheckBoxMenuItem( this ) )
			   : _item;
		}

	/**
	 * Creates a placeholder menu item.
	 *
	 * @return JMenuItem.
	 */
	public static JMenuItem createDummyMenuItem( String strLabel, int iHotKey )
		{
		JMenuItem item = new JMenuItem( strLabel, iHotKey );

		item.setEnabled( false );

		return item;
		}

	/**
	 * Executes a command.
	 */
	public void execute()
		{
		assert EventQueue.isDispatchThread();
		/*
		**  CODE
        */
		try
			{
			run();
			updateAll();
			}
		catch (Exception ex)
			{
			s_log.warn( String.format( "Command '%s' failed", getName() ), ex );
			}

		}

	/**
	 * Executes a command.
	 *
	 * @param id
	 * 	Command ID
	 */
	public static void execute( ID id )
		{
		DBC.requireNotNull( id, "Comand ID" );
		/*
		**  CODE
        */
		Command cmd = get( id );

		if (cmd != null)
			cmd.execute();
		}

	/**
	 * Executes a command.
	 */
	public void invoke()
		{
		if (EventQueue.isDispatchThread())
			execute();
		else
			{
			s_log.debug( "Invoking command '{}'", getName() );
			SwingUtilities.invokeLater( this );
			}
		}

	/**
	 * Executes a command.
	 *
	 * @param id
	 * 	Command ID
	 */
	public static void invoke( ID id )
		{
		DBC.requireNotNull( id, "Comand ID" );
		/*
		**  CODE
        */
		Command cmd = get( id );

		if (cmd != null)
			cmd.invoke();
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
		**	STUB METHOD
		*/
		}

	/**
	 * Updates the state of the application.
	 */
	public static void updateAll()
		{
		for ( Command cmd : s_commands.values() )
			cmd.update();
		}

//  -----------------------------------------------------------------------
//	PUBLIC GETTERS & SETTERS
//	-----------------------------------------------------------------------


	/**
	 * Gets the command object for a given command.
	 *
	 * @param id
	 * 	Command ID
	 *
	 * @return Command object.
	 */
	public static Command get( ID id )
		{
		DBC.requireNotNull( id, "Command ID" );
		/*
		**  CODE
        */
		return s_commands.get( id );
		}

	/**
	 * Gets the command's name.
	 *
	 * @return Name string.
	 */
	public String getName()
		{
		return (String) getValue( NAME );
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

//  -----------------------------------------------------------------------
//	IMPLEMENTATION
//	-----------------------------------------------------------------------

	/**
	 * Populates the various properties of the menu item.
	 *
	 * @param item
	 * 	Menu item to populate.
	 */
	private JMenuItem buildMenuItem( JMenuItem item )
		{
		if (item == null)
			return null;
		/*
		**	CODE
		*/
		Object obj;

		_item = item;

		// Add the optional icon
		if ((obj = getValue( SMALL_ICON )) != null)
			item.setIcon( (Icon) obj );

		// Add the accelerator key
		if ((obj = getValue( MNEMONIC_KEY )) != null)
			item.setMnemonic( (int) obj );

		// Add the optional tool tip text
		if ((obj = getValue( LONG_DESCRIPTION )) != null)
			item.setToolTipText( (String) obj );

		update();    // let the command update it's initial state.

		return _item;
		}

	/**
	 * Updates the item's checkmark.
	 *
	 * @param bChecked
	 * 	Checked/unchecked state.
	 */
	protected void setChecked( boolean bChecked )
		{
		if (_item != null &&
			_item.getClass() == JCheckBoxMenuItem.class)
			{
			_item.setSelected( bChecked );
			}
		}
	}   /* end of class Command */
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
