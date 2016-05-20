package sos.net.sosftp2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author Ghassan Beydoun */
public class SOSFTP extends FTPClient implements SOSFileTransfer {

    private static final Logger LOGGER = Logger.getLogger(SOSFTP.class);

    public SOSFTP() {
    }

    public SOSFTP(String host) throws SocketException, IOException, UnknownHostException {
        connect(host);
    }

    public SOSFTP(String host, int port) throws SocketException, IOException, UnknownHostException {
        connect(host, port);
    }

    public SOSFTP(String ftpHost, int ftpPort, String proxyHost, int proxyPort) throws SocketException, IOException, UnknownHostException {
        this.connect(ftpHost, ftpPort);
    }

    public void connect(String host, int port) throws SocketException, IOException, UnknownHostException {
        if (!isConnected()) {
            super.connect(host, port);
            logReply();
        }
    }

    public void connect(String hostname) throws SocketException, IOException, UnknownHostException {
        if (!isConnected()) {
            super.connect(hostname);
        }
        logReply();
    }

    public boolean mkdir(String pathname) throws IOException {
        return makeDirectory(pathname);
    }

    public boolean rmdir(String pathname) throws IOException {
        return removeDirectory(pathname);
    }

    public int passive() throws IOException {
        return pasv();
    }

    public Vector<String> nList(String pathname) throws Exception {
        return getFilenames(pathname);
    }

    private Vector<String> getFilenames(String pathname) throws IOException {
        return getFilenames(pathname, false);
    }

    private Vector<String> getFilenames(String pstrPathName, boolean flgRecurseSubFolders) throws IOException {
        Vector<String> vecListFileItems = new Vector<String>();
        String[] fileList = null;
        String strCurrentDirectory = doPWD();
        String lstrPathName = pstrPathName.trim();
        if (lstrPathName.isEmpty()) {
            lstrPathName = ".";
        }
        if (".".equals(lstrPathName)) {
            lstrPathName = strCurrentDirectory;
        }
        fileList = listNames(lstrPathName);
        if (fileList == null) {
            return vecListFileItems;
        }
        for (String strCurrentFile : fileList) {
            if (isNotHiddenFile(strCurrentFile)) {
                doCD(strCurrentFile);
                if (isNegativeCommandCompletion()) {
                    if (!strCurrentFile.startsWith(strCurrentDirectory)) {
                        strCurrentFile = strCurrentDirectory + "/" + strCurrentFile;
                    }
                    vecListFileItems.add(strCurrentFile);
                } else {
                    doCDUP();
                    if (flgRecurseSubFolders) {
                        Vector<String> vecNames = getFilenames(strCurrentFile);
                        if (vecNames != null) {
                            vecListFileItems.addAll(vecNames);
                        }
                    }
                }
            }
        }
        LOGGER.debug("strCurrentDirectory = " + strCurrentDirectory);
        doCD(strCurrentDirectory);
        doPWD();
        return vecListFileItems;
    }

    public String doPWD() {
        String strCurrentPath = "";
        try {
            LOGGER.debug("Try pwd.");
            pwd();
            strCurrentPath = getReplyString();
            int idx = strCurrentPath.indexOf('"');
            if (idx >= 0) {
                strCurrentPath = strCurrentPath.substring(idx + 1, strCurrentPath.length() - idx + 1);
            }
            logReply();
        } catch (IOException e) {
            LOGGER.error("Problems with pwd", e);
        }
        return strCurrentPath;
    }

    private int doCDUP() {
        try {
            LOGGER.debug("Try cdup .");
            cdup();
            logReply();
            doPWD();
        } catch (IOException e) {
            LOGGER.error("Problems with CDUP", e);
        }
        return 0;
    }

    private int doCD(final String strFolderName) {
        int x = 0;
        try {
            LOGGER.debug("Try cd with '" + strFolderName + "'.");
            x = cd(strFolderName);
            logReply();
        } catch (IOException e) {
        }
        return x;
    }

    private boolean logReply() {
        String strReply = getReplyString();
        LOGGER.debug(strReply);
        return true;
    }

    private boolean isNegativeCommandCompletion() {
        int x = getReplyCode();
        return (x > 300);
    }

