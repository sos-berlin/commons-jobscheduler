/**
 *
 */
package sos.net.sosftp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import sos.net.SOSFTP;
import sos.net.SOSFTPCommand;
import sos.util.SOSFile;
import sos.util.SOSFileOperations;
import sos.util.SOSGZip;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * Sends files by FTP
 *
 * @see documentation sos.scheduler.jobdoc.JobSchedulerFTPSend.xml
 *
 * @author mueruevet.oeksuez@sos-berlin.com
 *
 */
@SuppressWarnings("deprecation")
public class SOSFTPCommandSend extends SOSFTPCommand {

	@SuppressWarnings("unused")
	String						conSVNVersion					= "$Id: SOSFTPOptions.java 17481 2012-06-29 15:40:36Z kb $";

	// private static final String conParamFORCE_FILES = "force_files";
	private static final String	conParameterMAKE_DIRS			= "make_dirs";
	public static final String	RECURSIVE						= "recursive";
	public static final String	FILE_SIZE						= "file_size";

	private int					intPosixPermissions				= 0;
	/** sos.util.SOSString Object */
	private static SOSString	sosString						= new SOSString();

	/** transfer filename*/
	private File				localFile						= null;

	/** count of successful transfer files*/
	private int					successful_transfers			= 0;

	/** count of failed transfer files*/
	private int					failed_transfer					= 0;

	/** ftp, sftp or ftps. If sftp is chosen, the ssh_* parameters will be considered. Default ftp*/
	private String				protocol						= "ftp";
	private boolean				sshBasedProtocol				= false;

	/** Transfer mode can be either ascii or binary. Default binary */
	private String				transferMode					= "binary";
	/** Passive mode is often used with firewalls. Valid values are 0 or 1 . Default 0.*/
	private boolean				passiveMode						= false;

	/** Directory on the FTP server to which files should be transferred.*/
	private String				remoteDir						= "./";

	/** Local directory from which files should be transferred*/
	private String				localDir						= ".";
	private final String				defaultLocalDir					= ".";

	/** This parameter specifies a regular expression to select files to be transferred from a local directory.*/
	private String				fileSpec						= ".*";

	/** This parameter specifies whether target files should created with a suffix as "~", and then be renamed to the target file name after the file transfer has been completed*/
	private String				atomicSuffix					= "";

	/** This parameter determines whether the original file size and the number of bytes transferred should be compared after a file transfer and whether an error should be raised if they do not match.*/
	private boolean				checkSize						= true;

	/** This parameter specifies whether existing files on the remote host should be overwritten.*/
	private boolean				overwriteFiles					= true;

	/** This parameter specifies whether the content of the source files should be appended to the target files if the target files exist.*/
	private boolean				appendFiles						= false;

	/** This parameter specifies if the local file should be removed after transfer. */
	private boolean				removeFiles						= false;

	/** If this Parameter is set to "true", everything except the transfer itself will be performed. This can be used to just trigger for files or to only delete files on the ftp server.*/
	private boolean				skipTransfer					= false;

	private boolean				flgMakeDirs						= false;

	/** This parameter specifies whether an error should be raised if no files could be found for transfer. */
	private boolean				forceFiles						= true;

	/** This parameter specifies whether files should be compressed for transfer. A gzip-compatible compression is used, no further software components are required. */
	private boolean				compressFiles					= false;

	/** This parameter specifies the file extension should target file compression be specified using the ftp_compress_files parameter. */
	private String				compressedFileExtension			= "";

	/** This parameter specifies whether zero byte files should be transferred and processed by subsequent jobs. The following settings are available */
	private boolean				zeroByteFiles					= true;
	private boolean				zeroByteFilesStrict				= false;
	private boolean				zeroByteFilesRelaxed			= false;

	/** notification by e-mail in case of transfer of empty files. */
	private String				fileNotificationTo				= "";
	private String				fileNotificationCC				= "";
	private String				fileNotificationBCC				= "";
	private String				fileNotificationSubject			= "";
	private String				fileNotificationBody			= "";

	private String				fileZeroByteNotificationTo		= "";
	private String				fileZeroByteNotificationCC		= "";
	private String				fileZeroByteNotificationBCC		= "";
	private String				fileZeroByteNotificationSubject	= "";
	private String				fileZeroByteNotificationBody	= "";

	private boolean				keepConnection					= false;
	private boolean				sameConnection					= false;

	private int					count							= 0;
	private int					zeroByteCount					= 0;

	/** Regular Expression for filename replacement with replacement . */
	private String				replacing						= null;
	private String				replacement						= null;

	/** This parameter specifies if files from subdirectories should be transferred recursively*/
	private boolean				recursive						= false;
	private String				filePath						= "";
	private boolean				isFilePath						= false;

	/** Use of alternative access data and credentials */
	private String				alternativeHost					= "";
	private int					alternativePort					= 0;
	private String				alternativeUser					= "";
	private String				alternativePassword				= "";
	private String				alternativeAccount				= "";
	private String				alternativeRemoteDir			= "";
	private boolean				alternativePassiveMode			= false;
	private String				alternativeTransferMode			= "";

	private String				lastHost						= "";
	private int					lastPort						= 0;
	private String				lastUser						= "";
	private String				lastAccount						= "";

	private static String		state							= "";

	/**
	 * Resource filenames for operation = install
	 */
	// private String installpaths =
	// "%{local_dir}/banner_english.gif;%{local_dir}/banner_german.gif;%{local_dir}/sosftp.xml;%{local_dir}/sosftp.xsl;%{local_dir}/sos.net.jar;%{local_dir}/sos.settings.jar;%{local_dir}/sos.util.jar;%{local_dir}/trilead-ssh2-build211.jar;%{local_dir}/commons-net-1.2.2.jar";
	// private String installpaths =
	// "%{local_dir}/lib/sos.net.jar;%{local_dir}/lib/sos.settings.jar;%{local_dir}/lib/sos.util.jar;%{local_dir}/lib/trilead-ssh2-build211.jar;%{local_dir}/lib/commons-net-1.2.2.jar;%{local_dir}/readme.txt;%{local_dir}/ThirdParty_sosftp.txt";
	// private String installDocPaths =
	// "%{local_dir}/doc/banner_english.gif;%{local_dir}/doc/banner_german.gif;%{local_dir}/doc/sosftp.xml;%{local_dir}/doc/scheduler_job_documentation.xsl";
	// private String installpaths =
	// "%{local_dir}/sos.net.jar;%{local_dir}/sos.settings.jar;%{local_dir}/sos.util.jar;%{local_dir}/trilead-ssh2-build211.jar;%{local_dir}/commons-net-1.2.2.jar;%{local_dir}/readme.txt;%{local_dir}/ThirdParty.txt";
	// wegen Redesign sind 4 neue Bibliotheken hinzugekommen: sos.xml.jar; xercesImpl.jar; xml-apis.jar; sos.connection.jar;
	// private String installpaths =
	// "%{local_dir}/sos.xml.jar;%{local_dir}/xercesImpl.jar;%{local_dir}/xml-apis.jar;%{local_dir}/sos.connection.jar;%{local_dir}/sos.net.jar;%{local_dir}/sos.settings.jar;%{local_dir}/sos.util.jar;%{local_dir}/trilead-ssh2-build211.jar;%{local_dir}/commons-net-1.2.2.jar;%{local_dir}/readme.txt;%{local_dir}/ThirdParty.txt";
	// 03.03.2010 ab jdk 1.5 werden die xerces Bibliotheken mitgeliefert
	// private String installpaths =
	// "%{local_dir}/sos.xml.jar;%{local_dir}/xalan.jar;%{local_dir}/sos.connection.jar;%{local_dir}/sos.net.jar;%{local_dir}/sos.settings.jar;%{local_dir}/sos.util.jar;%{local_dir}/trilead-ssh2-build211.jar;%{local_dir}/commons-net-1.2.2.jar;%{local_dir}/readme.txt;%{local_dir}/ThirdParty.txt";
	// 29.03.2009 die Bibliotheken haben jetzt revisionsnummer und datum
	// private String installpaths =
	// "%{local_dir}/sos.xml.jar;%{local_dir}/xalan.jar;%{local_dir}/sos.connection.jar;%{local_dir}/sos.net.jar;%{local_dir}/sos.settings.jar;%{local_dir}/sos.util.jar;%{local_dir}/trilead-ssh2-build211.jar;%{local_dir}/commons-net-1.2.2.jar;%{local_dir}/readme.txt;%{local_dir}/ThirdParty.txt";
	private String				installpathsWithRevNr			= "%{local_dir}/com.sos.xml.*[.]jar$;%{local_dir}/com.sos.connection.*[.]jar$;%{local_dir}/com.sos.net.*[.]jar$;%{local_dir}/com.sos.settings.*[.]jar$;%{local_dir}/com.sos.util.*[.]jar$;%{local_dir}/com.sos.configuration.*[.]jar$";
	private final String				installpaths					= "%{local_dir}/xalan.jar;%{local_dir}/trilead-ssh2-build211.jar;%{local_dir}/commons-net-1.2.2.jar;%{local_dir}/readme.txt;%{local_dir}/ThirdParty.txt";
	private final String				installDocPaths					= "%{local_dir}/doc/banner_english.gif;%{local_dir}/doc/banner_german.gif;%{local_dir}/doc/sosftp.xml;%{local_dir}/doc/sosftp.xsl";

