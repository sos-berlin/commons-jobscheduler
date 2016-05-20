package com.sos.VirtualFileSystem.JCIFS;

import java.io.InputStream;
import java.io.OutputStream;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsJCIFSFile extends SOSVfsTransferFileBaseClass {

    public SOSVfsJCIFSFile(final String pstrFileName) {
        super(pstrFileName);
    }

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

}