package com.sos.vfs.common;

public class SOSCommandResult {

    private final StringBuilder stdOut;
    private final StringBuilder stdErr;

    private String command;
    private Integer exitCode;
    private Throwable exception;

    public SOSCommandResult(String cmd) {
        command = cmd;
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
    }

    public void setCommand(String val) {
        command = val;
    }

    public String getCommand() {
        return command;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer val) {
        exitCode = val;
    }

    public String getStdOut() {
        return stdOut.toString();
    }

    public void addStdOut(String val) {
        stdOut.append(val);
    }

    public boolean hasStdOut() {
        return stdOut.length() > 0;
    }

    public String getStdErr() {
        return stdErr.toString();
    }

    public void addStdErr(String val) {
        stdErr.append(val);
    }

    public boolean hasStdErr() {
        return stdErr.length() > 0;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable val) {
        exception = val;
    }

    public boolean hasError() {
        return hasError(false);
    }

    public boolean hasError(boolean checkStdError) {
        if (exception != null) {
            return true;
        }
        if (exitCode != null && exitCode > 0) {
            return true;
        }
        if (checkStdError && stdErr.length() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[").append(command).append("]");
        sb.append("[exitCode=").append(exitCode).append("]");
        sb.append("[std:out=").append(stdOut.toString().trim()).append("]");
        sb.append("[std:err=").append(stdErr.toString().trim()).append("]");
        if (exception != null) {
            sb.append("[exception=").append(exception.toString()).append("]");
        }
        return sb.toString();
    }
}
