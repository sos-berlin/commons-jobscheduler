package com.sos.VirtualFileSystem.FTPS;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.FTP.SOSFtpClientLogger;
import com.sos.VirtualFileSystem.FTP.SOSVfsFtpBaseClass2;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpS2 extends SOSVfsFtpBaseClass2 {
	@SuppressWarnings("unused")
	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	private final Logger		logger			= Logger.getLogger(this.getClass());
	private FTPSClient			objFTPClient	= null;

	/**
	 *
	 * \brief SOSVfsFtpS
	 *
	 * \details
	 *
	 */
	public SOSVfsFtpS2() {
	}

	public FTPSClient getClient() {
		return Client();
	}

	@Override
	protected FTPSClient Client() {
		if (objFTPClient == null) {
			try {
				String strProtocol = objConnection2Options.FtpS_protocol.Value();
				objFTPClient = new FTPSClient(strProtocol);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new JobSchedulerException("can not create FTPS-Client");
			}
			FTPClientConfig conf = new FTPClientConfig();
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
					objFTPClient.addProtocolCommandListener(objProtocolCommandListener);
				}
			}

			String strAddFTPProtocol = System.getenv("AddFTPProtocol");
			if (strAddFTPProtocol != null && strAddFTPProtocol.equalsIgnoreCase("true")) {
				objFTPClient.addProtocolCommandListener(objProtocolCommandListener);
			}

		}
		return objFTPClient;
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
			String strM = HostID("connect returns an exception");
			e.printStackTrace(System.err);
			logger.error(strM, e);
		}
	}
}
