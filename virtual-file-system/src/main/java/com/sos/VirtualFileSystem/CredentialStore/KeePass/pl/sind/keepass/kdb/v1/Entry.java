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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.log4j.Logger;

public class Entry {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());
	private UUIDField										uuid;
	private IdField											groupId;
	private IdField											imageId;
	private TextField										title;
	private TextField										url;
	private TextField										username;
	private TextField										password;
	private TextField										notes;
	private DateField										creationTime;
	private DateField										lastModificationTime;
	private DateField										lastAccessTime;
	private DateField										expirationTime;
	private TextField										binaryDescription;
	private BinaryField										binaryData;
	private List<Field>										comments;
	private List<Field>										unknowns;
	private String											strPath			= "";

	public Entry() {
		super();
	}

	public Entry(final UUIDField uuid, final IdField groupId, final IdField imageId, final TextField title, final TextField url, final TextField username,
			final TextField password, final TextField notes, final DateField creationTime, final DateField lastModificationTime,
			final DateField lastAccessTime, final DateField expirationTime, final TextField binaryDescription, final BinaryField binaryData,
			final List<Field> comments, final List<Field> unknowns) {
		super();
		this.uuid = uuid;
		this.groupId = groupId;
		this.imageId = imageId;
		this.title = title;
		this.url = url;
		this.username = username;
		this.password = password;
		this.notes = notes;
		this.creationTime = creationTime;
		this.lastModificationTime = lastModificationTime;
		this.lastAccessTime = lastAccessTime;
		this.expirationTime = expirationTime;
		this.binaryDescription = binaryDescription;
		this.binaryData = binaryData;
		this.comments = comments;
		this.unknowns = unknowns;
	}

	public String getPath() {
		return strPath;
	}

	public void setPath(final String pstrPath) {
		strPath = pstrPath;
	}

	public UUIDField getUuid() {
		return uuid;
	}

	public String Uuid() {
		return uuid.getUuid();
	}

	public void setUuid(final UUIDField uuid) {
		this.uuid = uuid;
	}

	public IdField getGroupId() {
		return groupId;
	}

	public int GroupId() {
		return groupId.getId();
	}

	public void setGroupId(final IdField groupId) {
		this.groupId = groupId;
	}

	public IdField getImageId() {
		return imageId;
	}

	public void setImageId(final IdField imageId) {
		this.imageId = imageId;
	}

	public TextField getTitle() {
		return title;
	}

	public String Title() {
		return title.getText();
	}

	public void setTitle(final TextField title) {
		this.title = title;
	}

	public TextField getUrl() {
		return url;
	}

	public void setUrl(final TextField url) {
		this.url = url;
	}

	public TextField getUsername() {
		return username;
	}

	public String UserName() {
		return username.getText();
	}

	public void setUsername(final TextField username) {
		this.username = username;
	}

	public TextField getPassword() {
		return password;
	}

	public String Password() {
		return password.getText();
	}

	public String Url() {
		return url.getText();
	}
	
	public void setPassword(final TextField password) {
		this.password = password;
	}

	public String[] getNotesAsArray () {
		StrTokenizer objT = new StrTokenizer (notes.getText());
		String[] strA = objT.getTokenArray();
		return strA;
	}

	public String getNotesText() {
		return notes.getText();
	}


	public TextField getNotes() {
		return notes;
	}

	public String Notes() {
		return notes.getText();
	}

	public void setNotes(final TextField notes) {
		this.notes = notes;
	}

	public DateField getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(final DateField creationTime) {
		this.creationTime = creationTime;
	}

	public DateField getLastModificationTime() {
		return lastModificationTime;
	}

	public void setLastModificationTime(final DateField lastModificationTime) {
		this.lastModificationTime = lastModificationTime;
	}

	public DateField getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(final DateField lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public DateField getExpirationTime() {
		return expirationTime;
	}

	public Date ExpirationDate() {
		return expirationTime.getDate();
	}

	public boolean isExpired() {
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		Calendar dteExpiryDate = Calendar.getInstance();
		dteExpiryDate.setTime(this.ExpirationDate());
		boolean flgIsExpired = today.after(dteExpiryDate);
		return flgIsExpired;
	}

	public void setExpirationTime(final DateField expirationTime) {
		this.expirationTime = expirationTime;
	}

	public TextField getBinaryDescription() {
		return binaryDescription;
	}

	public void setBinaryDescription(final TextField binaryDescription) {
		this.binaryDescription = binaryDescription;
	}

	public BinaryField getBinaryData() {
		return binaryData;
	}

	public File saveAttachmentAsFile(final String pstrFileName) {
		File fleO = null;
		try {
			fleO = new File(pstrFileName);
//			fleO.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(fleO);
			fos.write(getBinaryData().getFieldData());
			fos.close();
		}
		catch (FileNotFoundException ex) {
			System.out.println("FileNotFoundException : " + ex);
		}
		catch (IOException ioe) {
			System.out.println("IOException : " + ioe);
		}
		return fleO;
	}

	public void setBinaryData(final BinaryField binaryData) {
		this.binaryData = binaryData;
	}

	public List<Field> getComments() {
		return comments;
	}

	public void setComments(final List<Field> comments) {
		this.comments = comments;
	}

	public List<Field> getUnknowns() {
		return unknowns;
	}

	public void setUnknowns(final List<Field> unknowns) {
		this.unknowns = unknowns;
	}

	@Override public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (binaryData == null ? 0 : binaryData.hashCode());
		result = prime * result + (binaryDescription == null ? 0 : binaryDescription.hashCode());
		result = prime * result + (comments == null ? 0 : comments.hashCode());
		result = prime * result + (creationTime == null ? 0 : creationTime.hashCode());
		result = prime * result + (expirationTime == null ? 0 : expirationTime.hashCode());
		result = prime * result + (groupId == null ? 0 : groupId.hashCode());
		result = prime * result + (imageId == null ? 0 : imageId.hashCode());
		result = prime * result + (lastAccessTime == null ? 0 : lastAccessTime.hashCode());
		result = prime * result + (lastModificationTime == null ? 0 : lastModificationTime.hashCode());
		result = prime * result + (notes == null ? 0 : notes.hashCode());
		result = prime * result + (password == null ? 0 : password.hashCode());
		result = prime * result + (title == null ? 0 : title.hashCode());
		result = prime * result + (unknowns == null ? 0 : unknowns.hashCode());
		result = prime * result + (url == null ? 0 : url.hashCode());
		result = prime * result + (username == null ? 0 : username.hashCode());
		result = prime * result + (uuid == null ? 0 : uuid.hashCode());
		return result;
	}

	@Override public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entry other = (Entry) obj;
		if (binaryData == null) {
			if (other.binaryData != null)
				return false;
		}
		else
			if (!binaryData.equals(other.binaryData))
				return false;
		if (binaryDescription == null) {
			if (other.binaryDescription != null)
				return false;
		}
		else
			if (!binaryDescription.equals(other.binaryDescription))
				return false;
		if (comments == null) {
			if (other.comments != null)
				return false;
		}
		else
			if (!comments.equals(other.comments))
				return false;
		if (creationTime == null) {
			if (other.creationTime != null)
				return false;
		}
		else
			if (!creationTime.equals(other.creationTime))
				return false;
		if (expirationTime == null) {
			if (other.expirationTime != null)
				return false;
		}
		else
			if (!expirationTime.equals(other.expirationTime))
				return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		}
		else
			if (!groupId.equals(other.groupId))
				return false;
		if (imageId == null) {
			if (other.imageId != null)
				return false;
		}
		else
			if (!imageId.equals(other.imageId))
				return false;
		if (lastAccessTime == null) {
			if (other.lastAccessTime != null)
				return false;
		}
		else
			if (!lastAccessTime.equals(other.lastAccessTime))
				return false;
		if (lastModificationTime == null) {
			if (other.lastModificationTime != null)
				return false;
		}
		else
			if (!lastModificationTime.equals(other.lastModificationTime))
				return false;
		if (notes == null) {
			if (other.notes != null)
				return false;
		}
		else
			if (!notes.equals(other.notes))
				return false;
		if (password == null) {
			if (other.password != null)
				return false;
		}
		else
			if (!password.equals(other.password))
				return false;
		if (title == null) {
			if (other.title != null)
				return false;
		}
		else
			if (!title.equals(other.title))
				return false;
		if (unknowns == null) {
			if (other.unknowns != null)
				return false;
		}
		else
			if (!unknowns.equals(other.unknowns))
				return false;
		if (url == null) {
			if (other.url != null)
				return false;
		}
		else
			if (!url.equals(other.url))
				return false;
		if (username == null) {
			if (other.username != null)
				return false;
		}
		else
			if (!username.equals(other.username))
				return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		}
		else
			if (!uuid.equals(other.uuid))
				return false;
		return true;
	}

	@Override public String toString() {
		return this.Title() + ", " + this.getGroupId().getId();
		//		return "Entry [binaryData=" + binaryData
		//				+ ", binaryDescription=" + binaryDescription 
		//				+ ", comments=" + comments
		//				+ ", creationTime=" + creationTime + ", expirationTime="
		//				+ expirationTime + ", groupId=" + groupId + ", imageId="
		//				+ imageId + ", lastAccessTime=" + lastAccessTime
		//				+ ", lastModificationTime=" + lastModificationTime + ", notes="
		//				+ notes + ", password=" + password + ", title=" + title
		//				+ ", unknowns=" + unknowns + ", url=" + url + ", username="
		//				+ username + ", uuid=" + uuid + "]";
	}
}
