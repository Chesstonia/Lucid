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
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public class TestPgnStream extends TestBase
    {
    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Total number of nanoseconds spent generating moves. */
    protected static long s_lNetNanosecs = 0L;
    /** Total number of tokens read in. */
    protected static long s_lNetTokens   = 0;

    //  -----------------------------------------------------------------------
    //	UNIT TESTS
    //	-----------------------------------------------------------------------

    @Test
    public void t_comprehensive()
        {
        String strFile = "";
        String strPgn = "";
        Stopwatch swatch = new Stopwatch();

        try
            {
            for ( Path path : s_listPGN )
                {
                strFile = path.toString();

                PgnReader pgn = new PgnReader( new FileReader( path.toFile() ) );

                while ( (strPgn = pgn.readGame()) != null )
                    {
                    PgnStream stream = new PgnStream( strPgn );
                    PgnStream.PgnToken token;

                    swatch.start();

                    while ( (token = stream.nextToken()) != null )
                        {
                        s_lNetTokens++;
                        if (token == PgnStream.PgnToken.Result)
                            break;
                        }

                    swatch.stop();
                    if ((s_lNetNanosecs += swatch.getElapsed()) >= s_lMaxNanosecs)
                        return;
                    }
                }
            }
        catch (Exception ex)
            {
            fail( String.format( "%s:\n%s\n%s",
                                 strFile, strPgn, ex.getMessage() ) );
            }
        }

    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    @AfterClass
    public static void displayResults()
        {
        final long lMillisecs = TimeUnit.NANOSECONDS.toMillis( s_lNetNanosecs );

        if (lMillisecs > 0L && s_lNetTokens > 0)
            {
            s_log.info( String.format( "%s: PgnStream parsed %,d tokens in %s (%,d/sec)",
                                       DURATION.toString(),
                                       s_lNetTokens,
                                       TimeUtil.formatMillisecs( lMillisecs, true ),
                                       (s_lNetTokens * 1000L) / lMillisecs ) );
            }
        }

    @BeforeClass
    public static void setup()
        {
        s_lNetTokens = 0;
        s_lNetNanosecs = 0L;
        }

    }   /* end of class TestPgnStream */
