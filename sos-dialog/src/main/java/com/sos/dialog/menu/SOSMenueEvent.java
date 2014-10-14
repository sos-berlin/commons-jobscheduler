/**
 * 
 */
package com.sos.dialog.menu;

/**
 * @author KB
 *
 */
public class SOSMenueEvent {

	public boolean	doIt			= false;
	public boolean	showMenueItem	= true;
	public static enum enuOperation {
		show, execute;
	}
	public enuOperation	operation	= enuOperation.execute;
	private String		strMessage	= "";

	/**
	 * 
	 */
	public SOSMenueEvent() {
		strMessage = "";
	}

	public void setMessage(final String pstrMessage) {
		strMessage = pstrMessage;
	}

	public String getMessage() {
		return strMessage;
	}

	public void addMessage(final String pstrMessage) {
		if (pstrMessage != null) {
			//		if (strMessage.length() > 0) {
			//			strMessage += "\n";
			//		}
			strMessage += pstrMessage + "\n";
		}

	}
	
	public boolean hasMessage () {
		return strMessage.length() > 0;
	}
}
