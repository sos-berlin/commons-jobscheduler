package sos.net.sosftp2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * <p>Title: FTP-Client</p>
 * <p>Description: a wrapper around the Jakarta Commons FTP</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: SOS GmbH</p>
 * @author <a href="mailto:ghassan.beydoun@sos-berlin.com">Ghassan Beydoun</a>
 * @version $Id: SOSFTP.java 6601 2010-06-14 08:43:49Z kb $
 * @resource commons-net-1.3.0.jar, 
 *           oro.jar für den Aufruf FTPFile (=download=http://jakarta.apache.org/site/downloads/downloads_oro.cgi)
 */

public class SOSFTP  extends FTPClient implements SOSFileTransfer {

	private final String	conClassName	= "SOSFTP";
	private Logger			logger			= Logger.getLogger(SOSFTP.class);

	public SOSFTP() {
	}

	/**
	 * SOSFTP constructor: there is no need to call the connect-Method
	 * @param host the remote ftp server
	 * @throws java.net.SocketException
	 * @throws java.io.IOException
	 * @throws java.net.UnknownHostException
	 */
	public SOSFTP(String host) throws SocketException, IOException, UnknownHostException {
		connect(host);
	}

	/**
	 * SOSFTP constructor: there is no need to call the connect-Method
	 * @param host the remote ftp server
	 * @param port the port number of the remote server
	 * @throws java.net.SocketException
	 * @throws java.io.IOException
	 * @throws java.net.UnknownHostException
	 */
	public SOSFTP(String host, int port) throws SocketException, IOException, UnknownHostException {
		connect(host, port);
	}

	public SOSFTP(String ftpHost, int ftpPort, String proxyHost, int proxyPort) throws SocketException, IOException, UnknownHostException {
		// this.setProxyHost(proxyHost);
		// this.setProxyPort(proxyPort);
		this.connect(ftpHost, ftpPort);
	}

	/*
	 * @param host the remote ftp server
	 * @param port the port number of the remote server
	 * @throws java.net.SocketException
	 * @throws java.io.IOException
	 * @throws java.net.UnknownHostException
	 * @see org.apache.commons.net.SocketClient#connect(java.lang.String, int)
	 */
	public void connect(String host, int port) throws SocketException, IOException, UnknownHostException {

		if (!isConnected()) {
			super.connect(host, port);
			LogReply();
		}
	}

	/*
	 * @param host the remote ftp server
	 * @param port the port number of the remote server
	 * @throws java.net.SocketException
	 * @throws java.io.IOException
	 * @throws java.net.UnknownHostException
	 * @see org.apache.commons.net.SocketClient#connect(java.lang.String)
	 */
	public void connect(String hostname) throws SocketException, IOException, UnknownHostException {

		if (!isConnected())
			super.connect(hostname);
		LogReply();

	}

	/**
	 * Creates a new subdirectory on the FTP server in the current directory .
	 * @param pathname The pathname of the directory to create.
	 * @return True if successfully completed, false if not.
	 * @throws java.lang.IOException
	 */
	public boolean mkdir(String pathname) throws IOException {
		return makeDirectory(pathname);
	}

	/**
	 * Removes a directory on the FTP server (if empty).
	 * @param pathname The pathname of the directory to remove.
	 * @return True if successfully completed, false if not.
	 * @throws java.lang.IOException
	 */
	public boolean rmdir(String pathname) throws IOException {
		return removeDirectory(pathname);
	}

	/**
	 * turn passive transfer mode on.
	 *
	 * @return The reply code received from the server.
	 */
	public int passive() throws IOException {
		return pasv();
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
	public Vector<String> nList(String pathname) throws Exception {
		return getFilenames(pathname);
	} // nList

	private Vector<String> getFilenames(String pathname) throws IOException {
		return getFilenames(pathname, false);
	}

	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine (without subdirectory)
	 * 
	 * @param pstrPathName on remote machine
	 * @return a listing of the contents of a directory on the remote machine
	 * @throws IOException 
	 *
	 * @exception Exception
	 * @see #dir()
	 */
	private Vector<String> getFilenames(String pstrPathName, boolean flgRecurseSubFolders) throws IOException {
		Vector<String> vecListFileItems = new Vector<String>();
//		setListHiddenFiles(true);
		String[] fileList = null;
		String strCurrentDirectory = DoPWD();
		String lstrPathName = pstrPathName.trim();
		
		if (lstrPathName.length() <= 0) {
			lstrPathName = ".";
		}

		if (lstrPathName.equals(".")) {
			lstrPathName = strCurrentDirectory;
		}
		
		if (1 == 1) {
			fileList = listNames(lstrPathName);
		}
		else {
			FTPFile[] objFtpFiles = listFiles(lstrPathName);
			if (objFtpFiles != null) {
				int i = 0;
				for (FTPFile ftpFile : objFtpFiles) {
					fileList[i++] = ftpFile.getName();
				}
			}
		}
		if (fileList == null) {
			return vecListFileItems;
		}

		for (String strCurrentFile : fileList) {
			if (isNotHiddenFile(strCurrentFile)) {
				DoCD(strCurrentFile); // is this file-entry a subfolder?
				if (isNegativeCommandCompletion()) {
					if (strCurrentFile.startsWith(strCurrentDirectory) == false)
						strCurrentFile = strCurrentDirectory + "/" + strCurrentFile;

					vecListFileItems.add(strCurrentFile);
				}
				else { // yes, it's a subfolder. undo the cd now
//					String[] strSubFolders = strCurrentFile.split("/"); // get number of subfolders in path
//					for (int s = 0; s < strSubFolders.length; s++) {
//						DoCDUP();
//					}
					DoCDUP();
					if (flgRecurseSubFolders) {
						Vector<String> vecNames = getFilenames(strCurrentFile);
						if (vecNames != null) {
							vecListFileItems.addAll(vecNames);
						}
					}
				}
			}
		}

		System.out.println("strCurrentDirectory = " + strCurrentDirectory);
		DoCD(strCurrentDirectory);
		DoPWD();
		return vecListFileItems;

	} // nList

	public String DoPWD() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::DoPWD";
		String strCurrentPath = "";
		
		try {
			logger.debug("Try pwd.");

			pwd();
			strCurrentPath = getReplyString();
			int idx = strCurrentPath.indexOf('"');
			if (idx >= 0) {
				strCurrentPath = strCurrentPath.substring(idx+1, strCurrentPath.length()-idx+1);
			}
			LogReply();
		}
		catch (IOException e) {
			logger.error("Problems with pwd", e);
		}

		return strCurrentPath;
	} // private int DoPWD

	private int DoCDUP() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::DoPWD";

		try {
			logger.debug("Try cdup .");

			cdup();
			LogReply();
			DoPWD();

		}
		catch (IOException e) {
			logger.error("Problems with CDUP", e);
		}

		return 0;
	} // private int DoPWD

	private int DoCD(final String strFolderName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::DoCD";

		int x = 0;
		try {
			logger.debug("Try cd with '" + strFolderName + "'.");
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

		String strReply = getReplyString();
		logger.debug(strReply);

		return true;
	} // private boolean LogReply

	private boolean isNegativeCommandCompletion() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isNegativeCommandCompletion";

		int x = getReplyCode();

		return (x > 300);
	} // private boolean isNegativeCommandCompletion

	private boolean isPositiveCommandCompletion() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isPositiveCommandCompletion";

		int x = getReplyCode();

		return (x <= 300);
	} // private boolean isPositiveCommandCompletion

	public boolean isNotHiddenFile(final String strFileName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isNotHiddenFile";

		if (strFileName != ".." && strFileName != ".") {
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
	 * @exception Exception
	 * @see #dir()
	 */
	@Override
	public Vector<String> nList(String pathname, final boolean flgRecurseSubFolder) throws Exception {

		return getFilenames(pathname, flgRecurseSubFolder);
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

	private Vector<String> getFilenames(boolean flgRecurseSubFolders) throws Exception {
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
	public Vector<String> nList(boolean recursive) throws Exception {
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

	public Vector<String> dir(String pathname) throws Exception {
		return getFilenames(pathname);
	}

	/**
	 * return a listing of a directory in long format on
	 * the remote machine
	 * @param pathname on remote machine
	 * @return a listing of the contents of a directory on the remote machine
	 * @exception Exception
	 * @see #nList()
	 * @see #nList( String )
	 * @see #dir()
	 * @deprecated
	 */
	public Vector<String> dir(String pathname, int flag) throws Exception {

		Vector<String> fileList = new Vector<String>();
		FTPFile[] listFiles = listFiles(pathname);
		for (int i = 0; i < listFiles.length; i++) {
			if (flag > 0 && listFiles[i].isDirectory()) {
				fileList.addAll(this.dir(pathname + "/" + listFiles[i].toString(), ((flag >= 1024) ? flag : flag + 1024)));
			}
			else {
				if (flag >= 1024) {
					fileList.add(pathname + "/" + listFiles[i].toString());
				}
				else {
					fileList.add(listFiles[i].toString());
				}
			}
		}
		return fileList;
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
	public Vector<String> dir() throws Exception {
		return dir(".");
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
	@Override
	public long size(String remoteFile) throws Exception {

		this.sendCommand("SIZE " + remoteFile);
        // Version > 2.2 of commons-net mapped the constant 213 (FTPReply.CODE_213) to FTPReply.FILE_STATUS
        // see http://commons.apache.org/proper/commons-net/changes-report.html
        // see http://api.j2men.com/commons-net-2.2/
        // see http://commons.apache.org/proper/commons-net/apidocs/constant-values.html#org.apache.commons.net.ftp.FTPReply
        // if (this.getReplyCode() == FTPReply.CODE_213)
        if (this.getReplyCode() == FTPReply.FILE_STATUS)
			return Long.parseLong(trimResponseCode(this.getReplyString()));
		else
			return -1L;
	}

	/**
	 * trim the response code at the beginning
	 * @param response
	 * @return the response string without response code
	 * @throws Exception
	 */
	private String trimResponseCode(String response) throws Exception {
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
	public boolean get(String remoteFile, String localFile) throws Exception {
		FileOutputStream out = null;
		boolean rc = false;
		try {
			out = new FileOutputStream(localFile);
			rc = retrieveFile(remoteFile, out);
			return rc;
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			try {
				if (out != null)
					out.close();
			}
			catch (Exception e) {
			}
		}
	} // get

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
	public long getFile(String remoteFile, String localFile) throws Exception {
		final boolean flgAppendLocalFile = false;
		return getFile(remoteFile, localFile, flgAppendLocalFile);
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
	public long getFile(String remoteFile, String localFile, boolean append) throws Exception {
		InputStream in = null;
		OutputStream out = null;
		long totalBytes = 0;
		try {
			in = retrieveFileStream(remoteFile);
			// boolean test = retrieveFile(remoteFile, new FileOutputStream( new File(localFile+"test"), append ));

			if (in == null) {
				throw new JobSchedulerException("Could not open stream for " + remoteFile + ". Perhaps the file does not exist. Reply from ftp server: "
						+ getReplyString());
			}

			if (isPositiveCommandCompletion() == false) {
				throw new JobSchedulerException("..error occurred in getFile() [retrieveFileStream] on the FTP server for file [" + remoteFile + "]: "
						+ getReplyString());
			}

			// TODO Buffersize must be an Option
			byte[] buffer = new byte[4096];
			out = new FileOutputStream(new File(localFile), append);
			// TODO get progress info

			int bytes_read = 0;
			synchronized (this) {
				while ((bytes_read = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytes_read);
					out.flush();
					totalBytes += bytes_read;
				}
			}
			in.close();
			out.close();
			if (!completePendingCommand()) {
				logout();
				disconnect();
				throw (new JobSchedulerException("File transfer failed."));
			}
			if (isNegativeCommandCompletion()) {
				throw new JobSchedulerException("..error occurred in getFile() on the FTP server for file [" + remoteFile + "]: " + getReplyString());
			}
			if (totalBytes > 0)
				return totalBytes;
			else
				return -1L;
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (Exception e) {
			}
			try {
				if (out != null)
					out.flush();
			}
			catch (Exception e) {
			}
			try {
				if (out != null)
					out.close();
			}
			catch (Exception e) {
			}
		}
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
	public boolean put(String localFile, String remoteFile) throws Exception {
		FileInputStream in = null;
		boolean rc = false;
		try {
			in = new FileInputStream(localFile);
			// TODO get progress info
			rc = storeFile(remoteFile, in);
			return rc;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (Exception e) {
			}
		}
	}

	/**
	 * Stores a file on the server using the given name.
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @return The total number of bytes written.
	 * @exception Exception
	 * @see #put( String, String )
	 */
	@Override
	public long putFile(String localFile, String remoteFile) throws Exception {
		java.io.OutputStream outputStream = storeFileStream(remoteFile);
		if (isNegativeCommandCompletion()) {
			throw new JobSchedulerException("..error occurred in storeFileStream() on the FTP server for file [" + remoteFile + "]: " + getReplyString());
		}
		return putFile(localFile, outputStream);
	} // putFile

	/**
	 *
	 * @param localfile The name of the local file.
	 * @param an OutputStream through which data can be 
	 * written to store a file on the server using the given name.
	 * @return The total number of bytes written.
	 * @exception Exception
	 */
	@Override
	public long putFile(String localFile, OutputStream out) throws Exception {
		FileInputStream in = null;
		long totalBytes = 0;
		try {

			if (out == null)
				throw (new JobSchedulerException("output stream has null value."));
			// TODO Buffersize must be an Option
			byte[] buffer = new byte[4096];
			in = new FileInputStream(new File(localFile));
			// TODO get progress info
			int bytesWritten;
			synchronized (this) {
				while ((bytesWritten = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytesWritten);
					totalBytes += bytesWritten;
				}
			}

			in.close();
			out.close();
			if (!completePendingCommand()) {
				logout();
				disconnect();
				throw (new JobSchedulerException("File transfer failed."));
			}
			if (isNegativeCommandCompletion()) {
				throw new JobSchedulerException("..error occurred in putFile() on the FTP server for file [" + localFile + "]: " + getReplyString());
			}
			return totalBytes;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			try {
				if (in != null)
					in.close();
			}
			catch (Exception e) {
			}
			try {
				out.flush();
			}
			catch (Exception e) {
			}
			try {
				if (out != null)
					out.close();
			}
			catch (Exception e) {
			}
		}
	} // putFile

	/**
	 * append a local file to the remote one on the server
	 * 
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @return The total number of bytes appended.
	 * @exception Exception
	 * @see #put( String, String )
	 * @see #putFile( String, String )
	 */

	public long appendFile(String localFile, String remoteFile) throws Exception {
		return putFile(localFile, appendFileStream(remoteFile));
	} // appendFile

	/**
	 * Using ASCII mode for file transfers
	 * @return True if successfully completed, false if not.
	 * @throws IOException If an I/O error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	public boolean ascii() throws IOException {
		return setFileType(FTP.ASCII_FILE_TYPE);
	}

	/**
	 * Using Binary mode for file transfers
	 * @return True if successfully completed, false if not.
	 * @throws IOException If an I/O error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	public boolean binary() throws IOException {
		return setFileType(FTP.BINARY_FILE_TYPE);
	}

	/**
	 *
	 * @param directory The new working directory.
	 * @return The reply code received from the server.
	 * @throws IOException If an I/O error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	public int cd(String directory) throws IOException {
		return cwd(directory);
	}

	/**
	 * Deletes a file on the FTP server.
	 * @param The pathname of the file to be deleted.
	 * @return True if successfully completed, false if not.
	 * @throws IOException If an I/O error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	public boolean delete(String pathname) throws IOException {
		return deleteFile(pathname);
	}

	@Override
	public boolean login(String strUserName, String strPassword) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::login";

		try {
			super.login(strUserName, strPassword);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LogReply();

		return true;
	} // private boolean login
}
