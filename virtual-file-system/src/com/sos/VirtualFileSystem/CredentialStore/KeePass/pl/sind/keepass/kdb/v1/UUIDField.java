package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v1;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.util.Utils;


public class UUIDField extends Field {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());

	public UUIDField(final short fieldType, final int fieldSize, final ByteBuffer data) {
		super(fieldType, fieldSize, UUID_FIELD_SIZE, data);
	}

	public String getUuid() {
		return Utils.toHexString(getFieldData());
	}

	public void setUuid(final String uuid) {
		if (uuid == null || uuid.length() != 32) {
			throw new IllegalArgumentException("UUID has to be 32 characters long");
		}
		setFieldData(Utils.fromHexString(uuid));
	}
}
