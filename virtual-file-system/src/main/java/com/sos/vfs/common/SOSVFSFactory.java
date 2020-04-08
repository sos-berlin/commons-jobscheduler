package com.sos.vfs.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.exception.SOSYadeSourceConnectionException;
import com.sos.exception.SOSYadeTargetConnectionException;
import com.sos.vfs.ftp.SOSFTP;
import com.sos.vfs.ftp.SOSFTPS;
import com.sos.vfs.http.SOSHTTP;
import com.sos.vfs.common.interfaces.ISOSProvider;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSProviderOptions;
import com.sos.vfs.jms.SOSJMS;
import com.sos.vfs.sftp.SOSSFTP;
import com.sos.vfs.smb.SOSSMB;
import com.sos.vfs.webdav.SOSWebDAV;
import com.sos.vfs.common.SOSVFSMessageCodes;
import com.sos.vfs.local.SOSLocal;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVFSFactory extends SOSVFSMessageCodes {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVFSFactory.class);

    public final static String BUNDLE_NAME = "SOSVirtualFileSystem";
    public static String LOGGER_NAME = "SOSVfsReportLog";

    @I18NMessages(value = { @I18NMessage("%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "en_UK", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s liefert eine Instanz der Klasse %2$s", locale = "de", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "es", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "fr", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "it", explanation = "%1$s returns instance of %2$s") }, msgnum = "SOSVfs-D-0201", msgurl = "SOSVfs-D-0201")
    private static String PARENT_LOGGER_NAME = "";

    private SOSBaseOptions options = null;

    public SOSVFSFactory() {
        super(BUNDLE_NAME);
    }

    public SOSVFSFactory(final SOSBaseOptions opt) {
        options = opt;
    }

    public static ISOSProvider getProvider(final SOSOptionTransferType.TransferTypes type) throws Exception {
        return getProvider(type.name());
    }

    public static ISOSProvider getProvider(String protocol) throws Exception {
        ISOSProvider provider = null;
        protocol = protocol.toLowerCase();

        if (protocol.equals(TransferTypes.sftp.name()) || protocol.equals(TransferTypes.ssh.name())) {
            provider = new SOSSFTP();
        } else if (protocol.equals(TransferTypes.local.name())) {
            provider = new SOSLocal();
        } else if (protocol.equals(TransferTypes.ftp.name())) {
            provider = new SOSFTP();
        } else if (protocol.equals(TransferTypes.ftps.name())) {
            provider = new SOSFTPS();
        } else if (protocol.equals(TransferTypes.webdav.name())) {
            provider = new SOSWebDAV();
        } else if (protocol.equals(TransferTypes.http.name())) {
            provider = new SOSHTTP();
        } else if (protocol.equals(TransferTypes.smb.name())) {
            provider = new SOSSMB();
        } else if (protocol.equals(TransferTypes.mq.name())) {
            provider = new SOSJMS();
        }
        if (provider == null) {
            throw new Exception(SOSVfs_E_0203.params(protocol));
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(provider.getClass().getName());
        }
        return provider;
    }

    public static void setParentLogger(final String name) {
        PARENT_LOGGER_NAME = name;
    }

    public static String getLoggerName() {
        String name = LOGGER_NAME;
        if (!PARENT_LOGGER_NAME.isEmpty()) {
            name = PARENT_LOGGER_NAME + "." + name;
        }
        return name;
    }

    public ISOSProvider getConnectedProvider(SOSProviderOptions providerOptions) throws SOSYadeSourceConnectionException,
            SOSYadeTargetConnectionException {
        ISOSProvider provider = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s]%s", providerOptions.getRange(), providerOptions.getProtocol()));
            }
            provider = getProvider(providerOptions.getProtocol());
            try {
                handleOptions(providerOptions);
                provider.setBaseOptions(options);
                provider.connect(providerOptions);
            } catch (Exception e) {
                SOSProviderOptions alternative = providerOptions.getAlternative();
                if (alternative.optionsHaveMinRequirements()) {
                    LOGGER.warn(String.format("Connection failed : %s", e.toString()));
                    LOGGER.info(String.format("Try again using the alternate options ..."));
                    LOGGER.debug(alternative.dirtyString());
                    JobSchedulerException.LastErrorMessage = "";
                    try {
                        provider.disconnect();
                    } catch (Exception ce) {
                        LOGGER.warn(String.format("client disconnect failed : %s", ce.toString()), ce);
                    }
                    provider = getProvider(alternative.protocol.getValue());
                    handleOptions(alternative);
                    provider.connect(alternative);
                    providerOptions.alternateOptionsUsed.value(true);
                } else {
                    LOGGER.error(String.format("Connection failed : %s", e.toString()), e);
                    LOGGER.debug(String.format("alternate options are not defined"));
                    throw e;
                }
            }
        } catch (JobSchedulerException ex) {
            if (providerOptions.isSource()) {
                throw new SOSYadeSourceConnectionException(ex.getCause());
            } else {
                throw new SOSYadeTargetConnectionException(ex.getCause());
            }
        } catch (Exception ex) {
            if (providerOptions.isSource()) {
                throw new SOSYadeSourceConnectionException(ex);
            } else {
                throw new SOSYadeTargetConnectionException(ex);
            }
        }
        return provider;
    }

    private void handleOptions(SOSProviderOptions providerOptions) throws Exception {
        if (providerOptions.directory.isDirty()) {
            if (providerOptions.isSource()) {
                options.sourceDir = providerOptions.directory;
                options.localDir = providerOptions.directory;
            } else {
                options.targetDir = providerOptions.directory;
                options.remoteDir = providerOptions.directory;
            }
        }
        if (providerOptions.transferMode.isDirty() && providerOptions.transferMode.isNotEmpty()) {
            // client.transferMode(providerOptions.transferMode);
        } else {
            // client.transferMode(options.transferMode);
            providerOptions.transferMode = options.transferMode;
        }
    }
}