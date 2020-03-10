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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;

import org.jurr.jsch.bugfix111.JSCH111BugFix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.SOSRequiredAuthKeyboardInteractive;
import com.jcraft.jsch.SOSRequiredAuthPassword;
import com.jcraft.jsch.SOSRequiredAuthPublicKey;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSCommandResult;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSFileEntry.EntryType;
import com.sos.VirtualFileSystem.common.SOSShellInfo;
import com.sos.VirtualFileSystem.common.SOSShellInfo.OS;
import com.sos.VirtualFileSystem.common.SOSShellInfo.Shell;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.keepass.SOSKeePassDatabase;
import com.sos.keepass.SOSKeePassPath;

import sos.util.SOSDate;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtpJCraft extends SOSVfsTransferBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsSFtpJCraft.class);

    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final static int DEFAULT_CONNECTION_TIMEOUT = 30_000; // 0,5 minutes

    private JSch secureChannel = null;
    private Session sshSession = null;
    private ChannelSftp channelSftp = null;
    private ChannelExec channelExec = null;
    // proxy
    private SOSOptionProxyProtocol proxyProtocol = null;

    private Integer exitCode;
    private String exitSignal;
    private StringBuilder stdOut;
    private StringBuilder stdErr;

    // private boolean isWindowsShell = false;
    // private boolean isShellChecked = false;
    private SOSShellInfo shellInfo = null;
    private boolean simulateShell = false;

    private int proxyPort = 0;
    private int sessionConnectTimeout = 0;
    private int channelConnectTimeout = 0; // default 20sek
    private String proxyHost = null;
    private String proxyUser = null;
    private String proxyPassword = null;
    private final String lineSeparator = System.getProperty("line.separator");

    public SOSVfsSFtpJCraft() {
        super();
        JSCH111BugFix.init();
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
        connect(connection2OptionsAlternate);
        return this;
    }

    @Override
    public ISOSConnection connect(final SOSConnection2OptionsAlternate options) {
        connection2OptionsAlternate = options;
        if (connection2OptionsAlternate == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("connection2OptionsAlternate"));
        }
        setStrictHostKeyChecking(connection2OptionsAlternate.strictHostKeyChecking.getValue());
        doConnect(connection2OptionsAlternate.host.getValue(), connection2OptionsAlternate.port.value());
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
            doAuthenticate(authenticationOptions);
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
            if (isDebugEnabled) {
                LOGGER.debug(SOSVfs_D_132.params(userName));
            }
            createSession(userName, host, port);

            sshSession.setPassword(password);

            setConfigFromFiles();
            sshSession.connect();
            createSftpClient();

            reply = "OK";
            LOGGER.info(SOSVfs_D_133.params(userName));
            logReply();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("authentication"), e);
        }
    }

    @Override
    public void disconnect() {
        reply = "disconnect OK";
        if (channelSftp != null) {
            try {
                if (channelSftp.isConnected()) {
                    channelSftp.disconnect();
                    LOGGER.debug("[sftp]disconnected");
                } else {
                    LOGGER.debug("[sftp]not connected");
                }
                channelSftp = null;
            } catch (Exception ex) {
                reply = "[sftp][disconnect]" + ex.toString();
            }
        }

        if (channelExec != null) {
            try {
                if (channelExec.isConnected()) {
                    channelExec.disconnect();
                    LOGGER.debug("[exec]disconnected");
                } else {
                    LOGGER.debug("[exec]not connected");
                }
                channelExec = null;
            } catch (Exception ex) {
                reply = "[exec][disconnect]" + ex.toString();
            }
        }

        if (sshSession != null) {
            try {
                if (sshSession.isConnected()) {
                    sshSession.disconnect();
                    LOGGER.debug("[session]disconnected");
                } else {
                    LOGGER.debug("[session]not connected");
                }
                sshSession = null;
            } catch (Exception ex) {
                reply = "[session][disconnect]" + ex.toString();
            }
        }
        LOGGER.info(reply);
    }

    @Override
    public void logout() {
        try {

            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
                if (isDebugEnabled) {
                    LOGGER.debug(SOSVfs_D_138.params(host, getReplyString()));
                }
            } else {
                LOGGER.info("not connected, logout useless.");
            }
        } catch (Exception e) {
            LOGGER.warn(SOSVfs_W_140.get() + e.toString(), e);
        }
    }

    @Override
    public boolean isConnected() {
        if (channelSftp != null) {
            return channelSftp.isConnected();
        }
        return sshSession != null && sshSession.isConnected();
    }

    @Override
    public void mkdir(final String path) {
        try {
            String p = path.replaceAll("//+", "/").replaceFirst("/$", "");
            SOSOptionFolderName folderName = new SOSOptionFolderName(path);
            reply = "mkdir OK";
            if (isDebugEnabled) {
                LOGGER.debug(getHostID(SOSVfs_D_179.params("mkdir", p)));
            }
            String[] subfolders = folderName.getSubFolderArrayReverse();
            int idx = subfolders.length;
            for (String subFolder : folderName.getSubFolderArrayReverse()) {
                SftpATTRS attributes = getAttributes(subFolder);
                if (attributes != null && attributes.isDir()) {
                    if (isDebugEnabled) {
                        LOGGER.debug(SOSVfs_E_180.params(subFolder));
                    }
                    break;
                }
                if (attributes != null && !attributes.isDir()) {
                    throw new JobSchedulerException(SOSVfs_E_277.params(subFolder));
                }
                idx--;
            }
            subfolders = folderName.getSubFolderArray();
            for (int i = idx; i < subfolders.length; i++) {
                channelSftp.mkdir(subfolders[i]);
                if (isDebugEnabled) {
                    LOGGER.debug(getHostID(SOSVfs_E_0106.params("mkdir", subfolders[i], getReplyString())));
                }
            }
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("[%s] mkdir failed", path), e);
        }
    }

    @Override
    public void rmdir(final String path) {
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(path);
            reply = "rmdir OK";
            for (String subfolder : objF.getSubFolderArrayReverse()) {
                if (isDebugEnabled) {
                    LOGGER.debug(getHostID(SOSVfs_D_179.params("rmdir", subfolder)));
                }
                channelSftp.rmdir(subfolder);
                reply = "rmdir OK";
                if (isDebugEnabled) {
                    LOGGER.debug(getHostID(SOSVfs_D_181.params("rmdir", subfolder, getReplyString())));
                }
            }
            LOGGER.info(getHostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_134.params("[rmdir]"), e);
        }
    }

    @Override
    protected boolean fileExists(final String filename) {
        boolean result = false;
        SftpATTRS attributes = getAttributes(filename);
        if (attributes != null) {
            result = !attributes.isLink() || attributes.isDir();
        } else {
            result = false;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]fileExists=%s", filename, result));
        }
        return result;
    }

    @Override
    public boolean isDirectory(final String filename) {
        boolean result = false;
        SftpATTRS attributes = getAttributes(filename);
        if (attributes != null) {
            result = attributes.isDir();
            if (!result) {
                result = attributes.isLink();
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]isDirectory=%s", filename, result));
        }
        return result;
    }

    @Override
    public long size(String filename) throws Exception {
        filename = normalizePath(filename);
        long size = -1;
        SftpATTRS attributes = getAttributes(filename);
        if (attributes != null) {
            size = attributes.getSize();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]size=%s", filename, size));
        }
        return size;
    }

    public SftpATTRS getAttributes(final String filename) {
        SftpATTRS attributes = null;
        try {
            attributes = channelSftp.stat(filename);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof NullPointerException) {
                if (!channelSftp.isConnected()) {
                    throw new JobSchedulerException(String.format("[%s]not connected", filename));
                }
            }
        }
        return attributes;
    }

    @Override
    public SOSFileEntry getFileEntry(String pathname) throws Exception {
        Path path = Paths.get(pathname);
        SftpATTRS attrs = getAttributes(path.toString());
        if (attrs != null && !attrs.isDir()) {
            Path parent = Paths.get(pathname).getParent();
            return getFileEntry(attrs, path.getFileName().toString(), parent == null ? null : parent.toString());
        }
        return null;
    }

    private SOSFileEntry getFileEntry(SftpATTRS attrs, String fileName, String parentPath) {
        SOSFileEntry entry = new SOSFileEntry(EntryType.FILESYSTEM);
        entry.setDirectory(attrs.isDir());
        entry.setFilename(fileName);
        entry.setFilesize(attrs.getSize());
        entry.setLastModified(attrs.getMTime());
        entry.setParentPath(parentPath);
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        path = normalizePath(path);
        try {
            List<SOSFileEntry> result = new ArrayList<>();
            if (path.isEmpty()) {
                path = ".";
            }
            if (checkIfExists && !fileExists(path)) {
                return result;
            }
            if (checkIfIsDirectory && !isDirectory(path)) {
                reply = "ls OK";
                return result;
            }

            final Vector<LsEntry> list = new Vector<LsEntry>();
            LsEntrySelector selector = new LsEntrySelector() {

                public int select(LsEntry entry) {
                    final String filename = entry.getFilename();
                    if (filename.equals(".") || filename.equals("..")) {
                        return CONTINUE;
                    } else {
                        list.addElement(entry);
                    }
                    return CONTINUE;
                }
            };
            channelSftp.ls(path, selector);
            int size = list.size();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][ls] %s files or folders", path, size));
            }

            for (int i = 0; i < size; i++) {
                LsEntry file = list.get(i);
                result.add(getFileEntry(file.getAttrs(), file.getFilename(), path));
            }
            reply = "ls OK";
            return result;
        } catch (Exception e) {
            reply = e.toString();
            return null;
        }
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) {
        String sourceLocation = normalizePath(remoteFile);
        File transferFile = null;
        long remoteFileSize = -1;
        FileOutputStream fos = null;
        try {
            remoteFileSize = this.size(remoteFile);
            fos = new FileOutputStream(localFile, append);
            channelSftp.get(sourceLocation, fos);
            fos.flush();
            fos.close();
            fos = null;
            transferFile = new File(localFile);
            if (!append && remoteFileSize > 0 && remoteFileSize != transferFile.length()) {
                throw new JobSchedulerException(SOSVfs_E_162.params(remoteFileSize, transferFile.length()));
            }
            remoteFileSize = transferFile.length();
            reply = "get OK";
            LOGGER.info(getHostID(SOSVfs_I_182.params("getFile", sourceLocation, localFile, getReplyString())));
        } catch (Exception ex) {
            reply = ex.toString();
            throw new JobSchedulerException(SOSVfs_E_184.params("getFile", sourceLocation, localFile), ex);
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
        try {
            channelSftp.put(localFile, normalizePath(remoteFile), ChannelSftp.OVERWRITE);
            reply = "put OK";
            LOGGER.info(getHostID(SOSVfs_I_183.params("putFile", localFile, remoteFile, getReplyString())));
            return this.size(remoteFile);
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_185.params("putFile()", localFile, remoteFile), e);
        }
    }

    @Override
    public void delete(final String path, boolean checkIsDirectory) {
        try {
            if (checkIsDirectory && this.isDirectory(path)) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }
            channelSftp.rm(path);
        } catch (Exception ex) {
            reply = ex.toString();
            throw new JobSchedulerException(SOSVfs_E_187.params("delete", path), ex);
        }
        reply = "rm OK";
        LOGGER.info(getHostID(SOSVfs_D_181.params("delete", path, getReplyString())));
    }

    @Override
    public void rename(String from, String to) {
        from = normalizePath(from);
        to = normalizePath(to);
        try {
            channelSftp.rename(from, to);
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
    public void executeCommand(String cmd, SOSVfsEnv env) {
        getShellInfo();

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
            StringBuilder envs = new StringBuilder();
            if (env != null) {
                if (env.getGlobalEnvs() != null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[set global envs]%s", env.getGlobalEnvs()));
                    }
                    env.getGlobalEnvs().forEach((k, v) -> {
                        channelExec.setEnv(k, v);
                    });
                }
                if (env.getLocalEnvs() != null) {
                    env.getLocalEnvs().forEach((k, v) -> {
                        if (shellInfo.getShell().equals(Shell.WINDOWS)) {
                            envs.append(String.format("set %s=%s&", k, v));
                        } else {
                            envs.append(String.format("export \"%s=%s\";", k, v));
                        }
                    });
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[set local envs]%s", envs));
                    }
                }
            }
            cmd = cmd.replaceAll("\0", "\\\\\\\\").replaceAll("\"", "\\\"");
            if (envs.length() > 0) {
                channelExec.setCommand(envs.toString() + cmd);
            } else {
                channelExec.setCommand(cmd);
            }
            channelExec.setInputStream(null);
            channelExec.setErrStream(null);
            out = channelExec.getInputStream();
            err = channelExec.getErrStream();
            channelExec.connect();
            stdOut = new StringBuilder();
            byte[] tmp = new byte[1024];
            while (true) {
                while (out.available() > 0) {
                    int i = out.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    stdOut.append(new String(tmp, 0, i));
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
            boolean isErrorExitCode = exitCode != null && !exitCode.equals(new Integer(0));
            if (!isErrorExitCode && stdOut.length() > 0) {
                LOGGER.info(String.format("[%s][stdout]%s", cmd, stdOut.toString().trim()));
            }
            errReader = new BufferedReader(new InputStreamReader(err));
            stdErr = new StringBuilder();
            while (true) {
                String line = errReader.readLine();
                if (line == null) {
                    break;
                }
                stdErr.append(line + lineSeparator);
            }
            if (isErrorExitCode) {
                StringBuffer msg = new StringBuffer("[" + cmd + "]");
                if (stdOut.length() > 0) {
                    msg.append("[stdout=" + stdOut.toString().trim() + "]");
                }
                if (stdErr.length() > 0) {
                    msg.append("[stderr=" + stdErr.toString().trim() + "]");
                }
                msg.append("remote command terminated with the exit code " + exitCode.toString());
                throw new JobSchedulerException(msg.toString());
            } else {
                if (stdErr.length() > 0) {
                    LOGGER.info(String.format("[%s][stderr]%s", cmd, stdErr.toString().trim()));
                }
            }
            reply = "OK";
        } catch (JobSchedulerException ex) {
            reply = ex.toString();
            if (connection2OptionsAlternate.raiseExceptionOnError.value()) {
                throw ex;
            }
            LOGGER.info(String.format("[%s]%s", cmd, reply));
        } catch (Exception ex) {
            reply = ex.toString();
            if (connection2OptionsAlternate.raiseExceptionOnError.value()) {
                throw new JobSchedulerException(SOSVfs_E_134.params("ExecuteCommand"), ex);
            }
            LOGGER.info(String.format("[%s]%s", cmd, reply));
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
        }
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        try {
            return channelSftp.get(fileName);
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getInputStream()", fileName), ex);
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
            return channelSftp.put(fileName, transferMode);
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getOutputStream()", fileName), ex);
        }
    }

    @Override
    public boolean changeWorkingDirectory(String pathname) {
        try {
            pathname = normalizePath(pathname);
            if (!fileExists(pathname)) {
                reply = String.format("Filepath '%1$s' does not exist.", pathname);
                return false;
            }
            if (!isDirectory(pathname)) {
                reply = String.format("Filepath '%1$s' is not a directory.", pathname);
                return false;
            }
            channelSftp.cd(pathname);
            reply = "cwd OK";
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("cwd", pathname), ex);
        } finally {
            if (isDebugEnabled) {
                LOGGER.debug(SOSVfs_D_194.params(pathname, getReplyString()));
            }
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
            SftpATTRS objAttr = channelSftp.stat(path);
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
    protected String getCurrentPath() {
        String path = null;
        try {
            path = channelSftp.pwd();
            if (isDebugEnabled) {
                LOGGER.debug(getHostID(SOSVfs_D_195.params(path)));
            }
            logReply();
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_134.params("getCurrentPath"), e);
        }
        return path;
    }

    private void usePublicKeyMethod() throws Exception {
        String method = "usePublicKeyMethod";
        Object kd = connection2OptionsAlternate.keepass_database.value();
        Object ke = connection2OptionsAlternate.keepass_database_entry.value();
        if (connection2OptionsAlternate.useKeyAgent.isTrue()) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s]isUseKeyAgent", method));
            }

            Connector con = null;
            try {
                ConnectorFactory cf = ConnectorFactory.getDefault();
                con = cf.createConnector();
            } catch (AgentProxyException e) {
                LOGGER.error(e.getMessage(), e);
            }

            if (con != null) {
                IdentityRepository irepo = new RemoteIdentityRepository(con);
                secureChannel.setIdentityRepository(irepo);
            }

        } else {

            if (kd == null || ke == null) {
                SOSOptionInFileName authenticationFile = authenticationOptions.getAuthFile();
                authenticationFile.checkMandatory(true);
                if (authenticationFile.isNotEmpty()) {
                    try {
                        if (authenticationOptions.getPassphrase().isNotEmpty()) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s]file=%s, passphrase=?", method, authenticationOptions.getAuthFile().getValue()));
                            }
                            secureChannel.addIdentity(authenticationFile.getJSFile().getPath(), authenticationOptions.getPassphrase().getValue());
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s]file=%s", method, authenticationOptions.getAuthFile().getValue()));
                            }
                            secureChannel.addIdentity(authenticationFile.getJSFile().getPath());
                        }
                    } catch (JSchException e) {
                        throw new Exception(String.format("[%s][%s]%s", method, authenticationOptions.getAuthFile().getValue(), e.toString()), e);
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s]authFile is empty", method));
                    }
                }
            } else {
                SOSKeePassDatabase kpd = (SOSKeePassDatabase) kd;
                org.linguafranca.pwdb.Entry<?, ?, ?, ?> entry = (org.linguafranca.pwdb.Entry<?, ?, ?, ?>) ke;
                try {
                    byte[] pr = kpd.getAttachment(entry, connection2OptionsAlternate.keepass_attachment_property_name.getValue());
                    String keePassPath = entry.getPath() + SOSKeePassPath.PROPERTY_PREFIX
                            + connection2OptionsAlternate.keepass_attachment_property_name.getValue();
                    if (authenticationOptions.getPassphrase().isNotEmpty()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][keepass]attachment=%s, passphrase=?", method, keePassPath));
                        }
                        secureChannel.addIdentity(SOSVfsSFtpJCraft.class.getSimpleName(), pr, (byte[]) null, authenticationOptions.getPassphrase()
                                .getValue().getBytes());
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][keepass]attachment=%s", method, keePassPath));
                        }
                        secureChannel.addIdentity(SOSVfsSFtpJCraft.class.getSimpleName(), pr, (byte[]) null, (byte[]) null);
                    }
                } catch (Exception e) {
                    throw new Exception(String.format("[%s][keepass]%s", method, e.toString()), e);
                }
            }
        }
    }

    private void usePasswordMethod() throws Exception {
        LOGGER.debug("[password]");
        sshSession.setPassword(authenticationOptions.getPassword().getValue());
    }

    private void useKeyboardInteractive() throws Exception {
        if (isDebugEnabled) {
            LOGGER.debug("useKeyboardInteractive");
        }
        Object ui = connection2OptionsAlternate.user_info.value();
        if (ui == null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("use default %s implementation", SOSVfsSFtpJCraftUserInfo.class.getSimpleName()));
            }
            ui = new SOSVfsSFtpJCraftUserInfo();
        }
        sshSession.setUserInfo((UserInfo) ui);
    }

    private String usePreferredAuthentications(final String debugKey, final String preferredAuthentications) throws Exception {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[usePreferredAuthentications][%s]preferredAuthentications=%s", debugKey, preferredAuthentications));
        }
        try {
            usePublicKeyMethod();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        if (authenticationOptions.getAuthMethod().isKeyboardInteractive()) {
            useKeyboardInteractive();
        } else {
            usePasswordMethod();
        }
        return preferredAuthentications;
    }

    private String useRequiredAuthentications(final String requiredAuthentications) throws Exception {
        String preferredAuthentications = requiredAuthentications;
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[useRequiredAuthentications]preferredAuthentications=%s", preferredAuthentications));
        }
        sessionConnectTimeout = DEFAULT_CONNECTION_TIMEOUT;

        sshSession.setConfig("userauth.publickey", SOSRequiredAuthPublicKey.class.getName());

        usePublicKeyMethod();
        if (authenticationOptions.getAuthMethod().isKeyboardInteractive()) {
            sshSession.setConfig("userauth.keyboard-interactive", SOSRequiredAuthKeyboardInteractive.class.getName());
            useKeyboardInteractive();
        } else {
            sshSession.setConfig("userauth.password", SOSRequiredAuthPassword.class.getName());
            usePasswordMethod();
        }

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
                if (authenticationOptions.getAuthMethod().isPublicKey()) {
                    usePublicKeyMethod();
                } else if (authenticationOptions.getAuthMethod().isPassword()) {
                    usePasswordMethod();
                } else if (authenticationOptions.getAuthMethod().isKeyboardInteractive()) {
                    useKeyboardInteractive();
                }
            }
        }
        try {
            sshSession.setConfig("PreferredAuthentications", preferredAuthentications);
            setServerAlive();// server alive sets session timeout
            setSessionConnectTimeout();
            setChannelConnectTimeout();
            setConfigFromFiles();

            StringBuilder msg = new StringBuilder("[").append(connection2OptionsAlternate.protocol.getValue()).append("]").append(
                    "SessionConnectTimeout=").append(ms2string(sshSession.getTimeout()));
            if (sshSession.getServerAliveInterval() > 0) {
                msg.append(", ServerAliveInterval=").append(ms2string(sshSession.getServerAliveInterval()));
                msg.append(", ServerAliveCountMax=%s").append(sshSession.getServerAliveCountMax());
            }
            if (channelConnectTimeout > 0) {
                msg.append(", ChannelConnectTimeout=").append(ms2string(channelConnectTimeout));
            }
            LOGGER.info(msg.toString());

            sshSession.connect();
            if (connection2OptionsAlternate.protocol.getValue().equals(TransferTypes.sftp.name())) {
                createSftpClient();
            }
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(e.getClass().getName() + " - " + e.getMessage()), e);
        }
        reply = "OK";
        LOGGER.info(SOSVfs_D_133.params(userName));
        this.logReply();
        return this;
    }

    private String ms2string(int val) {
        if (val <= 0) {
            return String.valueOf(val).concat("ms");
        }
        try {
            return String.valueOf(Math.round(val / 1000)).concat("s");
        } catch (Throwable e) {
            return String.valueOf(val).concat("ms");
        }
    }

    private void setServerAlive() {
        String sai = connection2OptionsAlternate.server_alive_interval.getValue();
        if (!SOSString.isEmpty(sai)) {
            try {
                sshSession.setServerAliveInterval(SOSDate.resolveAge("ms", sai).intValue());
                String sacm = connection2OptionsAlternate.server_alive_count_max.getValue();
                if (!SOSString.isEmpty(sacm)) {
                    sshSession.setServerAliveCountMax(Integer.parseInt(sacm));
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("[setServerAlive]%s", ex.toString()), ex);
            }
        }
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
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("set configuration setting: %s = %s", key, value));
                        }
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

    private void setSessionConnectTimeout() throws Exception {
        String ct = connection2OptionsAlternate.session_connect_timeout.getValue();
        if (!SOSString.isEmpty(ct)) {
            sessionConnectTimeout = SOSDate.resolveAge("ms", ct).intValue();
        }
        if (sessionConnectTimeout > 0) {// can be set by proxy
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("session connect timeout = %s", (ct == null ? sessionConnectTimeout : ct)));
                }
                sshSession.setTimeout(sessionConnectTimeout);
            } catch (Exception ex) {
                LOGGER.warn(String.format("[setSessionConnectTimeout]%s", ex.toString()), ex);
            }
        }
    }

    private void setChannelConnectTimeout() throws Exception {
        String ct = connection2OptionsAlternate.channel_connect_timeout.getValue();
        if (!SOSString.isEmpty(ct)) {
            channelConnectTimeout = SOSDate.resolveAge("ms", ct).intValue();
        }
    }

    private void setKnownHostsFile() throws JSchException {
        if (secureChannel != null && connection2OptionsAlternate.strictHostKeyChecking.isTrue()) {
            File knownHostsFile = new File(System.getProperty("user.home"), ".ssh/known_hosts");
            secureChannel.setKnownHosts(knownHostsFile.getAbsolutePath());
        }
    }

    private void doConnect(final String phost, final int pport) {
        host = phost;
        port = pport;
        LOGGER.info(SOSVfs_D_0101.params(host, port));
        if (!this.isConnected()) {
            this.logReply();
        } else {
            LOGGER.warn(SOSVfs_D_0103.params(host, port));
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

    private void setCommandsTimeout() throws Exception {
        //
    }

    private void setProxy() throws Exception {
        if (!SOSString.isEmpty(this.proxyHost)) {
            LOGGER.info(String.format("using proxy: protocol=%s, host=%s, port=%s, user=%s, pass=?", proxyProtocol.getValue(), proxyHost, proxyPort,
                    proxyUser));
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
                sessionConnectTimeout = DEFAULT_CONNECTION_TIMEOUT;
            } else if (proxyProtocol.isSocks4()) {
                ProxySOCKS4 proxy = new ProxySOCKS4(proxyHost, proxyPort);
                if (!SOSString.isEmpty(proxyUser)) {
                    proxy.setUserPasswd(proxyUser, proxyPassword);
                }
                sshSession.setProxy(proxy);
                sessionConnectTimeout = DEFAULT_CONNECTION_TIMEOUT;
            } else {
                throw new Exception(String.format("unknown proxy protocol = %s", proxyProtocol.getValue()));
            }
        }
    }

    private void createSftpClient() throws Exception {
        if (sshSession == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("sshSession"));
        }
        channelSftp = (ChannelSftp) sshSession.openChannel("sftp");
        sshSession.setConfig("compression_level", connection2OptionsAlternate.zlibCompressionLevel.getValue());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("createSftpClient connect timeout = %s", channelConnectTimeout));
        }
        if (channelConnectTimeout > 0) {
            channelSftp.connect(channelConnectTimeout);
        } else {
            channelSftp.connect();
        }
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

    public StringBuilder getStdErr() {
        return stdErr;
    }

    public void resetStdErr() {
        stdErr = new StringBuilder();
    }

    public StringBuilder getStdOut() {
        return stdOut;
    }

    public void resetStdOut() {
        stdOut = new StringBuilder();
    }

    @Override
    public Integer getExitCode() {
        return exitCode;
    }

    public String getExitSignal() {
        return exitSignal;
    }

    public String createScriptFile(String content) throws Exception {
        getShellInfo();

        try {
            String commandScript = content;
            if (!shellInfo.getShell().equals(Shell.WINDOWS)) {
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
            if (!shellInfo.getShell().equals(Shell.WINDOWS)) {
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
        getShellInfo();
        if (shellInfo.getShell().equals(Shell.WINDOWS)) {
            return ".cmd";
        } else {
            return ".sh";
        }
    }

    private void putFile(File commandFile, Integer chmod) throws Exception {
        String name = commandFile.getName();
        channelSftp.put(commandFile.getCanonicalPath(), name);
        if (chmod != null) {
            channelSftp.chmod(chmod, name);
        }
    }

    public SOSShellInfo getShellInfo() {
        if (shellInfo == null) {
            SOSShellInfo info = new SOSShellInfo("uname");
            try {
                info.setCommandResult(executeResultCommand(info.getCommand()));

                String stdOut = info.getCommandResult().getStdOut().toString().toLowerCase();
                switch (info.getCommandResult().getExitCode()) {
                case 0:
                    if (stdOut.contains("linux") || stdOut.contains("darwin") || stdOut.contains("aix") || stdOut.contains("hp-ux") || stdOut
                            .contains("solaris") || stdOut.contains("sunos") || stdOut.contains("freebsd")) {
                        info.setOS(info.getCommandResult().getStdOut().toString().trim());
                        info.setShell(Shell.UNIX);
                    } else if (stdOut.contains("cygwin")) {
                        // OS is Windows but shell is Unix like
                        // unix commands have to be used
                        info.setOS(OS.WINDOWS.name());
                        info.setShell(Shell.CYGWIN);
                    } else {
                        info.setOS(OS.UNKNOWN.name());
                        info.setShell(Shell.UNIX);
                    }
                    break;
                case 9009:
                case 1:
                    // call of uname under Windows OS delivers exit code 9009 or exit code 1 and target shell cmd.exe
                    // the exit code depends on the remote SSH implementation
                    info.setOS(OS.WINDOWS.name());
                    info.setShell(Shell.WINDOWS);
                    break;
                case 127:
                    // call of uname under Windows OS with CopSSH (cygwin) and target shell /bin/bash delivers exit code 127
                    // command uname is not installed by default through CopSSH installation
                    info.setOS(OS.WINDOWS.name());
                    info.setShell(Shell.CYGWIN);
                    break;
                default:
                    info.setOS(OS.UNKNOWN.name());
                    info.setShell(Shell.UNKNOWN);
                    break;
                }
            } catch (Throwable e) {
                info.setCommandError(e);
                LOGGER.error(String.format("[%s]%s", info.getCommand(), e.toString()), e);
            } finally {
                shellInfo = info;
            }

            if (isDebugEnabled) {
                LOGGER.debug(shellInfo.toString());
            }
        }
        return shellInfo;
    }

    public SOSCommandResult executeResultCommand(String cmd) throws Exception {
        ChannelExec channel = null;
        InputStream in = null;
        InputStream err = null;
        BufferedReader errReader = null;
        SOSCommandResult result = new SOSCommandResult(cmd);
        try {
            cmd = cmd.trim();
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
                    Thread.sleep(1_000);
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

            if (isDebugEnabled) {
                LOGGER.debug(result.toString());
            }
        }
        return result;
    }

    @Override
    public boolean isSimulateShell() {
        return simulateShell;
    }

    @Override
    public void setSimulateShell(boolean val) {
        simulateShell = val;
    }

    public ChannelExec getChannelExec() {
        return channelExec;
    }

    public ChannelSftp getChannelSftp() {
        return channelSftp;
    }

}
