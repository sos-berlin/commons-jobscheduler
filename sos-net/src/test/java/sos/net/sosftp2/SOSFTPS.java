package sos.net.sosftp2;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import sos.net.SOSSSLSocketFactory;

/** <p>
 * Title: SOSFTPS-Client
 * </p>
 * <p>
 * Description: This class adds implicit SSL/TLS (FTP over SSL/TLS) support to
 * the org.apache.commons.net.ftp.FTPClient without changes.
 * </p>
 * <p>
 * HTTP Proxy support is also added.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: SOS GmbH
 * </p>
 * 
 * @author <a href="mailto:ghassan.beydoun@sos-berlin.com">Ghassan Beydoun</a>
 * @version $Id: SOSFTPS.java 6218 2010-03-24 10:55:14Z mo $ */

public class SOSFTPS extends SOSFTP {

    /** default security protocol SSL/TLS implicit */
    protected static final String DEFAULT_SSL_TLS_PROTOCOL = "TLS";

    private String securityProtocol = DEFAULT_SSL_TLS_PROTOCOL;

    private int ftpPort = 990;

    private String proxyHost;

    private int proxyPort;

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public SOSFTPS(String ftpHost, int ftpPort, String proxyHost, int proxyPort) throws SocketException, IOException, UnknownHostException {
        this.setProxyHost(proxyHost);
        this.setProxyPort(proxyPort);
        this.connect(ftpHost, ftpPort);
    }

    public SOSFTPS(String ftpHost, int ftpPort) throws SocketException, IOException, UnknownHostException {

        this.initProxy();

        this.connect(ftpHost, ftpPort);
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

    public void connect(String ftpHost, int ftpPort) throws SocketException, IOException {

        initProxy();

        if (!isConnected()) {
            this.setSocketFactory(new SOSSSLSocketFactory(getProxyHost(), getProxyPort(), getSecurityProtocol()));

            try {
                super.connect(ftpHost, ftpPort);
            } catch (NullPointerException e) {
                throw new SocketException("Connect failed! Probably HTTP proxy in use or the entered ftps port is invalid: " + e.toString());
            } catch (Exception e) {
                throw new SocketException("Connect failed!, reason: " + e.toString());
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
            } catch (Exception ex) {
                throw new NumberFormatException("Non-numeric value is set [proxyPort]: " + System.getProperty("proxyPort"));
            }
        }

        if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyHost").length() > 0)
            this.setProxyHost(System.getProperty("http.proxyHost"));

        if (System.getProperty("http.proxyPort") != null && System.getProperty("http.proxyPort").length() > 0) {
            try {
                this.setProxyPort(Integer.parseInt(System.getProperty("http.proxyPort")));
            } catch (Exception ex) {
                throw new NumberFormatException("Non-numeric value is set [http.proxyPort]: " + System.getProperty("http.proxyPort"));
            }
        }

    }

    public int getFtpPort() {
        return ftpPort;
    }

    public static void main(String[] args) throws Exception {
        SOSFTPS sosftp = null;
        try {

            sosftp = new SOSFTPS("WILMA", 21);
            sosftp.login("test", "12345");

            long s1 = System.currentTimeMillis();
            Vector va = sosftp.nList("/home/test/temp");
            long e1 = System.currentTimeMillis();
            long r1 = e1 - s1;

        } catch (Exception e) {
            System.err.println(e.toString());
        } finally {
            sosftp.disconnect();
        }
    }

}
