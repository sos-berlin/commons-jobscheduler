package com.sos.VirtualFileSystem.DataElements;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.exception.SOSYadeSourceConnectionException;
import com.sos.exception.SOSYadeTargetConnectionException;

/** @author KB */
public class SOSVfsConnectionFactory {

    protected SOSFTPOptions objOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsConnectionFactory.class);
    private SOSVfsConnectionPool objConnPoolSource = null;
    private SOSVfsConnectionPool objConnPoolTarget = null;

    public SOSVfsConnectionFactory(final SOSFTPOptions pobjOptions) {
        this.objOptions = pobjOptions;
    }

    public SOSVfsConnectionPool getSourcePool() {
        return objConnPoolSource;
    }

    public SOSVfsConnectionPool getTargetPool() {
        return objConnPoolTarget;
    }

    public void createConnectionPool() throws SOSYadeSourceConnectionException, SOSYadeTargetConnectionException {
        if (objConnPoolSource != null && objOptions.pollingServer.isTrue()) {
            LOGGER.debug("use existing connection pool");
        } else {
            int intMaxParallelTransfers = 1;
            if (objOptions.concurrentTransfer.isTrue()) {
                intMaxParallelTransfers = objOptions.maxConcurrentTransfers.value() + 1;
            }
            if (objConnPoolSource == null || objOptions.reuseConnection.isFalse()) {
                LOGGER.debug("create connection pool");
                objConnPoolSource = new SOSVfsConnectionPool();
                objConnPoolTarget = new SOSVfsConnectionPool();
                for (int i = 0; i < intMaxParallelTransfers; i++) {
                    objConnPoolSource.add(getVfsHandler(true));
                    if (objOptions.isNeedTargetClient()) {
                        objConnPoolTarget.add(getVfsHandler(false));
                    }
                }
            } else {
                LOGGER.debug("use existing connection pool");
            }
        }
    }

    private ISOSVFSHandler getVfsHandler(final boolean isSource) throws SOSYadeSourceConnectionException,
        SOSYadeTargetConnectionException {
        ISOSVFSHandler handler = null;
        try {
            SOSConnection2OptionsAlternate options;
            String dataType;
            if (isSource) {
                options = objOptions.getConnectionOptions().getSource();
                dataType = objOptions.getDataSourceType();
            } else {
                options = objOptions.getConnectionOptions().getTarget();
                dataType = objOptions.getDataTargetType();
            }
            options.loadClassName.setIfNotDirty(objOptions.getConnectionOptions().loadClassName);
            VFSFactory.setConnectionOptions(options);
            handler = prepareVFSHandler(handler, dataType, isSource);
            ISOSVfsFileTransfer client = (ISOSVfsFileTransfer) handler;
            try {
                handler.connect(options);
                handler.authenticate(options);
                handleClient(client, options, isSource);
            } catch (Exception e) {
                SOSConnection2OptionsAlternate alternatives = options.getAlternatives();
                if (alternatives.optionsHaveMinRequirements()) {
                    LOGGER.warn(String.format("Connection failed : %s", e.toString()));
                    LOGGER.info(String.format("Try again using the alternate options ..."));
                    LOGGER.debug(alternatives.dirtyString());
                    JobSchedulerException.LastErrorMessage = "";
                    try {
                        client.disconnect();
                    } catch (Exception ce) {
                        LOGGER.warn(String.format("client disconnect failed : %s", ce.toString()));
                    }
                    handler = prepareVFSHandler(handler, alternatives.protocol.getValue(), isSource);
                    client = (ISOSVfsFileTransfer) handler;
                    handler.connect(alternatives);
                    handler.authenticate(alternatives);
                    options.alternateOptionsUsed.value(true);
                    handleClient(client, alternatives, isSource);
                } else {
                    LOGGER.error(String.format("Connection failed : %s", e.toString()));
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
        return handler;
    }

    private ISOSVFSHandler prepareVFSHandler(ISOSVFSHandler handler, final String dataType, final boolean isSource) throws Exception {
        handler = VFSFactory.getHandler(dataType);
        handler.getOptions(objOptions);
        if (isSource) {
            handler.setSource();
        } else {
            handler.setTarget();
        }
        return handler;
    }

    private void handleClient(ISOSVfsFileTransfer client, SOSConnection2OptionsAlternate options, boolean isSource) throws Exception {
        if (options.directory.isDirty()) {
            if (isSource) {
                objOptions.sourceDir = options.directory;
                objOptions.localDir = options.directory;
            } else {
                objOptions.targetDir = options.directory;
                objOptions.remoteDir = options.directory;
            }
        }
        if (objOptions.passiveMode.value() || options.passiveMode.isTrue()) {
            client.passive();
        }
        if (options.transferMode.isDirty() && options.transferMode.isNotEmpty()) {
            client.transferMode(options.transferMode);
        } else {
            client.transferMode(objOptions.transferMode);
        }
    }

    public void clear() {
        objConnPoolSource.clear();
        objConnPoolTarget.clear();
    }

}