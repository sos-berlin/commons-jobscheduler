package com.sos.VirtualFileSystem.FTP;

import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer2;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsSuperClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpBaseClass2 extends SOSVfsFtpBaseClass implements ISOSVfsFileTransfer2 {

    protected Vector<SOSFileListEntry> objFileListEntries = null;
    private static final Logger LOGGER = Logger.getLogger(SOSVfsFtpBaseClass2.class);

    public SOSVfsFtpBaseClass2() {
        super();
    }

    @Override
    public void clearFileListEntries() {
        objFileListEntries = new Vector<SOSFileListEntry>();
    }

    @Override
    public void connect(final String phost, final int pport) {
        final String conMethodName = "SOSVfsFtpBaseClass2::connect";
        try {
            host = phost;
            port = pport;
            String strM = SOSVfs_D_0101.params(host, port);
            LOGGER.debug(strM);
            if (!isConnected()) {
                Client().connect(host, port);
                LOGGER.info(SOSVfs_D_0102.params(host, port));
                LogReply();
            } else {
                LOGGER.warn(SOSVfs_D_0103.params(host, port));

            }
        } catch (Exception e) {
            RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
        }
    }

    @Override
    public final ISOSConnection Connect() {
        final String conMethodName = "SOSVfsFtpBaseClass2::Connect";
        String strH = host = objConnectionOptions.getHost().Value();
        int intP = port = objConnectionOptions.getPort().value();
        LOGGER.debug(SOSVfs_D_0101.params(strH, intP));
        try {
            this.connect(strH, intP);
            LOGGER.info(SOSVfs_D_0102.params(strH, intP));
        } catch (RuntimeException e) {
            LOGGER.info(SOSVfs_E_0107.params(host, port) + e.getMessage());
            String strAltHost = host = objConnectionOptions.getalternative_host().Value();
            int intAltPort = port = objConnectionOptions.getalternative_port().value();
            if (isNotEmpty(strAltHost) && intAltPort > 0) {
                LOGGER.debug(SOSVfs_D_0101.params(strAltHost, intAltPort));
                this.connect(strAltHost, intAltPort);
                LOGGER.info(SOSVfs_D_0102.params(strAltHost, intAltPort));
            } else {
                LOGGER.info(SOSVfs_E_0107.params(host, port, e.getMessage()));
                RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
            }
        }
        return this;
    }

    @Deprecated
    @Override
    public ISOSConnection Connect(final ISOSConnectionOptions pobjConnectionOptions) throws Exception {
        final String conMethodName = "SOSVfsFtpBaseClass2::Connect";
        objConnectionOptions = pobjConnectionOptions;
        try {
            host = objConnectionOptions.getHost().Value();
            port = objConnectionOptions.getPort().value();
            this.connect(host, port);
        } catch (Exception e) {
            RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return this;
    }

    @Override
    public final ISOSConnection Connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) {
        final String conMethodName = "SOSVfsFtpBaseClass2::Connect";
        objConnection2Options = pobjConnectionOptions;
        try {
            objHost = objConnection2Options.getHost();
            objPort = objConnection2Options.getport();
            this.connect(objHost.Value(), objPort.value());
            if (!Client().isConnected()) {
                SOSConnection2OptionsSuperClass objAlternate = objConnection2Options.Alternatives();
                objHost = objAlternate.host;
                objPort = objAlternate.port;
                LOGGER.info(SOSVfs_I_0121.params(host));
                this.connect(objHost.Value(), objPort.value());
                if (!Client().isConnected()) {
                    objHost = null;
                    objPort = null;
                    host = "";
                    port = -1;
                    RaiseException(SOSVfs_E_204.get());
                }
            }
        } catch (Exception e) {
            RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
        }
        return this;
    }

    @Override
    public ISOSConnection Connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
        return null;
    }

    @Override
    public ISOSConnection Connect(final String pstrHostName, final int pintPortNumber) throws Exception {
        this.connect(pstrHostName, pintPortNumber);
        if (objConnectionOptions != null) {
            objConnectionOptions.getHost().Value(pstrHostName);
            objConnectionOptions.getPort().value(pintPortNumber);
        }
        return this;
    }

    @Override
    public Vector<SOSFileListEntry> getFileListEntries() {
        if (objFileListEntries == null) {
            objFileListEntries = new Vector<SOSFileListEntry>();
        }
        return objFileListEntries;
    }

    @Override
    public SOSFileList getFileListEntries(final SOSFileList pobjSOSFileList, final String folder, final String regexp, final boolean flgRecurseSubFolder) {
        getFilenames(folder, flgRecurseSubFolder, regexp);
        for (SOSFileListEntry objEntry : objFileListEntries) {
            pobjSOSFileList.add(objEntry);
        }
        return pobjSOSFileList;
    }

    protected Vector<String> getFilenames(final String pstrPathName, final boolean flgRecurseSubFolders, final String regexp) {
        String strCurrentDirectory = null;
        Vector<String> vecDirectoryListing = new Vector<String>();
        String lstrPathName = pstrPathName.trim();
        strCurrentDirectory = lstrPathName;
        if (lstrPathName.isEmpty()) {
            lstrPathName = ".";
        }
        if (".".equals(lstrPathName)) {
            lstrPathName = DoPWD();
            strCurrentDirectory = lstrPathName;
        }
        FTPFile[] objFTPFileList = null;
        getFileListEntries();
        try {
            LOGGER.debug(String.format("start directory scan for '%1$s'", lstrPathName));
            Client().setListHiddenFiles(false);
            objFTPFileList = Client().listFiles(lstrPathName);
        } catch (IOException e1) {
            throw new JobSchedulerException("listfiles failed", e1);
        }
        if (objFTPFileList == null || objFTPFileList.length <= 0) {
            return vecDirectoryListing;
        }
        Pattern pattern = null;
        if (isNotEmpty(regexp)) {
            pattern = Pattern.compile(regexp, 0);
        }
        strCurrentDirectory = addFileSeparator(strCurrentDirectory);
        for (FTPFile objFTPFile : objFTPFileList) {
            String strCurrentFile = objFTPFile.getName();
            if (isNotHiddenFile(strCurrentFile) && !strCurrentFile.trim().isEmpty()) {
                boolean flgIsDirectory = objFTPFile.isDirectory();
                if (!flgIsDirectory) {
                    boolean flgSelected = true;
                    if (pattern != null) {
                        Matcher matcher = pattern.matcher(strCurrentFile);
                        flgSelected = matcher.find();
                    }
                    if (flgSelected) {
                        if (!strCurrentFile.startsWith(strCurrentDirectory)) {
                            strCurrentFile = strCurrentDirectory + strCurrentFile;
                        }
                        vecDirectoryListing.add(strCurrentFile);
                        objFTPFile.setName(strCurrentFile);
                        SOSFileListEntry objF = new SOSFileListEntry(objFTPFile);
                        objF.VfsHandler(this);
                        objF.Options(objOptions);
                        objFileListEntries.add(objF);
                    }
                } else {
                    if (flgIsDirectory && flgRecurseSubFolders) {
                        Vector<String> vecNames = getFilenames(strCurrentDirectory + strCurrentFile, flgRecurseSubFolders, regexp);
                        if (vecNames != null & !vecNames.isEmpty()) {
                            vecDirectoryListing.addAll(vecNames);
                        }
                    }
                }
            }
        }
        return vecDirectoryListing;
    }

    @Override
    public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {
        try {
            return getFilenames(pathname, flgRecurseSubFolder, null);
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_128.params("getfilenames", "nLixt"), e);
        }
    }

    @Override
    public long size(final String pstrRemoteFileName) throws Exception {
        long lngSize = -1L;
        getFileListEntries();
        for (SOSFileListEntry objFileEntry : objFileListEntries) {
            if (objFileEntry.getSourceFilename().equalsIgnoreCase(pstrRemoteFileName)) {
                lngSize = objFileEntry.getFileSize();
                break;
            }
        }
        if (lngSize == -1L) {
            FTPFile objF = getFTPFile(pstrRemoteFileName);
            if (objF != null) {
                lngSize = objF.getSize();
            }
        }
        return lngSize;
    }
    
}