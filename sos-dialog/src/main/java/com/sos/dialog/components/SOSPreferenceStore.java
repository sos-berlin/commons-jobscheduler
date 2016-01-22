/**
 *
 */
package com.sos.dialog.components;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.dialog.interfaces.ISOSPreferenceStore;

/**
 * @author KB
 *
 */
public class SOSPreferenceStore implements ISOSPreferenceStore {

	@SuppressWarnings("unused")
	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused")
	private final Logger		logger			= Logger.getLogger(this.getClass());

	// TODO make class variable
	public Preferences			prefs			= Preferences.userRoot().node(this.getClass().getName());
	public static String		gstrApplication	= "SOS";
	protected String			strKey			= "";
	protected String			className		= "";
	protected Shell				shell			= null;
	private static final int	MAX_KEY_LENGTH	= Preferences.MAX_KEY_LENGTH;

	// Get maximum value length
	private final int			valueMax		= Preferences.MAX_VALUE_LENGTH;

	// Get maximum length of byte array values
	private final int			bytesMax		= Preferences.MAX_VALUE_LENGTH * 3 / 4;

	/**
	 *
	 */

	protected int getInt(final String s, final int def) {
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return def;
		}
	}

	public SOSPreferenceStore(final Class<?> c) {
		prefs = Preferences.userNodeForPackage(c);
		strKey = c.getName();
		className = strKey;
	}

	public SOSPreferenceStore() {
		className = this.getClass().getName();
		strKey = className;
	}

	private String normalizeKey(String key){
		key = key.replaceFirst("^\\/*(.*)","$1");
		key = key.replaceAll("\\/", "_");
		return key;
	}

	public SOSPreferenceStore(String instance) {
		className = instance;
		strKey = normalizeKey(instance);
	}	
	
	public void setKey(final String pstrKey) {
		strKey = gstrApplication + "/" + className + "/" + normalizeKey(pstrKey);
	}

	@Override
	public void setPreferenceStoreKey(final String pstrKey) {
		gstrApplication = pstrKey;
	}

	@Override
	public String getPreferenceStoreKey() {
		return gstrApplication;
	}

	public String getPropertyKey() {

		if (strKey.equals("")){
			strKey = "context";
		}
		String strT = "properties/" + strKey;

		logger.trace("key = " + strT);
		return strT;
	}

	public void saveProperty(final String pstrPropName, final String pstrPropValue) {
		prefs.node(getPropertyKey()).put(pstrPropName, pstrPropValue);
		logger.trace(String.format("saveProperty %1$s = %2$s", strKey + "/" + pstrPropName, pstrPropValue));
		try {
			prefs.flush();
		}
		catch (BackingStoreException e) {
			throw new JobSchedulerException(e);
		}
	}

	public String getProperty(final String pstrPropName, final String pstrDefaultValue) {
		String strR = prefs.node(getPropertyKey()).get(pstrPropName, pstrDefaultValue);
		logger.trace(String.format("getProperty %1$s = %2$s", strKey + "/" + pstrPropName, strR));
		return strR;
	}

	public String getProperty(final String pstrPropName) {
		String strR = prefs.node(getPropertyKey()).get(pstrPropName, "");
		logger.trace(String.format("getProperty %1$s = %2$s", strKey + "/" + pstrPropName, strR));
		return strR;
	}

	@Override
	public String readPreferenceStore() {
		String strT = "";
		if (prefs != null && gstrApplication.length() > 0) {
			strT = prefs.get(gstrApplication, "");
		}
		return strT;
	}

	@Override
	public String writePreferenceStore(final String strT) {
		if (prefs != null && gstrApplication.length() > 0) {
			prefs.put(gstrApplication, strT);
		}
		return strT;
	}

}
