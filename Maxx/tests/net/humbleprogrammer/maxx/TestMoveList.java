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

import net.humbleprogrammer.TestBase;
import net.humbleprogrammer.maxx.factories.BoardFactory;
import net.humbleprogrammer.maxx.interfaces.IMoveScorer;

import org.junit.*;

import static org.junit.Assert.*;

public class TestMoveList extends TestBase
	{

	//  -----------------------------------------------------------------------
	//	UNIT TESTS
	//	-----------------------------------------------------------------------

	@Test
	public void t_generate()
		{
		Board bd = BoardFactory.createInitial();
		MoveList moves = new MoveList( bd );

		assertEquals( 20, moves.size() );
		assertFalse( moves.isEmpty() );
		}

	@Test
	public void t_generate_blank()
		{
		final Board bd = BoardFactory.createBlank();
		final MoveList moves = new MoveList( bd );

		assertEquals( 0, moves.size() );
		assertTrue( moves.isEmpty() );
		}

	@Test( expected = IllegalArgumentException.class )
	public void t_generate_fail()
		{
		new MoveList( null );
		}

	@Test
	public void t_generate_long()
		{
		assertEquals( 85, countMoves( "r3k1br/R7/3Q4/7R/4B3/2B5/4K3/8 w k -" ) );
		assertEquals( 218, countMoves( "3Q4/1Q4Q1/4Q3/2Q4R/Q4Q2/3Q4/1Q4Rp/1K1BBNNk w - -" ) );
		assertEquals( 218, countMoves( "R6R/3Q4/1Q4Q1/4Q3/2Q4Q/Q4Q2/pp1Q4/kBNN1KB1 w - -" ) );
		}

	@Test
	public void t_sort()
		{
		Board bd = BoardFactory.createFromFEN( FEN_TEST );
		MoveList moves = new MoveList( bd );

		moves.sort( new IMoveScorer()
			{
			@Override
			public int scoreMove( final Board bd, final Move move )
				{
				return move.iSqFrom + (move.iSqTo << 6);
				}
			} );

		Move mvPrev = null;

		for ( Move move : moves )
			{
			if (mvPrev != null)
				{
				assertTrue(
					(mvPrev.iSqTo > move.iSqTo) ||
					(mvPrev.iSqTo == move.iSqTo && mvPrev.iSqFrom > move.iSqFrom) );
				}
			mvPrev = move;
			}
		}

	//  -----------------------------------------------------------------------
	//	METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Helper method to quickly count the avialble moves in a FEN position.
	 *
	 * @param strFEN
	 * 	Position as a Forsyth-Edwards Notation string.
	 *
	 * @return Count of moves available.
	 */
	private static int countMoves( final String strFEN )
		{
		Board bd = BoardFactory.createFromFEN( strFEN );

		return (bd != null) ? new MoveList( bd ).size() : 0;
		}

	} /* end of unit test class TestMoveList */
