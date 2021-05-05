package com.sos.vfs.common;

import com.sos.localization.SOSMsg;
import com.sos.localization.Messages;

public class SOSVFSMsg extends SOSMsg {

    public static final Messages SOSMsgVfsProperties = null;

    public SOSVFSMsg(String msg) {
        super(msg);
        if (getMessages() == null) {
            super.setMessageResource("SOSVirtualFileSystem");
        }
    }

}
