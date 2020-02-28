package com.sos.VirtualFileSystem.FTPS;

import java.io.File;
import java.net.ProxySelector;
import java.security.KeyStore;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol.Protocol;
import com.sos.VirtualFileSystem.FTP.SOSFtpClientLogger;
import com.sos.VirtualFileSystem.FTP.SOSVfsFtpBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpS extends SOSVfsFtpBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsFtpS.class);
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
                LOGGER.info(String.format("use %s client security", getConnectionOptionsAlternate().ftpsClientSecurity.getValue()));
                client = new FTPSClient(getConnectionOptionsAlternate().ftpsProtocol.getValue(), getConnectionOptionsAlternate().ftpsClientSecurity
                        .isImplicit());
                if (usingProxy()) {
                    LOGGER.info(String.format("using proxy: protocol = %s, host = %s, port = %s, user = %s, pass = ?", getProxyProtocol().getValue(),
                            getProxyHost(), getProxyPort(), getProxyUser()));

                    if (usingHttpProxy()) {
                        client.setProxy(getHTTPProxy());
                    } else {
                        SOSOptionProxyProtocol.Protocol proxyProtocol = getProxyProtocol().isSocks4() ? Protocol.socks4 : Protocol.socks5;
                        SOSVfsFtpSProxySelector ps = new SOSVfsFtpSProxySelector(proxyProtocol, getProxyHost(), getProxyPort(), getProxyUser(),
                                getProxyPassword());
                        ProxySelector.setDefault(ps);
                    }
                }
                if (!SOSString.isEmpty(getConnectionOptionsAlternate().keystoreFile.getValue())) {
                    setTrustManager(client);
                }

            } catch (Exception e) {
                throw new JobSchedulerException("can not create FTPS-Client", e);
            }
            setCommandListener(new SOSFtpClientLogger(getHostID("")));
            if (getConnectionOptionsAlternate() != null && getConnectionOptionsAlternate().protocolCommandListener.isTrue()) {
                client.addProtocolCommandListener(getCommandListener());
            }

            String addFTPProtocol = System.getenv("AddFTPProtocol");
            if (addFTPProtocol != null && "true".equalsIgnoreCase(addFTPProtocol)) {
                client.addProtocolCommandListener(getCommandListener());
            }

        }
        return client;
    }

    @Override
    public void doConnect(final String host, final int port) {
        try {
            if (!isConnected()) {
                super.doConnect(host, port);
                if (getConnectionOption() != null) {
                    getConnectionOption().getHost().setValue(host);
                    getConnectionOption().getPort().value(port);
                }
                Client().execPBSZ(0);
                logReply();
                Client().execPROT("P");
                logReply();
                Client().enterLocalPassiveMode();
                logReply();
            } else {
                LOGGER.warn(SOSVfs_D_0102.params(getHost(), getPort()));
            }
        } catch (Exception e) {
            String msg = getHostID("connect returns an exception");
            LOGGER.error(msg, e);
        }
    }

    @Override
    public void executeCommand(final String cmd) throws Exception {
        String command = cmd.trim();
        try {
            client.sendCommand(command);
            String replyString = client.getReplyString().trim();
            int replyCode = client.getReplyCode();
            if (FTPReply.isNegativePermanent(replyCode) || FTPReply.isNegativeTransient(replyCode) || replyCode >= 10_000) {
                throw new JobSchedulerException(SOSVfs_E_164.params(replyString + "[" + command + "]"));
            }
            LOGGER.info(SOSVfs_D_151.params(command, replyString));
        } catch (JobSchedulerException ex) {
            if (getConnectionOptionsAlternate().raiseExceptionOnError.value()) {
                throw ex;
            }
            LOGGER.info(SOSVfs_D_151.params(command, ex.toString()));
        } catch (Exception ex) {
            if (getConnectionOptionsAlternate().raiseExceptionOnError.value()) {
                throw new JobSchedulerException(SOSVfs_E_134.params("ExecuteCommand"), ex);
            }
            LOGGER.info(SOSVfs_D_151.params(command, ex.toString()));
        }
    }

    private void setTrustManager(FTPSClient client) throws Exception {
        LOGGER.info(String.format("using keystore: type = %s, file = %s", getConnectionOptionsAlternate().keystoreType.getValue(),
                getConnectionOptionsAlternate().keystoreFile.getValue()));
        KeyStore ks = loadKeyStore(getConnectionOptionsAlternate().keystoreType.getValue(), new File(getConnectionOptionsAlternate().keystoreFile
                .getValue()), getConnectionOptionsAlternate().keystorePassword.getValue());
        client.setTrustManager(TrustManagerUtils.getDefaultTrustManager(ks));
    }
}