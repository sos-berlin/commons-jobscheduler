package com.sos.VirtualFileSystem.WebDAV;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.contrib.ssl.StrictSSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.ssl.TrustMaterial;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.omg.SendingContext.RunTime;

import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntries;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * @ressources webdavclient4j-core-0.92.jar
 *
 * @author Robert Ehrlich
 *
 */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsWebDAV extends SOSVfsTransferBaseClass {

	private final String	conClassName		= "SOSVfsWebDAV";

	private final Logger	logger		= Logger.getLogger(SOSVfsWebDAV.class);

	private HttpURL			rootUrl		= null;
	private WebdavResource	davClient	= null;

	private String password = null;
	private String proxyHost = null;
	private int proxyPort = 0;
	private String proxyUser = null;
	private String proxyPassword = null;


	/**
	 *
	 * \brief SOSVfsWebDAV
	 *
	 * \details
	 *
	 */
	public SOSVfsWebDAV() {
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
		this.Connect(this.connection2OptionsAlternate);
		return this;

	}

	/**
	 * 
	 */
	@Override
	public ISOSConnection Connect(final SOSConnection2OptionsAlternate options) {
		connection2OptionsAlternate = options;

		if (connection2OptionsAlternate == null) {
			RaiseException(SOSVfs_E_190.params("connection2OptionsAlternate"));
		}

		this.connect(connection2OptionsAlternate.host.Value(), connection2OptionsAlternate.port.value());
		return this;
	}

	/**
	 * 
	 */
	@Override
	public ISOSConnection Authenticate(final ISOSAuthenticationOptions options) {
		authenticationOptions = options;
		try {
			proxyHost = connection2OptionsAlternate.proxy_host.Value();
			proxyPort = connection2OptionsAlternate.proxy_port.value();
			proxyUser = connection2OptionsAlternate.proxy_user.Value();
			proxyPassword = connection2OptionsAlternate.proxy_password.Value();

			this.doAuthenticate(authenticationOptions);
		}
		catch (Exception ex) {
			RaiseException(ex, SOSVfs_E_168.get());
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
		String method = "login";
		try {

			userName = pUserName;
			password = pPassword;

			logger.debug(SOSVfs_D_132.params(userName));

			HttpURL httpUrl = this.setRootHttpURL(userName, pPassword, host, port);
			davClient = getWebdavResource(httpUrl);

			if (!davClient.exists()) {
				throw new Exception(String.format("%s: HTTP-DAV isn't enabled %s ",
						method,
						getStatusMessage(davClient)));
			}

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
	@Override
	public void disconnect() {
		reply = "disconnect OK";

		if (davClient != null) {
			try {
				davClient.close();
				davClient = null;
			}
			catch (Exception ex) {
				reply = "disconnect: " + ex;
			}
		}

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
		return davClient != null;
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
			reply = "";
			logger.debug(HostID(SOSVfs_D_179.params("mkdir", path)));
			String[] subfolders = objF.getSubFolderArrayReverse();
			int idx = subfolders.length;
			for (String strSubFolder : objF.getSubFolderArrayReverse()) {
				if (this.isDirectory(strSubFolder)) {
					logger.debug(SOSVfs_E_180.params(strSubFolder));
					break;
				}
				else if (this.fileExists(strSubFolder)) {
					RaiseException(SOSVfs_E_277.params(strSubFolder));
					break;
				}
				idx--;
			}
			subfolders = objF.getSubFolderArray();
			for (int i = idx; i < subfolders.length; i++) {
				if (davClient.mkcolMethod(subfolders[i])) {
					reply = "mkdir OK";
					logger.debug(HostID(SOSVfs_E_0106.params("mkdir", subfolders[i], getReplyString())));
				}
				else {
					throw new Exception(getStatusMessage(davClient));
				}
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
	@Override
	public void rmdir(String path) {
		try {
			reply = "rmdir OK";

			path = this.normalizePath(path + "/");

			if (davClient.deleteMethod(path)) {
				reply = "rmdir OK";
				logger.debug(HostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
			}
			else {
				throw new JobSchedulerException(getStatusMessage(davClient));
			}

			logger.info(HostID(SOSVfs_D_181.params("rmdir", path, getReplyString())));
		}
		catch (Exception e) {
			reply = e.toString();
			throw new JobSchedulerException(SOSVfs_E_134.params("[rmdir]"), e);
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
		WebdavResource res = null;
		try {

			res = this.getResource(path);
			return res.isCollection();
		}
		catch (Exception e) {
		}
		finally {
			try {
				if (res != null) {
					res.close();
				}
			}
			catch (Exception ex) {
			}
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
		WebdavResource res = null;
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

			res = this.getResource(path);

			WebdavResource[] lsResult = res.listWebdavResources();
			String[] result = new String[lsResult.length];
			for (int i = 0; i < lsResult.length; i++) {
				WebdavResource entry = lsResult[i];
				result[i] = entry.getPath();
			}
			reply = "ls OK";
			return result;
		}
		catch (Exception e) {
			reply = e.toString();
			return null;
		}
		finally {
			if (res != null) {
				try {
					res.close();
				}
				catch (Exception ex) {
				}
			}
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
		WebdavResource res = null;
		try {
			res = this.getResource(path);
			if (res.exists()) {
				size = res.getGetContentLength();
			}
		}
		catch (Exception e) {
			logger.trace(e.getLocalizedMessage());
//			throw new Exception(SOSVfs_E_161.params("checking size", e));
		}
		finally {
			if (res != null) {
				try {
					res.close();
				}
				catch (Exception ex) {
				}
			}
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
	public long getFile(String remoteFile, final String localFile, final boolean append) {
		String sourceLocation = this.resolvePathname(remoteFile);
		File transferFile = null;
		long remoteFileSize = -1;
		File file = null;
		WebdavResource res = null;
		try {
			remoteFile = this.normalizePath(remoteFile);

			//remoteFileSize = this.size(remoteFile);

			// TODO append muss noch berücksichtigt werden
			file = new File(localFile);

			res = this.getResource(remoteFile);
			if (!res.exists()) {
				throw new Exception("remoteFile not found");
			}

			remoteFileSize = res.getGetContentLength();

			if (res.getMethod(sourceLocation, file)) {
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
			else {
				throw new Exception(res.getStatusMessage());
			}
		}
		catch (Exception ex) {
			reply = ex.toString();
			RaiseException(ex, SOSVfs_E_184.params("getFile", sourceLocation, localFile));
		}
		finally {
			try {
				if (res != null) {
					res.close();
				}
			}
			catch (Exception e) {
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
	public long putFile(final String localFile, String remoteFile) {
		long size = 0;
		try {
			remoteFile = this.normalizePath(remoteFile);

			if (davClient.putMethod(remoteFile, new File(localFile))) {
				reply = "put OK";
				logINFO(HostID(SOSVfs_I_183.params("putFile", localFile, remoteFile, getReplyString())));
				return this.size(remoteFile);
			}
			else {
				throw new Exception(getStatusMessage(davClient));
			}
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
	@Override
	public void delete(String path) {
		try {
			path = this.normalizePath(path);

			if (this.isDirectory(path)) {
				throw new JobSchedulerException(SOSVfs_E_186.params(path));
			}

			if (!davClient.deleteMethod(path)) {
				throw new Exception(getStatusMessage(davClient));
			}
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
	public void rename(String from, String to) {
		from = this.resolvePathname(from);
		to = this.resolvePathname(to);
		try {
			from = this.normalizePath(from);
			to = this.normalizePath(to);

			if (!davClient.moveMethod(from, to)) {
				throw new Exception(getStatusMessage(davClient));
			}
		}
		catch (Exception e) {
			reply = e.toString();
			RaiseException(e, SOSVfs_E_188.params("rename", from, to));
		}

		reply = "mv OK";
		logINFO(HostID(SOSVfs_I_189.params(from, to, getReplyString())));
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
	@Override  // SOSVfsTransferBaseClass
	public InputStream getInputStream(String path) {
		try {
			if (!path.startsWith("/")) {
				path = "/" + path;
			}

			return davClient.getMethodData(path);
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
	@Override  // SOSTransferBaseClass
	public OutputStream getOutputStream(final String path) {
		WebdavResource res = null;
//		Das muss in den WebDavFile ausgelagert werden. Andernfalls ist ein paralleles
//		Übertragen nicht möglich
		try {
			res = this.getResource(path, false);
//			res.lockMethod(path, owner, timeout, lockType, depth):
//			res.unlockMethod(path, owner);
			return new SOSVfsWebDAVOutputStream(res);
		}
		catch (Exception ex) {
			RaiseException(ex, SOSVfs_E_193.params("getOutputStream()", path));
			return null;
		}
		finally {
			try {
				if (res != null) {
					res.close();
				}
			}
			catch (Exception e) {
			}
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
	public boolean changeWorkingDirectory(String path) {
		try {
			String origPath = davClient.getPath();

			path = this.normalizePath(path+"/");

			davClient.setPath(path);
			if (davClient.exists()) {
				reply = "cwd OK";
				logger.debug(SOSVfs_D_194.params(path, getReplyString()));
			}
			else {
				davClient.setPath(origPath);
				reply = "cwd failed";
				logger.debug(SOSVfs_D_194.params(path, getReplyString()));
				return false;
			}
		}
		catch (Exception ex) {
			RaiseException(ex, SOSVfs_E_193.params("cwd", path));
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
		ISOSVirtualFile file = new SOSVfsWebDAVFile(fileName);
//		OutputStream os = getOutputStream(fileName);
		file.setHandler(this);

		//logger.debug(SOSVfs_D_196.params(fileName));

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
		WebdavResource res = null;
		String dateTime = null;
		try {
			res = this.getResource(path);
			if (res.exists()) {
				long lm = res.getGetLastModified();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dateTime = df.format(new Date(lm));
			}
		}
		catch (Exception ex) {

		}
		finally {
			if (res != null) {
				try {
					res.close();
				}
				catch (Exception e) {
				}
			}
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

		WebdavResource res = null;
		try {
			res = this.getResource(filename);

			return res.exists();
		}
		catch (Exception e) {
			return false;
		}
		finally {
			try {
				if (res != null) {
					res.close();
				}
			}
			catch (Exception e) {
			}
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
		String path = null;

		try {
			path = davClient.getPath();
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
	 * \brief getResource
	 *
	 * \details
	 *
	 * \return WebdavResource
	 *
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private WebdavResource getResource(final String path) throws Exception {
		return getResource(path, true);
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private WebdavResource getWebdavResource(final HttpURL url) throws Exception {
		WebdavResource res = null;
		if(SOSString.isEmpty(proxyHost)) {
			res = new WebdavResource(url);
		}
		else {
			res = new WebdavResource(url,proxyHost, proxyPort,new UsernamePasswordCredentials(proxyUser, proxyPassword));
		}
//		if(!isSuccessStatusCode(res.getStatusCode())){
//			throw new Exception(getStatusMessage(res));
//		}
		return res;
	}

	/**
	 * 
	 * @param statusCode
	 * @return
	 */
	private boolean isSuccessStatusCode(int statusCode){
		// siehe HTTP StatusCode unter http://www.elektronik-kompendium.de/sites/net/0902231.htm
		/**
		if(statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_NOT_FOUND){
			return true;
		}*/
		if(statusCode == HttpStatus.SC_OK){
			return true;
		}
		
		return false;
	}

	/**
	 * 
	 * @param path
	 * @param flgTryWithTrailingSlash
	 * @return
	 * @throws Exception
	 */
	private WebdavResource getResource(String path, final boolean flgTryWithTrailingSlash) throws Exception {
		path = this.normalizePath(path);
		WebdavResource res = getWebdavResource(getWebdavRessourceURL(path));
		//path of a folder must have a trailing slash otherwise it does not exist
		if(flgTryWithTrailingSlash == true && path.endsWith("/") == false && res.exists() == false) {
			//try with trailing slash
			WebdavResource res2 = null;
			try {
				res2 = getWebdavResource(getWebdavRessourceURL(path+"/"));
				if(res2.exists() == true) {
					res = res2;
				}
			}
			catch (Exception e) {}
			finally {
				try {
					if(res2 != null) {
						res2.close();
					}
				}
				catch (Exception e) {}
			}
		}
		return res;
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
	private String normalizePath(String path) {
		if(path.startsWith("/") == false) {
			path = "/" + path;
		}
		path = path.replaceAll("//+", "/");
		return path;
	}

	/**
	 *
	 * \brief normalizeRootHttpURL
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @param url
	 * @return String
	 */
	private String normalizeRootHttpURL(String url, final int port) {

		@SuppressWarnings("unused")
		final String	conMethodName	= conClassName + "::normalizeRootHttpURL";

		if(url.endsWith("/") == false) {
			url += "/";
		}

		// insert port into url
		if (connection2OptionsAlternate.auth_method.isURL()) {
			//--------------------- (schema://)(user:passw@)?(host)(:port)?(path) ----------
			url = url.replaceFirst("^(https?://)([^/@]+@)?([^/:]+)(:[^/]+)?(.*)$", "$1$2$3:"+port+"$5");
		}

		return url;
	} // private String normalizeRootHttpURL

	/**
	 *
	 * \brief setRootHttpURL
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param puser
	 * @param ppassword
	 * @param phost
	 * @param pport
	 * @return HttpURL
	 * @throws Exception
	 */
	private HttpURL setRootHttpURL(final String puser, final String ppassword, final String phost, final int pport) throws Exception {

		rootUrl = null;
		HttpURL httpUrl = null;
		String path = "/";
		String normalizedHost = normalizeRootHttpURL(phost, pport);

		if (connection2OptionsAlternate.auth_method.isURL()) {
			if (phost.toLowerCase().startsWith("https://")) {
				httpUrl = new HttpsURL(normalizedHost);
			}
			else {
				httpUrl = new HttpURL(normalizedHost);
			}

			String phostRootUrl = httpUrl.getScheme() + "://" + httpUrl.getAuthority() + "/";
			if (httpUrl.getScheme().equalsIgnoreCase("https")) {
				rootUrl = new HttpsURL(phostRootUrl);
			
				StrictSSLProtocolSocketFactory psf = new StrictSSLProtocolSocketFactory();
				//psf.setCheckCRL and psf.setCheckExpiry sind bei StrictSSL.. per default true
				psf.setCheckHostname(true);
				if(connection2OptionsAlternate.accept_untrusted_certificate.value()){
					//see apache ssl EasySSLProtocolSocketFactory implementation
					psf.useDefaultJavaCiphers();
					psf.addTrustMaterial(TrustMaterial.TRUST_ALL);
				}
				Protocol p = new Protocol("https", (ProtocolSocketFactory)psf, pport);
				Protocol.registerProtocol("https",p);
			}
			else {
				rootUrl = new HttpURL(phostRootUrl);
			}
		}
		else {
			httpUrl = new HttpURL(phost, pport, path);
			rootUrl = new HttpURL(phost, pport, path);
		}
		
		if(!SOSString.isEmpty(puser)){
			httpUrl.setUserinfo(puser, ppassword);
		}
		return httpUrl;
	}

	private HttpURL getWebdavRessourceURL(String path) throws Exception {
		HttpURL url = null;

		if(rootUrl != null) {
			path = path.startsWith("/") ? path.substring(1) : path;
			if(rootUrl.getScheme().equalsIgnoreCase("https")) {
				url = new HttpsURL(rootUrl.getEscapedURI()+path);
			}
			else {
				url = new HttpURL(rootUrl.getEscapedURI()+path);
			}
		}
		url.setUserinfo(userName,password);

		return url;
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private HttpURL getWebdavRessourceURL(HttpURL url) throws Exception {
		url.setUserinfo(userName,password);
		return url;
	}
	
	/**
	 * 
	 * @param options
	 * @return
	 * @throws Exception
	 */
	private ISOSConnection doAuthenticate(final ISOSAuthenticationOptions options) throws Exception {
		String method = "doAuthenticate";
		authenticationOptions = options;

		userName = authenticationOptions.getUser().Value();
		password = authenticationOptions.getPassword().Value();

		logger.debug(SOSVfs_D_132.params(userName));

		HttpURL httpUrl = this.setRootHttpURL(userName, password, host, port);

		try {
			if(!SOSString.isEmpty(proxyHost)) {
				logger.info(String.format("using proxy: %s:%s, proxy user = %s",
						proxyHost,
						proxyPort,
						proxyUser));
			}
			davClient = getWebdavResource(getWebdavRessourceURL(httpUrl));
			if (!davClient.exists()) {
				throw new JobSchedulerException(String.format("%s: HTTP-DAV isn't enabled %s ",
						method,
						getStatusMessage(davClient)));
			}
		}
		catch (Exception ex) {
			throw new JobSchedulerException(SOSVfs_E_167.params(authenticationOptions.getAuth_method().Value(), authenticationOptions.getAuth_file().Value()), ex);
		}

		reply = "OK";
		logger.info(SOSVfs_D_133.params(userName));
		this.LogReply();

		return this;
	}
	
	/**
	 * 
	 * @param client
	 * @return
	 */
	private String getStatusMessage(WebdavResource client){
		String msg = client.getStatusMessage();
		String uri = "";
		try{
			uri = client.getHttpURL().getURI();
		}
		catch(Exception ex){
			uri = String.format("unknown uri = %s",ex.toString());
		}
		if(SOSString.isEmpty(msg)){
			msg = "no details provided.";
			if(uri.toLowerCase().startsWith("https://") && client.getStatusCode() == 0){
				//TODO use DefaultHttpMethodRetryHandler und HttpClient beim Initialisieren von WebdabResource
				//Beispiel
				//DefaultHttpMethodRetryHandler rh = new DefaultHttpMethodRetryHandler(1,true);
				//client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,rh);
				if(!connection2OptionsAlternate.accept_untrusted_certificate.value()){
					msg+=" maybe is this the problem by using of a self-signed certificate (option accept_untrusted_certificate = false)";
				}
			}
			
		}
		
		String proxy = "";
		if(!SOSString.isEmpty(proxyHost)) {
			proxy = String.format("[proxy %s:%s, proxy user = %s]",
					proxyHost,
					proxyPort,
					proxyUser);
		}
		
		return String.format("[%s]%s %s",
				uri,
				proxy,
				msg);
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

		// TODO Meldung auch ohne Port oder Port mit ?? ausgeben.
		//String msgPort = connection2OptionsAlternate.auth_method.isURL() ? "??" : "" + port;

		logger.info(SOSVfs_D_0101.params(host, port));

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

    @Override
    public SOSFileEntries getSOSFileEntries() {
        return sosFileEntries;
    }

}
