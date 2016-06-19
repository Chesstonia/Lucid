/* ****************************************************************************
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
 **	other parties provide the program "as is" without warranty of any kind,
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
 * The {@link DBC} class implements a series of "Design by Contract" tests that are used to
 * validate method parameters.
 *
 * This class was inspired by .NET source code released as part of Microsoft's StyleCop
 * application.
 */
public final class DBC
    {

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Tests an expression to ensure it is <c>true</c>.
     *
     * @param bExpression
     *     Expression to test.
     * @param strText
     *     Name of the object.
     *
     * @throws java.lang.IllegalArgumentException
     *     if <c>bExpression</c> is <c>false</c>.
     */
    public static void require( final boolean bExpression, final String strText )
        {
        if (!bExpression)
            throw new IllegalArgumentException( strText );
        }


    /**
     * Tests an value to ensure it is greater than zero.
     *
     * @param dValue
     *     Expression to test.
     * @param strName
     *     Name of the object.
     *
     * @throws java.lang.IllegalArgumentException
     *     if value is zero or negative.
     */
    public static void requireGreaterThanZero( final double dValue, final String strName )
        {
        if (dValue <= 0.0)
            throw new IllegalArgumentException( strName + " must be greater than zero." );
        }

    /**
     * Tests a string to ensure it is not null, empty, or blank.
     *
     * @param str
     *     String to test.
     * @param strName
     *     Name of the object.
     *
     * @throws java.lang.IllegalArgumentException
     *     if <c>str</c> is blank.
     */
    public static void requireNotBlank( final String str, final String strName )
        {
        if (StrUtil.isBlank( str ))
            throw new IllegalArgumentException( strName + " is blank." );
        }

    /**
     * Tests an object to ensure it is not <c>null</c>.
     *
     * @param obj
     *     Object to test.
     * @param strName
     *     Name of the object.
     *
     * @throws java.lang.IllegalArgumentException
     *     if <c>obj</c> is null.
     */
    public static void requireNotNull( final Object obj, final String strName )
        {
        if (obj == null)
            throw new IllegalArgumentException( strName + " is null." );
        }

    /**
     * Tests an object to ensure it is <c>null</c>.
     *
     * @param obj
     *     Object to test.
     * @param strName
     *     Name of the object.
     *
     * @throws java.lang.IllegalArgumentException
     *     if <c>obj</c> is null.
     */
    public static void requireNull( final Object obj, final String strName )
        {
        if (obj != null)
            throw new IllegalArgumentException( strName + " must be null." );
        }
    }   /* end of class DBC */
