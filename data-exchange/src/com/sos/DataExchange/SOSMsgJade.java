package com.sos.DataExchange;

import org.apache.log4j.Logger;

import com.sos.localization.Messages;
import com.sos.localization.SOSMsg;

public class SOSMsgJade extends SOSMsg {
	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused") private final Logger logger = Logger.getLogger(this.getClass());
	

	public static final Messages	SOSMsgJadeProperties	= null;

	public SOSMsgJade(final String pstrMessageCode) {
		super(pstrMessageCode);
		
		if (Messages == null) {
			super.setMessageResource("SOSDataExchange");
			Messages = super.Messages;
		}
		else {
			super.Messages = Messages;
		}
	}
}
