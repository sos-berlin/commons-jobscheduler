/**
 * 
 */
package com.sos.JSHelper.Options;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author KB
 *
 */
public class SOSOptionVerbose extends SOSOptionInteger {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5484261268617623809L;

	/**
	 * @param pPobjParent
	 * @param pPstrKey
	 * @param pPstrDescription
	 * @param pPstrValue
	 * @param pPstrDefaultValue
	 * @param pPflgIsMandatory
	 */
	public SOSOptionVerbose(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
			boolean pPflgIsMandatory) {
		super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
	}

	public void initializeLog4jLevels () {
		int intVerbose = this.value();
		switch (intVerbose) {
			case -1:
				Logger.getRootLogger().setLevel(Level.ERROR);
				break;
			case 0:
			case 1:
				Logger.getRootLogger().setLevel(Level.INFO);
				break;
			case 9:
				Logger.getRootLogger().setLevel(Level.TRACE);
				logger.setLevel(Level.TRACE);
				logger.debug("set loglevel to TRACE due to option verbose = " + intVerbose);
				break;
			default:
				Logger.getRootLogger().setLevel(Level.DEBUG);
				logger.debug("set loglevel to DEBUG due to option verbose = " + intVerbose);
				break;
		}

	}
	
	@Override
	public void Value (final String pstrValue) {
		super.Value(pstrValue);
//		initializeLog4jLevels();
	}
}
