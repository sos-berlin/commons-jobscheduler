package sos.net.ssh;

import java.io.File;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.Session;

/** @author KB */
@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJob2SuperClass extends JSToolBox {

    protected SOSSSHJobOptions objOptions = null;
    protected Connection sshConnection = null;
    protected Session sshSession = null;

    SOSSSHJob2SuperClass() {
        //
    }

    public void Options(final SOSSSHJobOptions pobjOptions) throws Exception {
        objOptions = pobjOptions;
        objOptions.checkMandatory();
    }

    public SOSSSHJobOptions Options() {
        if (objOptions == null) {
            objOptions = new SOSSSHJobOptions();
        }
        return objOptions;
    }

    protected Connection getBaseAuthentication() throws Exception {
        final String conMethodName = "SOSSSHJob2SuperClass::getBaseAuthentication";
        try {
            boolean isAuthenticated = false;
            this.setSshConnection(new Connection(Options().host.Value(), objOptions.port.value()));
            if (!objOptions.proxyHost.IsEmpty()) {
                if (!objOptions.proxyUser.IsEmpty()) {
                    this.getSshConnection().setProxyData(new HTTPProxyData(objOptions.proxyHost.Value(), objOptions.proxyPort.value()));
                } else {
                    this.getSshConnection().setProxyData(
                            new HTTPProxyData(objOptions.proxyHost.Value(), objOptions.proxyPort.value(), objOptions.proxyUser.Value(),
                                    objOptions.proxyPassword.Value()));
                }
            }
            this.getSshConnection().connect();
            if (objOptions.authMethod.isPublicKey()) {
                File authenticationFile = new File(objOptions.authFile.Value());
                if (!authenticationFile.exists()) {
                    throw new Exception("authentication file does not exist: " + authenticationFile.getCanonicalPath());
                }
                if (!authenticationFile.canRead()) {
                    throw new Exception("authentication file not accessible: " + authenticationFile.getCanonicalPath());
                }
                isAuthenticated = this.getSshConnection().authenticateWithPublicKey(objOptions.user.Value(), authenticationFile, objOptions.password.Value());
            } else if (objOptions.authMethod.isPassword()) {
                isAuthenticated = this.getSshConnection().authenticateWithPassword(objOptions.user.Value(), objOptions.password.Value());
            }
            if (!isAuthenticated) {
                throw new Exception(conMethodName + ": " + "authentication failed " + objOptions.toString());
            }
            return this.getSshConnection();
        } catch (Exception e) {
            if (this.getSshConnection() != null) {
                try {
                    this.getSshConnection().close();
                    this.setSshConnection(null);
                } catch (Exception ex) {
                    //
                }
            }
            throw new Exception(e.getMessage());
        }
    }

    protected boolean sshFileExists(SFTPv3Client sftpClient, String filename) {
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

    protected boolean isDirectory(SFTPv3Client sftpClient, String filename) {
        try {
            return sftpClient.stat(filename).isDirectory();
        } catch (Exception e) {
            //
        }
        return false;
    }

    protected long getFileSize(SFTPv3Client sftpClient, String filename) throws Exception {
        return sftpClient.stat(filename).size.longValue();
    }

    protected int sshFilePermissions(SFTPv3Client sftpClient, String filename) {
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

    private String normalizePath(String path) throws Exception {
        String normalizedPath = path.replaceAll("\\\\", "/");
        while (normalizedPath.endsWith("\\") || normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        return normalizedPath;
    }

    protected Connection getSshConnection() {
        return sshConnection;
    }

    protected void setSshConnection(Connection sshConnection) {
        this.sshConnection = sshConnection;
    }

    public Session getSshSession() {
        return sshSession;
    }

    public void setSshSession(Session sshSession) {
        this.sshSession = sshSession;
    }

}