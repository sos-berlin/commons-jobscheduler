/**
 * 
 */
package com.sos.VirtualFileSystem.Interfaces;

import java.util.Vector;

import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;

/**
 * @author KB
 *
 */
public interface ISOSVfsFileTransfer2 extends ISOSVfsFileTransfer {

	public SOSFileList getFileListEntries(final SOSFileList pobjSOSFileList, final String folder, final String regexp, final boolean flgRecurseSubFolder);
	public Vector<SOSFileListEntry> getFileListEntries();
	public void clearFileListEntries();


}
