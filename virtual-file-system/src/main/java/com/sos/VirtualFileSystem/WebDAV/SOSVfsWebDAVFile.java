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
    private String strFileName = null;

    public SOSVfsWebDAVFile(final String pstrFileName) {
        super(pstrFileName);
        strFileName = pstrFileName;
    }

    @Override
    public int read(final byte[] bteBuffer) {
        try {
            InputStream is = this.getFileInputStream();
            if (is == null) {
                throw new JobSchedulerException(SOSVfs_E_177.get());
            }
            return is.read(bteBuffer);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("read", fileName), e);
        }
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        try {
            InputStream is = this.getFileInputStream();
            if (is == null) {
                throw new Exception(SOSVfs_E_177.get());
            }
            return is.read(bteBuffer, intOffset, intLength);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("read", fileName), e);
        }
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        try {
            if (objOutputStream == null) {
                objOutputStream = objVFSHandler.getOutputStream(strFileName);
            }
            if (objOutputStream == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            objOutputStream.write(bteBuffer, intOffset, intLength);
            ((SOSVfsWebDAVOutputStream) objOutputStream).put();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("write", fileName), e);
        }
    }

    @Override
    public void write(final byte[] bteBuffer) {
        try {
            if (objOutputStream == null) {
                objOutputStream = objVFSHandler.getOutputStream(strFileName);
            }
            if (objOutputStream == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            objOutputStream.write(bteBuffer);
            ((SOSVfsWebDAVOutputStream) objOutputStream).put();
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
            SOSVfsWebDAV handler = (SOSVfsWebDAV) objVFSHandler;
            return handler.getModificationTimeStamp(fileName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return -1L;
        }
    }

}