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
package net.humbleprogrammer.maxx;

import net.humbleprogrammer.maxx.factories.BoardFactory;

import static net.humbleprogrammer.maxx.Constants.*;

import java.util.*;

/**
 * The {@link Variation} class stores a sequence of moves.
 */
public class Variation implements Iterable<Move>
	{
	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Current position. */
	private final Board      _board = BoardFactory.createInitial();
	/** List of moves */
	private final List<Move> _moves = new ArrayList<>();

	/** First ply of the variation. */
	private int    _iFirstPly;
	/** Starting position, or null for initial setup. */
	private Board  _bdStart;
	/** Result, or <code>null</code> if not set. */
	private Result _result;

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Appends a move to the variation.
	 *
	 * @param move
	 * 	Move to append.
	 *
	 * @return <code>.T.</code> if added; <code>.F.</code> otherwise.
	 */
	public boolean appendMove( final Move move )
		{
		if (_board.isLegalMove( move ) && _moves.add( move ))
			{
			_board.makeMove( move );
			return true;
			}

		return false;
		}

	/**
	 * Tests if the variation contains any moves.
	 *
	 * @return .T. if at least one move present; .F. otherwise.
	 */
	public boolean isEmpty()
		{ return _moves.isEmpty(); }

	/**
	 * Gets the position <i>BEFORE</i> the player's move at a given move number.
	 *
	 * @param iDelta
	 * 	Distance to seek: positive from start, negative from end
	 *
	 * @return Board position, or <code>null</code> if move number or player are invalid.
	 */
	public Board seekPosition( int iDelta )
		{
		if (iDelta < 0)
			iDelta += _moves.size();

		if (iDelta < 0 || iDelta >= _moves.size())
			return null;
		//
		//	Make each move
		//
		Board bd = (_bdStart != null)
				   ? BoardFactory.createCopy( _bdStart )
				   : BoardFactory.createInitial();

		for (int idx = 0; idx < iDelta; ++idx)
			bd.makeMove( _moves.get(idx) );

		return bd;
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC GETTERS & SETTERS
	//	-----------------------------------------------------------------------

	/**
	 * Gets the current position.
	 *
	 * @return Board.
	 */
	public Board getCurrentPosition()
		{ return _board; }

	/**
	 * Gets the position <i>BEFORE</i> the player's move at a given move number.
	 *
	 * @param iMoveNum
	 * 	Move number (starts at 1)
	 * @param player
	 * 	Moving player [WHITE|BLACK]
	 *
	 * @return Board position, or <code>null</code> if move number or player are invalid.
	 */
	public Board getPosition( int iMoveNum, int player )
		{
		Board bd = (_bdStart != null)
				   ? BoardFactory.createCopy( _bdStart )
				   : BoardFactory.createInitial();

		if (bd.getMoveNumber() == iMoveNum && bd.getMovingPlayer() == player)
			return bd;
		//
		//	See if the move is in the move list.
		//
		int iPly = Board.computePly( iMoveNum, player ) - _iFirstPly;

		if (iPly >= 0 && iPly < _moves.size())
			{
			for ( Move mv : _moves )
				{
				bd.makeMove( mv );
				if (bd.getMoveNumber() == iMoveNum && bd.getMovingPlayer() == player)
					return bd;
				}
			}

		return null;
		}

	/**
	 * Gets the result.
	 *
	 * @return Result, or <code>null</code> if not set.
	 */
	public Result getResult()
		{ return _result; }

	/**
	 * Sets the result.
	 *
	 * @param result
	 * 	Desired result.
	 */
	public void setResult( Result result )
		{ _result = result; }

	/**
	 * Sets the starting position for the variation.  This is only necessary if the variation
	 * doesn't start at the initial position.
	 *
	 * @param bd
	 * 	Starting position.
	 *
	 * @return <code>.T.</code> if position is legal; <code>.F.</code> otherwise.
	 */
	public boolean setStartingPosition( final Board bd )
		{
		if (bd == null) return false;
		//	-----------------------------------------------------------------
		_iFirstPly = Board.computePly( bd.getMoveNumber(), bd.getMovingPlayer() );

		_board.copyFrom( bd );
		_bdStart = (bd.getZobristHash() != HASH_INITIAL) ? new Board( bd ) : null;
		_moves.clear();

		return true;
		}

	/**
	 * Sets the starting position for the variation.  This is only necessary if the variation
	 * doesn't start at the initial position.
	 *
	 * @param strFEN
	 * 	Starting position expressed as a FEN string.
	 *
	 * @return <code>.T.</code> if position is legal; <code>.F.</code> otherwise.
	 */
	public boolean setStartingPosition( final String strFEN )
		{ return setStartingPosition( BoardFactory.createFromFEN( strFEN ) ); }

	//  -----------------------------------------------------------------------
	//	INTEFACE: Iterable<Move>
	//	-----------------------------------------------------------------------
	@Override
	public Iterator<Move> iterator()
		{ return _moves.iterator(); }

	}   /* end of class Variation */
