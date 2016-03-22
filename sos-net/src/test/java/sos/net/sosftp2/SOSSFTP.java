/*
 * SOSSFTP.java Created on 19.12.2007
 */
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

    /** remote host name or ip address */
    protected String host = "";

    /** remote ssh2 port */
    protected int port = 22;

    /** user name on remote host */
    protected String user = "";

    /** for publickey authentication this password secures the authentication
     * file, for password authentication this is the password */
    protected String password = "";

    /** optional proxy configuration */
    protected String proxyHost = "";

    protected int proxyPort = 0;

    protected String proxyUser = "";

    protected String proxyPassword = "";

    /** authentication method: publickey, password */
    protected String authenticationMethod = "publickey";

    /** value for authentication method password */
    public static final String AUTH_METHOD_PASSWORD = "password";

    /** value for authentication method publickey */
    public static final String AUTH_METHOD_PUBLICKEY = "publickey";

    /** key file: ~/.ssh/id_rsa or ~/.ssh/id_dsa */
    protected String authenticationFilename = "";

    /** ssh connection object */
    protected Connection sshConnection = null;

    /** ssh session object */
    protected Session sshSession = null;

    /** SFTP Client **/
    protected SFTPv3Client sftpClient = null;

    protected boolean connected = false;

    // keep Track of current directory for ftp emulation
    private String currentDirectory = "";

    protected String reply = "OK";

    char[] authenticationFile = null;

    public SOSSFTP(String host) {
        this.host = host;
    }

    public SOSSFTP(String host, int port) {
        this(host);
        this.port = port;
    }

    public void connect() throws Exception {
        try { // to connect and authenticate
            boolean isAuthenticated = false;
            sshConnection = new Connection(this.getHost(), this.getPort());

            if (this.getProxyHost() != null && this.getProxyHost().length() > 0) {
                if (this.getProxyUser() != null && this.getProxyUser().length() > 0) {
                    sshConnection.setProxyData(new HTTPProxyData(this.getProxyHost(), this.getProxyPort(), this.getProxyUser(), this.getProxyPassword()));
                } else {
                    sshConnection.setProxyData(new HTTPProxyData(this.getProxyHost(), this.getProxyPort()));
                }
            }

            sshConnection.connect();

            if (this.getAuthenticationMethod().equalsIgnoreCase("publickey")) {
                if (getAuthenticationFile() != null) {
                    isAuthenticated = sshConnection.authenticateWithPublicKey(this.getUser(), getAuthenticationFile(), getPassword());
                } else if (getAuthenticationFilename() != null && getAuthenticationFilename().startsWith("local:")) {
                    String filename = getAuthenticationFilename().substring("local:".length());
                    String text = sos.util.SOSFile.readFile(new File(filename));
                    isAuthenticated = sshConnection.authenticateWithPublicKey(this.getUser(), text.toCharArray(), this.getPassword());
                } else if (getAuthenticationFilename() != null && getAuthenticationFilename().startsWith("filecontent:")) {
                    isAuthenticated = sshConnection.authenticateWithPublicKey(this.getUser(), getAuthenticationFilename().substring("filecontent:".length()).toCharArray(), this.getPassword());
                } else {
                    File authenticationFile = new File(this.getAuthenticationFilename());
                    if (!authenticationFile.exists())
                        throw new Exception("authentication file does not exist: " + authenticationFile.getCanonicalPath());
                    if (!authenticationFile.canRead())
                        throw new Exception("authentication file not accessible: " + authenticationFile.getCanonicalPath());
                    isAuthenticated = sshConnection.authenticateWithPublicKey(this.getUser(), authenticationFile, this.getPassword());
                    // isAuthenticated =
                    // sshConnection.authenticateWithPublicKey(this.getUser(),
                    // authenticationFile, this.getPassword());
                }
            } else if (this.getAuthenticationMethod().equalsIgnoreCase("password")) {
                isAuthenticated = sshConnection.authenticateWithPassword(this.getUser(), this.getPassword());
            } else {
                throw new Exception("Unknown authentication method: " + getAuthenticationMethod());
            }

            if (!isAuthenticated)
                throw new Exception("authentication failed [host=" + this.getHost() + ", port=" + this.getPort() + ", user:" + this.getUser()
                        + ", auth_method=" + this.getAuthenticationMethod() + ", auth_file=" + this.getAuthenticationFilename());

            sftpClient = new SFTPv3Client(sshConnection);
            connected = true;
            reply = "OK";
        } catch (Exception e) {
            reply = e.toString();
            if (sshConnection != null)
                try {
                    disconnect();
                } catch (Exception ex) {
                } // gracefully ignore this error
            throw new Exception("Error occured connecting: " + e, e);
        }
    }

    private String resolvePathname(String pathname) {
        if ((!pathname.startsWith("./") && !pathname.startsWith("/")) && currentDirectory.length() > 0) {
            // if (!pathname.startsWith("/") && currentDirectory.length()>0){
            String slash = "";
            if (!currentDirectory.endsWith("/"))
                slash = "/";
            pathname = currentDirectory + slash + pathname;
        }
        return pathname;
    }

    public boolean changeWorkingDirectory(String pathname) throws IOException {
        pathname = resolvePathname(pathname);
        // cut trailing "/" if it's not the only character
        if (pathname.length() > 1 && pathname.endsWith("/"))
            pathname = pathname.substring(0, pathname.length() - 1);
        if (!fileExists(pathname)) {
            reply = "\"" + pathname + "\" doesn't exist.";
            return false;
        }
        if (!isDirectory(pathname)) {
            reply = "\"" + pathname + "\" is not a directory.";
            return false;
        }
        if (pathname.startsWith("/") || currentDirectory.length() == 0) {
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
        if (sftpClient != null)
            try {
                sftpClient.close();
                sftpClient = null;
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            } // gracefully ignore this error
        if (sshConnection != null)
            try {
                sshConnection.close();
                sshConnection = null;
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            } // gracefully ignore this error
        connected = false;
    }

    // no reply String for sftp
    public String getReplyString() {
        return reply;
    }

    public boolean isConnected() {
        return connected;
    }

    public String[] listNames(String pathname) throws IOException {
        pathname = resolvePathname(pathname);
        try {
            if (pathname.length() == 0)
                pathname = ".";
            if (!fileExists(pathname))
                return null;
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
        // do nothing, only disconnect()
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

        if (pathname.length() == 0)
            pathname = ".";
        if (!fileExists(pathname))
            return rvVector;
        if (!isDirectory(pathname)) {
            File remoteFile = new File(pathname);
            rvVector.add(remoteFile.getName());
            reply = "ls OK";
            return rvVector;
        }
        Vector files = sftpClient.ls(pathname);

        for (int i = 0; i < files.size(); i++) {
            SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) files.get(i);
            if (!entry.attributes.isDirectory())
                rvVector.add(entry.filename);
        }
        reply = "ls OK";
        return rvVector;

    }

    /** return a listing of the contents of a directory in short format on the
     * remote machine
     *
     * @return a listing of the contents of a directory on the remote machine
     *
     * @exception Exception
     * @see #nList(String )
     * @see #dir()
     * @see #dir(String ) */
    public Vector nList(boolean recursive) throws Exception {
        String pathname = currentDirectory;
        if (!recursive)
            return nList("");
        Vector rvVector = new Vector();
        if (pathname.length() == 0)
            pathname = ".";
        Vector files = sftpClient.ls(pathname);
        for (int i = 0; i < files.size(); i++) {
            SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) files.get(i);
            if (!entry.attributes.isDirectory()) {
                rvVector.add(entry.filename);
            } else if (!entry.filename.equals(".") && !entry.filename.equals("..")) {
                nList(rvVector, currentDirectory, entry.filename);
            }
        }
        reply = "ls OK";
        return rvVector;
    }

    private void nList(Vector rvVector, String workingDirectory, String pathname) throws Exception {
        String slash = "";
        if (!workingDirectory.endsWith("/"))
            slash = "/";
        String fullPathname = currentDirectory + slash + pathname;
        Vector files = sftpClient.ls(fullPathname);
        for (int i = 0; i < files.size(); i++) {
            SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) files.get(i);
            if (!entry.attributes.isDirectory()) {
                rvVector.add(pathname + "/" + entry.filename);
            } else if (!entry.filename.equals(".") && !entry.filename.equals("..")) {
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
                // byte[] buffer = new byte[10240];
                byte[] buffer = new byte[32768];

                while (true) {
                    int len = fis.read(buffer, 0, buffer.length);
                    if (len <= 0)
                        break;
                    sftpClient.write(fileHandle, offset, buffer, 0, len);
                    offset += len;
                }

                fis.close();
                fis = null;

            } catch (Exception e) {
                throw new Exception("error occurred writing file [" + localFile + "]: " + e.getMessage());
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                        fis = null;
                    } catch (Exception ex) {
                    } // gracefully ignore this error
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

    /** return the size of remote-file on the remote machine on success,
     * otherwise -1
     * 
     * @param remoteFile the file on remote machine
     * @return the size of remote-file on remote machine */
    public long size(String remoteFile) throws Exception {
        remoteFile = resolvePathname(remoteFile);
        try {
            return sftpClient.stat(remoteFile).size.longValue();
        } catch (Exception e) {
            throw new Exception("Error occured checking size: " + e, e);
        }
    }

    /** Retrieves a named file from the ftp server.
     *
     * @param localFile The name of the local file.
     * @param remoteFile The name of the remote file.
     * @param append Appends the remote file to the local file.
     * @return The total number of bytes retrieved.
     * @see #get(String, String )
     * @exception Exception */
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
                    if (len <= 0)
                        break;
                    fos.write(buffer, 0, len);
                    offset += len;
                }
                fos.flush();
                fos.close();
                fos = null;

            } catch (Exception e) {
                throw new Exception("error occurred writing file [" + transferFile.getAbsolutePath() + "]: " + e.getMessage());
            } finally {
                if (fos != null)
                    try {
                        fos.close();
                        fos = null;
                    } catch (Exception ex) {
                    } // gracefully ignore this error
            }

            sftpClient.closeFile(sftpFileHandle);
            sftpFileHandle = null;

            if (remoteFileSize > 0 && remoteFileSize != transferFile.length())
                throw new Exception("remote file size [" + remoteFileSize + "] and local file size [" + transferFile.length()
                        + "] are different. Number of bytes written to local file: " + offset);

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

    /** @return the host */
    public String getHost() {
        return host;
    }

    /** @param host the host to set */
    public void setHost(String host) {
        this.host = host;
    }

    /** @return the port */
    public int getPort() {
        return port;
    }

    /** @param port the port to set */
    public void setPort(int port) {
        this.port = port;
    }

    /** @return the user */
    public String getUser() {
        return user;
    }

    /** @param user the user to set */
    public void setUser(String user) {
        this.user = user;
    }

    /** @return the password */
    public String getPassword() {
        return password;
    }

    /** @param password the password to set */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return the proxyHost */
    public String getProxyHost() {
        return proxyHost;
    }

    /** @param proxyHost the proxyHost to set */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /** @return the proxyPort */
    public int getProxyPort() {
        return proxyPort;
    }

    /** @param proxyPort the proxyPort to set */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /** @return the proxyUser */
    public String getProxyUser() {
        return proxyUser;
    }

    /** @param proxyUser the proxyUser to set */
    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    /** @return the proxyPassword */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /** @param proxyPassword the proxyPassword to set */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /** @return the authenticationMethod */
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    /** @param authenticationMethod the authenticationMethod to set */
    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    /** @return the authenticationFilename */
    public String getAuthenticationFilename() {
        return authenticationFilename;
    }

    /** @param authenticationFilename the authenticationFilename to set */
    public void setAuthenticationFilename(String authenticationFilename) {
        this.authenticationFilename = authenticationFilename;
    }

    /** @param authenticationFilename the authenticationFilename to set */
    public void setAuthenticationFile(char[] authenticationFile) {
        this.authenticationFile = authenticationFile;
    }

    public char[] getAuthenticationFile() {
        return authenticationFile;
    }

    /** Check existence of a file or directory
     * 
     * @param filename
     * @return true, if file exists
     * @throws Exception */
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

    /** Checks if file is a directory
     * 
     * @param filename
     * @return true, if filename is a directory */
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
        if (true)
            return;

        if (args.length < 1) {
            showUsage();
            System.exit(0);
        }
        if (args[0].trim().equalsIgnoreCase("-t")) {
            test();
            System.exit(0);
        }

        File file = new File(args[0]);
        String host = args[1];
        int port = Integer.parseInt(args[2]);

        String user = args[3];
        String password = args[4];
        SOSSFTP sftp = new SOSSFTP(host, port);

        sftp.setAuthenticationMethod("password");
        sftp.setUser(user);
        sftp.setPassword(password);
        if (args.length > 5) {// proxy
            System.out.println("Using proxy Server " + args[5]);
            sftp.setProxyHost(args[5]);
            sftp.setProxyPort(Integer.parseInt(args[6]));
            if (args.length > 7) {// proxy mit user
                System.out.println("Using proxy Server with username and password");
                sftp.setProxyUser(args[7]);
                sftp.setProxyPassword(args[8]);
            }
        }
        System.out.println("Connecting...");
        sftp.connect();
        System.out.println("isConnected: " + sftp.isConnected());
        String[] files = sftp.listNames("");
        System.out.println("files in remote directory: ");
        dumpArray(files);

        System.out.println("Transferring file " + file.getAbsolutePath());
        sftp.putFile(file.getAbsolutePath(), file.getName());
        System.out.println("Disconnecting...");
        sftp.disconnect();
        System.out.println("isConnected: " + sftp.isConnected());
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

        // test 1 lies den Inhalt der Datei aus
        sftp.setAuthenticationFilename("C:/scheduler/publickeys/mo");
        sftp.setUser("test");
        sftp.setPassword("12345");

        sftp.connect();
        // sftp.putFile("c:/sosftp/100MB.txt", "/home/test/sosftp/100MB.txt");
        sftp.putFile("c:/sosftp/100MB.txt", "/home/test/sosftp/100MB.txt");
        // test 2 übergibt die Dateiname
        // sftp.setAuthenticationFilename("C:/scheduler/publickeys/mo");

        // test 3 übergibt den Inhalt des Files als char[]
        /*
         * String filename = "C:/scheduler/publickeys/mo"; String text =
         * sos.util.SOSFile.readFile(new File(filename)); char[] c =
         * text.toCharArray(); System.out.println(c);
         * sftp.setAuthenticationFile(c);
         */

        // test 4 gibt den Inhalt als Text
        /*
         * String filename = "C:/scheduler/publickeys/mo"; String text =
         * sos.util.SOSFile.readFile(new File(filename));
         * sftp.setAuthenticationFilename("filecontent:" + text);
         */

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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Vector<String> nList() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vector<String> nList(String pathname, boolean flgRecurseSubFolder) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean put(String localFile, String remoteFile) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long putFile(String localFile, OutputStream out) throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }
}
