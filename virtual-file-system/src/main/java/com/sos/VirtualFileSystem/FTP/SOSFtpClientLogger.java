package com.sos.VirtualFileSystem.FTP;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.log4j.Logger;

public class SOSFtpClientLogger implements ProtocolCommandListener {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private final Logger logger = Logger.getLogger(this.getClass());

    private String clientId;

    public SOSFtpClientLogger(final String clientId) {
        this.clientId = clientId;
    }

    /** @return the clientId */
    public String getClientId() {
        return clientId;
    }

    /** @param clientId the clientId to set */
    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void protocolCommandSent(final ProtocolCommandEvent event) {
        if (event.getCommand() != "PASS") {
            logger.debug(clientId + " > " + event.getMessage().trim());
        }
    }

    @Override
    public void protocolReplyReceived(final ProtocolCommandEvent event) {
        logger.debug(clientId + " < " + event.getMessage().trim());
    }
}
