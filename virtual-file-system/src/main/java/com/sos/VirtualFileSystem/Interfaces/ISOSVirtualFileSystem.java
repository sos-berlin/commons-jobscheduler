package com.sos.VirtualFileSystem.Interfaces;

import java.io.IOException;

import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;

/** @author KB */
public interface ISOSVirtualFileSystem {

    public ISOSConnection getConnection();

    public ISOSSession getSession();

    public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) throws IOException;

    public boolean rmdir(final SOSFolderName pobjFolderName) throws IOException;

    public SOSFileList dir(SOSFolderName pobjFolderName);

    public SOSFileList dir(String pathname, int flag);

}
