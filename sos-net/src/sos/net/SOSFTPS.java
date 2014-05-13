package sos.net;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 * <p>Title: SOSFTPS-Client</p>
 * <p>Description: This class adds implicit SSL/TLS (FTP over SSL/TLS) support to the 
 *  org.apache.commons.net.ftp.FTPClient without changes.</p>
 * <p> HTTP Proxy support is also added. 
 * </p>  
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: SOS GmbH</p>
 * @author <a href="mailto:ghassan.beydoun@sos-berlin.com">Ghassan Beydoun</a>
 * @version $Id$
 */
public class SOSFTPS extends SOSFTP {
	/** default security protocol SSL/TLS implicit */
	protected static final String	DEFAULT_SSL_TLS_PROTOCOL	= "TLS";
//	protected static final String	DEFAULT_SSL_TLS_PROTOCOL	= "SSL";
	private String					securityProtocol			= DEFAULT_SSL_TLS_PROTOCOL;
	private int						ftpPort						= 990;
	private String					proxyHost;
	private int						proxyPort;

	public String getSecurityProtocol() {
		return securityProtocol;
	}

	public void setSecurityProtocol(String securityProtocol1) {
		this.securityProtocol = securityProtocol1;
	}

	public SOSFTPS(String ftpHost, int ftpPort1, String proxyHost1, int proxyPort1) throws SocketException, IOException, UnknownHostException {
		this.setProxyHost(proxyHost1);
		this.setProxyPort(proxyPort1);
		this.connect(ftpHost, ftpPort1);
	}

	public SOSFTPS(String ftpHost, int ftpPort1) throws SocketException, IOException, UnknownHostException {
		this.initProxy();
		this.connect(ftpHost, ftpPort1);
	}

	/*
	 */
	public SOSFTPS(String ftpHost) throws Exception {
		if (!isConnected())
			this.connect(ftpHost, this.getPort());
	}

	public void connect(String ftpHost) throws SocketException, IOException {
		if (!isConnected())
			this.connect(ftpHost, this.getPort());
	}

	public void connect(String ftpHost, int ftpPort1) throws SocketException, IOException {
		initProxy();
		if (isConnected() == false) {
			this.setSocketFactory(new SOSSSLSocketFactory(getProxyHost(), getProxyPort(), getSecurityProtocol()));
//			this.setSocketFactory(new SOSSSLSocketFactory());
			try {
				super.connect(ftpHost, ftpPort1);
			}
			catch (NullPointerException e) {
				throw new SocketException("Connect failed! Probably HTTP proxy in use or the entered ftps port is invalid: " + e.toString());
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new SocketException("Connect failed, reason: " + e.toString());
			}
			this.sendCommand("PBSZ 0");
			this.sendCommand("PROT P");
			this.enterLocalPassiveMode();
		}
	}

	public int getPort() {
		return ftpPort;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String httpProxyHost) {
		this.proxyHost = httpProxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int httpProxyPort) {
		this.proxyPort = httpProxyPort;
	}

	private void initProxy() {
		if (System.getProperty("proxyHost") != null && System.getProperty("proxyHost").length() > 0)
			this.setProxyHost(System.getProperty("proxyHost"));
		if (System.getProperty("proxyPort") != null && System.getProperty("proxyPort").length() > 0) {
			try {
				this.setProxyPort(Integer.parseInt(System.getProperty("proxyPort")));
			}
			catch (Exception ex) {
				throw new NumberFormatException("Non-numeric value is set [proxyPort]: " + System.getProperty("proxyPort"));
			}
		}
		if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyHost").length() > 0)
			this.setProxyHost(System.getProperty("http.proxyHost"));
		if (System.getProperty("http.proxyPort") != null && System.getProperty("http.proxyPort").length() > 0) {
			try {
				this.setProxyPort(Integer.parseInt(System.getProperty("http.proxyPort")));
			}
			catch (Exception ex) {
				throw new NumberFormatException("Non-numeric value is set [http.proxyPort]: " + System.getProperty("http.proxyPort"));
			}
		}
	}

	public int getFtpPort() {
		return ftpPort;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public static void main(String[] args) throws Exception {
		SOSFTPS sosftp = null;
		try {
//			sosftp = new SOSFTPS("WILMA", 21);
//			sosftp.login("test", "12345");
			sosftp = new SOSFTPS("localhost", 21);
			sosftp.login("kb", "kb");
			long s1 = System.currentTimeMillis();
			Vector va = sosftp.nList("/home/kb");
			long e1 = System.currentTimeMillis();
			long r1 = e1 - s1;
		}
		catch (Exception e) {
			System.err.println(e.toString());
		}
		finally {
			if (sosftp != null) {
				sosftp.disconnect();
			}
		}
	}
}
