/**
 *
 */
package com.sos.dialog.components;

import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import com.sos.dialog.interfaces.ISOSPreferenceStore;

/**
 * @author KB
 *
 */
public class SOSPreferenceStore  implements ISOSPreferenceStore {

	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused") private final Logger logger = Logger.getLogger(this.getClass());
	

	// TODO make class variable
	public Preferences	prefs = Preferences.userNodeForPackage(this.getClass());
	private String strPreferenceStoreKey = "JOE";
	/**
	 *
	 */

	public SOSPreferenceStore(final Class <?> c) {
		prefs = Preferences.userNodeForPackage(c);
	}

	
	public SOSPreferenceStore() {
	}
	@Override
	public void setPreferenceStoreKey(final String pstrKey) {
		strPreferenceStoreKey = pstrKey;
	}

	@Override
	public String getPreferenceStoreKey() {
		return strPreferenceStoreKey;
	}

	private String getPropertyKey() {
		return "properties/" + strPreferenceStoreKey;
	}

	public void saveProperty(final String pstrPropName, final String pstrPropValue) {
		prefs.node(getPropertyKey()).put(pstrPropName, pstrPropValue);
		logger.debug(String.format("saveProperty %1$s = %2$s", pstrPropName, pstrPropValue));
	}

	public String getProperty(final String pstrPropName) {
		String strR = prefs.node(getPropertyKey()).get(pstrPropName, "");
		logger.debug(String.format("getProperty %1$s = %2$s", pstrPropName, strR));
		return strR;
	}


}
