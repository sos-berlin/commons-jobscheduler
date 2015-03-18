package com.sos.VirtualFileSystem.common;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSDataProviderOptions;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSSession;
import com.sos.VirtualFileSystem.Interfaces.ISOSShellOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFileSystem;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFolder;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSVfsTransferBaseClass extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {

	private final static Logger					logger						= Logger.getLogger(SOSVfsTransferBaseClass.class);

	/** key file: ~/.ssh/id_rsa or ~/.ssh/id_dsa (must be OpenSSH-Format) */
	protected String							authenticationFilename		= "";
	private Vector<String>						directoryListing			= null;

	protected String							host						= EMPTY_STRING;
	protected int								port						= 0;
	protected String							userName					= EMPTY_STRING;

	protected String							reply						= "OK";

	// keep Track of current directory for ftp emulation
	protected String							currentDirectory			= "";

	protected SOSConnection2OptionsAlternate	connection2OptionsAlternate	= null;

	@SuppressWarnings("unused")
	protected ISOSAuthenticationOptions			authenticationOptions		= null;

	/**
	 *
	 * \brief SOSVfsSFtpBaseClass
	 *
	 * \details
	 *
	 */
	public SOSVfsTransferBaseClass() {
		super();
	}

	protected String HostID(final String msg) {
		return "(" + userName + "@" + host + ":" + port + ") " + msg;
	}

	/**
	 * turn passive transfer mode on.
	 *
	 * @return The reply code received from the server.
	 */
	@Override
	public int passive() {
		return 0;
	}

	@Override
	public String DoPWD() {
		try {
			logDEBUG(SOSVfs_D_141.params("pwd."));
			return this.getCurrentPath();
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_134.params("pwd"));
			return null;
		}
	} // private int DoPWD

	/**
	 * wird überschrieben
	 *
	 *
	 * \brief getCurrentPath
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	protected String getCurrentPath() {
		return null;
	}

	protected boolean LogReply() {
		reply = getReplyString();
		if (reply.trim().length() > 0) {
			logDEBUG(reply);
		}

		return true;
	} // private boolean LogReply

	@Override
	public boolean isNegativeCommandCompletion() {

		int x = 0;
		// TODO separate Routine draus machen

		// try {
		// if (Client().completePendingCommand() == false) {
		// logout();
		// disconnect();
		// RaiseException("File transfer failed. completePendingCommand() returns false");
		// }
		// }
		// catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// int x = getReplyCode();

		return x > 300;
	} // private boolean isNegativeCommandCompletion

	@Override
	public void CompletePendingCommand() {
		// try {
		// if (Client().completePendingCommand() == false) {
		// logout();
		// disconnect();
		// RaiseException("File transfer failed. completePendingCommand() returns false");
		// }
		// }
		// catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// RaiseException("File transfer failed. completePendingCommand() raised an exception");
		// }

	}

	/**
	 * Indicates what to do if the server's host key changed or the server is unknown.
	 * One of yes (refuse connection), ask (ask the user whether to add/change the key)
	 * and no (always insert the new key).
	 *
	 * @param pstrStrictHostKeyCheckingValue
	 */
	public void StrictHostKeyChecking (final String pstrStrictHostKeyCheckingValue) {

	}

	private boolean isPositiveCommandCompletion() {
		int x = 0;

		return x <= 300;
	} // private boolean isPositiveCommandCompletion

	public boolean isNotHiddenFile(final String strFileName) {
		if (strFileName.equalsIgnoreCase("..") == false && strFileName.equalsIgnoreCase(".") == false) {
			return true; // not a hidden file
		}

		return false; // it is a hidden-file
	} // private boolean isNotHiddenFile

	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine
	 * @param pathname on remote machine
	 * @return a listing of the contents of a directory on the remote machine
	 *
	 * @exception RuntimeException
	 * @see #dir()
	 */
	@Override
	public Vector<String> nList(final String pathname) {
		return getFilenames(pathname);
	} // nList

	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine
	 * @param pathname on remote machine
	 * @return a listing of the contents of a directory on the remote machine
	 *
	 * @exception JobSchedulerException
	 * @see #dir()
	 */
	@Override
	public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {

		Vector<String> result = null;
		try {
			result = getFilenames(pathname, flgRecurseSubFolder);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_134.params("nList"));
		}
		return result;
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
	public Vector<String> nList() {
		Vector<String> result = null;
		try {
			result = getFilenames();
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_134.params("nList"));
		}
		return result;

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
	public Vector<String> nList(final boolean recursive) {

		Vector<String> result = null;
		try {
			result = getFilenames(recursive);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_134.params("nList"));
		}
		return result;
	} // nList

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

		SOSFileList fileList = new SOSFileList();
		String[] listFiles = null;
		try {
			listFiles = this.listNames(pathname);
		}
		catch (IOException e) {
			RaiseException(e, SOSVfs_E_128.params(listFiles, "dir()"));
		}
		if (listFiles != null) {
			for (String listFile : listFiles) {
				if (flag > 0 && isDirectory(listFile)) {
					fileList.addAll(this.dir(pathname + "/" + listFile, flag >= 1024 ? flag : flag + 1024));
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
		}
		return fileList;
	}

	/**
	 * Checks if file is a directory
	 *
	 * @param filename
	 * @return true, if filename is a directory
	 */
	@Override
	public boolean isDirectory(final String filename) {
		logINFO("not implemented yet");
		return false;
	}

	/**
	 * wird überschrieben
	 *
	 * \brief listNames
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pathname
	 * @return
	 * @throws IOException
	 */
	@Override
	public String[] listNames(final String path) throws IOException {
		logINFO("not implemented yet");
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
	public SOSFileList dir() {
		try {
			return dir(".");
		}
		catch (Exception e) {
			throw new RuntimeException(SOSVfs_E_130.params("dir"), e);
		}
	}

	/**
	 * @return The entire text from the last FTP response as a String.
	 */
	public String getResponse() {
		return this.getReplyString();
	}

	/**
	* trim the response code at the beginning
	* @param response
	* @return the response string without response code
	* @throws Exception
	*/
	@SuppressWarnings("unused")
	protected String trimResponseCode(final String response) throws Exception {
		if (response.length() < 5)
			return response;
		return response.substring(4).trim();
	}

	protected void closeObject(OutputStream objO) {
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

	protected void closeInput(InputStream objO) {
		try {
			if (objO != null) {
				objO.close();
				objO = null;
			}
		}
		catch (IOException e) {
		}

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
		long lngNoOfBytesRead = 0;
		try {
			lngNoOfBytesRead = this.getFile(remoteFile, localFile, flgAppendLocalFile);
		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return lngNoOfBytesRead;
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

		this.putFile(localFile, remoteFile);
	}

	/**
	 * Stores a file on the server using the given name.
	 *
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @return file size
	 *
	 * @exception Exception
	 * @see #put( String, String )
	 */
	@Override
	// ISOSVfsFileTransfer
	public long putFile(final String localFile, final String remoteFile) {
		logINFO("not implemented yet");
		return 0;
	}

	/**
	 * written to store a file on the server using the given name.
	 *
	 * @param localfile The name of the local file.
	 * @param an OutputStream through which data can be
	 * @return The total number of bytes written.
	 * @exception Exception
	 */
	@SuppressWarnings("null")
	@Override
	public long putFile(final String localFile, final OutputStream out) {
		if (out == null) {
			RaiseException(SOSVfs_E_147.get());
		}

		FileInputStream in = null;
		long bytesWrittenTotal = 0;
		try {
			// TODO Buffersize must be an Option
			byte[] buffer = new byte[4096];
			in = new FileInputStream(new File(localFile));
			// TODO get progress info
			int bytesWritten;
			synchronized (this) {
				while ((bytesWritten = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytesWritten);
					bytesWrittenTotal += bytesWritten;
				}
			}
			closeInput(in);
			closeObject(out);
			return bytesWrittenTotal;
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_130.params("putFile()"));
		}
		finally {
			closeInput(in);
			closeObject(out);
		}
		return bytesWrittenTotal;
	} // putFile

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
	@Override
	public long appendFile(final String localFile, final String remoteFile) {
		notImplemented();
		return -1;
	} // appendFile

	/**
	 * Using ASCII mode for file transfers
	 * @return True if successfully completed, false if not.
	 * @throws IOException If an I/O error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	@Override
	public void ascii() {
		// try {
		// boolean flgResult = Client().setFileType(FTP.ASCII_FILE_TYPE);
		// if (flgResult == false) {
		// throw new JobSchedulerException("setFileType not possible, due to : " + getReplyString());
		// }
		// }
		// catch (IOException e) {
		// throw new JobSchedulerException("ascii returns an exception", e);
		// }
	}

	/**
	 * Using Binary mode for file transfers
	 * @return True if successfully completed, false if not.
	 * @throws IOException If an I/O error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	@Override
	public void binary() {
		// try {
		// boolean flgResult = Client().setFileType(FTP.BINARY_FILE_TYPE);
		// if (flgResult == false) {
		// throw new JobSchedulerException("setFileType not possible, due to : " + getReplyString());
		// }
		//
		// }
		// catch (IOException e) {
		// throw new JobSchedulerException("setFileType to binary returns an exception", e);
		// }
	}

	/**
	 *
	 * @param directory The new working directory.
	 * @return The reply code received from the server.
	 * @throws IOException If an I/O error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	public int cd(final String directory) throws Exception {
		changeWorkingDirectory(directory);
		return 1;
	}

	/**
	 * wird überschrieben
	 *
	 * \brief fileExists
	 *
	 * \details
	 *
	 * \return boolean
	 *
	 * @param filename
	 * @return
	 */
	protected boolean fileExists(final String filename) {
		return false;
	}

	/**
	 * wird überschrieben
	 *
	 * \brief changeWorkingDirectory
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pathname
	 * @return
	 * @throws IOException
	 */
	@Override
	public boolean changeWorkingDirectory(final String pathname) throws IOException {
		logINFO("not implemented yet");
		return true;
	}

	protected String resolvePathname(String pathname) {
		if (!pathname.startsWith("./") && !pathname.startsWith("/") && currentDirectory.length() > 0) {
			// if (!pathname.startsWith("/") && currentDirectory.length()>0){
			String slash = "";
			if (!currentDirectory.endsWith("/"))
				slash = "/";
			pathname = currentDirectory + slash + pathname;
		}
		while (pathname.contains("\\")) {
			pathname = pathname.replace('\\', '/');
		}
		return pathname;
	}

	@Override
	public void login(final String strUserName, final String strPassword) {

	} // private boolean login

	@Override
	public void disconnect() {
	}

	@Override
	public String getReplyString() {
		String strT = reply;
		return strT;
	}

	/**
	 * wird überschrieben
	 *
	 * \brief isConnected
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public boolean isConnected() {
		return false;
	}

	// @Override
	// public String[] listNames(String pathname) throws IOException {
	// String strA[] = Client().listNames(pathname);
	// logger.debug(String.format("reply from FTP-Server is %1$s, code = %2$d", Client().getReplyString(), Client().getReplyCode()));
	// return strA;
	// }

	@Override
	public void logout() {
		try {
			if (isConnected() == true) {
				disconnect();
				logDEBUG(SOSVfs_D_138.params(host, getReplyString()));
			}
			else {
				logINFO("not connected, logout useless.");
			}
		}
		catch (Exception e) { // no error-handling needed, due to end-of session
			logWARN(SOSVfs_W_140.get() + e.getMessage());
		}
	}

	@Override
	public ISOSVFSHandler getHandler() {
		return this;
	}

	@Override
	public void ExecuteCommand(final String strCmd) throws Exception {
		logINFO("not implemented yet");
	}

	@Override
	public String createScriptFile(final String pstrContent) throws Exception {
		notImplemented();
		return null;
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

	@Override
	public ISOSConnection Authenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
		return this;
	}

	@Override
	public void CloseConnection() throws Exception {
		if (isConnected()) {
			disconnect();
			logDEBUG(SOSVfs_D_125.params(host));
			LogReply();
		}
	}

	@Override
	public ISOSConnection Connect() {
		//this.connect(objConnection2Options.host.Value(), objConnection2Options.port.value());
		notImplemented();
		return this;
	}

	@Override
	public ISOSConnection Connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) {

		return this;
	}

	@Override
	public ISOSConnection Connect(final ISOSConnectionOptions pobjConnectionOptions) throws Exception {
		notImplemented();

		return this;
	}

	@Override
	public ISOSConnection Connect(final String pstrHostName, final int pintPortNumber) throws Exception {
		notImplemented();

		return this;
	}

	@Override
	public void CloseSession() throws Exception {
		this.logout();
	}

	@Override
	public ISOSSession OpenSession(final ISOSShellOptions pobjShellOptions) throws Exception {
		notImplemented();
		return null;
	}

	@Override
	public ISOSVirtualFile TransferMode(final SOSOptionTransferMode pobjFileTransferMode) {

		if (pobjFileTransferMode.isAscii()) {
			this.ascii();
		}
		else {
			this.binary();
		}
		return null;
	}

	public SOSFileListEntry getNewVirtualFile(final String pstrFileName) {
		SOSFileListEntry objF = new SOSFileListEntry(pstrFileName);
		objF.VfsHandler(this);
		return objF;
	}

	@Override
	public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) throws IOException {
		this.mkdir(pobjFolderName.Value());
		return null;
	}

	@Override
	public boolean rmdir(final SOSFolderName pobjFolderName) throws IOException {
		this.rmdir(pobjFolderName.Value());
		return true;
	}

	@Override
	public ISOSConnection getConnection() {
		return this;
	}

	@Override
	public ISOSSession getSession() {
		return null;
	}

	@Override
	public SOSFileList dir(final SOSFolderName pobjFolderName) {
		this.dir(pobjFolderName.Value());
		return null;
	}

	@Override
	public StringBuffer getStdErr() throws Exception {
		 
		return null;
	}

	@Override
	public StringBuffer getStdOut() throws Exception {
		 
		return null;
	}

	@Override
	public boolean remoteIsWindowsShell() {
		 
		return false;
	}

	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
		 

	}

	/**
	 * wird überschrieben
	 *
	 * \brief getFileHandle
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pstrFilename
	 * @return
	 */
	@Override
	public ISOSVirtualFile getFileHandle(final String pstrFilename) {
		return null;
	}

	@Override
	public String[] getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
		Vector<String> result = nList(folder, withSubFolder);

		Vector<String> newResult = new Vector<String>();
		Pattern pattern = Pattern.compile(regexp, flag);
		for (String strFile : result) {
			/**
			 * the file_spec has to be compared to the filename only ... excluding the path
			 */

			Matcher matcher = pattern.matcher(new File(strFile).getName());
			if (matcher.find() == true) {
				newResult.add(strFile);
			}
		}

		return newResult.toArray(new String[newResult.size()]);
	}

	@Override
	public String[] getFolderlist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
		Vector<String> result = nList(folder, withSubFolder);

		Vector<String> newResult = new Vector<String>();
		Pattern pattern = Pattern.compile(regexp, flag);
		for (String strFile : result) {
			/**
			 * the file_spec has to be compared to the filename only ... excluding the path
			 */

			Matcher matcher = pattern.matcher(new File(strFile).getName());
			if (matcher.find() == true) {
				newResult.add(strFile);
			}
		}

		return newResult.toArray(new String[newResult.size()]);
	}

	@Override
	public OutputStream getAppendFileStream(final String strFileName) {
		OutputStream objO = null;
		return objO;
	}

	protected long size(final String strFileName) throws Exception {
		logINFO("not implemented yet");
		return 0;
	}

	@Override
	public long getFileSize(final String strFileName) {
		long lngFileSize = 0;
		try {
			String lstrFileName = strFileName.replaceAll("\\\\", "/");
			lngFileSize = this.size(lstrFileName);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_134.params("getFileSize()"));
		}
		 
		return lngFileSize;
	}

	@Override
	public InputStream getInputStream(final String strFileName) {

		InputStream objI = null;
		// SFTPv3FileHandle objI = getClient().openFileRO(strFileName);

		return objI;
	}

	@Override
	public String getModificationTime(final String fileName) {
		logINFO("not implemented yet");
		return null;
	}

	@Override
	public abstract OutputStream getOutputStream(final String fileName);