    private boolean isPositiveCommandCompletion() {
        int x = getReplyCode();
        return (x <= 300);
    }

    public boolean isNotHiddenFile(final String strFileName) {
        if (!"..".equals(strFileName) && !".".equals(strFileName)) {
            return true;
        }
        return false;
    }

    @Override
    public Vector<String> nList(String pathname, final boolean flgRecurseSubFolder) throws Exception {
        return getFilenames(pathname, flgRecurseSubFolder);
    }

    @Override
    public Vector<String> nList() throws Exception {
        return getFilenames();
    }

    private Vector<String> getFilenames() throws Exception {
        return getFilenames("", false);
    }

    private Vector<String> getFilenames(boolean flgRecurseSubFolders) throws Exception {
        return getFilenames("", flgRecurseSubFolders);
    }

    @Override
    public Vector<String> nList(boolean recursive) throws Exception {
        return getFilenames(recursive);
    }

    public Vector<String> dir(String pathname) throws Exception {
        return getFilenames(pathname);
    }

    public Vector<String> dir(String pathname, int flag) throws Exception {
        Vector<String> fileList = new Vector<String>();
        FTPFile[] listFiles = listFiles(pathname);
        for (int i = 0; i < listFiles.length; i++) {
            if (flag > 0 && listFiles[i].isDirectory()) {
                fileList.addAll(this.dir(pathname + "/" + listFiles[i].toString(), ((flag >= 1024) ? flag : flag + 1024)));
            } else {
                if (flag >= 1024) {
                    fileList.add(pathname + "/" + listFiles[i].toString());
                } else {
                    fileList.add(listFiles[i].toString());
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
    public long size(String remoteFile) throws Exception {
        this.sendCommand("SIZE " + remoteFile);
        if (this.getReplyCode() == FTPReply.FILE_STATUS) {
            return Long.parseLong(trimResponseCode(this.getReplyString()));
        } else {
            return -1L;
        }
    }

    private String trimResponseCode(String response) throws Exception {
        if (response.length() < 5) {
            return response;
        }
        return response.substring(4).trim();
    }

    public boolean get(String remoteFile, String localFile) throws Exception {
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
    public long getFile(String remoteFile, String localFile) throws Exception {
        final boolean flgAppendLocalFile = false;
        return getFile(remoteFile, localFile, flgAppendLocalFile);
    }

    @Override
    public long getFile(String remoteFile, String localFile, boolean append) throws Exception {
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
                throw (new JobSchedulerException("File transfer failed."));
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
                //
            }
            try {
                if (out != null) {
                    out.flush();
                }
            } catch (Exception e) {
                //
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                //
            }
        }
    }

    @Override
    public boolean put(String localFile, String remoteFile) throws Exception {
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
                //
            }
        }
    }

    @Override
    public long putFile(String localFile, String remoteFile) throws Exception {
        java.io.OutputStream outputStream = storeFileStream(remoteFile);
        if (isNegativeCommandCompletion()) {
            throw new JobSchedulerException("..error occurred in storeFileStream() on the FTP server for file [" + remoteFile + "]: "
                    + getReplyString());
        }
        return putFile(localFile, outputStream);
    }

    @Override
    public long putFile(String localFile, OutputStream out) throws Exception {
        FileInputStream in = null;
        long totalBytes = 0;
        try {
            if (out == null) {
                throw (new JobSchedulerException("output stream has null value."));
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
                throw (new JobSchedulerException("File transfer failed."));
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
                //
            }
            try {
                out.flush();
            } catch (Exception e) {
                //
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                //
            }
        }
    }

    public long appendFile(String localFile, String remoteFile) throws Exception {
        return putFile(localFile, appendFileStream(remoteFile));
    }

    public boolean ascii() throws IOException {
        return setFileType(FTP.ASCII_FILE_TYPE);
    }

    public boolean binary() throws IOException {
        return setFileType(FTP.BINARY_FILE_TYPE);
    }

    public int cd(String directory) throws IOException {
        return cwd(directory);
    }

    public boolean delete(String pathname) throws IOException {
        return deleteFile(pathname);
    }

    @Override
    public boolean login(String strUserName, String strPassword) {
        try {
            super.login(strUserName, strPassword);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        logReply();
        return true;
    }

}