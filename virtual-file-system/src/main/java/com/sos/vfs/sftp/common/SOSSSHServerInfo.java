package com.sos.vfs.sftp.common;

import java.util.ArrayList;
import java.util.List;

import com.sos.vfs.common.SOSCommandResult;

import sos.util.SOSString;

public class SOSSSHServerInfo {

    public enum OS {
        UNKNOWN, UNIX, WINDOWS
    };

    public enum Shell {
        UNKNOWN, UNIX, WINDOWS, CYGWIN
    };

    /** e.g. "OpenSSH_$version" -> OpenSSH_for_Windows_8.1. Can be null. */
    private final String serverVersion;
    private final SOSCommandResult commandResult;
    private String os = OS.UNKNOWN.name();
    private Shell shell = Shell.UNKNOWN;

    public SOSSSHServerInfo(String serverVersion, SOSCommandResult commandResult) {
        this.serverVersion = serverVersion;
        this.commandResult = commandResult;
        analyze();
    }

    private void analyze() {
        if (commandResult == null) {
            return;
        }

        if (commandResult.getExitCode() == null) {
            if (commandResult.getStdErr().length() > 0) {
                os = OS.WINDOWS.name();
            } else {
                analyzeServerVersion();
            }
            return;
        }

        switch (commandResult.getExitCode()) {
        case 0:
            String stdOut = commandResult.getStdOut().trim();
            if (stdOut.matches("(?i).*(linux|darwin|aix|hp-ux|solaris|sunos|freebsd).*")) {
                os = stdOut;
                shell = Shell.UNIX;
            } else if (stdOut.matches("(?i).*cygwin.*")) {
                // OS is Windows but shell is Unix like
                // unix commands have to be used
                os = OS.WINDOWS.name();
                shell = Shell.CYGWIN;
            } else {
                analyzeServerVersion();
                shell = Shell.UNIX;
            }
            break;
        case 9009:
        case 1:
            // call of uname under Windows OS delivers exit code 9009 or exit code 1 and target shell cmd.exe
            // the exit code depends on the remote SSH implementation
            os = OS.WINDOWS.name();
            shell = Shell.WINDOWS;
            break;
        case 127:
            // call of uname under Windows OS with CopSSH (cygwin) and target shell /bin/bash delivers exit code 127
            // command uname is not installed by default through CopSSH installation
            os = OS.WINDOWS.name();
            shell = Shell.CYGWIN;
            break;
        default:
            analyzeServerVersion();
            shell = os.equals(OS.WINDOWS.name()) ? Shell.WINDOWS : Shell.UNKNOWN;
            break;
        }
    }

    private void analyzeServerVersion() {
        if (!SOSString.isEmpty(serverVersion)) {
            if (serverVersion.toUpperCase().contains(OS.WINDOWS.name())) {
                os = OS.WINDOWS.name();
            }
        }
    }

    public String getOS() {
        return os;
    }

    public Shell getShell() {
        return shell;
    }

    public SOSCommandResult getCommandResult() {
        return commandResult;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public boolean hasWindowsShell() {
        return shell.equals(Shell.WINDOWS);
    }

    @Override
    public String toString() {
        List<String> l = new ArrayList<>();
        if (serverVersion != null) {
            l.add("Identity=" + serverVersion);
        }
        l.add("OS=" + os);
        l.add("Shell=" + shell.name());

        StringBuilder result = new StringBuilder("Server ");
        result.append(String.join(", ", l));

        if (commandResult.hasError(false)) {
            result.append(" (");
            result.append(commandResult.getCommand());
            if (commandResult.getExitCode() != null && commandResult.getExitCode() > 0) {
                result.append(" exitCode=").append(commandResult.getExitCode());
            }
            if (commandResult.getException() != null) {
                result.append(" ").append(commandResult.getException().toString());
            }
            result.append(")");
        }
        return result.toString();
    }

}
