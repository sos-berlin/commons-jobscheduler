package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v1;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.util.Utils;


public class LevelField extends Field {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());

	public LevelField(final short fieldType, final int fieldSize, final ByteBuffer data) {
		super(fieldType, fieldSize, LEVEL_FIELD_SIZE, data);
	}

	public void setLevel(final short value) {
		Utils.shortTobytes(value, getFieldData());
	}

	public short getLevel() {
		return Utils.bytesToShort(getFieldData());
	}
}
