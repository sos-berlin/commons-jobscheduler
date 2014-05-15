/*
 * Copyright 2009 Lukasz Wozniak
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb;
public final class KeePassConst {
	@SuppressWarnings("unused") private final String		conClassName					= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion					= "$Id$";
	//KDB KeePass 1.x
	public static final int									KDB_FILE_VERSION				= 0x00030002;
	//	public static final int KDB_FILE_VERSION = 0x00030004;
	public static final int									KDB_FILE_VERSION_CRITICAL_MASK	= 0xFFFFFFFF;
	public static final int									KDB_SIG_1						= 0x9AA2D903;
	public static final int									KDB_SIG_2						= 0xB54BFB65;
	//KDBX KeePass 2.x
	public static final int									KDBX_FILE_VERSION				= 0x00020000;
	public static final int									KDBX_FILE_VERSION_CRITICAL_MASK	= 0xFFFF0000;
	public static final int									KDBX_SIG_1						= 0x9AA2D903;
	public static final int									KDBX_SIG_2						= 0xB54BFB67;
	public static final byte								KDB_FLAG_SHA_2					= 0x1;
	public static final byte								KDB_FLAG_AES					= 0x2;
	public static final byte								KDB_FLAG_ARC4					= 0x4;
	public static final byte								KDB_FLAG_TWOFISH				= 0x8;
}
