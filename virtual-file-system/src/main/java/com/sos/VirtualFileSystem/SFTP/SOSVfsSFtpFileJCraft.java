package com.sos.VirtualFileSystem.SFTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.ChannelSftp;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtpFileJCraft extends SOSVfsTransferFileBaseClass {

    public SOSVfsSFtpFileJCraft(final String pstrFileName) {
        super(pstrFileName);
    }

    @Override
    public int read(final byte[] bteBuffer) {
        try {
            InputStream is = this.getFileInputStream();
            if (is == null) {
                throw new Exception(SOSVfs_E_177.get());
            }
            return is.read(bteBuffer);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_173.params("read", fileName));
            return 0;
        }
    }

    @Override
    public InputStream getFileInputStream() {
        try {
            if (objInputStream == null) {
                fileName = adjustRelativePathName(fileName);
                int intTransferMode = ChannelSftp.OVERWRITE;
                if (flgModeAppend) {
                    intTransferMode = ChannelSftp.APPEND;
                } else if (flgModeRestart) {
                    intTransferMode = ChannelSftp.RESUME;
                }
                SOSVfsSFtpJCraft objJ = (SOSVfsSFtpJCraft) objVFSHandler;
                objInputStream = objJ.getClient().get(fileName, intTransferMode);
                if (objInputStream == null) {
                    objVFSHandler.openInputFile(fileName);
                }
            }
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_158.params("getFileInputStream()", fileName));
        }
        return objInputStream;
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
    public void write(final byte[] bteBuffer) {
        try {
            this.getFileOutputStream().write(bteBuffer);
        } catch (IOException e) {
            raiseException(e, SOSVfs_E_134.params("write()"));
        }
    }

    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (objOutputStream == null) {
                fileName = super.adjustRelativePathName(fileName);
                int intTransferMode = ChannelSftp.OVERWRITE;
                if (flgModeAppend) {
                    intTransferMode = ChannelSftp.APPEND;
                } else if (flgModeRestart) {
                    intTransferMode = ChannelSftp.RESUME;
                }
                SOSVfsSFtpJCraft objJ = (SOSVfsSFtpJCraft) objVFSHandler;
                objOutputStream = objJ.getClient().put(fileName, intTransferMode);
                if (objOutputStream == null) {
                    objVFSHandler.openOutputFile(fileName);
                }
            }
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_158.params("getFileOutputStream()", fileName));
        }
        return objOutputStream;
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