package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v1;
public final class GroupFieldTypes {
	@SuppressWarnings("unused") private final String		conClassName			= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion			= "$Id$";
	public static final short								INVALID_OR_COMMENT		= 0x0000;							// Invalid or comment block, block is ignored
	public static final short								ID						= 0x0001;							// Group ID, FIELDSIZE must be 4 bytes; It can be any 32-bit value except 0 and 0xFFFFFFFF
	public static final short								NAME					= 0x0002;							// Group name, FIELDDATA is an UTF-8 encoded string
	public static final short								CREATION_TIME			= 0x0003;							// Creation time, FIELDSIZE = 5, FIELDDATA = packed date/time
	public static final short								LAST_MODIFICATION_TIME	= 0x0004;							// Last modification time, FIELDSIZE = 5, FIELDDATA = packed date/time
	public static final short								LAST_ACCESS_TIME		= 0x0005;							// Last access time, FIELDSIZE = 5, FIELDDATA = packed date/time
	public static final short								EXPIRATION_TIME			= 0x0006;							// Expiration time, FIELDSIZE = 5, FIELDDATA = packed date/time
	public static final short								IMAGE_ID				= 0x0007;							// Image ID, FIELDSIZE must be 4 bytes
	public static final short								LEVEL					= 0x0008;							// Level, FIELDSIZE = 2
	public static final short								FLAGS					= 0x0009;							// Flags, 32-bit value, FIELDSIZE = 4
	public static final short								TERMINATOR				= (short) 0xFFFF;					// Flags, 32-bit value, FIELDSIZE = 4
}
