package com.sos.VirtualFileSystem.Factory;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.VirtualFileSystem.FTP.SOSVfsFtp;
import com.sos.VirtualFileSystem.FTPS.SOSVfsFtpS;
import com.sos.VirtualFileSystem.HTTP.SOSVfsHTTP;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.JCIFS.SOSVfsJCIFS;
import com.sos.VirtualFileSystem.JMS.SOSVfsJms;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
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
    private static ClassLoader CLASS_LOADER = null;

    public VFSFactory() {
        super(SOSVfsConstants.BUNDLE_NAME);
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

    public static ISOSVFSHandler getHandler(final SOSOptionTransferType.enuTransferTypes type) throws Exception {
        return getHandler(type.getText());
    }

    public static ISOSVFSHandler getHandler(String whatURL) throws Exception {
        final String method = "VFSFactory::getHandler";
        boolean authenticate = true;
        ISOSVFSHandler handler = null;
        URL url = null;
        if (whatURL.startsWith("local:")) {
            whatURL = whatURL.replace("local:", "file:");
        }
        String whatSystem = whatURL;
        try {
            url = new URL(whatURL);
            whatSystem = url.getProtocol();
            LOGGER.info(url.getFile());
            LOGGER.info(url.getPath());
        } catch (MalformedURLException e) {
            //
        }
        CLASS_LOADER = Thread.currentThread().getContextClassLoader();
        if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.sftp.getText())) {
            handler = new SOSVfsSFtpJCraft();
        } else if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.local.getText()) || whatSystem.equalsIgnoreCase(
                SOSOptionTransferType.enuTransferTypes.file.getText())) {
            handler = new SOSVfsLocal();
            authenticate = false;
        } else if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.ftp.getText())) {
            handler = new SOSVfsFtp();
        } else if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.ftps.getText())) {
            handler = new SOSVfsFtpS();
        } else if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.webdav.getText())) {
            handler = new SOSVfsWebDAV();
        } else if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.http.getText())) {
            handler = new SOSVfsHTTP();
        } else if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.smb.getText())) {
            handler = new SOSVfsJCIFS();
        } else if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.zip.getText())) {
            handler = new SOSVfsZip();
        } else if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.mq.getText())) {
            handler = new SOSVfsJms();
        } else if (whatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.ssh2.getText())) {
            Class<?> clazz = CLASS_LOADER.loadClass(SOSVfsSFtpJCraft.class.getName());
            ISOSVFSHandler h = (ISOSVFSHandler) clazz.newInstance();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(SOSVfs_D_0201.params(method, h.toString()));
            }
            if (h instanceof ISOSVFSHandler) {
                LOGGER.trace("ISOSVFSHandler is part of class   ...  " + clazz.toString());
                handler = h;
            } else {
                LOGGER.error("ISOSVFSHandler not part of class");
            }
        }
        if (handler == null) {
            throw new Exception(SOSVfs_E_0203.params(whatSystem));
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(handler.getClass().getName());
        }
        if (url != null && authenticate) {
            String host = url.getHost();
            if (host != null) {
                int port = url.getPort();
                handler.connect(host, port);
                ISOSAuthenticationOptions options = new SOSFTPOptions();
                String userInfo = url.getUserInfo();
                String[] arr = userInfo.split(":");
                options.getUser().setValue(arr[0]);
                options.getPassword().setValue("");
                if (arr.length > 1) {
                    options.getPassword().setValue(arr[1]);
                }
                handler.authenticate(options);
                options = null;
                LOGGER.info("objURL.getAuthority() : " + url.getAuthority());
                LOGGER.info("objURL.getFile()" + url.getFile());
            }
        }
        return handler;
    }
}