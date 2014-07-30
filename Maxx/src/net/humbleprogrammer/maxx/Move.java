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

import net.humbleprogrammer.humble.DBC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.humbleprogrammer.maxx.Constants.*;

public class Move
    {

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Logger */
    private static final Logger s_log = LoggerFactory.getLogger( Move.class );

    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** "From" square iLength, in 8x8 format. */
    final int         iSqFrom;
    /** "To" square iLength, in 8x8 format. */
    final int         iSqTo;
    /** Move type */
    final int         iType;
    /** Board state prior to the move being made. */
    final Board.State state;
    /** Zobrist hash AFTER move was made. */
    long hashAfter = HASH_INVALID;

    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    /**
     * Default CTOR.
     *
     * @param iPacked
     *     Packed move.
     * @param state
     *     Board state prior to the move being made.
     */
    Move( final int iPacked, final Board.State state )
        {
        assert state != null;
        /*
        **  CODE
        */
        iSqFrom = (iPacked >>> 8) & 0x3F;
        iSqTo = (iPacked >>> 16) & 0x3F;
        iType = iPacked & Type.MASK;

        this.state = state;
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Converts a SAN string to a move.
     *
     * @param bd
     *     Current position.
     * @param strSAN
     *     SAN string.
     *
     * @return Move on success; null if move is illegal or invalid.
     */
    public static Move fromString( Board bd, String strSAN )
        {
        DBC.requireNotNull( bd, "Board" );

        if (strSAN == null || strSAN.isEmpty())
            return null;
        /*
        **  CODE
        */
        MoveInfo info = new MoveInfo();

        if (info.parse( strSAN, bd.getMovingPlayer() ) <= 0)
            return null;
        //
        //  Generate a list of moves that match the parsed details.
        //
        int iSqTo;

        if (info.iType == Type.CASTLING)
            {
            iSqTo = (bd.getMovingPlayer() == WHITE)
                    ? Square.A1 + info.iFileTo
                    : Square.A8 + info.iFileTo;
            }
        else
            iSqTo = Square.toIndex( info.iRankTo, info.iFileTo );
        //
        //  Build a map of candidate pieces
        //
        long bbCandidates = bd.getPieceMap( info.iPieceMoving );

        if (info.iPieceMoving == PAWN && !info.bCapture)
            bbCandidates &= Bitboards.fileMask[ info.iFileTo ];

        if ((info.iFileFrom & ~0x07) == 0)
            bbCandidates &= Bitboards.fileMask[ info.iFileFrom ];

        if ((info.iRankFrom & ~0x07) == 0)
            bbCandidates &= Bitboards.rankMask[ info.iRankFrom ];
        //
        //  Look for a matching move.
        //
        Move moveFound = null;
        MoveList moves = new MoveList( bd, bbCandidates, iSqTo );

        for ( Move move : moves )
            if (move.iType < Type.CASTLING || move.iType == info.iType)
                {
                if (moveFound != null)
                    {
                    s_log.debug( "'{}' => '{}' is ambiguous.",
                                 bd,
                                 strSAN );
                    }

                moveFound = move;
                }

        return moveFound;
        }
    //  -----------------------------------------------------------------------
    //	IMPLEMENTATION
    //	-----------------------------------------------------------------------

    /**
     * Packs the "From" square, "To" square, and move type into a 32-bit integer.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     * @param iSqTo
     *     "To" square in 8x8 format.
     *
     * @return packed move.
     */

    static int pack( int iSqFrom, int iSqTo )
        { return (iSqTo << 16) | (iSqFrom << 8); }

    /**
     * Packs the "From" square, "To" square, and move type into a 32-bit integer.
     *
     * @param iSqFrom
     *     "From" square in 8x8 format.
     * @param iSqTo
     *     "To" square in 8x8 format.
     * @param iMoveType
     *     Move type.
     *
     * @return packed move.
     */
    static int pack( int iSqFrom, int iSqTo, int iMoveType )
        { return (iSqTo << 16) | (iSqFrom << 8) | (iMoveType & Type.MASK); }

    //  -----------------------------------------------------------------------
    //	OVERRIDES
    //	-----------------------------------------------------------------------

    @Override
    public String toString()
        {
        String str = Square.toString( iSqFrom ) +
                     Square.toString( iSqTo );

        if (iType >= Type.PROMOTION)
            {
            if (iType == Type.PROMOTION)
                str += Character.toLowerCase( Parser.pieceTypeToGlyph( QUEEN ) );
            else if (iType == Type.PROMOTE_KNIGHT)
                str += Character.toLowerCase( Parser.pieceTypeToGlyph( KNIGHT ) );
            else if (iType == Type.PROMOTE_BISHOP)
                str += Character.toLowerCase( Parser.pieceTypeToGlyph( BISHOP ) );
            else
                str += Character.toLowerCase( Parser.pieceTypeToGlyph( ROOK ) );
            }

        return str;

        }

    //  -----------------------------------------------------------------------
    //	NESTED CLASS: Type
    //	-----------------------------------------------------------------------

    public static class Type
        {
        /** Normal move. */
        public static final int NORMAL         = 0;
        /** Initial pawn advance. */
        public static final int PAWN_PUSH      = 1;
        /** Castling move (O-O or O-O-O). */
        public static final int EN_PASSANT     = 2;
        /** En Passant capture. */
        public static final int CASTLING       = 3;
        /** Promote to Queen. */
        public static final int PROMOTION      = 4;
        /** Under-promote to a Rook. */
        public static final int PROMOTE_ROOK   = 5;
        /** Under-promote to a Bishop. */
        public static final int PROMOTE_BISHOP = 6;
        /** Under-promote to a Knight. */
        public static final int PROMOTE_KNIGHT = 7;

        /** Mask for viable bits. */
        static final int MASK = 0x07;
        }   /* end of nested class Type */

    //  -----------------------------------------------------------------------
    //	NESTED CLASS: ParserInfo
    //	-----------------------------------------------------------------------

    /**
     * The MoveInfo class captures all the information extracted from a SAN move string.
     *
     * This code is loosely based on the source code to v1.6.2 of StockFish.
     */
    private static class MoveInfo
        {
        private static final int PS_Start            = 0;
        private static final int PS_ToFile           = 1;
        private static final int PS_ToRank           = 2;
        private static final int PS_Promotion        = 3;
        private static final int PS_Check            = 4;
        private static final int PS_CheckOrPromotion = 5;
        private static final int PS_Annotation       = 6;
        private static final int PS_End              = 7;

        /** .T. if a capture; .F. otherwise. */
        boolean bCapture;
        /** "To" file */
        int     iFileTo;
        /** Optional "From" file */
        int     iFileFrom;
        /** Number of characters consumed, or zero if invalid */
        int     iLength;
        /** Type of piece being moved */
        int     iPieceMoving;
        /** "To" rank */
        int     iRankTo;
        /** Optional "From" rank */
        int     iRankFrom;
        /** Move type */
        int     iType = Type.NORMAL;

        /**
         * Parses a SAN move.
         *
         * @param strIn
         *     String to parse.
         * @param player
         *     Moving player color.
         *
         * @return Number of characters parsed, or zero on error.
         */
        int parse( String strIn, int player )
            {
            assert strIn != null;
            assert (player == WHITE || player == BLACK);

            if (strIn.isEmpty() || !Character.isLetter( strIn.codePointAt( 0 ) ))
                return 0; // all moves must start with a letter.
            /*
            **  CODE
            */
            int iState = PS_Start;

            bCapture = false;
            iFileFrom = iFileTo = iRankFrom = iRankTo = INVALID;
            iPieceMoving = EMPTY;
            iType = Type.NORMAL;

            for ( iLength = 0; iLength < strIn.length(); ++iLength )
                {
                int ch = strIn.codePointAt( iLength );

                if (Character.isSupplementaryCodePoint( ch ))
                    iLength++;

                if (ch >= 'a' && ch <= 'h')
                    iState = parseFile( iState, (ch - 'a') );
                else if (ch >= '1' && ch <= '8')
                    iState = parseRank( iState, (ch - '1') );
                else if (iLength > 0 || Character.toUpperCase( ch ) != 'O')
                    iState = parseSpecial( iState, ch );
                else
                    {
                    iState = parseCastling( iState, strIn );
                    iRankFrom = iRankTo = (player & 0x01) * 7;
                    }

                //  See if we're all done yet.
                if (iState < PS_Start || iState == PS_End)
                    break;
                }
            //
            //  If we ran out of characters prematurely, the state will be set to something .LT.
            //  PS_Check, which is an error condition.  Otherwise, make sure we got the minimum
            //  amount of information.
            //
            if (iState < PS_Check || iPieceMoving == EMPTY)
                iLength = 0;
            else
                {
                assert iLength > 0;
                assert Square.isValidRankOrFile( iFileTo );
                assert Square.isValidRankOrFile( iRankTo );
                }

            return iLength;
            }

        /**
         * Parse a file indicator (a-h)
         *
         * @param iState
         *     Current state.
         * @param iFile
         *     File [0..7].
         */
        private int parseFile( int iState, int iFile )
            {
            assert (iFile & ~0x07) == 0;
            /*
            **  CODE
            */
            if (iState == PS_Start)
                {
                iPieceMoving = PAWN;
                iFileTo = iFile;
                return PS_ToRank;
                }

            if (iState == PS_ToFile)
                {
                iFileTo = iFile;
                return PS_ToRank;
                }

            if (iState == PS_ToRank && iFileFrom < 0)
                {
                iFileFrom = iFileTo;
                iFileTo = iFile;
                return PS_ToRank;
                }

            return INVALID;
            }

        /**
         * Parses a possible castling move.
         *
         * @param iState
         *     Current state.
         * @param strIn
         *     Input string.
         *
         * @return New state.
         */
        private int parseCastling( int iState, String strIn )
            {
            if (iState != PS_Start)
                return INVALID;

            if (strIn.regionMatches( true, iLength, "O-O-O", 0, 5 ))
                {
                iLength += 4;
                iFileTo = 2;
                }
            else if (strIn.regionMatches( true, iLength, "O-O", 0, 3 ))
                {
                iLength += 2;
                iFileTo = 6;
                }
            else
                return INVALID;

            iType = Type.CASTLING;
            iFileFrom = 4;
            iPieceMoving = KING;

            return PS_Check;
            }

        /**
         * Parse a rank indicator (1-8)
         *
         * @param iState
         *     Current state.
         * @param iRank
         *     Rank [0..7].
         */

        private int parseRank( int iState, int iRank )
            {
            assert (iRank & ~0x07) == 0;
            /*
            **  CODE
            */
            if (iState == PS_ToRank)
                {
                iRankTo = iRank;
                return PS_CheckOrPromotion;
                }

            if (iState == PS_ToFile && iRankFrom < 0)
                {
                iRankFrom = iRank;
                return PS_ToFile;
                }

            return INVALID;
            }

        /**
         * Parse other characters.
         *
         * @param iState
         *     Current state.
         * @param ch
         *     Character to parse.
         *
         * @return New state, or INVALID on error.
         */
        private int parseSpecial( int iState, int ch )
            {
            int iPiece;

            switch (ch)
                {
                case '=':
                case ':':
                    return (iState == PS_CheckOrPromotion && iPieceMoving == PAWN)
                           ? PS_Promotion
                           : INVALID;

                case '+':
                    return (iState >= PS_Check && iState <= PS_CheckOrPromotion)
                           ? PS_Check
                           : INVALID;

                case '#':
                    return (iState >= PS_Check && iState <= PS_Annotation)
                           ? PS_End
                           : INVALID;

                case '!':
                case '?':
                    return (iState >= PS_Check && iState <= PS_Annotation)
                           ? PS_Annotation
                           : INVALID;

                case 'x':
                case 'X':
                    if (bCapture)
                        return INVALID;

                    bCapture = true;

                    if (iState == PS_ToRank)
                        {
                        iFileFrom = iFileTo;
                        return PS_ToFile;
                        }

                    return (iState == PS_ToFile)
                           ? iState
                           : INVALID;

                default:
                    if ((iPiece = Parser.pieceTypeFromGlyph( ch )) > PAWN)
                        {
                        if (iState == PS_Start)
                            {
                            iPieceMoving = iPiece;
                            return PS_ToFile;
                            }

                        if (iState == PS_Promotion)
                            {
                            if (iPiece == QUEEN)
                                iType = Type.PROMOTION;
                            else if (iPiece == KNIGHT)
                                iType = Type.PROMOTE_KNIGHT;
                            else if (iPiece == BISHOP)
                                iType = Type.PROMOTE_BISHOP;
                            else if (iPiece == ROOK)
                                iType = Type.PROMOTE_ROOK;
                            else
                                return INVALID;

                            return PS_Check;
                            }
                        }
                    break;
                }

            return INVALID;
            }
        }
    }   /* end of class Move */
