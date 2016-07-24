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

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.humble.*;
import net.humbleprogrammer.maxx.interfaces.IMoveScorer;

import static net.humbleprogrammer.maxx.Constants.*;

@SuppressWarnings( "WeakerAccess" )
public class Evaluator
	{

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Piece Value Table */
	private static final   int[]  s_pieceValue = { 0, 100, 325, 325, 500, 900, 0 };
	/** Logger */
	protected static final Logger s_log        = LoggerFactory.getLogger( Evaluator.class );

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Returns the material score, from the perspective of the moving player.
	 *
	 * @param bd
	 * 	Position to evaluate.
	 *
	 * @return Material difference.
	 */
	public static int getMaterialScore( Board bd )
		{
		DBC.requireNotNull( bd, "Board" );
		//	-----------------------------------------------------------------
		//	@formatter:off
		int iScore = ((BitUtil.count( bd.map[ MAP_W_PAWN ] ) * s_pieceValue[ PAWN ]) +
					  (BitUtil.count( bd.map[ MAP_W_KNIGHT ] ) * s_pieceValue[ KNIGHT ]) +
					  (BitUtil.count( bd.map[ MAP_W_BISHOP ] ) * s_pieceValue[ BISHOP ]) +
					  (BitUtil.count( bd.map[ MAP_W_ROOK ] ) * s_pieceValue[ ROOK ]) +
					  (BitUtil.count( bd.map[ MAP_W_QUEEN ] ) * s_pieceValue[ QUEEN ])) -
					 ((BitUtil.count( bd.map[ MAP_B_PAWN ] ) * s_pieceValue[ PAWN ]) +
					  (BitUtil.count( bd.map[ MAP_B_KNIGHT ] ) * s_pieceValue[ KNIGHT ]) +
					  (BitUtil.count( bd.map[ MAP_B_BISHOP ] ) * s_pieceValue[ BISHOP ]) +
					  (BitUtil.count( bd.map[ MAP_B_ROOK ] ) * s_pieceValue[ ROOK ]) +
					  (BitUtil.count( bd.map[ MAP_B_QUEEN ] ) * s_pieceValue[ QUEEN ]));
		//	@formatter:on

		return (bd.getMovingPlayer() == WHITE) ? iScore : -iScore;
		}

	/**
	 * Gets the value of a piece by type.
	 *
	 * @param pt
	 * 	Piece type (PAWN, KNIGHT, BISHOP, etc.)
	 *
	 * @return Piece value.
	 */
	public static int getPieceValue( int pt )
		{
		return (pt >= PAWN && pt < KING) ? s_pieceValue[ pt ] : 0;
		}

	/**
	 * Finds all "en prise" pieces for a given position.
	 *
	 * @param bd
	 * 	Position to examine.
	 *
	 * @return Bitboard of pieces that are attacked, but not defended.
	 */
	public static long findEnPrisePieces( Board bd )
		{
		if (bd == null) return 0L;
		//	-----------------------------------------------------------------
		long bbEnPrise = 0L;
		int opponent = bd.getMovingPlayer() ^ 1;

		for ( long bb = bd.map[ opponent ]; bb != 0L; bb &= (bb - 1) )
			{
			int iSq = BitUtil.first( bb );

			if (isEnPrise( bd, iSq )) bbEnPrise |= Square.getMask( iSq );
			}

		return bbEnPrise;
		}

	/**
	 * Find all "Mate in X" moves.
	 *
	 * @param bd
	 * 	Position to analyze
	 * @param iMaxMoves
	 * 	Maximum number of moves, which must be .GT. zero.
	 *
	 * @return List of Variations.
	 */
	public static List<PV> findMateIn( final Board bd, int iMaxMoves )
		{
		return findMateIn( bd, iMaxMoves, true );
		}

	/**
	 * Find all "Mate in X" moves.
	 *
	 * @param bd
	 * 	Position to analyze
	 * @param iMaxMoves
	 * 	Maximum number of moves, which must be .GT. zero.
	 *
	 * @return List of variations.
	 */
	public static List<PV> findMateIn( final Board bd, int iMaxMoves, boolean bExactDepth )
		{
		final int iPlies = (iMaxMoves * 2) - 1;

		return (bd != null && iPlies > 0) ? new MateSearch().search( bd, iPlies, bExactDepth )
										  : new ArrayList<PV>();
		}

	/**
	 * Determines if the piece on a given square is "en prise", i.e., it is
	 * attacked, but not defended.
	 *
	 * @param bd
	 * 	Position to examine.
	 *
	 * @return <code>.T.</code> if square is attacked but not defended; <code>false</code>
	 * otherwise.
	 */
	public static boolean isEnPrise( Board bd, int iSqTarget )
		{
		if (bd == null || !Square.isValid( iSqTarget )) return false;
		//	-----------------------------------------------------------------
		MoveList moves = MoveList.generateCaptures( bd, iSqTarget );

		for ( Move mv : moves )
			if (MoveList.generateCaptures( new Board( bd, mv ), iSqTarget ).isEmpty())
				return true;

		return false;
		}

	/**
	 * Determines if a score is a forced mate.
	 *
	 * @param iScore
	 * 	Score to test.
	 *
	 * @return .T. if a mate, .F. otherwise.
	 */
	public static boolean isMateScore( int iScore )
		{
		return (Math.abs( iScore ) > (MAX_SCORE - MAX_MATE_DEPTH)); // ~32,255
		}

	//  -----------------------------------------------------------------------
	//	GETTERS & SETTERS
	//	-----------------------------------------------------------------------

	/**
	 * Gets the total node count.
	 *
	 * @return Node count.
	 */
	public static long getNodeCount()
		{
		return MateSearch.s_nodes;
		}

	/**
	 * Gets the "Nodes per Second" rate.
	 *
	 * @return NPS
	 */
	public static long getNPS()
		{
		return (MateSearch.s_elapsedMSecs > 0)
			   ? ((1000L * MateSearch.s_nodes) / MateSearch.s_elapsedMSecs)
			   : 0L;
		}

	/**
	 * Resets the node statistics.
	 */
	@SuppressWarnings( "unused" )
	public static void resetNPS()
		{ MateSearch.s_elapsedMSecs = MateSearch.s_nodes = 0L; }
	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Force a score to the allowable range.
	 *
	 * @param score
	 * 	Score to clamp.
	 *
	 * @return Clamped score, in the range [MIN_SCORE..MAX_SCORE]
	 */
	static int clampScore( int score )
		{
		if (score > MAX_SCORE) return MAX_SCORE;
		if (score < MIN_SCORE) return MIN_SCORE;

		return score;
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: MateSearch
	//	-----------------------------------------------------------------------

	private static class MateSearch implements IMoveScorer
		{
		/** Total count of nodes visited */
		static long s_nodes;
		/** Elapsed milliseconds */
		static long s_elapsedMSecs;

		/** Maximum search depth, in plies. */
		private int  _iMaxDepth;
		/** Number of nodes tested. */
		private long _lNodes;
		/** Pre-allocated array of lines. */
		private PV[] _pv;

		/**
		 * Root of the mate search.
		 *
		 * @param bd
		 * 	Position to search.
		 * @param bExactDepth
		 * 	.T. for exact depth only, .F. for shorter mates.
		 *
		 * @return List of solutions.
		 */
		List<PV> search( final Board bd, int maxDepth, boolean bExactDepth )
			{
			assert bd != null;
			assert maxDepth > 0;
			//	-------------------------------------------------------------
			final List<PV> solutions = new ArrayList<>();
			final Stopwatch swatch = Stopwatch.startNew();

			_iMaxDepth = maxDepth;
			_lNodes = 0L;
			_pv = new PV[ _iMaxDepth ];

			for ( int idx = 0; idx < _pv.length; ++idx )
				_pv[ idx ] = new PV();
			//
			//	Try all the top-level (root) moves.
			//
			MoveList moves = MoveList.generate( bd );

			moves.sort( this );

			for ( Move move : moves )
				{
				int iScore = -search( new Board( bd, move ), 0, MIN_SCORE, MAX_SCORE );

				if (iScore > (MAX_SCORE - MAX_MATE_DEPTH))
					{
					PV pv = new PV( move, _pv[ 0 ] );

					if (pv.size() < _iMaxDepth)
						{
						solutions.clear();

						if (bExactDepth)
							break;

						//	New max depth...
						_iMaxDepth = pv.size();
						}

					solutions.add( pv );
					}
				}

			swatch.stop();
			s_nodes += _lNodes;
			s_elapsedMSecs += swatch.getElapsedMillisecs();

			return solutions;
			}

		/**
		 * Non-root (recursive) part of the mate search.
		 *
		 * @param bd
		 * 	Position to search.
		 * @param iDepth
		 * 	Current depth, which should always be .GT. zero.
		 * @param iAlpha
		 * 	Alpha value (low cut off)
		 * @param iBeta
		 * 	Beta value (high cut off)
		 *
		 * @return Score.
		 */
		private int search( final Board bd, int iDepth, int iAlpha, int iBeta )
			{
			assert bd != null;
			assert iDepth >= 0;
			assert iAlpha < iBeta;
			//	-------------------------------------------------------------
			final int iDeeper = iDepth + 1;
			final int scoreMate = MAX_SCORE - iDepth;

			_lNodes++;
			_pv[ iDepth ].clear();
			//
			//	If this is a leaf node, the only thing we care about is whether or not the
			//	player has been mated.
			//
			if (iDeeper >= _iMaxDepth)
				{
				return Arbiter.isMated( bd )
					   ? -scoreMate
					   : Evaluator.getMaterialScore( bd );
				}
			//
			//	Now try the moves.
			//
			MoveList moves = MoveList.generate( bd );

			for ( Move move : moves.sort( this ) )
				{
				if (move.getPromotionPiece() == BISHOP || move.getPromotionPiece() == ROOK)
					continue; // ignore underpromotions for mate-in-x problems

				int iScore = -search( new Board( bd, move ), iDeeper, -iBeta, -iAlpha );

				if (iScore > iAlpha)
					{
					if (iScore >= iBeta) return iBeta;

					iAlpha = iScore;
					_pv[ iDepth ].build( move, _pv[ iDeeper ] );
					}
				}

			return moves.isEmpty()
				? (Arbiter.isInCheck( bd ) ? -scoreMate : 0)
				: iAlpha;
			}

		@Override
		public int scoreMove( final Board bd, final Move move )
			{
			assert bd != null;
			assert bd.isLegalMove( move );
			//	-----------------------------------------------------
			final Board bdAfter = new Board( bd, move );

			int score = Arbiter.isInCheck( bdAfter )
						? (MAX_SCORE >> 2) // BIG bonus for checking moves.
						: 0;
			//
			//	Bonus for capturing stuff, because that means fewer defenders.
			//
			int victim = Piece.getType( bd.sq[ move.iSqTo ] );

			if (victim != EMPTY)
				score += getPieceValue( victim );
			//
			//	Bonus for promoting a pawn, because we can always use bigger pieces...
			//
			if (move.isPromotion())
				score += getPieceValue( move.getPromotionPiece() ) - getPieceValue( PAWN );

			//	Penalize distance from opponent's King (-8 pts for every square away)
			int player = bd.getMovingPlayer();
			int opposingKingSq = bd.getKingSquare( player ^ 1 );

			score -= (Square.distance( move.iSqTo, opposingKingSq ) - 1) << 3;

			return clampScore( score );
			}
		}
	}
