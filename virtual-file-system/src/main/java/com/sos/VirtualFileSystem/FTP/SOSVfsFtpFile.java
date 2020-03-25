package com.sos.VirtualFileSystem.FTP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSVfsCommonFile;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpFile extends SOSVfsCommonFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsFtpFile.class);
    private static final String CLASSNAME = SOSVfsFtpFile.class.getSimpleName();
    private String fileName = EMPTY_STRING;

    public SOSVfsFtpFile(final String path) {
        super(SOSVfsConstants.BUNDLE_NAME);
        fileName = path;
    }

    public SOSVfsFtpFile(final FTPFile file) {
        super(SOSVfsConstants.BUNDLE_NAME);
        fileName = file.getName();
    }

    @Override
    public boolean fileExists() {
        boolean result = false;
        if (getHandler().getFileSize(fileName) >= 0) {
            result = true;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]fileExists=%s", fileName, result));
        }
        return result;
    }

    @Override
    public boolean delete(boolean chekIsDirectory) {
        try {
            getHandler().delete(fileName, chekIsDirectory);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s]deleted", fileName));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::delete"), e);
        }
        return true;
    }

    @Override
    public InputStream getFileInputStream() {
        try {
            if (getInputStream() == null) {
                setInputStream(getHandler().getInputStream(fileName));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::getFileInputStream"), e);
        }
        return getInputStream();
    }

    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (getOutputStream() == null) {
                if (isModeAppend()) {
                    setOutputStream(((SOSVfsFtpBaseClass) getHandler()).getAppendFileStream(fileName));
                } else {
                    setOutputStream(getHandler().getOutputStream(fileName));
                }
                if (getOutputStream() == null) {
                    throw new JobSchedulerException(getHandler().getReplyString());
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::getFileOutputStream"), e);
        }
        return getOutputStream();
    }

    @Override
    public Integer getFilePermissions() throws Exception {
        return 0;
    }

    @Override
    public long getFileSize() {
        long result = -1;
        try {
            result = getHandler().getFileSize(fileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("getFileSize()"), e);
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]fileSize=%s", fileName, result));
        }
        return result;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getParentVfs() {
        return null;
    }

    @Override
    public ISOSVirtualFile getParentVfsFile() {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return getHandler().isDirectory(fileName);
    }

    @Override
    public boolean isEmptyFile() {
        return this.getFileSize() <= 0;
    }

    @Override
    public boolean notExists() {
        boolean result = false;
        try {
            result = !fileExists();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::notExists"), e);
        }
        return result;
    }

    @Override
    public void putFile(final File path) {
        notImplemented();
    }

    @Override
    public void putFile(final String path) {
        notImplemented();
    }

    @Override
    public void rename(final String newPath) {
        getHandler().rename(fileName, newPath);
    }

    @Override
    public void setFilePermissions(final Integer val) {
        notImplemented();
    }

    @Override
    public String getModificationTime() {
        try {
            return getHandler().getModificationTime(fileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::getModificationTime"), e);
        }
    }

    @Override
    public void close() {
        if (getOutputStream() != null) {
            this.closeOutput();
            setOutputStream(null);
        } else {
            if (getInputStream() != null) {
                this.closeInput();
                setInputStream(null);
            }
        }
    }

    @Override
    public void closeInput() {
        try {
            if (getInputStream() != null) {
                try {
                    getInputStream().close();
                    setInputStream(null);
                    ((SOSVfsFtpBaseClass) getHandler()).completePendingCommand();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    throw new JobSchedulerException(e);
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::closeInput"), e);
        } finally {
            setInputStream(null);
        }
    }

    @Override
    public void closeOutput() {
        try {
            if (getOutputStream() != null) {
                getOutputStream().flush();
                getOutputStream().close();
                ((SOSVfsFtpBaseClass) getHandler()).completePendingCommand();
                if (getHandler().isNegativeCommandCompletion()) {
                    throw new JobSchedulerException(SOSVfs_E_175.params(fileName, getHandler().getReplyString()));
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::closeOutput"), e);
        } finally {
            setOutputStream(null);
        }
    }

    @Override
    public void flush() {
        try {
            if (getOutputStream() != null) {
                getOutputStream().flush();
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::flush"), e);
        }
    }

    @Override
    public int read(final byte[] buffer) {
        int bytes = 0;
        try {
            InputStream is = this.getFileInputStream();
            if (is != null) {
                bytes = is.read(buffer);
            } else {
                bytes = getHandler().read(buffer);
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::read"), e);
        }
        return bytes;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) {
        int bytes = 0;
        try {
            InputStream is = this.getFileInputStream();
            if (is != null) {
                bytes = is.read(buffer, offset, length);
            } else {
                bytes = getHandler().read(buffer, offset, length);
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::read"), e);
        }
        return bytes;
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        try {
            if (this.getFileOutputStream() == null) {
                throw new JobSchedulerException(SOSVfs_E_176.params(CLASSNAME + "::write", fileName));
            }
            this.getFileOutputStream().write(buffer, offset, length);
        } catch (IOException e) {
            throw new JobSchedulerException(String.format("%1$s failed for file %2$s", CLASSNAME + "::write", fileName), e);
        }
    }

    @Override
    public void write(final byte[] buffer) {
        try {
            this.getFileOutputStream().write(buffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.get(CLASSNAME + "::write"), e);
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile file) throws Exception {
        notImplemented();
    }

    @Override
    public long getModificationDateTime() {
        String dateTime = getHandler().getModificationTime(fileName);
        long result = -1;
        if (dateTime != null) {
            if (dateTime.startsWith("213 ")) {
                dateTime = dateTime.substring(3);
            }
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                result = df.parse(dateTime.trim()).getTime();
            } catch (Exception e) {
                result = -1L;
            }
        } else {
            result = -1L;
        }
        return result;
    }

    @Override
    public long setModificationDateTime(final long dateTime) {
        try {
            SOSVfsFtp handler = (SOSVfsFtp) getHandler();
            Date d = new Date(dateTime);
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            handler.getClient().setModificationTime(fileName, df.format(d));
            return dateTime;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return -1L;
        }
    }

}