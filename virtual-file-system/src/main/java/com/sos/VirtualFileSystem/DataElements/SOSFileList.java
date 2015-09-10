/**
 *
 */
package com.sos.VirtualFileSystem.DataElements;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.DataElements.JSDateFormat;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry.enuTransferStatus;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * @author KB
 *
 */
@I18NResourceBundle(
					baseName = "SOSVirtualFileSystem",
					defaultLocale = "en")
public class SOSFileList extends SOSVfsMessageCodes {
	@SuppressWarnings("unused")
	private final String					conSVNVersion						= "$Id$";
	private static String					conClassName						= "SOSFileList";
	private final static Logger				logger								= Logger.getLogger(SOSFileList.class);
	private final static Logger				objJadeReportLogger					= Logger.getLogger(VFSFactory.getLoggerName());
	private SOSFTPOptions					objOptions							= null;
	private Vector<SOSFileListEntry>		objFileListEntries					= new Vector<>();
	long									lngSuccessfulTransfers				= 0;
	long									lngFailedTransfers					= 0;
	long									lngSkippedTransfers					= 0;
	long									lngNoOfTransferHistoryRecordsSent	= 0;
	long									lngNoOfHistoryFileRecordsWritten	= 0;
	long									lngNoOfRecordsInResultSetFile		= 0;
	long									lngNoOfBytesTransferred				= 0;
	private boolean							transferCountersCounted				= false;
	@SuppressWarnings("unused")
	private SOSFileList						objParent							= null;
	private ISOSVFSHandler					objVFS								= null;
	public ISOSVfsFileTransfer				objDataTargetClient					= null;
	public ISOSVfsFileTransfer				objDataSourceClient					= null;
	private final JSDataElementDate			dteTransactionStart					= new JSDataElementDate(Now(), JSDateFormat.dfTIMESTAMPS);
	private final JSDataElementDate			dteTransactionEnd					= new JSDataElementDate(Now(), JSDateFormat.dfTIMESTAMPS);
//	public SchedulerObjectFactory			objFactory							= null;
	private final HashMap<String, String>	objSubFolders						= new HashMap<>();
	public int								lngNoOfZeroByteSizeFiles			= 0;
	private boolean							flgHistoryFileAlreadyWritten		= false;
	private boolean							flgResultSetFileAlreadyCreated		= false;

	public void VFSHandler(final ISOSVFSHandler pobjVFS) {
		objVFS = pobjVFS;
	}

	public ISOSVFSHandler VFSHandler() {
		return objVFS;
	}

	public HashMap<String, String> getSubFolderList() {
		return objSubFolders;
	}

	public boolean add2SubFolders(final String pstrSubFolderName) {
		boolean flgR = false;
		String strT = getSubFolderList().get(pstrSubFolderName);
		if (strT == null) {
			flgR = true;
			getSubFolderList().put(pstrSubFolderName, "");
		}
		return flgR;
	}

	public void setParent(final SOSFileList pobjParent) {
		objParent = pobjParent;
	}

	/**
	 *
	 */
	@SuppressWarnings("deprecation") public SOSFileList() {
		super(SOSVfsConstants.strBundleBaseName);
	}

	public SOSFileList(final ISOSVFSHandler pobjVFS) {
		this();
		this.VFSHandler(pobjVFS);
	}

	public SOSFTPOptions Options() {
		return objOptions;
	}

	public void Options(final SOSFTPOptions pobjOptions) {
		objOptions = pobjOptions;
	}

	public long count() {
		return objFileListEntries.size();
	}

	private void CountStatus() {
		if (!transferCountersCounted) {
			transferCountersCounted = true;
			lngSuccessfulTransfers = 0;
			lngFailedTransfers = 0;
			lngSkippedTransfers = 0;
			lngNoOfBytesTransferred = 0;
			if (objFileListEntries != null) {
				for (SOSFileListEntry objEntry : objFileListEntries) {
					if (objEntry == null) {
						continue;
					}
					lngNoOfBytesTransferred += objEntry.getFileSize();
					// What happens with IgnoredDueToZerobyteConstraint,
					// setBack, polling
					switch (objEntry.getTransferStatus()) {
					case transferred:
						lngSuccessfulTransfers++;
						break;
					case renamed:
						lngSuccessfulTransfers++;
						break;
					case deleted:
						lngSuccessfulTransfers++;
						break;
					case compressed:
						lngSuccessfulTransfers++;
						break;
					case transfer_has_errors:
						lngFailedTransfers++;
						break;
					case transfer_aborted:
						lngFailedTransfers++;
						break;
					case setBack:
						lngFailedTransfers++;
						break;
					case notOverwritten:
						lngSkippedTransfers++;
						break;
					case waiting4transfer:
						lngFailedTransfers++;
						break;
					case transfer_skipped:
						lngSkippedTransfers++;
						break;
					default:
						break;
					}
				}
			}
		}
	}

