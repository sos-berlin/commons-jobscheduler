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

import org.apache.commons.httpclient.methods.GetMethod;

import sos.net.SOSFTP;
import sos.net.SOSFTPCommand;
import sos.util.SOSFile;
import sos.util.SOSFileOperations;
import sos.util.SOSGZip;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;
import sos.util.SOSString;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author Mueruevet Oeksuez */
public class SOSFTPCommandSend extends SOSFTPCommand {

    private static final String PARAMETER_MAKE_DIRS = "make_dirs";
    private static SOSString sosString = new SOSString();
    private static String state = "";
    private final String defaultLocalDir = ".";
    private final String installpaths = "%{local_dir}/xalan.jar;%{local_dir}/trilead-ssh2-build211.jar;%{local_dir}/commons-net-1.2.2.jar;%{local_dir}"
            + "/readme.txt;%{local_dir}/ThirdParty.txt";
    private final String installDocPaths = "%{local_dir}/doc/banner_english.gif;%{local_dir}/doc/banner_german.gif;%{local_dir}/doc/sosftp.xml;%{local_dir}"
            + "/doc/sosftp.xsl";
    private int intPosixPermissions = 0;
    private int successful_transfers = 0;
    private int failed_transfer = 0;
    private int count = 0;
    private int zeroByteCount = 0;
    private int alternativePort = 0;
    private int lastPort = 0;
    private int pollTimeout = 0;
    private int pollIntervall = 60;
    private boolean testmode = false;
    private boolean simpleTransfer = false;
    private boolean recursive = false;
    private boolean isFilePath = false;
    private boolean alternativePassiveMode = false;
    private boolean sshBasedProtocol = false;
    private boolean passiveMode = false;
    private boolean checkSize = true;
    private boolean overwriteFiles = true;
    private boolean appendFiles = false;
    private boolean removeFiles = false;
    private boolean skipTransfer = false;
    private boolean flgMakeDirs = false;
    private boolean forceFiles = true;
    private boolean compressFiles = false;
    private boolean zeroByteFiles = true;
    private boolean zeroByteFilesStrict = false;
    private boolean zeroByteFilesRelaxed = false;
    private boolean keepConnection = false;
    private boolean sameConnection = false;
    private File localFile = null;
    private String protocol = "ftp";
    private String transferMode = "binary";
    private String remoteDir = "./";
    private String localDir = ".";
    private String fileSpec = ".*";
    private String atomicSuffix = "";
    private String compressedFileExtension = "";
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
    private String replacing = null;
    private String replacement = null;
    private String filePath = "";
    private String alternativeHost = "";
    private String alternativeUser = "";
    private String alternativePassword = "";
    private String alternativeAccount = "";
    private String alternativeRemoteDir = "";
    private String alternativeTransferMode = "";
    private String lastHost = "";
    private String lastUser = "";
    private String lastAccount = "";
    private String installpathsWithRevNr = "%{local_dir}/com.sos.xml.*[.]jar$;%{local_dir}/com.sos.connection.*[.]jar$;%{local_dir}/com.sos.net.*[.]jar$;%{local_dir}"
            + "/com.sos.settings.*[.]jar$;%{local_dir}/com.sos.util.*[.]jar$;%{local_dir}/com.sos.configuration.*[.]jar$";
    private String transActionalRemoteFiles = null;
    private ArrayList<File> transActionalLocalFiles = null;
    public static final String RECURSIVE = "recursive";
    public static final String FILE_SIZE = "file_size";

    public SOSFTPCommandSend(final SOSLogger logger, final Properties arguments_) throws Exception {
        super(logger, arguments_);
    }

    public SOSFTPCommandSend(final sos.configuration.SOSConfiguration sosConfiguration_, final SOSLogger logger) throws Exception {
        super(sosConfiguration_, logger);
    }

