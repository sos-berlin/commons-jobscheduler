package com.sos.VirtualFileSystem.HTTP;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;

import sos.util.SOSString;

import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsSuperClass;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.VirtualFileSystem.exceptions.JADEException;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 *
 * @author Robert Ehrlich
 *
 */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsHTTP extends SOSVfsTransferBaseClass {

	private final Logger				logger		= Logger.getLogger(SOSVfsHTTP.class);
	
	private MultiThreadedHttpConnectionManager connectionManager;
	private HttpClient  		httpClient;
	private HttpURL				rootUrl			= null;
	
	/**
	 *
	 */
	public SOSVfsHTTP() {
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
	public ISOSConnection Connect(final SOSConnection2OptionsAlternate pConnection2OptionsAlternate){
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
			this.doAuthenticate(authenticationOptions);
		}
		catch (Exception ex) {
			Exception exx = ex;

			this.disconnect();

			if (connection2OptionsAlternate != null) {
				SOSConnection2OptionsSuperClass optionsAlternatives = connection2OptionsAlternate.Alternatives();
				if (!optionsAlternatives.host.IsEmpty() && !optionsAlternatives.user.IsEmpty()) {
					logger.info(SOSVfs_I_170.params(connection2OptionsAlternate.Alternatives().host.Value()));
					try {
						this.connect(optionsAlternatives.host.Value(), 
								optionsAlternatives.port.value());
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
	public void login(final String user, final String password) {


		try {
			this.doLogin(user, password);

			reply = "OK";
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

		if (this.connectionManager != null) {
			try {
				this.connectionManager.shutdown();
			}
			catch (Exception ex) {
				reply = "disconnect: " + ex;
			}
			this.connectionManager = null;
			this.httpClient = null;
		}
		logger.info(reply);
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
		//return httpClient != null && httpClient.getConnectionManager() != null;
		return httpClient != null && connectionManager != null;
	}


	private String normalizeHttpPath(String path){
		if(!path.toLowerCase().startsWith("http://") && !path.toLowerCase().startsWith("https://")){
			if(!path.startsWith("/")){
				path = "/"+path; 
			}
			path = this.httpClient.getHostConfiguration().getHostURL()+path;
		}
		
		return path;
	}
	
	@Override
	public long getFile(final String remoteFile, final String localFile, final boolean append) {
		long fileSize = -1;
		FileOutputStream outputStream = null;
		
		try {
			InputStream responseStream = getInputStream(remoteFile);
			File local = new File(localFile);

			outputStream = new FileOutputStream(local,append);
			byte buffer[] = new byte[1000];
			int  numOfBytes = 0;

			while ( (numOfBytes = responseStream.read(buffer)) != -1 ) {
				outputStream.write(buffer, 0, numOfBytes);
			}

			fileSize = local.length();
			
			reply = "get OK";
			logger.info(HostID(SOSVfs_I_182.params("getFile", remoteFile, localFile, getReplyString())));
		}
		catch (Exception ex) {
			reply = ex.toString();
			RaiseException(ex, SOSVfs_E_184.params("getFile", remoteFile, localFile));
		}
		finally {
			try {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
				}
			}
			catch (Exception e) {
			}
		}
		return fileSize;
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
		ISOSVirtualFile file = new SOSVfsHTTPFile(fileName);
		file.setHandler(this);

		logger.debug(SOSVfs_D_196.params(fileName));

		return file;
	}

	/**
	 *
	 */
	@Override
	protected boolean fileExists(final String path) {
		GetMethod method = new GetMethod(normalizeHttpPath(path));
		try {
			return isSuccessStatusCode(executeHttpMethod(method));
		}
		catch (Exception ex) {}
		finally{
			try{
				method.releaseConnection();
			}
			catch(Exception ex){}
		}
		return false;
	}

	/**
	 *
	 * @param user
	 * @param password
	 * @throws Exception
	 */
	private void doLogin(final String user, final String password) throws Exception{
		this.userName = user;
		
		logger.debug(SOSVfs_D_132.params(userName));
		
		if(!SOSString.isEmpty(userName)){
			Credentials credentials = new UsernamePasswordCredentials(this.userName, password);
			this.httpClient.getState().setCredentials(AuthScope.ANY, credentials);
		}
		
		executeHttpMethod(new GetMethod());
	
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
		
		this.doLogin(authenticationOptions.getUser().Value(), 
				authenticationOptions.getPassword().Value());
		
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
	 * @throws URIException 
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 */
	private void connect(final String phost, final int pport){
	
		if (this.isConnected() == false) {
			try{
				this.port = pport;
				HostConfiguration hc = new HostConfiguration();
				
				if(phost.toLowerCase().startsWith("https://")){
					this.rootUrl 	= new HttpsURL(phost);
					this.host 	= this.rootUrl.getHost();
					if(this.port > 0){
						//Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), this.port));
						hc.setHost(this.host,this.port, new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), this.port));
					}
					else{
						hc.setHost(new HttpHost(host,port));
					}
				}
				else{
					this.rootUrl 	= new HttpURL(phost.toLowerCase().startsWith("http://") ? phost : "http://"+phost);
					this.host 		= this.rootUrl.getHost();
					hc.setHost(new HttpHost(host,port));
				}
				
				logger.debug(SOSVfs_D_0101.params(host, port));
				
				connectionManager = new MultiThreadedHttpConnectionManager();
				httpClient = new HttpClient(connectionManager);
				httpClient.setHostConfiguration(hc);
				
				this.LogReply();
			}
			catch(Exception ex){
				throw new JADEException(ex);
			}
		}
		else {
			logWARN(SOSVfs_D_0103.params(host, port));
		}
	}
	
	private int executeHttpMethod(HttpMethod method) throws Exception{
		try{
			this.httpClient.executeMethod(method);
			
			if(!isSuccessStatusCode(method.getStatusCode())){
				throw new Exception(String.format("HTTP [%s][%s] = %s",
						method.getURI(),
						method.getStatusCode(),
						method.getStatusText()));
			}
			return method.getStatusCode();
		}
		catch(Exception ex){
			throw ex;
		}
		finally{
			try{
				method.releaseConnection();
			}
			catch(Exception ex){};
		}
	}
	
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

	@Override
	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @TODO es fehlt method.releaseConnection();
	 * 
	 * @param fileName
	 * @return
	 */
	@Override public InputStream getInputStream(final String fileName) {
		try {
			GetMethod method = new GetMethod(normalizeHttpPath(fileName));
			this.httpClient.executeMethod(method);
				
			if(!isSuccessStatusCode(method.getStatusCode())){
				throw new Exception(String.format("HTTP [%s][%s] = %s",
						method.getURI(),
						method.getStatusCode(),
						method.getStatusText()));
			}
		  return method.getResponseBodyAsStream();
		}
		catch (Exception ex) {
			RaiseException(ex, SOSVfs_E_193.params("getInputStream()", fileName));
			return null;
		}
	}

	@Override
	public OutputStream getOutputStream(final String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

}
