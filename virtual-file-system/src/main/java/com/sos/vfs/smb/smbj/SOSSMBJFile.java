package com.sos.vfs.smb.smbj;

import java.io.InputStream;
import java.io.OutputStream;

import com.hierynomus.smbj.share.File;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.smb.common.SOSSMBFile;

public class SOSSMBJFile extends SOSSMBFile {

    private File smbFile;

    public SOSSMBJFile(String path) {
        super(path);
    }

    @Override
    public InputStream getFileInputStream() {
        try {
            if (getInputStream() == null) {
                fileName = super.adjustRelativePathName(fileName);
                SOSSMBJ handler = (SOSSMBJ) getProvider();
                smbFile = handler.openFile2Read(fileName);
                if (smbFile == null) {
                    throw new Exception("openFile2Read failed");
                } else {
                    setInputStream(smbFile.getInputStream());
                }
            }
        } catch (Throwable e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileInputStream()", fileName), e);
        }
        return getInputStream();
    }

    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (getOutputStream() == null) {
                fileName = super.adjustRelativePathName(fileName);
                SOSSMBJ handler = (SOSSMBJ) getProvider();
                smbFile = handler.openFile2Write(fileName, isModeAppend());
                if (smbFile == null) {
                    throw new Exception("openFile2Write failed");
                } else {
                    setOutputStream(smbFile.getOutputStream(isModeAppend()));
                }
            }
        } catch (Throwable e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileOutputStream()", fileName), e);
        }
        return getOutputStream();
    }

    @Override
    public void closeInput() {
        super.closeInput();
        closeSmbFile();
    }

    @Override
    public void closeOutput() {
        super.closeOutput();
        closeSmbFile();
    }

    private void closeSmbFile() {
        if (smbFile != null) {
            try {
                smbFile.close();
            } catch (Throwable e) {
            }
        }
    }
}
