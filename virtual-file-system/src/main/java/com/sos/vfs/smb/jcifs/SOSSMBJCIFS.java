package com.sos.vfs.smb.jcifs;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.smb.common.ASOSSMB;
import com.sos.vfs.smb.common.ISOSSMB;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.DialectVersion;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSMBJCIFS extends ASOSSMB implements ISOSSMB {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSMBJCIFS.class);

    private CIFSContext context = null;

    public SOSSMBJCIFS() {
        super();
    }

    @Override
    public void doConnect() throws Exception {
        try {
            createContext(getDomain(), host, port, user, getProviderOptions().password.getValue());
        } catch (Throwable ex) {
            throw new JobSchedulerException(ex);
        }
        checkConnection();
    }

    private void checkConnection() throws Exception {
        try {
            getFileEntry("/");
        } catch (Throwable e) {
            Exception ex = findConnectionException(e);
            if (ex != null) {
                throw e;
            }
        }
    }

    private static Exception findConnectionException(Throwable cause) {
        Throwable e = cause;
        while (e != null) {
            if (e instanceof java.net.UnknownHostException) {
                return (java.net.UnknownHostException) e;
            } else if (e instanceof java.net.ConnectException) {
                return (java.net.ConnectException) e;
            }
            e = e.getCause();
        }
        return null;
    }

    @Override
    public void doDisconnect() {
        if (context != null) {
            try {
                context.close();
            } catch (CIFSException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("%s[disconnect]%s", getLogPrefix(), e.toString()), e);
                }
            }
            context = null;
        }
    }

    private void createContext(String domain, String host, int port, String user, String password) throws Exception {
        Properties p = new Properties();
        // p.put("jcifs.smb.client.disableSMB1", "false");
        // p.put("jcifs.smb.client.enableSMB2", "true");
        p.put("jcifs.smb.client.minVersion", DialectVersion.SMB202.name());
        p.put("jcifs.smb.client.maxVersion", DialectVersion.SMB311.name());

        PropertyConfiguration pc = new PropertyConfiguration(getConfigFromFiles(p));
        BaseContext bc = new BaseContext(pc);
        if (LOGGER.isDebugEnabled()) {
            List<String> excluded = Arrays.asList("minVersion;maxVersion;localTimeZone;random;machineId".split(";"));
            LOGGER.debug(String.format("%s[createContext][config][minVersion=%s,maxVersion=%s]%s", getLogPrefix(), pc.getMinimumVersion(), pc
                    .getMaximumVersion(), SOSString.toString(pc, excluded)));
        }
        context = bc.withCredentials(new NtlmPasswordAuthenticator(domain, user, password));
    }

    @Override
    public boolean fileExists(String path) {
        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            boolean result = smbFile.exists();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[fileExists][%s]%s", getLogPrefix(), path, result));
            }
            return result;
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Throwable e) {
            throw new JobSchedulerException(SOSVfs_E_226.params(path), e);
        }
    }

    @Override
    public boolean directoryExists(String path) {
        return isDirectory(path);
    }

    @Override
    public long size(final String path) throws Exception {
        long size = -1;
        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            if (smbFile.exists()) {
                size = smbFile.length();
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[size][%s]%s", getLogPrefix(), path, size));
            }
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Throwable e) {
            throw new JobSchedulerException(String.format("%s[size][%s][failed]%s", getLogPrefix(), path, e.toString()), e);
        }
        return size;
    }

    @Override
    public boolean isDirectory(final String path) {
        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            boolean r = smbFile.isDirectory();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[isDirectory][%s]%s", getLogPrefix(), path, r));
            }
            return r;
        } catch (Throwable e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[isDirectory][%s][exception]%s", getLogPrefix(), path, e.toString()));
            }
        }
        return false;
    }

    public boolean isHidden(final String path) {
        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            boolean result = smbFile.isHidden();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("%s[isHidden][%s]%s", getLogPrefix(), path, result));
            }
            return result;
        } catch (Throwable e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[isHidden][%s][exception]%s", getLogPrefix(), path, e.toString()));
            }
        }
        return false;
    }

    @Override
    public void mkdir(final String path) {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        reply = "OK";

        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            if (smbFile.exists()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[mkdir][%s]already exists", getLogPrefix(), path));
                }
                return;
            }
            smbFile.mkdirs();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[mkdir][%s]created", getLogPrefix(), path));
            }
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[mkdir][%s]%s", getLogPrefix(), path, reply), e);
        }
    }

    @Override
    public void rmdir(String path) {
        reply = "rmdir OK";
        path = normalizePath(path);
        if (!path.endsWith("/")) {
            path += "/";
        }

        try (SmbFile smbFile = getSmbFile(path)) {
            if (!smbFile.exists()) {
                throw new JobSchedulerException(String.format("%s[rmdir][%s][failed]filepath does not exist", getLogPrefix(), smbFile.getPath()));
            }
            if (!smbFile.isDirectory()) {
                throw new JobSchedulerException(String.format("%s[rmdir][%s][failed]filepath is not a directory", getLogPrefix(), smbFile.getPath()));
            }
            smbFile.delete();
            LOGGER.info(String.format("%s[rmdir][%s]%s", getLogPrefix(), path, getReplyString()));
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[rmdir][%s]%s", getLogPrefix(), path, reply), e);
        }
    }

    @Override
    public SOSFileEntry getFileEntry(String path) throws Exception {
        SmbFile file = getSmbFile(normalizePath(path));
        if (file == null) {
            return null;
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[%s]found", path));
        }
        SOSFileEntry entry = getFileEntry(file, getParent(path));
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
    public List<SOSFileEntry> listNames(String path, int maxFiles, boolean checkIfExists, boolean checkIfIsDirectory) {
        List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
        if (path.isEmpty()) {
            path = ".";
        }

        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
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
                LOGGER.trace(String.format("[%s][listNames]%s files or folders", path, list.length));
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
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(reply, e);
        }
    }

    @Override
    public void delete(final String path, boolean checkIsDirectory) {
        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            if (checkIsDirectory && smbFile.isDirectory()) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[delete]%s", getLogPrefix(), path));
            }
            smbFile.delete();
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[rm][%s]%s", getLogPrefix(), path, reply), e);
        }
        reply = "rm OK";
        LOGGER.info(String.format("%s[delete][%s]%s", getLogPrefix(), path, getReplyString()));
    }

    @Override
    public void rename(final String from, final String to) {
        SmbFile smbFileFrom = null;
        SmbFile smbFileTo = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[rename][%s]%s", getLogPrefix(), from, to));
            }
            smbFileFrom = getSmbFile(normalizePath(from));
            smbFileTo = getSmbFile(normalizePath(to));
            smbFileFrom.renameTo(smbFileTo);
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[rename][%s][%s]%s", getLogPrefix(), from, to, reply), e);
        } finally {
            if (smbFileFrom != null) {
                smbFileFrom.close();
            }
            if (smbFileTo != null) {
                smbFileTo.close();
            }
        }
        reply = "OK";
        LOGGER.info(String.format("%s[rename][%s][%s]%s", getLogPrefix(), from, to, getReplyString()));
    }

    @Override
    public InputStream getInputStream(final String path) {
        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            return new SmbFileInputStream(smbFile);
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Throwable ex) {
            throw new JobSchedulerException(String.format("%s[getInputStream][%s][failed]%s", getLogPrefix(), path, ex.toString()), ex);
        }
    }

    @Override
    public OutputStream getOutputStream(final String path, boolean append, boolean resume) {
        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            return new SmbFileOutputStream(smbFile);
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Throwable ex) {
            throw new JobSchedulerException(String.format("%s[getOutputStream][%s][failed]%s", getLogPrefix(), path, ex.toString()), ex);
        }
    }

    @Override
    public String getModificationDateTime(final String path) {
        String r = null;
        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            if (smbFile.exists()) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                r = df.format(new Date(smbFile.getLastModified()));
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("%s[getModificationDateTime][%s]%s", getLogPrefix(), path, e.toString()), e);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s[%s]getModificationDateTime=%s", getLogPrefix(), path, r));
        }
        return r;
    }

    @Override
    public long getModificationTimeStamp(final String path) throws Exception {
        long r = -1L;
        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            if (smbFile.exists()) {
                r = smbFile.getLastModified();
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("%s[getModificationTimeStamp][%s]%s", getLogPrefix(), path, e.toString()), e);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]getModificationTimeStamp=%s", path, r));
        }
        return r;
    }

    public void setModificationTimeStamp(final String path, final long timeStamp) throws Exception {
        if (timeStamp <= 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][skip]setModificationTimeStamp=%s", path, timeStamp));
            }
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]setModificationTimeStamp=%s", path, timeStamp));
        }

        try (SmbFile smbFile = getSmbFile(normalizePath(path))) {
            if (smbFile.exists()) {
                smbFile.setLastModified(timeStamp);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("%s[setModificationTimeStamp][%s]%s", getLogPrefix(), path, e.toString()), e);
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
            throw new JobSchedulerException(String.format("%s[getSmbFile][%s][failed]%s", getLogPrefix(), path, ex.toString()), ex);
        }
    }
}