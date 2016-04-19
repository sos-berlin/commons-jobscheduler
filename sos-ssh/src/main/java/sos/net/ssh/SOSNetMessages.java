package sos.net.ssh;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSDataExchange", defaultLocale = "en")
public class SOSNetMessages extends JSToolBox {

    public final String conSVNVersion = "$Id$";
    public static final SOSMsgNet SOS_SSH_E_0100 = new SOSMsgNet("SOS_SSH_E_0100");
    public static final SOSMsgNet SOS_SSH_D_0110 = new SOSMsgNet("SOS_SSH_D_0110");
    public static final SOSMsgNet TRANSACTION_ABORTED = new SOSMsgNet("SOSDataExchangeEngine.TRANSACTION_ABORTED");
    public static final SOSMsgNet EXCEPTION_RAISED = new SOSMsgNet("EXCEPTION_RAISED");

    protected SOSNetMessages() {
        super();
    }

}