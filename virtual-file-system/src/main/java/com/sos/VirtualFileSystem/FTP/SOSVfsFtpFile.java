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
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSVfsCommonFile;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpFile extends SOSVfsCommonFile {

    private static final String CLASSNAME = "SOSVfsFtpFile";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsFtpFile.class);
    private String strFileName = EMPTY_STRING;

    public SOSVfsFtpFile(final String pstrFileName) {
        super(SOSVfsConstants.strBundleBaseName);
        strFileName = pstrFileName;
    }

    public SOSVfsFtpFile(final FTPFile pobjFileFile) {
        super(SOSVfsConstants.strBundleBaseName);
        strFileName = pobjFileFile.getName();
    }

    @Override
    public boolean fileExists() {
        final String conMethodName = CLASSNAME + "::FileExists";
        boolean flgResult = false;
        LOGGER.debug(SOSVfs_D_172.params(conMethodName, strFileName));
        if (objVFSHandler.getFileSize(strFileName) >= 0) {
            flgResult = true;
        }
        return flgResult;
    }

    @Override
    public boolean delete(boolean chekIsDirectory) {
        final String conMethodName = CLASSNAME + "::delete";
        try {
            objVFSHandler.delete(strFileName, chekIsDirectory);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return true;
    }

    @Override
    public OutputStream getFileAppendStream() {
        final String conMethodName = CLASSNAME + "::getFileAppendStream";
        OutputStream objO = null;
        try {
            objO = objVFSHandler.getAppendFileStream(strFileName);
        } catch (JobSchedulerException e) {
            throw e;
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
                objInputStream = objVFSHandler.getInputStream(strFileName);
                if (objInputStream == null) {
                    objVFSHandler.openInputFile(strFileName);
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
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
                if (flgModeAppend) {
                    objOutputStream = objVFSHandler.getAppendFileStream(strFileName);
                } else {
                    objOutputStream = objVFSHandler.getOutputStream(strFileName);
                }
                if (objOutputStream == null) {
                    throw new JobSchedulerException(objVFSHandler.getReplyString());
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return objOutputStream;
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
        objVFSHandler.rename(strFileName, pstrNewFileName);
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
        if (objOutputStream != null) {
            this.closeOutput();
            objOutputStream = null;
        } else {
            if (objInputStream != null) {
                this.closeInput();
                objInputStream = null;
            }
        }
    }

    @Override
    public void closeInput() {
        final String conMethodName = CLASSNAME + "::closeInput";
        try {
            if (objInputStream != null) {
                try {
                    objInputStream.close();
                    objInputStream = null;
                    objVFSHandler.completePendingCommand();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    throw new JobSchedulerException(e);
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        } finally {
            objInputStream = null;
        }
    }

    @Override
    public void closeOutput() {
        final String conMethodName = CLASSNAME + "::closeOutput";
        try {
            if (objOutputStream != null) {
                objOutputStream.flush();
                objOutputStream.close();
                objVFSHandler.completePendingCommand();
                if (objVFSHandler.isNegativeCommandCompletion()) {
                    throw new JobSchedulerException(SOSVfs_E_175.params(strFileName, objVFSHandler.getReplyString()));
                }
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        } finally {
            objOutputStream = null;
        }
    }

    @Override
    public void flush() {
        final String conMethodName = CLASSNAME + "::flush";
        try {
            if (objOutputStream != null) {
                objOutputStream.flush();
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
    }

    @Override
    public int read(final byte[] bteBuffer) {
        final String conMethodName = CLASSNAME + "::read";
        int lngBytesRed = 0;
        try {
            InputStream objI = this.getFileInputStream();
            if (objI != null) {
                lngBytesRed = objI.read(bteBuffer);
            } else {
                lngBytesRed = objVFSHandler.read(bteBuffer);
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return lngBytesRed;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        final String conMethodName = CLASSNAME + "::read";
        int lngBytesRed = 0;
        try {
            InputStream objI = this.getFileInputStream();
            if (objI != null) {
                lngBytesRed = objI.read(bteBuffer, intOffset, intLength);
            } else {
                lngBytesRed = objVFSHandler.read(bteBuffer, intOffset, intLength);
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params(conMethodName), e);
        }
        return lngBytesRed;
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        final String conMethodName = CLASSNAME + "::write";
        try {
            if (this.getFileOutputStream() == null) {
                throw new JobSchedulerException(SOSVfs_E_176.params(conMethodName, strFileName));
            }
            this.getFileOutputStream().write(bteBuffer, intOffset, intLength);
        } catch (IOException e) {
            throw new JobSchedulerException(String.format("%1$s failed for file %2$s", conMethodName, strFileName), e);
        }
    }

    @Override
    public void write(final byte[] bteBuffer) {
        final String conMethodName = CLASSNAME + "::write";
        try {
            this.getFileOutputStream().write(bteBuffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.get(conMethodName), e);
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception {
        notImplemented();
    }

    @Override
    public long getModificationDateTime() {
        String strT = objVFSHandler.getModificationTime(strFileName);
        long lngT = -1;
        if (strT != null) {
            if (strT.startsWith("213 ")) {
                strT = strT.substring(3);
            }
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                lngT = df.parse(strT.trim()).getTime();
            } catch (Exception e) {
                lngT = -1L;
            }
        } else {
            lngT = -1L;
        }
        return lngT;
    }

    @Override
    public long setModificationDateTime(final long pdteDateTime) {
        try {
            SOSVfsFtp handler = (SOSVfsFtp) objVFSHandler;
            Date d = new Date(pdteDateTime);
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            handler.Client().setModificationTime(strFileName, df.format(d));
            return pdteDateTime;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return -1L;
        }
    }

}