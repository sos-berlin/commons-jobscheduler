package com.sos.VirtualFileSystem.SFTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.sos.VirtualFileSystem.common.SOSVfsTransferFileBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtpFileJCraft extends SOSVfsTransferFileBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsSFtpFileJCraft.class);

    public SOSVfsSFtpFileJCraft(final String fileName) {
        super(fileName);
    }

    @Override
    public int read(final byte[] buffer) {
        try {
            InputStream is = this.getFileInputStream();
            if (is == null) {
                throw new Exception(SOSVfs_E_177.get());
            }
            return is.read(buffer);
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
                int transferMode = ChannelSftp.OVERWRITE;
                if (flgModeAppend) {
                    transferMode = ChannelSftp.APPEND;
                } else if (flgModeRestart) {
                    transferMode = ChannelSftp.RESUME;
                }
                SOSVfsSFtpJCraft handler = (SOSVfsSFtpJCraft) objVFSHandler;
                objInputStream = handler.getClient().get(fileName, transferMode);
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
    public int read(final byte[] buffer, final int offset, final int length) {
        try {
            InputStream is = this.getFileInputStream();
            if (is == null) {
                throw new Exception(SOSVfs_E_177.get());
            }
            return is.read(buffer, offset, length);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_173.params("read", fileName));
            return 0;
        }
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        try {
            OutputStream os = this.getFileOutputStream();
            if (os == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            os.write(buffer, offset, length);
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_173.params("write", fileName));
        }
    }

    @Override
    public void write(final byte[] buffer) {
        try {
            this.getFileOutputStream().write(buffer);
        } catch (IOException e) {
            raiseException(e, SOSVfs_E_134.params("write()"));
        }
    }

    @Override
    public OutputStream getFileOutputStream() {
        try {
            if (objOutputStream == null) {
                fileName = super.adjustRelativePathName(fileName);
                int transferMode = ChannelSftp.OVERWRITE;
                if (flgModeAppend) {
                    transferMode = ChannelSftp.APPEND;
                } else if (flgModeRestart) {
                    transferMode = ChannelSftp.RESUME;
                }
                SOSVfsSFtpJCraft handler = (SOSVfsSFtpJCraft) objVFSHandler;
                objOutputStream = handler.getClient().put(fileName, transferMode);
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
        String path = "";
        try {
            path = adjustRelativePathName(fileName);
            SOSVfsSFtpJCraft handler = (SOSVfsSFtpJCraft) objVFSHandler;
            SftpATTRS attrs = handler.getAttributes(path);
            if (attrs == null) {
                throw new Exception("SftpATTRS is null");
            }
            return attrs.getMTime() * 1000L;
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", path, e.toString()), e);
            return -1L;
        }
    }

    @Override
    public long setModificationDateTime(final long timestamp) {
        try {
            SOSVfsSFtpJCraft handler = (SOSVfsSFtpJCraft) objVFSHandler;
            int mTime = (int) (timestamp / 1000L);
            handler.getClient().setMtime(adjustRelativePathName(fileName), mTime);
            return timestamp;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return -1L;
        }
    }

}