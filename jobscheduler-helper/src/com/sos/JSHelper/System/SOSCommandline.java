package com.sos.JSHelper.System;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.log4j.Logger;

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
		Vector resultVector = new Vector();
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
			throw new Exception("error occurred splitting arguments: " + e.getMessage());
		}
	}

	/**
	 * executes a command
	 */
	public Vector execute(final String command) {
		return this.execute(command, null);
	}

	/**
	 * executes a command
	 */
	public Vector execute(final String command, final Object objDummy) {
		Vector returnValues = new Vector();
		try {
			try { // to execute command
				Process p = Runtime.getRuntime().exec(splitArguments(command));
				final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				p.waitFor();
				logger.debug("command returned exit code: " + p.exitValue());
				returnValues.add(0, new Integer(p.exitValue()));
				// logger.debug("number of characters available from stdout: " + p.getInputStream().available());
				// logger.debug("number of characters available from stderr: " + p.getErrorStream().available());
				try { // to process output to stdout
					String line = "";
					String stdout = "";
					while (line != null) {
						line = stdInput.readLine();
						if (line != null) {
							stdout += line;
						}
					}
					try {
						if (logger != null) {
							if (stdout != null && stdout.trim().length() > 0) {
								logger.debug("Command returned output to stdout ...");
							}
							else {
								logger.debug("Command did not return any output to stdout.");
							}
						}
					}
					catch (Exception exc) {
					}
					returnValues.add(1, stdout);
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
						;
					}
				}
				try { // to process output to stderr
					String line = "";
					String stderr = "";
					while (line != null) {
						line = stdError.readLine();
						if (line != null) {
							stderr += line;
						}
					}
					try {
						if (logger != null) {
							if (stderr != null && stderr.trim().length() > 0) {
								logger.debug("Command returned output to stderr ...");
							}
							else {
								logger.debug("Command did not return any output to stderr.");
							}
						}
					}
					catch (Exception exc) {
					}
					returnValues.add(2, stderr);
				}
				catch (Exception ex) {
					returnValues.add(2, ex.getMessage());
					if (logger != null) {
						try {
							logger.debug("error occurred processing stderr: " + ex.getMessage());
						}
						catch (Exception exc) {
						}
						;
					}
				}
				if (stdInput != null)
					stdInput.close();
				if (stdError != null)
					stdError.close();
			}
			catch (Exception ex) {
				returnValues.add(0, new Integer(1));
				returnValues.add(1, "");
				returnValues.add(2, ex.getMessage());
				if (logger != null) {
					try {
						logger.debug("error occurred executing command: " + ex.getMessage());
					}
					catch (Exception exc) {
					}
					;
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
			returnValues.add(0, new Integer(1));
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
				Vector returnValues = execute(command, logger);
				Integer exitValue = (Integer) returnValues.elementAt(0);
				if (exitValue.compareTo(new Integer(0)) == 0) {
					if ((String) returnValues.elementAt(1) != null) {
						returnPassword = (String) returnValues.elementAt(1);
					}
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
