package com.sos.VirtualFileSystem.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSTransferHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsTransferFileBaseClass extends SOSVfsCommonFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsTransferFileBaseClass.class);
    protected String fileName = EMPTY_STRING;

    public SOSVfsTransferFileBaseClass() {
        super("SOSVirtualFileSystem");
    }

    public SOSVfsTransferFileBaseClass(final String path) {
        this();
        fileName = adjustFileSeparator(path);
    }

    @Override
    public boolean fileExists() {
        boolean result = false;
        if (getHandler().getFileSize(fileName) >= 0) {
            result = true;
        }
        LOGGER.debug(String.format("[%s]%s", fileName, result));
        return result;
    }

    @Override
    public boolean delete(boolean checkIsDirectory) {
        try {
            getHandler().delete(fileName, checkIsDirectory);
        } catch (Exception e) {
            SOSVfs_E_158.get();
            throw new JobSchedulerException(SOSVfs_E_158.params("delete()", fileName), e);
        }
        return true;
    }

    @Override
    public InputStream getFileInputStream() {
        try {
            if (getInputStream() == null) {
                fileName = adjustRelativePathName(fileName);
                setInputStream(getHandler().getInputStream(fileName));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileInputStream()", fileName), e);
        }
        return getInputStream();
    }

    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (getOutputStream() == null) {
                fileName = adjustRelativePathName(fileName);
                setOutputStream(getHandler().getOutputStream(fileName, false, false));

            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileOutputStream()", fileName), e);
        }
        return getOutputStream();
    }

    protected String adjustRelativePathName(final String path) {
        return path.replaceAll("\\\\", "/");
    }

    @Override
    public Integer getFilePermissions() throws Exception {
        return 0;
    }

    @Override
    public long getFileSize() {
        long lngFileSize = -1;
        try {
            lngFileSize = getHandler().getFileSize(fileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("getFileSize()"), e);
        }
        return lngFileSize;
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
            result = !this.fileExists();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("notExists()"), e);
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
    public void setHandler(final ISOSTransferHandler handler) {
        super.setHandler(handler);
    }

    @Override
    public String getModificationTime() {
        String strT = "";
        try {
            strT = getHandler().getModificationTime(fileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("getModificationTime()"), e);
        }
        return strT;
    }

    @Override
    public void close() {
        this.closeInput();
        this.closeOutput();
    }

    @Override
    public void closeInput() {
        try {
            if (getInputStream() != null) {
                getInputStream().close();
                setInputStream(null);
            }
        } catch (Exception ex) {
            //
        }
    }

    @Override
    public void closeOutput() {
        try {
            if (getOutputStream() != null) {
                getOutputStream().flush();
                getOutputStream().close();
                setOutputStream(null);
            }
        } catch (Exception ex) {
            //
        }
    }

    @Override
    public void flush() {
        try {
            this.getFileOutputStream().flush();
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("flush()"), e);
        }
    }

    @Override
    public int read(final byte[] buffer) {
        return 0;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) {
        return 0;
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        //
    }

    @Override
    public void write(final byte[] buffer) {
        notImplemented();
        try {
            this.getFileOutputStream().write(buffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("write()"), e);
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile file) throws Exception {
        notImplemented();
    }

    @Override
    public long setModificationDateTime(final long dateTime) {
        return 0;
    }

    @Override
    public long getModificationDateTime() {
        return 0;
    }

}