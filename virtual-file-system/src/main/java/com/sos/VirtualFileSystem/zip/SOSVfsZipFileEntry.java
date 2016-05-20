package com.sos.VirtualFileSystem.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.DataElements.JSDataElementDateTime;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSVfsCommonFile;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsZipFileEntry extends SOSVfsCommonFile {

    private static final long serialVersionUID = -6693187904489763940L;
    private static final String CLASSNAME = "SOSVfsZipFileEntry";
    private static final Logger LOGGER = Logger.getLogger(SOSVfsZipFileEntry.class);
    private ZipEntry objZipEntry = null;

    public SOSVfsZipFileEntry(final String pstrFileName) {
        super();
    }

    @Override
    public boolean fileExists() throws Exception {
        return true;
    }

    @Override
    public boolean delete() {
        notImplemented();
        return true;
    }

    @Override
    public void deleteFile() {
        this.delete();
    }

    @Override
    public OutputStream getFileAppendStream() {
        notImplemented();
        return null;
    }

    @Override
    public InputStream getFileInputStream() {
        final String conMethodName = CLASSNAME + "::getFileInputStream";
        String strEntryName = "";
        try {
            if (objInputStream == null) {
                if (objVFSHandler == null) {
                    throw new JobSchedulerException("objVFSHandler == null");
                }
                if (objZipEntry == null) {
                    throw new JobSchedulerException("objZipEntry == null");
                }
                strEntryName = objZipEntry.getName();
                objInputStream = objVFSHandler.getInputStream(objZipEntry.getName());
                LOGGER.debug(SOSVfs_D_207.params(strEntryName));
            }
        } catch (Exception e) {
            String strT = SOSVfs_E_134.params(conMethodName);
            LOGGER.error(strT, e);
            throw new JobSchedulerException(strT, e);
        }
        return objInputStream;
    }

    @Override
    public OutputStream getFileOutputStream() {
        final String conMethodName = CLASSNAME + "::getFileOutputStream";
        try {
            if (objOutputStream == null) {
                objOutputStream = objVFSHandler.getOutputStream(objZipEntry.getName());
            }
        } catch (Exception e) {
            String strT = SOSVfs_E_134.params(conMethodName);
            throw new JobSchedulerException(strT, e);
        }
        return objOutputStream;
    }

    @Override
    public Integer getFilePermissions() throws Exception {
        return 0;
    }

    @Override
    public long getFileSize() {
        return objZipEntry.getSize();
    }

    @Override
    public String getModificationTime() {
        Date dteModificationTime = new Date(objZipEntry.getTime());
        return new JSDataElementDateTime(dteModificationTime).getFormattedValue();
    }

    @Override
    public String getName() {
        return objZipEntry.getName();
    }

    @Override
    public String getParentVfs() {
        notImplemented();
        return objZipEntry.getName();
    }

    @Override
    public ISOSVirtualFile getParentVfsFile() {
        notImplemented();
        return null;
    }

    @Override
    public boolean isDirectory() {
        boolean flgResult = false;
        if (objZipEntry != null) {
            flgResult = objZipEntry.isDirectory();
        } else {
            flgResult = true;
        }
        return flgResult;
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
    public void putFile(final File pfleFile) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void putFile(final String pstrFileName) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void rename(final String pstrNewFileName) {
        notImplemented();
    }

    @Override
    public void setFilePermissions(final Integer pintNewPermission) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void setHandler(final ISOSVfsFileTransfer pobjVFSHandler) {
        objVFSHandler = pobjVFSHandler;
    }

    @Override
    public void close() {
        if (objOutputStream != null) {
            this.closeOutput();
        } else {
            if (objInputStream != null) {
                this.closeInput();
            }
        }
    }

    @Override
    public void closeInput() {
        final String conMethodName = CLASSNAME + "::closeInput";
        try {
            if (objInputStream != null) {
                objInputStream.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        } finally {
            objInputStream = null;
        }
    }

    @Override
    public void closeOutput() {
        final String conMethodName = CLASSNAME + "::closeOutput";
        try {
            if (objEntryOutputStream == null) {
                return;
            }
            objEntryOutputStream.flush();
            objEntryOutputStream.closeEntry();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        } finally {
            objOutputStream = null;
        }
    }

    @Override
    public void flush() {
        final String conMethodName = CLASSNAME + "::flush";
        try {
            this.getFileOutputStream().flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
    }

    @Override
    public int read(final byte[] bteBuffer) {
        final String conMethodName = CLASSNAME + "::read";
        int lngBytesRed = 0;
        try {
            lngBytesRed = this.getFileInputStream().read(bteBuffer);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return lngBytesRed;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        final String conMethodName = CLASSNAME + "::read";
        int lngBytesRed = 0;
        try {
            lngBytesRed = this.getFileInputStream().read(bteBuffer, intOffset, intLength);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return lngBytesRed;
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        final String conMethodName = CLASSNAME + "::write";
        try {
            this.getFileOutputStream().write(bteBuffer, intOffset, intLength);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
    }

    @Override
    public void write(final byte[] bteBuffer) {
        final String conMethodName = CLASSNAME + "::write";
        try {
            this.getFileOutputStream().write(bteBuffer);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
    }

    public void setZipEntry(final ZipEntry objZE) {
        objZipEntry = objZE;
    }

    public ZipEntry getZipEntry() {
        return objZipEntry;
    }

    @Override
    public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception {
        notImplemented();
    }

    @Override
    public void setModeAppend(final boolean pflgModeAppend) {
        notImplemented();
    }

    @Override
    public long getModificationDateTime() {
        return 0;
    }

    @Override
    public long setModificationDateTime(final long pdteDateTime) {
        return 0;
    }

}