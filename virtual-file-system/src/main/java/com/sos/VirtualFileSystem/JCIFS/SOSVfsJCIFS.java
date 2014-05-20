package com.sos.VirtualFileSystem.JCIFS;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbSession;

import org.apache.log4j.Logger;
import org.omg.SendingContext.RunTime;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsSuperClass;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * @ressources webdavclient4j-core-0.92.jar
 *
 * @author Robert Ehrlich
 *
 */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsJCIFS extends SOSVfsTransferBaseClass {
	@SuppressWarnings("unused")
	private final String				conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String			conSVNVersion	= "$Id$";
	@SuppressWarnings("unused")
	private final Logger				logger			= Logger.getLogger(this.getClass());

	private NtlmPasswordAuthentication	authentication	= null;
	private boolean						isConnected		= false;
	private String						domain			= null;

	/**
	 *
	 * \brief SOSVfsJCIFS
	 *
	 * \details
	 *
	 */
	public SOSVfsJCIFS() {
		super();
	}

	/**
	 *
	 * \brief Connect
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public ISOSConnection Connect() {
		@SuppressWarnings("unused")
		SOSConnection2OptionsAlternate pConnection2OptionsAlternate = null;
		this.Connect(pConnection2OptionsAlternate);
		return this;

	}

	/**
	 *
	 * \brief Connect
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pobjConnectionOptions
	 * @return
	 */
	@Override
	public ISOSConnection Connect(final SOSConnection2OptionsAlternate pConnection2OptionsAlternate) {
		connection2OptionsAlternate = pConnection2OptionsAlternate;

		if (connection2OptionsAlternate == null) {
			RaiseException(SOSVfs_E_190.params("connection2OptionsAlternate"));
		}

		this.connect(connection2OptionsAlternate.host.Value(), connection2OptionsAlternate.port.value());
		return this;
	}

	/**
	 *
	 * \brief Authenticate
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pAuthenticationOptions
	 * @return
	 */
	@Override
	public ISOSConnection Authenticate(final ISOSAuthenticationOptions pAuthenticationOptions) {
		authenticationOptions = pAuthenticationOptions;

		try {
			domain = connection2OptionsAlternate.domain.Value();

			this.doAuthenticate(authenticationOptions);
		}
		catch (Exception ex) {
			Exception exx = ex;

			this.disconnect();

			if (connection2OptionsAlternate != null) {
				SOSConnection2OptionsSuperClass optionsAlternatives = connection2OptionsAlternate.Alternatives();
				if (!optionsAlternatives.host.IsEmpty() && !optionsAlternatives.user.IsEmpty()) {
					logINFO(SOSVfs_I_170.params(connection2OptionsAlternate.Alternatives().host.Value()));
					try {

						domain = optionsAlternatives.domain.Value();
						host = optionsAlternatives.host.Value();
						port = optionsAlternatives.port.value();

						this.doAuthenticate(optionsAlternatives);
						exx = null;
					}
					catch (Exception e) {
						exx = e;
					}
				}
			}

			if (exx != null) {
				RaiseException(exx, SOSVfs_E_168.get());
			}
		}

		return this;
	}

	/**
	 *
	 * \brief login
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pUserName
	 * @param pPassword
	 */
	@Override
	public void login(final String pUserName, final String pPassword) {

		isConnected = false;
		try {
			userName = pUserName;

			logger.debug(SOSVfs_D_132.params(userName));

			jcifs.Config.setProperty("jcifs.smb.client.useExtendedSecurity", "false");
			UniAddress hostAddress = UniAddress.getByName(host);
			authentication = new NtlmPasswordAuthentication(domain, userName, pPassword);
			SmbSession.logon(hostAddress, authentication);

			isConnected = true;

			reply = "OK";
			logger.debug(SOSVfs_D_133.params(domain));
			logger.debug(SOSVfs_D_133.params(userName));
			this.LogReply();
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_134.params("authentication"));
		}

	} // private boolean login

	/**
	 *
	 * \brief disconnect
	 *
	 * \details
	 *
	 * \return
	 *
	 */
	@Override
	public void disconnect() {
		reply = "disconnect OK";

		isConnected = false;

		this.logINFO(reply);
	}

	/**
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
		return isConnected;
	}

	/**
	 * Creates a new subdirectory on the FTP server in the current directory .
	 * @param path The pathname of the directory to create.
	 * @exception JobSchedulerException
	 */
	@Override
	public void mkdir(final String path) {
		try {
			SOSOptionFolderName objF = new SOSOptionFolderName(path);
			reply = "mkdir OK";
			logger.debug(HostID(SOSVfs_D_179.params("mkdir", path)));
			for (String strSubFolder : objF.getSubFolderArray()) {
				strSubFolder = this.normalizePath(strSubFolder);
				logger.debug(HostID(SOSVfs_D_179.params("mkdir", strSubFolder)));
				if (this.fileExists(strSubFolder) == false) {
					SmbFile f = getSmbFile(strSubFolder);
					f.mkdir();
					logger.debug(HostID(SOSVfs_D_181.params("mkdir", strSubFolder, getReplyString())));
				}
				else {
					if (this.isDirectory(strSubFolder) == false) {
						RaiseException(SOSVfs_E_277.params(strSubFolder));
					}
				}
			}
			logINFO(HostID(SOSVfs_D_181.params("mkdir", path, getReplyString())));
		}
		catch (Exception e) {
			reply = e.toString();
			RaiseException(e, SOSVfs_E_134.params("[mkdir]"));
		}
	}

	/**
	 * Removes a directory on the FTP server (if empty).
	 * @param path The pathname of the directory to remove.
	 * @exception JobSchedulerException
	 */
	@Override
	public void rmdir(String path) {
		try {
			reply = "rmdir OK";

			path = this.normalizePath(path);
			if (!path.endsWith("/")) {
				path += "/";
			}
			SmbFile f = getSmbFile(path);

			if (!f.exists()) {
				throw new Exception(String.format("Filepath '%1$s' does not exist.", f.getPath()));
			}
			if (!f.isDirectory()) {
				throw new Exception(String.format("Filepath '%1$s' is not a directory.", f.getPath()));
			}
			f.delete();

			reply = "rmdir OK";
			logger.info(HostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
		}
		catch (Exception e) {
			reply = e.toString();
			throw new JobSchedulerException(SOSVfs_E_134.params("[rmdir] " + path), e);
		}
	}

	/**
	 * Checks if file is a directory
	 *
	 * @param path
	 * @return true, if filename is a directory
	 */
	@Override
	public boolean isDirectory(final String path) {
		SmbFile f = null;

		try {
			f = getSmbFile(this.normalizePath(path));
			return f.isDirectory();
		}
		catch (Exception e) {
		}

		return false;
	}

	/**
	 * Checks if file is hidden
	 *
	 * @param path
	 * @return true, if filename is a directory
	 */
	public boolean isHidden(final String path) {
		SmbFile f = null;

		try {
			f = getSmbFile(this.normalizePath(path));
			return f.isHidden();
		}
		catch (Exception e) {
		}

		return false;
	}

	/**
	 *
	 * \brief listNames
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 */
	@Override
	public String[] listNames(String path) throws IOException {

		SmbFile f = null;
		try {
			if (path.length() == 0) {
				path = "/";
			}
			if (!this.fileExists(path)) {
				return null;
			}

			if (!this.isDirectory(path)) {
				reply = "ls OK";
				return new String[] { path };
			}

			f = getSmbFile(this.normalizePath(path));
			String sep = path.endsWith("/") ? "" : "/";

			SmbFile[] lsResult = f.listFiles();
			String[] result = new String[lsResult.length];
			for (int i = 0; i < lsResult.length; i++) {
				SmbFile entry = lsResult[i];
				//result[i] = entry.getPath();

				String strFileName = path + sep + entry.getName();
				result[i] = strFileName;
			}
			reply = "ls OK";
			return result;
		}
		catch (Exception e) {
			reply = e.toString();
			return null;
		}
	}

	/**
	 * return the size of remote-file on the remote machine on success, otherwise -1
	 * @param path the file on remote machine
	 * @return the size of remote-file on remote machine
	 */
	@Override
	public long size(final String path) throws Exception {
		long size = -1;
		SmbFile f = null;
		try {
			f = getSmbFile(this.normalizePath(path));
			if (f.exists()) {
				size = f.length();
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw new Exception(SOSVfs_E_161.params("checking size", e));
		}

		return size;
	}

	/**
	 *
	 * \brief getFile
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param remoteFile
	 * @param localFile
	 * @param append
	 * @return
	 */
	@Override
	public long getFile(final String remoteFile, final String localFile, final boolean append) {
		String sourceLocation = this.resolvePathname(remoteFile);
		File transferFile = null;
		long remoteFileSize = -1;
		FileOutputStream fos = null;
		SmbFile f = null;
		SmbFileInputStream in = null;
		try {
			remoteFileSize = this.size(this.normalizePath(remoteFile));

			f = getSmbFile(this.normalizePath(remoteFile));
			in = new SmbFileInputStream(f);
			fos = new FileOutputStream(localFile, append);

			byte[] b = new byte[8192];
			int n, tot = 0;
			while ((n = in.read(b)) > 0) {
				fos.write(b, 0, n);
				tot += n;
			}

			in.close();
			in = null;

			fos.flush();
			fos.close();
			fos = null;

			transferFile = new File(localFile);

			if (!append) {
				if (remoteFileSize > 0 && remoteFileSize != transferFile.length()) {
					throw new JobSchedulerException(SOSVfs_E_162.params(remoteFileSize, transferFile.length()));
				}
			}

			remoteFileSize = transferFile.length();
			reply = "get OK";
			logINFO(HostID(SOSVfs_I_182.params("getFile", sourceLocation, localFile, getReplyString())));
		}
		catch (Exception ex) {
			reply = ex.toString();
			RaiseException(ex, SOSVfs_E_184.params("getFile", sourceLocation, localFile));
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception ex) {
				}
			}
			if (fos != null) {
				try {
					fos.close();
				}
				catch (Exception ex) {
				}
			}
		}

		return remoteFileSize;

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
	public long putFile(final String localFilePath, final String remoteFilePath) {
		long size = 0;

		SmbFile remoteFile = null;
		SmbFileOutputStream out = null;
		FileInputStream in = null;

		try {
			remoteFile = getSmbFile(this.normalizePath(remoteFilePath));

			in = new FileInputStream(localFilePath);
			out = new SmbFileOutputStream(remoteFile);

			byte[] b = new byte[8192];
			int n, tot = 0;
			while ((n = in.read(b)) > 0) {
				out.write(b, 0, n);
				tot += n;
			}

			reply = "put OK";
			logINFO(HostID(SOSVfs_I_183.params("putFile", localFilePath, remoteFile.getPath(), getReplyString())));
			size = this.size(this.normalizePath(remoteFilePath));
		}
		catch (Exception e) {
			reply = e.toString();
			RaiseException(e, SOSVfs_E_185.params("putFile()", localFilePath, remoteFilePath));
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				}
				catch (Exception e) {
				}
			}
		}

		return size;
	}

	/**
	 * Deletes a file on the FTP server.
	 * @param The path of the file to be deleted.
	 * @return True if successfully completed, false if not.
	 * @throws RunTime error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	@Override
	public void delete(final String path) {
		try {
			if (this.isDirectory(this.normalizePath(path))) {
				throw new JobSchedulerException(SOSVfs_E_186.params(path));
			}

			SmbFile f = getSmbFile(this.normalizePath(path));
			f.delete();
		}
		catch (Exception ex) {
			reply = ex.toString();
			RaiseException(ex, SOSVfs_E_187.params("delete", path));
		}

		reply = "rm OK";
		logINFO(HostID(SOSVfs_D_181.params("delete", path, getReplyString())));
	}

	/**
	 *
	 * \brief rename
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param from
	 * @param to
	 */
	@Override
	public void rename(final String from, final String to) {
		SmbFile fromF = null;
		SmbFile toF = null;
		try {
			fromF = getSmbFile(this.normalizePath(from));
			toF = getSmbFile(this.normalizePath(to));

			fromF.renameTo(toF);
		}
		catch (Exception e) {
			reply = e.toString();
			throw new JobSchedulerException(SOSVfs_E_188.params("rename", from, to), e);
		}

		reply = "mv OK";
		logger.info(HostID(SOSVfs_I_189.params(from, to, getReplyString())));
	}

	/**
	 *
	 * \brief ExecuteCommand
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param strCmd
	 */
	@Override
	public void ExecuteCommand(final String cmd) {
		logger.debug("not implemented yet");
	}

	/**
	 *
	 * \brief getInputStream
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param path
	 * @return
	 */
	@Override
	// SOSVfsTransferBaseClass
	public InputStream getInputStream(final String path) {

		SmbFile f = null;
		try {
			f = getSmbFile(this.normalizePath(path));
			return new SmbFileInputStream(f);
		}
		catch (Exception ex) {
			RaiseException(ex, SOSVfs_E_193.params("getInputStream()", path));
			return null;
		}
	}

	/**
	 *
	 * \brief getOutputStream
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param path
	 * @return
	 */
	@Override
	// SOSTransferBaseClass
	public OutputStream getOutputStream(final String path) {
		SmbFile f = null;
		try {
			f = getSmbFile(this.normalizePath(path));
			return new SmbFileOutputStream(f);
		}
		catch (Exception ex) {
			RaiseException(ex, SOSVfs_E_193.params("getOutputStream()", path));
			return null;
		}
	}

	/**
	 *
	 * \brief changeWorkingDirectory
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param path
	 * @return
	 */
	@Override
	public boolean changeWorkingDirectory(final String path) {
		SmbFile f = null;
		try {
			f = getSmbFile(this.normalizePath(path));

			if (!f.exists()) {
				reply = String.format("Filepath '%1$s' does not exist.", f.getPath());
				return false;
			}
			if (!f.isDirectory()) {
				reply = String.format("Filepath '%1$s' is not a directory.", f.getPath());
				return false;
			}

			currentDirectory = f.getPath();
			reply = "cwd OK";
		}
		catch (Exception ex) {
			throw new JobSchedulerException(SOSVfs_E_193.params("cwd", path), ex);
		}
		finally {
			String strM = SOSVfs_D_194.params(path, getReplyString());
			logger.debug(strM);
		}
		return true;
	}

	/**
	 *
	 * \brief getFileHandle
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param fileName
	 * @return
	 */
	@Override
	public ISOSVirtualFile getFileHandle(String fileName) {
		fileName = adjustFileSeparator(fileName);
		ISOSVirtualFile file = new SOSVfsJCIFSFile(fileName);
		// os darf nicht an der Stelle verwendet werden
		//OutputStream os = getOutputStream(fileName);
		file.setHandler(this);

		logger.debug(SOSVfs_D_196.params(fileName));

		return file;
	}

	/**
	 *
	 * \brief getModificationTime
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param path
	 * @return
	 */
	@Override
	public String getModificationTime(final String path) {
		SmbFile f = null;
		String dateTime = null;
		try {
			f = getSmbFile(this.normalizePath(path));
			if (f.exists()) {
				long lm = f.getLastModified();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dateTime = df.format(new Date(lm));
			}
		}
		catch (Exception ex) {

		}
		return dateTime;
	}

	/**
	 *
	 * \brief fileExists
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param filename
	 * @return
	 */
	@Override
	protected boolean fileExists(final String filename) {

		SmbFile file = null;
		try {
			file = getSmbFile(this.normalizePath(filename));
			return file.exists();
		}
		catch (Exception e) {
			return false;
		}
	}

	/**
	 *
	 * \brief getCurrentPath
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	protected String getCurrentPath() {
		return currentDirectory;
	}

	/**
	 *
	 * \brief normalizePath
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @param path
	 * @return
	 */
	private String normalizePath(final String path) {
		return path.replaceAll("\\\\", "/");
	}

	private String getSmbFilePath(String path) {
		path = path.startsWith("/") ? path.substring(1) : path;
		return "smb://" + host + "/" + path;
	}

	private SmbFile getSmbFile(final String path) throws Exception {
		try {
			return new SmbFile(getSmbFilePath(path), authentication);
		}
		catch (Exception ex) {
			throw new Exception("cannot get SmbFile: " + path);
		}
	}

	/**
	 *
	 * \brief doAuthenticate
	 *
	 * \details
	 *
	 * \return ISOSConnection
	 *
	 * @param authenticationOptions
	 * @return
	 * @throws Exception
	 */
	private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions pAuthenticationOptions) throws Exception {

		authenticationOptions = pAuthenticationOptions;
		isConnected = false;
		//unsere tests sind ohne domain. eventuell muss noch gesetzt werden
		String domain = "";

		userName = authenticationOptions.getUser().Value();
		String password = authenticationOptions.getPassword().Value();
		logger.debug(SOSVfs_D_132.params(userName));

		try {
			//Accessing a DFS link on Samba directly could result in an error. 
			//This issue has been fixed. 
			//Samba 3.0.x does not support raw NTLMSSP and therefore the new default JCIFS settings that use NTLMSSP break JCIFS and Samba 3.0.x compatibility. 
			//To work-around, turn off extended security and use NTLMv1 by setting 
			//jcifs.smb.client.useExtendedSecurity=false 
			//and 
			//jcifs.smb.lmCompatibility=0. 
			jcifs.Config.setProperty("jcifs.smb.client.useExtendedSecurity", "false");
			UniAddress hostAddress = UniAddress.getByName(host);
			authentication = new NtlmPasswordAuthentication(domain, userName, password);
			SmbSession.logon(hostAddress, authentication);

			isConnected = true;
		}
		catch (Exception ex) {
			throw new JobSchedulerException(SOSVfs_E_167.params(authenticationOptions.getAuth_method().Value(), authenticationOptions.getAuth_file().Value()),
					ex);
		}

		reply = "OK";
		logger.debug(SOSVfs_D_133.params(userName));
		this.LogReply();

		return this;
	}

	/**
	 *
	 * \brief connect
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param phost
	 * @param pport
	 */
	private void connect(final String phost, final int pport) {

		host = phost;
		port = pport;

		logger.debug(SOSVfs_D_0101.params(host, port));

		if (this.isConnected() == false) {

			this.LogReply();
		}
		else {
			logWARN(SOSVfs_D_0103.params(host, port));
		}
	}

	@Override
	public OutputStream getOutputStream() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	public static void main(final String[] args) throws Exception {
		System.out.println("hallo");

		String host = "wilma.sos";
		String domain = null;
		String username = "dr";
		String password = "dr";

		jcifs.Config.setProperty("jcifs.smb.client.useExtendedSecurity", "false");
		UniAddress hostAddress = UniAddress.getByName(host);
		NtlmPasswordAuthentication authentication = new NtlmPasswordAuthentication(domain, username, password);
		//new NtlmPasswordAuthentication(username+":"+password);// new NtlmPasswordAuthentication(address, username, password);        
		SmbSession.logon(hostAddress, authentication);

		/**
		SmbFile file = new SmbFile("smb://wilma.sos/re/Documents/",authentication);

		SmbFile[] files = file.listFiles();

		for (SmbFile file2 : files) {
		    System.out.println( " " + file2.getName() );
		}*/

	}
}