	private ArrayList<File>		transActionalLocalFiles			= null;
	private String				transActionalRemoteFiles		= null;

	/** Polling for files */
	private int					pollTimeout						= 0;
	private int					pollIntervall					= 60;

	/** testmode is on. No files will be transfer. There is only looking for connection*/
	private boolean				testmode						= false;

	private boolean				simpleTransfer					= false;

	/**
	 * @param settingsFile
	 * @param settingsSection
	 * @param logger
	 * @param arguments_
	 */
	public SOSFTPCommandSend(final SOSLogger logger, final Properties arguments_) throws Exception {
		super(logger, arguments_);
	}

	public SOSFTPCommandSend(final sos.configuration.SOSConfiguration sosConfiguration_, final SOSLogger logger) throws Exception {
		super(sosConfiguration_, logger);
	}

	/**
	 * Sends files by FTP/SFTP to a remote server
	 *
	 * @return boolean
	 * @throws Exception
	 */
	@Override
	public boolean send() throws Exception {

		getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
		Vector filelist = null;
		boolean rc = false;

		try {

			setParam("successful_transfers", "0");
			setParam("failed_transfers", "0");

			if (this.getLogger() == null)
				this.setLogger(new SOSStandardLogger(0));

			if (!sosString.parseToString(arguments.get("operation")).startsWith("install") || flgJumpTransferDefined) {// argumente wurden
																														// in install
																														// gelesen
				// und verändert. Es darf nicht
				// nochmals überschrieben werden
				readSettings(true);
			}

			try { // to get the parameters
				getParameter();
				getFileNotificationParameter();
				printParameter();
				simpleTransfer = isSimpleTransfer();
			}
			catch (Exception e) {
				throw new Exception("could not process job parameters: " + e.getMessage());
			}

			if (!arguments.contains("port"))// falls noch execute anschliessend ausgeführt werden soll
				setParam("port", String.valueOf(port));

			try { // to check parameters
				if (host == null || host.length() == 0)
					RaiseException("no host was specified");
				if (user == null || user.length() == 0)
					RaiseException("no user was specified");
			}
			catch (Exception e) {
				throw new Exception("invalid or insufficient parameters: " + e.getMessage());
			}

			try { // to process ftp

				if (!sameConnection) {
					if (localDir.startsWith(conRegExpBackslash)) {
						// replaceAll has bugs
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
							"connecting by " + protocol + " to host " + host + ", port " + port + ", local directory " + localDir + ", remote directory "
									+ remoteDir + (isFilePath ? ", file " + filePath : ", file specification " + fileSpec));

					boolean alternativeUse = true;// Wiederholungskriterien
					int isAlternativeParameterUse = 0;// Abbruchbedingug, versuch mit alternative Parameter nur einmal
					while (alternativeUse && isAlternativeParameterUse <= 1) {
						try {
							if (protocol.equalsIgnoreCase("ftp")) {
								try {
									initSOSFTP();
								}
								catch (Exception e) {
									e.printStackTrace();
									throw e;
								}
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
							if (sosString.parseToString(arguments, "user").length() == 0 && sosString.parseToString(user).equals("anonymous")) {
								setParam("user", "anonymous");
							}
							alternativeUse = false;

						}
						catch (Exception ex) {
							ex.printStackTrace(System.err);
							this.getLogger().debug1(
									"..error in ftp server init with [host=" + host + "], [port=" + port + "] " + SOSFTPCommand.getErrorMessage(ex));

							alternativeUse = alternativeHost.concat(alternativeUser).concat(alternativePassword).concat(alternativeAccount).length() > 0 || alternativePort != 0;

							if (alternativeUse && isAlternativeParameterUse == 0) {
								//  http://www.sos-berlin.com/jira/browse/SOSFTP-113
								getLogger().reset();  // reset last error due to second try with alternate credentials

								// try to connect with alternative parameters
								if (ftpClient != null) {
									int orderQueueLength = 0;
									this.getLogger().debug("..ftp server reply: " + ftpClient.getReplyString());

									if (ftpClient.isConnected() && !keepConnection || ftpClient.isConnected() && keepConnection && orderQueueLength < 1) {
										if (isLoggedIn)
											try {
												ftpClient.logout();
											}
											catch (Exception e) {
											} // no error handling
										this.getLogger().debug("..ftp server reply [logout]: " + ftpClient.getReplyString());
										try {
											ftpClient.disconnect();
										}
										catch (Exception e) {
										} // no error handling
									}

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
								// Raise statt throw http://www.sos-berlin.com/jira/browse/SOSFTP-113
								RaiseException("..error in ftp server init with [host=" + host + "], [port=" + port + "] " + ex.getMessage());
							}
						}
					}

				}
				else {
					this.getLogger().debug1("reusing connection from previous transfer");
				}

				if (!isLoggedIn) {
					RaiseException(".. server reply [login failed] [user=" + user + "], [account=" + account + "]: " + ftpClient.getReplyString());
				}

				if (ftpClient instanceof SOSFTP) {
					SOSFTP sosftp = (SOSFTP) ftpClient;

					if (passiveMode) {
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
						sosftp.passive();
						if (sosftp.getReplyCode() > ERROR_CODE) {
							RaiseException("..ftp server reply [passive]: " + ftpClient.getReplyString());
						}
						else {
							this.getLogger().debug("..ftp server reply [passive]: " + ftpClient.getReplyString());
						}
					}

					if (transferMode.equalsIgnoreCase("ascii")) {
						if (sosftp.ascii()) {
							this.getLogger().debug("..using ASCII mode for file transfer");
							this.getLogger().debug("..ftp server reply [ascii]: " + ftpClient.getReplyString());
						}
						else {
							RaiseException(".. could not switch to ASCII mode for file transfer ..ftp server reply [ascii]: " + ftpClient.getReplyString());
						}
					}
					else {
						if (sosftp.binary()) {
							this.getLogger().debug("using binary mode for file transfer");
							this.getLogger().debug("..ftp server reply [binary]: " + ftpClient.getReplyString());
						}
						else {
							RaiseException(".. could not switch to binary mode for file transfer .. ftp server reply [ascii]: " + ftpClient.getReplyString());

						}
					}
					if (strPreFtpCommands.length() > 0) {
						String[] strA = strPreFtpCommands.split(";");
						for (String strCmd : strA) {
							sosftp.sendCommand(strCmd);
							this.getLogger().debug("..ftp server reply [" + strCmd + "]: " + ftpClient.getReplyString());
						}
					}

					if (strControlEncoding.length() > 0) {
						sosftp.setControlEncoding(strControlEncoding);
						this.getLogger().debug("..ftp server reply [" + strControlEncoding + "]: " + ftpClient.getReplyString());
					}
				}

				if (simpleTransfer) {
					return sendSimpleTransfer();
				}
				makeDirs();

				if (isFilePath) {
					filelist = getFilePaths();
				}
				else {
					if (this.createFile(localDir).isDirectory()) {
						this.getLogger().debug1("local directory: " + localDir + ", remote directory: " + remoteDir + ", file specification: " + fileSpec);
						filelist = SOSFile.getFilelist(this.createFile(localDir).getAbsolutePath(), fileSpec, 0, recursive);
					}
					else {
						this.getLogger().debug1("local file: " + localDir + ", remote directory: " + remoteDir + ", file specification: " + fileSpec);
						filelist = new Vector();
						filelist.add(localDir);
					}
				}
				Iterator iterator = filelist.iterator();

				if (!zeroByteFiles) {
					while (iterator.hasNext()) {
						File checkFile = (File) iterator.next();
						if (checkFile.exists()) {
							if (checkFile.length() == 0) {
								this.getLogger().debug1("skipping transfer of local file: " + checkFile.getAbsolutePath() + " due to zero byte constraint");
								filelist.remove(checkFile);
								if (removeFiles) {
									if (!checkFile.delete()) {
										RaiseException("..error occurred, could not remove local file: " + checkFile.getAbsolutePath());
									}
									else {
										this.getLogger().debug1("removing file: " + checkFile.getAbsolutePath());
									}
								}
								zeroByteCount++;
								fileZeroByteNotificationBody += checkFile.getName() + "\n";
							}
						}
					}
				}
				if (skipTransfer) {
					foundFiles(filelist);
					if (sosString.parseToString(replacement).length() > 0 && sosString.parseToString(replacing).length() == 0 && !testmode) {
						renameAtomicSuffixTransferFiles(filelist);
					}
					return true;
				}
				else {

					// if(!polling(filelist))
					// return false;

					int intStep = 1;
					boolean flgError = false;
					try {

						sendFiles(filelist);
						// if (transActional) {
						/**
						 * austauschen. Erst auf dem Zielserver umbenennen, dann im source-server löschen
						 * kb 2011-06-07
						 */
						// getDeleteTransferFiles();//nur wenn transactional=yes ist
						intStep = 2;
						getRenameAtomicSuffixTransferFiles();
						intStep = 3;
						getDeleteTransferFiles();
						// }
					}
					catch (Exception e) {
						flgError = true;
						if (transActional) {
							getLogger().warn("could not complete transaction, cause " + e.toString());
						}
						else {
							getLogger().warn("could not complete transaction, cause " + e.toString());
						}

						throw e;
					}
					finally {
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
					sendMails();

					rc = printState(rc);

				}
				setParam("successful_transfers", String.valueOf(count));
				// return the number of transferred files
				setParam("status", "success");
				return rc;

			}
			catch (Exception e) {
				setParam("status", "error");
				failed_transfer++;
				setParam("failed_transfers", String.valueOf(failed_transfer));
				rc = false;
				e.printStackTrace(System.err);
				throw new Exception("could not process file transfer: " + e.getMessage());
			}
			finally {
				if (ftpClient != null) {
					int orderQueueLength = 0;
					this.getLogger().debug("..ftp server reply: " + ftpClient.getReplyString());
					if (ftpClient.isConnected() && !keepConnection || ftpClient.isConnected() && keepConnection && orderQueueLength < 1) {
						if (isLoggedIn)
							try {
								ftpClient.logout();
							}
							catch (Exception e) {
							} // no error handling
						this.getLogger().debug("..ftp server reply [logout]: " + ftpClient.getReplyString());
						try {
							ftpClient.disconnect();
						}
						catch (Exception e) {
						} // no error handling
					}
				}
				if (flgJumpTransferDefined) {// jump host ist angegeben, hier müssen z.B. temporär generierte Verzeichnisse auf Remote host
												// gelöscht werden
					rc = doPostCommands(rc, filelist);
				}
			}

		}
		catch (Exception e) {
			this.getLogger().warn("ftp processing failed: " + e.getMessage());

			if (sosString.parseToString(getLogger().getWarning()).length() > 0 || sosString.parseToString(getLogger().getError()).length() > 0) {
				getArguments().put("last_error", getLogger().getWarning() + " " + getLogger().getError());
			}
			writeHistory(localFile != null ? localFile.getAbsolutePath() : "", "");

			// getRemoveTransferFiles();//nur wenn transactional=yes ist
			return false;
		}
	}

	/**
	 * Parameter auslesen
	 * @throws Exception
	 */
	private void getParameter() throws Exception {
		try {
			if (sosString.parseToString(arguments.get("keep_connection")).length() > 0) {
				String keep = sosString.parseToString(arguments.get("keep_connection"));
				if (keep.equalsIgnoreCase("true") || keep.equalsIgnoreCase("1")) {
					keepConnection = true;
				}
				else {
					keepConnection = false;
				}
			}
			else {
				keepConnection = false;
			}

			if (sosString.parseToString(arguments.get("protocol")).length() > 0)
				protocol = sosString.parseToString(arguments.get("protocol"));

			port = 21;
			if (protocol.equalsIgnoreCase("sftp")) {
				sshBasedProtocol = true;
				port = 22;
			}

			retrieveCommonParameters();
			host = getParam("host", "");

			if (sosString.parseToString(arguments.get("port")).length() > 0)
				port = Integer.parseInt(sosString.parseToString(arguments.get("port")));

			if (sosString.parseToString(arguments.get("posixPermissions")).length() > 0)
				intPosixPermissions = Integer.parseInt(sosString.parseToString(arguments.get("posixPermissions")));
			else {
				intPosixPermissions = 0;
			}

			if (sosString.parseToString(arguments.get("user")).length() > 0)
				user = sosString.parseToString(arguments.get("user"));

			if (sosString.parseToString(arguments.get("password")).length() > 0)
				password = sosString.parseToString(arguments.get("password"));

			if (sosString.parseToString(arguments.get("account")).length() > 0)
				account = sosString.parseToString(arguments.get("account"));

			if (keepConnection) {
				sameConnection = ftpClient != null && ftpClient.isConnected() && lastHost.equalsIgnoreCase(host) && lastPort == port
						&& lastUser.equalsIgnoreCase(user) && lastAccount.equalsIgnoreCase(account);
			}
			if (ftpClient != null && ftpClient.isConnected() && !sameConnection) {
				try {
					if (isLoggedIn)
						ftpClient.logout();
					ftpClient.disconnect();
				}
				catch (Exception e) {
				}
			}

			lastHost = host;
			lastPort = port;
			lastUser = user;
			lastAccount = account;

			if (sosString.parseToString(arguments.get("transfer_mode")).length() > 0)
				transferMode = sosString.parseToString(arguments.get("transfer_mode"));

			if (sosString.parseToString(arguments.get("passive_mode")).length() > 0)
				passiveMode = sosString.parseToBoolean(arguments.get("passive_mode"));

			if (sosString.parseToString(arguments.get("remote_dir")).length() > 0)
				remoteDir = sosString.parseToString(arguments.get("remote_dir"));

			if (sosString.parseToString(arguments.get("local_dir")).length() > 0)
				localDir = sosString.parseToString(arguments.get("local_dir"));

			if (sosString.parseToString(arguments.get("file_spec")).length() > 0)
				fileSpec = sosString.parseToString(arguments.get("file_spec"));

			if (sosString.parseToString(arguments.get("atomic_suffix")).length() > 0)
				atomicSuffix = sosString.parseToString(arguments.get("atomic_suffix"));

			if (sosString.parseToString(arguments.get("check_size")).length() > 0)
				checkSize = !sosString.parseToString(arguments.get("check_size")).equals("1")
						&& !sosString.parseToString(arguments.get("check_size")).equalsIgnoreCase("true")
						&& !sosString.parseToString(arguments.get("check_size")).equalsIgnoreCase("yes") ? false : true;

			if (sosString.parseToString(arguments.get("overwrite_files")).length() > 0)
				overwriteFiles = !sosString.parseToString(arguments.get("overwrite_files")).equals("1")
						&& !sosString.parseToString(arguments.get("overwrite_files")).equalsIgnoreCase("true")
						&& !sosString.parseToString(arguments.get("overwrite_files")).equalsIgnoreCase("yes") ? false : true;

			if (sosString.parseToString(arguments.get("append_files")).length() > 0)
				appendFiles = sosString.parseToString(arguments.get("append_files")).equals("1")
						|| sosString.parseToString(arguments.get("append_files")).equalsIgnoreCase("true")
						|| sosString.parseToString(arguments.get("append_files")).equalsIgnoreCase("yes") ? true : false;

			if (sosString.parseToString(arguments.get("remove_files")).length() > 0)
				removeFiles = sosString.parseToBoolean(arguments.get("remove_files"));

			if (sosString.parseToString(arguments, "skip_transfer").length() > 0)
				skipTransfer = sosString.parseToBoolean(arguments.get("skip_transfer"));

			if (sosString.parseToString(arguments.get(conParameterMAKE_DIRS)).length() > 0)
				flgMakeDirs = sosString.parseToString(arguments.get(conParameterMAKE_DIRS)).equals("1")
						|| sosString.parseToString(arguments.get(conParameterMAKE_DIRS)).equalsIgnoreCase("true")
						|| sosString.parseToString(arguments.get(conParameterMAKE_DIRS)).equalsIgnoreCase("yes") ? true : false;

			if (sosString.parseToString(arguments.get(conParamFORCE_FILES)).length() > 0) {
				forceFiles = !sosString.parseToString(arguments.get(conParamFORCE_FILES)).equals("1")
						&& !sosString.parseToString(arguments.get(conParamFORCE_FILES)).equalsIgnoreCase("true")
						&& !sosString.parseToString(arguments.get(conParamFORCE_FILES)).equalsIgnoreCase("yes") ? false : true;
			}
			else {
				forceFiles = true;
			}

			compressFiles = getBooleanParam("compress_files", "false");
			compressedFileExtension = getParam("compressed_file_extension", ".gz");
			// String strCompress = sosString.parseToString(arguments.get("compress_files"));
			// if (strCompress.length() > 0)
			// compressFiles = (strCompress.equals("1")
			// || strCompress.equalsIgnoreCase("true")
			// || strCompress.equalsIgnoreCase("yes") ? true : false);
			//
			// if (sosString.parseToString(arguments.get("compressed_file_extension")).length() > 0) {
			// compressedFileExtension = sosString.parseToString(arguments.get("compressed_file_extension"));
			// }
			// else {
			// compressedFileExtension = ".gz";
			// }

			if (sosString.parseToString(arguments.get("file_zero_byte_transfer")).length() > 0) {
				if (sosString.parseToString(arguments.get("file_zero_byte_transfer")).equals("1")
						|| sosString.parseToString(arguments.get("file_zero_byte_transfer")).equalsIgnoreCase("true")
						|| sosString.parseToString(arguments.get("file_zero_byte_transfer")).equalsIgnoreCase("yes")) {
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

			if (arguments.get("replacement") != null) {
				replacement = sosString.parseToString(arguments.get("replacement"));
			}

			if (sosString.parseToString(arguments.get("replacing")).length() > 0) {
				replacing = sosString.parseToString(arguments.get("replacing"));
			}
			else {
				if (sosString.parseToString(arguments.get("replacement")).length() > 0) {
					replacing = ""; // empty String, not null
				}
			}

			if (sosString.parseToString(arguments.get(RECURSIVE)).length() > 0) {
				String sRecursive = "";
				sRecursive = sosString.parseToString(arguments.get(RECURSIVE));
				recursive = sosString.parseToBoolean(sRecursive);
			}

			if (sosString.parseToString(arguments.get("poll_timeout")).length() > 0) {
				pollTimeout = Integer.parseInt(sosString.parseToString(arguments.get("poll_timeout")));
			}

			if (sosString.parseToString(arguments.get("poll_interval")).length() > 0) {
				pollIntervall = Integer.parseInt(sosString.parseToString(arguments.get("poll_interval")));
			}

			checkParameter();

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
				alternativePassiveMode = sosString.parseToBoolean(arguments.get("alternative_passive_mode"));

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

				if (sosString.parseToString(arguments.get("ssh_auth_method")).length() > 0) {
					if (sosString.parseToString(arguments.get("ssh_auth_method")).equalsIgnoreCase("publickey")
							|| sosString.parseToString(arguments.get("ssh_auth_method")).equalsIgnoreCase("password")) {
						authenticationMethod = sosString.parseToString(arguments.get("ssh_auth_method"));
					}
					else {
						RaiseException("invalid authentication method [publickey, password] specified: "
								+ sosString.parseToString(arguments.get("ssh_auth_method")));
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
					throw new NumberFormatException("illegal non-numeric value for parameter [http_proxy_port]: "
							+ sosString.parseToString(arguments.get("http_proxy_port")));
				}
			}

			if (sosString.parseToString(arguments.get("transactional")).length() > 0)
				transActional = sosString.parseToBoolean(arguments.get("transactional"));

			testmode = sosString.parseToBoolean(arguments.get("testmode"));

			if (sosString.parseToString(arguments.get("mail_smtp")).length() > 0)
				mailSMTP = sosString.parseToString(arguments.get("mail_smtp"));

			if (sosString.parseToString(arguments.get("mail_from")).length() > 0)
				mailFrom = sosString.parseToString(arguments.get("mail_from"));

			if (sosString.parseToString(arguments.get("mail_queue_dir")).length() > 0)
				mailQueueDir = sosString.parseToString(arguments.get("mail_queue_dir"));

		}
		catch (Exception e) {
			RaiseException("error while processing parameters in " + sos.util.SOSClassUtil.getMethodName() + ". cause: " + e.toString());

		}
	}

	/**
	 * Überprüfen der Parameter
	 * @throws Exception
	 */
	private void checkParameter() throws Exception {
		if (replacing != null && replacement == null) {
			RaiseException("parameter is missing for specified parameter [replacing=" + replacing + "]: [replacement]");
		}

		/**
		 * kb
		 * weil bei transactional der atomic_suffix umgebogen wird auf replacement (!) kommt es hier zu einer Fehlermeldung,
		 * die der Nutzer beim besten willen nicht versteht, weil er doch gar kein "replacement" angegeben hat.
		 * Deshalb: Prüfung hier rausnehmen. Kann nicht schaden.
		 */
		// if (replacing == null && replacement != null) {
		// RaiseException("parameter is missing for specified parameter [replacement=" + replacement + "]: [replacing]");
		// }

		if (appendFiles && compressFiles) {
			RaiseException("unsupported parameter settings [append_files, compress_files]: cannot append to compressed files");
		}

		if (sosString.parseToString(arguments.get("file_path")).length() > 0) {
			filePath = sosString.parseToString(arguments.get("file_path"));
			isFilePath = true;
		}
	}

	private void getFileNotificationParameter() throws Exception {
		try {
			if (fileNotificationTo != null && fileNotificationTo.length() > 0) {
				if (fileNotificationSubject == null || fileNotificationSubject.length() == 0) {
					fileNotificationSubject = "[debug1] SOSFTPCommand";
				}

				if (fileNotificationBody == null || fileNotificationBody.length() == 0) {
					fileNotificationBody = "The following files have been sent:\n\n";
				}
			}

			if (fileZeroByteNotificationTo != null && fileZeroByteNotificationTo.length() > 0) {
				if (fileZeroByteNotificationSubject == null || fileZeroByteNotificationSubject.length() == 0) {
					fileZeroByteNotificationSubject = "[warning] SOSFTPCommand";
				}

				if (fileZeroByteNotificationBody == null || fileZeroByteNotificationBody.length() == 0) {
					fileZeroByteNotificationBody = "The following files have not been sent and were removed due to zero byte constraints:\n\n";
				}
			}
		}
		catch (Exception e) {
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
			this.getLogger().debug(".. job parameter [append_files]                       : " + appendFiles);
			this.getLogger().debug(".. job parameter [overwrite_files]                    : " + overwriteFiles);
			this.getLogger().debug(".. job parameter [remove_files]                       : " + removeFiles);
			this.getLogger().debug(".. job parameter [force_files]                        : " + forceFiles);
			this.getLogger().debug(".. job parameter [skip_transfer]                      : " + skipTransfer);
			this.getLogger().debug(".. job parameter [make_dirs]                          : " + flgMakeDirs);
			this.getLogger().debug(".. job parameter [compress_files]                     : " + compressFiles);
			this.getLogger().debug(".. job parameter [compressed_file_extension]          : " + compressedFileExtension);

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

			this.getLogger().debug(".. job parameter [file_path]                          : " + filePath);
			this.getLogger().debug(".. job parameter [transactional]                      : " + transActional);

			this.getLogger().debug(".. job parameter [poll_timeout]                       : " + pollTimeout);
			this.getLogger().debug(".. job parameter [poll_interval]                      : " + pollIntervall);

			this.getLogger().debug(".. job parameter [testmode]                           : " + testmode);

			this.getLogger().debug(".. job parameter [mail_smtp]                          : " + mailSMTP);
			this.getLogger().debug(".. job parameter [mail_from]                          : " + mailFrom);
			this.getLogger().debug(".. job parameter [mail_queue_dir]                     : " + mailQueueDir);

			// alternative Parameter
			this.getLogger().debug(".. job parameter [alternative_host]                   : " + alternativeHost);
			this.getLogger().debug(".. job parameter [alternative_port]                   : " + alternativePort);
			this.getLogger().debug(".. job parameter [alternative_user]                   : " + alternativeUser);
			this.getLogger().debug(".. job parameter [alternative_password]               : " + alternativePassword);
			this.getLogger().debug(".. job parameter [alternative_account]                : " + alternativeAccount);
			this.getLogger().debug(".. job parameter [alternative_remote_dir]             : " + alternativeRemoteDir);
			this.getLogger().debug(".. job parameter [alternative_passive_mode]           : " + alternativePassiveMode);
			this.getLogger().debug(".. job parameter [alternative_transfer_mode]          : " + alternativeTransferMode);

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
			RaiseException("..error in setAlternativeParameter, cause: " + e);
		}
	}

	/**
	 * Verzeichnis generieren
	 * @throws Exception
	 */
	private void makeDirs() throws Exception {
		try {
			boolean cd = true;

			if (flgMakeDirs) {
				if (ftpClient.changeWorkingDirectory(remoteDir)) { // error code signals non-existing directory
					this.getLogger().debug("..ftp server reply [directory exists] [" + remoteDir + "]: " + ftpClient.getReplyString());
					cd = true;
				}
				else {
					boolean ok = ftpClient.mkdir(remoteDir, intPosixPermissions);
					if (!ok) {
						RaiseException("..error occurred creating directory [" + remoteDir + "]: " + ftpClient.getReplyString());
					}
					else {
						this.getLogger().debug("..ftp server reply [mkdir] [" + remoteDir + "]: " + ftpClient.getReplyString());
						cd = ftpClient.changeWorkingDirectory(remoteDir);
					}
				}
			}
			else
				if (remoteDir != null && remoteDir.length() > 0) {
					cd = ftpClient.changeWorkingDirectory(remoteDir);
				}

			if (!cd && sosString.parseToString(alternativeRemoteDir).length() > 0) {// alternative Parameter
				this.getLogger().debug("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
				this.getLogger().debug1("..try with alternative parameter [remoteDir=" + alternativeRemoteDir + "]");
				cd = ftpClient.changeWorkingDirectory(alternativeRemoteDir);
				remoteDir = alternativeRemoteDir;
			}

			if (!cd) {
				RaiseException("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
			}
			else {
				this.getLogger().debug("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			RaiseException("..error in Make_Dirs, cause: " + e);
		}
	}

	/**
	 * Wenn keine Datei transferiert werden.
	 * @throws Exception
	 */
	private void foundFiles(final Vector filelist_) throws Exception {
		try {
			getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
			Iterator iterator = filelist_.iterator();
			while (iterator.hasNext()) {

				Object fn = iterator.next();
				getLogger().debug1(fn + " found");
			}
			if (banner)
				getLogger().info(filelist_.size() + " files found.");
			else
				getLogger().debug1(filelist_.size() + " files found.");

			filelist = filelist_;
			this.getLogger().debug("..ftp server reply [listNames] " + ftpClient.getReplyString());

		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
		}
	}

	/**
	 * Senden alle Dateien
	 * @param filelist
	 * @throws Exception
	 */
	private void sendFiles(final Vector filelist) throws Exception {
		try {
			// count = 0;
			getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
			Iterator iterator = filelist.iterator();
			transActionalLocalFiles = new ArrayList();

			while (iterator.hasNext()) {

				Object fn = iterator.next();

				long bytesSend = 0;
				setParam(FILE_SIZE, "");
				if (!polling(fn.toString()))
					continue;
				localFile = new File(fn.toString());
				File subParent = null;
				String subPath = "";

				if (!localFile.exists()) {
					RaiseException(".. file [" + localFile + "] does not exist ");
				}

				if (recursive && !isFilePath) {
					// Überprüfen, ob das Verzeichnis auf den FTP Server existiert, wenn nicht dann soll das gleiche Verzeichnis generiert
					// werden
					if (localFile.getParent() != null && localFile.getParentFile().isDirectory()) { // es existieren Vaterknoten
						/**
						 * The first character of the name of the subfolder is truncated, if "+1" is executed.
						 * This appears under Windows and unix.
						 *
						 * SOSFTP-95
						 */
						// subPath = fn.toString().substring((localDir.length() + 1)); // Unterverzeichnisse sind alle Verzeichnisse
						// unterhalb der localDir
						subPath = fn.toString().substring(localDir.length()); // Unterverzeichnisse sind alle Verzeichnisse unterhalb der
																				// localDir
						subParent = new File(subPath).getParentFile();

						if (subParent != null) {
							subPath = subPath.replaceAll(conRegExpBackslash, "/");
							subPath = subPath.substring(0, subPath.length() - new File(fn.toString()).getName().length() - 1);
							this.getLogger().debug4(".. creating sub-directory on remote host: " + subPath);
							String[] ftpFiles = ftpClient.listNames(remoteDir + "/" + subPath);
							if (ftpFiles == null || ftpFiles.length == 0) {
								boolean ok = ftpClient.mkdir(remoteDir + "/" + subPath, intPosixPermissions);
								if (!ok) {
									RaiseException("..error occurred creating sub-directory [" + remoteDir + "/" + subPath + "]: " + ftpClient.getReplyString());
								}
								else {
									this.getLogger().debug(
											"..ftp server reply [mkdir sub-directory] [" + remoteDir + "/" + subPath + "]: " + ftpClient.getReplyString());
								}
							}
						}
					}
				}
				if (localFile == null)
					continue;
				if (!zeroByteFiles && localFile.length() == 0)
					continue;

				File transferFile = null;
				String transferFilename = null;
				File sourceFile = null;
				if (compressFiles) {
					transferFile = new File(localFile.getAbsolutePath() + compressedFileExtension);
					sourceFile = File.createTempFile("sos", ".gz");
				}
				else {
					transferFile = new File(localFile.getAbsolutePath());
					sourceFile = new File(localFile.getAbsolutePath());
				}

				transferFilename = transferFile.getName();
				if (replacement != null && replacing != null && replacing.length() > 0) {
					String currTransferFilename = SOSFileOperations.getReplacementFilename(transferFile.getName(), replacing, replacement);
					String currPath = transferFile.getParent() != null && transferFile.getParent().length() > 0 ? normalized(transferFile.getParent()) : "";
					this.getLogger().debug1("source filename [" + transferFile.getName() + "] is renamed to: " + currTransferFilename);
					transferFile = new File(currPath + currTransferFilename);
					transferFilename = transferFile.getName();
				}

				if (subParent != null && recursive) {
					transferFile = new File((subParent != null ? subParent.getName() + "/" : "") + transferFile.getName());
					transferFilename = (subParent != null ? subPath + "/" : "") + transferFile.getName();
				}

				if (!appendFiles && atomicSuffix != null && atomicSuffix.length() > 0) {
					if (!overwriteFiles) {
						Vector ftpFiles = ftpClient.nList(transferFile.getName());
						// fehler wird ueber nlist return value verwertet
						this.getLogger().debug("..ftp server reply [file exists] [" + transferFilename + "]: " + ftpClient.getReplyString());
						if (!ftpFiles.isEmpty()) {
							this.getLogger().debug1("..ftp transfer skipped for file [no overwrite]: " + transferFilename);
							continue;
						}
					}

					if (compressFiles) {
						SOSGZip.compressFile(localFile, sourceFile);
					}

					this.getLogger().info(
							"sending file : " + transferFile.getAbsolutePath() + atomicSuffix + " from source/temp file " + sourceFile.getAbsolutePath());
					bytesSend = ftpClient.putFile(sourceFile.getAbsolutePath(), transferFilename + atomicSuffix);
					transActionalRemoteFiles = transferFilename + atomicSuffix + ";" + sosString.parseToString(transActionalRemoteFiles);
					this.getLogger().info("file_size    : " + bytesSend + " bytes");
					setParam(FILE_SIZE, String.valueOf(bytesSend));
					setParam(FILE_MODIFICATION_DATE, String.valueOf(sourceFile.lastModified()));

					setParam("successful_transfers", String.valueOf(++successful_transfers));
					writeHistory(sourceFile.getAbsolutePath(), transferFile.getAbsolutePath());

					if (overwriteFiles) {
						Vector ftpFiles = ftpClient.nList(transferFilename);
						if (!ftpFiles.isEmpty() && ftpFiles.contains(transferFilename)) {
							boolean ok = ftpClient.delete(transferFilename);
							if (!ok) {
								RaiseException("..error occurred overwriting file [" + transferFilename + "]: " + ftpClient.getReplyString());
							}
							else {
								this.getLogger().debug("..ftp server reply [delete] [" + transferFilename + "]: " + ftpClient.getReplyString());
							}
						}
						if (transActional)
							ftpClient.nList(".");// nList liefert einen repyCode > 500, wenn transferFilename nicht vorhanden ist
					}
					this.getLogger().debug1("renaming file: " + transferFilename);

					if (!transActional)
						ftpClient.rename(transferFilename + atomicSuffix, transferFilename);

				}
				else {
					if (!overwriteFiles && !appendFiles) {
						Vector ftpFiles = ftpClient.nList(transferFilename);
						this.getLogger().debug("..ftp server reply [file exist] [" + transferFilename + "]: " + ftpClient.getReplyString());
						boolean bWhileContinue = false;
						for (int i = 0; i < ftpFiles.size(); i++) {
							String currFtpFilename = sosString.parseToString(ftpFiles.get(i));
							if (currFtpFilename.equalsIgnoreCase(transferFilename)) {
								this.getLogger().debug1("..ftp transfer skipped for file [no overwrite]: " + transferFilename);
								bWhileContinue = true;
								continue;
							}
						}
						if (bWhileContinue)
							continue;
					}

					if (compressFiles) {
						SOSGZip.compressFile(localFile, sourceFile);
					}

					if (appendFiles) {
						if (ftpClient instanceof SOSFTP) {
							this.getLogger().info("sending file : " + transferFile.getAbsolutePath() + " (append)");
							bytesSend = ((SOSFTP) ftpClient).appendFile(sourceFile.getAbsolutePath(), transferFilename);
							transActionalRemoteFiles = transferFilename + atomicSuffix + ";" + sosString.parseToString(transActionalRemoteFiles);
							this.getLogger().info("file_size    : " + bytesSend + " bytes");
							setParam(FILE_SIZE, String.valueOf(bytesSend));
							setParam("successful_transfers", String.valueOf(++successful_transfers));
							writeHistory(sourceFile.getAbsolutePath(), transferFile.getAbsolutePath());
						}
						else {
							RaiseException("append is not implemented for protocol " + protocol);
						}
					}
					else {
						if (compressFiles) {
							this.getLogger().info("sending file : " + transferFile.getAbsolutePath() + " from temporary file " + sourceFile.getAbsolutePath());
						}
						else {
							this.getLogger().info("sending file : " + transferFile.getAbsolutePath());
						}

						String strEncodedFileName = doEncoding(transferFilename, strFileNameEncoding);
						bytesSend = ftpClient.putFile(sourceFile.getAbsolutePath(), strEncodedFileName);
						transActionalRemoteFiles = transferFilename + atomicSuffix + ";" + sosString.parseToString(transActionalRemoteFiles);
						this.getLogger().info("file_size    : " + bytesSend + " bytes");
						setParam(FILE_SIZE, String.valueOf(bytesSend));
						setParam("successful_transfers", String.valueOf(++successful_transfers));
						writeHistory(sourceFile.getAbsolutePath(), transferFile.getAbsolutePath());
					}
				}

				if (ftpClient instanceof SOSFTP) {
					if (((SOSFTP) ftpClient).getReplyCode() > ERROR_CODE) {
						RaiseException("..error occurred sending file [" + transferFile.getAbsolutePath() + "]: " + ftpClient.getReplyString());
					}
					else {
						this.getLogger().debug(
								"..ftp server reply [put] [" + transferFilename + "---" + transferFile.getAbsolutePath() + ", size=" + bytesSend + "]: "
										+ ftpClient.getReplyString());
					}
				}

				long lngTransferFileSize = transferFile.length();
				if (checkSize && lngTransferFileSize > 0 && lngTransferFileSize != bytesSend) {
					RaiseException("..error occurred sending file, target file size [" + lngTransferFileSize + "] does not match number of bytes transferred ["
							+ bytesSend + "]");
				}

				if (compressFiles) {
					if (!sourceFile.delete()) {
						RaiseException("..error occurred, could not delete compressed local file: " + sourceFile.getAbsolutePath());
					}
					else {
						this.getLogger().debug3("delete compressed file: " + sourceFile.getAbsolutePath());
					}
				}

				if (transActional) {
					transActionalLocalFiles.add(localFile);
				}
				else
					if (removeFiles) {
						if (!localFile.delete()) {
							RaiseException(".. error occurred, could not remove local file: " + localFile.getAbsolutePath());
						}
						else {
							this.getLogger().debug1("removing file: " + localFile.getAbsolutePath());
						}
					}
				fileNotificationBody += transferFile.getName() + "\n";
				count++;
			}

		}
		catch (Exception e) {
			throw new JobSchedulerException("error in  " + sos.util.SOSClassUtil.getMethodName() + " could not send files, cause: " + e, e);
		}
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
	private boolean doPostCommands(boolean rc, final Vector filelist) throws Exception {
		try {
			getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
			setParam("xx_make_temp_directory_xx", "ok");// hilfsvariable, wenn dieses key existiert, dann gilt im execute diese
														// Parameter
			if (!getLogger().hasWarnings()) {// fehler beim transferieren, also vom jump_host nicht weitersenden
				if (sosString.parseToString(postCommands).length() > 0) {
					this.setCommands(postCommands.split(getCommandDelimiter()));
					if (!execute())
						RaiseException("error occurred processing command:" + normalizedPassword(postCommands));
				}
			}
			// Auf jeden fall soll das Temporäre Verzeichnis gelöscht werde
			// jump_command nicht ausgelesen //
			String com = sosString.parseToString(arguments.get("jump_command")) + " -operation=remove_temp_directory -input=\"" + tempJumpRemoteDir + "\"";
			this.setCommands(com.split(getCommandDelimiter()));
			if (!execute()) {
				arguments.remove("xx_make_temp_directory_xx");
				getLogger().warn("error occurred processing command:" + tempJumpRemoteDir);
			}
			arguments.remove("xx_make_temp_directory_xx");

			if (rc) {
				// jump_host hat erfolgreich alle Dateien an target_host weitergegeben. Jetzt
				// können die lokalen Dateien gelöscht werden, wenn der Parameter remove_files=yes angegeben ist
				if (!transActional && sosString.parseToBoolean(arguments.get("remove_after_jump_transfer"))) {
					for (int i = 0; i < filelist.size(); i++) {
						File f = new File(sosString.parseToString(filelist.get(i)));
						if (!f.delete()) {
							RaiseException("..error occurred, could not remove local file: " + f.getAbsolutePath());
						}
						else {
							this.getLogger().debug1("removing localfile: " + f.getAbsolutePath());
						}
					}
					arguments.remove("remove_after_jump_transfer");
				}
				else {
					rc = false;
				}
			}
		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
		}
		return rc;
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
	 * Statt ein Verzeichnis können eine oder mehrere - mit semikolen getrennte - dateien zum transferieren angegeben werden
	 * @return
	 * @throws Exception
	 */
	private Vector<String> getFilePaths() throws Exception {
		try {
			Vector<String> filelist1 = new Vector<String>();
			String[] split = filePath.split(";");
			String strLocalDir = normalized(localDir);
			int intLocalDirLen = strLocalDir.length();
			for (String element : split) {
				String strT = element;
				if (strT != null && strT.length() > 0) {
					if (intLocalDirLen > 0 && !sosString.parseToString(localDir).equalsIgnoreCase(defaultLocalDir)) {
						// \change kb 2012-04-25
						// Modify the filename only when the localdir is not already specified
						// JIRA: SOSFTP-106
						// OTRS: http://www.sos-berlin.com/otrs/index.pl?Action=AgentTicketZoom&TicketID=100
						if (isAPathName(strT) == false) {
							strT = strLocalDir + strT;
						}
					}
					filelist1.add(strT);
				}
			}
			fileSpec = ".*";
			return filelist1;
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw new JobSchedulerException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e, e);
		}
	}

	/**
	 * Zustand ausgeben. Erst ab log_level 1
	 * @param rc
	 * @return
	 * @throws Exception
	 */
	private boolean printState(boolean rc) throws Exception {
		try {
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
					rc = !forceFiles ? true : !zeroByteFilesRelaxed;
					break;
				case 1:
					state = "1 file transferred" + (zeroByteCount > 0 ? ", " + zeroByteCount + " files skipped due to zero byte constraint" : "");
					this.getLogger().debug1(state);
					rc = true;
					break;
				default:
					state = count + " files transferred" + (zeroByteCount > 0 ? ", " + zeroByteCount + " files skipped due to zero byte constraint" : "");
					this.getLogger().debug1(state);
					rc = true;
					break;
			}
		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
		}
		return rc;
	}

	@Override
	public boolean install() throws Exception {
		// Hier werden alle Bibliotheken (jar-Dateien) in installpaths (parameter classpath_base) übertragen.
		// Existiert ein sosftp.sh oder/und sosftp.sh, dann werden diese Dateien in ihrem CLASSPATH_BASE angepasst
		// und mit übertragen.
		//
		getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());

		String classPathBase = "";
		String installFiles = null;
		try {
			/*try {
				if(getSettingsFile() != null && getSettingsFile().length() > 0)
					this.setSettings(new SOSProfileSettings( this.getSettingsFile(), this.getSettingsSection(), this.getLogger()));
			} catch (Exception e) {
				RaiseException("error occurred retrieving settings: " + e.getMessage());
			}*/

			mergeSettings();

			if (sosString.parseToString(arguments.get("classpath_base")).length() > 0) {
				classPathBase = sosString.parseToString(arguments.get("classpath_base"));
				getLogger().debug1(".. parameter [classpath_base]: " + classPathBase);
			}

			if (sosString.parseToString(arguments, "operation").equals("install_doc")) {
				arguments.setProperty("remote_dir", normalized(classPathBase) + "doc");
			}
			else {
				arguments.setProperty("remote_dir", classPathBase);
			}
			installFiles = getInstallFiles(classPathBase);
			setParam("file_path", installFiles);

			if (banner)
				getLogger().info(getBanner(true));
			else
				getLogger().debug1(getBanner(true));

			if (!send()) {
				getLogger().warn("Could not send install files.");
				return false;
			}

			if (sosString.parseToString(arguments.get("protocol")).equalsIgnoreCase("sftp") && !flgJumpTransferDefined) {
				// sosftp.sh Datei die execute Berechtigung geben, wenn Protokoll sftp ist
				Iterator it = arguments.keySet().iterator();
				Properties jumpProp = new Properties();
				while (it.hasNext()) {
					// wenn keine jump_parameter angegeben ist
					Object key = sosString.parseToString(it.next());
					if (!arguments.contains("jump_" + key))
						jumpProp.put("jump_" + key, sosString.parseToString(arguments.get(key)));

				}
				arguments.putAll(jumpProp);

				if (!sosString.parseToString(arguments, "jump_ssh_auth_method").equalsIgnoreCase("publickey"))
					setParam("jump_ssh_auth_method", "password");

				try {
					boolean isNoWindow = true;
					try {
						isNoWindow = remoteIsWindowsShell(true);
					}
					catch (Exception r) {
						getLogger().info("Failed to check if remote system is Linux to change the File Attibutes Properties");
					}
					if (!isNoWindow) {
						setParam("xx_make_temp_directory_xx", "ok");// hilfsvariable, wenn dieses key existiert, dann gilt im execute
																	// diese Parameter
						// jump_command nicht ausgelesen
						String commands = "chmod u+x " + classPathBase + "/sosftp.sh";
						if (!sosString.parseToString(arguments, "jump_ssh_auth_method").equalsIgnoreCase("publickey"))
							setParam("jump_ssh_auth_method", "password");

						this.setCommands(commands.split(getCommandDelimiter()));
						String sh = installFiles.split(";")[installFiles.split(";").length - 1];
						setParam("jump_command_script_file", sh);
						flgJumpTransferDefined = false;
						if (!execute()) {
							RaiseException("error occurred processing command: " + normalizedPassword(commands));
						}
						arguments.remove("xx_make_temp_directory_xx");

					}
				}
				catch (Exception e) {
					getLogger().info("error occurred processing command: " + commands + ", cause: " + e.toString());
				}
			}

			return true;
		}
		catch (Exception e) {
			this.getLogger().warn("install failed: " + e.getMessage());
			return false;
		}
		finally {
			if (installFiles != null) {
				String[] split = installFiles.split(";");
				for (String element : split) {
					if (element.endsWith("sosftp.sh") || element.endsWith("sosftp.cmd")) {

						getLogger().debug5("deleting temp file: " + element + ": " + new File(element).delete());
					}
				}
			}
		}
	}

	/**
	 * Liefert alle Dateinamen die für die Installation auf den Remote Host hgebraucht werden.
	 *
	 * @param classPathBase
	 * @return alle zu transferierende Dateinamen mit semikolon getrennt
	 * @throws Exception
	 */
	private String getInstallFiles(final String classPathBase) throws Exception {
		String filePath1 = "";
		BufferedWriter out = null;
		BufferedWriter out2 = null;
		String shellscript = "";
		String cmd = "";
		File sosftpSH = null;
		File sosftpCMD = null;
		// String classPath = "CLASSPATH_BASE=";//script wurde verändert
		String classPath = "INSTALL_PATH=";
		try {

			String localDir1 = "";
			if (sosString.parseToString(arguments, "local_dir").length() > 0)
				localDir1 = sosString.parseToString(arguments, "local_dir") + "/";

			if (sosString.parseToString(arguments, "operation").equals("install_doc")) {
				filePath1 = installDocPaths.replaceAll("%\\{local_dir\\}/", localDir1);
			}
			else {

				// aktuelle Biblioteken mit Reevisionnr bestimmen
				installpathsWithRevNr = installpathsWithRevNr.replaceAll("%\\{local_dir\\}/", localDir1);
				String[] split = installpathsWithRevNr.split(";");
				for (String element : split) {
					Vector v = SOSFile.getFilelist(localDir1, new File(element).getName(), Pattern.CASE_INSENSITIVE);
					if (!v.isEmpty()) {
						filePath1 = filePath1.concat(sosString.parseToString(v.get(v.size() - 1))).concat(";");
					}
					else {
						RaiseException("missing library " + element + "to install sosftp");
					}
				}

				filePath1 = filePath1 + installpaths.replaceAll("%\\{local_dir\\}/", localDir1);

				String path = normalized(System.getProperty("java.io.tmpdir"));

				File fsh = new File(localDir1, "sosftp.sh");
				if (fsh.exists()) {
					shellscript = sos.util.SOSFile.readFile(fsh);

					// int pos1 = shellscript.indexOf("CLASSPATH_BASE=");
					int pos1 = shellscript.indexOf(classPath);

					int pos2 = shellscript.indexOf("\n", pos1);
					if (pos1 != -1 && pos2 != -1)
						shellscript = shellscript.substring(0, pos1) + classPath + classPathBase + shellscript.substring(pos2);
					getLogger().debug("shell script: " + shellscript);

					// für sh datei
					String currShellscript = shellscript;
					sosftpSH = new File(path, "sosftp.sh");
					sosftpSH.deleteOnExit();
					out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sosftpSH)));
					out.write(currShellscript);
					out.close();
				}
				else {
					getLogger().info("there is no Shell Script to install: " + new File(localDir1, "sosftp.sh"));
				}

				// für cmd Datei
				File fcmd = new File(localDir1, "sosftp.cmd");
				if (fcmd.exists()) {
					cmd = sos.util.SOSFile.readFile(fcmd);

					int pos1 = cmd.indexOf(classPath);
					int pos2 = cmd.indexOf("\n", pos1);
					if (pos1 != -1 && pos2 != -1)
						cmd = cmd.substring(0, pos1) + classPath + classPathBase + cmd.substring(pos2);
					getLogger().debug("cmd script: " + cmd);
					String currCMDFile = cmd.replaceAll("%\\{classpath_base\\}", classPathBase);
					sosftpCMD = new File(path, "sosftp.cmd");
					sosftpCMD.deleteOnExit();
					out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sosftpCMD)));
					out2.write(currCMDFile);
					out2.close();
				}
				else {
					getLogger().info("there is no cmd Script to install: " + new File(localDir1 + "sosftp.cmd"));
				}

				if (sosftpCMD != null)
					filePath1 = filePath1 + ";" + sosftpCMD.getCanonicalPath();

				if (sosftpSH != null)
					filePath1 = filePath1 + ";" + sosftpSH.getCanonicalPath();
			}
			arguments.remove("local_dir");

		}
		catch (Exception e) {
			RaiseException("error while get instal files, cause: " + e.toString());
		}
		finally {
			if (out != null)
				out.close();
			if (out2 != null)
				out2.close();
		}

		return filePath1;
	}

	/**
	 * Erst wenn alle Dateien erfolgreich transferieriert wurden, dann sollen die lokalen Dateien gelöscht werden.
	 * Parameter = transactional = yes und remove_files=yes
	 * @throws Exception
	 */
	public void getDeleteTransferFiles() throws Exception {
		if (transActional && (removeFiles || sosString.parseToBoolean(arguments.get("remove_after_jump_transfer")))) {
			if (transActionalLocalFiles == null)
				return;

			getLogger().debug(".. mark transactional files for removal: " + transActionalLocalFiles);
			// transActional LocalFiles löschen
			Properties p = new Properties();
			p.put("operation", "delete_local_files");
			p.put("files", transActionalLocalFiles);
			listOfSuccessTransfer.add(p);
		}
	}

	/**
	 * Bei einer transfer Fehler müssen alle bereits transferierte Dateien gelöscht werden.
	 * Gilt für Parameter transActional = yes
	 *
	 * @throws Exception
	 */
	public void getRemoveTransferFiles() throws Exception {

		getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
		if (transActional && sosString.parseToString(transActionalRemoteFiles).length() > 0) {
			if (flgJumpTransferDefined) {
				String com = getCommands()[0];

				String split[] = com.split(" -");
				String newarg = split[0];
				Properties p = (Properties) arguments.clone();
				for (int i = 1; i < split.length; i++) {

					if (split[i].indexOf("skip_transfer") == -1 && split[i].indexOf("replacement") == -1 && split[i].indexOf("file_path") == -1
							&& split[i].indexOf("operation") == -1 && split[i].indexOf("atomic_suffix") == -1 && split[i].indexOf("remove_files") == -1
							&& split[i].indexOf("transactional") == -1)
						newarg = newarg + " -" + split[i];

				}

				newarg = newarg + " " + transActionalRemoteFiles.replaceAll(";", " ")
						+ " -skip_transfer=\"yes\" -transactional=\"false\" -operation=\"remove\"" + " -remove_files=\"yes\"";

				p.put("command", newarg);
				p.put("operation", "execute");
				p.put("xx_make_temp_directory_success_transfer_xx", "ok");
				listOfErrorTransfer.add(p);
			}
			else {
				// getLogger().info(".. remove transactional files " + transActionalRemoteFiles);
				Properties p = (Properties) arguments.clone();
				getLogger().debug(".. mark transactional files for removal on error: " + transActionalLocalFiles);

				Iterator it = p.keySet().iterator();
				ArrayList rem = new ArrayList();
				while (it.hasNext()) {
					String key = sosString.parseToString(it.next());
					if (key.startsWith("file_spec") && key.length() > "file_spec".length()) {
						rem.add(key);
						String value = sosString.parseToString(p.get(key));
						if (value.indexOf("::") > -1) {
							value = value.substring(value.indexOf("::") + 2);
							rem.add(value);
						}
					}
				}
				for (int i = 0; i < rem.size(); i++) {

					p.remove(rem.get(i));
				}

				p.put("operation", "remove");
				// merke Parameter für send
				p.put("file_path", transActionalRemoteFiles);
				p.put("skip_transfer", "yes");
				p.put("remove_files", "yes");
				p.put("transactional", "no");
				listOfErrorTransfer.add(p);
			}

		}
	}

	/**
	 * Erst bei Erfolgreichen transferieren aller Dateien, wird der atomic suffix umbennant
	 * Bedingung: Parameter transActional = yes
	 * @throws Exception
	 */
	public void getRenameAtomicSuffixTransferFiles() throws Exception {

		try {
			getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());

			if (transActional) {

				if (sosString.parseToString(transActionalRemoteFiles).length() == 0)
					return;

				getLogger().debug(".. mark transactional files for renaming " + transActionalRemoteFiles);

				Properties p = (Properties) arguments.clone();
				ArrayList rem = new ArrayList();
				Iterator it = p.keySet().iterator();

				while (it.hasNext()) {
					String key = sosString.parseToString(it.next());
					if (sosString.parseToString(p.get(key)).indexOf("::") > -1) {
						rem.add(key);
					}
				}
				for (int i = 0; i < rem.size(); i++)
					p.remove(rem.get(i));

				if (flgJumpTransferDefined) {
					String com = getCommands()[0];

					String split[] = com.split(" -");
					String newarg = split[0];
					for (int i = 1; i < split.length; i++) {

						if (split[i].indexOf("skip_transfer") == -1 && split[i].indexOf("replacement") == -1 && split[i].indexOf("file_path") == -1
								&& split[i].indexOf("operation") == -1 && split[i].indexOf("atomic_suffix") == -1 && split[i].indexOf("remove_files") == -1
								&& split[i].indexOf("transactional") == -1)
							newarg = newarg + " -" + split[i];

					}

					newarg = newarg + " " + transActionalRemoteFiles.replaceAll(";", " ") + " -replacement=\"" + atomicSuffix + "\""
							+ " -skip_transfer=\"yes\" -replacing=\"\" -transactional=\"false\" -operation=\"send\""
							+ " -atomic_suffix=\"\" -remove_files=\"no\"";

					// p.put("atomic_suffix", atomicSuffix+ atomicSuffix);

					p.put("command", newarg);
					p.put("operation", "execute");
					p.put("xx_make_temp_directory_success_transfer_xx", "ok");

				}
				else {
					p.put("file_path", transActionalRemoteFiles);
					p.put("replacement", atomicSuffix);

					p.put("atomic_suffix", "");
					p.put("replacing", "");
					p.put("skip_transfer", "yes");
					p.put("remove_files", "no");
					p.put("operation", "send");
					p.put("transactional", "false");

				}
				listOfSuccessTransfer.add(p);

			}
		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
		}
	}

	public void renameAtomicSuffixTransferFiles(final Vector filelist) throws Exception {

		try {
			getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());

			Iterator iterator = filelist.iterator();

			while (iterator.hasNext()) {
				String transferFilename = sosString.parseToString(iterator.next());
				String rTransferFilename = transferFilename.length() > replacement.length() ? transferFilename.substring(0, transferFilename.length()
						- replacement.length()) : "";
				if (transferFilename.trim().length() > 0) {
					transferFilename = new File(transferFilename).getName();
					rTransferFilename = new File(rTransferFilename).getName();

					Vector _ftpFiles = ftpClient.nList(".");
					if (!_ftpFiles.isEmpty() && _ftpFiles.contains(rTransferFilename)) {
						getLogger().debug9("delete existing file: " + ftpClient.delete(rTransferFilename));
					}

					if (ftpClient.rename(transferFilename, rTransferFilename)) {
						if (ftpClient instanceof SOSFTP) {
							if (((SOSFTP) ftpClient).getReplyCode() > ERROR_CODE) {
								RaiseException("..error occurred renaming tranactional file [" + transferFilename + "]: " + ftpClient.getReplyString());
							}
							else {
								this.getLogger().debug(
										"..ftp server reply [rename] " + transferFilename + " in " + rTransferFilename + ": " + ftpClient.getReplyString());
							}
						}
					}
					else {
						RaiseException("..error occurred renaming tranactional file [" + transferFilename + "]: " + ftpClient.getReplyString());
					}
				}

			}
		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);

		}
	}

	private boolean polling(final String fileName) throws Exception {

		String lastmd5file = "";
		String message = "";
		try {
			if (pollTimeout > 0 && sosString.parseToString(fileName).length() > 0) {
				double delay = pollIntervall;
				double nrOfTries = pollTimeout * 60 / delay;
				int tries = 0;
				boolean found = true;
				getLogger().info("polling for file: " + fileName);
				lastmd5file = sos.util.SOSCrypt.MD5encrypt(new File(fileName));
				Thread.sleep((long) delay * 1000);
				for (int i = 0; i < nrOfTries; i++) {
					tries++;
					String newMD5File = sos.util.SOSCrypt.MD5encrypt(new File(fileName));

					getLogger().debug3(i + " polling and checking md5 hash: " + newMD5File);
					if (!lastmd5file.equals(newMD5File)) {
						lastmd5file = newMD5File;
						getLogger().info("polling for files ..." + fileName);

						Thread.sleep((long) delay * 1000);
						if (i + 1 == nrOfTries) {
							found = false;
							message = message + " " + fileName;
						}
					}
					else {
						break;
					}
				}

				if (!found) {
					message = "During triggering for " + pollTimeout + " minutes the file " + message + " has been changed repeatedly";
					if (forceFiles) {
						getLogger().warn(message);

						RaiseException(message);
					}
					else {
						getLogger().info(message);
						return false;
					}
				}

			}
		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " error while polling, cause: " + e);
		}
		return true;
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

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	private boolean sendSimpleTransfer() throws Exception {
		boolean cd = true;

		String localDir = sosString.parseToString(arguments.get("local_dir"));
		// SOSFileTransfer ftpClient = null;
		try {
			if (sosString.parseToBoolean(sosString.parseToString(arguments.get(conParameterMAKE_DIRS)))) {
				if (ftpClient.changeWorkingDirectory(remoteDir)) { // error code signals non-existing directory
					this.getLogger().debug("..ftp server reply [directory exists] [" + remoteDir + "]: " + ftpClient.getReplyString());
					cd = true;
				}
				else {
					boolean ok = ftpClient.mkdir(remoteDir, intPosixPermissions);
					if (!ok) {
						RaiseException("..error occurred creating directory [" + remoteDir + "]: " + ftpClient.getReplyString());
					}
					else {
						this.getLogger().debug("..ftp server reply [mkdir] [" + remoteDir + "]: " + ftpClient.getReplyString());
						cd = ftpClient.changeWorkingDirectory(remoteDir);
					}
				}
			}
			else
				if (remoteDir != null && remoteDir.length() > 0) {
					cd = ftpClient.changeWorkingDirectory(remoteDir);
				}
			String alternativeRemoteDir = sosString.parseToString(arguments.get("alternative_remote_dir"));
			if (!cd && sosString.parseToString(alternativeRemoteDir).length() > 0) {// alternative Parameter
				this.getLogger().debug("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
				this.getLogger().info("..try with alternative parameter [remoteDir=" + alternativeRemoteDir + "]");
				cd = ftpClient.changeWorkingDirectory(alternativeRemoteDir);
				remoteDir = alternativeRemoteDir;
			}

			if (!cd) {
				RaiseException("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
			}
			else {
				this.getLogger().debug("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
			}

			// einfaches transferieren einer Datei. Hier finden auch keine Überprüfung statt
			getLogger().debug9("..filepath: " + filePath);
			if (sosString.parseToString(remoteDir).length() > 0 && sosString.parseToString(remoteDir).equals("./")) {
				remoteDir = "";
			}

			String localFile = new File(localDir, filePath).getPath();
			String targetFile = new File(sosString.parseToString(remoteDir), new File(filePath).getName()).getPath().replaceAll(conRegExpBackslash, "/");

			if (!new File(localFile).exists()) {
				RaiseException(".. file [" + localFile + "] does not exist ");
			}

			long bytesSend = ftpClient.putFile(localFile, targetFile);
			this.getLogger().debug("..ftp server reply [putFile] [" + targetFile + ", size=" + bytesSend + "]: " + ftpClient.getReplyString());
			this.getLogger().debug("1 file send " + localFile + " to " + targetFile);
			this.getLogger().debug("sending file : " + localFile + " to " + targetFile);
			getLogger().debug("1 file send " + localFile + " to " + targetFile);
			this.getLogger().info("sending file : " + localFile);
			this.getLogger().info("file_size    : " + bytesSend + " bytes");
			// transferFileList.add(filePath);
			filelist = new Vector();
			filelist.add(filePath);
			setParam("successful_transfers", "1");
		}
		catch (Exception e) {
			setParam("failed_transfers", "1");
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " error while transfer simple File, cause: " + e);
		}
		return true;
	}

	private boolean isSimpleTransfer() throws Exception {
		boolean simpleTransfer = sosString.parseToBoolean(sosString.parseToString(arguments.get("simple_transfer")));

		if (simpleTransfer && sosString.parseToString(arguments.get("file_path")).length() == 0) {
			RaiseException("job parameter is missing for specified parameter [ftp_simple_transfer]: [ftp_file_path]");
		}

		if (simpleTransfer) {
			getLogger().info(
					"parameter [ftp_file_spec, replacement, replacing, ftp_force_files, ftp_atomic_suffix, ftp_recursive, ftp_compress, ftp_remove_files, ftp_make_dirs] "
							+ "will be ignored because parameter ftp_simple_transfer has been specified. ");
		}
		return simpleTransfer;
	}
}
