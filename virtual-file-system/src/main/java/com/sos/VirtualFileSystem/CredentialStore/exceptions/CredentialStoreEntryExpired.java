package com.sos.VirtualFileSystem.CredentialStore.exceptions;

import java.util.Date;

public class CredentialStoreEntryExpired extends SOSCredentialStoreException {
	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 8190764135924155901L;

	public CredentialStoreEntryExpired(final Date objExpDate) {
		super(String.format(String.format("SOSVfsCS_E_001: Entry is expired, valid until %1$s. Processing aborted", objExpDate)));
		gflgStackTracePrinted = true;
		this.Status(FATAL);
	}
}
