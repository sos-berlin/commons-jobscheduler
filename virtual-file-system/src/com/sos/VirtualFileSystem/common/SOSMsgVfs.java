/**
 * 
 */
package com.sos.VirtualFileSystem.common;

import com.sos.localization.SOSMsg;
import com.sos.localization.Messages;

/**
 * @author JS
 *
 */
public class SOSMsgVfs extends SOSMsg {

	public static final Messages	SOSMsgVfsProperties	= null;

	public SOSMsgVfs(String pstrMessageCode) {
		super(pstrMessageCode);
		if (this.Messages == null) {
			super.setMessageResource("SOSVirtualFileSystem");
			this.Messages = super.Messages;
		}
		else {
			super.Messages = this.Messages;
		}
	}

}
