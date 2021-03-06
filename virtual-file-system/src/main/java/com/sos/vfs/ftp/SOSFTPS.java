package com.sos.vfs.ftp;

import java.io.File;
import java.net.ProxySelector;
import java.security.KeyStore;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol.Protocol;
import com.sos.vfs.ftp.common.SOSFTPClientLogger;
import com.sos.vfs.ftp.common.SOSFTPSProxySelector;
import com.sos.vfs.ftp.common.SOSFTPBaseClass;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFTPS extends SOSFTPBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFTPS.class);

    @Override
    public void doConnect() {
        try {
            if (!isConnected()) {
                super.doConnect();

                FTPSClient client = (FTPSClient) super.getClient();
                client.execPBSZ(0);
                logReply("execPBSZ");
                client.execPROT("P");
                logReply("execPROT");
                getClient().enterLocalPassiveMode();
                logReply("enterLocalPassiveMode");
            } else {
                LOGGER.warn(SOSVfs_D_0102.params(getHost(), getPort()));
            }
        } catch (Exception e) {
            String msg = getHostID("connect returns an exception");
            LOGGER.error(msg, e);
        }
    }

    @Override
    public FTPClient getClient() {
        if (super.getClient() == null) {
            FTPSClient client = null;
            try {
                LOGGER.info(String.format("use %s client security", getProviderOptions().ftpsClientSecurity.getValue()));
                client = new FTPSClient(getProviderOptions().ftpsProtocol.getValue(), getProviderOptions().ftpsClientSecurity.isImplicit());
                if (usingProxy()) {
                    LOGGER.info(String.format("using proxy: protocol = %s, host = %s, port = %s, user = %s, pass = ?", getProxyProtocol().getValue(),
                            getProxyHost(), getProxyPort(), getProxyUser()));

                    if (usingHttpProxy()) {
                        client.setProxy(getHTTPProxy());
                    } else {
                        SOSOptionProxyProtocol.Protocol proxyProtocol = getProxyProtocol().isSocks4() ? Protocol.socks4 : Protocol.socks5;
                        SOSFTPSProxySelector ps = new SOSFTPSProxySelector(proxyProtocol, getProxyHost(), getProxyPort(), getProxyUser(),
                                getProxyPassword());
                        ProxySelector.setDefault(ps);
                    }
                }
                if (!SOSString.isEmpty(getProviderOptions().keystoreFile.getValue())) {
                    setTrustManager(client);
                }

            } catch (Exception e) {
                throw new JobSchedulerException("can not create FTPS-Client", e);
            }
            setCommandListener(new SOSFTPClientLogger(getHostID("")));
            if (getProviderOptions() != null && getProviderOptions().protocolCommandListener.isTrue()) {
                client.addProtocolCommandListener(getCommandListener());
            }

            String addFTPProtocol = System.getenv("AddFTPProtocol");
            if (addFTPProtocol != null && "true".equalsIgnoreCase(addFTPProtocol)) {
                client.addProtocolCommandListener(getCommandListener());
            }
            super.setClient(client);

        }
        return super.getClient();
    }

    private void setTrustManager(FTPSClient client) throws Exception {
        LOGGER.info(String.format("using keystore: type = %s, file = %s", getProviderOptions().keystoreType.getValue(),
                getProviderOptions().keystoreFile.getValue()));
        KeyStore ks = loadKeyStore(getProviderOptions().keystoreType.getValue(), new File(getProviderOptions().keystoreFile.getValue()),
                getProviderOptions().keystorePassword.getValue());
        client.setTrustManager(TrustManagerUtils.getDefaultTrustManager(ks));
    }
}