package com.sos.VirtualFileSystem.Interfaces;

import java.io.IOException;

import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;

public interface ISOSVirtualFolder {

    public SOSFileList dir(final SOSFolderName folderName);

    public SOSFileList dir(String path, int flag);

    public SOSFileList dir();

    public ISOSVirtualFolder mkdir(final SOSFolderName folderName) throws IOException;

    public boolean rmdir(final SOSFolderName folderName) throws IOException;

}
