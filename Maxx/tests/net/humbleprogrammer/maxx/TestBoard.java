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

import org.junit.Test;

import static net.humbleprogrammer.maxx.Constants.*;
import static org.junit.Assert.*;

public class TestBoard extends TestBase
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
            assertNull( bd.get( iSq ) );
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

    @Test
    public void t_isLegal()
        {
        final Board bd = BoardFactory.createFromFEN( FEN_TEST );

        assertNotNull( bd );
        assertTrue( bd.isValid() );
        }
    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    }   /* end of class TestBoard */
