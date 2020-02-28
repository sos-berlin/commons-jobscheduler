package com.sos.VirtualFileSystem.FTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSSession;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtp extends SOSVfsFtpBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSConnection {

    private static final String CLASS_NAME = SOSVfsFtp.class.getSimpleName();
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsFtp.class);
    private FTPClient ftpClient = null;
    private boolean simulateShell = false;

    @Override
    public long appendFile(final String localFile, final String remoteFile) {
        final String method = CLASS_NAME + "::appendFile";
        long i = 0;
        try {
            i = putFile(localFile, Client().appendFileStream(remoteFile));
            LOGGER.info(SOSVfs_I_155.params(i));
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        return i;
    }

    @Override
    public void ascii() {
        final String method = CLASS_NAME + "::ascii";
        try {
            if (!Client().setFileType(FTP.ASCII_FILE_TYPE)) {
                throw new JobSchedulerException(SOSVfs_E_149.params(getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public void binary() {
        final String method = CLASS_NAME + "::binary";
        try {
            if (!Client().setFileType(FTP.BINARY_FILE_TYPE)) {
                throw new JobSchedulerException(SOSVfs_E_149.params(getReplyString()));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public int cd(final String directory) throws IOException {
        return Client().cwd(directory);
    }

    @Override
    public boolean changeWorkingDirectory(final String pathname) {
        final String method = CLASS_NAME + "::changeWorkingDirectory";
        boolean flgR = true;
        try {
            String strT = pathname.replaceAll("\\\\", "/");
            Client().cwd(strT);
            LOGGER.debug(SOSVfs_D_135.params(strT, getReplyString(), "[directory exists]"));
            flgR = getFtpReply().isSuccessCode();
            if (flgR) {
                setCurrentPath(strT);
            }
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        return flgR;
    }

    @Override
    protected final FTPClient Client() {
        if (ftpClient == null) {
            if (usingProxy()) {
                LOGGER.info(String.format("using proxy: protocol = %s, host = %s, port = %s, user = %s, pass = ?", getProxyProtocol().getValue(),
                        getProxyHost(), getProxyPort(), getProxyUser()));
                if (usingHttpProxy()) {
                    if (SOSString.isEmpty(getProxyUser())) {
                        ftpClient = new FTPHTTPClient(getProxyHost(), getProxyPort());
                    } else {
                        ftpClient = new FTPHTTPClient(getProxyHost(), getProxyPort(), getProxyUser(), getProxyPassword());
                    }
                } else {
                    ftpClient = new FTPClient();
                    ftpClient.setProxy(getSocksProxy());
                }
            } else {
                ftpClient = new FTPClient();
            }
            setCommandListener(new SOSFtpClientLogger(getHostID("")));
            if (getConnectionOptionsAlternate() != null && getConnectionOptionsAlternate().protocolCommandListener.isTrue()) {
                ftpClient.addProtocolCommandListener(getCommandListener());
                LOGGER.debug("ProtocolcommandListener added and activated");
            }
            String addFTPProtocol = System.getenv("AddFTPProtocol");
            if (addFTPProtocol != null && "true".equalsIgnoreCase(addFTPProtocol)) {
                ftpClient.addProtocolCommandListener(getCommandListener());
            }
        }
        return ftpClient;
    }

    @Override
    public void closeConnection() throws Exception {
        if (Client().isConnected()) {
            Client().disconnect();
            logReply();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SOSVfs_I_0109.params(getHost(), getPort()));
            }
        }
    }

    @Override
    public void closeInput() {
        //
    }

    private void closeInput(InputStream is) {
        try {
            if (is != null) {
                is.close();
                is = null;
            }
        } catch (IOException e) {
            //
        }
    }

    private void closeObject(OutputStream os) {
        try {
            if (os != null) {
                os.flush();
                os.close();
                os = null;
            }
        } catch (Exception e) {
            //
        }
    }

    @Override
    public String createScriptFile(final String content) throws Exception {
        notImplemented();
        return null;
    }

    @Override
    public List<SOSFileEntry> dir() {
        final String method = CLASS_NAME + "::dir";
        try {
            return dir(".");
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public List<SOSFileEntry> dir(final SOSFolderName folderName) {
        dir(folderName.getValue());
        return null;
    }

    @Override
    public List<SOSFileEntry> dir(final String pathname) {
        return getFilenames(pathname);
    }

    @Override
    public List<SOSFileEntry> dir(final String pathname, final int flag) {
        final String method = CLASS_NAME + "::dir";
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        FTPFile[] list = null;
        try {
            list = Client().listFiles(pathname);
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
        if (list == null || list.length <= 0) {
            if (isNegativeCommandCompletion()) {
                String message = getHostID(SOSVfs_E_0105.params(method)) + ":" + getReplyString();
                throw new JobSchedulerException(message);
            }
            return result;
        }
        for (FTPFile file : list) {
            SOSFileEntry entry = getFileEntry(file, pathname);

            if (flag > 0 && file.isDirectory()) {
                result.addAll(this.dir(pathname + "/" + file.getName(), flag >= 1024 ? flag : flag + 1024));
            } else {
                result.add(entry);

            }
        }
        return result;
    }

    @Override
    public void disconnect() {
        final String method = CLASS_NAME + "::disconnect";
        try {
            if (Client().isConnected()) {
                Client().disconnect();
            }
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        }
    }

    @Override
    public void executeCommand(final String cmd) throws Exception {
        executeCommand(cmd, null);
    }

    @Override
    public void executeCommand(final String cmd, SOSVfsEnv env) throws Exception {
        String command = cmd.trim();
        try {
            ftpClient.sendCommand(command);
            String replyString = ftpClient.getReplyString().trim();
            int replyCode = ftpClient.getReplyCode();
            if (FTPReply.isNegativePermanent(replyCode) || FTPReply.isNegativeTransient(replyCode) || replyCode >= 10_000) {
                throw new JobSchedulerException(SOSVfs_E_164.params(replyString + "[" + command + "]"));
            }
            LOGGER.info(SOSVfs_D_151.params(command, replyString));
        } catch (JobSchedulerException ex) {
            if (getConnectionOptionsAlternate().raiseExceptionOnError.value()) {
                throw ex;
            }
            LOGGER.info(SOSVfs_D_151.params(command, ex.toString()), ex);
        } catch (Exception ex) {
            if (getConnectionOptionsAlternate().raiseExceptionOnError.value()) {
                throw new JobSchedulerException(SOSVfs_E_134.params("ExecuteCommand"), ex);
            }
            LOGGER.info(SOSVfs_D_151.params(command, ex.toString()), ex);
        }
    }

    @Override
    public void flush() {
        //
    }

    @Override
    public void get(final String remoteFile, final String localFile) {
        final String method = CLASS_NAME + "::get";
        FileOutputStream out = null;
        boolean rc = false;
        try {
            out = new FileOutputStream(localFile);
            rc = Client().retrieveFile(remoteFile, out);
            if (!rc) {
                throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        } finally {
            closeObject(out);
        }
    }

    public FTPClient getClient() {
        return Client();
    }

    @Override
    public ISOSConnection getConnection() {
        return this;
    }

    @Override
    public Integer getExitCode() {
        notImplemented();
        return null;
    }

    @Override
    public String getExitSignal() {
        notImplemented();
        return null;
    }

    @Override
    public long getFile(final String remoteFile, final String localFile) {
        return this.getFile(remoteFile, localFile, false);
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) {
        final String method = CLASS_NAME + "::getFile";
        InputStream in = null;
        OutputStream out = null;
        long totalBytes = 0;
        try {
            in = Client().retrieveFileStream(remoteFile);
            if (in == null) {
                throw new JobSchedulerException(SOSVfs_E_143.params(getReplyString()));
            }
            if (!isPositiveCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("getFile()", remoteFile, getReplyString()));
            }
            byte[] buffer = new byte[4096];
            out = new FileOutputStream(new File(localFile), append);
            int bytes_read = 0;
            synchronized (this) {
                while ((bytes_read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes_read);
                    out.flush();
                    totalBytes += bytes_read;
                }
            }
            closeInput(in);
            closeObject(out);
            this.completePendingCommand();
            if (totalBytes > 0) {
                return totalBytes;
            } else {
                return -1L;
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        } finally {
            closeInput(in);
            closeObject(out);
        }
    }

    @Override
    public ISOSVirtualFile getFileHandle(final String filename) {
        ISOSVirtualFile file = new SOSVfsFtpFile(filename.replaceAll("\\\\", "/"));
        file.setHandler(this);
        return file;
    }

    @Override
    public List<SOSFileEntry> getFilelist(final String folder, final String regexp, final int flag, final boolean recursive, boolean checkIfExists,
            String integrityHashType) {
        List<SOSFileEntry> vecDirectoryListing = nList(folder, recursive, checkIfExists);

        List<SOSFileEntry> list = new ArrayList<SOSFileEntry>();
        Pattern pattern = Pattern.compile(regexp, flag);
        for (SOSFileEntry entry : vecDirectoryListing) {
            if (integrityHashType != null && entry.getFilename().endsWith(integrityHashType)) {
                continue;
            }
            Matcher matcher = pattern.matcher(entry.getFilename());
            if (matcher.find()) {
                list.add(entry);
            }
        }
        return list;
    }

    private List<SOSFileEntry> getFilenames(final String pathname) {
        return getFilenames(pathname, false);
    }

    private List<SOSFileEntry> getFilenames(final String path, final boolean recursive) {
        return getFilenames(path, recursive, true);
    }

    private List<SOSFileEntry> getFilenames(final String path, final boolean recursive, final boolean checkReplyCode) {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        FTPFile[] list = null;

        String pathName = path.trim();
        if (pathName == null) {
            pathName = "";
        }
        if (pathName.isEmpty()) {
            pathName = ".";
        }
        LOGGER.debug("directory = " + pathName);
        try {
            list = Client().listFiles(pathName);
        } catch (IOException e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params("getFilenames")), e);
        }
        if (list == null || list.length <= 0) {
            if (isNegativeCommandCompletion()) {
                String message = getHostID(SOSVfs_E_0105.params("getFilenames")) + ":" + getReplyString();
                if (checkReplyCode) {
                    throw new JobSchedulerException(message);
                } else {
                    LOGGER.warn(message);
                }
            }
            return result;
        }
        for (FTPFile file : list) {
            String name = file.getName();
            if (!name.trim().isEmpty() && isNotHiddenFile(name)) {
                if (name.indexOf("/") == -1) {
                    name = pathName + "/" + name;
                    name = name.replaceAll("//+", "/");
                }
                if (file.isFile()) {
                    result.add(getFileEntry(file, pathName));
                } else if (file.isDirectory() && recursive) {
                    List<SOSFileEntry> filelist = getFilenames(name + "/", recursive, false);
                    if (filelist != null && !filelist.isEmpty()) {
                        result.addAll(filelist);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles() {
        return null;
    }

    @Override
    public Vector<ISOSVirtualFile> getFiles(final String string) {
        return null;
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
    }

    @Override
    public SOSFileListEntry getNewVirtualFile(final String fileName) {
        return new SOSFileListEntry(fileName);
    }

    @Override
    public String getResponse() {
        return this.getReplyString();
    }

    @Override
    public ISOSSession getSession() {
        return null;
    }

    @Override
    public StringBuffer getStdErr() throws Exception {
        return null;
    }

    @Override
    public StringBuffer getStdOut() throws Exception {
        return null;
    }

    @Override
    public boolean isNegativeCommandCompletion() {
        int x = Client().getReplyCode();
        return x > 300;
    }

    @Override
    public boolean isNotHiddenFile(final String fileName) {
        if (fileName == null || ".".equals(fileName) || "..".equals(fileName) || fileName.endsWith("/..") || fileName.endsWith("/.")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isPositiveCommandCompletion() {
        int x = Client().getReplyCode();
        return x <= 300;
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
    public void put(final String localFile, final String remoteFile) {
        final String method = CLASS_NAME + "::put";
        FileInputStream in = null;
        boolean rc = false;
        try {
            in = new FileInputStream(localFile);
            rc = Client().storeFile(remoteFile, in);
            if (!rc) {
                throw new JobSchedulerException(SOSVfs_E_154.params("put"));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        } finally {
            closeInput(in);
        }
    }

    @Override
    public void putFile(final ISOSVirtualFile file) {
        final String method = CLASS_NAME + "::putFile";
        OutputStream os = null;
        InputStream is = null;
        try {
            os = getFileHandle(file.getName()).getFileOutputStream();
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
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
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
    public long putFile(final String localFile, final OutputStream out) {
        final String method = CLASS_NAME + "::putFile";
        if (out == null) {
            throw new JobSchedulerException("OutputStream null value.");
        }
        FileInputStream in = null;
        long lngTotalBytesWritten = 0;
        try {
            byte[] buffer = new byte[4096];
            in = new FileInputStream(new File(localFile));
            int bytesWritten;
            synchronized (this) {
                while ((bytesWritten = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesWritten);
                    lngTotalBytesWritten += bytesWritten;
                }
            }
            closeInput(in);
            closeObject(out);
            this.completePendingCommand();
            return lngTotalBytesWritten;
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(SOSVfs_E_0105.params(method)), e);
        } finally {
            closeInput(in);
            closeObject(out);
        }
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) throws Exception {
        OutputStream outputStream = Client().storeFileStream(remoteFile);
        if (isNegativeCommandCompletion()) {
            throw new JobSchedulerException(SOSVfs_E_144.params("storeFileStream()", remoteFile, getReplyString()));
        }
        long i = putFile(localFile, outputStream);
        LOGGER.debug(SOSVfs_D_146.params(localFile, remoteFile));
        return i;
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
    public void rename(final String from, final String to) {
        try {
            this.Client().rename(from, to);
            if (isNegativeCommandCompletion()) {
                throw new JobSchedulerException(SOSVfs_E_144.params("rename()", from, getReplyString()));
            } else {
                LOGGER.info(String.format(SOSVfs_I_150.params(from, to)));
            }
        } catch (IOException e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("rename()"), e);
        }
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities utilities) {
        //
    }

    @Override
    public void write(final byte[] buffer) {
        //
    }

    @Override
    public void write(final byte[] buffer, final int offset, final int lLength) {
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

}