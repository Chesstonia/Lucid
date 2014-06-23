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
package net.humbleprogrammer.maxx.io;

import net.humbleprogrammer.humble.Stopwatch;
import net.humbleprogrammer.humble.TimeUtil;
import org.junit.*;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class TestPgnReader extends net.humbleprogrammer.maxx.TestBase
    {
    //  -----------------------------------------------------------------------
    //	CONSTANTS
    //	-----------------------------------------------------------------------

    /** File path. */
    private static final String FILE_PATH = "P:\\Chess\\PGN\\TWIC";
    /** Regular expression to match files. */
    private static final String FILE_RX   = "twic\\d+\\.pgn$";

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Total number of games read in. */
    protected static int        s_iNetGames    = 0;
    /** Total number of nanoseconds spent generating moves. */
    protected static long       s_lNetNanosecs = 0L;
    /** List of PGN files.. */
    protected static List<Path> s_listFiles    = new ArrayList<Path>();

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
        Stopwatch swatch = new Stopwatch();

        try
            {
            for ( Path path : s_listFiles )
                {
                PgnReader pgn = new PgnReader( new FileReader( path.toFile() ) );

                swatch.start();
                while ( pgn.readGame() != null )
                    s_iNetGames++;
                swatch.stop();

                s_lNetNanosecs += swatch.getElapsed();
                }
            }
        catch (IOException ex)
            {

            }
        }

    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    @AfterClass
    public static void displayResults()
        {
        final long lMillisecs = TimeUnit.NANOSECONDS.toMillis( s_lNetNanosecs );

        if (lMillisecs > 0L && s_iNetGames > 0)
            {
            s_log.info( String.format( "%s: PgnReader read %,d games in %s (%,d/sec)",
                                       DURATION.toString(),
                                       s_iNetGames,
                                       TimeUtil.formatMillisecs( lMillisecs, true ),
                                       (s_iNetGames * 1000L) / lMillisecs ) );
            }
        }

    @BeforeClass
    public static void setup()
        {
        final int[] iLimits = new int[]
            {
                25,     // QUICK    (~15 seconds)
                100,    // NORMAL   (~45 seconds)
                250,    // SLOW     (~15 minutes)
                1000    // EPIC     (~45 minutes)
            };

        s_iNetGames = 0;
        s_lNetNanosecs = 0L;
        //
        //  Build a list of *.pgn files.
        //
        int iLimit = (DURATION != Duration.UNLIMITED)
                     ? iLimits[ DURATION.ordinal() ]
                     : Integer.MAX_VALUE;

        FindFiles finder = new FindFiles( FILE_RX, iLimit );

        try
            {
            Files.walkFileTree( Paths.get( FILE_PATH ), finder );
            //
            //  Sort the list into alphabetical order so that it always is traversed
            //  in the same order.
            //
            s_listFiles = finder.getFiles();
            Collections.sort( s_listFiles );
            }
        catch (IOException ex)
            {
            s_log.error( "Files.walkFileTree() threw {} => {}",
                         ex.getClass(),
                         ex.getMessage() );
            }
        }

    //  -----------------------------------------------------------------------
    //	NESTED CLASS: FindFiles
    //	-----------------------------------------------------------------------

    /**
     * Traverses a directory, match files against a regular expression.
     */
    static class FindFiles extends SimpleFileVisitor<Path>
        {
        private final int        _iLimit;
        private final List<Path> _list;
        private final Pattern    _rxFile;

        public FindFiles( String strRX, int iLimit )
            {
            assert strRX != null;
            assert iLimit >= 0;
            /*
            **  CODE
            */
            _iLimit = iLimit;
            _list = new ArrayList<Path>();
            _rxFile = Pattern.compile( strRX, Pattern.CASE_INSENSITIVE );
            }

        @Override
        public FileVisitResult visitFile( Path file, BasicFileAttributes attr )
            {
            if (attr.isRegularFile())
                {
                if (_rxFile.matcher( file.toString() ).find())
                    _list.add( file );
                }

            return (_list.size() >= _iLimit)
                   ? FileVisitResult.TERMINATE
                   : FileVisitResult.CONTINUE;
            }

        List<Path> getFiles()
            { return _list; }
        }
    }   /* end of class TestPgnReader */
