package sos.scheduler.db;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.VirtualFileSystem.shell.cmdShell;

/**
 * \class 		SOSSQLPlusJob - Workerclass for "Start SQL*Plus client and execute a sql*plus script"
 *
 * \brief AdapterClass of SOSSQLPlusJob for the SOSJobScheduler
 *
 * This Class SOSSQLPlusJob is the worker-class.
 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\SOSSQLPlusJob.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse - Kopie\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20120927164040
 * \endverbatim
 */
public class SOSSQLPlusJob extends JSJobUtilitiesClass <SOSSQLPlusJobOptions> {
	private final String			conClassName	= "SOSSQLPlusJob";
	private static Logger			logger			= Logger.getLogger(SOSSQLPlusJob.class);

	/**
	 *
	 * \brief SOSSQLPlusJob
	 *
	 * \details
	 *
	 */
	public SOSSQLPlusJob() {
		super(new SOSSQLPlusJobOptions());
	}

	/**
	 *
	 * \brief Execute - Start the Execution of SOSSQLPlusJob
	 *
	 * \details
	 *
	 * For more details see
	 *
	 * \see JobSchedulerAdapterClass
	 * \see SOSSQLPlusJobMain
	 *
	 * \return SOSSQLPlusJob
	 *
	 * @return
	 */
	public SOSSQLPlusJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";

		JSJ_I_110.toLog(conMethodName);

