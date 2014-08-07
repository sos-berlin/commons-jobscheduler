/**
 * 
 */
package com.sos.VirtualFileSystem.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.enums.JADEExitCodes;
import com.sos.localization.SOSMsg;

/**
 * @author KB
 *
 */
public class JADEException extends JobSchedulerException {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 8221368427102971162L;
	private JADEExitCodes ExitCode = JADEExitCodes.someUnspecificError;  // default exitcode, if an exception occurs
	
	/**
	 * 
	 */
	public JADEException() {
	}

	/**
	 * @param pstrMessage
	 */
	public JADEException(final String pstrMessage) {
		super(pstrMessage);
	}

	/**
	 * @param pobjMsg
	 */
	public JADEException(final SOSMsg pobjMsg) {
		super(pobjMsg);
	}

	/**
	 * @param pstrMessage
	 * @param e
	 */
	public JADEException(final String pstrMessage, final Exception e) {
		super(pstrMessage, e);
	}

	public JADEException(final JADEExitCodes pintExitCode, final Exception e) {
		super( e);
		ExitCode = pintExitCode;
	}

	/**
	 * @param pobjMsg
	 * @param e
	 */
	public JADEException(final SOSMsg pobjMsg, final Exception e) {
		super(pobjMsg, e);
	}

	/**
	 * @param e
	 */
	public JADEException(final Exception e) {
		super(e);
	}

	public JADEExitCodes getExitCode() {
		return ExitCode;
	}

	public void setExitCode(final JADEExitCodes exitCode) {
		ExitCode = exitCode;
	}
}
