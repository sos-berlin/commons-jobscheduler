package com.sos.VirtualFileSystem.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSSession;
import com.sos.VirtualFileSystem.Interfaces.ISOSShellOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFileSystem;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFolder;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSCommandResult;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsZip extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsZip.class);
    private final InputStream objInputStream = null;
    private final OutputStream objOutputStream = null;
    private String strReplyString = "";
    private ZipFile objWorkingDirectory = null;
    public ZipOutputStream objZipOutputStream = null;
    private String strZipArchiveName = "";
    private String strCurrentEntryName = "";
    private SOSVfsZipFileEntry objCurrentZipFileEntry = null;
    private boolean simulateShell = false;

    public SOSVfsZip() {
        //
    }

    @Override
    public long appendFile(final String strSourceFileName, final String strTargetFileName) {
        JSFile objTargetFile = new JSFile(strTargetFileName);
        long lngFileSize = 0;
        try {
            lngFileSize = objTargetFile.appendFile(strSourceFileName);
        } catch (Exception e) {
            String strM = SOSVfs_E_134.params("appendFile()");
            throw new JobSchedulerException(strM, e);
        }
        return lngFileSize;
    }

    @Override
    public void ascii() {
        //
    }

    @Override
    public void binary() {
        //
    }

    @Override
    public boolean changeWorkingDirectory(final String pstrPathName) {
        boolean flgResult = true;
        try {
            if (objWorkingDirectory != null) {
                objWorkingDirectory.close();
            }
            strZipArchiveName = pstrPathName;
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
    public void delete(final String pathname) throws IOException {
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

    public ISOSVirtualFolder getFolder() {
        return null;
    }

    public ISOSVirtualFolder getFolder(final String pstrFolderName) {
        return null;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles() {
        Vector<ISOSVirtualFile> vecFiles = new Vector<ISOSVirtualFile>();
        for (Enumeration<? extends ZipEntry> e = objWorkingDirectory.entries(); e.hasMoreElements();) {
            ZipEntry entry = e.nextElement();
            String strZipEntryName = entry.getName();
            SOSVfsZipFileEntry objEntry = new SOSVfsZipFileEntry(strZipEntryName);
            objEntry.setZipEntry(entry);
            LOGGER.debug(SOSVfs_D_201.params(strZipEntryName));
            vecFiles.add(objEntry);
        }
        return vecFiles;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles(final String pstrFolderName) {
        changeWorkingDirectory(pstrFolderName);
        return getFiles();
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
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
    public String[] listNames(final String pathname) throws IOException {
        Vector<String> objV = nList(pathname);
        return objV.toArray(new String[objV.size()]);
    }

    @Override
    public void login(final String strUserName, final String strPassword) {
        //
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
    public Vector<String> nList(final String pathname) {
        changeWorkingDirectory(pathname);
        Vector<String> objV = new Vector<String>();
        for (Enumeration<? extends ZipEntry> e = objWorkingDirectory.entries(); e.hasMoreElements();) {
            ZipEntry entry = e.nextElement();
            String strZipEntryName = entry.getName();
            LOGGER.debug(SOSVfs_D_201.params(strZipEntryName));
            objV.add(strZipEntryName);
        }
        return objV;
    }

    @Override
    public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {
        return nList(pathname);
    }

    @Override
    public Vector<String> nList(final boolean recursive) throws Exception {
        return nList();
    }

    @Override
    public Vector<String> nList() throws Exception {
        notImplemented();
        return null;
    }

    @Override
    public int passive() {
        return 0;
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
    public void putFile(final ISOSVirtualFile objVirtualFile) {
        if (objZipOutputStream == null) {
            throw new JobSchedulerException(SOSVfs_E_202.get());
        }
        String strName = objVirtualFile.getName();
        strName = new File(strName).getAbsolutePath();
        if (strName.startsWith("c:")) {
            strName = strName.substring(3);
        }
        InputStream objFI = objVirtualFile.getFileInputStream();
        int lngBufferSize = 1024;
        byte[] buffer = new byte[lngBufferSize];
        int intBytesTransferred;
        long totalBytes = 0;
        try {
            synchronized (this) {
                while ((intBytesTransferred = objFI.read(buffer)) != -1) {
                    objZipOutputStream.write(buffer, 0, intBytesTransferred);
                    totalBytes += intBytesTransferred;
                }
                objFI.close();
                objZipOutputStream.closeEntry();
                LOGGER.debug(SOSVfs_D_203.params(objVirtualFile.getName(), strZipArchiveName, totalBytes));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_130.params("putFile()"), e);
        }
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) throws Exception {
        return 0;
    }

    @Override
    public ISOSConnection getConnection() {
        return null;
    }

    @Override
    public ISOSSession getSession() {
        return null;
    }

    @Override
    public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) throws IOException {
        notImplemented();
        new File(pobjFolderName.getValue()).mkdir();
        return null;
    }

    @Override
    public boolean rmdir(final SOSFolderName pobjFolderName) throws IOException {
        notImplemented();
        new File(pobjFolderName.getValue()).delete();
        return true;
    }

    @Override
    public SOSFileList dir(final SOSFolderName pobjFolderName) {
        String[] strEntryNames = getFilelist("", ".*", 1, true, null);
        SOSFileList objFL = new SOSFileList();
        objFL.add(strEntryNames, "");
        return objFL;
    }

    @Override
    public SOSFileList dir(final String pathname, final int flag) {
        notImplemented();
        return null;
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        //
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
    public String createScriptFile(final String pstrContent) throws Exception {
        return EMPTY_STRING;
    }

    @Override
    public Integer getExitCode() {
        return 0;
    }

    @Override
    public String getExitSignal() {
        return EMPTY_STRING;
    }

    @Override
    public StringBuffer getStdErr() throws Exception {
        return new StringBuffer();
    }

    @Override
    public StringBuffer getStdOut() throws Exception {
        return new StringBuffer();
    }

    @Override
    public boolean remoteIsWindowsShell() {
        return false;
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
        strReplyString = "230 Login successful.";
        return this;
    }

    @Override
    public void closeConnection() throws Exception {
        strReplyString = "ok";
    }

    @Override
    public ISOSConnection connect() throws Exception {
        strReplyString = "ok";
        return this;
    }

    @Override
    public ISOSConnection connect(final ISOSConnectionOptions pobjConnectionOptions) throws Exception {
        this.connect();
        return this;
    }

    @Override
    public ISOSConnection connect(final String pstrHostName, final int pintPortNumber) throws Exception {
        return null;
    }

    @Override
    public void closeSession() throws Exception {
        strReplyString = "221 Goodbye.";
    }

    @Override
    public ISOSSession openSession(final ISOSShellOptions pobjShellOptions) throws Exception {
        return null;
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
                objF.objOutputStream = objZipOutputStream;
                objF.objEntryOutputStream = objZipOutputStream;
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
    public String[] getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder, String integrityHashType) {
        String[] strS = null;
        try {
            Vector<String> objV = new Vector<String>();
            Pattern pattern = Pattern.compile(regexp, flag);
            Enumeration<?> zipEntries = objWorkingDirectory.entries();
            while (zipEntries.hasMoreElements()) {
                String strEntryName = ((ZipEntry) zipEntries.nextElement()).getName();
                if (integrityHashType != null && strEntryName.endsWith(integrityHashType)) {
                    continue;
                }
                Matcher matcher = pattern.matcher(strEntryName);
                if (matcher.find()) {
                    objV.add(strEntryName);
                }
            }
            strS = objV.toArray(new String[objV.size()]);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return strS;
    }

    @Override
    public String[] getFolderlist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
        String[] strS = null;
        try {
            Vector<String> objV = new Vector<String>();
            Pattern pattern = Pattern.compile(regexp, flag);
            Enumeration<?> zipEntries = objWorkingDirectory.entries();
            while (zipEntries.hasMoreElements()) {
                String strEntryName = ((ZipEntry) zipEntries.nextElement()).getName();
                Matcher matcher = pattern.matcher(strEntryName);
                if (matcher.find()) {
                    objV.add(strEntryName);
                }
            }
            strS = objV.toArray(new String[objV.size()]);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return strS;
    }

    @Override
    public void completePendingCommand() {
        // nothing to do
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) throws Exception {
        return null;
    }

    @Override
    public String doPWD() {
        return null;
    }

    @Override
    public OutputStream getAppendFileStream(final String strFileName) {
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
    public OutputStream getOutputStream(final String strFileName) {
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
    public void close() {
        if (objWorkingDirectory != null) {
            String strName = objWorkingDirectory.getName();
            try {
                objWorkingDirectory.close();
                LOGGER.debug(SOSVfs_D_204.params(strName));
                objWorkingDirectory = null;
            } catch (IOException e) {
                throw new JobSchedulerException(SOSVfs_E_205.params(strName));
            }
        } else {
            this.closeOutput();
        }
    }

    @Override
    public void closeInput() {
        //
    }

    @Override
    public void closeOutput() {
        if (objZipOutputStream != null) {
            try {
                objZipOutputStream.flush();
                objZipOutputStream.close();
                LOGGER.debug(SOSVfs_D_206.params(strZipArchiveName));
            } catch (IOException e) {
                throw new JobSchedulerException(SOSVfs_E_134.params("close()"), e);
            }
        }
    }

    @Override
    public void flush() {
        if (objZipOutputStream != null) {
            try {
                objZipOutputStream.flush();
            } catch (IOException e) {
                throw new JobSchedulerException(SOSVfs_E_134.params("flush()"), e);
            }
        }
    }

    @Override
    public int read(final byte[] bteBuffer) {
        return 0;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        return 0;
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        //
    }

    @Override
    public void write(final byte[] bteBuffer) {
        //
    }

    @Override
    public void openInputFile(final String pstrFileName) {
        //
    }

    @Override
    public void openOutputFile(final String pstrFileName) {
        //
    }

    @Override
    public ISOSVirtualFile transferMode(final SOSOptionTransferMode pobjFileTransferMode) {
        return null;
    }

    @Override
    public void rmdir(final String pstrFolderName) throws IOException {
        notImplemented();
    }

    @Override
    public void doPostLoginOperations() {
        //
    }

    @Override
    public ISOSConnection connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
        return null;
    }

    @Override
    public OutputStream getFileOutputStream() {
        return objOutputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return objOutputStream;
    }

    @Override
    public InputStream getInputStream() {
        return objInputStream;
    }

    @Override
    public SOSFileEntries getSOSFileEntries() {
        return sosFileEntries;
    }

    @Override
    public void reconnect(SOSConnection2OptionsAlternate options) {
        //
    }

    @Override
    public boolean isSimulateShell() {
        return this.simulateShell;
    }

    @Override
    public void setSimulateShell(boolean simulateShell) {
        this.simulateShell = simulateShell;
    }

    @Override
    public SOSCommandResult executePrivateCommand(String cmd) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}