package sos.net.ssh;

import sos.net.ssh.exceptions.SSHExecutionError;
import sos.net.ssh.exceptions.SSHMissingCommandError;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJobTrilead extends SOSSSHJob2 {

    private ISOSVFSHandler vfsHandler;

    @Override
    public void generateTemporaryFilename() {
    }

    @Override
    public String getPreCommand() {
        return "";
    }

    @Override
    public void processPostCommands(String tmpReturnValueFileName) {
    }

    @Override
    public void preparePostCommandHandler() {
    }

    @Override
    public String getTempFileName() {
        return "";
    }

    @Override
    public ISOSVFSHandler getVFSSSH2Handler() {
        try {
            vfsHandler = VFSFactory.getHandler("SSH2.TRILEAD");
        } catch (Exception e) {
            throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS", e);
        }
        return vfsHandler;
    }

    @Override
    public StringBuffer getStdErr() throws Exception {
        return vfsHandler.getStdErr();
    }

    @Override
    public StringBuffer getStdOut() throws Exception {
        return vfsHandler.getStdOut();
    }

    public SOSSSHJob2 execute() throws Exception {
        boolean flgScriptFileCreated = false; // http://www.sos-berlin.com/jira/browse/JITL-17
        vfsHandler.setJSJobUtilites(objJSJobUtilities);

        try {
            if (isConnected == false) {
                this.connect();
            }
            vfsHandler.OpenSession(objOptions);
            if (objOptions.command.IsEmpty() == false) {
                strCommands2Execute = objOptions.command.values();
            } else {
                if (objOptions.isScript() == true) {
                    strCommands2Execute = new String[1];
                    String strTemp = objOptions.command_script.Value();
                    if (objOptions.command_script.IsEmpty()) {
                        strTemp = objOptions.command_script_file.JSFile().File2String();
                    }
                    strTemp = objJSJobUtilities.replaceSchedulerVars(flgIsWindowsShell, strTemp);
                    strCommands2Execute[0] = vfsHandler.createScriptFile(strTemp);
                    flgScriptFileCreated = true; // http://www.sos-berlin.com/jira/browse/JITL-17
                    strCommands2Execute[0] += " " + objOptions.command_script_param.Value();
                } else {
                    throw new SSHMissingCommandError(objMsg.getMsg(SOS_SSH_E_100)); // "SOS-SSH-E-100: neither Commands nor Script(file) specified. Abort.");
                }
            }

            for (String strCmd : strCommands2Execute) {
                try {
                    /** \change Substitution of variables enabled
                     *
                     * see http://www.sos-berlin.com/jira/browse/JS-673 */
                    strCmd = objJSJobUtilities.replaceSchedulerVars(flgIsWindowsShell, strCmd);
                    logger.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
                    vfsHandler.ExecuteCommand(strCmd);
                    objJSJobUtilities.setJSParam(conExit_code, "0");
                    checkStdOut();
                    checkStdErr();
                    checkExitCode();
                    changeExitSignal();
                } catch (Exception e) {
                    checkStdOut();
                    checkStdErr();
                    checkExitCode();
                    changeExitSignal();
                    // logger.error(this.StackTrace2String(e));
                    throw new SSHExecutionError("Exception raised: " + e, e);
                } finally {
                    if (flgScriptFileCreated == true) {
                        // http://www.sos-berlin.com/jira/browse/JITL-17
                        // file will be deleted by the Vfs Component.
                    }
                }
            }
            // http://www.sos-berlin.com/jira/browse/JITL-112
        } catch (Exception e) {
            // logger.error(this.StackTrace2String(e));
            String strErrMsg = "SOS-SSH-E-120: error occurred processing ssh command: ";
            // logger.error(strErrMsg, e);
            throw new SSHExecutionError(strErrMsg, e);
        } finally {
            if (keepConnected == false) {
                disconnect();
            }
        }
        return this;
    }
}
