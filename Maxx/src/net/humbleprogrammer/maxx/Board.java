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

import net.humbleprogrammer.humble.BitUtil;
import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.maxx.factories.BoardFactory;

import java.util.Arrays;

import static net.humbleprogrammer.maxx.Constants.*;

/**
 * The Board entity is primarily responsible for keeping track of the game in progress.
 *
 * It must be able to verify that the current position is valid, and make/unmake moves.  It must
 * also be able to determine whether or not the moving player is in check.  This requires
 * maintaining the following information: <ul> <li>location of each piece</li> <li>which player
 * is "on the move"</li> <li>whether or not castling moves are possible</li> <li>if an en
 * passant capture is possible.</li> </ul>
 *
 * Conceptually, the Board represents a snapshot of the game in progress, and as such does not
 * maintain historical information: each new position completely supersedes it predecessors.
 * Consequently, the Board is not able to 'undo' or 'redo' moves, nor is it able to detect
 * repetition of moves or positions.
 *
 * Since a 'half move clock' is maintained, it is possible to detect games drawn by the '50-move
 * rule'.  Games drawn by insufficient material can also be detected.
 *
 * The Board entity can determine: <ul> <li>If a square on the board is empty or occupied</li>
 * <li>The piece (color and type) on an occupied square</li> <li>If the current position is
 * legal within the rules of chess</li> <li>If there is an en passant capture possible, and if
 * so, on which square</li> <li>The color of the player "on the move" (and "off the move").</li>
 * </ul> The Board entity <b>cannot</b> determine: <ul> <li>What the previous move was*</li>
 * <li>How many moves have been made so far by either player</li> <li>How many times the current
 * position has occurred</li> <li>If the game has reached a verdict.</li> </ul> * pawn advances
 * can be inferred by the existence of an e.p. square
 */
