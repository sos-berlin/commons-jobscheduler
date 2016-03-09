package sos.net.ssh;

import java.util.Vector;

import org.apache.log4j.Logger;

import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.i18n.Msg;
import com.sos.i18n.Msg.BundleBaseName;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public abstract class SOSSSHJob2 extends JSJobUtilitiesClass<SOSSSHJobOptions> {

    protected String[] strCommands2Execute = {};
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final String conExit_code = "exit_code";
    protected Msg objMsg;
    protected StringBuffer strbStdoutOutput;
    protected StringBuffer strbStderrOutput;
    protected Vector<String> tempFilesToDelete = new Vector<String>();
    private ISOSVFSHandler objVFS = null;
    private final String conStd_err_output = "std_err_output";
    private final String conStd_out_output = "std_out_output";
    private final String conExit_signal = "exit_signal";
    public boolean isConnected = false;
    public boolean flgIsWindowsShell = false;
    public boolean keepConnected = false;

    @I18NMessages(value = {
            @I18NMessage("neither Commands nor Script(file) specified. Abort."),
            @I18NMessage(value = "neither Commands nor Script(file) specified. Abort.", locale = "en_UK", explanation = "neither Commands nor Script(file) specified. Abort."),
            @I18NMessage(value = "Es wurde weder ein Kommando noch eine Kommandodatei angegeben. Abbruch.", locale = "de", explanation = "neither Commands nor Script(file) specified. Abort.") }, msgnum = "SOS-SSH-E-100", msgurl = "msgurl")
    public static final String SOS_SSH_E_100 = "SOS-SSH-E-100";

    @I18NMessages(value = { @I18NMessage("executing remote command: '%1$s'."),
            @I18NMessage(value = "executing remote command: '%1$s'.", locale = "en_UK", explanation = "executing remote command: '%1$s'."),
            @I18NMessage(value = "starte am remote-server das Kommando: '%1$s'.", locale = "de", explanation = "executing remote command: '%1$s'.") }, msgnum = "SOS-SSH-D-110", msgurl = "msgurl")
    protected static final String SOS_SSH_D_110 = "SOS-SSH-D-110";

    public String[] getCommands2Execute() {
        return strCommands2Execute;
    }

    protected ISOSVFSHandler getVFS() {
        objVFS = getVFSSSH2Handler();
        preparePostCommandHandler();
        return objVFS;
    }

    public SOSSSHJob2() {
        super(new SOSSSHJobOptions());
        objMsg = new Msg(new BundleBaseName(this.getClass().getAnnotation(I18NResourceBundle.class).baseName()));
        getVFS();
        // generateTemporaryFilename() has to be called once to generate a
        // temporary filename to use if return values have to be stored
        generateTemporaryFilename();
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        super.setJSJobUtilites(pobjJSJobUtilities);
        objVFS.setJSJobUtilites(pobjJSJobUtilities);
    }

    public SOSSSHJob2 connect() {
        getVFS();
        getOptions().CheckMandatory();
        try {
            objVFS.Connect(objOptions);
            ISOSAuthenticationOptions objAU = objOptions;
            ISOSConnection authenticate = objVFS.Authenticate(objAU);
            logger.debug("connection established");
        } catch (Exception e) {
            throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
        }
        flgIsWindowsShell = objVFS.remoteIsWindowsShell();
        isConnected = true;
        return this;
    }

    // moved to extending classes for JCraft and Trilead Job implementation
    // different implementations for trilead and JSch, therefore moved to the
    // extending classes SOSSSHJobTrilead respectively SOSSSHJobJSch
    public abstract SOSSSHJob2 execute() throws Exception;

    public void disconnect() {
        if (isConnected == true) {
            try {
                objVFS.CloseConnection();
            } catch (Exception e) {
                throw new SSHConnectionError("problems closing connection", e);
            }
            isConnected = false;
        }
    }

    public String changeExitSignal() {
        String strExitSignal = objVFS.getExitSignal();
        if (isNotEmpty(strExitSignal)) {
            objJSJobUtilities.setJSParam(conExit_signal, strExitSignal);
            if (objOptions.ignore_signal.isTrue()) {
                logger.info("SOS-SSH-I-130: exit signal is ignored due to option-settings: " + strExitSignal);
            } else {
                throw new SSHExecutionError("SOS-SSH-E-140: remote command terminated with exit signal: " + strExitSignal);
            }
        } else {
            objJSJobUtilities.setJSParam(conExit_signal, "");
        }
        return strExitSignal;
    }

    public Integer checkExitCode() {
        objJSJobUtilities.setJSParam("exit_code_ignored", "false");
        Integer intExitCode = objVFS.getExitCode();
        if (isNotNull(intExitCode)) {
            objJSJobUtilities.setJSParam(conExit_code, intExitCode.toString());
            if (!intExitCode.equals(new Integer(0))) {
                if (objOptions.ignore_error.isTrue() || objOptions.ignore_exit_code.Values().contains(intExitCode)) {
                    logger.info("SOS-SSH-E-140: exit code is ignored due to option-settings: " + intExitCode);
                    objJSJobUtilities.setJSParam("exit_code_ignored", "true");
                } else {
                    String strM = "SOS-SSH-E-150: remote command terminated with exit code: " + intExitCode;
                    objJSJobUtilities.setCC(intExitCode);
                    if (objOptions.raise_exception_on_error.isTrue()) {
                        if (objOptions.ignore_error.value()) {
                            logger.info(strM);
                        } else {
                            logger.error(strM);
                        }
                        throw new SSHExecutionError(strM);
                    }
                }
            }
        }
        return intExitCode;
    }

    public void checkStdErr() {
        try {
            StringBuffer stbStdErr = objVFS.getStdErr();
            if (strbStderrOutput == null) {
                strbStderrOutput = new StringBuffer();
            }
            strbStderrOutput.append(stbStdErr);
        } catch (Exception e) {
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
        if (isNotEmpty(strbStderrOutput)) {
            logger.info("stderr = " + strbStderrOutput.toString());

            objJSJobUtilities.setJSParam(conStd_err_output, strbStderrOutput);
            if (objOptions.ignore_stderr.value()) {
                logger.info("SOS-SSH-I-150: output to stderr is ignored: " + strbStderrOutput);
            } else {
                String strM = "SOS-SSH-E-160: remote execution reports error: " + strbStderrOutput;
                logger.error(strM);
                if (objOptions.raise_exception_on_error.value()) {
                    throw new SSHExecutionError(strM);
                }
            }
        }
    }

    public void clear() {
        strbStdoutOutput = new StringBuffer();
        strbStderrOutput = new StringBuffer();
    }

    public void checkStdOut() {
        try {
            StringBuffer stbStdOut = objVFS.getStdOut();
            if (strbStdoutOutput == null) {
                strbStdoutOutput = new StringBuffer();
            }
            strbStdoutOutput.append(stbStdOut);
        } catch (Exception e) {
            logger.error(this.StackTrace2String(e));
            throw new JobSchedulerException(e.getLocalizedMessage(), e);
        }
        objJSJobUtilities.setJSParam(conStd_out_output, strbStdoutOutput);
    }

    public StringBuffer getStdOut() throws Exception {
        return this.getVFS().getStdOut();
    }

    public StringBuffer getStdErr() throws Exception {
        return this.getVFS().getStdErr();
    }

    public abstract void generateTemporaryFilename();

    public abstract String getTempFileName();

    public abstract String getPreCommand();

    public abstract void processPostCommands(String tmpReturnValueFileName);

    public abstract void preparePostCommandHandler();

    public abstract ISOSVFSHandler getVFSSSH2Handler();

}
