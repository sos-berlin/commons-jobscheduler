package sos.scheduler.job;

import java.io.File;

import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.Session;

/** @author andreas.pueschel@sos-berlin.com
 * @author ghassan.beydoun@sos-berlin.com $Id: JobSchedulerSSHBaseJob.java 18220
 *         2012-10-18 07:46:10Z kb $ Base Class for jobs using ssh, sftp, scp */

public class JobSchedulerSSHBaseJob extends Job_impl {

    private final String conSVNVersion = "$Id: JobSchedulerSSHBaseJob.java 18220 2012-10-18 07:46:10Z kb $";

    /** remote host name or ip address */
    protected String host = "";

    /** remote ssh2 port */
    protected int port = 0;

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

    /** key file: ~/.ssh/id_rsa or ~/.ssh/id_dsa */
    protected String authenticationFilename = "";

    /** ssh connection object */
    protected Connection sshConnection = null;

    /** ssh session object */
    protected Session sshSession = null;

    /** Parameter-Processing */
    public void getBaseParameters() throws Exception {

        Order order = null;
        Variable_set params = null;

        try { // to fetch parameters, order parameters have precedence to job
              // parameters

            params = spooler_task.params();

            if (spooler_task.job().order_queue() != null) {
                order = spooler_task.order();
                if (order.params() != null)
                    params.merge(order.params());
            }

            if (params.value("host") != null && params.value("host").toString().length() > 0) {
                this.setHost(params.value("host"));
                spooler_log.info(".. parameter [host]: " + this.getHost());
            } else {
                throw new Exception("no host name or ip address was specified as parameter [host]");
            }

            if (params.value("port") != null && params.value("port").length() > 0) {
                try {
                    this.setPort(Integer.parseInt(params.value("port")));
                    spooler_log.info(".. parameter [port]: " + this.getPort());
                } catch (Exception ex) {
                    throw new Exception("illegal non-numeric value for parameter [port]: " + params.value("port"));
                }
            } else {
                this.setPort(22);
            }

            if (params.value("user") != null && params.value("user").length() > 0) {
                this.setUser(params.value("user"));
                spooler_log.info(".. parameter [user]: " + this.getUser());
            } else {
                throw new Exception("no user name was specified as parameter [user]");
            }

            if (params.value("password") != null && params.value("password").length() > 0) {
                this.setPassword(params.value("password"));
                spooler_log.info(".. parameter [password]: ********");
            } else {
                this.setPassword("");
            }

            if (params.value("proxy_host") != null && params.value("proxy_host").toString().length() > 0) {
                this.setProxyHost(params.value("proxy_host"));
                spooler_log.info(".. parameter [proxy_host]: " + this.getProxyHost());
            } else {
                this.setProxyHost("");
            }

            if (params.value("proxy_port") != null && params.value("proxy_port").length() > 0) {
                try {
                    this.setProxyPort(Integer.parseInt(params.value("proxy_port")));
                    spooler_log.info(".. parameter [proxy_port]: " + this.getProxyPort());
                } catch (Exception ex) {
                    throw new Exception("illegal non-numeric value for parameter [proxy_port]: " + params.value("proxy_port"));
                }
            } else {
                this.setProxyPort(3128);
            }

            if (params.value("proxy_user") != null && params.value("proxy_user").length() > 0) {
                this.setProxyUser(params.value("proxy_user"));
                spooler_log.info(".. parameter [proxy_user]: " + this.getProxyUser());
            } else {
                this.setProxyUser("");
            }

            if (params.value("proxy_password") != null && params.value("proxy_password").length() > 0) {
                this.setProxyPassword(params.value("proxy_password"));
                spooler_log.info(".. parameter [proxy_password]: ********");
            } else {
                this.setProxyPassword("");
            }

            if (params.value("auth_method") != null && params.value("auth_method").length() > 0) {
                if (params.value("auth_method").equalsIgnoreCase("publickey") || params.value("auth_method").equalsIgnoreCase("password")) {
                    this.setAuthenticationMethod(params.value("auth_method"));
                    spooler_log.info(".. parameter [auth_method]: " + this.getAuthenticationMethod());
                } else {
                    throw new Exception("invalid authentication method [publickey, password] specified: " + params.value("auth_method"));
                }
            } else {
                this.setAuthenticationMethod("publickey");
            }

            if (params.value("auth_file") != null && params.value("auth_file").length() > 0) {
                this.setAuthenticationFilename(params.value("auth_file"));
                spooler_log.info(".. parameter [auth_file]: " + this.getAuthenticationFilename());
            } else {
                if (this.getAuthenticationMethod().equalsIgnoreCase("publickey"))
                    throw new Exception("no authentication filename was specified as parameter [auth_file");
            }

        } catch (Exception e) {
            throw new Exception("error occurred processing parameters: " + e.getMessage());
        }
    }

