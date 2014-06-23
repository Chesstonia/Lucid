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
package net.humbleprogrammer.humble;

/**
 * The {@link StrUtil} class implements string-related utiltiy methods.
 */
public class StrUtil
    {

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Tests a string to see if it is blank, empty, or null.
     *
     * @param str
     *     String to test.
     *
     * @return <c>true</c> if null, empty, or consists of only whitepace; <c>false</c> otherwise.
     */
    public static boolean isBlank( final String str )
        {
        if (str == null)
            return true;
        /*
        **  CODE
        */
        for ( int idx = 0; idx < str.length(); ++idx )
            {
            final int ch = str.codePointAt( idx );

            if (!Character.isWhitespace( ch ))
                return false;

            if (Character.isSupplementaryCodePoint( ch ))
                idx++;
            }

        return true;
        }

    /**
     * Trims all trailing whitespace from the end of a StringBuilder.
     *
     * @param sb
     *     StringBuilder to trim.
     */
    public static void trim( StringBuilder sb )
        {
        if (sb == null || sb.length() <= 0)
            return;
        /*
        **  CODE
        */
        int index;

        for ( index = sb.length() - 1; index >= 0; --index )
            {
            int ch = sb.codePointAt( index );

            if (Character.isSupplementaryCodePoint( ch ))
                --index;

            if (!Character.isWhitespace( ch ))
                break;
            }

        sb.setLength( Math.max( 0, index ) );
        }
    }   /* end of class StrUtil */
