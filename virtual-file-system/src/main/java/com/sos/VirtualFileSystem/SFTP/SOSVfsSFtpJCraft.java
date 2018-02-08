package com.sos.VirtualFileSystem.SFTP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import sos.util.SOSString;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.SOSRequiredAuthPassword;
import com.jcraft.jsch.SOSRequiredAuthPublicKey;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.keepass.SOSKeePassDatabase;
import com.sos.keepass.SOSKeePassPath;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtpJCraft extends SOSVfsTransferBaseClass {

    private final static Logger LOGGER = Logger.getLogger(SOSVfsSFtpJCraft.class);
    private final static int DEFAULT_CONNECTION_TIMEOUT = 30_000; // 0,5 minutes
    private Channel sshConnection = null;
    private Session sshSession = null;
    private ChannelSftp sftpClient = null;
    private JSch secureChannel = null;
    private Integer exitCode;
    private String exitSignal;
    private StringBuffer outContent;
    private StringBuffer errContent;
    private boolean isRemoteWindowsShell = false;
    private boolean isUnix = false;
    private boolean isOSChecked = false;
    private int connectionTimeout = 0;
    private boolean simulateShell = false;
    private SOSOptionProxyProtocol proxyProtocol = null;
    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPassword = null;
    private ChannelExec channelExec = null;
    private final String lineSeparator = System.getProperty("line.separator");

    public SOSVfsSFtpJCraft() {
        super();
        JSch.setLogger(new SOSVfsSFtpJCraftLogger());
        secureChannel = new JSch();
    }

    @Override
    public void setStrictHostKeyChecking(final String val) {
        JSch.setConfig("StrictHostKeyChecking", val);
    }

    @SuppressWarnings("unused")
    private String getStrictHostKeyChecking(final SOSOptionBoolean val) {
        return val.value() ? "yes" : "no";
    }

    @Override
    public ISOSConnection connect() {
        this.connect(connection2OptionsAlternate);
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate options) {
        connection2OptionsAlternate = options;
        if (connection2OptionsAlternate == null) {
            raiseException(SOSVfs_E_190.params("connection2OptionsAlternate"));
        }
        this.setStrictHostKeyChecking(connection2OptionsAlternate.strictHostKeyChecking.getValue());
        this.doConnect(connection2OptionsAlternate.host.getValue(), connection2OptionsAlternate.port.value());
        return this;
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions options) {
        authenticationOptions = options;
        try {
            proxyProtocol = connection2OptionsAlternate.proxyProtocol;
            proxyHost = connection2OptionsAlternate.proxyHost.getValue();
            proxyPort = connection2OptionsAlternate.proxyPort.value();
            proxyUser = connection2OptionsAlternate.proxyUser.getValue();
            proxyPassword = connection2OptionsAlternate.proxyPassword.getValue();
            this.doAuthenticate(authenticationOptions);
        } catch (JobSchedulerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
        return this;
    }

    @Override
    public void login(final String user, final String password) {
        try {
            userName = user;
            LOGGER.debug(SOSVfs_D_132.params(userName));
            this.createSession(userName, host, port);
            sshSession.setPassword(password);
            setConfigFromFiles();
            sshSession.connect();
            this.createSftpClient();
            reply = "OK";
            LOGGER.info(SOSVfs_D_133.params(userName));
            this.logReply();
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_134.params("authentication"));
        }
    }

    @Override
    public void disconnect() {
        reply = "disconnect OK";
        if (sftpClient != null) {
            try {
                sftpClient.exit();
                if (sftpClient.isConnected()) {
                    sftpClient.disconnect();
                }
                sftpClient = null;
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            }
        }
        if (sshConnection != null) {
            try {
                sshConnection.disconnect();
                sshConnection = null;
            } catch (Exception ex) {
                reply = "disconnect: " + ex;
            }
        }
        this.logINFO(reply);
    }

    @Override
    public void logout() {
        try {

            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
                logDEBUG(SOSVfs_D_138.params(host, getReplyString()));
            } else {
                logINFO("not connected, logout useless.");
            }
        } catch (Exception e) {
            logWARN(SOSVfs_W_140.get() + e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        return sftpClient != null && sftpClient.isConnected();
    }

    @Override
    public void mkdir(final String path) {
        try {
            String p = path.replaceAll("//+", "/").replaceFirst("/$", "");
            SOSOptionFolderName objF = new SOSOptionFolderName(path);
            reply = "mkdir OK";
            LOGGER.debug(getHostID(SOSVfs_D_179.params("mkdir", p)));
            String[] subfolders = objF.getSubFolderArrayReverse();
            int idx = subfolders.length;
            for (String subFolder : objF.getSubFolderArrayReverse()) {
                SftpATTRS attributes = getAttributes(subFolder);
                if (attributes != null && attributes.isDir()) {
                    LOGGER.debug(SOSVfs_E_180.params(subFolder));
                    break;
                }
                if (attributes != null && !attributes.isDir()) {
                    raiseException(SOSVfs_E_277.params(subFolder));
                    break;
                }
                idx--;
            }
            subfolders = objF.getSubFolderArray();
            for (int i = idx; i < subfolders.length; i++) {
                this.getClient().mkdir(subfolders[i]);
                LOGGER.debug(getHostID(SOSVfs_E_0106.params("mkdir", subfolders[i], getReplyString())));
            }
        } catch (Exception e) {
            reply = e.toString();
            raiseException(e, SOSVfs_E_134.params("[mkdir]"));
        }
    }

    @Override
    public void rmdir(final String path) {
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(path);
            reply = "rmdir OK";
            for (String subfolder : objF.getSubFolderArrayReverse()) {
                LOGGER.debug(getHostID(SOSVfs_D_179.params("rmdir", subfolder)));
                this.getClient().rmdir(subfolder);
                reply = "rmdir OK";
                LOGGER.debug(getHostID(SOSVfs_D_181.params("rmdir", subfolder, getReplyString())));
            }
            logINFO(getHostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            raiseException(e, SOSVfs_E_134.params("[rmdir]"));
        }
    }

    @Override
    public boolean isDirectory(final String filename) {
        boolean result = false;
        try {
            SftpATTRS attributes = this.getClient().stat(filename);
            if (attributes != null) {
                result = attributes.isDir();
                if (!result) {
                    result = attributes.isLink();
                }
            }
        } catch (Exception e) {
            //
        }
        return result;
    }

    public SftpATTRS getAttributes(final String filename) {
        SftpATTRS attributes = null;
        try {
            attributes = this.getClient().stat(filename);
        } catch (Exception e) {
            //
        }
        return attributes;
    }

    @Override
    public String[] listNames(String path) throws IOException {
        path = resolvePathname(path);
        try {
            if (path.isEmpty()) {
                path = ".";
            }
            if (!this.fileExists(path)) {
                return null;
            }
            if (!isDirectory(path)) {
                reply = "ls OK";
                return new String[] { path };
            }
            @SuppressWarnings("unchecked")
            Vector<LsEntry> lsResult = this.getClient().ls(path);
            this.getSOSFileEntries().clear();
            String[] result = new String[lsResult.size()];
            String sep = path.endsWith("/") ? "" : "/";
            for (int i = 0, j = 0; i < lsResult.size(); i++) {
                LsEntry entry = lsResult.get(i);
                SOSFileEntry sosFileEntry = new SOSFileEntry();
                sosFileEntry.setDirectory(entry.getAttrs().isDir());
                sosFileEntry.setFilename(entry.getFilename());
                sosFileEntry.setFilesize(entry.getAttrs().getSize());
                sosFileEntry.setParentPath(path);
                this.getSOSFileEntries().add(sosFileEntry);
                String fileName = path + sep + entry.getFilename();
                result[j++] = fileName;
            }
            reply = "ls OK";
            return result;
        } catch (Exception e) {
            reply = e.toString();
            return null;
        }
    }

    @Override
    public long size(String remoteFile) throws Exception {
        remoteFile = this.resolvePathname(remoteFile);
        long size = -1;
        SftpATTRS objAttr;
        try {
            objAttr = this.getClient().stat(remoteFile);
            if (objAttr != null) {
                size = objAttr.getSize();
            }
        } catch (SftpException e) {
            //
        }
        return size;
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) {
        String sourceLocation = this.resolvePathname(remoteFile);
        File transferFile = null;
        long remoteFileSize = -1;
        FileOutputStream fos = null;
        try {
            remoteFileSize = this.size(remoteFile);
            fos = new FileOutputStream(localFile, append);
            this.getClient().get(sourceLocation, fos);
            fos.flush();
            fos.close();
            fos = null;
            transferFile = new File(localFile);
            if (!append && remoteFileSize > 0 && remoteFileSize != transferFile.length()) {
                throw new JobSchedulerException(SOSVfs_E_162.params(remoteFileSize, transferFile.length()));
            }
            remoteFileSize = transferFile.length();
            reply = "get OK";
            logINFO(getHostID(SOSVfs_I_182.params("getFile", sourceLocation, localFile, getReplyString())));
        } catch (Exception ex) {
            reply = ex.toString();
            raiseException(ex, SOSVfs_E_184.params("getFile", sourceLocation, localFile));
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex) {
                    //
                }
            }
        }
        return remoteFileSize;
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) {
        long size = 0;
        try {
            this.getClient().put(localFile, this.resolvePathname(remoteFile), ChannelSftp.OVERWRITE);
            reply = "put OK";
            logINFO(getHostID(SOSVfs_I_183.params("putFile", localFile, remoteFile, getReplyString())));
            return this.size(remoteFile);
        } catch (Exception e) {
            reply = e.toString();
            raiseException(e, SOSVfs_E_185.params("putFile()", localFile, remoteFile));
        }
        return size;
    }

    @Override
    public void delete(final String path) {
        try {
            if (this.isDirectory(path)) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }
            this.getClient().rm(path);
        } catch (Exception ex) {
            reply = ex.toString();
            raiseException(ex, SOSVfs_E_187.params("delete", path));
        }
        reply = "rm OK";
        logINFO(getHostID(SOSVfs_D_181.params("delete", path, getReplyString())));
    }

    @Override
    public void rename(String from, String to) {
        from = this.resolvePathname(from);
        to = this.resolvePathname(to);
        try {
            this.getClient().rename(from, to);
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_188.params("rename", from, to), e);
        }
        reply = "mv OK";
        LOGGER.info(getHostID(SOSVfs_I_189.params(from, to, getReplyString())));
    }

    @Override
    public void executeCommand(String cmd) {
        executeCommand(cmd, null);
    }

    @Override
    public void executeCommand(String cmd, Map<String, String> env) {
        checkOS();

        cmd = cmd.trim();
        channelExec = null;
        exitCode = null;
        InputStream out = null;
        InputStream err = null;
        BufferedReader errReader = null;
        try {
            if (sshSession == null) {
                throw new JobSchedulerException(SOSVfs_E_190.params("sshSession"));
            }
            channelExec = (ChannelExec) sshSession.openChannel("exec");
            channelExec.setPty(isSimulateShell());

            StringBuffer envs = new StringBuffer();
            if (env != null) {
                env.forEach((k, v) -> {
                    // envs.append(String.format("set %s=%s", k, v));
                    if (isUnix) {
                        envs.append(String.format("export %s=%s;", k, v));
                    } else {
                        envs.append(String.format("set %s=%s&", k, v));
                    }
                });
                LOGGER.debug(String.format("setEnv: %s", envs.toString()));
            }
            cmd = cmd.replaceAll("\0", "\\\\\\\\").replaceAll("\"", "\\\"");
            channelExec.setCommand(envs.toString() + cmd);
            channelExec.setInputStream(null);
            channelExec.setErrStream(null);
            out = channelExec.getInputStream();
            err = channelExec.getErrStream();
            channelExec.connect();
            LOGGER.debug(SOSVfs_D_163.params("stdout", cmd));
            outContent = new StringBuffer();
            byte[] tmp = new byte[1024];
            while (true) {
                while (out.available() > 0) {
                    int i = out.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    outContent.append(new String(tmp, 0, i));
                }
                if (channelExec.isClosed()) {
                    exitCode = channelExec.getExitStatus();
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    //
                }
            }
            if (outContent.length() > 0) {
                LOGGER.info(outContent);
            }
            LOGGER.debug(SOSVfs_D_163.params("stderr", cmd));
            errReader = new BufferedReader(new InputStreamReader(err));
            errContent = new StringBuffer();
            while (true) {
                String line = errReader.readLine();
                if (line == null) {
                    break;
                }
                errContent.append(line + lineSeparator);
            }
            LOGGER.debug(errContent);
            if (exitCode != null && !exitCode.equals(new Integer(0))) {
                StringBuffer errMsg = new StringBuffer();
                errMsg.append(exitCode.toString());
                if (errContent.length() > 0) {
                    errMsg.append(" (");
                    errMsg.append(errContent);
                    errMsg.append(")");
                }
                throw new JobSchedulerException(SOSVfs_E_164.params(errMsg));
            }
            reply = "OK";
        } catch (JobSchedulerException ex) {
            reply = ex.toString();
            if (connection2OptionsAlternate.raiseExceptionOnError.value()) {
                throw ex;
            }
        } catch (Exception ex) {
            reply = ex.toString();
            if (connection2OptionsAlternate.raiseExceptionOnError.value()) {
                raiseException(ex, SOSVfs_E_134.params("ExecuteCommand"));
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    //
                }
            }
            if (errReader != null) {
                try {
                    errReader.close();
                } catch (Exception e) {
                    //
                }
            }
            if (err != null) {
                try {
                    err.close();
                } catch (Exception e) {
                    //
                }
            }
            if (channelExec != null) {
                try {
                    channelExec.disconnect();
                } catch (Exception e) {
                    //
                }
            }
            logINFO(getHostID(SOSVfs_I_192.params(getReplyString())));
        }
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        try {
            return this.getClient().get(fileName);
        } catch (Exception ex) {
            raiseException(ex, SOSVfs_E_193.params("getInputStream()", fileName));
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(final String fileName) {
        try {
            boolean modeAppend = false;
            boolean modeRestart = false;
            int transferMode = ChannelSftp.OVERWRITE;
            if (modeAppend) {
                transferMode = ChannelSftp.APPEND;
            } else if (modeRestart) {
                transferMode = ChannelSftp.RESUME;
            }
            return this.getClient().put(fileName, transferMode);
        } catch (Exception ex) {
            raiseException(ex, SOSVfs_E_193.params("getOutputStream()", fileName));
            return null;
        }
    }

    @Override
    public boolean changeWorkingDirectory(String pathname) {
        try {
            pathname = this.resolvePathname(pathname);
            if (!this.fileExists(pathname)) {
                reply = String.format("Filepath '%1$s' does not exist.", pathname);
                return false;
            }
            if (!this.isDirectory(pathname)) {
                reply = String.format("Filepath '%1$s' is not a directory.", pathname);
                return false;
            }
            this.getClient().cd(pathname);
            reply = "cwd OK";
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("cwd", pathname), ex);
        } finally {
            LOGGER.debug(SOSVfs_D_194.params(pathname, getReplyString()));
        }
        return true;
    }

    @Override
    public ISOSVirtualFile getFileHandle(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSVirtualFile file = new SOSVfsSFtpFileJCraft(fileName);
        file.setHandler(this);
        return file;
    }

    @Override
    public String getModificationTime(final String path) {
        String dateTime = null;
        try {
            SftpATTRS objAttr = this.getClient().stat(path);
            if (objAttr != null) {
                int mt = objAttr.getMTime();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateTime = df.format(new Date(mt));
            }
        } catch (SftpException e) {
            //
        }
        return dateTime;
    }

    @Override
    protected boolean fileExists(final String filename) {
        try {
            SftpATTRS attributes = this.getClient().stat(filename);
            if (attributes != null) {
                return !attributes.isLink() || attributes.isDir();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getCurrentPath() {
        String path = null;
        try {
            path = this.getClient().pwd();
            LOGGER.debug(getHostID(SOSVfs_D_195.params(path)));
            logReply();
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_134.params("getCurrentPath"));
        }
        return path;
    }

    private void usePublicKeyMethod() throws Exception {
        String authMethod = "publickey";
        Object kd = connection2OptionsAlternate.keepass_database.value();
        if (kd == null) {
            SOSOptionInFileName authenticationFile = authenticationOptions.getAuthFile();
            authenticationFile.checkMandatory(true);
            if (authenticationFile.isNotEmpty()) {
                try {
                    if (authenticationOptions.getPassphrase().isNotEmpty()) {
                        LOGGER.debug(String.format("[%s]file=%s, passphrase=?", authMethod, authenticationOptions.getAuthFile().getValue()));
                        secureChannel.addIdentity(authenticationFile.getJSFile().getPath(), authenticationOptions.getPassphrase().getValue());
                    } else {
                        LOGGER.debug(String.format("[%s]file=%s", authMethod, authenticationOptions.getAuthFile().getValue()));
                        secureChannel.addIdentity(authenticationFile.getJSFile().getPath());
                    }
                } catch (JSchException e) {
                    throw new Exception(String.format("[%s][%s]%s", authMethod, authenticationOptions.getAuthFile().getValue(), e.toString()), e);
                }
            }
        } else {
            SOSKeePassDatabase kpd = (SOSKeePassDatabase) kd;
            org.linguafranca.pwdb.Entry<?, ?, ?, ?> entry =
                    (org.linguafranca.pwdb.Entry<?, ?, ?, ?>) connection2OptionsAlternate.keepass_database_entry.value();

            try {
                byte[] pr = kpd.getAttachment(entry, connection2OptionsAlternate.keepass_attachment_property_name.getValue());
                String keePassPath = entry.getPath() + SOSKeePassPath.PROPERTY_PREFIX + connection2OptionsAlternate.keepass_attachment_property_name
                        .getValue();
                if (authenticationOptions.getPassphrase().isNotEmpty()) {
                    LOGGER.debug(String.format("[%s][keepass]attachment=%s, passphrase=?", authMethod, keePassPath));
                    secureChannel.addIdentity("yade", pr, (byte[]) null, authenticationOptions.getPassphrase().getValue().getBytes());
                } else {
                    LOGGER.debug(String.format("[%s][keepass]attachment=%s", authMethod, keePassPath));
                    secureChannel.addIdentity("yade", pr, (byte[]) null, (byte[]) null);
                }
            } catch (Exception e) {
                throw new Exception(String.format("[%s][keepass]%s", authMethod, e.toString()), e);
            }
        }
    }

    private void usePasswordMethod() throws Exception {
        LOGGER.debug(String.format("[password]password=?"));
        sshSession.setPassword(authenticationOptions.getPassword().getValue());
    }

    private String usePreferredAuthentications(final String debugKey, final String preferredAuthentications) throws Exception {
        LOGGER.debug(String.format("[%s]preferredAuthentications=%s", debugKey, preferredAuthentications));
        try {
            usePublicKeyMethod();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        usePasswordMethod();
        return preferredAuthentications;
    }

    private String useRequiredAuthentications(final String requiredAuthentications) throws Exception {
        String preferredAuthentications = requiredAuthentications;
        LOGGER.debug(String.format("[required_authentications]preferredAuthentications=%s", preferredAuthentications));

        connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

        sshSession.setConfig("userauth.password", SOSRequiredAuthPassword.class.getName());
        sshSession.setConfig("userauth.publickey", SOSRequiredAuthPublicKey.class.getName());

        usePublicKeyMethod();
        usePasswordMethod();
        return preferredAuthentications;
    }

    private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions options) throws Exception {
        authenticationOptions = options;
        userName = authenticationOptions.getUser().getValue();
        setKnownHostsFile();
        createSession(userName, host, port);

        String preferredAuthentications = null;
        if (connection2OptionsAlternate.preferred_authentications.isNotEmpty()) {
            preferredAuthentications = usePreferredAuthentications("preferred_authentications", connection2OptionsAlternate.preferred_authentications
                    .getValue());
        } else if (connection2OptionsAlternate.required_authentications.isNotEmpty()) {
            preferredAuthentications = useRequiredAuthentications(connection2OptionsAlternate.required_authentications.getValue());
        } else {
            if (authenticationOptions.getPassword().isNotEmpty() && authenticationOptions.getAuthFile().isNotEmpty()) {
                preferredAuthentications = usePreferredAuthentications("password,publickey", "password,publickey");
            } else {
                preferredAuthentications = authenticationOptions.getAuthMethod().getValue();
                LOGGER.debug(String.format("preferredAuthentications=%s", preferredAuthentications));
                if (authenticationOptions.getAuthMethod().isPublicKey()) {
                    usePublicKeyMethod();
                } else {
                    usePasswordMethod();
                }
            }
        }
        try {
            setConnectionTimeout();
            sshSession.setConfig("PreferredAuthentications", preferredAuthentications);
            setConfigFromFiles();
            sshSession.connect();
            this.createSftpClient();
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(e.getClass().getName() + " - " + e.getMessage()), e);
        }
        reply = "OK";
        LOGGER.info(SOSVfs_D_133.params(userName));
        this.logReply();
        return this;
    }

    private void setConfigFromFiles() {
        if (!SOSString.isEmpty(connection2OptionsAlternate.configuration_files.getValue())) {
            String[] arr = connection2OptionsAlternate.configuration_files.getValue().split(";");
            for (int i = 0; i < arr.length; i++) {
                String file = arr[i].trim();
                LOGGER.info(String.format("use configuration file: %s", file));
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    Properties p = new Properties();
                    p.load(in);
                    for (Entry<Object, Object> entry : p.entrySet()) {
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();
                        LOGGER.debug(String.format("set configuration setting: %s = %s", key, value));
                        sshSession.setConfig(key, value);
                    }
                } catch (Exception ex) {
                    LOGGER.warn(String.format("error on read configuration file[%s]: %s", file, ex.toString()));
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception ex) {
                            //
                        }
                    }
                }
            }
        }
    }

    private void setKnownHostsFile() throws JSchException {
        if (secureChannel != null && connection2OptionsAlternate.strictHostKeyChecking.isTrue()) {
            File knownHostsFile = new File(System.getProperty("user.home"), ".ssh/known_hosts");
            secureChannel.setKnownHosts(knownHostsFile.getAbsolutePath());
        }
    }

    public ChannelSftp getClient() {
        if (sftpClient == null) {
            try {
                if (sshConnection == null) {
                    raiseException(SOSVfs_E_190.params("sshConnection Object"));
                }
                sftpClient = (ChannelSftp) sshConnection;
            } catch (Exception e) {
                raiseException(e, SOSVfs_E_196.get());
            }
        }
        return sftpClient;
    }

    private void doConnect(final String phost, final int pport) {
        host = phost;
        port = pport;
        LOGGER.info(SOSVfs_D_0101.params(host, port));
        if (!this.isConnected()) {
            this.logReply();
        } else {
            logWARN(SOSVfs_D_0103.params(host, port));
        }
    }

    private void createSession(final String puser, final String phost, final int pport) throws Exception {
        if (secureChannel == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("secureChannel"));
        }
        LOGGER.debug(String.format("user=%s, host=%s, port=%s", puser, phost, pport));
        sshSession = secureChannel.getSession(puser, phost, pport);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", connection2OptionsAlternate.strictHostKeyChecking.getValue());
        if (connection2OptionsAlternate.useZlibCompression.value()) {
            config.put("compression.s2c", "zlib@openssh.com,zlib,none");
            config.put("compression.c2s", "zlib@openssh.com,zlib,none");
            config.put("compression_level", connection2OptionsAlternate.zlibCompressionLevel.getValue());
            LOGGER.info(String.format("use zlib_compression: compression.s2c = %s, compression.c2s = %s, compression_level = %s", config.getProperty(
                    "compression.s2c"), config.getProperty("compression.c2s"), connection2OptionsAlternate.zlibCompressionLevel.getValue()));
        }
        sshSession.setConfig(config);
        setCommandsTimeout();
        setProxy();
    }

    private void setConnectionTimeout() throws Exception {
        if (connectionTimeout > 0) {
            LOGGER.info(String.format("connectionTimeout=%s ms", connectionTimeout));
            sshSession.setTimeout(connectionTimeout);
        }
    }

    private void setCommandsTimeout() throws Exception {
        //
    }

    private void setProxy() throws Exception {
        if (!SOSString.isEmpty(this.proxyHost)) {
            LOGGER.info(String.format("using proxy: protocol = %s, host = %s, port = %s, user = %s, pass = ?", proxyProtocol.getValue(), proxyHost,
                    proxyPort, proxyUser));
            if (proxyProtocol.isHttp()) {
                ProxyHTTP proxy = new ProxyHTTP(proxyHost, proxyPort);
                if (!SOSString.isEmpty(proxyUser)) {
                    proxy.setUserPasswd(proxyUser, proxyPassword);
                }
                sshSession.setProxy(proxy);
            } else if (proxyProtocol.isSocks5()) {
                ProxySOCKS5 proxy = new ProxySOCKS5(proxyHost, proxyPort);
                if (!SOSString.isEmpty(proxyUser)) {
                    proxy.setUserPasswd(proxyUser, proxyPassword);
                }
                sshSession.setProxy(proxy);
                connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
            } else if (proxyProtocol.isSocks4()) {
                ProxySOCKS4 proxy = new ProxySOCKS4(proxyHost, proxyPort);
                if (!SOSString.isEmpty(proxyUser)) {
                    proxy.setUserPasswd(proxyUser, proxyPassword);
                }
                sshSession.setProxy(proxy);
                connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
            } else {
                throw new Exception(String.format("unknown proxy protocol = %s", proxyProtocol.getValue()));
            }
        }
    }

    private void createSftpClient() throws Exception {
        if (sshSession == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("sshSession"));
        }
        sshConnection = sshSession.openChannel("sftp");
        sshSession.setConfig("compression_level", connection2OptionsAlternate.zlibCompressionLevel.getValue());
        sshConnection.connect();
        sftpClient = (ChannelSftp) sshConnection;
    }

    @Override
    public void close() {
        //
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public SOSFileEntries getSOSFileEntries() {
        return sosFileEntries;
    }

    public StringBuffer getStdErr() throws Exception {
        return errContent;
    }

    @Override
    public StringBuffer getStdOut() throws Exception {
        return outContent;
    }

    @Override
    public Integer getExitCode() {
        return exitCode;
    }

    @Override
    public String getExitSignal() {
        return exitSignal;
    }

    @Override
    public String createScriptFile(String content) throws Exception {
        try {
            String commandScript = content;
            if (!isRemoteWindowsShell) {
                commandScript = commandScript.replaceAll("(?m)\r", "");
            }
            LOGGER.debug(SOSVfs_I_233.params(content));
            File tempScriptFile = File.createTempFile("sos-sshscript", getScriptFileNameSuffix());
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempScriptFile)));
            out.write(commandScript);
            out.flush();
            out.close();
            tempScriptFile.deleteOnExit();
            putFile(tempScriptFile, 0700);
            String name = tempScriptFile.getName();
            if (!isRemoteWindowsShell) {
                name = "./" + name;
            }
            LOGGER.info(SOSVfs_I_253.params(tempScriptFile.getAbsolutePath()));
            return name;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    private String getScriptFileNameSuffix() {
        if (isRemoteWindowsShell) {
            return ".cmd";
        } else {
            return ".sh";
        }
    }

    @Override
    public boolean remoteIsWindowsShell() {
        executeCommand("echo %ComSpec%");
        if (outContent.toString().indexOf("cmd.exe") > -1) {
            LOGGER.debug(SOSVfs_D_237.get());
            isRemoteWindowsShell = true;
            return true;
        }
        return false;
    }

    private void putFile(File commandFile, Integer chmod) throws Exception {
        String name = commandFile.getName();
        getClient().put(commandFile.getCanonicalPath(), name);
        if (chmod != null) {
            getClient().chmod(chmod, name);
        }
    }

    @Override
    public boolean isSimulateShell() {
        return simulateShell;
    }

    @Override
    public void setSimulateShell(boolean simulateShell) {
        this.simulateShell = simulateShell;
    }

    public ChannelExec getChannelExec() {
        return channelExec;
    }

    private void checkOS() {
        if (!isOSChecked) {
            String cmd = "echo $PATH";
            try {
                CommandResult result = executePrivateCommand(cmd);
                // @TODO parse PATH
                if (result.getStdOut().toString().indexOf("/bin:") > -1) {
                    isUnix = true;
                }
                LOGGER.info(String.format("isUnix=%s", isUnix));
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s]%s", cmd, e.toString()));
            }
            isOSChecked = true;
        }
    }

    private CommandResult executePrivateCommand(String cmd) throws Exception {
        ChannelExec channel = null;
        InputStream in = null;
        InputStream err = null;
        BufferedReader errReader = null;
        CommandResult result = new CommandResult();
        try {
            cmd = cmd.trim();
            LOGGER.debug(String.format("cmd=%s", cmd));
            channel = (ChannelExec) sshSession.openChannel("exec");
            channel.setPty(isSimulateShell());
            channel.setCommand(cmd);
            channel.setInputStream(null);
            channel.setErrStream(null);
            in = channel.getInputStream();
            err = channel.getErrStream();
            channel.connect();
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    result.getStdOut().append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    result.setExitCode(channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Throwable ee) {
                    //
                }
            }

            errReader = new BufferedReader(new InputStreamReader(err));
            while (true) {
                String line = errReader.readLine();
                if (line == null) {
                    break;
                }
                result.getStdErr().append(line + lineSeparator);
            }

        } catch (Throwable e) {
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable ex) {
                }
            }
            if (errReader != null) {
                try {
                    errReader.close();
                } catch (Throwable ex) {
                }
            }
            if (err != null) {
                try {
                    err.close();
                } catch (Throwable ex) {
                }
            }
            if (channel != null) {
                try {
                    channel.disconnect();
                } catch (Throwable ex) {
                }
            }
        }
        return result;
    }

    private class CommandResult {

        private int _exitCode;
        private StringBuffer _stdOut;
        private StringBuffer _stdErr;

        public CommandResult() {
            _stdOut = new StringBuffer();
            _stdErr = new StringBuffer();
        }

        @SuppressWarnings("unused")
        public int getExitCode() {
            return _exitCode;
        }

        public void setExitCode(int val) {
            _exitCode = val;
        }

        public StringBuffer getStdOut() {
            return _stdOut;
        }

        public StringBuffer getStdErr() {
            return _stdErr;
        }
    }
}
