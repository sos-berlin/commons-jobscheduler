package com.sos.VirtualFileSystem.FTP;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSSession;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFileSystem;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtp2 extends SOSVfsFtpBaseClass2 implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {
	private final String	conClassName	= "SOSVfsFtp";
	private final Logger	logger			= Logger.getLogger(SOSVfsFtp2.class);

	private FTPClient		objFTPClient	= null;

	//	private FTPFile[]		objFTPFileList	= null;

	@SuppressWarnings("unused")
	private final ISOSAuthenticationOptions	objAO	= null;

	@Deprecated
	/**
	 *
	 * \brief SOSVfsFtp
	 *
	 * \details
	 *
	 */
	public SOSVfsFtp2() {
		super();
	}

	/**
	 * append a local file to the remote one on the server
	 *
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 *
	 * @return The total number of bytes appended.
	 *
	 * @exception Exception
	 * @see #put( String, String )
	 * @see #putFile( String, String )
	 */
//	@Override
//	public long appendFile(final String localFile, final String remoteFile) {
//		final String conMethodName = conClassName + "::appendFile";
//
//		long i = 0;
//		try {
//			i = putFile(localFile, Client().appendFileStream(remoteFile));
//			logger.info(SOSVfs_I_155.params(i));
//		}
//		catch (IOException e) {
//			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
//		}
//		return i;
//	} // appendFile

	@Override
	public boolean changeWorkingDirectory(final String pathname) {
		final String conMethodName = conClassName + "::changeWorkingDirectory";
		boolean flgR = true;
		try {
			String strT = pathname.replaceAll("\\\\", "/");
			Client().cwd(strT);
			logger.debug(SOSVfs_D_135.params(strT, getReplyString(), "[directory exists]"));
			flgR = objFTPReply.isSuccessCode();
		}
		catch (IOException e) {
			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		}
		return flgR;
	}

	//	@Override
//	public void CloseConnection() throws Exception {
//		if (Client().isConnected()) {
//			Client().disconnect();
//			LogReply();
//			logger.debug(SOSVfs_I_0109.params(host, port));
//		}
//	}
//
	@Override
	protected final FTPClient Client() {
		if (objFTPClient == null) {
			objFTPClient = new FTPClient();
			@SuppressWarnings("unused")
			FTPClientConfig conf = new FTPClientConfig();
			// TODO create additional Options for ClientConfig
			// conf.setServerLanguageCode("fr");
			 objFTPClient.configure(conf);
			/**
			 * This listener is to write all commands and response from commands to system.out
			 *
			 */
			objProtocolCommandListener = new SOSFtpClientLogger(HostID(""));
			if (objConnection2Options != null) {
				if (objConnection2Options.ProtocolCommandListener.isTrue()) {
					objFTPClient.addProtocolCommandListener(objProtocolCommandListener);
					logger.debug("ProtocolcommandListener added and activated");
				}
			}

			// TODO implement as an additional Option-setting
			String strAddFTPProtocol = System.getenv("AddFTPProtocol");
			if (strAddFTPProtocol != null && strAddFTPProtocol.equalsIgnoreCase("true")) {
				objFTPClient.addProtocolCommandListener(objProtocolCommandListener);
			}

		}
		return objFTPClient;
	}

	@Override
	public void closeInput() {
	}

	private void closeInput(InputStream objO) {
		try {
			if (objO != null) {
				objO.close();
				objO = null;
			}
		}
		catch (IOException e) {
		}
	}

	private void closeObject(OutputStream objO) {
		try {
			if (objO != null) {
				objO.flush();
				objO.close();
				objO = null;
			}
		}
		catch (Exception e) {
		}
	}

@Override
	public String createScriptFile(final String pstrContent) throws Exception {
		notImplemented();
		return null;
	}

	/**
	 * return a listing of the files of the current directory in long format on
	 * the remote machine
	 * @return a listing of the contents of the current directory on the remote machine
	 * @exception Exception
	 * @see #nList()
	 * @see #nList( String )
	 * @see #dir( String )
	 */
	@Override
	public SOSFileList dir() {
		final String conMethodName = conClassName + "::dir";

		try {
			return dir(".");
		}
		catch (Exception e) {
			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		}
		return null;
	}

	@Override
	public SOSFileList dir(final SOSFolderName pobjFolderName) {
		this.dir(pobjFolderName.Value());
		return null;
	}

	/**
	 * return a listing of the files in a directory in long format on
	 * the remote machine
	 * @param pathname on remote machine
	 * @return a listing of the contents of a directory on the remote machine
	 * @exception Exception
	 * @see #nList()
	 * @see #nList( String )
	 * @see #dir()
	 */
	@Override
	public SOSFileList dir(final String pathname) {
		Vector<String> strList = getFilenames(pathname);
		String[] strT = strList.toArray(new String[strList.size()]);
		SOSFileList objFileList = new SOSFileList(strT);
		return objFileList;
	}

	/**
	 * return a listing of a directory in long format on
	 * the remote machine
	 *
	 * @param pathname on remote machine
	 * @return a listing of the contents of a directory on the remote machine
	 * @exception Exception
	 * @see #nList()
	 * @see #nList( String )
	 * @see #dir()
	 */
	@Override
	public SOSFileList dir(final String pathname, final int flag) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::dir";

		SOSFileList fileList = new SOSFileList();
		FTPFile[] listFiles = null;
		try {
			listFiles = Client().listFiles(pathname);
		}
		catch (IOException e) {
			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		}

		for (FTPFile listFile : listFiles) {
			if (flag > 0 && listFile.isDirectory()) {
				fileList.addAll(this.dir(pathname + "/" + listFile.toString(), flag >= 1024 ? flag : flag + 1024));
			}
			else {
				if (flag >= 1024) {
					fileList.add(pathname + "/" + listFile.toString());
				}
				else {
					fileList.add(listFile.toString());
				}
			}
		}
		return fileList;
	}

	@Override
	public void ExecuteCommand(final String strCmd) throws Exception {
		final String conMethodName = conClassName + "::ExecuteCommand";

		objFTPClient.sendCommand(strCmd);
		logger.debug(HostID(SOSVfs_E_0106.params(conMethodName, strCmd, getReplyString())));
		objFTPClient.sendCommand("NOOP");
		getReplyString();
	}

	@Override
	public void flush() {
	}

	//	@Override
