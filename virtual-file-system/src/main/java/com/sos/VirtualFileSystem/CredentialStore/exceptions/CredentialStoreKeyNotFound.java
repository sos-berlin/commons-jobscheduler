/**
 * 
 */
package com.sos.VirtualFileSystem.CredentialStore.exceptions;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Options.keepass4j.SOSCredentialStoreOptions;

/**
 * @author KB
 *
 */
public class CredentialStoreKeyNotFound extends SOSCredentialStoreException {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -895707771551951127L;

	/**
	 * 
	 */
	public CredentialStoreKeyNotFound(final SOSCredentialStoreOptions pobjOptions) {
		super(String.format("SOSVfsCS_E_001: Entry '%1$s' in database '%2$s' not found", pobjOptions.CredentialStore_KeyPath.Value(),
				pobjOptions.CredentialStore_FileName.Value()));
		objCredentialStoreOptions = pobjOptions;
		gflgStackTracePrinted = true;
		this.Status(JobSchedulerException.FATAL);
	}

}
