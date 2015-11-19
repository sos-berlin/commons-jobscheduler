package com.sos.VirtualFileSystem.SFTP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.omg.SendingContext.RunTime;

import sos.util.SOSString;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSFileEntry;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * @ressources jsch-0.1.48.jar
 *
 * @author Robert Ehrlich
 *
 */
@I18NResourceBundle(
					baseName = "SOSVirtualFileSystem",
					defaultLocale = "en")
public class SOSVfsSFtpJCraft extends SOSVfsTransferBaseClass {
	@SuppressWarnings("unused")
	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	private final Logger		logger			= Logger.getLogger(this.getClass());
	/** ssh connection object */
	private Channel				sshConnection	= null;
	/** ssh session object */
	private Session				sshSession		= null;
	/** SFTP Client **/
	private ChannelSftp			sftpClient		= null;
	private JSch				secureChannel	= null;
	private Map environmentVariables = null;
	
	// additional properties for
	// https://change.sos-berlin.com/browse/JITL-123 [SP]
	private Integer exitCode;
	private String exitSignal;
	private StringBuffer outContent;
	private StringBuffer errContent;
    private boolean isRemoteWindowsShell = false;

    //proxy
    private SOSOptionProxyProtocol proxyProtocol = null;
    private String proxyHost = null;
	private int proxyPort = 0;
	private String proxyUser = null;
	private String proxyPassword = null;
	private int connectionTimeout = 0; //Millisekunden
	private boolean simulateShell = false;
    
	/**
	 *
	 * \brief SOSVfsSFtpJCraft
	 *
	 * \details
	 *
	 */
	public SOSVfsSFtpJCraft() {
		super();
		secureChannel = new JSch();
	}

