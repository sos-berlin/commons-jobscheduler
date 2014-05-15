package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v1;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

public class BinaryField extends Field {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());

	public BinaryField(final short fieldType, final int fieldSize, final ByteBuffer data) {
		super(fieldType, fieldSize, fieldSize, data);
	}
}
