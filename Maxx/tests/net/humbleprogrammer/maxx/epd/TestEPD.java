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
package net.humbleprogrammer.maxx.epd;

import org.junit.*;

import static org.junit.Assert.*;

public class TestEPD extends net.humbleprogrammer.TestBase
	{

	//  -----------------------------------------------------------------------
	//	UNIT TESTS
	//	-----------------------------------------------------------------------

	@Test
	public void t_ctor()
		{
		final String STR_EPD =
			"2r4k/pp3q1b/5PpQ/3p4/3Bp3/1P6/P5RP/6K1 w - - bm h4;";

		EPD epd = new EPD( STR_EPD );
		assertTrue( epd.isValid() );
		}

	@Test
	public void t_getOperand()
		{
		final String STR_EPD =
			"2r4k/pp3q1b/5PpQ/3p4/3Bp3/1P6/P5RP/6K1 w - - bm h4; id \"Undermine.037\"; c0 \"h4=10, Rg3=2, Rg4=2, Rg5=2\";";

		EPD epd = new EPD( STR_EPD );
		assertTrue( epd.isValid() );

		assertEquals( "h4", epd.getOperand( "bm" ) );
		assertEquals( "Undermine.037", epd.getOperand( "id" ) );
		assertEquals( "h4=10, Rg3=2, Rg4=2, Rg5=2", epd.getOperand( "c0" ) );
		}

	@Test
	public void t_getOperand_case()
		{
		final String STR_EPD =
			"2r4k/pp3q1b/5PpQ/3p4/3Bp3/1P6/P5RP/6K1 w - - bm h4; id \"Undermine.037\"; c0 \"h4=10, Rg3=2, Rg4=2, Rg5=2\";";

		EPD epd = new EPD( STR_EPD );
		assertTrue( epd.isValid() );

		assertEquals( "h4", epd.getOperand( "BM" ) );
		assertEquals( "Undermine.037", epd.getOperand( "ID" ) );
		assertEquals( "h4=10, Rg3=2, Rg4=2, Rg5=2", epd.getOperand( "C0" ) );
		}

	@Test
	public void t_getOperand_fail()
		{
		final String STR_EPD =
			"2r4k/pp3q1b/5PpQ/3p4/3Bp3/1P6/P5RP/6K1 w - - bm h4; id \"Undermine.037\"; c0 \"h4=10, Rg3=2, Rg4=2, Rg5=2\";";

		EPD epd = new EPD( STR_EPD );

		assertTrue( epd.isValid() );
		assertNull( epd.getOperand( null ));
		assertNull( epd.getOperand( "" ));
		assertNull( epd.getOperand( ",,,^..^,,," ));
		}

	@Test
	public void t_hasOpCode()
		{
		final String STR_EPD =
			"2r4k/pp3q1b/5PpQ/3p4/3Bp3/1P6/P5RP/6K1 w - - bm h4; id \"Undermine.037\"; c0 \"h4=10, Rg3=2, Rg4=2, Rg5=2\";";

		EPD epd = new EPD( STR_EPD );

		assertTrue( epd.isValid() );
		assertTrue( epd.hasOpCode( "bm" ));
		assertTrue( epd.hasOpCode( "id" ));
		assertTrue( epd.hasOpCode( "c0" ));
		}

	@Test
	public void t_hasOpCode_fail()
		{
		final String STR_EPD =
			"2r4k/pp3q1b/5PpQ/3p4/3Bp3/1P6/P5RP/6K1 w - - bm h4; id \"Undermine.037\"; c0 \"h4=10, Rg3=2, Rg4=2, Rg5=2\";";

		EPD epd = new EPD( STR_EPD );

		assertTrue( epd.isValid() );
		assertFalse( epd.hasOpCode( null ));
		assertFalse( epd.hasOpCode( "" ));
		assertFalse( epd.hasOpCode( ",,,^..^,,," ));
		}

	@Test
	public void t_hasOpCode_case()
		{
		final String STR_EPD =
			"2r4k/pp3q1b/5PpQ/3p4/3Bp3/1P6/P5RP/6K1 w - - bm h4; id \"Undermine.037\"; c0 \"h4=10, Rg3=2, Rg4=2, Rg5=2\";";

		EPD epd = new EPD( STR_EPD );

		assertTrue( epd.isValid() );
		assertTrue( epd.hasOpCode( "BM" ));
		assertTrue( epd.hasOpCode( "ID" ));
		assertTrue( epd.hasOpCode( "C0" ));
		}

	@Test( expected = IllegalArgumentException.class )
	public void t_ctor_fail_blank()
		{
		new EPD( "" );
		}

	@Test( expected = IllegalArgumentException.class )
	public void t_ctor_fail_null()
		{
		new EPD( null );
		}

	@Test
	public void t_matchEPD_fail()
		{
		assertNull( EPD.matchEPD( null ) );
		assertNull( EPD.matchEPD( "" ) );
		}
	}   /* end of class TestEPD */