	@Override public void StrictHostKeyChecking(final String pstrStrictHostKeyCheckingValue) {
		JSch.setConfig("StrictHostKeyChecking", pstrStrictHostKeyCheckingValue);
		//	'	sshConnection.StrictHostKeyChecking(pstrStrictHostKeyCheckingValue);
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
	@Override public ISOSConnection Connect() {
		this.Connect(connection2OptionsAlternate);
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
	@Override public ISOSConnection Connect(final SOSConnection2OptionsAlternate pConnection2OptionsAlternate) {
		connection2OptionsAlternate = pConnection2OptionsAlternate;
		if (connection2OptionsAlternate == null) {
			RaiseException(SOSVfs_E_190.params("connection2OptionsAlternate"));
		}
		this.StrictHostKeyChecking(connection2OptionsAlternate.StrictHostKeyChecking.Value());
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
	 * @param authenticationOptions
	 * @return
	 */
	@Override public ISOSConnection Authenticate(final ISOSAuthenticationOptions pAuthenticationOptions) {
		authenticationOptions = pAuthenticationOptions;
		try {
			proxyProtocol = connection2OptionsAlternate.proxy_protocol;
			proxyHost = connection2OptionsAlternate.proxy_host.Value();
			proxyPort = connection2OptionsAlternate.proxy_port.value();
			proxyUser = connection2OptionsAlternate.proxy_user.Value();
			proxyPassword = connection2OptionsAlternate.proxy_password.Value();
			
			this.doAuthenticate(authenticationOptions);
		}
		catch (JobSchedulerException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new JobSchedulerException(ex);
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
	 * @param strUserName
	 * @param strPassword
	 */
	@Override public void login(final String pUserName, final String pPassword) {
		try {
			userName = pUserName;
			logger.debug(SOSVfs_D_132.params(userName));
			this.createSession(userName, host, port);
			sshSession.setPassword(pPassword);
			sshSession.connect();
			this.createSftpClient();
			reply = "OK";
			logger.info(SOSVfs_D_133.params(userName));
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
	@Override public void disconnect() {
		reply = "disconnect OK";
		if (sftpClient != null) {
			try {
				sftpClient.exit();
				if (sftpClient.isConnected()) {
					sftpClient.disconnect();
				}
				sftpClient = null;
			}
			catch (Exception ex) {
				reply = "disconnect: " + ex;
			}
		}
		if (sshConnection != null) {
			try {
				sshConnection.disconnect();
				sshConnection = null;
			}
			catch (Exception ex) {
				reply = "disconnect: " + ex;
			}
		}
		this.logINFO(reply);
	}
	
	@Override
	public void logout() {
		try {
		  
   		  if ( sshSession != null && sshSession.isConnected() == true) {
   			    sshSession.disconnect();
				logDEBUG(SOSVfs_D_138.params(host, getReplyString()));
			}
			else {
				logINFO("not connected, logout useless.");
			}
		}
		catch (Exception e) { 
			logWARN(SOSVfs_W_140.get() + e.getMessage());
		}
	}

	

	@Override public boolean isConnected() {
		return sftpClient != null && sftpClient.isConnected();
	}

	/**
	 * Creates a directory on the Server.
	 * @param pstrPathName The pathname of the directory to create.
	 * @exception JobSchedulerException
	 */
	@Override public void mkdir(final String path) {
		try {
			String p = path.replaceAll("//+", "/").replaceFirst("/$", "");
			SOSOptionFolderName objF = new SOSOptionFolderName(path);
			reply = "mkdir OK";
			logger.debug(HostID(SOSVfs_D_179.params("mkdir", p)));
			String[] subfolders = objF.getSubFolderArrayReverse();
			int idx = subfolders.length;
			for (String strSubFolder : objF.getSubFolderArrayReverse()) {
				SftpATTRS attributes = getAttributes(strSubFolder);
				if (attributes != null && attributes.isDir()) {
					logger.debug(SOSVfs_E_180.params(strSubFolder));
					break;
				}
				if (attributes != null && !attributes.isDir()) {
					RaiseException(SOSVfs_E_277.params(strSubFolder));
					break;
				}
				idx--;
			}
			subfolders = objF.getSubFolderArray();
			for (int i = idx; i < subfolders.length; i++) {
				this.getClient().mkdir(subfolders[i]);
				logger.debug(HostID(SOSVfs_E_0106.params("mkdir", subfolders[i], getReplyString())));
			}
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
	@Override public void rmdir(final String path) {
		try {
			SOSOptionFolderName objF = new SOSOptionFolderName(path);
			reply = "rmdir OK";
			for (String subfolder : objF.getSubFolderArrayReverse()) {
				logger.debug(HostID(SOSVfs_D_179.params("rmdir", subfolder)));
				this.getClient().rmdir(subfolder);
				reply = "rmdir OK";
				logger.debug(HostID(SOSVfs_D_181.params("rmdir", subfolder, getReplyString())));
			}
			logINFO(HostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
		}
		catch (Exception e) {
			reply = e.toString();
			RaiseException(e, SOSVfs_E_134.params("[rmdir]"));
		}
	}

	/**
	 * Check and return "true" if file is a directory
	 *
	 * @param filename
	 * @return true, if filename is a directory
	 */
	@Override public boolean isDirectory(final String filename) {
		/**
		 * Problem: the concept of links is not considered in the Concept of SOSVfs
		 * Therefore: wie return here "true" if the filename is a link
		 */
		boolean flgR = false;
		try {
			SftpATTRS attributes = this.getClient().stat(filename);
			if (attributes != null) {
				flgR = attributes.isDir();
				if (flgR == false) {
					flgR = attributes.isLink();
				}
			}
		}
		catch (Exception e) {
		}
		return flgR;
	}

	/**
	 * returns attributes
	 *
	 * @param filename
	 * @return SftpATTRS
	 */
	public SftpATTRS getAttributes(final String filename) {
		SftpATTRS attributes = null;
		try {
			attributes = this.getClient().stat(filename);
		}
		catch (Exception e) {
		}
		return attributes;
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
	@Override public String[] listNames(String path) throws IOException {
		path = resolvePathname(path);
		try {
			if (path.length() == 0) {
				path = ".";
			}
			if (!this.fileExists(path)) {
				return null;
			}
			if (!isDirectory(path)) {
				reply = "ls OK";
				return new String[] { path };
			}
			@SuppressWarnings("unchecked") Vector<LsEntry> lsResult = this.getClient().ls(path);
			
			this.getSOSFileEntries().clear();

			
			String[] result = new String[lsResult.size()];
			String sep = path.endsWith("/") ? "" : "/";
			for (int i = 0, j = 0; i < lsResult.size(); i++) {
				LsEntry entry = lsResult.get(i);
 			    SOSFileEntry sosFileEntry = new SOSFileEntry();
	            sosFileEntry.setDirectory(entry.getAttrs().isDir());
	            sosFileEntry.setFilename(entry.getFilename());
	            sosFileEntry.setFilesize(entry.getAttrs().getSize());
	            sosFileEntry.setParentPath(path);
	            this.getSOSFileEntries().add(sosFileEntry);
				
				String strFileName = path + sep + entry.getFilename();
				//				if (isDirectory(strFileName) == false) {
				result[j++] = strFileName;
				//				}
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
	 * @param remoteFile the file on remote machine
	 * @return the size of remote-file on remote machine
	 */
	@Override public long size(String remoteFile) throws Exception {
		remoteFile = this.resolvePathname(remoteFile);
		long size = -1;
		SftpATTRS objAttr;
		try {
			objAttr = this.getClient().stat(remoteFile);
			if (objAttr != null) {
				size = objAttr.getSize();
			}
		}
		catch (SftpException e) {
			// e.printStackTrace();
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
	@Override public long getFile(final String remoteFile, final String localFile, final boolean append) {
		String sourceLocation = this.resolvePathname(remoteFile);
		File transferFile = null;
		long remoteFileSize = -1;
		FileOutputStream fos = null;
		try {
			remoteFileSize = this.size(remoteFile);
			fos = new FileOutputStream(localFile, append);
			this.getClient().get(sourceLocation, fos);
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
	@Override// ISOSVfsFileTransfer
	public long putFile(final String localFile, final String remoteFile) {
		long size = 0;
		try {
			// mit Progress Monitor
			// SftpProgressMonitor monitor = new ProgressMonitor();
			// this.getClient().put(localFile, this.resolvePathname(remoteFile),monitor,ChannelSftp.OVERWRITE);
			this.getClient().put(localFile, this.resolvePathname(remoteFile), ChannelSftp.OVERWRITE);
			reply = "put OK";
			logINFO(HostID(SOSVfs_I_183.params("putFile", localFile, remoteFile, getReplyString())));
			return this.size(remoteFile);
		}
		catch (Exception e) {
			reply = e.toString();
			RaiseException(e, SOSVfs_E_185.params("putFile()", localFile, remoteFile));
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
	@Override public void delete(final String path) {
		try {
			if (this.isDirectory(path)) {
				throw new JobSchedulerException(SOSVfs_E_186.params(path));
			}
			this.getClient().rm(path);
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
	@Override public void rename(String from, String to) {
		from = this.resolvePathname(from);
		to = this.resolvePathname(to);
		try {
			this.getClient().rename(from, to);
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
	@Override public void ExecuteCommand(String cmd) {
		final String strEndOfLine = System.getProperty("line.separator");
		ChannelExec channelExec = null;
		exitCode = null;
		InputStream out = null;
		InputStream err = null;
		BufferedReader errReader = null;
		try {
			if (sshSession == null) {
				// sshSession = sshConnection.openSession();
				// TODO create Session ??
				throw new JobSchedulerException(SOSVfs_E_190.params("sshSession"));
			}
			channelExec = (ChannelExec) sshSession.openChannel("exec");
			channelExec.setPty(isSimulateShell());
			//JITL-157
			cmd = cmd.replaceAll("\0", "\\\\\\\\").replaceAll("\"", "\\\"");
			channelExec.setCommand(cmd);
			channelExec.setInputStream(null);
			channelExec.setErrStream(null);
			out = channelExec.getInputStream();
			err = channelExec.getErrStream();
			// https://change.sos-berlin.com/browse/JITL-157
			if(environmentVariables != null && !environmentVariables.isEmpty()){
			  for(Object key : environmentVariables.keySet()){
			    channelExec.setEnv((String)key, (String)environmentVariables.get(key));
			  }
			}
			channelExec.connect();
			logger.debug(SOSVfs_D_163.params("stdout", cmd));
			outContent = new StringBuffer();
			byte[] tmp = new byte[1024];
			while (true) {
				while (out.available() > 0) {
					int i = out.read(tmp, 0, 1024);
					if (i < 0) {
						break;
					}
					// output line
					outContent.append(new String(tmp, 0, i));
				}
				if (channelExec.isClosed()) {
					exitCode = channelExec.getExitStatus();
					break;
				}
				try {
					Thread.sleep(1000);
				}
				catch (Exception ee) {
				}
			}
			//sdout output will be used from another applications 
			if(outContent.length() > 0){
				logger.info(outContent);
			}
			logger.debug(SOSVfs_D_163.params("stderr", cmd));
			errReader = new BufferedReader(new InputStreamReader(err));
			errContent = new StringBuffer();
			while (true) {
				String line = errReader.readLine();
				if (line == null) {
					break;
				}
				errContent.append(line + strEndOfLine);
			}
			logger.debug(errContent);
			if(exitCode != null && !exitCode.equals(new Integer(0))){
				StringBuffer errMsg = new StringBuffer();
				errMsg.append(exitCode.toString());
				if(errContent.length() > 0){
					errMsg.append(" (");
					errMsg.append(errContent);
					errMsg.append(")");
				}
				throw new JobSchedulerException(SOSVfs_E_164.params(errMsg));
			}
			
			reply = "OK";
		}
		catch (JobSchedulerException ex) {
			reply = ex.toString();
			if(connection2OptionsAlternate.raise_exception_on_error.value()){
				throw ex;
			}
			
		}
		catch (Exception ex) {
			reply = ex.toString();
			if(connection2OptionsAlternate.raise_exception_on_error.value()){
				RaiseException(ex, SOSVfs_E_134.params("ExecuteCommand"));
			}
			
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (Exception e) {
				}
			}
			if (errReader != null) {
				try {
					errReader.close();
				}
				catch (Exception e) {
				}
			}
			if (err != null) {
				try {
					err.close();
				}
				catch (Exception e) {
				}
			}
			if (channelExec != null) {
				try {
					channelExec.disconnect();
				}
				catch (Exception e) {
				}
			}
			logINFO(HostID(SOSVfs_I_192.params(getReplyString())));
		}
	}
	
	
	public void setEnvironmentVariables(Map<String, String> envVariables){
	  this.environmentVariables = envVariables;
	}

	/**
	 *
	 * \brief getInputStream
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param fileName
	 * @return
	 */
	@Override public InputStream getInputStream(final String fileName) {
		try {
			return this.getClient().get(fileName);
		}
		catch (Exception ex) {
			RaiseException(ex, SOSVfs_E_193.params("getInputStream()", fileName));
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
	 * @param fileName
	 * @return
	 */
	@Override public OutputStream getOutputStream(final String fileName) {
		try {
			boolean flgModeAppend = false;
			boolean flgModeRestart = false;
			int intTransferMode = ChannelSftp.OVERWRITE;
			if (flgModeAppend) {
				intTransferMode = ChannelSftp.APPEND;
			}
			else
				if (flgModeRestart) {
					intTransferMode = ChannelSftp.RESUME;
				}
			return this.getClient().put(fileName, intTransferMode);
		}
		catch (Exception ex) {
			RaiseException(ex, SOSVfs_E_193.params("getOutputStream()", fileName));
			return null;
		}
	}

	@Override public boolean changeWorkingDirectory(String pathname) {
		try {
			pathname = this.resolvePathname(pathname);
			if (!this.fileExists(pathname)) {
				reply = String.format("Filepath '%1$s' does not exist.", pathname);
				return false;
			}
			if (!this.isDirectory(pathname)) {
				reply = String.format("Filepath '%1$s' is not a directory.", pathname);
				return false;
			}
			this.getClient().cd(pathname);
			reply = "cwd OK";
		}
		catch (Exception ex) {
			throw new JobSchedulerException(SOSVfs_E_193.params("cwd", pathname), ex);
		}
		finally {
			String strM = SOSVfs_D_194.params(pathname, getReplyString());
			logger.debug(strM);
		}
		return true;
	}

	@Override public ISOSVirtualFile getFileHandle(String fileName) {
		fileName = adjustFileSeparator(fileName);
		ISOSVirtualFile file = new SOSVfsSFtpFileJCraft(fileName);
		file.setHandler(this);
//		logger.debug(SOSVfs_D_196.params(fileName));
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
	@Override public String getModificationTime(final String path) {
		String dateTime = null;
		try {
			SftpATTRS objAttr = this.getClient().stat(path);
			if (objAttr != null) {
				int mt = objAttr.getMTime();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dateTime = df.format(new Date(mt));
			}
		}
		catch (SftpException e) {
			// e.printStackTrace();
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
	@Override protected boolean fileExists(final String filename) {
		try {
			SftpATTRS attributes = this.getClient().stat(filename);
			if (attributes != null) {
				return !attributes.isLink() || attributes.isDir();
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			return false;
		}
	}

	@Override protected String getCurrentPath() {
		String path = null;
		try {
			path = this.getClient().pwd();
			logger.debug(HostID(SOSVfs_D_195.params(path)));
			LogReply();
		}
		catch (Exception e) {
			RaiseException(e, SOSVfs_E_134.params("getCurrentPath"));
		}
		return path;
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
		userName = authenticationOptions.getUser().Value();
		String password = authenticationOptions.getPassword().Value();
		logger.debug(SOSVfs_D_132.params(userName));
		this.createSession(userName, host, port);
		if (authenticationOptions.getAuth_method().isPublicKey()) {
			logger.debug(SOSVfs_D_165.params("userid", "publickey"));
			SOSOptionInFileName authenticationFile = authenticationOptions.getAuth_file();
			authenticationFile.CheckMandatory(true);
			if (authenticationFile.IsNotEmpty()) {
				secureChannel.addIdentity(authenticationFile.JSFile().getPath());
			}
		}
		else {
			if (authenticationOptions.getAuth_method().isPassword()) {
				logger.debug(SOSVfs_D_165.params("userid", "password"));
				sshSession.setPassword(password);
			}
			else {
				throw new JobSchedulerException(SOSVfs_E_166.params(authenticationOptions.getAuth_method().Value()));
			}
		}
		try {
			sshSession.connect();
			this.createSftpClient();
		}
		catch (Exception e) {
			throw new JobSchedulerException(HostID(e.getClass().getName() + " - " + e.getLocalizedMessage()), e);
		}
		reply = "OK";
		logger.info(SOSVfs_D_133.params(userName));
		this.LogReply();
		return this;
	}

	public ChannelSftp getClient() {
		if (sftpClient == null) {
			try {
				if (sshConnection == null) {
					RaiseException(SOSVfs_E_190.params("sshConnection Object"));
				}
				sftpClient = (ChannelSftp) sshConnection;
			}
			catch (Exception e) {
				RaiseException(e, SOSVfs_E_196.get());
			}
		}
		return sftpClient;
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
		logger.info(SOSVfs_D_0101.params(host, port));
		if (this.isConnected() == false) {
			this.LogReply();
		}
		else {
			logWARN(SOSVfs_D_0103.params(host, port));
		}
	}

	/**
	 *
	 * \brief createSession
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param puser
	 * @param phost
	 * @param pport
	 * @throws Exception
	 */
	private void createSession(final String puser, final String phost, final int pport) throws Exception {
		if (secureChannel == null) {
			throw new JobSchedulerException(SOSVfs_E_190.params("secureChannel"));
		}
		sshSession = secureChannel.getSession(puser, phost, pport);
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", connection2OptionsAlternate.StrictHostKeyChecking.Value());
		sshSession.setConfig(config);
		
		setCommandsTimeout();
		setProxy();
		
		//am Ende
		setConnectionTimeout();
	}

	/**
	 * @TODO Der Wert muss noch festgelegt werden
	 * 
	 * zZeit wird nur bei Proxy type socks4 und socks5 gesetzt - ohne timeout bleibt diese Verbindung hängen
	 * 
	 * @throws Exception
	 */
	private void setConnectionTimeout() throws Exception{
		if(connectionTimeout > 0){
			//in Millisekunden
			logger.info(String.format("connection timeout = %s ms",connectionTimeout));
			sshSession.setTimeout(connectionTimeout);
		}
	}
	
	/**
	 * See http://stackoverflow.com/questions/16546198/configuring-timeout-on-jsch-channelsftp-operations
	 * 
	 * Sets the interval to send a keep-alive message.  If zero is
     * specified, any keep-alive message must not be sent.  The default interval
     * is zero
	 * 
	 * @TODO not used
	 * 
	 * @throws Exception
	 */
	private void setCommandsTimeout() throws Exception{
		// specified interval, in milliseconds
		//int interval = 0;
		//sshSession.setServerAliveInterval(interval);
	}
	
	/**
	 * @TODO proxyType aus property lesen
	 * 
	 * @throws Exception
	 */
	private void setProxy() throws Exception{
		if(!SOSString.isEmpty(this.proxyHost)){
			int connTimeout = 30000; //0,5 Minute
			
			logger.info(String.format("using proxy: protocol = %s, host = %s, port = %s, user = %s, pass = ?",
					proxyProtocol.Value(),
					proxyHost,
					proxyPort,
					proxyUser));
			
			if(proxyProtocol.isHttp()){
				ProxyHTTP proxy = new ProxyHTTP(proxyHost,proxyPort);
				if(!SOSString.isEmpty(proxyUser)){
					proxy.setUserPasswd(proxyUser,proxyPassword);
				}
				sshSession.setProxy(proxy); 
			}
			else if(proxyProtocol.isSocks5()){
				ProxySOCKS5 proxy = new ProxySOCKS5(proxyHost,proxyPort);
				if(!SOSString.isEmpty(proxyUser)){
					proxy.setUserPasswd(proxyUser,proxyPassword);
				}
				sshSession.setProxy(proxy);
				
				connectionTimeout = connTimeout;
			}
			else if(proxyProtocol.isSocks4()){
				ProxySOCKS4 proxy = new ProxySOCKS4(proxyHost,proxyPort);
				if(!SOSString.isEmpty(proxyUser)){
					proxy.setUserPasswd(proxyUser,proxyPassword);
				}
				sshSession.setProxy(proxy);
				
				connectionTimeout = connTimeout;
			}
			else{
				throw new Exception(String.format("unknown proxy protocol = %s",proxyProtocol.Value()));
			}
		}
	}

	
	/**
	 *
	 * \brief createObjFTPClient
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @throws Exception
	 */
	private void createSftpClient() throws Exception {
		if (sshSession == null) {
			throw new JobSchedulerException(SOSVfs_E_190.params("sshSession"));
		}
		sshConnection = sshSession.openChannel("sftp");
		if (connection2OptionsAlternate.use_zlib_compression.value() == true) {
			sshSession.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
			sshSession.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
			sshSession.setConfig("compression_level", connection2OptionsAlternate.zlib_compression_level.Value());
		}
		sshConnection.connect();
		sftpClient = (ChannelSftp) sshConnection;
	}
	/**
	 *
	 * @author re
	 *
	 * Progress monitor (sftp operations GET & PUT)
	 *
	 */
	private static class ProgressMonitor implements SftpProgressMonitor {
		long	totalSize			= 0;
		long	transmittedSize		= 0;
		long	transmittedPercent	= -1;

		@Override public void init(final int operation, final String src, final String dest, final long size) {
			totalSize = size;
			transmittedSize = 0;
			transmittedPercent = -1;
		}

		@Override public boolean count(final long transmitted) {
			System.out.println("Completed " + transmittedSize + "(" + transmittedPercent + "%) out of " + totalSize + ".");
			transmittedSize += transmitted;
			long percent = transmittedSize * 100 / totalSize;
			if (transmittedPercent >= percent) {
				return true;
			}
			transmittedPercent = percent;
			return true;
		}

		@Override public void end() {
			System.out.println("END " + transmittedSize + "(" + transmittedPercent + "%) out of " + totalSize + ".");
		}
	}

	@Override public void close() {
		try {
			//			sshSession.disconnect();  // see SOSFTP-122
			//			sshSession = null;
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override public OutputStream getOutputStream() {
		return null;
	}

	@Override public InputStream getInputStream() {
		return null;
	}

  @Override
  public SOSFileEntries getSOSFileEntries() {
      return sosFileEntries;
  }
  
  public StringBuffer getStdErr() throws Exception {
    return errContent;
  }

  @Override
  public StringBuffer getStdOut() throws Exception {
    return outContent;
  }

  @Override
  public Integer getExitCode() {
    return exitCode;
  }

  @Override
  public String getExitSignal() {
    return exitSignal;
  }

  // https://change.sos-berlin.com/browse/JITL-123
  @Override
  public String createScriptFile(String pstrContent) throws Exception {
    try {
      String commandScript = pstrContent;
      if (isRemoteWindowsShell == false) {
        commandScript = commandScript.replaceAll("(?m)\r", "");
      }
      logger.debug(SOSVfs_I_233.params(pstrContent));
      File fleTempScriptFile = File.createTempFile("sos-sshscript", getScriptFileNameSuffix());
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fleTempScriptFile)));
      out.write(commandScript);
      out.flush();
      out.close();
      fleTempScriptFile.deleteOnExit();
      putFile(fleTempScriptFile, 0700);
      String strFileName2Return = fleTempScriptFile.getName();
      if (isRemoteWindowsShell == false) {
        strFileName2Return = "./" + strFileName2Return;
      }
      logger.info(SOSVfs_I_253.params(fleTempScriptFile.getAbsolutePath()));
      return strFileName2Return;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
    
  private String getScriptFileNameSuffix() {
    if(isRemoteWindowsShell){
      return ".cmd";
    }else{
      return ".sh";
    }
  }

  @Override
  public boolean remoteIsWindowsShell() {
     ExecuteCommand("echo %ComSpec%");
    if (outContent.toString().indexOf("cmd.exe") > -1) {
      logger.debug(SOSVfs_D_237.get());
      isRemoteWindowsShell = true;
      return true;
    }
    return false;
  }

  private void putFile(File pfleCommandFile, Integer chmod) throws Exception {
    String strFileName = pfleCommandFile.getName();
    getClient().put(pfleCommandFile.getCanonicalPath(), strFileName);
    if(chmod != null){
      getClient().chmod(chmod, strFileName);
    }
  }

    @Override
    public boolean isSimulateShell() {
        return simulateShell;
    }
    
    @Override
    public void setSimulateShell(boolean simulateShell) {
        this.simulateShell = simulateShell;
    }
    

}
