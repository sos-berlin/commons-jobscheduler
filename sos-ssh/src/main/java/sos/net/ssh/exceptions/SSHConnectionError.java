package sos.net.ssh.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SSHConnectionError extends JobSchedulerException {

    private static final long serialVersionUID = 6955572176597933634L;

    public SSHConnectionError(final String msg, final Exception e) {
        super(msg, e);
    }

}
