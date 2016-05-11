package com.sos.VirtualFileSystem.SSH;

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
import java.util.Vector;

import org.apache.log4j.Logger;

import sos.spooler.Variable_set;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSSession;
import com.sos.VirtualFileSystem.Interfaces.ISOSShell;
import com.sos.VirtualFileSystem.Interfaces.ISOSShellOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFileSystem;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFolder;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass;
import com.sos.i18n.Msg;
import com.sos.i18n.Msg.BundleBaseName;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.SFTPException;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSSH2TriLeadImpl extends SOSVfsBaseClass implements ISOSShell, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection, ISOSSession {

    protected Msg objMsg = new Msg(new BundleBaseName(this.getClass().getAnnotation(I18NResourceBundle.class).baseName()));
    protected String strCurrentLine = "";
    protected Connection sshConnection = null;
    protected Session sshSession = null;
    protected InputStream ipsStdOut;
    protected InputStream ipsStdErr;
    protected StringBuffer strbStdoutOutput;
    protected StringBuffer strbStderrOutput;
    protected long lngLastTime = 0;
    private static final Logger LOGGER = Logger.getLogger(SOSSSH2TriLeadImpl.class);
    private final String strEndOfLine = System.getProperty("line.separator");
    private boolean simulateShell = false;
    private ISOSConnectionOptions objCO = null;
    private ISOSAuthenticationOptions objAO = null;
    private ISOSShellOptions objSO = null;
    private boolean flgIsRemoteOSWindows = false;
    private SFTPv3Client sftpClient = null;
    private RemoteConsumer stdoutConsumer = null;
    private RemoteConsumer stderrConsumer = null;
    private OutputStream stdin;
    private OutputStreamWriter stdinWriter = null;
    private Integer exitStatus = null;
    private String exitSignal = null;
    private Vector<String> vecFilesToDelete = new Vector<String>();
    private Variable_set params = null;
    boolean isAuthenticated = false;
    boolean isConnected = false;

    public SOSSSH2TriLeadImpl() {
        //
    }

    private Vector<String> getFilesToDelete() {
        if (vecFilesToDelete == null) {
            vecFilesToDelete = new Vector<String>();
        }
        return vecFilesToDelete;
    }

    @Override
    public ISOSConnection Connect(final String pstrHostName, final int pintPortNumber) throws Exception {
        try {
            isConnected = false;
            this.setSshConnection(new Connection(pstrHostName, pintPortNumber));
        } catch (Exception e) {
            if (this.getSshConnection() != null) {
                try {
                    this.getSshConnection().close();
                    this.setSshConnection(null);
                } catch (Exception ex) {
                    //
                }
            }
            throw new Exception(e.getMessage());
        }
        return this;
    }

    @Override
    public ISOSConnection Connect() throws Exception {
        if (objCO == null) {
            throw new JobSchedulerException(SOSVfs_F_102.get());
        }
        try {
            isConnected = false;
            String strHostName = objCO.getHost().Value();
            int intPortNo = objCO.getPort().value();
            this.setSshConnection(new Connection(strHostName, intPortNo));
            if (objCO.getProxy_host().IsNotEmpty()) {
                HTTPProxyData objProxy = null;
                if (objCO.getProxy_user().IsEmpty()) {
                    objProxy = new HTTPProxyData(objCO.getProxy_host().Value(), objCO.getProxy_port().value());
                } else {
                    objProxy = new HTTPProxyData(objCO.getProxy_host().Value(), objCO.getProxy_port().value(), objCO.getProxy_user().Value(),
                                    objCO.getProxy_password().Value());
                }
                this.getSshConnection().setProxyData(objProxy);
            }
            this.getSshConnection().connect();
            isConnected = true;
            LOGGER.debug(SOSVfs_D_0102.params(strHostName, intPortNo));
        } catch (Exception e) {
            try {
                this.setSshConnection(null);
            } catch (Exception ex) {
                //
            }
            throw e;
        }
        return this;
    }

    protected boolean sshFileExists(final SFTPv3Client psftpClient, final String filename) {
        try {
            SFTPv3FileAttributes attributes = psftpClient.stat(filename);
            if (attributes != null) {
                return attributes.isRegularFile() || attributes.isDirectory();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isDirectory(final SFTPv3Client psftpClient, final String filename) {
        try {
            return psftpClient.stat(filename).isDirectory();
        } catch (Exception e) {
            //
        }
        return false;
    }

    protected long getFileSize(final SFTPv3Client psftpClient, final String filename) throws Exception {
        return psftpClient.stat(filename).size.longValue();
    }

    protected int sshFilePermissions(final SFTPv3Client psftpClient, final String filename) {
        try {
            SFTPv3FileAttributes attributes = psftpClient.stat(filename);
            if (attributes != null) {
                return attributes.permissions.intValue();
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    protected Connection getSshConnection() {
        return sshConnection;
    }

    protected void setSshConnection(final Connection psshConnection) {
        if (psshConnection == null) {
            isConnected = false;
            if (sftpClient != null) {
                if (this.getFilesToDelete() != null) {
                    for (String strFileNameToDelete : vecFilesToDelete) {
                        try {
                            this.deleteFile(strFileNameToDelete);
                            LOGGER.debug(SOSVfs_I_0113.params(strFileNameToDelete));
                        } catch (Exception e) {
                            LOGGER.error(e.getLocalizedMessage());
                        }
                    }
                    vecFilesToDelete = null;
                }
                sftpClient.close();
                sftpClient = null;
                LOGGER.debug(SOSVfs_D_232.params("sftpClient"));
            }
            if (stderrConsumer != null) {
                stderrConsumer.end();
                stderrConsumer = null;
                LOGGER.debug(SOSVfs_D_232.params("stderrConsumer"));
            }
            if (stdoutConsumer != null) {
                stdoutConsumer.end();
                stdoutConsumer = null;
                LOGGER.debug(SOSVfs_D_232.params("stdoutConsumer"));
            }
            if (sshSession != null) {
                sshSession.close();
                sshSession = null;
                LOGGER.debug(SOSVfs_D_232.params("sshSession"));
            }
            if (sshConnection != null) {
                sshConnection.close();
                LOGGER.debug(SOSVfs_D_232.params("sshConnection"));
            }
        }
        sshConnection = psshConnection;
    }

    public Session getSshSession() {
        return sshSession;
    }

    public void setSshSession(final Session psshSession) {
        sshSession = psshSession;
    }

    public SOSSSH2TriLeadImpl setJSParam(final String pstrKey, final String pstrValue) {
        if (params != null) {
            params.set_var(pstrKey, pstrValue);
        }
        return this;
    }

    public SOSSSH2TriLeadImpl setParameters(final Variable_set pVariableSet) {
        params = pVariableSet;
        return this;
    }

    @Override
    public String createScriptFile(final String pstrContent) throws Exception {
        try {
            String commandScript = pstrContent;
            if (!flgIsRemoteOSWindows) {
                commandScript = commandScript.replaceAll("(?m)\r", "");
            }
            LOGGER.debug(SOSVfs_I_233.params(pstrContent));
            replaceSchedulerVars(flgIsRemoteOSWindows, commandScript);
            File fleTempScriptFile = File.createTempFile("sos-sshscript", getScriptFileNameSuffix());
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fleTempScriptFile)));
            out.write(commandScript);
            out.flush();
            out.close();
            fleTempScriptFile.deleteOnExit();
            putFile(fleTempScriptFile);
            String strFileName2Return = fleTempScriptFile.getName();
            if (!flgIsRemoteOSWindows) {
                strFileName2Return = "./" + strFileName2Return;
            }
            add2Files2Delete(strFileName2Return);
            LOGGER.info(SOSVfs_I_253.params(fleTempScriptFile.getAbsolutePath()));
            return strFileName2Return;
        } catch (Exception e) {
            throw e;
        }
    }

    private void add2Files2Delete(final String pstrFilenName2Delete) {
        this.getFilesToDelete().add(pstrFilenName2Delete);
        LOGGER.debug(String.format(SOSVfs_D_254.params(pstrFilenName2Delete)));
    }

    protected String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
        String strTemp = pstrString2Modify;
        if (params != null) {
            LOGGER.debug(SOSVfs_D_255.get());
            String[] paramNames = params.names().split(";");
            for (String name : paramNames) {
                SignalDebug(SOSVfs_D_256.params(name));
                String regex = "(?i)";
                if (isWindows) {
                    regex += "%SCHEDULER_PARAM_" + name + "%";
                } else {
                    regex += "\\$\\{?SCHEDULER_PARAM_" + name + "\\}?";
                }
                strTemp = myReplaceAll(strTemp, regex, params.value(name));
            }
        }
        return strTemp;
    }

    public String myReplaceAll(final String source, final String what, final String replacement) {
        String newReplacement = replacement.replaceAll("\\$", "\\\\\\$");
        return source.replaceAll("(?m)" + what, newReplacement);
    }

    @Override
    public ISOSConnection getConnection() {
        return this;
    }

    @Override
    public ISOSSession getSession() {
        return this;
    }

    @Override
    public ISOSConnection Connect(final ISOSConnectionOptions pobjConnectionOptions) throws Exception {
        objCO = pobjConnectionOptions;
        if (objCO != null) {
            this.Connect();
        }
        return this;
    }

    @Override
    public void CloseConnection() throws Exception {
        this.setSshConnection(null);
    }

    @Override
    public ISOSConnection Authenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
        final String conMethodName = "SOSSSH2TriLeadImpl::Authenticate";
        objAO = pobjAO;
        if (objAO.getAuth_method().isPublicKey()) {
            File authenticationFile = new File(objAO.getAuth_file().Value());
            if (!authenticationFile.exists()) {
                throw new JobSchedulerException(SOSVfs_E_257.params(authenticationFile.getCanonicalPath()));
            }
            if (!authenticationFile.canRead()) {
                throw new JobSchedulerException(SOSVfs_E_258.params(authenticationFile.getCanonicalPath()));
            }
            isAuthenticated =
                    this.getSshConnection().authenticateWithPublicKey(objAO.getUser().Value(), authenticationFile, objAO.getPassword().Value());
        } else if (objAO.getAuth_method().isPassword()) {
            isAuthenticated = getSshConnection().authenticateWithPassword(objAO.getUser().Value(), objAO.getPassword().Value());
        }
        if (!isAuthenticated) {
            throw new JobSchedulerException(SOSVfs_E_235.params(conMethodName, objAO.toString()));
        }
        LOGGER.info(SOSVfs_D_133.params(objAO.getUser().Value()));
        return this;
    }

    @Override
    public ISOSVFSHandler getHandler() {
        return this;
    }

    @Override
    public boolean remoteIsWindowsShell() {
        Session objSSHSession = null;
        flgIsRemoteOSWindows = false;
        try {
            String checkShellCommand = "echo %ComSpec%";
            LOGGER.debug(SOSVfs_D_236.get());
            objSSHSession = this.getSshConnection().openSession();
            LOGGER.debug(SOSVfs_D_0151.params(checkShellCommand));
            objSSHSession.execCommand(checkShellCommand);
            LOGGER.debug(SOSVfs_D_163.params("stdout", checkShellCommand));
            ipsStdOut = new StreamGobbler(objSSHSession.getStdout());
            ipsStdErr = new StreamGobbler(objSSHSession.getStderr());
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(ipsStdOut));
            String stdOut = "";
            while (true) {
                String line = stdoutReader.readLine();
                if (line == null) {
                    break;
                }
                LOGGER.debug(line);
                stdOut += line;
            }
            LOGGER.debug(SOSVfs_D_163.params("stderr", checkShellCommand));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(ipsStdErr));
            while (true) {
                String line = stderrReader.readLine();
                if (line == null) {
                    break;
                }
                LOGGER.debug(line);
            }
            if (stdOut.indexOf("cmd.exe") > -1) {
                LOGGER.debug(SOSVfs_D_237.get());
                flgIsRemoteOSWindows = true;
                return true;
            }
        } catch (Exception e) {
            LOGGER.debug(SOSVfs_D_239.params(e));
        } finally {
            if (objSSHSession != null) {
                try {
                    objSSHSession.close();
                } catch (Exception e) {
                    LOGGER.debug(SOSVfs_D_240.params(e));
                }
            }
        }
        return false;
    }

    private String getScriptFileNameSuffix() {
        return flgIsRemoteOSWindows ? ".cmd" : ".sh";
    }

    public void putFile(File pfleCommandFile) throws Exception {
        String suffix = getScriptFileNameSuffix();
        String strFileName = pfleCommandFile.getName();
        try {
            boolean exists = true;
            while (exists) {
                try {
                    FtpClient().stat(strFileName);
                } catch (SFTPException e) {
                    LOGGER.debug(SOSVfs_E_241.params(e.getServerErrorCode()));
                    exists = false;
                }
                if (exists) {
                    LOGGER.debug(SOSVfs_D_242.get());
                    File fleResultFile = File.createTempFile("sos", suffix);
                    fleResultFile.delete();
                    pfleCommandFile.renameTo(fleResultFile);
                    pfleCommandFile = fleResultFile;
                }
            }
            SFTPv3FileHandle fileHandle = this.getFileHandle(strFileName, new Integer(0700));
            FileInputStream fis = null;
            long offset = 0;
            try {
                fis = new FileInputStream(pfleCommandFile);
                byte[] buffer = new byte[1024];
                while (true) {
                    int len = fis.read(buffer, 0, buffer.length);
                    if (len <= 0) {
                        break;
                    }
                    sftpClient.write(fileHandle, offset, buffer, 0, len);
                    offset += len;
                }
                fis.close();
                fis = null;
            } catch (Exception e) {
                throw e;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                        fis = null;
                    } catch (Exception ex) {
                        //
                    }
                }
            }
            LOGGER.debug(SOSVfs_D_243.params(sftpClient.canonicalPath(strFileName)));
            sftpClient.closeFile(fileHandle);
            fileHandle = null;
        } catch (Exception e) {
            throw e;
        }
    }

    public SFTPv3FileHandle getFileHandle(final String pstrFileName, final Integer pintPermissions) throws Exception {
        SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
        attr.permissions = pintPermissions;
        SFTPv3FileHandle fileHandle = this.FtpClient().createFileTruncate(pstrFileName, attr);
        return fileHandle;
    }

    public void setFilePermissions(final String pstrFileName, final Integer pintPermissions) throws Exception {
        SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
        attr.permissions = pintPermissions;
        SFTPv3FileHandle fileHandle = this.FtpClient().createFileTruncate(pstrFileName, attr);
    }

    public void deleteFile(final String pstrCommandFile) throws Exception {
        try {
            if (isNotEmpty(pstrCommandFile)) {
                this.FtpClient().rm(pstrCommandFile);
                LOGGER.debug(SOSVfs_I_0113.params(pstrCommandFile));
            }
        } catch (Exception e) {
            LOGGER.error(SOSVfs_E_244.params(e));
            throw new JobSchedulerException(e);
        }
    }

    private SFTPv3Client FtpClient() throws Exception {
        if (sftpClient == null) {
            sftpClient = new SFTPv3Client(this.getSshConnection());
        }
        return sftpClient;
    }

    @Override
    public void CloseSession() throws Exception {
        //
    }

    @Override
    public ISOSSession OpenSession(final ISOSShellOptions pobjShellOptions) throws Exception {
        objSO = pobjShellOptions;
        if (objSO == null) {
            throw new JobSchedulerException(SOSVfs_E_245.get());
        }
        long loginTimeout = objSO.getSimulate_shell_login_timeout().value();
        String strPromptTrigger = objSO.getSimulate_shell_prompt_trigger().Value();
        this.setSshSession(this.getSshConnection().openSession());
        if (objSO.getSimulate_shell().value()) {
            LOGGER.debug(SOSVfs_D_246.params("PTY"));
            this.getSshSession().requestDumbPTY();
            LOGGER.debug(SOSVfs_D_247.params("shell"));
            this.getSshSession().startShell();
            ipsStdOut = getSshSession().getStdout();
            ipsStdErr = getSshSession().getStderr();
            strbStdoutOutput = new StringBuffer();
            strbStderrOutput = new StringBuffer();
            stdoutConsumer = new RemoteConsumer(strbStdoutOutput, true, ipsStdOut);
            stderrConsumer = new RemoteConsumer(strbStderrOutput, false, ipsStdErr);
            stdoutConsumer.start();
            stderrConsumer.start();
            stdin = getSshSession().getStdin();
            stdinWriter = new OutputStreamWriter(stdin);
            LOGGER.debug(SOSVfs_D_248.get());
            boolean loggedIn = false;
            while (!loggedIn) {
                if (lngLastTime > 0) {
                    loggedIn = Check4TimeOutOrPrompt(loginTimeout, strPromptTrigger);
                }
            }
        } else {
            if (!objSO.getIgnore_hangup_signal().value()) {
                sshSession.requestPTY("vt100");
            }
        }
        return this;
    }

    private boolean Check4TimeOutOrPrompt(final long plngLoginTimeOut, final String pstrPromptTrigger) {
        long now = System.currentTimeMillis();
        if (plngLoginTimeOut > 0 && lngLastTime + plngLoginTimeOut < now) {
            return true;
        }
        LOGGER.debug("strCurrentLine=" + strCurrentLine + " pstrPromptTrigger=" + pstrPromptTrigger);
        if (pstrPromptTrigger.length() > 0 && strCurrentLine.indexOf(pstrPromptTrigger) != -1) {
            LOGGER.debug("strCurrentLine=" + strCurrentLine + " pstrPromptTrigger=" + pstrPromptTrigger);
            LOGGER.debug("Found login prompt " + pstrPromptTrigger);
            strCurrentLine = "";
            return true;
        }
        return false;
    }

    @Override
    public void ExecuteCommand(final String pstrCmd) throws Exception {
        exitStatus = null;
        exitSignal = null;
        int retval = 0;
        String strCmd = pstrCmd;
        long loginTimeout = objSO.getSimulate_shell_login_timeout().value();
        String strPromptTrigger = objSO.getSimulate_shell_prompt_trigger().Value();
        strbStderrOutput = new StringBuffer();
        strbStdoutOutput = new StringBuffer();
        if (objSO.getSimulate_shell().value()) {
            LOGGER.debug("executing: " + strCmd);
            stdinWriter.write(strCmd + strEndOfLine);
            stdinWriter.flush();
            boolean prompt = false;
            while (!prompt) {
                prompt = Check4TimeOutOrPrompt(loginTimeout, strPromptTrigger);
            }
            strCurrentLine = "";
            LOGGER.info(SOSVfs_D_163.params("stdout", strCmd));
            LOGGER.info(strbStdoutOutput.toString());
            stdoutConsumer.end();
            stderrConsumer.end();
            exitStatus = 0;
        } else {
            if (!flgIsRemoteOSWindows && !strCmd.startsWith("@") && !strCmd.startsWith("run ")) {
                strCmd = "echo $$ && " + strCmd;
            }
            LOGGER.info(SOSVfs_D_163.params("stdout", strCmd));
            this.getSshSession().execCommand(strCmd);
            ipsStdOut = new StreamGobbler(this.getSshSession().getStdout());
            ipsStdErr = new StreamGobbler(this.getSshSession().getStderr());
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(ipsStdOut));
            while (true) {
                String line = stdoutReader.readLine();
                if (line == null) {
                    break;
                }
                LOGGER.info(line);
                strbStdoutOutput.append(line + strEndOfLine);
            }
            LOGGER.debug(SOSVfs_D_163.params("stderr", strCmd));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(ipsStdErr));
            strbStderrOutput = new StringBuffer();
            while (true) {
                String line = stderrReader.readLine();
                if (line == null) {
                    break;
                }
                LOGGER.debug(line);
                strbStderrOutput.append(line + strEndOfLine);
            }
            int res = getSshSession().waitForCondition(ChannelCondition.EOF, 30 * 1000);
            long timeout = (2 * 60) * 1000;
            retval = getSshSession().waitForCondition(ChannelCondition.EXIT_STATUS, timeout);
            exitStatus = this.getSshSession().getExitStatus();
            if ((retval & ChannelCondition.TIMEOUT) != 0) {
                LOGGER.debug("Timeout reached");
                throw new java.util.concurrent.TimeoutException();
            } else {
                try {
                    exitStatus = this.getSshSession().getExitStatus();
                } catch (Exception e) {
                    LOGGER.info(SOSVfs_I_250.params("exit status"));
                }
            }
            try {
                exitSignal = this.getSshSession().getExitSignal();
            } catch (Exception e) {
                LOGGER.info(SOSVfs_I_250.params("exit signal"));
            }
        }
    }

    @Override
    public Integer getExitCode() {
        if (exitStatus == null) {
            throw new RuntimeException("Error reading exit code from SSH-Server. No exit code is available.");
        }
        return exitStatus;
    }

    @Override
    public String getExitSignal() {
        return exitSignal;
    }

    @Override
    public StringBuffer getStdErr() throws Exception {
        return strbStderrOutput;
    }

    @Override
    public StringBuffer getStdOut() throws Exception {
        return strbStdoutOutput;
    }

    /** This thread consumes output from the remote server puts it into fields of
     * the main class */
    class RemoteConsumer extends Thread {

        private final StringBuffer sbuf;
        private boolean writeCurrentline = false;
        private final InputStream stream;
        boolean end = false;

        RemoteConsumer(final StringBuffer buffer, final boolean writeCurr, final InputStream str) {
            sbuf = buffer;
            writeCurrentline = true;
            stream = str;
        }

        private void addText(final byte[] data, final int len) {
            lngLastTime = System.currentTimeMillis();
            String outstring = new String(data).substring(0, len);
            LOGGER.debug("--> outstring: " + outstring);
            sbuf.append(outstring);
            if (writeCurrentline) {
                int newlineIndex = outstring.indexOf(strEndOfLine);
                if (newlineIndex > -1) {
                    String stringAfterNewline = outstring.substring(newlineIndex);
                    strCurrentLine = stringAfterNewline;
                } else {
                    strCurrentLine += outstring;
                }
            }
        }

        @Override
        public void run() {
            byte[] buff = new byte[64];

            try {
                LOGGER.debug("run loop startet");
                while (!end) {
                    buff = new byte[8];
                    int len = stream.read(buff);
                    LOGGER.debug("run loop len:" + len);
                    if (len == -1) {
                        return;
                    }
                    addText(buff, len);
                    LOGGER.debug("run loop:" + strCurrentLine);
                }
                LOGGER.debug("run loop ended");
            } catch (Exception e) {
                //
            }
        }

        public synchronized void end() {
            end = true;
        }
    }

    @Override
    public SOSFileList dir(final SOSFolderName pobjFolderName) {
        return null;
    }

    @Override
    public SOSFileList dir(final String pathname, final int flag) {
        return null;
    }

    @Override
    public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) throws IOException {
        return null;
    }

    @Override
    public boolean rmdir(final SOSFolderName pobjFolderName) throws IOException {
        return false;
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        //
    }

    @Override
    public ISOSConnection Connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) throws Exception {
        return null;
    }

    @Override
    public void doPostLoginOperations() {
        //
    }

    @Override
    public ISOSConnection Connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
        return null;
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