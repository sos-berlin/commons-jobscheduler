package com.sos.vfs.local;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSVFSMessageCodes;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.interfaces.ISOSProviderFile;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSLocalFile extends JSFile implements ISOSProviderFile {

    private static final long serialVersionUID = 7478704922673917684L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLocalFile.class);

    private static final String CLASS_NAME = SOSLocalFile.class.getSimpleName();
    private ISOSProvider provider = null;
    private InputStream is = null;
    private OutputStream os = null;
    private boolean append = false;

    public SOSLocalFile(final String path) {
        super(path);
    }

    @Override
    public boolean fileExists() throws Exception {
        return super.exists();
    }

    @Override
    public boolean directoryExists() throws Exception {
        return super.exists();
    }

    @Override
    public boolean delete(boolean checkIsDirectory) {
        try {
            getProvider().delete(getAbsolutePath(), checkIsDirectory);
        } catch (Throwable e) {
            throw new JobSchedulerException(e);
        }
        return true;
    }

    @Override
    public InputStream getFileInputStream() {
        final String method = CLASS_NAME + "::getFileInputStream";
        try {
            if (is == null) {
                is = new FileInputStream(new File(strFileName));
            }
        } catch (FileNotFoundException e) {
            String msg = SOSVFSMessageCodes.SOSVfs_E_134.params(method);
            throw new JobSchedulerException(msg + " / " + strFileName, e);
        }
        return is;
    }

    private OutputStream getFileOutputStream() {
        final String method = CLASS_NAME + "::getFileOutputStream";
        try {
            if (os == null) {
                os = new FileOutputStream(new File(strFileName), append);
            }
        } catch (FileNotFoundException e) {
            String msg = SOSVFSMessageCodes.SOSVfs_E_134.params(method);
            throw new JobSchedulerException(msg + " / " + strFileName, e);
        }
        return os;
    }

    @Override
    public long getFileSize() {
        return super.length();
    }

    @Override
    public ISOSProvider getProvider() {
        return provider;
    }

    @Override
    public void setProvider(final ISOSProvider val) {
        provider = val;
    }

    @Override
    public String getName() {
        return super.getPath();
    }

    @Override
    public boolean isDirectory() {
        return super.isDirectory();
    }

    @Override
    public void rename(final String newFileName) {
        getProvider().rename(getAbsolutePath(), newFileName);
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
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_134.params("GZip"), e);
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
        if (os != null) {
            this.closeOutput();
        } else if (is != null) {
            this.closeInput();
        }
    }

    @Override
    public void closeInput() {
        final String method = CLASS_NAME + "::closeInput";
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_134.params(method), e);
        } finally {
            is = null;
        }
    }

    @Override
    public void closeOutput() {
        final String method = CLASS_NAME + "::closeOutput";
        try {
            getFileOutputStream().flush();
            getFileOutputStream().close();
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_134.params(method), e);
        } finally {
            os = null;
        }
    }

    @Override
    public int read(final byte[] buffer) {
        final String method = CLASS_NAME + "::read";
        try {
            return getFileInputStream().read(buffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_134.params(method), e);
        }
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        final String method = CLASS_NAME + "::write";
        try {
            getFileOutputStream().write(buffer, offset, length);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_134.params(method), e);
        }
    }

    @Override
    public void write(final byte[] buffer) {
        final String method = CLASS_NAME + "::write";
        try {
            getFileOutputStream().write(buffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_134.params(method), e);
        }
    }

    @Override
    public String file2String() {
        InputStream is = getFileInputStream();
        if (is == null) {
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_265.get());
        }
        StringBuilder sb = new StringBuilder((int) getFileSize());
        byte[] buffer = new byte[1024];
        int bytes;
        try {
            while ((bytes = is.read(buffer)) != -1) {
                sb.append(new String(buffer).substring(0, bytes));
            }
            is.close();
            is = null;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_134.params("file2String"), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void setModeAppend(final boolean val) {
        append = val;
    }

    @Override
    public long getModificationDateTime() {
        try {
            // result = new File(strFileName).lastModified();
            FileTime ft = Files.getLastModifiedTime(Paths.get(strFileName));
            return ft.to(TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", strFileName, e.toString()), e);
            return -1L;
        }
    }

    @Override
    public long setModificationDateTime(final long millis) {
        if (millis <= 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][skip]setModificationDateTime=%s", strFileName, millis));
            }
            return millis;
        }
        try {
            // File file = new File(strFileName);
            // file.setLastModified(dateTime);
            Files.setLastModifiedTime(Paths.get(strFileName), FileTime.fromMillis(millis));
            return millis;
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]%s", strFileName, millis, e.toString()), e);
            return -1L;
        }
    }

    @Override
    public void setModeRestart(final boolean restart) {
    }

}