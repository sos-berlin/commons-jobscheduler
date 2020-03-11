package com.sos.VirtualFileSystem.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSVfsCommonFile extends SOSVfsMessageCodes implements ISOSVirtualFile {

    private ISOSVfsFileTransfer handler = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private ZipOutputStream entryOutputStream = null;

    private boolean modeAppend = false;  // Append Mode for output file
    private boolean modeRestart = false;

    public SOSVfsCommonFile() {
        //
    }

    public SOSVfsCommonFile(final String name) {
        super(name);
    }

    @Override
    public void setHandler(final ISOSVfsFileTransfer val) {
        handler = val;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream is) {
        inputStream = is;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream os) {
        outputStream = os;
    }

    public ZipOutputStream getEntryOutputStream() {
        return entryOutputStream;
    }

    public void setEntryOutputStream(ZipOutputStream os) {
        entryOutputStream = os;
    }

    @Override
    public ISOSVfsFileTransfer getHandler() {
        return handler;
    }

    @Override
    public String file2String() {
        InputStream is = this.getFileInputStream();
        if (is == null) {
            throw new JobSchedulerException("input stream is null");
        }
        StringBuilder sb = new StringBuilder((int) this.getFileSize());
        byte[] buffer = new byte[1024];
        int bytes;
        try {
            while ((bytes = is.read(buffer)) != -1) {
                sb.append(new String(buffer).substring(0, bytes));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.get("File2String()"), e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }

    @Override
    public void string2File(final String content) {
        OutputStream os = null;
        try {
            os = this.getFileOutputStream();
            os.write(content.getBytes());
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_130.get("String2File"), e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void putFile(final File file) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void putFile(final String path) throws Exception {
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
    public void setFilePermissions(final Integer val) throws Exception {
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
    public boolean delete(boolean checkIsDirectory) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void rename(final String newPath) {
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
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        // TODO Auto-generated method stub
    }

    @Override
    public void write(final byte[] buffer) {
        // TODO Auto-generated method stub
    }

    @Override
    public int read(final byte[] buffer) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) {
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

    @Override
    public void deleteFile() {
        this.delete(true);
    }

    @Override
    public ISOSVirtualFile getFile() {
        return this;
    }

    @Override
    public void setModeAppend(final boolean val) {
        modeAppend = val;
    }

    public boolean isModeAppend() {
        return modeAppend;
    }

    @Override
    public void setModeRestart(final boolean val) {
        modeRestart = val;
    }

    public boolean isModeRestart() {
        return modeRestart;
    }

    @Override
    public void setModeOverwrite(final boolean val) {

    }

}
