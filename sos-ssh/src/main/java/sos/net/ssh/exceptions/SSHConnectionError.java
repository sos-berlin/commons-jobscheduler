/**
 * 
 */
package sos.net.ssh.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.SOSMsg;

/** @author KB */
public class SSHConnectionError extends JobSchedulerException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 6955572176597933634L;

    /**
	 * 
	 */
    public SSHConnectionError() {
    }

    /** @param pstrMessage */
    public SSHConnectionError(final String pstrMessage) {
        super(pstrMessage);
    }

    /** @param pobjMsg */
    public SSHConnectionError(final SOSMsg pobjMsg) {
        super(pobjMsg);
    }

    /** @param pstrMessage
     * @param e */
    public SSHConnectionError(final String pstrMessage, final Exception e) {
        super(pstrMessage, e);
    }

    /** @param pobjMsg
     * @param e */
    public SSHConnectionError(final SOSMsg pobjMsg, final Exception e) {
        super(pobjMsg, e);
    }

    /** @param e */
    public SSHConnectionError(final Exception e) {
        super(e);
    }

}
