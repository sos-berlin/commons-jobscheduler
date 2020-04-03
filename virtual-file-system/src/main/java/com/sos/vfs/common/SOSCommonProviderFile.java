package com.sos.vfs.common;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSCommonProviderFile extends SOSCommonFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSCommonProviderFile.class);
    protected String fileName = EMPTY_STRING;

    public SOSCommonProviderFile() {
        super("SOSVirtualFileSystem");
    }

    public SOSCommonProviderFile(final String path) {
        this();
        fileName = adjustFileSeparator(path);
    }

    @Override
    public boolean fileExists() {
        boolean result = false;
        if (getProvider().getFileSize(fileName) >= 0) {
            result = true;
        }
        LOGGER.debug(String.format("[%s]%s", fileName, result));
        return result;
    }

    @Override
    public boolean delete(boolean checkIsDirectory) {
        try {
            getProvider().delete(fileName, checkIsDirectory);
        } catch (Exception e) {
            SOSVfs_E_158.get();
            throw new JobSchedulerException(SOSVfs_E_158.params("delete()", fileName), e);
        }
        return true;
    }

    @Override
    public InputStream getFileInputStream() {
        try {
            if (getInputStream() == null) {
                fileName = adjustRelativePathName(fileName);
                setInputStream(getProvider().getInputStream(fileName));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_158.params("getFileInputStream()", fileName), e);
        }
        return getInputStream();
    }

    protected String adjustRelativePathName(final String path) {
        return path.replaceAll("\\\\", "/");
    }

    @Override
    public long getFileSize() {
        long lngFileSize = -1;
        try {
            lngFileSize = getProvider().getFileSize(fileName);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("getFileSize()"), e);
        }
        return lngFileSize;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public boolean isDirectory() {
        return getProvider().isDirectory(fileName);
    }

    @Override
    public void rename(final String newPath) {
        getProvider().rename(fileName, newPath);
    }

    @Override
    public void setProvider(final ISOSProvider provider) {
        super.setProvider(provider);
    }

    @Override
    public void closeInput() {
        try {
            if (getInputStream() != null) {
                getInputStream().close();
                setInputStream(null);
            }
        } catch (Exception ex) {
            //
        }
    }

    @Override
    public void closeOutput() {
        try {
            if (getOutputStream() != null) {
                getOutputStream().flush();
                getOutputStream().close();
                setOutputStream(null);
            }
        } catch (Exception ex) {
            //
        }
    }

    @Override
    public long setModificationDateTime(final long dateTime) {
        return 0;
    }

    @Override
    public long getModificationDateTime() {
        return 0;
    }

}