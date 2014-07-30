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
package net.humbleprogrammer.maxx.pgn;

import net.humbleprogrammer.maxx.Parser;

import java.util.*;
import java.util.regex.Pattern;

public class PgnParser extends Parser
    {

    //  -----------------------------------------------------------------------
    //	CONSTANTS
    //	-----------------------------------------------------------------------

    /** Regex to validate tag names. */
    private static final String STR_TAG_NAME = "^[A-Z]\\w{0,254}$";

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** The mandatory "Seven Tag Roster". */
    private static List<String> s_listTags =
        Arrays.asList( "Event", "Site", "Date", "Round", "White", "Black", "Result" );
    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Tests a tag name for validity.
     *
     * @param strName
     *     Name to test.
     *
     * @return <code>.T.</code> if valid; <code>.F.</code> otherwise.
     */
    public static boolean isValidTagName( final String strName )
        {
        return (strName != null && Pattern.matches( STR_TAG_NAME, strName ));
        }

    /**
     * Tests a tag value for validity.
     *
     * @param strValue
     *     Value to test.
     *
     * @return <code>.T.</code> if valid; <code>.F.</code> otherwise.
     */
    public static boolean isValidTagValue( final String strValue )
        {
        return (strValue != null && strValue.length() < 256);
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTERS
    //	-----------------------------------------------------------------------

    /**
     * Gets the mandatory tags.
     *
     * @return List of tags.
     */
    public static List<String> getMandatoryTags()
        { return Collections.unmodifiableList( s_listTags ); }
    }   /* end of class PgnParser() */
