package com.sos.VirtualFileSystem.Interfaces;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass.enuSourceOrTarget;

public interface ISOSVFSHandler extends ISOSShell, ISOSConnection, ISOSSession {

    public void doPostLoginOperations();

    public ISOSVFSHandler getHandler();

    public boolean isConnected();

    public boolean isLocked();

    public boolean isLoggedin();

    public boolean isSource();

    public boolean isTarget();

    public boolean isSimulateShell();

    public void lock();

    public SOSFTPOptions getOptions();

    public void getOptions(final SOSFTPOptions options);

    public void release();

    public void setConnected(final boolean isConnected);

    public void setJSJobUtilites(JSJobUtilities utilities);

    public void setLogin(final boolean isLogin);

    public void setSource();

    public void setTarget();

    public enuSourceOrTarget sourceOrTarget();

    public void setSimulateShell(boolean simulateShell);

}