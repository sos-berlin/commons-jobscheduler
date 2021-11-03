package com.sos.scheduler.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.vfs.common.SOSVFSFactory;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.scheduler.model.tools.PathResolver;

public class LiveConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveConnector.class);
    private final String liveFolder;
    private final ISOSProvider fileSystemHandler;
    private final ISOSProviderFile hotFolderHandle;
    private String workingDirectory;

    public LiveConnector(SOSOptionFolderName folderName) throws MalformedURLException {
        this(LiveConnector.getUrl(folderName.getValue()));
    }

    public LiveConnector(File folderName) throws MalformedURLException {
        this(LiveConnector.getUrl(folderName.getAbsolutePath()));
    }

    public LiveConnector(URL url) {
        this.fileSystemHandler = connect(url.toExternalForm());
        this.liveFolder = getUrl(url.toExternalForm()).getPath();
        setCurrentFolder(liveFolder);
        this.hotFolderHandle = fileSystemHandler.getFile(liveFolder);
    }

    public static URL getUrl(String urlPath) {
        URL result = null;
        String path = PathResolver.normalizePath(urlPath);
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        try {
            result = new URL(path);
        } catch (MalformedURLException e) {
            try {
                result = new URL("file://" + path);
            } catch (MalformedURLException e1) {
                throw new JobSchedulerException("the url " + urlPath + " is not valid.", e);
            }
        }
        return result;
    }

    public static String getPath(String urlPath) {
        String result = LiveConnector.getUrl(urlPath).getPath();
        if (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static ISOSProvider connect(String folder) {
        ISOSProvider result = null;
        try {
            SOSBaseOptions vfsOptions = new SOSBaseOptions();
            ISOSProvider vfs = SOSVFSFactory.getProvider(folder, vfsOptions.ssh_provider);
            if (vfs == null) {
                throw new JobSchedulerException();
            }
            result = vfs;
        } catch (Exception e) {
            throw new JobSchedulerException("error to connect folder " + folder, e);
        }
        return result;
    }

    public ISOSProviderFile getHotFolderHandle() {
        return hotFolderHandle;
    }

    public String getLiveFolder() {
        return liveFolder;
    }

    public String getCurrentFolder() {
        return workingDirectory;
    }

    public void setCurrentFolder(String directory) {
        String path = LiveConnector.getUrl(directory).getPath();
        if (!path.startsWith(getLiveFolder())) {
            String msgText = "the working directory " + path + " has to be a subfolder of " + getLiveFolder();
            LOGGER.error(msgText);
            throw new JobSchedulerException(msgText);
        }
        this.workingDirectory = path;
    }

    /** Gets the base folder for an order. An order like /folder/orderId bases on the root directory (e.g. the live folder). Orders with a relative name such as
     * ../folder/orderId based on the working directory.
     * 
     * @param baseName
     * @return */
    public String selectBaseFolder(String baseName) {
        return (baseName.startsWith("/")) ? getLiveFolder() : getCurrentFolder();
    }

    public ISOSProvider getFileSystemHandler() {
        return fileSystemHandler;
    }

}