package com.sos.VirtualFileSystem.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.JSHelper.io.Files.JSFile;
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
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSFileEntry.EntryType;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.VirtualFileSystem.shell.CmdShell;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSFile;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsLocal extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsLocal.class);
    private final InputStream objInputStream = null;
    private final OutputStream objOutputStream = null;
    private SOSConnection2OptionsAlternate connection2OptionsAlternate = null;
    private String strReplyString = "";
    private CmdShell objCmdShell = null;
    private boolean simulateShell = false;

    //
    @Override
    public long appendFile(final String strSourceFileName, final String strTargetFileName) {
        JSFile objTargetFile = new JSFile(strTargetFileName);
        long lngFileSize = 0;
        try {
            lngFileSize = objTargetFile.appendFile(strSourceFileName);
        } catch (Exception e) {
            String strM = SOSVfs_E_134.params("appendFile()");
            LOGGER.error(strM, e);
            throw new JobSchedulerException(strM, e);
        }
        return lngFileSize;
    }

    @Override
    public void ascii() {
        //
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
        strReplyString = "230 Login successful.";
        return this;
    }

    @Override
    public void binary() {
        //
    }

    @Override
    public boolean changeWorkingDirectory(final String pstrPathName) {
        boolean flgResult = true;
        File fleFile = new File(pstrPathName);
        if (fleFile.exists()) {
            if (fleFile.isDirectory()) {
                // objWorkingDirectory = new File(pstrPathName);
            } else {
                flgResult = false;
            }
        } else {
            flgResult = false;
        }
        return flgResult;
    }

    @Override
    public void close() {
        //
    }

    @Override
    public void closeConnection() throws Exception {
        strReplyString = "ok";
    }

    @Override
    public void closeInput() {
        //
    }

    @Override
    public void closeOutput() {
        //
    }

    @Override
    public void closeSession() throws Exception {
        strReplyString = "221 Goodbye.";
    }

    @Override
    public void completePendingCommand() {
        //
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
    public ISOSConnection connect(final SOSConnection2OptionsAlternate options) throws Exception {
        connection2OptionsAlternate = options;
        return null;
    }

    @Override
    public ISOSConnection connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
        return null;
    }

    @Override
    public ISOSConnection connect(final String pstrHostName, final int pintPortNumber) throws Exception {
        return null;
    }

    @Override
    public String createScriptFile(final String pstrContent) throws Exception {
        return EMPTY_STRING;
    }

    @Override
    public void delete(final String pathname, boolean checkIsDirectory) throws IOException {
        File objF = new File(getRealFileName(pathname));
        objF.delete();
    }

    @Override
    public List<SOSFileEntry> dir(final SOSFolderName pobjFolderName) {
        return null;
    }

    @Override
    public List<SOSFileEntry> dir(final String pathname, final int flag) {
        return null;
    }

    @Override
    public void disconnect() throws IOException {
        //
    }

    @Override
    public void doPostLoginOperations() {
        //
    }

    @Override
    public String doPWD() {
        return null;
    }

    @Override
    public void executeCommand(final String cmd) throws Exception {
        executeCommand(cmd, null);
    }

    @Override
    public void executeCommand(final String cmd, SOSVfsEnv env) throws Exception {
        if (objCmdShell == null) {
            objCmdShell = new CmdShell();
        }
        String command = cmd.trim();
        if (objCmdShell.isWindows()) {
            command = objCmdShell.replaceCommand4Windows(command);
        }
        int exitCode = objCmdShell.executeCommand(command, env);
        if (exitCode != 0) {
            boolean raiseException = true;
            if (connection2OptionsAlternate != null) {
                raiseException = connection2OptionsAlternate.raiseExceptionOnError.value();
            }
            if (raiseException) {
                throw new JobSchedulerException(SOSVfs_E_191.params(exitCode + ""));
            } else {
                LOGGER.info(SOSVfs_D_151.params(command, SOSVfs_E_191.params(exitCode + "")));
            }
        }
    }

    public CmdShell getCmdShell() {
        if (objCmdShell == null) {
            objCmdShell = new CmdShell();
        }
        return objCmdShell;
    }

    @Override
    public void flush() {
        //
    }

    @Override
    public OutputStream getAppendFileStream(final String strFileName) {
        return null;
    }

    @Override
    public ISOSConnection getConnection() {
        return null;
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
    public long getFile(final String remoteFile, final String localFile) throws Exception {
        return 0;
    }

    @Override
    public long getFile(final String pstrSourceFileName, final String pstrTargetFileName, final boolean append) throws Exception {
        long lngFileSize = 0;
        if (!append) {
            JSFile objF = new JSFile(pstrSourceFileName);
            lngFileSize = objF.length();
            objF.copy(pstrTargetFileName);
        } else {
            lngFileSize = this.appendFile(pstrSourceFileName, pstrTargetFileName);
        }
        return lngFileSize;
    }

    @Override
    public ISOSVirtualFile getFileHandle(final String pstrFileName) {
        SOSVfsLocalFile objF = new SOSVfsLocalFile(pstrFileName);
        objF.setHandler(this);
        return objF;
    }

    @Override
    public SOSFileEntry getFileEntry(final String pathname) throws Exception {
        Path path = Paths.get(pathname);
        SOSFileEntry entry = new SOSFileEntry(EntryType.FILESYSTEM);
        entry.setDirectory(Files.isDirectory(path));
        entry.setFilename(path.getFileName().toString());
        entry.setFilesize(path.toFile().length());
        entry.setParentPath(path.getParent().toString());
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(final String pathname, boolean checkIfExists, boolean checkIfIsDirector) throws IOException {
        File objF = new File(pathname);
        File[] objA = objF.listFiles();
        List<SOSFileEntry> list = new ArrayList<SOSFileEntry>();
        for (File file : objA) {
            SOSFileEntry sosFileEntry = new SOSFileEntry(EntryType.FILESYSTEM);
            sosFileEntry.setDirectory(file.isDirectory());
            sosFileEntry.setFilename(file.getName());
            sosFileEntry.setFilesize(file.length());
            sosFileEntry.setParentPath(file.getParent());
            list.add(sosFileEntry);
        }
        return list;
    }

    @Override
    public List<SOSFileEntry> getFilelist(final String folder, final String regexp, final int flag, final boolean pflgRecurseSubFolder,
            boolean checkIfExists, String integrityHashType) {
        List<SOSFileEntry> list = new ArrayList<SOSFileEntry>();
        try {
            Vector<File> objA = SOSFile.getFolderlist(folder, regexp, flag, pflgRecurseSubFolder);
            for (File objF : objA) {
                if (objF.isDirectory()) {
                    continue;
                }
                if (integrityHashType != null && objF.getName().endsWith(integrityHashType)) {
                    continue;
                }
                SOSFileEntry entry = new SOSFileEntry(EntryType.FILESYSTEM);
                entry.setFilename(objF.getName());
                entry.setFilesize(objF.length());
                entry.setDirectory(objF.isDirectory());
                entry.setParentPath(objF.getParent());
                list.add(entry);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return list;
    }

    @Override
    public List<SOSFileEntry> getFolderlist(final String folder, final String regexp, final int flag, final boolean pflgRecurseSubFolder) {
        List<SOSFileEntry> list = new ArrayList<SOSFileEntry>();
        try {
            Vector<File> objA = SOSFile.getFolderlist(folder, regexp, flag, pflgRecurseSubFolder);
            for (File objF : objA) {
                if (objF.isDirectory()) {
                    SOSFileEntry entry = new SOSFileEntry(EntryType.FILESYSTEM);
                    entry.setFilename(objF.getName());
                    entry.setFilesize(objF.length());
                    entry.setDirectory(objF.isDirectory());
                    entry.setParentPath(objF.getParent());
                    list.add(entry);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return list;
    }

    @Override
    public OutputStream getFileOutputStream() {
        return objOutputStream;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles() {
        notImplemented();
        return null;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles(final String string) {
        notImplemented();
        return null;
    }

    @Override
    public long getFileSize(final String strFileName) {
        return 0;
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
    }

    @Override
    public InputStream getInputStream() {
        return objInputStream;
    }

    @Override
    public InputStream getInputStream(final String strFileName) {
        return null;
    }

    @Override
    public String getModificationTime(final String strFileName) {
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public OutputStream getOutputStream(final String strFileName) {
        return null;
    }

    private String getRealFileName(final String pstrPathname) {
        return pstrPathname;
    }

    @Override
    public String getReplyString() {
        return strReplyString;
    }

    @Override
    public ISOSSession getSession() {
        return null;
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
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isDirectory(final String strFileName) {
        return new File(strFileName).isDirectory();
    }

    @Override
    public boolean isNegativeCommandCompletion() {
        return false;
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
    public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) throws IOException {
        new File(pobjFolderName.getValue()).mkdir();
        return null;
    }

    @Override
    public void mkdir(final String pathname) throws IOException {
        File objF = new File(pathname);
        if (!objF.exists()) {
            objF.mkdirs();
        } else {
            if (!objF.isDirectory()) {
                throw new JobSchedulerException(SOSVfs_E_277.params(pathname));
            }
        }
    }

    @Override
    public List<SOSFileEntry> nList(final String pathname, final boolean flgRecurseSubFolder, boolean checkIfExists) {
        notImplemented();
        return null;
    }

    @Override
    public void openInputFile(final String pstrFileName) {
        notImplemented();
    }

    @Override
    public void openOutputFile(final String pstrFileName) {
        notImplemented();
    }

    @Override
    public ISOSSession openSession(final ISOSShellOptions pobjShellOptions) throws Exception {
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
    public void putFile(final ISOSVirtualFile objVirtualFile) {
        String strName = objVirtualFile.getName();
        strName = new File(strName).getAbsolutePath();
        if (strName.startsWith("c:")) {
            strName = strName.substring(3);
        }
        ISOSVirtualFile objVF = this.getFileHandle(strName);
        OutputStream objOS = objVF.getFileOutputStream();
        InputStream objFI = objVirtualFile.getFileInputStream();
        int lngBufferSize = 1024;
        byte[] buffer = new byte[lngBufferSize];
        int intBytesTransferred;
        try {
            synchronized (this) {
                while ((intBytesTransferred = objFI.read(buffer)) != -1) {
                    objOS.write(buffer, 0, intBytesTransferred);
                }
                objFI.close();
                objOS.flush();
                objOS.close();
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("putFile()"), e);
        }
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
    public int read(final byte[] bteBuffer) {
        return 0;
    }

    @Override
    public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
        return 0;
    }

    @Override
    public boolean remoteIsWindowsShell() {
        return false;
    }

    @Override
    public void rename(final String strFileName, final String pstrNewFileName) {
        //
    }

    @Override
    public boolean rmdir(final SOSFolderName pobjFolderName) throws IOException {
        new File(pobjFolderName.getValue()).delete();
        return true;
    }

    @Override
    public void rmdir(final String pstrFolderName) throws IOException {
        new File(pstrFolderName).delete();
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        //
    }

    @Override
    public void setLogin(final boolean pflgIsLogin) {
        //
    }

    @Override
    public ISOSVirtualFile transferMode(final SOSOptionTransferMode pobjFileTransferMode) {
        return null;
    }

    @Override
    public void write(final byte[] bteBuffer) {
        notImplemented();
    }

    @Override
    public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
        notImplemented();
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