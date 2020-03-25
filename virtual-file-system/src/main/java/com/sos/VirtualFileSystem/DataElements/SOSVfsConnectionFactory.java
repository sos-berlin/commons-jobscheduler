package com.sos.VirtualFileSystem.DataElements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSTransferHandler;
import com.sos.VirtualFileSystem.Options.SOSBaseOptions;
import com.sos.VirtualFileSystem.Options.SOSDestinationOptions;
import com.sos.exception.SOSYadeSourceConnectionException;
import com.sos.exception.SOSYadeTargetConnectionException;

public class SOSVfsConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsConnectionFactory.class);
    private SOSVfsConnectionPool source = null;
    private SOSVfsConnectionPool target = null;
    private SOSBaseOptions options = null;

    public SOSVfsConnectionFactory(final SOSBaseOptions opt) {
        options = opt;
    }

    public SOSVfsConnectionPool getSourcePool() {
        return source;
    }

    public SOSVfsConnectionPool getTargetPool() {
        return target;
    }

    public void createConnectionPool() throws SOSYadeSourceConnectionException, SOSYadeTargetConnectionException {
        if (source != null && options.pollingServer.isTrue()) {
            LOGGER.debug("use existing connection pool");
        } else {
            int intMaxParallelTransfers = 1;
            if (options.concurrentTransfer.isTrue()) {
                intMaxParallelTransfers = options.maxConcurrentTransfers.value() + 1;
            }
            if (source == null || options.reuseConnection.isFalse()) {
                LOGGER.debug("create connection pool");
                source = new SOSVfsConnectionPool();
                target = new SOSVfsConnectionPool();
                for (int i = 0; i < intMaxParallelTransfers; i++) {
                    source.add(getVfsHandler(true));
                    if (options.isNeedTargetClient()) {
                        target.add(getVfsHandler(false));
                    }
                }
            } else {
                LOGGER.debug("use existing connection pool");
            }
        }
    }

    private ISOSTransferHandler getVfsHandler(final boolean isSource) throws SOSYadeSourceConnectionException, SOSYadeTargetConnectionException {
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
            client = VFSFactory.getHandler(dataType);
            try {
                handleOptions(destinationOptions, isSource);
                client.connect(destinationOptions);
                client.login(destinationOptions);
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
                    client = VFSFactory.getHandler(alternatives.protocol.getValue());
                    handleOptions(alternatives, isSource);
                    client.connect(alternatives);
                    client.login(alternatives);
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