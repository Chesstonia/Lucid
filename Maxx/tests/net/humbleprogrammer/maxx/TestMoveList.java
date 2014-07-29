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
package net.humbleprogrammer.maxx;

import net.humbleprogrammer.TestBase;
import net.humbleprogrammer.humble.Stopwatch;
import net.humbleprogrammer.humble.TimeUtil;
import org.junit.*;

import java.util.concurrent.TimeUnit;

import static net.humbleprogrammer.maxx.Constants.*;
import static org.junit.Assert.*;

public class TestMoveList extends TestBase
    {

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Total number of moves generated. */
    private static long s_lNetMoves    = 0L;
    /** Total number of nanoseconds spent generating moves. */
    private static long s_lNetNanosecs = 0L;

    //  -----------------------------------------------------------------------
    //	UNIT TESTS
    //	-----------------------------------------------------------------------

    @Test
    public void t_ctor_blank()
        {
        final MoveList moves = new MoveList( BoardFactory.createBlank() );

        assertFalse( moves.hasLegalMove() );
        }

    @Test(expected = IllegalArgumentException.class)
    public void t_ctor_fail()
        {
        new MoveList( null );
        }

    @Test
    public void t_count_checkmates()
        {
        //	http://www.chessgames.com/perl/chessgame?gid=1274437
        assertEquals( 0L,
                      countMoves( "r6r/pppk1ppp/8/8/2P5/2NbbN2/PPnK1nPP/1RB2B1R w - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1028832
        assertEquals( 0L,
                      countMoves( "r4r1k/pp4pp/3p4/3B4/8/1QN3Pb/PP3nKP/R5R1 w - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1483810
        assertEquals( 0L,
                      countMoves( "2r3k1/4Rppp/8/p7/Kb3PP1/1Pn4P/8/7R w - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1259009
        assertEquals( 0L,
                      countMoves(
                          "rn3r2/pbppq1p1/1p2pN2/8/3P2NP/6P1/PPPKBP1R/R5k1 b - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1251892
        assertEquals( 0L,
                      countMoves(
                          "rn1q1bnr/ppp1kB1p/3p2p1/3NN3/4P3/8/PPPP1PPP/R1BbK2R b KQ -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1056921
        assertEquals( 0L,
                      countMoves(
                          "r1b2rk1/pp3ppp/5n2/4q3/2P5/3n4/PP1NNPPP/R2QKB1R w KQ -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1124489
        assertEquals( 0L,
                      countMoves( "6rk/2pRPNpp/2p5/p4p2/6n1/q5P1/P3PP1P/6K1 b - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1651060
        assertEquals( 0L,
                      countMoves( "r4k2/p4p2/8/3p3r/B2Q2b1/R4pn1/1PP3NR/6BK w - -" ) );
        }

    @Test
    public void t_count_long()
        {
        assertEquals( 85,
                      countMoves( "r3k1br/R7/3Q4/7R/4B3/2B5/4K3/8 w k -" ) );
        assertEquals( 218,
                      countMoves( "3Q4/1Q4Q1/4Q3/2Q4R/Q4Q2/3Q4/1Q4Rp/1K1BBNNk w - -" ) );
        assertEquals( 218,
                      countMoves( "R6R/3Q4/1Q4Q1/4Q3/2Q4Q/Q4Q2/pp1Q4/kBNN1KB1 w - -" ) );
        }

    @Test
    public void t_count_stalemates()
        {
        //	http://www.chessgames.com/perl/chessgame?gid=1089020
        assertEquals( 0L,
                      countMoves( "8/3R4/2p2Qpk/2P1p2p/4P2P/8/6K1/2R5 b - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1252040
        assertEquals( 0L,
                      countMoves( "8/6k1/8/1p2p2p/1P2Pn1P/5Pq1/4r3/7K w - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1003533
        assertEquals( 0L,
                      countMoves( "k7/2Q2p2/5p2/KP3P1p/P6P/8/8/8 b - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1135871
        assertEquals( 0L,
                      countMoves( "8/k1N5/8/1R6/8/1P6/7K/8 b - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1003162
        assertEquals( 0L,
                      countMoves( "1R6/8/8/8/p2R4/k7/8/1K6 b - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1255706
        assertEquals( 0L,
                      countMoves( "6k1/6p1/7p/8/1p6/p1qp4/8/3K4 w - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1341430
        assertEquals( 0L,
                      countMoves( "8/8/1P6/8/6p1/5kP1/7P/4R1K1 b - -" ) );
        //	http://www.chessgames.com/perl/chessgame?gid=1070210
        assertEquals( 0L,
                      countMoves( "8/8/7k/2P1Q3/4B3/6K1/8/8 b - -" ) );
        }

    @Test
    public void t_perft()
        {
        int iMaxDepth = 0;
        long lMillisecs;

        for ( TestPosition position : s_positions )
            iMaxDepth = Math.max( iMaxDepth, position._lExpected.length );

        for ( int iDepth = 0; iDepth < iMaxDepth; ++iDepth )
            {
            for ( TestPosition position : s_positions )
                if (position.test( iDepth, WHITE ) >= s_lMaxNanosecs ||
                    position.test( iDepth, BLACK ) >= s_lMaxNanosecs)
                    {
                    return;
                    }

            if (iDepth > 0 &&
                s_lNetMoves > 0L &&
                (lMillisecs = TimeUnit.NANOSECONDS.toMillis( s_lNetNanosecs )) > 0L)
                {
                s_log.info( String.format( "Depth %d: generated %,d moves in %s (%,d mps)",
                                           iDepth,
                                           s_lNetMoves,
                                           TimeUtil.formatMillisecs( lMillisecs, true ),
                                           (s_lNetMoves * 1000) / lMillisecs ) );
                }
            }
        }
//  -----------------------------------------------------------------------
//	METHODS
//	-----------------------------------------------------------------------

    /**
     * Helper method to quickly count the avialble moves in a FEN position.
     *
     * @param strFEN
     *     Position as a Forsyth-Edwards Notation string.
     *
     * @return Count of moves available.
     */
    private static int countMoves( final String strFEN )
        { return new MoveList( BoardFactory.createFromFEN( strFEN ) ).size(); }

    @AfterClass
    public static void displayResults()
        {
        long lMillisecs;

        if (s_lNetMoves > 0L &&
            (lMillisecs = TimeUnit.NANOSECONDS.toMillis( s_lNetNanosecs )) > 0)
            {
            s_log.info( String.format( "%s: MoveList generated %,d moves in %s (%,d mps)",
                                       DURATION.toString(),
                                       s_lNetMoves,
                                       TimeUtil.formatMillisecs( lMillisecs, true ),
                                       (s_lNetMoves * 1000) / lMillisecs ) );
            }
        }

    @BeforeClass
    public static void setup()
        {
        s_lNetMoves = s_lNetNanosecs = 0L;
        }

//  -----------------------------------------------------------------------
//	NESTED CLASSES
//	-----------------------------------------------------------------------

    private static class TestPosition
        {
        private final Board  _board;
        private final Board  _boardMirror;
        private final long[] _lExpected;

        TestPosition( String strFEN, long[] lExpected )
            {
            _board = BoardFactory.createFromFEN( strFEN );
            _boardMirror = BoardFactory.createMirror( _board );

            _lExpected = lExpected;
            }

        private static long perft( final Board bd, int iDepth )
            {
            assert bd != null;
            assert (iDepth >= 0 && iDepth < Byte.MAX_VALUE);
            /*
            **  CODE
            */
            long lCount;
            final MoveList moves = new MoveList( bd );

            if (iDepth-- <= 0)
                lCount = moves.size();
            else
                {
                lCount = 0L;

                for ( Move mv : moves )
                    {
                    bd.makeMove( mv );
                    lCount += perft( bd, iDepth );
                    bd.undoMove( mv );
                    }
                }

            return lCount;
            }

        long test( int iDepth, int player )
            {
            if (iDepth < 0 || iDepth >= _lExpected.length)
                return s_lNetNanosecs;

            assert (player == WHITE || player == BLACK);
            /*
            **  CODE
            */
            final Stopwatch swatch = Stopwatch.startNew();
            final long lActual = perft( ((player == WHITE) ? _board : _boardMirror), iDepth );

            s_lNetNanosecs += swatch.getElapsed();
            s_lNetMoves += lActual;

            assertEquals( _lExpected[ iDepth ], lActual );

            return s_lNetNanosecs;
            }
        }

    private static final TestPosition[] s_positions = new TestPosition[]
        {
            //
            // http://chessprogramming.wikispaces.com/Perft+Results
            //
            new TestPosition( FEN_INITIAL,
                              new long[]{ 20L, 400L, 8902L, 197281L, 4865609L, 119060324L, 3195901860L } ),
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
                              new long[]{ 3L, 9L, 57L, 360L, 1969L, 10724 } ),
            new TestPosition( "8/8/k7/p7/P7/K7/8/8 w - -",
                              new long[]{ 3L, 9L, 57L, 360L, 1969L, 10724L, 65679L } ),
            new TestPosition( "k7/8/8/3p4/4p3/8/8/7K w - -",
                              new long[]{ 3L, 15L, 84L, 573L, 3013L, 22886 } ),
            new TestPosition( "4k2r/6K1/8/8/8/8/8/8 w k -",
                              new long[]{ 3L, 32L, 134L, 2073L, 10485L, 179869L, 954475L } ),
            new TestPosition( "K7/8/2n5/1n6/8/8/8/k6N w - -",
                              new long[]{ 3L, 51L, 345L, 5301L, 38348L, 588695L, 5041119L } ),
            new TestPosition( "k6N/8/8/8/1n6/2n5/8/K7 w - -",
                              new long[]{ 3L, 51L, 345L, 5301L, 38348L, 588695L, 5041119L } ),
            new TestPosition( "7k/8/8/3p4/8/3P4/8/K7 w - -",
                              new long[]{ 4L, 15L, 89L, 537L, 3309L, 21104 } ),
            new TestPosition( "k7/8/3p4/8/3P4/8/8/7K w - -",
                              new long[]{ 4L, 15L, 90L, 534L, 3450L, 20960 } ),
            new TestPosition( "k7/8/3p4/8/8/4P3/8/7K w - -",
                              new long[]{ 4L, 16L, 101L, 637L, 4271L, 28662 } ),
            new TestPosition( "k7/8/6p1/8/8/7P/8/K7 w - -",
                              new long[]{ 4L, 16L, 101L, 637L, 4354L, 29679 } ),
            new TestPosition( "k7/8/7p/8/8/6P1/8/K7 w - -",
                              new long[]{ 4L, 16L, 101L, 637L, 4354L, 29679 } ),
            new TestPosition( "7k/8/4p3/8/8/3P4/8/K7 w - -",
                              new long[]{ 4L, 16L, 101L, 637L, 4271L, 28662 } ),
            new TestPosition( "7k/8/p7/8/8/1P6/8/7K w - -",
                              new long[]{ 4L, 16L, 101L, 637L, 4354L, 29679 } ),
            new TestPosition( "7k/8/1p6/8/8/P7/8/7K w - -",
                              new long[]{ 4L, 16L, 101L, 637L, 4354L, 29679 } ),
            new TestPosition( "7k/3p4/8/8/3P4/8/8/K7 w - -",
                              new long[]{ 4L, 19L, 117L, 720L, 4661L, 32191 } ),
            new TestPosition( "k7/3p4/8/8/3P4/8/8/7K w - -",
                              new long[]{ 4L, 19L, 117L, 712L, 4658L, 30749 } ),
            new TestPosition( "r3k3/1K6/8/8/8/8/8/8 w q -",
                              new long[]{ 4L, 49L, 243L, 3991L, 20780L, 367724L, 1971278L } ),
            new TestPosition( "8/8/4K3/3Nn3/3nN3/3k4/8/8 w - -",
                              new long[]{ 4L, 68L, 1118L, 16199L, 281190L, 4405103L, 75214812L } ),
            new TestPosition( "7k/8/8/4P3/3P4/8/8/K7 w - -",
                              new long[]{ 5L, 15L, 102L, 569L, 4337L, 22579 } ),
            new TestPosition( "7k/8/8/3p4/8/8/3P4/K7 w - -",
                              new long[]{ 5L, 19L, 116L, 716L, 4786L, 30980 } ),
            new TestPosition( "k7/8/8/3p4/8/8/3P4/7K w - -",
                              new long[]{ 5L, 19L, 117L, 720L, 5014L, 32167 } ),
            new TestPosition( "k7/8/8/6p1/7P/8/8/K7 w - -",
                              new long[]{ 5L, 22L, 139L, 877L, 6112L, 41874 } ),
            new TestPosition( "k7/8/8/7p/6P1/8/8/K7 w - -",
                              new long[]{ 5L, 22L, 139L, 877L, 6112L, 41874 } ),
            new TestPosition( "7k/8/8/1p6/P7/8/8/7K w - -",
                              new long[]{ 5L, 22L, 139L, 877L, 6112L, 41874 } ),
            new TestPosition( "7k/8/8/p7/1P6/8/8/7K w - -",
                              new long[]{ 5L, 22L, 139L, 877L, 6112L, 41874 } ),
            new TestPosition( "k7/6p1/8/8/8/8/7P/K7 w - -",
                              new long[]{ 5L, 25L, 161L, 1035L, 7574L, 55338 } ),
            new TestPosition( "k7/7p/8/8/8/8/6P1/K7 w - -",
                              new long[]{ 5L, 25L, 161L, 1035L, 7574L, 55338 } ),
            new TestPosition( "8/8/3k4/3p4/3P4/3K4/8/8 w - -",
                              new long[]{ 5L, 25L, 180L, 1294L, 8296L, 53138 } ),
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
                              new long[]{ 7L, 49L, 378L, 2902L, 24122L, 199002 } ),
            new TestPosition( "K7/b7/1b6/1b6/8/8/8/k6B w - -",
                              new long[]{ 7L, 143L, 1416L, 31787L, 310862L, 7382896L, 75429328L } ),
            new TestPosition( "k6B/8/8/8/1b6/1b6/b7/K7 w - -",
                              new long[]{ 7L, 143L, 1416L, 31787L, 310862L, 7382896L, 75429328L } ),
            new TestPosition( "8/8/3k4/3p4/8/3P4/3K4/8 w - -",
                              new long[]{ 8L, 61L, 411L, 3213L, 21637L, 158065 } ),
            new TestPosition( "8/3k4/3p4/8/3P4/3K4/8/8 w - -",
                              new long[]{ 8L, 61L, 483L, 3213L, 23599L, 157093 } ),
            new TestPosition( "8/6kP/8/8/8/8/pK6/8 w - -",
                              new long[]{ 11L, 97L, 887L, 8048L, 90606L, 1030499 } ),
            new TestPosition( "8/Pk6/8/8/8/8/6Kp/8 w - -",
                              new long[]{ 11L, 97L, 887L, 8048L, 90606L, 1030499 } ),
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
                              new long[]{ 18L, 270L, 4699L, 79355L, 1533145L, 28859283 } ),
            new TestPosition( "8/4kPPP/8/8/8/8/pppK4/8 w - -",
                              new long[]{ 18L, 270L, 4699L, 79355L, 1533145L, 28859283 } ),
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
                              new long[]{ 24L, 421L, 7421L, 124608L, 2193768L, 37665329 } ),
            new TestPosition( "5n1n/5kP1/8/8/8/8/1pK5/N1N5 w - -",
                              new long[]{ 24L, 421L, 7421L, 124608L, 2193768L, 37665329 } ),
            new TestPosition( "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N w - -",
                              new long[]{ 24L, 496L, 9483L, 182838L, 3605103L, 71179139 } ),
            new TestPosition( "5n1n/4kPPP/8/8/8/8/pppK4/N1N5 w - -",
                              new long[]{ 24L, 496L, 9483L, 182838L, 3605103L, 71179139 } ),
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
            // http://www.talkchess.com/forum/viewtopic.php?t=47318
            //
            // self stalemate
            new TestPosition( "K1k5/8/P7/8/8/8/8/8 w - -",
                              new long[]{ 2L, 6L, 13L, 63L, 382L, 2217L } ),
            // underpromote to check
            new TestPosition( "8/P1k5/K7/8/8/8/8/8 w - -",
                              new long[]{ 6L, 27L, 273L, 1329L, 18135L, 92683L } ),
            //  avoid illegal en passant capture
            new TestPosition( "8/5bk1/8/2Pp4/8/1K6/8/8 w - d6",
                              new long[]{ 8L, 104L, 736L, 9287L, 62297L, 824064L } ),
            // promote to give check
            new TestPosition( "4k3/1P6/8/8/8/8/K7/8 w - -",
                              new long[]{ 9L, 40L, 472L, 2661L, 38983L, 217342L } ),
            // stalemate/checkmate
            new TestPosition( "8/k1P5/8/1K6/8/8/8/8 w - -",
                              new long[]{ 10L, 25L, 268L, 926L, 10857L, 43261L, 567584L } ),
            // promote out of check
            new TestPosition( "2K2r2/4P3/8/8/8/8/8/3k4 w - -",
                              new long[]{ 11L, 133L, 1442L, 19174L, 266199L, 3821001L } ),
            //  short castling gives check
            new TestPosition( "5k2/8/8/8/8/8/8/4K2R w K -",
                              new long[]{ 15L, 66L, 1198L, 6399L, 120330L, 661072L } ),
            //  en passant capture checks opponent
            new TestPosition( "8/5k2/8/2Pp4/2B5/1K6/8/8 w - d6",
                              new long[]{ 15L, 126L, 1928L, 13931L, 206379L, 1440467L } ),
            // long castling gives check
            new TestPosition( "3k4/8/8/8/8/8/8/R3K3 w Q -",
                              new long[]{ 16L, 71L, 1286L, 7418L, 141077L, 803711L } ),
            // contributed
            new TestPosition( "8/8/8/8/k1p4R/8/3P4/3K4 w - -",
                              new long[]{ 18L, 92L, 1670L, 10138L, 185429L, 1134888L } ),
            // castling (including losing castling rights due to rook capture)
            new TestPosition( "r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq -",
                              new long[]{ 26L, 1141L, 27826L, 1274206L, 31912360L, 1509218880L } ),
            // discovered check
            new TestPosition( "5K2/8/1Q6/2N5/8/1p2k3/8/8 w - -",
                              new long[]{ 29L, 165L, 5160L, 31961L, 1004658L, 6334638L } ),
            //  double check
            new TestPosition( "8/5k2/8/5N2/5Q2/2K5/8/8 w - -",
                              new long[]{ 37L, 183L, 6559L, 23527L, 811573L, 3114998L } ),
            // castling prevented
            new TestPosition( "r3k2r/8/5Q2/8/8/3q4/8/R3K2R w KQkq -",
                              new long[]{ 44L, 1494L, 50509L, 1720476L, 58773923L, 2010267707L } ),
            //
            //  http://www.chess.com/forum/view/more-puzzles/mate-in-292-movesblathys-monster
            //
            new TestPosition( "q5nn/1p2p3/p1k1P1p1/6Pp/PKp1p1pP/8/2P1P1PP/3Q4 w - -",
                              new long[]{ 23L, 351L, 7416L, 130826L, 2610361L, 49832214L, 956434803L } ),
            //
            //  Assorted positions from my own testing
            //

            // 3 pinned pieces
            new TestPosition( "rQ3rk1/p2pq1pp/6Q1/bbpnp3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ -",
                              new long[]{ 35L, 1892L, 64973L, 3321449L, 120745830L, 5966580991L, 225409779045L } ),
            new TestPosition( "r4rk1/pp1nq1p1/3bp2p/3p1pP1/2pP1P1P/2P1PN2/PPQ1B3/R3K2R w KQ f6",
                              new long[]{ 39L, 1484L, 55740L, 2070681L, 76647022L, 2803944626L, 102930720732L } ),
            // Really open position, with lots of depth 0 moves
            new TestPosition( "r3k1br/R7/3Q4/7R/4B3/2B5/4K3/8 w k -",
                              new long[]{ 85L, 1092L, 77449L, 1324003L, 84952445L, 1662371924L, 98898300898L } )
        };
    }   /* end of unit test class TestMoveList */
