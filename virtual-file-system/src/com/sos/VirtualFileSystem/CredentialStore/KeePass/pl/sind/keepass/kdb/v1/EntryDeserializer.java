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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class EntryDeserializer {
	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused") private final Logger logger = Logger.getLogger(this.getClass());
	

	private UUIDField			uuid;
	private IdField				groupId;
	private IdField				imageId;
	private TextField			title;
	private TextField			url;
	private TextField			username;
	private TextField			password;
	private TextField			notes;
	private DateField			creationTime;
	private DateField			lastModificationTime;
	private DateField			lastAccessTime;
	private DateField			expirationTime;
	private TextField			binaryDescription;
	private BinaryField		binaryData;
	private final ArrayList<Field>	comments	= new ArrayList<Field>();
	private final ArrayList<Field>	unknowns	= new ArrayList<Field>();

	public void readField(final short fieldType, final int fieldSize, final ByteBuffer data) throws UnsupportedEncodingException {
		switch (fieldType) {
			case 0x0000: // Invalid or comment block
				comments.add(new Field(fieldType, fieldSize, fieldSize, data));
				break;
			case EntryFieldTypes.UUID:  // 0x0001: // UUID, uniquely identifying an entry, FIELDSIZE must be 16
				uuid = new UUIDField(fieldType, fieldSize, data);
				break;
			case EntryFieldTypes.GROUP_ID:  // 0x0002: // Group ID, identifying the group of the entry, FIELDSIZE = 4; It can be any 32-bit value except 0 and 0xFFFFFFFF
				groupId = new IdField(fieldType, fieldSize, data);
				break;
			case 0x0003: // Image ID, identifying the image/icon of the entry, FIELDSIZE = 4
				imageId = new IdField(fieldType, fieldSize, data);
				break;
			case 0x0004: // Title of the entry, FIELDDATA is an UTF-8 encoded string
				title = new TextField(fieldType, fieldSize, data);
				break;
			case 0x0005: // URL string, FIELDDATA is an UTF-8 encoded string
				url = new TextField(fieldType, fieldSize, data);
				break;
			case 0x0006: // UserName string, FIELDDATA is an UTF-8 encoded string
				username = new TextField(fieldType, fieldSize, data);
				break;
			case 0x0007: // Password string, FIELDDATA is an UTF-8 encoded string
				password = new TextField(fieldType, fieldSize, data);
				break;
			case 0x0008: // Notes string, FIELDDATA is an UTF-8 encoded string
				notes = new TextField(fieldType, fieldSize, data);
				break;
			case 0x0009: // Creation time, FIELDSIZE = 5, FIELDDATA = packed date/time
				creationTime = new DateField(fieldType, fieldSize, data);
				break;
			case 0x000A: // Last modification time, FIELDSIZE = 5, FIELDDATA = packed date/time
				lastModificationTime = new DateField(fieldType, fieldSize, data);
				break;
			case 0x000B: // Last access time, FIELDSIZE = 5, FIELDDATA = packed date/time
				lastAccessTime = new DateField(fieldType, fieldSize, data);
				break;
			case 0x000C: // Expiration time, FIELDSIZE = 5, FIELDDATA = packed date/time
				expirationTime = new DateField(fieldType, fieldSize, data);
				break;
			case 0x000D: // Binary description UTF-8 encoded string
				binaryDescription = new TextField(fieldType, fieldSize, data);
				break;
			case 0x000E: // Binary data
				binaryData = new BinaryField(fieldType, fieldSize, data);
				break;
			default:
				unknowns.add(new Field(fieldType, fieldSize, fieldSize, data));
		}
	}

	public Entry getEntry() {
		Entry toReturn = new Entry(uuid, groupId, imageId, title, url, username, password, notes, creationTime, lastModificationTime, lastAccessTime,
				expirationTime, binaryDescription, binaryData, unknowns, comments);
		reset();
		return toReturn;
	}

	public void reset() {
		uuid = null;
		groupId = null;
		imageId = null;
		title = null;
		url = null;
		username = null;
		password = null;
		notes = null;
		creationTime = null;
		lastModificationTime = null;
		lastAccessTime = null;
		expirationTime = null;
		binaryDescription = null;
		binaryData = null;
	}
}
