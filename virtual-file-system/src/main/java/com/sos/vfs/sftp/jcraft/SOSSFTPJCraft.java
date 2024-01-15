package com.sos.vfs.sftp.jcraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.jurr.jsch.bugfix111.JSCH111BugFix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
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
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.exception.SOSMissingDataException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.keepass.SOSKeePassDatabase;
import com.sos.keepass.SOSKeePassPath;
import com.sos.vfs.common.SOSCommandResult;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.SOSFileEntryFile;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.sftp.SOSSFTP;
import com.sos.vfs.sftp.common.ISOSSFTP;
import com.sos.vfs.sftp.common.SOSSFTPUserInfo;
import com.sos.vfs.sftp.common.SOSSSHServerInfo;
import com.sos.vfs.sftp.common.SOSSSHServerInfo.Shell;
import com.sos.vfs.sftp.jcraft.common.SOSSFTPLogger;

import sos.util.SOSDate;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSFTPJCraft extends SOSCommonProvider implements ISOSSFTP {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSFTPJCraft.class);

    private final static int DEFAULT_CONNECTION_TIMEOUT = 30_000; // 0,5 minutes

    private JSch secureChannel = null;
    private Session sshSession = null;
    private ChannelSftp channelSftp = null;
    private ChannelExec channelExec = null;

    private Integer exitCode;
    private String exitSignal;
    private StringBuilder stdOut;
    private StringBuilder stdErr;

    private SOSSSHServerInfo serverInfo = null;
    private boolean simulateShell = false;

    private int sessionConnectTimeout = 0;
    private int channelConnectTimeout = 0; // default 20sek
    private final String lineSeparator = System.getProperty("line.separator");

    public SOSSFTPJCraft() {
        super();
        JSCH111BugFix.init();
        JSch.setLogger(new SOSSFTPLogger());
        secureChannel = new JSch();
    }

    @Override
    public boolean isConnected() {
        if (channelSftp != null) {
            return channelSftp.isConnected();
        }
        return sshSession != null && sshSession.isConnected();
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        super.connect(options);

        LOGGER.info(new StringBuilder("[").append(getProviderOptions().protocol.getValue()).append("]").append(SOSVfs_D_0101.params(
                getProviderOptions().host.getValue(), getProviderOptions().port.value())).toString());

        try {
            doConnect();
        } catch (JobSchedulerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
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
    public void mkdir(final String path) {
        try {
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            String p = path.replaceAll("//+", "/").replaceFirst("/$", "");
            SOSOptionFolderName folderName = new SOSOptionFolderName(path);
            reply = "mkdir OK";
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[mkdir][%s]try to create ...", p));
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
                    LOGGER.debug(String.format("[mkdir][%s]created", subfolders[i]));
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
            if (SOSString.isEmpty(path)) {
                throw new SOSMissingDataException("path");
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[rmdir][%s]try to remove ...", path));
            }
            final Deque<SOSFileEntry> toRemove = new LinkedList<>();
            dirInfo(path, toRemove, true);
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            while (!toRemove.isEmpty()) {
                SOSFileEntry resource = toRemove.pop();
                String resourcePath = resource.getFullPath();
                if (isDebugEnabled) {
                    LOGGER.debug(getHostID(SOSVfs_D_179.params("rmdir", resourcePath)));
                }
                if (resource.isDirectory()) {
                    channelSftp.rmdir(resourcePath);
                    if (isDebugEnabled) {
                        LOGGER.debug(getHostID(SOSVfs_D_181.params("rmdir", resourcePath, "rmdir OK")));
                    }
                } else {
                    channelSftp.rm(resourcePath);
                    if (isDebugEnabled) {
                        LOGGER.debug(getHostID(SOSVfs_D_181.params("rmdir", resourcePath, "rm OK")));
                    }
                }
            }
            channelSftp.rmdir(path);
            reply = "rmdir OK";
            LOGGER.info(getHostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("[%s]rmdir failed", path), e);
        }
    }

    private void dirInfo(String path, Deque<SOSFileEntry> result, boolean recursive) throws Exception {
        List<SOSFileEntry> infos = listNames(path, -1, false, false);
        for (SOSFileEntry resource : infos) {
            result.push(resource);
            if (recursive && resource.isDirectory()) {
                dirInfo(resource.getFullPath(), result, recursive);
            }
        }
    }

    @Override
    public boolean fileExists(final String filename) {
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
    public boolean directoryExists(final String filename) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]directoryExists", filename));
        }
        return isDirectory(filename);
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

    protected SftpATTRS getAttributes(final String filename) {
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
        SftpATTRS attrs = getAttributes(pathname);
        if (attrs != null && !attrs.isDir()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s]found", pathname));
            }

            SOSFileEntryFile f = new SOSFileEntryFile(pathname);
            return getFileEntry(attrs, f.getName(), f.getParent());
        }
        return null;
    }

    private SOSFileEntry getFileEntry(SftpATTRS attrs, String fileName, String parentPath) {
        SOSFileEntry entry = new SOSFileEntry(EntryType.FILESYSTEM);
        entry.setDirectory(attrs.isDir());
        entry.setFilename(fileName);
        entry.setFilesize(attrs.getSize());
        // entry.setLastModified(attrs.getMTime());
        entry.setParentPath(parentPath);
        if (parentPath != null && SOSSFTP.hasWindowsOpenSSHDriverLetterSpecifier(parentPath)) {
            entry.setFullPath("/" + entry.getFullPath());
        }
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(String path, int maxFiles, boolean checkIfExists, boolean checkIfIsDirectory) {
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

            final List<LsEntry> list = new ArrayList<LsEntry>();
            LsEntrySelector selector = new LsEntrySelector() {

                public int select(LsEntry entry) {
                    final String filename = entry.getFilename();
                    if (filename.equals(".") || filename.equals("..")) {
                        return CONTINUE;
                    } else {
                        list.add(entry);
                    }
                    return CONTINUE;
                }
            };
            channelSftp.ls(path, selector);
            int size = list.size();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s][ls] %s files or folders", path, size));
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
    public void executeCommand(String cmd, SOSEnv env) {
        channelExec = null;
        exitCode = null;
        InputStream out = null;
        InputStream err = null;
        BufferedReader errReader = null;
        try {
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            if (sshSession == null) {
                throw new JobSchedulerException(SOSVfs_E_190.params("sshSession"));
            }
            channelExec = (ChannelExec) sshSession.openChannel("exec");
            channelExec.setPty(isSimulateShell());
            StringBuilder localEnvs = new StringBuilder();
            if (env != null) {
                if (env.getGlobalEnvs() != null && env.getGlobalEnvs().size() > 0) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[set global envs]%s", env.getGlobalEnvs()));
                    }
                    env.getGlobalEnvs().forEach((k, v) -> {
                        channelExec.setEnv(k, v);
                    });
                }
                if (env.getLocalEnvs() != null && env.getLocalEnvs().size() > 0) {
                    getSSHServerInfo();

                    env.getLocalEnvs().forEach((k, v) -> {
                        if (serverInfo.getShell().equals(Shell.WINDOWS)) {
                            localEnvs.append(String.format("set %s=%s&", k, v));
                        } else {
                            localEnvs.append(String.format("export \"%s=%s\";", k, v));
                        }
                    });
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[set local envs]%s", localEnvs));
                    }
                }
            }
            cmd = cmd.trim().replaceAll("\0", "\\\\\\\\").replaceAll("\"", "\\\"");
            if (localEnvs.length() > 0) {
                cmd = localEnvs.toString() + cmd;
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[cmd]%s", cmd));
            }
            channelExec.setCommand(cmd);
            channelExec.setInputStream(null);
            channelExec.setErrStream(null);
            out = channelExec.getInputStream();
            err = channelExec.getErrStream();
            channelExec.connect(channelConnectTimeout);
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
                LOGGER.info(String.format("[%s][std:out]%s", cmd, stdOut.toString().trim()));
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
                    msg.append("[std:out=" + stdOut.toString().trim() + "]");
                }
                if (stdErr.length() > 0) {
                    msg.append("[std:err=" + stdErr.toString().trim() + "]");
                }
                msg.append("remote command terminated with the exit code " + exitCode.toString());
                throw new JobSchedulerException(msg.toString());
            } else {
                if (stdErr.length() > 0) {
                    LOGGER.info(String.format("[%s][std:err]%s", cmd, stdErr.toString().trim()));
                }
            }
            reply = "OK";
        } catch (JobSchedulerException ex) {
            reply = ex.toString();
            if (getProviderOptions().raiseExceptionOnError.value()) {
                throw ex;
            }
            LOGGER.info(String.format("[%s]%s", cmd, reply));
        } catch (Exception ex) {
            reply = ex.toString();
            if (getProviderOptions().raiseExceptionOnError.value()) {
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
    public boolean isExecSessionExists() {
        return channelExec != null;
    }

    @Override
    public boolean isExecSessionConnected() {
        return channelExec != null && channelExec.isConnected();
    }

    @Override
    public void execSessionSendSignalContinue() throws Exception {
        if (channelExec != null) {
            channelExec.sendSignal("CONT");
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
    public OutputStream getOutputStream(String fileName, boolean append, boolean resume) {
        try {
            int transferMode = ChannelSftp.OVERWRITE;
            if (append) {
                transferMode = ChannelSftp.APPEND;
            } else if (resume) {
                transferMode = ChannelSftp.RESUME;
            }
            return channelSftp.put(fileName, transferMode);
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getOutputStream()", fileName), ex);
        }
    }

    @Override
    public ISOSProviderFile getFile(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSProviderFile file = new SOSSFTPFileJCraft(fileName);
        file.setProvider(this);
        return file;
    }

    @Override
    public String getModificationDateTime(final String path) {
        String dateTime = null;
        try {
            SftpATTRS objAttr = channelSftp.stat(path);
            if (objAttr != null) {
                long mt = Long.valueOf(objAttr.getMTime()) * 1_000;
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateTime = df.format(new Date(mt));
            }
        } catch (SftpException e) {
            //
        }
        return dateTime;
    }

    private void usePublicKeyMethod() throws Exception {
        String method = "usePublicKeyMethod";
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        Object kd = getProviderOptions().keepass_database.value();
        Object ke = getProviderOptions().keepass_database_entry.value();
        if (getProviderOptions().useKeyAgent.isTrue()) {
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
                SOSOptionInFileName authenticationFile = getProviderOptions().authFile;
                authenticationFile.checkMandatory(true);
                if (authenticationFile.isNotEmpty()) {
                    try {
                        if (getProviderOptions().passphrase.isNotEmpty()) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s]file=%s, passphrase=?", method, getProviderOptions().authFile.getValue()));
                            }
                            secureChannel.addIdentity(authenticationFile.getJSFile().getPath(), getProviderOptions().passphrase.getValue());
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("[%s]file=%s", method, getProviderOptions().authFile.getValue()));
                            }
                            secureChannel.addIdentity(authenticationFile.getJSFile().getPath());
                        }
                    } catch (JSchException e) {
                        throw new Exception(String.format("[%s][%s]%s", method, getProviderOptions().authFile.getValue(), e.toString()), e);
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
                    byte[] pr = kpd.getAttachment(entry, getProviderOptions().keepass_attachment_property_name.getValue());
                    String keePassPath = entry.getPath() + SOSKeePassPath.PROPERTY_PREFIX + getProviderOptions().keepass_attachment_property_name
                            .getValue();
                    if (getProviderOptions().passphrase.isNotEmpty()) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][keepass]attachment=%s, passphrase=?", method, keePassPath));
                        }
                        secureChannel.addIdentity(SOSSFTPJCraft.class.getSimpleName(), pr, (byte[]) null, getProviderOptions().passphrase.getValue()
                                .getBytes());
                    } else {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[%s][keepass]attachment=%s", method, keePassPath));
                        }
                        secureChannel.addIdentity(SOSSFTPJCraft.class.getSimpleName(), pr, (byte[]) null, (byte[]) null);
                    }
                } catch (Exception e) {
                    throw new Exception(String.format("[%s][keepass]%s", method, e.toString()), e);
                }
            }
        }
    }

    private void usePasswordMethod() throws Exception {
        LOGGER.debug("[password]");
        sshSession.setPassword(getProviderOptions().password.getValue());
    }

    private void useKeyboardInteractive() throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug("useKeyboardInteractive");
        }
        Object ui = getProviderOptions().user_info.value();
        if (ui == null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("use default %s implementation", SOSSFTPUserInfo.class.getSimpleName()));
            }
            ui = new SOSSFTPUserInfo();
        }
        sshSession.setUserInfo((UserInfo) ui);
    }

    private String usePreferredAuthentications(final String debugKey, final String preferredAuthentications) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[usePreferredAuthentications][%s]preferredAuthentications=%s", debugKey, preferredAuthentications));
        }
        try {
            usePublicKeyMethod();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        if (getProviderOptions().authMethod.isKeyboardInteractive()) {
            useKeyboardInteractive();
        } else {
            usePasswordMethod();
        }
        return preferredAuthentications;
    }

    private String useRequiredAuthentications(final String requiredAuthentications) throws Exception {
        String preferredAuthentications = requiredAuthentications;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[useRequiredAuthentications]preferredAuthentications=%s", preferredAuthentications));
        }
        sessionConnectTimeout = DEFAULT_CONNECTION_TIMEOUT;

        sshSession.setConfig("userauth.publickey", SOSRequiredAuthPublicKey.class.getName());

        usePublicKeyMethod();
        if (getProviderOptions().authMethod.isKeyboardInteractive()) {
            sshSession.setConfig("userauth.keyboard-interactive", SOSRequiredAuthKeyboardInteractive.class.getName());
            useKeyboardInteractive();
        } else {
            sshSession.setConfig("userauth.password", SOSRequiredAuthPassword.class.getName());
            usePasswordMethod();
        }

        return preferredAuthentications;
    }

    private void doConnect() throws Exception {
        setKnownHostsFile();
        createSession();

        String preferredAuthentications = null;
        if (getProviderOptions().preferred_authentications.isNotEmpty()) {
            preferredAuthentications = usePreferredAuthentications("preferred_authentications", getProviderOptions().preferred_authentications
                    .getValue());
        } else if (getProviderOptions().required_authentications.isNotEmpty()) {
            preferredAuthentications = useRequiredAuthentications(getProviderOptions().required_authentications.getValue());
        } else {
            if (getProviderOptions().password.isNotEmpty() && getProviderOptions().authFile.isNotEmpty()) {
                preferredAuthentications = usePreferredAuthentications("password,publickey", "password,publickey");
            } else {
                preferredAuthentications = getProviderOptions().authMethod.getValue();
                if (getProviderOptions().authMethod.isPublicKey()) {
                    usePublicKeyMethod();
                } else if (getProviderOptions().authMethod.isPassword()) {
                    usePasswordMethod();
                } else if (getProviderOptions().authMethod.isKeyboardInteractive()) {
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

            printConnectionInfos();

            sshSession.connect();
            if (getProviderOptions().protocol.getValue().equals(TransferTypes.sftp.name())) {
                createSftpClient();
            }
        } catch (Exception e) {
            throw new JobSchedulerException(getHostID(e.getClass().getName() + " - " + e.toString()), e);
        }
        reply = "OK";
        LOGGER.info(SOSVfs_D_133.params(getProviderOptions().user.getValue()));
        this.logReply();
    }

    private void printConnectionInfos() {
        List<String> msg = new ArrayList<String>();
        if (sshSession.getTimeout() > 0) {
            msg.add("SessionConnectTimeout=" + ms2string(sshSession.getTimeout()));
        }
        if (sshSession.getServerAliveInterval() > 0) {
            msg.add("ServerAliveInterval=" + ms2string(sshSession.getServerAliveInterval()) + ", ServerAliveCountMax=" + sshSession
                    .getServerAliveCountMax());
        }
        if (channelConnectTimeout > 0) {
            msg.add("ChannelConnectTimeout=" + ms2string(channelConnectTimeout));
        }
        if (msg.size() > 0) {
            LOGGER.info(Joiner.on(", ").join(msg));
        }
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
        String sai = getProviderOptions().server_alive_interval.getValue();
        if (!SOSString.isEmpty(sai)) {
            try {
                sshSession.setServerAliveInterval(SOSDate.resolveAge("ms", sai).intValue());
                String sacm = getProviderOptions().server_alive_count_max.getValue();
                if (!SOSString.isEmpty(sacm)) {
                    sshSession.setServerAliveCountMax(Integer.parseInt(sacm));
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("[setServerAlive]%s", ex.toString()), ex);
            }
        }
    }

    private void setConfigFromFiles() {
        if (!SOSString.isEmpty(getProviderOptions().configuration_files.getValue())) {
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            String[] arr = getProviderOptions().configuration_files.getValue().split(";");
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
        String ct = getProviderOptions().connect_timeout.getValue();
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
        String ct = getProviderOptions().channel_connect_timeout.getValue();
        if (!SOSString.isEmpty(ct)) {
            channelConnectTimeout = SOSDate.resolveAge("ms", ct).intValue();
        }
    }

    private void setKnownHostsFile() throws JSchException {
        if (secureChannel != null && getProviderOptions().strictHostKeyChecking.isTrue()) {
            File knownHostsFile = new File(System.getProperty("user.home"), ".ssh/known_hosts");
            secureChannel.setKnownHosts(knownHostsFile.getAbsolutePath());
        }
    }

    private void createSession() throws Exception {
        if (secureChannel == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("secureChannel"));
        }
        LOGGER.debug(String.format("user=%s, host=%s, port=%s", user, host, port));
        sshSession = secureChannel.getSession(user, host, port);

        java.util.Properties config = new java.util.Properties();
        // JSch.setConfig("StrictHostKeyChecking", getProviderOptions().strictHostKeyChecking.getValue());
        config.put("StrictHostKeyChecking", getProviderOptions().strictHostKeyChecking.getValue());
        if (getProviderOptions().useZlibCompression.value()) {
            config.put("compression.s2c", "zlib@openssh.com,zlib,none");
            config.put("compression.c2s", "zlib@openssh.com,zlib,none");
            config.put("compression_level", getProviderOptions().zlibCompressionLevel.getValue());
            LOGGER.info(String.format("use zlib_compression: compression.s2c = %s, compression.c2s = %s, compression_level = %s", config.getProperty(
                    "compression.s2c"), config.getProperty("compression.c2s"), getProviderOptions().zlibCompressionLevel.getValue()));
        }
        sshSession.setConfig(config);
        setCommandsTimeout();
        setProxy();
    }

    private void setCommandsTimeout() throws Exception {
        //
    }

    private void setProxy() throws Exception {
        SOSOptionProxyProtocol proxyProtocol = getProviderOptions().proxyProtocol;
        String proxyHost = getProviderOptions().proxyHost.getValue();
        String proxyUser = getProviderOptions().proxyUser.getValue();
        String proxyPassword = getProviderOptions().proxyPassword.getValue();
        int proxyPort = getProviderOptions().proxyPort.value();

        if (!SOSString.isEmpty(proxyHost)) {
            LOGGER.info(String.format("using proxy: protocol=%s, host=%s, port=%d, user=%s, pass=?", proxyProtocol.getValue(), proxyHost, proxyPort,
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
        sshSession.setConfig("compression_level", getProviderOptions().zlibCompressionLevel.getValue());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("createSftpClient connect timeout = %s", channelConnectTimeout));
        }
        if (channelConnectTimeout > 0) {
            channelSftp.connect(channelConnectTimeout);
        } else {
            channelSftp.connect();
        }
    }

    public String getStdErr() {
        return stdErr.toString();
    }

    public void resetStdErr() {
        stdErr = null;
    }

    public String getStdOut() {
        return stdOut.toString();
    }

    public void resetStdOut() {
        stdOut = null;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public String getExitSignal() {
        return exitSignal;
    }

    public long putFile(final String source, final String target) {
        try {
            Instant start = Instant.now();
            channelSftp.put(source, normalizePath(target), ChannelSftp.OVERWRITE);
            Instant end = Instant.now();

            reply = new StringBuilder("put OK (").append(SOSDate.getDuration(start, end)).append(")").toString();
            LOGGER.info(getHostID(SOSVfs_I_183.params("putFile", source, target, getReplyString())));

            long size = size(target);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[put][%s]size=%s", target, size));
            }
            return size;
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_185.params("putFile()", source, target), e);
        }
    }

    public void putFile(File source, String target, int chmod) throws Exception {
        putFile(source.getCanonicalPath(), target);
        channelSftp.chmod(chmod, target);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[put][%s]chmod=%s", target, chmod));
        }
    }

    public void get(final String source, final String target) {
        try {
            Instant start = Instant.now();
            channelSftp.get(source, normalizePath(target));
            Instant end = Instant.now();

            reply = new StringBuilder("get OK (").append(SOSDate.getDuration(start, end)).append(")").toString();
            LOGGER.info(getHostID(SOSVfs_I_183.params("get", source, target, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_185.params("get()", source, target), e);
        }
    }

    @Override
    public SOSSSHServerInfo getSSHServerInfo() {
        if (serverInfo == null) {
            serverInfo = new SOSSSHServerInfo(null, executeResultCommand("uname"));
        }
        return serverInfo;
    }

    public SOSCommandResult executeResultCommand(String cmd) {
        ChannelExec channel = null;
        InputStream in = null;
        InputStream err = null;
        BufferedReader errReader = null;
        SOSCommandResult result = new SOSCommandResult(cmd);
        try {
            cmd = cmd.trim();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[cmd]%s", cmd));
            }
            channel = (ChannelExec) sshSession.openChannel("exec");
            channel.setPty(isSimulateShell());
            channel.setCommand(cmd);
            channel.setInputStream(null);
            channel.setErrStream(null);
            in = channel.getInputStream();
            err = channel.getErrStream();
            channel.connect(channelConnectTimeout);
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    result.addStdOut(new String(tmp, 0, i));
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
                result.addStdErr(line + lineSeparator);
            }

        } catch (Throwable e) {
            result.setException(e);
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
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            if (channel != null) {
                try {
                    channel.disconnect();
                } catch (Throwable ex) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[executeResultCommand][disconnect]%s", ex.toString()), ex);
                    }
                }
            }

            if (isDebugEnabled) {
                LOGGER.debug(result.toString());
            }
        }
        return result;
    }

    public boolean isSimulateShell() {
        return simulateShell;
    }

    public void setSimulateShell(boolean val) {
        simulateShell = val;
    }

    protected ChannelSftp getChannelSftp() {
        return channelSftp;
    }

}
