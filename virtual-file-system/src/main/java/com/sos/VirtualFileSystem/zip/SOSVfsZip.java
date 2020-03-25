package com.sos.VirtualFileSystem.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSTransferHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSDestinationOptions;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSFileEntry.EntryType;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsZip extends SOSVfsBaseClass implements ISOSTransferHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsZip.class);
    private String strReplyString = "";
    private ZipFile objWorkingDirectory = null;
    public ZipOutputStream objZipOutputStream = null;
    private String strCurrentEntryName = "";
    private SOSVfsZipFileEntry objCurrentZipFileEntry = null;
    private boolean simulateShell = false;

    public SOSVfsZip() {
        //
    }

    @Override
    public boolean changeWorkingDirectory(final String pstrPathName) {
        boolean flgResult = true;
        try {
            if (objWorkingDirectory != null) {
                objWorkingDirectory.close();
            }
            JSFile objFile = new JSFile(pstrPathName);
            if (this.isTarget()) {
                if (objFile.exists()) {
                    objFile.delete();
                }
                objZipOutputStream = new ZipOutputStream(new FileOutputStream(pstrPathName));
                flgResult = true;
                LOGGER.debug(SOSVfs_D_200.params(pstrPathName, "write"));
            } else {
                if (objFile.exists()) {
                    objWorkingDirectory = new ZipFile(objFile, ZipFile.OPEN_READ);
                    flgResult = true;
                    LOGGER.debug(SOSVfs_D_200.params(pstrPathName, "read"));
                }
            }
        } catch (IOException e) {
            throw new JobSchedulerException(e);
        }
        return flgResult;
    }

    @Override
    public void delete(final String pathname, boolean checkIsDirectory) throws IOException {
        throw new JSNotImplementedException();
    }

    @Override
    public void disconnect() throws IOException {
        //
    }

    @Override
    public long getFile(final String pstrSourceFileName, final String pstrTargetFileName, final boolean append) throws Exception {
        long lngFileSize = 0;
        notImplemented();
        return lngFileSize;
    }

    @Override
    public long getFile(final String remoteFile, final String localFile) throws Exception {
        return 0;
    }

    @Override
    public String getReplyString() {
        return strReplyString;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public List<SOSFileEntry> listNames(final String pathname, boolean checkIfExists, boolean checkIfIsDirector) throws IOException {
        return nList(pathname, false, checkIfExists);
    }

    @Override
    public void logout() throws IOException {
        //
    }

    @Override
    public void mkdir(final String pathname) throws IOException {
        notImplemented();
    }

    @Override
    public List<SOSFileEntry> nList(final String pathname, final boolean flgRecurseSubFolder, boolean checkIfExists) {
        changeWorkingDirectory(pathname);
        List<SOSFileEntry> list = new ArrayList<SOSFileEntry>();
        for (Enumeration<? extends ZipEntry> e = objWorkingDirectory.entries(); e.hasMoreElements();) {
            ZipEntry zipEntry = e.nextElement();

            SOSFileEntry entry = new SOSFileEntry(EntryType.ZIP);
            entry.setFilename(zipEntry.getName());
            entry.setFilesize(zipEntry.getSize());
            entry.setDirectory(zipEntry.isDirectory());
            entry.setParentPath(pathname);

            list.add(entry);
        }
        return list;
    }

    @Override
    public void put(final String localFile, final String remoteFile) {
        //
    }

    @Override
    public long putFile(final String localFile, final OutputStream out) {
        return 0;
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) throws Exception {
        return 0;
    }

    @Override
    public void executeCommand(final String strCmd) throws Exception {
        executeCommand(strCmd, null);
    }

    @Override
    public void executeCommand(final String strCmd, SOSVfsEnv env) throws Exception {
        //
    }

    @Override
    public Integer getExitCode() {
        return 0;
    }

    @Override
    public StringBuilder getStdErr() {
        return new StringBuilder();
    }

    @Override
    public StringBuilder getStdOut() {
        return new StringBuilder();
    }

    @Override
    public void login(final ISOSAuthenticationOptions pobjAO) throws Exception {
        strReplyString = "230 Login successful.";
    }

    @Override
    public ISOSVirtualFile getFileHandle(final String pstrFileName) {
        if (pstrFileName.equalsIgnoreCase(strCurrentEntryName)) {
            return objCurrentZipFileEntry;
        }
        String strEncodedEntryName = pstrFileName;
        strEncodedEntryName = new File(strEncodedEntryName).getName();
        SOSVfsZipFileEntry objF = new SOSVfsZipFileEntry(strEncodedEntryName);
        ZipEntry objZE = null;
        if (objZipOutputStream == null || !this.isTarget()) {
            objZE = objWorkingDirectory.getEntry(strEncodedEntryName);
            if (objZE != null) {
                objF.setHandler(this);
            } else {
                objZE = objWorkingDirectory.getEntry(pstrFileName);
                if (objZE != null) {
                    objF.setHandler(this);
                }
            }
        } else {
            objZE = new ZipEntry(strEncodedEntryName);
            try {
                objZE.setComment("created by com.sos.SOSVirtualFileSystem.SOSVfsZip, locale " + Locale.getDefault());
                objZE.setMethod(ZipEntry.DEFLATED);
                objZipOutputStream.putNextEntry(objZE);
                objF.setOutputStream(objZipOutputStream);
                objF.setEntryOutputStream(objZipOutputStream);
                objF.setHandler(this);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        objF.setHandler(this);
        objF.setZipEntry(objZE);
        objCurrentZipFileEntry = objF;
        strCurrentEntryName = pstrFileName;
        return objF;
    }

    @Override
    public boolean isNegativeCommandCompletion() {
        return false;
    }

    @Override
    public List<SOSFileEntry> getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder,
            boolean checkIfExists, String integrityHashType) {
        List<SOSFileEntry> list = new ArrayList<>();
        try {
            Pattern pattern = Pattern.compile(regexp, flag);
            Enumeration<?> zipEntries = objWorkingDirectory.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();

                SOSFileEntry sosFileEntry = new SOSFileEntry(EntryType.ZIP);
                sosFileEntry.setDirectory(zipEntry.isDirectory());
                sosFileEntry.setFilename(zipEntry.getName());
                sosFileEntry.setFilesize(zipEntry.getSize());
                sosFileEntry.setParentPath(folder);

                if (integrityHashType != null && sosFileEntry.getFilename().endsWith(integrityHashType)) {
                    continue;
                }
                Matcher matcher = pattern.matcher(sosFileEntry.getFilename());
                if (matcher.find()) {
                    list.add(sosFileEntry);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return list;
    }

    @Override
    public List<SOSFileEntry> getFolderlist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
        List<SOSFileEntry> list = new ArrayList<>();
        try {
            Pattern pattern = Pattern.compile(regexp, flag);
            Enumeration<?> zipEntries = objWorkingDirectory.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
                Matcher matcher = pattern.matcher(zipEntry.getName());
                if (matcher.find()) {
                    SOSFileEntry sosFileEntry = new SOSFileEntry(EntryType.ZIP);
                    sosFileEntry.setDirectory(zipEntry.isDirectory());
                    sosFileEntry.setFilename(zipEntry.getName());
                    sosFileEntry.setFilesize(zipEntry.getSize());
                    sosFileEntry.setParentPath(folder);
                    list.add(sosFileEntry);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return list;
    }

    @Override
    public void connect(final SOSDestinationOptions pobjConnectionOptions) throws Exception {
    }

    @Override
    public String doPWD() {
        return null;
    }

    @Override
    public long getFileSize(final String strFileName) {
        return 0;
    }

    @Override
    public InputStream getInputStream(final String strFileName) {
        ZipEntry objZE = objWorkingDirectory.getEntry(strFileName);
        InputStream objI = null;
        if (objZE != null) {
            try {
                objI = objWorkingDirectory.getInputStream(objZE);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                throw new JobSchedulerException(e);
            }
        }
        return objI;
    }

    @Override
    public String getModificationTime(final String strFileName) {
        return null;
    }

    @Override
    public OutputStream getOutputStream(final String strFileName, boolean append, boolean resume) {
        ZipOutputStream objZOS = null;
        ZipEntry objZE = objWorkingDirectory.getEntry(strFileName);
        if (objZE != null) {
            try {
                FileOutputStream objFOS = new FileOutputStream(objWorkingDirectory.getName());
                objZOS = new ZipOutputStream(objFOS);
                objZOS.putNextEntry(objZE);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                throw new JobSchedulerException(e);
            }
        }
        return objZOS;
    }

    @Override
    public boolean isDirectory(final String strFileName) {
        return false;
    }

    @Override
    public void rename(final String strFileName, final String pstrNewFileName) {
        //
    }

    @Override
    public void rmdir(final String pstrFolderName) throws IOException {
        notImplemented();
    }

    @Override
    public void reconnect(SOSDestinationOptions options) {
        //
    }

    @Override
    public boolean isSimulateShell() {
        return simulateShell;
    }

    @Override
    public void setSimulateShell(boolean val) {
        simulateShell = val;
    }

    @Override
    public SOSFileEntry getFileEntry(final String pathname) throws Exception {
        notImplemented();
        return null;
    }

}