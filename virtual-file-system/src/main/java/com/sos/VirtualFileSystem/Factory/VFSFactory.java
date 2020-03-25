package com.sos.VirtualFileSystem.Factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.VirtualFileSystem.FTP.SOSVfsFtp;
import com.sos.VirtualFileSystem.FTPS.SOSVfsFtpS;
import com.sos.VirtualFileSystem.HTTP.SOSVfsHTTP;
import com.sos.VirtualFileSystem.Interfaces.ISOSTransferHandler;
import com.sos.VirtualFileSystem.JCIFS.SOSVfsJCIFS;
import com.sos.VirtualFileSystem.JMS.SOSVfsJms;
import com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft;
import com.sos.VirtualFileSystem.WebDAV.SOSVfsWebDAV;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.VirtualFileSystem.local.SOSVfsLocal;
import com.sos.VirtualFileSystem.zip.SOSVfsZip;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class VFSFactory extends SOSVfsMessageCodes {

    private static final Logger LOGGER = LoggerFactory.getLogger(VFSFactory.class);

    @I18NMessages(value = { @I18NMessage("%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "en_UK", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s liefert eine Instanz der Klasse %2$s", locale = "de", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "es", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "fr", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "it", explanation = "%1$s returns instance of %2$s") }, msgnum = "SOSVfs-D-0201", msgurl = "SOSVfs-D-0201")
    private static String PARENT_LOGGER_NAME = "";

    public VFSFactory() {
        super(SOSVfsConstants.BUNDLE_NAME);
    }

    public static ISOSTransferHandler getHandler(final SOSOptionTransferType.TransferTypes type) throws Exception {
        return getHandler(type.name());
    }

    public static ISOSTransferHandler getHandler(String protocol) throws Exception {
        ISOSTransferHandler handler = null;

        protocol = protocol.toLowerCase();

        if (protocol.equals(TransferTypes.sftp.name()) || protocol.equals(TransferTypes.ssh.name())) {
            handler = new SOSVfsSFtpJCraft();
        } else if (protocol.equals(TransferTypes.local.name())) {
            handler = new SOSVfsLocal();
        } else if (protocol.equals(TransferTypes.ftp.name())) {
            handler = new SOSVfsFtp();
        } else if (protocol.equals(TransferTypes.ftps.name())) {
            handler = new SOSVfsFtpS();
        } else if (protocol.equals(TransferTypes.webdav.name())) {
            handler = new SOSVfsWebDAV();
        } else if (protocol.equals(TransferTypes.http.name())) {
            handler = new SOSVfsHTTP();
        } else if (protocol.equals(TransferTypes.smb.name())) {
            handler = new SOSVfsJCIFS();
        } else if (protocol.equals(TransferTypes.zip.name())) {
            handler = new SOSVfsZip();
        } else if (protocol.equals(TransferTypes.mq.name())) {
            handler = new SOSVfsJms();
        }
        if (handler == null) {
            throw new Exception(SOSVfs_E_0203.params(protocol));
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(handler.getClass().getName());
        }
        return handler;
    }

    public static void setParentLogger(final String name) {
        PARENT_LOGGER_NAME = name;
    }

    public static String getLoggerName() {
        String name = SOSVfsConstants.LOGGER_NAME;
        if (!PARENT_LOGGER_NAME.isEmpty()) {
            name = PARENT_LOGGER_NAME + "." + name;
        }
        return name;
    }
}