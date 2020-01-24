package com.sos.VirtualFileSystem.common;

import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsBaseClass extends SOSVfsMessageCodes {

    protected boolean flgIsLocked = false;
    protected boolean flgLoggedIn = false;
    protected boolean flgConnected = false;
    protected SOSFTPOptions objOptions = null;
    protected SOSFileEntries sosFileEntries = null;
    private enuSourceOrTarget intSourceOrTarget = enuSourceOrTarget.isUndefined;

    public static enum enuSourceOrTarget {
        isUndefined, isSource, isTarget
    }

    public SOSVfsBaseClass() {
        super("SOSVirtualFileSystem");
        sosFileEntries = new SOSFileEntries();
    }

    public boolean isConnected() {
        return flgConnected;
    }

    public boolean isLocked() {
        return flgIsLocked;
    }

    public boolean isLoggedin() {
        return flgLoggedIn;
    }

    public boolean isSource() {
        if (intSourceOrTarget == enuSourceOrTarget.isSource) {
            return true;
        }
        return false;
    }

    public boolean isTarget() {
        if (intSourceOrTarget == enuSourceOrTarget.isTarget) {
            return true;
        }
        return false;
    }

    public void lock() {
        flgIsLocked = true;
    }

    final public SOSFTPOptions getOptions() {
        return objOptions;
    }

    public void getOptions(final SOSFTPOptions pobjOptions) {
        objOptions = pobjOptions;
    }

    public void release() {
        flgIsLocked = false;
    }

    public void setConnected(final boolean pflgIsConnected) {
        flgConnected = pflgIsConnected;
    }

    public void setLogin(final boolean pflgIsLogin) {
        flgIsLocked = pflgIsLogin;
    }

    public void setSource() {
        intSourceOrTarget = enuSourceOrTarget.isSource;
    }

    public void setTarget() {
        intSourceOrTarget = enuSourceOrTarget.isTarget;
    }

    final public enuSourceOrTarget sourceOrTarget() {
        return intSourceOrTarget;
    }

}