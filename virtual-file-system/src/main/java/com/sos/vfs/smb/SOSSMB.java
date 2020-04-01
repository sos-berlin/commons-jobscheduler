package com.sos.vfs.smb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.interfaces.ISOSVirtualFile;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSEnv;
import com.sos.vfs.shell.SOSShell;
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
public class SOSSMB extends SOSCommonProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSMB.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private static final int DEFAULT_PORT = 445;
    private CIFSContext context = null;
    private boolean isConnected = false;

    private String domain = null;

    private SOSShell shell = null;
    private String logPrefix = null;

    public SOSSMB() {
        super();
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void connect(final SOSProviderOptions options) throws Exception {
        super.connect(options);

        try {
            domain = providerOptions.domain.getValue();
            port = providerOptions.port.isDirty() ? port : DEFAULT_PORT;
            setLogPrefix();
            try {
                createContext(domain, host, port, user, providerOptions.password.getValue());
            } catch (Exception ex) {
                throw new JobSchedulerException(ex);
            }
            reply = "OK";
            LOGGER.info(logPrefix);
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
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
        if (!SOSString.isEmpty(providerOptions.configuration_files.getValue())) {
            String[] files = providerOptions.configuration_files.getValue().split(";");
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
    public boolean fileExists(String filename) {
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
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[mkdir][%s]created", logPrefix, path));
            }
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[mkdir][%s]%s", logPrefix, path, e.toString()), e);
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
    public SOSFileEntry getFileEntry(String pathname) throws Exception {
        SmbFile file = getSmbFile(normalizePath(pathname));
        if (file == null) {
            return null;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]found", pathname));
        }
        String parent = "/";
        try {
            parent = SOSCommonProvider.normalizePath(Paths.get(pathname).getParent().toString());
        } catch (Exception e) {
            LOGGER.error(String.format("[%s][can't get parent path]%s", pathname, e.toString()), e);
        }

        SOSFileEntry entry = getFileEntry(file, parent);
        return entry;
    }

    private SOSFileEntry getFileEntry(SmbFile file, String parentPath) throws Exception {
        SOSFileEntry entry = new SOSFileEntry(EntryType.SMB);
        entry.setDirectory(file.isDirectory());
        entry.setFilename(file.getName());
        entry.setFilesize(file.length());
        entry.setParentPath(parentPath);
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(String path, boolean checkIfExists, boolean checkIfIsDirectory) throws IOException {
        SmbFile smbFile = null;
        try {
            List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
            if (path.isEmpty()) {
                path = ".";
            }
            smbFile = getSmbFile(normalizePath(path));
            if (checkIfExists && !smbFile.exists()) {
                return result;
            }
            if (checkIfIsDirectory && !smbFile.isDirectory()) {
                reply = "ls OK";
                return result;
            }

            path = path.endsWith("/") ? path : path + "/";
            SmbFile[] list = smbFile.listFiles();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s][listFiles] %s files or folders", path, list.length));
            }

            for (int i = 0; i < list.length; i++) {
                SmbFile file = list[i];
                result.add(getFileEntry(file, path));

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
    public void delete(final String path, boolean checkIsDirectory) {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            if (checkIsDirectory && smbFile.isDirectory()) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[delete]%s", logPrefix, path));
            }
            smbFile.delete();
        } catch (Exception ex) {
            reply = ex.toString();
            throw new JobSchedulerException(String.format("%s[delete][%s][failed]%s", logPrefix, path, ex.toString()), ex);
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
    public void executeCommand(final String cmd, SOSEnv env) throws Exception {
        if (shell == null) {
            shell = new SOSShell();
        }
        String command = cmd.trim();
        if (shell.isWindows()) {
            command = shell.replaceCommand4Windows(command);
        }
        int exitCode = shell.executeCommand(command, env);
        if (exitCode != 0) {
            boolean raiseException = true;
            if (providerOptions != null) {
                raiseException = providerOptions.raiseExceptionOnError.value();
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
            throw new JobSchedulerException(String.format("%s[getInputStream][%s][failed]%s", logPrefix, path, ex.toString()), ex);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    public OutputStream getOutputStream(final String path, boolean append, boolean resume) {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            return new SmbFileOutputStream(smbFile);
        } catch (Exception ex) {
            throw new JobSchedulerException(String.format("%s[getOutputStream][%s][failed]%s", logPrefix, path, ex.toString()), ex);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    public ISOSVirtualFile getFileHandle(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSVirtualFile file = new SOSSMBFile(fileName);
        file.setHandler(this);
        return file;
    }

    @Override
    public String getModificationDateTime(final String path) {
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
        sb.append("[").append(user).append("@").append(host).append(":").append(port).append("]");
        logPrefix = sb.toString();
    }

}