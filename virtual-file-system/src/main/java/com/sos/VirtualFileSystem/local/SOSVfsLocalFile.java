package com.sos.VirtualFileSystem.local;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.DataElements.JSDataElementDateTime;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsLocalFile extends JSFile implements ISOSVirtualFile {

    private static final long serialVersionUID = 7478704922673917684L;
    private static final Logger LOGGER = Logger.getLogger(SOSVfsLocalFile.class);
    private ISOSVfsFileTransfer objVFSHandler = null;
    private InputStream objInputStream = null;
    private OutputStream objOutputStream = null;
    private boolean flgModeAppend = false;
    private static final String CLASS_NAME = "SOSVfsLocalFile";

    public SOSVfsLocalFile(final String pstrFileName) {
        super(pstrFileName);
    }

    @Override
    public boolean FileExists() throws Exception {
        return super.exists();
    }

    @Override
    public boolean delete() {
        super.delete();
        LOGGER.debug(SOSVfsMessageCodes.SOSVfs_I_0113.params(strFileName));
        return true;
    }

    @Override
    public void deleteFile() {
        super.delete();
    }

    @Override
    public ISOSVirtualFile getFile() throws Exception {
        return this;
    }

    @Override
    public OutputStream getFileAppendStream() {
        final String conMethodName = CLASS_NAME + "::getFileAppendStream";
        OutputStream objO = null;
        try {
            objO = new FileOutputStream(new File(strFileName), true);
        } catch (FileNotFoundException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName), e);
        }
        return objO;
    }

    @Override
    public InputStream getFileInputStream() {
        final String conMethodName = CLASS_NAME + "::getFileInputStream";
        try {
            if (objInputStream == null) {
                objInputStream = new FileInputStream(new File(strFileName));
            }
        } catch (FileNotFoundException e) {
            String strT = SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName);
            LOGGER.error(strT, e);
            throw new JobSchedulerException(strT + " / " + strFileName, e);
        }
        return objInputStream;
    }

    @Override
    public OutputStream getFileOutputStream() {
        final String conMethodName = CLASS_NAME + "::getFileOutputStream";
        try {
            if (objOutputStream == null) {
                objOutputStream = new FileOutputStream(new File(strFileName), flgModeAppend);
            }
        } catch (FileNotFoundException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName), e);
        }
        return objOutputStream;
    }

    @Override
    public Integer getFilePermissions() throws Exception {
        return 0;
    }

    @Override
    public long getFileSize() {
        return super.length();
    }

    @Override
    public ISOSVfsFileTransfer getHandler() {
        return objVFSHandler;
    }

    @Override
    public String getModificationTime() {
        Date dteModificationTime = new Date(super.lastModified());
        return new JSDataElementDateTime(dteModificationTime).FormattedValue();
    }

    @Override
    public String getName() {
        return super.getPath();
    }

    @Override
    public String getParentVfs() {
        return super.getParent();
    }

    @Override
    public ISOSVirtualFile getParentVfsFile() {
        ISOSVirtualFile objF = new SOSVfsLocalFile(super.getParent());
        objF.setHandler(getHandler());
        return objF;
    }

    @Override
    public boolean isDirectory() {
        boolean flgResult = super.isDirectory();
        return flgResult;
    }

    @Override
    public boolean isEmptyFile() {
        return super.length() <= 0;
    }

    @Override
    public boolean notExists() {
        return !super.exists();
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
        super.renameTo(new File(pstrNewFileName));
        LOGGER.info(SOSVfsMessageCodes.SOSVfs_I_150.params(strFileName, pstrNewFileName));
    }

    @Override
    public void setFilePermissions(final Integer pintNewPermission) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void setHandler(final ISOSVfsFileTransfer pobjVFSHandler) {
        objVFSHandler = pobjVFSHandler;
    }

    public void compressFile(final File outputFile) throws Exception {
        BufferedInputStream in = null;
        GZIPOutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(fleFile));
            out = new GZIPOutputStream(new FileOutputStream(outputFile));
            byte buffer[] = new byte[60000];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params("GZip"), e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                //
            }
        }
    }

    @Override
    public void close() {
        if (objOutputStream != null) {
            this.closeOutput();
        } else if (objInputStream != null) {
            this.closeInput();
        }
    }

    @Override
    public void closeInput() {
        final String conMethodName = CLASS_NAME + "::closeInput";
        try {
            if (objInputStream != null) {
                objInputStream.close();
            }
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName), e);
        } finally {
            objInputStream = null;
        }
    }

    @Override
    public void closeOutput() {
        final String conMethodName = CLASS_NAME + "::closeOutput";
        try {
            this.getFileOutputStream().flush();
            this.getFileOutputStream().close();
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName), e);
        } finally {
            objOutputStream = null;
        }
    }

    @Override
    public void flush() {
        final String conMethodName = CLASS_NAME + "::flush";
        try {
            this.getFileOutputStream().flush();
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName), e);
        }

    }

    @Override
    public int read(final byte[] bteBuffer) {
        final String conMethodName = CLASS_NAME + "::read";
        int lngBytesRed = 0;
        try {
            lngBytesRed = this.getFileInputStream().read(bteBuffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName), e);
        }
        return lngBytesRed;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        final String conMethodName = CLASS_NAME + "::read";
        int lngBytesRed = 0;
        try {
            lngBytesRed = this.getFileInputStream().read(bteBuffer, intOffset, intLength);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName), e);
        }
        return lngBytesRed;
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        final String conMethodName = CLASS_NAME + "::write";
        try {
            this.getFileOutputStream().write(bteBuffer, intOffset, intLength);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName), e);
        }
    }

    @Override
    public void write(final byte[] bteBuffer) {
        final String conMethodName = CLASS_NAME + "::write";
        try {
            this.getFileOutputStream().write(bteBuffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(conMethodName), e);
        }
    }

    @Override
    public void String2File(final String pstrContent) {
        try {
            OutputStream objOS = this.getFileOutputStream();
            objOS.write(pstrContent.getBytes());
            objOS.close();
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_130.params("String2File"), e);
        }

    }

    @Override
    public String File2String() {
        InputStream objFI = this.getFileInputStream();
        if (objFI == null) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_265.get());
        }
        StringBuffer strB = new StringBuffer((int) this.getFileSize());
        int lngBufferSize = 1024;
        byte[] buffer = new byte[lngBufferSize];
        int intBytesTransferred;
        try {
            while ((intBytesTransferred = objFI.read(buffer)) != -1) {
                strB.append(new String(buffer).substring(0, intBytesTransferred));
            }
            objFI.close();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params("File2String"), e);
        }
        return strB.toString();
    }

    @Override
    public void putFile(final ISOSVirtualFile pobjVirtualFile) throws Exception {
        boolean flgClosingDone = false;
        long lngTotalBytesTransferred = 0;
        try {
            int lngBufferSize = 4096;
            byte[] buffer = new byte[lngBufferSize];
            int intBytesTransferred;
            synchronized (this) {
                while ((intBytesTransferred = pobjVirtualFile.read(buffer)) != -1) {
                    try {
                        this.write(buffer, 0, intBytesTransferred);
                    } catch (JobSchedulerException e) {
                        break;
                    }
                    lngTotalBytesTransferred += intBytesTransferred;
                }
            }
            pobjVirtualFile.closeInput();
            this.closeOutput();
            flgClosingDone = true;
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_266.get(), e);
        } finally {
            if (!flgClosingDone) {
                pobjVirtualFile.closeInput();
                this.closeOutput();
                flgClosingDone = true;
            }
        }
    }

    @Override
    public void setModeAppend(final boolean pflgModeAppend) {
        flgModeAppend = pflgModeAppend;
    }

    @Override
    public long getModificationDateTime() {
        long lngR = 0;
        try {
            lngR = new File(strFileName).lastModified();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            lngR = -1;
        }
        return lngR;
    }

    @Override
    public long setModificationDateTime(final long pdteDateTime) {
        long lngR = 0;
        try {
            File fleF = new File(strFileName);
            fleF.setLastModified(pdteDateTime);
            lngR = pdteDateTime;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            lngR = -1;
        }
        return lngR;
    }

    @Override
    public void setModeRestart(final boolean pflgModeRestart) {
        //
    }

    @Override
    public void setModeOverwrite(final boolean pflgModeOverwrite) {
        //
    }

}