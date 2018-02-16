package com.sos.VirtualFileSystem.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.Interfaces.ISOSCmdShellOptions;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;

import sos.util.SOSString;

public class CmdShell extends SOSVfsMessageCodes implements Runnable {

    private static final String CHARACTER_ENCODING = "Cp1252";
    private static final Logger LOGGER = Logger.getLogger(CmdShell.class);
    private String strStdOut = "";
    private String strStdErr = "";
    private int intCC = 0;
    private ISOSCmdShellOptions objShellOptions = null;
    private String strCommand = "";
    String osn = System.getProperty("os.name");
    String fcp = System.getProperty("file.encoding");
    String ccp = System.getProperty("console.encoding");

    public CmdShell() {
        //
    }

    public CmdShell(final ISOSCmdShellOptions pobjOptions) {
        objShellOptions = pobjOptions;
    }

    public boolean isWindows() {
        return osn != null && osn.contains("Windows");
    }

    public String replaceCommand4Windows(final String cmd) {
        String command = cmd;
        command = command.replaceAll("/(?=[^ ]*/)", "\\\\");
        command = command.replaceAll("(?<! )/", "\\\\");
        return command;
    }
    
    public int getCC() {
        return intCC;
    }

    public String getStdOut() {
        return strStdOut;
    }

    public String getStdErr() {
        return strStdErr;
    }

    class OutputPipe implements Runnable {

        private final InputStream in;
        private final PrintStream out;

        OutputPipe(final InputStream in1, final PrintStream out1) {
            in = in1;
            out = out1;
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                for (int n = 0; n != -1; n = in.read(buffer)) {
                    out.write(buffer, 0, n);
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }

    private int executeCommand(final String[] pstrCommand, final boolean showCommand) throws Exception {
        return executeCommand(pstrCommand, showCommand, null);
    }

    private int executeCommand(final String[] pstrCommand, final boolean showCommand, Map<String, String> env) throws Exception {
        ByteArrayOutputStream bytStdOut = new ByteArrayOutputStream();
        ByteArrayOutputStream bytStdErr = new ByteArrayOutputStream();
        PrintStream psStdOut = new PrintStream(bytStdOut, true, CHARACTER_ENCODING);
        PrintStream psStdErr = new PrintStream(bytStdErr, true, CHARACTER_ENCODING);
        strStdOut = "";
        strStdErr = "";
        ProcessBuilder objShell = null;
        if (showCommand) {
            LOGGER.debug(SOSVfs_D_0151.params(pstrCommand));
        }
        objShell = new ProcessBuilder(pstrCommand);
        if (env != null) {
            LOGGER.debug(String.format("set env=%s", env));
            objShell.environment().putAll(env);
        }
        final Process objCommand = objShell.start();
        createOutputPipe(objCommand.getInputStream(), psStdOut);
        createOutputPipe(objCommand.getErrorStream(), psStdErr);
        pipein(System.in, objCommand.getOutputStream());
        intCC = objCommand.waitFor();
        strStdOut = bytStdOut.toString(CHARACTER_ENCODING);
        strStdErr = bytStdErr.toString(CHARACTER_ENCODING);
        String cmd = objShell.command().get(objShell.command().size() - 1);
        if (!SOSString.isEmpty(strStdOut)) {
            LOGGER.info(String.format("[%s][stdout]%s", cmd, strStdOut.trim()));
        }
        if (!SOSString.isEmpty(strStdErr)) {
            LOGGER.info(String.format("[%s][stderr]%s", cmd, strStdErr.trim()));
        }
        return intCC;
    }

    public void setCommand(final String pstrcommand) throws Exception {
        strCommand = pstrcommand;
    }

    public int executeCommand(final String pstrcommand) throws Exception {
        return executeCommand(createCommand(pstrcommand), true);
    }

    public int executeCommand(final String pstrcommand, Map<String, String> env) throws Exception {
        return executeCommand(createCommand(pstrcommand), true, env);
    }

    public int executeCommand(final ISOSCmdShellOptions pobjOptions) throws Exception {
        objShellOptions = pobjOptions;
        String strCommand[] = createCommandUsingOptions();
        return executeCommand(strCommand, true);
    }

    private String[] createCommands(final String pstrComSpec, final String pstrComSpecDefault, final String pstrStartShellCommandParameter) {
        final String[] command = { " ", " ", " " };
        String strComSpec = "";
        int intCmdIndex = 0;
        String strStartShellCommandParameter = pstrStartShellCommandParameter;
        if (objShellOptions.getStartShellCommand().isDirty()) {
            strComSpec = objShellOptions.getStartShellCommand().getValue();
            if (!"none".equalsIgnoreCase(strComSpec)) {
                command[intCmdIndex++] = strComSpec;
                if (objShellOptions.getStartShellCommandParameter().isDirty()) {
                    strStartShellCommandParameter = objShellOptions.getStartShellCommandParameter().getValue();
                    command[intCmdIndex++] = strStartShellCommandParameter;
                }
                command[intCmdIndex++] = objShellOptions.getShellCommand().getValue() + " " + objShellOptions.getCommandLineOptions().getValue() + " "
                        + objShellOptions.getShellCommandParameter().getValue();
            } else {
                command[intCmdIndex++] = objShellOptions.getShellCommand().getValue();
                command[intCmdIndex++] = objShellOptions.getCommandLineOptions().getValue() + " " + objShellOptions.getShellCommandParameter()
                        .getValue();
            }
        } else {
            strComSpec = System.getenv(pstrComSpec);
            if (strComSpec == null) {
                strComSpec = pstrComSpecDefault;
            }
            command[intCmdIndex++] = strComSpec;
            command[intCmdIndex++] = strStartShellCommandParameter;
            command[intCmdIndex++] = objShellOptions.getShellCommand().getValue() + " " + objShellOptions.getCommandLineOptions().getValue() + " "
                    + objShellOptions.getShellCommandParameter().getValue();
        }
        LOGGER.debug(SOSVfs_D_230.params(strComSpec));
        return command;
    }

    private String[] createCommandUsingOptions() {
        if (isWindows()) {
            return createCommands("comspec", "cmd.exe", "/C");
        } else {
            return createCommands("SHELL", "bin.sh", "-c");
        }
    }

    private String[] createCommand(final String pstrCommand) {
        final String[] command = new String[2 + 1];
        if (isWindows()) {
            String strComSpec = System.getenv("comspec");
            LOGGER.debug(SOSVfs_D_230.params(strComSpec));
            command[0] = strComSpec;
            command[1] = "/C";
            command[2] = pstrCommand;
        } else {
            String strComSpec = System.getenv("SHELL");
            if (strComSpec == null) {
                strComSpec = "/bin/sh";
            }
            LOGGER.debug(SOSVfs_D_230.params(strComSpec));
            command[0] = strComSpec;
            command[1] = "-c";
            command[2] = pstrCommand;
        }
        return command;
    }

    public int executeCommandWithoutDebugCommand(final String pstrCommand) throws Exception {
        return executeCommand(createCommand(pstrCommand), false);
    }

    private void pipein(final InputStream src, final OutputStream dest) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    int ret = -1;
                    while ((ret = src.read()) != -1) {
                        dest.write(ret);
                        dest.flush();
                    }
                } catch (IOException e) {
                    //
                }
            }
        }).start();
    }

    private void createOutputPipe(final InputStream in, final PrintStream out) {
        new Thread(new OutputPipe(in, out)).start();
    }

    @Override
    public void run() {
        try {
            this.executeCommand(strCommand);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

}
