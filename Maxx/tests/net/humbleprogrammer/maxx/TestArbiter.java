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

public class TestArbiter extends TestBase
	{

	//  -----------------------------------------------------------------------
	//	UNIT TESTS
	//	-----------------------------------------------------------------------

	@Test
	public void t_isLegalPosition()
		{
		final Board bd = BoardFactory.createFromFEN(FEN_TEST);

		assertTrue(Arbiter.isLegalPosition(bd));
		}

	@Test
	public void t_isMated()
		{
		String[] strFEN = {
				//	http://www.chessgames.com/perl/chessgame?gid=1274437
				"r6r/pppk1ppp/8/8/2P5/2NbbN2/PPnK1nPP/1RB2B1R w - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1028832
				"r4r1k/pp4pp/3p4/3B4/8/1QN3Pb/PP3nKP/R5R1 w - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1483810
				"2r3k1/4Rppp/8/p7/Kb3PP1/1Pn4P/8/7R w - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1259009
				"rn3r2/pbppq1p1/1p2pN2/8/3P2NP/6P1/PPPKBP1R/R5k1 b - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1251892
				"rn1q1bnr/ppp1kB1p/3p2p1/3NN3/4P3/8/PPPP1PPP/R1BbK2R b KQ -",
				//	http://www.chessgames.com/perl/chessgame?gid=1056921
				"r1b2rk1/pp3ppp/5n2/4q3/2P5/3n4/PP1NNPPP/R2QKB1R w KQ -",
				//	http://www.chessgames.com/perl/chessgame?gid=1124489
				"6rk/2pRPNpp/2p5/p4p2/6n1/q5P1/P3PP1P/6K1 b - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1651060
				"r4k2/p4p2/8/3p3r/B2Q2b1/R4pn1/1PP3NR/6BK w - -" };

		for ( String str : strFEN )
			assertTrue(Arbiter.isMated(BoardFactory.createFromFEN(str)));
		}

	@Test
	public void t_isStalemated()
		{
		String[] strFEN = {
				//	http://www.chessgames.com/perl/chessgame?gid=1089020
				"8/3R4/2p2Qpk/2P1p2p/4P2P/8/6K1/2R5 b - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1252040
				"8/6k1/8/1p2p2p/1P2Pn1P/5Pq1/4r3/7K w - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1003533
				"k7/2Q2p2/5p2/KP3P1p/P6P/8/8/8 b - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1135871
				"8/k1N5/8/1R6/8/1P6/7K/8 b - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1003162
				"1R6/8/8/8/p2R4/k7/8/1K6 b - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1255706
				"6k1/6p1/7p/8/1p6/p1qp4/8/3K4 w - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1341430
				"8/8/1P6/8/6p1/5kP1/7P/4R1K1 b - -",
				//	http://www.chessgames.com/perl/chessgame?gid=1070210
				"8/8/7k/2P1Q3/4B3/6K1/8/8 b - -" };

		for ( String str : strFEN )
			assertTrue(Arbiter.isStalemated(BoardFactory.createFromFEN(str)));
		}

	} /* end of class TestArbiter */
