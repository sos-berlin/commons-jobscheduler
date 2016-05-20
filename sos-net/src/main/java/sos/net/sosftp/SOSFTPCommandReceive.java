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

    private static final String conTextFILES_REMOVE = " files removed.";
    private static int count = 0;
    private static String state = "";
    private final String defaultRemoteDir = "./";
    private final HashMap<File, File> checkFileList = new HashMap<File, File>();
    private String transferMode = "binary";
    private String remoteDir = "";
    private String localDir = ".";
    private String fileSpec = conRegExpAllChars;
    private String atomicSuffix = "";
    private String fileNotificationTo = "";
    private String fileNotificationCC = "";
    private String fileNotificationBCC = "";
    private String fileNotificationSubject = "";
    private String fileNotificationBody = "";
    private String fileZeroByteNotificationTo = "";
    private String fileZeroByteNotificationCC = "";
    private String fileZeroByteNotificationBCC = "";
    private String fileZeroByteNotificationSubject = "";
    private String fileZeroByteNotificationBody = "";
    private String pollFilesErrorState = "";
    private String filePath = "";
    private String alternativeHost = "";
    private String alternativeUser = "";
    private String alternativePassword = "";
    private String alternativeAccount = "";
    private String alternativeRemoteDir = "";
    private String alternativeTransferMode = "binary";
    private String hlocalFile = "";
    private String htargetFile = "";
    private String transActionalRemoteFiles = null;
    private String strFilesRemainingOnSource = null;
    private boolean alternativePassiveMode = false;
    private boolean isFilePath = false;
    private boolean checkSize = true;
    private boolean passiveMode = false;
    private boolean overwriteFiles = true;
    private boolean appendFiles = false;
    private boolean removeFiles = false;
    private boolean skipTransfer = false;
    private boolean forceFiles = true;
    private boolean zeroByteFiles = true;
    private boolean zeroByteFilesStrict = false;
    private boolean zeroByteFilesRelaxed = false;
    private boolean recursive = false;
    private boolean testmode = false;
    private boolean simpleTransfer = false;
    private long checkInterval = 60;
    private long checkRetry = 0;
    private int pollTimeout = 0;
    private int pollIntervall = 60;
    private int pollMinFiles = 1;
    private int alternativePort = 0;
    private int successful_transfers = 0;
    private int failed_transfer = 0;
    private int zeroByteCount = 0;
    private Pattern pattern = null;
    private ArrayList<String> transActionalLocalFiles = null;
    public static final String conTransferModeASCII = "ascii";
    public static final String conReturnValueFAILED_TRANSFERS = "failed_transfers";
    public static final String conReturnValueSUCCESSFUL_TRANSFERS = "successful_transfers";
    public static final String FTP_SERVER_REPLY = "..ftp server reply [";
    public static final String conClosingBracketWithColon = "]: ";
    public static final String FTP_SERVER_REPLY_CD_REMOTE_DIR = "..ftp server reply [cd] [remoteDir=";
    boolean rc = false;
    boolean sshBasedProtocol = false;
    String protocol = "ftp";

    public SOSFTPCommandReceive(final SOSLogger logger, final Properties arguments_) throws Exception {
        super(logger, arguments_);
    }

    public SOSFTPCommandReceive(final sos.configuration.SOSConfiguration sosConfiguration_, final SOSLogger logger) throws Exception {
        super(sosConfiguration_, logger);
    }

    @Override
    public boolean receive() throws Exception {
        getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
        try {
            arguments.put(conReturnValueSUCCESSFUL_TRANSFERS, "0");
            arguments.put(conReturnValueFAILED_TRANSFERS, "0");
            if (this.getLogger() == null) {
                this.setLogger(new SOSStandardLogger(0));
            }
            readSettings(true);
            if (!doPreCommands()) {
                writeHistory(hlocalFile, htargetFile);
                return false;
            }
            try {
                getParameter();
                getFileNotificationParameter();
            } catch (Exception e) {
                rc = false;
                throw new RuntimeException("could not process job parameters: " + e.getMessage(), e);
            }
            if (skipTransfer && flgJumpTransferDefined) {
                return true;
            }
            simpleTransfer = isSimpleTransfer();
            if (simpleTransfer) {
                return receiveSimpleTransfer();
            }
            printParameter();
            try {
                if (host == null || host.isEmpty()) {
                    raiseException("no host was specified");
                }
                if (user == null || user.isEmpty()) {
                    raiseException("no user was specified");
                }
            } catch (Exception e) {
                rc = false;
                throw new RuntimeException("invalid or insufficient parameters: " + e.getMessage(), e);
            }
            try {
                if (localDir.startsWith(conRegExpBackslash)) {
                    while (localDir.indexOf("\\") != -1) {
                        localDir = localDir.replace('\\', '/');
                    }
                }
                if (localDir.startsWith("file://") && !new File(createURI(localDir)).exists()) {
                    raiseException("local directory does not exist or is not accessible: " + localDir);
                }
                this.getLogger().debug1(
                        "connecting to host " + host + ", port " + port + ", local directory " + localDir + ", remote directory " + remoteDir
                                + (isFilePath ? ", file " + filePath : ", file specification " + fileSpec));
                boolean alternativeUse = true;
                int isAlternativeParameterUse = 0;
                while (alternativeUse && isAlternativeParameterUse <= 1) {
                    try {
                        initFTPServer();
                        alternativeUse = false;
                    } catch (Exception ex) {
                        if (isAlternativeParameterUse <= 0 && alternativeUse) {
                            this.getLogger().reset();
                        }
                        this.getLogger().debug1("..error in ftp server init with [host=" + host + "], [port=" + port + "] " + ex.getMessage());
                        alternativeUse =
                                !alternativeHost.concat(alternativeUser).concat(alternativePassword).concat(alternativeAccount).isEmpty()
                                        || alternativePort != 0;
                        if (alternativeUse && isAlternativeParameterUse == 0) {
                            getLogger().reset();
                            if (ftpClient != null) {
                                if (isLoggedIn) {
                                    try {
                                        ftpClient.logout();
                                    } catch (Exception e) {
                                        // no error handling
                                    }
                                }
                                if (ftpClient.isConnected()) {
                                    try {
                                        ftpClient.disconnect();
                                    } catch (Exception e) {
                                        // no error handling
                                    }
                                }
                            }
                            isAlternativeParameterUse++;
                            setAlternativeParameter();
                            arguments.put("user", user);
                            this.getLogger().debug1(
                                    "..try login with alternative parameter [host=" + host + "], [port=" + port + "] " + "[user=" + user
                                            + "], [account=" + account + "], [remoteDir=" + remoteDir + "], [passiveMode=" + passiveMode + "], "
                                            + "[transferMode=" + transferMode + "]");
                        } else {
                            raiseException("..error in ftp server init with [host=" + host + "], [port=" + port + "] " + ex.getMessage());
                        }
                    }
                }
                if (!isLoggedIn) {
                    raiseException(".. server reply [login failed] [user=" + user + "], [account=" + account + "]: " + ftpClient.getReplyString());
                }
                if (ftpClient instanceof SOSFTP) {
                    SOSFTP sosftp = (SOSFTP) ftpClient;
                    if (passiveMode) {
                        sosftp.passive();
                        if (sosftp.getReplyCode() > ERROR_CODE) {
                            raiseException("..ftp server reply [passive]: " + ftpClient.getReplyString());
                        } else {
                            this.getLogger().debug("..ftp server reply [passive]: " + ftpClient.getReplyString());
                        }
                        sosftp.enterLocalPassiveMode();
                    }
                    if (conTransferModeASCII.equalsIgnoreCase(transferMode)) {
                        if (sosftp.ascii()) {
                            this.getLogger().debug("..using ASCII mode for file transfer");
                            this.getLogger().debug("..ftp server reply" + " [ascii]: " + ftpClient.getReplyString());
                        } else {
                            raiseException(".. could not switch to ASCII mode for file transfer ..ftp server reply [ascii]: "
                                    + ftpClient.getReplyString());
                        }
                    } else {
                        if (sosftp.binary()) {
                            this.getLogger().debug("using binary mode for file transfers.");
                            this.getLogger().debug("..ftp server reply" + " [binary]: " + ftpClient.getReplyString());
                        } else {
                            raiseException(".. could not switch to binary mode for file transfer ..ftp server reply [ascii]: "
                                    + ftpClient.getReplyString());
                        }
                    }
                    if (!strPreFtpCommands.isEmpty()) {
                        String[] strA = strPreFtpCommands.split(";");
                        for (String strCmd : strA) {
                            this.getLogger().debug("..try to send [" + strCmd + "] to the Server ");
                            sosftp.sendCommand(strCmd);
                            this.getLogger().debug(FTP_SERVER_REPLY + strCmd + conClosingBracketWithColon + ftpClient.getReplyString());
                        }
                    }
                    if (!strControlEncoding.isEmpty()) {
                        sosftp.setControlEncoding(strControlEncoding);
                        this.getLogger().debug(FTP_SERVER_REPLY + strControlEncoding + conClosingBracketWithColon + ftpClient.getReplyString());
                    }
                }
                if (isFilePath) {
                    String cfilePath = filePath.split(";")[0];
                    String currRemoteDir = "";
                    if (!sosString.parseToString(remoteDir).isEmpty() && !sosString.parseToString(remoteDir).equals(defaultRemoteDir)) {
                        currRemoteDir = remoteDir;
                    } else if (new File(cfilePath).getParent() != null) {
                        currRemoteDir = new File(cfilePath).getParent().replaceAll(conRegExpBackslash, "/");
                    }
                    if (!sosString.parseToString(currRemoteDir).isEmpty()) {
                        if (!ftpClient.changeWorkingDirectory(currRemoteDir)) {
                            raiseException("..ftp server reply" + " [cd] [directory file_path=" + currRemoteDir + conClosingBracketWithColon
                                    + ftpClient.getReplyString());
                        } else {
                            getLogger().debug(
                                    "..ftp server reply" + " [cd] [directory file_path=" + currRemoteDir + conClosingBracketWithColon
                                            + ftpClient.getReplyString());
                        }
                    }
                } else if (!sosString.parseToString(remoteDir).isEmpty()) {
                    if (!ftpClient.changeWorkingDirectory(remoteDir)) {
                        raiseException(FTP_SERVER_REPLY_CD_REMOTE_DIR + remoteDir + conClosingBracketWithColon + ftpClient.getReplyString());
                    } else {
                        getLogger().debug(FTP_SERVER_REPLY_CD_REMOTE_DIR + remoteDir + conClosingBracketWithColon + ftpClient.getReplyString());
                    }
                }
                if (isFilePath) {
                    filelist = new Vector<String>();
                    String[] split = filePath.split(";");
                    for (String element : split) {
                        if (element != null && !element.isEmpty()) {
                            filelist.add(element);
                        }
                    }
                    fileSpec = conRegExpAllChars;
                } else {
                    filelist = ftpClient.nList(recursive);
                    getLogger().debug("..ftp server reply [nlist]: " + ftpClient.getReplyString());
                }
                count = 0;
                zeroByteCount = 0;
                pattern = Pattern.compile(fileSpec, 0);
                if (!polling()) {
                    return false;
                } else if (!isFilePath && pollTimeout > 0) {
                    filelist = ftpClient.nList(recursive);
                }
                int intStep = 1;
                boolean flgError = false;
                try {
                    receiveFiles(filelist);
                    intStep = 2;
                    getRenameAtomicSuffixTransferFiles();
                    intStep = 3;
                    getDeleteTransferFiles();
                } catch (Exception e) {
                    flgError = true;
                    getLogger().warn("could not complete transaction, cause \n" + e.getMessage());
                    throw e;
                } finally {
                    if (flgError) {
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
                        if (checkFile == null || transferFile == null) {
                            raiseException("..error occurred, empty file list is corrupted");
                        }
                        if (transferFileList.isEmpty() || zeroByteFilesRelaxed) {
                            this.getLogger().debug1("removing local temporary file : " + checkFile.getAbsolutePath() + " due to zero byte constraint");
                            if (!checkFile.delete()) {
                                raiseException("could not remove temporary file: " + checkFile.getAbsolutePath());
                            }
                            zeroByteCount++;
                            fileZeroByteNotificationBody += strEncodedFileName + conNewLine;
                        } else {
                            if (atomicSuffix != null && !atomicSuffix.isEmpty() && checkFile.getAbsolutePath().endsWith(atomicSuffix)) {
                                this.getLogger().debug1("renaming local temporary file: " + transferFile.getAbsolutePath());
                                if (!checkFile.renameTo(transferFile)) {
                                    raiseException("could not rename temporary file [" + checkFile.getCanonicalPath() + "] to: "
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
                if (!sosString.parseToString(hlocalFile).isEmpty() && !sosString.parseToString(htargetFile).isEmpty()) {
                    arguments.put(conReturnValueSUCCESSFUL_TRANSFERS, String.valueOf(count));
                }
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
            } catch (Exception e) {
                arguments.put(conSettingSTATUS, conStatusERROR);
                rc = false;
                throw new RuntimeException("could not process file transfer: " + e.getMessage(), e);
            } finally {
                if (ftpClient != null) {
                    if (isLoggedIn) {
                        try {
                            ftpClient.logout();
                        } catch (Exception e) {
                            // no error handling
                        }
                    }
                    if (ftpClient.isConnected()) {
                        try {
                            ftpClient.disconnect();
                        } catch (Exception e) {
                            // no error handling
                        }
                    }
                }
                if (flgJumpTransferDefined) {
                    doPostCommands();
                }
            }
        } catch (Exception e) {
            this.getLogger().warn("ftp processing failed: " + e.getMessage());
            arguments.put(conReturnValueFAILED_TRANSFERS, String.valueOf(++failed_transfer));
            writeHistory(hlocalFile, htargetFile);
            return false;
        }
    }

    private long transferFile(final SOSFileTransfer pobjFtpClient, final File sourceFile, final File targetFile, final long checkRetry1,
            final long plngCheckInterval, final boolean pflgCheckSize, final boolean appendFiles1) throws Exception {
        long retry = checkRetry1 > 0 ? checkRetry1 : 0;
        long interval = plngCheckInterval > 0 ? plngCheckInterval : 60;
        long lngCurrentBytesReceived = 0;
        long lngPreviousBytesReceived = 0;
        try {
            if (transActional) {
                transActionalRemoteFiles = hlocalFile + ";" + sosString.parseToString(transActionalRemoteFiles);
            } else if (!removeFiles && flgJumpTransferDefined) {
                strFilesRemainingOnSource = hlocalFile + ";" + strFilesRemainingOnSource;
            }
            String strSourceFileName = sourceFile.getName();
            String strTargetFileName = targetFile.getAbsolutePath();
            long lngSourceFileSize = pobjFtpClient.size(strSourceFileName);
            lngSourceFileSize = pobjFtpClient.size(strSourceFileName);
            for (int i = -1; i < retry; i++) {
                strTargetFileName = doEncoding(strTargetFileName, strFileNameEncoding);
                if (flgConvertUmlaute) {
                    strTargetFileName = doEncodeUmlaute(strTargetFileName, strFileNameEncoding);
                }
                getLogger().info("receiving file: " + strSourceFileName + " as " + strTargetFileName + " with " + lngSourceFileSize + " bytes");
                lngCurrentBytesReceived = pobjFtpClient.getFile(strSourceFileName, strTargetFileName, appendFiles1);
                htargetFile = strTargetFileName;
                if (lngCurrentBytesReceived < 0) {
                    lngCurrentBytesReceived = 0;
                }
                this.getLogger().debug(
                        "..ftp server reply [getFile] [" + strTargetFileName + ", size=" + lngCurrentBytesReceived + conClosingBracketWithColon
                                + pobjFtpClient.getReplyString());
                if (!appendFiles1 && retry > 0 && lngCurrentBytesReceived != lngPreviousBytesReceived) {
                    this.getLogger().info(
                            "..retry " + (i + 2) + " of " + retry + " to wait " + interval
                                    + "s for file transfer being completed, current file size: " + lngCurrentBytesReceived + " bytes");
                    try {
                        Thread.sleep(interval * 1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    break;
                }
                lngPreviousBytesReceived = lngCurrentBytesReceived;
            }
            long lngTargetFileSize = targetFile.length();
            if (pflgCheckSize && lngTargetFileSize > 0 && lngSourceFileSize != lngCurrentBytesReceived) {
                raiseException("..error occurred receiving file, source file size [" + lngSourceFileSize
                        + "] does not match number of bytes transferred [" + lngCurrentBytesReceived + "], target file size is " + lngTargetFileSize);
            }
            transActionalLocalFiles.add(htargetFile);
            return lngCurrentBytesReceived;
        } catch (Exception e) {
            raiseException("file transfer failed: " + e.getMessage(), e);
        }
        return lngCurrentBytesReceived;
    }

    private boolean doPreCommands() throws Exception {
        if (flgJumpTransferDefined) {
            if (!execute()) {
                String s = "";
                for (int i = 0; i < getCommands().length; i++) {
                    s = s + getCommands()[i] + getCommandDelimiter();
                }
                getLogger().warn("error occurred processing command: " + normalizedPassword(s));
                arguments.put("xx_make_temp_directory_xx", "ok");
                String curCommands =
                        sosString.parseToString(arguments.get("jump_command")) + " -operation=remove_temp_directory -input=\"" + tempJumpRemoteDir
                                + "\"";
                this.setCommands(curCommands.split(getCommandDelimiter()));
                if (!execute()) {
                    raiseException("error occurred processing command: " + curCommands);
                }
                arguments.remove("xx_make_temp_directory_xx");
                return false;
            }
            filelist = filelisttmp;
        }
        return true;
    }

    private void doPostCommands() throws Exception {
        getLogger().debug9("postCommands:  " + normalizedPassword(postCommands));
        if (getBool(conParameterREMOVE_AFTER_JUMP_TRANSFER) && !sosString.parseToString(postCommands).isEmpty()) {
            if (transActional) {
                getLogger().debug(".. mark transactional files for removal: " + transActionalRemoteFiles);
                Properties p = (Properties) arguments.clone();
                p.put("command", postCommands);
                p.put("operation", conOperationEXECUTE);
                p.put("xx_make_temp_directory_success_transfer_xx", "ok");
                postCommands = "";
                listOfSuccessTransfer.add(p);
            } else {
                arguments.put("xx_make_temp_directory_xx", "ok");
                int len = 0;
                int pos1 = 0;
                if (postCommands.indexOf("-file_path=") > -1) {
                    len = "-file_path=".length();
                    pos1 = postCommands.indexOf("-file_path=") + len;
                } else if (postCommands.indexOf("-remote_dir=") > -1) {
                    len = "-remote_dir=".length();
                    pos1 = postCommands.indexOf("-remote_dir=") + len;
                }
                int pos2 = postCommands.indexOf("-", pos1);
                if (pos2 == -1) {
                    pos2 = postCommands.length();
                }
                String path = "";
                if (postCommands.indexOf("-file_path=") > -1) {
                    int pos3 = postCommands.indexOf(";", pos1);
                    if (pos3 > -1) {
                        path = new File(postCommands.substring(pos1, pos3)).getParent();
                    } else {
                        path = new File(postCommands.substring(pos1, pos2)).getParent();
                    }
                } else {
                    path = postCommands.substring(pos1, pos2);
                }
                if (path == null) {
                    path = "";
                }
                path = path.replaceAll("\"", "");
                String curC = "";
                if (postCommands.endsWith(getCommandDelimiter())) {
                    postCommands = postCommands.substring(0, postCommands.lastIndexOf(getCommandDelimiter()));
                }
                Iterator<String> iterator = filelist.iterator();
                while (iterator.hasNext()) {
                    String filen = iterator.next();
                    Matcher matcher = pattern.matcher(filen);
                    if (matcher.find()) {
                        filen = (!path.trim().isEmpty() ? normalized(path.trim()) : "") + new File(sosString.parseToString(filen)).getName();
                        getLogger().debug9("delete remote file: " + filen);
                        if (pos2 > postCommands.length()) {
                            pos2 = postCommands.length();
                        }
                        curC =
                                curC + " " + postCommands.substring(0, pos1 - len) + " " + postCommands.substring(pos2) + " -file_path=\"" + filen
                                        + "\" ";
                        if (iterator.hasNext()) {
                            curC = curC + " " + getCommandDelimiter();
                        }
                    }
                }
                if (!filelist.isEmpty()) {
                    postCommands = curC;
                }
            }
        }
        postCommands =
                (sosString.parseToString(postCommands).length() == 0 ? "" : postCommands + " " + getCommandDelimiter() + " ")
                        + sosString.parseToString(arguments.get("jump_command")) + " -operation=remove_temp_directory -input=\"" + tempJumpRemoteDir
                        + "\"";
        getLogger().debug5("post-processing commands are: " + normalizedPassword(postCommands));
        this.setCommands(postCommands.split(getCommandDelimiter()));
        if (!execute()) {
            arguments.remove("xx_make_temp_directory_xx");
            raiseException("error occurred processing command: " + normalizedPassword(postCommands));
        }
        if (arguments.contains("xx_make_temp_directory_xx")) {
            arguments.remove("xx_make_temp_directory_xx");
        }
    }

    private void getParameter() throws Exception {
        try {
            protocol = getParam("protocol", "ftp");
            port = 21;
            if ("sftp".equalsIgnoreCase(protocol)) {
                sshBasedProtocol = true;
                port = 22;
            }
            retrieveCommonParameters();
            host = getParam("host", "");
            if (!sosString.parseToString(arguments.get("port")).isEmpty()) {
                try {
                    port = Integer.parseInt(sosString.parseToString(arguments.get("port")));
                } catch (Exception e) {
                    raiseException("illegal value for parameter [port]: " + sosString.parseToString(arguments.get("port") + " " + e.getMessage()), e);
                }
            }
            if (sosString.parseToString(arguments.get("port")).isEmpty()) {
                arguments.put("port", String.valueOf(port));
            }
            user = getParam(conParamUSER, "");
            if (!sosString.parseToString(arguments.get("password")).isEmpty()) {
                password = sosString.parseToString(arguments.get("password"));
            }
            if (!sosString.parseToString(arguments.get("account")).isEmpty()) {
                account = sosString.parseToString(arguments.get("account"));
            }
            if (!sosString.parseToString(arguments.get(conSettingTRANSFER_MODE)).isEmpty()) {
                transferMode = sosString.parseToString(arguments.get(conSettingTRANSFER_MODE));
            }
            if (!sosString.parseToString(arguments.get("passive_mode")).isEmpty()) {
                passiveMode = sosString.parseToBoolean(arguments.get("passive_mode"));
            }
            if (!sosString.parseToString(arguments.get(conSettingREMOTE_DIR)).isEmpty()) {
                remoteDir = sosString.parseToString(arguments.get(conSettingREMOTE_DIR));
                if (".".equals(remoteDir)) {
                    remoteDir = "/";
                }
            }
            if (!sosString.parseToString(arguments.get("local_dir")).isEmpty()) {
                localDir = sosString.parseToString(arguments.get("local_dir"));
            }
            fileSpec = getParam(conSettingFILE_SPEC, "");
            if (!sosString.parseToString(arguments.get("atomic_suffix")).isEmpty()) {
                atomicSuffix = sosString.parseToString(arguments.get("atomic_suffix"));
            }
            if (!sosString.parseToString(arguments.get("check_size")).isEmpty()) {
                checkSize = sosString.parseToBoolean(arguments.get("check_size"));
            }
            if (!sosString.parseToString(arguments.get("check_interval")).isEmpty()) {
                try {
                    checkInterval = Long.parseLong(sosString.parseToString(arguments.get("check_interval")));
                } catch (Exception e) {
                    raiseException("illegal value for parameter [check_interval]: " + sosString.parseToString(arguments.get("check_interval")) + " "
                            + e.getMessage(), e);
                }
            }
            if (!sosString.parseToString(arguments.get("check_retry")).isEmpty()) {
                try {
                    checkRetry = Long.parseLong(sosString.parseToString(arguments.get("check_retry")));
                } catch (Exception e) {
                    raiseException("invalid value for parameter [check_retry]: " + sosString.parseToString(arguments.get("check_retry")) + " "
                            + e.getMessage(), e);
                }
            }
            if (!sosString.parseToString(arguments.get("overwrite_files")).isEmpty()) {
                overwriteFiles = !sosString.parseToBoolean(arguments.get("overwrite_files"));
            }
            if (!sosString.parseToString(arguments.get("append_files")).isEmpty()) {
                appendFiles = sosString.parseToBoolean(arguments.get("append_files"));
            }
            if (!sosString.parseToString(arguments.get(conSettingREMOVE_FILES)).isEmpty()) {
                removeFiles = sosString.parseToBoolean(arguments.get(conSettingREMOVE_FILES));
            }
            if (!sosString.parseToString(arguments, "skip_transfer").isEmpty()) {
                skipTransfer = sosString.parseToBoolean(arguments.get("skip_transfer"));
            }
            if (!sosString.parseToString(arguments.get(conParamFORCE_FILES)).isEmpty()) {
                forceFiles = !sosString.parseToBoolean(arguments.get(conParamFORCE_FILES));
            }
            if (!sosString.parseToString(arguments.get("file_zero_byte_transfer")).isEmpty()) {
                if (sosString.parseToBoolean(arguments.get("file_zero_byte_transfer"))) {
                    zeroByteFiles = true;
                    zeroByteFilesStrict = false;
                } else if ("strict".equalsIgnoreCase(sosString.parseToString(arguments.get("file_zero_byte_transfer")))) {
                    zeroByteFiles = false;
                    zeroByteFilesStrict = true;
                } else if ("relaxed".equalsIgnoreCase(sosString.parseToString(arguments.get("file_zero_byte_transfer")))) {
                    zeroByteFiles = false;
                    zeroByteFilesStrict = false;
                    zeroByteFilesRelaxed = true;
                } else {
                    zeroByteFiles = false;
                    zeroByteFilesStrict = false;
                }
            }
            if (!sosString.parseToString(arguments.get("file_notification_to")).isEmpty()) {
                fileNotificationTo = sosString.parseToString(arguments.get("file_notification_to"));
            }
            if (!sosString.parseToString(arguments.get("file_notification_cc")).isEmpty()) {
                fileNotificationCC = sosString.parseToString(arguments.get("file_notification_cc"));
            }
            if (!sosString.parseToString(arguments.get("file_notification_bcc")).isEmpty()) {
                fileNotificationBCC = sosString.parseToString(arguments.get("file_notification_bcc"));
            }
            if (!sosString.parseToString(arguments.get("file_notification_subject")).isEmpty()) {
                fileNotificationSubject = sosString.parseToString(arguments.get("file_notification_subject"));
            }
            if (!sosString.parseToString(arguments.get("file_notification_body")).isEmpty()) {
                fileNotificationBody = sosString.parseToString(arguments.get("file_notification_body"));
            }
            if (!sosString.parseToString(arguments.get("file_zero_byte_notification_to")).isEmpty()) {
                fileZeroByteNotificationTo = sosString.parseToString(arguments.get("file_zero_byte_notification_to"));
            }
            if (!sosString.parseToString(arguments.get("file_zero_byte_notification_cc")).isEmpty()) {
                fileZeroByteNotificationCC = sosString.parseToString(arguments.get("file_zero_byte_notification_cc"));
            }
            if (!sosString.parseToString(arguments.get("file_zero_byte_notification_bcc")).isEmpty()) {
                fileZeroByteNotificationBCC = sosString.parseToString(arguments.get("file_zero_byte_notification_bcc"));
            }
            if (!sosString.parseToString(arguments.get("file_zero_byte_notification_subject")).isEmpty()) {
                fileZeroByteNotificationSubject = sosString.parseToString(arguments.get("file_zero_byte_notification_subject"));
            }
            if (!sosString.parseToString(arguments.get("file_zero_byte_notification_body")).isEmpty()) {
                fileZeroByteNotificationBody = sosString.parseToString(arguments.get("file_zero_byte_notification_body"));
            }
            if (!sosString.parseToString(arguments.get(conParamREPLACING)).isEmpty()) {
                replacing = sosString.parseToString(arguments.get(conParamREPLACING));
            }
            if (arguments.get(conParamREPLACEMENT) != null) {
                replacement = sosString.parseToString(arguments.get(conParamREPLACEMENT));
            }
            if (!sosString.parseToString(arguments.get("recursive")).isEmpty()) {
                recursive = sosString.parseToBoolean(arguments.get("recursive"));
            }
            if (!sosString.parseToString(arguments.get("poll_timeout")).isEmpty()) {
                pollTimeout = Integer.parseInt(sosString.parseToString(arguments.get("poll_timeout")));
            }
            if (!sosString.parseToString(arguments.get("poll_interval")).isEmpty()) {
                pollIntervall = Integer.parseInt(sosString.parseToString(arguments.get("poll_interval")));
            }
            if (!sosString.parseToString(arguments.get("poll_minfiles")).isEmpty()) {
                pollMinFiles = Integer.parseInt(sosString.parseToString(arguments.get("poll_minfiles")));
            }
            if (!sosString.parseToString(arguments.get("poll_error_state")).isEmpty()) {
                pollFilesErrorState = sosString.parseToString(arguments.get("poll_error_state"));
            }
            if (!sosString.parseToString(arguments.get("alternative_host")).isEmpty()) {
                alternativeHost = sosString.parseToString(arguments.get("alternative_host"));
            }
            if (!sosString.parseToString(arguments.get("alternative_port")).isEmpty()) {
                alternativePort = Integer.parseInt(sosString.parseToString(arguments.get("alternative_port")));
            }
            if (!sosString.parseToString(arguments.get("alternative_password")).isEmpty()) {
                alternativePassword = sosString.parseToString(arguments.get("alternative_password"));
            }
            if (!sosString.parseToString(arguments.get("alternative_user")).isEmpty()) {
                alternativeUser = sosString.parseToString(arguments.get("alternative_user"));
            }
            if (!sosString.parseToString(arguments.get("alternative_account")).isEmpty()) {
                alternativeAccount = sosString.parseToString(arguments.get("alternative_account"));
            }
            if (!sosString.parseToString(arguments.get("alternative_remote_dir")).isEmpty()) {
                alternativeRemoteDir = sosString.parseToString(arguments.get("alternative_remote_dir"));
            }
            if (!sosString.parseToString(arguments.get("alternative_passive_mode")).isEmpty()) {
                alternativePassiveMode = sosString.parseToBoolean(arguments.get("alternative_passive_mode"));
            }
            if (!sosString.parseToString(arguments.get("alternative_transfer_mode")).isEmpty()) {
                alternativeTransferMode = sosString.parseToString(arguments.get("alternative_transfer_mode"));
            }
            if (sshBasedProtocol) {
                if (!sosString.parseToString(arguments.get("ssh_proxy_host")).isEmpty()) {
                    proxyHost = sosString.parseToString(arguments.get("ssh_proxy_host"));
                }
                if (!sosString.parseToString(arguments.get("ssh_proxy_port")).isEmpty()) {
                    try {
                        proxyPort = Integer.parseInt(sosString.parseToString(arguments.get("ssh_proxy_port")));
                    } catch (Exception ex) {
                        raiseException("illegal non-numeric value for parameter [ssh_proxy_port]: "
                                + sosString.parseToString(arguments.get("ssh_proxy_port")) + " " + ex.getMessage(), ex);
                    }
                } else {
                    proxyPort = 3128;
                }
                if (!sosString.parseToString(arguments.get("ssh_proxy_user")).isEmpty()) {
                    proxyUser = sosString.parseToString(arguments.get("ssh_proxy_user"));
                }
                if (!sosString.parseToString(arguments.get("ssh_proxy_password")).isEmpty()) {
                    proxyPassword = sosString.parseToString(arguments.get("ssh_proxy_password"));
                }
                if (!sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD)).isEmpty()) {
                    if ("publickey".equalsIgnoreCase(sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD)))
                            || "password".equalsIgnoreCase(sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD)))) {
                        authenticationMethod = sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD));
                    } else {
                        raiseException("invalid authentication method [publickey, password] specified: "
                                + sosString.parseToString(arguments.get(conParamSSH_AUTH_METHOD)));
                    }
                } else {
                    authenticationMethod = "publickey";
                }
                if (!sosString.parseToString(arguments.get("ssh_auth_file")).isEmpty()) {
                    authenticationFilename = sosString.parseToString(arguments.get("ssh_auth_file"));
                } else if ("publickey".equalsIgnoreCase(authenticationMethod)) {
                    raiseException("no authentication filename was specified as parameter [ssh_auth_file]");
                }
            }
            if (!sosString.parseToString(arguments.get("http_proxy_host")).isEmpty()) {
                proxyHost = sosString.parseToString(arguments.get("http_proxy_host"));
            }
            if (!sosString.parseToString(arguments.get("http_proxy_port")).isEmpty()) {
                try {
                    proxyPort = Integer.parseInt(sosString.parseToString(arguments.get("http_proxy_port")));
                } catch (Exception ex) {
                    throw new NumberFormatException("illegal non-numeric value for parameter [http_proxy_port]: "
                            + sosString.parseToString(arguments.get("http_proxy_port")) + " " + ex.getMessage());
                }
            }
            if (!flgJumpTransferDefined) {
                if (!sosString.parseToString(arguments.get("file_path")).isEmpty()) {
                    filePath = sosString.parseToString(arguments.get("file_path"));
                    isFilePath = true;
                } else {
                    isFilePath = false;
                }
            }
            transActional = getBooleanParam("transactional", "false");
            if (flgJumpTransferDefined) {
                transActional = true;
            }
            testmode = sosString.parseToBoolean(arguments.get("mail_smtp"));
            if (!sosString.parseToString(arguments.get("mail_smtp")).isEmpty()) {
                mailSMTP = sosString.parseToString(arguments.get("mail_smtp"));
            }
            if (!sosString.parseToString(arguments.get("mail_PortNumber")).isEmpty()) {
                mailPortNumber = sosString.parseToString(arguments.get("mail_PortNumber"));
            }
            if (!sosString.parseToString(arguments.get("mail_from")).isEmpty()) {
                mailFrom = sosString.parseToString(arguments.get("mail_from"));
            }
            if (!sosString.parseToString(arguments.get("mail_queue_dir")).isEmpty()) {
                mailQueueDir = sosString.parseToString(arguments.get("mail_queue_dir"));
            }
            if (!sosString.parseToString(arguments.get("simple_transfer")).isEmpty()) {
                getLogger().info("Parameter 'simple_transfer' is deactivated.");
            }
            simpleTransfer = false;
        } catch (Exception e) {
            throw new RuntimeException("could not process job parameters: " + e.getMessage(), e);
        }
    }

    private void getFileNotificationParameter() throws Exception {
        try {
            if (fileNotificationTo != null && !fileNotificationTo.isEmpty()) {
                if (fileNotificationSubject == null || fileNotificationSubject.isEmpty()) {
                    fileNotificationSubject = "SOSFTPCommand";
                }
                if (fileNotificationBody == null || fileNotificationBody.isEmpty()) {
                    fileNotificationBody = "The following files have been received:\n\n";
                }
            }
            if (fileZeroByteNotificationTo != null && !fileZeroByteNotificationTo.isEmpty()) {
                if (fileZeroByteNotificationSubject == null || fileZeroByteNotificationSubject.isEmpty()) {
                    fileZeroByteNotificationSubject = "[warning] SOSFTPCommand";
                }
                if (fileZeroByteNotificationBody == null || fileZeroByteNotificationBody.isEmpty()) {
                    fileZeroByteNotificationBody = "The following files have been received and were removed due to zero byte constraints:\n\n";
                }
            }
        } catch (Exception e) {
            raiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
        }
    }

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
            this.getLogger().debug(".. job parameter [alternative_host]                   : " + alternativeHost);
            this.getLogger().debug(".. job parameter [alternative_port]                   : " + alternativePort);
            this.getLogger().debug(".. job parameter [alternative_user]                   : " + alternativeUser);
            this.getLogger().debug(
                    ".. job parameter [alternative_password]               : " + normalizedPassword(sosString.parseToString(alternativePassword)));
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
            try {
                this.getLogger().debug(".. job parameter [http_proxy_host]                : " + proxyHost);
                this.getLogger().debug(".. job parameter [http_proxy_port]                : " + proxyPort);
            } catch (Exception e) {
            }
        } catch (Exception e) {
            raiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " could not read parameters, cause: " + e.getMessage(), e);
        }
    }

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
        } catch (Exception e) {
            raiseException("..error in setAlternativeParameter, cause: " + e.getMessage(), e);
        }
    }

    private void receiveFiles(final Vector<String> pvecFileList1) throws Exception {
        Iterator<String> iterator = pvecFileList1.iterator();
        transActionalLocalFiles = new ArrayList<String>();
        while (iterator.hasNext()) {
            String fileName = iterator.next();
            String strFileName4Matcher = fileName;
            if (flgUsePathAndFileName4Matching == false) {
                strFileName4Matcher = new File(fileName).getName();
            }
            Matcher matcher = pattern.matcher(strFileName4Matcher);
            File transferFile = null;
            hlocalFile = "";
            htargetFile = "";
            arguments.put(conSettingFILE_SIZE, "");
            this.getLogger().debug7("Processing file " + fileName);
            if (recursive && !isFilePath && !sosString.parseToString(remoteDir).isEmpty()) {
                if (!ftpClient.changeWorkingDirectory(remoteDir)) {
                    raiseException(FTP_SERVER_REPLY_CD_REMOTE_DIR + remoteDir + conClosingBracketWithColon + ftpClient.getReplyString());
                } else {
                    getLogger().debug(FTP_SERVER_REPLY_CD_REMOTE_DIR + remoteDir + conClosingBracketWithColon + ftpClient.getReplyString());
                }
            }
            if (matcher.find() || isFilePath) {
                if (replacement != null && replacing != null && !replacing.isEmpty()) {
                    String strFileName4Replacing = new File(fileName).getName();
                    String currTransferFilename = SOSFileOperations.getReplacementFilename(strFileName4Replacing, replacing, replacement);
                    this.getLogger().debug1(String.format("transfer file [%1$s] is renamed to [%2$s]", strFileName4Replacing, currTransferFilename));
                    if (isFilePath) {
                        File f = new File(currTransferFilename);
                        transferFile = this.createFile(adjustPathName(localDir) + f.getName());
                    } else {
                        transferFile = this.createFile(adjustPathName(localDir) + currTransferFilename);
                    }
                } else {
                    if (isFilePath) {
                        transferFile = this.createFile(adjustPathName(localDir) + new File(fileName).getName());
                    } else {
                        String strT = new File(fileName).getName();
                        if (recursive) {
                            strT = stripRemoteDirName(remoteDir, fileName);
                        }
                        transferFile = this.createFile(adjustPathName(localDir) + strT);
                    }
                }
                File transFile = transferFile;
                if (transFile.getParent() != null && !transFile.getParentFile().exists()) {
                    if (transFile.getParentFile().mkdirs()) {
                        this.getLogger().debug1("..create parent directory [" + transFile.getParentFile() + "]");
                    } else {
                        raiseException("..error occurred creating directory [" + transFile.getParentFile() + "]");
                    }
                }
                if (!appendFiles && !overwriteFiles && transferFile.exists()) {
                    this.getLogger().debug1("..ftp transfer skipped for file [no overwrite]: " + transferFile.getName());
                    continue;
                }
                long bytesSent = 0;
                if (!appendFiles && atomicSuffix != null && !atomicSuffix.isEmpty()) {
                    File atomicFile = new File(transferFile.getAbsolutePath() + atomicSuffix);
                    File file = new File(fileName);
                    if (recursive && file.getParent() != null && !isFilePath) {
                        String[] splitParent = file.getParent().split(conRegExpBackslash);
                        for (int i = 0; i < splitParent.length; i++) {
                            if (!sosString.parseToString(splitParent[i]).isEmpty()) {
                                if (!ftpClient.changeWorkingDirectory(splitParent[i])) {
                                    raiseException(FTP_SERVER_REPLY_CD_REMOTE_DIR + splitParent[i] + conClosingBracketWithColon
                                            + ftpClient.getReplyString());
                                } else {
                                    getLogger().debug(
                                            FTP_SERVER_REPLY_CD_REMOTE_DIR + splitParent[i] + conClosingBracketWithColon + ftpClient.getReplyString());
                                }
                            }
                        }
                    }
                    if (skipTransfer) {
                        bytesSent = ftpClient.size(file.getName());
                        this.getLogger().debug7(" Processing file " + file.getName());
                        if (!removeFiles && !testmode && replacement != null && replacing != null && !replacing.isEmpty()) {
                            ftpClient.rename(file.getName(), transferFile.getName());
                        }
                        fileNotificationBody += doEncoding(transferFile.getName(), strFileNameEncoding) + conNewLine;
                        Matcher matcher1 = pattern.matcher(transferFile.getName());
                        if (matcher1.find()) {
                            transferFileList.add(transferFile);
                            count++;
                        }
                    } else {
                        hlocalFile = file.getName();
                        htargetFile = atomicFile.getAbsolutePath().substring(0, atomicFile.getAbsolutePath().indexOf(atomicSuffix));
                        bytesSent =
                                this.transferFile(ftpClient, new File(file.getName()), atomicFile, checkRetry, checkInterval, checkSize, appendFiles);
                        arguments.put(conSettingFILE_SIZE, String.valueOf(bytesSent));
                        if (transferFile.exists() && overwriteFiles && !transferFile.delete()) {
                            raiseException(String.format("overwrite or delete of local file '%1$s' failed ", transferFile.getAbsolutePath()));
                        }
                        if (bytesSent <= 0 && !zeroByteFiles && zeroByteFilesStrict) {
                            this.getLogger().debug1("removing local file : " + transferFile.getAbsolutePath() + " due to zero byte strict constraint");
                            if (!atomicFile.delete()) {
                                raiseException("..error occurred, could not remove temporary file: " + atomicFile.getAbsolutePath());
                            }
                            fileZeroByteNotificationBody += transferFile.getName() + conNewLine;
                            zeroByteCount++;
                        } else {
                            if (bytesSent <= 0 && !zeroByteFiles) {
                                checkFileList.put(atomicFile, transferFile);
                            } else {
                                if (!transActional) {
                                    this.getLogger().debug1(
                                            "renaming local temporary file '" + atomicFile.getAbsolutePath() + "' to '"
                                                    + transferFile.getAbsolutePath() + "'");
                                    if (!atomicFile.renameTo(transferFile)) {
                                        raiseException("could not rename temporary file [" + atomicFile.getCanonicalPath() + "] to: "
                                                + transferFile.getAbsolutePath());
                                    }
                                }
                                fileNotificationBody += doEncoding(transferFile.getName(), strFileNameEncoding) + conNewLine;
                                transferFileList.add(transferFile);
                                count++;
                            }
                        }
                    }
                } else {
                    File file = new File(fileName);
                    if (recursive) {
                        String strP = file.getParent();
                        if (strP != null) {
                            String[] splitParent = strP.split(conRegExpBackslash);
                            for (String strF : splitParent) {
                                if (!strF.trim().isEmpty()) {
                                    String strM = FTP_SERVER_REPLY_CD_REMOTE_DIR + strF + conClosingBracketWithColon + ftpClient.getReplyString();
                                    if (!ftpClient.changeWorkingDirectory(strF)) {
                                        raiseException(strM);
                                    } else {
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
                    } else {
                        hlocalFile = file.getName();
                        htargetFile = transferFile.getAbsolutePath();
                        bytesSent =
                                this.transferFile(ftpClient, new File(file.getName()), transferFile, checkRetry, checkInterval, checkSize,
                                        appendFiles);
                        arguments.put(conSettingFILE_SIZE, String.valueOf(bytesSent));
                        if (bytesSent <= 0 && !zeroByteFiles && zeroByteFilesStrict) {
                            this.getLogger().debug1("removing local file : " + transferFile.getAbsolutePath() + " due to zero byte strict constraint");
                            if (!transferFile.delete()) {
                                raiseException("..error occurred, could not remove temporary file: " + transferFile.getAbsolutePath());
                            }
                            fileZeroByteNotificationBody += transferFile.getName() + conNewLine;
                            zeroByteCount++;
                        } else if (bytesSent <= 0 && !zeroByteFiles) {
                            checkFileList.put(transferFile, transferFile);
                        } else {
                            fileNotificationBody += doEncoding(transferFile.getName(), strFileNameEncoding) + conNewLine;
                            transferFileList.add(transferFile);
                            count++;
                        }
                    }
                }
                if (!transActional && removeFiles) {
                    boolean ok = ftpClient.delete(new File(fileName).getName());
                    if (!ok) {
                        raiseException("..error occurred, could not remove remote file [" + transferFile.getName() + conClosingBracketWithColon
                                + ftpClient.getReplyString());
                    } else {
                        if (listOfSuccessTransfer != null && !listOfSuccessTransfer.isEmpty()) {
                            this.getLogger().debug("removing remote file: " + transferFile.getName());
                        } else {
                            this.getLogger().info("removing remote file: " + transferFile.getName());
                        }
                    }
                }
                if (!sosString.parseToString(hlocalFile).isEmpty() && !sosString.parseToString(htargetFile).isEmpty()) {
                    arguments.put(conReturnValueSUCCESSFUL_TRANSFERS, String.valueOf(++successful_transfers));
                }
                writeHistory(hlocalFile, htargetFile);
            }
        }
    }

    private void sendMails() throws Exception {
        if (zeroByteCount > 0 && fileZeroByteNotificationTo != null && !fileZeroByteNotificationTo.isEmpty()) {
            sendMail(fileZeroByteNotificationTo, fileZeroByteNotificationCC, fileZeroByteNotificationBCC, fileZeroByteNotificationSubject,
                    fileZeroByteNotificationBody);
        }
        if (count > 0 && fileNotificationTo != null && !fileNotificationTo.isEmpty()) {
            sendMail(fileNotificationTo, fileNotificationCC, fileNotificationBCC, fileNotificationSubject, fileNotificationBody);
        }
    }

    private boolean printState(boolean rc) throws Exception {
        String received = "received";
        if (skipTransfer) {
            if (conOperationREMOVE.equalsIgnoreCase(sosString.parseToString(arguments, "operation"))) {
                if (listOfSuccessTransfer != null && !listOfSuccessTransfer.isEmpty()) {
                    getLogger().debug1(count + conTextFILES_REMOVE);
                } else {
                    getLogger().info(count + conTextFILES_REMOVE);
                    state = count + conTextFILES_REMOVE;
                }
            } else {
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
            } else if (zeroByteCount > 0 && zeroByteFilesStrict) {
                raiseException("zero byte file(s) found");
            } else if (forceFiles) {
                raiseException("no matching files found");
            } else {
                this.getLogger().debug1("no matching files found");
                state = "no matching files found";
            }
            if (forceFiles) {
                rc = !zeroByteFilesRelaxed;
            } else {
                return false;
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

    private boolean polling() throws Exception {
        try {
            if (testmode && flgJumpTransferDefined && filelist.isEmpty()) {
                return true;
            }
            if (schedulerJob != null && pollTimeout > 0) {
                Class cl = Class.forName("sos.scheduler.ftp.JobSchedulerFTPReceive");
                if (cl.getName().indexOf("FTPReceive") > -1) {
                    Method mthd1 =
                            cl.getMethod("polling", new Class[] { Vector.class, Boolean.TYPE, String.class, SOSFileTransfer.class, String.class,
                                    Boolean.TYPE, Boolean.TYPE, int.class, int.class, int.class, String.class });
                    boolean output1 =
                            sosString.parseToBoolean(mthd1.invoke(schedulerJob, new Object[] { filelist, new Boolean(isFilePath), filePath,
                                    ftpClient, fileSpec, Boolean.valueOf(recursive), Boolean.valueOf(forceFiles), new Integer(pollTimeout),
                                    new Integer(pollIntervall), new Integer(pollMinFiles), pollFilesErrorState }));
                    return output1;
                }
            }
            Iterator iterator = filelist.iterator();
            if (pollTimeout > 0) {
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
                                File file = new File(fileName);
                                long si = ftpClient.size(file.getName());
                                if (si > -1) {
                                    found = true;
                                }
                            } catch (Exception e) {
                                getLogger().debug9("File " + fileName + " was not found. " + e.getMessage());
                            }
                            if (found) {
                                matchedFiles++;
                                getLogger().debug8("Found matching file " + fileName);
                            }
                        } else {
                            Matcher matcher = pattern.matcher(fileName);
                            if (matcher.find()) {
                                matchedFiles++;
                                getLogger().debug8("Found matching file " + fileName);
                            }
                        }
                    }
                    getLogger().debug3(matchedFiles + " matching files found");
                    if (matchedFiles < pollMinFiles) {
                        if (tries < nrOfTries) {
                            getLogger().info("polling for files ...");
                            Thread.sleep((long) delay * 1000);
                            if (isFilePath) {
                                filelist = new Vector();
                                String[] split = filePath.split(";");
                                for (String element : split) {
                                    if (element != null && !element.isEmpty()) {
                                        filelist.add(element);
                                    }
                                }
                                fileSpec = conRegExpAllChars;
                            } else {
                                filelist = ftpClient.nList(recursive);
                            }
                            iterator = filelist.iterator();
                        } else {
                            giveUpPoll = true;
                        }
                    } else {
                        done = true;
                    }
                    if (giveUpPoll) {
                        String message = "Failed to find at least " + pollMinFiles + " files matching \"" + fileSpec + "\" ";
                        if (isFilePath) {
                            message = "Failed to find file \"" + filePath + "\" ";
                        }
                        message += "after triggering for " + pollTimeout + " minutes.";
                        if (matchedFiles > 0) {
                            message += " (only " + matchedFiles + " files were found)";
                        }
                        if (pollFilesErrorState != null && !pollFilesErrorState.isEmpty()) {
                            getLogger().warn(pollFilesErrorState);
                        }
                        if (forceFiles) {
                            getLogger().warn(message);
                            String body = message + conNewLine;
                            body += "See attached logfile for details.";

                        } else {
                            getLogger().info(message);
                        }
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            raiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " error while polling, cause: " + e.getMessage(), e);
        }
        return true;
    }

    private void getDeleteTransferFiles() throws Exception {
        if ((transActionalRemoteFiles != null || strFilesRemainingOnSource != null)
                && (removeFiles && transActional || getBool(conParameterREMOVE_AFTER_JUMP_TRANSFER))) {
            getLogger().debug(".. mark transactional files to remove " + transActionalRemoteFiles);
            Properties p = (Properties) arguments.clone();
            p.put(conSettingREMOTE_DIR, savedArguments.getProperty(conSettingREMOTE_DIR));
            p.put(conSettingLOCAL_DIR, "");
            if (savedArguments.containsKey(conParamSSH_AUTH_FILE)) {
                p.put(conParamSSH_AUTH_FILE, savedArguments.getProperty(conParamSSH_AUTH_FILE));
            }
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
            if (transActional) {
                p.put("file_path", transActionalRemoteFiles);
            } else {
                p.put("file_path", strFilesRemainingOnSource);
            }
            p.put("transactional", "no");
            listOfSuccessTransfer.add(p);
        }
    }

    private void getRenameAtomicSuffixTransferFiles() throws Exception {
        try {
            if (transActional && !sosString.parseToString(atomicSuffix).isEmpty()) {
                Properties p = new Properties();
                p.put("operation", "rename_local_files");
                p.put("files", transActionalLocalFiles);
                p.put("atomic_suffix", atomicSuffix);
                listOfSuccessTransfer.add(p);
                getLogger().debug(".. mark transactional files to rename " + transActionalLocalFiles);
            }
        } catch (Exception e) {
            raiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
        }
    }

    private void getRemoveTransferFiles() throws Exception {
        if (transActional && transActionalLocalFiles != null && !transActionalLocalFiles.isEmpty()) {
            Properties p = new Properties();
            p.put("operation", "delete_local_files");
            p.put("files", transActionalLocalFiles);
            listOfErrorTransfer.add(p);
            getLogger().debug(".. mark transactional files to remove on error: " + transActionalLocalFiles);
        }
    }

    public int getOfTransferFilesCount() {
        return count;
    }

    public int getZeroByteCount() {
        return zeroByteCount;
    }

    public SOSFileTransfer initFTPServer() throws Exception {
        if ("ftp".equalsIgnoreCase(protocol)) {
            initSOSFTP();
        } else if ("sftp".equalsIgnoreCase(protocol)) {
            initSOSSFTP();
        } else if ("ftps".equalsIgnoreCase(protocol)) {
            initSOSFTPS();
        } else {
            raiseException("Unknown protocol: " + protocol);
        }
        if (sosString.parseToString(arguments, conParamUSER).isEmpty() && "anonymous".equals(sosString.parseToString(user))) {
            arguments.put(conParamUSER, "anonymous");
        }
        return ftpClient;
    }

    public String getState() {
        return state;
    }

    private boolean receiveSimpleTransfer() throws Exception {
        try {
            getLogger().debug9("..filepath: " + filePath);
            if (!sosString.parseToString(remoteDir).isEmpty() && "./".equals(sosString.parseToString(remoteDir))) {
                remoteDir = "";
            }
            String targetFile = null;
            String localDir = sosString.parseToString(arguments.get(conSettingLOCAL_DIR));
            boolean appendFiles = sosString.parseToBoolean(sosString.parseToString(arguments.get("append_files")));
            boolean overwriteFiles = sosString.parseToBoolean(sosString.parseToString(arguments.get("overwrite_files")));
            if (!sosString.parseToString(remoteDir).isEmpty()) {
                targetFile = new File(sosString.parseToString(remoteDir), filePath).getPath().replaceAll(conRegExpBackslash, "/");
            } else {
                targetFile = new File(filePath).getPath().replaceAll(conRegExpBackslash, "/");
            }
            String localFile = new File(localDir, new File(targetFile).getName()).getPath();
            if (!appendFiles && !overwriteFiles && new File(localFile).exists()) {
                this.getLogger().info("..ftp transfer skipped for file [no overwrite]: " + localFile);
                return false;
            }
            SOSFileTransfer ftpClient = initFTPServer();
            long currentBytesSent = ftpClient.getFile(targetFile, localFile, appendFiles);
            this.getLogger().debug(
                    "..ftp server reply [getFile] [" + localFile + ", size=" + currentBytesSent + conClosingBracketWithColon
                            + ftpClient.getReplyString());
            this.getLogger().debug("1 file received " + localFile);
            getLogger().info("receiving file: " + localFile + " " + ftpClient.size(targetFile) + " bytes");
            transferFileList.add(new File(filePath));
            filelist = new Vector<String>();
            filelist.add(filePath);
            arguments.put(conReturnValueSUCCESSFUL_TRANSFERS, "1");
            return true;
        } catch (Exception e) {
            arguments.put(conReturnValueFAILED_TRANSFERS, "1");
            raiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
        }
        return true;
    }

    private boolean isSimpleTransfer() throws Exception {
        return false;
    }

    private String adjustPathName(final String pstrPathName) {
        String strResult = pstrPathName + (pstrPathName.endsWith("/") || pstrPathName.endsWith("\\") ? "" : "/");
        return strResult;
    }

}
