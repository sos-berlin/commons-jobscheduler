package com.sos.VirtualFileSystem.FTP;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSFtpClientLogger implements ProtocolCommandListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFtpClientLogger.class);
    private String clientId;

    public SOSFtpClientLogger(final String val) {
        clientId = val;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String val) {
        clientId = val;
    }

    @Override
    public void protocolCommandSent(final ProtocolCommandEvent event) {
        if (!"PASS".equalsIgnoreCase(event.getCommand())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(clientId + " > " + event.getMessage().trim());
            }
        }
    }

    @Override
    public void protocolReplyReceived(final ProtocolCommandEvent event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(clientId + " < " + event.getMessage().trim());
        }
    }

}