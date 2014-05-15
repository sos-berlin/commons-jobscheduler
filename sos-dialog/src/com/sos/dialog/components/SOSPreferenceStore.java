/**
 *
 */
package com.sos.dialog.components;

import java.util.prefs.Preferences;

import com.sos.dialog.interfaces.ISOSPreferenceStore;

/**
 * @author KB
 *
 */
public class SOSPreferenceStore  implements ISOSPreferenceStore {

	// TODO make class variable
	public final Preferences	prefs = Preferences.userNodeForPackage(this.getClass());
	private String strPreferenceStoreKey = "";
	/**
	 *
	 */
	public SOSPreferenceStore() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void setPreferenceStoreKey(final String pstrKey) {
		strPreferenceStoreKey = pstrKey;
	}

	@Override
	public String getPreferenceStoreKey() {
		return strPreferenceStoreKey;
	}

}
