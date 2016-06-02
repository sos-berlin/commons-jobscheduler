package com.sos.JSHelper.Options;

import org.apache.log4j.Logger;

/** \class SOSOptionLocale
 * 
 * \brief SOSOptionLocale -
 * 
 * \details
 *
 * \section SOSOptionLocale.java_intro_sec Introduction
 *
 * \section SOSOptionLocale.java_samples Some Samples
 *
 * \code .... code goes here ... \endcode
 *
 * <p style="text-align:center">
 * <br />
 * --------------------------------------------------------------------------- <br />
 * APL/Software GmbH - Berlin <br />
 * ##### generated by ClaviusXPress (http://www.sos-berlin.com) ######### <br />
 * ---------------------------------------------------------------------------
 * </p>
 * \author KB \version 14.05.2011 \see reference
 *
 * Created on 14.05.2011 18:04:43 */

/** @author KB */
public class SOSOptionLocale extends SOSOptionString {

    /**
	 * 
	 */
    private static final long serialVersionUID = 333232591167035226L;

    @SuppressWarnings("unused")
    private final String conSVNVersion = "$Id$";

    private final String conClassName = "SOSOptionLocale";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SOSOptionLocale.class);

    public static java.util.Locale i18nLocale = java.util.Locale.getDefault();

    /** \brief SOSOptionLocale
     *
     * \details
     *
     * @param pPobjParent
     * @param pPstrKey
     * @param pPstrDescription
     * @param pPstrValue
     * @param pPstrDefaultValue
     * @param pPflgIsMandatory */
    public SOSOptionLocale(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
            boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        // TODO Auto-generated constructor stub
    }

    public java.util.Locale getI18NLocale() {
        return new java.util.Locale(this.getValue());
    }

    public void setLocale(final java.util.Locale pobjLocale) {
        this.strValue = pobjLocale.toString();
        SOSOptionLocale.i18nLocale = pobjLocale;
    }

    /** \brief Value
     * 
     * \details
     *
     * \return
     *
     * @param pstrLocale */
    @Override
    public void setValue(final String pstrLocale) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::Value";

        if (this.strValue.equalsIgnoreCase(pstrLocale) == false) {
            this.strValue = pstrLocale;
            SOSOptionLocale.i18nLocale = this.getI18NLocale();
        }

    } // private void Value

}
