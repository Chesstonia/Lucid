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

import java.util.Arrays;

/**
 * The {@link StrUtil} class implements string-related utiltiy methods.
 */
public class StrUtil
	{

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Create a string that contains a repeated character
	 *
	 * @param ch
	 *            Character to fill with.
	 * @param iCount
	 *            Count.
	 * @return String containing <c>iCount</c> characters.
	 */
	public static String create( char ch, int iCount )
		{
		if (ch == 0 || iCount <= 0) return "";

		assert iCount < (1024 * 1024); // 1MB sanity check
		//	-----------------------------------------------------------------
		char[] array = new char[iCount];

		Arrays.fill(array, ch);

		return new String(array);
		}

	/**
	 * Searchs a string array for a given value.
	 *
	 * @param array
	 *            Array to search
	 * @param strKey
	 *            Desired value.
	 * @return <c>true</c> if found; <c>false</c> otherwise.
	 */
	public static boolean contains( final String[] array, final String strKey )
		{
		if (array == null || strKey == null) return false;
		//	-----------------------------------------------------------------
		for ( final String str : array )
			if (strKey.equals(str)) return true;

		return false;
		}

	/**
	 * Tests a string to see if it is blank, empty, or null.
	 *
	 * @param str
	 *            String to test.
	 * @return <c>true</c> if null, empty, or consists of only whitepace;
	 *         <c>false</c> otherwise.
	 */
	public static boolean isBlank( final String str )
		{
		if (str == null) return true;
		//	-----------------------------------------------------------------
		for ( int idx = 0; idx < str.length(); ++idx )
			{
			final int ch = str.codePointAt(idx);

			if (!Character.isWhitespace(ch)) return false;

			if (Character.isSupplementaryCodePoint(ch)) idx++;
			}

		return true;
		}

	/**
	 * Returns the singular or plural form of a string, based on the count.
	 * 
	 * @param count
	 *            Number of elements.
	 * @param singular
	 *            Singular form.
	 * @param plural
	 *            Plural form, or null if plural is singular + 's'
	 * @return Correct form.
	 */
	public static String pluralize( int count, final String singular, final String plural )
		{
		if (count == 1) return singular;
		//	-----------------------------------------------------------------
		return (plural != null) ? plural : singular + 's';
		}

	} /* end of class StrUtil */
