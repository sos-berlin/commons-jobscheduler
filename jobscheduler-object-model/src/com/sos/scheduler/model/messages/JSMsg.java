/**
 *
 */
package com.sos.scheduler.model.messages;

import com.sos.localization.SOSMsg;

/**
 * @author KB
 *
 */
public class JSMsg extends SOSMsg {

	public static int	VerbosityLevel	= 0;

	/**
	 * @param pstrMessageCode
	 */
	public JSMsg(final String pstrMessageCode) {
		super(pstrMessageCode);

		if (Messages == null) {
			super.setMessageResource("com_sos_scheduler_model");
			Messages = super.Messages;
		}
		else {
			super.Messages = Messages;
		}

		intVerbosityLevel = VerbosityLevel;
		curVerbosityLevel = VerbosityLevel;
	}

	public JSMsg(final String pstrMessageCode, final int pintVerbosityLevel) {
		this(pstrMessageCode);
		intVerbosityLevel = pintVerbosityLevel;
	}

	@Override
	protected void checkVerbosityLevel() {
		super.curVerbosityLevel = VerbosityLevel;
	}
}
