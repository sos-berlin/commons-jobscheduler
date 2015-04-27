package com.sos.VirtualFileSystem.FTPS;

import java.io.File;
import java.net.ProxySelector;
import java.security.KeyStore;

import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.log4j.Logger;

import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol.Protocol;
import com.sos.VirtualFileSystem.FTP.SOSFtpClientLogger;
import com.sos.VirtualFileSystem.FTP.SOSVfsFtpBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpS extends SOSVfsFtpBaseClass {
	@SuppressWarnings("unused")
	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	private final Logger		logger			= Logger.getLogger(this.getClass());
	private FTPSClient			client	= null;

	/**
	 *
	 * \brief SOSVfsFtpS
	 *
	 * \details
	 *
	 */
	public SOSVfsFtpS() {
	}

	public FTPSClient getClient() {
		return Client();
	}

	@Override
	protected FTPSClient Client() {
		if (client == null) {
			try {
				logger.info(String.format("use %s client security",
						objConnection2Options.ftps_client_secutity.Value()));
				
				client = new FTPSClient(objConnection2Options.FtpS_protocol.Value(),objConnection2Options.ftps_client_secutity.isImplicit());
				if(usingProxy()){
					logger.info(String.format("using proxy: protocol = %s, host = %s, port = %s, user = %s, pass = ?",
							getProxyProtocol().Value(),
							getProxyHost(),
							getProxyPort(),
							getProxyUser()));
					
					if(usingHttpProxy()){
						logger.info(String.format("ftps via http proxy is experimental and not tested ..."));
						client.setProxy(getHTTPProxy());
					}
					else{
						//client.setProxy(getSocksProxy());
						SOSOptionProxyProtocol.Protocol proxyProtocol = getProxyProtocol().isSocks4() ? Protocol.socks4 : Protocol.socks5;
						SOSVfsFtpSProxySelector ps = new SOSVfsFtpSProxySelector(proxyProtocol,
								getProxyHost(),
								getProxyPort(),
								getProxyUser(),
								getProxyPassword());
							ProxySelector.setDefault(ps);
					}
				}
				
				if(!SOSString.isEmpty(objConnection2Options.keystore_file.Value())){
					setTrustManager(client);
				}
				
			}
			catch (Exception e) {
				throw new JobSchedulerException("can not create FTPS-Client", e);
			}
			
			//FTPClientConfig conf = new FTPClientConfig();
			//			conf.setServerLanguageCode("fr");
			//			objFTPClient.configure(conf);
			/**
			 * This listener is to write all commands and response from commands to system.out
			 *
			 */
			objProtocolCommandListener = new SOSFtpClientLogger(HostID(""));
			// TODO create a hidden debug-option to activate this listener
			if (objConnection2Options != null) {
				if (objConnection2Options.ProtocolCommandListener.isTrue()) {
					client.addProtocolCommandListener(objProtocolCommandListener);
				}
			}

			String addFTPProtocol = System.getenv("AddFTPProtocol");
			if (addFTPProtocol != null && addFTPProtocol.equalsIgnoreCase("true")) {
				client.addProtocolCommandListener(objProtocolCommandListener);
			}

		}
		return client;
	}

	@Override
	public void connect(final String phost, final int pport) {
		try {
			if (isConnected() == false) {
				super.connect(phost, pport);

				/**
				 * PBSZ (protection buffer size) command, as detailed in [RFC-2228],
				 * is compulsory prior to any PROT command.
				 */
				Client().execPBSZ(0);
				LogReply();
				Client().execPROT("P"); // Secure Data channel, see http://www.faqs.org/rfcs/rfc2228.html
				LogReply();
				Client().enterLocalPassiveMode();
			}
			else {
				logger.warn(SOSVfs_D_0102.params(host, port));
			}
		}
		catch (Exception e) {
			String msg = HostID("connect returns an exception");
			logger.error(msg, e);
		}
	}
	
	/**
	 * 
	 * @param client
	 * @throws Exception
	 */
	private void setTrustManager(FTPSClient client) throws Exception{
		logger.info(String.format("using keystore: type = %s, file = %s",
				objConnection2Options.keystore_type.Value(),
				objConnection2Options.keystore_file.Value()));
		
		KeyStore ks = loadKeyStore(objConnection2Options.keystore_type.Value(),
				new File(objConnection2Options.keystore_file.Value()), 
				objConnection2Options.keystore_password.Value());
		
		client.setTrustManager(TrustManagerUtils.getDefaultTrustManager(ks));
	}
}
