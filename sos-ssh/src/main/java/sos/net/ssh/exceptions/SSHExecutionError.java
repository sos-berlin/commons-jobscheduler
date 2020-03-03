package sos.net.ssh.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SSHExecutionError extends JobSchedulerException {

    private static final long serialVersionUID = 5148373372299244495L;

    public SSHExecutionError(final String msg) {
        super(msg);
    }

    public SSHExecutionError(final String msg, final Exception e) {
        super(msg, e);
    }

}
