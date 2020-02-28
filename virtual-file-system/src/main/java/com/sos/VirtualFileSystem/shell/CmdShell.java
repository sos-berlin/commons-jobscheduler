package com.sos.VirtualFileSystem.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.VirtualFileSystem.Interfaces.ISOSCmdShellOptions;
import com.sos.VirtualFileSystem.common.SOSVfsEnv;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;

import sos.util.SOSString;

public class CmdShell extends SOSVfsMessageCodes implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdShell.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final String CHARACTER_ENCODING = "Cp1252";

    private ISOSCmdShellOptions shellOptions = null;
    private String stdOut = "";
    private String stdErr = "";
    private int exitValue = 0;
    private String command = "";
    private String osn = System.getProperty("os.name");
   
    public CmdShell() {
        //
    }

    public CmdShell(final ISOSCmdShellOptions opt) {
        shellOptions = opt;
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

    public int getExitValue() {
        return exitValue;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    class OutputPipe implements Runnable {

        private final InputStream in;
        private final PrintStream out;

        OutputPipe(final InputStream is, final PrintStream ps) {
            in = is;
            out = ps;
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

    private int executeCommand(final String[] commands, final boolean showCommand) throws Exception {
        return executeCommand(commands, showCommand, null);
    }

    private int executeCommand(final String[] commands, final boolean showCommand, SOSVfsEnv env) throws Exception {
        ByteArrayOutputStream bytStdOut = new ByteArrayOutputStream();
        ByteArrayOutputStream bytStdErr = new ByteArrayOutputStream();
        PrintStream psStdOut = new PrintStream(bytStdOut, true, CHARACTER_ENCODING);
        PrintStream psStdErr = new PrintStream(bytStdErr, true, CHARACTER_ENCODING);
        stdOut = "";
        stdErr = "";
        ProcessBuilder pb = null;
        if (showCommand) {
            LOGGER.debug(SOSVfs_D_0151.params(commands));
        }
        pb = new ProcessBuilder(commands);
        if (env != null) {
            if (env.getGlobalEnvs() != null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[set global envs]%s", env.getGlobalEnvs()));
                }
                pb.environment().putAll(env.getGlobalEnvs());
            }
            if (env.getLocalEnvs() != null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[set local envs]%s", env.getLocalEnvs()));
                }
                pb.environment().putAll(env.getLocalEnvs());
            }
        }
        final Process p = pb.start();
        createOutputPipe(p.getInputStream(), psStdOut);
        createOutputPipe(p.getErrorStream(), psStdErr);
        pipein(System.in, p.getOutputStream());
        exitValue = p.waitFor();
        stdOut = bytStdOut.toString(CHARACTER_ENCODING);
        stdErr = bytStdErr.toString(CHARACTER_ENCODING);
        String cmd = pb.command().get(pb.command().size() - 1);
        if (!SOSString.isEmpty(stdOut)) {
            LOGGER.info(String.format("[%s][stdout]%s", cmd, stdOut.trim()));
        }
        if (!SOSString.isEmpty(stdErr)) {
            LOGGER.info(String.format("[%s][stderr]%s", cmd, stdErr.trim()));
        }
        return exitValue;
    }

    public void setCommand(final String val) throws Exception {
        command = val;
    }

    public int executeCommand(final String cmd) throws Exception {
        return executeCommand(createCommand(cmd), true);
    }

    public int executeCommand(final String cmd, SOSVfsEnv env) throws Exception {
        return executeCommand(createCommand(cmd), true, env);
    }

    public int executeCommand(final ISOSCmdShellOptions options) throws Exception {
        shellOptions = options;
        return executeCommand(createCommandUsingOptions(), true);
    }

    private String[] createCommands(final String envComSpecName, final String defaultComSpec, final String startParam) {
        final String[] command = { " ", " ", " " };
        String comSpec = "";
        int indx = 0;
        String startShellCommandParam = startParam;
        if (shellOptions.getStartShellCommand().isDirty()) {
            comSpec = shellOptions.getStartShellCommand().getValue();
            if (!"none".equalsIgnoreCase(comSpec)) {
                command[indx++] = comSpec;
                if (shellOptions.getStartShellCommandParameter().isDirty()) {
                    startShellCommandParam = shellOptions.getStartShellCommandParameter().getValue();
                    command[indx++] = startShellCommandParam;
                }
                command[indx++] = shellOptions.getShellCommand().getValue() + " " + shellOptions.getCommandLineOptions().getValue() + " "
                        + shellOptions.getShellCommandParameter().getValue();
            } else {
                command[indx++] = shellOptions.getShellCommand().getValue();
                command[indx++] = shellOptions.getCommandLineOptions().getValue() + " " + shellOptions.getShellCommandParameter().getValue();
            }
        } else {
            comSpec = System.getenv(envComSpecName);
            if (comSpec == null) {
                comSpec = defaultComSpec;
            }
            command[indx++] = comSpec;
            command[indx++] = startShellCommandParam;
            command[indx++] = shellOptions.getShellCommand().getValue() + " " + shellOptions.getCommandLineOptions().getValue() + " " + shellOptions
                    .getShellCommandParameter().getValue();
        }
        LOGGER.debug(SOSVfs_D_230.params(comSpec));
        return command;
    }

    private String[] createCommandUsingOptions() {
        if (isWindows()) {
            return createCommands("comspec", "cmd.exe", "/C");
        } else {
            return createCommands("SHELL", "bin.sh", "-c");
        }
    }

    private String[] createCommand(final String cmd) {
        final String[] command = new String[2 + 1];
        if (isWindows()) {
            String comSpec = System.getenv("comspec");
            LOGGER.debug(SOSVfs_D_230.params(comSpec));
            command[0] = comSpec;
            command[1] = "/C";
            command[2] = cmd;
        } else {
            String comSpec = System.getenv("SHELL");
            if (comSpec == null) {
                comSpec = "/bin/sh";
            }
            LOGGER.debug(SOSVfs_D_230.params(comSpec));
            command[0] = comSpec;
            command[1] = "-c";
            command[2] = cmd;
        }
        return command;
    }

    public int executeCommandWithoutDebugCommand(final String cmd) throws Exception {
        return executeCommand(createCommand(cmd), false);
    }

    private void pipein(final InputStream is, final OutputStream os) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    int ret = -1;
                    while ((ret = is.read()) != -1) {
                        os.write(ret);
                        os.flush();
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
            executeCommand(command);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

}