	public long NoOfBytesTransferred() {
		return lngNoOfBytesTransferred;
	}

	public long SuccessfulTransfers() {
		this.CountStatus();
		return lngSuccessfulTransfers;
	}

	public long FailedTransfers() {
		CountStatus();
		return lngFailedTransfers;
	}
	
	public long SkippedTransfers() {
		CountStatus();
		return lngSkippedTransfers;
	}

	/**
	 *
	 * \brief SOSFileList
	 *
	 * \details
	 *
	 * @param pstrFileList
	 */
	public SOSFileList(final String[] pstrFileList) {
		this();
		for (String strFileName : pstrFileList) {
			this.add(strFileName);
		}
	}

	public Vector<SOSFileListEntry> List() {
		if (objFileListEntries == null) {
			objFileListEntries = new Vector<>();
		}
		return objFileListEntries;
	}

	/**
	 *
	 * \brief SOSFileList
	 *
	 * \details
	 *
	 * @param pvecFileList
	 */
	public SOSFileList(final Vector<File> pvecFileList) {
		this();
		for (File fleFileName : pvecFileList) {
			this.add(fleFileName.getAbsolutePath());
		}
	}

	/**
	 *
	 * \brief addAll
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pobjFileList
	 */
	public void addAll(final SOSFileList pobjFileList) {
		for (SOSFileListEntry objFile : pobjFileList.List()) {
			this.add(objFile.SourceFileName());
		}
	}

	public void add(final String[] pstrA, final String pstrFolderName) {
		add(pstrA, pstrFolderName, false);
	}
	
	public void add(final String[] pstrA, final String pstrFolderName, boolean withExistCheck) {
		if (pstrA == null) {
			return;
		}
		for (String strFileName : pstrA) {
			try {
				if (withExistCheck && objDataSourceClient.getFileHandle(strFileName).FileExists()) {
					this.add(strFileName);
				}
				else if (!withExistCheck) {
					this.add(strFileName);
				}
			} catch (Exception e) {
				if (withExistCheck) {
					throw new JobSchedulerException(e);
				}
			}
		}
	}

	public void addFileNames(final Vector<String> pstrA) {
		for (String strFileName : pstrA) {
			this.add(strFileName);
		}
	}

	public void addFiles(final Vector<File> pfleA) {
		for (File fleFileName : pfleA) {
			this.add(fleFileName.getAbsolutePath());
		}
	}

	/**
	 *
	 * \brief add
	 *
	 * \details
	 *
	 * \return SOSFileListEntry
	 *
	 * @param pstrLocalFileName
	 * @return
	 */
	public SOSFileListEntry add(final String pstrLocalFileName) {
		if (objFileListEntries == null) {
			objFileListEntries = new Vector<SOSFileListEntry>();
		}
		SOSFileListEntry objEntry = this.Find(pstrLocalFileName);
		if (objEntry == null) {
			objEntry = new SOSFileListEntry(pstrLocalFileName); 
			objEntry.VfsHandler((ISOSVfsFileTransfer) objVFS);
			objEntry.setParent(this);
			objFileListEntries.add(objEntry);
			objEntry.Options(objOptions);
			if (objOptions.skip_transfer.isFalse()) {
				objEntry.setStatus(SOSFileListEntry.enuTransferStatus.waiting4transfer);
			}
			else {
				objEntry.setStatus(SOSFileListEntry.enuTransferStatus.transfer_skipped);
			}
		}
		return objEntry;
	}

	public void clearFileList() {
		objFileListEntries = new Vector<>();
	}

