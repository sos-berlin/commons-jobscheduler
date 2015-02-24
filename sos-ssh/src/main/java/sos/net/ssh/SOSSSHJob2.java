package sos.net.ssh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.net.ssh.exceptions.SSHMissingCommandError;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.Msg;
import com.sos.i18n.Msg.BundleBaseName;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class SOSSSHJob2 - Start a Script or command using SSH
 *
 * \brief Start a Script using SSH
 *
 * Start a Script using SSH
 *
 * \section
 *
 * \see
 *
 * \code ....
 * code goes here ...
 * \endcode
 *
* @version $Id$
 *
 * This Source-Code was created by JETTemplate SimpleClass.javajet, Version 1.0
 * from 2009-12-26, written by kb
 */

@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJob2 extends JSJobUtilitiesClass<SOSSSHJobOptions> {

	private final String conClassName = this.getClass().getSimpleName();
	private final Logger logger = Logger.getLogger(this.getClass());

	@SuppressWarnings("unused")
	private final String		conSVNVersion		= "$Id$";

	private final String		conStd_err_output	= "std_err_output";
	private final String		conStd_out_output	= "std_out_output";
	private final String		conExit_code		= "exit_code";
	private final String		conExit_signal		= "exit_signal";

	protected Msg				objMsg				= new Msg(new BundleBaseName(this.getClass().getAnnotation(I18NResourceBundle.class).baseName()));

	@I18NMessages(value = { @I18NMessage("neither Commands nor Script(file) specified. Abort."), //
			@I18NMessage(value = "neither Commands nor Script(file) specified. Abort.", locale = "en_UK", //
			explanation = "neither Commands nor Script(file) specified. Abort." //
			), //
			@I18NMessage(value = "Es wurde weder ein Kommando noch eine Kommandodatei angegeben. Abbruch.", locale = "de", //
			explanation = "neither Commands nor Script(file) specified. Abort." //
			) //
	}, msgnum = "SOS-SSH-E-100", msgurl = "msgurl")
	/*!
	 * \var MsgKey
	 * \brief neither Commands nor Script(file) specified. Abort.
	 */
	public static final String	SOS_SSH_E_100		= "SOS-SSH-E-100";

	@I18NMessages(value = { @I18NMessage("executing remote command: '%1$s'."), //
			@I18NMessage(value = "executing remote command: '%1$s'.", locale = "en_UK", //
			explanation = "executing remote command: '%1$s'." //
			), //
			@I18NMessage(value = "starte am remote-server das Kommando: '%1$s'.", locale = "de", //
			explanation = "executing remote command: '%1$s'." //
			) //
	}, msgnum = "SOS-SSH-D-110", msgurl = "msgurl")
	/*!
	 * \var SOS-SSH-D-110
	 * \brief SOS-SSH-D-110: executing remote command: '%1$s'.
	 */
	private static final String	SOS_SSH_D_110		= "SOS-SSH-D-110";
  private static final String COMMAND_DELIMITER = ";";
  private static final String SCHEDULER_RETURN_VALUES = "SCHEDULER_RETURN_VALUES";
  private static final String SCHEDULER_RETURN_VALUES_PARAM_LINUX = "$SCHEDULER_RETURN_VALUES";
  private static final String SCHEDULER_RETURN_VALUES_PARAM_WINDOWS = "%SCHEDULER_RETURN_VALUES%";

	public boolean				isConnected			= false;
	public boolean				flgIsWindowsShell	= false;
	public boolean				keepConnected		= false;

	/** array of commands that have been separated by the commandDelimiter */
	protected String[]			strCommands2Execute	= {};

	/** Output from stdout and stderr **/
	protected StringBuffer		strbStdoutOutput;
	protected StringBuffer		strbStderrOutput;
  private String tmpReturnValueFileName;
  private String exportEnvVariableCommand;

	private ISOSVFSHandler		objVFS				= null;

	public String[] getCommands2Execute() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getCommands2Execute";

		return strCommands2Execute;
	} // private String[] getCommands2Execute

	/**
	 *
	 * \brief getVFS
	 *
	 * \details
	 *
	 * \return ISOSVFSHandler
	 *
	 * @return
	 * @throws Exception
	 */
	private ISOSVFSHandler getVFS() {

		// TODO type of connection/file-system as an option in Option-class

		if (objVFS == null) {
			try {
				objVFS = VFSFactory.getHandler("SSH2");
			}
			catch (Exception e) {
				// TODO msg must be used in the VFSFactory because it is an VFS Msg
				throw new JobSchedulerException("SOS-VFS-E-0010: unable to initialize VFS", e);
			}
		}
		return objVFS;
	}

	/**
	 *
	 * \brief SOSSSHJob2
	 *
	 * \details
	 *
	 */
	public SOSSSHJob2() {
		super(new SOSSSHJobOptions());
		logger.info(conSVNVersion);
		getVFS();
    UUID uuid = UUID.randomUUID();
    tmpReturnValueFileName = "sos-ssh-return-values-" + uuid + ".txt";
    StringBuilder strb = new StringBuilder();
    strb.append("export ")
        .append(SCHEDULER_RETURN_VALUES)
        .append("=")
        .append(tmpReturnValueFileName)
        .append(COMMAND_DELIMITER);
    exportEnvVariableCommand = strb.toString();
	}

	/**
	 *
	 * \brief setJSJobUtilites
	 *
	 * \details
	 * The JobUtilities are a set of methods used by the SSH-Job or can be used be other, similar, job-
	 * implementations.
	 *
	 * \return void
	 *
	 * @param pobjJSJobUtilities
	 */
	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
		super.setJSJobUtilites(pobjJSJobUtilities);
		objVFS.setJSJobUtilites(pobjJSJobUtilities);
	}

	public SOSSSHJob2 Connect() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Connect";

		getVFS();
		Options().CheckMandatory();
		logger.debug(Options().dirtyString());

		try {
			objVFS.Connect(objOptions);
			ISOSAuthenticationOptions objAU = objOptions;
			ISOSConnection authenticate = objVFS.Authenticate(objAU);
			logger.debug("connection established");
		}
		catch (Exception e) {
			throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
		}

		flgIsWindowsShell = objVFS.remoteIsWindowsShell();
		isConnected = true;
		return this;
	} // private SOSSSHJob2 Connect

	/**
	 *
	 * \brief Execute - Start the Execution of commands or scripts on the SSH-Server-Site
	 *
	 * \details
	 * This Method is to start commands or a script on the SSH-Server-Site.
	 * What to start and how to handle is specified in the related Option-Class SOSSSHJobOptions.
	 *
	 * For more details see
	 *
	 * \see SOSSSHJob2JSAdapter
	 * \see SOSSSH2Main
	 *
	 * \return SOSSSHJob2
	 *
	 * @return
	 */
	public SOSSSHJob2 Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";
		boolean flgScriptFileCreated = false; // http://www.sos-berlin.com/jira/browse/JITL-17
		logger.debug(conMethodName + " start ...");

		objVFS.setJSJobUtilites(objJSJobUtilities);

		try {
			if (isConnected == false) {
				this.Connect();
			}

			objVFS.OpenSession(objOptions);

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
					strCommands2Execute[0] = objVFS.createScriptFile(strTemp);
					flgScriptFileCreated = true; // http://www.sos-berlin.com/jira/browse/JITL-17
					strCommands2Execute[0] += " " + objOptions.command_script_param.Value();
				} else {
					throw new SSHMissingCommandError(objMsg.getMsg(SOS_SSH_E_100)); // "SOS-SSH-E-100: neither Commands nor Script(file) specified. Abort.");
				}
			}
			for (String strCmd : strCommands2Execute) {
				try {
          strCmd = exportEnvVariableCommand + strCmd;
					/**
					 * \change Substitution of variables enabled
					 *
					 * see http://www.sos-berlin.com/jira/browse/JS-673
					 *
					 */
					logger.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
					strCmd = objJSJobUtilities.replaceSchedulerVars(flgIsWindowsShell, strCmd);
					logger.debug(String.format(objMsg.getMsg(SOS_SSH_D_110), strCmd));
					objVFS.ExecuteCommand(strCmd);
					objJSJobUtilities.setJSParam(conExit_code, "0");
					CheckStdOut();
					CheckStdErr();
					CheckExitCode();
					ChangeExitSignal();
				} catch (Exception e) {
					logger.error(this.StackTrace2String(e));
					throw new SSHExecutionError("Exception raised: " + e, e);
				} finally {
					if (flgScriptFileCreated == true) { 
					  // http://www.sos-berlin.com/jira/browse/JITL-17
						// file will be deleted by the Vfs Component.
					}
				}
			}
      readReturnValues(tmpReturnValueFileName);
		}
		catch (Exception e) {
			logger.error(this.StackTrace2String(e));
			String strErrMsg = "SOS-SSH-E-120: error occurred processing ssh command: ";
			logger.error(strErrMsg, e);
			throw new SSHExecutionError(strErrMsg, e);
		}
		finally {
			if (keepConnected == false) {
				DisConnect();
			}
		}
		return this;
	}
	
	private void readReturnValues(String tmpFileName){
	  // set the environmental variable to be available for the following script execution
    String cmdTempFileExists = 
        "if [ -f " + SCHEDULER_RETURN_VALUES_PARAM_LINUX + " ]; " + 
        "then cat " + SCHEDULER_RETURN_VALUES_PARAM_LINUX + "; " + 
        "fi;";
    String cmdReadReturnValues = exportEnvVariableCommand + cmdTempFileExists;
    String stdErr = "";
    try {
      objVFS.ExecuteCommand(cmdReadReturnValues);
      // check if command was processed correctly 
      if(objVFS.getExitCode() == 0){
        // read stdout of the cat statement and split the lines
        String[] lines = objVFS.getStdOut().toString().split("\\n");
        logger.debug("received Return Values are: ");
        // check if lines are available in the return values temp file
        if(lines.length > 0 && !lines[0].isEmpty()){
          // extract key value pairs from the read line
          for(String line : lines){
            String[] keyValue = line.split("=");
            objJSJobUtilities.setJSParam(keyValue[0], keyValue[1]);
          }
          // remove temp file after parsing return values from file
          objVFS.ExecuteCommand(exportEnvVariableCommand + "rm " + SCHEDULER_RETURN_VALUES_PARAM_LINUX);
          logger.debug(SOSVfsMessageCodes.SOSVfs_I_0113.params(tmpFileName));
        }else{
          logger.debug("no return values received!");
        }
      }else{
        stdErr = objVFS.getStdErr().toString();
        if(stdErr.length() > 0){
          logger.error(stdErr);
        }
      }
    } catch (Exception e) {
      logger.debug("no temp file for return values found!");
//      e.printStackTrace();
    }
	  
	}

	public void DisConnect() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::DisConnect";

		if (isConnected == true) {
			try {
				objVFS.CloseConnection();
			}
			catch (Exception e) {
				throw new SSHConnectionError("problems closing connection", e);
			}
			isConnected = false;
		}

	} // private void DisConnect

	/**
	 *
	 * \brief ChangeExitSignal
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	public String ChangeExitSignal() {
		String strExitSignal = objVFS.getExitSignal();
		if (isNotEmpty(strExitSignal)) {
			objJSJobUtilities.setJSParam(conExit_signal, strExitSignal);
			if (objOptions.ignore_signal.isTrue()) {
				logger.info("SOS-SSH-I-130: exit signal is ignored due to option-settings: " + strExitSignal);
			}
			else {
				throw new SSHExecutionError("SOS-SSH-E-140: remote command terminated with exit signal: " + strExitSignal);
			}
		}
		else {
			objJSJobUtilities.setJSParam(conExit_signal, "");
		}
		return strExitSignal;
	}

	/**
	 *
	 * \brief CheckExitCode
	 *
	 * \details
	 *
	 * \return Integer
	 *
	 * @return
	 */
	public Integer CheckExitCode() {
		objJSJobUtilities.setJSParam("exit_code_ignored", "false");
		Integer intExitCode = objVFS.getExitCode();
		if (isNotNull(intExitCode)) {
			objJSJobUtilities.setJSParam(conExit_code, intExitCode.toString());
			if (!intExitCode.equals(new Integer(0))) {
				if (objOptions.ignore_error.isTrue() || objOptions.ignore_exit_code.Values().contains(intExitCode)) {
					logger.info("SOS-SSH-E-140: exit code is ignored due to option-settings: " + intExitCode);
					objJSJobUtilities.setJSParam("exit_code_ignored", "true");
				}
				else {
					// TODO set next state to name### or to name
					String strM = "SOS-SSH-E-150: remote command terminated with exit code: " + intExitCode;
					// TODO set logger.error only if RaiseExceptionOnError is true
					logger.error(strM);
					objJSJobUtilities.setCC(intExitCode);
					if (objOptions.RaiseExceptionOnError.isTrue()) {
						throw new SSHExecutionError(strM);
					}
				}
			}
		}
		return intExitCode;
	}

	/**
	 *
	 * \brief CheckStdErr
	 *
	 * \details
	 *
	 * \return void
	 *
	 */
	private void CheckStdErr() {
 		try {
			StringBuffer stbStdErr = objVFS.getStdErr();
			if (strbStderrOutput == null) {
				strbStderrOutput = new StringBuffer();
			}
			strbStderrOutput.append(stbStdErr);
		}
		catch (Exception e) {
			throw new JobSchedulerException(e.getLocalizedMessage(), e);
		}
		if (isNotEmpty(strbStderrOutput)) {
			logger.info("stderr = " + strbStderrOutput.toString());

			objJSJobUtilities.setJSParam(conStd_err_output, strbStderrOutput);
			if (objOptions.ignore_stderr.value() == true) {
				logger.info("SOS-SSH-I-150: output to stderr is ignored: " + strbStderrOutput);
			}
			else {
				intCC =  objVFS.getExitCode();
				
				if (intCC != 0 && objOptions.RaiseExceptionOnError.isTrue()) {
					CheckExitCode();
				}

					String strM = "SOS-SSH-E-160: remote execution reports error: " + strbStderrOutput;
					logger.error(strM);
					if (objOptions.RaiseExceptionOnError.isTrue()) {
						throw new SSHExecutionError(strM);
					}

			}
		}
	}

	public void Clear() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Clear";

		strbStdoutOutput = new StringBuffer();
		strbStderrOutput = new StringBuffer();

	} // private void Clear

	/**
	 *
	 * \brief CheckStdOut
	 *
	 * \details
	 *
	 * \return void
	 *
	 */
	private void CheckStdOut() {
		try {
			StringBuffer stbStdOut = objVFS.getStdOut();
			if (strbStdoutOutput == null) {
				strbStdoutOutput = new StringBuffer();
			}
			strbStdoutOutput.append(stbStdOut);
		}
		catch (Exception e) {
			logger.error(this.StackTrace2String(e));
			throw new JobSchedulerException(e.getLocalizedMessage(), e);
		}
		objJSJobUtilities.setJSParam(conStd_out_output, strbStdoutOutput);
		logger.info("stdout = " + strbStdoutOutput.toString());
	}

	/**
	 *
	 * \brief getStdOut - return the STDOUT from the SSH-Server
	 *
	 * \details
	 * The STDOUT-messages as an result of the execution of the command.
	 *
	 * \return StringBuffer
	 *
	 * @throws Exception
	 */
	public StringBuffer getStdOut() throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getStdOut";

		return this.getVFS().getStdOut();
	} // private StringBuffer getStdOut

	/**
	 *
	 * \brief getStdErr
	 *
	 * \details
	 *
	 * \return StringBuffer
	 *
	 * @return
	 * @throws Exception
	 */
	public StringBuffer getStdErr() throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getStdErr";

		return this.getVFS().getStdErr();
	} // private StringBuffer getStdOut

} // SOSSSHJob2