//	{
//		logINFO("not implemented yet");
//		return null;
//	}

	@Override
	public void close() {
	}

	@Override
	public void closeInput() {
	}

	@Override
	public void closeOutput() {
	}

	@Override
	public void flush() {
	}

	@Override
	public int read(final byte[] bteBuffer) {
		return 0;
	}

	@Override
	public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
		return 0;
	}

	@Override
	public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
		logINFO("not implemented yet");
	}

	@Override
	public void write(final byte[] bteBuffer) {
		 

	}

	@Override
	public void openInputFile(final String pstrFileName) {

	}

	@Override
	public void openOutputFile(final String pstrFileName) {
	}

	@Override
	public Vector<ISOSVirtualFile> getFiles(final String string) {
		 
		return null;
	}

	@Override
	public Vector<ISOSVirtualFile> getFiles() {
		 
		return null;
	}

	@Override
	public void putFile(final ISOSVirtualFile objVirtualFile) {

		String strName = objVirtualFile.getName();
		// strName = new File(strName).getAbsolutePath();
		// if (strName.startsWith("c:") == true) {
		// strName = strName.substring(3);
		// }
		ISOSVirtualFile objVF = this.getFileHandle(strName);
		OutputStream objOS = objVF.getFileOutputStream();

		InputStream objFI = objVirtualFile.getFileInputStream();

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
			throw new JobSchedulerException(SOSVfs_E_130.params("putfile()"), e);
		}
		finally {
		}

	}

	@Override
	public void ControlEncoding(final String pstrControlEncoding) {

	}

	@Override
	public void mkdir(final String pathname) throws IOException {
		logINFO("not implemented yet");
	}

	@Override
	public void rmdir(final String pstrFolderName) throws IOException {
		logINFO("not implemented yet");
	}

	@Override
	public void delete(final String pathname) throws IOException {
		logINFO("not implemented yet");
	}

	@Override
	public long getFile(final String remoteFile, final String localFile, final boolean append) throws Exception {
		 
		return 0;
	}

	/**
	 * wird überschrieben
	 *
	 * \brief rename
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param strFileName
	 * @param pstrNewFileName
	 */
	@Override
	public void rename(final String from, final String to) {
		logINFO("not implemented yet");
	}

	/**
	 *
	 * \brief RaiseException
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param e	
	 * @param msg
	 */
	protected void RaiseException(final Exception e, final String msg) {
		logger.error(msg + " (" + e.getLocalizedMessage() + ")");
		throw new JobSchedulerException(msg, e);
	}

	/**
	 *
	 * \brief RaiseException
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void RaiseException(final String msg) {
		logEXCEPTION(msg);
		throw new JobSchedulerException(msg);
	}

	/**
	 *
	 * \brief getLogPrefix
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	private String getLogPrefix() {
		return getLogPrefix(4);
	}

	/**
	 *
	 * \brief getLogPrefix
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	private String getLogPrefix(final int level) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[level];
		String[] classNameArr = ste.getClassName().split("\\.");

		return "(" + classNameArr[classNameArr.length - 1] + "::" + ste.getMethodName() + ") ";
	}

	/**
	 *
	 * \brief logINFO
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void logINFO(final Object msg) {
		logger.info(this.getLogPrefix() + msg);
	}

	/**
	 *
	 * \brief logDEBUG
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void logDEBUG(final Object msg) {
		logger.debug(this.getLogPrefix() + msg);
	}

	/**
	 *
	 * \brief logWARN
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void logWARN(final Object msg) {
		logger.warn(this.getLogPrefix() + msg);
	}

	/**
	 *
	 * \brief logERROR
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param msg
	 */
	protected void logERROR(final Object msg) {
		logger.error(this.getLogPrefix() + msg);
	}

	protected void logEXCEPTION(final Object msg) {
		logger.error(this.getLogPrefix(4) + msg);
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
		return getFilenames("", false);
	} // getFilenames

	private Vector<String> getFilenames(final boolean flgRecurseSubFolders) throws Exception {
		return getFilenames("", flgRecurseSubFolders);
	} // getFilenames

	private Vector<String> getFilenames(final String pathname) {
		try {
			return getFilenames(pathname, false);
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_130.params("getFilelist"));
			return null;
		}
	}

	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine (without subdirectory)
	 *
	 * @param pathname on remote machine
	 * @return a listing of the contents of a directory on the remote machine
	 * @throws IOException
	 *
	 * @exception Exception
	 * @see #dir()
	 */
	private Vector<String> getFilenames(final String path, final boolean recurseSubFolders) throws Exception {
		return getFilenames(path, recurseSubFolders, 0);
	}

	private Vector<String> getFilenames(String path, final boolean recurseSubFolders, int recLevel) throws Exception {

		if (recLevel == 0) {
			directoryListing = new Vector<String>();
		}

		String[] fileList = null;
		String currentPath = this.DoPWD();

		path = path.trim();
		if (path.length() <= 0) {
			path = ".";
		}
		if (path.equals(".")) {
			path = currentPath;
		}

		try {
			fileList = listNames(path);
		}
		catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}

		if (fileList == null) {
			return directoryListing;
		}

		for (String strCurrentFile : fileList) {
			if (strCurrentFile == null || strCurrentFile.endsWith("/.") || strCurrentFile.endsWith("/..")) {
				continue;
			}

			/**
			 * kb 2012-10-08
			 * The filename could be a subfolder. the name of this subfolder must not included in the
			 * list of filenames.
			 */
			//			directoryListing.add(strCurrentFile);

			/**
			 * isDirectory is suboptiomal in this situation. think of links
			 * better is isRegularFile instead
			 * TODO create isRegularFile
			 */
			if (this.isDirectory(strCurrentFile) == true) {
				if (recurseSubFolders) {
					this.cd(strCurrentFile);
					recLevel++;
					this.getFilenames(strCurrentFile, recurseSubFolders, recLevel);
				}
			}
			else {
				directoryListing.add(strCurrentFile);
			}
		}

		logDEBUG("currentPath = " + currentPath);

		return directoryListing;

	} // nList

	@Override
	public void doPostLoginOperations() {

	}

	@Override
	public ISOSConnection Connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
		return null;
	}

	@Override
	public OutputStream getFileOutputStream() {
		 
		return null;
	}

}
