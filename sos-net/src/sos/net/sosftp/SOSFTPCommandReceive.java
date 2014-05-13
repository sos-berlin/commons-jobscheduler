package sos.net.sosftp;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sos.net.SOSFTP;
import sos.net.SOSFTPCommand;
import sos.net.SOSFileTransfer;
import sos.util.SOSFileOperations;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

public class SOSFTPCommandReceive extends SOSFTPCommand {

	@SuppressWarnings("unused")
	String						conSVNVersion						= "$Id: SOSFTPOptions.java 17481 2012-06-29 15:40:36Z kb $";

	private static final String	conTextFILES_REMOVE					= " files removed.";
	public static final String	conTransferModeASCII				= "ascii";
	public static final String	conReturnValueFAILED_TRANSFERS		= "failed_transfers";
	public static final String	conReturnValueSUCCESSFUL_TRANSFERS	= "successful_transfers";
	public static final String	FTP_SERVER_REPLY					= "..ftp server reply [";
	public static final String	conClosingBracketWithColon			= "]: ";
	public static final String	FTP_SERVER_REPLY_CD_REMOTE_DIR		= "..ftp server reply [cd] [remoteDir=";

	private static String		conClassName						= "SOSFTPCommandReceive";

	// /** sos.util.SOSString Object */
	// private static SOSString sosString = new SOSString();

	boolean						rc									= false;

	/** ftp, sftp or ftps. If sftp is chosen, the ssh_* parameters will be considered. Default ftp*/
	String						protocol							= "ftp";
	boolean						sshBasedProtocol					= false;

	/** Transfer mode can be either ascii or binary. Default binary */
	private String				transferMode						= "binary";

	/** Passive mode is often used with firewalls. Valid values are 0 or 1 . Default 0. */
	private boolean				passiveMode							= false;

	/** Directory on the FTP server from which files should be transferred. */
	private String				remoteDir							= "";
	private final String				defaultRemoteDir					= "./";

	/** Local directory into which files should be transferred. */
	private String				localDir							= ".";

	/** This parameter specifies a regular expression to select the files to be transferred by FTP from a remote directory. */
	private String				fileSpec							= conRegExpAllChars;

	/** This parameter specifies whether target files should created with a suffix such as "~", and should be renamed to the target file name after the file transfer is completed. */
	private String				atomicSuffix						= "";

	/** This parameter determines whether the original file size and the number of bytes transferred should be compared after a file transfer and whether an error should be raised if they do not match*/
	private boolean				checkSize							= true;

	/** This parameter specifies the interval in seconds between two file transfer trials, if repeated transfer of files has been configured using the ftp_check_retry parameter*/
	private long				checkInterval						= 60;

	/** This parameter specifies whether a file transfer should be repeated in order to ensure that the file was complete when this job started. This is relevant for Unix systems that allow read and write access to files at the same time. */
	private long				checkRetry							= 0;

	/** This parameter specifies if existing local files should be overwritten */
	private boolean				overwriteFiles						= true;

	/** This parameter specifies whether the content of the source files should be appended to the target files if the target files exist. */
	private boolean				appendFiles							= false;

	/** This parameter specifies whether files on the FTP server should be removed after transfer. */
	private boolean				removeFiles							= false;

	/** If this Parameter is set to "true", everything except the transfer itself will be performed. This can be used to just trigger for files or to only delete files on the ftp server. */
	private boolean				skipTransfer						= false;

	/** This parameter specifies whether an error should be raised if no files could be found for transfer. */
	private boolean				forceFiles							= true;

	/** notification by e-mail in case of transfer of empty files. */
	private boolean				zeroByteFiles						= true;
	private boolean				zeroByteFilesStrict					= false;
	private boolean				zeroByteFilesRelaxed				= false;
	private String				fileNotificationTo					= "";
	private String				fileNotificationCC					= "";
	private String				fileNotificationBCC					= "";
	private String				fileNotificationSubject				= "";
	private String				fileNotificationBody				= "";
	private String				fileZeroByteNotificationTo			= "";
	private String				fileZeroByteNotificationCC			= "";
	private String				fileZeroByteNotificationBCC			= "";
	private String				fileZeroByteNotificationSubject		= "";
	private String				fileZeroByteNotificationBody		= "";

	private final HashMap<File, File>	checkFileList						= new HashMap<File, File>();

	/** This parameter specifies if files from subdirectories should be transferred recursively*/
	private boolean				recursive							= false;

	/** Polling for files */
	private int					pollTimeout							= 0;
	private int					pollIntervall						= 60;
	private int					pollMinFiles						= 1;
	private String				pollFilesErrorState					= "";

	/** This parameter accepts the absolute name and path of file at the FTP server that should be transferred. The file name has to include both path and name of the file at the FTP server. */
	private String				filePath							= "";
	private boolean				isFilePath							= false;

	/** Use of alternative access data and credentials */
	private String				alternativeHost						= "";
	private int					alternativePort						= 0;
	private String				alternativeUser						= "";
	private String				alternativePassword					= "";
	private String				alternativeAccount					= "";
	private String				alternativeRemoteDir				= "";
	private boolean				alternativePassiveMode				= false;
	private String				alternativeTransferMode				= "binary";

	/** count of successful transer file*/
	private int					successful_transfers				= 0;

	/** count of failed transer file*/
	private int					failed_transfer						= 0;

	private Pattern				pattern								= null;
	private static int			count								= 0;
	// hilfsvariablen
	private String				hlocalFile							= "";
	private String				htargetFile							= "";
	private int					zeroByteCount						= 0;

	private ArrayList<String>	transActionalLocalFiles				= null;

	private String				transActionalRemoteFiles			= null;
	/* List of Files in nontransactional mode which has to be possibiliy deleted after transfer, e.g. from the source of a jump-server */
	private String				strFilesRemainingOnSource			= null;

	/** testmode is on. No files will be transfered. */
	private boolean				testmode							= false;

	private static String		state								= "";

	private boolean				simpleTransfer						= false;

	/**
	 * Konstructor
	 * @param settingsFile
	 * @param settingsSection
	 * @param logger
	 * @param arguments_
	 */
	public SOSFTPCommandReceive(final SOSLogger logger, final Properties arguments_) throws Exception {
		super(logger, arguments_);
	}

	public SOSFTPCommandReceive(final sos.configuration.SOSConfiguration sosConfiguration_, final SOSLogger logger) throws Exception {
		super(sosConfiguration_, logger);
	}

	/**
	 * Receive files by FTP/SFTP from a remote server
	 *
	 * @return boolean
	 * @throws Exception
	 */
	@Override
	public boolean receive() throws Exception {

		getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());

