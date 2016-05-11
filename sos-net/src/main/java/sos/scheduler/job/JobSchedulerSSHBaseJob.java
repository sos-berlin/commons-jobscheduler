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

/** @author andreas pueschel
 * @author ghassan beydoun */
public class JobSchedulerSSHBaseJob extends Job_impl {

    protected String host = "";
    protected int port = 0;
    protected String user = "";
    protected String password = "";
    protected String proxyHost = "";
    protected int proxyPort = 0;
    protected String proxyUser = "";
    protected String proxyPassword = "";
    protected String authenticationMethod = "publickey";
    protected String authenticationFilename = "";
    protected Connection sshConnection = null;
    protected Session sshSession = null;

    public void getBaseParameters() throws Exception {
        Order order = null;
        Variable_set params = null;
        try {
            params = spooler_task.params();
            if (spooler_task.job().order_queue() != null) {
                order = spooler_task.order();
                if (order.params() != null) {
                    params.merge(order.params());
                }
            }
            if (params.value("host") != null && !params.value("host").toString().isEmpty()) {
                this.setHost(params.value("host"));
                spooler_log.info(".. parameter [host]: " + this.getHost());
            } else {
                throw new Exception("no host name or ip address was specified as parameter [host]");
            }
            if (params.value("port") != null && !params.value("port").isEmpty()) {
                try {
                    this.setPort(Integer.parseInt(params.value("port")));
                    spooler_log.info(".. parameter [port]: " + this.getPort());
                } catch (Exception ex) {
                    throw new Exception("illegal non-numeric value for parameter [port]: " + params.value("port"));
                }
            } else {
                this.setPort(22);
            }
            if (params.value("user") != null && !params.value("user").isEmpty()) {
                this.setUser(params.value("user"));
                spooler_log.info(".. parameter [user]: " + this.getUser());
            } else {
                throw new Exception("no user name was specified as parameter [user]");
            }
            if (params.value("password") != null && !params.value("password").isEmpty()) {
                this.setPassword(params.value("password"));
                spooler_log.info(".. parameter [password]: ********");
            } else {
                this.setPassword("");
            }
            if (params.value("proxy_host") != null && !params.value("proxy_host").toString().isEmpty()) {
                this.setProxyHost(params.value("proxy_host"));
                spooler_log.info(".. parameter [proxy_host]: " + this.getProxyHost());
            } else {
                this.setProxyHost("");
            }
            if (params.value("proxy_port") != null && !params.value("proxy_port").isEmpty()) {
                try {
                    this.setProxyPort(Integer.parseInt(params.value("proxy_port")));
                    spooler_log.info(".. parameter [proxy_port]: " + this.getProxyPort());
                } catch (Exception ex) {
                    throw new Exception("illegal non-numeric value for parameter [proxy_port]: " + params.value("proxy_port"));
                }
            } else {
                this.setProxyPort(3128);
            }
            if (params.value("proxy_user") != null && !params.value("proxy_user").isEmpty()) {
                this.setProxyUser(params.value("proxy_user"));
                spooler_log.info(".. parameter [proxy_user]: " + this.getProxyUser());
            } else {
                this.setProxyUser("");
            }
            if (params.value("proxy_password") != null && !params.value("proxy_password").isEmpty()) {
                this.setProxyPassword(params.value("proxy_password"));
                spooler_log.info(".. parameter [proxy_password]: ********");
            } else {
                this.setProxyPassword("");
            }
            if (params.value("auth_method") != null && !params.value("auth_method").isEmpty()) {
                if ("publickey".equalsIgnoreCase(params.value("auth_method")) || "password".equalsIgnoreCase(params.value("auth_method"))) {
                    this.setAuthenticationMethod(params.value("auth_method"));
                    spooler_log.info(".. parameter [auth_method]: " + this.getAuthenticationMethod());
                } else {
                    throw new Exception("invalid authentication method [publickey, password] specified: " + params.value("auth_method"));
                }
            } else {
                this.setAuthenticationMethod("publickey");
            }
            if (params.value("auth_file") != null && !params.value("auth_file").isEmpty()) {
                this.setAuthenticationFilename(params.value("auth_file"));
                spooler_log.info(".. parameter [auth_file]: " + this.getAuthenticationFilename());
            } else if ("publickey".equalsIgnoreCase(this.getAuthenticationMethod())) {
                throw new Exception("no authentication filename was specified as parameter [auth_file");
            }
        } catch (Exception e) {
            throw new Exception("error occurred processing parameters: " + e.getMessage());
        }
    }

