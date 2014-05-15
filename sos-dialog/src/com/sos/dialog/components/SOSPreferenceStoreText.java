/**
 *
 */
package com.sos.dialog.components;

import java.util.prefs.Preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.sos.dialog.interfaces.ISOSPreferenceStore;

/**
 * @author KB
 *
 */
public class SOSPreferenceStoreText extends Text implements ISOSPreferenceStore {

	// TODO make class variable
	public final Preferences	prefs = Preferences.userNodeForPackage(this.getClass());
	private String strPreferenceStoreKey = "";
	/**
	 *
	 */
	public SOSPreferenceStoreText(final Composite pobjComposite, final int arg1) {
		super(pobjComposite, arg1);
	}

	@Override
	public void setPreferenceStoreKey(final String pstrKey) {
		strPreferenceStoreKey = pstrKey;
	}

	@Override
	public String getPreferenceStoreKey() {
		return strPreferenceStoreKey;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
