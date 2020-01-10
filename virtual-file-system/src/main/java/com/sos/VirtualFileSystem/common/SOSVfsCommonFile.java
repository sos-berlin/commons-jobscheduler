package com.sos.VirtualFileSystem.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.i18n.annotation.I18NResourceBundle;

/** \class SOSVfsCommonFile
 *
 * \brief SOSVfsCommonFile -
 *
 * \details
 *
 * \code .... code goes here ... \endcode
 *
 * <p style="text-align:center">
 * <br />
 * --------------------------------------------------------------------------- <br />
 * APL/Software GmbH - Berlin <br />
 * ##### generated by ClaviusXPress (http://www.sos-berlin.com) ######### <br />
 * ---------------------------------------------------------------------------
 * </p>
 * \author KB \version $Id$ 29.03.2011 \see reference
 *
 * Created on 29.03.2011 18:08:16 */
/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSVfsCommonFile extends SOSVfsMessageCodes implements ISOSVirtualFile {

    @SuppressWarnings("unused")
    private final String conClassName = "SOSVfsCommonFile";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsCommonFile.class);
    protected ISOSVfsFileTransfer objVFSHandler = null;
    protected InputStream objInputStream = null;
    public OutputStream objOutputStream = null;
    public ZipOutputStream objEntryOutputStream = null;

    protected boolean flgModeAppend = false;  // Append Mode for output file
    protected boolean flgModeRestart = false;
    protected boolean flgModeOverwrite = true;

    public SOSVfsCommonFile() {
        //
    }

    public SOSVfsCommonFile(final String pstrBundleBaseName) {
        super(pstrBundleBaseName);
    }

    /** \brief getHandler
     *
     * \details
     *
     * \return
     *
     * @return */
    @Override
    public ISOSVfsFileTransfer getHandler() {
        return objVFSHandler;
    }

    @Override
    public String file2String() {
        InputStream objFI = this.getFileInputStream();
        if (objFI == null) {
            throw new JobSchedulerException(SOSVfs_E_177);
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
            throw new JobSchedulerException(SOSVfs_E_134.get("File2String()"), e);
        }
        return strB.toString();
    }

    @Override
    public void string2File(final String pstrContent) {

        try {
            OutputStream objOS = this.getFileOutputStream();
            objOS.write(pstrContent.getBytes());
            objOS.close();
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.get("String2File"), e);
        }
    }

    @Override
    public void putFile(final File fleFile) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void putFile(final String strFileName) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public long getFileSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Integer getFilePermissions() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getModificationTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setFilePermissions(final Integer pintNewPermission) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean fileExists() throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDirectory() throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean notExists() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmptyFile() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public OutputStream getFileOutputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream getFileAppendStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getFileInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean delete() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void rename(final String pstrNewFileName) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getParentVfs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ISOSVirtualFile getParentVfsFile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setHandler(final ISOSVfsFileTransfer pobjVFSHandler) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        // TODO Auto-generated method stub
    }

    @Override
    public void write(final byte[] bteBuffer) {
        // TODO Auto-generated method stub
    }

    @Override
    public int read(final byte[] bteBuffer) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub
    }

    @Override
    public void closeInput() {
        // TODO Auto-generated method stub
    }

    @Override
    public void closeOutput() {
        // TODO Auto-generated method stub
    }

    /** \brief deleteFile
     *
     * \details
     *
     * \return
     *
     * @throws Exception */
    @Override
    public void deleteFile() {
        this.delete();
    }

    /** \brief getFile
     *
     * \details
     *
     * \return
     *
     * @return
     * @throws Exception */
    @Override
    public ISOSVirtualFile getFile() {
        return this;
    }

    @Override
    public void setModeAppend(final boolean pflgModeAppend) {
        flgModeAppend = pflgModeAppend;
    }

    @Override
    public void setModeRestart(final boolean pflgModeRestart) {
        flgModeRestart = pflgModeRestart;
    }

    @Override
    public void setModeOverwrite(final boolean pflgModeOverwrite) {
        flgModeOverwrite = pflgModeOverwrite;
    }

}
