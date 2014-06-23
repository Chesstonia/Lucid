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

public class Stopwatch
    {

    //  -----------------------------------------------------------------------
    //	DECLARATIONS
    //	-----------------------------------------------------------------------

    /** System time when stopwatch was started, or -1 if never started. */
    private long _lStarted = -1L;
    /** System time when stopwatch was stopped, or -1 if still running. */
    private long _lStopped;

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /** Stops the stopwatch and clears any elapsed time. */
    public void reset()
        { _lStarted = _lStopped = -1L; }

    /** Starts (or restarts) the stopwatch. */
    public void start()
        {
        _lStopped = -1L;
        _lStarted = System.nanoTime();
        }

    /**
     * Creates and starts a new stopwatch.
     *
     * @return Running stopwatch object.
     */
    public static Stopwatch startNew()
        {
        final Stopwatch swatch = new Stopwatch();

        swatch.start();
        return swatch;
        }

    /** Stops the stopwatch, if running. */
    public void stop()
        {
        _lStopped = (_lStarted >= 0L) ? System.nanoTime() : -1L;
        }

    //  -----------------------------------------------------------------------
    //	PUBLIC GETTERS & SETTER
    //	-----------------------------------------------------------------------

    /**
     * Gets the raw elapsed time.
     *
     * @return Elapsed time, in nanoseconds.
     */
    public long getElapsed()
        {
        if (_lStarted < 0L)
            return 0L;  // never started
        /*
        **  CODE
        */
        return (_lStopped < _lStarted)
               ? (System.nanoTime() - _lStarted)
               : (_lStopped - _lStarted);
        }

    /**
     * Gest the elapsed time in milliseconds.
     *
     * @return Elapsed time, measured in milliseconds.
     */
    public long getElapsedMillisecs()
        { return getElapsed() / (TimeUtil.NANOSECONDS / TimeUtil.MILLISECONDS); }

    /**
     * Gets the elapsed time in seconds.
     *
     * @return Elapsed time, measured in seconds.
     */
    public double getElapsedSeconds()
        { return (double) getElapsed() / TimeUtil.NANOSECONDS; }

    /**
     * Gets the state of the stopwatch.
     *
     * @return <c>true</c> if running; <c>false</c> if stopped.
     */
    public boolean isRunning()
        { return (_lStarted >= 0L); }

    /**
     * Gets a string representation of the elapsed time.
     *
     * @return String in the format "HH:MM:SS.FFF"
     */
    @Override
    public String toString()
        { return TimeUtil.formatMillisecs( getElapsedMillisecs(), true ); }
    }   /* end of class Stopwatch */
