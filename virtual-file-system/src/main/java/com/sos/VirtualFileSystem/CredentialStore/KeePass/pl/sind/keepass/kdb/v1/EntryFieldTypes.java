package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v1;
public final class EntryFieldTypes {
	@SuppressWarnings("unused") private final String		conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion		= "$Id$";
	public static final int									BINARY_DATA			= 0x000E;
	public static final int									BINARY_DESCRIPTION	= 0x000D;
	public static final int									EXPIRATION_TIME		= 0x000C;
	public static final int									LAST_ACCESS_TIME	= 0x000B;
	public static final int									LAST_MODIFICATION	= 0x000A;
	public static final int									CREATION_TIME		= 0x0009;
	public static final int									NOTES				= 0x0008;
	public static final int									PASSWORD			= 0x0007;
	public static final int									USERNAME			= 0x0006;
	public static final int									URL					= 0x0005;
	public static final int									TITLE				= 0x0004;
	public static final int									IMAGE_ID			= 0x0003;
	public static final int									GROUP_ID			= 0x0002;
	public static final int									UUID				= 0x0001;
	public static final int									INVALID_OR_COMMENT	= 0x0000;
	public static final short								TERMINATOR			= (short) 0xFFFF;					// Flags, 32-bit value, FIELDSIZE = 4
}