	public SOSFileListEntry add(final SOSFileListEntry pobjFileListEntry) {
		if (objFileListEntries == null) {
			objFileListEntries = new Vector<>();
		}
		SOSFileListEntry objEntry = this.Find(pobjFileListEntry.getSourceFilename());
		if (objEntry == null) {
			pobjFileListEntry.VfsHandler((ISOSVfsFileTransfer) objVFS);
			pobjFileListEntry.setParent(this);
			objFileListEntries.add(pobjFileListEntry);
			pobjFileListEntry.Options(objOptions);
			if (objOptions.skip_transfer.isFalse()) {
				pobjFileListEntry.setStatus(SOSFileListEntry.enuTransferStatus.waiting4transfer);
			}
			else {
				pobjFileListEntry.setStatus(SOSFileListEntry.enuTransferStatus.transfer_skipped);
			}
		}
		return pobjFileListEntry;
	}

	public SOSFileListEntry Find(final String pstrLocalFileName) {
		// TODO for performance a hashmap should be considered
		for (SOSFileListEntry objEntry : objFileListEntries) {
			if (objEntry == null) {
				continue;
			}
			String strT = objEntry.SourceFileName();
			if (strT == null) {
				continue;
			}
			// SOSFTP-185 Filenames must be compared by respecting upper-/lowercase-letters, otherwise unix-like filenames are not unique
			//			if (pstrLocalFileName.equalsIgnoreCase(strT)) {
			if (fileNamesAreEqual(pstrLocalFileName, strT, true)) {
				return objEntry;
			}
		}
		return null;
	}

	public int getZeroByteCount() {
		return lngNoOfZeroByteSizeFiles;
	}

	/**
	 * Erst wenn alle Dateien erfolgreich transferieriert wurden, dann sollen die lokalen Dateien geloescht werden.
	 * Parameter = objOptions.transactional.value() = yes und remove_files=yes
	 * @throws Exception
	 */
	public void deleteSourceFiles() throws Exception {
		if (objOptions.remove_files.isTrue()) {
			boolean filesDeleted = false;
			for (SOSFileListEntry entry : objFileListEntries) {
				if (entry.getTransferStatus() == enuTransferStatus.transferred) {
					if (filesDeleted == false) {
						filesDeleted = true;
						logger.debug(SOSVfs_D_208.get());
					}
					entry.DeleteSourceFile();
				}
			}
		}
	}

	public void logFileList() {
		String strT = "";
		for (SOSFileListEntry objListItem : objFileListEntries) {
			String strFileName = objListItem.getFileName4ResultList();
			strT += "\n" + strFileName;
		}
		logger.info(strT);
		objJadeReportLogger.info(strT);
	}

	/**
	 *
	 *
	 * \brief SetFile
	 *
	 * \details
	 * The content of the ResultSet File is a list of all Source-File Names.
	 *
	 * \return void
	 *
	 */
	public void CreateResultSetFile() {
		if (flgResultSetFileAlreadyCreated == true) {
			return;
		}
		flgResultSetFileAlreadyCreated = true;
		try {
			//if (objOptions.CreateResultSet.isTrue()) {
				if (objOptions.ResultSetFileName.isDirty() && objOptions.ResultSetFileName.IsNotEmpty()) {
					// TODO use the file object from the option
					JSFile objResultSetFile = objOptions.ResultSetFileName.JSFile();
					for (SOSFileListEntry objListItem : objFileListEntries) {
						String strFileName = objListItem.getFileName4ResultList();
						objResultSetFile.WriteLine(strFileName);
						lngNoOfRecordsInResultSetFile++;
					}
					objResultSetFile.close();
					logger.info(String.format("ResultSet to '%1$s' is written", objResultSetFile.getAbsoluteFile()));
				}
			//}
		}
		catch (Exception e) {
			throw new JobSchedulerException("Problems occured creating ResultSetFile", e);
		}
	}

