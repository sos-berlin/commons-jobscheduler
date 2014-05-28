/**
 *
 */
package com.sos.VirtualFileSystem.DataElements;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

/**
 * @author KB
 *
 */
public class SOSVfsConnectionFactory {
	@SuppressWarnings("unused") private final String		conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion		= "$Id$";
	private final static Logger		logger				= Logger.getLogger(SOSVfsConnectionFactory.class);
	protected SOSFTPOptions									objOptions			= null;
	private SOSVfsConnectionPool							objConnPoolSource	= null;
	private SOSVfsConnectionPool							objConnPoolTarget	= null;

	/**
	 *
	 */
	public SOSVfsConnectionFactory(final SOSFTPOptions pobjOptions) {
		super();
		objOptions = pobjOptions;
		createConnectionPool();
	}

	public SOSVfsConnectionPool getSourcePool() {
		return objConnPoolSource;
	}

	public SOSVfsConnectionPool getTargetPool() {
		return objConnPoolTarget;
	}

	public void createConnectionPool() {
		if (objConnPoolSource != null && objOptions.PollingServer.isTrue()) {
			// dann nehmen wir genau diesen Pool
			logger.debug("use existing connection pool");
		}
		else {
			int intMaxParallelTransfers = 1;
			if (objOptions.ConcurrentTransfer.isTrue()) {
				intMaxParallelTransfers = objOptions.MaxConcurrentTransfers.value() + 1;
			}
			if (objConnPoolSource == null || objOptions.reuseConnection.isFalse()) {
				logger.debug("create connection pool");
				objConnPoolSource = new SOSVfsConnectionPool();
				objConnPoolTarget = new SOSVfsConnectionPool();
				for (int i = 0; i < intMaxParallelTransfers; i++) {
					objConnPoolSource.add(getVfsHandler(true));
					// TODO handle multiple targets
					if (objOptions.NeedTargetClient() == true) {
						objConnPoolTarget.add(getVfsHandler(false));
					}
				}
			}
			else {
				// TODO check for changes in the connection options (host, user)
				logger.debug("use existing connection pool");
			}
		}
	}

	private ISOSVFSHandler getVfsHandler(final boolean pflgIsDatasource) {
		ISOSVFSHandler objVFS4Handler = null;
		try {
			SOSConnection2OptionsAlternate objConnectOptions;
			String strDataType;
			if (pflgIsDatasource) {
				objConnectOptions = objOptions.getConnectionOptions().Source();
				strDataType = objOptions.getDataSourceType();
			}
			else {
				objConnectOptions = objOptions.getConnectionOptions().Target();
				strDataType = objOptions.getDataTargetType();
			}
			objConnectOptions.loadClassName.SetIfNotDirty(objOptions.getConnectionOptions().loadClassName);
			VFSFactory.setConnectionOptions(objConnectOptions);
			objVFS4Handler = VFSFactory.getHandler(strDataType);
			objVFS4Handler.Options(objOptions);
			if (pflgIsDatasource) {
				objVFS4Handler.setSource();
				doConnect(objVFS4Handler, objConnectOptions);
				doAuthenticate(objVFS4Handler, objConnectOptions, pflgIsDatasource);
			}
			else {
				objVFS4Handler.setTarget();
				// TODO implement lazyconnectionmode
				doConnect(objVFS4Handler, objConnectOptions);
				String strAuthMethod = objConnectOptions.getAuth_method().Value();
				doAuthenticate(objVFS4Handler, objConnectOptions, pflgIsDatasource);
			}
		}
		catch (Exception ex) {
			throw (RuntimeException) ex;
		}
		return objVFS4Handler;
	}

	private void doConnect(final ISOSVFSHandler objVFS4Handler, final SOSConnection2OptionsAlternate objConnectOptions) {
		try {
			objVFS4Handler.Connect(objConnectOptions);
		}
		catch (Exception e) { // Problem to connect, try alternate host
			// TODO respect alternate data-source type? alternate port etc. ?
			JobSchedulerException.LastErrorMessage = "";
			try {
				objVFS4Handler.Connect(objConnectOptions.Alternatives());
				objConnectOptions.setAlternateOptionsUsed("true");
			}
			catch (Exception e1) {
				throw new JobSchedulerException(e);
			}
			// TODO get an instance of .Alternatives for Authentication ...
		}
	}

	private void doAuthenticate(final ISOSVFSHandler objVFS4Handler, final SOSConnection2OptionsAlternate objConnectOptions, final boolean pflgIsDataSource)
			throws Exception {
		try {
			objVFS4Handler.Authenticate(objConnectOptions);
		}
		catch (Exception e) { // SOSFTP-113: Problem to login, try alternate User
			// TODO respect alternate authentication, eg password and/or public key
			JobSchedulerException.LastErrorMessage = "";
			try {
				objVFS4Handler.Authenticate(objConnectOptions.Alternatives());
			}
			catch (RuntimeException e1) {
				throw e1;
			}
			objConnectOptions.setAlternateOptionsUsed("true");
		}
		ISOSVfsFileTransfer objDataClient = (ISOSVfsFileTransfer) objVFS4Handler;
		if (objOptions.passive_mode.value() || objConnectOptions.passive_mode.isTrue()) {
			objDataClient.passive();
		}
		//objConnectOptions.transfer_mode is not used?
		if (objConnectOptions.transfer_mode.isDirty()) {
			objDataClient.TransferMode(objConnectOptions.transfer_mode);
		}
		else {
			objDataClient.TransferMode(objOptions.transfer_mode);
		}
		objDataClient.ControlEncoding(objOptions.ControlEncoding.Value());
		// TODO pre-commands for source and target seperately
		if (objOptions.PreFtpCommands.IsNotEmpty() && pflgIsDataSource == false) {
			// TODO Command separator as option
			for (String strCmd : objOptions.PreFtpCommands.split()) {
				strCmd = objOptions.replaceVars(strCmd);
				objDataClient.getHandler().ExecuteCommand(strCmd);
			}
		}
	}

	public void clear() {
		objConnPoolSource.clear();
		objConnPoolTarget.clear();
	}
}
