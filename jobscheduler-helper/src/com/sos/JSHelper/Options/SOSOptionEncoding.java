/**
 * 
 */
package com.sos.JSHelper.Options;

/**
 * @author KB
 *
 */
public class SOSOptionEncoding extends SOSOptionStringValueList {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -4494527209385782642L;

	/**
	 * @param pobjParent
	 * @param pstrKey
	 * @param pstrDescription
	 * @param pstrValue
	 * @param pstrDefaultValue
	 * @param pflgIsMandatory
	 */
	public SOSOptionEncoding(final JSOptionsClass pobjParent, final String pstrKey, final String pstrDescription, final String pstrValue, final String pstrDefaultValue,
			final boolean pflgIsMandatory) {
		super(pobjParent, pstrKey, pstrDescription, pstrValue, pstrDefaultValue, pflgIsMandatory);
	}

}