    public Connection getBaseAuthentication() throws Exception {
        try {
            boolean isAuthenticated = false;
            this.setSshConnection(new Connection(this.getHost(), this.getPort()));
            if (this.getProxyHost() != null && !this.getProxyHost().isEmpty()) {
                if (this.getProxyUser() != null && !this.getProxyUser().isEmpty()) {
                    this.getSshConnection().setProxyData(new HTTPProxyData(this.getProxyHost(), this.getProxyPort()));
                } else {
                    this.getSshConnection().setProxyData(
                            new HTTPProxyData(this.getProxyHost(), this.getProxyPort(), this.getProxyUser(), this.getProxyPassword()));
                }
            }
            this.getSshConnection().connect();
            if ("publickey".equalsIgnoreCase(this.getAuthenticationMethod())) {
                File authenticationFile = new File(this.getAuthenticationFilename());
                if (!authenticationFile.exists()) {
                    throw new Exception("authentication file does not exist: " + authenticationFile.getCanonicalPath());
                }
                if (!authenticationFile.canRead()) {
                    throw new Exception("authentication file not accessible: " + authenticationFile.getCanonicalPath());
                }
                isAuthenticated = this.getSshConnection().authenticateWithPublicKey(this.getUser(), authenticationFile, this.getPassword());
            } else if ("password".equalsIgnoreCase(this.getAuthenticationMethod())) {
                isAuthenticated = this.getSshConnection().authenticateWithPassword(this.getUser(), this.getPassword());
            }
            if (!isAuthenticated) {
                throw new Exception("authentication failed [host=" + this.getHost() + ", port=" + this.getPort() + ", user:" + this.getUser()
                        + ", auth_method=" + this.getAuthenticationMethod() + ", auth_file=" + this.getAuthenticationFilename());
            }
            return this.getSshConnection();
        } catch (Exception e) {
            if (this.getSshConnection() != null) {
                try {
                    this.getSshConnection().close();
                    this.setSshConnection(null);
                } catch (Exception ex) {
                    // gracefully ignore this error
                }
            }
            throw new Exception(e.getMessage());
        }
    }

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

    public boolean isDirectory(final SFTPv3Client sftpClient, final String filename) {
        try {
            return sftpClient.stat(filename).isDirectory();
        } catch (Exception e) {
            //
        }
        return false;
    }

    public long getFileSize(final SFTPv3Client sftpClient, final String filename) throws Exception {
        return sftpClient.stat(filename).size.longValue();
    }

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

    public String normalizePath(final String path) throws Exception {
        String normalizedPath = path.replaceAll("\\\\", "/");
        while (normalizedPath.endsWith("\\") || normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        return normalizedPath;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(final String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getAuthenticationFilename() {
        return authenticationFilename;
    }

    public void setAuthenticationFilename(final String authenticationFilename) {
        this.authenticationFilename = authenticationFilename;
    }

    private String getProxyPassword() {
        return proxyPassword;
    }

    private void setProxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    private String getProxyHost() {
        return proxyHost;
    }

    private void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    private int getProxyPort() {
        return proxyPort;
    }

    private void setProxyPort(final int proxyPort) {
        this.proxyPort = proxyPort;
    }

    private String getProxyUser() {
        return proxyUser;
    }

    private void setProxyUser(final String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public Connection getSshConnection() {
        return sshConnection;
    }

    public void setSshConnection(final Connection sshConnection) {
        this.sshConnection = sshConnection;
    }

    public Session getSshSession() {
        return sshSession;
    }

    public void setSshSession(final Session sshSession) {
        this.sshSession = sshSession;
    }

}