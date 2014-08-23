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
@SuppressWarnings("unused")
public abstract class TestBase
    {

    //  -----------------------------------------------------------------------
    //	CONSTANTS
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

    /** Square index used for range testing. */
    protected static final int SQ_LO = -512;
    /** Square index used for range testing. */
    protected static final int SQ_HI = +512;

    /** Default file encoding. */
    private static final   String ENCODING      = "UTF-8";
    /** Test file for FEN strings. */
    private static final   String FEN_TEST_FILE = "FEN-Test.txt";
    /** Sample game. */
    protected static final String SAMPLE_PGN    =
        "[Event \"M.I.Chigorin Memorial Open\"]\n" +
        "[Site \"St.Petersburg (Russia)\"]\n" +
        "[Date \"1998.??.??\"]\n" +
        "[Round \"6.10\"]\n" +
        "[White \"Rusanov, M \"]\n" +
        "[Black \"Voitsekhovsky, S \"]\n" +
        "[Result \"0-1\"]\n" +
        "[ECO \"A45\"]\n" +
        "[Opening \"Trompovsky attack (Ruth, Opovcensky opening)\"]\n" +
        "\n" +
        "1. d4 Nf6 2. Bg5 d5 3. Bxf6 exf6 4. e3 Be6 5. Nd2 c6 6. c3 f5 7. Bd3 Nd7 8. Qf3 \n" +
        "g6 9. Ne2 Bd6 10. Nf4 Qc7 11. O-O {castle short} Nf6 12. h3 h5 13. Nxe6 fxe6 14.\n" +
        "c4 g5 15. Qe2 g4 16. c5 Be7 17. f4 gxf3 {e.p.} 18. Nxf3 O-O-O {castle long} 19. \n" +
        "b4 Rdg8 {file specifier} 20. b5 cxb5 21. Rfb1 Qg3 22. Rxb5 Qxh3 {pawn on g2 is\n" +
        "pinned} 23. Ne1 Bd8 24. Rxb7 Bc7 25. Rxc7+ {check} Kxc7 26. Qd2 Kd7 27. Rb1 Ne4 \n" +
        "28. Bxe4 fxe4 29. c6+ Ke7 30. Rb2 Qh4 31. Qb4+ Kf6 32. Rf2+ Kg6 33. Qd6 {pawn on\n" +
        "e6 is pinned} Re8 34. c7 Qe7 35. c8=N {under-promotion} Qxd6 36. Nxd6 Re7 37. \n" +
        "Rc2 Rb8 38. Rc6 Rb2 39. a4 Ra2 40. Nc8 Rb7 41. Rxe6+ Kf7 42. Nd6+ Kxe6 43. Nxb7 \n" +
        "h4 44. Kf1 Ke7 45. Nc5 Kd6 46. Nb7+ Kc6 47. Nd8+ Kb6 48. Ne6 Rxa4 49. Nf4 Kc6 \n" +
        "50. Ke2 Ra2+ 51. Kd1 Ra1+ 52. Kd2 a5 53. Nc2 Rg1 54. Ne1 a4 55. Nc2 Kb5 56. Kc3 \n" +
        "Rxg2 57. Nb4 a3 58. Nxg2 h3 59. Ne1 h2 60. Nec2 Ka4 61. Nxd5 h1=Q {promotion} \n" +
        "62. Nb6+ Kb5 63. Nc4 a2 64. N4a3+ {rank specifier} Ka4 65. Kb2 Qh7 66. Kxa2 Qf7+ \n" +
        "67. Kb2 Qb7+ 68. Kc3 Qb3+ 69. Kd2 Qd3+ 70. Ke1 Kb3 71. Kf2 Qd1 72. Kg3 Qf3+ 73. \n" +
        "Kh4 Qf5 74. Kg3 Kc3 75. Ne1 Kd2 76. Nac2 Ke2 77. Ng2 Qg5+ 0-1\n";

    /** Sample game as a list of SAN moves. */
    protected static final String[] SAMPLE_MOVES =
        {
            "d4", "Nf6", "Bg5", "d5", "Bxf6", "exf6", "e3", "Be6", "Nd2", "c6", "c3", "f5",
            "Bd3", "Nd7", "Qf3", "g6", "Ne2", "Bd6", "Nf4", "Qc7", "O-O", "Nf6", "h3", "h5",
            "Nxe6", "fxe6", "c4", "g5", "Qe2", "g4", "c5", "Be7", "f4", "gxf3", "Nxf3", "O-O-O",
            "b4", "Rdg8", "b5", "cxb5", "Rfb1", "Qg3", "Rxb5", "Qxh3", "Ne1", "Bd8", "Rxb7",
            "Bc7", "Rxc7+", "Kxc7", "Qd2", "Kd7", "Rb1", "Ne4", "Bxe4", "fxe4", "c6+", "Ke7",
            "Rb2", "Qh4", "Qb4+", "Kf6", "Rf2+", "Kg6", "Qd6", "Re8", "c7", "Qe7", "c8=N",
            "Qxd6", "Nxd6", "Re7", "Rc2", "Rb8", "Rc6", "Rb2", "a4", "Ra2", "Nc8", "Rb7",
            "Rxe6+", "Kf7", "Nd6+", "Kxe6", "Nxb7", "h4", "Kf1", "Ke7", "Nc5", "Kd6", "Nb7+",
            "Kc6", "Nd8+", "Kb6", "Ne6", "Rxa4", "Nf4", "Kc6", "Ke2", "Ra2+", "Kd1", "Ra1+",
            "Kd2", "a5", "Nc2", "Rg1", "Ne1", "a4", "Nc2", "Kb5", "Kc3", "Rxg2", "Nb4", "a3",
            "Nxg2", "h3", "Ne1", "h2", "Nec2", "Ka4", "Nxd5", "h1=Q", "Nb6+", "Kb5", "Nc4",
            "a2", "N4a3+", "Ka4", "Kb2", "Qh7", "Kxa2", "Qf7+", "Kb2", "Qb7+", "Kc3", "Qb3+",
            "Kd2", "Qd3+", "Ke1", "Kb3", "Kf2", "Qd1", "Kg3", "Qf3+", "Kh4", "Qf5", "Kg3",
            "Kc3", "Ne1", "Kd2", "Nac2", "Ke2", "Ng2", "Qg5+"
        };
    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Maximum test duration, in nanoseconds. */
    protected static final long s_lMaxNanosecs;
    /** Logger. */
    protected static final Logger s_log    = LoggerFactory.getLogger( "MAXXTEST" );
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
    protected static final String EPD_TEST =
        "r3k2r/ppq1b3/2p1pn2/2Pp1p1p/3P1Pp1/3BP2P/PP1NQ1P1/R4RK1 b kq f3";
    /** Test position expressed as an FEN string, after 17. f4 . */
    protected static final String FEN_TEST = EPD_TEST + " 0 17";

    /** Array of FEN strings. */
    private static List<String> s_listFEN;
    /** Array of PGN files. */
    private static List<Path>   s_listPGN;

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
        }

    //  -----------------------------------------------------------------------
    //	METHODS
    //	-----------------------------------------------------------------------

    /**
     * Gets a set of FEN positions.
     *
     * @return Collection of FEN positions.
     */
    protected static List<String> getFEN()
        {
        if (s_listFEN == null)
            {
            s_listFEN = new ArrayList<>();

            try (BufferedReader reader = openTestFile( FEN_TEST_FILE ))
                {
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
            }

        return Collections.unmodifiableList( s_listFEN );
        }

    /**
     * Builds a set of *.pgn files.
     *
     * @return Collection of files.
     */
    protected static List<Path> getPGN()
        {
        if (s_listPGN == null)
            {
            Path pathPgnRoot = Paths.get( "P:\\Chess\\PGN\\TWIC" );

            s_listPGN = new ArrayList<>();

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

        return Collections.unmodifiableList( s_listPGN );
        }

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