    @Override
    public boolean send() throws Exception {
        getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
        Vector filelist = null;
        boolean rc = false;
        try {
            setParam("successful_transfers", "0");
            setParam("failed_transfers", "0");
            if (this.getLogger() == null) {
                this.setLogger(new SOSStandardLogger(0));
            }
            if (!sosString.parseToString(arguments.get("operation")).startsWith("install") || flgJumpTransferDefined) {
                readSettings(true);
            }
            try {
                getParameter();
                getFileNotificationParameter();
                printParameter();
                simpleTransfer = isSimpleTransfer();
            } catch (Exception e) {
                throw new Exception("could not process job parameters: " + e.getMessage());
            }
            if (!arguments.contains("port")) {
                setParam("port", String.valueOf(port));
            }
            try {
                if (host == null || host.isEmpty()) {
                    RaiseException("no host was specified");
                }
                if (user == null || user.isEmpty()) {
                    RaiseException("no user was specified");
                }
            } catch (Exception e) {
                throw new Exception("invalid or insufficient parameters: " + e.getMessage());
            }
            try {
                if (!sameConnection) {
                    if (localDir.startsWith(conRegExpBackslash)) {
                        while (localDir.indexOf("\\") != -1) {
                            localDir = localDir.replace('\\', '/');
                        }
                    }
                    if (localDir.startsWith("file://") && !new File(createURI(localDir)).exists()) {
                        RaiseException("local directory does not exist or is not accessible: " + localDir);
                    }
                    this.getLogger().debug1(
                            "connecting by " + protocol + " to host " + host + ", port " + port + ", local directory " + localDir + ", remote directory "
                                    + remoteDir + (isFilePath ? ", file " + filePath : ", file specification " + fileSpec));
                    boolean alternativeUse = true;
                    int isAlternativeParameterUse = 0;
                    while (alternativeUse && isAlternativeParameterUse <= 1) {
                        try {
                            if ("ftp".equalsIgnoreCase(protocol)) {
                                try {
                                    initSOSFTP();
                                } catch (Exception e) {
                                    throw e;
                                }
                            } else if ("sftp".equalsIgnoreCase(protocol)) {
                                initSOSSFTP();
                            } else if ("ftps".equalsIgnoreCase(protocol)) {
                                initSOSFTPS();
                            } else {
                                RaiseException("Unknown protocol: " + protocol);
                            }
                            if (sosString.parseToString(arguments, "user").isEmpty() && "anonymous".equals(sosString.parseToString(user))) {
                                setParam("user", "anonymous");
                            }
                            alternativeUse = false;
                        } catch (Exception ex) {
                            this.getLogger().debug1(
                                    "..error in ftp server init with [host=" + host + "], [port=" + port + "] " + SOSFTPCommand.getErrorMessage(ex));
                            alternativeUse = !(alternativeHost.concat(alternativeUser).concat(alternativePassword).concat(alternativeAccount)).isEmpty()
                                    || alternativePort != 0;
                            if (alternativeUse && isAlternativeParameterUse == 0) {
                                getLogger().reset();
                                if (ftpClient != null) {
                                    int orderQueueLength = 0;
                                    this.getLogger().debug("..ftp server reply: " + ftpClient.getReplyString());
                                    if (ftpClient.isConnected() && !keepConnection || ftpClient.isConnected() && keepConnection && orderQueueLength < 1) {
                                        if (isLoggedIn) {
                                            try {
                                                ftpClient.logout();
                                            } catch (Exception e) {
                                                // no error handling
                                            }
                                        }
                                        this.getLogger().debug("..ftp server reply [logout]: " + ftpClient.getReplyString());
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
                                        "..try login with alternative parameter [host=" + host + "], [port=" + port + "] " + "[user=" + user + "], [account="
                                                + account + "], [remoteDir=" + remoteDir + "], [passiveMode=" + passiveMode + "], " + "[transferMode="
                                                + transferMode + "]");
                            } else {
                                RaiseException("..error in ftp server init with [host=" + host + "], [port=" + port + "] " + ex.getMessage());
                            }
                        }
                    }
                } else {
                    this.getLogger().debug1("reusing connection from previous transfer");
                }
                if (!isLoggedIn) {
                    RaiseException(".. server reply [login failed] [user=" + user + "], [account=" + account + "]: " + ftpClient.getReplyString());
                }
                if (ftpClient instanceof SOSFTP) {
                    SOSFTP sosftp = (SOSFTP) ftpClient;
                    if (passiveMode) {
                        sosftp.enterLocalPassiveMode();
                        sosftp.passive();
                        if (sosftp.getReplyCode() > ERROR_CODE) {
                            RaiseException("..ftp server reply [passive]: " + ftpClient.getReplyString());
                        } else {
                            this.getLogger().debug("..ftp server reply [passive]: " + ftpClient.getReplyString());
                        }
                    }
                    if ("ascii".equalsIgnoreCase(transferMode)) {
                        if (sosftp.ascii()) {
                            this.getLogger().debug("..using ASCII mode for file transfer");
                            this.getLogger().debug("..ftp server reply [ascii]: " + ftpClient.getReplyString());
                        } else {
                            RaiseException(".. could not switch to ASCII mode for file transfer ..ftp server reply [ascii]: " + ftpClient.getReplyString());
                        }
                    } else {
                        if (sosftp.binary()) {
                            this.getLogger().debug("using binary mode for file transfer");
                            this.getLogger().debug("..ftp server reply [binary]: " + ftpClient.getReplyString());
                        } else {
                            RaiseException(".. could not switch to binary mode for file transfer .. ftp server reply [ascii]: " + ftpClient.getReplyString());
                        }
                    }
                    if (!strPreFtpCommands.isEmpty()) {
                        String[] strA = strPreFtpCommands.split(";");
                        for (String strCmd : strA) {
                            sosftp.sendCommand(strCmd);
                            this.getLogger().debug("..ftp server reply [" + strCmd + "]: " + ftpClient.getReplyString());
                        }
                    }
                    if (!strControlEncoding.isEmpty()) {
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
                } else {
                    if (this.createFile(localDir).isDirectory()) {
                        this.getLogger().debug1("local directory: " + localDir + ", remote directory: " + remoteDir + ", file specification: " + fileSpec);
                        filelist = SOSFile.getFilelist(this.createFile(localDir).getAbsolutePath(), fileSpec, 0, recursive);
                    } else {
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
                                    } else {
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
                    if (!sosString.parseToString(replacement).isEmpty() && sosString.parseToString(replacing).isEmpty() && !testmode) {
                        renameAtomicSuffixTransferFiles(filelist);
                    }
                    return true;
                } else {
                    int intStep = 1;
                    boolean flgError = false;
                    try {
                        sendFiles(filelist);
                        intStep = 2;
                        getRenameAtomicSuffixTransferFiles();
                        intStep = 3;
                        getDeleteTransferFiles();
                    } catch (Exception e) {
                        flgError = true;
                        if (transActional) {
                            getLogger().warn("could not complete transaction, cause " + e.toString());
                        } else {
                            getLogger().warn("could not complete transaction, cause " + e.toString());
                        }
                        throw e;
                    } finally {
                        if (flgError = true) {
                            switch (intStep) {
                            case 1:
                                getRemoveTransferFiles();
                                break;
                            case 2:
                                getRemoveTransferFiles();
                                break;
                            case 3:
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
                setParam("status", "success");
                return rc;
            } catch (Exception e) {
                setParam("status", "error");
                failed_transfer++;
                setParam("failed_transfers", String.valueOf(failed_transfer));
                rc = false;
                throw new Exception("could not process file transfer: " + e.getMessage(), e);
            } finally {
                if (ftpClient != null) {
                    int orderQueueLength = 0;
                    this.getLogger().debug("..ftp server reply: " + ftpClient.getReplyString());
                    if (ftpClient.isConnected() && !keepConnection || ftpClient.isConnected() && keepConnection && orderQueueLength < 1) {
                        if (isLoggedIn) {
                            try {
                                ftpClient.logout();
                            } catch (Exception e) {
                                // no error handling
                            }
                        }
                        this.getLogger().debug("..ftp server reply [logout]: " + ftpClient.getReplyString());
                        try {
                            ftpClient.disconnect();
                        } catch (Exception e) {
                            // no error handling
                        }
                    }
                }
                if (flgJumpTransferDefined) {
                    rc = doPostCommands(rc, filelist);
                }
            }
        } catch (Exception e) {
            this.getLogger().warn("ftp processing failed: " + e.getMessage());
            if (!sosString.parseToString(getLogger().getWarning()).isEmpty() || !sosString.parseToString(getLogger().getError()).isEmpty()) {
                getArguments().put("last_error", getLogger().getWarning() + " " + getLogger().getError());
            }
            writeHistory(localFile != null ? localFile.getAbsolutePath() : "", "");
            return false;
        }
    }

    private void getParameter() throws Exception {
        try {
            if (!sosString.parseToString(arguments.get("keep_connection")).isEmpty()) {
                String keep = sosString.parseToString(arguments.get("keep_connection"));
                if ("true".equalsIgnoreCase(keep) || "1".equalsIgnoreCase(keep)) {
                    keepConnection = true;
                } else {
                    keepConnection = false;
                }
            } else {
                keepConnection = false;
            }
            if (!sosString.parseToString(arguments.get("protocol")).isEmpty()) {
                protocol = sosString.parseToString(arguments.get("protocol"));
            }
            port = 21;
            if ("sftp".equalsIgnoreCase(protocol)) {
                sshBasedProtocol = true;
                port = 22;
            }
            retrieveCommonParameters();
            host = getParam("host", "");
            if (!sosString.parseToString(arguments.get("port")).isEmpty()) {
                port = Integer.parseInt(sosString.parseToString(arguments.get("port")));
            }
            if (!sosString.parseToString(arguments.get("posixPermissions")).isEmpty()) {
                intPosixPermissions = Integer.parseInt(sosString.parseToString(arguments.get("posixPermissions")));
            } else {
                intPosixPermissions = 0;
            }
            if (!sosString.parseToString(arguments.get("user")).isEmpty()) {
                user = sosString.parseToString(arguments.get("user"));
            }
            if (!sosString.parseToString(arguments.get("password")).isEmpty()) {
                password = sosString.parseToString(arguments.get("password"));
            }
            if (!sosString.parseToString(arguments.get("account")).isEmpty()) {
                account = sosString.parseToString(arguments.get("account"));
            }
            if (keepConnection) {
                sameConnection = ftpClient != null && ftpClient.isConnected() && lastHost.equalsIgnoreCase(host) && lastPort == port
                        && lastUser.equalsIgnoreCase(user) && lastAccount.equalsIgnoreCase(account);
            }
            if (ftpClient != null && ftpClient.isConnected() && !sameConnection) {
                try {
                    if (isLoggedIn) {
                        ftpClient.logout();
                    }
                    ftpClient.disconnect();
                } catch (Exception e) {
                }
            }
            lastHost = host;
            lastPort = port;
            lastUser = user;
            lastAccount = account;
            if (!sosString.parseToString(arguments.get("transfer_mode")).isEmpty()) {
                transferMode = sosString.parseToString(arguments.get("transfer_mode"));
            }
            if (!sosString.parseToString(arguments.get("passive_mode")).isEmpty()) {
                passiveMode = sosString.parseToBoolean(arguments.get("passive_mode"));
            }
            if (!sosString.parseToString(arguments.get("remote_dir")).isEmpty()) {
                remoteDir = sosString.parseToString(arguments.get("remote_dir"));
            }
            if (!sosString.parseToString(arguments.get("local_dir")).isEmpty()) {
                localDir = sosString.parseToString(arguments.get("local_dir"));
            }
            if (!sosString.parseToString(arguments.get("file_spec")).isEmpty()) {
                fileSpec = sosString.parseToString(arguments.get("file_spec"));
            }
            if (!sosString.parseToString(arguments.get("atomic_suffix")).isEmpty()) {
                atomicSuffix = sosString.parseToString(arguments.get("atomic_suffix"));
            }
            if (!sosString.parseToString(arguments.get("check_size")).isEmpty()) {
                checkSize = !"1".equals(sosString.parseToString(arguments.get("check_size")))
                        && !"true".equalsIgnoreCase(sosString.parseToString(arguments.get("check_size")))
                        && !"yes".equalsIgnoreCase(sosString.parseToString(arguments.get("check_size"))) ? false : true;
            }
            if (!sosString.parseToString(arguments.get("overwrite_files")).isEmpty()) {
                overwriteFiles = !"1".equals(sosString.parseToString(arguments.get("overwrite_files")))
                        && !"true".equalsIgnoreCase(sosString.parseToString(arguments.get("overwrite_files")))
                        && !"yes".equalsIgnoreCase(sosString.parseToString(arguments.get("overwrite_files"))) ? false : true;
            }
            if (!sosString.parseToString(arguments.get("append_files")).isEmpty()) {
                appendFiles = "1".equals(sosString.parseToString(arguments.get("append_files")))
                        || "true".equalsIgnoreCase(sosString.parseToString(arguments.get("append_files")))
                        || "yes".equalsIgnoreCase(sosString.parseToString(arguments.get("append_files"))) ? true : false;
            }
            if (!sosString.parseToString(arguments.get("remove_files")).isEmpty()) {
                removeFiles = sosString.parseToBoolean(arguments.get("remove_files"));
            }
            if (!sosString.parseToString(arguments, "skip_transfer").isEmpty()) {
                skipTransfer = sosString.parseToBoolean(arguments.get("skip_transfer"));
            }
            if (!sosString.parseToString(arguments.get(PARAMETER_MAKE_DIRS)).isEmpty()) {
                flgMakeDirs = "1".equals(sosString.parseToString(arguments.get(PARAMETER_MAKE_DIRS)))
                        || "true".equalsIgnoreCase(sosString.parseToString(arguments.get(PARAMETER_MAKE_DIRS)))
                        || "yes".equalsIgnoreCase(sosString.parseToString(arguments.get(PARAMETER_MAKE_DIRS))) ? true : false;
            }
            if (!sosString.parseToString(arguments.get(conParamFORCE_FILES)).isEmpty()) {
                forceFiles = !"1".equals(sosString.parseToString(arguments.get(conParamFORCE_FILES)))
                        && !"true".equalsIgnoreCase(sosString.parseToString(arguments.get(conParamFORCE_FILES)))
                        && !"yes".equalsIgnoreCase(sosString.parseToString(arguments.get(conParamFORCE_FILES))) ? false : true;
            } else {
                forceFiles = true;
            }
            compressFiles = getBooleanParam("compress_files", "false");
            compressedFileExtension = getParam("compressed_file_extension", ".gz");
            if (!sosString.parseToString(arguments.get("file_zero_byte_transfer")).isEmpty()) {
                if ("1".equals(sosString.parseToString(arguments.get("file_zero_byte_transfer")))
                        || "true".equalsIgnoreCase(sosString.parseToString(arguments.get("file_zero_byte_transfer")))
                        || "yes".equalsIgnoreCase(sosString.parseToString(arguments.get("file_zero_byte_transfer")))) {
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
            if (arguments.get("replacement") != null) {
                replacement = sosString.parseToString(arguments.get("replacement"));
            }
            if (!sosString.parseToString(arguments.get("replacing")).isEmpty()) {
                replacing = sosString.parseToString(arguments.get("replacing"));
            } else if (!sosString.parseToString(arguments.get("replacement")).isEmpty()) {
                replacing = "";
            }
            if (!sosString.parseToString(arguments.get(RECURSIVE)).isEmpty()) {
                String sRecursive = "";
                sRecursive = sosString.parseToString(arguments.get(RECURSIVE));
                recursive = sosString.parseToBoolean(sRecursive);
            }
            if (!sosString.parseToString(arguments.get("poll_timeout")).isEmpty()) {
                pollTimeout = Integer.parseInt(sosString.parseToString(arguments.get("poll_timeout")));
            }
            if (!sosString.parseToString(arguments.get("poll_interval")).isEmpty()) {
                pollIntervall = Integer.parseInt(sosString.parseToString(arguments.get("poll_interval")));
            }
            checkParameter();
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
                        RaiseException("illegal non-numeric value for parameter [ssh_proxy_port]: " + sosString.parseToString(arguments.get("ssh_proxy_port")));
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
                if (!sosString.parseToString(arguments.get("ssh_auth_method")).isEmpty()) {
                    if ("publickey".equalsIgnoreCase(sosString.parseToString(arguments.get("ssh_auth_method")))
                            || "password".equalsIgnoreCase(sosString.parseToString(arguments.get("ssh_auth_method")))) {
                        authenticationMethod = sosString.parseToString(arguments.get("ssh_auth_method"));
                    } else {
                        RaiseException("invalid authentication method [publickey, password] specified: "
                                + sosString.parseToString(arguments.get("ssh_auth_method")));
                    }
                } else {
                    authenticationMethod = "publickey";
                }
                if (!sosString.parseToString(arguments.get("ssh_auth_file")).isEmpty()) {
                    authenticationFilename = sosString.parseToString(arguments.get("ssh_auth_file"));
                } else {
                    if ("publickey".equalsIgnoreCase(authenticationMethod)) {
                        RaiseException("no authentication filename was specified as parameter [ssh_auth_file]");
                    }
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
                            + sosString.parseToString(arguments.get("http_proxy_port")));
                }
            }
            if (!sosString.parseToString(arguments.get("transactional")).isEmpty()) {
                transActional = sosString.parseToBoolean(arguments.get("transactional"));
            }
            testmode = sosString.parseToBoolean(arguments.get("testmode"));
            if (!sosString.parseToString(arguments.get("mail_smtp")).isEmpty()) {
                mailSMTP = sosString.parseToString(arguments.get("mail_smtp"));
            }
            if (!sosString.parseToString(arguments.get("mail_from")).isEmpty()) {
                mailFrom = sosString.parseToString(arguments.get("mail_from"));
            }
            if (!sosString.parseToString(arguments.get("mail_queue_dir")).isEmpty()) {
                mailQueueDir = sosString.parseToString(arguments.get("mail_queue_dir"));
            }
        } catch (Exception e) {
            RaiseException("error while processing parameters in " + sos.util.SOSClassUtil.getMethodName() + ". cause: " + e.toString());
        }
    }

    private void checkParameter() throws Exception {
        if (replacing != null && replacement == null) {
            RaiseException("parameter is missing for specified parameter [replacing=" + replacing + "]: [replacement]");
        }
        if (appendFiles && compressFiles) {
            RaiseException("unsupported parameter settings [append_files, compress_files]: cannot append to compressed files");
        }
        if (!sosString.parseToString(arguments.get("file_path")).isEmpty()) {
            filePath = sosString.parseToString(arguments.get("file_path"));
            isFilePath = true;
        }
    }

    private void getFileNotificationParameter() throws Exception {
        try {
            if (fileNotificationTo != null && !fileNotificationTo.isEmpty()) {
                if (fileNotificationSubject == null || fileNotificationSubject.isEmpty()) {
                    fileNotificationSubject = "[debug1] SOSFTPCommand";
                }
                if (fileNotificationBody == null || fileNotificationBody.isEmpty()) {
                    fileNotificationBody = "The following files have been sent:\n\n";
                }
            }
            if (fileZeroByteNotificationTo != null && !fileZeroByteNotificationTo.isEmpty()) {
                if (fileZeroByteNotificationSubject == null || fileZeroByteNotificationSubject.isEmpty()) {
                    fileZeroByteNotificationSubject = "[warning] SOSFTPCommand";
                }
                if (fileZeroByteNotificationBody == null || fileZeroByteNotificationBody.isEmpty()) {
                    fileZeroByteNotificationBody = "The following files have not been sent and were removed due to zero byte constraints:\n\n";
                }
            }
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
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
            try {
                this.getLogger().debug(".. job parameter [http_proxy_host]                : " + proxyHost);
                this.getLogger().debug(".. job parameter [http_proxy_port]                : " + proxyPort);
            } catch (Exception e) {
            }
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " could not read parameters, cause: " + e);
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
            RaiseException("..error in setAlternativeParameter, cause: " + e);
        }
    }

    private void makeDirs() throws Exception {
        try {
            boolean cd = true;
            if (flgMakeDirs) {
                if (ftpClient.changeWorkingDirectory(remoteDir)) {
                    this.getLogger().debug("..ftp server reply [directory exists] [" + remoteDir + "]: " + ftpClient.getReplyString());
                    cd = true;
                } else {
                    boolean ok = ftpClient.mkdir(remoteDir, intPosixPermissions);
                    if (!ok) {
                        RaiseException("..error occurred creating directory [" + remoteDir + "]: " + ftpClient.getReplyString());
                    } else {
                        this.getLogger().debug("..ftp server reply [mkdir] [" + remoteDir + "]: " + ftpClient.getReplyString());
                        cd = ftpClient.changeWorkingDirectory(remoteDir);
                    }
                }
            } else if (remoteDir != null && !remoteDir.isEmpty()) {
                cd = ftpClient.changeWorkingDirectory(remoteDir);
            }
            if (!cd && !sosString.parseToString(alternativeRemoteDir).isEmpty()) {
                this.getLogger().debug("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
                this.getLogger().debug1("..try with alternative parameter [remoteDir=" + alternativeRemoteDir + "]");
                cd = ftpClient.changeWorkingDirectory(alternativeRemoteDir);
                remoteDir = alternativeRemoteDir;
            }
            if (!cd) {
                RaiseException("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
            } else {
                this.getLogger().debug("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
            }
        } catch (Exception e) {
            RaiseException("..error in Make_Dirs, cause: " + e.getMessage(), e);
        }
    }

    private void foundFiles(final Vector filelist_) throws Exception {
        try {
            getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
            Iterator iterator = filelist_.iterator();
            while (iterator.hasNext()) {
                Object fn = iterator.next();
                getLogger().debug1(fn + " found");
            }
            if (banner) {
                getLogger().info(filelist_.size() + " files found.");
            } else {
                getLogger().debug1(filelist_.size() + " files found.");
            }
            filelist = filelist_;
            this.getLogger().debug("..ftp server reply [listNames] " + ftpClient.getReplyString());
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
        }
    }

    private void sendFiles(final Vector filelist) throws Exception {
        try {
            getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
            Iterator iterator = filelist.iterator();
            transActionalLocalFiles = new ArrayList();
            while (iterator.hasNext()) {
                Object fn = iterator.next();
                long bytesSend = 0;
                setParam(FILE_SIZE, "");
                if (!polling(fn.toString())) {
                    continue;
                }
                localFile = new File(fn.toString());
                File subParent = null;
                String subPath = "";
                if (!localFile.exists()) {
                    RaiseException(".. file [" + localFile + "] does not exist ");
                }
                if (recursive && !isFilePath) {
                    if (localFile.getParent() != null && localFile.getParentFile().isDirectory()) {
                        subPath = fn.toString().substring(localDir.length());
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
                                } else {
                                    this.getLogger().debug(
                                            "..ftp server reply [mkdir sub-directory] [" + remoteDir + "/" + subPath + "]: " + ftpClient.getReplyString());
                                }
                            }
                        }
                    }
                }
                if (localFile == null) {
                    continue;
                }
                if (!zeroByteFiles && localFile.length() == 0) {
                    continue;
                }
                File transferFile = null;
                String transferFilename = null;
                File sourceFile = null;
                if (compressFiles) {
                    transferFile = new File(localFile.getAbsolutePath() + compressedFileExtension);
                    sourceFile = File.createTempFile("sos", ".gz");
                } else {
                    transferFile = new File(localFile.getAbsolutePath());
                    sourceFile = new File(localFile.getAbsolutePath());
                }
                transferFilename = transferFile.getName();
                if (replacement != null && replacing != null && !replacing.isEmpty()) {
                    String currTransferFilename = SOSFileOperations.getReplacementFilename(transferFile.getName(), replacing, replacement);
                    String currPath = transferFile.getParent() != null && !transferFile.getParent().isEmpty() ? normalized(transferFile.getParent()) : "";
                    this.getLogger().debug1("source filename [" + transferFile.getName() + "] is renamed to: " + currTransferFilename);
                    transferFile = new File(currPath + currTransferFilename);
                    transferFilename = transferFile.getName();
                }
                if (subParent != null && recursive) {
                    transferFile = new File((subParent != null ? subParent.getName() + "/" : "") + transferFile.getName());
                    transferFilename = (subParent != null ? subPath + "/" : "") + transferFile.getName();
                }
                if (!appendFiles && atomicSuffix != null && !atomicSuffix.isEmpty()) {
                    if (!overwriteFiles) {
                        Vector ftpFiles = ftpClient.nList(transferFile.getName());
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
                            } else {
                                this.getLogger().debug("..ftp server reply [delete] [" + transferFilename + "]: " + ftpClient.getReplyString());
                            }
                        }
                        if (transActional) {
                            ftpClient.nList(".");
                        }
                    }
                    this.getLogger().debug1("renaming file: " + transferFilename);
                    if (!transActional) {
                        ftpClient.rename(transferFilename + atomicSuffix, transferFilename);
                    }
                } else {
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
                        if (bWhileContinue) {
                            continue;
                        }
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
                        } else {
                            RaiseException("append is not implemented for protocol " + protocol);
                        }
                    } else {
                        if (compressFiles) {
                            this.getLogger().info("sending file : " + transferFile.getAbsolutePath() + " from temporary file " + sourceFile.getAbsolutePath());
                        } else {
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
                    } else {
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
                    } else {
                        this.getLogger().debug3("delete compressed file: " + sourceFile.getAbsolutePath());
                    }
                }
                if (transActional) {
                    transActionalLocalFiles.add(localFile);
                } else if (removeFiles) {
                    if (!localFile.delete()) {
                        RaiseException(".. error occurred, could not remove local file: " + localFile.getAbsolutePath());
                    } else {
                        this.getLogger().debug1("removing file: " + localFile.getAbsolutePath());
                    }
                }
                fileNotificationBody += transferFile.getName() + "\n";
                count++;
            }
        } catch (Exception e) {
            throw new JobSchedulerException("error in  " + sos.util.SOSClassUtil.getMethodName() + " could not send files, cause: " + e.getMessage(), e);
        }
    }

    private boolean doPostCommands(boolean rc, final Vector filelist) throws Exception {
        try {
            getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
            setParam("xx_make_temp_directory_xx", "ok");
            if (!getLogger().hasWarnings()) {
                if (!sosString.parseToString(postCommands).isEmpty()) {
                    this.setCommands(postCommands.split(getCommandDelimiter()));
                    if (!execute()) {
                        RaiseException("error occurred processing command:" + normalizedPassword(postCommands));
                    }
                }
            }
            String com = sosString.parseToString(arguments.get("jump_command")) + " -operation=remove_temp_directory -input=\"" + tempJumpRemoteDir + "\"";
            this.setCommands(com.split(getCommandDelimiter()));
            if (!execute()) {
                arguments.remove("xx_make_temp_directory_xx");
                getLogger().warn("error occurred processing command:" + tempJumpRemoteDir);
            }
            arguments.remove("xx_make_temp_directory_xx");
            if (rc) {
                if (!transActional && sosString.parseToBoolean(arguments.get("remove_after_jump_transfer"))) {
                    for (int i = 0; i < filelist.size(); i++) {
                        File f = new File(sosString.parseToString(filelist.get(i)));
                        if (!f.delete()) {
                            RaiseException("..error occurred, could not remove local file: " + f.getAbsolutePath());
                        } else {
                            this.getLogger().debug1("removing localfile: " + f.getAbsolutePath());
                        }
                    }
                    arguments.remove("remove_after_jump_transfer");
                } else {
                    rc = false;
                }
            }
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
        }
        return rc;
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

    private Vector<String> getFilePaths() throws Exception {
        try {
            Vector<String> filelist1 = new Vector<String>();
            String[] split = filePath.split(";");
            String strLocalDir = normalized(localDir);
            int intLocalDirLen = strLocalDir.length();
            for (String element : split) {
                String strT = element;
                if (strT != null && !strT.isEmpty()) {
                    if (intLocalDirLen > 0 && !sosString.parseToString(localDir).equalsIgnoreCase(defaultLocalDir)) {
                        if (isAPathName(strT) == false) {
                            strT = strLocalDir + strT;
                        }
                    }
                    filelist1.add(strT);
                }
            }
            fileSpec = ".*";
            return filelist1;
        } catch (Exception e) {
            throw new JobSchedulerException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
        }
    }

    private boolean printState(boolean rc) throws Exception {
        try {
            switch (count) {
            case 0:
                if (zeroByteCount > 0 && zeroByteFilesRelaxed) {
                    this.getLogger().debug1("no matching files found, " + zeroByteCount + " zero byte files skipped");
                    state = "no matching files found, " + zeroByteCount + " zero byte files skipped";
                } else if (zeroByteCount > 0 && zeroByteFilesStrict) {
                    RaiseException("zero byte file(s) found");
                } else if (forceFiles) {
                    RaiseException("no matching files found");
                } else {
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
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
        }
        return rc;
    }

    @Override
    public boolean install() throws Exception {
        getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
        String classPathBase = "";
        String installFiles = null;
        try {
            mergeSettings();
            if (!sosString.parseToString(arguments.get("classpath_base")).isEmpty()) {
                classPathBase = sosString.parseToString(arguments.get("classpath_base"));
                getLogger().debug1(".. parameter [classpath_base]: " + classPathBase);
            }
            if ("install_doc".equals(sosString.parseToString(arguments, "operation"))) {
                arguments.setProperty("remote_dir", normalized(classPathBase) + "doc");
            } else {
                arguments.setProperty("remote_dir", classPathBase);
            }
            installFiles = getInstallFiles(classPathBase);
            setParam("file_path", installFiles);
            if (banner) {
                getLogger().info(getBanner(true));
            } else {
                getLogger().debug1(getBanner(true));
            }
            if (!send()) {
                getLogger().warn("Could not send install files.");
                return false;
            }
            if ("sftp".equalsIgnoreCase(sosString.parseToString(arguments.get("protocol"))) && !flgJumpTransferDefined) {
                Iterator it = arguments.keySet().iterator();
                Properties jumpProp = new Properties();
                while (it.hasNext()) {
                    Object key = sosString.parseToString(it.next());
                    if (!arguments.contains("jump_" + key)) {
                        jumpProp.put("jump_" + key, sosString.parseToString(arguments.get(key)));
                    }
                }
                arguments.putAll(jumpProp);
                if (!"publickey".equalsIgnoreCase(sosString.parseToString(arguments, "jump_ssh_auth_method"))) {
                    setParam("jump_ssh_auth_method", "password");
                }
                try {
                    boolean isNoWindow = true;
                    try {
                        isNoWindow = remoteIsWindowsShell(true);
                    } catch (Exception r) {
                        getLogger().info("Failed to check if remote system is Linux to change the File Attibutes Properties");
                    }
                    if (!isNoWindow) {
                        setParam("xx_make_temp_directory_xx", "ok");
                        String commands = "chmod u+x " + classPathBase + "/sosftp.sh";
                        if (!"publickey".equalsIgnoreCase(sosString.parseToString(arguments, "jump_ssh_auth_method"))) {
                            setParam("jump_ssh_auth_method", "password");
                        }
                        this.setCommands(commands.split(getCommandDelimiter()));
                        String sh = installFiles.split(";")[installFiles.split(";").length - 1];
                        setParam("jump_command_script_file", sh);
                        flgJumpTransferDefined = false;
                        if (!execute()) {
                            RaiseException("error occurred processing command: " + normalizedPassword(commands));
                        }
                        arguments.remove("xx_make_temp_directory_xx");
                    }
                } catch (Exception e) {
                    getLogger().info("error occurred processing command: " + commands + ", cause: " + e.toString());
                }
            }
            return true;
        } catch (Exception e) {
            this.getLogger().warn("install failed: " + e.getMessage());
            return false;
        } finally {
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

    private String getInstallFiles(final String classPathBase) throws Exception {
        String filePath1 = "";
        BufferedWriter out = null;
        BufferedWriter out2 = null;
        String shellscript = "";
        String cmd = "";
        File sosftpSH = null;
        File sosftpCMD = null;
        String classPath = "INSTALL_PATH=";
        try {
            String localDir1 = "";
            if (!sosString.parseToString(arguments, "local_dir").isEmpty()) {
                localDir1 = sosString.parseToString(arguments, "local_dir") + "/";
            }
            if (sosString.parseToString(arguments, "operation").equals("install_doc")) {
                filePath1 = installDocPaths.replaceAll("%\\{local_dir\\}/", localDir1);
            } else {
                installpathsWithRevNr = installpathsWithRevNr.replaceAll("%\\{local_dir\\}/", localDir1);
                String[] split = installpathsWithRevNr.split(";");
                for (String element : split) {
                    Vector v = SOSFile.getFilelist(localDir1, new File(element).getName(), Pattern.CASE_INSENSITIVE);
                    if (!v.isEmpty()) {
                        filePath1 = filePath1.concat(sosString.parseToString(v.get(v.size() - 1))).concat(";");
                    } else {
                        RaiseException("missing library " + element + "to install sosftp");
                    }
                }
                filePath1 = filePath1 + installpaths.replaceAll("%\\{local_dir\\}/", localDir1);
                String path = normalized(System.getProperty("java.io.tmpdir"));
                File fsh = new File(localDir1, "sosftp.sh");
                if (fsh.exists()) {
                    shellscript = sos.util.SOSFile.readFile(fsh);
                    int pos1 = shellscript.indexOf(classPath);
                    int pos2 = shellscript.indexOf("\n", pos1);
                    if (pos1 != -1 && pos2 != -1) {
                        shellscript = shellscript.substring(0, pos1) + classPath + classPathBase + shellscript.substring(pos2);
                    }
                    getLogger().debug("shell script: " + shellscript);
                    String currShellscript = shellscript;
                    sosftpSH = new File(path, "sosftp.sh");
                    sosftpSH.deleteOnExit();
                    out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sosftpSH)));
                    out.write(currShellscript);
                    out.close();
                } else {
                    getLogger().info("there is no Shell Script to install: " + new File(localDir1, "sosftp.sh"));
                }
                File fcmd = new File(localDir1, "sosftp.cmd");
                if (fcmd.exists()) {
                    cmd = sos.util.SOSFile.readFile(fcmd);
                    int pos1 = cmd.indexOf(classPath);
                    int pos2 = cmd.indexOf("\n", pos1);
                    if (pos1 != -1 && pos2 != -1) {
                        cmd = cmd.substring(0, pos1) + classPath + classPathBase + cmd.substring(pos2);
                    }
                    getLogger().debug("cmd script: " + cmd);
                    String currCMDFile = cmd.replaceAll("%\\{classpath_base\\}", classPathBase);
                    sosftpCMD = new File(path, "sosftp.cmd");
                    sosftpCMD.deleteOnExit();
                    out2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sosftpCMD)));
                    out2.write(currCMDFile);
                    out2.close();
                } else {
                    getLogger().info("there is no cmd Script to install: " + new File(localDir1 + "sosftp.cmd"));
                }
                if (sosftpCMD != null) {
                    filePath1 = filePath1 + ";" + sosftpCMD.getCanonicalPath();
                }
                if (sosftpSH != null) {
                    filePath1 = filePath1 + ";" + sosftpSH.getCanonicalPath();
                }
            }
            arguments.remove("local_dir");
        } catch (Exception e) {
            RaiseException("error while get instal files, cause: " + e.toString());
        } finally {
            if (out != null) {
                out.close();
            }
            if (out2 != null) {
                out2.close();
            }
        }
        return filePath1;
    }

    public void getDeleteTransferFiles() throws Exception {
        if (transActional && (removeFiles || sosString.parseToBoolean(arguments.get("remove_after_jump_transfer")))) {
            if (transActionalLocalFiles == null) {
                return;
            }
            getLogger().debug(".. mark transactional files for removal: " + transActionalLocalFiles);
            Properties p = new Properties();
            p.put("operation", "delete_local_files");
            p.put("files", transActionalLocalFiles);
            listOfSuccessTransfer.add(p);
        }
    }

    public void getRemoveTransferFiles() throws Exception {
        getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
        if (transActional && !sosString.parseToString(transActionalRemoteFiles).isEmpty()) {
            if (flgJumpTransferDefined) {
                String com = getCommands()[0];
                String split[] = com.split(" -");
                String newarg = split[0];
                Properties p = (Properties) arguments.clone();
                for (int i = 1; i < split.length; i++) {
                    if (split[i].indexOf("skip_transfer") == -1 && split[i].indexOf("replacement") == -1 && split[i].indexOf("file_path") == -1
                            && split[i].indexOf("operation") == -1 && split[i].indexOf("atomic_suffix") == -1 && split[i].indexOf("remove_files") == -1
                            && split[i].indexOf("transactional") == -1) {
                        newarg = newarg + " -" + split[i];
                    }
                }
                newarg = newarg + " " + transActionalRemoteFiles.replaceAll(";", " ")
                        + " -skip_transfer=\"yes\" -transactional=\"false\" -operation=\"remove\"" + " -remove_files=\"yes\"";
                p.put("command", newarg);
                p.put("operation", "execute");
                p.put("xx_make_temp_directory_success_transfer_xx", "ok");
                listOfErrorTransfer.add(p);
            } else {
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
                p.put("file_path", transActionalRemoteFiles);
                p.put("skip_transfer", "yes");
                p.put("remove_files", "yes");
                p.put("transactional", "no");
                listOfErrorTransfer.add(p);
            }
        }
    }

    public void getRenameAtomicSuffixTransferFiles() throws Exception {
        try {
            getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
            if (transActional) {
                if (sosString.parseToString(transActionalRemoteFiles).isEmpty()) {
                    return;
                }
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
                for (int i = 0; i < rem.size(); i++) {
                    p.remove(rem.get(i));
                }
                if (flgJumpTransferDefined) {
                    String com = getCommands()[0];
                    String split[] = com.split(" -");
                    String newarg = split[0];
                    for (int i = 1; i < split.length; i++) {
                        if (split[i].indexOf("skip_transfer") == -1 && split[i].indexOf("replacement") == -1 && split[i].indexOf("file_path") == -1
                                && split[i].indexOf("operation") == -1 && split[i].indexOf("atomic_suffix") == -1 && split[i].indexOf("remove_files") == -1
                                && split[i].indexOf("transactional") == -1) {
                            newarg = newarg + " -" + split[i];
                        }
                    }
                    newarg = newarg + " " + transActionalRemoteFiles.replaceAll(";", " ") + " -replacement=\"" + atomicSuffix + "\""
                            + " -skip_transfer=\"yes\" -replacing=\"\" -transactional=\"false\" -operation=\"send\""
                            + " -atomic_suffix=\"\" -remove_files=\"no\"";
                    p.put("command", newarg);
                    p.put("operation", "execute");
                    p.put("xx_make_temp_directory_success_transfer_xx", "ok");
                } else {
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
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
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
                if (!transferFilename.trim().isEmpty()) {
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
                            } else {
                                this.getLogger().debug(
                                        "..ftp server reply [rename] " + transferFilename + " in " + rTransferFilename + ": " + ftpClient.getReplyString());
                            }
                        }
                    } else {
                        RaiseException("..error occurred renaming tranactional file [" + transferFilename + "]: " + ftpClient.getReplyString());
                    }
                }
            }
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
        }
    }

