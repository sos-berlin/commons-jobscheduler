package com.sos.vfs.smb.jcifs;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

@SuppressWarnings("deprecation")
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
        Properties properties = new Properties();
        properties.put("jcifs.smb.client.disableSMB1", "false");
        properties.put("jcifs.smb.client.enableSMB2", "true");

        BaseContext bc = new BaseContext(new PropertyConfiguration(getConfigFromFiles(properties)));
        NtlmPasswordAuthentication creds = new NtlmPasswordAuthentication(bc, domain, user, password);
        context = bc.withCredentials(creds);
    }

    @Override
    public boolean fileExists(String path) {
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            boolean result = smbFile.exists();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[fileExists][%s]%s", getLogPrefix(), path, result));
            }
            return result;
        } catch (Exception e) {
            path = smbFile == null ? path : smbFile.getCanonicalPath();
            throw new JobSchedulerException(SOSVfs_E_226.params(path), e);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
    }

    @Override
    public boolean directoryExists(String path) {
        return isDirectory(path);
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[size][%s]%s", getLogPrefix(), path, size));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("%s[size][%s][failed]%s", getLogPrefix(), path, e.toString()), e);
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[isDirectory][%s]%s", getLogPrefix(), path, result));
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
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("%s[isHidden][%s]%s", getLogPrefix(), path, result));
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
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            reply = "OK";
            smbFile = getSmbFile(normalizePath(path));
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
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[mkdir][%s]%s", getLogPrefix(), path, e.toString()), e);
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
            reply = "rmdir OK";
            path = normalizePath(path);
            if (!path.endsWith("/")) {
                path += "/";
            }
            smbFile = getSmbFile(path);
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[delete]%s", getLogPrefix(), path));
            }
            smbFile.delete();
        } catch (Exception ex) {
            reply = ex.toString();
            throw new JobSchedulerException(String.format("%s[delete][%s][failed]%s", getLogPrefix(), path, ex.toString()), ex);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
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
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[rename][%s][%s][failed]%s", getLogPrefix(), from, to, e.toString()), e);
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
        SmbFile smbFile = null;
        try {
            smbFile = getSmbFile(normalizePath(path));
            return new SmbFileInputStream(smbFile);
        } catch (Exception ex) {
            throw new JobSchedulerException(String.format("%s[getInputStream][%s][failed]%s", getLogPrefix(), path, ex.toString()), ex);
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
            throw new JobSchedulerException(String.format("%s[getOutputStream][%s][failed]%s", getLogPrefix(), path, ex.toString()), ex);
        } finally {
            if (smbFile != null) {
                smbFile.close();
            }
        }
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

    @Override
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
            throw new JobSchedulerException(String.format("%s[getSmbFile][%s][failed]%s", getLogPrefix(), path, ex.toString()), ex);
        }
    }
}