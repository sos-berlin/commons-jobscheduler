package com.sos.JSHelper.Options;

public class SOSOptionPlatform extends SOSOptionStringValueList {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1272813840437569113L;

	public enum enuValidPlatforms {
		unix, linux, hpux, aix, windows, bs2000
	}
	private final enuValidPlatforms	enuPlatform	= null;

	public SOSOptionPlatform(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
			final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
		super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
	}

	public boolean isBS2000() {
		return enuPlatform == enuValidPlatforms.bs2000;
	}

	public boolean isWindows() {
		return enuPlatform == enuValidPlatforms.windows;
	}

	public boolean isUnix() {
		return enuPlatform == enuValidPlatforms.unix;
	}

	public boolean isLinux() {
		return enuPlatform == enuValidPlatforms.linux;
	}

	public String getPathDelimiter() {
		String strT = "/";

		if (isWindows() == true) {
			strT = "\\";
		}
		else {
			if (isBS2000() == true) {
				strT = "";
			}
		}
		return strT;
	}

	@Override
	public void Value(final String pstrValue) {
		super.Value(pstrValue);
	}

	@Override
	public String[] getValueList() {

		if (strValueList == null) {
			strValueList = new String[] {};
			createValueList("unix;linux;hpux;aix;windows;bs2000");
		}

		return strValueList;
	}


}
