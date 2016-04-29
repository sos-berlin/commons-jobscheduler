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

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.HTTPProxyData;
import ch.ethz.ssh2.SFTPException;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3FileAttributes;
import ch.ethz.ssh2.SFTPv3FileHandle;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

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
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSSSH2GanymedImpl extends SOSVfsBaseClass implements JSJobUtilities, ISOSShell, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection,
        ISOSSession {

    protected String strCurrentLine = "";
    protected Connection sshConnection = null;
    protected Session sshSession = null;
    protected InputStream ipsStdOut;
    protected InputStream ipsStdErr;
    protected StringBuffer strbStdoutOutput = new StringBuffer();
    protected StringBuffer strbStderrOutput = new StringBuffer();
    protected long lngLastTime = 0;
    private static final Logger LOGGER = Logger.getLogger(SOSSSH2GanymedImpl.class);
    private JSJobUtilities objJSJobUtilities = this;
    private ISOSConnectionOptions objCO = null;
    private ISOSAuthenticationOptions objAO = null;
    private ISOSShellOptions objSO = null;
    private boolean flgIsRemoteOSWindows = false;
    private SFTPv3Client sftpClient = null;
    private RemoteConsumer stdoutConsumer = null;
    private RemoteConsumer stderrConsumer = null;
    private OutputStream stdin;
    private OutputStreamWriter stdinWriter = null;
    private Integer intExitStatus = null;
    private String strExitSignal = null;
    private Vector<String> vecFilesToDelete = new Vector<String>();
    private boolean simulateShell = false;
    boolean isAuthenticated = false;
    boolean isConnected = false;

    public SOSSSH2GanymedImpl() {
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
                }
            }
            throw new Exception(e.getMessage(), e);
        }
        return this;
    }

    @Override
    public ISOSConnection Connect() throws Exception {
        try {
            isConnected = false;
            this.setSshConnection(new Connection(objCO.getHost().Value(), objCO.getPort().value()));
            if (objCO.getProxy_host().IsNotEmpty()) {
                HTTPProxyData objProxy = null;
                if (objCO.getProxy_user().IsEmpty()) {
                    objProxy = new HTTPProxyData(objCO.getProxy_host().Value(), objCO.getProxy_port().value());
                } else {
                    objProxy = new HTTPProxyData(objCO.getProxy_host().Value(), objCO.getProxy_port().value(), objCO.getProxy_user().Value(), objCO
                            .getProxy_password().Value());
                }
                this.getSshConnection().setProxyData(objProxy);
            }
            this.getSshConnection().connect();
            isConnected = true;
            LOGGER.debug(SOSVfs_D_0102.params(objCO.getHost().Value(), objCO.getPort().value()));
        } catch (Exception e) {
            try {
                this.setSshConnection(null);
            } catch (Exception ex) {
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

    private String normalizePath(final String path) throws Exception {
        String normalizedPath = path.replaceAll("\\\\", "/");
        while (normalizedPath.endsWith("\\") || normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        return normalizedPath;
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
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
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

    @Override
    public String createScriptFile(final String pstrContent) throws Exception {
        try {
            String commandScript = pstrContent;
            if (!flgIsRemoteOSWindows) {
                commandScript = commandScript.replaceAll("(?m)\r", "");
            }
            LOGGER.info(SOSVfs_I_233.params("commandScript"));
            commandScript = objJSJobUtilities.replaceSchedulerVars(commandScript);
            String suffix = flgIsRemoteOSWindows ? ".cmd" : ".sh";
            File resultFile = File.createTempFile("sos", suffix);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile)));
            out.write(commandScript);
            out.flush();
            out.close();
            resultFile.deleteOnExit();
            putFile(resultFile);
            String strFileName2Return = resultFile.getName();
            if (!flgIsRemoteOSWindows) {
                strFileName2Return = "./" + strFileName2Return;
            }
            this.getFilesToDelete().add(strFileName2Return);
            return strFileName2Return;
        } catch (Exception e) {
            throw e;
        }
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
        final String conMethodName = "SOSSSH2GanymedImpl::Authenticate";
        if (pobjAO == null) {
            throw new Exception(SOSVfs_E_234.params("SOSSSH2GanymedImpl"));
        }
        objAO = pobjAO;
        String strUserID = objAO.getUser().Value();
        String strPW = objAO.getPassword().Value();
        if (objAO.getAuth_method().isPublicKey()) {
            objAO.getAuth_file().CheckMandatory(true);
            File authenticationFile = objAO.getAuth_file().JSFile();
            isAuthenticated = getSshConnection().authenticateWithPublicKey(strUserID, authenticationFile, strPW);
        } else if (objAO.getAuth_method().isPassword()) {
            isAuthenticated = getSshConnection().authenticateWithPassword(strUserID, strPW);
        }
        if (!isAuthenticated) {
            throw new Exception(SOSVfs_E_235.params(conMethodName, objAO.toString()));
        }
        LOGGER.info(SOSVfs_D_133.params(strUserID));
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
            } else {
                LOGGER.debug(SOSVfs_D_238.get());
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

    public void putFile(File pfleCommandFile) throws Exception {
        String suffix = flgIsRemoteOSWindows ? ".cmd" : ".sh";
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
                    File resultFile = File.createTempFile("sos", suffix);
                    resultFile.delete();
                    pfleCommandFile.renameTo(resultFile);
                    pfleCommandFile = resultFile;
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

    }

    @Override
    public ISOSSession OpenSession(final ISOSShellOptions pobjShellOptions) {
        objSO = pobjShellOptions;
        if (objSO == null) {
            throw new RuntimeException(SOSVfs_E_245.get());
        }
        try {
            this.setSshSession(this.getSshConnection().openSession());
            if (objSO.getSimulate_shell().value()) {
                long loginTimeout = objSO.getSimulate_shell_login_timeout().value();
                String strPromptTrigger = objSO.getSimulate_shell_prompt_trigger().Value();
                LOGGER.debug(SOSVfs_D_246.params("PTY"));
                this.getSshSession().requestDumbPTY();
                LOGGER.debug(SOSVfs_D_247.params("shell"));
                this.getSshSession().startShell();
                ipsStdOut = getSshSession().getStdout();
                ipsStdErr = getSshSession().getStderr();
                stdoutConsumer = new RemoteConsumer(getStdOutBuffer(), true, ipsStdOut);
                stderrConsumer = new RemoteConsumer(getStdErrBuffer(), false, ipsStdErr);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    private boolean Check4TimeOutOrPrompt(final long plngTimeOut, final String pstrPromptTrigger) {
        long currentTimeMillis = System.currentTimeMillis();
        if (plngTimeOut > 0 && lngLastTime + plngTimeOut < currentTimeMillis) {
            return true;
        }
        if (pstrPromptTrigger.length() > 0 && strCurrentLine.indexOf(pstrPromptTrigger) != -1) {
            LOGGER.debug(SOSVfs_D_249.params(pstrPromptTrigger));
            strCurrentLine = "";
            return true;
        }
        return false;
    }

    @Override
    public void ExecuteCommand(final String pstrCmd) throws Exception {
        intExitStatus = null;
        strExitSignal = null;
        String strCmd = pstrCmd;
        if (!objSO.getSimulate_shell().value()) {
            long lngInactivityTimeout = objSO.getSimulate_shell_inactivity_timeout().value();
            String strPromptTrigger = objSO.getSimulate_shell_prompt_trigger().Value();
            stdinWriter.write(strCmd + "\n");
            stdinWriter.flush();
            boolean prompt = false;
            while (!prompt) {
                prompt = Check4TimeOutOrPrompt(lngInactivityTimeout, strPromptTrigger);
            }
            strCurrentLine = "";
            LOGGER.debug(SOSVfs_D_163.params("stdout", strCmd));
            LOGGER.debug(getStdOutBuffer().toString());
            strbStdoutOutput = new StringBuffer();
        } else {
            if (flgIsRemoteOSWindows == false && !strCmd.startsWith("@") && !strCmd.startsWith("run ")) {
                strCmd = "echo $$ && " + strCmd;
            }
            this.getSshSession().execCommand(strCmd);
            LOGGER.info(SOSVfs_D_163.params("stdout", strCmd));
            ipsStdOut = new StreamGobbler(this.getSshSession().getStdout());
            ipsStdErr = new StreamGobbler(this.getSshSession().getStderr());
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(ipsStdOut));
            strbStdoutOutput = new StringBuffer();
            while (true) {
                String line = stdoutReader.readLine();
                if (line == null) {
                    break;
                }
                LOGGER.info(line);
                getStdOutBuffer().append(line + "\n");
            }
            LOGGER.info(SOSVfs_D_163.params("stderr", strCmd));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(ipsStdErr));
            strbStderrOutput = new StringBuffer();
            while (true) {
                String line = stderrReader.readLine();
                if (line == null) {
                    break;
                }
                LOGGER.info(line);
                strbStderrOutput.append(line + "\n");
            }
            int res = getSshSession().waitForCondition(ChannelCondition.EOF, 30 * 1000);
        }
        String strWhatSignal = "";
        try {
            strWhatSignal = "exit status";
            intExitStatus = this.getSshSession().getExitStatus();
            strWhatSignal = "exit signal";
            strExitSignal = this.getSshSession().getExitSignal();
        } catch (Exception e) {
            LOGGER.info(SOSVfs_I_250.params(strWhatSignal));
        }
    }

    @Override
    public Integer getExitCode() {
        return null;
    }

    @Override
    public String getExitSignal() {
        return strExitSignal;
    }

    public StringBuffer getStdErrBuffer() throws Exception {
        if (strbStderrOutput == null) {
            strbStderrOutput = new StringBuffer();
        }
        return strbStderrOutput;
    }

    public StringBuffer getStdOutBuffer() throws Exception {
        if (strbStdoutOutput == null) {
            strbStdoutOutput = new StringBuffer();
        }
        return strbStdoutOutput;
    }

    class RemoteConsumer extends Thread {

        private final StringBuffer sbuf;
        private boolean writeCurrentline = false;
        private final InputStream stream;
        boolean end = false;

        RemoteConsumer(StringBuffer buffer, final boolean writeCurr, final InputStream str) {
            if (buffer == null) {
                buffer = new StringBuffer();
            }
            sbuf = buffer;
            writeCurrentline = true;
            stream = str;
        }

        private void addText(final byte[] data, final int len) {
            lngLastTime = System.currentTimeMillis();
            String outstring = new String(data).substring(0, len);
            sbuf.append(outstring);
            if (writeCurrentline) {
                int newlineIndex = outstring.indexOf("\n");
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
                while (!end) {
                    buff = new byte[8];
                    int len = stream.read(buff);
                    if (len == -1) {
                        return;
                    }
                    addText(buff, len);
                }
            } catch (Exception e) {
            }
        }

        public synchronized void end() {
            end = true;
        }
    }

    @Override
    public SOSFileList dir(final SOSFolderName pobjFolderName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean rmdir(final SOSFolderName pobjFolderName) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String replaceSchedulerVars(final String pstrString2Modify) {
        LOGGER.debug(SOSVfs_D_251.get());
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {

    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        LOGGER.debug(SOSVfs_D_252.params(objJSJobUtilities.getClass().getName()));
    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
        setJSParam(pstrKey, pstrValue.toString());
    }

    @Override
    public StringBuffer getStdErr() throws Exception {
        return getStdErrBuffer();
    }

    @Override
    public StringBuffer getStdOut() throws Exception {
        return getStdOutBuffer();
    }

    @Override
    public SOSFileList dir(final String pathname, final int flag) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ISOSConnection Connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCurrentNodeName() {
        return "";
    }

    @Override
    public void setStateText(final String pstrStateText) {
        // TODO Auto-generated method stub
    }

    @Override
    public void doPostLoginOperations() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setCC(final int pintCC) {
        // TODO Auto-generated method stub
    }

    @Override
    public ISOSConnection Connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
        // TODO Auto-generated method stub
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
