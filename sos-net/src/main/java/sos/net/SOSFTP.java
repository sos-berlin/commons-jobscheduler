package sos.net;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import sos.util.SOSLogger;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

/** @author Ghassan Beydoun */
public class SOSFTP extends FTPClient implements SOSFileTransfer {

    private static final Logger LOGGER = Logger.getLogger(SOSFTP.class);
    protected static final String conRegExpBackslash = "\\\\";
    private ProtocolCommandListener listener = null;

    public SOSFTP() {
    }

    public SOSFTP(final String host) throws SocketException, IOException, UnknownHostException {
        connect(host);
    }

    public SOSFTP(final String host, final int port) throws SocketException, IOException, UnknownHostException {
        connect(host, port);
    }

    public SOSFTP(final String host, final int port, final SOSLogger logger) {
        try {
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public SOSFTP(final String ftpHost, final int ftpPort, final String proxyHost, final int proxyPort) throws SocketException, IOException,
            UnknownHostException {
        this.connect(ftpHost, ftpPort);
    }

    @Override
    public void connect(final String host, final int port) throws SocketException, IOException, UnknownHostException {
        if (!isConnected()) {
            listener = new PrintCommandListener(new PrintWriter(System.out));
            String strAddFTPProtocol = System.getenv("AddFTPProtocol");
            if (strAddFTPProtocol != null && "true".equalsIgnoreCase(strAddFTPProtocol)) {
                this.addProtocolCommandListener(listener);
            }
            super.connect(host, port);
            LogReply();
        }
    }

    @Override
    public void connect(final String hostname) throws SocketException, IOException, UnknownHostException {
        if (!isConnected()) {
            super.connect(hostname);
        }
        LogReply();
    }

    @Override
    public boolean mkdir(final String pathname) throws IOException {
        int intPosixPermissions = 484;
        return mkdir(pathname, intPosixPermissions);
    }

    @Override
    public boolean mkdir(final String pathname, final int pintPosixPermissions) throws IOException {
        boolean flgRet = makeDirectory(pathname);
        this.sendCommand("CHMOD " + Integer.toOctalString(pintPosixPermissions) + " " + pathname);
        return flgRet;
    }

    public boolean rmdir(final String pathname) throws IOException {
        return removeDirectory(pathname);
    }

    public int passive() throws IOException {
        return pasv();
    }

    @Override
    public Vector<String> nList(final String pathname) throws Exception {
        return getFilenames(pathname);
    }

    private Vector<String> getFilenames(final String pathname) throws IOException {
        return getFilenames(pathname, false);
    }

    private Vector<String> getFilenames(final String pstrPathName, final boolean flgRecurseSubFolders) throws IOException {
        Vector<String> vecListFileItems = new Vector<String>();
        String[] fileList = null;
        String strCurrentDirectoryName = DoPWD();
        String lstrPathName = pstrPathName.trim();
        if (lstrPathName.isEmpty()) {
            lstrPathName = ".";
        }
        if (".".equals(lstrPathName)) {
            lstrPathName = strCurrentDirectoryName;
        }
        LOGGER.debug("nlist with " + lstrPathName);
        fileList = listNames(lstrPathName);
        if (fileList == null) {
            return vecListFileItems;
        }
        for (String strCurrentFileName : fileList) {
            if (isNotHiddenFile(strCurrentFileName)) {
                DoCD(strCurrentFileName);
                if (isNegativeCommandCompletion()) {
                    if (flgRecurseSubFolders && !strCurrentFileName.startsWith(strCurrentDirectoryName)) {
                        if (!strCurrentDirectoryName.isEmpty()) {
                            strCurrentFileName = strCurrentDirectoryName + "/" + strCurrentFileName;
                        }
                    }
                    strCurrentFileName = strCurrentFileName.replaceAll(conRegExpBackslash, "/");
                    vecListFileItems.add(strCurrentFileName);
                } else {
                    DoCDUP();
                    if (flgRecurseSubFolders) {
                        Vector<String> vecNames = getFilenames(strCurrentFileName);
                        if (vecNames != null) {
                            vecListFileItems.addAll(vecNames);
                        }
                    }
                }
            }
        }
        LOGGER.debug("strCurrentDirectory = " + strCurrentDirectoryName);
        DoCD(strCurrentDirectoryName);
        DoPWD();
        return vecListFileItems;
    }

    public String DoPWD() {
        String strCurrentPathName = "";
        try {
            pwd();
            strCurrentPathName = getReplyString();
            int idx = strCurrentPathName.indexOf('"');
            if (idx >= 0) {
                strCurrentPathName = strCurrentPathName.substring(idx + 1, strCurrentPathName.length() - idx + 1);
                idx = strCurrentPathName.indexOf('"');
                if (idx > 0) {
                    strCurrentPathName = strCurrentPathName.substring(0, idx);
                }
            }
            LogReply();
        } catch (IOException e) {
            RaiseException("Problems with pwd", e);
        }
        return strCurrentPathName;
    }

    private int DoCDUP() {
        try {
            LOGGER.debug("Try cdup .");
            cdup();
            LogReply();
            DoPWD();
        } catch (IOException e) {
            //
        }
        return 0;
    }

    private int DoCD(final String strFolderName) {
        int x = 0;
        try {
            x = cd(strFolderName);
            String strReply = getReplyString();
        } catch (IOException e) {
        }
        return x;
    }

    private boolean LogReply() {
        String strReply = getReplyString();
        LOGGER.debug(strReply);
        return true;
    }

    private boolean isNegativeCommandCompletion() {
        int x = getReplyCode();
        return x > 300;
    }

    private boolean isPositiveCommandCompletion() {
        int x = getReplyCode();
        return x <= 300;
    }

    public boolean isNotHiddenFile(final String strFileName) {
        if (!"..".equalsIgnoreCase(strFileName) && !".".equalsIgnoreCase(strFileName)) {
            return true;
        }
        return false;
    }

    @Override
    public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) throws Exception {
        return getFilenames(pathname, flgRecurseSubFolder);
    }

    @Override
    public Vector<String> nList() throws Exception {
        return getFilenames();
    }

    private Vector<String> getFilenames() throws Exception {
        return getFilenames("", false);
    }

    private Vector<String> getFilenames(final boolean flgRecurseSubFolders) throws Exception {
        return getFilenames("", flgRecurseSubFolders);
    }

    @Override
    public Vector<String> nList(final boolean recursive) throws Exception {
        return getFilenames(recursive);
    }

    public Vector<String> dir(final String pathname) throws Exception {
        return getFilenames(pathname);
    }

    @Deprecated
    public Vector<String> dir(final String pathname, final int flag) throws Exception {
        Vector<String> fileList = new Vector<String>();
        FTPFile[] listFiles = listFiles(pathname);
        for (FTPFile listFile : listFiles) {
            if (flag > 0 && listFile.isDirectory()) {
                fileList.addAll(this.dir(pathname + "/" + listFile.toString(), flag >= 1024 ? flag : flag + 1024));
            } else {
                if (flag >= 1024) {
                    fileList.add(pathname + "/" + listFile.toString());
                } else {
                    fileList.add(listFile.toString());
                }
            }
        }
        return fileList;
    }

    public Vector<String> dir() throws Exception {
        return dir(".");
    }

    public String getResponse() {
        return this.getReplyString();
    }

    @Override
    public long size(final String remoteFile) throws Exception {
        this.sendCommand("SIZE " + remoteFile);
        if (this.getReplyCode() == FTPReply.FILE_STATUS) {
            return Long.parseLong(trimResponseCode(this.getReplyString()));
        } else {
            return -1L;
        }
    }

    private String trimResponseCode(final String response) throws Exception {
        if (response.length() < 5) {
            return response;
        }
        return response.substring(4).trim();
    }

    public boolean get(final String remoteFile, final String localFile) throws Exception {
        FileOutputStream out = null;
        boolean rc = false;
        try {
            out = new FileOutputStream(localFile);
            rc = retrieveFile(remoteFile, out);
            return rc;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public long getFile(final String remoteFile, final String localFile) throws Exception {
        final boolean flgAppendLocalFile = false;
        return getFile(remoteFile, localFile, flgAppendLocalFile);
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        long totalBytes = 0;
        try {
            in = retrieveFileStream(remoteFile);
            if (in == null) {
                throw new JobSchedulerException("Could not open stream for " + remoteFile
                        + ". Perhaps the file does not exist. Reply from ftp server: " + getReplyString());
            }
            if (!isPositiveCommandCompletion()) {
                throw new JobSchedulerException("..error occurred in getFile() [retrieveFileStream] on the FTP server for file [" + remoteFile
                        + "]: " + getReplyString());
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
            in.close();
            out.close();
            if (!completePendingCommand()) {
                logout();
                disconnect();
                throw new JobSchedulerException("File transfer failed.");
            }
            if (isNegativeCommandCompletion()) {
                throw new JobSchedulerException("..error occurred in getFile() on the FTP server for file [" + remoteFile + "]: " + getReplyString());
            }
            if (totalBytes > 0) {
                return totalBytes;
            } else {
                return -1L;
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
            try {
                if (out != null) {
                    out.flush();
                }
            } catch (Exception e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean put(final String localFile, final String remoteFile) throws Exception {
        FileInputStream in = null;
        boolean rc = false;
        try {
            in = new FileInputStream(localFile);
            rc = storeFile(remoteFile, in);
            return rc;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) throws Exception {
        java.io.OutputStream outputStream = storeFileStream(remoteFile);
        if (isNegativeCommandCompletion()) {
            throw new JobSchedulerException("..error occurred in storeFileStream() on the FTP server for file [" + remoteFile + "]: "
                    + getReplyString());
        }
        return putFile(localFile, outputStream);
    }

    @Override
    public long putFile(final String localFile, final OutputStream out) throws Exception {
        FileInputStream in = null;
        long totalBytes = 0;
        try {
            if (out == null) {
                throw new JobSchedulerException("output stream has null value.");
            }
            byte[] buffer = new byte[4096];
            in = new FileInputStream(new File(localFile));
            int bytesWritten;
            synchronized (this) {
                while ((bytesWritten = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesWritten);
                    totalBytes += bytesWritten;
                }
            }
            in.close();
            out.close();
            if (!completePendingCommand()) {
                logout();
                disconnect();
                throw new JobSchedulerException("File transfer failed.");
            }
            if (isNegativeCommandCompletion()) {
                throw new JobSchedulerException("..error occurred in putFile() on the FTP server for file [" + localFile + "]: " + getReplyString());
            }
            return totalBytes;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
            }
            try {
                out.flush();
            } catch (Exception e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public long appendFile(final String localFile, final String remoteFile) throws Exception {
        return putFile(localFile, appendFileStream(remoteFile));
    }

    public boolean ascii() throws IOException {
        return setFileType(FTP.ASCII_FILE_TYPE);
    }

    public boolean binary() throws IOException {
        return setFileType(FTP.BINARY_FILE_TYPE);
    }

    public int cd(final String directory) throws IOException {
        return cwd(directory);
    }

    @Override
    public boolean delete(final String pathname) throws IOException {
        return deleteFile(pathname);
    }

    @Override
    public boolean login(final String strUserName, final String strPassword) {
        try {
            super.login(strUserName, strPassword);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LogReply();
        return true;
    }

    protected void RaiseException(final Exception e, final String pstrM) {
        if (e != null) {
            LOGGER.error(pstrM, e);
            throw new JobSchedulerException(pstrM, e);
        } else {
            LOGGER.error(pstrM);
            throw new JobSchedulerException(pstrM);
        }
    }

    protected void RaiseException(final String pstrM, final Exception e) {
        RaiseException(e, pstrM);
    }

    protected void RaiseException(final String pstrM) {
        RaiseException(null, pstrM);
    }

}
