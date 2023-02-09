package com.sos.vfs.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.webdav.common.ISOSWebDAV;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSWebDAV extends SOSCommonProvider implements ISOSWebDAV {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSWebDAV.class);

    public enum WebDAVProvider {
        WEBDAVCLIENT4J, JACKRABBIT
    }

    private ISOSWebDAV provider;

    public SOSWebDAV(SOSOptionString providerOption) {
        setProvider(providerOption);
    }

    private void setProvider(SOSOptionString providerOption) {
        if (providerOption == null) {
            SOSBaseOptions baseOptions = new SOSBaseOptions();
            providerOption = baseOptions.webdav_provider;
        }
        String webDavProvider = providerOption.getValue().toUpperCase();
        if (webDavProvider.equals(WebDAVProvider.WEBDAVCLIENT4J.name())) {
            provider = new com.sos.vfs.webdav.webdavclient4j.SOSWebDAV();
        } else {
            provider = new com.sos.vfs.webdav.jackrabbit.SOSWebDAV();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("provider=%s", provider.getClass().getName()));
        }
    }

    @Override
    public boolean isConnected() {
        return provider.isConnected();
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        provider.connect(options);
    }

    @Override
    public void disconnect() {
        provider.disconnect();
    }

    @Override
    public void mkdir(final String path) {
        provider.mkdir(path);
    }

    @Override
    public void rmdir(final String path) {
        provider.rmdir(path);
    }

    @Override
    public boolean fileExists(final String filename) {
        return provider.fileExists(filename);
    }

    @Override
    public boolean directoryExists(final String filename) {
        return provider.directoryExists(filename);
    }

    @Override
    public boolean isDirectory(final String filename) {
        return provider.isDirectory(filename);
    }

    @Override
    public long size(String filename) throws Exception {
        return provider.size(filename);
    }

    @Override
    public SOSFileEntry getFileEntry(String pathname) throws Exception {
        return provider.getFileEntry(pathname);
    }

    @Override
    public List<SOSFileEntry> listNames(String path, int maxFiles, boolean checkIfExists, boolean checkIfIsDirectory) {
        return provider.listNames(path, maxFiles, checkIfExists, checkIfIsDirectory);
    }

    @Override
    public void delete(final String path, boolean checkIsDirectory) {
        provider.delete(path, checkIsDirectory);
    }

    @Override
    public void rename(String from, String to) {
        provider.rename(from, to);
    }

    @Override
    public void executeCommand(String cmd) {
        // LOGGER.debug("not implemented yet");
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        return provider.getInputStream(fileName);
    }

    @Override
    public OutputStream getOutputStream(String fileName, boolean append, boolean resume) {
        return provider.getOutputStream(fileName, append, resume);
    }

    @Override
    public ISOSProviderFile getFile(String fileName) {
        return provider.getFile(fileName);
    }

    @Override
    public String getModificationDateTime(final String path) {
        return provider.getModificationDateTime(path);
    }

    @Override
    public boolean isSFTP() {
        return false;
    }

    @Override
    public long copy(String source, String target) {
        return provider.copy(source, target);
    }
}
