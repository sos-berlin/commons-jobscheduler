package com.sos.VirtualFileSystem.HTTP;

import java.io.InputStream;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsHTTPFile extends SOSVfsTransferFileBaseClass {

    public SOSVfsHTTPFile(final String path) {
        super(path);
    }

    @Override
    public boolean fileExists() {
        Long fs = objVFSHandler.getFileSize(fileName);
        return fs >= 0;
    }

    @Override
    public int read(byte[] buffer) {
        try {
            InputStream is = getFileInputStream();

            if (is == null) {
                throw new JobSchedulerException(SOSVfs_E_177.get());
            }
            return is.read(buffer);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("read", this.fileName), e);
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int len) {
        try {
            InputStream is = getFileInputStream();
            if (is == null) {
                throw new Exception(SOSVfs_E_177.get());
            }
            return is.read(buffer, offset, len);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("read", this.fileName), e);
        }
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        try {
            if (objOutputStream == null) {
                objOutputStream = objVFSHandler.getOutputStream(fileName);
            }
            if (objOutputStream == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            objOutputStream.write(buffer, offset, length);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("write", fileName), e);
        }
    }

    @Override
    public void write(final byte[] buffer) {
        try {
            if (objOutputStream == null) {
                objOutputStream = objVFSHandler.getOutputStream(fileName);
            }
            if (objOutputStream == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            objOutputStream.write(buffer);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("write", fileName), e);
        }
    }

    @Override
    public void closeInput() {
        super.closeInput();
        ((SOSVfsHTTP) objVFSHandler).resetLastInputStreamGetMethod();
    }

    @Override
    public void closeOutput() {
        try {
            if (objOutputStream != null) {
                objOutputStream.flush();
                objOutputStream.close();

                ((SOSVfsHTTP) objVFSHandler).put(fileName);
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        } finally {
            objOutputStream = null;
        }
    }
}
