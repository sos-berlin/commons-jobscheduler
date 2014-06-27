/**
 * 
 */
package com.sos.VirtualFileSystem.common;

import com.sos.localization.Messages;
import com.sos.localization.SOSMsg;

/**
 * @author JS
 *
 */
public class SOSMsgVfs extends SOSMsg {

	public static final Messages	SOSMsgVfsProperties	= null;

	public SOSMsgVfs(final String pstrMessageCode) {
		super(pstrMessageCode);
		if (Messages == null) {
			super.setMessageResource("SOSVirtualFileSystem");
			// Self assignment of field com.sos.localization.SOSMsg.Messages in new com.sos.VirtualFileSystem.common.SOSMsgVfs(String) [Scariest(1), High confidence]
//			this.Messages = super.Messages;
		}
		else {
//			super.Messages = this.Messages;
		}
	}

}
