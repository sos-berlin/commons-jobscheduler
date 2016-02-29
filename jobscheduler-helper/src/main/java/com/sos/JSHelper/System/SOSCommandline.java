package com.sos.JSHelper.System;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.log4j.Logger;

/** @author Andreas Püschel */
public class SOSCommandline {

    private static final Logger LOGGER = Logger.getLogger(SOSCommandline.class);

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
        } catch (Exception e) {
            throw new Exception("error occurred splitting arguments: " + e.getMessage());
        }
    }

    public Vector execute(final String command) {
        return this.execute(command, null);
    }

    public Vector execute(final String command, final Object objDummy) {
        Vector returnValues = new Vector();
        try {
            try {
                Process p = Runtime.getRuntime().exec(splitArguments(command));
                final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                p.waitFor();
                LOGGER.debug("command returned exit code: " + p.exitValue());
                returnValues.add(0, new Integer(p.exitValue()));
                try {
                    String line = "";
                    String stdout = "";
                    while (line != null) {
                        line = stdInput.readLine();
                        if (line != null) {
                            stdout += line;
                        }
                    }
                    if (stdout != null && !stdout.trim().isEmpty()) {
                        LOGGER.debug("Command returned output to stdout ...");
                    } else {
                        LOGGER.debug("Command did not return any output to stdout.");
                    }
                    returnValues.add(1, stdout);
                } catch (Exception ex) {
                    returnValues.add(1, "");
                    returnValues.add(2, ex.getMessage());
                    LOGGER.debug("error occurred processing stdout: " + ex.getMessage());
                }
                try {
                    String line = "";
                    String stderr = "";
                    while (line != null) {
                        line = stdError.readLine();
                        if (line != null) {
                            stderr += line;
                        }
                    }
                    if (stderr != null && !stderr.trim().isEmpty()) {
                        LOGGER.debug("Command returned output to stderr ...");
                    } else {
                        LOGGER.debug("Command did not return any output to stderr.");
                    }
                    returnValues.add(2, stderr);
                } catch (Exception ex) {
                    returnValues.add(2, ex.getMessage());
                    LOGGER.debug("error occurred processing stderr: " + ex.getMessage());
                }
                if (stdInput != null) {
                    stdInput.close();
                }
                if (stdError != null) {
                    stdError.close();
                }
            } catch (Exception ex) {
                returnValues.add(0, new Integer(1));
                returnValues.add(1, "");
                returnValues.add(2, ex.getMessage());
                LOGGER.debug("error occurred executing command: " + ex.getMessage());
            }
        } catch (Exception e) {
            LOGGER.debug("Command could not be executed successfully: " + e.getMessage());
            returnValues.add(0, new Integer(1));
            returnValues.add(1, "");
            returnValues.add(2, e.getMessage());
        }
        return returnValues;
    }

    public String getExternalPassword(final String password) {
        return getExternalPassword(password, null);
    }

    public String getExternalPassword(final String password, final Object objdummy) {
        String returnPassword = password;
        try {
            if (password != null && password.startsWith("`") && password.endsWith("`")) {
                String command = password.substring(1, password.length() - 1);
                LOGGER.debug("Trying to get password by executing command in backticks: " + command);
                Vector returnValues = execute(command, LOGGER);
                Integer exitValue = (Integer) returnValues.elementAt(0);
                if (exitValue.compareTo(new Integer(0)) == 0) {
                    if ((String) returnValues.elementAt(1) != null) {
                        returnPassword = (String) returnValues.elementAt(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Using password string as password. Command could not be executed: " + e);
        }
        return returnPassword;
    }

}
