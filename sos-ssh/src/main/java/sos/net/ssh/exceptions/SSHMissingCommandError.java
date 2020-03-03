package sos.net.ssh.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SSHMissingCommandError extends JobSchedulerException {

    private static final long serialVersionUID = -6271837981473074910L;

    public SSHMissingCommandError(final String msg) {
        super(msg);
    }

}
