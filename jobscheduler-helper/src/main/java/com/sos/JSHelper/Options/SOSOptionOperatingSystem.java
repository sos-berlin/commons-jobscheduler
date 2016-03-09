/**
 * 
 */
package com.sos.JSHelper.Options;

/** @author KB */
public class SOSOptionOperatingSystem extends SOSOptionStringValueList {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3026292830546635361L;

    /** @param pPobjParent
     * @param pPstrKey
     * @param pPstrDescription
     * @param pPstrValue
     * @param pPstrDefaultValue
     * @param pPflgIsMandatory */
    public SOSOptionOperatingSystem(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        // TODO Auto-generated constructor stub
    }

    /** @param pobjParent
     * @param pstrIndexedKey
     * @param pstrDescription
     * @param pstrDefaultValue
     * @param pflgIsMandatory */
    public SOSOptionOperatingSystem(final JSOptionsClass pobjParent, final String pstrIndexedKey, final String pstrDescription, final String pstrDefaultValue,
            final boolean pflgIsMandatory) {
        super(pobjParent, pstrIndexedKey, pstrDescription, pstrDefaultValue, pflgIsMandatory);
        // TODO Auto-generated constructor stub
    }

}
