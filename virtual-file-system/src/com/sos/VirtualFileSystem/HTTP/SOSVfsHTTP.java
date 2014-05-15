package com.sos.VirtualFileSystem.HTTP;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.log4j.Logger;

import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSConnection;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsSuperClass;
import com.sos.VirtualFileSystem.common.SOSVfsTransferBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * @ressources httpclient-4.2.1.jar,httpcore-4.2.1.jar
 *
 * @author Robert Ehrlich
 *
 */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsHTTP extends SOSVfsTransferBaseClass {

	private final Logger				logger		= Logger.getLogger(SOSVfsHTTP.class);
	private DefaultHttpClient 	httpClient;
	private HttpHost 			targetHost;

	private final boolean  isHTTPS = false;

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
	public void login(final String user, final String password) {


		try {

			userName = user;

			logger.debug(SOSVfs_D_132.params(userName));

			this.doLogin(userName, password);

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

		if (httpClient != null) {
			try {
				httpClient.getConnectionManager().shutdown();
			}
			catch (Exception ex) {
				reply = "disconnect: " + ex;
			}
			httpClient = null;
		}

		targetHost = null;

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
		return httpClient != null && httpClient.getConnectionManager() != null;
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

		FileOutputStream outputStream = null;
		long fileSize = -1;
		try {

			HttpGet request 		= new HttpGet(remoteFile);
			HttpResponse response 	= httpClient.execute(targetHost, request);
			StatusLine status 		= response.getStatusLine();

			if (status.getStatusCode() < 200 || status.getStatusCode() >= 300) {
				throw new Exception("HTTP Error : "+ status.toString());
			}

			HttpEntity entity = response.getEntity();

			if(entity != null) {
				File local = new File(localFile);

				outputStream = new FileOutputStream(local,append);

				byte buffer[] = new byte[1000];
				int  numOfBytes = 0;

				InputStream responseStream = entity.getContent();
				while ( (numOfBytes = responseStream.read(buffer)) != -1 ) {
					outputStream.write(buffer, 0, numOfBytes);
				}

				fileSize = local.length();
			}
			else {
				throw new JobSchedulerException("HTTP Response is empty (HttpEntity is NULL)");
			}

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
			catch (Exception e) {}
		}

		return fileSize;
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
	public InputStream getInputStream(final String path) {
		try {

			HttpGet request 		= new HttpGet(path);
			HttpResponse response 	= httpClient.execute(targetHost, request);
			StatusLine status 		= response.getStatusLine();

			if (status.getStatusCode() < 200 || status.getStatusCode() >= 300) {
				throw new Exception("HTTP Error : "+ status.toString());
			}

			HttpEntity entity = response.getEntity();

			if(entity != null) {
				return entity.getContent();
			}
			else {
				throw new Exception("HTTP Response is empty (HttpEntity is NULL)");
			}
		}
		catch (Exception ex) {
			RaiseException(ex, SOSVfs_E_193.params("getInputStream()", path));
		}
		return null;
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
		try {

			HttpGet request 		= new HttpGet(path);
			HttpResponse response 	= httpClient.execute(targetHost, request);
			StatusLine status 		= response.getStatusLine();

			// siehe HTTP StatusCode unter http://www.elektronik-kompendium.de/sites/net/0902231.htm
			if (status.getStatusCode() >= 200 && status.getStatusCode() < 404) {
				return true;
			}
		}
		catch (Exception ex) {}

		return false;
	}

	/**
	 *
	 * @param user
	 * @param password
	 * @throws Exception
	 */
	private void doLogin(final String user, final String password) throws Exception{

		if(!SOSString.isEmpty(user)) { //theoretisch password kann auch leer sein
			httpClient.getCredentialsProvider().
							setCredentials( new AuthScope(
									targetHost.getHostName(),
									targetHost.getPort()),
									new UsernamePasswordCredentials(user,password));

			HttpResponse response = httpClient.execute(targetHost,new HttpGet("/"));
			StatusLine status	= response.getStatusLine();

			if (status.getStatusCode() < 200 || status.getStatusCode() >= 300) {
				throw new Exception("HTTP Error : "+ status.toString());
			}
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

		userName = authenticationOptions.getUser().Value();
		String password = authenticationOptions.getPassword().Value();
		logger.debug(SOSVfs_D_132.params(userName));

		if(httpClient == null) {
			throw new Exception("HTTP Client ist not initialized (NULL)");
		}
		if(targetHost == null) {
			throw new Exception("Target Host ist not initialized (NULL)");
		}

		try {
			this.doLogin(userName,password);
		}
		catch (Exception ex) {
			throw new JobSchedulerException(SOSVfs_E_167.params(authenticationOptions.getAuth_method().Value(), authenticationOptions.getAuth_file()
					.Value()));
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

		String protocol;
		SchemeSocketFactory scheme;


		if (this.isConnected() == false) {

			if(isHTTPS) {//default port 443
				protocol = "https";
				scheme = new EasySSLSocketFactory();
			}
			else {//default port 80
				protocol = "http";
				scheme = PlainSocketFactory.getSocketFactory();
			}


			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme(protocol, port,scheme));

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setUseExpectContinue(params, false);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

			ClientConnectionManager cm = new BasicClientConnectionManager(schemeRegistry);
			httpClient = new DefaultHttpClient(cm, params);

			try {
				targetHost = new HttpHost(host,port,protocol);
				httpClient.execute(targetHost,new HttpGet("/"));

				this.LogReply();
			}
			catch(Exception ex) {
				logERROR(ex);
			}
		}
		else {
			logWARN(SOSVfs_D_0103.params(host, port));
		}
	}

	@Override
	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getOutputStream(final String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

}
