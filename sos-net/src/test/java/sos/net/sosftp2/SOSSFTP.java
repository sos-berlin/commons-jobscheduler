package sos.net.sosftp2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;
import com.trilead.ssh2.Session;

public class SOSSFTP implements SOSFileTransfer {

    protected String host = "";
    protected int port = 22;
    protected String user = "";
    protected String password = "";
    protected String proxyHost = "";
    protected int proxyPort = 0;
    protected String proxyUser = "";
    protected String proxyPassword = "";
    protected String authenticationMethod = "publickey";
    protected String reply = "OK";
    protected String authenticationFilename = "";
    protected Connection sshConnection = null;
    protected Session sshSession = null;
    protected SFTPv3Client sftpClient = null;
    protected boolean connected = false;
    private String currentDirectory = "";
    public static final String AUTH_METHOD_PASSWORD = "password";
    public static final String AUTH_METHOD_PUBLICKEY = "publickey";
    char[] authenticationFile = null;

    public SOSSFTP(String host) {
        this.host = host;
    }

    public SOSSFTP(String host, int port) {
        this(host);
        this.port = port;
    }

    public void connect() throws Exception {
        try {
            boolean isAuthenticated = false;
            sshConnection = new Connection(this.getHost(), this.getPort());
            if (this.getProxyHost() != null && !this.getProxyHost().isEmpty()) {
                if (this.getProxyUser() != null && !this.getProxyUser().isEmpty()) {
                    sshConnection.setProxyData(new HTTPProxyData(this.getProxyHost(), this.getProxyPort(), this.getProxyUser(),
                            this.getProxyPassword()));
                } else {
                    sshConnection.setProxyData(new HTTPProxyData(this.getProxyHost(), this.getProxyPort()));
                }
            }
            sshConnection.connect();
            if ("publickey".equalsIgnoreCase(this.getAuthenticationMethod())) {
                if (getAuthenticationFile() != null) {
                    isAuthenticated = sshConnection.authenticateWithPublicKey(this.getUser(), getAuthenticationFile(), getPassword());
                } else if (getAuthenticationFilename() != null && getAuthenticationFilename().startsWith("local:")) {
                    String filename = getAuthenticationFilename().substring("local:".length());
                    String text = sos.util.SOSFile.readFile(new File(filename));
                    isAuthenticated = sshConnection.authenticateWithPublicKey(this.getUser(), text.toCharArray(), this.getPassword());
                } else if (getAuthenticationFilename() != null && getAuthenticationFilename().startsWith("filecontent:")) {
                    isAuthenticated =
                            sshConnection.authenticateWithPublicKey(this.getUser(),
                                    getAuthenticationFilename().substring("filecontent:".length()).toCharArray(), this.getPassword());
                } else {
                    File authenticationFile = new File(this.getAuthenticationFilename());
                    if (!authenticationFile.exists()) {
                        throw new Exception("authentication file does not exist: " + authenticationFile.getCanonicalPath());
                    }
                    if (!authenticationFile.canRead()) {
                        throw new Exception("authentication file not accessible: " + authenticationFile.getCanonicalPath());
                    }
                    isAuthenticated = sshConnection.authenticateWithPublicKey(this.getUser(), authenticationFile, this.getPassword());
                }
            } else if ("password".equalsIgnoreCase(this.getAuthenticationMethod())) {
                isAuthenticated = sshConnection.authenticateWithPassword(this.getUser(), this.getPassword());
            } else {
                throw new Exception("Unknown authentication method: " + getAuthenticationMethod());
            }
            if (!isAuthenticated) {
                throw new Exception("authentication failed [host=" + this.getHost() + ", port=" + this.getPort() + ", user:" + this.getUser()
                        + ", auth_method=" + this.getAuthenticationMethod() + ", auth_file=" + this.getAuthenticationFilename());
            }
            sftpClient = new SFTPv3Client(sshConnection);
            connected = true;
            reply = "OK";
        } catch (Exception e) {
            reply = e.toString();
            if (sshConnection != null) {
                try {
                    disconnect();
                } catch (Exception ex) {
                    // gracefully ignore this error
                }
            }
            throw new Exception("Error occured connecting: " + e, e);
        }
    }

