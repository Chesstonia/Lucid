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
package net.humbleprogrammer.maxx;

import net.humbleprogrammer.humble.BitUtil;

import static net.humbleprogrammer.maxx.Constants.*;

@SuppressWarnings( "WeakerAccess" )
public class Arbiter
	{

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Tests a board for validity.
	 * In order to be valid, all of the following must be <i>true</i>:
	 * <ul>
	 * <li>Neither side can
	 * have more than 16 pieces.</li>
	 * <li>Each side must have one (and only one) King.</li>
	 * <li>Neither side can have more than 8 pawns.</li>
	 * <li>No pawns on the first or last
	 * rank.</li>
	 * <li>The player "on the move" cannot be able to capture the opposing King.
	 * </li>
	 * </ul>
	 *
	 * @return .T. if the position is valid; .F. otherwise.
	 */
	public static boolean isLegalPosition( Board bd )
		{
		if (bd == null) return false;
		//	-----------------------------------------------------------------

		//  Test 1 -- neither player can have more than 16 pieces on the board.
		if (BitUtil.count( bd.map[ MAP_W_ALL ] ) > 16 ||
			BitUtil.count( bd.map[ MAP_B_ALL ] ) > 16) return false;

		//  Test 2 -- both players must have one (and only one) king on the board.
		if (!(BitUtil.singleton( bd.map[ MAP_W_KING ] ) &&
			  BitUtil.singleton( bd.map[ MAP_B_KING ] )))
			{ return false; }

		// Test 3 -- neither player can have more than 8 pawns on the board.
		final int iWPawns = BitUtil.count( bd.map[ MAP_W_PAWN ] );
		final int iBPawns = BitUtil.count( bd.map[ MAP_B_PAWN ] );

		if (iWPawns > 8 || iBPawns > 8) return false;

		// Test 4 -- no pawns on the first or last rank.
		if (((bd.map[ MAP_W_PAWN ] | bd.map[ MAP_B_PAWN ]) & Square.NO_PAWN_ZONE) != 0L)
			{ return false; }

		//  Test 5 -- no more than 9 queens + pawns
		if ((BitUtil.count( bd.map[ MAP_W_QUEEN ] ) + iWPawns) > 9 ||
			(BitUtil.count( bd.map[ MAP_B_QUEEN ] ) + iBPawns) > 9)
			{ return false; }

		//  Test 6 -- no more than 10 minor pieces + pawns
		if ((BitUtil.count( bd.map[ MAP_W_KNIGHT ] ) + iWPawns) > 10 ||    // white knights
			(BitUtil.count( bd.map[ MAP_B_KNIGHT ] ) + iBPawns) > 10 || // black knights
			(BitUtil.count( bd.map[ MAP_W_BISHOP ] ) + iWPawns) > 10 || // white bishops
			(BitUtil.count( bd.map[ MAP_B_BISHOP ] ) + iBPawns) > 10 || // black bishops
			(BitUtil.count( bd.map[ MAP_W_ROOK ] ) + iWPawns) > 10 ||    // white rooks
			(BitUtil.count( bd.map[ MAP_B_ROOK ] ) + iBPawns) > 10)        // black rooks
			{ return false; }

		// Test 7 -- opposing player's king can't be in check.
		int player = bd.getMovingPlayer();

		return (player == WHITE)
			   ? !Bitboards.isAttackedByWhite( bd.map, bd.getOpposingKingSquare() )
			   : !Bitboards.isAttackedByBlack( bd.map, bd.getOpposingKingSquare() );
		}

	/**
	 * Determines if the current position is mate.
	 *
	 * @param bd
	 * 	Position to test.
	 *
	 * @return .T. if moving player is in check but has no legal moves; .F. otherwise.
	 */
	public static boolean isMated( Board bd )
		{
		if (bd == null) return false;
		//	-----------------------------------------------------------------
		return (bd.isInCheck() && !MoveGenerator.hasLegalMove( bd ));
		}

	/**
	 * Determines if the current position is stalemated.
	 *
	 * @param bd
	 * 	Position to test.
	 *
	 * @return .T. if moving player has no legal moves, but is not in check; .F. otherwise.
	 */
	public static boolean isStalemated( Board bd )
		{
		if (bd == null) return false;
		//	-----------------------------------------------------------------
		return !(bd.isInCheck() || MoveGenerator.hasLegalMove( bd ));
		}

	} /* end of class Arbiter */
