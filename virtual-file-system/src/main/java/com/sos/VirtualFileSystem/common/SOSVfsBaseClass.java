package com.sos.VirtualFileSystem.common;

import com.sos.VirtualFileSystem.Options.SOSBaseOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsBaseClass extends SOSVfsMessageCodes {

    private SOSBaseOptions options = null;
    private enuSourceOrTarget sourceOrTarget = enuSourceOrTarget.isUndefined;
    private boolean isLocked = false;
    private boolean loggedIn = false;
    private boolean connected = false;

    public static enum enuSourceOrTarget {
        isUndefined, isSource, isTarget
    }

    public SOSVfsBaseClass() {
        super("SOSVirtualFileSystem");
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean isLoggedin() {
        return loggedIn;
    }

    public boolean isSource() {
        if (sourceOrTarget.equals(enuSourceOrTarget.isSource)) {
            return true;
        }
        return false;
    }

    public boolean isTarget() {
        if (sourceOrTarget.equals(enuSourceOrTarget.isTarget)) {
            return true;
        }
        return false;
    }

    public void lock() {
        isLocked = true;
    }

    final public SOSBaseOptions getOptions() {
        return options;
    }

    public void getOptions(final SOSBaseOptions opt) {
        options = opt;
    }

    public void release() {
        isLocked = false;
    }

    public void setConnected(final boolean val) {
        connected = val;
    }

    public void setLogin(final boolean val) {
        isLocked = val;
    }

    public void setSource() {
        sourceOrTarget = enuSourceOrTarget.isSource;
    }

    public void setTarget() {
        sourceOrTarget = enuSourceOrTarget.isTarget;
    }

    final public enuSourceOrTarget sourceOrTarget() {
        return sourceOrTarget;
    }

}