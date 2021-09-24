package com.sos.vfs.sftp.sshj;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.exception.SOSException;
import com.sos.exception.SOSMissingDataException;
import com.sos.exception.SOSNoSuchFileException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommandResult;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.exception.SOSAuthenticationFailedException;
import com.sos.vfs.sftp.SOSSFTP;
import com.sos.vfs.sftp.common.ISOSSFTP;
import com.sos.vfs.sftp.common.SOSSSHServerInfo;
import com.sos.vfs.sftp.exception.SOSSFTPClientNotInitializedException;
import com.sos.vfs.sftp.exception.SOSSSHCommandExitViolentlyException;
import com.sos.vfs.sftp.sshj.common.SSHProviderUtil;
import com.sos.vfs.sftp.sshj.common.proxy.Proxy;
import com.sos.vfs.sftp.sshj.common.proxy.ProxySocketFactory;

import net.schmizz.keepalive.KeepAlive;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.keepalive.KeepAliveRunner;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.Service;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.Response.StatusCode;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.PasswordResponseProvider;
import net.schmizz.sshj.xfer.FileSystemFile;
import sos.util.SOSDate;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSFTPSSHJ extends SOSCommonProvider implements ISOSSFTP {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSFTPSSHJ.class);

    private Config config;
    private SSHClient sshClient;
    private SFTPClient sftpClient;

    private Command sshClientCommand;
    private Integer exitCode;
    private String exitSignal;
    private String stdOut;
    private String stdErr;

    private boolean simulateShell = false;

    private SOSSSHServerInfo serverInfo;
    /** e.g. "OpenSSH_$version" -> OpenSSH_for_Windows_8.1. Can be null. */
    private String serverVersion;

    public SOSSFTPSSHJ() {
        super();
    }

    @Override
    public boolean isConnected() {
        if (sshClient == null) {
            return false;
        }
        return sshClient.isConnected();
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        super.connect(options);

        LOGGER.info(new StringBuilder("[").append(getProviderOptions().protocol.getValue()).append("]").append(SOSVfs_D_0101.params(
                getProviderOptions().host.getValue(), getProviderOptions().port.value())).toString());

        try {
            createSSHClient();
            sshClient.connect(options.host.getValue(), options.port.value());
            authenticate();
            setKeepAlive();
            serverVersion = sshClient.getTransport().getServerVersion();
            createSFTPClient();
            printConnectionInfos();
        } catch (JobSchedulerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
    }

    private void printConnectionInfos() {
        List<String> msg = new ArrayList<String>();
        if (sshClient.getTimeout() > 0 || sshClient.getConnectTimeout() > 0) {
            msg.add("ConnectTimeout=" + ms2string(sshClient.getConnectTimeout()) + ", SocketTimeout=" + ms2string(sshClient.getTimeout()));
        }
        if (sshClient.getConnection() != null) {
            KeepAlive r = sshClient.getConnection().getKeepAlive();
            if (r.getKeepAliveInterval() > 0) {
                if (r instanceof KeepAliveRunner) {
                    msg.add("KeepAliveInterval=" + r.getKeepAliveInterval() + "s, MaxAliveCount=" + ((KeepAliveRunner) r).getMaxAliveCount());
                } else {
                    msg.add("KeepAliveInterval=" + r.getKeepAliveInterval() + "s");
                }
            }
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

    @Override
    public void disconnect() {
        reply = "disconnect OK";
        if (sftpClient != null) {
            try {
                sftpClient.close();
                LOGGER.debug("[sftp client]disconnected");
            } catch (Throwable e) {
                reply = "[sftp client][disconnect]" + e.toString();
            }
        }
        if (sshClient != null) {
            try {
                sshClient.close();
                LOGGER.debug("[ssh client]disconnected");
            } catch (Throwable e) {
                reply = "[ssh client][disconnect]" + e.toString();
            }
        }
        LOGGER.info(reply);
    }

    @Override
    public void mkdir(final String path) {
        try {
            if (sftpClient == null) {
                throw new SOSSFTPClientNotInitializedException();
            }
            if (SOSString.isEmpty(path)) {
                throw new SOSMissingDataException("path");
            }
            String p = path.replaceAll("//+", "/").replaceFirst("/$", "");
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[mkdir][%s]try to create ...", p));
            }
            sftpClient.mkdirs(path);
            reply = "mkdir OK";
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("[%s] mkdir failed", path), e);
        }
    }

    @Override
    public void rmdir(final String path) {
        try {
            if (sftpClient == null) {
                throw new SOSSFTPClientNotInitializedException();
            }
            if (SOSString.isEmpty(path)) {
                throw new SOSMissingDataException("path");
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[rmdir][%s]try to remove ...", path));
            }
            final Deque<RemoteResourceInfo> toRemove = new LinkedList<RemoteResourceInfo>();
            dirInfo(sftpClient, path, toRemove, true);
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            while (!toRemove.isEmpty()) {
                RemoteResourceInfo resource = toRemove.pop();
                String resourcePath = resource.getPath();
                if (isDebugEnabled) {
                    LOGGER.debug(getHostID(SOSVfs_D_179.params("rmdir", resourcePath)));
                }
                if (resource.isDirectory()) {
                    sftpClient.rmdir(resourcePath);
                } else if (resource.isRegularFile()) {
                    sftpClient.rm(resourcePath);
                }
                if (isDebugEnabled) {
                    LOGGER.debug(getHostID(SOSVfs_D_181.params("rmdir", resourcePath, getReplyString())));
                }
            }
            sftpClient.rmdir(path);
            reply = "rmdir OK";
            LOGGER.info(getHostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("[%s] rmdir failed", path), e);
        }
    }

    private void dirInfo(SFTPClient client, String path, Deque<RemoteResourceInfo> result, boolean recursive) throws Exception {
        List<RemoteResourceInfo> infos = client.ls(path);// SFTPException: No such file
        for (RemoteResourceInfo resource : infos) {
            result.push(resource);
            if (recursive && resource.isDirectory()) {
                dirInfo(client, resource.getPath(), result, recursive);
            }
        }
    }

    @Override
    public boolean fileExists(final String path) {
        boolean result = is(path, FileMode.Type.REGULAR);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]fileExists=%s", path, result));
        }
        return result;
    }

    @Override
    public boolean directoryExists(final String path) {
        return isDirectory(path);
    }

    @Override
    public boolean isDirectory(final String path) {
        boolean result = is(path, FileMode.Type.DIRECTORY);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]isDirectory=%s", path, result));
        }
        return result;
    }

    @Override
    public long size(String path) throws Exception {
        path = normalizePath(path);
        long size = -1;
        try {
            FileAttributes attr = getFileAttributes(path);
            if (attr != null) {
                size = attr.getSize();
            }
        } catch (SFTPException e) {
            try {
                throwException(e, path);
            } catch (SOSNoSuchFileException ex) {
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]size=%s", path, size));
        }
        return size;
    }

    private boolean is(String path, FileMode.Type type) {
        try {
            FileAttributes attr = getFileAttributes(path);
            if (attr != null) {
                return type.equals(attr.getType());
            }
        } catch (Throwable e) {
        }
        return false;
    }

    protected FileAttributes getFileAttributes(String path) throws Exception {
        if (sftpClient == null) {
            throw new SOSSFTPClientNotInitializedException();
        }
        if (SOSString.isEmpty(path)) {
            throw new SOSMissingDataException("path");
        }
        return sftpClient.stat(path);
    }

    private void createSSHClient() throws Exception {
        setConfig();
        setKeepAliveProvider();
        sshClient = new SSHClient(config);
        setHostKeyVerifier();
        setCompression();
        setTimeout();
        setProxy();
    }

    private void createSFTPClient() throws Exception {
        if (sshClient == null || !getProviderOptions().protocol.getValue().equals(TransferTypes.sftp.name())) {
            return;
        }
        sftpClient = sshClient.newSFTPClient();
    }

    private void setConfig() {
        config = new DefaultConfig();
    }

    private void setKeepAliveProvider() {
        if (!SOSString.isEmpty(getProviderOptions().server_alive_interval.getValue())) {
            config.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);

        }
    }

    private void setKeepAlive() {
        String sai = getProviderOptions().server_alive_interval.getValue();
        if (!SOSString.isEmpty(sai)) {
            try {
                // sshClient.getConnection().getKeepAlive().setKeepAliveInterval(Integer.parseInt(sai));
                KeepAliveRunner r = (KeepAliveRunner) sshClient.getConnection().getKeepAlive();
                r.setKeepAliveInterval(SOSDate.resolveAge("s", sai).intValue());

                String sacm = getProviderOptions().server_alive_count_max.getValue();
                if (!SOSString.isEmpty(sacm)) {
                    r.setMaxAliveCount(Integer.parseInt(sacm));
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("[setKeepAlive]%s", ex.toString()), ex);
            }
        }
    }

    private void setHostKeyVerifier() throws Exception {
        // default HostKeyVerifier -> OpenSSHKnownHosts
        if (getProviderOptions().strictHostKeyChecking.isTrue()) {
            sshClient.loadKnownHosts();// default search in <user.home>/.ssh/known_hosts|known_hosts2
        } else {
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        }
    }

    private void setCompression() throws TransportException {
        if (getProviderOptions().useZlibCompression.value()) {
            // JCRAFT compression com.jcraft.jzlib-1.1.3.jar
            // uses default compression_level=6 (1-best speed, 9-best compression)
            // see JZlib.Z_DEFAULT_COMPRESSION(-1) and com.jcraft.jzlib.Deplate.deflateInit(with Z_DEFAULT_COMPRESSION))
            sshClient.useCompression();
        }
    }

    private void setTimeout() {
        String t = getProviderOptions().connect_timeout.getValue();
        if (!SOSString.isEmpty(t)) {
            int connectTimeout;
            try {
                connectTimeout = SOSDate.resolveAge("ms", t).intValue();
                if (connectTimeout > 0) {
                    sshClient.setConnectTimeout(connectTimeout);
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("[setConnectTimeout][%s]%s", t, ex.toString()), ex);
            }

        }
        t = getProviderOptions().channel_connect_timeout.getValue();
        if (!SOSString.isEmpty(t)) {
            try {
                int socketTimeout = SOSDate.resolveAge("ms", t).intValue();
                if (socketTimeout > 0) {
                    sshClient.setTimeout(socketTimeout);
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("[setTimeout][%s]%s", t, ex.toString()), ex);
            }
        }
    }

    public void setProxy() {
        String proxyHost = getProviderOptions().proxyHost.getValue();
        if (!SOSString.isEmpty(proxyHost)) {
            SOSOptionProxyProtocol proxyProtocol = getProviderOptions().proxyProtocol;
            String proxyUser = getProviderOptions().proxyUser.getValue();
            String proxyPassword = getProviderOptions().proxyPassword.getValue();
            int proxyPort = getProviderOptions().proxyPort.value();
            int proxyConnectTimeout = 30_000;// ms
            String t = getProviderOptions().connect_timeout.getValue();
            if (!SOSString.isEmpty(t)) {
                try {
                    proxyConnectTimeout = SOSDate.resolveAge("ms", t).intValue();
                } catch (Exception ex) {
                    LOGGER.warn(String.format("[proxyConnectTimeout][%s]%s", t, ex.toString()), ex);
                }
            }
            LOGGER.info(String.format("using proxy: protocol=%s, host=%s, port=%d, user=%s, pass=?", proxyProtocol.getValue(), proxyHost, proxyPort,
                    proxyUser));

            java.net.Proxy.Type type = proxyProtocol.isHttp() ? java.net.Proxy.Type.HTTP : java.net.Proxy.Type.SOCKS;
            Proxy proxy = new Proxy(type, proxyHost, proxyPort, proxyUser, proxyPassword, proxyConnectTimeout);
            sshClient.setSocketFactory(new ProxySocketFactory(proxy));
        }
    }

    private List<String> toList(String s) {
        if (SOSString.isEmpty(s)) {
            return null;
        }
        return Stream.of(s.trim().replaceAll(";", ",").split(",")).map(String::trim).collect(Collectors.toList());
    }

    private void authenticate() throws Exception {
        if (getProviderOptions().preferred_authentications.isNotEmpty()) {
            usePreferredAuthentications();
        } else if (getProviderOptions().required_authentications.isNotEmpty()) {
            useRequiredAuthentications();
        } else {
            useAuthMethodAuthentication();
        }
    }

    private void usePreferredAuthentications() throws Exception {
        List<net.schmizz.sshj.userauth.method.AuthMethod> methods = new LinkedList<>();
        List<String> list = toList(getProviderOptions().preferred_authentications.getValue().toLowerCase());
        if (list == null) {
            return;
        }
        for (String am : list) {
            switch (am) {
            case "publickey":
                methods.add(getAuthPublickey());
                break;
            case "password":
                methods.add(getAuthPassword());
                break;
            case "keyboardinteractive":
            case "keyboard_interactive":
                methods.add(getAuthKeyboardInteractive());
                break;
            }
        }
        sshClient.auth(getProviderOptions().user.getValue(), methods);
    }

    /** ssh(d)_config AuthenticationMethods */
    private void useRequiredAuthentications() throws Exception {
        List<String> list = toList(getProviderOptions().required_authentications.getValue().toLowerCase());
        if (list == null) {
            return;
        }
        for (String am : list) {
            switch (am) {
            case "publickey":
                partialAuthentication(getAuthPublickey());
                break;
            case "password":
                partialAuthentication(getAuthPassword());
                break;
            case "keyboardinteractive":
            case "keyboard_interactive":
                partialAuthentication(getAuthKeyboardInteractive());
                break;
            }
        }
    }

    private void useAuthMethodAuthentication() throws Exception {
        if (SOSString.isEmpty(getProviderOptions().authMethod.getValue())) {
            throw new SOSException("missing required argument \"auth_method\"");
        }
        net.schmizz.sshj.userauth.method.AuthMethod method = null;
        switch (getProviderOptions().authMethod.getValue().toLowerCase()) {
        case "publickey":
            method = getAuthPublickey();
            break;
        case "password":
            method = getAuthPassword();
            break;
        case "keyboardinteractive":
        case "keyboard_interactive":
            method = getAuthKeyboardInteractive();
            break;
        }
        if (method == null) {
            throw new SOSException(String.format("unknown method=%s", getProviderOptions().authMethod.getValue()));
        }
        sshClient.auth(getProviderOptions().user.getValue(), method);
    }

    private void partialAuthentication(net.schmizz.sshj.userauth.method.AuthMethod method) throws SOSAuthenticationFailedException, UserAuthException,
            TransportException {
        if (!sshClient.getUserAuth().authenticate(getProviderOptions().user.getValue(), (Service) sshClient.getConnection(), method, sshClient
                .getTransport().getTimeoutMs())) {
            if (!sshClient.getUserAuth().hadPartialSuccess()) {
                throw new SOSAuthenticationFailedException();
            }
        }
    }

    private AuthPassword getAuthPassword() throws SOSException, UserAuthException, TransportException {
        if (SOSString.isEmpty(getProviderOptions().password.getValue())) {
            throw new SOSException("missing required argument \"password\"");
        }
        return new AuthPassword(SSHProviderUtil.getPasswordFinder(getProviderOptions().password.getValue()));
    }

    private AuthKeyboardInteractive getAuthKeyboardInteractive() throws SOSException, UserAuthException, TransportException {
        if (SOSString.isEmpty(getProviderOptions().password.getValue())) {
            throw new SOSException("missing required argument \"password\"");
        }
        return new AuthKeyboardInteractive(new PasswordResponseProvider(SSHProviderUtil.getPasswordFinder(getProviderOptions().password.getValue())));
    }

    private AuthPublickey getAuthPublickey() throws Exception {
        // TODO Agent support getArguments().getUseKeyAgent().getValue()
        KeyProvider keyProvider = null;
        if (getProviderOptions().keepass_database.value() != null) {   // from Keepass attachment
            keyProvider = SSHProviderUtil.getKeyProviderFromKeepass(config, getProviderOptions());
        } else {// from File
            if (SOSString.isEmpty(getProviderOptions().authFile.getValue())) {
                throw new SOSException("missing required argument \"auth_file\"");
            }
            Path authFile = Paths.get(getProviderOptions().authFile.getValue());
            if (SOSString.isEmpty(getProviderOptions().passphrase.getValue())) {
                keyProvider = sshClient.loadKeys(authFile.toFile().getCanonicalPath());
            } else {
                keyProvider = sshClient.loadKeys(authFile.toFile().getCanonicalPath(), getProviderOptions().passphrase.getValue());
            }
        }
        return new AuthPublickey(keyProvider);
    }

    @Override
    public SOSFileEntry getFileEntry(String pathname) throws Exception {
        FileAttributes attrs = getFileAttributes(pathname);
        if (attrs != null && !FileMode.Type.DIRECTORY.equals(attrs.getType())) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s]found", pathname));
            }

            Path tmpPath = Paths.get(pathname);
            Path parent = tmpPath.getParent();
            return getFileEntry(attrs, tmpPath.getFileName().toString(), parent == null ? null : parent.toString());
        }
        return null;
    }

    private SOSFileEntry getFileEntry(FileAttributes attrs, String fileName, String parentPath) {
        SOSFileEntry entry = new SOSFileEntry(EntryType.FILESYSTEM);
        entry.setDirectory(FileMode.Type.DIRECTORY.equals(attrs.getType()));
        entry.setFilename(fileName);
        entry.setFilesize(attrs.getSize());
        // entry.setLastModified(attrs.getMTime());
        entry.setParentPath(parentPath);
        // is not necessary for SSHJ but due to YADE functionality to compare the directories
        // see SOSFileListEntry.getTargetFile: if (!fileNamesAreEqual(
        if (parentPath != null && SOSSFTP.hasWindowsOpenSSHDriverLetterSpecifier(parentPath)) {
            entry.setFullPath("/" + entry.getFullPath());
        }
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) {
        path = normalizePath(path);
        try {
            if (sftpClient == null) {
                throw new SOSSFTPClientNotInitializedException();
            }
            if (SOSString.isEmpty(path)) {
                throw new SOSMissingDataException("path");
            }

            List<SOSFileEntry> result = new ArrayList<>();
            if (path.isEmpty()) {
                path = ".";
            }
            if (checkIfExists && !fileExists(path)) {// TODO ? fileExists
                return result;
            }
            if (checkIfIsDirectory && !isDirectory(path)) {
                reply = "ls OK";
                return result;
            }
            List<RemoteResourceInfo> infos = sftpClient.ls(path);// SFTPException: No such file
            if (infos != null && infos.size() > 0) {
                for (RemoteResourceInfo resource : infos) {
                    String name = resource.getName();
                    if (name.equals(".") || name.equals("..")) {
                        continue;
                    }
                    result.add(getFileEntry(resource.getAttributes(), name, path));
                }
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s][ls] %s files or folders", path, result.size()));
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
            if (sftpClient == null) {
                throw new SOSSFTPClientNotInitializedException();
            }
            if (SOSString.isEmpty(path)) {
                throw new SOSMissingDataException("path");
            }

            if (checkIsDirectory && this.isDirectory(path)) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }
            try {
                sftpClient.rm(sftpClient.canonicalize(path));
            } catch (SFTPException e) {
                throwException(e, path);
            }
        } catch (Exception ex) {
            reply = ex.toString();
            throw new JobSchedulerException(SOSVfs_E_187.params("delete", path), ex);
        }
        reply = "rm OK";
        LOGGER.info(getHostID(SOSVfs_D_181.params("delete", path, getReplyString())));
    }

    @Override
    public void rename(String oldpath, String newpath) {
        oldpath = normalizePath(oldpath);
        newpath = normalizePath(newpath);
        try {
            if (sftpClient == null) {
                throw new SOSSFTPClientNotInitializedException();
            }
            if (SOSString.isEmpty(oldpath) || SOSString.isEmpty(newpath)) {
                throw new SOSMissingDataException("oldpath or newpath");
            }
            try {
                sftpClient.rename(sftpClient.canonicalize(oldpath), newpath);
            } catch (SFTPException e) {
                throwException(e, oldpath);
            }
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_188.params("rename", oldpath, newpath), e);
        }
        reply = "mv OK";
        LOGGER.info(getHostID(SOSVfs_I_189.params(oldpath, newpath, getReplyString())));
    }

    @Override
    public void executeCommand(String cmd) {
        executeCommand(cmd, null);
    }

    @Override
    public void executeCommand(String command, SOSEnv env) {
        if (sshClient == null || command == null) {
            return;
        }
        command = command.trim().replaceAll("\0", "\\\\\\\\").replaceAll("\"", "\\\"");
        SOSCommandResult result = new SOSCommandResult(command);
        sshClientCommand = null;
        exitCode = null;
        stdOut = null;
        stdErr = null;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        try (Session session = sshClient.startSession()) {

            if (isSimulateShell()) {
                session.allocateDefaultPTY();
            }
            command = handleEnvs(command, session, env, isDebugEnabled);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[cmd]%s", command));
            }
            sshClientCommand = session.exec(command);

            result.addStdOut(IOUtils.readFully(sshClientCommand.getInputStream()).toString());
            result.addStdErr(IOUtils.readFully(sshClientCommand.getErrorStream()).toString());

            sshClientCommand.join();
            result.setExitCode(sshClientCommand.getExitStatus());
            if (result.getExitCode() == null) {
                if (sshClientCommand.getExitSignal() != null) {
                    throw new SOSSSHCommandExitViolentlyException(sshClientCommand.getExitSignal(), sshClientCommand.getExitErrorMessage());
                }
            }
        } catch (Throwable e) {
            result.setException(e);
        } finally {
            if (sshClientCommand != null) {
                try {
                    sshClientCommand.close();
                } catch (Throwable e) {
                }
                sshClientCommand = null;
            }
        }

        exitCode = result.getExitCode();
        stdOut = result.getStdOut();
        stdErr = result.getStdErr();

        if (result.hasError(false)) {
            reply = result.toString();
            if (getProviderOptions().raiseExceptionOnError.value()) {
                if (result.getException() == null) {
                    throw new JobSchedulerException(reply);
                } else {
                    throw new JobSchedulerException(result.getException());
                }
            }
            LOGGER.info(String.format("[%s][error]%s", command, reply));
        } else {
            LOGGER.info(String.format("[%s][std:out]%s", command, result.getStdOut().trim()));
        }
    }

    private String handleEnvs(String command, Session session, SOSEnv env, boolean isDebugEnabled) throws Exception {
        if (env == null) {
            return command;
        }
        // global/ssh server env vars
        if (env.getGlobalEnvs() != null && env.getGlobalEnvs().size() > 0) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[set global envs]%s", env.getGlobalEnvs()));
            }
            for (Map.Entry<String, String> entry : env.getGlobalEnvs().entrySet()) {
                try {
                    session.setEnvVar(entry.getKey(), entry.getValue());
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[can't set ssh session environment variable][%s=%s]%s", entry.getKey(), entry.getValue(), e
                            .toString()), e);
                }
            }
        }

        // local/system env vars
        if (env.getLocalEnvs() != null && env.getLocalEnvs().size() > 0) {
            getSSHServerInfo();

            StringBuilder envs = new StringBuilder();
            for (Map.Entry<String, String> entry : env.getLocalEnvs().entrySet()) {
                if (serverInfo.hasWindowsShell()) {
                    envs.append(String.format("set %s=%s&", entry.getKey(), entry.getValue()));
                } else {
                    envs.append(String.format("export \"%s=%s\";", entry.getKey(), entry.getValue()));
                }
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[set local envs]%s", envs));
            }
            command = envs.toString() + command;
        }
        return command;
    }

    @Override
    public SOSSSHServerInfo getSSHServerInfo() {
        if (serverInfo == null) {
            serverInfo = new SOSSSHServerInfo(serverVersion, executeResultCommand("uname"));
        }
        return serverInfo;
    }

    @Override
    public boolean isExecSessionExists() {
        return sshClientCommand != null;
    }

    @Override
    public boolean isExecSessionConnected() {
        return sshClientCommand != null && sshClientCommand.isOpen();
    }

    @Override
    public void execSessionSendSignalContinue() throws Exception {
        if (sshClientCommand != null && sshClient != null) {
            sshClient.getConnection().sendGlobalRequest("keepalive@openssh.com", true, new byte[0]);
        }
    }

    @Override
    public InputStream getInputStream(final String fileName) {
        try {
            return createInputStream(sftpClient.open(fileName));
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getInputStream()", fileName), ex);
        }
    }

    @Override
    public OutputStream getOutputStream(String fileName, boolean append, boolean resume) {
        try {
            EnumSet<OpenMode> set = EnumSet.of(OpenMode.WRITE, OpenMode.CREAT, OpenMode.TRUNC);
            if (append) {
                set.add(OpenMode.APPEND);
            } else if (resume) {
                // transferMode = ChannelSftp.RESUME; //TODO?
            }
            RemoteFile remoteFile = sftpClient.open(fileName, set);
            return createOutputStream(remoteFile);
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("getOutputStream()", fileName), ex);
        }
    }

    private InputStream createInputStream(RemoteFile remoteFile) throws IOException {
        return remoteFile.new ReadAheadRemoteFileInputStream(16) {

            private final AtomicBoolean close = new AtomicBoolean();

            @Override
            public void close() throws IOException {
                if (close.get()) {
                    return;
                }
                try {
                    super.close();
                } finally {
                    remoteFile.close();
                    close.set(true);
                }
            }
        };
        /*
         * return remoteFile.new RemoteFileInputStream() { private boolean isClosed = false;
         * @Override public synchronized void close() throws IOException { if (!isClosed) { remoteFile.close(); isClosed = true; } } };
         */

    }

    private static OutputStream createOutputStream(RemoteFile remoteFile) {
        return remoteFile.new RemoteFileOutputStream(0, 16) {

            private boolean isClosed = false;

            @Override
            public synchronized void close() throws IOException {
                if (!isClosed) {
                    remoteFile.close();
                    isClosed = true;
                }
            }
        };
    }

    @Override
    public ISOSProviderFile getFile(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSProviderFile file = new SOSSFTPFileSSHJ(fileName);
        file.setProvider(this);
        return file;
    }

    @Override
    public String getModificationDateTime(final String path) {
        String dateTime = null;
        try {
            if (SOSString.isEmpty(path)) {
                throw new SOSMissingDataException("path");
            }
            try {
                FileAttributes attr = getFileAttributes(path);
                if (attr != null) {
                    long mt = attr.getMtime();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dateTime = df.format(new Date(mt));
                }
            } catch (SFTPException e) {
                throwException(e, path);
            }
        } catch (Throwable e) {
            //
        }
        return dateTime;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void resetStdErr() {
        stdErr = null;
    }

    public String getStdOut() {
        return stdOut;
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
            if (sftpClient == null) {
                throw new SOSSFTPClientNotInitializedException();
            }
            Instant start = Instant.now();
            sftpClient.put(new FileSystemFile(source), normalizePath(target));
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
        sftpClient.chmod(target, chmod);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[put][%s]chmod=%s", target, chmod));
        }
    }

    public void get(final String source, final String target) {
        try {
            if (sftpClient == null) {
                throw new SOSSFTPClientNotInitializedException();
            }
            Instant start = Instant.now();
            sftpClient.getFileTransfer().setPreserveAttributes(false);
            sftpClient.get(normalizePath(source), new FileSystemFile(target));
            Instant end = Instant.now();

            reply = new StringBuilder("get OK (").append(SOSDate.getDuration(start, end)).append(")").toString();
            LOGGER.info(getHostID(SOSVfs_I_183.params("get", source, target, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_185.params("get()", source, target), e);
        }
    }

    public SOSCommandResult executeResultCommand(String command) {
        SOSCommandResult result = new SOSCommandResult(command);
        if (sshClient == null) {
            return result;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[cmd]%s", command));
        }
        try (Session session = sshClient.startSession()) {
            if (isSimulateShell()) {
                session.allocateDefaultPTY();
            }
            Command cmd = session.exec(command);
            result.addStdOut(IOUtils.readFully(cmd.getInputStream()).toString());
            result.addStdErr(IOUtils.readFully(cmd.getErrorStream()).toString());

            cmd.join();
            result.setExitCode(cmd.getExitStatus());
            if (result.getExitCode() == null) {
                if (cmd.getExitSignal() != null) {
                    throw new SOSSSHCommandExitViolentlyException(cmd.getExitSignal(), cmd.getExitErrorMessage());
                }
            }
        } catch (Throwable e) {
            result.setException(e);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(result.toString());
        }
        return result;
    }

    private void throwException(SFTPException e, String msg) throws Exception {
        StatusCode sc = e.getStatusCode();
        if (sc != null) {
            if (sc.equals(StatusCode.NO_SUCH_FILE) || sc.equals(StatusCode.NO_SUCH_PATH)) {
                throw new SOSNoSuchFileException(msg, e);
            }
        }
        throw e;
    }

    @Override
    public boolean isSFTP() {
        return true;
    }

    public boolean isSimulateShell() {
        return simulateShell;
    }

    public void setSimulateShell(boolean val) {
        simulateShell = val;
    }

    protected SFTPClient getSFTPClient() {
        return sftpClient;
    }

}
