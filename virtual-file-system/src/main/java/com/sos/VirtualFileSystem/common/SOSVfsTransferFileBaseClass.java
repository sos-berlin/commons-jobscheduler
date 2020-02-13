package com.sos.VirtualFileSystem.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsTransferFileBaseClass extends SOSVfsCommonFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsTransferFileBaseClass.class);
    protected String fileName = EMPTY_STRING;

    public SOSVfsTransferFileBaseClass() {
        super("SOSVirtualFileSystem");
    }

    public SOSVfsTransferFileBaseClass(final String pFileName) {
        this();
        String name = pFileName;
        fileName = adjustFileSeparator(name);
    }

    @Override
    public boolean fileExists() {
        boolean result = false;
        if (objVFSHandler.getFileSize(fileName) >= 0) {
            result = true;
        }
        LOGGER.debug(String.format("[%s]%s", fileName, result));
        return result;
    }

    @Override
    public boolean delete(boolean checkIsDirectory) {
        try {
            objVFSHandler.delete(fileName, checkIsDirectory);
        } catch (Exception e) {
            SOSVfs_E_158.get();
            throw new JobSchedulerException(SOSVfs_E_158.params("delete()", fileName),e);
        }
        return true;
    }

    @Override
    public OutputStream getFileAppendStream() {
        OutputStream objO = null;
        try {
            fileName = adjustRelativePathName(fileName);
            objO = objVFSHandler.getAppendFileStream(fileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileAppendStream()", fileName), e);
        }
        return objO;
    }

    @Override
    public InputStream getFileInputStream() {
        try {
            if (objInputStream == null) {
                fileName = adjustRelativePathName(fileName);
                objInputStream = objVFSHandler.getInputStream(fileName);
                if (objInputStream == null) {
                    objVFSHandler.openInputFile(fileName);
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileInputStream()", fileName), e);
        }
        return objInputStream;
    }

    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (objOutputStream == null) {
                fileName = adjustRelativePathName(fileName);
                if (objOutputStream == null) {
                    objVFSHandler.openOutputFile(fileName);
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileOutputStream()", fileName), e);
        }
        return objOutputStream;
    }

    protected String adjustRelativePathName(final String pstrPathName) {
        return pstrPathName.replaceAll("\\\\", "/");
    }

    @Override
    public Integer getFilePermissions() throws Exception {
        return 0;
    }

    @Override
    public long getFileSize() {
        long lngFileSize = -1;
        try {
            lngFileSize = objVFSHandler.getFileSize(fileName);
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
        return objVFSHandler.isDirectory(fileName);
    }

    @Override
    public boolean isEmptyFile() {
        return this.getFileSize() <= 0;
    }

    @Override
    public boolean notExists() {
        boolean flgResult = false;
        try {
            flgResult = !this.fileExists();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("notExists()"), e);
        }
        return flgResult;
    }

    @Override
    public void putFile(final File fleFile) {
        notImplemented();
    }

    @Override
    public void putFile(final String strFileName) {
        notImplemented();
    }

    @Override
    public void rename(final String pstrNewFileName) {
        objVFSHandler.rename(fileName, pstrNewFileName);
    }

    @Override
    public void setFilePermissions(final Integer pintNewPermission) {
        notImplemented();
    }

    @Override
    public void setHandler(final ISOSVfsFileTransfer pobjVFSHandler) {
        objVFSHandler = pobjVFSHandler;
    }

    @Override
    public String getModificationTime() {
        String strT = "";
        try {
            strT = objVFSHandler.getModificationTime(fileName);
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
            if (objInputStream != null) {
                objInputStream.close();
                objInputStream = null;
            }
        } catch (Exception ex) {
            //
        }
    }

    @Override
    public void closeOutput() {
        try {
            if (objOutputStream != null) {
                objOutputStream.flush();
                objOutputStream.close();
                objOutputStream = null;
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
    public int read(final byte[] bteBuffer) {
        return 0;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        return 0;
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        //
    }

    @Override
    public void write(final byte[] bteBuffer) {
        notImplemented();
        try {
            this.getFileOutputStream().write(bteBuffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("write()"), e);
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception {
        notImplemented();
    }

    @Override
    public long setModificationDateTime(final long pdteDateTime) {
        return 0;
    }

    @Override
    public long getModificationDateTime() {
        return 0;
    }

}