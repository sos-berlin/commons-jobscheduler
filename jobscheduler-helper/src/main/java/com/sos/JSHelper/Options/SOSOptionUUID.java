package com.sos.JSHelper.Options;

import java.util.UUID;

/**
* \class SOSOptionUUID 
* 
* \brief SOSOptionUUID - 
* 
* \details
*
* \section SOSOptionUUID.java_intro_sec Introduction
*
* \section SOSOptionUUID.java_samples Some Samples
*
* \code
*   .... code goes here ...
* \endcode
*
* <p style="text-align:center">
* <br />---------------------------------------------------------------------------
* <br /> APL/Software GmbH - Berlin
* <br />##### generated by ClaviusXPress (http://www.sos-berlin.com) #########
* <br />---------------------------------------------------------------------------
* </p>
* \author oh
* @version $Id$
* \see reference
*
* Created on 28.02.2012 01:17:21
 */
public class SOSOptionUUID extends SOSOptionString {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4666193500605817476L;
	@SuppressWarnings("unused")
	private final String		conClassName		= "SOSOptionUUID";

	/**
	 * \brief SOSOptionJdbcUrl
	 *
	 * \details
	 *
	 * @param pPobjParent
	 * @param pPstrKey
	 * @param pPstrDescription
	 * @param pPstrValue
	 * @param pPstrDefaultValue
	 * @param pPflgIsMandatory
	 */
	public SOSOptionUUID(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
			boolean pPflgIsMandatory) {
		super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
	}

	public SOSOptionUUID() {
		super(null, "", "", "", "", false);
	}

	public String createUUID() {
		return UUID.randomUUID().toString();
	}

	@Override
	public String Value() {
		if (isEmpty(strValue) == true) {
			this.Value(createUUID());
		}
		return strValue;
	}
}