//	public long size(final String pstrRemoteFileName) throws Exception {
//		long lngSize = -1L;
//		FTPFile[] objFTPFileList = Client().listFiles(pstrRemoteFileName);
//		if (objFTPFileList != null) {
//			for (FTPFile objFTPFile : objFTPFileList) {
//				if (objFTPFile.getName().equalsIgnoreCase(pstrRemoteFileName)) {
//					lngSize = objFTPFile.getSize();
//					break;
//				}
//			}
//			return lngSize;
//		}
//		else {
//			Client().sendCommand("SIZE " + pstrRemoteFileName);
//			LogReply();
//			if (Client().getReplyCode() == FTPReply.FILE_STATUS)
//				return Long.parseLong(trimResponseCode(this.getReplyString()));
//			else
//				return -1L; // file not found or size not available
//		}
//	}
//
	/**
	 * Retrieves a named file from the ftp server.
	 *
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @exception Exception
	 * @see #getFile( String, String )
	 */
	@Override
	public void get(final String remoteFile, final String localFile) {
		final String conMethodName = conClassName + "::get";

		FileOutputStream out = null;
		boolean rc = false;
		try {
			out = new FileOutputStream(localFile);
			rc = Client().retrieveFile(remoteFile, out);
			if (rc == false) {
				RaiseException(HostID(SOSVfs_E_0105.params(conMethodName)));
			}
		}
		catch (IOException e) {
			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		}
		finally {
			closeObject(out);
		}
	} // get

	public FTPClient getClient() {
		return Client();
	}

	@Override
	public ISOSConnection getConnection() {
		return this;
	}

	@Override
	public Integer getExitCode() {
		notImplemented();
		return null;
	}

	@Override
	public String getExitSignal() {
		notImplemented();
		return null;
	}

	/**
	 * Retrieves a named file from the ftp server.
	 *
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @return  The total number of bytes retrieved.
	 * @see #get( String, String )
	 * @exception Exception
	 */
	@Override
	public long getFile(final String remoteFile, final String localFile) {
		final boolean flgAppendLocalFile = false;
		return this.getFile(remoteFile, localFile, flgAppendLocalFile);
	}

	/**
	 * Retrieves a named file from the ftp server.
	 *
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @param append Appends the remote file to the local file.
	 * @return  The total number of bytes retrieved.
	 * @see #get( String, String )
	 * @exception Exception
	 */
	@Override
	public long getFile(final String remoteFile, final String localFile, final boolean append) {
		final String conMethodName = conClassName + "::getFile";

		InputStream in = null;
		OutputStream out = null;
		long totalBytes = 0;
		try {
			// TODO get filesize and report as a message
			in = Client().retrieveFileStream(remoteFile);
			if (in == null) {
				throw new JobSchedulerException(SOSVfs_E_143.params(getReplyString()));
			}
			if (isPositiveCommandCompletion() == false) {
				throw new JobSchedulerException(SOSVfs_E_144.params("getFile()", remoteFile, getReplyString()));
			}
			byte[] buffer = new byte[Options().BufferSize.value()];
			out = new FileOutputStream(new File(localFile), append);
			// TODO get progress info
			int bytes_read = 0;
			synchronized (this) {
				while ((bytes_read = in.read(buffer)) != -1) {
					// TODO create progress message
					out.write(buffer, 0, bytes_read);
					out.flush();
					totalBytes += bytes_read;
				}
			}
			closeInput(in);
			closeObject(out);
			this.CompletePendingCommand();
			// TODO create completed Message
			if (totalBytes > 0)
				return totalBytes;
			else
				return -1L;
		}
		catch (IOException e) {
			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		}
		finally {
			closeInput(in);
			closeObject(out);
		}
		return totalBytes;
	}

	@Override
	public ISOSVirtualFile getFileHandle(final String pstrFilename) {
		final String conMethodName = conClassName + "::getFileHandle";
		String strT = pstrFilename.replaceAll("\\\\", "/");
		ISOSVirtualFile objFtpFile = new SOSVfsFtpFile(strT);
		objFtpFile.setHandler(this);
		//		ISOSVirtualFile objFtpFile = null;
		//		try {
		//			FTPFile[] objFTPFiles = Client().listFiles(strT);
		//			objFtpFile = new SOSVfsFtpFile(objFTPFiles[0]);
		//			objFtpFile.setHandler(this);
		//		}
		//		catch (IOException e) {
		//			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		//		}
		logger.trace(SOSVfs_D_152.params(strT, conMethodName));
		return objFtpFile;
	}

	@Override
	@Deprecated // use getFilenames in superclass
	public String[] getFilelist(final String folder, final String regexp, final int flag, final boolean flgRecurseSubFolder) {
		// TODO vecDirectoryListing = null; prüfen, ob notwendig
		vecDirectoryListing = nList(folder, flgRecurseSubFolder);
		Vector<String> strB = new Vector<String>();
		Pattern pattern = Pattern.compile(regexp, flag);
		for (String strFile : vecDirectoryListing) {
			/**
			 * the file_spec has to be compared to the filename only ... excluding the path
			 */
			String strFileName = new File(strFile).getName();
			Matcher matcher = pattern.matcher(strFileName);
			if (matcher.find() == true) {
				strB.add(strFile);
			}
		}
		return strB.toArray(new String[strB.size()]);
	}

	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine (without subdirectory)
	 *
	 * @return a listing of the contents of a directory on the remote machine
	 *
	 * @exception Exception
	 * @see #nList( String )
	 * @see #dir()
	 * @see #dir( String )
	 */
	private Vector<String> getFilenames() throws Exception {
		return getFilenames("", false, null);
	} // getFilenames

	private Vector<String> getFilenames(final boolean flgRecurseSubFolders) throws Exception {
		return getFilenames("", flgRecurseSubFolders, null);
	} // getFilenames

	private Vector<String> getFilenames(final String pathname) {
		return getFilenames(pathname, false, null);
	}

	@Override
	public Vector<ISOSVirtualFile> getFiles() {
		return null;
	}

	@Override
	public Vector<ISOSVirtualFile> getFiles(final String string) {
		return null;
	}
	@Override
	public ISOSVFSHandler getHandler() {
		return this;
	}

