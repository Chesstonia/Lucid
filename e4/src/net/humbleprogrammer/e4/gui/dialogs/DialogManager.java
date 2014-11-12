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
package net.humbleprogrammer.e4.gui.dialogs;

import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.e4.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

@SuppressWarnings( "unused" )
public class DialogManager
    {

    //  -----------------------------------------------------------------------
    //	STATIC DECLARATIONS
    //	-----------------------------------------------------------------------

    /** Logger */
    private static final Logger s_log = LoggerFactory.getLogger( DialogManager.class );

    //  -----------------------------------------------------------------------
    //	PUBLIC METHODS
    //	-----------------------------------------------------------------------

    /**
     * Displays a simple message to the user.
     *
     * @param strMessage
     *     Question to ask.
     */
    public static void advise( String strMessage )
        {
        advise( strMessage, App.getName() );
        }

    /**
     * Displays a simple message to the user.
     *
     * @param strMessage
     *     Question to ask.
     * @param strCaption
     *     Dialog title, or null for generic App name.
     */
    public static void advise( String strMessage, String strCaption )
        {
        DBC.requireNotBlank( strMessage, "Message" );
        DBC.requireNotBlank( strCaption, "Caption" );
        /*
        **  CODE
        */
        JOptionPane.showMessageDialog( App.getFrame(),
                                       strMessage,
                                       strCaption,
                                       JOptionPane.INFORMATION_MESSAGE );

        s_log.info( "User advised '{}'", strMessage );
        }

    /**
     * Asks the user a simple "Yes/No" question.
     *
     * @param strMessage
     *     Question to ask.
     *
     * @return .T. if user answers "Yes"; .F. otherwise.
     */
    public static boolean confirm( String strMessage )
        {
        return confirm( strMessage, App.getName() );
        }

    /**
     * Asks the user a simple "Yes/No" question.
     *
     * @param strMessage
     *     Question to ask.
     * @param strCaption
     *     Dialog title, or null for generic App name.
     *
     * @return .T. if user answers "Yes"; .F. otherwise.
     */
    public static boolean confirm( String strMessage, String strCaption )
        {
        DBC.requireNotBlank( strMessage, "Message" );
        DBC.requireNotBlank( strCaption, "Caption" );
        /*
        **  CODE
        */
        boolean bYes = JOptionPane.YES_OPTION ==
                       JOptionPane.showConfirmDialog( App.getFrame(),
                                                      strMessage,
                                                      strCaption,
                                                      JOptionPane.YES_NO_OPTION );

        s_log.info( "User confirmed '{}' => {}", strMessage, bYes );

        return bYes;
        }

    /**
     * Displays an error message to the user.
     *
     * @param strMessage
     *     Question to ask.
     */
    public static void warn( String strMessage )
        {
        advise( strMessage, App.getName() );
        }

    /**
     * Displays an error message to the user.
     *
     * @param strMessage
     *     Question to ask.
     * @param strCaption
     *     Dialog title, or null for generic App name.
     */
    public static void warn( String strMessage, String strCaption )
        {
        DBC.requireNotBlank( strMessage, "Message" );
        DBC.requireNotBlank( strCaption, "Caption" );
        /*
        **  CODE
        */
        JOptionPane.showMessageDialog( App.getFrame(),
                                       strMessage,
                                       strCaption,
                                       JOptionPane.ERROR_MESSAGE );

        s_log.info( "User warned '{}'", strMessage );
        }

    }   /* end of class DialogManager */
