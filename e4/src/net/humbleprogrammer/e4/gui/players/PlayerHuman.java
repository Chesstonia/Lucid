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
package net.humbleprogrammer.e4.gui.players;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.e4.interfaces.*;
import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.maxx.*;

import static net.humbleprogrammer.maxx.Constants.*;

public class PlayerHuman implements IBoardController, IPlayer
	{

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	private static final Logger s_log = LoggerFactory.getLogger( PlayerHuman.class );

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Color being played. */
	private final int   _player;
	/** Position */
	private final Board _board;

	/** Board presenter */
	private IBoardPresenter _presenter;
	/** Legal moves. */
	private MoveList        _movesLegal;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * CTOR
	 *
	 * @param player
	 * 	[WHITE|BLACK]
	 * @param bd
	 * 	Board.
	 */
	public PlayerHuman( int player, Board bd )
		{
		DBC.require( (player == WHITE || player == BLACK), "Invalid Player" );
		DBC.requireNotNull( bd, "Board" );

		s_log.debug( "{} ctor()", Parser.playerToString( player ) );
		//	-----------------------------------------------------------------
		_board = bd;
		_player = player;
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
		return _board;
		}

	/**
	 * Sets the board presenter.
	 *
	 * @param presenter
	 * 	Board presenter.
	 */
	@Override
	public void setBoardPresenter( IBoardPresenter presenter )
		{
		DBC.requireNotNull( presenter, "Board presenter" );
		//	-----------------------------------------------------------------
		_presenter = presenter;
		_presenter.setBoardController( this );
		}

	//  -----------------------------------------------------------------------
	//	INTERFACE: IPlayer
	//	-----------------------------------------------------------------------

	/**
	 * Tells the player to start thinking.
	 */
	@Override
	public void startThinking()
		{
		s_log.debug( "{} start thinking.", Parser.playerToString( _player ) );

		assert _presenter != null;
		//	-----------------------------------------------------------------
		_movesLegal = MoveList.generate( _board );
		}

	}	/* end of class PlayerHuman */
