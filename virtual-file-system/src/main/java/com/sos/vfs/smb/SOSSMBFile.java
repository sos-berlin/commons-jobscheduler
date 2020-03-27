package com.sos.vfs.smb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.SOSCommonTransferFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSMBFile extends SOSCommonTransferFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSMBFile.class);

    public SOSSMBFile(final String path) {
        super(path);
    }

    @Override
    public int read(final byte[] buffer) {
        try {
            InputStream is = getFileInputStream();
            if (is == null) {
                throw new JobSchedulerException(SOSVfs_E_177.get());
            }
            return is.read(buffer);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("read", fileName), e);
        }
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        try {
            OutputStream os = getFileOutputStream();
            if (os == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            os.write(buffer, offset, length);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("write", fileName), e);
        }
    }

    @Override
    public void write(final byte[] buffer) {
        try {
            getFileOutputStream().write(buffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("write()"), e);
        }
    }

    private OutputStream getFileOutputStream() {
        try {
            if (getOutputStream() == null) {
                fileName = super.adjustRelativePathName(fileName);
                SOSSMB handler = (SOSSMB) getHandler();
                setOutputStream(handler.getOutputStream(fileName, isModeAppend(), isModeRestart()));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileOutputStream()", fileName), e);
        }
        return getOutputStream();
    }

    @Override
    public long setModificationDateTime(final long timeStamp) {
        try {
            SOSSMB handler = (SOSSMB) getHandler();
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
            SOSSMB handler = (SOSSMB) getHandler();
            return handler.getModificationTimeStamp(fileName);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return -1L;
        }
    }
}