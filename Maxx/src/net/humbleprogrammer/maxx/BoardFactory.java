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
import net.humbleprogrammer.humble.StrUtil;

import static net.humbleprogrammer.maxx.Constants.*;

public class BoardFactory extends Parser
    {

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Initial board. */
    static final         Board   s_bdInitial     = fromString( FEN_INITIAL );
    /** Used for creating mirrored positions. */
    private static final Piece[] s_pieceMirrored = new Piece[ Piece.values().length ];

    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    static
        {
        Piece[] pieces = Piece.values();

        for ( int idx = 0; idx < s_pieceMirrored.length; idx += 2 )
            {
            s_pieceMirrored[ idx ] = pieces[ idx + 1 ];
            s_pieceMirrored[ idx + 1 ] = pieces[ idx ];
            }
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Creates a new board with no pieces on it.
     *
     * @return Board object.
     */
    public static Board createBlank()
        { return new Board(); }

    /**
     * Creates a copy of an existing board.
     *
     * @param src
     *     Board to copy from.
     *
     * @return Board object.
     */
    public static Board createCopy( final Board src )
        {
        return (src != null)
               ? new Board( src )
               : null;
        }

    /**
     * Creates a board from a FEN string.
     *
     * @param strFEN
     *     FEN string.
     *
     * @return Board object, or <code>null</code> if FEN is invalid.
     */
    public static Board createFromFEN( String strFEN )
        {
        if (StrUtil.isBlank( strFEN ))
            return null;
        /*
        **  CODE
        */
        return strFEN.equals( FEN_INITIAL )
               ? new Board( s_bdInitial )
               : fromString( strFEN );

        }

    /**
     * Creates a new board set to the starting position.
     *
     * @return Board object.
     */
    public static Board createInitial()
        { return new Board( s_bdInitial ); }

    /**
     * Creates a mirror of an existing board.
     *
     * @param src
     *     Board to mirror.
     *
     * @return Mirror copy.
     */
    public static Board createMirror( Board src )
        {
        DBC.requireNotNull( src, "Source board" );
        /*
        **  CODE
        */
        Board bd = new Board();
        Piece piece;

        // Invert the board.
        for ( int iSq = 0; iSq < 64; ++iSq )
            if ((piece = src.get( iSq )) != null)
                bd.placePiece( Square.toMirror( iSq ), s_pieceMirrored[ piece.ordinal() ] );

        // Invert the castling flags.
        int castlingSrc = src.getCastlingFlags();

        if (castlingSrc == Board.CastlingFlags.ALL)
            bd.setCastlingFlags( Board.CastlingFlags.ALL );
        else if (castlingSrc != Board.CastlingFlags.NONE)
            {
            int castling = ((castlingSrc & Board.CastlingFlags.WHITE_BOTH) << 2) |
                           ((castlingSrc & Board.CastlingFlags.BLACK_BOTH) >>> 2);

            bd.setCastlingFlags( castling );
            }

        // Invert the player
        bd.setMovingPlayer( src.getMovingPlayer() ^ 1 );

        // Invert the e.p. square (player color must already be set).
        int iSqEP = src.getEnPassantSquare();
        if (Square.isValid( iSqEP ))
            bd.setEnPassantSquare( Square.toMirror( iSqEP ) );

        return bd;
        }

    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    /**
     * Creates a board from a FEN string.
     *
     * @param strFEN
     *     FEN string.
     *
     * @return Populated board, or null if FEN string is not valid.
     */
    private static Board fromString( String strFEN )
        {
        assert strFEN != null;
        /*
        **  CODE
        */
        String[] strFields = strFEN.split( "\\s+", 8 ); // limit the number of fields returned

        if (strFields.length > 3)
            {
            Board bd = new Board();

            if (parsePosition( bd, strFields[ 0 ] ) &&
                parseMovingPlayer( bd, strFields[ 1 ] ) &&
                parseCastlingFlags( bd, strFields[ 2 ] ) &&
                parseEnPassantSquare( bd, strFields[ 3 ] ) &&
                parseClocks( bd,
                             ((strFields.length > 4) ? strFields[ 4 ] : null),
                             ((strFields.length > 5) ? strFields[ 5 ] : null) ))
                {
                return bd;
                }
            }

        return null;
        }

    /**
     * Parses the castling flags from a FEN string.
     *
     * @param bd
     *     Board to populate.
     * @param strFlags
     *     String to parse.
     *
     * @return .T. on success; .F. on failure.
     */
    private static boolean parseCastlingFlags( Board bd, final String strFlags )
        {
        assert bd != null;

        if (StrUtil.isBlank( strFlags ))
            return false;
        /*
        **  CODE
        */
        int iFlags = Board.CastlingFlags.NONE;

        if (!strFlags.equals( STR_DASH ))
            {
            int iFlag;
            Piece piece;

            for ( int idx = 0; idx < strFlags.length(); ++idx )
                {
                int ch = strFlags.codePointAt( idx );

                if ((piece = pieceFromGlyph( ch )) == null)
                    return false;

                if (Character.isSupplementaryCodePoint( ch ))
                    idx++;

                switch (piece)
                    {
                    case W_KING:
                        iFlag = Board.CastlingFlags.WHITE_SHORT;
                        break;
                    case W_QUEEN:
                        iFlag = Board.CastlingFlags.WHITE_LONG;
                        break;
                    case B_KING:
                        iFlag = Board.CastlingFlags.BLACK_SHORT;
                        break;
                    case B_QUEEN:
                        iFlag = Board.CastlingFlags.BLACK_LONG;
                        break;

                    default:
                        return false;
                    }

                if ((iFlags & iFlag) != 0)
                    return false;

                iFlags |= iFlag;
                }
            }

        bd.setCastlingFlags( iFlags );

        return true;
        }

    /**
     * Parses the en passant square from a FEN string.
     *
     * @param bd
     *     Board to populate.
     * @param strEP
     *     String to parse.
     *
     * @return .T. on success; .F. on failure.
     */
    private static boolean parseEnPassantSquare( Board bd, final String strEP )
        {
        assert bd != null;

        if (StrUtil.isBlank( strEP ) || strEP.length() > 2)
            return false;
        /*
        **  CODE
        */
        int iSqEP;

        if (strEP.equals( STR_DASH ))
            iSqEP = INVALID;
        else
            {
            iSqEP = Square.fromString( strEP );

            if (!Square.isValid( iSqEP ) ||
                Square.getRank( iSqEP ) != ((bd.getMovingPlayer() == WHITE) ? 5 : 2))
                {
                return false;
                }
            }

        bd.setEnPassantSquare( iSqEP );

        return true;
        }

    /**
     * Parses the half move clock from a FEN string.
     *
     * @param bd
     *     Board to populate.
     * @param strMoves
     *     String to parse.
     *
     * @return .T. on success; .F. on failure.
     */
    private static boolean parseClocks( Board bd, String strHalf, String strMoves )
        {
        assert bd != null;
        /*
        **  CODE
        */
        int iHalf;
        int iMove;

        try
            {
            iHalf = (strHalf != null)
                    ? Integer.parseInt( strHalf )
                    : 0;
            iMove = (strMoves != null)
                    ? Integer.parseInt( strMoves )
                    : 1;

            if (iHalf < 0 || iMove < 1)
                return false;
            }
        catch (NumberFormatException ex)
            { return false; }

        bd.setHalfMoveClock( iHalf );
        bd.setMoveNumber( iMove );

        return true;
        }

    /**
     * Parses the moving player from a FEN string.
     *
     * @param bd
     *     Board to populate.
     * @param strPlayer
     *     String to parse.
     *
     * @return .T. on success; .F. on failure.
     */
    private static boolean parseMovingPlayer( Board bd, String strPlayer )
        {
        assert bd != null;
        /*
        **  CODE
        */
        if (StrUtil.isBlank( strPlayer ) || strPlayer.length() != 1)
            return false;

        int player = playerFromGlyph( strPlayer.codePointAt( 0 ) );
        if (!(player == WHITE || player == BLACK))
            return false;

        bd.setMovingPlayer( player );
        return true;
        }

    /**
     * Parses the position from a FEN string.
     *
     * @param bd
     *     Board to populate.
     * @param strPosition
     *     String to parse.
     *
     * @return .T. on success; .F. on failure.
     */
    private static boolean parsePosition( Board bd, final String strPosition )
        {
        assert bd != null;

        if (strPosition == null || strPosition.isEmpty())
            return false;
        /*
        **  CODE
        */
        int iRank = 7;  // EPD/FEN positions start at the back rank
        int iFile = 0;
        Piece piece;

        for ( int idx = 0; idx < strPosition.length(); ++idx )
            {
            int ch = strPosition.codePointAt( idx );

            if (Character.isSupplementaryCodePoint( ch ))
                idx++;

            if (ch >= '1' && ch <= '8')
                iFile += ch - '0';
            else if (ch == '/')
                {
                if (iFile != 8)
                    return false;

                iFile = 0;
                iRank--;
                }
            else if ((piece = pieceFromGlyph( ch )) != null)
                {
                int iSq = Square.toIndex( iRank, iFile++ );

                if (!bd.set( iSq, piece ))
                    return false;
                }
            else
                return false;
            }

        return (iRank == 0 && iFile == 8);
        }

    /**
     * Converts a board to it's string equivalent.
     *
     * @param bd
     *     Board to convert.
     *
     * @return FEN string.
     */
    static String toString( Board bd )
        {
        DBC.requireNotNull( bd, "Board" );
        /*
        **  CODE
        */
        StringBuilder sb = new StringBuilder();

        for ( int iRank = 7; iRank >= 0; --iRank )
            {
            int iSkip = 0;

            for ( int iFile = 0; iFile < 8; ++iFile )
                {
                Piece piece = bd.get( Square.toIndex( iRank, iFile ) );

                if (piece == null)
                    iSkip++;
                else
                    {
                    if (iSkip > 0)
                        {
                        sb.append( iSkip );
                        iSkip = 0;
                        }

                    sb.append( pieceToGlyph( piece ) );
                    }
                }

            if (iSkip > 0)
                sb.append( iSkip );

            if (iRank > 0)
                sb.append( '/' );
            }
        //
        //	Export the player on the move.  If the board has been cleared,
        //	then the MovingPlayer will be PieceColor.None, which is changed
        //	to White.
        //
        sb.append( ' ' );
        sb.append( playerToGlyph( bd.getMovingPlayer() ) );
        //
        //	Export the castling flags
        //
        int castling = bd.getCastlingFlags();

        sb.append( ' ' );
        if (castling == Board.CastlingFlags.NONE)
            sb.append( STR_DASH );
        else
            {
            if ((castling & Board.CastlingFlags.WHITE_SHORT) != 0)
                sb.append( pieceToGlyph( Piece.W_KING ) );
            if ((castling & Board.CastlingFlags.WHITE_LONG) != 0)
                sb.append( pieceToGlyph( Piece.W_QUEEN ) );
            if ((castling & Board.CastlingFlags.BLACK_SHORT) != 0)
                sb.append( pieceToGlyph( Piece.B_KING ) );
            if ((castling & Board.CastlingFlags.BLACK_LONG) != 0)
                sb.append( pieceToGlyph( Piece.B_QUEEN ) );
            }
        //
        //	Export the e.p. square.
        //
        int iSqEP = bd.getEnPassantSquare();

        sb.append( ' ' );
        if (Square.isValid( iSqEP ))
            sb.append( Square.toString( iSqEP ) );
        else
            sb.append( STR_DASH );
        //
        //	Export the Half Move clock
        //
        sb.append( ' ' );
        sb.append( bd.getHalfMoveClock() );
        //
        //	Export the Full Move clock
        //
        sb.append( ' ' );
        sb.append( bd.getMoveNumber() );

        return sb.toString();
        }

    }   /* end of class BoardFactory */
