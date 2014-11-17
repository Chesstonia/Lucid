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

import net.humbleprogrammer.maxx.factories.BoardFactory;
import net.humbleprogrammer.maxx.factories.MoveFactory;

import java.util.*;

/**
 * The {@link Variation} class stores a sequence of moves.
 */
public class Variation implements Iterable<Move>
    {
    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Current position. */
    private final Board _board;
    /** List of moves */
    private final List<Move> _moves = new ArrayList<>();

    /** First ply of the variation. */
    private int         _iFirstPly;
    /** Starting position. */
    private Board.State _stateStart;
    /** Result, or <code>null</code> if not set. */
    private Result      _result;

    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    public Variation()
        {
        _board = BoardFactory.createInitial();
        _stateStart = _board.getState();
        }
    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Appends a move to the variation.
     *
     * @param move
     *     Move to append.
     *
     * @return <code>.T.</code> if added; <code>.F.</code> otherwise.
     */
    public boolean appendMove( final Move move )
        {
        if (_board.isLegalMove( move ) && _moves.add( move ))
            {
            _board.makeMove( move );
            return true;
            }

        return false;
        }

    /**
     * Appends a move to the variation.
     *
     * @param strSAN
     *     Move to append, in SAN notation.
     *
     * @return <code>.T.</code> if added; <code>.F.</code> otherwise.
     */
    public boolean appendMove( final String strSAN )
        { return appendMove( MoveFactory.fromSAN( _board, strSAN ) ); }

    /**
     * Tests if the variation contains any moves.
     *
     * @return .T. if at least one move present; .F. otherwise.
     */
    public boolean isEmpty()
        { return _moves.isEmpty(); }
    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Gets the current position.
     *
     * @return Board.
     */
    public Board getCurrentPosition()
        { return _board; }

    /**
     * Gets the move move at a given move number.
     *
     * @param iMoveNum
     *     Move number (starts at 1).
     * @param player
     *     Moving player [WHITE|BLACK].
     *
     * @return Move made by the player at the requested point in the variation.
     */
    public Move getMove( int iMoveNum, int player )
        {
        if (iMoveNum < 1 || (player & ~0x01) != 0)
            return null;
        /*
        **  CODE
        */
        final int index = Board.computePly( iMoveNum, player ) - _iFirstPly;
        Move move = null;

        if (index >= 0 && index < _moves.size())
            {
            move = _moves.get( index );

            assert move.state.iFullMoves == iMoveNum;
            assert move.state.player == player;
            }

        return move;
        }

    /**
     * Gets the position <i>BEFORE</i> the player's move at a given move number.
     *
     * @param iMoveNum
     *     Move number (starts at 1)
     * @param player
     *     Moving player [WHITE|BLACK]
     *
     * @return Board position, or <code>null</code> if move number or player are invalid.
     */
    public Board getPosition( final int iMoveNum, final int player )
        {
        if (iMoveNum < 1)
            return null;
        /*
        **  CODE
        */
        if (_stateStart.iFullMoves == iMoveNum && _stateStart.player == player)
            return new Board( _stateStart );
        //
        //  Not the initial position, so get the move and create the board from
        //  the saved move state.
        //
        final Move move = getMove( iMoveNum, player );

        return (move != null)
               ? new Board( move.state )
               : null;
        }

    /**
     * Gets the result.
     *
     * @return Result, or <code>null</code> if not set.
     */
    public Result getResult()
        { return _result; }

    /**
     * Sets the result.
     *
     * @param result
     *     Desired result.
     */
    public void setResult( Result result )
        { _result = result; }

    /**
     * Sets the starting position for the variation.  This is only necessary if the variation
     * doesn't start at the initial position.
     *
     * @param bd
     *     Starting position.
     *
     * @return <code>.T.</code> if position is legal; <code>.F.</code> otherwise.
     */
    public boolean setStartingPosition( final Board bd )
        {
        if (bd == null)
            return false;
        /*
        **  CODE
        */
        _moves.clear();
        _stateStart = bd.getState();
        _board.setState( _stateStart );
        _iFirstPly = Board.computePly( _stateStart.iFullMoves, _stateStart.player );

        return true;
        }

    /**
     * Sets the starting position for the variation.  This is only necessary if the variation
     * doesn't start at the initial position.
     *
     * @param strFEN
     *     Starting position expressed as a FEN string.
     *
     * @return <code>.T.</code> if position is legal; <code>.F.</code> otherwise.
     */
    public boolean setStartingPosition( final String strFEN )
        { return setStartingPosition( BoardFactory.createFromFEN( strFEN ) ); }

    //  -----------------------------------------------------------------------
    //	INTEFACE: Iterable<Move>
    //	-----------------------------------------------------------------------
    @Override
    public Iterator<Move> iterator()
        { return _moves.iterator(); }
    }   /* end of class Variation */
