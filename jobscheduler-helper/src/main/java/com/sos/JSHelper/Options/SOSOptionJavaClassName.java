/**
 * 
 */
package com.sos.JSHelper.Options;

/** @author KB */
public class SOSOptionJavaClassName extends SOSOptionString {

    /**
	 * 
	 */
    private static final long serialVersionUID = 5778274891766632381L;

    /** @param pPobjParent
     * @param pPstrKey
     * @param pPstrDescription
     * @param pPstrValue
     * @param pPstrDefaultValue
     * @param pPflgIsMandatory */
    public SOSOptionJavaClassName(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

}
