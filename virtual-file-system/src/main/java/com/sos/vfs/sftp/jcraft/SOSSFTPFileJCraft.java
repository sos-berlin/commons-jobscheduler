package com.sos.vfs.sftp.jcraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.SOSCommonProviderFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSFTPFileJCraft extends SOSCommonProviderFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSFTPFileJCraft.class);

    public SOSSFTPFileJCraft(final String fileName) {
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
            throw new JobSchedulerException(SOSVfs_E_173.params("read", fileName), e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public InputStream getFileInputStream() {
        try {
            if (getInputStream() == null) {
                fileName = adjustRelativePathName(fileName);
                int mode = ChannelSftp.OVERWRITE;
                if (isModeAppend()) {
                    mode = ChannelSftp.APPEND;
                } else if (isModeRestart()) {
                    mode = ChannelSftp.RESUME;
                }
                SOSSFTPJCraft handler = (SOSSFTPJCraft) getProvider();
                setInputStream(handler.getChannelSftp().get(fileName, mode));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileInputStream()", fileName), e);
        }
        return getInputStream();
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        try {
            OutputStream os = getFileOutputStream();
            if (os == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            os.write(buffer, offset, length);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("write", fileName), e);
        }
    }

    @Override
    public void write(final byte[] buffer) {
        try {
            getFileOutputStream().write(buffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("write()"), e);
        }
    }

    private OutputStream getFileOutputStream() {
        try {
            if (getOutputStream() == null) {
                fileName = adjustRelativePathName(fileName);
                setOutputStream(getProvider().getOutputStream(fileName, isModeAppend(), isModeRestart()));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileOutputStream()", fileName), e);
        }
        return getOutputStream();
    }

    @Override
    public long getModificationDateTime() {
        String path = "";
        try {
            path = adjustRelativePathName(fileName);
            SOSSFTPJCraft handler = (SOSSFTPJCraft) getProvider();
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
            SOSSFTPJCraft handler = (SOSSFTPJCraft) getProvider();
            int mTime = (int) (timestamp / 1000L);
            handler.getChannelSftp().setMtime(adjustRelativePathName(fileName), mTime);
            return timestamp;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return -1L;
        }
    }

}