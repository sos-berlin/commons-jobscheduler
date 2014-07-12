package com.sos.JSHelper.System;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * @author Andreas Püschel <andreas.pueschel@sos-berlin.com>
 * @since 2009-02-20
 * @version 1.0
 *
 * Command line processing
 */
public class SOSCommandline {
	@SuppressWarnings("unused")
	private final String	conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused")
	private final Logger	logger			= Logger.getLogger("SOSCommandline");

	/**
	 * Parse and split command line arguments
	 */
	public String[] splitArguments(final String arguments) throws Exception {
		String[] resultArguments = null;
		Vector <String> resultVector = new Vector<>();
		int resultIndex = 0;
		String resultString = "";
		boolean inQuote = false;
		try {
			for (int i = 0; i < arguments.length(); i++) {
				if (arguments.substring(i).startsWith("\\\"")) {
					int pos1 = i;
					int pos2 = arguments.indexOf("\\\"", pos1 + 1);
					if (pos2 > -1) {
						//resultString = arguments.substring(pos1, pos2+5);
						resultString = arguments.substring(pos1, pos2 + 1);
						resultVector.add(resultIndex++, resultString);
						resultString = "";
						i = pos2 + 1;
						continue;
					}
				}
				if (arguments.substring(i).startsWith("\'")) {
					int pos1 = i;
					int pos2 = arguments.indexOf("\'", pos1 + 1);
					if (pos2 > -1) {
						//resultString = arguments.substring(pos1, pos2+5);
						resultString = arguments.substring(pos1, pos2 + 1);
						resultVector.add(resultIndex++, resultString);
						resultString = "";
						i = pos2 + 1;
						continue;
					}
				}
				if (inQuote) {
					resultString += arguments.charAt(i);
					continue;
				}
				if (arguments.charAt(i) != ' ') {
					resultString += arguments.charAt(i);
				}
				else {
					resultVector.add(resultIndex++, resultString);
					resultString = "";
				}
			}
			if (resultString.trim().length() > 0) {
				resultVector.add(resultIndex++, resultString);
			}
			resultArguments = new String[resultIndex];
			resultVector.copyInto(resultArguments);
			return resultArguments;
			/*//original
			for (int i=0; i<arguments.length(); i++) {
			    if (arguments.charAt(i) == '\'' ) {
			       inQuote = !inQuote;
			    }



			    if (inQuote) { 
			        resultString += arguments.charAt(i); 
			        continue;
			    }

			    if (arguments.charAt(i) != ' ') {
			        resultString += arguments.charAt(i);
			    } else {
			        resultVector.add(resultIndex++, resultString);
			        resultString = "";
			    }
			}

			if (resultString.trim().length() > 0) {
			    resultVector.add(resultIndex++, resultString);
			}

			resultArguments = new String[resultIndex];
			resultVector.copyInto(resultArguments);
			return resultArguments;
			 */}
		catch (Exception e) {
			throw new JobSchedulerException("error occurred splitting arguments: " + e.getMessage(), e);
		}
	}

	/**
	 * executes a command
	 */
	public Vector<String> execute(final String command) {
		return this.execute(command, null);
	}

	Vector<String> returnValues = new Vector<>();
	private BufferedReader stbStdInput = null;
	private BufferedReader stbStdError = null;
	
	public String getStdOut() {
		if (stdOut == null) {
			stdOut = new StringBuffer("");
		}
		return stdOut.toString();
	}

	public int getExitValue() {
		return exitValue;
	}

	public String getStdError() {
		if (stdError == null) {
			stdError = new StringBuffer("");
		}
		return stdError.toString();
	}

	private StringBuffer stdOut = null;
	private StringBuffer stdError = null;
	private int exitValue = 0;
	/**
	 * executes a command
	 */
	public Vector<String> execute(final String command, final Object objDummy) {

		try {
			try { // to execute command
				Process p = Runtime.getRuntime().exec(splitArguments(command));
				stbStdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				stbStdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				p.waitFor();
				exitValue = p.exitValue();
				logger.debug("command returned exit code: " + exitValue);
				returnValues.add(0, exitValue + "");
				// logger.debug("number of characters available from stdout: " + p.getInputStream().available());
				// logger.debug("number of characters available from stderr: " + p.getErrorStream().available());
				try { // to process output to stdout
					String line = "";
					stdOut = new StringBuffer("");
					while (line != null) {
						line = stbStdInput.readLine();
						if (line != null) {
							stdOut.append(line);
						}
					}
					try {
						if (logger != null) {
							if (stdOut != null && stdOut.length() > 0) {
								logger.debug("Command returned output to stdout ...");
							}
							else {
								logger.debug("Command did not return any output to stdout.");
							}
						}
					}
					catch (Exception exc) {
					}
					returnValues.add(1, stdOut.toString());
				}
				catch (Exception ex) {
					returnValues.add(1, "");
					returnValues.add(2, ex.getMessage());
					if (logger != null) {
						try {
							logger.debug("error occurred processing stdout: " + ex.getMessage());
						}
						catch (Exception exc) {
						}
					}
				}
				try { // to process output to stderr
					String line = "";
					while (line != null) {
						line = stbStdError.readLine();
						if (line != null) {
							stdError.append(line);
						}
					}
					try {
						if (logger != null) {
							if (stdError != null && stdError.length() > 0) {
								logger.debug("Command returned output to stderr ...");
								logger.debug(stdError);
								returnValues.add(2, stdError.toString());
							}
							else {
								logger.debug("Command did not return any output to stderr.");
								returnValues.add(2, "");
							}
						}
					}
					catch (Exception exc) {
					}
				}
				catch (Exception ex) {
					returnValues.add(2, ex.getMessage());
					if (logger != null) {
						try {
							logger.debug("error occurred processing stderr: " + ex.getMessage(), ex);
						}
						catch (Exception exc) {
						}
					}
				}
				if (stbStdInput != null)
					stbStdInput.close();
				if (stbStdError != null)
					stbStdError.close();
			}
			catch (Exception ex) {
				returnValues.add(0, "1");
				returnValues.add(1, stdError.toString());
				returnValues.add(2, ex.getMessage());
				if (logger != null) {
					try {
						logger.debug("error occurred executing command: " + ex.getMessage());
					}
					catch (Exception exc) {
					}
				}
			}
		}
		catch (Exception e) {
			try {
				if (logger != null)
					logger.debug("Command could not be executed successfully: " + e.getMessage());
			}
			catch (Exception ex) {
			}
			returnValues.add(0, "1");
			returnValues.add(1, "");
			returnValues.add(2, e.getMessage());
		}
		return returnValues;
	}

