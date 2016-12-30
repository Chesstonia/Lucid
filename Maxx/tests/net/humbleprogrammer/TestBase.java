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
package net.humbleprogrammer;

import net.humbleprogrammer.humble.StrUtil;
import net.humbleprogrammer.maxx.*;
import net.humbleprogrammer.maxx.epd.EPD;
import net.humbleprogrammer.maxx.factories.*;

import org.slf4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.humbleprogrammer.maxx.Constants.FEN_INITIAL;

/**
 * The {@link TestBase} class contains all of the test configuration
 * information.
 */
@SuppressWarnings( "unused" )
public abstract class TestBase
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	@SuppressWarnings( "WeakerAccess" )
	protected enum Duration
		{
			QUICK,
			NORMAL,
			SLOW,
			EPIC,
			UNLIMITED
		}

	/** Test duration. */
	protected static final Duration DURATION;

	/** Square index used for range testing. */
	protected static final int SQ_LO = -512;
	/** Square index used for range testing. */
	protected static final int SQ_HI = +512;

	/** Default file encoding. */
	private static final   String ENCODING      = "UTF-8";
	/** Test file for FEN strings. */
	private static final   String FEN_TEST_FILE = "P:\\Chess\\Test Data\\FEN-Test.txt";
	/**
	 * Sample game.
	 *
	 * This game was chosen because it has a variety of moves: castling on both sides, an e.p.
	 * capture, promotion, underpromotion, pinned pieces, and SAN moves that require rank/file
	 * disambiguation.
	 */
	protected static final String SAMPLE_PGN    = "[Event \"M.I.Chigorin Memorial Open\"]\n" +
												  "[Site \"St.Petersburg (Russia)\"]\n" +
												  "[Date \"1998.??.??\"]\n" +
												  "[Round \"6.10\"]\n" +
												  "[White \"Rusanov, M \"]\n" +
												  "[Black \"Voitsekhovsky, S \"]\n" +
												  "[Result \"0-1\"]\n" + "[ECO \"A45\"]\n" +
												  "[Opening \"Trompovsky attack (Ruth, Opovcensky opening)\"]\n" +
												  "\n" +
												  "1. d4 Nf6 2. Bg5 d5 3. Bxf6 exf6 4. e3 Be6 5. Nd2 c6 6. c3 f5 7. Bd3 Nd7 8. Qf3 \n" +
												  "g6 9. Ne2 Bd6 10. Nf4 Qc7 11. O-O {castle short} Nf6 12. h3 h5 13. Nxe6 fxe6 14.\n" +
												  "c4 g5 15. Qe2 g4 16. c5 Be7 17. f4 gxf3 {e.p.} 18. Nxf3 O-O-O {castle long} 19. \n" +
												  "b4 Rdg8 {file specifier} 20. b5 cxb5 21. Rfb1 Qg3 22. Rxb5 Qxh3 {pawn on g2 is\n" +
												  "pinned} 23. Ne1 Bd8 24. Rxb7 Bc7 25. Rxc7+ {check} Kxc7 26. Qd2 Kd7 27. Rb1 Ne4 \n" +
												  "28. Bxe4 fxe4 29. c6+ Ke7 30. Rb2 Qh4 31. Qb4+ Kf6 32. Rf2+ Kg6 33. Qd6 {pawn on\n" +
												  "e6 is pinned} Re8 34. c7 Qe7 35. c8=N {under-promotion} Qxd6 36. Nxd6 Re7 37. \n" +
												  "Rc2 Rb8 38. Rc6 Rb2 39. a4 Ra2 40. Nc8 Rb7 41. Rxe6+ Kf7 42. Nd6+ Kxe6 43. Nxb7 \n" +
												  "h4 44. Kf1 Ke7 45. Nc5 Kd6 46. Nb7+ Kc6 47. Nd8+ Kb6 48. Ne6 Rxa4 49. Nf4 Kc6 \n" +
												  "50. Ke2 Ra2+ 51. Kd1 Ra1+ 52. Kd2 a5 53. Nc2 Rg1 54. Ne1 a4 55. Nc2 Kb5 56. Kc3 \n" +
												  "Rxg2 57. Nb4 a3 58. Nxg2 h3 59. Ne1 h2 60. Nec2 Ka4 61. Nxd5 h1=Q {promotion} \n" +
												  "62. Nb6+ Kb5 63. Nc4 a2 64. N4a3+ {rank specifier} Ka4 65. Kb2 Qh7 66. Kxa2 Qf7+ \n" +
												  "67. Kb2 Qb7+ 68. Kc3 Qb3+ 69. Kd2 Qd3+ 70. Ke1 Kb3 71. Kf2 Qd1 72. Kg3 Qf3+ 73. \n" +
												  "Kh4 Qf5 74. Kg3 Kc3 75. Ne1 Kd2 76. Nac2 Ke2 77. Ng2 Qg5+ 0-1\n";

	/** Sample game as a list of SAN moves. */
	protected static final String[] SAMPLE_MOVES =
		{
			"d4", "Nf6", "Bg5", "d5", "Bxf6", "exf6", "e3", "Be6", "Nd2",
			"c6", "c3", "f5", "Bd3", "Nd7", "Qf3", "g6", "Ne2", "Bd6", "Nf4", "Qc7", "O-O", "Nf6", "h3", "h5", "Nxe6",
			"fxe6", "c4", "g5", "Qe2", "g4", "c5", "Be7", "f4", "gxf3", "Nxf3", "O-O-O", "b4", "Rdg8", "b5", "cxb5",
			"Rfb1", "Qg3", "Rxb5", "Qxh3", "Ne1", "Bd8", "Rxb7", "Bc7", "Rxc7+", "Kxc7", "Qd2", "Kd7", "Rb1", "Ne4",
			"Bxe4", "fxe4", "c6+", "Ke7", "Rb2", "Qh4", "Qb4+", "Kf6", "Rf2+", "Kg6", "Qd6", "Re8", "c7", "Qe7", "c8=N",
			"Qxd6", "Nxd6", "Re7", "Rc2", "Rb8", "Rc6", "Rb2", "a4", "Ra2", "Nc8", "Rb7", "Rxe6+", "Kf7", "Nd6+",
			"Kxe6", "Nxb7", "h4", "Kf1", "Ke7", "Nc5", "Kd6", "Nb7+", "Kc6", "Nd8+", "Kb6", "Ne6", "Rxa4", "Nf4", "Kc6",
			"Ke2", "Ra2+", "Kd1", "Ra1+", "Kd2", "a5", "Nc2", "Rg1", "Ne1", "a4", "Nc2", "Kb5", "Kc3", "Rxg2", "Nb4",
			"a3", "Nxg2", "h3", "Ne1", "h2", "Nec2", "Ka4", "Nxd5", "h1=Q", "Nb6+", "Kb5", "Nc4", "a2", "N4a3+", "Ka4",
			"Kb2", "Qh7", "Kxa2", "Qf7+", "Kb2", "Qb7+", "Kc3", "Qb3+", "Kd2", "Qd3+", "Ke1", "Kb3", "Kf2", "Qd1",
			"Kg3", "Qf3+", "Kh4", "Qf5", "Kg3", "Kc3", "Ne1", "Kd2", "Nac2", "Ke2", "Ng2", "Qg5+"
		};

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Maximum perft depth. */
	protected static final int  s_iMaxDepth;
	/** Maximum test duration, in nanoseconds. */
	protected static final long s_lMaxNanosecs;
	/** Logger. */
	protected static final Logger s_log    = LoggerFactory.getLogger( "MAXXTEST" );
	//
	// Sample position used for testing; after 17. f4
	//	    a   b   c   d   e   f   g   h
	//	  +---+---+---+---+---+---+---+---+
	//	8 |-r-|///|   |///|-k-|///|   |-r-| 8
	//	  +---+---+---+---+---+---+---+---+
	//	7 |-p-|-p-|-q-|   |-b-|   |///|   | 7
	//	  +---+---+---+---+---+---+---+---+
	//	6 |   |///|-p-|///|-p-|-n-|   |///| 6
	//	  +---+---+---+---+---+---+---+---+
	//	5 |///|   |=P=|-p-|///|-p-|///|-p-| 5
	//	  +---+---+---+---+---+---+---+---+
	//	4 |   |///|   |=P=|   |=P=|-p-|///| 4
	//	  +---+---+---+---+---+---+---+---+
	//	3 |///|   |///|=B=|=P=|   |///|=P=| 3
	//	  +---+---+---+---+---+---+---+---+
	//	2 |=P=|=P=|   |=N=|-Q-|///|=P=|///| 2
	//	  +---+---+---+---+---+---+---+---+
	//	1 |=R=|   |///|   |///|=R=|=K=|   | 1
	//	  +---+---+---+---+---+---+---+---+
	//	    a   b   c   d   e   f   g   h
	//
	/** Test position expressed as an EPD string, after 17. f4 */
	private static final   String EPD_TEST =
		"r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3";
	/** Test position expressed as an FEN string, after 17. f4 . */
	protected static final String FEN_TEST = EPD_TEST + " 0 17";

	/** Array of FEN strings, populated in {@link #getFEN()}. */
	private static List<String> s_listFEN;
	/** Array of PGN files, populated in {@link #getPGN()}. */
	private static List<Path>   s_listPGN;

	private static List<Move> s_listMoves;

	//  -----------------------------------------------------------------------
	//	TEST DATA
	//	-----------------------------------------------------------------------

	protected static final TestPosition[] s_positions = new TestPosition[]
		{
			//
			// http://chessprogramming.wikispaces.com/Perft+Results
			//
			new TestPosition( FEN_INITIAL,
							  new long[]{ 20L, 400L, 8902L, 197281L, 4865609L, 119060324L, 3195901860L } ),
			//	Position 2 AKA "Kiwipete"
			new TestPosition(
				"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -",
				new long[]{ 48L, 2039L, 97862L, 4085603L, 193690690L, 8031647685L, 374190009323L } ),
			//	Position 5
			new TestPosition( "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ -",
							  new long[]{ 44L, 1486L, 62379L, 2103487L, 89941194L, 3048196529L, 131724123591L } ),
			//
			// http://www.open-chess.org/viewtopic.php?f=3&t=2580
			//
			new TestPosition( "k7/7p/8/2nKb3/8/8/8/8 w - -",
							  new long[]{ 4L, 82L, 433L, 7658L, 43150L, 733006L, 3958415L } ),
			new TestPosition( "7K/k7/8/P7/6pP/8/8/8 w - -",
							  new long[]{ 5L, 25L, 164L, 909L, 6768L, 41398L, 319954L } ),
			new TestPosition( "8/8/8/4p3/5k2/8/4K3/8 w - -",
							  new long[]{ 6L, 42L, 224L, 1721L, 10261L, 76965L, 469317L } ),
			new TestPosition( "5K2/8/8/8/3k3p/8/7P/8 w - -",
							  new long[]{ 6L, 53L, 376L, 3166L, 21550L, 176062L, 1214321L } ),
			new TestPosition( "8/8/8/Pk5P/3p1n2/8/3K4/8 w - -",
							  new long[]{ 6L, 97L, 645L, 9117L, 62562L, 819521L, 5952823L } ),
			new TestPosition( "8/5p2/7k/6p1/5P1P/8/8/7K w - -",
							  new long[]{ 7L, 48L, 334L, 2441L, 17540L, 121688L, 872712L } ),
			new TestPosition( "8/1p6/4p3/1K2k3/8/P7/8/8 w - -",
							  new long[]{ 7L, 55L, 316L, 2854L, 18291L, 169628L, 1133246L } ),
			new TestPosition( "1B1b4/7K/1p6/1k6/8/8/8/8 w - -",
							  new long[]{ 12L, 142L, 1754L, 22037L, 275423L, 3626281L, 45772919L } ),
			new TestPosition( "8/1B4k1/8/p7/5bK1/8/7p/8 w - -",
							  new long[]{ 15L, 323L, 4342L, 85718L, 1104932L, 21678336L, 272371904L } ),
			new TestPosition( "5k2/4b1pp/8/8/5BK1/8/8/8 w - -",
							  new long[]{ 16L, 227L, 3201L, 46424L, 615016L, 9029887L, 118253653L } ),
			new TestPosition( "8/pKR5/8/p7/8/8/2p5/2k5 w - -",
							  new long[]{ 17L, 95L, 1727L, 14690L, 265447L, 2747984L, 48064246L } ),
			new TestPosition( "5k2/8/8/3R4/6K1/8/3b2pP/8 w - -",
							  new long[]{ 21L, 332L, 6169L, 100578L, 1805936L, 30669874L, 535087052L } ),
			new TestPosition( "k2N2K1/8/8/8/5R2/3n4/3p4/8 w - -",
							  new long[]{ 23L, 309L, 6673L, 90668L, 1933648L, 30567785L, 626952236L } ),
			new TestPosition( "8/6R1/r7/8/2K5/5k2/1P4p1/8 w - -",
							  new long[]{ 23L, 489L, 9388L, 202720L, 3677931L, 82179636L, 1443179182L } ),
			new TestPosition( "8/8/6k1/5p2/4q3/8/3Q4/3K4 w - -",
							  new long[]{ 23L, 585L, 9604L, 224795L, 4087490L, 89726430L, 1713104983L } ),
			new TestPosition( "8/k7/8/2R5/8/4q3/8/4B2K w - -",
							  new long[]{ 23L, 587L, 10041L, 234668L, 4173427L, 95031011L, 1730763310L } ),
			//
			//  http://www.talkchess.com/forum/viewtopic.php?p=509159#509159
			//
			new TestPosition( "r3k1rr/8/8/3PpP2/8/8/8/R3K2R w KQkq e6",
							  new long[]{ 29L, 829L, 20501L, 624871L, 15446339L, 485647607L, 12010433360L } ),
			new TestPosition( "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq -",
							  new long[]{ 6L, 264L, 9467L, 422333L, 15833292L, 706045033L, 27209691363L } ),
			new TestPosition( "1Rb2rk1/p5pp/8/p1PpN3/4K3/P5PB/7P/8 w - d6",
							  new long[]{ 5L, 117L, 3293L, 67197L, 1881089L, 38633283L, 1069189070L } ),
			new TestPosition( "1Q6/8/8/5K2/P2B2R1/2P5/3k4/8 w - -",
							  new long[]{ 50L, 279L, 13310L, 54703L, 2538084L, 10809689L, 493407574L } ),
			new TestPosition( "rnbqkb1r/ppppp1pp/7n/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6",
							  new long[]{ 31L, 570L, 17546L, 351806L, 11139762L, 244063299L, 7930902498L } ),
			new TestPosition(
				"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -",
				new long[]{ 48L, 2039L, 97862L, 4085603L, 193690690L, 8031647685L, 374190009323L } ),
			new TestPosition( "8/p7/8/1P6/K1k3p1/6P1/7P/8 w - -",
							  new long[]{ 5L, 39L, 237L, 2002L, 14062L, 120995L, 966152L } ),
			new TestPosition( "r3k2r/p6p/8/B7/1pp1p3/3b4/P6P/R3K2R w KQkq -",
							  new long[]{ 17L, 341L, 6666L, 150072L, 3186478L, 77054993L, 1740098841L } ),
			new TestPosition( "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -",
							  new long[]{ 14L, 191L, 2812L, 43238L, 674624L, 11030083L, 178633661L } ),
			new TestPosition( "8/1p6/8/P3k3/2K3p1/8/5P2/8 w - -",
							  new long[]{ 9L, 85L, 795L, 7658L, 72120L, 703851L, 6627106L } ),
			new TestPosition( "r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R w KQkq -",
							  new long[]{ 33L, 946L, 30962L, 899715L, 29179893L, 857918729L, 27638313532L } ),
			//
			// http://www.rocechess.ch/perftsuite.zip
			//
			new TestPosition( "7K/7p/7k/8/8/8/8/8 w - -",
							  new long[]{ 1L, 3L, 12L, 80L, 342L, 2343L, 12377L } ),
			new TestPosition( "K7/p7/k7/8/8/8/8/8 w - -",
							  new long[]{ 1L, 3L, 12L, 80L, 342L, 2343L, 12377L } ),
			new TestPosition( "8/8/8/8/8/4k3/4P3/4K3 w - -",
							  new long[]{ 2L, 8L, 44L, 282L, 1814L, 11848L, 83195L } ),
			new TestPosition( "6kq/8/8/8/8/8/8/7K w - -",
							  new long[]{ 2L, 36L, 143L, 3637L, 14893L, 391507L, 1750864L } ),
			new TestPosition( "7K/8/8/8/8/8/8/6kq w - -",
							  new long[]{ 2L, 36L, 143L, 3637L, 14893L, 391507L, 1750864L } ),
			new TestPosition( "8/8/8/8/8/K7/P7/k7 w - -",
							  new long[]{ 3L, 7L, 43L, 199L, 1347L, 6249L, 45628L } ),
			new TestPosition( "8/8/8/8/8/7K/7P/7k w - -",
							  new long[]{ 3L, 7L, 43L, 199L, 1347L, 6249L, 45628L } ),
			new TestPosition( "8/8/7k/7p/7P/7K/8/8 w - -",
							  new long[]{ 3L, 9L, 57L, 360L, 1969L, 10724L, 65679L } ),
			new TestPosition( "8/8/k7/p7/P7/K7/8/8 w - -",
							  new long[]{ 3L, 9L, 57L, 360L, 1969L, 10724L, 65679L } ),
			new TestPosition( "k7/8/8/3p4/4p3/8/8/7K w - -",
							  new long[]{ 3L, 15L, 84L, 573L, 3013L, 22886L, 128193L } ),
			new TestPosition( "4k2r/6K1/8/8/8/8/8/8 w k -",
							  new long[]{ 3L, 32L, 134L, 2073L, 10485L, 179869L, 954475L } ),
			new TestPosition( "K7/8/2n5/1n6/8/8/8/k6N w - -",
							  new long[]{ 3L, 51L, 345L, 5301L, 38348L, 588695L, 5041119L } ),
			new TestPosition( "k6N/8/8/8/1n6/2n5/8/K7 w - -",
							  new long[]{ 3L, 51L, 345L, 5301L, 38348L, 588695L, 5041119L } ),
			new TestPosition( "7k/8/8/3p4/8/3P4/8/K7 w - -",
							  new long[]{ 4L, 15L, 89L, 537L, 3309L, 21104L, 132804L } ),
			new TestPosition( "k7/8/3p4/8/3P4/8/8/7K w - -",
							  new long[]{ 4L, 15L, 90L, 534L, 3450L, 20960L, 141778L } ),
			new TestPosition( "k7/8/3p4/8/8/4P3/8/7K w - -",
							  new long[]{ 4L, 16L, 101L, 637L, 4271L, 28662L, 204279L } ),
			new TestPosition( "k7/8/6p1/8/8/7P/8/K7 w - -",
							  new long[]{ 4L, 16L, 101L, 637L, 4354L, 29679L, 216305L } ),
			new TestPosition( "k7/8/7p/8/8/6P1/8/K7 w - -",
							  new long[]{ 4L, 16L, 101L, 637L, 4354L, 29679L, 216305L } ),
			new TestPosition( "7k/8/4p3/8/8/3P4/8/K7 w - -",
							  new long[]{ 4L, 16L, 101L, 637L, 4271L, 28662L, 204279L } ),
			new TestPosition( "7k/8/p7/8/8/1P6/8/7K w - -",
							  new long[]{ 4L, 16L, 101L, 637L, 4354L, 29679L, 216305L } ),
			new TestPosition( "7k/8/1p6/8/8/P7/8/7K w - -",
							  new long[]{ 4L, 16L, 101L, 637L, 4354L, 29679L, 216305L } ),
			new TestPosition( "7k/3p4/8/8/3P4/8/8/K7 w - -",
							  new long[]{ 4L, 19L, 117L, 720L, 4661L, 32191L, 220314L } ),
			new TestPosition( "k7/3p4/8/8/3P4/8/8/7K w - -",
							  new long[]{ 4L, 19L, 117L, 712L, 4658L, 30749L, 213308L } ),
			new TestPosition( "r3k3/1K6/8/8/8/8/8/8 w q -",
							  new long[]{ 4L, 49L, 243L, 3991L, 20780L, 367724L, 1971278L } ),
			new TestPosition( "8/8/4K3/3Nn3/3nN3/3k4/8/8 w - -",
							  new long[]{ 4L, 68L, 1118L, 16199L, 281190L, 4405103L, 75214812L } ),
			new TestPosition( "7k/8/8/4P3/3P4/8/8/K7 w - -",
							  new long[]{ 5L, 15L, 102L, 569L, 4337L, 22579L, 184873L } ),
			new TestPosition( "7k/8/8/3p4/8/8/3P4/K7 w - -",
							  new long[]{ 5L, 19L, 116L, 716L, 4786L, 30980L, 204340L } ),
			new TestPosition( "k7/8/8/3p4/8/8/3P4/7K w - -",
							  new long[]{ 5L, 19L, 117L, 720L, 5014L, 32167L, 226157L } ),
			new TestPosition( "k7/8/8/6p1/7P/8/8/K7 w - -",
							  new long[]{ 5L, 22L, 139L, 877L, 6112L, 41874L, 304498L } ),
			new TestPosition( "k7/8/8/7p/6P1/8/8/K7 w - -",
							  new long[]{ 5L, 22L, 139L, 877L, 6112L, 41874L, 304498L } ),
			new TestPosition( "7k/8/8/1p6/P7/8/8/7K w - -",
							  new long[]{ 5L, 22L, 139L, 877L, 6112L, 41874L, 304498L } ),
			new TestPosition( "7k/8/8/p7/1P6/8/8/7K w - -",
							  new long[]{ 5L, 22L, 139L, 877L, 6112L, 41874L, 304498L } ),
			new TestPosition( "k7/6p1/8/8/8/8/7P/K7 w - -",
							  new long[]{ 5L, 25L, 161L, 1035L, 7574L, 55338L, 419956L } ),
			new TestPosition( "k7/7p/8/8/8/8/6P1/K7 w - -",
							  new long[]{ 5L, 25L, 161L, 1035L, 7574L, 55338L, 419956L } ),
			new TestPosition( "8/8/3k4/3p4/3P4/3K4/8/8 w - -",
							  new long[]{ 5L, 25L, 180L, 1294L, 8296L, 53138L, 345129L } ),
			new TestPosition( "8/8/8/8/3p2k1/3Pp3/2K1P3/8 w - -",
							  new long[]{ 5L, 35L, 182L, 1091L, 5408L, 34822L, 186948L } ),
			new TestPosition( "4k2r/8/8/8/8/8/8/4K3 w k -",
							  new long[]{ 5L, 75L, 459L, 8290L, 47635L, 899442L, 5353768L } ),
			new TestPosition( "r3k3/8/8/8/8/8/8/4K3 w q -",
							  new long[]{ 5L, 80L, 493L, 8897L, 52710L, 1001523L, 5938701L } ),
			new TestPosition( "r3k2r/8/8/8/8/8/8/4K3 w kq -",
							  new long[]{ 5L, 130L, 782L, 22180L, 118882L, 3517770L, 19168414L } ),
			new TestPosition( "K7/8/8/3Q4/4q3/8/8/7k w - -",
							  new long[]{ 6L, 35L, 495L, 8349L, 166741L, 3370175L, 68590202L } ),
			new TestPosition( "7K/8/8/4Q3/3q4/8/8/k7 w - -",
							  new long[]{ 6L, 35L, 495L, 8349L, 166741L, 3370175L, 68590202L } ),
			new TestPosition( "B6b/8/5K2/2k5/8/8/8/b6B w - -",
							  new long[]{ 6L, 106L, 1829L, 31151L, 530585L, 9250746L, 160677837L } ),
			new TestPosition( "8/2k1p3/3pP3/3P2K1/8/8/8/8 w - -",
							  new long[]{ 7L, 35L, 210L, 1091L, 7028L, 34834L, 221609L } ),
			new TestPosition( "3k4/3pp3/8/8/8/8/3PP3/3K4 w - -",
							  new long[]{ 7L, 49L, 378L, 2902L, 24122L, 199002L, 1694225L } ),
			new TestPosition( "K7/b7/1b6/1b6/8/8/8/k6B w - -",
							  new long[]{ 7L, 143L, 1416L, 31787L, 310862L, 7382896L, 75429328L } ),
			new TestPosition( "k6B/8/8/8/1b6/1b6/b7/K7 w - -",
							  new long[]{ 7L, 143L, 1416L, 31787L, 310862L, 7382896L, 75429328L } ),
			new TestPosition( "8/8/3k4/3p4/8/3P4/3K4/8 w - -",
							  new long[]{ 8L, 61L, 411L, 3213L, 21637L, 158065L, 1055522L } ),
			new TestPosition( "8/3k4/3p4/8/3P4/3K4/8/8 w - -",
							  new long[]{ 8L, 61L, 483L, 3213L, 23599L, 157093L, 1144376L } ),
			new TestPosition( "8/6kP/8/8/8/8/pK6/8 w - -",
							  new long[]{ 11L, 97L, 887L, 8048L, 90606L, 1030499L, 13644504L } ),
			new TestPosition( "8/Pk6/8/8/8/8/6Kp/8 w - -",
							  new long[]{ 11L, 97L, 887L, 8048L, 90606L, 1030499L, 13644504L } ),
			new TestPosition( "8/1k6/8/5N2/8/4n3/8/2K5 w - -",
							  new long[]{ 11L, 156L, 1636L, 20534L, 223507L, 2594412L, 28293654L } ),
			new TestPosition( "8/8/8/8/8/8/6k1/4K2R w K -",
							  new long[]{ 12L, 38L, 564L, 2219L, 37735L, 185867L, 3329588L } ),
			new TestPosition( "8/1n4N1/2k5/8/8/5K2/1N4n1/8 w - -",
							  new long[]{ 14L, 195L, 2760L, 38675L, 570726L, 8107539L, 121484034L } ),
			new TestPosition( "8/8/8/8/8/8/1k6/R3K3 w Q -",
							  new long[]{ 15L, 65L, 1018L, 4573L, 80619L, 413018L, 7573823L } ),
			new TestPosition( "4k3/8/8/8/8/8/8/4K2R w K -",
							  new long[]{ 15L, 66L, 1197L, 7059L, 133987L, 764643L, 14805594L } ),
			new TestPosition( "8/1n4N1/5k2/8/8/2K5/1N4n1/8 w - -",
							  new long[]{ 15L, 193L, 2816L, 40039L, 582642L, 8503277L, 124446964L } ),
			new TestPosition( "4k3/8/8/8/8/8/8/R3K3 w Q -",
							  new long[]{ 16L, 71L, 1287L, 7626L, 145232L, 846648L, 16460756L } ),
			new TestPosition( "2k5/8/4N3/8/5n2/8/1K6/8 w - -",
							  new long[]{ 16L, 180L, 2290L, 24640L, 288141L, 3147566L, 36049251L } ),
			new TestPosition( "k7/8/2N5/1N6/8/8/8/K6n w - -",
							  new long[]{ 17L, 54L, 835L, 5910L, 92250L, 688780L, 10553652L } ),
			new TestPosition( "K6n/8/8/8/1N6/2N5/8/k7 w - -",
							  new long[]{ 17L, 54L, 835L, 5910L, 92250L, 688780L, 10553652L } ),
			new TestPosition( "B6b/8/8/8/2K5/4k3/8/b6B w - -",
							  new long[]{ 17L, 278L, 4607L, 76778L, 1320507L, 22823890L, 408424597L } ),
			new TestPosition( "7k/2b1B3/8/7K/7B/1b6/8/8 w - -",
							  new long[]{ 17L, 309L, 5133L, 93603L, 1591064L, 29027891L, 499360711L } ),
			new TestPosition( "8/PPPk4/8/8/8/8/4Kppp/8 w - -",
							  new long[]{ 18L, 270L, 4699L, 79355L, 1533145L, 28859283L, 614154982L } ),
			new TestPosition( "8/4kPPP/8/8/8/8/pppK4/8 w - -",
							  new long[]{ 18L, 270L, 4699L, 79355L, 1533145L, 28859283L, 614154982L } ),
			new TestPosition( "7k/RR6/8/8/8/8/rr6/7K w - -",
							  new long[]{ 19L, 275L, 5300L, 104342L, 2161211L, 44956585L, 974704196L } ),
			new TestPosition( "8/8/4k3/3Nn3/3nN3/4K3/8/8 w - -",
							  new long[]{ 19L, 289L, 4442L, 73584L, 1198299L, 19870403L, 320053913L } ),
			new TestPosition( "k7/B7/1B6/1B6/8/8/8/K6b w - -",
							  new long[]{ 21L, 144L, 3242L, 32955L, 787524L, 7881673L, 194492651L } ),
			new TestPosition( "K6b/8/8/8/1B6/1B6/B7/k7 w - -",
							  new long[]{ 21L, 144L, 3242L, 32955L, 787524L, 7881673L, 194492651L } ),
			new TestPosition( "8/8/1B6/7b/7k/8/2B1b3/7K w - -",
							  new long[]{ 21L, 316L, 5744L, 93338L, 1713368L, 28861171L, 531840753L } ),
			new TestPosition( "7k/8/8/8/8/8/8/6QK w - -",
							  new long[]{ 22L, 43L, 1015L, 4167L, 105749L, 419369L, 10830989L } ),
			new TestPosition( "n1n5/1Pk5/8/8/8/8/5Kp1/5N1N w - -",
							  new long[]{ 24L, 421L, 7421L, 124608L, 2193768L, 37665329L, 690692460L } ),
			new TestPosition( "5n1n/5kP1/8/8/8/8/1pK5/N1N5 w - -",
							  new long[]{ 24L, 421L, 7421L, 124608L, 2193768L, 37665329L, 690692460L } ),
			new TestPosition( "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N w - -",
							  new long[]{ 24L, 496L, 9483L, 182838L, 3605103L, 71179139L, 1482218224L } ),
			new TestPosition( "5n1n/4kPPP/8/8/8/8/pppK4/N1N5 w - -",
							  new long[]{ 24L, 496L, 9483L, 182838L, 3605103L, 71179139L, 1482218224L } ),
			new TestPosition( "r3k2r/8/8/8/8/8/8/R3K1R1 w Qkq -",
							  new long[]{ 25L, 547L, 13579L, 316214L, 7878456L, 189224276L, 4746418097L } ),
			new TestPosition( "r3k2r/8/8/8/8/8/8/2R1K2R w Kkq -",
							  new long[]{ 25L, 548L, 13502L, 312835L, 7736373L, 184411439L, 4594253079L } ),
			new TestPosition( "r3k2r/8/8/8/8/8/8/R3K2R w KQkq -",
							  new long[]{ 26L, 568L, 13744L, 314346L, 7594526L, 179862938L, 4408318687L } ),
			new TestPosition( "2r1k2r/8/8/8/8/8/8/R3K2R w KQk -",
							  new long[]{ 25L, 560L, 13592L, 317324L, 7710115L, 185959088L, 4569222578L } ),
			new TestPosition( "r3k1r1/8/8/8/8/8/8/R3K2R w KQq -",
							  new long[]{ 25L, 560L, 13607L, 320792L, 7848606L, 190755813L, 4720992739L } ),
			new TestPosition( "r3k2r/8/8/8/8/8/8/1R2K2R w Kkq -",
							  new long[]{ 25L, 567L, 14095L, 328965L, 8153719L, 195629489L, 4887262145L } ),
			new TestPosition( "4k3/8/8/8/8/8/8/R3K2R w KQ -",
							  new long[]{ 26L, 112L, 3189L, 17945L, 532933L, 2788982L, 84866591L } ),
			new TestPosition( "1r2k2r/8/8/8/8/8/8/R3K2R w KQk -",
							  new long[]{ 26L, 583L, 14252L, 334705L, 8198901L, 198328929L, 4908056073L } ),
			new TestPosition( "R6r/8/8/2K5/5k2/8/8/r6R w - -",
							  new long[]{ 36L, 1027L, 29215L, 771461L, 20506480L, 525169084L, 13554890298L } ),
			new TestPosition( "R6r/8/8/5K2/2k5/8/8/r6R w - -",
							  new long[]{ 36L, 1027L, 29227L, 771368L, 20521342L, 524966748L, 13566510069L } ),
			//
			//	https://github.com/ankan-ban/perft_cpu
			//
			new TestPosition(
				"r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -",
				new long[]{ 48L, 2039L, 97862L, 4085603L, 193690690L, 8031647685L, 374190009323L } ),
			//
			// http://www.talkchess.com/forum/viewtopic.php?t=47318
			//
			// self stalemate
			new TestPosition( "K1k5/8/P7/8/8/8/8/8 w - -",
							  new long[]{ 2L, 6L, 13L, 63L, 382L, 2217L, 15453L } ),
			// underpromote to check
			new TestPosition( "8/P1k5/K7/8/8/8/8/8 w - -",
							  new long[]{ 6L, 27L, 273L, 1329L, 18135L, 92683L, 1555980L } ),
			//  avoid illegal en passant capture
			new TestPosition( "8/5bk1/8/2Pp4/8/1K6/8/8 w - d6",
							  new long[]{ 8L, 104L, 736L, 9287L, 62297L, 824064L, 5580696L } ),
			// promote to give check
			new TestPosition( "4k3/1P6/8/8/8/8/K7/8 w - -",
							  new long[]{ 9L, 40L, 472L, 2661L, 38983L, 217342L, 3742283L } ),
			// stalemate/checkmate
			new TestPosition( "8/k1P5/8/1K6/8/8/8/8 w - -",
							  new long[]{ 10L, 25L, 268L, 926L, 10857L, 43261L, 567584L } ),
			// promote out of check
			new TestPosition( "2K2r2/4P3/8/8/8/8/8/3k4 w - -",
							  new long[]{ 11L, 133L, 1442L, 19174L, 266199L, 3821001L, 60651209L } ),
			//  short castling gives check
			new TestPosition( "5k2/8/8/8/8/8/8/4K2R w K -",
							  new long[]{ 15L, 66L, 1198L, 6399L, 120330L, 661072L, 12762196L } ),
			//  en passant capture checks opponent
			new TestPosition( "8/5k2/8/2Pp4/2B5/1K6/8/8 w - d6",
							  new long[]{ 15L, 126L, 1928L, 13931L, 206379L, 1440467L, 21190412L } ),
			// long castling gives check
			new TestPosition( "3k4/8/8/8/8/8/8/R3K3 w Q -",
							  new long[]{ 16L, 71L, 1286L, 7418L, 141077L, 803711L, 15594314L } ),
			// contributed
			new TestPosition( "8/8/8/8/k1p4R/8/3P4/3K4 w - -",
							  new long[]{ 18L, 92L, 1670L, 10138L, 185429L, 1134888L, 20757544L } ),
			// castling (including losing castling rights due to rook capture)
			new TestPosition( "r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq -",
							  new long[]{ 26L, 1141L, 27826L, 1274206L, 31912360L, 1509218880L, 38604512464L } ),
			// discovered check
			new TestPosition( "5K2/8/1Q6/2N5/8/1p2k3/8/8 w - -",
							  new long[]{ 29L, 165L, 5160L, 31961L, 1004658L, 6334638L, 197013195L } ),
			//  double check
			new TestPosition( "8/5k2/8/5N2/5Q2/2K5/8/8 w - -",
							  new long[]{ 37L, 183L, 6559L, 23527L, 811573L, 3114998L, 104644508L } ),
			// castling prevented
			new TestPosition( "r3k2r/8/5Q2/8/8/3q4/8/R3K2R w KQkq -",
							  new long[]{ 44L, 1494L, 50509L, 1720476L, 58773923L, 2010267707L, 69145944208L } ),
			//
			//  http://www.chess.com/forum/view/more-puzzles/mate-in-292-movesblathys-monster
			//
			new TestPosition( "q5nn/1p2p3/p1k1P1p1/6Pp/PKp1p1pP/8/2P1P1PP/3Q4 w - -",
							  new long[]{ 23L, 351L, 7416L, 130826L, 2610361L, 49832214L, 956434803L } ),
			//
			//  Assorted positions from my own testing
			//
			// checking pawn on f6 can be captured via e.p.
			new TestPosition( "3r1rk1/6pp/4n3/ppbNPp2/3nKP2/7P/PB6/2RR1B2 w - f6",
							  new long[]{ 3L, 98L, 2838L, 92186L, 2764109L, 92969197L, 2892953940L } ),
			// 3 pinned pieces
			new TestPosition( "r3k2r/Pppp1ppp/1b3nbN/nP6/BBPNP3/6q1/P2PQ1PP/Rq3RK1 w kq -",
							  new long[]{ 35L, 1892L, 64973L, 3321449L, 120745830L, 5966580991L, 225409779045L } ),
			new TestPosition( "r4rk1/pp1nq1p1/3bp2p/3p1pP1/2pP1P1P/2P1PN2/PPQ1B3/R3K2R w KQ f6",
							  new long[]{ 39L, 1484L, 55740L, 2070681L, 76647022L, 2803944626L, 102930720732L } ),
			//	Only legal move is promotion
			new TestPosition( "8/5P2/6n1/8/8/1p6/2k5/K7 w - -",
							  new long[]{ 4L, 48L, 478L, 4888L, 70115L, 686398L, 11057131L } ),
			new TestPosition( "8/4P3/8/1p4n1/1P6/6k1/6p1/6K1 w - -",
							  new long[]{ 4L, 44L, 420L, 3808L, 52988L, 498518L, 7702653L } ),
			//	Only legal move is e.p. capture
			new TestPosition( "2kb3r/pp4p1/2p3p1/4Pp2/2Pn1NK1/1P4BP/P5P1/2r2B1R w - f6",
							  new long[]{ 1L, 38L, 823L, 29985L, 714223L, 25194145L, 630840166L } ),
			new TestPosition( "r2r4/np2k1pp/4p3/p1b1Pp2/4KP2/P4N2/1P4PP/R1B4R w - f6",
							  new long[]{ 1L, 7L, 176L, 6110L, 154750L, 5182177L, 135500849L } ),
			new TestPosition( "8/8/p7/Ppb5/K1k5/8/8/4B3 w - b6",
							  new long[]{ 1L, 14L, 129L, 1764L, 18363L, 251278L, 2978207L } ),

			// Really open positions, with lots of depth 0 moves
			new TestPosition( "r3k1br/R7/3Q4/7R/4B3/2B5/4K3/8 w k -",
							  new long[]{ 85L, 1092L, 77449L, 1324003L, 84952445L, 1662371924L, 98898300898L } ),
			new TestPosition( "QQ2Q3/6p1/5k2/5p2/8/5Q1p/3R3P/2R4K w - -",
							  new long[]{ 92L, 332L, 29595L, 105610L, 9217437L, 34237036L, 2939802528L } ),
			new TestPosition( "5Q2/P3R3/7Q/5K2/2Q5/1P6/1k6/6Q1 w - -",
							  new long[]{ 99L, 223L, 20837L, 43188L, 3882877L, 9443512L, 828139724L } ),
			new TestPosition( "5Q2/PP2R3/7Q/5K2/2Q5/8/1k6/6Q1 w - -",
							  new long[]{ 103L, 153L, 14619L, 26728L, 2413870L, 6029169L, 529459567L } ),
			new TestPosition( "1Q3Q2/P3R3/7Q/5K2/2Q5/k7/8/6Q1 w - -",
							  new long[]{ 115L, 38L, 3992L, 3988L, 400124L, 568836L, 55143907L } )
		};

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	static
		{
		DURATION = getDuration();

		switch (DURATION)
			{
			case QUICK:
				s_iMaxDepth = 3;
				s_lMaxNanosecs = TimeUnit.SECONDS.toNanos( 15 );
				break;
			case NORMAL:
				s_iMaxDepth = 4;
				s_lMaxNanosecs = TimeUnit.MINUTES.toNanos( 1 );
				break;
			case SLOW:
				s_iMaxDepth = 5;
				s_lMaxNanosecs = TimeUnit.MINUTES.toNanos( 15 );
				break;
			case EPIC:
				s_iMaxDepth = 6;
				s_lMaxNanosecs = TimeUnit.HOURS.toNanos( 1 );
				break;
			default:
				s_iMaxDepth = 7;
				s_lMaxNanosecs = Long.MAX_VALUE;
				break;
			}
		}

	//  -----------------------------------------------------------------------
	//	METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Reads the test duration from the TEST_DURATION environment variable.
	 *
	 * @return Test duration.
	 */
	private static Duration getDuration()
		{
		try
			{
			String strDuration = System.getenv( "TEST_DURATION" );

			if (!StrUtil.isBlank( strDuration )) return Duration.valueOf( strDuration );
			}
		catch (Exception ignored)
			{

			}

		return Duration.NORMAL;
		}

	/**
	 * Gets a set of FEN positions.
	 *
	 * @return Collection of FEN positions.
	 */
	protected static synchronized List<String> getEPD( String strPath )
		{
		String strEPD;
		List<String> list = new ArrayList<>();

		try (BufferedReader reader = openTestFile( strPath ))
			{
			while ( (strEPD = reader.readLine()) != null )
				if (EPD.matchEPD( strEPD ) != null)
					list.add( strEPD );

			reader.close();
			}
		catch (IOException ex)
			{
			s_log.warn( "Failed to read from {}: {}", FEN_TEST_FILE, ex.getMessage() );
			}


		return list;
		}

	/**
	 * Gets a set of FEN positions.
	 *
	 * @return Collection of FEN positions.
	 */
	protected static synchronized List<String> getFEN()
		{
		if (s_listFEN == null)
			{
			String strFEN;
			List<String> list = new ArrayList<>();

			try (BufferedReader reader = openTestFile( FEN_TEST_FILE ))
				{
				while ( (strFEN = reader.readLine()) != null )
					if (Parser.matchFEN( strFEN ) != null) list.add( strFEN );

				reader.close();
				}
			catch (IOException ex)
				{
				s_log.warn( "Failed to read from {}: {}", FEN_TEST_FILE, ex.getMessage() );
				}

			s_listFEN = list;
			s_log.debug( "Found {} FEN samples.", list.size() );
			}

		return Collections.unmodifiableList( s_listFEN );
		}

	/**
	 * Builds a list of valid moves.
	 *
	 * @return Moves
	 */
	protected static synchronized List<Move> getMoves()
		{
		if (s_listMoves == null)
			{
			Board bd = BoardFactory.createInitial();
			List<Move> list = new ArrayList<>();

			for ( String str : SAMPLE_MOVES )
				{
				Move move = MoveFactory.fromSAN( bd, str );

				list.add( move );
				bd.makeMove( move );
				}

			s_listMoves = list;
			}

		return s_listMoves;
		}

	/**
	 * Builds a set of *.pgn files.
	 *
	 * @return Collection of files.
	 */
	protected static synchronized List<Path> getPGN()
		{
		if (s_listPGN == null)
			{
			List<Path> list = new ArrayList<>();
			Path pathPgnRoot = Paths.get( "P:\\Chess\\PGN\\TWIC" );

			try
				{
				DirectoryStream<Path> stream = Files.newDirectoryStream( pathPgnRoot, "*.pgn" );

				for ( Path path : stream )
					list.add( path );

				stream.close();
				}
			catch (IOException ex)
				{
				s_log.warn( "Failed to find PGN files: {}", ex.getMessage() );
				}

			Collections.sort( list );
			s_listPGN = list;

			s_log.debug( "Found {} PGN files.", list.size() );
			}

		return Collections.unmodifiableList( s_listPGN );
		}

	/**
	 * Opens a reader for a file in the system Test Data directory.
	 *
	 * @param strFilename
	 * 	Name of file to open.
	 *
	 * @return Reader object if file found, <code>null</code> otherwise.
	 */

	private static BufferedReader openTestFile( final String strFilename )
		{
		BufferedReader reader = null;

		try
			{
			final Path path = Paths.get( "" ).resolve( strFilename );
			final FileInputStream fis = new FileInputStream( path.toFile() );
			final InputStreamReader isr = new InputStreamReader( fis, ENCODING );

			reader = new BufferedReader( isr );
			}
		catch (IOException ex)
			{
			s_log.warn( "Failed to open test file '{}'", strFilename );
			s_log.error( ex.getMessage(), ex );
			}

		return reader;
		}
	} /* end of class TestBase */
