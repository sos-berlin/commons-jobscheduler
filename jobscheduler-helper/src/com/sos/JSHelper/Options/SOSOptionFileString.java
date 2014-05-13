/**
 *
 */
package com.sos.JSHelper.Options;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;

/**
 * @author KB
 *
 */
public class SOSOptionFileString extends SOSOptionString {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 8834092589948617350L;
	private final String		conClassName		= "SOSOptionFileString";
	private String strFileName="";

	/**
	 * @param pPobjParent
	 * @param pPstrKey
	 * @param pPstrDescription
	 * @param pPstrValue
	 * @param pPstrDefaultValue
	 * @param pPflgIsMandatory
	 */
	public SOSOptionFileString(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
			final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
		super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
	}

	@Override
	public void Value(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Value";

		strFileName = pstrValue;
		if (pstrValue == null) {
			super.Value(pstrValue);
		}
		else {
			if (pstrValue.toLowerCase().startsWith("file:")) {
				strFileName = pstrValue.substring(5);

				JSFile objFle = new JSFile(strFileName);
				try {
					objFle.MustExist();
				}
				catch (Exception e) {
					throw new JobSchedulerException(String.format("File '%1$s' does not exist", strFileName), e);
				}
			}
			else {
				strFileName = pstrValue;
			}

			JSFile objF = new JSFile(strFileName);
			if (objF.canRead() == true) {
				String strT = objF.getContent().trim();
				super.Value(strT);
			}
			else {
				// TODO unescape html/xml entities
				super.Value(pstrValue);
			}
		}

		//	return void;
	} // private void Value

	public String getStrFileName() {
		return strFileName;
	}

}