	/**
	 * Checks if a command needs to be executed to get the password
	 */
	public String getExternalPassword(final String password) {
		return getExternalPassword(password, null);
	}

	/**
	 * Checks if an external command needs to be executed to get the password
	 */
	public String getExternalPassword(final String password, final Object objdummy) {
		String returnPassword = password;
		try {
			if (password != null && password.startsWith("`") && password.endsWith("`")) {
				String command = password.substring(1, password.length() - 1);
				try {
					if (logger != null)
						logger.debug("Trying to get password by executing command in backticks: " + command);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
//				Vector<?> returnValues = execute(command, logger);
				int exitValue = getExitValue();
				if (exitValue == 0) {
					returnPassword = getStdOut();
				}
			}
		}
		catch (Exception e) {
			if (logger != null) {
				try {
					logger.debug("Using password string as password. Command could not be executed: " + e);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return returnPassword;
	}
	//	public  void main(String[] args) {
	//		try {
	//			SOSStandardLogger logger = new SOSStandardLogger(9);
	//			//String password = "hallo";
	//			//String password = "`c:/sosftp/getpassword.cmd`";    		
	//			//String password = "`c:/sosftp/getpassword.cmd \"ggg\"aa `";   		    		
	//			String password = "`c:/sosftp/getpassword.cmd \"a\" \"b\" `";
	//
	//			//String password = "`c:/sosftp/getpassword.cmd \"a\" \"b\" `"; //resultvector hat drei einträge[c:/sosftp/getpassword.cmd, "a", "b"]
	//			//String password = "`c:/sosftp/getpassword.cmd \"a\" b `"; //resultvector hat drei einträge[c:/sosftp/getpassword.cmd, "a", b]
	//			//String password = "`c:/sosftp/getpassword.cmd a\'b `"; //resultvector hat zwei einträge [c:/sosftp/getpassword.cmd, a'b ]
	//			//String password = "`c:/sosftp/getpassword.cmd 'a' 'b' `"; //resultvector hat drei einträge[c:/sosftp/getpassword.cmd, 'a', 'b' ]
	//
	//			//String password = "`a b`"; //resultvector hat zwei einträge [a, b]
	//			//String password = "`a\"b`"; //resultvector hat ein eintrag  [a"b]
	//
	//
	//			//ab hier test ok
	//
	//			//String password = "`c:/sosftp/getpassword.cmd 'a b' `";  //resultvector hat zwei einträge  [c:/sosftp/getpassword.cmd, 'a b']
	//			//String password = "`cmd \"a b\"`"; //resultvector hat drei einträge  [c:/sosftp/getpassword.cmd, "a, b"] 
	//			//String password = "`cmd a b`"; //resultvector hat 3 einträge  [cmd, a, b]
	//			//String password = "`c:/sosftp/getpassword.cmd ggg\"aa `";//resultvector hat zwei einträge  [c:/sosftp/getpassword.cmd, ggg"aa]
	//			//String password = "`c:/sosftp/getpassword.cmd 'ggg\"aa' `";//resultvector hat zwei einträge  [c:/sosftp/getpassword.cmd, 'ggg"aa']
	//			//String password = "`c:/sosftp/getpassword.cmd \"a\" \"b\" xyz `"; //resultvector hat 4 einträge [c:/sosftp/getpassword.cmd, "a", "b", xyz]		
	//			//String password = "`c:/sosftp/getpassword.cmd '\"a\" \"b\"' xyz `"; //resultvector hat 3 einträge [c:/sosftp/getpassword.cmd, '"a" "b"', xyz]		
	//
	//			//String password = "`c:/sosftp/getpassword.cmd \"'a b'\"`";  //resultvector hat 2 einträge [c:/sosftp/getpassword.cmd,  'a b']  
	//
	//            password = "`\\tmp\\lGetPasswd.cmd ssh wilma.sos sos`";
	//            System.out.println(getExternalPassword(password, logger));
	//		} catch (Exception e){
	//			System.out.println("error:" + e.toString() );
	//		}
	//
	//	}
}
