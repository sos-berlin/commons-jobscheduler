package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v1;
import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.util.Utils;

public class DateField extends Field {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());

	public DateField(final short fieldType, final int fieldSize, final ByteBuffer data) {
		super(fieldType, fieldSize, DATE_FIELD_SIZE, data);
	}

	public Date getDate() {
		return Utils.unpackDate(getFieldData());
	}

	public void setDate(final Date date) {
		setFieldData(Utils.packDate(date));
	}
}
