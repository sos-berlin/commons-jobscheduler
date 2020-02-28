package com.sos.VirtualFileSystem.WebDAV;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsWebDAVFile extends SOSVfsTransferFileBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsWebDAVFile.class);
    private String fileName = null;

    public SOSVfsWebDAVFile(final String path) {
        super(path);
        fileName = path;
    }

    @Override
    public int read(final byte[] buffer) {
        try {
            InputStream is = this.getFileInputStream();
            if (is == null) {
                throw new JobSchedulerException(SOSVfs_E_177.get());
            }
            return is.read(buffer);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("read", fileName), e);
        }
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) {
        try {
            InputStream is = this.getFileInputStream();
            if (is == null) {
                throw new Exception(SOSVfs_E_177.get());
            }
            return is.read(buffer, offset, length);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("read", fileName), e);
        }
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        try {
            if (getOutputStream() == null) {
                setOutputStream(getHandler().getOutputStream(fileName));
            }
            if (getOutputStream() == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            getOutputStream().write(buffer, offset, length);
            ((SOSVfsWebDAVOutputStream) getOutputStream()).put();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("write", fileName), e);
        }
    }

    @Override
    public void write(final byte[] bteBuffer) {
        try {
            if (getOutputStream() == null) {
                setOutputStream(getHandler().getOutputStream(fileName));
            }
            if (getOutputStream() == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            getOutputStream().write(bteBuffer);
            ((SOSVfsWebDAVOutputStream) getOutputStream()).put();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("write", fileName), e);
        }
    }

    @Override
    public long setModificationDateTime(final long timeStamp) {
        // not supported
        return -1L;
    }

    @Override
    public long getModificationDateTime() {
        try {
            SOSVfsWebDAV handler = (SOSVfsWebDAV) getHandler();
            return handler.getModificationTimeStamp(fileName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return -1L;
        }
    }

}