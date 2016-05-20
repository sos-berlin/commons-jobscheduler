package com.sos.VirtualFileSystem.SFTP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSVfsCommonFile;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtpFile extends SOSVfsCommonFile {

    private static final String CLASSNAME = "SOSVfsSFtpFile";
    private static final Logger LOGGER = Logger.getLogger(SOSVfsSFtpFile.class);
    private SFTPv3FileHandle objFileHandle = null;
    private String strFileName = EMPTY_STRING;
    private long lngFileReadOffset = 0;
    private long lngFileWriteOffset = 0;

    public SOSVfsSFtpFile(final String pstrFileName) {
        final String conMethodName = CLASSNAME + "::SOSVfsSFtpFile";
        String strF = pstrFileName;
        if (objVFSHandler != null) {
            String strCurrDir = objVFSHandler.doPWD();
            LOGGER.debug(SOSVfs_D_171.params(conMethodName, strCurrDir));
            if (strF.startsWith("./") == true) {
                strF = strF.replace("./", strCurrDir + "/");
            }
        }
        strFileName = adjustFileSeparator(strF);
    }

    @Override
    public boolean fileExists() {
        final String conMethodName = CLASSNAME + "::FileExists";
        boolean flgResult = false;
        LOGGER.debug(SOSVfs_D_172.params(conMethodName, strFileName));
        if (objVFSHandler.getFileSize(strFileName) >= 0) {
            flgResult = true;
        }
        LOGGER.debug(SOSVfs_D_157.params(conMethodName, flgResult, strFileName));
        return flgResult;
    }

    @Override
    public boolean delete() {
        final String conMethodName = CLASSNAME + "::delete";
        try {
            objVFSHandler.delete(strFileName);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName + " -> " + strFileName), e);
        }
        return true;
    }

    @Override
    public OutputStream getFileAppendStream() {
        final String conMethodName = CLASSNAME + "::getFileAppendStream";
        OutputStream objO = null;
        try {
            strFileName = AdjustRelativePathName(strFileName);
            objO = objVFSHandler.getAppendFileStream(strFileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return objO;
    }

    @Override
    public InputStream getFileInputStream() {
        final String conMethodName = CLASSNAME + "::getFileInputStream";
        try {
            if (objInputStream == null) {
                strFileName = AdjustRelativePathName(strFileName);
                objInputStream = objVFSHandler.getInputStream(strFileName);
                if (objInputStream == null) {
                    objVFSHandler.openInputFile(strFileName);
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return objInputStream;
    }

    @Override
    public OutputStream getFileOutputStream() {
        final String conMethodName = CLASSNAME + "::getFileOutputStream";
        try {
            if (objOutputStream == null) {
                strFileName = AdjustRelativePathName(strFileName);
                if (flgModeAppend) {
                    objOutputStream = objVFSHandler.getAppendFileStream(strFileName);
                } else {
                    objOutputStream = objVFSHandler.getOutputStream(strFileName);
                }
                if (objOutputStream == null) {
                    objVFSHandler.openOutputFile(strFileName);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return objOutputStream;
    }

    private String AdjustRelativePathName(final String pstrPathName) {
        String strT = pstrPathName;
        if (pstrPathName.startsWith("./") || pstrPathName.startsWith(".\\")) {
            String strPath = objVFSHandler.doPWD() + "/";
            strT = new File(pstrPathName).getName();
            strT = strPath + strT;
            LOGGER.debug(SOSVfs_D_159.params(pstrPathName, strT));
        }
        return strT;
    }

    @Override
    public Integer getFilePermissions() throws Exception {
        return 0;
    }

    @Override
    public long getFileSize() {
        long lngFileSize = -1;
        try {
            lngFileSize = objVFSHandler.getFileSize(strFileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("getFileSize()"), e);
        }
        return lngFileSize;
    }

    @Override
    public String getName() {
        return strFileName;
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
        return objVFSHandler.isDirectory(strFileName);
    }

    @Override
    public boolean isEmptyFile() {
        return this.getFileSize() <= 0;
    }

    @Override
    public boolean notExists() {
        final String conMethodName = CLASSNAME + "::notExists";
        boolean flgResult = false;
        try {
            flgResult = !this.fileExists();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
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
        objVFSHandler.rename(adjustFileSeparator(strFileName), adjustFileSeparator(pstrNewFileName));
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
        final String conMethodName = CLASSNAME + "::getModificationTime";
        String strT = "";
        try {
            strT = objVFSHandler.getModificationTime(strFileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return strT;
    }

    @Override
    public void close() {
        final String conMethodName = CLASSNAME + "::close";
        try {
            if (objFileHandle != null) {
                objFileHandle.getClient().closeFile(objFileHandle);
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        } finally {
            objFileHandle = null;
            lngFileReadOffset = 0;
            lngFileWriteOffset = 0;
        }
    }

    @Override
    public void closeInput() {
        close();
    }

    @Override
    public void closeOutput() {
        close();
    }

    @Override
    public void flush() {
        final String conMethodName = CLASSNAME + "::flush";
        try {
            this.getFileOutputStream().flush();
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
    }

    @Override
    public int read(final byte[] bteBuffer) {
        int intLength = bteBuffer.length;
        int lngBytesRed = read(bteBuffer, (int) lngFileReadOffset, intLength);
        lngFileReadOffset += lngBytesRed;
        return lngBytesRed;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        final String conMethodName = CLASSNAME + "::read";
        int lngBytesRed = 0;
        try {
            if (objFileHandle == null) {
                SOSVfsSFtp objT = (SOSVfsSFtp) objVFSHandler;
                objFileHandle = objT.getInputFileHandle(strFileName);
            }
            lngBytesRed = objFileHandle.getClient().read(objFileHandle, intOffset, bteBuffer, 0, intLength);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_173.params(conMethodName, strFileName), e);
        }
        return lngBytesRed;
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        final String conMethodName = CLASSNAME + "::write";
        try {
            if (objFileHandle == null) {
                SOSVfsSFtp objT = (SOSVfsSFtp) objVFSHandler;
                objFileHandle = objT.getOutputFileHandle(strFileName);
                lngFileWriteOffset = 0;
            }
            objFileHandle.getClient().write(objFileHandle, lngFileWriteOffset, bteBuffer, 0, intLength);
            lngFileWriteOffset += intLength;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_174.params(conMethodName, strFileName, lngFileWriteOffset, intLength), e);
        }
    }

    @Override
    public void write(final byte[] bteBuffer) {
        final String conMethodName = CLASSNAME + "::write";
        notImplemented();
        try {
            this.getFileOutputStream().write(bteBuffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception {
        notImplemented();
    }

    @Override
    public long getModificationDateTime() {
        long lngR = 0;
        if (objFileHandle != null) {
            try {
                SFTPv3FileAttributes objFA = objFileHandle.getClient().fstat(objFileHandle);
                lngR = objFA.mtime;
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return lngR;
    }

    @Override
    public long setModificationDateTime(final long pdteDateTime) {
        long lngR = 0;
        if (objFileHandle != null) {
            try {
                SFTPv3FileAttributes objFA = objFileHandle.getClient().fstat(objFileHandle);
                objFA.mtime = new Integer((int) pdteDateTime);
                lngR = pdteDateTime;
            } catch (IOException e) {
                LOGGER.error(e.getLocalizedMessage());
            }
        }
        return lngR;
    }

}