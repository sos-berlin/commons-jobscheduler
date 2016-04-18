package com.sos.VirtualFileSystem.FTP;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.log4j.Logger;

public class SOSFtpClientLogger implements ProtocolCommandListener {

    private static final Logger LOGGER = Logger.getLogger(SOSFtpClientLogger.class);
    private String clientId;

    public SOSFtpClientLogger(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void protocolCommandSent(final ProtocolCommandEvent event) {
        if (!"PASS".equalsIgnoreCase(event.getCommand())) {
            LOGGER.debug(clientId + " > " + event.getMessage().trim());
        }
    }

    @Override
    public void protocolReplyReceived(final ProtocolCommandEvent event) {
        LOGGER.debug(clientId + " < " + event.getMessage().trim());
    }

}