	/**
	 * Erst beim erfolgreichen Transfer aller Dateien, wird der atomic suffix umbenannt
	 * Bedingung: Parameter objOptions.transactional.value() = yes
	 * @throws Exception
	 */
	public void renameAtomicTransferFiles() throws Exception {
		final String conMethodName = conClassName + "::renameAtomicTransferFiles";
		try {
			if (objOptions.isAtomicTransfer() && objOptions.transactional.isTrue()) {
				logger.debug(SOSVfs_D_209.get());
				for (SOSFileListEntry objListItem : objFileListEntries) {
					if (objListItem.isNotOverwritten()) {
						continue;
					}
					
					String strTargetTransferName = objListItem.TargetTransferName();
					String strToFilename = MakeFullPathName(objOptions.TargetDir.Value(), objListItem.TargetFileName());
					if (!fileNamesAreEqual(objListItem.TargetFileName(), strTargetTransferName, false)) { // SOSFTP-142
						ISOSVirtualFile objF = null;
						if (objOptions.overwrite_files.isTrue() && objListItem.FileExists() == true) {
							objListItem.setTargetFileAlreadyExists(true);
							objF = objDataTargetClient.getFileHandle(strToFilename);
							objF.delete();
						}
						if (objListItem.TargetFileName().contains("/") && objOptions.makeDirs.isTrue()) {
							String parent = objListItem.normalized(new File(strToFilename).getParent());
							objDataTargetClient.mkdir(parent);
						}
						objF = objDataTargetClient.getFileHandle(MakeFullPathName(objOptions.TargetDir.Value(), strTargetTransferName));
						objF.rename(strToFilename);
						objListItem.createChecksumFile(strToFilename);
						objListItem.executePostCommands();
						// if cumulative_files is true then this loop has to much entries
						if (objOptions.CumulateFiles.isTrue()) {
							break;
						}
					}
				}
			}
			else {
				for (SOSFileListEntry objListItem : objFileListEntries) {
					objListItem.executePostCommands();
				}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(SOSVfs_E_210.params(conMethodName, e.toString()), e);
		}
	}

	/**
	 *
	 * \brief EndTransaction
	 *
	 * \details
	 * This routine has to be called anyway. It belongs not to transactional mode.
	 *
	 * \return void
	 *
	 */
	public void EndTransaction() {
		dteTransactionEnd.setParsePattern(JSDateFormat.dfTIMESTAMPS);
		dteTransactionEnd.Value(Now());
//		this.sendTransferHistory();
		this.writeTransferHistory();
		CreateResultSetFile();
	}


	public void StartTransaction() {
		dteTransactionStart.setParsePattern(JSDateFormat.dfTIMESTAMPS);
		dteTransactionStart.Value(Now());
	}

	public void Rollback() {
		
		String msg = null;
		if(objOptions.transactional.value()){
			msg = SOSVfs_I_211.get();
		}
		else{
			msg = "Rollback aborted files.";
		}
		logger.info(msg);
		objJadeReportLogger.info(msg);

		// TODO check out, on which stage the process was aborted. Example: 1) source2target, 2)
		// renameAtomic, 3) DeleteSource, 4) deleteOnly, 5) renameOnly
		if (objOptions.isAtomicTransfer()) {
			for (SOSFileListEntry entry : objFileListEntries) {
				if (!objOptions.transactional.value()) {
					if(entry.getTransferStatus().equals(enuTransferStatus.transferred)){
						continue;
					}
				}
				entry.setStatus(enuTransferStatus.transfer_aborted);
				
				String atomicFileName = entry.getAtomicFileName();
				if (atomicFileName.isEmpty()) {
					continue;
				}
				atomicFileName = MakeFullPathName(objOptions.TargetDir.Value(),	entry.getAtomicFileName());
				if (isNotEmpty(entry.getAtomicFileName())) {
					try {
						ISOSVirtualFile atomicFile = objDataTargetClient.getFileHandle(atomicFileName);
						if (atomicFile.FileExists()) {
							atomicFile.delete();
						}
						String strT = SOSVfs_D_212.params(atomicFileName);
						logger.debug(strT);
						objJadeReportLogger.info(strT);
						entry.setAtomicFileName(EMPTY_STRING);
						entry.setStatus(enuTransferStatus.setBack);
					} catch (Exception e) {
						logger.error(e.toString());
					}
				}
				if (!entry.isTargetFileAlreadyExists()) {
					try {
						String strTargetFilename = MakeFullPathName(objOptions.TargetDir.Value(), entry.TargetFileName());
						ISOSVirtualFile targetFile = objDataTargetClient.getFileHandle(strTargetFilename);
						if(targetFile.FileExists()) {
							targetFile.delete();
						}
						msg = SOSVfs_D_212.params(targetFile.getName());
						logger.debug(msg);
						objJadeReportLogger.info(msg);
						entry.setStatus(enuTransferStatus.setBack);
					} catch (Exception e) {
						logger.error(e.toString());
					}
				}
				if (entry.hasChecksumFile()) {
					try {
						ISOSVirtualFile checksumFile = entry.getChecksumFile();
						if(checksumFile.FileExists()) {
							checksumFile.delete();
						}
						msg = SOSVfs_D_212.params(checksumFile.getName());
						logger.debug(msg);
						objJadeReportLogger.info(msg);
					} catch (Exception e) {
						logger.error(e.toString());
					}
				}
			}
		}
		else{
			for (SOSFileListEntry entry : objFileListEntries) {
				if(entry.getTransferStatus().equals(enuTransferStatus.transferred)){
					continue;
				}
				entry.setStatus(enuTransferStatus.transfer_aborted);
			}
		}
		if (!objOptions.transactional.value()) {
			try {
				deleteSourceFiles();
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
		this.EndTransaction();
	}
	
	/**
	 * Felder der Historiendatei
	 */
	private final String	historyFields		= "guid;mandator;transfer_end;pid;ppid;operation;localhost;localhost_ip;local_user;remote_host;remote_host_ip;remote_user;protocol;port;local_dir;remote_dir;local_filename;remote_filename;file_size;md5;status;last_error_message;log_filename";
	/**
	 * neue Felder der Historiendatei. Der Aufbau ist wie folgt: historyFields;<history_entry_>;newHistoryFields
	 */
	private final String	newHistoryFields	= "jump_host;jump_host_ip;jump_port;jump_protocol;jump_user;transfer_start;modification_date";

	public void writeTransferHistory() {
		if (flgHistoryFileAlreadyWritten == true) {
			return;
		}
		flgHistoryFileAlreadyWritten = true;
		dteTransactionEnd.FormatString(JSDateFormat.dfTIMESTAMPS);
		dteTransactionStart.FormatString(JSDateFormat.dfTIMESTAMPS);
		long duration = dteTransactionEnd.getDateObject().getTime() - dteTransactionStart.getDateObject().getTime();
		String operation = objOptions.operation.Value();
		String msg = SOSVfs_D_213.params(dteTransactionStart.FormattedValue(), dteTransactionEnd.FormattedValue(), duration, operation);
		logger.debug(msg);
		objJadeReportLogger.info(msg);
		String historyFileName = objOptions.HistoryFileName.Value();
		JSFile historyFile = null;
		try {
			if (objOptions.HistoryFileName.isDirty()) {
				historyFile = objOptions.HistoryFileName.JSFile();
				if (objOptions.HistoryFileAppendMode.isTrue()) {
					historyFile.setAppendMode(true);
					if (!historyFile.exists()) {
						historyFile.WriteLine(historyFields + ";" + newHistoryFields);
					}
				} else {
					historyFile.WriteLine(historyFields + ";" + newHistoryFields);
				}
				for (SOSFileListEntry entry : objFileListEntries) {
					// TODO  CSV or XML or Hibernate?
					historyFile.WriteLine(entry.toCsv());
					lngNoOfHistoryFileRecordsWritten++;
				}
				
				logger.info(String.format("%s records written to history file '%s', HistoryFileAppendMode = %s", lngNoOfHistoryFileRecordsWritten, 
						historyFileName,
						objOptions.HistoryFileAppendMode.Value()));
			}
		}
		catch (Exception e) {
			logger.warn(String.format("Error occured during writing of the history file: %s",e.toString()));
		}
		finally{
			if(historyFile != null){
				try{
					historyFile.close();
				}
				catch(Exception ex){}
			}
		}
		for (SOSFileListEntry entry : objFileListEntries) {
			if (entry.TargetFileName().isEmpty() == false) {
				msg = entry.toString();
				logger.trace(msg);
				objJadeReportLogger.info(msg);
			}
		}
	}

	public long size() {
		long intS = 0;
		if (List() != null) {
			intS = this.List().size();
		}
		return intS;
	}
	
	private boolean fileNamesAreEqual(String filenameA, String filenameB, boolean caseSensitiv) {
		String a = filenameA.replaceAll("[\\\\/]+", "/");
		String b = filenameB.replaceAll("[\\\\/]+", "/");
		return (caseSensitiv) ? a.equals(b) : a.equalsIgnoreCase(b);
	}
}
