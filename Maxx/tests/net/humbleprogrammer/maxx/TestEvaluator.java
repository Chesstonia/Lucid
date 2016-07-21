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
package net.humbleprogrammer.maxx;

import net.humbleprogrammer.TestBase;
import net.humbleprogrammer.maxx.factories.BoardFactory;

import org.junit.Test;

import static org.junit.Assert.*;
import static net.humbleprogrammer.maxx.Constants.*;

import java.util.List;

public class TestEvaluator extends TestBase
	{

	//  -----------------------------------------------------------------------
	//	UNIT TESTS
	//	-----------------------------------------------------------------------

	@Test
	public void t_getMaterialScore()
		{
		assertEquals( 0, Evaluator.getMaterialScore( BoardFactory.createInitial() ) );
		assertEquals( 0, Evaluator.getMaterialScore( BoardFactory.createFromFEN( FEN_TEST ) ) );
		}

	@Test
	public void t_isMateScore()
		{
		for ( int iPlies = 0; iPlies < MAX_MATE_DEPTH; ++iPlies )
			{
			assertTrue( Evaluator.isMateScore( MAX_SCORE - iPlies ) );
			assertTrue( Evaluator.isMateScore( MIN_SCORE + iPlies ) );
			}
		}

	@Test
	public void t_isMateScore_fail()
		{
		final int iMin = MIN_SCORE + MAX_MATE_DEPTH;
		final int iMax = MAX_SCORE - MAX_MATE_DEPTH;

		for ( int iScore = iMin; iScore < iMax; ++iScore )
			assertFalse( Evaluator.isMateScore( iScore ) );
		}

	@Test
	public void t_findMateIn_2_exact()
		{
		String[] strFEN = {
			//	Discovered mate administered by pawn: 1...Qf3+ 2. Ng3 hxg3#
			"1Q4n1/nq2k1b1/b2rpppr/p3p3/P1pP1P1p/NP2P2K/R1P1N2R/2B5 b - -",
			//	Underpromote to Knight: 1. d7+ kB7 2. d8=N#
			"1k6/8/3P4/2PK4/R4BB1/8/5N2/8 w - -",
			//	Intervening check: 1. Rc8+ Qf8+ 2. Rfxf8#
			"6kn/4R3/N1R4K/7p/4r1P1/7P/2p2q2/5R2 w - -",
			//	3 minor pieces combine: 1. Bg3+ Kh3 2. Ng5#
			"1B3B2/5N2/8/7p/1n5k/5K2/8/5b2 w - -",
			//	Lone king in the middle of the board
			"1B4B1/2R5/8/4k3/R6K/8/8/8 w - -" };

		for ( String str : strFEN )
			{
			Board bd = BoardFactory.createFromFEN( str );
			List<PV> solutions = Evaluator.findMateIn( bd, 2 );

			assertNotNull( solutions );
			assertTrue( solutions.size() > 0 );

			for ( PV pv : solutions )
				assertEquals( 3, pv.size() );
			}
		}

	@Test
	public void t_findMateIn_2_fail()
		{
		//	Has a mate in 1: 1...Qxh2#
		Board bd = BoardFactory.createFromFEN( "8/p5k1/2p3p1/8/1P6/P2P1pPq/4r2P/1R5K b - -" );
		List<PV> solutions = Evaluator.findMateIn( bd, 2, true );

		assertNotNull( solutions );
		assertTrue( solutions.isEmpty() );
		}

	@Test
	public void t_findMateIn_2_same()
		{
		//	Has two mate in 2 solutions, but only reports the first line:
		//		1. Rxh3+ Bxh3 Qh2#
		//		1. Rxh3+ Bhx3 Ng3#
		Board bd = BoardFactory.createFromFEN( "r5k1/pR2R1p1/6pp/3p4/P1p5/5r1P/5qB1/4Bn1K b - -" );
		List<PV> solutions = Evaluator.findMateIn( bd, 2, true );

		assertNotNull( solutions );
		assertEquals( 1, solutions.size() );
		}

	@Test
	public void t_findMateIn_2_multiple()
		{
		//	Has five mate in 2 solutions:
		//		1. Kc6 Kf8 2. Rb8#
		//		1. Kd6 Kf8 2. Rb8#
		//		1. Ra7 Kf8 2. Rb8#
		//		1. Rh7 Kf8 2. Rb8#
		//		1. Rbb7 Kf8 2. Rb8#
		Board bd = BoardFactory.createFromFEN( "4k3/3R4/1R6/2K5/7P/8/8/8 w - - 0 1" );
		List<PV> solutions = Evaluator.findMateIn( bd, 2, true );

		assertNotNull( solutions );
		assertEquals( 5, solutions.size() );
		}

	@Test
	public void t_findMateIn_deep()
		{
		Board bd = BoardFactory.createFromFEN( "8/1k1K4/8/8/1pN5/1N6/8/8 w - -" );
		List<PV> solutions = Evaluator.findMateIn( bd, 6 );

		assertNotNull( solutions );
		assertTrue( solutions.size() > 0 );

		for ( PV pv : solutions )
			assertEquals( 11, pv.size() );
		}
	} /* end of class TestEvaluator */
