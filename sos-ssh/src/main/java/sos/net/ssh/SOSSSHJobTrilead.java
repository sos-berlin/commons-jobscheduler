package sos.net.ssh;

import org.apache.log4j.Logger;

import sos.net.ssh.exceptions.SSHExecutionError;
import sos.net.ssh.exceptions.SSHMissingCommandError;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJobTrilead extends SOSSSHJob2 {

    private static final Logger LOGGER = Logger.getLogger(SOSSSHJobTrilead.class);
    private ISOSVFSHandler vfsHandler;

    @Override
    public void generateTemporaryFilename() {
        //
    }

    @Override
    public String getPreCommand() {
        return "";
    }

    @Override
    public void processPostCommands(String tmpReturnValueFileName) {
        //
    }

    @Override
    public void preparePostCommandHandler() {
        //
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
        vfsHandler.setJSJobUtilites(objJSJobUtilities);
        try {
            if (!isConnected) {
                this.connect();
            }
            vfsHandler.openSession(objOptions);
            if (!objOptions.command.IsEmpty()) {
                strCommands2Execute = objOptions.command.values();
            } else {
                if (objOptions.isScript()) {
                    strCommands2Execute = new String[1];
                    String strTemp = objOptions.commandScript.getValue();
                    if (objOptions.commandScript.IsEmpty()) {
                        strTemp = objOptions.commandScriptFile.getJSFile().file2String();
                    }
                    strTemp = objJSJobUtilities.replaceSchedulerVars(flgIsWindowsShell, strTemp);
                    strCommands2Execute[0] = vfsHandler.createScriptFile(strTemp);
                    strCommands2Execute[0] += " " + objOptions.commandScriptParam.getValue();
                } else {
                    throw new SSHMissingCommandError(objMsg.getMsg(SOS_SSH_E_100));
                }
            }
            for (String strCmd : strCommands2Execute) {
                try {
                    strCmd = objJSJobUtilities.replaceSchedulerVars(flgIsWindowsShell, strCmd);
                    LOGGER.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
                    vfsHandler.executeCommand(strCmd);
                    objJSJobUtilities.setJSParam(conExit_code, "0");
                    checkStdOut();
                    checkStdErr();
                    checkExitCode();
                    changeExitSignal();
                } catch (Exception e) {
                    throw new SSHExecutionError("Exception raised: " + e, e);
                }
            }
        } catch (Exception e) {
            String strErrMsg = "SOS-SSH-E-120: error occurred processing ssh command: ";
            throw new SSHExecutionError(strErrMsg, e);
        } finally {
            if (!keepConnected) {
                disconnect();
            }
        }
        return this;
    }

}