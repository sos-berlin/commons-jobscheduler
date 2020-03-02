/**
 * 
 */
package com.sos.JSHelper.Basics;

import com.sos.localization.Messages;
import com.sos.localization.SOSMsg;

/** @author JS */
public class SOSMsgJsh extends SOSMsg {

    public static final Messages SOSMsgVfsProperties = null;

    public SOSMsgJsh(final String pstrMessageCode) {
        super(pstrMessageCode);
        if (getMessages() == null) {
            super.setMessageResource("com_sos_JSHelper_Messages");
        }
    }

}
