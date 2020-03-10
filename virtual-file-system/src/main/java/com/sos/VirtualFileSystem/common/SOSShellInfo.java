package com.sos.VirtualFileSystem.common;

public class SOSShellInfo {

    public enum OS {
        UNKNOWN, UNIX, WINDOWS
    };

    public enum Shell {
        UNKNOWN, UNIX, WINDOWS, CYGWIN
    };

    private String os = OS.UNIX.name();
    private Shell shell = Shell.UNIX;
    private SOSCommandResult commandResult;
    private Throwable commandError;
    private String command;

    public SOSShellInfo(String cmd) {
        command = cmd;
    }

    public String getOS() {
        return os;
    }

    public void setOS(String val) {
        os = val;
    }

    public Shell getShell() {
        return shell;
    }

    public void setShell(Shell val) {
        shell = val;
    }

    public String getCommand() {
        return command;
    }

    public SOSCommandResult getCommandResult() {
        return commandResult;
    }

    public void setCommandResult(SOSCommandResult val) {
        commandResult = val;
    }

    public Throwable getCommandError() {
        return commandError;
    }

    public void setCommandError(Throwable val) {
        commandError = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OS=").append(os);
        sb.append(", Shell=").append(shell.name());
        if (commandResult != null && commandResult.getExitCode() > 0) {
            sb.append(", ").append(command).append(" exit code=").append(commandResult.getExitCode());
        }
        return sb.toString();
    }
}
