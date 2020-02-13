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
                LOGGER.info(String.format("use %s client security", objConnection2Options.ftpsClientSecurity.getValue()));
                client = new FTPSClient(objConnection2Options.ftpsProtocol.getValue(), objConnection2Options.ftpsClientSecurity.isImplicit());
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
                if (!SOSString.isEmpty(objConnection2Options.keystoreFile.getValue())) {
                    setTrustManager(client);
                }

            } catch (Exception e) {
                throw new JobSchedulerException("can not create FTPS-Client", e);
            }
            objProtocolCommandListener = new SOSFtpClientLogger(getHostID(""));
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
    public void doConnect(final String phost, final int pport) {
        try {
            if (!isConnected()) {
                super.doConnect(phost, pport);
                if (objConnectionOptions != null) {
                    objConnectionOptions.getHost().setValue(phost);
                    objConnectionOptions.getPort().value(pport);
                }
                Client().execPBSZ(0);
                logReply();
                Client().execPROT("P");
                logReply();
                Client().enterLocalPassiveMode();
            } else {
                LOGGER.warn(SOSVfs_D_0102.params(host, port));
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
            if (objConnection2Options.raiseExceptionOnError.value()) {
                throw ex;
            }
            LOGGER.info(SOSVfs_D_151.params(command, ex.toString()));
        } catch (Exception ex) {
            if (objConnection2Options.raiseExceptionOnError.value()) {
                throw new JobSchedulerException(SOSVfs_E_134.params("ExecuteCommand"), ex);
            }
            LOGGER.info(SOSVfs_D_151.params(command, ex.toString()));
        }
    }

    private void setTrustManager(FTPSClient client) throws Exception {
        LOGGER.info(String.format("using keystore: type = %s, file = %s", objConnection2Options.keystoreType.getValue(),
                objConnection2Options.keystoreFile.getValue()));
        KeyStore ks = loadKeyStore(objConnection2Options.keystoreType.getValue(), new File(objConnection2Options.keystoreFile.getValue()),
                objConnection2Options.keystorePassword.getValue());
        client.setTrustManager(TrustManagerUtils.getDefaultTrustManager(ks));
    }
}