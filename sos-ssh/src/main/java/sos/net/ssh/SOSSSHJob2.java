package sos.net.ssh;

import java.util.Vector;

import org.apache.log4j.Logger;

import sos.net.ssh.exceptions.SSHConnectionError;
import sos.net.ssh.exceptions.SSHExecutionError;
import sos.net.ssh.exceptions.SSHMissingCommandError;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.SSH.SOSSSH2TriLeadImpl;
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
public abstract class SOSSSHJob2 extends JSJobUtilitiesClass<SOSSSHJobOptions> {

	private final String conClassName = this.getClass().getSimpleName();
	protected final Logger logger = Logger.getLogger(this.getClass());

	private final String		conSVNVersion		= "$Id$";

	private final String		conStd_err_output	= "std_err_output";
	private final String		conStd_out_output	= "std_out_output";
	protected final String		conExit_code		= "exit_code";
	private final String		conExit_signal		= "exit_signal";

	protected Msg				objMsg				;

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
  protected
	/*!
	 * \var SOS-SSH-D-110
	 * \brief SOS-SSH-D-110: executing remote command: '%1$s'.
	 */
	static final String	SOS_SSH_D_110		= "SOS-SSH-D-110";

	public boolean				isConnected			= false;
	public boolean				flgIsWindowsShell	= false;
	public boolean				keepConnected		= false;

	/** array of commands that have been separated by the commandDelimiter */
	protected String[]			strCommands2Execute	= {};

	/** Output from stdout and stderr **/
	protected StringBuffer		strbStdoutOutput;
	protected StringBuffer		strbStderrOutput;
  // http://www.sos-berlin.com/jira/browse/JITL-112
  protected Vector<String> tempFilesToDelete = new Vector<String>();

  private ISOSVFSHandler    objVFS        = null;
  
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
	protected ISOSVFSHandler getVFS() {
	  // http://www.sos-berlin.com/jira/browse/JITL-112: get the Handler from the correct Job Extension Class
	  objVFS = getVFSSSH2Handler();
    // http://www.sos-berlin.com/jira/browse/JITL-112: second instance with own session for pre/post commands
    preparePostCommandHandler();
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
	  objMsg        = new Msg(new BundleBaseName(this.getClass().getAnnotation(I18NResourceBundle.class).baseName()));

		logger.info(conSVNVersion);
		getVFS();
		// http://www.sos-berlin.com/jira/browse/JITL-112: 
		//   generateTemporaryFilename() has to be called once to generate a temporary filename 
		//   to use if return values have to be stored
		generateTemporaryFilename();
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

	@SuppressWarnings("deprecation")
  public SOSSSHJob2 Connect() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Connect";

		getVFS();
		Options().CheckMandatory();
		// logging commented, because this would be the third time these Options are logged.
		// first while logging the CommandLineArgs in the Option class itself
		// second while logging the DumpSettings in the Option class itself
		// no need for a third one [SP]
//		logger.debug(Options().dirtyString());

		try {
			objVFS.Connect(objOptions);
			ISOSAuthenticationOptions objAU = objOptions;
			@SuppressWarnings("unused")
      ISOSConnection authenticate = objVFS.Authenticate(objAU);
			logger.debug("connection established");
		}
		catch (Exception e) {
			throw new SSHConnectionError("Error occured during connection/authentication: " + e.getLocalizedMessage(), e);
		}

		flgIsWindowsShell = objVFS.remoteIsWindowsShell();
		isConnected = true;
		
    // http://www.sos-berlin.com/jira/browse/JITL-112: 
    //   preparePostCommandHandler() has to be called once to generate a 
		//   second instance for post processing of stored return values
		preparePostCommandHandler();
		return this;
	} // private SOSSSHJob2 Connect
	
	
	// moved to extending classes for JCraft and Trilead Job implementation 
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
	public abstract SOSSSHJob2 Execute() throws Exception;
	// different implementations for trilead and JSch, therefore moved to the extending classes
	// SOSSSHJobTrilead respectively SOSSSHJobJSch

	public void DisConnect() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::DisConnect";

		if (isConnected == true) {
			try {
        objVFS.CloseConnection();
			} catch (Exception e) {
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
				} else {
					// TODO set next state to name### or to name
					String strM = "SOS-SSH-E-150: remote command terminated with exit code: " + intExitCode;
					objJSJobUtilities.setCC(intExitCode);
					if (objOptions.raise_exception_on_error.isTrue()) {
					  if(objOptions.ignore_error.value()){
	            logger.info(strM);
					  }else{
					    logger.error(strM);
					  }
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
	public void CheckStdErr() {
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
			if (objOptions.ignore_stderr.value()) {
				logger.info("SOS-SSH-I-150: output to stderr is ignored: " + strbStderrOutput);
			}
			else {
        String strM = "SOS-SSH-E-160: remote execution reports error: " + strbStderrOutput;
        logger.error(strM);
				if(objOptions.raise_exception_on_error.value()){
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
	public void CheckStdOut() {
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

  public abstract void generateTemporaryFilename();

  public abstract String getTempFileName();
  
  public abstract String getPreCommand();

  // http://www.sos-berlin.com/jira/browse/JITL-112
  public abstract void processPostCommands(String tmpReturnValueFileName);
  
  public abstract void preparePostCommandHandler();
  
  public abstract ISOSVFSHandler getVFSSSH2Handler();

} // SOSSSHJob2
