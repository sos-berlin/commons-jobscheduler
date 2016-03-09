package sos.net.ssh;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSDataExchange", defaultLocale = "en")
public class SOSNetMessages extends JSToolBox {

    public final String conSVNVersion = "$Id$";
    public static final SOSMsgNet SOS_SSH_E_0100 = new SOSMsgNet("SOS_SSH_E_0100");										// Es
                                                                                    // wurde
                                                                                    // weder
                                                                                    // ein
                                                                                    // Kommando
                                                                                    // noch
                                                                                    // eine
                                                                                    // Kommandodatei
                                                                                    // angegeben.
                                                                                    // Abbruch.
    public static final SOSMsgNet SOS_SSH_D_0110 = new SOSMsgNet("SOS_SSH_D_0110");										// starte
                                                                                    // am
                                                                                    // remote-server
                                                                                    // das
                                                                                    // Kommando:
                                                                                    // '%1$s'.

    /*
     * ! \var TRANSACTION_ABORTED \brief could not complete transaction, cause:
     * %1$s
     */
    public static final SOSMsgNet TRANSACTION_ABORTED = new SOSMsgNet("SOSDataExchangeEngine.TRANSACTION_ABORTED");
    /*
     * ! \var EXCEPTION_RAISED \brief %1$s: Exception raised, cause: %2$s
     */

    public static final SOSMsgNet EXCEPTION_RAISED = new SOSMsgNet("EXCEPTION_RAISED");

    protected SOSNetMessages() {
        super();
    }
}
