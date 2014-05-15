package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v1;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

public class TextField extends Field {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());
	private static final Charset							CHARSET			= Charset.forName("UTF-8");

	public TextField(final short fieldType, final int fieldSize, final ByteBuffer data) {
		// Strings are null terminated, remove 1 from size and remove from buffer
		super(fieldType, fieldSize - 1, fieldSize - 1, data);
		data.get();
	}

	public String getText() {
		return new String(getFieldData(), CHARSET);
	}

	public void setText(final String text) {
		setFieldData(text.getBytes(CHARSET));
	}
}
