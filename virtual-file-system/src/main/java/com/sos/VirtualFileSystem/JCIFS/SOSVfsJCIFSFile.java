package com.sos.VirtualFileSystem.JCIFS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsJCIFSFile extends SOSVfsTransferFileBaseClass {

    private static final Logger LOGGER = Logger.getLogger(SOSVfsJCIFSFile.class);

    public SOSVfsJCIFSFile(final String pstrFileName) {
        super(pstrFileName);
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
            raiseException(e, SOSVfs_E_173.params("read", fileName));
            return 0;
        }
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        try {
            InputStream is = getFileInputStream();
            if (is == null) {
                throw new Exception(SOSVfs_E_177.get());
            }
            return is.read(bteBuffer, intOffset, intLength);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_173.params("read", fileName));
            return 0;
        }
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        try {
            OutputStream os = getFileOutputStream();
            if (os == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            os.write(bteBuffer, intOffset, intLength);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_173.params("write", fileName));
        }
    }

    @Override
    public void write(final byte[] buffer) {
        try {
            getFileOutputStream().write(buffer);
        } catch (IOException e) {
            raiseException(e, SOSVfs_E_134.params("write()"));
        }
    }

    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (objOutputStream == null) {
                fileName = super.adjustRelativePathName(fileName);
                SOSVfsJCIFS objJ = (SOSVfsJCIFS) objVFSHandler;
                objOutputStream = objJ.getOutputStream(fileName);
                if (objOutputStream == null) {
                    objVFSHandler.openOutputFile(fileName);
                }
            }
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_158.params("getFileOutputStream()", fileName));
        }
        return objOutputStream;
    }

    @Override
    public long setModificationDateTime(final long timeStamp) {
        try {
            SOSVfsJCIFS handler = (SOSVfsJCIFS) objVFSHandler;
            handler.setModificationTimeStamp(fileName, timeStamp);
            return timeStamp;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return -1L;
        }
    }

    @Override
    public long getModificationDateTime() {
        try {
            SOSVfsJCIFS handler = (SOSVfsJCIFS) objVFSHandler;
            return handler.getModificationTimeStamp(fileName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return -1L;
        }
    }
}