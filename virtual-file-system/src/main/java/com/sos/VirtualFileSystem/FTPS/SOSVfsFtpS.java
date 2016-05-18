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

    private static final Logger LOGGER = Logger.getLogger(SOSVfsFtpS.class);
    private FTPSClient client = null;

    public SOSVfsFtpS() {
    }

    public FTPSClient getClient() {
        return Client();
    }

    @Override
    protected FTPSClient Client() {
        if (client == null) {
            try {
                LOGGER.info(String.format("use %s client security", objConnection2Options.ftpsClientSecurity.Value()));
                client = new FTPSClient(objConnection2Options.ftpsProtocol.Value(), objConnection2Options.ftpsClientSecurity.isImplicit());
                if (usingProxy()) {
                    LOGGER.info(String.format("using proxy: protocol = %s, host = %s, port = %s, user = %s, pass = ?", getProxyProtocol().Value(),
                            getProxyHost(), getProxyPort(), getProxyUser()));

                    if (usingHttpProxy()) {
                        client.setProxy(getHTTPProxy());
                    } else {
                        SOSOptionProxyProtocol.Protocol proxyProtocol = getProxyProtocol().isSocks4() ? Protocol.socks4 : Protocol.socks5;
                        SOSVfsFtpSProxySelector ps =
                                new SOSVfsFtpSProxySelector(proxyProtocol, getProxyHost(), getProxyPort(), getProxyUser(), getProxyPassword());
                        ProxySelector.setDefault(ps);
                    }
                }
                if (!SOSString.isEmpty(objConnection2Options.keystoreFile.Value())) {
                    setTrustManager(client);
                }

            } catch (Exception e) {
                throw new JobSchedulerException("can not create FTPS-Client", e);
            }
            objProtocolCommandListener = new SOSFtpClientLogger(HostID(""));
            if (objConnection2Options != null && objConnection2Options.protocolCommandListener.isTrue()) {
                client.addProtocolCommandListener(objProtocolCommandListener);
            }

            String addFTPProtocol = System.getenv("AddFTPProtocol");
            if (addFTPProtocol != null && "true".equalsIgnoreCase(addFTPProtocol)) {
                client.addProtocolCommandListener(objProtocolCommandListener);
            }

        }
        return client;
    }

    @Override
    public void connect(final String phost, final int pport) {
        try {
            if (!isConnected()) {
                super.connect(phost, pport);
                Client().execPBSZ(0);
                LogReply();
                Client().execPROT("P");
                LogReply();
                Client().enterLocalPassiveMode();
            } else {
                LOGGER.warn(SOSVfs_D_0102.params(host, port));
            }
        } catch (Exception e) {
            String msg = HostID("connect returns an exception");
            LOGGER.error(msg, e);
        }
    }

    private void setTrustManager(FTPSClient client) throws Exception {
        LOGGER.info(String.format("using keystore: type = %s, file = %s", objConnection2Options.keystoreType.Value(),
                objConnection2Options.keystoreFile.Value()));
        KeyStore ks = loadKeyStore(objConnection2Options.keystoreType.Value(), new File(objConnection2Options.keystoreFile.Value()),
                objConnection2Options.keystorePassword.Value());
        client.setTrustManager(TrustManagerUtils.getDefaultTrustManager(ks));
    }
}