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

public class TestPiece extends TestBase
    {
    //  -----------------------------------------------------------------------
    //	UNIT TESTS
    //	-----------------------------------------------------------------------
    @Test
    public void t_index()
        {
        int iBits = 0;

        for ( final Piece piece : Piece.values() )
            {
            assertTrue( piece.index >= 2 && piece.index < 14 );
            iBits |= 1 << piece.index;
            }

        assertEquals( 0x3FFC, iBits );
        }

    @Test
    public void t_side()
        {
        assertEquals( WHITE, Piece.W_PAWN.color );
        assertEquals( BLACK, Piece.B_PAWN.color );
        assertEquals( WHITE, Piece.W_KNIGHT.color );
        assertEquals( BLACK, Piece.B_KNIGHT.color );
        assertEquals( WHITE, Piece.W_BISHOP.color );
        assertEquals( BLACK, Piece.B_BISHOP.color );
        assertEquals( WHITE, Piece.W_ROOK.color );
        assertEquals( BLACK, Piece.B_ROOK.color );
        assertEquals( WHITE, Piece.W_QUEEN.color );
        assertEquals( BLACK, Piece.B_QUEEN.color );
        assertEquals( WHITE, Piece.W_KING.color );
        assertEquals( BLACK, Piece.B_KING.color );
        }

    @Test
    public void t_type()
        {
        assertEquals( PAWN, Piece.W_PAWN.type );
        assertEquals( PAWN, Piece.B_PAWN.type );
        assertEquals( KNIGHT, Piece.W_KNIGHT.type );
        assertEquals( KNIGHT, Piece.B_KNIGHT.type );
        assertEquals( BISHOP, Piece.W_BISHOP.type );
        assertEquals( BISHOP, Piece.B_BISHOP.type );
        assertEquals( ROOK, Piece.W_ROOK.type );
        assertEquals( ROOK, Piece.B_ROOK.type );
        assertEquals( QUEEN, Piece.W_QUEEN.type );
        assertEquals( QUEEN, Piece.B_QUEEN.type );
        assertEquals( KING, Piece.W_KING.type );
        assertEquals( KING, Piece.B_KING.type );
        }
    }   /* end of class TestPiece */
