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

import java.util.Arrays;

import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.maxx.factories.BoardFactory;

import static net.humbleprogrammer.maxx.Constants.*;
import static org.junit.Assert.*;

public class TestPosition
	{

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	private final Board  _board;
	private final Board  _boardMirror;
	private final long[] _lActual;
	private final long[] _lExpected;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	public TestPosition( String strFEN, long[] lExpected )
		{
		DBC.require( BoardFactory.isValidFEN( strFEN ), "strFEN" );
		DBC.requireNotNull( lExpected, "Expected" );
		//	-----------------------------------------------------------------
		Board bd = BoardFactory.createFromFEN( strFEN );
		assertNotNull( bd );

		if (bd.getMovingPlayer() == WHITE)
			{
			_board = bd;
			_boardMirror = BoardFactory.createMirror( bd );
			}
		else
			{
			_boardMirror = bd;
			_board = BoardFactory.createMirror( bd );
			}

		_lExpected = lExpected;
		_lActual = new long[ _lExpected.length ];
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	public int test( int player, int iMaxDepth )
		{
		final int iDepth = Math.min( iMaxDepth, _lExpected.length - 1 );

		Arrays.fill( _lActual, 0L );

		if (iDepth > 0)
			{
			Board bd = (player == WHITE) ? _board : _boardMirror;

			perft( bd, 0, iDepth );
			}

		return iDepth;
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	long[] getActual( int iDepth ) { return Arrays.copyOfRange( _lActual, 0, iDepth + 1 ); }

	long[] getExpected( int iDepth ) { return Arrays.copyOfRange( _lExpected, 0, iDepth + 1 ); }

	String getFEN( int player )
		{
		return (player == WHITE) ? _board.toString() : _boardMirror.toString();
		}

	private void perft( final Board bd, int iDepth, int iMaxDepth )
		{
		MoveList moves = new MoveList( bd );

		_lActual[ iDepth ] += moves.size();

		if (++iDepth <= iMaxDepth)
			{
			for ( Move move : moves )
				perft( new Board( bd, move ), iDepth, iMaxDepth );
			}
		}
	}	/* end of class TestPosition */
