/**
 * 
 */
package com.sos.JSHelper.enums;


public enum enuTransferModes  {
	ascii("ascii"), binary("binary"), text("text");

	public final String	description;

	private enuTransferModes() {
		this(null);
	}

	public String Text() {
		String strT = this.name();
		return strT;
	}

	/**
	 * constructor for enum
	 * @param name
	 */
	private enuTransferModes(final String name) {
		String k;
		if (name == null) {
			k = this.name();
		}
		else {
			k = name;
		}
		description = k;
	}
	
	public static String[] getArray() {
		String[] strA = new String[enuTransferModes.values().length];
		int i = 0;
		for (enuTransferModes enuType : enuTransferModes.values()) {
			strA[i++] = enuType.Text();
		}
		return strA;
	}

}