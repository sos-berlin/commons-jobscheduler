package com.sos.VirtualFileSystem.Interfaces;

import java.io.IOException;
import java.util.List;

import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.common.SOSFileEntry;

public interface ISOSVirtualFileSystem {

    public ISOSConnection getConnection();

    public ISOSSession getSession();

    public ISOSVirtualFolder mkdir(final SOSFolderName folderName) throws IOException;

    public boolean rmdir(final SOSFolderName folderName) throws IOException;

    public List<SOSFileEntry> dir(SOSFolderName folderName);

    public List<SOSFileEntry> dir(String path, int flag);

}
