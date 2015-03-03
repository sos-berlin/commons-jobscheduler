package com.sos.VirtualFileSystem.common;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NMsg;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsMessageCodes extends JSToolBox {

	@SuppressWarnings("unused")
	private static final String			conSVNVersion		= "$Id$";

	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_122		= new SOSMsgVfs("SOSVfs_D_122");								// "using '%1$s' mode for file transfer";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_123		= new SOSMsgVfs("SOSVfs_D_123");								// "ftp server reply ['%1$s']: '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_124		= new SOSMsgVfs("SOSVfs_E_124");								// "..error occurred 'NegativeCommandCompletion' on the FTP server: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_125		= new SOSMsgVfs("SOSVfs_D_125");								// "Disconnected from host '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_126		= new SOSMsgVfs("SOSVfs_I_126");								// "strCurrentDirectory = '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_127		= new SOSMsgVfs("SOSVfs_D_127");								// "Try cd with ''%1$s''.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_128		= new SOSMsgVfs("SOSVfs_E_128");								// "%1$s in %2$s returns an exception";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_130		= new SOSMsgVfs("SOSVfs_E_130");								// "'%1$s' returns an exception";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_131		= new SOSMsgVfs("SOSVfs_I_131");								// "File deleted : '%1$s', reply is '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_132		= new SOSMsgVfs("SOSVfs_D_132");								// "Try to login with user '%1$s' and password.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_133		= new SOSMsgVfs("SOSVfs_D_133");								// "user '%1$s' logged in.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_134		= new SOSMsgVfs("SOSVfs_E_134");								// "'%1$s' failed";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_135		= new SOSMsgVfs("SOSVfs_D_135");								// "..ftp server reply '%3$s' ['%1$s']: %2$s.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_136		= new SOSMsgVfs("SOSVfs_E_136");								// "'%1$s' reports exception";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_W_136		= new SOSMsgVfs("SOSVfs_W_136");								// "Problems during disconnect. ";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_137		= new SOSMsgVfs("SOSVfs_D_137");								// "reply from FTP-Server is '%1$s', code = '%2$d'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_138		= new SOSMsgVfs("SOSVfs_D_138");								// "logout from host '%1$s', reply '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_139		= new SOSMsgVfs("SOSVfs_I_139");								// "not connected, logout useless.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_W_140		= new SOSMsgVfs("SOSVfs_W_140");								// "problems during logout. ";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_141		= new SOSMsgVfs("SOSVfs_D_141");								// "Try pwd.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_142		= new SOSMsgVfs("SOSVfs_E_142");								// "retrieveFile returns a negative return-code";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_143		= new SOSMsgVfs("SOSVfs_E_143");								// "Could not open stream for '%1$s'. Perhaps the file does not exist. Reply from ftp server: %2$s.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_144		= new SOSMsgVfs("SOSVfs_E_144");								// "..error occurred in getFile() on the FTP server for file ['%1$s']: %3$s"
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_146		= new SOSMsgVfs("SOSVfs_D_146");								// "file '%1$s' transfered to '%2$s'.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_147		= new SOSMsgVfs("SOSVfs_E_147");								// "OutputStream null value.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_148		= new SOSMsgVfs("SOSVfs_E_148");								// "completePendingCommand() returns false";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_149		= new SOSMsgVfs("SOSVfs_E_149");								// "setFileType not possible, due to : %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_150		= new SOSMsgVfs("SOSVfs_I_150");								// "rename file '%1$s' to '%2$s'.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_151		= new SOSMsgVfs("SOSVfs_D_151");								// "..ftp server reply 'sendCommand' ['%1$s']: %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_152		= new SOSMsgVfs("SOSVfs_D_152");								// "'%2$s': getFileHandle for '%1$s' ";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_153		= new SOSMsgVfs("SOSVfs_E_153");								// "Problem with '%1$s'.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_154		= new SOSMsgVfs("SOSVfs_E_154");								// "%1$s returns a negative returncode.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_155		= new SOSMsgVfs("SOSVfs_I_155");								// "bytes appended : %1$s.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_156		= new SOSMsgVfs("SOSVfs_D_156");								// "SourceFileName = %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_157		= new SOSMsgVfs("SOSVfs_D_157");								// "flgResult = %1$s for File %2$s.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_158		= new SOSMsgVfs("SOSVfs_E_158");								// "%1$s failed in %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0121		= new SOSMsgVfs("SOSVfs_I_0121");								//
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_159		= new SOSMsgVfs("SOSVfs_D_159");								// "Path adjusted from '%1$s' to '%2$s'.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_160		= new SOSMsgVfs("SOSVfs_I_160");								// "%1$s not possible and not implemented.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_170		= new SOSMsgVfs("SOSVfs_I_170");								// "try alternate host due to connection-error. host: '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_171		= new SOSMsgVfs("SOSVfs_D_171");								// "%1$s: currDir = %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_172		= new SOSMsgVfs("SOSVfs_D_172");								// "%1$s: SourceFileName = %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_173		= new SOSMsgVfs("SOSVfs_E_173");								// "%1$s failed for file %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_174		= new SOSMsgVfs("SOSVfs_E_174");								// "%1$s failed for file %2$s. offset = %3$d, blklength = %4$d";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_175		= new SOSMsgVfs("SOSVfs_E_175");								// "...error occurred during transfer on the data-target for file ['%1$s']: %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_176		= new SOSMsgVfs("SOSVfs_E_176");								// "%1$s failed, can not get OutputStream for file %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_177		= new SOSMsgVfs("SOSVfs_E_177");								// "InputStream is null.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_178		= new SOSMsgVfs("SOSVfs_E_178");								// "authentication failed.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_179		= new SOSMsgVfs("SOSVfs_D_179");								// "..try ['%1$s'] ['%2$s']";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_180		= new SOSMsgVfs("SOSVfs_E_180");								// "directory ['%1$s'] already exists";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_181		= new SOSMsgVfs("SOSVfs_D_181");								// "...sftp server reply ['%1$s'] ['%2$s']: '%3$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_182		= new SOSMsgVfs("SOSVfs_I_182");								// "...sftp server reply ['%1$s'] remote ['%2$s'"]
																															// local
																															// ['%3$s']:
																															// '%4$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_183		= new SOSMsgVfs("SOSVfs_I_183");								// "..sftp server reply ['%1$s'] local ['%2$s'] remote ['%3$s']: '%4$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_184		= new SOSMsgVfs("SOSVfs_E_184");								// ['%1$s']
																															// remote
																															// ['%2$s']
																															// local
																															// ['%3$s']
																															// failed;
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_185		= new SOSMsgVfs("SOSVfs_E_185");								// "['%1$s'] local ['%2$s'] remote ['%3$s'] failed";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_186		= new SOSMsgVfs("SOSVfs_E_186");								// "'%1$s' is a directory.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_187		= new SOSMsgVfs("SOSVfs_E_187");								// "['%1$s'] file ['%2$s'] failed"
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_188		= new SOSMsgVfs("SOSVfs_E_188");								// "['%1$s']  '%2$s' to '%3$s' failed";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_189		= new SOSMsgVfs("SOSVfs_I_189");								// "..sftp server reply [rename] from ['%1$s'] to ['%2$s'] : %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_190		= new SOSMsgVfs("SOSVfs_E_190");								// "'%1$s' is not initialized (NULL)";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_191		= new SOSMsgVfs("SOSVfs_E_191");								// "remote command terminated with exit code: '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_192		= new SOSMsgVfs("SOSVfs_I_192");								// "...sftp server reply [ExecuteCommand] '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_193		= new SOSMsgVfs("SOSVfs_E_193");								// "'%1$s' failed ['%2$s']";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_194		= new SOSMsgVfs("SOSVfs_D_194");								// "...cwd ['%1$s']: '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_196		= new SOSMsgVfs("SOSVfs_D_196");								// "getFileHandle for %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_195		= new SOSMsgVfs("SOSVfs_D_195");								// "pwd = %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_196		= new SOSMsgVfs("SOSVfs_E_196");								// "ChannelSftp can't be created.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_207		= new SOSMsgVfs("SOSVfs_D_207");								// "InputStream for '%1$s' created.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_204		= new SOSMsgVfs("SOSVfs_E_204");								// "Connection not possible";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_161		= new SOSMsgVfs("SOSVfs_E_161");								// "SOSVfs_E_161 = Error occured '%1$s': '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_162		= new SOSMsgVfs("SOSVfs_E_162");								// "remote file size ['%1$s'] and local file size ['%2$s'] are different. Number of bytes written to local file: '%3$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_163		= new SOSMsgVfs("SOSVfs_D_163");								// "output to '%1$s' for remote command: '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_164		= new SOSMsgVfs("SOSVfs_E_164");								// "SOS-SSH-E-150: remote command terminated with exit code: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_165		= new SOSMsgVfs("SOSVfs_D_165");								// "Auth_Method is %1$s/%2$s...";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_166		= new SOSMsgVfs("SOSVfs_E_166");								// "Unknown authentication method: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_167		= new SOSMsgVfs("SOSVfs_E_167");								// "Authentication failed. auth_method = '%1$s', auth_file = '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_168		= new SOSMsgVfs("SOSVfs_E_168");								// "Authentication problem.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_169		= new SOSMsgVfs("SOSVfs_D_169");								// "Trying to connect...";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_197		= new SOSMsgVfs("SOSVfs_E_197");								// "can not substitute parameters in: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_198		= new SOSMsgVfs("SOSVfs_E_198");								// "SOSVfs-E-0000: error in createURI(): %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_199		= new SOSMsgVfs("SOSVfs_D_199");								// "DataSourceType = %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_200		= new SOSMsgVfs("SOSVfs_D_200");								// "Zip-File '%1$s' created and opened for '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_201		= new SOSMsgVfs("SOSVfs_D_201");								// "Zip-Entry '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_202		= new SOSMsgVfs("SOSVfs_E_202");								// "No Zip Archive open for output";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_203		= new SOSMsgVfs("SOSVfs_D_203");								// "put file '%1$s' to zip-file %2$s, number of Bytes: %3$d";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_204		= new SOSMsgVfs("SOSVfs_D_204");								// "Zip-File '%1$s' closed.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_205		= new SOSMsgVfs("SOSVfs_E_205");								// "Exception occured during close for '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_206		= new SOSMsgVfs("SOSVfs_D_206");								// "Zip Archive '%1$s' closed.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0109		= new SOSMsgVfs("SOSVfs_I_0109");
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0107		= new SOSMsgVfs("SOSVfs_E_0107");
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_0101		= new SOSMsgVfs("SOSVfs-D-0101");
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_0102		= new SOSMsgVfs("SOSVfs_D_0102");
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_0103		= new SOSMsgVfs("SOSVfs_D_0103");
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0106		= new SOSMsgVfs("SOSVfs_E_0106");
	@I18NMsg
	protected static final SOSMsgVfs	SOSVfs_E_0105		= new SOSMsgVfs("SOSVfs_E_0105");
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_F_102		= new SOSMsgVfs("SOSVfs_F_102");
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_208		= new SOSMsgVfs("SOSVfs_D_208");								// "start erasing local files due to 'remove_files=true'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_209		= new SOSMsgVfs("SOSVfs_D_209");								// "start renaming of atomic files...";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_210		= new SOSMsgVfs("SOSVfs_E_210");								// "error in  '%1$s', reason: '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_211		= new SOSMsgVfs("SOSVfs_I_211");								// "Rollback initiated.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_212		= new SOSMsgVfs("SOSVfs_D_212");								// "SetBack. File %1$s deleted.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_213		= new SOSMsgVfs("SOSVfs_D_213");								// "Transfer-operation '%4$s' started at %1$s, ended at %2$s. Duration: %3$d";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_0110		= new SOSMsgVfs("SOSVfs_D_0110");								// "transfer skipped for file %1$s ";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_0111		= new SOSMsgVfs("SOSVfs_D_0111");								// "transfer skipped for file %1$s due to overwrite constraint";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_0112		= new SOSMsgVfs("SOSVfs_D_0112");								// "number of bytes transferred: %1$d ";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0113		= new SOSMsgVfs("SOSVfs_I_0113");								// "Source-File %1$s deleted";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0108		= new SOSMsgVfs("SOSVfs_I_0108");								// "Transfer of %1$s started";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_0151		= new SOSMsgVfs("SOSVfs_D_0151");								// "Execute commands: '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0120		= new SOSMsgVfs("SOSVfs_I_0120");								// "%1$s for file %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0119		= new SOSMsgVfs("SOSVfs_I_0119");								// "transfer aborted";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0118		= new SOSMsgVfs("SOSVfs_I_0118");								// "processing normal end";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0114		= new SOSMsgVfs("SOSVfs_I_0114");								// "processing in progress";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0115		= new SOSMsgVfs("SOSVfs_I_0115");								// "waiting for processing";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0116		= new SOSMsgVfs("SOSVfs_I_0116");								// "processing skipped";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_0117		= new SOSMsgVfs("SOSVfs_I_0117");								// "processing started";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0150		= new SOSMsgVfs("SOSVfs_E_0150");								// "getreplacing().doReplace aborted with an exception";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_214		= new SOSMsgVfs("SOSVfs_D_214");								// "Operation = %4$s, TargetFile = %1$s, SourceFile = %2$s, BytesTransferred = %3$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_215		= new SOSMsgVfs("SOSVfs_D_215");								// "TargetFile = %1$s, SourceFile = %2$s, BytesTransferred = %3$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_216		= new SOSMsgVfs("SOSVfs_E_216");								// "File-Size failure for file '%3$s'. No of bytes transferred '%1$d', size of file is '%2$d'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_217		= new SOSMsgVfs("SOSVfs_D_217");								// "%1$s(%2$s)";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_218		= new SOSMsgVfs("SOSVfs_D_218");								// "SourceFileName = %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_219		= new SOSMsgVfs("SOSVfs_D_219");								// "SourceTransferFileName = %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_220		= new SOSMsgVfs("SOSVfs_D_220");								// "TargetTransferFileName = %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_221		= new SOSMsgVfs("SOSVfs_D_221");								// "TargetFileName = %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_222		= new SOSMsgVfs("SOSVfs_I_222");								// "polling for file: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_223		= new SOSMsgVfs("SOSVfs_D_223");								// "%1$s polling and checking md5 hash: %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_224		= new SOSMsgVfs("SOSVfs_E_224");								// "During triggering for %1$s minutes the file %2$s has been changed repeatedly";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_225		= new SOSMsgVfs("SOSVfs_E_225");								// "Error in %1$s. Error while polling, reason: %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_226		= new SOSMsgVfs("SOSVfs_E_226");								// ".. file '%1$s' does not exist.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_227		= new SOSMsgVfs("SOSVfs_D_227");								// ".. creating sub-directory on data-target: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_228		= new SOSMsgVfs("SOSVfs_E_228");								// "Overwrite contention: data-target(-server) replied: filename exists '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_229		= new SOSMsgVfs("SOSVfs_E_229");								// "error. unable to transfer data, reason: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_0201		= new SOSMsgVfs("SOSVfs_D_0201");								// "%1$s returns instance of %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0203		= new SOSMsgVfs("SOSVfs_E_0203");								// "undefined Virtual File System requested: %1$s"
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_W_0070		= new SOSMsgVfs("SOSVfs_W_0070");								// "possible wrong variable-value specified in '%1$s' of key '%2$s'. not substituted."
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0060		= new SOSMsgVfs("SOSVfs_E_0060");								// "Profile/Section '%1$s' not found or is empty in file '%2$s'"
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0050		= new SOSMsgVfs("SOSVfs_E_0050");								// "usage of option '%1$s' is in conflict with option(s) '%2$s'."
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0040		= new SOSMsgVfs("SOSVfs_E_0040");								// "option(s) '%1$s' not supported by the requested protocol '%2$s'."
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0030		= new SOSMsgVfs("SOSVfs_E_0030");								// "unsupported parameter settings '%1$s': only one of this parameters can be used at the same time."
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0000		= new SOSMsgVfs("SOSVfs-E-0000");								// "Section with name '%1$s' for include not found or is empty in file '%2$s'"
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0010		= new SOSMsgVfs("SOSVfs-E-0010");								// "local directory does not exist or is not accessible: '%1$s'."
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_0020		= new SOSMsgVfs("SOSVfs_E_0020");								// "usage of parameter '%1$s' requires one or more additional parameters, which are missing: '%2$s'."
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_230		= new SOSMsgVfs("SOSVfs_D_230");								// "strComSpec: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_231		= new SOSMsgVfs("SOSVfs_D_231");								// "CC from executeCommand: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_232		= new SOSMsgVfs("SOSVfs_D_232");								// "%1$s closed.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_233		= new SOSMsgVfs("SOSVfs_I_233");								// "commandScript: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_234		= new SOSMsgVfs("SOSVfs_E_234");								// "%1$s: Instance for Options is null but mandatory.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_235		= new SOSMsgVfs("SOSVfs_E_235");								// "%1$s: authentication failed %2$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_236		= new SOSMsgVfs("SOSVfs_D_236");								// "Opening new session...";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_237		= new SOSMsgVfs("SOSVfs_D_237");								// "Remote shell is a Windows shell.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_238		= new SOSMsgVfs("SOSVfs_D_238");								// "Remote shell is a Linux/Unix shell.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_239		= new SOSMsgVfs("SOSVfs_D_239");								// "Failed to check if remote system is windows shell: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_240		= new SOSMsgVfs("SOSVfs_D_240");								// "Failed to close session: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_241		= new SOSMsgVfs("SOSVfs_E_241");								// "Error code when checking file existence: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_242		= new SOSMsgVfs("SOSVfs_D_242");								// "File with that name already exists, trying another name...";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_243		= new SOSMsgVfs("SOSVfs_D_243");								// "canonical path: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_244		= new SOSMsgVfs("SOSVfs_E_244");								// "Failed to delete remote command script: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_245		= new SOSMsgVfs("SOSVfs_E_245");								// "No shell-options specified but mandatory.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_246		= new SOSMsgVfs("SOSVfs_D_246");								// "Requesting %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_247		= new SOSMsgVfs("SOSVfs_D_247");								// "Starting %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_248		= new SOSMsgVfs("SOSVfs_D_248");								// "Waiting for login prompt...";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_249		= new SOSMsgVfs("SOSVfs_D_249");								// "Found login prompt %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_250		= new SOSMsgVfs("SOSVfs_I_250");								// "Could not retrieve '%1$s', possibly not supported by remote ssh server";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_251		= new SOSMsgVfs("SOSVfs_D_251");								// "replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_252		= new SOSMsgVfs("SOSVfs_D_252");								// "objJSJobUtilities = %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_253		= new SOSMsgVfs("SOSVfs_I_253");								// "Temporary Script file created: '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_254		= new SOSMsgVfs("SOSVfs_D_254");								// "File '%1$s' marked for deletion.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_255		= new SOSMsgVfs("SOSVfs_D_255");								// "Replacing task and order parameters...";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_256		= new SOSMsgVfs("SOSVfs_D_256");								// "Replacing parameter '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_257		= new SOSMsgVfs("SOSVfs_E_257");								// "Authentication file does not exist: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_258		= new SOSMsgVfs("SOSVfs_E_258");								// "Authentication file not accessible: %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_259		= new SOSMsgVfs("SOSVfs_D_259");								// "csv created: '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_260		= new SOSMsgVfs("SOSVfs_E_260");								// "error in %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_261		= new SOSMsgVfs("SOSVfs_E_261");								// "%1$s: No export detail data are set";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_262		= new SOSMsgVfs("SOSVfs_D_262");								// "DataTargetType = %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_263		= new SOSMsgVfs("SOSVfs_I_263");								// "Value of '%1$s' overwritten with '%2$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_264		= new SOSMsgVfs("SOSVfs_I_264");								// "Start compress file";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_265		= new SOSMsgVfs("SOSVfs_E_265");								// "Can't get InputStream";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_266		= new SOSMsgVfs("SOSVfs_E_266");								// "Transfer aborted with an exception";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_T_267		= new SOSMsgVfs("SOSVfs_T_267");								// "Set parameter for prefix '%1$s'";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_T_268		= new SOSMsgVfs("SOSVfs_T_268");								// "source options: \n %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_T_269		= new SOSMsgVfs("SOSVfs_T_269");								// "target options: \n %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_270		= new SOSMsgVfs("SOSVfs_E_270");								// "FileChanged";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_271		= new SOSMsgVfs("SOSVfs_E_271");								// "FileSize";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_D_272		= new SOSMsgVfs("SOSVfs_D_272");								// "Thread is starting...";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_273		= new SOSMsgVfs("SOSVfs_E_273");								// "virtual %1$s File has null value.";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_I_274		= new SOSMsgVfs("SOSVfs_I_274");								// "Security hash (%3$s) for file %2$s is %1$s";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_Title_276	= new SOSMsgVfs("SOSVfs_Title_276");							// "Test for UDP communication method";
	@I18NMsg
	public static final SOSMsgVfs		SOSVfs_E_277		= new SOSMsgVfs("SOSVfs_E_277");								// "a file '%1$s' already exists.";
  @I18NMsg
  public static final SOSMsgVfs   SOSVfs_D_280    = new SOSMsgVfs("SOSVfs_D_280");               // "no return values received!"
  @I18NMsg
  public static final SOSMsgVfs   SOSVfs_D_281    = new SOSMsgVfs("SOSVfs_D_281");               // "no temporary file with return values found to process."
  @I18NMsg
  public static final SOSMsgVfs   SOSVfs_D_282    = new SOSMsgVfs("SOSVfs_D_282");               // "no temporary file for return values found!"
  @I18NMsg
  public static final SOSMsgVfs   SOSVfs_E_283    = new SOSMsgVfs("SOSVfs_E_283");               // "Error occured while opening a Session for postCommands: %1$s"
  @I18NMsg
  public static final SOSMsgVfs   SOSVfs_D_284    = new SOSMsgVfs("SOSVfs_D_284");               // "received Return Values are: "
	
	
	@Deprecated
	public SOSVfsMessageCodes() {
		super("SOSVirtualFileSystem");
	}

	@Deprecated
	public SOSVfsMessageCodes(final String pstrBundleBaseName) {
		super(pstrBundleBaseName);
	}

}