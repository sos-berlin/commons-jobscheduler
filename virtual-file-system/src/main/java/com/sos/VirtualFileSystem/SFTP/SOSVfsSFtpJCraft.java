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

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtpJCraft extends SOSVfsTransferBaseClass {

    private final static Logger LOGGER = Logger.getLogger(SOSVfsSFtpJCraft.class);
    private Channel sshConnection = null;
    private Session sshSession = null;
    private ChannelSftp sftpClient = null;
    private JSch secureChannel = null;
    private Map environmentVariables = null;
    private Integer exitCode;
    private String exitSignal;
    private StringBuffer outContent;
    private StringBuffer errContent;
    private boolean isRemoteWindowsShell = false;
    private int connectionTimeout = 0;
    private boolean simulateShell = false;
    private SOSOptionProxyProtocol proxyProtocol = null;
    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPassword = null;

    public SOSVfsSFtpJCraft() {
        super();
        secureChannel = new JSch();
    }

    @Override
    public void setStrictHostKeyChecking(final String val) {
        JSch.setConfig("StrictHostKeyChecking", val);
    }

    private String getStrictHostKeyChecking(final SOSOptionBoolean val) {
        return val.value() ? "yes" : "no";
    }

    @Override
    public ISOSConnection Connect() {
        this.Connect(connection2OptionsAlternate);
        return this;
    }

    @Override
    public ISOSConnection Connect(final SOSConnection2OptionsAlternate options) {
        connection2OptionsAlternate = options;
        if (connection2OptionsAlternate == null) {
            RaiseException(SOSVfs_E_190.params("connection2OptionsAlternate"));
        }
        this.setStrictHostKeyChecking(getStrictHostKeyChecking(connection2OptionsAlternate.strictHostKeyChecking));
        this.connect(connection2OptionsAlternate.host.Value(), connection2OptionsAlternate.port.value());
        return this;
    }

    @Override
    public ISOSConnection Authenticate(final ISOSAuthenticationOptions options) {
        authenticationOptions = options;
        try {
            proxyProtocol = connection2OptionsAlternate.proxy_protocol;
            proxyHost = connection2OptionsAlternate.proxy_host.Value();
            proxyPort = connection2OptionsAlternate.proxy_port.value();
            proxyUser = connection2OptionsAlternate.proxy_user.Value();
            proxyPassword = connection2OptionsAlternate.proxy_password.Value();
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
            this.LogReply();
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_134.params("authentication"));
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
            LOGGER.debug(HostID(SOSVfs_D_179.params("mkdir", p)));
            String[] subfolders = objF.getSubFolderArrayReverse();
            int idx = subfolders.length;
            for (String subFolder : objF.getSubFolderArrayReverse()) {
                SftpATTRS attributes = getAttributes(subFolder);
                if (attributes != null && attributes.isDir()) {
                    LOGGER.debug(SOSVfs_E_180.params(subFolder));
                    break;
                }
                if (attributes != null && !attributes.isDir()) {
                    RaiseException(SOSVfs_E_277.params(subFolder));
                    break;
                }
                idx--;
            }
            subfolders = objF.getSubFolderArray();
            for (int i = idx; i < subfolders.length; i++) {
                this.getClient().mkdir(subfolders[i]);
                LOGGER.debug(HostID(SOSVfs_E_0106.params("mkdir", subfolders[i], getReplyString())));
            }
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_134.params("[mkdir]"));
        }
    }

    @Override
    public void rmdir(final String path) {
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(path);
            reply = "rmdir OK";
            for (String subfolder : objF.getSubFolderArrayReverse()) {
                LOGGER.debug(HostID(SOSVfs_D_179.params("rmdir", subfolder)));
                this.getClient().rmdir(subfolder);
                reply = "rmdir OK";
                LOGGER.debug(HostID(SOSVfs_D_181.params("rmdir", subfolder, getReplyString())));
            }
            logINFO(HostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_134.params("[rmdir]"));
        }
    }

    @Override
    public boolean isDirectory(final String filename) {
        /** Problem: the concept of links is not considered in the Concept of
         * SOSVfs Therefore: wie return here "true" if the filename is a link */
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
            logINFO(HostID(SOSVfs_I_182.params("getFile", sourceLocation, localFile, getReplyString())));
        } catch (Exception ex) {
            reply = ex.toString();
            RaiseException(ex, SOSVfs_E_184.params("getFile", sourceLocation, localFile));
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
            logINFO(HostID(SOSVfs_I_183.params("putFile", localFile, remoteFile, getReplyString())));
            return this.size(remoteFile);
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_185.params("putFile()", localFile, remoteFile));
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
            RaiseException(ex, SOSVfs_E_187.params("delete", path));
        }
        reply = "rm OK";
        logINFO(HostID(SOSVfs_D_181.params("delete", path, getReplyString())));
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
        LOGGER.info(HostID(SOSVfs_I_189.params(from, to, getReplyString())));
    }

    @Override
    public void ExecuteCommand(String cmd) {
        final String endOfLine = System.getProperty("line.separator");
        ChannelExec channelExec = null;
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
            cmd = cmd.replaceAll("\0", "\\\\\\\\").replaceAll("\"", "\\\"");
            channelExec.setCommand(cmd);
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
                errContent.append(line + endOfLine);
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
            if (connection2OptionsAlternate.raise_exception_on_error.value()) {
                throw ex;
            }
        } catch (Exception ex) {
            reply = ex.toString();
            if (connection2OptionsAlternate.raise_exception_on_error.value()) {
                RaiseException(ex, SOSVfs_E_134.params("ExecuteCommand"));
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
            logINFO(HostID(SOSVfs_I_192.params(getReplyString())));
        }
    }

    public void setEnvironmentVariables(Map<String, String> envVariables) {
        this.environmentVariables = envVariables;
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        try {
            return this.getClient().get(fileName);
        } catch (Exception ex) {
            RaiseException(ex, SOSVfs_E_193.params("getInputStream()", fileName));
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
            RaiseException(ex, SOSVfs_E_193.params("getOutputStream()", fileName));
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
            LOGGER.debug(HostID(SOSVfs_D_195.params(path)));
            LogReply();
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_134.params("getCurrentPath"));
        }
        return path;
    }

    private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions options) throws Exception {
        authenticationOptions = options;
        userName = authenticationOptions.getUser().Value();
        String password = authenticationOptions.getPassword().Value();
        LOGGER.debug(SOSVfs_D_132.params(userName));
        setKnownHostsFile();
        this.createSession(userName, host, port);
        if (authenticationOptions.getAuth_method().isPublicKey()) {
            LOGGER.debug(SOSVfs_D_165.params("userid", "publickey"));
            SOSOptionInFileName authenticationFile = authenticationOptions.getAuth_file();
            authenticationFile.CheckMandatory(true);
            if (authenticationFile.IsNotEmpty()) {
                if (authenticationOptions.getPassword().IsNotEmpty()) {
                    secureChannel.addIdentity(authenticationFile.JSFile().getPath(), authenticationOptions.getPassword().Value());
                } else {
                    secureChannel.addIdentity(authenticationFile.JSFile().getPath());
                }
            }
        } else {
            if (authenticationOptions.getAuth_method().isPassword()) {
                LOGGER.debug(SOSVfs_D_165.params("userid", "password"));
                sshSession.setPassword(password);
            } else {
                throw new JobSchedulerException(SOSVfs_E_166.params(authenticationOptions.getAuth_method().Value()));
            }
        }
        try {
            sshSession.setConfig("PreferredAuthentications", authenticationOptions.getAuth_method().Value());
            setConfigFromFiles();
            sshSession.connect();
            this.createSftpClient();
        } catch (Exception e) {
            throw new JobSchedulerException(HostID(e.getClass().getName() + " - " + e.getMessage()), e);
        }
        reply = "OK";
        LOGGER.info(SOSVfs_D_133.params(userName));
        this.LogReply();
        return this;
    }

    private void setConfigFromFiles() {
        if (!SOSString.isEmpty(connection2OptionsAlternate.configuration_files.Value())) {
            String[] arr = connection2OptionsAlternate.configuration_files.Value().split(";");
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
                    RaiseException(SOSVfs_E_190.params("sshConnection Object"));
                }
                sftpClient = (ChannelSftp) sshConnection;
            } catch (Exception e) {
                RaiseException(e, SOSVfs_E_196.get());
            }
        }
        return sftpClient;
    }

    private void connect(final String phost, final int pport) {
        host = phost;
        port = pport;
        LOGGER.info(SOSVfs_D_0101.params(host, port));
        if (!this.isConnected()) {
            this.LogReply();
        } else {
            logWARN(SOSVfs_D_0103.params(host, port));
        }
    }

    private void createSession(final String puser, final String phost, final int pport) throws Exception {
        if (secureChannel == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("secureChannel"));
        }
        sshSession = secureChannel.getSession(puser, phost, pport);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", connection2OptionsAlternate.strictHostKeyChecking.Value());
        if (connection2OptionsAlternate.use_zlib_compression.value()) {
            config.put("compression.s2c", "zlib@openssh.com,zlib,none");
            config.put("compression.c2s", "zlib@openssh.com,zlib,none");
            config.put("compression_level", connection2OptionsAlternate.zlib_compression_level.Value());
            LOGGER.info(String.format("use zlib_compression: compression.s2c = %s, compression.c2s = %s, compression_level = %s",
                    config.getProperty("compression.s2c"), config.getProperty("compression.c2s"),
                    connection2OptionsAlternate.zlib_compression_level.Value()));
        }
        sshSession.setConfig(config);
        setCommandsTimeout();
        setProxy();
        setConnectionTimeout();
    }

    private void setConnectionTimeout() throws Exception {
        if (connectionTimeout > 0) {
            LOGGER.info(String.format("connection timeout = %s ms", connectionTimeout));
            sshSession.setTimeout(connectionTimeout);
        }
    }

    private void setCommandsTimeout() throws Exception {
        //
    }

    private void setProxy() throws Exception {
        if (!SOSString.isEmpty(this.proxyHost)) {
            int connTimeout = 30000; // 0,5 Minute
            LOGGER.info(String.format("using proxy: protocol = %s, host = %s, port = %s, user = %s, pass = ?", proxyProtocol.Value(), proxyHost,
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
                connectionTimeout = connTimeout;
            } else if (proxyProtocol.isSocks4()) {
                ProxySOCKS4 proxy = new ProxySOCKS4(proxyHost, proxyPort);
                if (!SOSString.isEmpty(proxyUser)) {
                    proxy.setUserPasswd(proxyUser, proxyPassword);
                }
                sshSession.setProxy(proxy);
                connectionTimeout = connTimeout;
            } else {
                throw new Exception(String.format("unknown proxy protocol = %s", proxyProtocol.Value()));
            }
        }
    }

    private void createSftpClient() throws Exception {
        if (sshSession == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("sshSession"));
        }
        sshConnection = sshSession.openChannel("sftp");
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
        ExecuteCommand("echo %ComSpec%");
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

}