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

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbSession;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsJCIFS extends SOSVfsTransferBaseClass {

    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(this.getClass());
    private static final int DEFAULT_PORT = 445;
    private NtlmPasswordAuthentication authentication = null;
    private boolean isConnected = false;
    private String domain = null;
    private String currentDirectory = "";
    private boolean simulateShell = false;

    public SOSVfsJCIFS() {
        super();
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

        int port = connection2OptionsAlternate.port.isDirty() ? connection2OptionsAlternate.port.value() : DEFAULT_PORT;
        this.connect(connection2OptionsAlternate.host.Value(), port);
        return this;
    }

    @Override
    public ISOSConnection Authenticate(final ISOSAuthenticationOptions options) {
        authenticationOptions = options;

        try {
            domain = connection2OptionsAlternate.domain.Value();

            this.doAuthenticate(authenticationOptions);
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
        return this;
    }

    @Override
    public void login(String user, final String password) {

        isConnected = false;
        try {
            userName = user;

            logger.debug(SOSVfs_D_132.params(userName));

            smbLogin(domain, host, port, userName, password);

            isConnected = true;

            reply = "OK";
            logger.info(SOSVfs_D_133.params(userName));
            this.LogReply();
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_134.params("authentication"));
        }
    }

    @Override
    public void disconnect() {
        reply = "disconnect OK";

        isConnected = false;
        this.logINFO(reply);
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void mkdir(final String path) {
        try {
            SOSOptionFolderName folderName = new SOSOptionFolderName(path);
            reply = "mkdir OK";
            logger.debug(HostID(SOSVfs_D_179.params("mkdir", path)));
            for (String subFolder : folderName.getSubFolderArray()) {
                subFolder = this.normalizePath(subFolder);
                logger.debug(HostID(SOSVfs_D_179.params("mkdir", subFolder)));
                if (this.fileExists(subFolder) == false) {
                    SmbFile f = getSmbFile(subFolder);
                    f.mkdir();
                    logger.debug(HostID(SOSVfs_D_181.params("mkdir", subFolder, getReplyString())));
                } else {
                    if (this.isDirectory(subFolder) == false) {
                        RaiseException(SOSVfs_E_277.params(subFolder));
                    }
                }
            }
            logINFO(HostID(SOSVfs_D_181.params("mkdir", path, getReplyString())));
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_134.params("[mkdir]"));
        }
    }

    @Override
    public void rmdir(String path) {
        try {
            reply = "rmdir OK";

            path = this.normalizePath(path);
            if (!path.endsWith("/")) {
                path += "/";
            }
            SmbFile f = getSmbFile(path);

            if (!f.exists()) {
                throw new JobSchedulerException(String.format("[rmdir] failed. Filepath '%1$s' does not exist.", f.getPath()));
            }
            if (!f.isDirectory()) {
                throw new JobSchedulerException(String.format("[rmdir] failed.  Filepath '%1$s' is not a directory.", f.getPath()));
            }
            f.delete();

            reply = "rmdir OK";
            logger.info(HostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_134.params("[rmdir] " + path), e);
        }
    }

    @Override
    public boolean isDirectory(final String path) {
        SmbFile f = null;

        try {
            f = getSmbFile(this.normalizePath(path));
            return f.isDirectory();
        } catch (Exception e) {
        }

        return false;
    }

    public boolean isHidden(final String path) {
        SmbFile f = null;

        try {
            f = getSmbFile(this.normalizePath(path));
            return f.isHidden();
        } catch (Exception e) {
        }

        return false;
    }

    @Override
    public String[] listNames(String path) throws IOException {

        SmbFile f = null;
        try {
            if (path.length() == 0) {
                path = ".";
            }
            if (!this.fileExists(path)) {
                throw new JobSchedulerException(SOSVfs_E_226.params(path));
            }

            if (!this.isDirectory(path)) {
                reply = "ls OK";
                return new String[] { path };
            }

            f = getSmbFile(this.normalizePath(path));
            String sep = path.endsWith("/") ? "" : "/";

            SmbFile[] lsResult = f.listFiles();
            String[] result = new String[lsResult.length];
            for (int i = 0; i < lsResult.length; i++) {
                SmbFile entry = lsResult[i];

                String fileName = path + sep + entry.getName();
                result[i] = fileName;
            }
            reply = "ls OK";
            return result;
        } catch (JobSchedulerException e) {
            reply = e.toString();
            throw e;
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public long size(final String path) throws Exception {
        long size = -1;
        SmbFile f = null;
        try {
            f = getSmbFile(this.normalizePath(path));
            if (f.exists()) {
                size = f.length();
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfs_E_161.params("checking size", e));
        }

        return size;
    }

    @Override
    public long getFile(final String remoteFile, final String localFile, final boolean append) {
        File transferFile = null;
        long remoteFileSize = -1;
        FileOutputStream fos = null;
        SmbFile f = null;
        SmbFileInputStream in = null;
        try {
            remoteFileSize = this.size(this.normalizePath(remoteFile));

            f = getSmbFile(this.normalizePath(remoteFile));
            in = new SmbFileInputStream(f);
            fos = new FileOutputStream(localFile, append);

            byte[] b = new byte[8192];
            int n, tot = 0;
            while ((n = in.read(b)) > 0) {
                fos.write(b, 0, n);
                tot += n;
            }

            in.close();
            in = null;

            fos.flush();
            fos.close();
            fos = null;

            transferFile = new File(localFile);

            if (!append) {
                if (remoteFileSize > 0 && remoteFileSize != transferFile.length()) {
                    throw new JobSchedulerException(SOSVfs_E_162.params(remoteFileSize, transferFile.length()));
                }
            }

            remoteFileSize = transferFile.length();
            reply = "get OK";
            logINFO(HostID(SOSVfs_I_182.params("getFile", this.normalizePath(remoteFile), localFile, getReplyString())));
        } catch (Exception ex) {
            reply = ex.toString();
            RaiseException(ex, SOSVfs_E_184.params("getFile", this.normalizePath(remoteFile), localFile));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ex) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex) {
                }
            }
        }

        return remoteFileSize;

    }

    @Override
    public long putFile(final String localFilePath, final String remoteFilePath) {
        long size = 0;

        SmbFile remoteFile = null;
        SmbFileOutputStream out = null;
        FileInputStream in = null;

        try {
            remoteFile = getSmbFile(this.normalizePath(remoteFilePath));

            in = new FileInputStream(localFilePath);
            out = new SmbFileOutputStream(remoteFile);

            byte[] b = new byte[8192];
            int n, tot = 0;
            while ((n = in.read(b)) > 0) {
                out.write(b, 0, n);
                tot += n;
            }

            reply = "put OK";
            logINFO(HostID(SOSVfs_I_183.params("putFile", localFilePath, remoteFile.getPath(), getReplyString())));
            size = this.size(this.normalizePath(remoteFilePath));
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_185.params("putFile()", localFilePath, remoteFilePath));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }

        return size;
    }

    @Override
    public void delete(final String path) {
        try {
            if (this.isDirectory(this.normalizePath(path))) {
                throw new JobSchedulerException(SOSVfs_E_186.params(path));
            }

            SmbFile f = getSmbFile(this.normalizePath(path));
            f.delete();
        } catch (Exception ex) {
            reply = ex.toString();
            RaiseException(ex, SOSVfs_E_187.params("delete", path));
        }

        reply = "rm OK";
        logINFO(HostID(SOSVfs_D_181.params("delete", path, getReplyString())));
    }

    @Override
    public void rename(final String from, final String to) {
        SmbFile fromF = null;
        SmbFile toF = null;
        try {
            fromF = getSmbFile(this.normalizePath(from));
            toF = getSmbFile(this.normalizePath(to));

            fromF.renameTo(toF);
        } catch (Exception e) {
            reply = e.toString();
            throw new JobSchedulerException(SOSVfs_E_188.params("rename", from, to), e);
        }

        reply = "mv OK";
        logger.info(HostID(SOSVfs_I_189.params(from, to, getReplyString())));
    }

    @Override
    public void ExecuteCommand(final String cmd) {
        logger.debug("not implemented yet");
    }

    @Override
    public InputStream getInputStream(final String path) {

        SmbFile f = null;
        try {
            f = getSmbFile(this.normalizePath(path));
            return new SmbFileInputStream(f);
        } catch (Exception ex) {
            RaiseException(ex, SOSVfs_E_193.params("getInputStream()", path));
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(final String path) {
        SmbFile f = null;
        try {
            f = getSmbFile(this.normalizePath(path));
            return new SmbFileOutputStream(f);
        } catch (Exception ex) {
            RaiseException(ex, SOSVfs_E_193.params("getOutputStream()", path));
            return null;
        }
    }

    @Override
    public boolean changeWorkingDirectory(final String path) {
        SmbFile f = null;
        try {
            f = getSmbFile(this.normalizePath(path));

            if (!f.exists()) {
                reply = String.format("Filepath '%1$s' does not exist.", f.getPath());
                return false;
            }
            if (!f.isDirectory()) {
                reply = String.format("Filepath '%1$s' is not a directory.", f.getPath());
                return false;
            }

            currentDirectory = f.getPath();
            reply = "cwd OK";
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_193.params("cwd", path), ex);
        } finally {
            logger.debug(SOSVfs_D_194.params(path, getReplyString()));
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
        SmbFile f = null;
        String dateTime = null;
        try {
            f = getSmbFile(this.normalizePath(path));
            if (f.exists()) {
                long lm = f.getLastModified();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateTime = df.format(new Date(lm));
            }
        } catch (Exception ex) {

        }
        return dateTime;
    }

    @Override
    protected boolean fileExists(String filename) {

        SmbFile file = null;
        try {
            file = getSmbFile(this.normalizePath(filename));
            return file.exists();
        } catch (Exception e) {
            filename = file == null ? filename : file.getCanonicalPath();
            throw new JobSchedulerException(SOSVfs_E_226.params(filename), e);
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
            return new SmbFile(getSmbFilePath(path), authentication);
        } catch (Exception ex) {
            throw new JobSchedulerException("cannot get SmbFile: " + path);
        }
    }

    private void smbLogin(String domain, String host, int port, String user, String password) throws Exception {
        UniAddress hostAddress = UniAddress.getByName(host);
        authentication = new NtlmPasswordAuthentication(domain, user, password);
        SmbSession.logon(hostAddress, port, authentication);
    }

    private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions options) throws Exception {

        authenticationOptions = options;
        isConnected = false;

        userName = authenticationOptions.getUser().Value();
        String password = authenticationOptions.getPassword().Value();
        logger.debug(SOSVfs_D_132.params(userName));

        try {
            smbLogin(domain, host, port, userName, password);

            isConnected = true;
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }

        reply = "OK";
        logger.info(SOSVfs_D_133.params(userName));

        this.LogReply();

        return this;
    }

    private void connect(final String phost, final int pport) {

        host = phost;
        port = pport;

        logger.info(SOSVfs_D_0101.params(host, port));

        if (this.isConnected() == false) {

            this.LogReply();
        } else {
            logWARN(SOSVfs_D_0103.params(host, port));
        }
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
        return this.simulateShell;
    }

    @Override
    public void setSimulateShell(boolean simulateShell) {
        this.simulateShell = simulateShell;
    }
}