@Override
	public SOSFileListEntry getNewVirtualFile(final String pstrFileName) {
		SOSFileListEntry objF = new SOSFileListEntry(pstrFileName);
		objF.VfsHandler(this);
		return objF;
	}

	@Override
	public ISOSSession getSession() {
		return null;
	}

	@Override
	public String[] listNames(final String pathname) throws IOException {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::listNames";
		String strT = pathname.replaceAll("\\\\", "/");
		String strA[] = Client().listNames(strT);
		if (strA != null) {
			for (int i = 0; i < strA.length; i++) {
				strA[i] = strA[i].replaceAll("\\\\", "/");
			}
		}
		else {
			strA = new String[] {};
		}
		logger.debug(SOSVfs_D_137.params(Client().getReplyString(), Client().getReplyCode()));
		return strA;
	}

	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine
	 *
	 * @return a listing of the contents of a directory on the remote machine
	 *
	 * @exception Exception
	 * @see #nList( String )
	 * @see #dir()
	 * @see #dir( String )
	 */
	@Override
	public Vector<String> nList() throws Exception {
		return getFilenames();
	} // nList

	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine
	 *
	 * @return a listing of the contents of a directory on the remote machine
	 *
	 * @exception Exception
	 * @see #nList( String )
	 * @see #dir()
	 * @see #dir( String )
	 */
	@Override
	public Vector<String> nList(final boolean recursive) throws Exception {
		return getFilenames(recursive);
	} // nList

	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine
	 * @param pathname on remote machine
	 * @return a listing of the contents of a directory on the remote machine
	 *
	 * @exception Exception
	 * @see #dir()
	 */
	@Override
	public Vector<String> nList(final String pathname) {
		return getFilenames(pathname);
	} // nList

	//	private Vector<String> getFilenames(final String pstrPathName, final boolean flgRecurseSubFolders) {
