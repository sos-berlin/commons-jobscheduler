package com.sos.dialog.message;
import org.apache.log4j.Logger;

import com.sos.dialog.classes.SOSMsgControl;
import com.sos.localization.Messages;

public class DialogMsg extends SOSMsgControl {
	@SuppressWarnings("unused") 
	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id: JADEMsg.java 22018 2014-01-08 11:04:33Z kb $";
	@SuppressWarnings("unused")
	private final Logger		logger			= Logger.getLogger(this.getClass());
	private static Messages		objDialogMessages	= null;

	public DialogMsg(final String pstrMessageCode) {
		super(adjustMsgCode(pstrMessageCode));
		if (objDialogMessages == null) {
			super.setMessageResource("DialogMessages");
			objDialogMessages = super.Messages;
		}
		else {
			super.Messages = objDialogMessages;
		}
	} // public

	@Override
	public DialogMsg newMsg(final String pstrMessageCode) {
		return new DialogMsg (pstrMessageCode);
	}

	private static  String adjustMsgCode (final String pstrMsgCode) {
		return "Dialog_L_" + pstrMsgCode.toLowerCase();
	}
	@Override
	public void openHelp(final String helpKey) {
	} // public void openHelp
}