		try {

			arguments.put(conReturnValueSUCCESSFUL_TRANSFERS, "0");
			arguments.put(conReturnValueFAILED_TRANSFERS, "0");

			if (this.getLogger() == null)
				this.setLogger(new SOSStandardLogger(0));

			readSettings(true);

			if (!doPreCommands()) {
				writeHistory(hlocalFile, htargetFile);
				return false;
			}

			try { // to get the parameters
				getParameter();
				getFileNotificationParameter();
			}
			catch (Exception e) {
				rc = false;
				e.printStackTrace(System.err);
				throw new RuntimeException("could not process job parameters: " + e.getMessage());
			}

			// es wird nicht transferiert. Der jump host soll die ANzahl der Dateien auf der ftp server überprüfen.
			// Der filelist soll auch mit Dateinamen im targethost gefüllt werden. Dieses
			// passiert in der methode execute.
			if (skipTransfer && flgJumpTransferDefined)
				return true;

			simpleTransfer = isSimpleTransfer();
			if (simpleTransfer)
				return receiveSimpleTransfer();

			printParameter();

			try { // to check parameters
				if (host == null || host.length() == 0)
					RaiseException("no host was specified");
				if (user == null || user.length() == 0)
					RaiseException("no user was specified");
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				rc = false;
				throw new RuntimeException("invalid or insufficient parameters: " + e.getMessage());
			}

			try { // to process ftp
				if (localDir.startsWith(conRegExpBackslash)) {
					while (localDir.indexOf("\\") != -1) {
						localDir = localDir.replace('\\', '/');
					}
				}
				if (localDir.startsWith("file://")) {
					if (!new File(createURI(localDir)).exists()) {
						RaiseException("local directory does not exist or is not accessible: " + localDir);
					}
				}

				this.getLogger().debug1(
						"connecting to host " + host + ", port " + port + ", local directory " + localDir + ", remote directory " + remoteDir
								+ (isFilePath ? ", file " + filePath : ", file specification " + fileSpec));

				boolean alternativeUse = true;// Wiederholungskriterien
				int isAlternativeParameterUse = 0;// Abbruchbedingug, versuch mit alternative Parameter nur einmal
				while (alternativeUse && isAlternativeParameterUse <= 1) {
					try {

						initFTPServer();
						alternativeUse = false;
					}
					catch (Exception ex) {
						if (isAlternativeParameterUse <= 0 && alternativeUse) {
							this.getLogger().reset(); // if the alternative connect is working we should have no error message   http://www.sos-berlin.com/jira/browse/SOSFTP-113
						}

						this.getLogger().debug1("..error in ftp server init with [host=" + host + "], [port=" + port + "] " + ex.getMessage());
						alternativeUse = alternativeHost.concat(alternativeUser).concat(alternativePassword).concat(alternativeAccount).length() > 0 || alternativePort != 0;

						if (alternativeUse && isAlternativeParameterUse == 0) {
							//  http://www.sos-berlin.com/jira/browse/SOSFTP-113
							getLogger().reset();  // reset last error due to second try with alternate credentials
							if (ftpClient != null) {
								if (isLoggedIn)
									try {
										ftpClient.logout();
									}
									catch (Exception e) {
									} // no error handling
								if (ftpClient.isConnected())
									try {
										ftpClient.disconnect();
									}
									catch (Exception e) {
									} // no error handling
							}

							isAlternativeParameterUse++;
							setAlternativeParameter();
							//  http://www.sos-berlin.com/jira/browse/SOSFTP-113
							arguments.put("user", user);   // an uggly adjust to keep the protocol tidy
							this.getLogger().debug1(
									"..try login with alternative parameter [host=" + host + "], [port=" + port + "] " + "[user=" + user + "], [account="
											+ account + "], [remoteDir=" + remoteDir + "], [passiveMode=" + passiveMode + "], " + "[transferMode="
											+ transferMode + "]");

						}
						else {
							RaiseException("..error in ftp server init with [host=" + host + "], [port=" + port + "] " + ex.getMessage());
						}

					}
				}

				if (!isLoggedIn) {
					RaiseException(".. server reply [login failed] [user=" + user + "], [account=" + account + "]: " + ftpClient.getReplyString());
				}

				if (ftpClient instanceof SOSFTP) {
					SOSFTP sosftp = (SOSFTP) ftpClient;
					if (passiveMode) {
						sosftp.passive();
						if (sosftp.getReplyCode() > ERROR_CODE) {
							RaiseException("..ftp server reply [passive]: " + ftpClient.getReplyString());
						}
						else {
							this.getLogger().debug("..ftp server reply [passive]: " + ftpClient.getReplyString());
						}
						/**
						 * this call for "enterLocalPassiveMode" seems to be doubled, because the sosftp.passive()
						 * is the same functionality.
						 * Unfortunately the "enterLocalPassiveMode" did not reset the ReplyString, therefore
						 * a call to this method will result into an error, if the previous command returns a > 500 response.
						 *
						 */
						sosftp.enterLocalPassiveMode();
						/**
						 * JIRA SOSFTP-93
						 * this method is not changing the Replystring. Therefore we don't have to check the response.
						 */
						// if (sosftp.getReplyCode() > ERROR_CODE) {
						// RaiseException("..ftp server reply [enterLocalPassiveMode]: " + ftpClient.getReplyString());
						// }
						// else {
						// this.getLogger().debug("..ftp server reply [enterLocalPassiveMode]: " + ftpClient.getReplyString());
						// }

					}

					if (transferMode.equalsIgnoreCase(conTransferModeASCII)) {
						if (sosftp.ascii()) {
							this.getLogger().debug("..using ASCII mode for file transfer");
							this.getLogger().debug("..ftp server reply" + " [ascii]: " + ftpClient.getReplyString());
						}
						else {
							RaiseException(".. could not switch to ASCII mode for file transfer ..ftp server reply [ascii]: " + ftpClient.getReplyString());
						}
					}
					else {
						if (sosftp.binary()) {
							this.getLogger().debug("using binary mode for file transfers.");
							this.getLogger().debug("..ftp server reply" + " [binary]: " + ftpClient.getReplyString());
						}
						else {
							RaiseException(".. could not switch to binary mode for file transfer ..ftp server reply [ascii]: " + ftpClient.getReplyString());
						}
					}
					if (strPreFtpCommands.length() > 0) {
						String[] strA = strPreFtpCommands.split(";");
						for (String strCmd : strA) {
							this.getLogger().debug("..try to send [" + strCmd + "] to the Server ");
							sosftp.sendCommand(strCmd);
							this.getLogger().debug(FTP_SERVER_REPLY + strCmd + conClosingBracketWithColon + ftpClient.getReplyString());
						}
					}

					if (strControlEncoding.length() > 0) {
						sosftp.setControlEncoding(strControlEncoding);
						this.getLogger().debug(FTP_SERVER_REPLY + strControlEncoding + conClosingBracketWithColon + ftpClient.getReplyString());
					}
				}

				if (isFilePath) {
					String cfilePath = filePath.split(";")[0];
					String currRemoteDir = "";

					if (sosString.parseToString(remoteDir).length() > 0 && !sosString.parseToString(remoteDir).equals(defaultRemoteDir))
						currRemoteDir = remoteDir;
					else
						if (new File(cfilePath).getParent() != null)
							currRemoteDir = new File(cfilePath).getParent().replaceAll(conRegExpBackslash, "/");

					if (sosString.parseToString(currRemoteDir).length() > 0) {
						if (!ftpClient.changeWorkingDirectory(currRemoteDir)) {
							RaiseException("..ftp server reply" + " [cd] [directory file_path=" + currRemoteDir + conClosingBracketWithColon
									+ ftpClient.getReplyString());
						}
						else {
							getLogger().debug(
									"..ftp server reply" + " [cd] [directory file_path=" + currRemoteDir + conClosingBracketWithColon
											+ ftpClient.getReplyString());
						}
					}
				}
				else
					if (sosString.parseToString(remoteDir).length() > 0) {
						if (!ftpClient.changeWorkingDirectory(remoteDir)) {
							RaiseException(FTP_SERVER_REPLY_CD_REMOTE_DIR + remoteDir + conClosingBracketWithColon + ftpClient.getReplyString());
						}
						else {
							getLogger().debug(FTP_SERVER_REPLY_CD_REMOTE_DIR + remoteDir + conClosingBracketWithColon + ftpClient.getReplyString());
						}
					}

				if (isFilePath) {
					filelist = new Vector<String>();
					String[] split = filePath.split(";");
					for (String element : split) {
						if (element != null && element.length() > 0)
							filelist.add(element);
					}
					/**
					 * it is not clear to me why fileSpec is initialized with this regexp,
					 * because fileSpec is not relevant at all if isFilePath is in effect.
					 * kb
					 */
					fileSpec = conRegExpAllChars; // ".*";
				}
				else {
					filelist = ftpClient.nList(recursive);
					getLogger().debug("..ftp server reply [nlist]: " + ftpClient.getReplyString());
				}
				count = 0;
				zeroByteCount = 0;

				pattern = Pattern.compile(fileSpec, 0);

				if (!polling()) {
					return false;
				}
				/**
				 * \change kb 2011-05-12
				 * nach dem polling muß die filelist nocheinmal aufgebaut werden, damit die
				 * in der Zwischenzeit eingetroffenen Files auch berücksichtigt werden können.
				 * Sonst: wird Meldung "no matching files" gebracht.
				 */
				// else if (pollTimeout > 0 ) {
				// JS-699
				else
					if (isFilePath == false && pollTimeout > 0) {
						// JS-699
						filelist = ftpClient.nList(recursive);
					}

				int intStep = 1;
				boolean flgError = false;

				try {
					receiveFiles(filelist);
					/**
					 * austauschen. Erst auf dem Zielserver umbenennen, dann im source-server löschen
					 * kb 2011-06-07
					 */
					// getDeleteTransferFiles();//nur wenn transactional=yes ist
					intStep = 2;
					getRenameAtomicSuffixTransferFiles();
					intStep = 3;
					getDeleteTransferFiles();
				}
				catch (Exception e) {
					flgError = true;
					e.printStackTrace(System.err);
					getLogger().warn("could not complete transaction, cause \n" + e.toString());
					throw e;
				}
				finally {
					// // nur wenn transaction = yes ist
					// getRemoveTransferFiles();
					if (flgError = true) {
						switch (intStep) {
							case 1:
								getRemoveTransferFiles();
								break;

							case 2:
								getRemoveTransferFiles();
								break;

							case 3:
								// nothing to do ?
								break;

							default:
								break;
						}
					}
				}
				if (!zeroByteFiles) {
					Iterator checkFileListIterator = checkFileList.keySet().iterator();
					while (checkFileListIterator.hasNext()) {
						File checkFile = (File) checkFileListIterator.next();
						File transferFile = checkFileList.get(checkFile);
						String strEncodedFileName = doEncoding(transferFile.getName(), strFileNameEncoding);
						if (checkFile == null || transferFile == null)
							RaiseException("..error occurred, empty file list is corrupted");

						// if no files with more than zero byte have been transferred then all zero byte files will be removed
						if (transferFileList.isEmpty() || zeroByteFilesRelaxed) {
							this.getLogger().debug1("removing local temporary file : " + checkFile.getAbsolutePath() + " due to zero byte constraint");
							if (!checkFile.delete()) {
								RaiseException("could not remove temporary file: " + checkFile.getAbsolutePath());
							}
							zeroByteCount++;
							fileZeroByteNotificationBody += strEncodedFileName + conNewLine;
							// otherwise rename files with atomic suffixes to their target names
						}
						else {
							if (atomicSuffix != null && atomicSuffix.length() > 0 && checkFile.getAbsolutePath().endsWith(atomicSuffix)) {
								this.getLogger().debug1("renaming local temporary file: " + transferFile.getAbsolutePath());
								if (!checkFile.renameTo(transferFile)) {
									RaiseException("could not rename temporary file [" + checkFile.getCanonicalPath() + "] to: "
											+ transferFile.getAbsolutePath());
								}
							}
							count++;
							fileNotificationBody += strEncodedFileName + conNewLine;
						}
					}
				}

				sendMails();

				if (!flgJumpTransferDefined) {
					rc = printState(rc);
				}
				if (sosString.parseToString(hlocalFile).length() > 0 && sosString.parseToString(htargetFile).length() > 0)
					arguments.put(conReturnValueSUCCESSFUL_TRANSFERS, String.valueOf(count));

				String fileNames = "";
				String filePaths = "";
				Iterator transferredIterator = transferFileList.iterator();
				while (transferredIterator.hasNext()) {
					File curFile = (File) transferredIterator.next();
					filePaths += curFile.getAbsolutePath();
					fileNames += curFile.getName();
					if (transferredIterator.hasNext()) {
						filePaths += ";";
						fileNames += ";";
					}
				}
				arguments.put(conSettingSTATUS, conStatusSUCCESS);
				rc = true;
				return rc;

			}
			catch (Exception e) {
				e.printStackTrace(System.err);

				arguments.put(conSettingSTATUS, conStatusERROR);
				rc = false;
				throw new RuntimeException("could not process file transfer: " + e.getMessage());
			}
			finally {

				if (ftpClient != null) {
					if (isLoggedIn)
						try {
							ftpClient.logout();
						}
						catch (Exception e) {
						} // no error handling
					if (ftpClient.isConnected())
						try {
							ftpClient.disconnect();
						}
						catch (Exception e) {
						} // no error handling
				}

				if (flgJumpTransferDefined) {
					doPostCommands();
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			this.getLogger().warn("ftp processing failed: " + e.getMessage());
			arguments.put(conReturnValueFAILED_TRANSFERS, String.valueOf(++failed_transfer));
			writeHistory(hlocalFile, htargetFile);
			return false;
		}
	}

	private long transferFile(final SOSFileTransfer pobjFtpClient, final File sourceFile, final File targetFile, final long checkRetry1, final long plngCheckInterval, final boolean pflgCheckSize,
			final boolean appendFiles1) throws Exception {

		long retry = checkRetry1 > 0 ? checkRetry1 : 0;
		long interval = plngCheckInterval > 0 ? plngCheckInterval : 60;
		long lngCurrentBytesReceived = 0;
		long lngPreviousBytesReceived = 0;

		try {
			if (transActional == true) {
				transActionalRemoteFiles = hlocalFile + ";" + sosString.parseToString(transActionalRemoteFiles);
			}
			else {
				if (removeFiles == false && flgJumpTransferDefined == true) {  // must be deleted later, not direct after getting. e.g. jump-server processing
					strFilesRemainingOnSource = hlocalFile + ";" + strFilesRemainingOnSource;
				}
			}
			// String strAtomicSuffix = sosString.parseToString(arguments.get("atomic_suffix")).trim();
			// if (strAtomicSuffix.length() > 0 && targetFile.getAbsolutePath().endsWith(strAtomicSuffix)) {
			// getLogger().info(
			// "receiving file: " + targetFile.getAbsolutePath().substring(0, targetFile.getAbsolutePath().indexOf(strAtomicSuffix)) + " "
			// + pobjFtpClient.size(sourceFile.getName()) + " bytes");
			// }
			String strSourceFileName = sourceFile.getName();
			String strTargetFileName = targetFile.getAbsolutePath();
			long lngSourceFileSize = pobjFtpClient.size(strSourceFileName);

			// TODO this is the steady-state algorithm. Seems to be a little bit unperformand

			lngSourceFileSize = pobjFtpClient.size(strSourceFileName);
			for (int i = -1; i < retry; i++) {
				strTargetFileName = doEncoding(strTargetFileName, strFileNameEncoding);
				if (flgConvertUmlaute == true) {
					strTargetFileName = doEncodeUmlaute(strTargetFileName, strFileNameEncoding);
				}
				getLogger().info("receiving file: " + strSourceFileName + " as " + strTargetFileName + " with " + lngSourceFileSize + " bytes");

				//
				lngCurrentBytesReceived = pobjFtpClient.getFile(strSourceFileName, strTargetFileName, appendFiles1);
				htargetFile = strTargetFileName;
				if (lngCurrentBytesReceived < 0) {
					lngCurrentBytesReceived = 0;
				}
				this.getLogger().debug(
						"..ftp server reply [getFile] [" + strTargetFileName + ", size=" + lngCurrentBytesReceived + conClosingBracketWithColon
								+ pobjFtpClient.getReplyString());
				if (appendFiles1 == false && retry > 0 && lngCurrentBytesReceived != lngPreviousBytesReceived) {
					this.getLogger().info(
							"..retry " + (i + 2) + " of " + retry + " to wait " + interval + "s for file transfer being completed, current file size: "
									+ lngCurrentBytesReceived + " bytes");
					try {
						Thread.sleep(interval * 1000);
					}
					catch (InterruptedException e) {
					}
				}
				else {
					break;
				}
				lngPreviousBytesReceived = lngCurrentBytesReceived;
			}

			long lngTargetFileSize = targetFile.length();
			if (pflgCheckSize && lngTargetFileSize > 0 && lngSourceFileSize != lngCurrentBytesReceived) {
				RaiseException("..error occurred receiving file, source file size [" + lngSourceFileSize + "] does not match number of bytes transferred ["
						+ lngCurrentBytesReceived + "], target file size is " + lngTargetFileSize);
			}
			// transActionalLocalFiles.add(htargetFile + atomicSuffix);
			transActionalLocalFiles.add(htargetFile);
			return lngCurrentBytesReceived;
		}
		catch (Exception e) {
			RaiseException("file transfer failed: " + e.getMessage());
		}
		return lngCurrentBytesReceived;

	}

	/**
	 * Holt die Dateien vom target host zu jump host
	 * @return
	 * @throws Exception
	 */
	private boolean doPreCommands() throws Exception {
		if (flgJumpTransferDefined) {

//			if(getString(conSettingOPERATION).equalsIgnoreCase(conOperationREMOVE)) {
//				for (int i = 0; i < getCommands().length; i++) {
//					"-file_path=\"[^\"]*\""
//				}
//			}
			// Ein Jump Host ist angegeben
			// Hole die Dateien per ftp vom targethost zu jumphost
			if (!execute()) {
				String s = "";
				for (int i = 0; i < getCommands().length; i++)
					s = s + getCommands()[i] + getCommandDelimiter();
				getLogger().warn("error occurred processing command: " + normalizedPassword(s));

				arguments.put("xx_make_temp_directory_xx", "ok");// hilfsvariable, wenn dieses key existiert,
				// dann wird der Parameter jump_command nicht ausgelesen,
				// sondern der jump_command wird hier neu gebildet.
				String curCommands = sosString.parseToString(arguments.get("jump_command")) + " -operation=remove_temp_directory -input=\"" + tempJumpRemoteDir
						+ "\"";
				this.setCommands(curCommands.split(getCommandDelimiter()));
				if (!execute()) {
					RaiseException("error occurred processing command: " + curCommands);
				}
				arguments.remove("xx_make_temp_directory_xx");

				return false;
			} // anschliessend im erfolgsfall hole die Dateien per ftp vom jump_host zu localhost
			filelist = filelisttmp;
		}
		return true;
	}

	/**
	 * Es wurde ein Jump Host angegeben.
	 *
	 *  Alle temporären Verzeichnisse auf der Remote Host sollen gelöscht werden.
	 *
	 * @param rc
	 * @param filelist
	 * @return
	 * @throws Exception
	 */
	private void doPostCommands() throws Exception {
		// Auf den jump host wurde ein temporäres Verzeichnis generiert. Diese unbedingt löschen
		getLogger().debug9("postCommands:  " + normalizedPassword(postCommands));
		if (getBool(conParameterREMOVE_AFTER_JUMP_TRANSFER) && sosString.parseToString(postCommands).length() > 0) {
			// Erst nach erfolgereichen Transfer können die Dateien auf dem target Rechner gelöscht werden, wenn der
			// Parameter remove_files=yes angegeben wurde.

			if (transActional) {
				getLogger().debug(".. mark transactional files for removal: " + transActionalRemoteFiles);
				Properties p = (Properties) arguments.clone();

				p.put("command", postCommands);
				p.put("operation", conOperationEXECUTE);
				p.put("xx_make_temp_directory_success_transfer_xx", "ok");
				postCommands = "";
				listOfSuccessTransfer.add(p);
			}
			else {
				arguments.put("xx_make_temp_directory_xx", "ok");// hilfsvariable, wenn dieses key existiert, dann gilt im execute diese
																	// Parameter
				// jump_command nicht ausgelesen
				int len = 0;
				int pos1 = 0;

				if (postCommands.indexOf("-file_path=") > -1) {
					len = "-file_path=".length();
					pos1 = postCommands.indexOf("-file_path=") + len;
				}
				else
					if (postCommands.indexOf("-remote_dir=") > -1) {
						len = "-remote_dir=".length();
						pos1 = postCommands.indexOf("-remote_dir=") + len;
					}

				int pos2 = postCommands.indexOf("-", pos1);

				if (pos2 == -1)
					pos2 = postCommands.length();

				String path = "";
				if (postCommands.indexOf("-file_path=") > -1) {
					int pos3 = postCommands.indexOf(";", pos1);
					if (pos3 > -1) {
						path = new File(postCommands.substring(pos1, pos3)).getParent();
					}
					else {
						path = new File(postCommands.substring(pos1, pos2)).getParent();
					}
				}
				else {
					path = postCommands.substring(pos1, pos2);
				}

				if (path == null)
					path = "";

				path = path.replaceAll("\"", "");

				String curC = "";

				if (postCommands.endsWith(getCommandDelimiter()))
					postCommands = postCommands.substring(0, postCommands.lastIndexOf(getCommandDelimiter()));

				Iterator<String> iterator = filelist.iterator();

				while (iterator.hasNext()) {
					String filen = iterator.next();
					Matcher matcher = pattern.matcher(filen);
					if (matcher.find()) {
						filen = (path.trim().length() > 0 ? normalized(path.trim()) : "") + new File(sosString.parseToString(filen)).getName();
						getLogger().debug9("delete remote file: " + filen);
						if (pos2 > postCommands.length())
							pos2 = postCommands.length();
						curC = curC + " " + postCommands.substring(0, pos1 - len) + " " + postCommands.substring(pos2) + " -file_path=\"" + filen + "\" ";
						if (iterator.hasNext())
							curC = curC + " " + getCommandDelimiter();
					}
				}

				if (filelist.size() > 0) {
					postCommands = curC;
				}
			}
		}
		postCommands = (sosString.parseToString(postCommands).length() == 0 ? "" : postCommands + " " + getCommandDelimiter() + " ")
				+ sosString.parseToString(arguments.get("jump_command")) + " -operation=remove_temp_directory -input=\"" + tempJumpRemoteDir + "\"";

		getLogger().debug5("post-processing commands are: " + normalizedPassword(postCommands));

		this.setCommands(postCommands.split(getCommandDelimiter()));

		if (!execute()) {
			arguments.remove("xx_make_temp_directory_xx");
			RaiseException("error occurred processing command: " + normalizedPassword(postCommands));
		}
		if (arguments.contains("xx_make_temp_directory_xx"))
			arguments.remove("xx_make_temp_directory_xx");

	}

	/**
	 * Parameter auslesen
	 * @throws Exception
	 */
	private void getParameter() throws Exception {
		try { // to get the parameters
			protocol = getParam("protocol", "ftp");
			port = 21;

			if (protocol.equalsIgnoreCase("sftp")) {
				sshBasedProtocol = true;
				// use other defaults
				// warum wird hier einfach der port gesetzt? so ein scheiss.
				// damit wird der vom Nutzer vorgegebene Port einfach und simple überschrieben ....
				port = 22;
				// arguments.put("port", "22");
			}

			// flgUsePathAndFileName4Matching = getBooleanParam("use_path_and_file_name_4_matching", "false");
			// flgCheckServerFeatures = getBooleanParam("check_server_features", "");
			// strPreFtpCommands = getParam("pre_ftp_commands", "");
			// strControlEncoding = getParam("control_encoding", "");
			// strFileNameEncoding = getParam("Filename_encoding", "");

			retrieveCommonParameters();

			host = getParam("host", "");

			if (sosString.parseToString(arguments.get("port")).length() > 0) {
				try {
					port = Integer.parseInt(sosString.parseToString(arguments.get("port")));
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
					RaiseException("illegal value for parameter [port]: " + sosString.parseToString(arguments.get("port")));
				}
			}

			// und was ist das für ein Trick? Scheiss
			if (sosString.parseToString(arguments.get("port")).length() == 0)
				arguments.put("port", String.valueOf(port));

			user = getParam(conParamUSER, "");

			if (sosString.parseToString(arguments.get("password")).length() > 0)
				password = sosString.parseToString(arguments.get("password"));

			if (sosString.parseToString(arguments.get("account")).length() > 0)
				account = sosString.parseToString(arguments.get("account"));

			if (sosString.parseToString(arguments.get(conSettingTRANSFER_MODE)).length() > 0)
				transferMode = sosString.parseToString(arguments.get(conSettingTRANSFER_MODE));

			if (sosString.parseToString(arguments.get("passive_mode")).length() > 0)
				passiveMode = sosString.parseToBoolean(arguments.get("passive_mode")) ? true : false;

			if (sosString.parseToString(arguments.get(conSettingREMOTE_DIR)).length() > 0) {
				remoteDir = sosString.parseToString(arguments.get(conSettingREMOTE_DIR));
				if (remoteDir.equals(".")) {
					remoteDir = "/";
				}
			}

			if (sosString.parseToString(arguments.get("local_dir")).length() > 0)
				localDir = sosString.parseToString(arguments.get("local_dir"));

			fileSpec = getParam(conSettingFILE_SPEC, "");
			// if (sosString.parseToString(arguments.get("file_spec")).length() > 0)
			// fileSpec = sosString.parseToString(arguments.get("file_spec"));

			if (sosString.parseToString(arguments.get("atomic_suffix")).length() > 0)
				atomicSuffix = sosString.parseToString(arguments.get("atomic_suffix"));

			if (sosString.parseToString(arguments.get("check_size")).length() > 0)
				checkSize = sosString.parseToBoolean(arguments.get("check_size"));

			if (sosString.parseToString(arguments.get("check_interval")).length() > 0) {
				try {
					checkInterval = Long.parseLong(sosString.parseToString(arguments.get("check_interval")));
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
					RaiseException("illegal value for parameter [check_interval]: " + sosString.parseToString(arguments.get("check_interval")));
				}
			}

			if (sosString.parseToString(arguments.get("check_retry")).length() > 0) {
				try {
					checkRetry = Long.parseLong(sosString.parseToString(arguments.get("check_retry")));
				}
				catch (Exception e) {
					e.printStackTrace(System.err);
					RaiseException("invalid value for parameter [check_retry]: " + sosString.parseToString(arguments.get("check_retry")));
				}
			}

			if (sosString.parseToString(arguments.get("overwrite_files")).length() > 0)
				overwriteFiles = !sosString.parseToBoolean(arguments.get("overwrite_files")) ? false : true;

			if (sosString.parseToString(arguments.get("append_files")).length() > 0)
				appendFiles = sosString.parseToBoolean(arguments.get("append_files")) ? true : false;

			if (sosString.parseToString(arguments.get(conSettingREMOVE_FILES)).length() > 0)
				removeFiles = sosString.parseToBoolean(arguments.get(conSettingREMOVE_FILES)) ? true : false;

			if (sosString.parseToString(arguments, "skip_transfer").length() > 0)
				skipTransfer = sosString.parseToBoolean(arguments.get("skip_transfer"));

			if (sosString.parseToString(arguments.get(conParamFORCE_FILES)).length() > 0)
				forceFiles = !sosString.parseToBoolean(arguments.get(conParamFORCE_FILES)) ? false : true;

			if (sosString.parseToString(arguments.get("file_zero_byte_transfer")).length() > 0) {
				if (sosString.parseToBoolean(arguments.get("file_zero_byte_transfer"))) {
					zeroByteFiles = true;
					zeroByteFilesStrict = false;
				}
				else
					if (sosString.parseToString(arguments.get("file_zero_byte_transfer")).equalsIgnoreCase("strict")) {
						zeroByteFiles = false;
						zeroByteFilesStrict = true;
					}
					else
						if (sosString.parseToString(arguments.get("file_zero_byte_transfer")).equalsIgnoreCase("relaxed")) {
							zeroByteFiles = false;
							zeroByteFilesStrict = false;
							zeroByteFilesRelaxed = true;
						}
						else {
							zeroByteFiles = false;
							zeroByteFilesStrict = false;
						}
			}

			if (sosString.parseToString(arguments.get("file_notification_to")).length() > 0) {
				fileNotificationTo = sosString.parseToString(arguments.get("file_notification_to"));
			}

			if (sosString.parseToString(arguments.get("file_notification_cc")).length() > 0) {
				fileNotificationCC = sosString.parseToString(arguments.get("file_notification_cc"));
			}

			if (sosString.parseToString(arguments.get("file_notification_bcc")).length() > 0) {
				fileNotificationBCC = sosString.parseToString(arguments.get("file_notification_bcc"));
			}

			if (sosString.parseToString(arguments.get("file_notification_subject")).length() > 0) {
				fileNotificationSubject = sosString.parseToString(arguments.get("file_notification_subject"));
			}

			if (sosString.parseToString(arguments.get("file_notification_body")).length() > 0) {
				fileNotificationBody = sosString.parseToString(arguments.get("file_notification_body"));
			}

			if (sosString.parseToString(arguments.get("file_zero_byte_notification_to")).length() > 0) {
				fileZeroByteNotificationTo = sosString.parseToString(arguments.get("file_zero_byte_notification_to"));
			}

			if (sosString.parseToString(arguments.get("file_zero_byte_notification_cc")).length() > 0) {
				fileZeroByteNotificationCC = sosString.parseToString(arguments.get("file_zero_byte_notification_cc"));
			}

			if (sosString.parseToString(arguments.get("file_zero_byte_notification_bcc")).length() > 0) {
				fileZeroByteNotificationBCC = sosString.parseToString(arguments.get("file_zero_byte_notification_bcc"));
			}

			if (sosString.parseToString(arguments.get("file_zero_byte_notification_subject")).length() > 0) {
				fileZeroByteNotificationSubject = sosString.parseToString(arguments.get("file_zero_byte_notification_subject"));
			}

			if (sosString.parseToString(arguments.get("file_zero_byte_notification_body")).length() > 0) {
				fileZeroByteNotificationBody = sosString.parseToString(arguments.get("file_zero_byte_notification_body"));
			}

			if (sosString.parseToString(arguments.get(conParamREPLACING)).length() > 0) {
				replacing = sosString.parseToString(arguments.get(conParamREPLACING));
			}

			if (arguments.get(conParamREPLACEMENT) != null) {
				replacement = sosString.parseToString(arguments.get(conParamREPLACEMENT));
			}

			if (sosString.parseToString(arguments.get("recursive")).length() > 0) {
				recursive = sosString.parseToBoolean(arguments.get("recursive"));
			}

			// if (replacing != null && replacement == null) {
			// RaiseException("job parameter is missing for specified parameter [replacing]: [replacement]");
			// }

			if (sosString.parseToString(arguments.get("poll_timeout")).length() > 0) {
				pollTimeout = Integer.parseInt(sosString.parseToString(arguments.get("poll_timeout")));
			}

			if (sosString.parseToString(arguments.get("poll_interval")).length() > 0) {
				pollIntervall = Integer.parseInt(sosString.parseToString(arguments.get("poll_interval")));
			}

			if (sosString.parseToString(arguments.get("poll_minfiles")).length() > 0) {
				pollMinFiles = Integer.parseInt(sosString.parseToString(arguments.get("poll_minfiles")));
			}

			if (sosString.parseToString(arguments.get("poll_error_state")).length() > 0) {
				pollFilesErrorState = sosString.parseToString(arguments.get("poll_error_state"));
			}

			// alternative Parameter
			if (sosString.parseToString(arguments.get("alternative_host")).length() > 0)
				alternativeHost = sosString.parseToString(arguments.get("alternative_host"));

			if (sosString.parseToString(arguments.get("alternative_port")).length() > 0)
				alternativePort = Integer.parseInt(sosString.parseToString(arguments.get("alternative_port")));

			if (sosString.parseToString(arguments.get("alternative_password")).length() > 0)
				alternativePassword = sosString.parseToString(arguments.get("alternative_password"));

			if (sosString.parseToString(arguments.get("alternative_user")).length() > 0)
				alternativeUser = sosString.parseToString(arguments.get("alternative_user"));

			if (sosString.parseToString(arguments.get("alternative_account")).length() > 0)
				alternativeAccount = sosString.parseToString(arguments.get("alternative_account"));

			if (sosString.parseToString(arguments.get("alternative_remote_dir")).length() > 0)
				alternativeRemoteDir = sosString.parseToString(arguments.get("alternative_remote_dir"));

			if (sosString.parseToString(arguments.get("alternative_passive_mode")).length() > 0)
				alternativePassiveMode = sosString.parseToBoolean(arguments.get("alternative_passive_mode")) ? true : false;

			if (sosString.parseToString(arguments.get("alternative_transfer_mode")).length() > 0)
				alternativeTransferMode = sosString.parseToString(arguments.get("alternative_transfer_mode"));

			if (sshBasedProtocol) {
				// parameters for ssh-based protocols
				if (sosString.parseToString(arguments.get("ssh_proxy_host")).length() > 0) {
					proxyHost = sosString.parseToString(arguments.get("ssh_proxy_host"));
				}

				if (sosString.parseToString(arguments.get("ssh_proxy_port")).length() > 0) {
					try {
						proxyPort = Integer.parseInt(sosString.parseToString(arguments.get("ssh_proxy_port")));
					}
					catch (Exception ex) {
						ex.printStackTrace(System.err);
						RaiseException("illegal non-numeric value for parameter [ssh_proxy_port]: " + sosString.parseToString(arguments.get("ssh_proxy_port")));
					}
				}
				else {
					proxyPort = 3128;
				}

				if (sosString.parseToString(arguments.get("ssh_proxy_user")).length() > 0) {
					proxyUser = sosString.parseToString(arguments.get("ssh_proxy_user"));
				}

				if (sosString.parseToString(arguments.get("ssh_proxy_password")).length() > 0) {
					proxyPassword = sosString.parseToString(arguments.get("ssh_proxy_password"));
				}

				if (sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD)).length() > 0) {
					if (sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD)).equalsIgnoreCase("publickey")
							|| sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD)).equalsIgnoreCase("password")) {
						authenticationMethod = sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD));
					}
					else {
						RaiseException("invalid authentication method [publickey, password] specified: "
								+ sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD)));
					}
				}
				else {
					authenticationMethod = "publickey";
				}

				if (sosString.parseToString(arguments.get("ssh_auth_file")).length() > 0) {
					authenticationFilename = sosString.parseToString(arguments.get("ssh_auth_file"));
				}
				else {
					if (authenticationMethod.equalsIgnoreCase("publickey"))
						RaiseException("no authentication filename was specified as parameter [ssh_auth_file]");
				}
			}

			// check if http proxy set for SSL/TLS connection
			if (sosString.parseToString(arguments.get("http_proxy_host")).length() > 0) {
				proxyHost = sosString.parseToString(arguments.get("http_proxy_host"));
			}

			if (sosString.parseToString(arguments.get("http_proxy_port")).length() > 0) {
				try {
					proxyPort = Integer.parseInt(sosString.parseToString(arguments.get("http_proxy_port")));
				}
				catch (Exception ex) {
					ex.printStackTrace(System.err);
					throw new NumberFormatException("illegal non-numeric value for parameter [http_proxy_port]: "
							+ sosString.parseToString(arguments.get("http_proxy_port")));
				}
			}

			if (!flgJumpTransferDefined) {
				if (sosString.parseToString(arguments.get("file_path")).length() > 0) {
					filePath = sosString.parseToString(arguments.get("file_path"));
					isFilePath = true;
				}
				else {
					isFilePath = false;
				}
			}

			transActional = getBooleanParam("transactional", "false");
			// if (sosString.parseToString(arguments.get("transactional")).length() > 0)
			// transActional = (sosString.parseToBoolean(arguments.get("transactional")));

			//OH: 2012-07-04 Transfer with Jump is always transactional
			if (flgJumpTransferDefined == true) {
				transActional = true;
			}

			testmode = sosString.parseToBoolean(arguments.get("mail_smtp"));

			if (sosString.parseToString(arguments.get("mail_smtp")).length() > 0)
				mailSMTP = sosString.parseToString(arguments.get("mail_smtp"));

			if (sosString.parseToString(arguments.get("mail_PortNumber")).length() > 0)
				mailPortNumber = sosString.parseToString(arguments.get("mail_PortNumber"));

			if (sosString.parseToString(arguments.get("mail_from")).length() > 0)
				mailFrom = sosString.parseToString(arguments.get("mail_from"));

			if (sosString.parseToString(arguments.get("mail_queue_dir")).length() > 0)
				mailQueueDir = sosString.parseToString(arguments.get("mail_queue_dir"));

			if (sosString.parseToString(arguments.get("simple_transfer")).length() > 0)
				// http://www.sos-berlin.com/jira/browse/SOSFTP-140
				getLogger().info("Parameter 'simple_transfer' is deactivated.");
				simpleTransfer = false;
