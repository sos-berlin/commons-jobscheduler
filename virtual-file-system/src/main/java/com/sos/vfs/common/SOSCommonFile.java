package com.sos.vfs.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSCommonFile extends SOSVFSMessageCodes implements ISOSProviderFile {

    private ISOSProvider provider = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private ZipOutputStream entryOutputStream = null;

    private boolean modeAppend = false;  // Append Mode for output file
    private boolean modeRestart = false;

    public SOSCommonFile() {
        //
    }

    public SOSCommonFile(final String name) {
        super(name);
    }

    @Override
    public void setProvider(final ISOSProvider val) {
        provider = val;
    }

    @Override
    public ISOSProvider getProvider() {
        return provider;
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
    public InputStream getFileInputStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getFileSize() {
        // TODO Auto-generated method stub
        return 0;
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
    public boolean delete(boolean checkIsDirectory) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void rename(final String newPath) {
        // TODO Auto-generated method stub
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
    public void closeInput() {
        // TODO Auto-generated method stub
    }

    @Override
    public void closeOutput() {
        // TODO Auto-generated method stub
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

}
