package com.sos.vfs.smb.smbj;

import static com.hierynomus.msfscc.FileAttributes.FILE_ATTRIBUTE_DIRECTORY;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.mserref.NtStatus;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2Dialect;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.SmbConfig.Builder;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.hierynomus.smbj.utils.SmbFiles;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.vfs.common.SOSCommonProvider;
import com.sos.vfs.common.SOSFileEntry;
import com.sos.vfs.common.SOSFileEntry.EntryType;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.vfs.smb.common.ASOSSMB;
import com.sos.vfs.smb.common.ISOSSMB;

import sos.util.SOSDate;
import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSMBJ extends ASOSSMB implements ISOSSMB {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSMBJ.class);

    private SMBClient client = null;
    private Connection connection = null;
    private Session session = null;
    private DiskShare diskShare;
    private String shareName;
    boolean accessMaskMaximumAllowed = false;

    public SOSSMBJ() {
        super();
    }

    @Override
    public void doConnect() throws Exception {
        try {
            createClient();
            connection = client.connect(host, port);
            session = connection.authenticate(getAuthenticationContext());
        } catch (Throwable ex) {
            throw new JobSchedulerException(getLogPrefix(), ex);
        }
    }

    @Override
    public void doDisconnect() throws Exception {
        try {
            if (diskShare != null) {
                diskShare.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close(true);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            close(diskShare);
            close(session);
            close(connection);
            close(client);

            diskShare = null;
            session = null;
            connection = null;
            client = null;
        }
    }

    @Override
    public boolean fileExists(String path) {
        tryConnectShare("fileExists", path);

        boolean result = diskShare.fileExists(getSmbPath(path));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]fileExists=%s", path, result));
        }
        return result;
    }

    @Override
    public boolean directoryExists(String path) {
        return isDirectory(path);
    }

    @Override
    public long size(final String path) throws Exception {
        tryConnectShare("size", path);

        long size = -1;
        try (File f = openFile2Read(path)) {
            if (f != null) {
                size = getSize(f.getFileInformation());
            }
        } catch (SMBApiException e) {
            switch (NtStatus.valueOf(e.getStatusCode())) {
            case STATUS_OBJECT_NAME_NOT_FOUND:
                break;
            default:
                throw e;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]size=%s", path, size));
        }
        return size;
    }

    @Override
    public boolean isDirectory(final String path) {
        tryConnectShare("isDirectory", path);

        try {
            boolean r = diskShare.getFileInformation(getSmbPath(path)).getStandardInformation().isDirectory();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[isDirectory][%s]%s", getLogPrefix(), path, r));
            }
            return r;
        } catch (Throwable e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[isDirectory][%s][exception]%s", getLogPrefix(), path, e.toString()));
            }
            return false;
        }
    }

    @Override
    public void mkdir(final String path) {
        try {
            tryConnectShare("mkdir", path);

            new SmbFiles().mkdirs(diskShare, getSmbPath(path));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("%s[mkdir][%s]created", getLogPrefix(), path));
            }
            reply = "OK";
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
        try {
            tryConnectShare("rmdir", path);

            diskShare.rmdir(getSmbPath(path), true);
            reply = "rmdir OK";
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
    public void delete(final String path, boolean checkIsDirectory) {
        try {
            tryConnectShare("delete", path);

            diskShare.rm(getSmbPath(path));
            reply = "rm OK";
            LOGGER.info(String.format("%s[rm][%s]%s", getLogPrefix(), path, getReplyString()));
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[rm][%s]%s", getLogPrefix(), path, reply), e);
        }
    }

    @Override
    public void rename(final String from, final String to) {
        try {
            tryConnectShare("rename", from);

            try (DiskEntry f = openExistingFile4Rename(from)) {
                f.rename(getSmbPath(to));
            }
            reply = "rename OK";
            LOGGER.info(String.format("%s[rename][%s][%s]%s", getLogPrefix(), from, to, getReplyString()));
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Throwable e) {
            reply = e.toString();
            throw new JobSchedulerException(String.format("%s[rename][%s][%s]%s", getLogPrefix(), from, to, reply), e);
        }
    }

    @Override
    public SOSFileEntry getFileEntry(String path) throws Exception {
        tryConnectShare("getFileEntry", path);

        try (File f = openFile2Read(path)) {
            if (f != null) {
                return getFileEntry(f.getFileInformation(), getParent(path));
            }
        }
        return null;
    }

    private SOSFileEntry getFileEntry(FileAllInformation fi, String parentPath) throws Exception {
        SOSFileEntry entry = new SOSFileEntry(EntryType.SMB);
        entry.setDirectory(fi.getStandardInformation().isDirectory());
        entry.setFilename(SOSCommonProvider.getBaseNameFromPath(normalizePath(fi.getNameInformation())));
        entry.setFilesize(getSize(fi));
        entry.setParentPath(parentPath);
        return entry;
    }

    private SOSFileEntry getFileEntry(FileIdBothDirectoryInformation fi, String parentPath) throws Exception {
        SOSFileEntry entry = new SOSFileEntry(EntryType.SMB);
        entry.setDirectory(EnumWithValue.EnumUtils.isSet(fi.getFileAttributes(), FILE_ATTRIBUTE_DIRECTORY));
        entry.setFilename(fi.getFileName());
        entry.setFilesize(getSize(fi));
        entry.setParentPath(parentPath);
        return entry;
    }

    @Override
    public List<SOSFileEntry> listNames(String path, int maxFiles, boolean checkIfExists, boolean checkIfIsDirectory) {
        tryConnectShare("listNames", path);

        try {
            List<SOSFileEntry> result = new ArrayList<SOSFileEntry>();
            if (checkIfExists && !directoryExists(path)) {
                return result;
            }
            if (checkIfIsDirectory && !isDirectory(path)) {
                reply = "ls OK";
                return result;
            }
            List<FileIdBothDirectoryInformation> list = diskShare.list(getSmbPath(path));
            for (FileIdBothDirectoryInformation fin : list) {
                if (fin.getFileName() == null || fin.getFileName().equals(".") || fin.getFileName().equals("..")) {
                    continue;
                }
                result.add(getFileEntry(fin, path));
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[%s][listNames]%s files or folders", path, result.size()));
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
    public InputStream getInputStream(final String path) {
        return ((SOSSMBJFile) getFile(path)).getFileInputStream();
    }

    @Override
    public OutputStream getOutputStream(final String path, boolean append, boolean resume) {
        return ((SOSSMBJFile) getFile(path)).getFileOutputStream();
    }

    @Override
    public String getModificationDateTime(final String path) {
        String r = null;
        try {
            tryConnectShare("getModificationDateTime", path);
            try (File f = openFile2Read(path)) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                r = df.format(f.getFileInformation().getBasicInformation().getLastWriteTime().toDate());
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
        try {
            tryConnectShare("getModificationTimeStamp", path);

            try (File f = openFile2Read(path)) {
                r = f.getFileInformation().getBasicInformation().getLastWriteTime().toEpochMillis();
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("%s[getModificationTimeStamp][%s]%s", getLogPrefix(), path, e.toString()), e);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]getModificationTimeStamp=%s", path, r));
        }
        return r;
    }

    public void setModificationTimeStamp(final String path, final long millis) throws Exception {
        if (millis <= 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][skip]setModificationTimeStamp=%s", path, millis));
            }
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s]setModificationTimeStamp=%s", path, millis));
        }
        try {
            tryConnectShare("setModificationTimeStamp", path);
            try (File f = openExistingFile2Write(path)) {
                FileBasicInformation current = f.getFileInformation().getBasicInformation();
                FileTime ft = FileTime.ofEpochMillis(millis);
                f.setFileInformation(new FileBasicInformation(current.getCreationTime(), ft, ft, ft, current.getFileAttributes()));
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("%s[setModificationTimeStamp][%s][%s]%s", getLogPrefix(), path, millis, e.toString()), e);
        }
    }

    @Override
    public ISOSProviderFile getFile(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSProviderFile file = new SOSSMBJFile(fileName);
        file.setProvider(this);
        return file;
    }

    // FILE_OPEN - If the file already exists, return success; otherwise, fail the operation.
    protected File openFile2Read(String path) {
        return openFile(path, AccessMask.GENERIC_READ, SMB2CreateDisposition.FILE_OPEN);
    }

    private File openExistingFile2Write(String path) {
        return openFile(path, AccessMask.GENERIC_WRITE, SMB2CreateDisposition.FILE_OPEN);
    }

    // FILE_OPEN_IF - Open the file if it already exists; otherwise, create the file.
    // FILE_OVERWRITE_IF - Overwrite the file if it already exists; otherwise, create the file.
    protected File openFile2Write(String path, boolean append) {
        tryConnectShare("openFile2Write", path);

        SMB2CreateDisposition cd = append ? SMB2CreateDisposition.FILE_OPEN_IF : SMB2CreateDisposition.FILE_OVERWRITE_IF;
        return openFile(path, AccessMask.GENERIC_WRITE, cd);
    }

    private DiskEntry openExistingFile4Rename(String path) {
        Set<AccessMask> ams = new HashSet<>();
        if (accessMaskMaximumAllowed) {
            ams.add(AccessMask.MAXIMUM_ALLOWED);
        } else {
            ams.add(AccessMask.FILE_WRITE_ATTRIBUTES);
            ams.add(AccessMask.DELETE);
        }

        Set<FileAttributes> fa = new HashSet<>();
        fa.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);

        Set<SMB2ShareAccess> sa = new HashSet<>();
        sa.addAll(SMB2ShareAccess.ALL);

        Set<SMB2CreateOptions> co = new HashSet<>();
        co.add(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE);

        return diskShare.open(getSmbPath(path), ams, fa, sa, SMB2CreateDisposition.FILE_OPEN, co);
    }

    private File openFile(String path, AccessMask accessMask, SMB2CreateDisposition createDisposition) {
        Set<AccessMask> ams = new HashSet<>();
        if (accessMaskMaximumAllowed) {
            ams.add(AccessMask.MAXIMUM_ALLOWED);
        } else {
            ams.add(accessMask);
        }

        Set<SMB2ShareAccess> sa = new HashSet<>();
        sa.addAll(SMB2ShareAccess.ALL);

        Set<SMB2CreateOptions> co = new HashSet<>();
        co.add(SMB2CreateOptions.FILE_WRITE_THROUGH);

        return diskShare.openFile(getSmbPath(path), ams, null, sa, createDisposition, co);
    }

    private void tryConnectShare(String caller, String path) {
        try {
            if (diskShare == null || !diskShare.isConnected()) {
                diskShare = (DiskShare) session.connectShare(getShareName(path));
            }
        } catch (Throwable e) {
            String msg = "[" + caller + "][tryConnectShare][" + path + "]" + e.toString();
            LOGGER.error(msg, e);
            throw new JobSchedulerException(msg, e);
        }
    }

    private String getShareName(String path) {
        if (shareName == null) {
            if (path == null) {
                shareName = "";
            } else {
                // path=/sos/yade/test/a/b/c
                String p = normalizePath(path);
                p = p.startsWith("/") ? p.substring(1) : p;
                String[] arr = p.split("/");
                // shareName=sos
                shareName = arr.length == 0 ? "" : arr[0];
            }
        }
        return shareName;
    }

    private String getSmbPath(String path) {
        // path=/sos/yade/test/a/b/c/ <-- sos is the share name
        String p = normalizePath(path);
        p = p.startsWith("/") ? p.substring(1) : p;
        int snl = getShareName(path).length();
        if (snl > 0 && p.length() > snl) {
            p = p.substring(snl + 1);
        }
        // smbPath=yade/test/a/b/c
        p = p.endsWith("/") ? p.substring(0, p.length() - 1) : p;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[getSmbPath][path=%s]smbPath=%s", path, p));
        }
        return p;
    }

    private void createClient() throws Exception {
        Builder b = SmbConfig.builder();
        Properties p = setConfigFromFiles(b);
        setConnectTimeout(b);

        try {
            SmbConfig config = b.build();
            if (LOGGER.isDebugEnabled()) {
                List<String> excluded = Arrays.asList(
                        "authenticators;socketFactory;random;securityProvider;transportLayerFactory;clientGSSContextConfig;ntlmConfig".split(";"));
                LOGGER.debug(String.format("%s[createClient][config]%s", getLogPrefix(), SOSString.toString(config, excluded)));
            }

            client = new SMBClient(config);
        } catch (Throwable e) {
            if (p.size() > 0) {
                LOGGER.info(String.format("%s[createClient][config]%s", getLogPrefix(), p));
            }
            throw e;
        }
    }

    private AuthenticationContext getAuthenticationContext() {
        // TODO - currently only NTLM
        char[] p = SOSString.isEmpty(getProviderOptions().password.getValue()) ? new char[0] : getProviderOptions().password.getValue().toCharArray();
        return new AuthenticationContext(user, p, getDomain());
    }

    @SuppressWarnings("deprecation")
    private Properties setConfigFromFiles(Builder b) {
        Properties p = getConfigFromFiles(new Properties());
        if (p.size() > 0) {
            p.entrySet().forEach(e -> {
                String key = e.getKey().toString().trim();
                String val = e.getValue().toString().trim();
                try {
                    long t;
                    switch (key) {
                    case "workStationName": // Default: null
                        b.withWorkStationName(val);
                        break;

                    case "soTimeout":// Default: 0
                        t = SOSDate.resolveAge("ms", val).longValue();
                        b.withSoTimeout(t, TimeUnit.MILLISECONDS);
                        break;

                    case "timeout":// Default: 60s, sets readTimeout, transactTimeout, writeTimeout
                        t = SOSDate.resolveAge("ms", val).longValue();
                        b.withTimeout(t, TimeUnit.MILLISECONDS);
                        break;
                    case "readTimeout":// Default: see timeout
                        t = SOSDate.resolveAge("ms", val).longValue();
                        b.withReadTimeout(t, TimeUnit.MILLISECONDS);
                        break;
                    case "transactTimeout":// Default: see timeout
                        t = SOSDate.resolveAge("ms", val).longValue();
                        b.withTransactTimeout(t, TimeUnit.MILLISECONDS);
                        break;
                    case "writeTimeout":// Default: see timeout
                        t = SOSDate.resolveAge("ms", val).longValue();
                        b.withWriteTimeout(t, TimeUnit.MILLISECONDS);
                        break;

                    case "bufferSize":// Default: 1048576(1024 * 1024),sets readBufferSize, transactBufferSize, writeBufferSize
                        b.withBufferSize(Integer.parseInt(val));
                        break;
                    case "readBufferSize":// Default: see bufferSize
                        b.withReadBufferSize(Integer.parseInt(val));
                        break;
                    case "transactBufferSize":// Default: see bufferSize
                        b.withTransactBufferSize(Integer.parseInt(val));
                        break;
                    case "writeBufferSize":// Default: see bufferSize
                        b.withWriteBufferSize(Integer.parseInt(val));
                        break;

                    case "dialects": // All: UNKNOWN; SMB_2_0_2; SMB_2_1; SMB_2XX; SMB_3_0; SMB_3_0_2; SMB_3_1_1
                        // Default: SMB_3_1_1; SMB_3_0_2; SMB_3_0; SMB_2_1; SMB_2_0_2
                        List<SMB2Dialect> l = Arrays.stream(val.split(";")).map(d -> SMB2Dialect.valueOf(d.trim())).collect(Collectors.toList());
                        b.withDialects(l);
                        break;

                    case "signingRequired": // Default: false
                        b.withSigningRequired(Boolean.parseBoolean(val));
                        break;
                    case "dfsEnabled":// Default: false
                        b.withDfsEnabled(Boolean.parseBoolean(val));
                        break;
                    case "multiProtocolNegotiate":// Default: false
                        b.withMultiProtocolNegotiate(Boolean.parseBoolean(val));
                        break;
                    case "encryptData":// Default: false
                        b.withEncryptData(Boolean.parseBoolean(val));
                        break;
                    case "sossmbj.accessMaskMaximumAllowed":
                        accessMaskMaximumAllowed = Boolean.parseBoolean(val);
                        break;
                    }
                } catch (Throwable te) {
                    LOGGER.warn(String.format("[setConfigFromFiles][%s=%s]%s", key, val, te.toString()), te);
                }
            });

        }
        return p;
    }

    private void setConnectTimeout(Builder b) {
        String ct = getProviderOptions().connect_timeout.getValue();
        if (!SOSString.isEmpty(ct)) {
            try {
                long t = SOSDate.resolveAge("ms", ct).longValue();
                if (t > 0) {
                    b.withSoTimeout(t, TimeUnit.MILLISECONDS);
                }
            } catch (Exception ex) {
                LOGGER.warn(String.format("%s[setConnectTimeout][%s]%s", getLogPrefix(), ct, ex.toString()), ex);
            }

        }
    }

    private void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable e) {
            }
        }
    }

    private void close(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable e) {
            }
        }
    }

    private long getSize(FileAllInformation fi) {
        if (fi == null) {
            return -1L;
        }
        return fi.getStandardInformation().getEndOfFile();
    }

    private long getSize(FileIdBothDirectoryInformation fi) {
        if (fi == null) {
            return -1L;
        }
        return fi.getEndOfFile();
    }
}