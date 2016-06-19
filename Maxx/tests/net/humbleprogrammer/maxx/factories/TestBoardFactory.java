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
package net.humbleprogrammer.maxx.factories;

import net.humbleprogrammer.TestBase;
import net.humbleprogrammer.humble.Stopwatch;
import net.humbleprogrammer.humble.TimeUtil;
import net.humbleprogrammer.maxx.*;
import net.humbleprogrammer.maxx.factories.BoardFactory;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

import static net.humbleprogrammer.maxx.Constants.*;

public class TestBoardFactory extends TestBase
    {

    //  -----------------------------------------------------------------------
    //	UNIT TESTS
    //	-----------------------------------------------------------------------

    @Test
    public void t_createBlank()
        {
        Board bd = BoardFactory.createBlank();

        assertNotNull( bd );

        assertEquals( Board.CastlingFlags.NONE, bd.getCastlingFlags() );
        assertEquals( INVALID, bd.getEnPassantSquare() );
        assertEquals( WHITE, bd.getMovingPlayer() );
        assertEquals( HASH_BLANK, bd.getZobristHash() );

        for ( int iSq = 0; iSq < 64; ++iSq )
            assertEquals( EMPTY, bd.get( iSq ) );
        }

    @Test
    public void t_createCopy()
        {
        final Board bdSrc = BoardFactory.createFromFEN( FEN_TEST );

        assertNotNull( bdSrc );

        final Board bdDst = BoardFactory.createCopy( bdSrc );

        assertNotNull( bdDst );

        assertEquals( bdSrc.getCastlingFlags(), bdDst.getCastlingFlags() );
        assertEquals( bdSrc.getEnPassantSquare(), bdDst.getEnPassantSquare() );
        assertEquals( bdSrc.getHalfMoveClock(), bdDst.getHalfMoveClock() );
        assertEquals( bdSrc.getMoveNumber(), bdDst.getMoveNumber() );
        assertEquals( bdSrc.getMovingPlayer(), bdDst.getMovingPlayer() );
        assertEquals( bdSrc.getZobristHash(), bdDst.getZobristHash() );

        for ( int iSq = 0; iSq < 64; ++iSq )
            assertEquals( bdSrc.get( iSq ), bdDst.get( iSq ) );

        }

    @Test
    public void t_createFromFEN()
        {
        final Collection<String> listFEN = getFEN();
        final Stopwatch swatch = Stopwatch.startNew();

        int iCount = 0;

        for ( String strFEN : listFEN )
            {
            Board bd = BoardFactory.createFromFEN(strFEN);
            
            assertNotNull( strFEN, bd );
            assertTrue( strFEN, Arbiter.isLegalPosition(bd) );
            
            if ((++iCount % 1000) == 0 && swatch.getElapsed() > s_lMaxNanosecs)
                break;
            }
        //
        //	Display some simple metrics.
        //
		long lMillisecs = swatch.getElapsedMillisecs();

		if (iCount > 0 && lMillisecs > 0)
			{
			s_log.info( String.format( "%s: Parsed %,d FEN strings in %s (%,d /sec)",
									   DURATION.toString(),
									   iCount,
									   TimeUtil.formatMillisecs( lMillisecs, true ),
									   (iCount * 1000L) / lMillisecs ) );
			}
        }

    @Test
    public void t_createFromFEN_fail()
        {
        assertNull( BoardFactory.createFromFEN( null ) );
        assertNull( BoardFactory.createFromFEN( "" ) );
        assertNull( BoardFactory.createFromFEN( "   " ) );
        assertNull( BoardFactory.createFromFEN( Parser.STR_CRLF ) );
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
            assertTrue( str, Arbiter.isLegalPosition(bd) );
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

    @Test
    public void t_createInitial()
        {
        Board bd = BoardFactory.createInitial();

        assertNotNull( bd );

        assertEquals( Board.CastlingFlags.ALL, bd.getCastlingFlags() );
        assertEquals( INVALID, bd.getEnPassantSquare() );
        assertEquals( 1, bd.getMoveNumber() );
        assertEquals( WHITE, bd.getMovingPlayer() );
        assertEquals( HASH_INITIAL, bd.getZobristHash() );
        }
    }
