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
import org.junit.Test;

import static net.humbleprogrammer.maxx.Constants.*;
import static org.junit.Assert.assertEquals;

public class TestBitboards extends TestBase
    {

    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Attack bitboards for the test position. */
    private static final long[] s_bbAttacks = new long[]
        {
            // A1..H1
            0x0000000000000020L, 0x0000000000080821L, 0x0000000000000021L, 0x0000000000001021L,
            0x0000000000001021L, 0x0000000000001841L, 0x0000000000000020L, 0x0000000000000040L,
            // A2..H2
            0x0000000000000001L, 0x0000000000000000L, 0x0000000000080000L, 0x0000000000001000L,
            0x0000000000080000L, 0x0000000000001060L, 0x0000000000001040L, 0x0000000000000040L,
            // A3..H3
            0x0000000000000200L, 0x0000000000000900L, 0x0000000000000200L, 0x0000000000001000L,
            0x0000000000001000L, 0x0000000040005820L, 0x0000000000000000L, 0x0000000040004000L,
            // A4..H4
            0x0000000000000000L, 0x0000000000000000L, 0x0000000800080800L, 0x0000000000100000L,
            0x0000202800080800L, 0x0004000000100020L, 0x000020A000801000L, 0x0000000000000000L,
            // A5..H5
            0x0004000000000000L, 0x0000040000080000L, 0x0010000008000000L, 0x0000340000000000L,
            0x0004000028000000L, 0x0000100000080000L, 0x0000000020000000L, 0x8000200000000000L,
            // A6..H6
            0x0002000000080000L, 0x0005000400000000L, 0x0006000000000000L, 0x0014000400000000L,
            0x0000000000000000L, 0x0010000000000000L, 0x0000000000000000L, 0x8000000000000000L,
            // A7..H7
            0x0100000000000000L, 0x0004000000000000L, 0x0000000000000000L, 0x1004200000000000L,
            0x1004000000000000L, 0x1000000000000000L, 0x0000000000000000L, 0x8000200000000000L,
            // A8..H8
            0x0000000000000000L, 0x0104000000000000L, 0x0104000000000000L, 0x1114000000000000L,
            0x8100200000000000L, 0x9010000000000000L, 0x8000200000000000L, 0x0000000000000000L
        };

    /** Piece bitboards (by color) for the test position. */
    private static final long[] s_bbPieces = new long[]
        {
            0x0000000428985B61L, 0x911734A840000000L
        };

    //  -----------------------------------------------------------------------
    //	UNIT TESTS
    //	-----------------------------------------------------------------------

    @Test
    public void t_getAttackedBy()
        {
        Board.State state = BoardFactory.createFromFEN( FEN_TEST ).getState();

        for ( int iSq = 0; iSq < Math.min( 64, s_bbAttacks.length ); ++iSq )
            {
            long bbAttacks = s_bbAttacks[ iSq ];

            assertEquals( "White attacking " + Square.toString( iSq ),
                          (s_bbPieces[ WHITE ] & bbAttacks),
                          Bitboards.getAttackedBy( state.map, iSq, WHITE ) );

            assertEquals( "Black attacking " + Square.toString( iSq ),
                          (s_bbPieces[ BLACK ] & bbAttacks),
                          Bitboards.getAttackedBy( state.map, iSq, BLACK ) );
            }
        }

    @Test
    public void t_isAttackedBy()
        {
        Board.State state = BoardFactory.createFromFEN( FEN_TEST ).getState();

        for ( int iSq = 0; iSq < 64; ++iSq )
            {
            assertEquals( "White attacking " + Square.toString( iSq ),
                          ((s_bbAttacks[ iSq ] & s_bbPieces[ WHITE ]) != 0L),
                          Bitboards.isAttackedBy( state.map, iSq, WHITE ) );

            assertEquals( "Black attacking " + Square.toString( iSq ),
                          ((s_bbAttacks[ iSq ] & s_bbPieces[ BLACK ]) != 0L),
                          Bitboards.isAttackedBy( state.map, iSq, BLACK ) );
            }
        }

    }   /* end of class TestBitboard */
