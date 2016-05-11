package com.sos.VirtualFileSystem.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsTransferFileBaseClass extends SOSVfsCommonFile {

    protected String fileName = EMPTY_STRING;
    private static final Logger LOGGER = Logger.getLogger(SOSVfsTransferFileBaseClass.class);

    public SOSVfsTransferFileBaseClass() {
        super("SOSVirtualFileSystem");
    }

    public SOSVfsTransferFileBaseClass(final String pFileName) {
        this();
        String name = pFileName;
        fileName = adjustFileSeparator(name);
    }

    @Override
    public boolean FileExists() {
        boolean flgResult = false;
        logDEBUG(SOSVfs_D_156.params(fileName));
        if (objVFSHandler.getFileSize(fileName) >= 0) {
            flgResult = true;
        }
        logDEBUG(SOSVfs_D_157.params(flgResult, fileName));
        return flgResult;
    }

    @Override
    public boolean delete() {
        try {
            objVFSHandler.delete(fileName);
        } catch (Exception e) {
            SOSVfs_E_158.get();
            RaiseException(e, SOSVfs_E_158.params("delete()", fileName));
        }
        return true;
    }

    @Override
    public OutputStream getFileAppendStream() {
        OutputStream objO = null;
        try {
            fileName = AdjustRelativePathName(fileName);
            objO = objVFSHandler.getAppendFileStream(fileName);
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_158.params("getFileAppendStream()", fileName));
        }
        return objO;
    }

    @Override
    public InputStream getFileInputStream() {
        try {
            if (objInputStream == null) {
                fileName = AdjustRelativePathName(fileName);
                objInputStream = objVFSHandler.getInputStream(fileName);
                if (objInputStream == null) {
                    objVFSHandler.openInputFile(fileName);
                }
            }
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_158.params("getFileInputStream()", fileName));
        }
        return objInputStream;
    }

    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (objOutputStream == null) {
                fileName = AdjustRelativePathName(fileName);
                if (objOutputStream == null) {
                    objVFSHandler.openOutputFile(fileName);
                }
            }
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_158.params("getFileOutputStream()", fileName));
        }
        return objOutputStream;
    }

    protected String AdjustRelativePathName(final String pstrPathName) {
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
            RaiseException(e, SOSVfs_E_134.params("getFileSize()"));
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
            flgResult = !this.FileExists();
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_134.params("notExists()"));
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
            RaiseException(e, SOSVfs_E_134.params("getModificationTime()"));
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
            RaiseException(e, SOSVfs_E_134.params("flush()"));
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
            RaiseException(e, SOSVfs_E_134.params("write()"));
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception {
        notImplemented();
    }

    protected void RaiseException(final Exception e, final String msg) {
        LOGGER.error(msg + " (" + e.getLocalizedMessage() + ")");
        throw new JobSchedulerException(msg, e);
    }

    protected void RaiseException(final String msg) {
        LOGGER.error(msg);
        throw new JobSchedulerException(msg);
    }

    private String getLogPrefix() {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        String[] classNameArr = ste.getClassName().split("\\.");
        return "(" + classNameArr[classNameArr.length - 1] + "::" + ste.getMethodName() + ") ";
    }

    protected void logINFO(final Object msg) {
        LOGGER.info(this.getLogPrefix() + msg);
    }

    protected void logDEBUG(final Object msg) {
        LOGGER.debug(this.getLogPrefix() + msg);
    }

    protected void logWARN(final Object msg) {
        LOGGER.warn(this.getLogPrefix() + msg);
    }

    protected void logERROR(final Object msg) {
        LOGGER.error(this.getLogPrefix() + msg);
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