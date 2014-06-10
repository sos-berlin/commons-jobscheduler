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
package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.crypto.Cipher;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.crypto.CipherException;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.crypto.CipherFactory;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.exceptions.DecryptionFailedException;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.exceptions.KeePassDataBaseException;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.exceptions.UnsupportedDataBaseException;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.hash.Hash;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.hash.HashFactory;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.KeePassConst;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.kdb.KeePassDataBase;
import com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.util.Utils;

/**
 * KDB file database format reader.
 * Supports password, keyfile and password+keyfile database access.
 * 
 * @author Lukasz Wozniak
 *
 */
public class KeePassDataBaseV1 implements KeePassDataBase {
	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused") private final Logger logger = Logger.getLogger(this.getClass());
	

	// private HeaderV1 header;
	private byte[]				keyFileHash;
	private byte[]				passwordHash;
	private final Hash			hash;
	private final List<Entry>	entries;
	private final List<Group>	groups;
	private final Cipher		cipher;
	private final int			keyEncRounds;
	private final byte[]		masterSeed;
	private final byte[]		masterSeed2;
	private final byte[]		encryptionIv;

	public KeePassDataBaseV1(final byte[] data, final InputStream keyFile, final String password) throws UnsupportedDataBaseException, KeePassDataBaseException {
		super();
		ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		HeaderV1 header = new HeaderV1(bb);
		validateHeader(header);
		hash = HashFactory.getHash(Hash.SHA_256);
		cipher = CipherFactory.getCipher(Cipher.AES);
		keyEncRounds = header.getKeyEncRounds();
		setPassword(password);
		setKeyFile(keyFile);
		if (passwordHash == null && keyFileHash == null) {
			throw new KeePassDataBaseException("Password and key file cannot be both null");
		}
		byte[] content = new byte[data.length - bb.position()];
		bb.get(content, 0, content.length);
		masterSeed = header.getMasterSeed();
		masterSeed2 = header.getMasterSeed2();
		encryptionIv = header.getEncryptionIV();
		byte[] result = null;
		try {
			result = decrypt(content);
		}
		catch (CipherException e) {
			throw new KeePassDataBaseException("Unable to decrypt database.", e);
		}
		if (!Arrays.equals(hash.hash(result), header.getContentsHash())) {
			throw new DecryptionFailedException("Data corrupted or invalid password or key file.");
		}
		entries = new ArrayList<Entry>();
		groups = new ArrayList<Group>();
		try {
			bb = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
			GroupDeserializer groupsDes = new GroupDeserializer();
			for (int i = 0; i < header.getGroups(); i++) {
				short fieldType;
				while ((fieldType = bb.getShort()) != GroupFieldTypes.TERMINATOR) {
					//					System.out.println(String.format("Group fieldType %x", fieldType));
					if (fieldType == 0) {
						continue;
					}
					int fieldSize = bb.getInt();
					groupsDes.readField(fieldType, fieldSize, bb);
				}
				bb.getInt(); // reading FIELDSIZE of group entry terminator
				groups.add(groupsDes.getGroup());
				groupsDes.reset();
			}
			EntryDeserializer entryDes = new EntryDeserializer();
			for (int i = 0; i < header.getEntries(); i++) {
				short fieldType;
				while ((fieldType = bb.getShort()) != GroupFieldTypes.TERMINATOR) {
					if (fieldType == 0) {
						continue;
					}
					int fieldSize = bb.getInt();
					entryDes.readField(fieldType, fieldSize, bb);
				}
				bb.getInt(); // reading FIELDSIZE of entry terminator
				entries.add(entryDes.getEntry());
				entryDes.reset();
			}
			for (Entry objEntry : entries) {
				String strPath = objEntry.Title();
				int intGroupId = objEntry.GroupId();
				while (true) {
					Group objG = getGroup(intGroupId);
					if (objG != null) {
						strPath = objG.Name() + "/" + strPath;
						if (objG.getParent() != null) {
							intGroupId = objG.getParent().Id();
							continue;
						}
						else {
							break;
						}
					}
				}
//				System.out.println(strPath);
				objEntry.setPath(strPath);
			}
		}
		catch (UnsupportedEncodingException e) {
			// weird...
			throw new KeePassDataBaseException("UTF-8 is not supported on this platform");
		}
	}

	private byte[] decrypt(final byte[] content) throws CipherException {
		return cipher.decrypt(prepareKey(), content, encryptionIv);
	}