    /** Authentication-Processing */
    public Connection getBaseAuthentication() throws Exception {

        try { // to connect and authenticate
            boolean isAuthenticated = false;
            this.setSshConnection(new Connection(this.getHost(), this.getPort()));

            if (this.getProxyHost() != null && this.getProxyHost().length() > 0) {
                if (this.getProxyUser() != null && this.getProxyUser().length() > 0) {
                    this.getSshConnection().setProxyData(new HTTPProxyData(this.getProxyHost(), this.getProxyPort()));
                } else {
                    this.getSshConnection().setProxyData(new HTTPProxyData(this.getProxyHost(), this.getProxyPort(), this.getProxyUser(), this.getProxyPassword()));
                }
            }

            this.getSshConnection().connect();

            if (this.getAuthenticationMethod().equalsIgnoreCase("publickey")) {
                File authenticationFile = new File(this.getAuthenticationFilename());
                if (!authenticationFile.exists())
                    throw new Exception("authentication file does not exist: " + authenticationFile.getCanonicalPath());
                if (!authenticationFile.canRead())
                    throw new Exception("authentication file not accessible: " + authenticationFile.getCanonicalPath());

                isAuthenticated = this.getSshConnection().authenticateWithPublicKey(this.getUser(), authenticationFile, this.getPassword());
            } else if (this.getAuthenticationMethod().equalsIgnoreCase("password")) {
                isAuthenticated = this.getSshConnection().authenticateWithPassword(this.getUser(), this.getPassword());
            }

            if (!isAuthenticated)
                throw new Exception("authentication failed [host=" + this.getHost() + ", port=" + this.getPort() + ", user:" + this.getUser()
                        + ", auth_method=" + this.getAuthenticationMethod() + ", auth_file=" + this.getAuthenticationFilename());

            return this.getSshConnection();

        } catch (Exception e) {
            if (this.getSshConnection() != null)
                try {
                    this.getSshConnection().close();
                    this.setSshConnection(null);
                } catch (Exception ex) {
                } // gracefully ignore this error
            throw new Exception(e.getMessage());
        }
    }

    /** Check existence of a file or directory
     * 
     * @param sftpClient
     * @param filename
     * @return true, if file exists
     * @throws Exception */
    public boolean sshFileExists(final SFTPv3Client sftpClient, final String filename) {

        try {
            SFTPv3FileAttributes attributes = sftpClient.stat(filename);

            if (attributes != null) {
                return attributes.isRegularFile() || attributes.isDirectory();
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    /** Checks if file is a directory
     * 
     * @param sftpClient
     * @param filename
     * @return true, if filename is a directory */
    public boolean isDirectory(final SFTPv3Client sftpClient, final String filename) {
        try {
            return sftpClient.stat(filename).isDirectory();
        } catch (Exception e) {
        }
        return false;
    }

    /** Returns the file size of a file
     * 
     * @param sftpClient
     * @param filename
     * @return the size of the file
     * @throws Exception */
    public long getFileSize(final SFTPv3Client sftpClient, final String filename) throws Exception {
        return sftpClient.stat(filename).size.longValue();
    }

    /** Check existence of a file or directory
     * 
     * @param sftpClient
     * @param filename
     * @return integer representation of file permissions
     * @throws Exception */
    public int sshFilePermissions(final SFTPv3Client sftpClient, final String filename) {

        try {
            SFTPv3FileAttributes attributes = sftpClient.stat(filename);

            if (attributes != null) {
                return attributes.permissions.intValue();
            } else {
                return 0;
            }

        } catch (Exception e) {
            return 0;
        }
    }

    /** normalize / to \ and remove trailing slashes from a path
     * 
     * @param path
     * @return normalized path
     * @throws Exception */
    public String normalizePath(final String path) throws Exception {

        String normalizedPath = path.replaceAll("\\\\", "/");
        while (normalizedPath.endsWith("\\") || normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }

        return normalizedPath;
    }

    /** @return Returns the host. */
    public String getHost() {
        return host;
    }

    /** @param host The host to set. */
    public void setHost(final String host) {
        this.host = host;
    }

    /** @return Returns the port. */
    public int getPort() {
        return port;
    }

    /** @param port The port to set. */
    public void setPort(final int port) {
        this.port = port;
    }

    /** @return Returns the authenticationMethod. */
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    /** @param authenticationMethod The authenticationMethod to set. */
    public void setAuthenticationMethod(final String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    /** @return Returns the password. */
    public String getPassword() {
        return password;
    }

    /** @param password The password to set. */
    public void setPassword(final String password) {
        this.password = password;
    }

    /** @return Returns the user. */
    public String getUser() {
        return user;
    }

    /** @param user The user to set. */
    public void setUser(final String user) {
        this.user = user;
    }

    /** @return Returns the authenticationFilename. */
    public String getAuthenticationFilename() {
        return authenticationFilename;
    }

    /** @param authenticationFilename The authenticationFilename to set. */
    public void setAuthenticationFilename(final String authenticationFilename) {
        this.authenticationFilename = authenticationFilename;
    }

    /** @return Returns the proxyPassword. */
    private String getProxyPassword() {
        return proxyPassword;
    }

    /** @param proxyPassword The proxyPassword to set. */
    private void setProxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /** @return Returns the proxyHost. */
    private String getProxyHost() {
        return proxyHost;
    }

    /** @param proxyHost The proxyHost to set. */
    private void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /** @return Returns the proxyPort. */
    private int getProxyPort() {
        return proxyPort;
    }

    /** @param proxyPort The proxyPort to set. */
    private void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /** @return Returns the proxyUser. */
    private String getProxyUser() {
        return proxyUser;
    }

    /** @param proxyUser The proxyUser to set. */
    private void setProxyUser(final String proxyUser) {
        this.proxyUser = proxyUser;
    }

    /** @return Returns the sshConnection. */
    public Connection getSshConnection() {
        return sshConnection;
    }

    /** @param sshConnection The sshConnection to set. */
    public void setSshConnection(final Connection sshConnection) {
        this.sshConnection = sshConnection;
    }

    /** @return Returns the sshSession. */
    public Session getSshSession() {
        return sshSession;
    }

    /** @param sshSession The sshSession to set. */
    public void setSshSession(final Session sshSession) {
        this.sshSession = sshSession;
    }

}