//
//		String conMethodName = "getFilenames";
//		String strCurrentDirectory = null;
//		Vector<String> vecDirectoryListing = new Vector<String>();
//		//			String[] fileList = null;
//		strCurrentDirectory = DoPWD();
//		String lstrPathName = pstrPathName.trim();
//		if (lstrPathName.length() <= 0) {
//			lstrPathName = ".";
//		}
//		if (lstrPathName.equals(".")) {
//			lstrPathName = strCurrentDirectory;
//		}
//		FTPFile[] objFTPFileList = null;
//		getFileListEntries();
//
//		try {
//			objFTPFileList = Client().listFiles(lstrPathName);
//		}
//		catch (IOException e) {
//			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
//		}
//
//		if (objFTPFileList == null || objFTPFileList.length <= 0) {
//			return vecDirectoryListing;
//		}
//
//		for (FTPFile objFTPFile : objFTPFileList) {
//			String strCurrentFile = objFTPFile.getName();
//			if (isNotHiddenFile(strCurrentFile) && strCurrentFile.trim().length() > 0) {
//				boolean flgIsDirectory = objFTPFile.isDirectory();
//				if (flgIsDirectory == false) {
//					if (lstrPathName.startsWith("/") == false) { // JIRA SOSFTP-124
//						if (strCurrentFile.startsWith(strCurrentDirectory) == false) {
//							strCurrentFile = addFileSeparator(strCurrentDirectory) + strCurrentFile;
//						}
//					}
//					vecDirectoryListing.add(strCurrentFile);
//					SOSFileListEntry objF = new SOSFileListEntry(objFTPFile);
//					objF.VfsHandler(this);
//					objFileListEntries.add(objF);
//				}
//				else {
//					if (flgIsDirectory && flgRecurseSubFolders == true) {
//						DoCD(strCurrentDirectory);
//						if (flgRecurseSubFolders) {
//							logger.debug(String.format(""));
//							Vector<String> vecNames = getFilenames(strCurrentFile, flgRecurseSubFolders);
//							if (vecNames != null) {
//								vecDirectoryListing.addAll(vecNames);
//							}
//						}
//					}
//				}
//			}
//		}
//
//		logger.debug("strCurrentDirectory = " + strCurrentDirectory);
//		if (strCurrentDirectory != null) {
//			DoCD(strCurrentDirectory);
//			DoPWD();
//		}
//		return vecDirectoryListing;
//	}// nList
//
	@Override
	public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::nList";

		try {
			return getFilenames(pathname, flgRecurseSubFolder, null);
		}
		catch (Exception e) {
			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		}
		return null; // useless, but required by syntax-check
	} // nList

	@Override
	public void openInputFile(final String pstrFileName) {
	}

	@Override
	public void openOutputFile(final String pstrFileName) {
	}

	/**
	 * Stores a file on the server using the given name.
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @return True if successfully completed, false if not.
	 * @exception Exception
	 * @see #putFile( String, String )
	 */
	@Override
	public void put(final String localFile, final String remoteFile) {
		final String conMethodName = conClassName + "::put";

		FileInputStream in = null;
		boolean rc = false;
		try {
			in = new FileInputStream(localFile);
			// TODO get progress info
			rc = Client().storeFile(remoteFile, in);
			if (rc == false) {
				RaiseException(SOSVfs_E_154.params("put"));
			}
		}
		catch (Exception e) {
			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		}
		finally {
			closeInput(in);
		}
	}

	@Override
	public void putFile(final ISOSVirtualFile objVirtualFile) {
		final String conMethodName = conClassName + "::putFile";

		String strName = objVirtualFile.getName();
		ISOSVirtualFile objVF = this.getFileHandle(strName);
		OutputStream objOS = objVF.getFileOutputStream();
		InputStream objFI = objVirtualFile.getFileInputStream();
		// TODO Buffersize must be defined via Options-Class
		int lngBufferSize = 1024;
		byte[] buffer = new byte[lngBufferSize];
		int intBytesTransferred;
		long totalBytes = 0;
		try {
			synchronized (this) {
				while ((intBytesTransferred = objFI.read(buffer)) != -1) {
					objOS.write(buffer, 0, intBytesTransferred);
					totalBytes += intBytesTransferred;
				}
				objFI.close();
				objOS.flush();
				objOS.close();
			}
		}
		catch (Exception e) {
			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		}
		finally {
		}
	}

	/**
	 * written to store a file on the server using the given name.
	 *
	 * @param localfile The name of the local file.
	 * @param an OutputStream through which data can be
	 * @return The total number of bytes written.
	 * @exception Exception
	 */
	@Override
	public long putFile(final String localFile, final OutputStream out) {
		final String conMethodName = conClassName + "::putFile";

		if (out == null)
			RaiseException("OutputStream null value.");
		FileInputStream in = null;
		long lngTotalBytesWritten = 0;
		try {
			byte[] buffer = new byte[Options().BufferSize.value()];
			in = new FileInputStream(new File(localFile));
			// TODO get progress info
			int bytesWritten;
			synchronized (this) {
				while ((bytesWritten = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytesWritten);
					lngTotalBytesWritten += bytesWritten;
				}
			}
			closeInput(in);
			closeObject(out);
			this.CompletePendingCommand();
			return lngTotalBytesWritten;
		}
		catch (Exception e) {
			RaiseException(e, HostID(SOSVfs_E_0105.params(conMethodName)));
		}
		finally {
			closeInput(in);
			closeObject(out);
		}
		return lngTotalBytesWritten;
	} // putFile

	/**
	 * Stores a file on the server using the given name.
	 *
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @return The total number of bytes written.
	 *
	 * @exception Exception
	 * @see #put( String, String )
	 */
	@Override
	// ISOSVfsFileTransfer
	public long putFile(final String localFile, final String remoteFile) throws Exception {
		OutputStream outputStream = Client().storeFileStream(remoteFile);
		if (isNegativeCommandCompletion()) {
			RaiseException(SOSVfs_E_144.params("storeFileStream()", remoteFile, getReplyString()));
		}
		long i = putFile(localFile, outputStream);
		logger.debug(SOSVfs_D_146.params(localFile, remoteFile));
		return i;
	} // putFile

	@Override
	public int read(final byte[] bteBuffer) {
		return 0;
	}

	@Override
	public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
		return 0;
	}

	@Override
	public boolean remoteIsWindowsShell() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void rename(final String from, final String to) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::rename";

		try {
			this.Client().rename(from, to);
		}
		catch (IOException e) {
			RaiseException(e, SOSVfs_E_134.params("rename()"));
		}
		logger.info(SOSVfs_I_150.params(from, to));
	}

	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
		// TODO Auto-generated method stub
	}

	@Override
	public void write(final byte[] bteBuffer) {
	}

	@Override
	public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::write";
	}
}