    private boolean polling(final String fileName) throws Exception {
        String lastmd5file = "";
        String message = "";
        try {
            if (pollTimeout > 0 && !sosString.parseToString(fileName).isEmpty()) {
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
                    } else {
                        break;
                    }
                }
                if (!found) {
                    message = "During triggering for " + pollTimeout + " minutes the file " + message + " has been changed repeatedly";
                    if (forceFiles) {
                        getLogger().warn(message);
                        RaiseException(message);
                    } else {
                        getLogger().info(message);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " error while polling, cause: " + e.getMessage(), e);
        }
        return true;
    }

    public int getOfTransferFilesCount() {
        return count;
    }

    public int getZeroByteCount() {
        return zeroByteCount;
    }

    public String getState() {
        return state;
    }

    private boolean sendSimpleTransfer() throws Exception {
        boolean cd = true;
        String localDir = sosString.parseToString(arguments.get("local_dir"));
        try {
            if (sosString.parseToBoolean(sosString.parseToString(arguments.get(PARAMETER_MAKE_DIRS)))) {
                if (ftpClient.changeWorkingDirectory(remoteDir)) {
                    this.getLogger().debug("..ftp server reply [directory exists] [" + remoteDir + "]: " + ftpClient.getReplyString());
                    cd = true;
                } else {
                    boolean ok = ftpClient.mkdir(remoteDir, intPosixPermissions);
                    if (!ok) {
                        RaiseException("..error occurred creating directory [" + remoteDir + "]: " + ftpClient.getReplyString());
                    } else {
                        this.getLogger().debug("..ftp server reply [mkdir] [" + remoteDir + "]: " + ftpClient.getReplyString());
                        cd = ftpClient.changeWorkingDirectory(remoteDir);
                    }
                }
            } else if (remoteDir != null && !remoteDir.isEmpty()) {
                cd = ftpClient.changeWorkingDirectory(remoteDir);
            }
            String alternativeRemoteDir = sosString.parseToString(arguments.get("alternative_remote_dir"));
            if (!cd && !sosString.parseToString(alternativeRemoteDir).isEmpty()) {
                this.getLogger().debug("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
                this.getLogger().info("..try with alternative parameter [remoteDir=" + alternativeRemoteDir + "]");
                cd = ftpClient.changeWorkingDirectory(alternativeRemoteDir);
                remoteDir = alternativeRemoteDir;
            }
            if (!cd) {
                RaiseException("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
            } else {
                this.getLogger().debug("..ftp server reply [cd] [remoteDir=" + remoteDir + "]: " + ftpClient.getReplyString());
            }
            getLogger().debug9("..filepath: " + filePath);
            if (!sosString.parseToString(remoteDir).isEmpty() && "./".equals(sosString.parseToString(remoteDir))) {
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
            filelist = new Vector();
            filelist.add(filePath);
            setParam("successful_transfers", "1");
        } catch (Exception e) {
            setParam("failed_transfers", "1");
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " error while transfer simple File, cause: " + e.getMessage(), e);
        }
        return true;
    }

    private boolean isSimpleTransfer() throws Exception {
        boolean simpleTransfer = sosString.parseToBoolean(sosString.parseToString(arguments.get("simple_transfer")));
        if (simpleTransfer && sosString.parseToString(arguments.get("file_path")).isEmpty()) {
            RaiseException("job parameter is missing for specified parameter [ftp_simple_transfer]: [ftp_file_path]");
        }
        if (simpleTransfer) {
            getLogger().info(
                    "parameter [ftp_file_spec, replacement, replacing, ftp_force_files, ftp_atomic_suffix, "
                            + "ftp_recursive, ftp_compress, ftp_remove_files, ftp_make_dirs] "
                            + "will be ignored because parameter ftp_simple_transfer has been specified.");
        }
        return simpleTransfer;
    }

}
