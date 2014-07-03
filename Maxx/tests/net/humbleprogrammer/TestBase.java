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
package net.humbleprogrammer;

import net.humbleprogrammer.maxx.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** The {@link TestBase} class contains all of the test configuration information. */
public abstract class TestBase
    {

    //  -----------------------------------------------------------------------
    //	CONSTANTS
    //	-----------------------------------------------------------------------

    /** Square index used for range testing. */
    protected static final int SQ_LO = -512;
    /** Square index used for range testing. */
    protected static final int SQ_HI = +512;

    /** Default file encoding. */
    private static final String ENCODING      = "UTF-8";
    /** Test file for FEN strings. */
    private static final String FEN_TEST_FILE = "FEN-Test.txt";

    //  -----------------------------------------------------------------------
    //	PUBLIC DECLARATIONS
    //	-----------------------------------------------------------------------

    public enum Duration
        {
            QUICK,
            NORMAL,
            SLOW,
            EPIC,
            UNLIMITED
        }

    /** Test duration. */
    protected static final Duration DURATION = Duration.NORMAL;

    /** Maximum test duration, in nanoseconds. */
    protected static final long s_lMaxNanosecs;
    /** Array of FEN strings. */
    protected static final List<String> s_listFEN = new ArrayList<String>();
    /** Array of PGN files. */
    protected static final List<Path>   s_listPGN = new ArrayList<Path>();
    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Logger. */
    protected static final Logger s_log = LoggerFactory.getLogger( "MAXXTEST" );

    //
    // Sample position used for testing; after 17. f4
    //	    a   b   c   d   e   f   g   h
    //	  +---+---+---+---+---+---+---+---+
    //	8 |-r-|///|   |///|-k-|///|   |-r-| 8
    //	  +---+---+---+---+---+---+---+---+
    //	7 |-p-|-p-|-q-|   |-b-|   |///|   | 7
    //	  +---+---+---+---+---+---+---+---+
    //	6 |   |///|-p-|///|-p-|-n-|   |///| 6
    //	  +---+---+---+---+---+---+---+---+
    //	5 |///|   |=P=|-p-|///|-p-|///|-p-| 5
    //	  +---+---+---+---+---+---+---+---+
    //	4 |   |///|   |=P=|   |=P=|-p-|///| 4
    //	  +---+---+---+---+---+---+---+---+
    //	3 |///|   |///|=B=|=P=|   |///|=P=| 3
    //	  +---+---+---+---+---+---+---+---+
    //	2 |=P=|=P=|   |=N=|-Q-|///|=P=|///| 2
    //	  +---+---+---+---+---+---+---+---+
    //	1 |=R=|   |///|   |///|=R=|=K=|   | 1
    //	  +---+---+---+---+---+---+---+---+
    //	    a   b   c   d   e   f   g   h
    //
    /** Test position expressed as an EPD string, after 17. f4 */
    protected static final String EPD_TEST = "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3";
    /** Test position expressed as an FEN string, after 17. f4 . */
    protected static final String FEN_TEST = EPD_TEST + " 0 17";


    //  -----------------------------------------------------------------------
    //	CTOR
    //	-----------------------------------------------------------------------

    static
        {
        switch (DURATION)
            {
            case QUICK:
                s_lMaxNanosecs = TimeUnit.SECONDS.toNanos( 15 );
                break;
            case NORMAL:
                s_lMaxNanosecs = TimeUnit.MINUTES.toNanos( 2 );
                break;
            case SLOW:
                s_lMaxNanosecs = TimeUnit.MINUTES.toNanos( 15 );
                break;
            case EPIC:
                s_lMaxNanosecs = TimeUnit.HOURS.toNanos( 2 );
                break;
            default:
                s_lMaxNanosecs = Long.MAX_VALUE;
                break;
            }
        //
        //  Build a list of FEN positions.
        //
        try
            {
            BufferedReader reader = openTestFile( FEN_TEST_FILE );
            String strFEN;

            while ( (strFEN = reader.readLine()) != null )
                if (Parser.matchFEN( strFEN ) != null)
                    s_listFEN.add( strFEN );

            reader.close();
            }
        catch (IOException ex)
            {
            s_log.warn( "Failed to read from {}: {}",
                        FEN_TEST_FILE,
                        ex.getMessage() );
            }

        s_log.debug( "Found {} FEN samples.", s_listFEN.size() );
        Collections.sort( s_listFEN );
        //
        //  Build a list of *.pgn files
        //
        Path pathPgnRoot = Paths.get( "P:\\Chess\\PGN\\TWIC" );

        try
            {
            DirectoryStream<Path> stream = Files.newDirectoryStream( pathPgnRoot, "*.pgn" );

            for ( Path path : stream )
                s_listPGN.add( path );

            stream.close();
            }
        catch (IOException ex)
            {
            s_log.warn( "Failed to find PGN files: {}",
                        ex.getMessage() );
            }

        s_log.debug( "Found {} PGN files.", s_listPGN.size() );
        Collections.sort( s_listPGN );
        }

    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    /**
     * Opens a reader for a file in the system Test Data directory.
     *
     * @param strFilename
     *     Name of file to open.
     *
     * @return Reader object if file found, <code>null</code> otherwise.
     */

    protected static BufferedReader openTestFile( final String strFilename )
        {
        BufferedReader reader = null;

        try
            {
            final Path path = Paths.get( "P:\\Chess\\Test Data" )
                                   .resolve( strFilename );

            final FileInputStream fis = new FileInputStream( path.toFile() );
            final InputStreamReader isr = new InputStreamReader( fis, ENCODING );

            reader = new BufferedReader( isr );
            }
        catch (IOException ex)
            {
            s_log.warn( "Failed to open test file '{}'", strFilename );
            s_log.error( ex.getMessage(), ex );
            }

        return reader;
        }
    }   /* end of class TestBase */
