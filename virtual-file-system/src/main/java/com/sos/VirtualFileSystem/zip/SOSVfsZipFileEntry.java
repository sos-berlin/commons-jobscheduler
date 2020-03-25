package com.sos.VirtualFileSystem.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.DataElements.JSDataElementDateTime;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSTransferHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSVfsCommonFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsZipFileEntry extends SOSVfsCommonFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsZipFileEntry.class);
    private static final String CLASSNAME = "SOSVfsZipFileEntry";
    private ZipEntry zipEntry = null;

    public SOSVfsZipFileEntry(final String path) {
        super();
    }

    @Override
    public boolean fileExists() throws Exception {
        return true;
    }

    @Override
    public boolean delete(boolean checkIsDirectory) {
        notImplemented();
        return true;
    }

    @Override
    public void deleteFile() {
        this.delete(false);
    }

    @Override
    public InputStream getFileInputStream() {
        String entryName = "";
        try {
            if (getInputStream() == null) {
                if (getHandler() == null) {
                    throw new JobSchedulerException("objVFSHandler == null");
                }
                if (zipEntry == null) {
                    throw new JobSchedulerException("objZipEntry == null");
                }
                entryName = zipEntry.getName();
                setInputStream(getHandler().getInputStream(zipEntry.getName()));
                LOGGER.debug(SOSVfs_D_207.params(entryName));
            }
        } catch (Exception e) {
            String msg = SOSVfs_E_134.params(CLASSNAME + "::getFileInputStream");
            LOGGER.error(msg, e);
            throw new JobSchedulerException(msg, e);
        }
        return getInputStream();
    }

    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (getOutputStream() == null) {
                setOutputStream(getHandler().getOutputStream(zipEntry.getName()));
            }
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
        return zipEntry.getSize();
    }

    @Override
    public String getModificationTime() {
        Date date = new Date(zipEntry.getTime());
        return new JSDataElementDateTime(date).getFormattedValue();
    }

    @Override
    public String getName() {
        return zipEntry.getName();
    }

    @Override
    public String getParentVfs() {
        notImplemented();
        return zipEntry.getName();
    }

    @Override
    public ISOSVirtualFile getParentVfsFile() {
        notImplemented();
        return null;
    }

    @Override
    public boolean isDirectory() {
        if (zipEntry != null) {
            return zipEntry.isDirectory();
        } else {
            return true;
        }
    }

    @Override
    public boolean isEmptyFile() {
        return this.getFileSize() <= 0;
    }

    @Override
    public boolean notExists() {
        return false;
    }

    @Override
    public void putFile(final File file) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void putFile(final String path) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void rename(final String newPath) {
        notImplemented();
    }

    @Override
    public void setFilePermissions(final Integer val) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void setHandler(final ISOSTransferHandler val) {
        super.setHandler(val);
    }

    @Override
    public void close() {
        if (getOutputStream() != null) {
            this.closeOutput();
        } else {
            if (getInputStream() != null) {
                this.closeInput();
            }
        }
    }

    @Override
    public void closeInput() {
        try {
            if (getInputStream() != null) {
                getInputStream().close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::closeInput"), e);
        } finally {
            setInputStream(null);
        }
    }

    @Override
    public void closeOutput() {
        try {
            if (getEntryOutputStream() == null) {
                return;
            }
            getEntryOutputStream().flush();
            getEntryOutputStream().closeEntry();
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::closeOutput"), e);
        } finally {
            setOutputStream(null);
        }
    }

    @Override
    public void flush() {
        try {
            this.getFileOutputStream().flush();
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::flush"), e);
        }
    }

    @Override
    public int read(final byte[] buffer) {
        try {
            return this.getFileInputStream().read(buffer);
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::read"), e);
        }
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) {
        try {
            return this.getFileInputStream().read(buffer, offset, length);
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::read"), e);
        }
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        try {
            this.getFileOutputStream().write(buffer, offset, length);
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::write"), e);
        }
    }

    @Override
    public void write(final byte[] buffer) {
        try {
            this.getFileOutputStream().write(buffer);
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(SOSVfs_E_134.params(CLASSNAME + "::write"), e);
        }
    }

    public void setZipEntry(final ZipEntry val) {
        zipEntry = val;
    }

    public ZipEntry getZipEntry() {
        return zipEntry;
    }

    @Override
    public void putFile(final ISOSVirtualFile file) throws Exception {
        notImplemented();
    }

    @Override
    public void setModeAppend(final boolean val) {
        notImplemented();
    }

    @Override
    public long getModificationDateTime() {
        return 0;
    }

    @Override
    public long setModificationDateTime(final long val) {
        return 0;
    }

}