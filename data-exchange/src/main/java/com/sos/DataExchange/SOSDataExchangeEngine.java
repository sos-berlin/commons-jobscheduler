package com.sos.DataExchange;
import static com.sos.DataExchange.SOSJadeMessageCodes.EXCEPTION_RAISED;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_D_0200;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_E_0100;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_E_0101;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_I_0100;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_I_0101;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_I_0102;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_I_0104;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_I_0115;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_T_0010;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_T_0012;
import static com.sos.DataExchange.SOSJadeMessageCodes.SOSJADE_T_0013;
import static com.sos.DataExchange.SOSJadeMessageCodes.TRANSACTION_ABORTED;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import sos.net.SOSMail;
import sos.net.mail.options.SOSSmtpMailOptions;
import sos.net.mail.options.SOSSmtpMailOptions.enuMailClasses;

import com.sos.DataExchange.Options.JADEOptions;
import com.sos.JSHelper.Basics.JSVersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.concurrent.SOSThreadPoolExecutor;
import com.sos.JSHelper.interfaces.IJadeEngine;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.DataElements.SOSFileList;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;
import com.sos.VirtualFileSystem.DataElements.SOSVfsConnectionFactory;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer2;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.commands.JSCmdAddOrder;
import com.sos.scheduler.model.objects.Params;
import com.sos.scheduler.model.objects.Spooler;

public class SOSDataExchangeEngine extends JadeBaseEngine implements Runnable, IJadeEngine {
	private static final String		conKeyWordLAST_ERROR			= "last_error";
	private static final String		conKeywordSTATE					= "state";
	private static final String		conKeywordSUCCESSFUL_TRANSFERS	= "successful_transfers";
	private static final String		conKeywordFAILED_TRANSFERS		= "failed_transfers";
	private static final String		conKeywordSTATUS				= "status";
	private final String			conClassName					= "SOSDataExchangeEngine";
	public final String				conSVNVersion					= "$Id$";
	private final Logger			logger							= Logger.getLogger(SOSDataExchangeEngine.class);
	final String					strLoggerName					= "JadeReportLog";
	private final Logger			objJadeReportLogger				= Logger.getLogger(strLoggerName);
	private ISOSVFSHandler			objVfs4Target					= null;
	private final ISOSVFSHandler	objVfs4Source					= null;
	public ISOSVfsFileTransfer		objDataTargetClient				= null;
	public ISOSVfsFileTransfer		objDataSourceClient				= null;
	private SOSFileList				objSourceFileList				= null;
	/** notification by e-mail in case of transfer of empty files. */
	// private boolean sameConnection = false;
	@SuppressWarnings("unused")
	private final boolean			testmode						= false;
	private SOSVfsConnectionFactory	objConFactory					= null;
	private long					lngNoOfPollingServerFiles		= 0;

	public SOSDataExchangeEngine() throws Exception {
		init();
	}

	public SOSDataExchangeEngine(final HashMap<String, String> pobjJSSettings) throws Exception {
		this.Options();
		objOptions.setAllOptions(pobjJSSettings);
	}

	/**
	 * @param settingsFile
	 * @param settingsSection
	 * @param logger
	 * @param arguments_
	 */
	public SOSDataExchangeEngine(final Properties pobjProperties) throws Exception {
		this.Options();
		// TODO Properties in die OptionsClasse weiterreichen
		// objOptions.setAllOptions(pobjProperties);
	}

	public SOSDataExchangeEngine(final JADEOptions pobjOptions) throws Exception {
		super(pobjOptions);
		objOptions = pobjOptions;
		if (objOptions.settings.isDirty()) {
			objOptions.ReadSettingsFile();
		}
	}

