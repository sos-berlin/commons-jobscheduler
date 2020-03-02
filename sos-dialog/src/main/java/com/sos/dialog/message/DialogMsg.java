package com.sos.dialog.message;

import com.sos.localization.Messages;
import com.sos.localization.SOSMsg;

public class DialogMsg extends SOSMsg {

    public static Messages objDialogMessages = null;

    public DialogMsg(final String pstrMessageCode) {
        super(adjustMsgCode(pstrMessageCode));
        if (objDialogMessages == null) {
            super.setMessageResource("DialogMessages");
            objDialogMessages = super.getMessages();
        } else {
            setMessages(objDialogMessages);
        }
    }

    public DialogMsg newMsg(final String pstrMessageCode) {
        return new DialogMsg(pstrMessageCode);
    }

    private static String adjustMsgCode(final String pstrMsgCode) {
        if (pstrMsgCode.toLowerCase().startsWith("dialog_")) {
            return pstrMsgCode;
        }
        return "Dialog_L_" + pstrMsgCode.toLowerCase();
    }

    public void openHelp(final String helpKey) {
        //
    }

}