package com.sos.VirtualFileSystem.Interfaces;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass.enuSourceOrTarget;

/** @author KB */
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

    public SOSFTPOptions Options();

    public void Options(final SOSFTPOptions pobjOptions);

    public void release();

    public void setConnected(final boolean pflgIsConnected);

    public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities);

    public void setLogin(final boolean pflgIsLogin);

    public void setSource();

    public void setTarget();

    public enuSourceOrTarget SourceOrTarget();

    public void setSimulateShell(boolean simulateShell);

}