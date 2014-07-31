/**
 * 
 */
package com.sos.JSHelper.enums;
/**
 * @author KB
 *
 */
public enum enuTransferTypes {
	// @formatter:off
	local, /* filesystem on localhost */
	file, /* same as local */
	ftp, 
	sftp, 
	ftps, 
	ssh2, 
	zip, 
	mq, // Message Queue
	http, 
	https, 
	svn, 
	webdav, 
	smb, 
	smtp, 
	imap
	/* */;
	// @formatter:on
	public String Text() {
		String strT = this.name();
		return strT;
	}

	public static String[] getArray() {
		String[] strA = new String[enuTransferTypes.values().length];
		int i = 0;
		for (enuTransferTypes enuType : enuTransferTypes.values()) {
			strA[i++] = enuType.Text();
		}
		return strA;
	}
}