	private byte[] prepareKey() throws CipherException {
		byte[] passwordKey;
		if (keyFileHash == null) {
			passwordKey = passwordHash;
		}
		else
			if (passwordHash == null) {
				passwordKey = keyFileHash;
			}
			else {
				passwordKey = passwordHash;
				hash.reset();
				hash.update(passwordHash);
				hash.update(keyFileHash);
				passwordKey = hash.digest();
			}
		byte[] masterKey = cipher.encrypt(masterSeed2, passwordKey, null, keyEncRounds, false);
		masterKey = hash.hash(masterKey);
		hash.reset();
		hash.update(masterSeed);
		hash.update(masterKey);
		masterKey = hash.digest();
		return masterKey;
	}

	@Override public void setPassword(final String password) {
		if (password != null) {
			passwordHash = hash.hash(password.getBytes());
		}
		else {
			passwordHash = null;
		}
	}

	@Override public void setKeyFile(final InputStream keyFile) throws KeePassDataBaseException {
		if (keyFile != null) {
			try {
				byte[] buffer = new byte[2048];
				int read = keyFile.read(buffer);
				switch (read) {
					case 32:
						keyFileHash = Arrays.copyOf(buffer, read);
						return;
					case 64:
						keyFileHash = Utils.fromHexString(new String(buffer, 0, 64));
						return;
					default:
						hash.reset();
						hash.update(buffer, 0, read);
						if (read == buffer.length) {
							while (read > 0) {
								read = keyFile.read(buffer);
								hash.update(buffer, 0, read);
							}
						}
						keyFileHash = hash.digest();
				}
			}
			catch (IOException e) {
				throw new KeePassDataBaseException("Unable to read key file.", e);
			}
		}
		else {
			keyFileHash = null;
		}
	}

	/**
	 * Validates version and encryption flags.<br>
	 * 
	 * @throws UnsupportedDataBaseException
	 */
	private void validateHeader(final HeaderV1 header) throws UnsupportedDataBaseException {
		if ((header.getVersion() & KeePassConst.KDB_FILE_VERSION_CRITICAL_MASK) != (KeePassConst.KDB_FILE_VERSION & KeePassConst.KDB_FILE_VERSION_CRITICAL_MASK)) {
			throw new UnsupportedDataBaseException(String.format("Invalid database version %x. Only %x version is supported", header.getVersion(),
					KeePassConst.KDB_FILE_VERSION));
		}
		int flags = header.getFlags();
		if ((flags & KeePassConst.KDB_FLAG_TWOFISH) == KeePassConst.KDB_FLAG_TWOFISH) {
			throw new UnsupportedDataBaseException("Twofish algorithm is not supported");
		}
		if ((flags & KeePassConst.KDB_FLAG_ARC4) == KeePassConst.KDB_FLAG_ARC4) {
			throw new UnsupportedDataBaseException("Twofish algorithm is not supported");
		}
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public Group getGroup(final int plngGroupId) {
		for (Group objGroup : groups) {
			if (objGroup.getGroupId().getId() == plngGroupId) {
				return objGroup;
			}
		}
		return null;
	}

	public Group getGroup(final String pstrGroupName) {
		for (Group objGroup : groups) {
			if (objGroup.Name().equalsIgnoreCase(pstrGroupName.toLowerCase()) == true) {
				return objGroup;
			}
		}
		return null;
	}

	private Group getGroup(final String pstrGroupName, final int plngGroupId) {
		for (Group objGroup : groups) {
			if (objGroup.Name().equalsIgnoreCase(pstrGroupName.toLowerCase()) == true) {
				int intGroupId = objGroup.getParent().getGroupId().getId();
				if (intGroupId == plngGroupId) {
					return objGroup;
				}
			}
		}
		return null;
	}

	public Entry getEntry(final String pstrPath) {
		String strEntryName = pstrPath;
		if (pstrPath.contains("/")) {
			String[] strA = pstrPath.split("/");
			Group objGroup = this.getGroup(strA[0]);
			int i = 0;
			if (objGroup != null) {
				for (i = 1; i < strA.length - 1; i++) {
					if (objGroup != null) {
						String strGroupName = strA[i];
						int intId = objGroup.Id();
						objGroup = this.getGroup(strGroupName, intId);
					}
				}
				strEntryName = strA[i];
			}
			if (objGroup != null) {
				return getSingleEntry(strEntryName, objGroup.Id());
			}
		}
		else {
			return getSingleEntry(strEntryName, 0);
		}
		return null;
	}

	private Entry getSingleEntry(final String pstrName, final int plngGroupId) {
		for (Entry objEntry : entries) {
			String strEntryTitle = objEntry.Title();
			if (pstrName.equalsIgnoreCase(strEntryTitle)) {
				int intGroupId = objEntry.getGroupId().getId();
				if (plngGroupId == 0 || plngGroupId == intGroupId) {
					return objEntry;
				}
			}
		}
		return null;
	}
}
