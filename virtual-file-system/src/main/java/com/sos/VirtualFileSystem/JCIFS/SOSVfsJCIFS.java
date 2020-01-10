package com.sos.VirtualFileSystem.JCIFS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSCommandResult;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.VirtualFileSystem.shell.CmdShell;
import com.sos.i18n.annotation.I18NResourceBundle;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsJCIFS extends SOSVfsTransferBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsJCIFS.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private static final int DEFAULT_PORT = 445;
    private CIFSContext context = null;
    private boolean isConnected = false;
    private String domain = null;
    private String currentDirectory = "";
    private boolean simulateShell = false;
    private CmdShell cmdShell = null;
    private String logPrefix = null;

    public SOSVfsJCIFS() {
        super();
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
            raiseException(SOSVfs_E_190.params("connection2OptionsAlternate"));
        }
        return this;
    }

    @Override
    public ISOSConnection authenticate(final ISOSAuthenticationOptions options) {
        authenticationOptions = options;
        try {
            domain = connection2OptionsAlternate.domain.getValue();
            host = connection2OptionsAlternate.host.getValue();
            port = connection2OptionsAlternate.port.isDirty() ? connection2OptionsAlternate.port.value() : DEFAULT_PORT;
            userName = authenticationOptions.getUser().getValue();
            setLogPrefix();
            try {
                createContext(domain, host, port, userName, authenticationOptions.getPassword().getValue());
            } catch (Exception ex) {
                throw new JobSchedulerException(ex);
            }
            reply = "OK";
            LOGGER.info(logPrefix);
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
        return this;
    }

    @Override
    public void login(String user, final String password) {
        try {
            userName = user;
            createContext(domain, host, port, userName, password);
            reply = "OK";
            logReply();
        } catch (Exception e) {
            raiseException(e, SOSVfs_E_134.params("authentication"));
        }
    }

    private void createContext(String domain, String host, int port, String user, String password) throws Exception {
        isConnected = false;
        Properties properties = new Properties();
        properties.put("jcifs.smb.client.disableSMB1", "false");
        properties.put("jcifs.smb.client.enableSMB2", "true");

        BaseContext bc = new BaseContext(new PropertyConfiguration(setConfigFromFiles(properties)));
        NtlmPasswordAuthentication creds = new NtlmPasswordAuthentication(bc, domain, user, password);
        context = bc.withCredentials(creds);
        isConnected = true;
    }

    private Properties setConfigFromFiles(Properties properties) {
        if (!SOSString.isEmpty(connection2OptionsAlternate.configuration_files.getValue())) {
            String[] files = connection2OptionsAlternate.configuration_files.getValue().split(";");
            for (int i = 0; i < files.length; i++) {
                String file = files[i].trim();
                LOGGER.info(String.format("%s[setConfigFromFiles][%s]", logPrefix, file));
                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    Properties p = new Properties();
                    p.load(in);
                    for (Entry<Object, Object> entry : p.entrySet()) {
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();
                        if (key.startsWith(";")) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[setConfigFromFiles][%s][skip]%s=%s", logPrefix, file, key, value));
                            }
                        } else {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("%s[setConfigFromFiles][%s]%s=%s", logPrefix, file, key, value));
                            }
                            properties.put(key, value);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.warn(String.format("%s[setConfigFromFiles][%s][failed]%s", logPrefix, file, ex.toString()));
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
        return properties;
    }

    @Override
    public void logout() {
        // do nothing
    }

    @Override
    public void disconnect() {
        reply = "OK";
        if (context != null) {
            try {
                context.close();
            } catch (CIFSException e) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[disconnect]%s", logPrefix, e.toString()), e);
                }
            }
            context = null;
        }
        isConnected = false;
        LOGGER.info(String.format("%s[disconnect]%s", logPrefix, getReplyString()));
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    protected boolean fileExists(String filename) {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(filename));
            boolean result = smbFile.exists();
            if (isTraceEnabled) {
                LOGGER.trace(String.format("%s[fileExists][%s]%s", logPrefix, filename, result));
            }
            return result;
        } catch (Exception e) {
            filename = smbFile == null ? filename : smbFile.getCanonicalPath();
            throw new JobSchedulerException(SOSVfs_E_226.params(filename), e);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    public long size(final String path) throws Exception {
        long size = -1;
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            if (smbFile.exists()) {
                size = smbFile.length();
            }
            if (isTraceEnabled) {
                LOGGER.trace(String.format("%s[size][%s]%s", logPrefix, path, size));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("%s[size][%s][failed]%s", logPrefix, path, e.toString()), e);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
        return size;
    }

    @Override
    public boolean isDirectory(final String path) {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            boolean result = smbFile.isDirectory();
            if (isTraceEnabled) {
                LOGGER.trace(String.format("%s[isDirectory][%s]%s", logPrefix, path, result));
            }
            return result;
        } catch (Exception e) {
            //
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
        return false;
    }

    public boolean isHidden(final String path) {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            boolean result = smbFile.isHidden();
            if (isTraceEnabled) {
                LOGGER.trace(String.format("%s[isHidden][%s]%s", logPrefix, path, result));
            }
            return result;
        } catch (Exception e) {
            //
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
        return false;
    }

    @Override
    public void mkdir(final String path) {
        SmbFile smbFile = null;
        try {
            reply = "OK";
            smbFile = getSmbFile(normalizePath(path));
            if (smbFile.exists()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[mkdir][%s]already exists", logPrefix, path));
                }
                return;
            }
            smbFile.mkdirs();
            LOGGER.info(String.format("%s[mkdir][%s]%s", logPrefix, path, getReplyString()));
        } catch (Exception e) {
            reply = e.toString();
            raiseException(e, String.format("%s[mkdir][%s]%s", logPrefix, path, e.toString()));
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    public void rmdir(String path) {
        SmbFile smbFile = null;
        try {
            reply = "OK";
            path = normalizePath(path);
            if (!path.endsWith("/")) {
                path += "/";
            }
            smbFile = getSmbFile(path);
            if (!smbFile.exists()) {
                throw new JobSchedulerException(String.format("%s[rmdir][%s][failed]filepath does not exist", logPrefix, smbFile.getPath()));
            }
            if (!smbFile.isDirectory()) {
                throw new JobSchedulerException(String.format("%s[rmdir][%s][failed]filepath is not a directory", logPrefix, smbFile.getPath()));
            }
            smbFile.delete();
            LOGGER.info(String.format("%s[rmdir][%s]%s", logPrefix, path, getReplyString()));
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_134.params("[rmdir] " + path), e);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    public String[] listNames(String path) throws IOException {
        SmbFile smbFile = null;
        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[listNames]%s", logPrefix, path));
            }
            if (path.isEmpty()) {
                path = ".";
            }

            smbFile = getSmbFile(normalizePath(path));
            // TODO skip checks: currently will not occur due previous isDirectory check
            if (!smbFile.exists()) {
                throw new JobSchedulerException(SOSVfs_E_226.params(path));
            }
            if (!smbFile.isDirectory()) {
                reply = "ls OK";
                return new String[] { path };
            }

            path = path.endsWith("/") ? path : path + "/";
            SmbFile[] smbFiles = smbFile.listFiles();
            String[] result = new String[smbFiles.length];
            for (int i = 0; i < smbFiles.length; i++) {
                result[i] = new StringBuilder(path).append(smbFiles[i].getName()).toString();
            }
            reply = "ls OK";
            return result;
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(e);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) {
        long fileSize = -1;

        SmbFile smbFile = null;
        SmbFileInputStream in = null;
        FileOutputStream fos = null;
        String remoteFilePath = normalizePath(remoteFile);
        try {
            fileSize = size(remoteFilePath);
            smbFile = getSmbFile(remoteFilePath);

            in = new SmbFileInputStream(smbFile);
            fos = new FileOutputStream(localFile, append);
            byte[] b = new byte[8192];
            int n = 0;
            while ((n = in.read(b)) > 0) {
                fos.write(b, 0, n);
            }
            in.close();
            in = null;
            fos.flush();
            fos.close();
            fos = null;

            File transferFile = new File(localFile);
            if (!append && fileSize > 0 && fileSize != transferFile.length()) {
                throw new JobSchedulerException(SOSVfs_E_162.params(fileSize, transferFile.length()));
            }
            fileSize = transferFile.length();
            reply = "OK";
            LOGGER.info(String.format("%s[getFile][%s][%s]%s", logPrefix, remoteFilePath, localFile, getReplyString()));

        } catch (Exception ex) {
            reply = ex.toString();
            raiseException(ex, String.format("%s[getFile][%s][%s][failed]%s", logPrefix, remoteFilePath, localFile, ex.toString()));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                    //
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex) {
                    //
                }
            }
            if (smbFile != null) {
                smbFile.close();
            }
        }
        return fileSize;
    }

    @Override
    public long putFile(final String localFile, final String remoteFile) {
        long size = 0;
        SmbFile smbFile = null;
        SmbFileOutputStream out = null;
        FileInputStream in = null;
        try {
            smbFile = getSmbFile(normalizePath(remoteFile));
            in = new FileInputStream(localFile);
            out = new SmbFileOutputStream(smbFile);
            byte[] b = new byte[8192];
            int n = 0;
            while ((n = in.read(b)) > 0) {
                out.write(b, 0, n);
            }
            reply = "put OK";
            LOGGER.info(String.format("%s[putFile][%s][%s]%s", logPrefix, localFile, smbFile.getPath(), getReplyString()));
            size = size(normalizePath(remoteFile));
        } catch (Exception e) {
            reply = e.toString();
            raiseException(e, String.format("%s[putFile][%s][%s][failed]%s", logPrefix, localFile, remoteFile, e.toString()));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    //
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    //
                }
            }
            if (smbFile != null) {
                smbFile.close();
            }
        }
        return size;
    }

    @Override
    public void delete(final String path) {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            if (smbFile.isDirectory()) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[delete]%s", logPrefix, path));
            }
            smbFile.delete();
        } catch (Exception ex) {
            reply = ex.toString();
            raiseException(ex, String.format("%s[delete][%s][failed]%s", logPrefix, path, ex.toString()));
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
        reply = "rm OK";
        LOGGER.info(String.format("%s[delete][%s]%s", logPrefix, path, getReplyString()));
    }

    @Override
    public void rename(final String from, final String to) {
        SmbFile smbFileFrom = null;
        SmbFile smbFileTo = null;
        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[rename][%s]%s", logPrefix, from, to));
            }
            smbFileFrom = getSmbFile(normalizePath(from));
            smbFileTo = getSmbFile(normalizePath(to));
            smbFileFrom.renameTo(smbFileTo);
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[rename][%s][%s][failed]%s", logPrefix, from, to, e.toString()), e);
        } finally {
            if (smbFileFrom != null) {
                smbFileFrom.close();
            }
            if (smbFileTo != null) {
                smbFileTo.close();
            }
        }
        reply = "OK";
        LOGGER.info(String.format("%s[rename][%s][%s]%s", logPrefix, from, to, getReplyString()));
    }

    @Override
    public void executeCommand(final String cmd) throws Exception {
        executeCommand(cmd, null);
    }

    @Override
    public void executeCommand(final String cmd, SOSVfsEnv env) throws Exception {
        if (cmdShell == null) {
            cmdShell = new CmdShell();
        }
        String command = cmd.trim();
        if (cmdShell.isWindows()) {
            command = cmdShell.replaceCommand4Windows(command);
        }
        int exitCode = cmdShell.executeCommand(command, env);
        if (exitCode != 0) {
            boolean raiseException = true;
            if (connection2OptionsAlternate != null) {
                raiseException = connection2OptionsAlternate.raiseExceptionOnError.value();
            }
            if (raiseException) {
                throw new JobSchedulerException(SOSVfs_E_191.params(exitCode + ""));
            } else {
                LOGGER.info(SOSVfs_D_151.params(command, SOSVfs_E_191.params(exitCode + "")));
            }
        }
    }

    @Override
    public InputStream getInputStream(final String path) {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            return new SmbFileInputStream(smbFile);
        } catch (Exception ex) {
            raiseException(ex, String.format("%s[getInputStream][%s][failed]%s", logPrefix, path, ex.toString()));
            return null;
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    public OutputStream getOutputStream(final String path) {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            return new SmbFileOutputStream(smbFile);
        } catch (Exception ex) {
            raiseException(ex, String.format("%s[getOutputStream][%s][failed]%s", logPrefix, path, ex.toString()));
            return null;
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    public boolean changeWorkingDirectory(final String path) {
        SmbFile smbFile = null;
        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[cwd]%s", logPrefix, path));
            }
            smbFile = getSmbFile(normalizePath(path));
            if (!smbFile.exists()) {
                reply = String.format("%s[cwd][%s]filepath does not exist", logPrefix, smbFile.getPath());
                return false;
            }
            if (!smbFile.isDirectory()) {
                reply = String.format("%s[cwd][%s]filepath is not a directory", logPrefix, smbFile.getPath());
                return false;
            }
            currentDirectory = smbFile.getPath();
            reply = "OK";
        } catch (Exception ex) {
            throw new JobSchedulerException(String.format("%s[cwd][%s][failed]%s", logPrefix, path, ex.toString()), ex);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
            if (isDebugEnabled) {
                LOGGER.debug(getReplyString());
            }
        }
        return true;
    }

    @Override
    public ISOSVirtualFile getFileHandle(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSVirtualFile file = new SOSVfsJCIFSFile(fileName);
        file.setHandler(this);
        return file;
    }

    @Override
    public String getModificationTime(final String path) {
        String dateTime = null;
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            if (smbFile.exists()) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateTime = df.format(new Date(smbFile.getLastModified()));
            }
        } catch (Exception ex) {
            //
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
        return dateTime;
    }

    public long getModificationTimeStamp(final String path) throws Exception {
        long timestamp = -1L;
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            if (smbFile.exists()) {
                timestamp = smbFile.getLastModified();
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
        return timestamp;
    }

    public void setModificationTimeStamp(final String path, final long timeStamp) throws Exception {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            if (smbFile.exists()) {
                smbFile.setLastModified(timeStamp);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    protected String getCurrentPath() {
        return currentDirectory;
    }

    private String normalizePath(final String path) {
        return path.replaceAll("\\\\", "/");
    }

    private String getSmbFilePath(String path) {
        path = path.startsWith("/") ? path.substring(1) : path;
        return "smb://" + host + "/" + path;
    }

    private SmbFile getSmbFile(final String path) throws Exception {
        try {
            return new SmbFile(getSmbFilePath(path), context);
        } catch (Exception ex) {
            throw new JobSchedulerException(String.format("%s[getSmbFile][%s][failed]%s", logPrefix, path, ex.toString()), ex);
        }
    }

    private void setLogPrefix() {
        StringBuilder sb = new StringBuilder("[smb]");
        if (!SOSString.isEmpty(domain)) {
            sb.append("[").append(domain).append("]");
        }
        sb.append("[").append(userName).append("@").append(host).append(":").append(port).append("]");
        logPrefix = sb.toString();
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

    @Override
    public boolean isSimulateShell() {
        return simulateShell;
    }

    @Override
    public void setSimulateShell(boolean val) {
        simulateShell = val;
    }

    @Override
    public SOSCommandResult executePrivateCommand(String cmd) throws Exception {
        return null;
    }

}