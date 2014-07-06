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

import net.humbleprogrammer.maxx.pgn.PgnStream;

import java.util.*;

public class Game
    {
    //  -----------------------------------------------------------------------
    //	PUBLIC DECLARATIONS
    //	-----------------------------------------------------------------------

    public enum Verdict
        {
            INDETERMINATE,
            DRAW,
            WON_BY_BLACK,
            WON_BY_WHITE
        }

    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Current position. */
    private final Board _board;
    /** Initial position, or <code>null</code> for normal starting position. */
    private       Board.State         _stateInitial = null;
    /** Map of PGN tags. */
    private final Map<String, String> _tags         = new HashMap<String, String>();
    /** Verdict, or <code>null</code> if not set. */
    private Verdict _verdict;

    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    Game()
        {
        _board = BoardFactory.createInitial();
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Gets the current position.
     *
     * @return Board.
     */
    public Board getPosition()
        { return _board; }

    /**
     * Gets the verdict.
     *
     * @return Game verdict.
     */
    public Verdict getVerdict()
        { return _verdict; }

    /**
     * Gets a PGN tag.
     *
     * @param strName
     *     Tag key.
     *
     * @return Tag value, or <code>null</code> if not found.
     */
    public String getTag( String strName )
        {
        return (strName != null)
               ? _tags.get( strName )
               : null;
        }

    /**
     * Sets a PGN tag.
     *
     * @param strName
     *     Tag key.
     * @param strValue
     *     Tag value.
     */
    public void setTag( String strName, String strValue )
        {
        if (!PgnStream.isValidTagName( strName ))
            return;
        /*
        **  CODE
        */
        _tags.put( strName, strValue );

        if (strName.equalsIgnoreCase( "FEN" ))
            setStartingPosition( strValue );
        }

    /**
     * Gets all the tag names.
     *
     * @return Set of tag names.
     */
    public Set<String> getTagNames()
        { return Collections.unmodifiableSet( _tags.keySet() ); }

    /**
     * Sets the starting position for the game.
     *
     * @param strFEN
     *     Starting position expressed as a FEN string.
     *
     * @return <code>.T.</code> if successful; <code>.F.</code> otherwise.
     */
    public boolean setStartingPosition( String strFEN )
        {
        Board bd = BoardFactory.createFromFEN( strFEN );

        if (bd == null)
            return false;

        _stateInitial = bd.getState();
        _board.setState( _stateInitial );

        return true;
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC OVERRIDES
    //	-----------------------------------------------------------------------

    /**
     * Creates a Portable Game Notation (PGN) string for the game.
     *
     * @return PGN string.
     */
    @Override
    public String toString()
        { return GameFactory.toString( this ); }

    }   /* end of class Game */
