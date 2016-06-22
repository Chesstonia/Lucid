/* ****************************************************************************
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
package net.humbleprogrammer.maxx.pgn;

import net.humbleprogrammer.maxx.*;
import net.humbleprogrammer.maxx.factories.MoveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

import static net.humbleprogrammer.maxx.Constants.*;

public class PgnValidator extends PgnAdapter
	{
	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	private static final Logger		s_log		= LoggerFactory.getLogger(PgnParser.class);

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Nested variations. */
	private final Stack<Variation>	_variations	= new Stack<>();

	/** Player color of first move in the current variation. */
	private int						_player		= INVALID;
	/** First move number in the current variation. */
	private int						_iMoveNum	= 0;
	/** Current variation, or <c>null</c> if ignoring variation. */
	protected Variation				_pv			= new Variation();

	//  -----------------------------------------------------------------------
	//	INTERFACE: IPgnListener
	//	-----------------------------------------------------------------------

	/**
	 * A move has been parsed.
	 *
	 * @param strSAN
	 *            Move string.
	 * @param strSuffix
	 *            Optional suffix string.
	 * @return .T. if parsing is to continue; .F. to abort parsing.
	 */
	@Override
	public boolean onMove( final String strSAN, final String strSuffix )
		{
		assert strSAN != null;
		assert strSuffix != null;

		if (_pv == null) return true;
		//	-----------------------------------------------------------------
		if (_pv.isEmpty())
			{
			if (_variations.isEmpty())
				{
				final Board bd = _pv.getCurrentPosition();
				//
				//  If there are no previous variations, this must be the main line.
				//  The starting position must already have been set, either by
				//  default of by a FEN tag.
				//
				if (_iMoveNum != bd.getMoveNumber() || _player != bd.getMovingPlayer()) return false;
				}
			else
				{
				//
				//  Try to get the position matching the move number and player from
				//  the previous variation.
				//
				final Variation pv = _variations.peek();
				final Board bd = (pv != null) ? pv.getPosition(_iMoveNum, _player) : null;
				if (bd == null) return false;

				_pv.setStartingPosition(bd);
				}
			}
		//
		//  Try to find the move based on the current position.  This will fail if the
		//  move is nonsensical, illegal, or ambiguous.
		//
		final Move moveFound = MoveFactory.fromSAN(_pv.getCurrentPosition(), strSAN);

		if (moveFound != null) return _pv.appendMove(moveFound);

		s_log.debug("{} => '{}' is illegal or ambiguous.", _pv.getCurrentPosition(), strSAN);

		// MoveFactory.fromSAN( _pv.getCurrentPosition(), strSAN );

		return false;
		}

	/**
	 * A move number has been parsed.
	 *
	 * @param iMoveNumber
	 *            Move number.
	 * @return .T. if parsing is to continue; .F. to abort parsing.
	 */
	@Override
	public boolean onMoveNumber( final int iMoveNumber )
		{
		assert iMoveNumber > 0;
		if (_pv == null) return true;
		//	-----------------------------------------------------------------
		if (_pv.isEmpty())
			{
			_player = WHITE;
			_iMoveNum = iMoveNumber;
			return true;
			}

		return (iMoveNumber == _pv.getCurrentPosition().getMoveNumber());
		}

	/**
	 * A move placeholder ("..") has been parsed.
	 *
	 * @return .T. if move placeholder is valid; .F. to abort parsing.
	 */
	@Override
	public boolean onMovePlaceholder()
		{
		if (_pv == null) return true;
		//	-----------------------------------------------------------------
		if (_pv.isEmpty())
			{
			if (_player == BLACK) return false;

			_player = BLACK;
			return true;
			}

		return (_pv.getCurrentPosition().getMovingPlayer() == BLACK);
		}

	/**
	 * A null move ("--") has been parsed.
	 *
	 * @return .T. if parsing is to continue; .F. to abort parsing.
	 */
	public boolean onNullMove()
		{
		if (_variations.isEmpty()) return false;
		//	-----------------------------------------------------------------
		_pv = null;
		return true;
		}

	/**
	 * A move number has been parsed.
	 *
	 * @param result
	 *            Result
	 * @return .T. if parsing is to continue; .F. to abort parsing.
	 */
	public boolean onResult( final Result result )
		{
		assert result != null;
		//	-----------------------------------------------------------------
		if (_pv != null)
			{
			if (_pv.getResult() != null) return false;

			_pv.setResult(result);
			}

		return true;
		}

	/**
	 * A tag name/value pair has been parsed.
	 *
	 * @param strName
	 *            Tag name.
	 * @param strValue
	 *            Tag value.
	 * @return .T. if parsing should continue; .F. to abort parsing.
	 */
	public boolean onTag( final String strName, final String strValue )
		{
		assert PgnParser.isValidTagName(strName);
		assert PgnParser.isValidTagValue(strValue);
		//	-----------------------------------------------------------------
		return (!strName.equalsIgnoreCase(PgnParser.TAG_FEN) || _pv.setStartingPosition(strValue));
		}

	/**
	 * A variation open marker '(' was parsed.
	 */
	public void onVariationEnter()
		{
		_variations.push(_pv);
		_pv = new Variation();
		}

	/**
	 * A variation close marker ')' was parsed.
	 */
	public void onVariationExit()
		{
		assert _variations.size() > 0;
		//	-----------------------------------------------------------------
		_pv = _variations.pop();
		}

	/**
	 * A new game is being started.
	 */
	@Override
	public void onGameStart()
		{
		super.onGameStart();
		//	-----------------------------------------------------------------
		_iMoveNum = 0;
		_player = INVALID;
		_variations.clear();
		_pv = new Variation();
		}
	} /* end of class PgnValidator */
