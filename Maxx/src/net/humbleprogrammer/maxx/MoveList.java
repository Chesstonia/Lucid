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

import java.util.Iterator;

import static net.humbleprogrammer.maxx.Constants.*;

@SuppressWarnings( "unused" )
public class MoveList implements Iterable<Move>
    {

    //  -----------------------------------------------------------------------
    //	CONSTANTS
    //	-----------------------------------------------------------------------

    /** Maximum possible moves in a single position. */
    private static final int MAX_MOVE_COUNT = 224;

    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Color of opposing player. */
    private final int         _opponent;
    /** Color of moving player. */
    private final int         _player;
    /** Square occupied by the moving player's King. */
    private final int         _iSqKing;
    /** Bitboard of all pieces. */
    private final long        _bbAll;
    /** Bitboard of opposing pieces. */
    private final long        _bbOpponent;
    /** Current position. */
    private final Board.State _state;
    /** Array of packed moves. */
    private final int[]  _moves = new int[ MAX_MOVE_COUNT ];
    /** Saved copy of the board maps. */
    private final long[] _map   = new long[ MAP_LENGTH * 2 ];

    /** Number of moves in {@link #_moves}. */
    private int  _iCount;
    /** Bitboard of pieces threatening the moving player's King. */
    private long _bbCheckers;
    /** Bitboard of all pinned pieces. */
    private long _bbPinned;
    /** Bitboard of potential "To" squares. */
    private long _bbToSq;

    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    /**
     * Default CTOR.
     *
     * @param bd
     *     Board to generate moves for.
     *
     * @throws java.lang.IllegalArgumentException
     *     if board is <code>null</code>.
     */
    MoveList( Board bd )
        {
        DBC.requireNotNull( bd, "bd" );
        /*
        **  CODE
        */
        _state = bd.getState();

        _player = _state.player;
        _opponent = _player ^ 1;

        _bbAll = _state.map[ MAP_W_ALL ] | _state.map[ MAP_B_ALL ];
        _bbOpponent = _state.map[ _opponent ];

        _iSqKing = (_player == WHITE) ? _state.iSqKingW : _state.iSqKingB;
        _bbToSq = ~_state.map[ _player ];

        if (Square.isValid( _iSqKing ))
            generateAllMoves( initBitboards( _state.map[ _player ] ) );
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Gets the empty / not empty state of the move list.
     *
     * @return <code>.T.</code> if no legal moves available; <code>.F.</code> otherwise.
     */
    public boolean hasLegalMove()
        { return (_iCount > 0); }

    /**
     * Gets the number of legal moves found.
     *
     * @return Move count.
     */
    public int size()
        { return _iCount; }
    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    /**
     * Adds a move to the move list, testing it for legality if necessary.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     * @param iSqTo
     *     "To" square in 8x8 format.
     * @param iType
     *     Type of move (Type.*)
     */
    private void addMove( int iSqFrom, int iSqTo, int iType )
        {
        boolean bLegal;
        //
        //  If the King is moving, discard the "To" square if it is attacked by the opponent.
        //  Test all others in case the King is moving into his own "shadow", i.e., into a
        //  square that he currently blocks.
        //
        //  If this is an en passant capture, test to make sure the King isn't left exposed to
        //  check.  This eliminates the rare situation where the pawn being captured is pinned
        //  against the moving player's King, but the "From" and "To" squares are not seen by
        //  the King.
        //
        if (iType == Move.Type.EN_PASSANT || iSqFrom == _iSqKing)
            bLegal = testMove( iSqFrom, iSqTo, iType );
        //
        //  If the King is NOT in check, and the moving piece is not pinned, then the move is
        //  considered safe.  Otherwise, the move has to be tested.
        //
        else if (_bbCheckers == 0L)
            bLegal = (_bbPinned & (1L << iSqFrom)) == 0L || testMove( iSqFrom, iSqTo, iType );
        //
        //  The King is in check, so the move must either capture an attacker, or end on an
        //  interposing square.  If it does not, it can be discarded.
        //
        else
            bLegal = (_bbToSq & (1L << iSqTo)) != 0L && testMove( iSqFrom, iSqTo, iType );
        //
        //  If the move is legal, add it to the list, expanding promotions if necessary.
        //
        if (bLegal)
            {
            if (iType != Move.Type.PROMOTION)
                _moves[ _iCount++ ] = Move.pack( iSqFrom, iSqTo, iType );
            else
                {
                int iPacked = Move.pack( iSqFrom, iSqTo );

                _moves[ _iCount++ ] = iPacked | Move.Type.PROMOTION;
                _moves[ _iCount++ ] = iPacked | Move.Type.PROMOTE_KNIGHT;
                _moves[ _iCount++ ] = iPacked | Move.Type.PROMOTE_BISHOP;
                _moves[ _iCount++ ] = iPacked | Move.Type.PROMOTE_ROOK;
                }
            }
        }

    /**
     * Adds a set of moves to the move list, testing it for legality if necessary.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     * @param bbTo
     *     Bitboard of potential "To" squares.
     * @param iType
     *     Type of move (Type.*)
     */
    private void addMoves( int iSqFrom, long bbTo, int iType )
        {
        int iSqTo;

        bbTo &= _bbToSq;

        while ( bbTo != 0L )
            {
            iSqTo = BitUtil.first( bbTo );
            bbTo ^= 1L << iSqTo;
            addMove( iSqFrom, iSqTo, iType );
            }
        }

    /**
     * Generate all legal moves for a set of pieces.
     *
     * @param bbPieces
     *     Bitboard of pieces to generate moves for.
     */
    private void generateAllMoves( long bbPieces )
        {
        final long bbEP = Square.isValid( _state.iSqEP )
                          ? (1L << _state.iSqEP)
                          : 0L;

        while ( bbPieces != 0L )
            {
            int iSq = BitUtil.first( bbPieces );

            bbPieces ^= 1L << iSq;

            switch (_state.sq[ iSq ])
                {
                case W_PAWN:
                    generatePawnMovesWhite( iSq );
                    if ((bbEP & Bitboards.pawnUpwards[ iSq ]) != 0L)
                        addMove( iSq, _state.iSqEP, Move.Type.EN_PASSANT );
                    break;

                case B_PAWN:
                    generatePawnMovesBlack( iSq );
                    if ((bbEP & Bitboards.pawnDownwards[ iSq ]) != 0L)
                        addMove( iSq, _state.iSqEP, Move.Type.EN_PASSANT );
                    break;

                case W_KNIGHT:
                case B_KNIGHT:
                    addMoves( iSq,
                              Bitboards.knight[ iSq ],
                              Move.Type.NORMAL );
                    break;
                case W_BISHOP:
                case B_BISHOP:
                    addMoves( iSq,
                              Bitboards.getBishopAttacks( iSq, _bbAll ),
                              Move.Type.NORMAL );
                    break;

                case W_ROOK:
                case B_ROOK:
                    addMoves( iSq,
                              Bitboards.getRookAttacks( iSq, _bbAll ),
                              Move.Type.NORMAL );
                    break;

                case W_QUEEN:
                case B_QUEEN:
                    addMoves( iSq,
                              Bitboards.getQueenAttacks( iSq, _bbAll ),
                              Move.Type.NORMAL );
                    break;

                case W_KING:
                    generateKingMovesWhite( iSq );
                    break;

                case B_KING:
                    generateKingMovesBlack( iSq );
                    break;

                default:
                    throw new RuntimeException( "Invalid piece type." );
                }
            }
        }

    /**
     * Generate all moves for a Black King on a given square.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     */
    private void generateKingMovesBlack( int iSqFrom )
        {
        assert _state.sq[ iSqFrom ] == Piece.B_KING;
        /*
        **  CODE
        */
        addMoves( iSqFrom,
                  Bitboards.king[ iSqFrom ],
                  Move.Type.NORMAL );

        if (_bbCheckers == 0 && iSqFrom == Square.E8)
            {
            if ((_state.castling & Board.CastlingFlags.BLACK_SHORT) != 0 &&
                (_bbAll & Board.CastlingFlags.BLACK_SHORT_MASK) == 0L &&
                !Bitboards.isAttackedByWhite( _state.map, Square.F8 ))
                {
                // Black  O-O
                addMove( Square.E8, Square.G8, Move.Type.CASTLING );
                }

            if ((_state.castling & Board.CastlingFlags.BLACK_LONG) != 0 &&
                (_bbAll & Board.CastlingFlags.BLACK_LONG_MASK) == 0L &&
                !Bitboards.isAttackedByWhite( _state.map, Square.D8 ))
                {
                // Black O-O-O
                addMove( Square.E8, Square.C8, Move.Type.CASTLING );
                }
            }
        }


    /**
     * Generate all moves for a White King on a given square.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     */
    private void generateKingMovesWhite( int iSqFrom )
        {
        assert _state.sq[ iSqFrom ] == Piece.W_KING;
        /*
        **  CODE
        */
        addMoves( iSqFrom,
                  Bitboards.king[ iSqFrom ],
                  Move.Type.NORMAL );

        if (_bbCheckers == 0 && iSqFrom == Square.E1)
            {
            if ((_state.castling & Board.CastlingFlags.WHITE_SHORT) != 0 &&
                (_bbAll & Board.CastlingFlags.WHITE_SHORT_MASK) == 0L &&
                (_bbToSq & Square.G1_MASK) != 0L &&
                !Bitboards.isAttackedByBlack( _state.map, Square.F1 ))
                {
                // White O-O
                addMove( Square.E1, Square.G1, Move.Type.CASTLING );
                }

            if ((_state.castling & Board.CastlingFlags.WHITE_LONG) != 0 &&
                (_bbAll & Board.CastlingFlags.WHITE_LONG_MASK) == 0L &&
                (_bbToSq & Square.C1_MASK) != 0L &&
                !Bitboards.isAttackedByBlack( _state.map, Square.D1 ))
                {
                // White O-O-O
                addMove( Square.E1, Square.C1, Move.Type.CASTLING );
                }
            }
        }

    /**
     * Generate all moves for a Black pawn on a given square.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     */
    private void generatePawnMovesBlack( int iSqFrom )
        {
        assert _state.sq[ iSqFrom ] == Piece.B_PAWN;
        /*
        **  CODE
        */
        int iType = (iSqFrom > Square.H2)
                    ? Move.Type.NORMAL
                    : Move.Type.PROMOTION;
        //
        //  Pawn captures (not including e.p. captures)
        //
        addMoves( iSqFrom,
                  (_bbOpponent & Bitboards.pawnDownwards[ iSqFrom ]),
                  iType );
        //
        //  Check for normal moves
        //
        int iSqTo = iSqFrom - 8;

        if (_state.sq[ iSqTo ] == null)
            {
            addMove( iSqFrom, iSqTo, iType );
            // If moving from 7th rank, check for pawn advance.
            if (iSqFrom >= Square.A7 && _state.sq[ (iSqTo -= 8) ] == null)
                addMove( iSqFrom, iSqTo, Move.Type.PAWN_PUSH );
            }
        }


    /**
     * Generate all moves for a White pawn on a given square.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     */
    private void generatePawnMovesWhite( final int iSqFrom )
        {
        assert _state.sq[ iSqFrom ] == Piece.W_PAWN;
        /*
        **  CODE
        */
        int iType = (iSqFrom < Square.A7)
                    ? Move.Type.NORMAL
                    : Move.Type.PROMOTION;
        //
        //  Pawn captures (not including e.p. captures)
        //
        addMoves( iSqFrom,
                  (_bbOpponent & Bitboards.pawnUpwards[ iSqFrom ]),
                  iType );
        //
        //  Check for normal moves
        //
        int iSqTo = iSqFrom + 8;

        if (_state.sq[ iSqTo ] == null)
            {
            addMove( iSqFrom, iSqTo, iType );
            // If moving from 2nd rank, check for pawn advance.
            if (iSqFrom <= Square.H2 && _state.sq[ (iSqTo += 8) ] == null)
                addMove( iSqFrom, iSqTo, Move.Type.PAWN_PUSH );
            }
        }

    /**
     * Initializes the internal bitboards.
     *
     * @param bbFromSq
     *     Bitboard of candidate pieces.
     *
     * @return Bitboard of candidate pieces.
     */
    private long initBitboards( long bbFromSq )
        {
        long bbPieces;

        _bbCheckers = Bitboards.getAttackedBy( _state.map, _iSqKing, _opponent );
        _bbPinned = 0L;
        //
        //  The moving player is NOT in check.
        //
        if (_bbCheckers == 0L)
            {
            //
            //  Find pinned pieces.  This is done by finding all of the opposing pieces that
            //  could attack the King if the moving player's pieces were removed.  If there is
            //  one (and only one) moving piece that lies on the path between a threatening
            //  piece and the King, then it is pinned.
            //
            long bbPinners = 0L;
            long bbPlayer = _state.map[ MAP_W_ALL + _player ];

            bbPieces = ~Bitboards.king[ _iSqKing ] &
                       (_state.map[ MAP_W_BISHOP + _opponent ] |
                        _state.map[ MAP_W_QUEEN + _opponent ]);

            if ((bbPieces & Bitboards.bishop[ _iSqKing ]) != 0L)
                bbPinners |= bbPieces & Bitboards.getBishopAttacks( _iSqKing, _bbOpponent );

            bbPieces = ~Bitboards.king[ _iSqKing ] &
                       (_state.map[ MAP_W_ROOK + _opponent ] |
                        _state.map[ MAP_W_QUEEN + _opponent ]);

            if ((bbPieces & Bitboards.rook[ _iSqKing ]) != 0L)
                bbPinners |= bbPieces & Bitboards.getRookAttacks( _iSqKing, _bbOpponent );

            while ( bbPinners != 0L )
                {
                int iSqPinner = BitUtil.first( bbPinners );
                long bbBetween = bbPlayer & Bitboards.getSquaresBetween( _iSqKing, iSqPinner );

                bbPinners ^= 1L << iSqPinner;

                if (BitUtil.singleton( bbBetween ))
                    _bbPinned |= bbBetween;
                }
            }
        //
        //  The King is being threatened by a single attacker.  The possible evasions are
        //  (1) capture the attacker, (2) move the King, or (3) interpose another piece. If
        //  the attacker is a Knight or Pawn--or is adjacent to the King--then the bitboard
        //  of interposing pieces (_bbXRays) will be zero.
        //
        else if (BitUtil.singleton( _bbCheckers ))
            {
            int iSqChecker = BitUtil.first( _bbCheckers );
            long bbXRays = Bitboards.getQueenAttacks( iSqChecker, _bbAll ) &
                           Bitboards.getQueenAttacks( _iSqKing, _bbAll );

            //
            //  If the King is being checked by a Knight or Pawn--or the attacker is adjacent
            //  to the King--so the only possible "From" squares are the King's square, plus
            //  any of the moving player's pieces that are able to reach the checking piece.
            //
            if (bbXRays == 0L)
                bbFromSq &= ((1L << _iSqKing) | Bitboards.all[ iSqChecker ]);

            _bbToSq &= (_bbCheckers | bbXRays | Bitboards.king[ _iSqKing ]);
            }
        //
        //  The King is being threatened by multiple attackers (double check).  The only
        //  possible evasion is to move the King.
        //
        else
            {
            _bbToSq &= Bitboards.king[ _iSqKing ];
            bbFromSq &= (1L << _iSqKing);
            }

        return bbFromSq;
        }


    /**
     * Tests a move to see if the King is left exposed to check.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     * @param iSqTo
     *     "To" square in 8x8 format.
     * @param iMoveType
     *     Move type (Move.Type.*)
     *
     * @return <code>.T.</code> if move is legal; <code>.F.</code> if king left in check.
     */
    @SuppressWarnings( "PointlessBitwiseExpression" )
    private boolean testMove( int iSqFrom, int iSqTo, int iMoveType )
        {
        assert Square.isValid( iSqFrom );
        assert Square.isValid( iSqTo );
        /*
        **  CODE
        */
        final long bbSqFrom = 1L << iSqFrom;
        final long bbSqTo = 1L << iSqTo;
        Piece piece = _state.sq[ iSqFrom ];

        System.arraycopy( _state.map, 0, _map, 0, MAP_LENGTH );

        _map[ _player ] ^= bbSqFrom | bbSqTo;
        _map[ piece.index ] ^= bbSqFrom | bbSqTo;

        if (iMoveType == Move.Type.CASTLING)
            {
            if (_player == WHITE)
                {
                long bbMask = (iSqFrom < iSqTo)
                              ? (Square.H1_MASK | Square.F1_MASK)
                              : (Square.A1_MASK | Square.D1_MASK);


                _map[ MAP_W_ALL ] ^= bbMask;
                _map[ MAP_W_ROOK ] ^= bbMask;
                }
            else
                {
                long bbMask = (iSqFrom < iSqTo)
                              ? (Square.H8_MASK | Square.F8_MASK)
                              : (Square.A8_MASK | Square.D8_MASK);

                _map[ MAP_B_ALL ] ^= bbMask;
                _map[ MAP_B_ROOK ] ^= bbMask;
                }
            }
        else if (iMoveType == Move.Type.EN_PASSANT)
            {
            long bbMask = 1L << ((iSqFrom & 0x38) | (iSqTo & 0x07));

            _map[ _opponent ] ^= bbMask;
            _map[ MAP_W_PAWN + _opponent ] ^= bbMask;
            }
        else if ((piece = _state.sq[ iSqTo ]) != null)
            {
            _map[ _opponent ] ^= bbSqTo;
            _map[ piece.index ] ^= bbSqTo;
            }

        return !Bitboards.isAttackedBy( _map,
                                        ((iSqFrom == _iSqKing) ? iSqTo : _iSqKing),
                                        _opponent );
        }

//  -----------------------------------------------------------------------
//	INTERFACE: Iterable<Move>
//	-----------------------------------------------------------------------

    @Override public Iterator<Move> iterator()
        { return new MoveListIterator(); }

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
            { return _iNext < _iCount; }

        /**
         * Returns the next move in the move list.
         *
         * @return Next move, or <code>null</code> if no more moves available.
         */
        @Override
        public Move next()
            {
            if (_iNext >= _iCount)
                throw new java.util.NoSuchElementException();
            /*
            **  CODE
            */
            return new Move( _moves[ _iNext++ ], _state );
            }

        @Override
        public void remove()
            { throw new sun.reflect.generics.reflectiveObjects.NotImplementedException(); }
        }   /* end of nested class MoveListIterator */

    }   /* end of class MoveList */
