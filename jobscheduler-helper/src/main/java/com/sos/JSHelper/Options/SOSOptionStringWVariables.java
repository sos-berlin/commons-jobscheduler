package com.sos.JSHelper.Options;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

 
public class SOSOptionStringWVariables extends SOSOptionElement {
	private static final long	serialVersionUID	= 3890065543134955852L;
	private final String		conClassName		= "SOSOptionFileName";
	protected String			strOriginalValue	= "";

	/**
	 * \brief SOSOptionStringWVariables
	 *
	 * \details
	 *
	 * @param pPobjParent
	 * @param pPstrKey
	 * @param pPstrDescription
	 * @param pPstrValue
	 * @param pPstrDefaultValue
	 * @param pPflgIsMandatory
	 * @throws Exception
	 */
	public SOSOptionStringWVariables(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
			final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
		super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
		intOptionType = isOptionTypeString;
	}

	public SOSOptionStringWVariables(final String pstrValue) {
		this(null, "", "", pstrValue, pstrValue, false);
	}

	private boolean hasPlaceHolder(final String pstrString) {
		boolean flgRet = false;
		int intKlAuf = pstrString.indexOf("[");
		int intKlZu = pstrString.indexOf("]");
		if (intKlAuf > -1 && intKlZu > -1 && intKlZu > intKlAuf) {
			flgRet = true;
		}
		return flgRet;
	}

	public final String substituteAllDate() throws Exception {
		String temp = OriginalValue();
		if (hasPlaceHolder(strOriginalValue)) {
			String targetFileName = strValue;
			temp = substituteDateMask();
			while (!targetFileName.equals(temp)) {
				targetFileName = temp;
				temp = substituteDateMask();
			}
		}
		else {
		}
		return temp;
	}

	private String substituteDateMask() throws Exception {
		final String conVarName = "[date:";
		String targetFilename = OriginalValue();
		try {
			// check for a date format string given in the file mask
			if (targetFilename.matches("(.*)(\\" + conVarName + ")([^\\]]*)(\\])(.*)")) {
				int posBegin = targetFilename.indexOf(conVarName);
				if (posBegin > -1) {
					int posEnd = targetFilename.indexOf("]", posBegin + 6);
					if (posEnd > -1) {
						String strDateMask = targetFilename.substring(posBegin + 6, posEnd);
						if (strDateMask.length() <= 0) {
							strDateMask = SOSOptionTime.dateTimeFormat;
						}
						String strDateTime = SOSOptionTime.getCurrentTimeAsString(strDateMask);
						String strT = (posBegin > 0 ? targetFilename.substring(0, posBegin) : "") + strDateTime;
						if (targetFilename.length() > posEnd) {
							strT += targetFilename.substring(posEnd + 1);
						}
						targetFilename = strT;
					}
				}
			}
			return targetFilename;
		}
		catch (Exception e) {
			throw new JobSchedulerException("error substituting [date:]: " + e.getMessage(), e);
		}
	}

	public String OriginalValue() {
		String strT = strOriginalValue;
		if (isEmpty(strT)) {
			strT = strValue;
		}
		return strT;
	}

	public void doReSubstitution() {
		try {
			String strT = substituteAllDate();
			super.Value(strT);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String Value() {
		String strT = strValue;
		try {
			if (strValue != null) {
				strT = substituteAllDate();
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(e);
		}
		return strT;
	}

	@Override
	public void Value(final String pstrStringValue) {
		super.Value(pstrStringValue);
		strOriginalValue = strValue;
	} // public void Value

}
