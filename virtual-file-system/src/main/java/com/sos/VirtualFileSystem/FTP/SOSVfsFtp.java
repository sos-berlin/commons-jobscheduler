package com.sos.VirtualFileSystem.FTP;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.i18n.annotation.I18NResourceBundle;

import sos.util.SOSString;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtp extends SOSVfsFtpBaseClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsFtp.class);

    @Override
    public final FTPClient getClient() {
        if (super.getClient() == null) {
            FTPClient client = null;
            if (usingProxy()) {
                LOGGER.info(String.format("using proxy: protocol = %s, host = %s, port = %s, user = %s, pass = ?", getProxyProtocol().getValue(),
                        getProxyHost(), getProxyPort(), getProxyUser()));
                if (usingHttpProxy()) {
                    if (SOSString.isEmpty(getProxyUser())) {
                        client = new FTPHTTPClient(getProxyHost(), getProxyPort());
                    } else {
                        client = new FTPHTTPClient(getProxyHost(), getProxyPort(), getProxyUser(), getProxyPassword());
                    }
                } else {
                    client = new FTPClient();
                    client.setProxy(getSocksProxy());
                }
            } else {
                client = new FTPClient();
            }
            setCommandListener(new SOSFtpClientLogger(getHostID("")));
            if (getDestinationOptions() != null && getDestinationOptions().protocolCommandListener.isTrue()) {
                client.addProtocolCommandListener(getCommandListener());
                LOGGER.debug("ProtocolcommandListener added and activated");
            }
            String addFTPProtocol = System.getenv("AddFTPProtocol");
            if (addFTPProtocol != null && "true".equalsIgnoreCase(addFTPProtocol)) {
                client.addProtocolCommandListener(getCommandListener());
            }
            super.setClient(client);
        }
        return super.getClient();
    }
}