/**
 * 
 */
package sos.net.ssh.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.SOSMsg;

/**
 * @author KB
 *
 */
public class SSHExecutionError extends JobSchedulerException {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5148373372299244495L;

	/**
	 * 
	 */
	public SSHExecutionError() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param pstrMessage
	 */
	public SSHExecutionError(final String pstrMessage) {
		super(pstrMessage);
	}

	/**
	 * @param pobjMsg
	 */
	public SSHExecutionError(final SOSMsg pobjMsg) {
		super(pobjMsg);
	}

	/**
	 * @param pstrMessage
	 * @param e
	 */
	public SSHExecutionError(final String pstrMessage, final Exception e) {
		super(pstrMessage, e);
	}

	/**
	 * @param pobjMsg
	 * @param e
	 */
	public SSHExecutionError(final SOSMsg pobjMsg, final Exception e) {
		super(pobjMsg, e);
	}

	/**
	 * @param e
	 */
	public SSHExecutionError(final Exception e) {
		super(e);
	}

}
