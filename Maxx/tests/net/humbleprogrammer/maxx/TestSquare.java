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

import static net.humbleprogrammer.maxx.Constants.INVALID;
import static org.junit.Assert.*;

public class TestSquare extends TestBase
    {

    //  -----------------------------------------------------------------------
    //	UNIT TESTS
    //	-----------------------------------------------------------------------

    @Test
    public void t_getFile()
        {
        for ( int iSq = 0; iSq < 64; ++iSq )
            assertEquals( (iSq % 8), Square.getFile( iSq ) );
        }

    @Test
    public void t_getFile_fail()
        {
        // fail low
        for ( int iSq = SQ_LO; iSq < 0; ++iSq )
            assertEquals( INVALID, Square.getFile( iSq ) );
        // fail high
        for ( int iSq = 64; iSq < SQ_HI; ++iSq )
            assertEquals( INVALID, Square.getFile( iSq ) );
        }

    @Test
    public void t_getRank()
        {
        for ( int iSq = 0; iSq < 64; ++iSq )
            assertEquals( (iSq / 8), Square.getRank( iSq ) );
        }

    @Test
    public void t_getRank_fail()
        {
        // fail low
        for ( int iSq = SQ_LO; iSq < 0; ++iSq )
            assertEquals( INVALID, Square.getRank( iSq ) );
        // fail high
        for ( int iSq = 64; iSq < SQ_HI; ++iSq )
            assertEquals( INVALID, Square.getRank( iSq ) );
        }

    @Test
    public void t_isDark()
        {
        final long bbDark = 0xAA55AA55AA55AA55L;

        for ( int index = SQ_LO; index < SQ_HI; ++index )
            if (Square.isValid( index ) && (bbDark & (1L << index)) != 0)
                assertTrue( Square.isDark( index ) );
            else
                assertFalse( Square.isDark( index ) );
        }

    @Test
    public void t_isValid()
        {
        assertFalse( Square.isValid( INVALID ) );

        for ( int index = SQ_LO; index < SQ_HI; ++index )
            assertEquals( (index >= 0 && index < 64), Square.isValid( index ) );
        }

    @Test
    public void t_isValidRankOrFile()
        {
        assertFalse( Square.isValidRankOrFile( INVALID ) );

        for ( int index = SQ_LO; index < SQ_HI; ++index )
            assertEquals( (index >= 0 && index < 8), Square.isValidRankOrFile( index ) );
        }

    @Test
    public void t_toIndex()
        {
        for ( int index = 0; index < 64; ++index )
            assertEquals( index, Square.toIndex( (index >> 3), (index & 0x07) ) );
        }

    @Test
    public void t_toIndex_fail()
        {
        // Fail low
        for ( int iRank = SQ_LO; iRank < 0; ++iRank )
            for ( int iFile = SQ_LO; iFile < 8; ++iFile )
                assertEquals( INVALID, Square.toIndex( iRank, iFile ) );
        // Fail high
        for ( int iRank = 8; iRank < SQ_HI; ++iRank )
            for ( int iFile = 8; iFile < SQ_HI; ++iFile )
                assertEquals( INVALID, Square.toIndex( iRank, iFile ) );
        }
    }   /* end of class TestSquare */
