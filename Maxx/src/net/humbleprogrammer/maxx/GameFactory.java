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
import net.humbleprogrammer.maxx.pgn.*;

public class GameFactory extends Parser
    {

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** The mandatory "Seven Tag Roster". */
    private static final String[] s_strTags = new String[]
        {
            "Event", "Site", "Date", "Round", "White", "Black", "Result"
        };

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Creates a new game.
     *
     * @return Game object.
     */
    public static Game createBlank()
        { return new Game(); }

    /**
     * Creates a game from a PGN string.
     *
     * @param strPGN
     *     FEN string.
     *
     * @return Game object, or <code>null</code> if PGN is invalid.
     */
    public static Game createFromPGN( String strPGN )
        {
        return fromString( strPGN );
        }

    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    /**
     * Converts a PGN string to a Game.
     *
     * <PGN-game> ::= <tag-section> <movetext-section>
     *
     * @param strPGN
     *     String to parse.
     *
     * @return {@link Game} object if parsed, <c>null</c> otherwise.
     */
    private static Game fromString( final String strPGN )
        {
        if (StrUtil.isBlank( strPGN ))
            return null;
        /*
        **  CODE
        */
        s_strError = null;

        try
            {
            PgnStream stream = new PgnStream(strPGN);

            while (stream.nextToken() != null)
                { /* EMPTY BLOCK */ }
            }
        catch (PgnException ex)
            {
            s_strError = ex.getMessage();
            s_log.debug( s_strError, ex );
            }

        return null;
        }

    /**
     * Exports a game as a PGN string.
     *
     * @param gm
     *     Game to export.
     *
     * @return PGN string.
     */
    public static String toString( Game gm )
        {
        DBC.requireNotNull( gm, "Game" );
        /*
        **  CODE
        */
        String strValue;
        StringBuilder sb = new StringBuilder();

        for ( String strName : s_strTags )
            {
            if ((strValue = gm.getTag( strName )) == null)
                strValue = "";

            sb.append( String.format( "[%s \"%s\"]" + STR_CRLF,
                                      strName,
                                      strValue ) );
            }
        //
        //  Export optional tags.
        //
        for ( String strName : gm.getTagNames() )
            if (!StrUtil.contains( s_strTags, strName ) &&
                (strValue = gm.getTag( strName )) != null)
                {
                sb.append( String.format( "[%s \"%s\"]" + STR_CRLF,
                                          strName,
                                          strValue ) );
                }

        sb.append( STR_CRLF );  // empty line after tag section

        return sb.toString();
        }

    }   /* end of class GameFactory */
