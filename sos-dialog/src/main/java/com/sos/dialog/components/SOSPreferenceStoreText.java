/**
 *
 */
package com.sos.dialog.components;

import java.util.prefs.Preferences;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.sos.dialog.classes.SOSTextBox;
import com.sos.dialog.interfaces.ISOSPreferenceStore;

/** @author KB */
public class SOSPreferenceStoreText extends SOSTextBox implements ISOSPreferenceStore {

    // TODO make class variable
    public Preferences prefs = Preferences.userRoot().node("SOSPreferenceStore");
    protected String strPreferenceStoreKey = "";

    class theModifyListener implements ModifyListener {

        @Override
        public void modifyText(final ModifyEvent e) {
            String strT = getText();
            if (strT.trim().length() > 0) {
                writePreferenceStore(getText());
            }
        }
    }

    private final ModifyListener objModifyListener = new theModifyListener();

    /**
	 *
	 */
    public SOSPreferenceStoreText(final Composite pobjComposite, final int arg1) {
        super(pobjComposite, arg1);
        this.addModifyListener(objModifyListener);
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

    @Override
    public String readPreferenceStore() {
        String strT = "";
        if (prefs != null && strPreferenceStoreKey.length() > 0) {
            strT = prefs.get(strPreferenceStoreKey, "");
        }
        return strT;
    }

    public void initialize() {
        String strT = readPreferenceStore();
        setText(strT);
    }

    @Override
    public String writePreferenceStore(final String strT) {
        if (prefs != null && strPreferenceStoreKey.length() > 0) {
            prefs.put(strPreferenceStoreKey, strT);
        }
        return strT;
    }

    @Override
    public void dispose() {
        this.removeModifyListener(objModifyListener);
        prefs = null;
    }
}
