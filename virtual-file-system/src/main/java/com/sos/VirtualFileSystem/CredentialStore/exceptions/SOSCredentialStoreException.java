/**
 * 
 */
package com.sos.VirtualFileSystem.CredentialStore.exceptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Options.keepass4j.SOSCredentialStoreOptions;
import com.sos.localization.SOSMsg;

/**
 * @author KB
 *
 */
public class SOSCredentialStoreException extends JobSchedulerException {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -6421417813962650491L;

	protected SOSCredentialStoreOptions		objCredentialStoreOptions	= null;

	/**
	 * 
	 */
	public SOSCredentialStoreException() {
		
	}

	/**
	 * @param pstrMessage
	 */
	public SOSCredentialStoreException(final String pstrMessage) {
		super(pstrMessage);
		
	}

	/**
	 * @param pobjMsg
	 */
	public SOSCredentialStoreException(final SOSMsg pobjMsg) {
		super(pobjMsg);
		
	}

	/**
	 * @param pstrMessage
	 * @param e
	 */
	public SOSCredentialStoreException(final String pstrMessage, final Exception e) {
		super(pstrMessage, e);
		
	}

	/**
	 * @param pobjMsg
	 * @param e
	 */
	public SOSCredentialStoreException(final SOSMsg pobjMsg, final Exception e) {
		super(pobjMsg, e);
		
	}

	/**
	 * @param e
	 */
	public SOSCredentialStoreException(final Exception e) {
		super(e);
		
	}
}
