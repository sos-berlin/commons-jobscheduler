/**
 *
 */
package com.sos.dialog.components;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.dialog.interfaces.ISOSPreferenceStore;

/** @author KB */
public class SOSPreferenceStore implements ISOSPreferenceStore {

    protected String strKey = "";
    protected String className = "";
    protected Shell shell = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSPreferenceStore.class);
    public static String gstrApplication = "SOS";
    public Preferences prefs = Preferences.userRoot().node(this.getClass().getName());

    protected int getInt(final String s, final int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
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

    private String normalizeKey(String key) {
        key = key.replaceFirst("^\\/*(.*)", "$1");
        key = key.replaceAll("\\/", "_");
        return key;
    }

    public SOSPreferenceStore(String instance) {
        className = normalizeKey(instance);
        if (className.isEmpty()) {
            className = normalizeKey(this.getClass().getName());
            strKey = className;
        } else {
            strKey = className;
        }
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
        if ("".equals(strKey)) {
            strKey = "context";
        }
        LOGGER.trace("key = " + "properties/" + strKey);
        return "properties/" + strKey;
    }

    public void saveProperty(final String pstrPropName, final String pstrPropValue) {
        prefs.node(getPropertyKey()).put(pstrPropName, pstrPropValue);
        LOGGER.trace(String.format("saveProperty %1$s = %2$s", strKey + "/" + pstrPropName, pstrPropValue));
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            throw new JobSchedulerException(e);
        }
    }

    public String getProperty(final String pstrPropName, final String pstrDefaultValue) {
        String strR = prefs.node(getPropertyKey()).get(pstrPropName, pstrDefaultValue);
        LOGGER.trace(String.format("getProperty %1$s = %2$s", strKey + "/" + pstrPropName, strR));
        return strR;
    }

    public String getProperty(final String pstrPropName) {
        String strR = prefs.node(getPropertyKey()).get(pstrPropName, "");
        LOGGER.trace(String.format("getProperty %1$s = %2$s", strKey + "/" + pstrPropName, strR));
        return strR;
    }

    @Override
    public String readPreferenceStore() {
        String strT = "";
        if (prefs != null && !gstrApplication.isEmpty()) {
            strT = prefs.get(gstrApplication, "");
        }
        return strT;
    }

    @Override
    public String writePreferenceStore(final String strT) {
        if (prefs != null && !gstrApplication.isEmpty()) {
            prefs.put(gstrApplication, strT);
        }
        return strT;
    }

}