	// TODO in die DataSource verlagern? Oder in die FileList? Multithreaded ausführen?
	public boolean checkSteadyStateOfFiles() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::checkSteadyStateOfFiles";
		boolean flgAllFilesAreSteady = true;
		if (objOptions.CheckSteadyStateOfFiles.isTrue() && objSourceFileList != null) {
			long lngCheckSteadyStateInterval = objOptions.CheckSteadyStateInterval.getTimeAsSeconds();
			long lngSteadyCount = objOptions.CheckSteadyCount.value();
			setInfo("checking file(s) for steady state");
			for (int i = 0; i < lngSteadyCount; i++) {
				flgAllFilesAreSteady = true;
				for (SOSFileListEntry objFile : objSourceFileList.List()) {
					if (objFile.isSteady() == false) {
						long lastFileLength = objDataSourceClient.getFileHandle(objFile.SourceFileName()).getFileSize();
						logger.debug(String.format("waiting %1$d for steady check", lngCheckSteadyStateInterval));
						doSleep(lngCheckSteadyStateInterval);
						long lngActFileLength = objDataSourceClient.getFileHandle(objFile.SourceFileName()).getFileSize();
						logger.debug(String.format("Last file length %1$d, actual file length %2$d", lastFileLength, lngActFileLength));
						if (lastFileLength != lngActFileLength) {
							flgAllFilesAreSteady = false;
							logger.info(String.format("File '%1$s' changed during checking steady state", objFile.SourceFileName()));
							objFile.setSteady(false);
						}
						else {
							objFile.setSteady(true);
							logger.info(String.format("File '%1$s' was not changed during checking steady state", objFile.SourceFileName()));
						}
						objFile.setParent(objSourceFileList); // this is changing the filesize info in the object
					}
				} // For
				if (flgAllFilesAreSteady == false) {
					logger.debug(String.format("waiting %1$d for steady check", lngCheckSteadyStateInterval));
					doSleep(lngCheckSteadyStateInterval);
				}
				else {
					break;
				}
			}
			if (flgAllFilesAreSteady == false) {
				String strM = "not all files are steady";
				logger.error(strM);
				for (SOSFileListEntry objFile : objSourceFileList.List()) {
					if (objFile.isSteady() == false) {
						logger.info(String.format("File '%1$s' is not steady", objFile.SourceFileName()));
					}
				}
				if (objOptions.SteadyStateErrorState.isDirty()) {
					objJSJobUtilities.setNextNodeState(objOptions.SteadyStateErrorState.Value());
				}
				else {
					throw new JobSchedulerException(strM);
				}
			}
		}
		return flgAllFilesAreSteady;
	} // private boolean checkSteadyStateOfFiles

	private void doLogout(ISOSVfsFileTransfer pobjClient) throws Exception {
		if (pobjClient != null) {
			pobjClient.logout();
			pobjClient.disconnect();
			pobjClient.close();
			pobjClient = null;
		}
	}

	// TODO multi threaded approach
	// TODO move to SOSFileList
	private String[] doPollingForFiles() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::doPollingForFiles";
		String[] strFileList = null;
		if (objOptions.isFilePollingEnabled()) {
			long lngPollTimeOut = getPollTimeout();
			long lngCurrentPollingTime = 0;
			long lngNoOfFilesFound = 0;
			String strSourceDir = objOptions.SourceDir.Value();
			long lngPollInterval = objOptions.poll_interval.getTimeAsSeconds();
			long lngCurrentNoOfFilesFound = objSourceFileList.size();
			String strRegExp4FileNames = objOptions.file_spec.Value();
			boolean flgSourceDirFound = false;
			ISOSVirtualFile objSourceFile = null;
			PollingLoop:
			while (true) {
				if (lngCurrentPollingTime > lngPollTimeOut) { // time exceeded ?
					setInfo(String.format("file-polling: time '%1$s' is over. polling terminated", getPollTimeoutText()));
					break PollingLoop;
				}
				if (flgSourceDirFound == false) {
					objSourceFile = objDataSourceClient.getFileHandle(strSourceDir);
					if (objOptions.pollingWait4SourceFolder.isFalse()) {
						flgSourceDirFound = true; // To keep the previous behaviour
					}
					else {
						try { // Test, wether the source folder is available. if not and polling is active, wait for the folder
							if (objSourceFile.notExists() == true) {
								logger.info(String.format("directory %1$s not found. Wait for the directory due to polling mode.", strSourceDir));
							}
							else {
								flgSourceDirFound = true;
							}
						}
						catch (Exception e) {
							flgSourceDirFound = false;
						}
					}
				}
				if (flgSourceDirFound == true) {
					try {
						if (objOptions.OneOrMoreSingleFilesSpecified() == true) {
							lngCurrentNoOfFilesFound = 0;
							objOptions.poll_minfiles.value(objSourceFileList.count());
							for (SOSFileListEntry objItem : objSourceFileList.List()) {
								if (objItem.SourceFileExists()) {
									lngCurrentNoOfFilesFound++;
									objItem.setParent(objSourceFileList); // to reinit the file-size
								}
							}
						}
						else {
							selectFilesOnSource(objSourceFile, objOptions.SourceDir, objOptions.file_spec, objOptions.recursive);
							lngCurrentNoOfFilesFound = objSourceFileList.count();
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					if (objOptions.poll_minfiles.isNotDirty() && lngCurrentNoOfFilesFound > 0) { // amount
						break PollingLoop; // minfiles not specified and one/some files found
					}
					if (objOptions.poll_minfiles.isDirty() && lngCurrentNoOfFilesFound >= objOptions.poll_minfiles.value()) { // amount
						break PollingLoop;
					}
				}
				//
				setInfo(String.format("file-polling: going to sleep for %1$d seconds. regexp '%2$s'", lngPollInterval, strRegExp4FileNames));
				doSleep(lngPollInterval);
				lngCurrentPollingTime += lngPollInterval;
				setInfo(String.format("file-polling: %1$d files found for regexp '%2$s' on directory '%3$s'.", lngCurrentNoOfFilesFound, strRegExp4FileNames,
						strSourceDir));
				if (lngNoOfFilesFound >= lngCurrentNoOfFilesFound && lngNoOfFilesFound != 0) { // no additional file found
					if (objOptions.WaitingForLateComers.isTrue()) { // just wait a round for latecommers
						objOptions.WaitingForLateComers.setFalse();
					}
					else {
						break PollingLoop;
					}
				}
			} // while
		}
		return strFileList;
	} // private void doPollingForFiles

	private void doProcessMail(final enuMailClasses penuMailClass) {
		SOSSmtpMailOptions objO = objOptions.getMailOptions();
		SOSSmtpMailOptions objMOS = objO.getOptions(penuMailClass);
		if (objMOS == null || objMOS.FileNotificationTo.isDirty() == false) {
			objMOS = objO;
		}
		processSendMail(objMOS);
	}

	private void doSleep(final long lngTime2Sleep) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::doSleep";
		try {
			Thread.sleep(lngTime2Sleep * 1000);
		}
		catch (InterruptedException e) {
		} // wait some seconds
	} // private void doSleep

	/* (non-Javadoc)
	 * @see com.sos.DataExchange.IJadeEngine#Execute()
	 */
	@Override public boolean Execute() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::execute";
		long startTime = System.currentTimeMillis();
		//		long startTime = System.nanoTime();
		VFSFactory.setParentLogger(strLoggerName);
		int intVerbose = objOptions.verbose.value();
		if (intVerbose <= 1) {
			Logger.getRootLogger().setLevel(Level.INFO);
		}
		else {
			if (intVerbose == 9) {
				Logger.getRootLogger().setLevel(Level.TRACE);
				logger.setLevel(Level.TRACE);
				logger.debug("set loglevel to TRACE due to option verbose = " + intVerbose);
			}
			else {
				Logger.getRootLogger().setLevel(Level.DEBUG);
				logger.debug("set loglevel to DEBUG due to option verbose = " + intVerbose);
			}
		}
		String strV = conSVNVersion + " -- " + JSVersionInfo.getVersionString();
		logger.info(strV);
		objOptions.getTextProperties().put("version", strV);
		objOptions.log_filename.setLogger(objJadeReportLogger);
		objOptions.remote_dir.SetIfNotDirty(objOptions.TargetDir);
		objOptions.local_dir.SetIfNotDirty(objOptions.SourceDir);
		objOptions.host.SetIfNotDirty(objOptions.Target().host);
		String strT = "";
		if (objOptions.banner_header.isDirty()) {
			strT = objOptions.banner_header.JSFile().getContent();
		}
		else {
			strT = SOSJADE_T_0010.get(); // LogFile Header
		}
		strT = objOptions.replaceVars(strT);
		objJadeReportLogger.info(strT + "");
		boolean flgOK = false;
		try {
			flgOK = this.transfer();
		}
		catch (Exception e) {
			throw new JobSchedulerException(e.getLocalizedMessage(), e);
		}
		finally {
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			long intNoOfFilesTransferred = getSuccessfulTransfers();
			if (intNoOfFilesTransferred <= 0) {
				intNoOfFilesTransferred = 1;
			}
			logger.info("Elapsed time = " + elapsedTime + ", per File = " + elapsedTime / intNoOfFilesTransferred + ", total bytes = "
					+ getSuccessfulTransfers());
			if (objOptions.banner_footer.isDirty()) {
				objOptions.banner_footer.JSFile().getContent();
			}
			else {
				strT = SOSJadeMessageCodes.SOSJADE_T_0011.get(); // LogFile Footer
			}
			setTextProperties();
			strT = objOptions.replaceVars(strT);
			objJadeReportLogger.info(strT);
			sendNotifications();
		}
		return flgOK;
	}

	private void fillFileList(final String[] strFileList, final String strSourceDir) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::fillFileList";
		if (objOptions.MaxFiles.isDirty() == true && strFileList.length > objOptions.MaxFiles.value()) {
			for (int i = 0; i < objOptions.MaxFiles.value(); i++) {
				objSourceFileList.add(strFileList[i]);
			}
		}
		else {
			objSourceFileList.add(strFileList, strSourceDir);
		}
	} // private void fillFileList

	/* (non-Javadoc)
	 * @see com.sos.DataExchange.IJadeEngine#getFileList()
	 */
	public SOSFileList getFileList() {
		return objSourceFileList;
	}

	private long getPollTimeout() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPollTimeout";
		long lngPollTimeOut = 0;
		if (objOptions.poll_timeout.isDirty()) {
			lngPollTimeOut = objOptions.poll_timeout.value() * 60; // convert to seconds, poll_timeout is defined in minutes
		}
		else {
			lngPollTimeOut = objOptions.PollingDuration.getTimeAsSeconds();
		}
		return lngPollTimeOut;
	} // private long getPollTimeout

	private String getPollTimeoutText() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPollTimeoutText";
		String strPollTimeOut = "";
		if (objOptions.poll_timeout.isDirty()) {
			strPollTimeOut = objOptions.poll_timeout.Value();
		}
		else {
			strPollTimeOut = objOptions.PollingDuration.Value();
		}
		return strPollTimeOut;
	} // private long getPollTimeoutText

	private String[] getSingleFileNames() {
		final String conMethodName = conClassName + "::getSingleFileNames";
		Vector<String> filelist = new Vector();
		if (objOptions.file_path.IsNotEmpty() == true) {
			String filePath = objOptions.file_path.Value();
			logger.debug(String.format("single file(s) specified : '%1$s'", filePath));
			try {
				String localDir = objOptions.SourceDir.ValueWithFileSeparator();
				// TODO separator-char variable as Option
				String[] strSingleFileNameArray = filePath.split(";");
				for (String strSingleFileName : strSingleFileNameArray) {
					strSingleFileName = strSingleFileName.trim();
					if (strSingleFileName.length() > 0) {
						if (localDir.trim().length() > 0) {
							if (isAPathName(strSingleFileName) == false) {
								/**
								 * Problem with folders, when pgs run on Windows, but has to
								 * create a path for unix-systems.
								 */
								// strT = new File(localDir, strT).getAbsolutePath();
								strSingleFileName = localDir + strSingleFileName;
							}
						}
						filelist.add(strSingleFileName);
					}
				}
			}
			catch (Exception e) {
				String strM = String.format("error in  %1$s", conMethodName);
				logger.error(strM + e);
				throw new JobSchedulerException(strM, e);
			}
		}
		if (objOptions.FileListName.IsNotEmpty() == true) {
			String strFileListName = objOptions.FileListName.Value();
			JSFile objF = new JSFile(strFileListName);
			if (objF.exists() == true) {
				// TODO create method in JSFile: File2Array
				StringBuffer strRec = null;
				while ((strRec = objF.GetLine()) != null) {
					filelist.add(strRec.toString());
				}
				try {
					objF.close();
				}
				catch (IOException e) {
				}
			}
		}
		String[] strA = { "" };
		if (filelist.size() > 0) {
			strA = filelist.toArray(new String[filelist.size()]);
		}
		return strA;
	}

	/* (non-Javadoc)
	 * @see com.sos.DataExchange.IJadeEngine#getState()
	 */
	@Override public String getState() {
		String state = (String) objOptions.getTextProperties().get(conKeywordSTATE);
		return state;
	}

	@SuppressWarnings("unused") private ISOSVFSHandler getVFS() throws Exception {
		if (objVfs4Target == null) {
			objVfs4Target = VFSFactory.getHandler(objOptions.getDataTargetType());
		}
		return objVfs4Target;
	}

	private void handleZeroByteFiles() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::handleZeroByteFiles";
		if (objOptions.TransferZeroByteFilesNo() == true) {
			if (objSourceFileList.size() > 0) {
				boolean flgTransferZeroByteFilesNo = false;
				for (SOSFileListEntry objEntry : objSourceFileList.List()) {
					if (objEntry.getFileSize() > 0) { // zero byte size file
						flgTransferZeroByteFilesNo = true;
					}
					else {
						objSourceFileList.lngNoOfZeroByteSizeFiles++;
					}
				}
				if (flgTransferZeroByteFilesNo == false) { // all files are zbs files
					throw new JobSchedulerException("All files have zero byte size, transfer aborted");
				}
			}
		}
		Vector<SOSFileListEntry> objResultListClone = new Vector<SOSFileListEntry>();
		for (SOSFileListEntry objEntry : objSourceFileList.List()) { // just to avoid concurrent modification exception
			objResultListClone.add(objEntry);
		}
		for (SOSFileListEntry objEntry : objResultListClone) {
			if (objEntry.getFileSize() <= 0) { // zero byte size file
				if (objOptions.TransferZeroByteFilesStrict() == true) {
					throw new JobSchedulerException(String.format("zero byte size file detected: %1$s", objEntry.getSourceFilename()));
				}
				objEntry.setTransferSkipped();
				if (objOptions.remove_files.isTrue()) {
					objEntry.DeleteSourceFile();
				}
				objSourceFileList.lngNoOfZeroByteSizeFiles++;
				objSourceFileList.List().remove(objEntry);
			}
			else {
				// TODO Datei (nicht mehr) da? Fehler auslösen, weil in Liste enthalten.
			}
		}
	} // private void handleZeroByteFiles

	private void init() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::init";
		// logger is not yet initialized.
		//		logger.info(conClassName + " --- " + conSVNVersion);
	} // private void init

	protected boolean isAPathName(final String pstrFileAndPathName) {
		boolean flgOK = false;
		String lstrPathName = pstrFileAndPathName.replaceAll("\\\\", "/");
		if (lstrPathName.startsWith(".") || lstrPathName.startsWith("..")) { // relative to localdir
			flgOK = false;
		}
		else {
			if (lstrPathName.contains(":/") || lstrPathName.startsWith("/")) {
				flgOK = true;
			}
			else {
				// flgOK = (lstrPathName.contains("/") == true);
			}
		}
		return flgOK;
	}

	/* (non-Javadoc)
	 * @see com.sos.DataExchange.IJadeEngine#Logout()
	 */
	@Override public void Logout() {
		try {
			doLogout(objDataTargetClient);
			doLogout(objDataSourceClient);
		}
		catch (Exception e) {
		}
	}

	private void makeDirs() {
		if (objOptions.skip_transfer.isFalse()) {
			makeDirs(objOptions.TargetDir.Value());
		}
	} // private void makeDirs()

	private boolean makeDirs(final String pstrPath) {
		boolean cd = true;
		try {
			boolean flgMakeDirs = objOptions.makeDirs.value();
			String strTargetDir = pstrPath;
			if (flgMakeDirs) {
				if (objDataTargetClient.changeWorkingDirectory(strTargetDir)) {
					cd = true;
				}
				else {
					objDataTargetClient.mkdir(strTargetDir);
					cd = objDataTargetClient.changeWorkingDirectory(strTargetDir);
				}
			}
			else {
				if (strTargetDir != null && strTargetDir.length() > 0) {
					cd = objDataTargetClient.changeWorkingDirectory(strTargetDir);
				}
			}
			// TODO alternative_remote_dir, wozu und wie gehen wir damit um?
			if (cd == false && objOptions.alternative_remote_dir.IsNotEmpty()) {// alternative Parameter
				String alternativeRemoteDir = objOptions.alternative_remote_dir.Value();
				logger.debug("..try with alternative parameter [remoteDir=" + alternativeRemoteDir + "]");
				cd = objDataTargetClient.changeWorkingDirectory(alternativeRemoteDir);
				objOptions.TargetDir.Value(alternativeRemoteDir);
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException("..error in makeDirs: " + e, e);
		}
		return cd;
	}

	private long OneOrMoreSingleFilesSpecified(final String strSourceDir) {
		long lngNoOfFilesFound = 0;
		if (objOptions.skip_transfer.isFalse()) {
			objSourceFileList.add(getSingleFileNames(), strSourceDir);
			if (objOptions.isFilePollingEnabled() == true) {
				long lngCurrentPollingTime = 0;
				long lngPollInterval = objOptions.poll_interval.getTimeAsSeconds();
				long lngCurrentNumberOfFilesFound = objSourceFileList.size();
				long lngPollTimeOut = getPollTimeout();
				while (true) {
					lngNoOfFilesFound = 0;
					if (lngCurrentPollingTime > lngPollTimeOut) { // time exceeded ?
						logger.info(String.format("polling: time '%1$s' is over. polling terminated", getPollTimeoutText()));
						break;
					}
					for (SOSFileListEntry objItem : objSourceFileList.List()) {
						// long lngFileSize = objItem.getFileSize();
						if (objItem.SourceFileExists()) {
							lngNoOfFilesFound++;
							objItem.setParent(objSourceFileList); // to reinit the file-size
						}
					}
					if (lngNoOfFilesFound == lngCurrentNumberOfFilesFound) {
						break;
					}
					if (objOptions.poll_minfiles.value() > 0 && lngNoOfFilesFound >= objOptions.poll_minfiles.value()) {
						break;
					}
					String strM = String.format("file-polling: going to sleep for %1$d seconds. '%2$d' files found, waiting for '%3$d' files", lngPollInterval,
							lngNoOfFilesFound, lngCurrentNumberOfFilesFound);
					setInfo(strM);
					doSleep(lngPollInterval);
					lngCurrentPollingTime += lngPollInterval;
				}
			}
		}
		return lngNoOfFilesFound;
	}

	/* (non-Javadoc)
	 * @see com.sos.DataExchange.IJadeEngine#Options()
	 */
	@Override public JADEOptions Options() {
		if (objOptions == null) {
			objOptions = new JADEOptions();
		}
		return objOptions;
	}

	@Override public void setJadeOptions(final JSOptionsClass pobjOptions) {
		objOptions = (JADEOptions) pobjOptions;
	}

	private boolean printState(boolean rc) throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::printState";
		String state = "processing successful ended";
		try {
			int intNoOfFilesTransferred = (int) getSuccessfulTransfers();
			String strMsg = SOSJADE_E_0100.params(objOptions.file_spec.Value());
			int zeroByteCount = objSourceFileList.getZeroByteCount();
			switch (intNoOfFilesTransferred) {
				case 0:
					if (zeroByteCount > 0 && objOptions.TransferZeroByteFilesRelaxed()) {
						state = strMsg;
					}
					else
						if (zeroByteCount > 0 && objOptions.TransferZeroByteFilesRelaxed()) {
							throw new JobSchedulerException(SOSJADE_I_0104.get());
						}
						else {
							if (objOptions.force_files.isTrue()) {
								if (intNoOfFilesTransferred <= 0 && lngNoOfPollingServerFiles <= 0) {
									objJadeReportLogger.info(strMsg);
									throw new JobSchedulerException(strMsg);
								}
							}
							else {
								state = strMsg;
							}
						}
					rc = objOptions.force_files.value() == false ? true : !objOptions.TransferZeroByteFilesRelaxed();
					break;
				case 1:
					state = SOSJADE_I_0100.get();
					rc = true;
					break;
				default:
					state = SOSJADE_I_0101.params(intNoOfFilesTransferred);
					rc = true;
					break;
			}
			if (zeroByteCount > 0) {
				state += " " + SOSJADE_I_0102.params(zeroByteCount);
			}
			logger.debug(state);
			objJadeReportLogger.info(state);
			objOptions.getTextProperties().put(conKeywordSTATE, state);
			return rc;
		}
		catch (Exception e) {
			objJadeReportLogger.info(e.getLocalizedMessage());
			throw e;
		}
		finally {
			objOptions.getTextProperties().put(conKeywordSTATE, state);
		}
	}

	private void processSendMail(final SOSSmtpMailOptions pobjO) {
		if (pobjO != null && pobjO.FileNotificationTo.isDirty() == true) {
			try {
				String strA = pobjO.attachment.Value();
				if (pobjO.attachment.isDirty()) {
					strA += ";";
				}
				if (objOptions.log_filename.isDirty() == true) {
					String strF = objOptions.log_filename.getHtmlLogFileName();
					if (strF.length() > 0) {
						strA += strF;
					}
					strF = objOptions.log_filename.Value();
					if (strF.length() > 0) {
						if (strA.length() > 0) {
							strA += ";";
						}
						strA += strF;
					}
					if (strA.length() > 0) {
						pobjO.attachment.Value(strA);
					}
				}
				if (pobjO.subject.isDirty() == false) {
					String strT = "JADE: ";
					pobjO.subject.Value(strT);
				}
				String strM = pobjO.subject.Value();
				pobjO.subject.Value(objOptions.replaceVars(strM));
				strM = pobjO.body.Value();
				pobjO.body.Value(objOptions.replaceVars(strM));
				strM += "\n" + "List of transferred Files:" + "\n";
				for (SOSFileListEntry objListItem : objSourceFileList.List()) {
					String strSourceFileName = objListItem.getSourceFilename();
					strM += strSourceFileName + "\n";
				}
				if (pobjO.from.isDirty() == false) {
					pobjO.from.Value("JADE@sos-berlin.com");
				}
				SOSMail objMail = new SOSMail(pobjO.host.Value());
				logger.debug(pobjO.dirtyString());
				objMail.sendMail(pobjO);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	} // private void sendMail

	/* (non-Javadoc)
	 * @see com.sos.DataExchange.IJadeEngine#run()
	 */
	@Override public void run() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Run";
		try {
			this.Execute();
		}
		catch (Exception e) {
			throw new JobSchedulerException(EXCEPTION_RAISED.get(e), e);
		}
	}

	// TODO prüfen, ob eine Verlagerung in SOSFileList die bessere Lösung ist. Stichwort: Multithreading
	private void sendFiles(final SOSFileList pobjFileList) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::sendFiles";
		int intMaxParallelTransfers = 0;
		if (objOptions.ConcurrentTransfer.isTrue()) {
			// TODO resolve problem with apache ftp client in multithreading mode
			//			intMaxParallelTransfers = objOptions.MaxConcurrentTransfers.value();
		}
		int intNoOfFiles2Transfer = (int) pobjFileList.size();
		int intFileCount = 0;
		if (intMaxParallelTransfers <= 0 || objOptions.CumulateFiles.isTrue()) {
			for (SOSFileListEntry objSourceFile : pobjFileList.List()) {
				objSourceFile.Options(objOptions);
				objSourceFile.setDataSourceClient(objDataSourceClient);
				objSourceFile.setDataTargetClient(objDataTargetClient);
				objSourceFile.setConnectionPool4Source(objConFactory.getSourcePool());
				objSourceFile.setConnectionPool4Target(objConFactory.getTargetPool());
				intFileCount = intFileCount + 1;
				objSourceFile.run();
			}
		}
		else {
			SOSThreadPoolExecutor objTPE = new SOSThreadPoolExecutor(intMaxParallelTransfers);
			for (SOSFileListEntry objSourceFile : pobjFileList.List()) {
				objSourceFile.Options(objOptions);
				objSourceFile.setConnectionPool4Source(objConFactory.getSourcePool());
				objSourceFile.setConnectionPool4Target(objConFactory.getTargetPool());
				intFileCount = intFileCount + 1;
				objTPE.runTask(objSourceFile);
			}
			try {
				objTPE.shutDown();
				objTPE.objThreadPool.awaitTermination(1, TimeUnit.DAYS);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (objOptions.transactional.isFalse()) {
			pobjFileList.EndTransaction();
			sendTransferHistory();
		}
	}

	private void sendNotifications() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::sendNotifications";
		// TODO Nagios anbinden
		// TODO Status über MQ senden
		// TODO Fehler an JIRA, Peregrine, etc. senden. Ticket aufmachen.
		SOSSmtpMailOptions objO = objOptions.getMailOptions();
		if (objOptions.mail_on_success.isTrue() && objSourceFileList.FailedTransfers() <= 0 || objO.FileNotificationTo.isDirty() == true) {
			doProcessMail(enuMailClasses.MailOnSuccess);
		}
		if (objOptions.mail_on_error.isTrue() && (objSourceFileList.FailedTransfers() > 0 || JobSchedulerException.LastErrorMessage.length() > 0)) {
			doProcessMail(enuMailClasses.MailOnError);
		}
		if (objOptions.mail_on_empty_files.isTrue() && objSourceFileList.getZeroByteCount() > 0) {
			doProcessMail(enuMailClasses.MailOnEmptyFiles);
		}
	}

	private void setInfo(final String strInfoText) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setInfo";
		logger.info(strInfoText);
		objJSJobUtilities.setStateText(strInfoText);
	} // private void setInfo

	private void setTextProperties() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setTextProperties";
		// TODO das muß beim JobScheduler-Job in die Parameter zurück, aber nur da
		// siehe hierzu das Interface ...
		objOptions.getTextProperties().put(conKeywordSUCCESSFUL_TRANSFERS, String.valueOf(getSuccessfulTransfers()));
		objOptions.getTextProperties().put(conKeywordFAILED_TRANSFERS, String.valueOf(getFailedTransfers()));
		// return the number of transferred files
		if (JobSchedulerException.LastErrorMessage.length() <= 0) {
			objOptions.getTextProperties().put(conKeywordSTATUS, SOSJADE_T_0012.get());
		}
		else {
			objOptions.getTextProperties().put(conKeywordSTATUS, SOSJADE_T_0013.get());
		}
		objOptions.getTextProperties().put(conKeyWordLAST_ERROR, JobSchedulerException.LastErrorMessage);
	} // private void setTextProperties

	private long getSuccessfulTransfers() {
		long lngTransfers = 0;
		if (objSourceFileList != null) {
			lngTransfers = objSourceFileList.SuccessfulTransfers();
			if (lngNoOfPollingServerFiles > 0) {
				lngTransfers = lngNoOfPollingServerFiles;
			}
		}
		return lngTransfers;
	}

	private long getFailedTransfers() {
		long lngTransfers = 0;
		if (objSourceFileList != null) {
			lngTransfers = objSourceFileList.FailedTransfers();
		}
		return lngTransfers;
	}

	public ISOSVfsFileTransfer DataTargetClient() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::DataTargetClient";
		return objDataTargetClient;
	} // private ISOSVfsFileTransfer DataTargetClient

	/**
	 * Send files by  .... from source to a target
	 *
	 * @return boolean
	 * @throws Exception
	 */
	public boolean transfer() throws Exception {
		boolean flgReturnCode = false;
		try { // to connect, authenticate and execute commands
			logger.debug(Options().dirtyString());
			logger.debug("Source : " + Options().Source().dirtyString());
			logger.debug("Target : " + Options().Target().dirtyString());
			Options().CheckMandatory();
			logger.debug("Source : " + Options().Source().dirtyString());
			logger.debug("Target : " + Options().Target().dirtyString());
			setTextProperties();
			objSourceFileList = new SOSFileList(objVfs4Target);
			objSourceFileList.Options(objOptions);
			objSourceFileList.StartTransaction();
			// TODO separate Operations: (1) connect and (2) authenticate
			//			if (objConFactory == null) {
			objConFactory = new SOSVfsConnectionFactory(objOptions);
			//			}
			if (objOptions.LazyConnectionMode.isFalse() && objOptions.NeedTargetClient()) {
				objDataTargetClient = (ISOSVfsFileTransfer) objConFactory.getTargetPool().getUnused();
				objSourceFileList.objDataTargetClient = objDataTargetClient;
				makeDirs();
			}
			try {
				objDataSourceClient = (ISOSVfsFileTransfer) objConFactory.getSourcePool().getUnused();
				objSourceFileList.objDataSourceClient = objDataSourceClient;
				String strSourceDir = objOptions.SourceDir.Value();
				String strRemoteDir = objOptions.TargetDir.Value();
				long lngStartPollingServer = System.currentTimeMillis() / 1000;
				lngNoOfPollingServerFiles = 0;
				PollingServerLoop:
				while (true) {
					if (objOptions.isFilePollingEnabled() == true) {
						doPollingForFiles();
						if (objSourceFileList.size() <= 0 && objOptions.PollingServer.isFalse()) {
							if (objOptions.PollErrorState.isDirty()) {
								String strPollErrorState = objOptions.PollErrorState.Value();
								logger.info("set order-state to " + strPollErrorState);
								setNextNodeState(strPollErrorState);
								break PollingServerLoop;
							}
						}
					}
					else {
						if (objOptions.OneOrMoreSingleFilesSpecified()) {
							OneOrMoreSingleFilesSpecified(strSourceDir);
						}
						else {
							objDataSourceClient.changeWorkingDirectory(strSourceDir);
							ISOSVirtualFile objLocFile = objDataSourceClient.getFileHandle(strSourceDir);
							String strM = "";
							if (objOptions.NeedTargetClient() == true) {
								strM = "source directory/file: " + strSourceDir + ", target directory: " + strRemoteDir + ", file regexp: "
										+ objOptions.file_spec;
							}
							else {
								strM = SOSJADE_D_0200.params(strSourceDir, objOptions.file_spec.Value());
							}
							logger.debug(strM);
							selectFilesOnSource(objLocFile, objOptions.SourceDir, objOptions.file_spec, objOptions.recursive);
						}
					}
					// TODO checkSteadyStateOfFiles in FileListEntry einbauen
					if (checkSteadyStateOfFiles() == false) { // not all files are steady ...
						break PollingServerLoop;
					}
					if (objOptions.TransferZeroByteFiles() == false) {
						handleZeroByteFiles();
					} // (zeroByteFiles == false)
					try {
						if (objOptions.operation.isOperationGetList()) {
							String strM = SOSJADE_I_0115.get();
							logger.info(strM);
							objJadeReportLogger.info(strM);
							objOptions.remove_files.setFalse();
							objOptions.force_files.setFalse();
							objSourceFileList.CreateResultSetFile();
						}
						else {
							if (objOptions.CreateSecurityHashFile.isTrue() || objOptions.CheckSecurityHash.isTrue()) {
								objSourceFileList.createHashFileEntries(objOptions.SecurityHashType.Value());
							}
							if (objSourceFileList.size() > 0 && objOptions.skip_transfer.isFalse()) {
								if (objOptions.LazyConnectionMode.isTrue()) {
									objDataTargetClient = (ISOSVfsFileTransfer) objConFactory.getTargetPool().getUnused();
									objSourceFileList.objDataTargetClient = objDataTargetClient;
									makeDirs();
								}
								sendFiles(objSourceFileList);
								// execute postTransferCommands after renameAtomicTransferFiles (transactional=true)-Problem
								// http://www.sos-berlin.com/jira/browse/SOSFTP-186
								objSourceFileList.renameAtomicTransferFiles();
								if (objOptions.PostTransferCommands.IsNotEmpty()) {
									// TODO Command separator as global option
									for (String strCmd : objOptions.PostTransferCommands.split()) {
										strCmd = objOptions.replaceVars(strCmd);
										objDataTargetClient.getHandler().ExecuteCommand(strCmd);
									}
								}
								objSourceFileList.DeleteSourceFiles();
								if (objOptions.TransactionMode.isTrue()) {
									objSourceFileList.EndTransaction();
									sendTransferHistory();
								}
							}
						}
						// -----
						if (objOptions.PollingServer.isFalse() || objOptions.skip_transfer.isTrue()) {
							if (objOptions.NeedTargetClient() == true) {
								objDataTargetClient.close();
							}
							objDataSourceClient.close();
							break PollingServerLoop;
						}
						else {
							if (objOptions.pollingServerDuration.isDirty() && objOptions.PollingServerPollForever.isFalse()) {
								long lngActTime = System.currentTimeMillis() / 1000;
								long lngDuration = lngActTime - lngStartPollingServer;
								if (lngDuration >= objOptions.pollingServerDuration.getTimeAsSeconds()) {
									logger.debug("PollingServerMode: time elapsed. terminate polling server");
									break PollingServerLoop;
								}
							}
							logger.debug("PollingServerMode: start next polling cycle");
							// TODO check external end signal for Polling server
							// TODO end-time for polling server as an option
							//							sendNotifications();
							lngNoOfPollingServerFiles += objSourceFileList.size();
							objSourceFileList.clearFileList();
						}
					}
					catch (JobSchedulerException e) {
						String strM = TRANSACTION_ABORTED.get(e);
						logger.error(strM, e);
						objJadeReportLogger.info(strM);
						objSourceFileList.Rollback();
						throw e;
					}
				} // end while (true)
				flgReturnCode = printState(flgReturnCode);
				return flgReturnCode;
			}
			catch (Exception e) {
				flgReturnCode = false;
				throw e;
			}
			finally {
				setTextProperties();
			}
		}
		catch (Exception e) {
			setTextProperties();
			objOptions.getTextProperties().put(conKeywordSTATUS, SOSJADE_T_0013.get());
			String strM = SOSJADE_E_0101.params(e.getLocalizedMessage());
			objJadeReportLogger.info(strM, e);
			throw e;
		}
	}

	private void selectFilesOnSource(final ISOSVirtualFile objLocFile, final SOSOptionFolderName pobjSourceDir, final SOSOptionRegExp pobjRegExp4FileNames,
			final SOSOptionBoolean poptRecurseFolders) throws Exception {
		if (objLocFile.isDirectory() == true) {
			if (objDataSourceClient instanceof ISOSVfsFileTransfer2) {
				ISOSVfsFileTransfer2 objF = (ISOSVfsFileTransfer2) objDataSourceClient;
				objF.clearFileListEntries();
				objSourceFileList = objF.getFileListEntries(objSourceFileList, pobjSourceDir.Value(), pobjRegExp4FileNames.Value(), poptRecurseFolders.value());
			}
			else {
				String[] strFileList = objDataSourceClient.getFilelist(pobjSourceDir.Value(), pobjRegExp4FileNames.Value(), 0, poptRecurseFolders.value());
				fillFileList(strFileList, pobjSourceDir.Value());
			}
			setInfo(String.format("%1$d files found for regexp '%2$s'.", objSourceFileList.size(), pobjRegExp4FileNames.Value()));
		}
		else { // not a directory, seems to be a filename
			objSourceFileList.add(pobjSourceDir.Value());
		}
	}
	@SuppressWarnings("unused")
	private final SOSFileList	objParent							= null;
	long						lngNoOfTransferHistoryRecordsSent	= 0;

	/**
	 *
	 * \brief sendTransferHistory
	 *
	 * \details
	 * Send the transfer history for all transferred files to the background service.
	 *
	 * \return void
	 *
	 */
	public void sendTransferHistory() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::sendTransferHistory";
		String strBackgroundServiceHostName = objOptions.scheduler_host.Value();
		if (isEmpty(strBackgroundServiceHostName)) {
//			Messages is null
//			logger.debug(Messages.getMsg("No data sent to the background service due to missing host name"));
			logger.debug("No data sent to the background service due to missing host name");
		}
		else {
			for (SOSFileListEntry objEntry : objSourceFileList.List()) {
				if (sendTransferHistory4File(objEntry)) {
					lngNoOfTransferHistoryRecordsSent++;
				}
			}
			// TODO I18N
			logger.debug(String.format("%1$d transfer history records sent to background service", lngNoOfTransferHistoryRecordsSent));
		}
	} // private void sendTransferHistory
	SchedulerObjectFactory	objFactory	= null;

	/**
	 *
	 * \brief sendTransferHistory
	 *
	 * \details
	 * This methods sends for each file the needed informations about the transfer history
	 * to the background service (JobScheduler).
	 *
	 * \return void
	 *
	 */
	private boolean sendTransferHistory4File(final SOSFileListEntry pobjEntry) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::sendTransferHistory";
		Properties objSchedulerOrderParameterSet = pobjEntry.getFileAttributesAsProperties();
		// TODO custom-fields einbauen
		/**
		 * bei SOSFTP ist es mï¿½glich "custom" Felder zu definieren, die in der Transfer History als Auftragsparameter mitgeschickt werden.
		 * Damit man diese Felder identifizieren kann, werden hier Parameter defininiert, die beim Auftrag dabei sind, aber keine
		 * "custom" Felder sind
		 *
		 * ? alternativ Metadaten der Tabelle lesen (Spalten) und mit den Auftragsparameter vergleichen
		 */
		if (objFactory == null) {
			objFactory = new SchedulerObjectFactory(objOptions.scheduler_host.Value(), objOptions.scheduler_port.value());
			objFactory.initMarshaller(Spooler.class);
			objFactory.Options().TransferMethod.Value(objOptions.Scheduler_Transfer_Method.Value());
			objFactory.Options().PortNumber.Value(objOptions.scheduler_port.Value());
			objFactory.Options().ServerName.Value(objOptions.scheduler_host.Value());
		}
		JSCmdAddOrder objOrder = objFactory.createAddOrder();
		objOrder.setJobChain(objOptions.scheduler_job_chain.Value() /* "scheduler_sosftp_history" */);
		//		objOrder.setTitle(SOSVfs_Title_276.get());
		Params objParams = objFactory.setParams(objSchedulerOrderParameterSet);
		objOrder.setParams(objParams);
		//		logger.debug(objOrder.toXMLString());
		objOrder.run();
		boolean flgRet = true;
		return flgRet;
	} // private void sendTransferHistory
}
