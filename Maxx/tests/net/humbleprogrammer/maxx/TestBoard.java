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

import static net.humbleprogrammer.maxx.Constants.*;
import static org.junit.Assert.*;

@SuppressWarnings("unused")
public class TestBoard extends TestBase
	{

	//  -----------------------------------------------------------------------
	//	UNIT TESTS
	//	-----------------------------------------------------------------------

	@Test(expected = IllegalArgumentException.class)
	public void t_ctor_fail()
		{
		Board bd = new Board(null);
		}
	
	@Test
	public void t_get()
		{
		Board bd = BoardFactory.createFromFEN(FEN_TEST);

		assertEquals(Piece.W_ROOK, bd.get(Square.A1));
		assertEquals(Piece.W_ROOK, bd.get(Square.F1));
		assertEquals(Piece.W_KING, bd.get(Square.G1));

		assertEquals(Piece.W_PAWN, bd.get(Square.A2));
		assertEquals(Piece.W_PAWN, bd.get(Square.B2));
		assertEquals(Piece.W_KNIGHT, bd.get(Square.D2));
		assertEquals(Piece.W_QUEEN, bd.get(Square.E2));
		assertEquals(Piece.W_PAWN, bd.get(Square.G2));

		assertEquals(Piece.W_BISHOP, bd.get(Square.D3));
		assertEquals(Piece.W_PAWN, bd.get(Square.E3));
		assertEquals(Piece.W_PAWN, bd.get(Square.H3));

		assertEquals(Piece.W_PAWN, bd.get(Square.D4));
		assertEquals(Piece.W_PAWN, bd.get(Square.F4));
		assertEquals(Piece.B_PAWN, bd.get(Square.G4));

		assertEquals(Piece.W_PAWN, bd.get(Square.C5));
		assertEquals(Piece.B_PAWN, bd.get(Square.D5));
		assertEquals(Piece.B_PAWN, bd.get(Square.F5));
		assertEquals(Piece.B_PAWN, bd.get(Square.H5));

		assertEquals(Piece.B_PAWN, bd.get(Square.C6));
		assertEquals(Piece.B_PAWN, bd.get(Square.E6));
		assertEquals(Piece.B_KNIGHT, bd.get(Square.F6));

		assertEquals(Piece.B_PAWN, bd.get(Square.A7));
		assertEquals(Piece.B_PAWN, bd.get(Square.B7));
		assertEquals(Piece.B_QUEEN, bd.get(Square.C7));
		assertEquals(Piece.B_BISHOP, bd.get(Square.E7));

		assertEquals(Piece.B_ROOK, bd.get(Square.A8));
		assertEquals(Piece.B_KING, bd.get(Square.E8));
		assertEquals(Piece.B_ROOK, bd.get(Square.H8));
		}

	@Test
	public void t_get_fail_low()
		{
		Board bd = BoardFactory.createInitial();

		for ( int iSq = SQ_LO; iSq < Square.A1; ++iSq )
			assertEquals(EMPTY, bd.get(iSq));
		}

	@Test
	public void t_get_fail_high()
		{
		Board bd = BoardFactory.createInitial();

		for ( int iSq = Square.H8 + 1; iSq < SQ_HI; ++iSq )
			assertEquals(EMPTY, bd.get(iSq));
		}

	@Test
	public void t_getZobristHash()
		{
		Board bd = BoardFactory.createFromFEN(FEN_TEST);
		Board bdCopy = new Board(bd);

		assertEquals(bd.hashCode(), bdCopy.hashCode());
		assertEquals(bd.getZobristHash(), bdCopy.getZobristHash());

		bdCopy.setCastlingFlags(Board.CastlingFlags.NONE);
		assertNotEquals(bd.hashCode(), bdCopy.hashCode());
		assertNotEquals(bd.getZobristHash(), bdCopy.getZobristHash());
		bdCopy.copyFrom(bd);

		bdCopy.setEnPassantSquare(INVALID);
		assertNotEquals(bd.hashCode(), bdCopy.hashCode());
		assertNotEquals(bd.getZobristHash(), bdCopy.getZobristHash());
		bdCopy.copyFrom(bd);

		bdCopy.setMovingPlayer(bd.getMovingPlayer() ^ 1);
		assertNotEquals(bd.hashCode(), bdCopy.hashCode());
		assertNotEquals(bd.getZobristHash(), bdCopy.getZobristHash());
		bdCopy.copyFrom(bd);
		//
		//	The move clocks do not affect the Zobrist hash.
		//
		bdCopy.setHalfMoveClock( bdCopy.getHalfMoveClock() + 1);
		assertNotEquals(bd.hashCode(), bdCopy.hashCode());
		assertEquals(bd.getZobristHash(), bdCopy.getZobristHash());
		bdCopy.copyFrom(bd);

		bdCopy.setMoveNumber( bdCopy.getMoveNumber() + 1);
		assertNotEquals(bd.hashCode(), bdCopy.hashCode());
		assertEquals(bd.getZobristHash(), bdCopy.getZobristHash());
		bdCopy.copyFrom(bd);

		assertEquals(bd.hashCode(), bdCopy.hashCode());
		assertEquals(bd.getZobristHash(), bdCopy.getZobristHash());
		}
	} /* end of class TestBoard */
