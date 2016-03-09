/**
 * 
 */
package sos.net.ssh.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.SOSMsg;

/** @author KB */
public class SSHMissingCommandError extends JobSchedulerException {

    /**
	 * 
	 */
    private static final long serialVersionUID = -6271837981473074910L;

    /**
	 * 
	 */
    public SSHMissingCommandError() {
        // TODO Auto-generated constructor stub
    }

    /** @param pstrMessage */
    public SSHMissingCommandError(final String pstrMessage) {
        super(pstrMessage);
    }

    /** @param pobjMsg */
    public SSHMissingCommandError(final SOSMsg pobjMsg) {
        super(pobjMsg);
    }

    /** @param pstrMessage
     * @param e */
    public SSHMissingCommandError(final String pstrMessage, final Exception e) {
        super(pstrMessage, e);
    }

    /** @param pobjMsg
     * @param e */
    public SSHMissingCommandError(final SOSMsg pobjMsg, final Exception e) {
        super(pobjMsg, e);
    }

    /** @param e */
    public SSHMissingCommandError(final Exception e) {
        super(e);
    }

}
