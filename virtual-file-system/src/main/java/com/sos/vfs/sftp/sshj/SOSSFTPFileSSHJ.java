package com.sos.vfs.sftp.sshj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommonProviderFile;

import net.schmizz.sshj.sftp.FileAttributes;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSFTPFileSSHJ extends SOSCommonProviderFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSFTPFileSSHJ.class);

    public SOSSFTPFileSSHJ(final String fileName) {
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

    @Override
    public InputStream getFileInputStream() {
        try {
            if (getInputStream() == null) {
                fileName = adjustRelativePathName(fileName);
                SOSSFTPSSHJ handler = (SOSSFTPSSHJ) getProvider();
                setInputStream(handler.getInputStream(fileName));
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
            SOSSFTPSSHJ handler = (SOSSFTPSSHJ) getProvider();
            FileAttributes attrs = handler.getFileAttributes(path);
            if (attrs == null) {
                throw new Exception("FileAttributes is null");
            }
            return attrs.getMtime() * 1_000L;
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", path, e.toString()), e);
            return -1L;
        }
    }

    @Override
    public long setModificationDateTime(final long millis) {
        if (millis <= 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][skip]setModificationDateTime=%s", fileName, millis));
            }
            return millis;
        }
        try {
            SOSSFTPSSHJ handler = (SOSSFTPSSHJ) getProvider();
            String path = adjustRelativePathName(fileName);
            FileAttributes attrs = handler.getFileAttributes(path);
            if (attrs == null) {
                throw new Exception("FileAttributes is null");
            }
            long seconds = millis / 1_000L;
            FileAttributes newattrs = new FileAttributes.Builder().withAtimeMtime(attrs.getAtime(), seconds).build();
            handler.getSFTPClient().setattr(path, newattrs);
            return millis;
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][%s]%s", fileName, millis, e.toString()), e);
            return -1L;
        }
    }

}