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
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;

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
	private boolean alternativeConnectionInUse = false;

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
			//			https://change.sos-berlin.com/browse/SOSFTP-212
			//			throw (RuntimeException) ex;
			throw new JobSchedulerException(ex);
		}
		return objVFS4Handler;
	}

	private void doConnect(final ISOSVFSHandler objVFS4Handler, final SOSConnection2OptionsAlternate objConnectOptions) {
		alternativeConnectionInUse = false;
		try {
			objVFS4Handler.Connect(objConnectOptions);
		}
		catch (Exception e) { // Problem to connect, try alternate host
			// TODO respect alternate data-source type? alternate port etc. ?
			SOSConnection2OptionsAlternate alternatives = objConnectOptions.Alternatives();
			if (alternatives.optionsHaveMinRequirements()) {
				logger.warn(e);
				logger.info(SOSVfsMessageCodes.SOSVfs_I_170.params(alternatives.host.Value()));
				try {
					JobSchedulerException.LastErrorMessage = "";
					objVFS4Handler.Connect(alternatives);
					alternativeConnectionInUse = true;
				} catch (Exception e1) {
					throw new JobSchedulerException(e1);
				}
			}
			else {
				throw new JobSchedulerException(e);
			}
			// TODO get an instance of .Alternatives for Authentication ...
		}
	}

	private void doAuthenticate(final ISOSVFSHandler objVFS4Handler, final SOSConnection2OptionsAlternate objConnectOptions, final boolean pflgIsDataSource)
			throws Exception {
		try {
			if (alternativeConnectionInUse) {
				alternativeAuthenticate(objVFS4Handler, objConnectOptions);
			}
			else {
				objVFS4Handler.Authenticate(objConnectOptions);
			}
		}
		catch (Exception e) {
			alternativeAuthenticate(objVFS4Handler, objConnectOptions, e);
		}
		afterAuthenticate(objVFS4Handler, objConnectOptions, pflgIsDataSource);
	}
	
	
	private void alternativeAuthenticate(ISOSVFSHandler objVFS4Handler, SOSConnection2OptionsAlternate objConnectOptions) throws Exception {
		alternativeAuthenticate(objVFS4Handler, objConnectOptions, null);
	}
	
	private void alternativeAuthenticate(ISOSVFSHandler objVFS4Handler, SOSConnection2OptionsAlternate objConnectOptions, Exception e) throws Exception {
		SOSConnection2OptionsAlternate alternatives = objConnectOptions.Alternatives();
		if (alternatives.optionsHaveMinRequirements()) {
			if(e != null) {
				logger.warn(e);
				logger.info(SOSVfsMessageCodes.SOSVfs_I_170.params(alternatives.host.Value()));
				JobSchedulerException.LastErrorMessage = "";
			}
			alternatives.AlternateOptionsUsed.value(true);
			objVFS4Handler.Authenticate(alternatives);
		}
		else {
			if(e != null) throw e;
		}
	}
	
	private void afterAuthenticate(final ISOSVFSHandler objVFS4Handler, final SOSConnection2OptionsAlternate objConnectOptions, final boolean pflgIsDataSource) throws Exception {
		ISOSVfsFileTransfer objDataClient = (ISOSVfsFileTransfer) objVFS4Handler;
		if (objOptions.passive_mode.value() || objConnectOptions.passive_mode.isTrue()) {
			objDataClient.passive();
		}
		//objConnectOptions.transfer_mode is not used?
		if (objConnectOptions.transfer_mode.isDirty() && objConnectOptions.transfer_mode.IsNotEmpty()) {
			objDataClient.TransferMode(objConnectOptions.transfer_mode);
		}
		else {
			objDataClient.TransferMode(objOptions.transfer_mode);
		}
		objDataClient.ControlEncoding(objOptions.ControlEncoding.Value());
		// TODO pre-commands for source and target separately
		if (objOptions.PreFtpCommands.IsNotEmpty() && pflgIsDataSource == false) {
			// TODO Command separator as option
			for (String strCmd : objOptions.PreFtpCommands.split()) {
				strCmd = objOptions.replaceVars(strCmd);
				objDataClient.getHandler().ExecuteCommand(strCmd);
			}
		}
		if (objConnectOptions.PreFtpCommands.IsNotEmpty()) {
			// TODO Command separator as option
			for (String strCmd : objConnectOptions.PreFtpCommands.split()) {
				strCmd = objConnectOptions.replaceVars(strCmd);
				objDataClient.getHandler().ExecuteCommand(strCmd);
			}
		}
	}

	public void clear() {
		objConnPoolSource.clear();
		objConnPoolTarget.clear();
	}
}
