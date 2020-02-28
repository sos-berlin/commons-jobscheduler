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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.DataElements.JSDataElementDateTime;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsLocalFile extends JSFile implements ISOSVirtualFile {

    private static final long serialVersionUID = 7478704922673917684L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsLocalFile.class);

    private static final String CLASS_NAME = SOSVfsLocalFile.class.getSimpleName();
    private ISOSVfsFileTransfer handler = null;
    private InputStream is = null;
    private OutputStream os = null;
    private boolean append = false;

    public SOSVfsLocalFile(final String path) {
        super(path);
    }

    @Override
    public boolean fileExists() throws Exception {
        return super.exists();
    }

    @Override
    public boolean delete(boolean checkIsDirectory) {
        super.delete();
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
        final String method = CLASS_NAME + "::getFileAppendStream";
        OutputStream aos = null;
        try {
            aos = new FileOutputStream(new File(strFileName), true);
        } catch (FileNotFoundException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(method), e);
        }
        return aos;
    }

    @Override
    public InputStream getFileInputStream() {
        final String method = CLASS_NAME + "::getFileInputStream";
        try {
            if (is == null) {
                is = new FileInputStream(new File(strFileName));
            }
        } catch (FileNotFoundException e) {
            String msg = SOSVfsMessageCodes.SOSVfs_E_134.params(method);
            LOGGER.error(msg, e);
            throw new JobSchedulerException(msg + " / " + strFileName, e);
        }
        return is;
    }

    @Override
    public OutputStream getFileOutputStream() {
        final String method = CLASS_NAME + "::getFileOutputStream";
        try {
            if (os == null) {
                os = new FileOutputStream(new File(strFileName), append);
            }
        } catch (FileNotFoundException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(method), e);
        }
        return os;
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
        return handler;
    }

    @Override
    public String getModificationTime() {
        Date date = new Date(super.lastModified());
        return new JSDataElementDateTime(date).getFormattedValue();
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
        ISOSVirtualFile file = new SOSVfsLocalFile(super.getParent());
        file.setHandler(getHandler());
        return file;
    }

    @Override
    public boolean isDirectory() {
        return super.isDirectory();
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
    public void putFile(final File file) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void putFile(final String fileName) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void rename(final String newFileName) {
        super.renameTo(new File(newFileName));
    }

    @Override
    public void setFilePermissions(final Integer permission) throws Exception {
        JSToolBox.notImplemented();
    }

    @Override
    public void setHandler(final ISOSVfsFileTransfer val) {
        handler = val;
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
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(method), e);
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
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(method), e);
        } finally {
            os = null;
        }
    }

    @Override
    public void flush() {
        final String method = CLASS_NAME + "::flush";
        try {
            getFileOutputStream().flush();
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(method), e);
        }

    }

    @Override
    public int read(final byte[] buffer) {
        final String method = CLASS_NAME + "::read";
        try {
            return getFileInputStream().read(buffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(method), e);
        }
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) {
        final String method = CLASS_NAME + "::read";
        try {
            return getFileInputStream().read(buffer, offset, length);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(method), e);
        }
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        final String method = CLASS_NAME + "::write";
        try {
            getFileOutputStream().write(buffer, offset, length);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(method), e);
        }
    }

    @Override
    public void write(final byte[] buffer) {
        final String method = CLASS_NAME + "::write";
        try {
            getFileOutputStream().write(buffer);
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params(method), e);
        }
    }

    @Override
    public void string2File(final String content) {
        try {
            OutputStream os = getFileOutputStream();
            os.write(content.getBytes());
            os.close();
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_130.params("String2File"), e);
        }

    }

    @Override
    public String file2String() {
        InputStream is = getFileInputStream();
        if (is == null) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_265.get());
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
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_134.params("file2String"), e);
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
    public void putFile(final ISOSVirtualFile file) throws Exception {
        boolean closed = false;
        try {
            byte[] buffer = new byte[4096];
            int bytes;
            synchronized (this) {
                while ((bytes = file.read(buffer)) != -1) {
                    try {
                        this.write(buffer, 0, bytes);
                    } catch (JobSchedulerException e) {
                        LOGGER.error(String.format("[break]%s", e.toString()), e);
                        break;
                    }
                }
            }
            file.closeInput();
            closeOutput();
            closed = true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_266.get(), e);
        } finally {
            if (!closed) {
                file.closeInput();
                closeOutput();
                closed = true;
            }
        }
    }

    @Override
    public void setModeAppend(final boolean val) {
        append = val;
    }

    @Override
    public long getModificationDateTime() {
        long result = 0L;
        try {
            result = new File(strFileName).lastModified();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            result = -1L;
        }
        return result;
    }

    @Override
    public long setModificationDateTime(final long dateTime) {
        long result = 0L;
        try {
            File file = new File(strFileName);
            file.setLastModified(dateTime);
            result = dateTime;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            result = -1L;
        }
        return result;
    }

    @Override
    public void setModeRestart(final boolean restart) {
    }

    @Override
    public void setModeOverwrite(final boolean mode) {
    }

}