		try {
			Options().CheckMandatory();
			logger.debug(objOptions.dirtyString());
			cmdShell objShell = new cmdShell();
			String strCommand = objOptions.shell_command.OptionalQuotedValue();
			if (objShell.isWindows() == true) {
				strCommand = "echo 1 | " + strCommand;
			}

			String strCommandParams = "";

			if (objOptions.CommandLineOptions.IsNotEmpty() == true) {  // don't use isDirty because the defaults should be set anyway
				strCommand += " " + objOptions.CommandLineOptions.Value();
			}

			String strDBConn = objOptions.getConnectionString();
			if (strDBConn.length() > 0) {
				strCommandParams += " " + strDBConn;
			}

			File objTempFile = File.createTempFile("sos", ".sql");
			String strTempFileName = objTempFile.getAbsolutePath();
			objOptions.command_script_file.CheckMandatory();
			if (objOptions.command_script_file.isDirty() == true) {
				strCommandParams += " @" + strTempFileName;
			}

			/**
			 * create Tempfile
			 */

			HashMap<String, String> objSettings = objOptions.Settings4StepName();
			JSTextFile objTF = new JSTextFile(strTempFileName);

			for (final Object element : objSettings.entrySet()) {
				@SuppressWarnings("unchecked")
				final Map.Entry<String, String> mapItem = (Map.Entry<String, String>) element;
				String strMapKey = mapItem.getKey().toString();
				strMapKey = sqlPlusVariableName(strMapKey);

				if (mapItem.getValue() != null) {
					String strT = String.format("DEFINE %1$s = %2$s (char)", strMapKey, AddQuotes(mapItem.getValue().toString()));
					logger.debug (strT);
					objTF.WriteLine(strT);
				}
			}

			if (objOptions.include_files.isDirty() == true) {
				String strA[] = objOptions.include_files.Value().split(";");
				for (String strFileName2Include : strA) {
					logger.debug(String.format("Append file '%1$s' to script", strFileName2Include));
					objTF.AppendFile(strFileName2Include);
				}
			}

			final String conNL = System.getProperty("line.separator");

			objOptions.Shell_command_Parameter.Value(strCommandParams);
			// the options class will return the script, even it the value of the Options is a filename
			String strFC = objOptions.command_script_file.Value();
//			String strFC = objOptions.command_script_file.unescapeXML();
			strFC = objJSJobUtilities.replaceSchedulerVars(false, strFC);
//			File cFile = new File(objOptions.command_script_file.getStrFileName());
			logger.debug(objOptions.command_script_file.Value());
			// dangerous: the value of the param could be a script, not in all cases a file name
			// if it is a filename and this file is accessible, the variable sstrFC will hold already the content of the file
//			if (!cFile.exists()){
// 				throw new JobSchedulerException(String.format("Commandfile %s does not exist", cFile.getAbsolutePath()));
//			}
			strFC += "\n" + "exit;\n";
			objTF.WriteLine(strFC);
			//			objTF.close();

			/**
			 * if an ORA- Error is occured, the intCC can contain the number of the Message
			 */
//			int intCC = objShell.executeCommandWithoutDebugCommand(strCommand);
			int intCC = objShell.executeCommand(objOptions);
			String strCC = String.valueOf(intCC);
			String f = "00000";
			strCC = f.substring(0,strCC.length()-1) + strCC;

			String strSQLError = "";
			int intRegExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;
			String strStdOut = objShell.getStdOut();
			String strA[] = strStdOut.split(conNL);

			boolean flgAVariableFound = false;
			String strRegExp = objOptions.VariableParserRegExpr.Value();
			if (strRegExp.length() >= 0) {
				Pattern objRegExprPattern = Pattern.compile(strRegExp, intRegExpFlags);
				for (String string : strA) {
					Matcher objMatch = objRegExprPattern.matcher(string);
					if (objMatch.matches() == true) {
						objJSJobUtilities.setJSParam(objMatch.group(1), objMatch.group(2).trim());
						flgAVariableFound = true;
					}
				}
			}

			if (flgAVariableFound == false) {
				logger.info(String.format("no JS-variable definitions found using reg-exp '%1$s'.", strRegExp));
			}

			/**
			 * sql*plus gives an \r as a last character in the line on windows.
			 * that's why we use must Pattern.DOTALL. otherwise the regexpr match will fail.
			 */
			boolean flgIgnoreSP2MsgNo = false;
			if (objOptions.ignore_sp2_messages.contains("*all")) {
				flgIgnoreSP2MsgNo = true;
			}

			boolean flgIgnoreOraMsgNo = false;
			if (objOptions.ignore_ora_messages.contains("*all")) {
				flgIgnoreOraMsgNo = true;
			}
			// TODO the regular expressions for error parsing should be a parameter
			Pattern objErrorPattern = Pattern.compile("^\\s*SP2-(\\d\\d\\d\\d):\\s*(.*)$", intRegExpFlags);
			Pattern objORAPattern = Pattern.compile("^ORA-(\\d\\d\\d\\d\\d):\\s*(.*)$", intRegExpFlags);
			for (String strStdoutLine : strA) {
				strStdoutLine = strStdoutLine.trim();
				Matcher objMatch = objErrorPattern.matcher(strStdoutLine);
				Matcher objMatch2 = objORAPattern.matcher(strStdoutLine);
				if (objMatch.matches() == true || objMatch2.matches() == true) {
					boolean flgIsError = false;
					if (objMatch.matches() == true && flgIgnoreSP2MsgNo == false) {
						String strMsgNo = objMatch.group(1).toString();
						if (objOptions.ignore_sp2_messages.contains(strMsgNo) == false) {
							flgIsError = true;
						}
					}
					if (objMatch2.matches() == true && flgIgnoreOraMsgNo == false) {
						String strMsgNo = objMatch2.group(1).toString();
						if (objOptions.ignore_ora_messages.contains(strMsgNo) == false) {
							flgIsError = true;
						}
					}
					if (flgIsError == true) {
						strSQLError += strStdoutLine + conNL;
						logger.debug("error found: " + strStdoutLine);
						objJSJobUtilities.setStateText(strStdoutLine);
					}
					else {
						logger.info (String.format("Error '%1$s' ignored due to settings",  strStdoutLine));
					}
				}else{
					objJSJobUtilities.setStateText("");
				}
			}

			String strStdErr = objShell.getStdErr();
			objJSJobUtilities.setJSParam(conSettingSTD_OUT_OUTPUT, strStdOut.trim());
			objJSJobUtilities.setJSParam(conSettingSTD_ERR_OUTPUT, strStdErr.trim());
			objJSJobUtilities.setJSParam(conSettingSQL_ERROR, strSQLError.trim());

			if (intCC == 0) {
				if (strStdErr.trim().length() > 0) {
					intCC = 99;
				}
				if (strSQLError.length() > 0) {
					intCC = 98;
				}
			}
			objJSJobUtilities.setJSParam(conSettingEXIT_CODE, "" + intCC);
			if (intCC != 0 && objOptions.ignore_ora_messages.contains(strCC) == false) {
 				throw new JobSchedulerException(String.format("Exit-Code set to '%1$s': %2$s", "" + intCC,strSQLError.trim()));
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(JSJ_F_107.get(conMethodName) + ": "+ e.getMessage(), e);
		}
		finally {
		}

		JSJ_I_111.toLog(conMethodName);
		return this;
	}

	public String sqlPlusVariableName(String s){
		if (s.length() > 30){
			s = s.substring(0,29) + "_";
		}
		return s;
	}


} // class SOSSQLPlusJob