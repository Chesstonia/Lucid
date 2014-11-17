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
package net.humbleprogrammer.maxx.factories;

import net.humbleprogrammer.maxx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.humbleprogrammer.maxx.Constants.*;

public class MoveFactory
    {

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Logger */
    private static final Logger s_log = LoggerFactory.getLogger( MoveFactory.class );

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
     * @return Move on success; null if move is illegal, ambiguous, or invalid.
     */
    public static Move fromSAN( Board bd, String strSAN )
        {
        if (bd == null || strSAN == null || strSAN.isEmpty())
            return null;
        /*
        **  CODE
        */
        final MoveInfo info = new MoveInfo( strSAN, bd.getMovingPlayer() );

        if (info.iLength <= 0)
            return null;
        //
        //  Create a bitboard of moving pieces (candidates) and find all
        //  legal moves to the target square.
        //
        int iSqTo = Square.toIndex( info.iRankTo, info.iFileTo );
        long bbCandidates = bd.getCandidates( iSqTo, info.iPieceMoving );

        if (info.iPieceMoving == PAWN)
            {
            bbCandidates &= info.bCapture
                            ? ~Bitboards.getFileMask( info.iFileTo )
                            : Bitboards.getFileMask( info.iFileTo );
            }

        if (Square.isValidRankOrFile( info.iFileFrom ))
            bbCandidates &= Bitboards.getFileMask( info.iFileFrom );

        if (Square.isValidRankOrFile( info.iRankFrom ))
            bbCandidates &= Bitboards.getRankMask( info.iRankFrom );

        if (bbCandidates == 0L)
            return null;
        //
        //  Look for a matching move.  In a majority of cases (99.99% in over a million legal games)
        //  only a single move will be returned.  THe only exception is promotions, which have to
        //  be searched to find the matching piece type.
        //
        Move moveFound = null;
        MoveList moves = new MoveList( bd ).generate( bbCandidates, iSqTo );

        if (moves.size() == 1)
            moveFound = moves.getAt( 0 );
        else if (moves.size() > 1)
            {
            //
            //  Handle the edge case where a pawn in being promoted--but the piece
            //  isn't specified--by always promoting to a queen.
            //
            if (info.iPieceMoving == PAWN &&
                info.iType == Move.Type.NORMAL &&
                (info.iRankTo == 0 || info.iRankTo == 7))
                {
                info.iType = Move.Type.PROMOTION;
                s_log.debug( "'{}' => auto-promoting to a Queen.", strSAN );
                }

            for ( Move move : moves )
                if (move.iType < Move.Type.CASTLING || move.iType == info.iType)
                    {
                    if (moveFound == null)
                        moveFound = move;
                    else
                        {
                        s_log.debug( "'{}' => '{}' is ambiguous.",
                                     bd,
                                     strSAN );
                        return null;
                        }
                    }
            }

        return moveFound;
        }

//  -----------------------------------------------------------------------
//	NESTED CLASSES: MoveInfo
//	-----------------------------------------------------------------------

    /**
     * The MoveInfo class extracts information from a SAN move string.
     *
     * This code is loosely based on the source for v. 1.6.2 of StockFish by Tord Romstad, Marco
     * Costalba, Joona Kiiski, et al.
     */
    private static class MoveInfo
        {
        private static final int STATE_START      = 0;
        private static final int STATE_FILE       = 1;
        private static final int STATE_RANK       = 2;
        private static final int STATE_PROMOTION  = 3;
        private static final int STATE_CHECK      = 4;
        private static final int STATE_SUFFIX     = 5;
        private static final int STATE_ANNOTATION = 6;

        boolean bCapture;
        boolean bCheck;

        int iFileFrom = INVALID;
        int iFileTo   = INVALID;
        int iRankFrom = INVALID;
        int iRankTo   = INVALID;

        int iLength;
        int iPieceMoving = EMPTY;
        int iType        = Move.Type.NORMAL;

        /**
         * CTOR
         *
         * @param strIn
         *     SAN string.
         * @param player
         *     Moving player color [WHITE|BLACK]
         */
        MoveInfo( String strIn, int player )
            {
            assert strIn != null;
            assert (player & ~0x01) == 0;
            /*
            **  CODE
            */
            int ch = strIn.codePointAt( 0 );

            if (!Character.isLetter( ch ))
                return;
            //
            //  Try castling first, because it's simple to parse.
            //
            int index = 0;
            int iState = (ch == 'O' && (index = parseCastling( strIn, player )) > 0)
                         ? STATE_CHECK
                         : STATE_START;

            while ( index < strIn.length() )
                {
                ch = strIn.codePointAt( index++ );
                if (Character.isSupplementaryCodePoint( ch ))
                    index++;

                if (ch >= 'a' && ch <= 'h')
                    iState = parseFile( iState, (ch - 'a') );
                else if (ch >= '1' && ch <= '8')
                    iState = parseRank( iState, (ch - '1') );
                else
                    iState = parseSpecial( iState, ch );

                if (iState < STATE_START)
                    return;
                }
            //
            //  If we ran out of input prematurely, the state will be set to something .LT.
            //  STATE_CHECK, which is an error condition.
            //
            if (iState >= STATE_CHECK && iPieceMoving != EMPTY)
                iLength = index;
            }

        /**
         * Parses a castling move ("O-O" or "O-O-O")
         *
         * @param strIn
         *     Input string.
         * @param player
         *     Moving player color [BLACK|WHITE] return Length of parsed move, or zero on error.
         */
        private int parseCastling( String strIn, int player )
            {
            int iLen;

            if (strIn.startsWith( Parser.STR_CASTLE_LONG ))
                {
                iFileTo = 2;
                iLen = Parser.STR_CASTLE_LONG.length();
                }
            else if (strIn.startsWith( Parser.STR_CASTLE_SHORT ))
                {
                iFileTo = 6;
                iLen = Parser.STR_CASTLE_SHORT.length();
                }
            else
                return 0;

            iFileFrom = 4;
            iPieceMoving = KING;
            iRankFrom = iRankTo = (player == WHITE) ? 0 : 7;

            return iLen;
            }

        /**
         * Parse a file indicator (a-h)
         *
         * @param iState
         *     Current parse state.
         * @param iFile
         *     File [0..7]
         *
         * @return New state, or INVALID on error.
         */
        private int parseFile( int iState, int iFile )
            {
            if (iState == STATE_START)
                iPieceMoving = PAWN;
            else if (iState == STATE_RANK && iFileFrom < 0)
                iFileFrom = iFileTo;
            else if (iState != STATE_FILE)
                return INVALID;

            iFileTo = iFile;
            return STATE_RANK;
            }

        /**
         * Parse a rank indicator (1-8)
         *
         * @param iState
         *     Current parse state.
         * @param iRank
         *     Rank [0..7]
         *
         * @return New state, or INVALID on error.
         */
        private int parseRank( int iState, int iRank )
            {
            if (iState == STATE_RANK)
                {
                iRankTo = iRank;
                return STATE_SUFFIX;
                }

            if (iState == STATE_FILE && iRankFrom < 0)
                {
                iRankFrom = iRank;
                return STATE_FILE;
                }

            return INVALID;
            }

        /**
         * Parse all other characters.
         *
         * @param iState
         *     Current parse state.
         * @param ch
         *     Character to parse.
         *
         * @return New state, or INVALID on error.
         */
        private int parseSpecial( int iState, int ch )
            {
            switch (ch)
                {
                case '=':
                case ':':
                    return (iState == STATE_SUFFIX && iPieceMoving == PAWN)
                           ? STATE_PROMOTION
                           : INVALID;

                case '+':
                    if (bCheck)
                        return INVALID;

                    bCheck = true;
                    return (iState == STATE_CHECK || iState == STATE_SUFFIX)
                           ? STATE_CHECK
                           : INVALID;

                case '#':
                    bCheck = true;
                    return (iState >= STATE_CHECK)
                           ? STATE_ANNOTATION
                           : INVALID;

                case '!':
                case '?':
                    return (iState >= STATE_CHECK)
                           ? STATE_ANNOTATION
                           : INVALID;

                case 'x':
                case 'X':
                    if (bCapture)
                        return INVALID;

                    bCapture = true;

                    if (iState == STATE_RANK)
                        {
                        iFileFrom = iFileTo;
                        return STATE_FILE;
                        }

                    return (iState == STATE_FILE)
                           ? STATE_FILE
                           : INVALID;
                }
            //
            //  Handle everything else, which should be a piece type: either the
            //  moving piece, or the piece being promoted to.
            //
            int pt = Parser.pieceTypeFromGlyph( ch );

            if (pt <= PAWN || pt > KING)
                return INVALID;

            if (iState == STATE_START)
                {
                iPieceMoving = pt;
                return STATE_FILE;
                }
            //
            //  If we got a piece type -- but didn't see the promotion separator -- make sure
            //  this is really a promotion move.
            //
            if (iState == STATE_PROMOTION ||
                (iState == STATE_SUFFIX && iPieceMoving == PAWN &&
                 (iRankTo == 0 || iRankTo == 7)))
                {
                if (pt == QUEEN)
                    iType = Move.Type.PROMOTION;
                else if (pt == KNIGHT)
                    iType = Move.Type.PROMOTE_KNIGHT;
                else if (pt == BISHOP)
                    iType = Move.Type.PROMOTE_BISHOP;
                else if (pt == ROOK)
                    iType = Move.Type.PROMOTE_ROOK;
                else
                    return INVALID;

                return STATE_CHECK;
                }

            return INVALID;
            }
        }   /* end of nested class MoveInfo */

    }   /* end of class MoveFactory */
