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
package net.humbleprogrammer.maxx.factories;

import net.humbleprogrammer.TestBase;
import net.humbleprogrammer.maxx.*;
import net.humbleprogrammer.maxx.factories.BoardFactory;
import net.humbleprogrammer.maxx.factories.MoveFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestMoveFactory extends TestBase
    {

    //  -----------------------------------------------------------------------
    //	UNIT TESTS
    //	-----------------------------------------------------------------------

    @Test
    public void t_sample()
        {
        Board bd = BoardFactory.createInitial();

        for ( String str : SAMPLE_MOVES )
            {
            Move move = MoveFactory.fromSAN( bd, str );

            assertNotNull( str, move );
            assertTrue( bd.isLegalMove( move ) );

            bd.makeMove( move );
            }
        }

    @Test
    public void t_fromSAN_fail()
        {
        assertNull( MoveFactory.fromSAN( null, null ) );
        }

    @Test
    public void t_fromSAN_fail_blank()
        {
        Board bd = BoardFactory.createInitial();

        assertNull( MoveFactory.fromSAN( bd, null ) );
        assertNull( MoveFactory.fromSAN( bd, "" ) );
        assertNull( MoveFactory.fromSAN( bd, "   " ) );
        assertNull( MoveFactory.fromSAN( bd, Parser.STR_CRLF ) );
        }

    @Test
    public void t_fromSAN_fail_invalidFile()
        {
        Board bd = BoardFactory.createInitial();

        assertNull( MoveFactory.fromSAN( bd, "x4" ) );
        assertNull( MoveFactory.fromSAN( bd, "14" ) );
        assertNull( MoveFactory.fromSAN( bd, "-4" ) );
        assertNull( MoveFactory.fromSAN( bd, "E4" ) );
        assertNull( MoveFactory.fromSAN( bd, "*4" ) );
        }

    @Test
    public void t_fromSAN_fail_invalidRank()
        {
        Board bd = BoardFactory.createInitial();

        assertNull( MoveFactory.fromSAN( bd, "ex" ) );
        assertNull( MoveFactory.fromSAN( bd, "e0" ) );
        assertNull( MoveFactory.fromSAN( bd, "e9" ) );
        assertNull( MoveFactory.fromSAN( bd, "e" ) );
        assertNull( MoveFactory.fromSAN( bd, "e " ) );
        assertNull( MoveFactory.fromSAN( bd, "e-" ) );
        }

    @Test
    public void t_fromSAN_fail_invalidPiece()
        {
        Board bd = BoardFactory.createInitial();

        assertNull( MoveFactory.fromSAN( bd, "Je4" ) );
        assertNull( MoveFactory.fromSAN( bd, "1e4" ) );
        assertNull( MoveFactory.fromSAN( bd, "-e4" ) );
        assertNull( MoveFactory.fromSAN( bd, "pe4" ) );
        assertNull( MoveFactory.fromSAN( bd, "Pe4" ) );
        }
    }   /* end of class TestMoveFactory */
