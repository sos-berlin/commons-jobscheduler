package com.sos.VirtualFileSystem.JCIFS;

import java.io.InputStream;
import java.io.OutputStream;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsJCIFSFile extends SOSVfsTransferFileBaseClass {

    private String strFileName = null;

    /** \brief SOSVfsJCIFSFile
     *
     * \details
     *
     * @param pstrFileName */
    public SOSVfsJCIFSFile(final String pstrFileName) {
        super(pstrFileName);
        strFileName = pstrFileName;
    }

    /** \brief read
     *
     * \details
     *
     * \return
     *
     * @param bteBuffer
     * @return */
    @Override
    public int read(final byte[] bteBuffer) {
        try {
            InputStream is = this.getFileInputStream();

            if (is == null) {
                throw new JobSchedulerException(SOSVfs_E_177.get());
            }
            return is.read(bteBuffer);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_173.params("read", fileName));
            return 0;
        }
    }

    /** \brief read
     *
     * \details
     *
     * \return
     *
     * @param bteBuffer
     * @param intOffset
     * @param intLength
     * @return */
    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        try {
            InputStream is = this.getFileInputStream();
            if (is == null) {
                throw new Exception(SOSVfs_E_177.get());
            }
            return is.read(bteBuffer, intOffset, intLength);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_173.params("read", fileName));
            return 0;
        }
    }

    /** \brief write
     *
     * \details
     *
     * \return
     *
     * @param bteBuffer
     * @param intOffset
     * @param intLength */

    // private final OutputStream os = null;

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        try {
            OutputStream os = this.getFileOutputStream();
            if (os == null) {

                throw new Exception(SOSVfs_E_147.get());
            }
            os.write(bteBuffer, intOffset, intLength);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_173.params("write", fileName));
        }
    }

    /** \brief getFileOutputStream
     *
     * \details
     *
     * \return
     *
     * @return */
    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (objOutputStream == null) {
                fileName = super.adjustRelativePathName(fileName);

                SOSVfsJCIFS objJ = (SOSVfsJCIFS) objVFSHandler;
                objOutputStream = objJ.getOutputStream(fileName);

                if (objOutputStream == null) {
                    objVFSHandler.openOutputFile(fileName);
                }
            }
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_158.params("getFileOutputStream()", fileName));
        }
        return objOutputStream;
    }

    // @Override
    // public void closeOutput() {
    // if (objOutputStream != null) {
    // try {
    // objOutputStream.flush();
    // }
    // catch (IOException e) {}
    // try {
    // objOutputStream.close();
    // }
    // catch (IOException e) {
    // RaiseException(e, SOSVfs_E_173.params("write", fileName));
    // }
    // objOutputStream = null;
    // }
    // }
}