//				simpleTransfer = sosString.parseToBoolean(sosString.parseToString(arguments.get("simple_transfer")));

		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw new RuntimeException("could not process job parameters: " + e.getMessage());
		}
	}

	private void getFileNotificationParameter() throws Exception {
		try {
			if (fileNotificationTo != null && fileNotificationTo.length() > 0) {
				if (fileNotificationSubject == null || fileNotificationSubject.length() == 0) {
					fileNotificationSubject = "SOSFTPCommand";
				}

				if (fileNotificationBody == null || fileNotificationBody.length() == 0) {
					fileNotificationBody = "The following files have been received:\n\n";
				}
			}

			if (fileZeroByteNotificationTo != null && fileZeroByteNotificationTo.length() > 0) {
				if (fileZeroByteNotificationSubject == null || fileZeroByteNotificationSubject.length() == 0) {
					fileZeroByteNotificationSubject = "[warning] SOSFTPCommand";
				}

				if (fileZeroByteNotificationBody == null || fileZeroByteNotificationBody.length() == 0) {
					fileZeroByteNotificationBody = "The following files have been received and were removed due to zero byte constraints:\n\n";
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
		}
	}

	/**
	 * Loggt die Parameterwerte aus
	 * @throws Exception
	 */
	private void printParameter() throws Exception {
		try {
			this.getLogger().debug(".. job parameter [protocol]                           : " + protocol);
			this.getLogger().debug(".. job parameter [host]                               : " + host);
			this.getLogger().debug(".. job parameter [port]                               : " + port);
			this.getLogger().debug(".. job parameter [user]                               : " + user);
			this.getLogger().debug(".. job parameter [account]                            : " + account);
			this.getLogger().debug(".. job parameter [transfer_mode]                      : " + transferMode);
			this.getLogger().debug(".. job parameter [passive_mode]                       : " + passiveMode);
			this.getLogger().debug(".. job parameter [remote_dir]                         : " + remoteDir);
			this.getLogger().debug(".. job parameter [local_dir]                          : " + localDir);
			this.getLogger().debug(".. job parameter [file_spec]                          : " + fileSpec);
			this.getLogger().debug(".. job parameter [atomic_suffix]                      : " + atomicSuffix);
			this.getLogger().debug(".. job parameter [check_size]                         : " + checkSize);
			this.getLogger().debug(".. job parameter [check_interval]                     : " + checkInterval);
			this.getLogger().debug(".. job parameter [check_retry]                        : " + checkRetry);
			this.getLogger().debug(".. job parameter [overwrite_files]                    : " + overwriteFiles);
			this.getLogger().debug(".. job parameter [append_files]                       : " + appendFiles);
			this.getLogger().debug(".. job parameter [remove_files]                       : " + removeFiles);
			this.getLogger().debug(".. job parameter [force_files]                        : " + forceFiles);

			this.getLogger().debug(".. job parameter [skip_transfer]                      : " + skipTransfer);

			this.getLogger().debug(".. job parameter [zero_byte_transfer] zeroByte        : " + zeroByteFiles);
			this.getLogger().debug(".. job parameter [zero_byte_transfer] strict          : " + zeroByteFilesStrict);
			this.getLogger().debug(".. job parameter [zero_byte_transfer] relaxed         : " + zeroByteFilesRelaxed);

			this.getLogger().debug(".. job parameter [file_notification_to]               : " + fileNotificationTo);
			this.getLogger().debug(".. job parameter [file_notification_cc]               : " + fileNotificationCC);
			this.getLogger().debug(".. job parameter [file_notification_bcc]              : " + fileNotificationBCC);
			this.getLogger().debug(".. job parameter [file_notification_subject]          : " + fileNotificationSubject);
			this.getLogger().debug(".. job parameter [file_notification_body]             : " + fileNotificationBody);

			this.getLogger().debug(".. job parameter [file_zero_byte_notification_to]     : " + fileZeroByteNotificationTo);
			this.getLogger().debug(".. job parameter [file_zero_byte_notification_cc]     : " + fileZeroByteNotificationCC);
			this.getLogger().debug(".. job parameter [file_zero_byte_notification_bcc]    : " + fileZeroByteNotificationBCC);
			this.getLogger().debug(".. job parameter [file_zero_byte_notification_subject]: " + fileZeroByteNotificationSubject);
			this.getLogger().debug(".. job parameter [file_zero_byte_notification_body]   : " + fileZeroByteNotificationBody);

			this.getLogger().debug(".. job parameter [replacing]                          : " + replacing);
			this.getLogger().debug(".. job parameter [replacement]                        : " + replacement);

			this.getLogger().debug(".. job parameter [recursive]                          : " + recursive);
			this.getLogger().debug(".. job parameter [control_encoding]                   : " + strControlEncoding);
			this.getLogger().debug(".. job parameter [pre_ftp_commands]                   : " + strPreFtpCommands);

			this.getLogger().debug(".. job parameter [poll_timeout]                       : " + pollTimeout);
			this.getLogger().debug(".. job parameter [poll_interval]                      : " + pollIntervall);
			this.getLogger().debug(".. job parameter [poll_minfiles]                      : " + pollMinFiles);
			this.getLogger().debug(".. job parameter [poll_error_state]                   : " + pollFilesErrorState);

			this.getLogger().debug(".. job parameter [file_path]                          : " + filePath);
			// alternative Parameter
			this.getLogger().debug(".. job parameter [alternative_host]                   : " + alternativeHost);
			this.getLogger().debug(".. job parameter [alternative_port]                   : " + alternativePort);
			this.getLogger().debug(".. job parameter [alternative_user]                   : " + alternativeUser);
			// this.getLogger().debug(".. job parameter [alternative_password]               : " +
			// ((sosString.parseToString(alternativePassword).length() > 0) ? "******" : ""));
			this.getLogger().debug(
					".. job parameter [alternative_password]               : " + normalizedPassword(sosString.parseToString(alternativePassword))); // ((sosString.parseToString(alternativePassword).length()
																																					// >
			this.getLogger().debug(".. job parameter [alternative_account]                : " + alternativeAccount);
			this.getLogger().debug(".. job parameter [alternative_remote_dir]             : " + alternativeRemoteDir);
			this.getLogger().debug(".. job parameter [alternative_passive_mode]           : " + alternativePassiveMode);
			this.getLogger().debug(".. job parameter [alternative_transfer_mode]          : " + alternativeTransferMode);

			this.getLogger().debug(".. job parameter [testmode]                           : " + testmode);

			this.getLogger().debug(".. job parameter [mail_smtp]                          : " + mailSMTP);
			this.getLogger().debug(".. job parameter [mail_from]                          : " + mailFrom);
			this.getLogger().debug(".. job parameter [mail_queue_dir]                     : " + mailQueueDir);

			if (sshBasedProtocol) {
				this.getLogger().debug(".. job parameter [ssh_proxy_host]                 : " + proxyHost);
				this.getLogger().debug(".. job parameter [ssh_proxy_port]                 : " + proxyPort);
				this.getLogger().debug(".. job parameter [ssh_proxy_user]                 : " + proxyUser);
				this.getLogger().debug(".. job parameter [ssh_auth_method]                : " + authenticationMethod);
				this.getLogger().debug(".. job parameter [ssh_auth_file]                  : " + authenticationFilename);
			}
			// SSL/TLS
			try {
				this.getLogger().debug(".. job parameter [http_proxy_host]                : " + proxyHost);
				this.getLogger().debug(".. job parameter [http_proxy_port]                : " + proxyPort);
			}
			catch (Exception e) {
			}

		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " could not read parameters, cause: " + e);
		}
	}

	/**
	 * FTP Verbindung hat nicht geklappt.
	 * Versuche mit Alternativen Parametern
	 * @throws Exception
	 */
	private void setAlternativeParameter() throws Exception {
		try {
			host = getAlternative(host, alternativeHost);
			port = getAlternative(port, alternativePort);
			user = getAlternative(user, alternativeUser);
			password = getAlternative(password, alternativePassword);
			account = getAlternative(account, alternativeAccount);
			remoteDir = getAlternative(remoteDir, alternativeRemoteDir);
			passiveMode = alternativePassiveMode;
			transferMode = getAlternative(transferMode, alternativeTransferMode);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			RaiseException("..error in setAlternativeParameter, cause: " + e);
		}
	}

	/**
	 * Holt alle Dateien einzeln vom FTP Server
	 *
	 * @param pvecFileList1
	 * @throws Exception
	 */
	private void receiveFiles(final Vector<String> pvecFileList1) throws Exception {
		Iterator<String> iterator = pvecFileList1.iterator();
		transActionalLocalFiles = new ArrayList<String>();
		while (iterator.hasNext()) {
			String fileName = iterator.next();
			/**
			 * \Change
			 *
			 * kb 2011-03-18
			 * Da der Wert für "file_spec" sich nur auf den FileNamen beziehen darf entfernen wir hier aus
			 * dem Namen in der FileList einen eventuell vorhandenen Pfad bezieht und damit das Ergebnis
			 * verfälscht und Dateien nicht gefunden werden.
			 */
			String strFileName4Matcher = fileName;
			if (flgUsePathAndFileName4Matching == false) {
				strFileName4Matcher = new File(fileName).getName();
			}
			/* */
			Matcher matcher = pattern.matcher(strFileName4Matcher);
			File transferFile = null;
			hlocalFile = "";
			htargetFile = "";
			arguments.put(conSettingFILE_SIZE, "");

			this.getLogger().debug7("Processing file " + fileName);
			if (recursive && !isFilePath) {
				if (sosString.parseToString(remoteDir).length() > 0) {
					if (!ftpClient.changeWorkingDirectory(remoteDir)) {
						RaiseException(FTP_SERVER_REPLY_CD_REMOTE_DIR + remoteDir + conClosingBracketWithColon + ftpClient.getReplyString());
					}
					else {
						getLogger().debug(FTP_SERVER_REPLY_CD_REMOTE_DIR + remoteDir + conClosingBracketWithColon + ftpClient.getReplyString());
					}
				}
			}

			if (matcher.find() || isFilePath) {
				if (replacement != null && replacing != null && replacing.length() > 0) {
					String strFileName4Replacing = new File(fileName).getName();
					String currTransferFilename = SOSFileOperations.getReplacementFilename(strFileName4Replacing, replacing, replacement);
					this.getLogger().debug1(String.format("transfer file [%1$s] is renamed to [%2$s]", strFileName4Replacing, currTransferFilename));
					if (isFilePath) {
						File f = new File(currTransferFilename);
						transferFile = this.createFile(adjustPathName(localDir) + f.getName());
					}
					else {
						transferFile = this.createFile(adjustPathName(localDir) + currTransferFilename);
					}
				}
				else {
					if (isFilePath) {
						transferFile = this.createFile(adjustPathName(localDir) + new File(fileName).getName());
					}
					else {
						// transferFile = this.createFile(localDir + (localDir.endsWith("/") || localDir.endsWith("\\") ? "" : "/") +
						// fileName);
						String strT = new File(fileName).getName();
						/**
						 * SOSFTP-94
						 */
						if (recursive == true) {
							strT = stripRemoteDirName(remoteDir, fileName);
						}
						transferFile = this.createFile(adjustPathName(localDir) + strT);
					}
				}
				File transFile = transferFile;
				if (transFile.getParent() != null && !transFile.getParentFile().exists()) {
					if (transFile.getParentFile().mkdirs()) {
						this.getLogger().debug1("..create parent directory [" + transFile.getParentFile() + "]");
					}
					else {
						RaiseException("..error occurred creating directory [" + transFile.getParentFile() + "]");
					}
				}

				if (!appendFiles && !overwriteFiles && transferFile.exists()) {
					this.getLogger().debug1("..ftp transfer skipped for file [no overwrite]: " + transferFile.getName());
					continue;
				}

				long bytesSent = 0;

				if (!appendFiles && atomicSuffix != null && atomicSuffix.length() > 0) {
					File atomicFile = new File(transferFile.getAbsolutePath() + atomicSuffix);

					File file = new File(fileName);
					if (recursive) {
						if (file.getParent() != null && !isFilePath) {
							String[] splitParent = file.getParent().split(conRegExpBackslash);
							for (int i = 0; i < splitParent.length; i++) {
								if (sosString.parseToString(splitParent[i]).length() > 0) {
									if (!ftpClient.changeWorkingDirectory(splitParent[i])) {
										RaiseException(FTP_SERVER_REPLY_CD_REMOTE_DIR + splitParent[i] + conClosingBracketWithColon
												+ ftpClient.getReplyString());
									}
									else {
										getLogger().debug(
												FTP_SERVER_REPLY_CD_REMOTE_DIR + splitParent[i] + conClosingBracketWithColon + ftpClient.getReplyString());
									}
								}
							}
						}
					}

					if (skipTransfer) {
						bytesSent = ftpClient.size(file.getName());
						this.getLogger().debug7(" Processing file " + file.getName());
						// loc
						if (!removeFiles && !testmode) {// wenn eine Datei gelöscht werden soll, dann braucht es auch nicht umbennant zu
														// werden
							if (replacement != null && replacing != null && replacing.length() > 0) {
								ftpClient.rename(file.getName(), transferFile.getName());
							}
						}
						fileNotificationBody += doEncoding(transferFile.getName(), strFileNameEncoding) + conNewLine;
						Matcher matcher1 = pattern.matcher(transferFile.getName());
						if (matcher1.find()) {
							transferFileList.add(transferFile);
							count++;
						}
					}
					else {
						hlocalFile = file.getName();
						htargetFile = atomicFile.getAbsolutePath().substring(0, atomicFile.getAbsolutePath().indexOf(atomicSuffix));

						bytesSent = this.transferFile(ftpClient, new File(file.getName()), atomicFile, checkRetry, checkInterval, checkSize, appendFiles);
						arguments.put(conSettingFILE_SIZE, String.valueOf(bytesSent));

						if (transferFile.exists()) {
							if (overwriteFiles) {
								if (!transferFile.delete()) {
									RaiseException(String.format("overwrite or delete of local file '%1$s' failed ", transferFile.getAbsolutePath()));
								}
							}
							else {
								// eigentlich doch fehlermeldung, oder?
							}
						}

						if (bytesSent <= 0 && !zeroByteFiles && zeroByteFilesStrict) {
							this.getLogger().debug1("removing local file : " + transferFile.getAbsolutePath() + " due to zero byte strict constraint");
							if (!atomicFile.delete()) {
								RaiseException("..error occurred, could not remove temporary file: " + atomicFile.getAbsolutePath());
							}
							fileZeroByteNotificationBody += transferFile.getName() + conNewLine;
							zeroByteCount++;
						}
						else {
							if (bytesSent <= 0 && !zeroByteFiles) {
								checkFileList.put(atomicFile, transferFile);
							}
							else {
								if (!transActional) {
									this.getLogger().debug1(
											"renaming local temporary file '" + atomicFile.getAbsolutePath() + "' to '" + transferFile.getAbsolutePath() + "'");
									if (!atomicFile.renameTo(transferFile)) {
										RaiseException("could not rename temporary file [" + atomicFile.getCanonicalPath() + "] to: "
												+ transferFile.getAbsolutePath());
									}
								}
								fileNotificationBody += doEncoding(transferFile.getName(), strFileNameEncoding) + conNewLine;
								transferFileList.add(transferFile);
								count++;
							}
						}
					}
				}
				else {
					File file = new File(fileName);
					if (recursive) {
						String strP = file.getParent();
						if (strP != null) {
							String[] splitParent = strP.split(conRegExpBackslash);
							for (String strF : splitParent) {
								if (strF.trim().length() > 0) {
									String strM = FTP_SERVER_REPLY_CD_REMOTE_DIR + strF + conClosingBracketWithColon + ftpClient.getReplyString();
									if (!ftpClient.changeWorkingDirectory(strF)) {
										RaiseException(strM);
									}
									else {
										getLogger().debug(strM);
									}
								}
							}
						}
					}

					if (skipTransfer) {
						bytesSent = ftpClient.size(file.getName());
						fileNotificationBody += doEncoding(transferFile.getName(), strFileNameEncoding) + conNewLine;
						Matcher matcher1 = pattern.matcher(transferFile.getName());
						if (matcher1.find()) {
							transferFileList.add(transferFile);
							count++;
						}
					}
					else {
						hlocalFile = file.getName();
						htargetFile = transferFile.getAbsolutePath();

						bytesSent = this.transferFile(ftpClient, new File(file.getName()), transferFile, checkRetry, checkInterval, checkSize, appendFiles);
						arguments.put(conSettingFILE_SIZE, String.valueOf(bytesSent));

						if (bytesSent <= 0 && !zeroByteFiles && zeroByteFilesStrict) {
							this.getLogger().debug1("removing local file : " + transferFile.getAbsolutePath() + " due to zero byte strict constraint");
							if (!transferFile.delete()) {
								RaiseException("..error occurred, could not remove temporary file: " + transferFile.getAbsolutePath());
							}
							fileZeroByteNotificationBody += transferFile.getName() + conNewLine;
							zeroByteCount++;
						}
						else
							if (bytesSent <= 0 && !zeroByteFiles) {
								checkFileList.put(transferFile, transferFile);
							}
							else {
								fileNotificationBody += doEncoding(transferFile.getName(), strFileNameEncoding) + conNewLine;
								transferFileList.add(transferFile);
								count++;
							}
					}
				}

				if (transActional == false && removeFiles == true) {
					boolean ok = ftpClient.delete(new File(fileName).getName());
					// rueckgabewert genuegt
					if (ok == false) {
						RaiseException("..error occurred, could not remove remote file [" + transferFile.getName() + conClosingBracketWithColon
								+ ftpClient.getReplyString());
					}
					else {
						if (listOfSuccessTransfer != null && listOfSuccessTransfer.size() > 0)
							this.getLogger().debug("removing remote file: " + transferFile.getName());
						else
							this.getLogger().info("removing remote file: " + transferFile.getName());
					}
				}
				// }
				if (sosString.parseToString(hlocalFile).length() > 0 && sosString.parseToString(htargetFile).length() > 0) {
					arguments.put(conReturnValueSUCCESSFUL_TRANSFERS, String.valueOf(++successful_transfers));
				}

				writeHistory(hlocalFile, htargetFile);
			}
		}

	}

	/**
	 * senden von Mails
	 * @throws Exception
	 */
	private void sendMails() throws Exception {

		if (zeroByteCount > 0 && fileZeroByteNotificationTo != null && fileZeroByteNotificationTo.length() > 0) {
			sendMail(fileZeroByteNotificationTo, fileZeroByteNotificationCC, fileZeroByteNotificationBCC, fileZeroByteNotificationSubject,
					fileZeroByteNotificationBody);
		}

		if (count > 0 && fileNotificationTo != null && fileNotificationTo.length() > 0) {
			sendMail(fileNotificationTo, fileNotificationCC, fileNotificationBCC, fileNotificationSubject, fileNotificationBody);
		}
	}

	/**
	 * print State
	 * @param rc
	 * @return
	 * @throws Exception
	 */
	private boolean printState(boolean rc) throws Exception {
		String received = "received";
		if (skipTransfer) {
			if (sosString.parseToString(arguments, "operation").equalsIgnoreCase(conOperationREMOVE))
				if (listOfSuccessTransfer != null && listOfSuccessTransfer.size() > 0)
					getLogger().debug1(count + conTextFILES_REMOVE);
				else {
					getLogger().info(count + conTextFILES_REMOVE);
					state = count + conTextFILES_REMOVE;
				}
			else {
				getLogger().info(count + " files found.");
				state = count + " files found.";
			}
			received = "found";
		}
		switch (count) {
			case 0:
				if (zeroByteCount > 0 && zeroByteFilesRelaxed) {
					this.getLogger().debug1("no matching files found, " + zeroByteCount + " zero byte files skipped");
					state = "no matching files found, " + zeroByteCount + " zero byte files skipped";
				}
				else
					if (zeroByteCount > 0 && zeroByteFilesStrict) {
						RaiseException("zero byte file(s) found");
					}
					else
						if (forceFiles) {
							RaiseException("no matching files found");
						}
						else {
							this.getLogger().debug1("no matching files found");
							state = "no matching files found";
						}

				if (!forceFiles) {
					return false;
				}
				else {
					rc = !forceFiles ? true : !zeroByteFilesRelaxed;
				}
				break;
			case 1:
				state = "1 file " + received + (zeroByteCount > 0 ? ", " + zeroByteCount + " files skipped due to zero byte constraint" : "");
				this.getLogger().debug1(state);
				rc = true;
				break;
			default:
				state = count + " files " + received + (zeroByteCount > 0 ? ", " + zeroByteCount + " files skipped due to zero byte constraint" : "");
				this.getLogger().debug1(state);
				rc = true;
				break;
		}

		return rc;

	}

	// private boolean polling(Vector filelist) throws Exception{
	private boolean polling() throws Exception {
		try {
			if (testmode && flgJumpTransferDefined && filelist.isEmpty()) {
				// wenn ein jump Host angegeben ist und testmode=yes ist, dann werden keine Dateien transferiert
				// Also ist hier eine Überprüfung sinnlos, weil auf der jump_host auch keine transferierten Dateien liegen.
				// Eine Info mit der Anzahl der gefundenen dateien auf der Ziel Rechner wird in der Methode execute ausgegeben
				return true;
			}

			if (schedulerJob != null && pollTimeout > 0) {
				Class cl = Class.forName("sos.scheduler.ftp.JobSchedulerFTPReceive");
				if (cl.getName().indexOf("FTPReceive") > -1) {
					Method mthd1 = cl.getMethod("polling", new Class[] { Vector.class, Boolean.TYPE, String.class, SOSFileTransfer.class, String.class,
							Boolean.TYPE, Boolean.TYPE, int.class, int.class, int.class, String.class });

					boolean output1 = sosString.parseToBoolean(mthd1.invoke(schedulerJob, new Object[] { filelist, new Boolean(isFilePath), filePath,
							ftpClient, fileSpec, Boolean.valueOf(recursive), Boolean.valueOf(forceFiles), new Integer(pollTimeout), new Integer(pollIntervall),
							new Integer(pollMinFiles), pollFilesErrorState }));
					return output1;
				}
			}

			Iterator iterator = filelist.iterator();
			if (pollTimeout > 0) {
				// before any processing, check if files are available
				boolean done = false;
				boolean giveUpPoll = false;
				double delay = pollIntervall;
				double nrOfTries = pollTimeout * 60 / delay;
				int tries = 0;
				while (!done && !giveUpPoll) {
					tries++;
					int matchedFiles = 0;
					while (iterator.hasNext()) {
						String fileName = (String) iterator.next();
						if (isFilePath) {
							boolean found = false;
							try {
								// we are already in the directory, so use only name:
								File file = new File(fileName);
								long si = ftpClient.size(file.getName());
								if (si > -1)
									found = true;
							}
							catch (Exception e) {
								e.printStackTrace(System.err);
								getLogger().debug9("File " + fileName + " was not found.");
							}
							if (found) {
								matchedFiles++;
								getLogger().debug8("Found matching file " + fileName);
							}
						}
						else {
							Matcher matcher = pattern.matcher(fileName);
							if (matcher.find()) {
								matchedFiles++;
								getLogger().debug8("Found matching file " + fileName);
							}
						}
					}
					getLogger().debug3(matchedFiles + " matching files found");
					if (matchedFiles < pollMinFiles) {
						// simple job
						if (tries < nrOfTries) {
							getLogger().info("polling for files ...");
							Thread.sleep((long) delay * 1000);
							// TODO eliminate duplicated code
							if (isFilePath) {
								filelist = new Vector();
								String[] split = filePath.split(";");
								for (String element : split) {
									if (element != null && element.length() > 0)
										filelist.add(element);
								}
								fileSpec = conRegExpAllChars;
							}
							else {
								filelist = ftpClient.nList(recursive);
							}
							iterator = filelist.iterator();
						}
						else {
							giveUpPoll = true;
						}

					}
					else {
						done = true;
					}
					if (giveUpPoll) {
						// keep configuration order monitor from repeating:

						String message = "Failed to find at least " + pollMinFiles + " files matching \"" + fileSpec + "\" ";
						if (isFilePath)
							message = "Failed to find file \"" + filePath + "\" ";
						message += "after triggering for " + pollTimeout + " minutes.";
						if (matchedFiles > 0)
							message += " (only " + matchedFiles + " files were found)";
						if (pollFilesErrorState != null && pollFilesErrorState.length() > 0) {
							getLogger().warn(pollFilesErrorState);
						}
						if (forceFiles) {
							getLogger().warn(message);
							String body = message + conNewLine;
							body += "See attached logfile for details.";

						}
						else {
							getLogger().info(message);
						}
						return false;
					}
				}
			}
			return true;
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " error while polling, cause: " + e);
		}
		return true;
	}

	/**
	 * Erst wenn alle Dateien erfolgreich transferieriert wurden, dann sollen die remote Dateien gelöscht werden.
	 * Parameter = transactional = yes und remove_files=yes
	 * @throws Exception
	 */
	private void getDeleteTransferFiles() throws Exception {

		if (transActionalRemoteFiles != null || strFilesRemainingOnSource != null) {
			if (removeFiles == true && transActional || getBool(conParameterREMOVE_AFTER_JUMP_TRANSFER)) {
				getLogger().debug(".. mark transactional files to remove " + transActionalRemoteFiles);
				Properties p = (Properties) arguments.clone();

				/*
				 * die "richtigen" Werte restoren.
				 */
				p.put(conSettingREMOTE_DIR, savedArguments.getProperty(conSettingREMOTE_DIR));
				p.put(conSettingLOCAL_DIR, "");
				/* Sonst steht im remove der jump_ssh_auth_file in ssh_auth_file
				 * OH 2012-06-07
				 */
				if (savedArguments.containsKey(conParamSSH_AUTH_FILE)) {
					p.put(conParamSSH_AUTH_FILE, savedArguments.getProperty(conParamSSH_AUTH_FILE));
				}
				/* Sonst steht im remove der jump host als target host
				 * und jump user als target user
				 * OH 2012-06-08
				 */
				if (savedArguments.containsKey(conParamHOST)) {
					p.put(conParamHOST, savedArguments.getProperty(conParamHOST));
				}
				if (savedArguments.containsKey(conParamUSER)) {
					p.put(conParamUSER, savedArguments.getProperty(conParamUSER));
				}
				if (savedArguments.containsKey(conParamPASSWORD)) {
					p.put(conParamPASSWORD, savedArguments.getProperty(conParamPASSWORD));
				}
				if (savedArguments.containsKey(conParamSSH_AUTH_METHOD)) {
					p.put(conParamSSH_AUTH_METHOD, savedArguments.getProperty(conParamSSH_AUTH_METHOD));
				}
				if (savedArguments.containsKey(conParamPORT)) {
					p.put(conParamPORT, savedArguments.getProperty(conParamPORT));
				}
				if (savedArguments.containsKey(conParamPROTOCOL)) {
					p.put(conParamPROTOCOL, savedArguments.getProperty(conParamPROTOCOL));
				}

				p.put("operation", conOperationREMOVE);
				/**
				 * http://www.sos-berlin.com/jira/browse/SOSFTP-108
				 */
				if (transActional == true) {
					p.put("file_path", transActionalRemoteFiles);
				}
				else {
					p.put("file_path", strFilesRemainingOnSource);
				}
				p.put("transactional", "no");
				listOfSuccessTransfer.add(p);
			}
		}
	}

	/**
	 * Erst bei Erfolgreichen transferieren aller Dateien, wird der atomic suffix umbennant
	 * Bedingung: Parameter transActional = yes und atomic_suffix <> ""
	 * @throws Exception
	 */
	private void getRenameAtomicSuffixTransferFiles() throws Exception {

		try {
			if (transActional) {
				if (sosString.parseToString(atomicSuffix).length() > 0) {
					Properties p = new Properties();
					p.put("operation", "rename_local_files");
					p.put("files", transActionalLocalFiles);
					p.put("atomic_suffix", atomicSuffix);
					listOfSuccessTransfer.add(p);
					getLogger().debug(".. mark transactional files to rename " + transActionalLocalFiles);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
		}
	}

	/**
	 * Bei einer transfer Fehler müssen alle bereits transferierte Dateien gelöscht werden.
	 * Gilt für Parameter transActional = yes
	 *
	 * @throws Exception
	 */
	private void getRemoveTransferFiles() throws Exception {

		if (transActional && transActionalLocalFiles != null && transActionalLocalFiles.size() > 0) {

			Properties p = new Properties();
			p.put("operation", "delete_local_files");
			p.put("files", transActionalLocalFiles);
			listOfErrorTransfer.add(p);
			getLogger().debug(".. mark transactional files to remove on error: " + transActionalLocalFiles);
		}
	}

	/**
	 * @return the count
	 */
	public int getOfTransferFilesCount() {
		return count;
	}

	/**
	 * @return the zeroByteCount
	 */
	public int getZeroByteCount() {
		return zeroByteCount;
	}

	public SOSFileTransfer initFTPServer() throws Exception {

		if (protocol.equalsIgnoreCase("ftp")) {
			initSOSFTP();
		}
		else
			if (protocol.equalsIgnoreCase("sftp")) {
				initSOSSFTP();
			}
			else
				if (protocol.equalsIgnoreCase("ftps")) {
					initSOSFTPS();
				}
				else {
					RaiseException("Unknown protocol: " + protocol);
				}
		if (sosString.parseToString(arguments, conParamUSER).length() == 0 && sosString.parseToString(user).equals("anonymous")) {
			arguments.put(conParamUSER, "anonymous");
		}

		return ftpClient;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Transfer Files
	 *
	 * @return boolean
	 * @throws Exception
	 */
	private boolean receiveSimpleTransfer() throws Exception {
		try {

			// einfaches transferieren einer Datei. Hier finden auch keine Überprüfung statt
			getLogger().debug9("..filepath: " + filePath);

			if (sosString.parseToString(remoteDir).length() > 0 && sosString.parseToString(remoteDir).equals("./")) {
				remoteDir = "";
			}
			String targetFile = null;
			String localDir = sosString.parseToString(arguments.get(conSettingLOCAL_DIR));
			boolean appendFiles = sosString.parseToBoolean(sosString.parseToString(arguments.get("append_files")));
			boolean overwriteFiles = sosString.parseToBoolean(sosString.parseToString(arguments.get("overwrite_files")));

			if (sosString.parseToString(remoteDir).length() > 0)
				targetFile = new File(sosString.parseToString(remoteDir), filePath).getPath().replaceAll(conRegExpBackslash, "/");
			else
				targetFile = new File(filePath).getPath().replaceAll(conRegExpBackslash, "/");
			String localFile = new File(localDir, new File(targetFile).getName()).getPath();

			if (!appendFiles && !overwriteFiles && new File(localFile).exists()) {
				this.getLogger().info("..ftp transfer skipped for file [no overwrite]: " + localFile);
				return false;
			}

			SOSFileTransfer ftpClient = initFTPServer();

			long currentBytesSent = ftpClient.getFile(targetFile, localFile, appendFiles);
			this.getLogger().debug(
					"..ftp server reply [getFile] [" + localFile + ", size=" + currentBytesSent + conClosingBracketWithColon + ftpClient.getReplyString());
			this.getLogger().debug("1 file received " + localFile);
			getLogger().info("receiving file: " + localFile + " " + ftpClient.size(targetFile) + " bytes");
			transferFileList.add(new File(filePath));
			filelist = new Vector<String>();
			filelist.add(filePath);
			arguments.put(conReturnValueSUCCESSFUL_TRANSFERS, "1");

			return true;

		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			arguments.put(conReturnValueFAILED_TRANSFERS, "1");
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e, e);
		}
		return true;
	}

	private boolean isSimpleTransfer() throws Exception {
		// Deactivate the Parameter "simple_transfer"  http://www.sos-berlin.com/jira/browse/SOSFTP-140
		return false;
//		boolean simpleTransfer = sosString.parseToBoolean(sosString.parseToString(arguments.get("simple_transfer")));
//
//		if (simpleTransfer && sosString.parseToString(arguments.get("file_path")).length() == 0) {
//			RaiseException("job parameter is missing for specified parameter [ftp_simple_transfer]: [ftp_file_path]");
//		}
//
//		if (simpleTransfer) {
//			getLogger().info(
//					"parameter [ftp_file_spec, replacement, replacing, ftp_force_files, ftp_atomic_suffix, ftp_recursive, ftp_compress, ftp_remove_files, ftp_make_dirs] "
//							+ "will be ignored because parameter ftp_simple_transfer has been specified. ");
//		}
//		return simpleTransfer;
	}

	private String adjustPathName(final String pstrPathName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::adjustPathName";

		String strResult = pstrPathName + (pstrPathName.endsWith("/") || pstrPathName.endsWith("\\") ? "" : "/");

		return strResult;
	} // private String adjustPathName

}
