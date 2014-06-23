package net.humbleprogrammer.maxx;

import net.humbleprogrammer.maxx.parsers.Parser;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestBoardFactory extends TestBase
    {

    //  -----------------------------------------------------------------------
    //	UNIT TESTS
    //	-----------------------------------------------------------------------

    @Test
    public void t_createFromFEN()
        {
        assertNotNull( BoardFactory.createFromFEN( FEN_TEST ) );

        for ( String strFEN : s_listFEN )
            assertNotNull( strFEN, BoardFactory.createFromFEN( strFEN ) );
        }

    @Test
    public void t_createFromFEN_fail()
        {
        assertNull( BoardFactory.createFromFEN( null ) );
        assertNull( BoardFactory.createFromFEN( "" ) );
        assertNull( BoardFactory.createFromFEN( "  \n\r\t" ) );
        }

    @Test
    public void t_createFromFEN_fail_castling()
        {
        String[] strFEN =
            {
                // Double dash
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b -- f3 0 1",
                // Wrong piece type
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b Rr f3 0 1",
                // Invalid character
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b n/a f3 0 1",
                // Missing
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b f3 0 1"
            };

        for ( String str : strFEN )
            assertNull( str, BoardFactory.createFromFEN( str ) );
        }

    @Test
    public void t_createFromFEN_fail_ep()
        {
        String[] strFEN =
            {
                //	Bad character
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq * 0 1",
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq 1 0 1",
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq g 0 1",
                //	Not a valid square
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq 3g 0 1",
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq g9 0 1",
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq kk 0 1",
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq 44 0 1",
                //  Wrong rank
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f2 0 1",
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f4 0 1",
            };

        for ( String str : strFEN )
            assertNull( str, BoardFactory.createFromFEN( str ) );
        }

    @Test
    public void t_createFromFEN_ignore_ep()
        {
        String[] strFEN =
            {
                //	Not behind a pawn...
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq g3 0 1",
                //	Square is occupied
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq d3 0 1"
            };

        for ( String str : strFEN )
            {
            Board bd = BoardFactory.createFromFEN( str );

            assertNotNull( str, bd );
            assertTrue( str, bd.isValid() );
            assertFalse( str, Square.isValid( bd.getEnPassantSquare() ) );
            }
        }

    @Test
    public void t_createFromFEN_fail_moveNumber()
        {
        String[] strFEN =
            {
                // value of zero
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 0",
                // negative value
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 -1",
                // invalid character
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 -",
                // commas
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 1,000",
                // decimal places
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 1.0",
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 0.5",
            };

        for ( String str : strFEN )
            assertNull( str, BoardFactory.createFromFEN( str ) );
        }

    @Test
    public void t_createFromFEN_fail_player()
        {
        String[] strFEN =
            {
                // Dash instead of [bw]
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 - kq f3 0 1",
                // Wrong case
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 W kq f3 0 1",
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 B kq f3 0 1",
                // Invalid character
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 L kq f3 0 1",
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 x kq f3 0 1",
                // Too many characters
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 White kq f3 0 1",
            };

        for ( String str : strFEN )
            assertNull( str, BoardFactory.createFromFEN( str ) );
        }

    @Test
    public void t_createFromFEN_fail_position()
        {
        String[] strFEN =
            {
                // Invalid character in rank 4
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3PLPp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 1",
                // Invalid character in rank 8
                "r3k-r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 1",
                // Invalid number in rank 4
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P0Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 1",
                // Invalid number in rank 8
                "r3k9r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 1",
                // Leading slash
                "/r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 1",
                // Double slashes
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p//3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3 0 1",
                // Trailing slash
                "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1/ b kq f3 0 1",
                // Too many pieces on first rank.
                "rnbqkbnrrnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "rnbq8kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                // Too many pieces on second rank.
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPPPPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPP8PPPP/RNBQKBNR w KQkq - 0 1",
                // Too many pieces on last rank.
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNRRNBQKBNR w KQkq - 0 1",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQ8KBNR w KQkq - 0 1"
            };

        for ( String str : strFEN )
            assertNull( str, BoardFactory.createFromFEN( str ) );
        }
    }
