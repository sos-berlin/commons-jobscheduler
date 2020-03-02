package sos.net.ssh;

import com.sos.localization.Messages;
import com.sos.localization.SOSMsg;

public class SOSMsgNet extends SOSMsg {

    public static final Messages SOSMsgNetProperties = null;

    public SOSMsgNet(final String pstrMessageCode) {
        super(pstrMessageCode);

        if (getMessages() == null) {
            super.setMessageResource("com_sos_net_messages");
        }
    }
}
