package com.sos.VirtualFileSystem.FTPS;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.FTP.SOSFtpClientLogger;
import com.sos.VirtualFileSystem.FTP.SOSVfsFtpBaseClass2;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpS2 extends SOSVfsFtpBaseClass2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsFtpS2.class);
    private FTPSClient objFTPClient = null;

    public SOSVfsFtpS2() {
    }

    public FTPSClient getClient() {
        return Client();
    }

    @Override
    protected FTPSClient Client() {
        if (objFTPClient == null) {
            try {
                String strProtocol = objConnection2Options.ftpsProtocol.getValue();
                objFTPClient = new FTPSClient(strProtocol);
            } catch (Exception e) {
                throw new JobSchedulerException("can not create FTPS-Client", e);
            }
            FTPClientConfig conf = new FTPClientConfig();
            objProtocolCommandListener = new SOSFtpClientLogger(getHostID(""));
            if (objConnection2Options != null && objConnection2Options.protocolCommandListener.isTrue()) {
                objFTPClient.addProtocolCommandListener(objProtocolCommandListener);
            }
            String strAddFTPProtocol = System.getenv("AddFTPProtocol");
            if (strAddFTPProtocol != null && "true".equalsIgnoreCase(strAddFTPProtocol)) {
                objFTPClient.addProtocolCommandListener(objProtocolCommandListener);
            }
        }
        return objFTPClient;
    }

    @Override
    public void doConnect(final String phost, final int pport) {
        try {
            if (!isConnected()) {
                super.connect(phost, pport);
                Client().execPBSZ(0);
                logReply();
                Client().execPROT("P");
                logReply();
                Client().enterLocalPassiveMode();
            } else {
                LOGGER.warn(SOSVfs_D_0102.params(host, port));
            }
        } catch (Exception e) {
            String strM = getHostID("connect returns an exception");
            LOGGER.error(strM, e);
        }
    }

}