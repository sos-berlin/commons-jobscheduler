package com.sos.VirtualFileSystem.HTTP;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;

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
//import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;

import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
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
	//fileSize wird von Jade mehr mals ermittelt. 
	//Das ist ein Workaround um die mehreren Aufrufe zu vermeiden
	//Dei HTTP sollte das keine Probleme verursachen, da nicht so viele? Dateien auf ein mal übertragen werden
	private HashMap<String,Long> fileSizes = null;
	
	private String			proxyHost		= null;
	private int				proxyPort		= 0;
	private String			proxyUser		= null;
	private String			proxyPassword	= null;

	
	/**
	 *
	 */
	public SOSVfsHTTP() {
		super();
		this.fileSizes = new HashMap<String,Long>();
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

		proxyHost = connection2OptionsAlternate.proxy_host.Value();
		proxyPort = connection2OptionsAlternate.proxy_port.value();
		proxyUser = connection2OptionsAlternate.proxy_user.Value();
		proxyPassword = connection2OptionsAlternate.proxy_password.Value();
				
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
				SOSConnection2OptionsAlternate optionsAlternatives = connection2OptionsAlternate.Alternatives();
				if (optionsAlternatives.optionsHaveMinRequirements()) {
					logger.warn(ex);
					logINFO(SOSVfs_I_170.params(optionsAlternatives.host.Value()));
					JobSchedulerException.LastErrorMessage = "";
					try {
						proxyHost = optionsAlternatives.proxy_host.Value();
						proxyPort = optionsAlternatives.proxy_port.value();
						proxyUser = optionsAlternatives.proxy_user.Value();
						proxyPassword = optionsAlternatives.proxy_password.Value();
						optionsAlternatives.AlternateOptionsUsed.value(true);
						this.connect(optionsAlternatives.host.Value(), 
								optionsAlternatives.port.value());
						this.doAuthenticate(optionsAlternatives);
						exx = null;
					}
					catch (Exception e) {
						logger.error(e);
						exx = e;
					}
				}
			}
			if (exx != null) {
				throw new JobSchedulerException(exx);
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
		
		this.fileSizes = new HashMap<String,Long>();
		
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
			
			this.httpClient.executeMethod(method);
			
			return isSuccessStatusCode(method.getStatusCode());
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
		
		this.checkConnection();
	}

	private void checkConnection() throws Exception{
		GetMethod method = new GetMethod(this.rootUrl.getURI());
		try {
			
			this.httpClient.executeMethod(method);
		}
		catch (Exception ex) {
			throw new Exception(this.getHttpMethodExceptionText(method, ex));
		}
		finally{
			try{
				method.releaseConnection();
			}
			catch(Exception ex){}
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
						//mit self signed zertifikaten
						Protocol p = new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), this.port);
						Protocol.registerProtocol("https",p);
						hc.setHost(this.host,this.port,p);
						//ohne self signed
						//hc.setHost(this.host,this.port, new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), this.port));
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

				//connectionManager.getConnection(httpClient.getHostConfiguration()).open();
				
				this.setProxyCredentionals();
				
				this.LogReply();
			}
			catch(Exception ex){
				throw new JobSchedulerException(ex);
			}
		}
		else {
			logWARN(SOSVfs_D_0103.params(host, port));
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private void setProxyCredentionals() throws Exception{
		if(!SOSString.isEmpty(this.proxyHost)){
			logger.info(String.format("using proxy: host = %s, port = %s, user = %s, pass = ?",this.proxyHost,this.proxyPort,this.proxyUser));
			
			httpClient.getHostConfiguration().setProxy(this.proxyHost,this.proxyPort);
            
			Credentials credentials = new UsernamePasswordCredentials(this.proxyUser, this.proxyPassword);
			AuthScope authScope = new AuthScope(this.proxyHost,this.proxyPort);
			 
			this.httpClient.getState().setProxyCredentials(authScope, credentials);
		}
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

	@Override
	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	public static byte[] getBytesX(InputStream is) throws IOException {

	    int len;
	    int size = 1024;
	    byte[] buf;

	    if (is instanceof ByteArrayInputStream) {
	      size = is.available();
	      buf = new byte[size];
	      len = is.read(buf, 0, size);
	    } else {
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      buf = new byte[size];
	      while ((len = is.read(buf, 0, size)) != -1)
	        bos.write(buf, 0, len);
	      buf = bos.toByteArray();
	    }
	    return buf;
	  }

	public static Long getInputStreamLen(InputStream is) throws IOException {

		long total = 0;
		try{
			int intBytesTransferred = 0;
			byte[] buffer = new byte[1024];
			while ((intBytesTransferred = is.read(buffer)) != -1) {
				total += intBytesTransferred;
			}
		}
		catch(Exception ex){
			throw ex;
		}
		finally{
			try{ is.close();}catch (Exception ex){}
		}
		
		return new Long(total);
	}
	
	@Override 
	public long size(final String path) throws Exception {
		
		if(this.fileSizes.containsKey(path)){
			return this.fileSizes.get(path);
		}
		
		Long size = new Long(-1);
		GetMethod method = new GetMethod(normalizeHttpPath(path));
		try{
			this.httpClient.executeMethod(method);
			
			if(!isSuccessStatusCode(method.getStatusCode())){
				throw new Exception(this.getHttpMethodExceptionText(method));
			}
			size = method.getResponseContentLength(); 
			if(size < 0){
				//size = new Long(getBytes(method.getResponseBodyAsStream()).length);
				size = getInputStreamLen(method.getResponseBodyAsStream());
			}
			
			this.fileSizes.put(path,size);
		}
		catch(Exception ex){
			throw ex;
			//throw new Exception(this.getHttpMethodExceptionText(method,ex));
		}
		finally{
			try{
				method.releaseConnection();
			}
			catch(Exception ex){}
		}
		
	  return size;
	}
	
	
	/**
	@Override 
	public long size(final String path) throws Exception {
	  return new Long(1);
	}*/
	
	@Override public boolean changeWorkingDirectory(String path) {
		return true;
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
		if (path.length() == 0) {
			path = "/";
		}
		
		/**
		if (!this.fileExists(path)) {
			return null;
		}*/
		reply = "ls OK";
		return new String[] { path };
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
				throw new Exception(this.getHttpMethodExceptionText(method));
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
	
	/**
	 * 
	 * @param method
	 * @return
	 * @throws Exception
	 */
	private String getHttpMethodExceptionText(HttpMethod method) throws Exception{
		return this.getHttpMethodExceptionText(method,null);
	}
	
	/**
	 * 
	 * @param method
	 * @param ex
	 * @return
	 * @throws Exception
	 */
	private String getHttpMethodExceptionText(HttpMethod method, Exception ex) throws Exception{
		String uri = this.getMethodUri(method);
		int code = this.getMethodStatusCode(method);
		String text = this.getMethodStatusText(method);
		if(ex == null){
			return String.format("HTTP [%s][%s] = %s",
					uri,
					code,
					text);
		}
		else{
			return String.format("HTTP [%s][%s][%s] = %s",
					uri,
					code,
					text,
					ex);
		}
	}
	
	/**
	 * 
	 * @param method
	 * @return
	 */
	private int getMethodStatusCode(HttpMethod method){
		int val = -1;
		
		try{
			val = method.getStatusCode();
		}
		catch(Exception ex){}
		
		return val;
	}
	
	/**
	 * 
	 * @param method
	 * @return
	 */
	private String getMethodStatusText(HttpMethod method){
		String val = "";
		
		try{
			val = method.getStatusText();
		}
		catch(Exception ex){}
		
		return val;
	}
	
	/**
	 * 
	 * @param method
	 * @return
	 */
	private String getMethodUri(HttpMethod method){
		String val = "";
		
		try{
			val = method.getURI().getURI();
		}
		catch(Exception ex){}
		
		return val;
	}
	

}