@SuppressWarnings( "unused" )
public class Board
    {

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Castling flags, by square. */
    private static final int[] s_castling = new int[ 64 ];

    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Current castling privileges. */
    private int _castling   = CastlingFlags.NONE;
    /** Color of player currently "on the move" */
    private int _player     = WHITE;
    /** Move number, which starts at one and increments after Black's move. */
    private int _iFullMoves = 1;
    /** Half move clock, which is the number of moves since the last capture or pawn move. */
    private int _iHalfMoves;
    /** En passant square in 8x8 format, or <c>INVALID</c> if e.p. not possible. */
    private int  _iSqEP      = INVALID;
    /** Zobrist hash of pieces, castling privileges, e.p. square, and moving player. */
    private long _hashFull   = HASH_BLANK;
    /** Zobrist hash of piece/square combinations. */
    private long _hashPieces = HASH_BLANK;

    /** Array of pieces */
    private final int[]  _sq  = new int[ 64 ];
    /** Array of bitboards. */
    private final long[] _map = new long[ 14 ];
    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    static
        {
        Arrays.fill( s_castling, CastlingFlags.ALL );

        s_castling[ Square.A1 ] &= ~CastlingFlags.WHITE_LONG;
        s_castling[ Square.E1 ] &= ~CastlingFlags.WHITE_BOTH;
        s_castling[ Square.H1 ] &= ~CastlingFlags.WHITE_SHORT;

        s_castling[ Square.A8 ] &= ~CastlingFlags.BLACK_LONG;
        s_castling[ Square.E8 ] &= ~CastlingFlags.BLACK_BOTH;
        s_castling[ Square.H8 ] &= ~CastlingFlags.BLACK_SHORT;
        }

    /**
     * Default CTOR for the {@link Board} class.
     */
    public Board()
        {
        /*
        **  EMPTY CTOR
        */
        }

    /**
     * Copy CTOR for the {@link Board} class.
     *
     * @param src
     *     Board to copy from.
     */
    public Board( Board src )
        {
        assert src != null;
        assert src != this;
        /*
        **  CODE
        */
        setState( src.getState() );
        }

    /**
     * Alternate CTOR for the {@link Board} class.
     *
     * @param state
     *     Board state to copy from.
     */
    Board( State state )
        {
        assert state != null;
        /*
        **  CODE
        */
        setState( state );
        }

    //  -----------------------------------------------------------------------
    //	OVERRIDES
    //	-----------------------------------------------------------------------

    @Override
    public boolean equals( Object obj )
        {
        return ((obj instanceof Board) &&
                (obj == this || _hashFull == ((Board) obj)._hashFull));
        }

    @Override
    public int hashCode()
        {
        return ((397 * (int) (_hashFull >>> 32)) ^ (int) (_hashFull & 0xFFFFFFFFL));
        }

    /**
     * Creates a Forsth-Edwards Notation (FEN) string for the board.
     *
     * @return FEN string.
     */
    @Override
    public String toString()
        {
        return BoardFactory.toString( this );
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Computes the ply from a move number and player.
     *
     * @param iMoveNum
     *     Move number (starts at 1).
     * @param player
     *     Moving player [WHITE|BLACK].
     *
     * @return Zero-based ply number, or INVALID if move number is less than 1.
     */
    public static int computePly( final int iMoveNum, final int player )
        {
        return (iMoveNum >= 1)
               ? (((iMoveNum - 1) << 1) + (player & 0x01))
               : INVALID;
        }

    /**
     * Tests a board for validity.
     *
     * In order to be valid, all of the following must be <i>true</i>: <ul> <li>Neither side can
     * have more than 16 pieces.</li> <li>Each side must have one (and only one) King.</li>
     * <li>Neither side can have more than 8 pawns.</li> <li>No pawns on the first or last
     * rank.</li> <li>The player "on the move" cannot be able to capture the opposing King.</li>
     * </ul>
     *
     * @return .T. if the position is valid; .F. otherwise.
     */
    public boolean isLegal()
        {
        //  Test 1 -- neither player can have more than 16 pieces on the board.
        if (BitUtil.count( _map[ MAP_W_ALL ] ) > 16 || BitUtil.count( _map[ MAP_B_ALL ] ) > 16)
            return false;

        //  Test 2 -- both players must have one (and only one) king on the board.
        if (!BitUtil.singleton( _map[ MAP_W_KING ] ) ||
            !BitUtil.singleton( _map[ MAP_B_KING ] ))
            return false;

        // Test 3 -- neither player can have more than 8 pawns on the board.
        final int iWPawns = BitUtil.count( _map[ MAP_W_PAWN ] );
        final int iBPawns = BitUtil.count( _map[ MAP_B_PAWN ] );

        if (iWPawns > 8 || iBPawns > 8)
            return false;

        // Test 4 -- no pawns on the first or last rank.
        if (((_map[ MAP_W_PAWN ] | _map[ MAP_B_PAWN ]) & 0xFF000000000000FFL) != 0L)
            return false;

        //  Test 5 -- no more than 9 queens + pawns
        if ((BitUtil.count( _map[ MAP_W_QUEEN ] ) + iWPawns) > 9 ||
            (BitUtil.count( _map[ MAP_B_QUEEN ] ) + iBPawns) > 9)
            {
            return false;
            }

        //  Test 6 -- no more than 10 minor pieces + pawns
        if ((BitUtil.count( _map[ MAP_W_KNIGHT ] ) + iWPawns) > 10 ||   // white knights
            (BitUtil.count( _map[ MAP_B_KNIGHT ] ) + iBPawns) > 10 ||   // black knights
            (BitUtil.count( _map[ MAP_W_BISHOP ] ) + iWPawns) > 10 ||   // white bishops
            (BitUtil.count( _map[ MAP_B_BISHOP ] ) + iBPawns) > 10 ||   // black bishops
            (BitUtil.count( _map[ MAP_W_ROOK ] ) + iWPawns) > 10 ||   // white rooks
            (BitUtil.count( _map[ MAP_B_ROOK ] ) + iBPawns) > 10)     // black rooks
            {
            return false;
            }

        // Test 7 -- moving player's king can't be in check.
        return (_player == WHITE)
               ? !Bitboards.isAttackedBy( _map, getKingSquare( WHITE ), BLACK )
               : !Bitboards.isAttackedBy( _map, getKingSquare( BLACK ), WHITE );
        }

    /**
     * Tests a move for legality against the current position.
     *
     * @param move
     *     Move to test.
     *
     * @return <code>.T.</code> if move is legal; <code>.F.</code> otherwise.
     */
    public boolean isLegalMove( final Move move )
        {
        return (move != null && move.state.hashFull == _hashFull);
        }

    /**
     * Makes a move on the board.
     *
     * @param move
     *     Move to make.
     */
    public void makeMove( Move move )
        {
        DBC.requireNotNull( move, "move" );

        if (move.state.hashFull != _hashFull)
            throw new IllegalMoveException( this, move );
        /*
        **  CODE
        */
        final int iSqFrom = move.iSqFrom;
        final int iSqTo = move.iSqTo;

        _iSqEP = INVALID;

        if (_sq[ iSqTo ] != EMPTY)
            {
            _iHalfMoves = 0;
            removePiece( iSqTo );
            }
        else if (_sq[ iSqFrom ] <= MAP_B_PAWN)
            _iHalfMoves = 0;
        else
            _iHalfMoves++;

        switch (move.iType)
            {
            case Move.Type.NORMAL:
                movePiece( iSqFrom, iSqTo );
                break;

            case Move.Type.CASTLING:
                movePiece( iSqFrom, iSqTo );
                if (iSqTo > iSqFrom)    // .T. if O-O; .F. if O-O-O
                    movePiece( iSqTo + 1, iSqTo - 1 );
                else
                    movePiece( iSqTo - 2, iSqTo + 1 );
                break;

            case Move.Type.EN_PASSANT:
                movePiece( iSqFrom, iSqTo );
                removePiece( (iSqFrom & 0x38) | (iSqTo & 0x07) );
                break;

            case Move.Type.PAWN_PUSH:
                movePiece( iSqFrom, iSqTo );
                _iSqEP = (iSqFrom + iSqTo) >>> 1;
                break;

            case Move.Type.PROMOTION:
                removePiece( iSqFrom );
                placePiece( iSqTo, Piece.W_QUEEN + _player );
                break;

            case Move.Type.PROMOTE_ROOK:
                removePiece( iSqFrom );
                placePiece( iSqTo, Piece.W_ROOK + _player );
                break;

            case Move.Type.PROMOTE_BISHOP:
                removePiece( iSqFrom );
                placePiece( iSqTo, Piece.W_BISHOP + _player );
                break;

            case Move.Type.PROMOTE_KNIGHT:
                removePiece( iSqFrom );
                placePiece( iSqTo, Piece.W_KNIGHT + _player );
                break;

            default:
                throw new RuntimeException( "Unrecognized move type." );
            }
        //
        //  Update the castling flags, move number, and flip the player.
        //
        if (_castling != CastlingFlags.NONE)
            _castling &= s_castling[ iSqFrom ] & s_castling[ iSqTo ];

        if ((_player ^= 1) == WHITE)
            _iFullMoves++;

        move.hashAfter = _hashFull = _hashPieces ^
                                     ZobristHash.getExtraHash( _castling, _iSqEP, _player );
        }

    /**
     * Takes back a move on the board.
     *
     * @param move
     *     Move to take back.
     */
    public void undoMove( final Move move )
        {
        DBC.requireNotNull( move, "move" );

        if (move.hashAfter != _hashFull)
            throw new IllegalMoveException( this, move );
        /*
        **  CODE
        */
        setState( move.state );
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Gets the piece on a square.
     *
     * @param iSq
     *     Square index in 8x8 format.
     *
     * @return Piece on square, or <c>EMPTY</c> if square is empty.
     */
    public int get( final int iSq )
        {
        return Square.isValid( iSq ) ? _sq[ iSq ] : EMPTY;
        }

    /**
     * Sets the piece on a square.
     *
     * @param iSq
     *     Square index in 8x8 format.
     * @param piece
     *     Piece to place, or <c>null</c> to remove the existing piece.
     *
     * @return .T. on success, .F. on failure.
     */
    public boolean set( final int iSq, final int piece )
        {
        if (!Square.isValid( iSq ) || piece < MAP_W_PAWN || piece > MAP_B_KING)
            return false;
        /*
        **  CODE
        */
        if (_sq[ iSq ] != piece)
            {
            if (_sq[ iSq ] != EMPTY)
                removePiece( iSq );

            if (piece != EMPTY)
                placePiece( iSq, piece );

            _hashFull = _hashPieces ^ ZobristHash.getExtraHash( _castling, _iSqEP, _player );
            }

        return true;
        }

    /**
     * Gets all pieces for a given type that can move to a target square.
     *
     * @param iSqTo
     *     Target square, in 8x8 format.
     * @param pt
     *     Piece type [PAWN|KNIGHT|BISHOP|ROOK|QUEEN|KING].
     *
     * @return Bitboard of pieces.
     */
    public long getCandidates( int iSqTo, int pt )
        {
        if (!Square.isValid( iSqTo ))
            return 0L;
        /*
        **  CODE
        */
        switch (pt)
            {
            case PAWN:
                return (_player == WHITE)
                       ? (_map[ MAP_W_PAWN ] & (Bitboards.pawnDownwards[ iSqTo ] |
                                                Bitboards.fileMask[ iSqTo & 0x07 ]))
                       : (_map[ MAP_B_PAWN ] & (Bitboards.pawnUpwards[ iSqTo ] |
                                                Bitboards.fileMask[ iSqTo & 0x07 ]));

            case KNIGHT:
                return _map[ MAP_W_KNIGHT + _player ] & Bitboards.knight[ iSqTo ];

            case BISHOP:
                return _map[ MAP_W_BISHOP + _player ] &
                       Bitboards.getAttackedBy( _map, iSqTo, _player );

            case ROOK:
                return _map[ MAP_W_ROOK + _player ] &
                       Bitboards.getAttackedBy( _map, iSqTo, _player );

            case QUEEN:
                return _map[ MAP_W_QUEEN + _player ] &
                       Bitboards.getAttackedBy( _map, iSqTo, _player );

            case KING:
                //  Don't mask against Bitboards.king[] because that excludes castling moves.
                return _map[ MAP_W_KING + _player ];
            }

        return 0L;
        }

    /**
     * Gets the current castling privileges.
     *
     * @return Set of <c>CastlingFlags.*</c> values.
     */
    public int getCastlingFlags()
        {
        return _castling;
        }

    /**
     * Sets the castling flags.
     *
     * @param iFlags
     *     Flags to set
     */
    public void setCastlingFlags( int iFlags )
        {
        iFlags &= CastlingFlags.ALL;    // drop any extra bits
        //
        //  Check White player's King and Rooks
        //
        if (_sq[ Square.E1 ] != Piece.W_KING)
            iFlags &= ~CastlingFlags.WHITE_BOTH;
        else
            {
            if (_sq[ Square.A1 ] != Piece.W_ROOK)
                iFlags &= ~CastlingFlags.WHITE_LONG;
            if (_sq[ Square.H1 ] != Piece.W_ROOK)
                iFlags &= ~CastlingFlags.WHITE_SHORT;
            }
        //
        //  And the same for Black
        //
        if (_sq[ Square.E8 ] != Piece.B_KING)
            iFlags &= ~CastlingFlags.BLACK_BOTH;
        else
            {
            if (_sq[ Square.A8 ] != Piece.B_ROOK)
                iFlags &= ~CastlingFlags.BLACK_LONG;
            if (_sq[ Square.H8 ] != Piece.B_ROOK)
                iFlags &= ~CastlingFlags.BLACK_SHORT;
            }

        if (_castling != iFlags)
            {
            _castling = iFlags;
            _hashFull = _hashPieces ^ ZobristHash.getExtraHash( _castling, _iSqEP, _player );
            }
        }

    /**
     * Gets the current En Passant square.
     *
     * @return e.p. square index, in 8x8 format.
     */
    public int getEnPassantSquare()
        {
        return _iSqEP;
        }

    /**
     * Sets the current En Passant square.
     *
     * @param iSq
     *     Square index in 8x8 format.
     */
    public void setEnPassantSquare( int iSq )
        {
        if (Square.isValid( iSq ) && _sq[ iSq ] == EMPTY)
            {
            if (_player == WHITE)
                {
                if (Square.getRank( iSq ) != 5 || _sq[ iSq - 8 ] != Piece.B_PAWN)
                    iSq = INVALID;
                }
            else // if (_player == BLACK)
                {
                if (Square.getRank( iSq ) != 2 || _sq[ iSq + 8 ] != Piece.W_PAWN)
                    iSq = INVALID;
                }
            }
        else
            iSq = INVALID;

        if (_iSqEP != iSq)
            {
            _iSqEP = iSq;
            _hashFull = _hashPieces ^ ZobristHash.getExtraHash( _castling, _iSqEP, _player );
            }
        }

    /**
     * Gets the current 'half move clock'.
     *
     * @return Number of plies since last capture or pawn move.
     */
    public int getHalfMoveClock()
        {
        return _iHalfMoves;
        }

    /**
     * Sets the current 'half move clock'.
     *
     * @param iNumber
     *     Number of pliece since last capture or pawn move, which must be .GE. zero.
     */
    public void setHalfMoveClock( int iNumber )
        {
        _iHalfMoves = Math.max( iNumber, 0 );
        }

    /**
     * Gets the square occupied by a player's King.
     *
     * @param iPlayer
     *     Player color [WHITE|BLACK].
     *
     * @return King square in 8x8 format, or INVALID if no king on the board.
     */
    public int getKingSquare( final int iPlayer )
        {
        return BitUtil.first( _map[ MAP_W_KING + (iPlayer & 0x01) ] );
        }

    /**
     * Gets all legal moves for the current position.
     *
     * @return {@link MoveList} object.
     */
    public MoveList getLegalMoves()
        {
        MoveList moves = new MoveList( this );
        moves.generate();
        return moves;
        }

    /**
     * Gets the current move number.
     *
     * @return Move number.
     */
    public int getMoveNumber()
        {
        return _iFullMoves;
        }

    /**
     * Sets the current mvoe number.
     *
     * @param iNumber
     *     Move number, which must be .GT. zero.
     */
    public void setMoveNumber( int iNumber )
        {
        _iFullMoves = Math.max( iNumber, 1 );
        }

    /**
     * Gets the player currently "on the move".
     *
     * @return [WHITE|BLACK].
     */
    public int getMovingPlayer()
        {
        return _player;
        }

    /**
     * Sets the color of the player currently "on the move".
     *
     * @param player
     *     Moving player [WHITE|BLACK]
     */
    public void setMovingPlayer( final int player )
        {
        DBC.require( (player == WHITE || player == BLACK), "Invalid player color." );
        /*
        **  CODE
        */
        if (_player != player)
            {
            _player = player;
            _hashFull = _hashPieces ^ ZobristHash.getExtraHash( _castling, _iSqEP, _player );
            }
        }

    /**
     * Gets the Zobrist hash for the current position.
     *
     * @return 64-bit hash value.
     */
    public long getZobristHash()
        {
        return _hashFull;
        }

    //  -----------------------------------------------------------------------
    //	GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Collects the internal board state.
     *
     * @return Board state.
     */
    State getState()
        {
        return new State( this );
        }

    /**
     * Restores the board to a previously saved state.
     *
     * @param state
     *     State to restore.
     */
    void setState( State state )
        {
        DBC.requireNotNull( state, "Board state" );

        if (state.hashFull == _hashFull)
            return;
        /*
        **  CODE
        */
        _castling = state.castling;
        _iFullMoves = state.iFullMoves;
        _iHalfMoves = state.iHalfMoves;
        _iSqEP = state.iSqEP;
        _hashFull = state.hashFull;
        _hashPieces = state.hashPieces;
        _player = state.player;

        System.arraycopy( state.map, 0, _map, 0, MAP_LENGTH );
        System.arraycopy( state.sq, 0, _sq, 0, 64 );
        }

    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    /**
     * Moves a piece from one square to another.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     * @param iSqTo
     *     "To" square in 8x8 format.
     */
    private void movePiece( int iSqFrom, int iSqTo )
        {
        assert Square.isValid( iSqFrom );
        assert Square.isValid( iSqTo );

        assert _sq[ iSqFrom ] != EMPTY;
        assert _sq[ iSqTo ] == EMPTY;
        /*
        **  CODE
        */
        final int piece = _sq[ iSqFrom ];
        final long bbSqMask = (1L << iSqFrom) | (1L << iSqTo);

        _map[ piece ] ^= bbSqMask;
        _map[ piece & 1 ] ^= bbSqMask;

        _sq[ iSqFrom ] = EMPTY;
        _sq[ iSqTo ] = piece;

        _hashPieces ^= ZobristHash.getPieceHash( iSqFrom, iSqTo, piece );
        }

    /**
     * Places a piece on a square.
     *
     * @param iSq
     *     Square index, in 8x8 format.
     * @param piece
     *     Piece to place.
     */
    void placePiece( int iSq, final int piece )
        {
        assert Square.isValid( iSq );
        assert piece >= MAP_W_PAWN && piece <= MAP_B_KING;
        /*
        **  CODE
        */
        final long bbMask = 1L << iSq;

        _map[ piece ] |= bbMask;
        _map[ piece & 1 ] |= bbMask;

        _sq[ iSq ] = piece;
        _hashPieces ^= ZobristHash.getPieceHash( iSq, piece );
        }

    /**
     * Removes the piece on a square.
     *
     * @param iSq
     *     Square index, in 8x8 format.
     */
    void removePiece( int iSq )
        {
        assert Square.isValid( iSq );
        /*
        **  CODE
        */
        final int piece = _sq[ iSq ];
        final long bbNotMask = ~(1L << iSq);

        _map[ piece ] &= bbNotMask;
        _map[ piece & 1 ] &= bbNotMask;

        _sq[ iSq ] = EMPTY;
        _hashPieces ^= ZobristHash.getPieceHash( iSq, piece );
        }

    //  -----------------------------------------------------------------------
    //	NESTED CLASS: CastlingFlags
    //	-----------------------------------------------------------------------

    public static class CastlingFlags
        {
        /** No castling is possible in the current position. */
        public static final int NONE = 0;

        /** White can castle king-side (short). */
        @SuppressWarnings( "PointlessBitwiseExpression" )
        public static final int WHITE_SHORT = 1 << 0;
        /** White can castling queen-side (long). */
        public static final int WHITE_LONG  = 1 << 1;
        /** White can castle in both directions. */
        public static final int WHITE_BOTH  = WHITE_SHORT | WHITE_LONG;

        /** Black can castle king-side (short). */
        public static final int BLACK_SHORT = 1 << 2;
        /** Black can castling queen-side (long). */
        public static final int BLACK_LONG  = 1 << 3;
        /** Black can castle in both directions. */
        public static final int BLACK_BOTH  = BLACK_SHORT | BLACK_LONG;

        /** All of the castling flags. */
        public static final int ALL = WHITE_BOTH | BLACK_BOTH;

        /** Squares that block Black castling Queen-side (O-O-O). */
        static final long BLACK_LONG_MASK  = (Square.B8_MASK | Square.C8_MASK | Square.D8_MASK);
        /** Squares that block Black castling King-side (O-O). */
        static final long BLACK_SHORT_MASK = (Square.F8_MASK | Square.G8_MASK);
        /** Squares that block Black castling Queen-side (O-O-O). */
        static final long WHITE_LONG_MASK  = (Square.B1_MASK | Square.C1_MASK | Square.D1_MASK);
        /** Squares that block Black castling King-side (O-O). */
        static final long WHITE_SHORT_MASK = (Square.F1_MASK | Square.G1_MASK);
        }   /* end of class CastlingFlags */

    static class State
        {
        final int  castling;
        final int  iFullMoves;
        final int  iHalfMoves;
        final int  iSqEP;
        final int  player;
        final long hashFull;
        final long hashPieces;

        final int[]  sq;
        final long[] map;

        State( Board bd )
            {
            assert bd != null;
            /*
            **  CODE
            */
            castling = bd._castling;
            iFullMoves = bd._iFullMoves;
            iHalfMoves = bd._iHalfMoves;
            iSqEP = bd._iSqEP;
            hashFull = bd._hashFull;
            hashPieces = bd._hashPieces;
            player = bd._player;

            map = Arrays.copyOf( bd._map, MAP_LENGTH );
            sq = Arrays.copyOf( bd._sq, 64 );
            }
        }
    }   /* end of class Board */