    private String resolvePathname(String pathname) {
        if ((!pathname.startsWith("./") && !pathname.startsWith("/")) && !currentDirectory.isEmpty()) {
            String slash = "";
            if (!currentDirectory.endsWith("/")) {
                slash = "/";
            }
            pathname = currentDirectory + slash + pathname;
        }
        return pathname;
    }

    public boolean changeWorkingDirectory(String pathname) throws IOException {
        pathname = resolvePathname(pathname);
        if (pathname.length() > 1 && pathname.endsWith("/")) {
            pathname = pathname.substring(0, pathname.length() - 1);
        }
        if (!fileExists(pathname)) {
            reply = "\"" + pathname + "\" doesn't exist.";
            return false;
        }
        if (!isDirectory(pathname)) {
            reply = "\"" + pathname + "\" is not a directory.";
            return false;
        }
        if (pathname.startsWith("/") || currentDirectory.isEmpty()) {
            currentDirectory = pathname;
            reply = "cd OK";
            return true;
        }
        currentDirectory = pathname;
        reply = "cd OK";
        return true;
    }

    public boolean delete(String pathname) throws IOException {
        pathname = resolvePathname(pathname);
        try {
            sftpClient.rm(pathname);
            reply = "rm OK";
        } catch (Exception e) {
            reply = e.toString();
            return false;
        }
        return true;
    }

    public void disconnect() throws IOException {
        reply = "disconnect OK";
        if (sftpClient != null) {
            try {
                sftpClient.close();
                sftpClient = null;
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            }
        }
        if (sshConnection != null) {
            try {
                sshConnection.close();
                sshConnection = null;
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            }
        }
        connected = false;
    }

    public String getReplyString() {
        return reply;
    }

    public boolean isConnected() {
        return connected;
    }

