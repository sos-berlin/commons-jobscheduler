package com.sos.VirtualFileSystem.SFTP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
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
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsSuperClass;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.trilead.ssh2.ChannelCondition;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
import com.trilead.ssh2.channel.Channel;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsSFtp extends SOSVfsBaseClass implements ISOSVfsFileTransfer, ISOSVFSHandler, ISOSVirtualFileSystem, ISOSConnection {

	private final String					conClassName			= "SOSVfsSFtp";
	private final Logger					logger					= Logger.getLogger(SOSVfsSFtp.class);

	private Vector<String>					vecDirectoryListing		= null;

	/** key file: ~/.ssh/id_rsa or ~/.ssh/id_dsa (must be OpenSSH-Format) */
	protected String						authenticationFilename	= "";

	/** ssh connection object */
	protected Connection					sshConnection			= null;

	/** ssh session object */
	protected Session						sshSession				= null;

	/** SFTP Client **/
	protected SFTPv3Client					objFTPClient			= null;
	// private FTPClient objFTPClient = null;
	@Deprecated
	private ISOSConnectionOptions			objConnectionOptions	= null;
	private SOSConnection2OptionsAlternate	objConnection2Options	= null;

	private final String					strCurrentPath			= EMPTY_STRING;
	//	private String							strReply				= EMPTY_STRING;
	// private ProtocolCommandListener listener = null;

	private String							host					= EMPTY_STRING;
	private int								port					= 0;
	private String							gstrUser				= EMPTY_STRING;

	private SOSOptionHostName				objHost					= null;
	private SOSOptionPortNumber				objPort					= null;

	protected boolean						isConnected				= false;

	// keep Track of current directory for ftp emulation
	private String							currentDirectory		= "";

	protected String						reply					= "OK";

	private final char[]					authenticationFile		= null;

	/**
	 *
	 * \brief SOSVfsFtp
	 *
	 * \details
	 *
	 */
	public SOSVfsSFtp() {
		super();
	}

	private String HostID(final String pstrText) {
		return "(" + gstrUser + "@" + host + ":" + port + ") " + pstrText;
	}

	/*
	 * @param host the remote ftp server
	 * @param port the port number of the remote server
	 * @throws java.net.SocketException
	 * @throws java.io.IOException
	 * @throws java.net.UnknownHostException
	 * @see org.apache.commons.net.SocketClient#connect(java.lang.String, int)
	 */
	public void connect(final String phost, final int pport) {

		try {
			host = phost;
			port = pport;
			logger.debug(SOSVfs_D_0101.params(host, port));
			if (isConnected() == false) {
				sshConnection = new Connection(host, port);
				sshConnection.connect();
				isConnected = true;
				logger.debug(SOSVfs_D_0102.params(host, port));
				LogReply();
			}
			else {
				logger.warn(SOSVfs_D_0103.params(host, port));
			}
		}
		catch (Exception e) {
			String strM = HostID(SOSVfs_E_130.params("connect()"));
			// e.printStackTrace(System.err);
			logger.error(strM, e);
			// throw new RuntimeException(strM, e);
		}
	}

	@SuppressWarnings("unused")
	private void connect(final String hostname) throws SocketException, IOException, UnknownHostException {

		if (isConnected() == false)
			this.connect(hostname, 22);
	}
	
	/**
	 * returns attributes
	 *
	 * @param filename
	 * @return SftpATTRS
	 */
	public SFTPv3FileAttributes getAttributes(final String filename) {
		SFTPv3FileAttributes attributes = null;
		try {
			attributes = this.getClient().lstat(filename);
		}
		catch (Exception e) {
			attributes = null;
		}
		return attributes;
	}

	/**
	 * Creates a new subdirectory on the FTP server in the current directory .
	 * @param pstrPathName The pathname of the directory to create.
	 * @return True if successfully completed, false if not.
	 * @throws java.lang.IOException
	 */
	@Override
	public void mkdir(final String pstrPathName) {
		try {
			SOSOptionFolderName objF = new SOSOptionFolderName(pstrPathName);
			reply = "mkdir OK";
			logger.debug(HostID(SOSVfs_D_179.params("mkdir", pstrPathName)));
			for (String strSubFolder : objF.getSubFolderArray()) {
				SFTPv3FileAttributes attributes = getAttributes(strSubFolder);
				if (attributes == null) {
					Client().mkdir(strSubFolder, 484);
//					logger.debug(HostID(SOSVfs_D_135.params("[mkdir]", strSubFolder, getReplyString())));
				}
				else {
					if (attributes.isDirectory() == false) {
						RaiseException(SOSVfs_E_277.params(strSubFolder));
					}
				}
			}
//			DoCD(strCurrentDir);
			logger.debug(HostID(SOSVfs_D_181.params("mkdir", pstrPathName, getReplyString())));
		}
		catch (IOException e) {
			throw new JobSchedulerException(HostID(SOSVfs_E_130.params("makeDirectory()")), e);
		}
	}

	/**
	 * Removes a directory on the FTP server (if empty).
	 * @param pathname The pathname of the directory to remove.
	 * @throws java.lang.IOException
	 */
	@Override
	public void rmdir(final String pstrPathName) throws IOException {
		try {
			SOSOptionFolderName objF = new SOSOptionFolderName(pstrPathName);
			reply = "rmdir OK";
			for (String subfolder : objF.getSubFolderArrayReverse()) {
				String strT = subfolder + "/";
				logger.debug(HostID(SOSVfs_D_135.params("[rmdir]", strT, getReplyString())));
				Client().rmdir(strT);
			}
//			String[] strP = pstrPathName.split("/");
//			for (int i = strP.length; i > 0; i--) {
//				String strT = "";
//				for (int j = 0; j < i; j++) {
//					strT += strP[j] + "/";
//				}
//				logger.debug(HostID(SOSVfs_D_135.params("[rmdir]", strT, getReplyString())));
//				Client().rmdir(strT);
//			}
			reply = "rmdir OK";
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			throw new JobSchedulerException(HostID(SOSVfs_E_130.params("removeDirectory()")), e);
		}
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

	private Vector<String> getFilenames(final String pathname) {
		Vector<String> vecT;
		try {
			vecT = getFilenames(pathname, false);
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_134.params("getFileNames()"), e);
		}
		;
		return vecT;
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
	private Vector<String> getFilenames(final String pstrPathName, final boolean flgRecurseSubFolders) throws Exception {
		String strCurrentDirectory = null;
		Vector<String> vecDirectoryListing = null;
		if (vecDirectoryListing == null) {
			vecDirectoryListing = new Vector<String>();
			String[] fileList = null;
			strCurrentDirectory = DoPWD();
			String lstrPathName = pstrPathName.trim();

			if (lstrPathName.length() <= 0) {
				lstrPathName = ".";
			}

			if (lstrPathName.equals(".")) {
				lstrPathName = strCurrentDirectory;
			}

			if (1 == 1) {
				try {
					fileList = listNames(lstrPathName);
				}
				catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}

			if (fileList == null) {
				return vecDirectoryListing;
			}

			for (String strCurrentFile : fileList) {
				if (isNotHiddenFile(strCurrentFile)) {
					if (Client().lstat(strCurrentFile).isDirectory() == false) {
						if (lstrPathName.startsWith("/") == false) { // JIRA SOSFTP-124
							if (strCurrentFile.startsWith(strCurrentDirectory) == false) {
								strCurrentFile = addFileSeparator(strCurrentDirectory) + strCurrentFile;
							}
						}
						vecDirectoryListing.add(strCurrentFile);
					}
					else {
						if (flgRecurseSubFolders) {
							logger.debug(String.format("start scan for subdirectory '%1$s' ", strCurrentFile));
							Vector<String> vecNames = getFilenames(strCurrentFile, flgRecurseSubFolders);
							if (vecNames != null) {
								vecDirectoryListing.addAll(vecNames);
							}
						}
					}
				}
			}
		}
		logger.debug(SOSVfs_I_126.params(strCurrentDirectory));
		if (strCurrentDirectory != null) {
			DoCD(strCurrentDirectory);
			DoPWD();
		}
		return vecDirectoryListing;

	} // nList

	@Override
	public String DoPWD() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::DoPWD";
		String lstrCurrentPath = "";

		try {
			// logger.debug("Try pwd.");
			lstrCurrentPath = getCurrentPath();
		}
		catch (Exception e) {
			logger.error(HostID(SOSVfs_E_153.params("pwd.")), e);
			throw new JobSchedulerException(e);
		}

		return lstrCurrentPath;
	} // private int DoPWD

	private String getCurrentPath() {
		String lstrCurrentPath = strCurrentPath;
		// try {
		// Client().pwd();
		// lstrCurrentPath = getReplyString();
		// logger.debug(String.format("reply from pwd is : %1$s", lstrCurrentPath));
		// // Windows reply from pwd is : 257 "/kb" is current directory.
		// // Unix reply from pwd is : 257 "/home/kb"
		// int idx = lstrCurrentPath.indexOf('"'); // Unix?
		// if (idx >= 0) {
		// lstrCurrentPath = lstrCurrentPath.substring(idx + 1, lstrCurrentPath.length() - idx + 1);
		// idx = lstrCurrentPath.indexOf('"');
		// if (idx >= 0) {
		// lstrCurrentPath = lstrCurrentPath.substring(0, idx);
		// }
		// }
		// LogReply();
		// }
		// catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		return lstrCurrentPath;
	}

	// private int DoCDUP() {
	//
	// final String conMethodName = conClassName + "::DoCDUP";
	//
	// try {
	// logger.debug("Try cdup .");
	//
	// Client().cdup();
	// LogReply();
	// DoPWD();
	//
	// }
	// catch (IOException e) {
	// logger.error("Problems with CDUP", e);
	// }
	//
	// return 0;
	// } // private int DoPWD

	private int DoCD(final String strFolderName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::DoCD";

		int x = 0;
		try {
			// logger.debug("Try cd with '" + strFolderName + "'.");
			x = cd(strFolderName);
			LogReply();
		}
		catch (IOException e) {
		}

		return x;
	} // private int DoCD

	private boolean LogReply() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::LogReply";

		reply = getReplyString();
		if (reply.trim().length() > 0) {
			logger.debug(reply);
		}

		return true;
	} // private boolean LogReply

	@Override
	public boolean isNegativeCommandCompletion() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isNegativeCommandCompletion";
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

	private boolean isPositiveCommandCompletion() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isPositiveCommandCompletion";

		int x = 0;

		return x <= 300;
	} // private boolean isPositiveCommandCompletion

	public boolean isNotHiddenFile(final String strFileName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isNotHiddenFile";

		return !isHiddenFile(strFileName);

		//		if (strFileName.endsWith("/..") == false && strFileName.endsWith("/.") == false) {
		//			return true; // not a hidden file
		//		}
		//
		//		if (strFileName.equalsIgnoreCase("..") == false && strFileName.equalsIgnoreCase(".") == false) {
		//			return true; // not a hidden file
		//		}
		//
		//		return false; // it is a hidden-file
	} // private boolean isNotHiddenFile

	private boolean isHiddenFile(final String pstrFolderName) {
		boolean flgR = false;

		if (pstrFolderName.endsWith("..") == true || pstrFolderName.endsWith(".") == true) {
			flgR = true; // a hidden file
		}

		return flgR;
	}

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
	public Vector<String> nList(final String pathname, final boolean flgRecurseSubFolder) {

		try {
			return getFilenames(pathname, flgRecurseSubFolder);
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_128.params("getfilenames()", "nList"), e);
		}
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
	public Vector<String> nList() throws Exception {
		return getFilenames();

	} // nList

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
			RaiseException(e, SOSVfs_E_128.params("listfiles()", "dir"));
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
		try {
			return Client().stat(filename).isDirectory();
		}
		catch (Exception e) {
		}
		return false;
	}

	@Override
	public String[] listNames(String pathname) throws IOException {
		pathname = resolvePathname(pathname);
		try {
			if (pathname.length() == 0)
				pathname = ".";

			if (!fileExists(pathname))
				return null;

			if (!isDirectory(pathname)) {
				File remoteFile = new File(pathname);
				reply = "ls OK";
				return new String[] { remoteFile.getName() };
			}

			@SuppressWarnings("unchecked")
			Vector<SFTPv3DirectoryEntry> files = Client().ls(pathname);
			String[] rvFiles = new String[files.size()];

			for (int i = 0; i < files.size(); i++) {
				SFTPv3DirectoryEntry entry = files.get(i);
				rvFiles[i] = addFileSeparator(pathname) + entry.filename;
			}
			reply = "ls OK";
			return rvFiles;
		}
		catch (Exception e) {
			reply = e.toString();
			return null;
		}
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
	 * return the size of remote-file on the remote machine on success, otherwise -1
	 * @param remoteFile the file on remote machine
	 * @return the size of remote-file on remote machine
	 */
	public long size(String remoteFile) throws Exception {
		remoteFile = resolvePathname(remoteFile);
		long lngFileSize = -1;
		try {
			SFTPv3FileAttributes objAttr = Client().stat(remoteFile);
			if (objAttr != null) {
				lngFileSize = objAttr.size.longValue();
			}
			return lngFileSize;
		}
		catch (com.trilead.ssh2.SFTPException e) {
			//			e.printStackTrace(System.err);
			return lngFileSize;
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw new Exception(SOSVfs_E_161.params("checking size", e));
		}
	}

	/**
	 * trim the response code at the beginning
	 * @param response
	 * @return the response string without response code
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private String trimResponseCode(final String response) throws Exception {
		if (response.length() < 5)
			return response;
		return response.substring(4).trim();
	}

	/**
	 * Retrieves a named file from the ftp server.
	 *
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @exception Exception
	 * @see #getFile( String, String )
	 */
	// public void get(String remoteFile, String localFile) {
	// FileOutputStream out = null;
	// boolean rc = false;
	// try {
	// out = new FileOutputStream(localFile);
	// rc = Client().retrieveFile(remoteFile, out);
	// if (rc == false) {
	// throw new JobSchedulerException("retrieveFile returns a negative return-code");
	// }
	// }
	// catch (IOException e) {
	// throw new JobSchedulerException("get returns an exception", e);
	// }
	// finally {
	// closeObject(out);
	// }
	// } // get

	@Override
	public long getFile(final String remoteFile, final String localFile, final boolean append) throws Exception {
		String sourceLocation = resolvePathname(remoteFile);
		SFTPv3FileHandle sftpFileHandle = null;
		FileOutputStream fos = null;
		File transferFile = null;
		long remoteFileSize = -1;

		try {
			transferFile = new File(localFile);
			remoteFileSize = size(remoteFile);
			sftpFileHandle = objFTPClient.openFileRO(sourceLocation);

			fos = null;
			long offset = 0;
			try {
				fos = new FileOutputStream(transferFile, append);
				byte[] buffer = new byte[32768];
				while (true) {
					int len = objFTPClient.read(sftpFileHandle, offset, buffer, 0, buffer.length);
					if (len <= 0)
						break;
					fos.write(buffer, 0, len);
					offset += len;
				}
				fos.flush();
				fos.close();
				fos = null;

			}
			catch (Exception e) {
				throw new Exception(SOSVfs_E_161.params("get file [" + transferFile.getAbsolutePath() + "]", e.getMessage()));
			}
			finally {
				if (fos != null)
					try {
						fos.close();
						fos = null;
					}
					catch (Exception ex) {
					} // gracefully ignore this error
			}

			objFTPClient.closeFile(sftpFileHandle);
			sftpFileHandle = null;

			if (remoteFileSize > 0 && remoteFileSize != transferFile.length())
				throw new Exception(SOSVfs_E_162.params(remoteFileSize, transferFile.length(), offset));

			return transferFile.length();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				objFTPClient.closeFile(sftpFileHandle);
			}
			catch (Exception e) {
			}
		}
	}

	public long readFile(final String pstrFilename) throws Exception {
		SFTPv3FileHandle sftpFileHandle = null;

		try {
			String sourceLocation = resolvePathname(pstrFilename);
			long remoteFileSize = size(sourceLocation);
			sftpFileHandle = openFileRO(pstrFilename);

			long offset = 0;
			try {
				byte[] buffer = new byte[32768];
				while (true) {
					int len = objFTPClient.read(sftpFileHandle, offset, buffer, 0, buffer.length);
					if (len <= 0)
						break;
					offset += len;
				}
			}
			catch (Exception e) {
				throw new JobSchedulerException(SOSVfs_E_161.params("reading file [" + pstrFilename + "]", e.getMessage()));
			}

			// if (remoteFileSize > 0 && remoteFileSize != transferFile.length())
			// throw new Exception("remote file size [" + remoteFileSize + "] and local file size [" + transferFile.length()
			// + "] are different. Number of bytes written to local file: " + offset);

			return remoteFileSize;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				objFTPClient.closeFile(sftpFileHandle);
				sftpFileHandle = null;
			}
			catch (Exception e) {
				throw e;
			}
		}
	}

	// private SFTPv3FileHandle getHandle (final String pstrFileName) {
	// @SuppressWarnings("unused")
	// final String conMethodName = conClassName + "::getHandle";
	//
	// String sourceLocation = resolvePathname(pstrFileName);
	//
	// SFTPv3FileHandle sftpFileHandle = null;
	// try {
	// sftpFileHandle = objFTPClient.openFileRO(sourceLocation);
	// }
	// catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// return sftpFileHandle;
	// }
	public SFTPv3FileHandle openFileRO(final String pstrFilename) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::openFileRO";

		String sourceLocation = resolvePathname(pstrFilename);
		SFTPv3FileHandle sftpFileHandle = null;

		try {
			sftpFileHandle = objFTPClient.openFileRO(sourceLocation);
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_161.params("opening file [" + sourceLocation + "]", e.getMessage()));
		}
		finally {
		}

		return sftpFileHandle;
	}

	public SFTPv3FileHandle openFileWR(final String pstrFilename) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::openFileWR";

		SFTPv3FileHandle sftpFileHandle = null;
		String sourceLocation = resolvePathname(pstrFilename);

		try {
			//			sftpFileHandle = objFTPClient.createFile(pstrFilename);
			sftpFileHandle = objFTPClient.createFileTruncate(sourceLocation);

		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_161.params("opening file [" + sourceLocation + "]", e.getMessage()));
		}
		finally {
		}

		return sftpFileHandle;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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

		try {
			long lngBytesWritten = this.putFile(localFile, remoteFile);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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
	public long putFile(final String localFile, String remoteFile) throws Exception {
		long offset = 0;
		try {
			remoteFile = resolvePathname(remoteFile);
			SFTPv3FileHandle fileHandle = this.Client().createFileTruncate(remoteFile);
			File localF = new File(localFile);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(localF);
				byte[] buffer = new byte[32768];

				while (true) {
					int len = fis.read(buffer, 0, buffer.length);
					if (len <= 0)
						break;
					this.Client().write(fileHandle, offset, buffer, 0, len);
					offset += len;
				}

				fis.close();
				fis = null;

			}
			catch (Exception e) {
				RaiseException(SOSVfs_E_161.params("writing file [" + localFile + "]", e.getMessage()));
			}
			finally {
				if (fis != null)
					try {
						fis.close();
						fis = null;
					}
					catch (Exception ex) {
					} // gracefully ignore this error
			}
			objFTPClient.closeFile(fileHandle);
			logger.debug(SOSVfs_D_146.params(localFile, remoteFile));
			fileHandle = null;
			reply = "put OK";
			return offset;
		}
		catch (Exception e) {
			reply = e.toString();
			RaiseException(e, SOSVfs_E_130.params("putFile()"));
		}
		return offset;
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
		if (out == null)
			RaiseException(SOSVfs_E_147.get());

		FileInputStream in = null;
		long lngTotalBytesWritten = 0;
		try {
			// TODO Buffersize must be an Option
			byte[] buffer = new byte[4096];
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
			return lngTotalBytesWritten;
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_130.params("putFile()"));
		}
		finally {
			closeInput(in);
			closeObject(out);
		}
		return lngTotalBytesWritten;
	} // putFile

	private void RaiseException(final Exception e, final String pstrM) {
		logger.error(pstrM);
		throw new JobSchedulerException(pstrM, e);
	}

	private void RaiseException(final String pstrM) {
		logger.error(pstrM);
		throw new JobSchedulerException(pstrM);
	}

	public SFTPv3Client getClient() {
		return Client();
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
	@Override
	public long appendFile(final String localFile, final String remoteFile) {
		notImplemented();
		// long i;
		// try {
		// i = putFile(localFile, Client().appendFileStream(remoteFile));
		// logger.info("bytes appended : " + i);
		//
		// }
		// catch (IOException e) {
		// throw new RuntimeException("appendFileStream returns an exception", e);
		// }

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
	public int cd(final String directory) throws IOException {
		changeWorkingDirectory(directory);
		return 1;
	}

	private boolean fileExists(final String filename) {

		try {
			SFTPv3FileAttributes attributes = Client().stat(filename);

			if (attributes != null) {
				return attributes.isRegularFile() || attributes.isDirectory();
			}
			else {
				return false;
			}

		}
		catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean changeWorkingDirectory(String pathname) throws IOException {
		pathname = resolvePathname(pathname);
		// cut trailing "/" if it's not the only character
		if (pathname.length() > 1 && pathname.endsWith("/"))
			pathname = pathname.substring(0, pathname.length() - 1);
		if (!fileExists(pathname)) {
			reply = "\"" + pathname + "\" doesn't exist.";
			return false;
		}
		if (!isDirectory(pathname)) {
			reply = "\"" + pathname + "\" is not a directory.";
			return false;
		}
		if (pathname.startsWith("/") || currentDirectory.length() == 0) {
			currentDirectory = pathname;
			reply = "cd OK";
			return true;
		}
		currentDirectory = pathname;
		reply = "cd OK";
		return true;
	}

	private String resolvePathname(final String pathname) {
		String strR = pathname;
		if (!pathname.startsWith("./") && !pathname.startsWith("/") && currentDirectory.length() > 0) {
			String slash = "";
			if (!currentDirectory.endsWith("/"))
				slash = "/";
			strR = currentDirectory + slash + pathname;
		}
		while (pathname.contains("\\")) {
			strR = pathname.replace('\\', '/');
		}
		if (strR.endsWith("/")) {
			strR = strR.substring(0, strR.length() - 1);
		}
		return strR;
	}

	/**
	 * Deletes a file on the FTP server.
	 * @param The pathname of the file to be deleted.
	 * @return True if successfully completed, false if not.
	 * @throws IOException If an I/O error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	@Override
	public void delete(final String pathname) throws IOException {
		Client().rm(pathname);
		reply = "rm OK";
		logger.info(SOSVfs_I_131.params(pathname, getReplyString()));
	}

	@Override
	public void login(final String strUserName, final String strPassword) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::login";
		boolean isAuthenticated = false;

		try {
			gstrUser = strUserName;

			logger.debug(SOSVfs_D_132.params(strUserName));
			isAuthenticated = sshConnection.authenticateWithPassword(strUserName, strPassword);

			if (isAuthenticated == false) {
				RaiseException(SOSVfs_E_134.params("authentication"));
			}

			reply = "OK";
			logger.debug(SOSVfs_D_133.params(strUserName));
			LogReply();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		LogReply();

	} // private boolean login

	@Override
	public void disconnect() {
		reply = "disconnect OK";
		if (objFTPClient != null)
			try {
				objFTPClient.close();
				objFTPClient = null;
			}
			catch (Exception ex) {
				reply = "disconnect: " + ex;
			} // gracefully ignore this error
		if (sshConnection != null)
			try {
				sshConnection.close();
				sshConnection = null;
			}
			catch (Exception ex) {
				reply = "disconnect: " + ex;
			} // gracefully ignore this error
		isConnected = false;

		logger.info(reply);
	}

	@Override
	public String getReplyString() {
		String strT = reply;
		return strT;
	}

	@Override
	public boolean isConnected() {
		return isConnected;
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
				logger.debug(SOSVfs_D_138.params(objHost.Value(), getReplyString()));
			}
			else {
				logger.info(SOSVfs_I_139.get());
			}
		}
		catch (Exception e) { // no error-handling needed, due to end-of session
			logger.warn(SOSVfs_W_140.get() + e.getMessage());
		}
	}

	@Override
	public void rename(String from, String to) {
		from = resolvePathname(from);
		to = resolvePathname(to);
		try {
			Client().mv(from, to);
		}
		catch (Exception e) {
			reply = e.toString();
			RaiseException(e, SOSVfs_E_134.params("rename()"));
		}
		reply = "mv OK";
		logger.info(SOSVfs_I_150.params(from, to));
	}

	@Override
	public ISOSVFSHandler getHandler() {
		return this;
	}

	@Override
	public void ExecuteCommand(final String strCmd) throws Exception {

		final String strEndOfLine = System.getProperty("line.separator");

		if (sshSession == null) {
			sshSession = sshConnection.openSession();
		}
		sshSession.execCommand(strCmd);
		//		int intExitStatus = this.sshSession.getExitStatus();
		//		logger.info("ExitStatus = " + intExitStatus);
		logger.debug(SOSVfs_D_163.params("stdout", strCmd));
		InputStream ipsStdOut = new StreamGobbler(sshSession.getStdout());
		InputStream ipsStdErr = new StreamGobbler(sshSession.getStderr());
		BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(ipsStdOut));
		StringBuffer strbStdoutOutput = new StringBuffer();
		while (true) {
			String line = stdoutReader.readLine();
			if (line == null)
				break;
			strbStdoutOutput.append(line + strEndOfLine);

		}

		logger.debug(strbStdoutOutput);

		logger.debug(SOSVfs_D_163.params("stderr", strCmd));
		BufferedReader stderrReader = new BufferedReader(new InputStreamReader(ipsStdErr));
		StringBuffer strbStderrOutput = new StringBuffer();
		while (true) {
			String line = stderrReader.readLine();
			if (line == null)
				break;
			strbStderrOutput.append(line + strEndOfLine);
		}
		logger.debug(strbStderrOutput);
		@SuppressWarnings("unused")
		int res = sshSession.waitForCondition(ChannelCondition.EOF, 30 * 1000);

		Integer intExitCode = sshSession.getExitStatus();
		if (intExitCode != null) {
			//			objJSJobUtilities.setJSParam(conExit_code, intExitCode.toString());
			if (!intExitCode.equals(new Integer(0))) {
				//				if (objOptions.ignore_error.isTrue() || objOptions.ignore_exit_code.Values().contains(intExitCode)) {
				//					logger.info("SOS-SSH-E-140: exit code is ignored due to option-settings: " + intExitCode);
				//				}
				//				else {
				throw new JobSchedulerException(SOSVfs_E_164.params(intExitCode));
				//				}
			}
		}
		else {
			//			objJSJobUtilities.setJSParam(conExit_code, "");
		}

		sshSession.close();
		sshSession = null;
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

	@SuppressWarnings("unused")
	private ISOSAuthenticationOptions	objAO	= null;

	@Override
	public ISOSConnection Authenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
		objAO = pobjAO;
		// TODO allow alternate Authentications
		doAuthenticate(pobjAO);
		return this;
	}

	private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions pobjAO) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doAuthenticate";

		boolean isAuthenticated = false;

		try {
			gstrUser = pobjAO.getUser().Value();
			String strPW = pobjAO.getPassword().Value();
			String strUserName = gstrUser;
			logger.debug(SOSVfs_D_132.params(strUserName));

			String strAuthMethod = pobjAO.getAuth_method().Value();
			if (pobjAO.getAuth_method().isPublicKey()) {
				logger.debug(SOSVfs_D_165.params("userid", "publickey"));
				SOSOptionInFileName objAF = pobjAO.getAuth_file();
				objAF.CheckMandatory(true);
				if (objAF.IsNotEmpty()) {
					char[] chrAFContent = objAF.JSFile().File2String().toCharArray();
					isAuthenticated = sshConnection.authenticateWithPublicKey(strUserName, chrAFContent, strPW);
				}
			}
			else {
				if (pobjAO.getAuth_method().isPassword()) {
					logger.debug(SOSVfs_D_165.params("userid", "password"));
					isAuthenticated = sshConnection.authenticateWithPassword(gstrUser, strPW);
				}
				else {
					throw new Exception(SOSVfs_E_166.params(pobjAO.getAuth_method().Value()));
				}
			}
			if (!isAuthenticated) {
				throw new JobSchedulerException(SOSVfs_E_167.params(pobjAO.getAuth_method().Value(), pobjAO.getAuth_file().Value()));
			}
			reply = "OK";
			logger.debug(SOSVfs_D_133.params(strUserName));
			LogReply();
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_168.get(), e);
		}
		finally {

		}
		return this;
	}

	@Override
	public void CloseConnection() throws Exception {
		if (isConnected()) {
			disconnect();
			logger.debug(SOSVfs_D_125.params(objConnectionOptions.getHost().Value()));
			LogReply();
		}
	}

	private SFTPv3Client Client() {
		if (objFTPClient == null) {
			try {
				objFTPClient = new SFTPv3Client(sshConnection);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/**
			 * This listener is to write all commands and response from commands to system.out
			 *
			 */
			// this.Connect();
		}
		return objFTPClient;
	}

	@Override
	public ISOSConnection Connect() {
		logger.debug(SOSVfs_D_169.get());
		try {
			this.connect(objConnectionOptions.getHost().Value(), objConnectionOptions.getPort().value());
			logger.info(SOSVfs_D_0102.params(objConnectionOptions.getHost().Value(), objConnectionOptions.getPort().value()));
		}
		catch (RuntimeException e) {
			if (objConnectionOptions.getalternative_host().IsNotEmpty() && objConnectionOptions.getalternative_port().IsNotEmpty()) {
				logger.info(SOSVfs_E_204.get());
				this.connect(objConnectionOptions.getalternative_host().Value(), //
						objConnectionOptions.getalternative_port().value());
				logger.info(SOSVfs_D_0102.params(objConnectionOptions.getalternative_host().Value(), //
						objConnectionOptions.getalternative_port().value()));
			}
			else {
				throw e;
			}
		}
		isConnected = true;
		reply = "OK";

		return this;
	}

	@Override
	public ISOSConnection Connect(final SOSConnection2OptionsAlternate pobjConnectionOptions) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Connect";

		objConnection2Options = pobjConnectionOptions;
		try {

			objHost = objConnection2Options.getHost();
			objHost.CheckMandatory();
			objPort = objConnection2Options.getport();
			objPort.CheckMandatory();
			host = objHost.Value();
			port = objPort.value();
			this.connect(objHost.Value(), objPort.value());
			if (isConnected == false) {
				SOSConnection2OptionsSuperClass objAlternate = objConnection2Options.Alternatives();
				objHost = objAlternate.host;
				objPort = objAlternate.port;
				host = objHost.Value();
				port = objPort.value();
				logger.info(SOSVfs_I_170.params(host));
				this.connect(objHost.Value(), objPort.value());
				if (isConnected == false) {
					objHost = null;
					objPort = null;
					host = "";
					port = -1;

					RaiseException(SOSVfs_E_204.get());
				}
			}
			// TODO find the "Microsoft FTP Server" String from the reply and set the HostType accordingly
			// TODO respect Proxy-Server. implement handling of
		}
		catch (Exception e) {
			logger.error("exception occured", e);
			throw new JobSchedulerException("exception occured:", e);
		}

		return this;
	}

	@Override
	public void Options(final SOSFTPOptions pobjOptions) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options";

		super.Options(pobjOptions);

		if (pobjOptions.BufferSize.isDirty()) {
			int intBufferSize = pobjOptions.BufferSize.value();
			Channel.CHANNEL_BUFFER_SIZE = intBufferSize;
			SFTPv3Client.MAX_RECEIVE_BUFFER_SIZE = intBufferSize;
		}
	} // private void Options

	@Override
	public ISOSConnection Connect(final ISOSConnectionOptions pobjConnectionOptions) throws Exception {
		objConnectionOptions = pobjConnectionOptions;
		try {
			String host = objConnectionOptions.getHost().Value();
			int port = objConnectionOptions.getPort().value();
			// TODO try alternate host, if this connection is not possible
			this.connect(host, port);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	@Override
	public ISOSConnection Connect(final String pstrHostName, final int pintPortNumber) throws Exception {
		this.connect(pstrHostName, pintPortNumber);
		if (objConnectionOptions != null) {
			objConnectionOptions.getHost().Value(pstrHostName);
			objConnectionOptions.getPort().value(pintPortNumber);
		}
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
			logger.debug(SOSVfs_D_122.params("ASCII"));
			logger.debug(SOSVfs_D_123.params("ascii", getReplyString()));
		}
		else {
			this.binary();
			logger.debug(SOSVfs_D_122.params("binary"));
			logger.debug(SOSVfs_D_123.params("binary", getReplyString()));
		}

		return null;
	}

	public SOSFileListEntry getNewVirtualFile(final String pstrFileName) {
		SOSFileListEntry objF = new SOSFileListEntry(pstrFileName);
		objF.VfsHandler(this);
		return objF;
	}

	@Override
	public ISOSVirtualFolder mkdir(final SOSFolderName pobjFolderName) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getStdOut() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remoteIsWindowsShell() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
		// TODO Auto-generated method stub

	}

	@Override
	public ISOSVirtualFile getFileHandle(String pstrFilename) {
		final String conMethodName = conClassName + "::getFileHandle";

		pstrFilename = pstrFilename.replaceAll("\\\\", "/");
		ISOSVirtualFile objFtpFile = new SOSVfsSFtpFile(pstrFilename);
		logger.debug(SOSVfs_D_152.params(pstrFilename, conMethodName));
		objFtpFile.setHandler(this);
		return objFtpFile;
	}

	@Override
	public String[] getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
		vecDirectoryListing = null; // start with a fresh copy of this list (due to polling ...)
		if (vecDirectoryListing == null) {
			String strT = folder.replaceAll("\\\\", "/");
			vecDirectoryListing = nList(folder, withSubFolder);
		}

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

	@Override
	public String[] getFolderlist(final String folder, final String regexp, final int flag, final boolean withSubFolder) {
		vecDirectoryListing = null; // start with a fresh copy of this list (due to polling ...)
		if (vecDirectoryListing == null) {
			String strT = folder.replaceAll("\\\\", "/");
			vecDirectoryListing = nList(folder, withSubFolder);
		}

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

	@Override
	public OutputStream getAppendFileStream(final String strFileName) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getAppendFileStream";

		OutputStream objO = null;
		return objO;
	}

	@Override
	public long getFileSize(final String strFileName) {
		final String conMethodName = conClassName + "::getFileSize";

		long lngFileSize = 0;
		try {
			String lstrFileName = strFileName.replaceAll("\\\\", "/");
			lngFileSize = this.size(lstrFileName);
		}
		catch (Exception e) {
			e.printStackTrace();
			RaiseException(e, SOSVfs_E_153.params(conMethodName));
		}
		// TODO Auto-generated method stub
		return lngFileSize;
	}

	@Override
	public InputStream getInputStream(final String strFileName) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getInputStream";

		InputStream objI = null;
		// SFTPv3FileHandle objI = getClient().openFileRO(strFileName);

		return objI;
	}

	@Override
	public String getModificationTime(final String strFileName) {
		String strT = "";
		// TODO transform mtime to real date
		// try {
		// strT = Client().stat(strFileName).mtime;
		// }
		// catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return strT;
	}

	@Override
	public OutputStream getOutputStream(final String strFileName) {

		OutputStream objO = null;
		return objO;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeInput() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeOutput() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public int read(final byte[] bteBuffer) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::read";

		int intL = -1;
		try {
			// int intMaxBuffLen = bteBuffer.length;
			int intMaxBuffLen = bteBuffer.length;
			// if (intMaxBuffLen > 32000) {
			// intMaxBuffLen = 32000;
			// }
			intL = objFTPClient.read(objInputFile, lngReadOffset, bteBuffer, 0, intMaxBuffLen);
			lngReadOffset += intL;
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return intL;
	}

	@Override
	public int read(final byte[] bteBuffer, final int intOffset, final int intLength) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::read";
		int intL = -1;
		try {
			intL = objFTPClient.read(objInputFile, lngReadOffset, bteBuffer, 0, intLength);
			lngReadOffset += intL;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return intL;
	}

	@Override
	public void write(final byte[] bteBuffer, final int intOffset, final int intLength) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::write";
		try {
			objFTPClient.write(objOutputFile, lngWriteOffset, bteBuffer, 0, intLength);
			lngWriteOffset += intLength;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void write(final byte[] bteBuffer) {
		// TODO Auto-generated method stub

	}

	private SFTPv3FileHandle	objInputFile	= null;
	private long				lngReadOffset	= 0;
	private SFTPv3FileHandle	objOutputFile	= null;
	private long				lngWriteOffset	= 0;

	@Override
	public void openInputFile(final String pstrFileName) {
		try {
			if (objInputFile == null || objInputFile.isClosed() == true) {
				objInputFile = openFileRO(pstrFileName);
				lngReadOffset = 0;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void openOutputFile(final String pstrFileName) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::openOutputFile";

		try {
			objOutputFile = openFileWR(pstrFileName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Vector<ISOSVirtualFile> getFiles(final String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector<ISOSVirtualFile> getFiles() {
		// TODO Auto-generated method stub
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

		// TODO Buffersize Option
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
			throw new JobSchedulerException(SOSVfs_E_130.params("putFile()"), e);
		}
		finally {
		}

	}

	@Override
	public void ControlEncoding(final String pstrControlEncoding) {

	}

	public SFTPv3FileHandle getOutputFileHandle(final String pstrFileName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getOutputFileHandle";

		openOutputFile(pstrFileName);

		return objOutputFile;
	} // private SFTPv3FileHandle getOutputFileHandle

	public SFTPv3FileHandle getInputFileHandle(final String pstrFileName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getOutputFileHandle";

		openInputFile(pstrFileName);

		return objInputFile;
	} // private SFTPv3FileHandle getOutputFileHandle

	@Override
	public void doPostLoginOperations() {

	}

	@Override
	public ISOSConnection Connect(final ISOSDataProviderOptions pobjConnectionOptions) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getFileOutputStream() {
//		ISOSVirtualFile objVF = this.getFileHandle(strName);
//		OutputStream objOS = objVF.getFileOutputStream();

		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}
}