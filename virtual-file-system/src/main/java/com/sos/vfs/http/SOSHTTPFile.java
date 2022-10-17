package com.sos.vfs.http;

import java.io.InputStream;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSCommonProviderFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSHTTPFile extends SOSCommonProviderFile {

    public SOSHTTPFile(final String path) {
        fileName = SOSCommonProvider.normalizePath(path);
    }

    @Override
    public int read(byte[] buffer) {
        try {
            InputStream is = getFileInputStream();

            if (is == null) {
                throw new JobSchedulerException(SOSVfs_E_177.get());
            }
            return is.read(buffer);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("read", this.fileName), e);
        }
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
        try {
            if (getOutputStream() == null) {
                setOutputStream(getProvider().getOutputStream(fileName, isModeAppend(), isModeRestart()));
            }
            if (getOutputStream() == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            getOutputStream().write(buffer, offset, length);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("write", fileName), e);
        }
    }

    @Override
    public void write(final byte[] buffer) {
        try {
            if (getOutputStream() == null) {
                setOutputStream(getProvider().getOutputStream(fileName, isModeAppend(), isModeRestart()));
            }
            if (getOutputStream() == null) {
                throw new Exception(SOSVfs_E_147.get());
            }
            getOutputStream().write(buffer);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_173.params("write", fileName), e);
        }
    }

    @Override
    public void closeInput() {
        super.closeInput();
        ((SOSHTTP) getProvider()).resetLastInputStreamGetMethod();
    }

    @Override
    public void closeOutput() {
        try {
            if (getOutputStream() != null) {
                getOutputStream().flush();
                getOutputStream().close();

                ((SOSHTTP) getProvider()).put(fileName);
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        } finally {
            setOutputStream(null);
        }
    }
}
