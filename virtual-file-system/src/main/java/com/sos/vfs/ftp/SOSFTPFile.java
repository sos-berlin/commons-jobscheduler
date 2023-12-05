package com.sos.vfs.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.SOSCommonFile;
import com.sos.vfs.common.SOSVFSFactory;
import com.sos.vfs.ftp.common.SOSFTPBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFTPFile extends SOSCommonFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFTPFile.class);
    private static final String CLASSNAME = SOSFTPFile.class.getSimpleName();
    private String fileName = EMPTY_STRING;

    public SOSFTPFile(final String path) {
        super(SOSVFSFactory.BUNDLE_NAME);
        fileName = path;
    }

    public SOSFTPFile(final FTPFile file) {
        super(SOSVFSFactory.BUNDLE_NAME);
        fileName = file.getName();
    }

    @Override
    public boolean fileExists() {
        boolean result = false;
        if (getProvider().getFileSize(fileName) >= 0) {
            result = true;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]fileExists=%s", fileName, result));
        }
        return result;
    }

    @Override
    public boolean directoryExists() {
        boolean result = getProvider().directoryExists(fileName);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]fileExists=%s", fileName, result));
        }
        return result;
    }

    @Override
    public boolean delete(boolean chekIsDirectory) {
        try {
            getProvider().delete(fileName, chekIsDirectory);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s]deleted", fileName));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException("[" + fileName + "]" + e.toString(), e);
        }
        return true;
    }

    @Override
    public InputStream getFileInputStream() {
        try {
            if (getInputStream() == null) {
                setInputStream(getProvider().getInputStream(fileName));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException("[" + fileName + "]" + e.toString(), e);
        }
        return getInputStream();
    }

    private OutputStream getFileOutputStream() {
        try {
            if (getOutputStream() == null) {
                setOutputStream(getProvider().getOutputStream(fileName, isModeAppend(), isModeRestart()));
                if (getOutputStream() == null) {
                    throw new JobSchedulerException(getProvider().getReplyString());
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException("[" + fileName + "]" + e.toString(), e);
        }
        return getOutputStream();
    }

    @Override
    public long getFileSize() {
        long result = -1;
        try {
            result = getProvider().getFileSize(fileName);
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException("[" + fileName + "]" + e.toString(), e);
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
    public boolean isDirectory() {
        return getProvider().isDirectory(fileName);
    }

    @Override
    public void rename(final String newPath) {
        getProvider().rename(fileName, newPath);
    }

    @Override
    public void closeInput() {
        try {
            if (getInputStream() != null) {
                try {
                    getInputStream().close();
                    setInputStream(null);
                    ((SOSFTPBaseClass) getProvider()).completePendingCommand();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    throw new JobSchedulerException(e);
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException("[" + fileName + "]" + e.toString(), e);
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
                ((SOSFTPBaseClass) getProvider()).completePendingCommand();
                if (getProvider().isNegativeCommandCompletion()) {
                    throw new JobSchedulerException(SOSVfs_E_175.params(fileName, getProvider().getReplyString()));
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException("[" + fileName + "]" + e.toString(), e);
        } finally {
            setOutputStream(null);
        }
    }

    @Override
    public int read(final byte[] buffer) {
        int bytes = 0;
        try {
            InputStream is = this.getFileInputStream();
            if (is != null) {
                bytes = is.read(buffer);
            }
        } catch (IOException e) {
            throw new JobSchedulerException("[" + fileName + "]" + e.toString(), e);
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
            throw new JobSchedulerException("[" + fileName + "]" + e.toString(), e);
        }
    }

    @Override
    public void write(final byte[] buffer) {
        try {
            this.getFileOutputStream().write(buffer);
        } catch (IOException e) {
            throw new JobSchedulerException("[" + fileName + "]" + e.toString(), e);
        }
    }

    @Override
    public long getModificationDateTime() {
        String dateTime = getProvider().getModificationDateTime(fileName);
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
            SOSFTP handler = (SOSFTP) getProvider();
            Date d = new Date(dateTime);
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            handler.getClient().setModificationTime(fileName, df.format(d));
            return dateTime;
        } catch (IOException e) {
            LOGGER.error("[" + fileName + "]" + e.toString(), e);
            return -1L;
        }
    }

}