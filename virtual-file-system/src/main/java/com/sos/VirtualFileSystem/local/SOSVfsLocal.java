package com.sos.VirtualFileSystem.local;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private final InputStream inputStream = null;
    private final OutputStream outputStream = null;
    private SOSConnection2OptionsAlternate connection2OptionsAlternate = null;
    private String reply = "";
    private CmdShell cmdShell = null;
    private boolean simulateShell = false;

    //
    @Override
    public long appendFile(final String sourceFileName, final String targetFileName) {
        JSFile targetFile = new JSFile(targetFileName);
        long size = 0;
        try {
            size = targetFile.appendFile(sourceFileName);
        } catch (Exception e) {
            String msg = SOSVfs_E_134.params("appendFile()");
            LOGGER.error(msg, e);
            throw new JobSchedulerException(msg, e);
        }
        return size;
    }

    @Override
    public void ascii() {
        //
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions options) throws Exception {
        reply = "230 Login successful.";
        return this;
    }

    @Override
    public void binary() {
        //
    }

    @Override
    public boolean changeWorkingDirectory(final String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            return false;
        }
        return true;
    }

    @Override
    public void close() {
        //
    }

    @Override
    public void closeConnection() throws Exception {
        reply = "ok";
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
        reply = "221 Goodbye.";
    }

    @Override
    public void completePendingCommand() {
        //
    }

    @Override
    public ISOSConnection connect() throws Exception {
        reply = "ok";
        return this;
    }

    @Override
    public ISOSConnection connect(final ISOSConnectionOptions options) throws Exception {
        this.connect();
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate options) throws Exception {
        connection2OptionsAlternate = options;
        return null;
    }

    @Override
    public ISOSConnection connect(final ISOSDataProviderOptions options) throws Exception {
        return null;
    }

    @Override
    public ISOSConnection connect(final String host, final int port) throws Exception {
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
    public List<SOSFileEntry> dir(final SOSFolderName folderName) {
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
        if (cmdShell == null) {
            cmdShell = new CmdShell();
        }
        String command = cmd.trim();
        if (cmdShell.isWindows()) {
            command = cmdShell.replaceCommand4Windows(command);
        }
        int exitCode = cmdShell.executeCommand(command, env);
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
        if (cmdShell == null) {
            cmdShell = new CmdShell();
        }
        return cmdShell;
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
    public long getFile(final String sourceFileName, final String targetFileName, final boolean append) throws Exception {
        long size = 0;
        if (!append) {
            JSFile file = new JSFile(sourceFileName);
            size = file.length();
            file.copy(targetFileName);
        } else {
            size = this.appendFile(sourceFileName, targetFileName);
        }
        return size;
    }

    @Override
    public ISOSVirtualFile getFileHandle(final String fileName) {
        SOSVfsLocalFile file = new SOSVfsLocalFile(fileName);
        file.setHandler(this);
        return file;
    }

    @Override
    public SOSFileEntry getFileEntry(final String pathname) throws Exception {
        File file = new File(pathname);
        if (file.exists() && file.isFile()) {
            return getFileEntry(file);
        }
        return null;
    }

    private SOSFileEntry getFileEntry(File file) {
        SOSFileEntry entry = new SOSFileEntry(EntryType.FILESYSTEM);
        entry.setDirectory(file.isDirectory());
        entry.setFilename(file.getName());
        entry.setFilesize(file.length());
        entry.setLastModified(file.lastModified());
        entry.setParentPath(file.getParent());
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(final String pathname, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        File dir = new File(pathname);
        if (checkIfExists && !dir.exists()) {
            return result;
        }
        if (checkIfIsDirectory && !dir.isDirectory()) {
            reply = "ls OK";
            return result;
        }

        File[] list = dir.listFiles();
        for (File file : list) {
            result.add(getFileEntry(file));
        }
        return result;
    }

    @Override
    public List<SOSFileEntry> getFilelist(final String folder, final String regexp, final int flag, final boolean recursive, boolean checkIfExists,
            String integrityHashType) {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        try {
            Vector<File> list = SOSFile.getFolderlist(folder, regexp, flag, recursive);
            for (File file : list) {
                if (file.isDirectory()) {
                    continue;
                }
                if (integrityHashType != null && file.getName().endsWith(integrityHashType)) {
                    continue;
                }
                result.add(getFileEntry(file));
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        return result;
    }

    @Override
    public List<SOSFileEntry> getFolderlist(final String folder, final String regexp, final int flag, final boolean recursive) {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        try {
            Vector<File> list = SOSFile.getFolderlist(folder, regexp, flag, recursive);
            for (File file : list) {
                if (file.isDirectory()) {
                    result.add(getFileEntry(file));
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
        return result;
    }

    @Override
    public OutputStream getFileOutputStream() {
        return outputStream;
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
        return inputStream;
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        return null;
    }

    @Override
    public String getModificationTime(final String fileName) {
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public OutputStream getOutputStream(final String fileName) {
        return null;
    }

    private String getRealFileName(final String pathname) {
        return pathname;
    }

    @Override
    public String getReplyString() {
        return reply;
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
    public boolean isDirectory(final String fileName) {
        return new File(fileName).isDirectory();
    }

    @Override
    public boolean isNegativeCommandCompletion() {
        return false;
    }

    @Override
    public void login(final String user, final String password) {
        //
    }

    @Override
    public void logout() throws IOException {
        //
    }

    @Override
    public ISOSVirtualFolder mkdir(final SOSFolderName folderName) throws IOException {
        new File(folderName.getValue()).mkdir();
        return null;
    }

    @Override
    public void mkdir(final String pathname) throws IOException {
        File dir = new File(pathname);
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            if (!dir.isDirectory()) {
                throw new JobSchedulerException(SOSVfs_E_277.params(pathname));
            }
        }
    }

    @Override
    public List<SOSFileEntry> nList(final String pathname, final boolean recursive, boolean checkIfExists) {
        notImplemented();
        return null;
    }

    @Override
    public void openInputFile(final String fileName) {
        notImplemented();
    }

    @Override
    public void openOutputFile(final String fileName) {
        notImplemented();
    }

    @Override
    public ISOSSession openSession(final ISOSShellOptions options) throws Exception {
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
    public void putFile(final ISOSVirtualFile file) {
        String name = file.getName();
        name = new File(name).getAbsolutePath();
        if (name.startsWith("c:")) {
            name = name.substring(3);
        }
        OutputStream os = null;
        InputStream is = null;
        try {
            os = getFileHandle(name).getFileOutputStream();
            is = file.getFileInputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            synchronized (this) {
                while ((bytes = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytes);
                }
                is.close();
                os.flush();
                os.close();

                is = null;
                os = null;
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("putFile()"), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public long putFile(final String localFile, final OutputStream os) {
        return 0;
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) throws Exception {
        return 0;
    }

    @Override
    public int read(final byte[] buffer) {
        return 0;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) {
        return 0;
    }

    @Override
    public boolean remoteIsWindowsShell() {
        return false;
    }

    @Override
    public void rename(final String fileName, final String newFileName) {
        //
    }

    @Override
    public boolean rmdir(final SOSFolderName folderName) throws IOException {
        new File(folderName.getValue()).delete();
        return true;
    }

    @Override
    public void rmdir(final String folderName) throws IOException {
        new File(folderName).delete();
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities utilities) {
        //
    }

    @Override
    public void setLogin(final boolean isLogin) {
        //
    }

    @Override
    public ISOSVirtualFile transferMode(final SOSOptionTransferMode mode) {
        return null;
    }

    @Override
    public void write(final byte[] buffer) {
        notImplemented();
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int length) {
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