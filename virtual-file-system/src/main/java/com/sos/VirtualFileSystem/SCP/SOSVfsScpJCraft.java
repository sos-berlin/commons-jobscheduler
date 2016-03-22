package com.sos.VirtualFileSystem.SCP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.omg.SendingContext.RunTime;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsSuperClass;
import com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpFileJCraft;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @ressources jsch-0.1.48.jar
 *
 * @author Robert Ehrlich
 *
 *         http://kickjava.com/src/org.apache.tools.ant.taskdefs.optional.ssh.
 *         index.htm */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsScpJCraft extends SOSVfsTransferBaseClass {

    public static final String conConfigurationSTRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    private final Logger logger = Logger.getLogger(SOSVfsScpJCraft.class);
    /** ssh connection object */
    private Channel sshConnection = null;
    /** ssh session object */
    private Session sshSession = null;
    /** SFTP Client **/
    private ChannelSftp sftpClient = null;
    private JSch secureChannel = null;
    private boolean simulateShell = false;

    /** \brief SOSVfsSFtpJCraft
     *
     * \details */
    public SOSVfsScpJCraft() {
        super();
        secureChannel = new JSch();
    }

    @Override
    public void StrictHostKeyChecking(final String pstrStrictHostKeyCheckingValue) {
        JSch.setConfig(conConfigurationSTRICT_HOST_KEY_CHECKING, pstrStrictHostKeyCheckingValue);
        // '
        // sshConnection.StrictHostKeyChecking(pstrStrictHostKeyCheckingValue);
    }

    /** \brief Connect
     *
     * \details
     *
     * \return
     *
     * @return */
    @Override
    public ISOSConnection Connect() {
        SOSConnection2OptionsAlternate pConnection2OptionsAlternate = null;
        this.Connect(pConnection2OptionsAlternate);
        return this;
    }

    /** \brief Connect
     *
     * \details
     *
     * \return
     *
     * @param pobjConnectionOptions
     * @return */
    @Override
    public ISOSConnection Connect(final SOSConnection2OptionsAlternate pConnection2OptionsAlternate) {
        connection2OptionsAlternate = pConnection2OptionsAlternate;
        if (connection2OptionsAlternate == null) {
            RaiseException(SOSVfs_E_190.params("connection2OptionsAlternate"));
        }
        this.StrictHostKeyChecking(connection2OptionsAlternate.StrictHostKeyChecking.Value());
        this.connect(connection2OptionsAlternate.host.Value(), connection2OptionsAlternate.port.value());
        return this;
    }

    /** \brief Authenticate
     *
     * \details
     *
     * \return
     *
     * @param authenticationOptions
     * @return */
    @Override
    public ISOSConnection Authenticate(final ISOSAuthenticationOptions pAuthenticationOptions) {
        authenticationOptions = pAuthenticationOptions;
        try {
            this.doAuthenticate(authenticationOptions);
        } catch (JobSchedulerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JobSchedulerException(ex);
        }
        return this;
    }

    /** \brief login
     *
     * \details
     *
     * \return
     *
     * @param strUserName
     * @param strPassword */
    @Override
    public void login(final String pUserName, final String pPassword) {
        try {
            userName = pUserName;
            logger.debug(SOSVfs_D_132.params(userName));
            this.createSession(userName, host, port);
            sshSession.setPassword(pPassword);
            sshSession.connect();
            this.createSftpClient();
            reply = "OK";
            logger.info(SOSVfs_D_133.params(userName));
            this.LogReply();
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_134.params("authentication"));
        }
    } // private boolean login

    /** \brief disconnect
     *
     * \details
     *
     * \return */
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
    public boolean isConnected() {
        return sftpClient != null && sftpClient.isConnected();
    }

    /** Creates a new subdirectory on the FTP server in the current directory .
     * 
     * @param pstrPathName The pathname of the directory to create.
     * @exception JobSchedulerException */
    @Override
    public void mkdir(final String path) {
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(path);
            reply = "mkdir OK";
            logger.debug(HostID(SOSVfs_D_179.params("mkdir", path)));
            for (String strSubFolder : objF.getSubFolderArray()) {
                SftpATTRS attributes = getAttributes(strSubFolder);
                if (attributes == null) {
                    this.getClient().mkdir(strSubFolder);
                    // this.getClient().chmod(484, strSubFolder); //was ist
                    // chmod 484?
                } else {
                    if (attributes.isDir() == false) {
                        RaiseException(SOSVfs_E_277.params(strSubFolder));
                    }
                }
            }
            // DoCD(strCurrentDir);
            logger.debug(HostID(SOSVfs_D_181.params("mkdir", path, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_134.params("[mkdir]"));
        }
    }

    /** Removes a directory on the FTP server (if empty).
     * 
     * @param path The pathname of the directory to remove.
     * @exception JobSchedulerException */
    @Override
    public void rmdir(final String path) {
        try {
            SOSOptionFolderName objF = new SOSOptionFolderName(path);
            reply = "rmdir OK";
            for (String subfolder : objF.getSubFolderArrayReverse()) {
                String strT = subfolder + "/";
                logger.debug(HostID(SOSVfs_D_179.params("rmdir", strT)));
                this.getClient().rmdir(strT);
                reply = "rmdir OK";
                logger.debug(HostID(SOSVfs_D_181.params("rmdir", strT, getReplyString())));
            }
            // String[] pathArray = path.split("/");
            // for (int i = pathArray.length; i > 0; i--) {
            // String strT = "";
            // for (int j = 0; j < i; j++) {
            // strT += pathArray[j] + "/";
            // }
            // logger.debug(HostID(SOSVfs_D_179.params("rmdir", strT)));
            // this.getClient().rmdir(strT);
            //
            // reply = "rmdir OK";
            // logger.debug(HostID(SOSVfs_D_181.params("rmdir", strT,
            // getReplyString())));
            // }
            logINFO(HostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
        } catch (Exception e) {
            reply = e.toString();
            RaiseException(e, SOSVfs_E_134.params("[rmdir]"));
        }
    }

    /** returns attributes
     *
     * @param filename
     * @return SftpATTRS */
    public SftpATTRS getAttributes(final String filename) {
        SftpATTRS attributes = null;
        try {
            attributes = this.getClient().stat(filename);
        } catch (Exception e) {
        }
        return attributes;
    }

    /** Check and return "true" if file is a directory
     *
     * @param filename
     * @return true, if filename is a directory */
    @Override
    public boolean isDirectory(final String filename) {
        /** Problem: the concept of links is not considered in the Concept of
         * SOSVfs Therefore: wie return here "true" if the filename is a link */
        boolean flgR = false;
        try {
            SftpATTRS attributes = this.getClient().stat(filename);
            if (attributes != null) {
                flgR = attributes.isDir();
                if (flgR == false) {
                    flgR = attributes.isLink();
                }
            }
        } catch (Exception e) {
        }
        return flgR;
    }

    /** \brief listNames
     *
     * \details
     *
     * \return
     *
     * @param path
     * @return
     * @throws IOException */
    @Override
    public String[] listNames(String path) throws IOException {
        path = resolvePathname(path);
        try {
            if (path.length() == 0) {
                path = ".";
            }
            if (!this.fileExists(path)) {
                return null;
            }
            if (!isDirectory(path)) {
                reply = "ls OK";
                return new String[] { path };
            }
            @SuppressWarnings("unchecked")
            Vector<LsEntry> lsResult = this.getClient().ls(path);
            String[] result = new String[lsResult.size()];
            String sep = path.endsWith("/") ? "" : "/";
            for (int i = 0, j = 0; i < lsResult.size(); i++) {
                LsEntry entry = lsResult.get(i);
                String strFileName = path + sep + entry.getFilename();
                // if (isDirectory(strFileName) == false) {
                result[j++] = strFileName;
                // }
            }
            reply = "ls OK";
            return result;
        } catch (Exception e) {
            reply = e.toString();
            return null;
        }
    }

    /** return the size of remote-file on the remote machine on success,
     * otherwise -1
     * 
     * @param remoteFile the file on remote machine
     * @return the size of remote-file on remote machine */
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
            // e.printStackTrace();
        }
        return size;
    }

    /** \brief getFile
     *
     * \details
     *
     * \return
     *
     * @param remoteFile
     * @param localFile
     * @param append
     * @return */
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
            if (!append) {
                if (remoteFileSize > 0 && remoteFileSize != transferFile.length()) {
                    throw new JobSchedulerException(SOSVfs_E_162.params(remoteFileSize, transferFile.length()));
                }
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
                }
            }
        }
        return remoteFileSize;
    }

    /** Stores a file on the server using the given name.
     *
     * @param localFile The name of the local file.
     * @param remoteFile The name of the remote file.
     * @return file size
     *
     * @exception Exception
     * @see #put(String, String ) */
    @Override
    // ISOSVfsFileTransfer
    public long putFile(final String localFile, final String remoteFile) {
        long size = 0;
        try {
            // mit Progress Monitor
            // SftpProgressMonitor monitor = new ProgressMonitor();
            // this.getClient().put(localFile,
            // this.resolvePathname(remoteFile),monitor,ChannelSftp.OVERWRITE);
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

    /** Deletes a file on the FTP server.
     * 
     * @param The path of the file to be deleted.
     * @return True if successfully completed, false if not.
     * @throws RunTime error occurs while either sending a command to the server
     *             or receiving a reply from the server. */
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

    /** \brief rename
     *
     * \details
     *
     * \return
     *
     * @param from
     * @param to */
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
        logger.info(HostID(SOSVfs_I_189.params(from, to, getReplyString())));
    }

    /** \brief ExecuteCommand
     *
     * \details
     *
     * \return
     *
     * @param strCmd */
    @Override
    public void ExecuteCommand(final String cmd) {
        final String strEndOfLine = System.getProperty("line.separator");
        ChannelExec channelExec = null;
        Integer exitCode = null;
        InputStream out = null;
        InputStream err = null;
        BufferedReader errReader = null;
        try {
            if (sshSession == null) {
                // sshSession = sshConnection.openSession();
                // TODO create Session ??
                throw new JobSchedulerException(SOSVfs_E_190.params("sshSession"));
            }
            channelExec = (ChannelExec) sshSession.openChannel("exec");
            channelExec.setCommand(cmd);
            channelExec.setInputStream(null);
            channelExec.setErrStream(null);
            out = channelExec.getInputStream();
            err = channelExec.getErrStream();
            channelExec.connect();
            logger.debug(SOSVfs_D_163.params("stdout", cmd));
            StringBuffer outContent = new StringBuffer();
            byte[] tmp = new byte[1024];
            while (true) {
                while (out.available() > 0) {
                    int i = out.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    // output line
                    outContent.append(new String(tmp, 0, i) + strEndOfLine);
                }
                if (channelExec.isClosed()) {
                    exitCode = channelExec.getExitStatus();
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
            logger.debug(outContent);
            logger.debug(SOSVfs_D_163.params("stderr", cmd));
            errReader = new BufferedReader(new InputStreamReader(err));
            StringBuffer errContent = new StringBuffer();
            while (true) {
                String line = errReader.readLine();
                if (line == null) {
                    break;
                }
                errContent.append(line + strEndOfLine);
            }
            logger.debug(errContent);
            if (exitCode != null) {
                if (!exitCode.equals(new Integer(0))) {
                    throw new JobSchedulerException(SOSVfs_E_164.params(exitCode));
                }
            }
            reply = "OK";
            logINFO(HostID(SOSVfs_I_192.params(getReplyString())));
        } catch (Exception ex) {
            reply = ex.toString();
            RaiseException(ex, SOSVfs_E_134.params("ExecuteCommand"));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
            if (errReader != null) {
                try {
                    errReader.close();
                } catch (Exception e) {
                }
            }
            if (err != null) {
                try {
                    err.close();
                } catch (Exception e) {
                }
            }
            if (channelExec != null) {
                try {
                    channelExec.disconnect();
                } catch (Exception e) {
                }
            }
        }
    }

    /** \brief getInputStream
     *
     * \details
     *
     * \return
     *
     * @param fileName
     * @return */
    @Override
    public InputStream getInputStream(final String fileName) {
        try {
            return this.getClient().get(fileName);
        } catch (Exception ex) {
            RaiseException(ex, SOSVfs_E_193.params("getInputStream()", fileName));
            return null;
        }
    }

    /** \brief getOutputStream
     *
     * \details
     *
     * \return
     *
     * @param fileName
     * @return */
    @Override
    public OutputStream getOutputStream(final String fileName) {
        try {
            boolean flgModeAppend = false;
            boolean flgModeRestart = false;
            int intTransferMode = ChannelSftp.OVERWRITE;
            if (flgModeAppend) {
                intTransferMode = ChannelSftp.APPEND;
            } else if (flgModeRestart) {
                intTransferMode = ChannelSftp.RESUME;
            }
            return this.getClient().put(fileName, intTransferMode);
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
            String strM = SOSVfs_D_194.params(pathname, getReplyString());
            logger.debug(strM);
        }
        return true;
    }

    @Override
    public ISOSVirtualFile getFileHandle(String fileName) {
        fileName = adjustFileSeparator(fileName);
        ISOSVirtualFile file = new SOSVfsSFtpFileJCraft(fileName);
        file.setHandler(this);
        // logger.debug(SOSVfs_D_196.params(fileName));
        return file;
    }

    /** \brief getModificationTime
     *
     * \details
     *
     * \return
     *
     * @param path
     * @return */
    @Override
    public String getModificationTime(final String path) {
        String dateTime = null;
        try {
            SftpATTRS objAttr = this.getClient().stat(path);
            if (objAttr != null) {
                long mt = objAttr.getMTime() * 1000L;
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateTime = df.format(new Date(mt));
            }
        } catch (SftpException e) {
            // e.printStackTrace();
        }
        return dateTime;
    }

    /** \brief fileExists
     *
     * \details
     *
     * \return
     *
     * @param filename
     * @return */
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
            logger.debug(HostID(SOSVfs_D_195.params(path)));
            LogReply();
        } catch (Exception e) {
            RaiseException(e, SOSVfs_E_134.params("getCurrentPath"));
        }
        return path;
    }

    /** \brief doAuthenticate
     *
     * \details
     *
     * \return ISOSConnection
     *
     * @param authenticationOptions
     * @return
     * @throws Exception */
    private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions pAuthenticationOptions) throws Exception {
        authenticationOptions = pAuthenticationOptions;
        userName = authenticationOptions.getUser().Value();
        String password = authenticationOptions.getPassword().Value();
        logger.debug(SOSVfs_D_132.params(userName));
        this.createSession(userName, host, port);
        if (authenticationOptions.getAuth_method().isPublicKey()) {
            logger.debug(SOSVfs_D_165.params("userid", "publickey"));
            SOSOptionInFileName authenticationFile = authenticationOptions.getAuth_file();
            authenticationFile.CheckMandatory(true);
            if (authenticationFile.IsNotEmpty()) {
                secureChannel.addIdentity(authenticationFile.JSFile().getPath());
            }
        } else {
            if (authenticationOptions.getAuth_method().isPassword()) {
                logger.debug(SOSVfs_D_165.params("userid", "password"));
                sshSession.setPassword(password);
            } else {
                throw new JobSchedulerException(SOSVfs_E_166.params(authenticationOptions.getAuth_method().Value()));
            }
        }
        try {
            sshSession.connect();
            this.createSftpClient();
        } catch (Exception ex) {
            throw new JobSchedulerException(SOSVfs_E_167.params(authenticationOptions.getAuth_method().Value(), authenticationOptions.getAuth_file().Value()));
        }
        reply = "OK";
        logger.info(SOSVfs_D_133.params(userName));
        this.LogReply();
        return this;
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

    /** \brief connect
     *
     * \details
     *
     * \return void
     *
     * @param phost
     * @param pport */
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

    /** \brief createSession
     *
     * \details
     *
     * \return void
     *
     * @param puser
     * @param phost
     * @param pport
     * @throws Exception */
    private void createSession(final String puser, final String phost, final int pport) throws Exception {
        if (secureChannel == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("secureChannel"));
        }
        sshSession = secureChannel.getSession(puser, phost, pport);
        java.util.Properties config = new java.util.Properties();
        config.put(conConfigurationSTRICT_HOST_KEY_CHECKING, connection2OptionsAlternate.StrictHostKeyChecking.Value());
        sshSession.setConfig(config);
    }

    /** \brief createObjFTPClient
     *
     * \details
     *
     * \return void
     *
     * @throws Exception */
    private void createSftpClient() throws Exception {
        if (sshSession == null) {
            throw new JobSchedulerException(SOSVfs_E_190.params("sshSession"));
        }
        sshConnection = sshSession.openChannel("sftp");
        sshConnection.connect();
        sftpClient = (ChannelSftp) sshConnection;
    }

    /** @author re
     *
     *         Progress monitor (sftp operations GET & PUT) */
    private static class ProgressMonitor implements SftpProgressMonitor {

        long totalSize = 0;
        long transmittedSize = 0;
        long transmittedPercent = -1;

        @Override
        public void init(final int operation, final String src, final String dest, final long size) {
            totalSize = size;
            transmittedSize = 0;
            transmittedPercent = -1;
        }

        @Override
        public boolean count(final long transmitted) {
            System.out.println("Completed " + transmittedSize + "(" + transmittedPercent + "%) out of " + totalSize + ".");
            transmittedSize += transmitted;
            long percent = transmittedSize * 100 / totalSize;
            if (transmittedPercent >= percent) {
                return true;
            }
            transmittedPercent = percent;
            return true;
        }

        @Override
        public void end() {
            System.out.println("END " + transmittedSize + "(" + transmittedPercent + "%) out of " + totalSize + ".");
        }
    }

    @Override
    public void close() {
        try {
        } catch (Exception e) {
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
