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
import com.sos.vfs.common.interfaces.ISOSTransferHandler;
import com.sos.vfs.common.options.SOSBaseOptions;
import com.sos.vfs.common.options.SOSDestinationOptions;
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

    @I18NMessages(value = { @I18NMessage("%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "en_UK", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s liefert eine Instanz der Klasse %2$s", locale = "de", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "es", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "fr", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "it", explanation = "%1$s returns instance of %2$s") }, msgnum = "SOSVfs-D-0201", msgurl = "SOSVfs-D-0201")
    private static String PARENT_LOGGER_NAME = "";

    private SOSBaseOptions options = null;

    public SOSVFSFactory() {
        super(SOSVFSConstants.BUNDLE_NAME);
    }

    public SOSVFSFactory(final SOSBaseOptions opt) {
        options = opt;
    }

    public static ISOSTransferHandler getHandler(final SOSOptionTransferType.TransferTypes type) throws Exception {
        return getHandler(type.name());
    }

    public static ISOSTransferHandler getHandler(String protocol) throws Exception {
        ISOSTransferHandler handler = null;

        protocol = protocol.toLowerCase();

        if (protocol.equals(TransferTypes.sftp.name()) || protocol.equals(TransferTypes.ssh.name())) {
            handler = new SOSSFTP();
        } else if (protocol.equals(TransferTypes.local.name())) {
            handler = new SOSLocal();
        } else if (protocol.equals(TransferTypes.ftp.name())) {
            handler = new SOSFTP();
        } else if (protocol.equals(TransferTypes.ftps.name())) {
            handler = new SOSFTPS();
        } else if (protocol.equals(TransferTypes.webdav.name())) {
            handler = new SOSWebDAV();
        } else if (protocol.equals(TransferTypes.http.name())) {
            handler = new SOSHTTP();
        } else if (protocol.equals(TransferTypes.smb.name())) {
            handler = new SOSSMB();
        } else if (protocol.equals(TransferTypes.mq.name())) {
            handler = new SOSJMS();
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
        String name = SOSVFSConstants.LOGGER_NAME;
        if (!PARENT_LOGGER_NAME.isEmpty()) {
            name = PARENT_LOGGER_NAME + "." + name;
        }
        return name;
    }

    public ISOSTransferHandler getConnectedHandler(final boolean isSource) throws SOSYadeSourceConnectionException, SOSYadeTargetConnectionException {
        ISOSTransferHandler client = null;
        try {
            SOSDestinationOptions destinationOptions;
            String dataType;
            if (isSource) {
                destinationOptions = options.getTransferOptions().getSource();
                dataType = options.getDataSourceType();
            } else {
                destinationOptions = options.getTransferOptions().getTarget();
                dataType = options.getDataTargetType();
            }
            destinationOptions.loadClassName.setIfNotDirty(options.getTransferOptions().loadClassName);
            client = getHandler(dataType);
            try {
                handleOptions(destinationOptions, isSource);
                client.setBaseOptions(options);
                client.connect(destinationOptions);
            } catch (Exception e) {
                SOSDestinationOptions alternatives = destinationOptions.getAlternatives();
                if (alternatives.optionsHaveMinRequirements()) {
                    LOGGER.warn(String.format("Connection failed : %s", e.toString()));
                    LOGGER.info(String.format("Try again using the alternate options ..."));
                    LOGGER.debug(alternatives.dirtyString());
                    JobSchedulerException.LastErrorMessage = "";
                    try {
                        client.disconnect();
                    } catch (Exception ce) {
                        LOGGER.warn(String.format("client disconnect failed : %s", ce.toString()), ce);
                    }
                    client = getHandler(alternatives.protocol.getValue());
                    handleOptions(alternatives, isSource);
                    client.connect(alternatives);
                    destinationOptions.alternateOptionsUsed.value(true);
                } else {
                    LOGGER.error(String.format("Connection failed : %s", e.toString()), e);
                    LOGGER.debug(String.format("alternate options are not defined"));
                    throw e;
                }
            }
        } catch (JobSchedulerException ex) {
            if (isSource) {
                throw new SOSYadeSourceConnectionException(ex.getCause());
            } else {
                throw new SOSYadeTargetConnectionException(ex.getCause());
            }
        } catch (Exception ex) {
            if (isSource) {
                throw new SOSYadeSourceConnectionException(ex);
            } else {
                throw new SOSYadeTargetConnectionException(ex);
            }
        }
        return client;
    }

    private void handleOptions(SOSDestinationOptions destinationOptions, boolean isSource) throws Exception {
        if (destinationOptions.directory.isDirty()) {
            if (isSource) {
                options.sourceDir = destinationOptions.directory;
                options.localDir = destinationOptions.directory;
            } else {
                options.targetDir = destinationOptions.directory;
                options.remoteDir = destinationOptions.directory;
            }
        }
        if (destinationOptions.transferMode.isDirty() && destinationOptions.transferMode.isNotEmpty()) {
            // client.transferMode(destinationOptions.transferMode);
        } else {
            // client.transferMode(options.transferMode);
            destinationOptions.transferMode = options.transferMode;
        }
    }
}