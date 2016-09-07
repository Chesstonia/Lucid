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

import java.util.Iterator;

import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.maxx.interfaces.IMoveScorer;

@SuppressWarnings( "WeakerAccess" )
public class MoveList extends MoveGenerator implements Iterable<Move>
	{

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Zobrist hash of board that moves were generated for. */
	private final long _hashZobrist;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 *
	 * @param bd
	 * 	Board to generate moves for.
	 *
	 * @throws java.lang.IllegalArgumentException
	 * 	if board is <code>null</code>.
	 */
	public MoveList( Board bd )
		{
		super( bd );
		//	-----------------------------------------------------------------
		_hashZobrist = bd.getZobristHash();

		generateAll();
		}

	public MoveList( Board bd, int iSqTo )
		{
		super( bd );
		//	-----------------------------------------------------------------
		_hashZobrist = bd.getZobristHash();

		generateSome( ~0L, Square.getMask( iSqTo ));
		}

	public MoveList( Board bd, long bbFromMask, long bbToMask )
		{
		super( bd );
		//	-----------------------------------------------------------------
		_hashZobrist = bd.getZobristHash();

		generateSome( bbFromMask, bbToMask);
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Sort all the moves into place based on the score supplied.
	 *
	 * @param scorer
	 * 	Object to provide scores.
	 *
	 * @return Always this
	 */
	public MoveList sort( IMoveScorer scorer )
		{
		DBC.requireNotNull( scorer, "Move Scorer" );
		if (_iCount <= 1) return this;
		//	-----------------------------------------------------------------
		int[] scores = new int[ _iCount ];

		for ( int idx = 0; idx < _iCount; ++idx )
			scores[ idx ] = scorer.scoreMove( _board, new Move( _moves[ idx ], _hashZobrist ) );
		//
		//	Now do a simple selection sort, using the scores[] array as the
		//	determining factor.
		//
		for ( int index = 0; index < _iCount; ++index )
			{
			int best = index;

			for ( int idx = index + 1; idx < _iCount; ++idx )
				if (scores[ idx ] > scores[ best ])
					best = idx;

			if (best == index) continue; // got lucky...no change

			int tmp = scores[ index ];
			scores[ index ] = scores[ best ];
			scores[ best ] = tmp;

			tmp = _moves[ index ];
			_moves[ index ] = _moves[ best ];
			_moves[ best ] = tmp;
			}

		return this;
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC GETTERS & SETTERS
	//	-----------------------------------------------------------------------

	/**
	 * Gets the move at a given index.
	 *
	 * @param index
	 * 	Zero-based index of move.
	 *
	 * @return Move if valid index; <c>null/c> otherwise.
	 */
	public Move getAt( int index )
		{
		return (index >= 0 && index < _iCount)
			   ? new Move( _moves[ index ], _hashZobrist )
			   : null;
		}

	/**
	 * Tests the move list to see if it is empty, i.e., has no moves.
	 *
	 * @return <code>.T.</code> if empty; <code>.F.</code> if one or more moves present.
	 */
	public boolean isEmpty()
		{
		return (_iCount <= 0);
		}

	/**
	 * Gets the number of legal moves found.
	 *
	 * @return Move count.
	 */
	public int size()
		{
		return _iCount;
		}

	//  -----------------------------------------------------------------------
	//	INTERFACE: Iterable<Move>
	//	-----------------------------------------------------------------------

	@Override
	public Iterator<Move> iterator()
		{
		return new MoveListIterator();
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: MoveListIterator
	//	-----------------------------------------------------------------------

	private class MoveListIterator implements Iterator<Move>
		{
		/** Next element in the _moves[] array. */
		private int _iNext = 0;

		/**
		 * Returns true if the move list has more moves.
		 *
		 * @return <code>.T.</code> if more moves available; <code>.F.</code> otherwise.
		 */
		@Override
		public boolean hasNext()
			{
			return _iNext < _iCount;
			}

		/**
		 * Returns the next move in the move list.
		 *
		 * @return Next move, or <code>null</code> if no more moves available.
		 */
		@Override
		public Move next()
			{
			assert (_iNext < _iCount);
			//	-------------------------------------------------------------
			return new Move( _moves[ _iNext++ ], _hashZobrist );
			}

		@Override
		public void remove()
			{
			throw new sun.reflect.generics.reflectiveObjects.NotImplementedException();
			}
		} /* end of nested class MoveListIterator */

	} /* end of class MoveList */