    public String[] listNames(String pathname) throws IOException {
        pathname = resolvePathname(pathname);
        try {
            if (pathname.isEmpty()) {
                pathname = ".";
            }
            if (!fileExists(pathname)) {
                return null;
            }
            if (!isDirectory(pathname)) {
                File remoteFile = new File(pathname);
                reply = "ls OK";
                return new String[] { remoteFile.getName() };
            }
            Vector files = sftpClient.ls(pathname);
            String[] rvFiles = new String[files.size()];
            for (int i = 0; i < files.size(); i++) {
                SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) files.get(i);
                rvFiles[i] = entry.filename;
            }
            reply = "ls OK";
            return rvFiles;
        } catch (Exception e) {
            reply = e.toString();
            return null;
        }
    }

    public boolean logout() throws IOException {
        reply = "logout OK";
        return false;
    }

    public boolean mkdir(String pathname) throws IOException {
        pathname = resolvePathname(pathname);
        try {
            sftpClient.mkdir(pathname, 484);
        } catch (Exception e) {
            reply = e.toString();
            return false;
        }
        reply = "mkdir OK";
        return true;
    }

    public Vector nList(String pathname) throws Exception {
        pathname = resolvePathname(pathname);
        Vector rvVector = new Vector();
        if (pathname.isEmpty()) {
            pathname = ".";
        }
        if (!fileExists(pathname)) {
            return rvVector;
        }
        if (!isDirectory(pathname)) {
            File remoteFile = new File(pathname);
            rvVector.add(remoteFile.getName());
            reply = "ls OK";
            return rvVector;
        }
        Vector files = sftpClient.ls(pathname);
        for (int i = 0; i < files.size(); i++) {
            SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) files.get(i);
            if (!entry.attributes.isDirectory()) {
                rvVector.add(entry.filename);
            }
        }
        reply = "ls OK";
        return rvVector;
    }

    public Vector nList(boolean recursive) throws Exception {
        String pathname = currentDirectory;
        if (!recursive) {
            return nList("");
        }
        Vector rvVector = new Vector();
        if (pathname.isEmpty()) {
            pathname = ".";
        }
        Vector files = sftpClient.ls(pathname);
        for (int i = 0; i < files.size(); i++) {
            SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) files.get(i);
            if (!entry.attributes.isDirectory()) {
                rvVector.add(entry.filename);
            } else if (!".".equals(entry.filename) && !"..".equals(entry.filename)) {
                nList(rvVector, currentDirectory, entry.filename);
            }
        }
        reply = "ls OK";
        return rvVector;
    }

    private void nList(Vector rvVector, String workingDirectory, String pathname) throws Exception {
        String slash = "";
        if (!workingDirectory.endsWith("/")) {
            slash = "/";
        }
        String fullPathname = currentDirectory + slash + pathname;
        Vector files = sftpClient.ls(fullPathname);
        for (int i = 0; i < files.size(); i++) {
            SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) files.get(i);
            if (!entry.attributes.isDirectory()) {
                rvVector.add(pathname + "/" + entry.filename);
            } else if (!".".equals(entry.filename) && !"..".equals(entry.filename)) {
                nList(rvVector, currentDirectory, pathname + "/" + entry.filename);
            }
        }
    }

    public long putFile(String localFile, String remoteFile) throws Exception {
        try {
            remoteFile = resolvePathname(remoteFile);
            SFTPv3FileHandle fileHandle = sftpClient.createFileTruncate(remoteFile);
            File localF = new File(localFile);
            FileInputStream fis = null;
            long offset = 0;
            try {
                fis = new FileInputStream(localF);
                byte[] buffer = new byte[32768];
                while (true) {
                    int len = fis.read(buffer, 0, buffer.length);
                    if (len <= 0) {
                        break;
                    }
                    sftpClient.write(fileHandle, offset, buffer, 0, len);
                    offset += len;
                }
                fis.close();
                fis = null;
            } catch (Exception e) {
                throw new Exception("error occurred writing file [" + localFile + "]: " + e.getMessage());
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                        fis = null;
                    } catch (Exception ex) {
                        // gracefully ignore this error
                    }
                }
            }
            sftpClient.closeFile(fileHandle);
            fileHandle = null;
            reply = "put OK";
            return offset;
        } catch (Exception e) {
            reply = e.toString();
            throw new Exception("Error during putFile: " + e, e);
        }
    }

    public boolean rename(String from, String to) throws IOException {
        from = resolvePathname(from);
        to = resolvePathname(to);
        try {
            sftpClient.mv(from, to);
        } catch (Exception e) {
            reply = e.toString();
            return false;
        }
        reply = "mv OK";
        return true;
    }

    public long size(String remoteFile) throws Exception {
        remoteFile = resolvePathname(remoteFile);
        try {
            return sftpClient.stat(remoteFile).size.longValue();
        } catch (Exception e) {
            throw new Exception("Error occured checking size: " + e, e);
        }
    }

    public long getFile(String remoteFile, String localFile, boolean append) throws Exception {
        String sourceLocation = resolvePathname(remoteFile);
        SFTPv3FileHandle sftpFileHandle = null;
        FileOutputStream fos = null;
        File transferFile = null;
        long remoteFileSize = -1;
        try {
            transferFile = new File(localFile);
            remoteFileSize = size(remoteFile);
            sftpFileHandle = sftpClient.openFileRO(sourceLocation);
            fos = null;
            long offset = 0;
            try {
                fos = new FileOutputStream(transferFile, append);
                byte[] buffer = new byte[32768];
                while (true) {
                    int len = sftpClient.read(sftpFileHandle, offset, buffer, 0, buffer.length);
                    if (len <= 0) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    offset += len;
                }
                fos.flush();
                fos.close();
                fos = null;
            } catch (Exception e) {
                throw new Exception("error occurred writing file [" + transferFile.getAbsolutePath() + "]: " + e.getMessage());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (Exception ex) {
                        // gracefully ignore this error
                    }
                }
            }
            sftpClient.closeFile(sftpFileHandle);
            sftpFileHandle = null;
            if (remoteFileSize > 0 && remoteFileSize != transferFile.length()) {
                throw new Exception("remote file size [" + remoteFileSize + "] and local file size [" + transferFile.length()
                        + "] are different. Number of bytes written to local file: " + offset);
            }
            return transferFile.length();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                sftpClient.closeFile(sftpFileHandle);
            } catch (Exception e) {
            }
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getAuthenticationFilename() {
        return authenticationFilename;
    }

    public void setAuthenticationFilename(String authenticationFilename) {
        this.authenticationFilename = authenticationFilename;
    }

    public void setAuthenticationFile(char[] authenticationFile) {
        this.authenticationFile = authenticationFile;
    }

    public char[] getAuthenticationFile() {
        return authenticationFile;
    }

    private boolean fileExists(String filename) {
        try {
            SFTPv3FileAttributes attributes = sftpClient.stat(filename);
            if (attributes != null) {
                return (attributes.isRegularFile() || attributes.isDirectory());
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDirectory(String filename) {
        try {
            return sftpClient.stat(filename).isDirectory();
        } catch (Exception e) {
        }
        return false;
    }

    public static void showUsage() {
        System.out.println("usage: SOSSFTP file sftphost sftpport sftpuser sftppassword [proxyhost] [proxyport] [proxyuser] [proxypassword]");
    }

    public static void main(String[] args) throws Exception {
        test2();
        return;
    }

    public static void test() throws Exception {
        SOSSFTP sftp = new SOSSFTP("wilma.sos");
        sftp.setAuthenticationMethod("password");
        sftp.setUser("test");
        sftp.setPassword("12345");
        sftp.connect();
        System.out.println("isConnected: " + sftp.isConnected());
        String[] files = sftp.listNames("");
        System.out.println("files: ");
        dumpArray(files);
        boolean rv = sftp.changeWorkingDirectory("ftp_data");
        sftp.changeWorkingDirectory("..");
        System.out.println("size: " + sftp.size("scheduler.test/bin/scheduler"));
        sftp.getFile("scheduler.test/bin/jobscheduler.sh", "C:/temp/ftp/jobscheduler.sh", false);
        files = sftp.listNames("scheduler.test/bin");
        System.out.println("files(scheduler.test/bin): ");
        dumpArray(files);
        files = sftp.listNames("scheduler.test/bin/jobscheduler.sh");
        System.out.println("files(scheduler.test/bin/jobscheduler.sh): ");
        dumpArray(files);
        sftp.changeWorkingDirectory("scheduler.managed2.demo");
        files = sftp.listNames("");
        System.out.println("files:(cd scheduler.managed2.demo)");
        dumpArray(files);
        sftp.changeWorkingDirectory("bin/");
        files = sftp.listNames("");
        System.out.println("files:(cd bin/)");
        dumpArray(files);
        sftp.changeWorkingDirectory("..");
        files = sftp.listNames("");
        System.out.println("files:(..)");
        dumpArray(files);
        sftp.mkdir("test");
        Vector homeSos = sftp.nList("/home/sos/");
        Vector recurs = sftp.nList(true);
        sftp.disconnect();
        System.out.println("isConnected: " + sftp.isConnected());
    }

    public static void test2() throws Exception {
        SOSSFTP sftp = new SOSSFTP("wilma.sos");
        sftp.setAuthenticationMethod("publickey");
        sftp.setAuthenticationFilename("C:/scheduler/publickeys/mo");
        sftp.setUser("test");
        sftp.setPassword("12345");
        sftp.connect();
        sftp.putFile("c:/sosftp/100MB.txt", "/home/test/sosftp/100MB.txt");
        sftp.setUser("test");
        sftp.setPassword("12345");
        sftp.connect();
        System.out.println("isConnected: " + sftp.isConnected());
        sftp.disconnect();
    }

    public static void dumpArray(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.println("  " + array[i].toString());
        }
    }

    @Override
    public long getFile(String remoteFile, String localFile) throws Exception {
        // TO DO Auto-generated method stub
        return 0;
    }

    @Override
    public Vector<String> nList() throws Exception {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public Vector<String> nList(String pathname, boolean flgRecurseSubFolder) throws Exception {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public boolean put(String localFile, String remoteFile) throws Exception {
        // TO DO Auto-generated method stub
        return false;
    }

    @Override
    public long putFile(String localFile, OutputStream out) throws Exception {
        // TO DO Auto-generated method stub
        return 0;
    }

}