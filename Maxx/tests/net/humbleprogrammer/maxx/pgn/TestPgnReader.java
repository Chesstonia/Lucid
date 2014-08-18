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

import net.humbleprogrammer.TestBase;
import net.humbleprogrammer.humble.Stopwatch;
import net.humbleprogrammer.humble.TimeUtil;
import org.junit.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public class TestPgnReader extends TestBase
    {
    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Total number of games read in. */
    protected static int  s_iNetGames    = 0;
    /** Total number of nanoseconds spent generating moves. */
    protected static long s_lNetNanosecs = 0L;

    //  -----------------------------------------------------------------------
    //	UNIT TESTS
    //	-----------------------------------------------------------------------

    @Test( expected = IllegalArgumentException.class )
    public void t_ctor_fail_null()
        {
        new PgnReader( null );
        }

    @Test
    public void t_comprehensive()
        {
        Collection<Path> listPGN = getPGN();
        Stopwatch swatch = new Stopwatch();

        try
            {
            for ( Path path : listPGN )
                {
                PgnReader pgn = new PgnReader( new FileReader( path.toFile() ) );

                swatch.start();
                while ( pgn.readGame() != null )
                    s_iNetGames++;
                swatch.stop();

                if ((s_lNetNanosecs += swatch.getElapsed()) >= s_lMaxNanosecs)
                    break;
                }
            }
        catch (IOException ex)
            {
            fail( ex.getMessage() );
            }
        }

    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    @AfterClass
    public static void displayResults()
        {
        long lMillisecs = TimeUnit.NANOSECONDS.toMillis( s_lNetNanosecs );

        if (lMillisecs > 0L)
            {
            s_log.info( String.format( "%s: PgnReader read %,d games in %s (%,d gps)",
                                       DURATION.toString(),
                                       s_iNetGames,
                                       TimeUtil.formatMillisecs( lMillisecs, true ),
                                       (s_iNetGames * 1000L) / lMillisecs ) );
            }
        }

    @BeforeClass
    public static void setup()
        {
        s_iNetGames = 0;
        s_lNetNanosecs = 0L;
        }

    }   /* end of class TestPgnReader */
