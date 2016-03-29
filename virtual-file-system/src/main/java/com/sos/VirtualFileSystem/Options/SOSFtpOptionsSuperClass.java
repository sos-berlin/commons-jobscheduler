package com.sos.VirtualFileSystem.Options;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobChain;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionArrayList;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBackgroundServiceTransferMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandScript;
import com.sos.JSHelper.Options.SOSOptionCommandScriptFile;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionEncoding;
import com.sos.JSHelper.Options.SOSOptionFileName;
import com.sos.JSHelper.Options.SOSOptionFileSize;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionIniFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJSTransferMethod.enuJSTransferModes;
import com.sos.JSHelper.Options.SOSOptionJadeOperation;
import com.sos.JSHelper.Options.SOSOptionJobChainNode;
import com.sos.JSHelper.Options.SOSOptionOutFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPlatform;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProcessID;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionRelOp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.Options.SOSOptionZeroByteTransfer;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSFtpOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;

@JSOptionClass(name = "SOSFtpOptionsSuperClass", description = "SOSFtpOptionsSuperClass")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSFtpOptionsSuperClass extends JSOptionsClass implements ISOSConnectionOptions, ISOSAuthenticationOptions, ISOSFtpOptions {

    protected Messages objMsg = new Messages(this.getClass().getAnnotation(I18NResourceBundle.class).baseName());
    private static final long serialVersionUID = -4445655877481869778L;
    private final String conClassName = "SOSFtpOptionsSuperClass";
    private final static Logger logger = Logger.getLogger(SOSFtpOptionsSuperClass.class);

    @JSOptionDefinition(name = "TFN_Post_Command", description = "Post commands executed after creating the final TargetFile", key = "TFN_Post_Command",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString TFN_Post_Command = new SOSOptionString(this, conClassName + ".TFN_Post_Command",
            "Post commands executed after creating the final TargetFileName", "", "", false);

    public SOSOptionString getTFN_Post_Command() {
        return TFN_Post_Command;
    }

    public SOSFtpOptionsSuperClass setTFN_Post_Command(final SOSOptionString pstrValue) {
        TFN_Post_Command = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "polling_wait_4_Source_Folder", description = "During polling", key = "polling_wait_4_Source_Folder", type = "SOSOptionBoolean",
            mandatory = true)
    public SOSOptionBoolean pollingWait4SourceFolder = new SOSOptionBoolean(this, conClassName + ".polling_wait_4_Source_Folder", "During polling", "false",
            "false", true);

    public String getPollingWait4SourceFolder() {
        return pollingWait4SourceFolder.Value();
    }

    public SOSFtpOptionsSuperClass setPollingWait4SourceFolder(final String pstrValue) {
        pollingWait4SourceFolder.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "include", description = "the include directive as an option", key = "include", type = "SOSOptionString", mandatory = false)
    public SOSOptionString include = new SOSOptionString(this, conClassName + ".include", "the include directive as an option", "", "", false);

    public String getinclude() {
        return include.Value();
    }

    public SOSFtpOptionsSuperClass setinclude(final String pstrValue) {
        include.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "use_filters", description = "Use filters for source and/or Targe", key = "use_filters", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean use_filters = new SOSOptionBoolean(this, conClassName + ".use_filters", "Use filters for source and/or Targe", "false", "false",
            false);

    public SOSOptionBoolean getuse_filters() {
        return use_filters;
    }

    public SOSFtpOptionsSuperClass setuse_filters(final SOSOptionBoolean pstrValue) {
        use_filters = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "is_fragment", description = "Mark an profile as a fragment", key = "is_fragment", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean isFragment = new SOSOptionBoolean(this, conClassName + ".is_fragment", "Mark an profile as a fragment", "false", "false", false);

    public String getis_fragment() {
        return isFragment.Value();
    }

    public SOSFtpOptionsSuperClass setis_fragment(final String pstrValue) {
        isFragment.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "reuse_connection", description = "reuse the current connections for all transfers", key = "reuse_connection",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean reuseConnection = new SOSOptionBoolean(this, conClassName + ".reuse_connection", "reuse the current connections for all transfers",
            "false", "false", false);

    public String getreuse_connection() {
        return reuseConnection.Value();
    }

    public SOSFtpOptionsSuperClass setreuse_connection(final String pstrValue) {
        reuseConnection.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "polling_server", description = "act as a polling server", key = "polling_server", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean PollingServer = new SOSOptionBoolean(this, conClassName + ".polling_server", "act as a polling server", "false", "false", false);

    public String getpolling_server() {
        return PollingServer.Value();
    }

    public SOSFtpOptionsSuperClass setpolling_server(final String pstrValue) {
        PollingServer.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "polling_end_at", description = "PollingServer should stop at the specified date/time", key = "polling_end_at",
            type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingEndAt = new SOSOptionTime(this, conClassName + ".polling_end_at", "Polling should stop at the specified date/time", "0", "0",
            false);

    public String getpolling_end_at() {
        return pollingEndAt.Value();
    }

    public SOSFtpOptionsSuperClass setpolling_end_at(final String pstrValue) {
        pollingEndAt.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "polling_server_poll_forever", description = "poll forever", key = "polling_server_poll_forever", type = "SOSOptionBoolean",
            mandatory = true)
    public SOSOptionBoolean PollingServerPollForever = new SOSOptionBoolean(this, conClassName + ".polling_server_poll_forever", "poll forever", "false",
            "false", true);

    public String getpolling_server_poll_forever() {
        return PollingServerPollForever.Value();
    }

    public SOSFtpOptionsSuperClass setpolling_server_poll_forever(final String pstrValue) {
        PollingServerPollForever.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "polling_server_duration", description = "How long the PollingServer should run", key = "polling_server_duration",
            type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingServerDuration = new SOSOptionTime(this, conClassName + ".polling_server_duration", "How long the PollingServer should run",
            "0", "0", false);

    public String getpolling_server_duration() {
        return pollingServerDuration.Value();
    }

    public SOSFtpOptionsSuperClass setpolling_server_duration(final String pstrValue) {
        pollingServerDuration.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Lazy_Connection_Mode", description = "Connect to Target as late as possible", key = "Lazy_Connection_Mode",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean LazyConnectionMode = new SOSOptionBoolean(this, conClassName + ".Lazy_Connection_Mode", "Connect to Target as late as possible",
            "false", "false", false);

    public String getLazy_Connection_Mode() {
        return LazyConnectionMode.Value();
    }

    public SOSFtpOptionsSuperClass setLazy_Connection_Mode(final String pstrValue) {
        LazyConnectionMode.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "platform", description = "platform on which the app is running", key = "platform", type = "SOSOptionString", mandatory = false)
    public SOSOptionPlatform platform = new SOSOptionPlatform(this, conClassName + ".platform", "platform on which the app is running", "", "", false);

    public String getplatform() {
        return platform.Value();
    }

    public SOSFtpOptionsSuperClass setplatform(final String pstrValue) {
        platform.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "mail_on_success", description = "Send a Mail in case of sucess", key = "mail_on_success", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean mail_on_success = new SOSOptionBoolean(this, conClassName + ".mail_on_success", "Send a Mail in case of sucess", "false", "false",
            false);

    public String getmail_on_success() {
        return mail_on_success.Value();
    }

    public SOSFtpOptionsSuperClass setmail_on_success(final String pstrValue) {
        mail_on_success.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "mail_on_error", description = "Send a Mail in case of error", key = "mail_on_error", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean mail_on_error = new SOSOptionBoolean(this, conClassName + ".mail_on_error", "Send a Mail in case of sucess", "false", "false",
            false);

    public String getmail_on_error() {
        return mail_on_error.Value();
    }

    public SOSFtpOptionsSuperClass setmail_on_error(final String pstrValue) {
        mail_on_error.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "mail_on_empty_files", description = "Send a Mail in case of empty files", key = "mail_on_empty_files",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_on_empty_files = new SOSOptionBoolean(this, conClassName + ".mail_on_empty_files", "Send a Mail in case of empty files",
            "false", "false", false);

    public String getmail_on_empty_files() {
        return mail_on_empty_files.Value();
    }

    public SOSFtpOptionsSuperClass setmail_on_empty_files(final String pstrValue) {
        mail_on_empty_files.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "title", description = "The Title for a section /profile", key = "title", type = "SOSOptionString", mandatory = false)
    public SOSOptionString title = new SOSOptionString(this, conClassName + ".title", "The Title for a section /profile", "", "", false);

    public String gettitle() {
        return title.Value();
    }

    public SOSFtpOptionsSuperClass settitle(final String pstrValue) {
        title.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "keep_modification_date", description = "Keep Modification Date of File", key = "keep_modification_date",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean keep_modification_date = new SOSOptionBoolean(this, conClassName + ".keep_modification_date", "Keep Modification Date of File",
            "false", "false", false);

    public SOSOptionBoolean KeepModificationDate = (SOSOptionBoolean) keep_modification_date.SetAlias("KeepModificationate");

    public String getkeep_modification_date() {
        return keep_modification_date.Value();
    }

    public SOSFtpOptionsSuperClass setkeep_modification_date(final String pstrValue) {
        keep_modification_date.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "cumulate_files", description = "cumulate (all) files into one file by append", key = "cumulate_files",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CumulateFiles = new SOSOptionBoolean(this, conClassName + ".cumulate_files", "cumulate (all) files into one file by append",
            "false", "false", false);

    public String getcumulate_files() {
        return CumulateFiles.Value();
    }

    public SOSFtpOptionsSuperClass setcumulate_files(final String pstrValue) {
        CumulateFiles.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "cumulative_filename", description = "Name of File into which all files hat to be cumulated", key = "cumulative_filename",
            type = "SOSOptionFileName", mandatory = true)
    public SOSOptionFileName CumulativeFileName = new SOSOptionFileName(this, conClassName + ".cumulative_filename",
            "Name of File into which all files hat to be cumulated", "", "", false);

    public String getcumulative_filename() {
        return CumulativeFileName.Value();
    }

    public SOSFtpOptionsSuperClass setcumulative_filename(final String pstrValue) {
        CumulativeFileName.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "cumulative_file_separator", description = "Text which has to beplaced between cumulated files",
            key = "cumulative_file_separator", type = "SOSOptionString", mandatory = false)
    public SOSOptionString CumulativeFileSeparator = new SOSOptionString(this, conClassName + ".cumulative_file_separator",
            "Text which has to beplaced between cumulated files", "", "", false);

    public String getcumulative_file_separator() {
        return CumulativeFileSeparator.Value();
    }

    public SOSFtpOptionsSuperClass setcumulative_file_separator(final String pstrValue) {
        CumulativeFileSeparator.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "cumulative_file_delete", description = "Delete cumulative file before starting transfer", key = "cumulative_file_delete",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CumulativeFileDelete = new SOSOptionBoolean(this, conClassName + ".cumulative_file_delete",
            "Delete cumulative file before starting transfer", "false", "false", false);

    public String getcumulative_file_delete() {
        return CumulativeFileDelete.Value();
    }

    public SOSFtpOptionsSuperClass setcumulative_file_delete(final String pstrValue) {
        CumulativeFileDelete.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Post_Command", description = "FTP-Command to be executed after transfer", key = "Post_Command", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionCommandString Post_Command = new SOSOptionCommandString(this, conClassName + ".Post_Command", "FTP-Command to be executed after transfer",
            "", "", false);

    public String getPost_Command() {
        return Post_Command.Value();
    }

    public SOSFtpOptionsSuperClass setPost_Command(final String pstrValue) {
        Post_Command.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Pre_Command", description = "FTP-Command to be execute before transfer", key = "Pre_Command", type = "SOSOptionString  ",
            mandatory = false)
    public SOSOptionCommandString Pre_Command = new SOSOptionCommandString(this, conClassName + ".Pre_Command", "", "", "", false);

    public String getPre_Command() {
        return Pre_Command.Value();
    }

    public SOSFtpOptionsSuperClass setPre_Command(final String pstrValue) {
        Pre_Command.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "CheckServerFeatures", description = "The available features of a ftp-server", key = "Check_Server_Features",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CheckServerFeatures = new SOSOptionBoolean(this, conClassName + ".Check_Server_Features", "The available features of a ftp-server",
            "false", "false", false);

    @Override
    public SOSOptionBoolean CheckServerFeatures() {
        return CheckServerFeatures;
    }

    public String getCheckServerFeatures() {
        return CheckServerFeatures.Value();
    }

    public SOSFtpOptionsSuperClass setCheckServerFeatures(final String pstrValue) {
        CheckServerFeatures.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "PollKeepConnection", description = "Keep connection while polling", key = "PollKeepConnection", type = "SOSOptionBoolean",
            mandatory = true)
    public SOSOptionBoolean PollKeepConnection = new SOSOptionBoolean(this, conClassName + ".PollKeepConnection", "Keep connection while polling", "false",
            "false", true);

    public String getPollKeepConnection() {
        return PollKeepConnection.Value();
    }

    public SOSFtpOptionsSuperClass setPollKeepConnection(final String pstrValue) {
        PollKeepConnection.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "FileNameEncoding", description = "Set the encoding-type of a file name", key = "FileNameEncoding", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString FileNameEncoding = new SOSOptionString(this, conClassName + ".FileNameEncoding", "Set the encoding-type of a file name", "",
            "ISO-8859-1", false);

    public String getFileNameEncoding() {
        return FileNameEncoding.Value();
    }

    public SOSFtpOptionsSuperClass setFileNameEncoding(final String pstrValue) {
        FileNameEncoding.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Strict_HostKey_Checking", description = "Check the hostkey against known hosts for SSH", key = "Strict_HostKey_Checking",
            type = "SOSOptionValueList", mandatory = false)
    public SOSOptionStringValueList StrictHostKeyChecking = new SOSOptionStringValueList(this, conClassName + ".strict_hostkey_checking",
            "Check the hostkey against known hosts for SSH", "ask;yes;no", "no", false);

    public String getStrict_HostKey_Checking() {
        return StrictHostKeyChecking.Value();
    }

    public SOSFtpOptionsSuperClass setStrict_HostKey_Checking(final String pstrValue) {
        StrictHostKeyChecking.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "ControlEncoding", description = "Specify the encoding-type, e.g. utf-8, used by the server", key = "ControlEncoding",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionEncoding ControlEncoding = new SOSOptionEncoding(this, conClassName + ".ControlEncoding",
            "Specify the encoding-type, e.g. utf-8, used by the server", "", "", false);

    public String getControlEncoding() {
        return ControlEncoding.Value();
    }

    public SOSFtpOptionsSuperClass setControlEncoding(final String pstrValue) {
        ControlEncoding.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "History_File_Append_Mode", description = "Specifies wether the History File has to be written in append mode",
            key = "History_File_Append_Mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean HistoryFileAppendMode = new SOSOptionBoolean(this, conClassName + ".History_File_Append_Mode",
            "Specifies wether the History File has to be written in append mode", "false", "false", false);

    public SOSOptionBoolean getHistoryFileAppendMode() {
        return HistoryFileAppendMode;
    }

    public SOSFtpOptionsSuperClass setHistoryFileAppendMode(final SOSOptionBoolean pstrValue) {
        HistoryFileAppendMode = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "HistoryEntries", description = "List of additional entries for the transfer history record.", key = "HistoryEntries",
            type = "SOSOptionArrayList", mandatory = false)
    public SOSOptionArrayList HistoryEntries = new SOSOptionArrayList(this, conClassName + ".HistoryEntries",
            "List of additional entries for the transfer history record.", "", "", false);

    public String getHistoryEntries() {
        return HistoryEntries.Value();
    }

    public SOSFtpOptionsSuperClass setHistoryEntries(final String pstrValue) {
        HistoryEntries.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "SendTransferHistory",
            description = "If this option is set to true, the transfer history will be sent to the background service.", key = "SendTransferHistory",
            type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean SendTransferHistory = new SOSOptionBoolean(this, conClassName + ".SendTransferHistory",
            "If this option is set to true, the transfer history will be sent to the background service.", "false", "false", false);

    public String getSendTransferHistory() {
        return SendTransferHistory.Value();
    }

    public SOSFtpOptionsSuperClass setSendTransferHistory(final String pstrValue) {
        SendTransferHistory.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Scheduler_Transfer_Method", description = "The technical method of how to communicate with the JobScheduler",
            key = "Scheduler_Transfer_Method", type = "SOSOptionJSTransferMethod", mandatory = true)
    public SOSOptionBackgroundServiceTransferMethod Scheduler_Transfer_Method = new SOSOptionBackgroundServiceTransferMethod(this, conClassName
            + ".Scheduler_Transfer_Method", "The technical method of how to communicate with the JobScheduler", enuJSTransferModes.udp.description,
            enuJSTransferModes.udp.description, true);

    public String getScheduler_Transfer_Method() {
        return Scheduler_Transfer_Method.Value();
    }

    public SOSFtpOptionsSuperClass setScheduler_Transfer_Method(final String pstrValue) {
        Scheduler_Transfer_Method.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "PreFtpCommands", description = "FTP commands, which has to be executed before the transfer started.", key = "PreFtpCommands",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString PreFtpCommands = new SOSOptionCommandString(this, conClassName + ".Pre_Ftp_Commands",
            "FTP commands, which has to be executed before the transfer started.", "", "", false);

    public SOSOptionCommandString PreTransferCommands = (SOSOptionCommandString) PreFtpCommands.SetAlias("pre_transfer_commands");

    public String getPreFtpCommands() {
        return PreFtpCommands.Value();
    }

    public SOSFtpOptionsSuperClass setPreFtpCommands(final String pstrValue) {
        PreFtpCommands.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "PostTransferCommands", description = "FTP commands, which has to be executed after the transfer ended.",
            key = "PostTransferCommands", type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString PostTransferCommands = new SOSOptionCommandString(this, conClassName + ".post_transfer_Commands",
            "FTP commands, which has to be executed after the transfer ended.", "", "", false);

    public SOSOptionString PostFtpCommands = (SOSOptionString) PostTransferCommands.SetAlias("post_Transfer_commands");

    public String getPostTransferCommands() {
        return PostTransferCommands.Value();
    }

    public SOSFtpOptionsSuperClass setPostTransferCommands(final String pstrValue) {
        PostTransferCommands.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "SecurityHashType", description = "", key = "security_hash_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString SecurityHashType = new SOSOptionString(this, conClassName + ".security_hash_type", "The Type of the security hash, e.g. MD5", "MD5",
            "MD5", false);

    @JSOptionDefinition(name = "DecompressAfterTransfer", description = "", key = "Decompress_After_Transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean DecompressAfterTransfer = new SOSOptionBoolean(this, conClassName + ".Decompress_After_Transfer",
            "Decompress zipped-files after transfer", "false", "false", false);

    @JSOptionDefinition(name = "ConcurrentTransfer", description = "", key = "Concurrent_Transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean ConcurrentTransfer = new SOSOptionBoolean(this, conClassName + ".Concurrent_Transfer", "Process transfers simultaneously", "false",
            "false", false);

    @JSOptionDefinition(name = "CheckSecurityHash", description = "", key = "Check_Security_Hash", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CheckSecurityHash = new SOSOptionBoolean(this, conClassName + ".Check_Security_Hash", "Decompress zipped-files after transfer",
            "false", "false", false);

    @JSOptionDefinition(name = "MaxConcurrentTransfers", description = "", key = "Max_Concurrent_Transfers", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger MaxConcurrentTransfers = new SOSOptionInteger(this, conClassName + ".Max_Concurrent_Transfers",
            "Maximum Numbers of parallel transfers", "5", "1", false);

    @JSOptionDefinition(name = "CreateSecurityHashFile", description = "", key = "create_security_hash_file", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CreateSecurityHashFile = new SOSOptionBoolean(this, conClassName + ".create_security_hash_file", "CreateSecurityHashFile", "false",
            "false", false);

    @JSOptionDefinition(name = "CreateSecurityHash", description = "", key = "create_security_hash", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CreateSecurityHash = new SOSOptionBoolean(this, conClassName + ".create_security_hash",
            "This parameter specifies whether the content of a", "true", "false", false);

    @JSOptionDefinition(name = "BufferSize", description = "", key = "buffer_Size", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger BufferSize = new SOSOptionInteger(this, conClassName + ".buffer_Size", "This parameter specifies the interval in seconds", "32000",
            "4096", false);

    @JSOptionDefinition(name = "create_order", description = "Activate file-order creation With this parameter it is possible to specif", key = "create_order",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean create_order = new SOSOptionBoolean(this, conClassName + ".create_order",
            "Activate file-order creation With this parameter it is possible to specif", "false", "false", false);

    public SOSOptionBoolean getcreate_order() {
        return create_order;
    }

    public void setcreate_order(final SOSOptionBoolean p_create_order) {
        create_order = p_create_order;
    }

    @JSOptionDefinition(name = "create_orders_for_all_files", description = "Create a file-order for every file in the result-list",
            key = "create_orders_for_all_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean create_orders_for_all_files = new SOSOptionBoolean(this, conClassName + ".create_orders_for_all_files",
            "Create a file-order for every file in the result-list", "false", "false", false);

    public SOSOptionBoolean getcreate_orders_for_all_files() {
        return create_orders_for_all_files;
    }

    public void setcreate_orders_for_all_files(final SOSOptionBoolean p_create_orders_for_all_files) {
        create_orders_for_all_files = p_create_orders_for_all_files;
    }

    @JSOptionDefinition(name = "expected_size_of_result_set", description = "number of expected hits in result-list", key = "expected_size_of_result_set",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger expected_size_of_result_set = new SOSOptionInteger(this, conClassName + ".expected_size_of_result_set",
            "number of expected hits in result-list", "0", "0", false);

    public SOSOptionInteger getexpected_size_of_result_set() {
        return expected_size_of_result_set;
    }

    public void setexpected_size_of_result_set(final SOSOptionInteger p_expected_size_of_result_set) {
        expected_size_of_result_set = p_expected_size_of_result_set;
    }

    @JSOptionDefinition(name = "file", description = "File or Folder to watch for Checked file or directory Supports", key = "file", type = "SOSOptionString",
            mandatory = true)
    public SOSOptionFileName file = new SOSOptionFileName(this, conClassName + ".file", "File or Folder to watch for Checked file or directory Supports", ".",
            ".", true);

    @JSOptionDefinition(name = "target", description = "target or Folder to watch for Checked target or directory Supports", key = "target",
            type = "SOSOptionString", mandatory = true)
    public SOSOptionFileName target = new SOSOptionFileName(this, conClassName + ".target",
            "target or Folder to watch for Checked target or directory Supports", ".", ".", true);

    public SOSOptionFileName getfile() {
        return file;
    }

    public void setfile(final SOSOptionFileName p_file) {
        file = p_file;
    }

    public SOSOptionFileName FileName = (SOSOptionFileName) file.SetAlias(conClassName + ".FileName");
    @JSOptionDefinition(name = "max_file_age", description = "maximum age of a file Specifies the maximum age of a file. If a file", key = "max_file_age",
            type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime max_file_age = new SOSOptionTime(this, conClassName + ".max_file_age",
            "maximum age of a file Specifies the maximum age of a file. If a file", "0", "0", false);

    public SOSOptionTime getmax_file_age() {
        return max_file_age;
    }

    public void setmax_file_age(final SOSOptionTime p_max_file_age) {
        max_file_age = p_max_file_age;
    }

    public SOSOptionTime FileAgeMaximum = (SOSOptionTime) max_file_age.SetAlias(conClassName + ".FileAgeMaximum");

    @JSOptionDefinition(name = "max_file_size", description = "maximum size of a file Specifies the maximum size of a file in", key = "max_file_size",
            type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize max_file_size = new SOSOptionFileSize(this, conClassName + ".max_file_size",
            "maximum size of a file Specifies the maximum size of a file in", "-1", "-1", false);

    public SOSOptionFileSize getmax_file_size() {
        return max_file_size;
    }

    public void setmax_file_size(final SOSOptionFileSize p_max_file_size) {
        max_file_size = p_max_file_size;
    }

    public SOSOptionFileSize FileSizeMaximum = (SOSOptionFileSize) max_file_size.SetAlias(conClassName + ".FileSizeMaximum");

    @JSOptionDefinition(name = "min_file_age", description = "minimum age of a file Specifies the minimum age of a files. If the fi", key = "min_file_age",
            type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime min_file_age = new SOSOptionTime(this, conClassName + ".min_file_age",
            "minimum age of a file Specifies the minimum age of a files. If the fi", "0", "0", false);

    public SOSOptionTime getmin_file_age() {
        return min_file_age;
    }

    public void setmin_file_age(final SOSOptionTime p_min_file_age) {
        min_file_age = p_min_file_age;
    }

    public SOSOptionTime FileAgeMinimum = (SOSOptionTime) min_file_age.SetAlias(conClassName + ".FileAgeMinimum");

    @JSOptionDefinition(name = "min_file_size", description = "minimum size of one or multiple files Specifies the minimum size of one", key = "min_file_size",
            type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize min_file_size = new SOSOptionFileSize(this, conClassName + ".min_file_size",
            "minimum size of one or multiple files Specifies the minimum size of one", "-1", "-1", false);

    public SOSOptionFileSize getmin_file_size() {
        return min_file_size;
    }

    public void setmin_file_size(final SOSOptionFileSize p_min_file_size) {
        min_file_size = p_min_file_size;
    }

    public SOSOptionFileSize FileSizeMinimum = (SOSOptionFileSize) min_file_size.SetAlias(conClassName + ".FileSizeMinimum");

    @JSOptionDefinition(name = "MergeOrderParameter", description = "Merge created order parameter with parameter of current order",
            key = "MergeOrderParameter", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean MergeOrderParameter = new SOSOptionBoolean(this, conClassName + ".MergeOrderParameter",
            "Merge created order parameter with parameter of current order", "false", "false", false);

    public String getMergeOrderParameter() {
        return MergeOrderParameter.Value();
    }

    public SOSFtpOptionsSuperClass setMergeOrderParameter(final String pstrValue) {
        MergeOrderParameter.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "next_state", description = "The first node to execute in a jobchain The name of the node of a jobchai", key = "next_state",
            type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode next_state = new SOSOptionJobChainNode(this, conClassName + ".next_state",
            "The first node to execute in a jobchain The name of the node of a jobchai", "", "", false);

    public SOSOptionJobChainNode getnext_state() {
        return next_state;
    }

    public void setnext_state(final SOSOptionJobChainNode p_next_state) {
        next_state = p_next_state;
    }

    @JSOptionDefinition(name = "on_empty_result_set", description = "Set next node on empty result set The next Node (Step, Job) to execute i",
            key = "on_empty_result_set", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode on_empty_result_set = new SOSOptionJobChainNode(this, conClassName + ".on_empty_result_set",
            "Set next node on empty result set The next Node (Step, Job) to execute i", "", "", false);

    public SOSOptionJobChainNode geton_empty_result_set() {
        return on_empty_result_set;
    }

    public void seton_empty_result_set(final SOSOptionJobChainNode p_on_empty_result_set) {
        on_empty_result_set = p_on_empty_result_set;
    }

    @JSOptionDefinition(name = "order_jobchain_name", description = "The name of the jobchain which belongs to the order The name of the jobch",
            key = "order_jobchain_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString order_jobchain_name = new SOSOptionString(this, conClassName + ".order_jobchain_name",
            "The name of the jobchain which belongs to the order The name of the jobch", "", "", false);

    public SOSOptionString getorder_jobchain_name() {
        return order_jobchain_name;
    }

    public void setorder_jobchain_name(final SOSOptionString p_order_jobchain_name) {
        order_jobchain_name = p_order_jobchain_name;
    }

    @JSOptionDefinition(name = "raise_error_if_result_set_is", description = "raise error on expected size of result-set With this parameter it is poss",
            key = "raise_error_if_result_set_is", type = "SOSOptionRelOp", mandatory = false)
    public SOSOptionRelOp raise_error_if_result_set_is = new SOSOptionRelOp(this, conClassName + ".raise_error_if_result_set_is",
            "raise error on expected size of result-set With this parameter it is poss", "", "", false);

    public SOSOptionRelOp getraise_error_if_result_set_is() {
        return raise_error_if_result_set_is;
    }

    public void setraise_error_if_result_set_is(final SOSOptionRelOp p_raise_error_if_result_set_is) {
        raise_error_if_result_set_is = p_raise_error_if_result_set_is;
    }

    @JSOptionDefinition(name = "result_list_file", description = "Name of the result-list file If the value of this parameter specifies a v",
            key = "result_list_file", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName result_list_file = new SOSOptionFileName(this, conClassName + ".result_list_file",
            "Name of the result-list file If the value of this parameter specifies a v", "", "", false);

    public SOSOptionFileName getresult_list_file() {
        return result_list_file;
    }

    public void setresult_list_file(final SOSOptionFileName p_result_list_file) {
        result_list_file = p_result_list_file;
    }

    @JSOptionDefinition(name = "scheduler_file_name", description = "Name of the file to process for a file-order", key = "scheduler_file_name",
            type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName scheduler_file_name = new SOSOptionFileName(this, conClassName + ".scheduler_file_name",
            "Name of the file to process for a file-order", "", "", false);

    public SOSOptionFileName getscheduler_file_name() {
        return scheduler_file_name;
    }

    public void setscheduler_file_name(final SOSOptionFileName p_scheduler_file_name) {
        scheduler_file_name = p_scheduler_file_name;
    }

    @JSOptionDefinition(name = "scheduler_file_parent", description = "pathanme of the file to process for a file-order", key = "scheduler_file_parent",
            type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName scheduler_file_parent = new SOSOptionFileName(this, conClassName + ".scheduler_file_parent",
            "pathanme of the file to process for a file-order", "", "", false);

    public SOSOptionFileName getscheduler_file_parent() {
        return scheduler_file_parent;
    }

    public void setscheduler_file_parent(final SOSOptionFileName p_scheduler_file_parent) {
        scheduler_file_parent = p_scheduler_file_parent;
    }

    @JSOptionDefinition(name = "scheduler_file_path", description = "file to process for a file-order Using Directory Monitoring with",
            key = "scheduler_file_path", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName scheduler_file_path = new SOSOptionFileName(this, conClassName + ".scheduler_file_path",
            "file to process for a file-order Using Directory Monitoring with", "", "", false);

    public SOSOptionFileName getscheduler_file_path() {
        return scheduler_file_path;
    }

    public void setscheduler_file_path(final SOSOptionFileName p_scheduler_file_path) {
        scheduler_file_path = p_scheduler_file_path;
    }

    @JSOptionDefinition(name = "scheduler_sosfileoperations_resultsetsize", description = "The amount of hits in the result set of the operation",
            key = "scheduler_sosfileoperations_resultsetsize", type = "SOSOptionsInteger", mandatory = false)
    public SOSOptionInteger scheduler_sosfileoperations_resultsetsize = new SOSOptionInteger(this, conClassName + ".scheduler_sosfileoperations_resultsetsize",
            "The amount of hits in the result set of the operation", "", "", false);

    public SOSOptionInteger getscheduler_sosfileoperations_resultsetsize() {
        return scheduler_sosfileoperations_resultsetsize;
    }

    public void setscheduler_sosfileoperations_resultsetsize(final SOSOptionInteger p_scheduler_sosfileoperations_resultsetsize) {
        scheduler_sosfileoperations_resultsetsize = p_scheduler_sosfileoperations_resultsetsize;
    }

    public SOSOptionInteger ResultSetSize = (SOSOptionInteger) scheduler_sosfileoperations_resultsetsize.SetAlias(conClassName + ".ResultSetSize");

    @JSOptionDefinition(name = "skip_first_files", description = "number of files to remove from the top of the result-set The numbe",
            key = "skip_first_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skip_first_files = new SOSOptionInteger(this, conClassName + ".skip_first_files",
            "number of files to remove from the top of the result-set The numbe", "0", "0", false);

    public SOSOptionInteger getskip_first_files() {
        return skip_first_files;
    }

    public void setskip_first_files(final SOSOptionInteger p_skip_first_files) {
        skip_first_files = p_skip_first_files;
    }

    public SOSOptionInteger NoOfFirstFiles2Skip = (SOSOptionInteger) skip_first_files.SetAlias(conClassName + ".NoOfFirstFiles2Skip");

    @JSOptionDefinition(name = "skip_last_files", description = "number of files to remove from the bottom of the result-set The numbe",
            key = "skip_last_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skip_last_files = new SOSOptionInteger(this, conClassName + ".skip_last_files",
            "number of files to remove from the bottom of the result-set The numbe", "0", "0", false);

    public SOSOptionInteger getskip_last_files() {
        return skip_last_files;
    }

    public void setskip_last_files(final SOSOptionInteger p_skip_last_files) {
        skip_last_files = p_skip_last_files;
    }

    public SOSOptionInteger NoOfLastFiles2Skip = (SOSOptionInteger) skip_last_files.SetAlias(conClassName + ".NoOfLastFiles2Skip");

    @JSOptionDefinition(name = "Max_Files", description = "Maximum number of files to process", key = "Max_Files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger MaxFiles = new SOSOptionInteger(this, conClassName + ".Max_Files", "Maximum number of files to process", "-1", "-1", false);

    public String getMax_Files() {
        return MaxFiles.Value();
    }

    public SOSFtpOptionsSuperClass setMax_Files(final String pstrValue) {
        MaxFiles.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "check_steady_count", description = "Number of tries for Steady check", key = "check_steady_count", type = "SOSOptionInteger",
            mandatory = false)
    public SOSOptionInteger CheckSteadyCount = new SOSOptionInteger(this, conClassName + ".check_steady_count", "Number of tries for Steady check", "10", "10",
            false);

    public String getcheck_steady_count() {
        return CheckSteadyCount.Value();
    }

    public SOSFtpOptionsSuperClass setcheck_steady_count(final String pstrValue) {
        CheckSteadyCount.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "check_steady_state_interval", description = "The intervall for steady state checking", key = "check_steady_state_interval",
            type = "SOSOptionFileTime", mandatory = false)
    public SOSOptionTime check_steady_state_interval = new SOSOptionTime(this, conClassName + ".check_steady_state_interval",
            "The intervall for steady state checking", "1", "1", false);

    public SOSOptionTime CheckSteadyStateInterval = (SOSOptionTime) check_steady_state_interval.SetAlias("check_steady_state_interval");

    public String getcheck_steady_state_interval() {
        return check_steady_state_interval.Value();
    }

    public SOSFtpOptionsSuperClass setcheck_steady_state_interval(final String pstrValue) {
        check_steady_state_interval.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Check_Steady_State_Of_Files", description = "Check wether a file is beeing modified", key = "Check_Steady_State_Of_Files",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CheckSteadyStateOfFiles = new SOSOptionBoolean(this, conClassName + ".Check_Steady_State_Of_Files",
            "Check wether a file is beeing modified", "false", "false", false);

    public String getCheckSteadyStateOfFiles() {
        return CheckSteadyStateOfFiles.Value();
    }

    public SOSFtpOptionsSuperClass setCheckSteadyStateOfFiles(final String pstrValue) {
        CheckSteadyStateOfFiles.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "PollErrorState", description = "Next state in Chain if no files found", key = "Poll_Error_State", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionJobChainNode PollErrorState = new SOSOptionJobChainNode(this, conClassName + ".Poll_Error_State", "Next state in Chain if no files found",
            "", "", false);

    public SOSOptionJobChainNode NoFilesState = (SOSOptionJobChainNode) PollErrorState.SetAlias("No_files_state");

    public String getPollErrorState() {
        return PollErrorState.Value();
    }

    public SOSFtpOptionsSuperClass setPollErrorState(final String pstrValue) {
        PollErrorState.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Steady_state_error_state", description = "Next state in JobChain if check steady state did not comes to an normal end",
            key = "Steady_state_error_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode Steady_state_error_state = new SOSOptionJobChainNode(this, conClassName + ".Steady_state_error_state",
            "Next state in JobChain if check steady state did not comes to an normal end", "", "", false);

    public SOSOptionJobChainNode SteadyStateErrorState = (SOSOptionJobChainNode) Steady_state_error_state.SetAlias("SteadyErrorState");

    public String getSteady_state_error_state() {
        return Steady_state_error_state.Value();
    }

    public SOSFtpOptionsSuperClass setSteady_state_error_state(final String pstrValue) {
        Steady_state_error_state.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "make_Dirs", description = "Create missing Directory on Target", key = "make_Dirs", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean makeDirs = new SOSOptionBoolean(this, conClassName + ".make_Dirs", "Create missing Directory on Target", "true", "true", false);

    public SOSOptionBoolean createFoldersOnTarget = (SOSOptionBoolean) makeDirs.SetAlias("create_folders_on_target");

    public String getmake_Dirs() {
        return makeDirs.Value();
    }

    public SOSFtpOptionsSuperClass setmake_Dirs(final String pstrValue) {
        makeDirs.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "File_List_Name", description = "File with a list of file names", key = "File_List_Name", type = "SOSOptionInFileName",
            mandatory = false)
    public SOSOptionInFileName FileListName = new SOSOptionInFileName(this, conClassName + ".File_List_Name", "File with a list of file names", "", "", false);

    public String getFileListName() {
        return FileListName.Value();
    }

    public SOSFtpOptionsSuperClass setFileListName(final String pstrValue) {
        FileListName.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Create_Result_Set", description = "Write the ResultSet to a file", key = "Create_Result_Set", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean CreateResultSet = new SOSOptionBoolean(this, conClassName + ".Create_Result_Set", "Write the ResultSet to a file", "false",
            "false", false);

    public SOSOptionBoolean CreateResultList = (SOSOptionBoolean) CreateResultSet.SetAlias("create_result_list");

    public String getCreateResultSet() {
        return CreateResultSet.Value();
    }

    public SOSFtpOptionsSuperClass setCreateResultSet(final String pstrValue) {
        CreateResultSet.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "ResultSetFileName", description = "Name of a File with a filelist or a resultlist", key = "Result_Set_FileName",
            type = "SOSOptionFileName", mandatory = false)
    public SOSOptionOutFileName ResultSetFileName = new SOSOptionOutFileName(this, conClassName + ".Result_Set_File_Name",
            "Name of a File with a filelist or a resultlist", "", "", false);

    public String getResultSetFileName() {
        return ResultSetFileName.Value();
    }

    public SOSFtpOptionsSuperClass setResultSetFileName(final String pstrValue) {
        ResultSetFileName.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "source_dir", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionFolderName SourceDir = new SOSOptionFolderName(this, conClassName + ".source_dir", "local_dir Local directory into which or from which",
            "", "", false);

    @JSOptionDefinition(name = "target_dir", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionFolderName TargetDir = new SOSOptionFolderName(this, conClassName + ".target_dir", "target_dir directory into which or from which", "", "",
            false);

    @JSOptionDefinition(name = "account", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString account = new SOSOptionString(this, conClassName + ".account", "Optional account info for authentication with an", " ", " ", false);

    @Override
    public SOSOptionString getaccount() {
        return account;
    }

    @Override
    public void setaccount(final SOSOptionString p_account) {
        account = p_account;
    }

    @JSOptionDefinition(name = "alternative_account", description = "Alternative parameter for the primary parameter", key = "alternative_account",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternative_account = new SOSOptionString(this, conClassName + ".alternative_account",
            "Alternative parameter for the primary parameter", " ", " ", false);

    @Override
    public SOSOptionString getalternative_account() {
        return alternative_account;
    }

    @Override
    public void setalternative_account(final SOSOptionString p_alternative_account) {
        alternative_account = p_alternative_account;
    }

    @JSOptionDefinition(name = "alternative_host", description = "Alternative parameter for the primary parameter", key = "alternative_host",
            type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName alternative_host = new SOSOptionHostName(this, conClassName + ".alternative_host",
            "Alternative parameter for the primary parameter", " ", " ", false);

    @Override
    public SOSOptionHostName getalternative_host() {
        return alternative_host;
    }

    @Override
    public void setalternative_host(final SOSOptionHostName p_alternative_host) {
        alternative_host = p_alternative_host;
    }

    @JSOptionDefinition(name = "alternative_passive_mode", description = "Alternative parameter for the primary parameter", key = "alternative_passive_mode",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternative_passive_mode = new SOSOptionString(this, conClassName + ".alternative_passive_mode",
            "Alternative parameter for the primary parameter", " ", " ", false);

    @Override
    public SOSOptionString getalternative_passive_mode() {
        return alternative_passive_mode;
    }

    @Override
    public void setalternative_passive_mode(final SOSOptionString p_alternative_passive_mode) {
        alternative_passive_mode = p_alternative_passive_mode;
    }

    @JSOptionDefinition(name = "alternative_password", description = "Alternative parameter for the primary parameter", key = "alternative_password",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword alternative_password = new SOSOptionPassword(this, conClassName + ".alternative_password",
            "Alternative parameter for the primary parameter", " ", " ", false);

    @Override
    public SOSOptionPassword getalternative_password() {
        return alternative_password;
    }

    @Override
    public void setalternative_password(final SOSOptionPassword p_alternative_password) {
        alternative_password = p_alternative_password;
    }

    @JSOptionDefinition(name = "alternative_port", description = "Alternative parameter for the primary parameter", key = "alternative_port",
            type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber alternative_port = new SOSOptionPortNumber(this, conClassName + ".alternative_port",
            "Alternative parameter for the primary parameter", "21", "21", false);

    @Override
    public SOSOptionPortNumber getalternative_port() {
        return alternative_port;
    }

    @Override
    public void setalternative_port(final SOSOptionPortNumber p_alternative_port) {
        alternative_port = p_alternative_port;
    }

    public SOSOptionPortNumber AlternativePortNumber = (SOSOptionPortNumber) alternative_port.SetAlias(conClassName + ".AlternativePortNumber");

    @JSOptionDefinition(name = "alternative_remote_dir", description = "Alternative parameter for the primary parameter", key = "alternative_remote_dir",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternative_remote_dir = new SOSOptionString(this, conClassName + ".alternative_remote_dir",
            "Alternative parameter for the primary parameter", " ", " ", false);

    @Override
    public SOSOptionString getalternative_remote_dir() {
        return alternative_remote_dir;
    }

    @Override
    public void setalternative_remote_dir(final SOSOptionString p_alternative_remote_dir) {
        alternative_remote_dir = p_alternative_remote_dir;
    }

    @JSOptionDefinition(name = "alternative_transfer_mode", description = "Alternative parameter for the primary parameter", key = "alternative_transfer_mode",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternative_transfer_mode = new SOSOptionString(this, conClassName + ".alternative_transfer_mode",
            "Alternative parameter for the primary parameter", " ", " ", false);

    @Override
    public SOSOptionString getalternative_transfer_mode() {
        return alternative_transfer_mode;
    }

    @Override
    public void setalternative_transfer_mode(final SOSOptionString p_alternative_transfer_mode) {
        alternative_transfer_mode = p_alternative_transfer_mode;
    }

    @JSOptionDefinition(name = "alternative_user", description = "Alternative parameter for the primary parameter", key = "alternative_user",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName alternative_user = new SOSOptionUserName(this, conClassName + ".alternative_user",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionUserName getalternative_user() {
        return alternative_user;
    }

    @Override
    public void setalternative_user(final SOSOptionUserName p_alternative_user) {
        alternative_user = p_alternative_user;
    }

    @JSOptionDefinition(name = "append_files", description = "This parameter specifies whether the content of a", key = "append_files",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean append_files = new SOSOptionBoolean(this, conClassName + ".append_files", "This parameter specifies whether the content of a",
            "false", "false", false);

    @Override
    public SOSOptionBoolean getappend_files() {
        return append_files;
    }

    @Override
    public void setappend_files(final SOSOptionBoolean p_append_files) {
        append_files = p_append_files;
    }

    @JSOptionDefinition(name = "atomic_prefix", description = "This parameter specifies whether target files shou", key = "atomic_prefix",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString atomic_prefix = new SOSOptionString(this, conClassName + ".atomic_prefix", "This parameter specifies whether target files shou", "",
            "", false);

    @Override
    public SOSOptionString getatomic_prefix() {
        return atomic_prefix;
    }

    @Override
    public void setatomic_prefix(final SOSOptionString p_atomic_prefix) {
        atomic_prefix = p_atomic_prefix;
    }

    @JSOptionDefinition(name = "atomic_suffix", description = "This parameter specifies whether target files shou", key = "atomic_suffix",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString atomic_suffix = new SOSOptionString(this, conClassName + ".atomic_suffix", "This parameter specifies whether target files shou", "",
            "", false);

    @Override
    public SOSOptionString getatomic_suffix() {
        return atomic_suffix;
    }

    @Override
    public void setatomic_suffix(final SOSOptionString p_atomic_suffix) {
        atomic_suffix = p_atomic_suffix;
    }

    @JSOptionDefinition(name = "banner_footer", description = "Name der Template-Datei für Protokoll-Ende This p", key = "banner_footer",
            type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName banner_footer = new SOSOptionInFileName(this, conClassName + ".banner_footer",
            "Name der Template-Datei für Protokoll-Ende This p", "", "", false);

    @Override
    public SOSOptionInFileName getbanner_footer() {
        return banner_footer;
    }

    @Override
    public void setbanner_footer(final SOSOptionInFileName p_banner_footer) {
        banner_footer = p_banner_footer;
    }

    @JSOptionDefinition(name = "banner_header", description = "Name of Template-File for log-File", key = "banner_header", type = "SOSOptionInFileName",
            mandatory = false)
    public SOSOptionInFileName banner_header = new SOSOptionInFileName(this, conClassName + ".banner_header", "Name of Template-File for log-File", "", "",
            false);

    @Override
    public SOSOptionInFileName getbanner_header() {
        return banner_header;
    }

    @Override
    public void setbanner_header(final SOSOptionInFileName p_banner_header) {
        banner_header = p_banner_header;
    }

    @JSOptionDefinition(name = "check_interval", description = "This parameter specifies the interval in seconds", key = "check_interval",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger check_interval = new SOSOptionInteger(this, conClassName + ".check_interval", "This parameter specifies the interval in seconds",
            "60", "60", false);

    @Override
    public SOSOptionInteger getcheck_interval() {
        return check_interval;
    }

    @Override
    public void setcheck_interval(final SOSOptionInteger p_check_interval) {
        check_interval = p_check_interval;
    }

    @JSOptionDefinition(name = "check_retry", description = "This parameter specifies whether a file transfer", key = "check_retry", type = "SOSOptionInteger",
            mandatory = false)
    public SOSOptionInteger check_retry = new SOSOptionInteger(this, conClassName + ".check_retry", "This parameter specifies whether a file transfer", "0",
            "0", false);

    @Override
    public SOSOptionInteger getcheck_retry() {
        return check_retry;
    }

    @Override
    public void setcheck_retry(final SOSOptionInteger p_check_retry) {
        check_retry = p_check_retry;
    }

    @JSOptionDefinition(name = "check_size", description = "This parameter determines whether the original f", key = "check_size", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean check_size = new SOSOptionBoolean(this, conClassName + ".check_size", "This parameter determines whether the original f", "true",
            "true", false);

    public SOSOptionBoolean CheckFileSizeAfterTransfer = (SOSOptionBoolean) check_size.SetAlias(conClassName + ".CheckFileSizeAfterTransfer");

    @JSOptionDefinition(name = "classpath_base", description = "The parameter is used during installation of this", key = "classpath_base",
            type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName classpath_base = new SOSOptionFolderName(this, conClassName + ".classpath_base",
            "The parameter is used during installation of this", "", "", false);

    @Override
    public SOSOptionFolderName getclasspath_base() {
        return classpath_base;
    }

    @Override
    public void setclasspath_base(final SOSOptionFolderName p_classpath_base) {
        classpath_base = p_classpath_base;
    }

    @JSOptionDefinition(name = "compress_files", description = "This parameter specifies whether the content of the source files", key = "compress_files",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean compress_files = new SOSOptionBoolean(this, conClassName + ".compress_files",
            "This parameter specifies whether the content of the source files", "false", "false", false);

    @JSOptionDefinition(name = "compressed_file_extension", description = "Additional file-name extension for compressed files This parameter spe",
            key = "compressed_file_extension", type = "SOSOptionString", mandatory = false)
    public SOSOptionString compressed_file_extension = new SOSOptionString(this, conClassName + ".compressed_file_extension",
            "Additional file-name extension for compressed files This parameter spe", ".gz", ".gz", false);

    @JSOptionDefinition(name = "current_pid", description = "This parameter is used for Unix systems and - as o", key = "current_pid",
            type = "SOSOptionProcessID", mandatory = false)
    public SOSOptionProcessID current_pid = new SOSOptionProcessID(this, conClassName + ".current_pid", "This parameter is used for Unix systems and - as o",
            " ", " ", false);

    @Override
    public SOSOptionProcessID getcurrent_pid() {
        return current_pid;
    }

    @Override
    public void setcurrent_pid(final SOSOptionProcessID p_current_pid) {
        current_pid = p_current_pid;
    }

    @JSOptionDefinition(name = "file_path", description = "This parameter is used alternatively to the parame", key = "file_path", type = "SOSOptionFileName",
            mandatory = false)
    public SOSOptionFileName file_path = new SOSOptionFileName(this, conClassName + ".file_path", "This parameter is used alternatively to the parame", "", "",
            false);

    @Override
    public SOSOptionFileName getfile_path() {
        return file_path;
    }

    @Override
    public void setfile_path(final SOSOptionFileName p_file_path) {
        file_path = p_file_path;
    }

    @JSOptionDefinition(name = "file_spec", description = "file_spec This parameter expects a regular expressi", key = "file_spec", type = "SOSOptionRegExp",
            mandatory = false)
    public SOSOptionRegExp file_spec = new SOSOptionRegExp(this, conClassName + ".file_spec", "file_spec This parameter expects a regular expressi", "^.*$",
            "^.*$", false);

    public SOSOptionRegExp FileNameRegExp = (SOSOptionRegExp) file_spec.SetAlias(conClassName + ".FileNameRegExp");

    @Override
    public SOSOptionRegExp getfile_spec() {
        return file_spec;
    }

    @Override
    public void setfile_spec(final SOSOptionRegExp p_file_spec) {
        file_spec = p_file_spec;
    }

    public SOSOptionRegExp FileNamePatternRegExp = (SOSOptionRegExp) file_spec.SetAlias(conClassName + ".FileNamePatternRegExp");

    @JSOptionDefinition(name = "force_files", description = "This parameter specifies whether an error should b", key = "force_files",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean force_files = new SOSOptionBoolean(this, conClassName + ".force_files", "This parameter specifies whether an error should b",
            "true", "true", false);

    public SOSOptionBoolean ErrorOnNoDataFound = (SOSOptionBoolean) force_files.SetAlias("error_on_no_data_found", "error_when_no_data_found");

    @Override
    public SOSOptionBoolean getforce_files() {
        return force_files;
    }

    @Override
    public void setforce_files(final SOSOptionBoolean p_force_files) {
        force_files = p_force_files;
    }

    @JSOptionDefinition(name = "history", description = "This parameter causes a history file to be written", key = "history", type = "SOSOptionOutFileName",
            mandatory = false)
    public SOSOptionOutFileName history = new SOSOptionOutFileName(this, conClassName + ".history", "This parameter causes a history file to be written", "",
            "", false);

    public SOSOptionOutFileName HistoryFileName = (SOSOptionOutFileName) history.SetAlias("history_file_name");

    @Override
    public SOSOptionOutFileName gethistory() {
        return history;
    }

    @Override
    public void sethistory(final SOSOptionOutFileName p_history) {
        history = p_history;
    }

    public SOSOptionOutFileName SOSFtpHistoryFileName = (SOSOptionOutFileName) history.SetAlias(conClassName + ".SOSFtpHistoryFileName");

    @JSOptionDefinition(name = "history_repeat", description = "The parameter is used in order to synchronize para", key = "history_repeat",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger history_repeat = new SOSOptionInteger(this, conClassName + ".history_repeat", "The parameter is used in order to synchronize para",
            "3", "3", false);

    @Override
    public SOSOptionInteger gethistory_repeat() {
        return history_repeat;
    }

    @Override
    public void sethistory_repeat(final SOSOptionInteger p_history_repeat) {
        history_repeat = p_history_repeat;
    }

    @JSOptionDefinition(name = "history_repeat_interval", description = "The parameter is used in order to synchronize para", key = "history_repeat_interval",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger history_repeat_interval = new SOSOptionInteger(this, conClassName + ".history_repeat_interval",
            "The parameter is used in order to synchronize para", "1", "1", false);

    @Override
    public SOSOptionInteger gethistory_repeat_interval() {
        return history_repeat_interval;
    }

    @Override
    public void sethistory_repeat_interval(final SOSOptionInteger p_history_repeat_interval) {
        history_repeat_interval = p_history_repeat_interval;
    }

    @JSOptionDefinition(name = "host", description = "Host-Name This parameter specifies th", key = "host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName host = new SOSOptionHostName(this, conClassName + ".host", "Host-Name This parameter specifies th", " ", " ", false);

    @Override
    public SOSOptionHostName gethost() {
        return host;
    }

    @Override
    public void sethost(final SOSOptionHostName p_host) {
        host = p_host;
    }

    public SOSOptionHostName HostName = (SOSOptionHostName) host.SetAlias(conClassName + ".HostName");

    @JSOptionDefinition(name = "http_proxy_host", description = "The value of this parameter is the host name or th", key = "http_proxy_host",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString http_proxy_host = new SOSOptionString(this, conClassName + ".http_proxy_host", "The value of this parameter is the host name or th",
            "", "", false);

    @Override
    public SOSOptionString gethttp_proxy_host() {
        return http_proxy_host;
    }

    @Override
    public void sethttp_proxy_host(final SOSOptionString p_http_proxy_host) {
        http_proxy_host = p_http_proxy_host;
    }

    @JSOptionDefinition(name = "http_proxy_port", description = "This parameter specifies the port of a proxy that", key = "http_proxy_port",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString http_proxy_port = new SOSOptionString(this, conClassName + ".http_proxy_port", "This parameter specifies the port of a proxy that",
            " ", " ", false);

    @Override
    public SOSOptionString gethttp_proxy_port() {
        return http_proxy_port;
    }

    @Override
    public void sethttp_proxy_port(final SOSOptionString p_http_proxy_port) {
        http_proxy_port = p_http_proxy_port;
    }

    @JSOptionDefinition(name = "jump_command", description = "This parameter specifies a command that is to be e", key = "jump_command",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_command = new SOSOptionString(this, conClassName + ".jump_command", "This parameter specifies a command that is to be e", " ",
            " ", false);

    @Override
    public SOSOptionString getjump_command() {
        return jump_command;
    }

    @Override
    public void setjump_command(final SOSOptionString p_jump_command) {
        jump_command = p_jump_command;
    }

    @JSOptionDefinition(name = "jump_command_delimiter", description = "Command delimiter characters are specified using t", key = "jump_command_delimiter",
            type = "SOSOptionString", mandatory = true)
    public SOSOptionString jump_command_delimiter = new SOSOptionString(this, conClassName + ".jump_command_delimiter",
            "Command delimiter characters are specified using t", "%%", "%%", true);

    @Override
    public SOSOptionString getjump_command_delimiter() {
        return jump_command_delimiter;
    }

    @Override
    public void setjump_command_delimiter(final SOSOptionString p_jump_command_delimiter) {
        jump_command_delimiter = p_jump_command_delimiter;
    }

    @JSOptionDefinition(name = "jump_command_script", description = "This parameter can be used as an alternative to ju", key = "jump_command_script",
            type = "SOSOptionCommandScript", mandatory = false)
    public SOSOptionCommandScript jump_command_script = new SOSOptionCommandScript(this, conClassName + ".jump_command_script",
            "This parameter can be used as an alternative to ju", " ", " ", false);

    @Override
    public SOSOptionCommandScript getjump_command_script() {
        return jump_command_script;
    }

    @Override
    public void setjump_command_script(final SOSOptionCommandScript p_jump_command_script) {
        jump_command_script = p_jump_command_script;
    }

    @JSOptionDefinition(name = "jump_command_script_file", description = "This parameter can be used as an alternative to ju",
            key = "jump_command_script_file", type = "SOSOptionCommandScriptFile", mandatory = false)
    public SOSOptionCommandScriptFile jump_command_script_file = new SOSOptionCommandScriptFile(this, conClassName + ".jump_command_script_file",
            "This parameter can be used as an alternative to ju", " ", " ", false);

    @Override
    public SOSOptionCommandScriptFile getjump_command_script_file() {
        return jump_command_script_file;
    }

    @Override
    public void setjump_command_script_file(final SOSOptionCommandScriptFile p_jump_command_script_file) {
        jump_command_script_file = p_jump_command_script_file;
    }

    @JSOptionDefinition(name = "jump_host", description = "When using a jump_host then files are first transf", key = "jump_host", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionHostName jump_host = new SOSOptionHostName(this, conClassName + ".jump_host", "When using a jump_host then files are first transf", " ",
            " ", false);

    @Override
    public SOSOptionHostName getjump_host() {
        return jump_host;
    }

    @Override
    public void setjump_host(final SOSOptionHostName p_jump_host) {
        jump_host = p_jump_host;
    }

    @JSOptionDefinition(name = "jump_ignore_error", description = "Should the value true be specified, then execution", key = "jump_ignore_error",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_ignore_error = new SOSOptionBoolean(this, conClassName + ".jump_ignore_error",
            "Should the value true be specified, then execution", "false", "false", false);

    @Override
    public SOSOptionBoolean getjump_ignore_error() {
        return jump_ignore_error;
    }

    @Override
    public void setjump_ignore_error(final SOSOptionBoolean p_jump_ignore_error) {
        jump_ignore_error = p_jump_ignore_error;
    }

    @JSOptionDefinition(name = "jump_ignore_signal", description = "Should the value true be specified, t", key = "jump_ignore_signal",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_ignore_signal = new SOSOptionBoolean(this, conClassName + ".jump_ignore_signal", "Should the value true be specified, t",
            "false", "false", false);

    @Override
    public SOSOptionBoolean getjump_ignore_signal() {
        return jump_ignore_signal;
    }

    @Override
    public void setjump_ignore_signal(final SOSOptionBoolean p_jump_ignore_signal) {
        jump_ignore_signal = p_jump_ignore_signal;
    }

    @JSOptionDefinition(name = "jump_ignore_stderr", description = "This job checks if any output to stderr has been c", key = "jump_ignore_stderr",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_ignore_stderr = new SOSOptionBoolean(this, conClassName + ".jump_ignore_stderr",
            "This job checks if any output to stderr has been c", "false", "false", false);

    @Override
    public SOSOptionBoolean getjump_ignore_stderr() {
        return jump_ignore_stderr;
    }

    @Override
    public void setjump_ignore_stderr(final SOSOptionBoolean p_jump_ignore_stderr) {
        jump_ignore_stderr = p_jump_ignore_stderr;
    }

    @JSOptionDefinition(name = "jump_password", description = "Password for authentication with the jump_host.", key = "jump_password",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword jump_password = new SOSOptionPassword(this, conClassName + ".jump_password", "Password for authentication with the jump_host.",
            " ", " ", false);

    @Override
    public SOSOptionPassword getjump_password() {
        return jump_password;
    }

    @Override
    public void setjump_password(final SOSOptionPassword p_jump_password) {
        jump_password = p_jump_password;
    }

    @JSOptionDefinition(name = "jump_port", description = "Port on the jump_host by which files should be tra", key = "jump_port", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionPortNumber jump_port = new SOSOptionPortNumber(this, conClassName + ".jump_port", "Port on the jump_host by which files should be tra",
            "22", "22", false);

    @Override
    public SOSOptionPortNumber getjump_port() {
        return jump_port;
    }

    @Override
    public void setjump_port(final SOSOptionPortNumber p_jump_port) {
        jump_port = p_jump_port;
    }

    @JSOptionDefinition(name = "jump_protocol", description = "When using a jump_host then files are first transf", key = "jump_protocol",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_protocol = new SOSOptionString(this, conClassName + ".jump_protocol", "When using a jump_host then files are first transf",
            "sftp", "sftp", false);

    @Override
    public SOSOptionString getjump_protocol() {
        return jump_protocol;
    }

    @Override
    public void setjump_protocol(final SOSOptionString p_jump_protocol) {
        jump_protocol = p_jump_protocol;
    }

    @JSOptionDefinition(name = "jump_proxy_host", description = "The value of this parameter is the host name or th", key = "jump_proxy_host",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_proxy_host = new SOSOptionString(this, conClassName + ".jump_proxy_host", "The value of this parameter is the host name or th",
            " ", " ", false);

    @Override
    public SOSOptionString getjump_proxy_host() {
        return jump_proxy_host;
    }

    @Override
    public void setjump_proxy_host(final SOSOptionString p_jump_proxy_host) {
        jump_proxy_host = p_jump_proxy_host;
    }

    @JSOptionDefinition(name = "jump_proxy_password", description = "This parameter specifies the password for the prox", key = "jump_proxy_password",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_proxy_password = new SOSOptionString(this, conClassName + ".jump_proxy_password",
            "This parameter specifies the password for the prox", " ", " ", false);

    @Override
    public SOSOptionString getjump_proxy_password() {
        return jump_proxy_password;
    }

    @Override
    public void setjump_proxy_password(final SOSOptionString p_jump_proxy_password) {
        jump_proxy_password = p_jump_proxy_password;
    }

    @JSOptionDefinition(name = "jump_proxy_port", description = "This parameter specifies the port of a proxy that", key = "jump_proxy_port",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_proxy_port = new SOSOptionString(this, conClassName + ".jump_proxy_port", "This parameter specifies the port of a proxy that",
            " ", " ", false);

    @Override
    public SOSOptionString getjump_proxy_port() {
        return jump_proxy_port;
    }

    @Override
    public void setjump_proxy_port(final SOSOptionString p_jump_proxy_port) {
        jump_proxy_port = p_jump_proxy_port;
    }

    @JSOptionDefinition(name = "jump_proxy_user", description = "The value of this parameter specifies the user acc", key = "jump_proxy_user",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName jump_proxy_user = new SOSOptionUserName(this, conClassName + ".jump_proxy_user",
            "The value of this parameter specifies the user acc", " ", " ", false);

    @Override
    public SOSOptionUserName getjump_proxy_user() {
        return jump_proxy_user;
    }

    @Override
    public void setjump_proxy_user(final SOSOptionUserName p_jump_proxy_user) {
        jump_proxy_user = p_jump_proxy_user;
    }

    @JSOptionDefinition(name = "jump_proxy_protocol", description = "Jump Proxy protocol", key = "jump_proxy_protocol", type = "SOSOptionProxyProtocol",
            mandatory = false)
    public SOSOptionProxyProtocol jump_proxy_protocol = new SOSOptionProxyProtocol(this, conClassName + ".jump_proxy_protocol", "Jump Proxy protocol",
            SOSOptionProxyProtocol.Protocol.http.name(), SOSOptionProxyProtocol.Protocol.http.name(), false);

    public SOSOptionProxyProtocol getjump_proxy_protocol() {
        return jump_proxy_protocol;
    }

    public void setjump_proxy_protocol(SOSOptionProxyProtocol val) {
        jump_proxy_protocol = val;
    }

    @JSOptionDefinition(name = "jump_simulate_shell", description = "Should the value true be specified for this parame", key = "jump_simulate_shell",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_simulate_shell = new SOSOptionBoolean(this, conClassName + ".jump_simulate_shell",
            "Should the value true be specified for this parame", "false", "false", false);

    @Override
    public SOSOptionBoolean getjump_simulate_shell() {
        return jump_simulate_shell;
    }

    @Override
    public void setjump_simulate_shell(final SOSOptionBoolean p_jump_simulate_shell) {
        jump_simulate_shell = p_jump_simulate_shell;
    }

    @JSOptionDefinition(name = "jump_simulate_shell_inactivity_timeout", description = "If no new characters are written to stdout or stde",
            key = "jump_simulate_shell_inactivity_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger jump_simulate_shell_inactivity_timeout = new SOSOptionInteger(this, conClassName + ".jump_simulate_shell_inactivity_timeout",
            "If no new characters are written to stdout or stde", " ", " ", false);

    @Override
    public SOSOptionInteger getjump_simulate_shell_inactivity_timeout() {
        return jump_simulate_shell_inactivity_timeout;
    }

    @Override
    public void setjump_simulate_shell_inactivity_timeout(final SOSOptionInteger p_jump_simulate_shell_inactivity_timeout) {
        jump_simulate_shell_inactivity_timeout = p_jump_simulate_shell_inactivity_timeout;
    }

    @JSOptionDefinition(name = "jump_simulate_shell_login_timeout", description = "If no new characters are written to stdout or stde",
            key = "jump_simulate_shell_login_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger jump_simulate_shell_login_timeout = new SOSOptionInteger(this, conClassName + ".jump_simulate_shell_login_timeout",
            "If no new characters are written to stdout or stde", " ", " ", false);

    @Override
    public SOSOptionInteger getjump_simulate_shell_login_timeout() {
        return jump_simulate_shell_login_timeout;
    }

    @Override
    public void setjump_simulate_shell_login_timeout(final SOSOptionInteger p_jump_simulate_shell_login_timeout) {
        jump_simulate_shell_login_timeout = p_jump_simulate_shell_login_timeout;
    }

    @JSOptionDefinition(name = "jump_simulate_shell_prompt_trigger", description = "The expected command line prompt. Using this promp",
            key = "jump_simulate_shell_prompt_trigger", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_simulate_shell_prompt_trigger = new SOSOptionString(this, conClassName + ".jump_simulate_shell_prompt_trigger",
            "The expected command line prompt. Using this promp", " ", " ", false);

    @Override
    public SOSOptionString getjump_simulate_shell_prompt_trigger() {
        return jump_simulate_shell_prompt_trigger;
    }

    @Override
    public void setjump_simulate_shell_prompt_trigger(final SOSOptionString p_jump_simulate_shell_prompt_trigger) {
        jump_simulate_shell_prompt_trigger = p_jump_simulate_shell_prompt_trigger;
    }

    @JSOptionDefinition(name = "jump_ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "jump_ssh_auth_file",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionInFileName jump_ssh_auth_file = new SOSOptionInFileName(this, conClassName + ".jump_ssh_auth_file",
            "This parameter specifies the path and name of a us", " ", " ", false);

    @Override
    public SOSOptionInFileName getjump_ssh_auth_file() {
        return jump_ssh_auth_file;
    }

    @Override
    public void setjump_ssh_auth_file(final SOSOptionInFileName p_jump_ssh_auth_file) {
        jump_ssh_auth_file = p_jump_ssh_auth_file;
    }

    @JSOptionDefinition(name = "jump_ssh_auth_method", description = "This parameter specifies the authentication method", key = "jump_ssh_auth_method",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionAuthenticationMethod jump_ssh_auth_method = new SOSOptionAuthenticationMethod(this, conClassName + ".jump_ssh_auth_method",
            "This parameter specifies the authentication method", " ", " ", false);

    @Override
    public SOSOptionAuthenticationMethod getjump_ssh_auth_method() {
        return jump_ssh_auth_method;
    }

    @Override
    public void setjump_ssh_auth_method(final SOSOptionAuthenticationMethod p_jump_ssh_auth_method) {
        jump_ssh_auth_method = p_jump_ssh_auth_method;
    }

    @JSOptionDefinition(name = "jump_user", description = "User name for authentication with the jump_host.", key = "jump_user", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionUserName jump_user = new SOSOptionUserName(this, conClassName + ".jump_user", "User name for authentication with the jump_host.", "", "",
            false);

    @Override
    public SOSOptionUserName getjump_user() {
        return jump_user;
    }

    @Override
    public void setjump_user(final SOSOptionUserName p_jump_user) {
        jump_user = p_jump_user;
    }

    @JSOptionDefinition(name = "jump_dir", description = "This parameter specifies the directory on the jump host", key = "jump_dir", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString jump_dir = new SOSOptionString(this, conClassName + ".jump_dir", "This parameter specifies the directory on the jump host", "/tmp",
            "/tmp", false);

    public SOSOptionString getjump_dir() {
        return jump_dir;
    }

    public void setjump_dir(final SOSOptionString val) {
        jump_dir = val;
    }

    @JSOptionDefinition(name = "jump_platform", description = "This parameter specifies the platform on the jump host", key = "jump_dir",
            type = "SOSOptionPlatform", mandatory = false)
    public SOSOptionPlatform jump_platform = new SOSOptionPlatform(this, conClassName + ".jump_platform",
            "This parameter specifies the platform on the jump host", SOSOptionPlatform.enuValidPlatforms.unix.name(),
            SOSOptionPlatform.enuValidPlatforms.unix.name(), false);

    public SOSOptionPlatform getjump_platform() {
        return jump_platform;
    }

    public void setjump_platform(final SOSOptionPlatform val) {
        jump_platform = val;
    }

    @JSOptionDefinition(name = "local_dir", description = "local_dir Local directory into which or from which", key = "local_dir",
            type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName local_dir = new SOSOptionFolderName(this, conClassName + ".local_dir", "local_dir Local directory into which or from which", "",
            "", false);

    @Override
    public SOSOptionFolderName getlocal_dir() {
        return local_dir;
    }

    @Override
    public void setlocal_dir(final SOSOptionFolderName p_local_dir) {
        local_dir = p_local_dir;
    }

    @JSOptionDefinition(name = "mandator", description = "This parameter specifies the mandator for which a", key = "mandator", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString mandator = new SOSOptionString(this, conClassName + ".mandator", "This parameter specifies the mandator for which a", "SOS", "SOS",
            false);

    @Override
    public SOSOptionString getmandator() {
        return mandator;
    }

    @Override
    public void setmandator(final SOSOptionString p_mandator) {
        mandator = p_mandator;
    }

    @JSOptionDefinition(name = "operation", description = "Operation to be executed send, receive, remove,", key = "operation",
            type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionJadeOperation operation = new SOSOptionJadeOperation(this, conClassName + ".operation", "Operation to be executed send, receive, remove,",
            "send", "send", true);

    @Override
    public SOSOptionJadeOperation getoperation() {
        return operation;
    }

    @Override
    public void setoperation(final SOSOptionJadeOperation p_operation) {
        operation = p_operation;
    }

    @JSOptionDefinition(name = "overwrite_files", description = "This parameter specifies if existing files should", key = "overwrite_files",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean overwrite_files = new SOSOptionBoolean(this, conClassName + ".overwrite_files",
            "This parameter specifies if existing files should", "true", "true", false);

    @Override
    public SOSOptionBoolean getoverwrite_files() {
        return overwrite_files;
    }

    @Override
    public void setoverwrite_files(final SOSOptionBoolean p_overwrite_files) {
        overwrite_files = p_overwrite_files;
    }

    @JSOptionDefinition(name = "passive_mode", description = "passive_mode Passive mode for FTP is often used wit", key = "passive_mode",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean passive_mode = new SOSOptionBoolean(this, conClassName + ".passive_mode", "passive_mode Passive mode for FTP is often used wit",
            "false", "false", false);

    @Override
    public SOSOptionBoolean getpassive_mode() {
        return passive_mode;
    }

    @Override
    public void setpassive_mode(final SOSOptionBoolean p_passive_mode) {
        passive_mode = p_passive_mode;
    }

    public SOSOptionBoolean FTPTransferModeIsPassive = (SOSOptionBoolean) passive_mode.SetAlias(conClassName + ".FTPTransferModeIsPassive");

    @JSOptionDefinition(name = "password", description = "Password for UserID Password for a", key = "password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword password = new SOSOptionPassword(this, conClassName + ".password", "Password for UserID Password for a", " ", " ", false);

    @Override
    public SOSOptionPassword getpassword() {
        return password;
    }

    @Override
    public void setpassword(final SOSOptionPassword p_password) {
        password = p_password;
    }

    @JSOptionDefinition(name = "poll_interval", description = "This parameter specifies the interval in seconds", key = "poll_interval",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionTime poll_interval = new SOSOptionTime(this, conClassName + ".poll_interval", "This parameter specifies the interval in seconds", "60",
            "60", false);

    @Override
    public SOSOptionTime getpoll_interval() {
        return poll_interval;
    }

    @Override
    public void setpoll_interval(final SOSOptionTime p_poll_interval) {
        poll_interval = p_poll_interval;
    }

    @JSOptionDefinition(name = "Waiting_for_Late_comers", description = "Wait an additional interval for late comers", key = "Waiting_for_Late_comers",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean WaitingForLateComers = new SOSOptionBoolean(this, conClassName + ".Waiting_for_Late_comers",
            "Wait an additional interval for late comers", "false", "false", false);

    public String getWaiting_for_Late_comers() {
        return WaitingForLateComers.Value();
    }

    public SOSFtpOptionsSuperClass setWaiting_for_Late_comers(final String pstrValue) {
        WaitingForLateComers.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "poll_minfiles", description = "This parameter specifies the number of files tha", key = "poll_minfiles",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger poll_minfiles = new SOSOptionInteger(this, conClassName + ".poll_minfiles", "This parameter specifies the number of files tha",
            "0", "0", false);

    @Override
    public SOSOptionInteger getpoll_minfiles() {
        return poll_minfiles;
    }

    @Override
    public void setpoll_minfiles(final SOSOptionInteger p_poll_minfiles) {
        poll_minfiles = p_poll_minfiles;
    }

    @JSOptionDefinition(name = "PollingDuration", description = "The duration of the polling period", key = "PollingDuration", type = "SOSOptionTime",
            mandatory = false)
    public SOSOptionTime PollingDuration = new SOSOptionTime(this, conClassName + ".PollingDuration", "The duration of the polling period", "0", "0", false);

    public String getPollingDuration() {
        return PollingDuration.Value();
    }

    public SOSFtpOptionsSuperClass setPollingDuration(final String pstrValue) {
        PollingDuration.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "poll_timeout", description = "This parameter specifies the time in minutes, how", key = "poll_timeout",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger poll_timeout = new SOSOptionInteger(this, conClassName + ".poll_timeout", "This parameter specifies the time in minutes, how", "0",
            "0", false);

    @Override
    public SOSOptionInteger getpoll_timeout() {
        return poll_timeout;
    }

    @Override
    public void setpoll_timeout(final SOSOptionInteger p_poll_timeout) {
        poll_timeout = p_poll_timeout;
    }

    @JSOptionDefinition(name = "port", description = "Port-Number to be used for Data-Transfer", key = "port", type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber port = new SOSOptionPortNumber(this, conClassName + ".port", "Port-Number to be used for Data-Transfer", "21", "21", true);

    @Override
    public SOSOptionPortNumber getport() {
        return port;
    }

    @Override
    public void setport(final SOSOptionPortNumber p_port) {
        port = p_port;
    }

    @JSOptionDefinition(name = "ppid", description = "This parameter is used for Unix systems and - as o", key = "ppid", type = "SOSOptionProcessID",
            mandatory = false)
    public SOSOptionProcessID ppid = new SOSOptionProcessID(this, conClassName + ".ppid", "This parameter is used for Unix systems and - as o", " ", " ", false);

    @Override
    public SOSOptionProcessID getppid() {
        return ppid;
    }

    @Override
    public void setppid(final SOSOptionProcessID p_ppid) {
        ppid = p_ppid;
    }

    public SOSOptionProcessID ParentProcessID = (SOSOptionProcessID) ppid.SetAlias(conClassName + ".ParentProcessID");

    @JSOptionDefinition(name = "profile", description = "The Name of a Profile-Section to be executed", key = "profile", type = "SOSOptionString",
            mandatory = false)
    public SOSOptionString profile = new SOSOptionString(this, conClassName + ".profile", "The Name of a Profile-Section to be executed", " ", " ", false);

    @Override
    public SOSOptionString getprofile() {
        return profile;
    }

    @Override
    public void setprofile(final SOSOptionString p_profile) {
        profile = p_profile;
    }

    public SOSOptionString SectionName = (SOSOptionString) profile.SetAlias(conClassName + ".SectionName");

    @JSOptionDefinition(name = "protocol", description = "Type of requested Datatransfer The values ftp, sftp", key = "protocol",
            type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionTransferType protocol = new SOSOptionTransferType(this, conClassName + ".protocol", "Type of requested Datatransfer The values ftp, sftp",
            "ftp", "ftp", true);

    @Override
    public SOSOptionTransferType getprotocol() {
        return protocol;
    }

    @Override
    public void setprotocol(final SOSOptionTransferType p_protocol) {
        protocol = p_protocol;
    }

    public SOSOptionTransferType TransferProtocol = (SOSOptionTransferType) protocol.SetAlias(conClassName + ".TransferProtocol");

    @JSOptionDefinition(name = "recursive", description = "This parameter specifies if files from subdirector", key = "recursive", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean recursive = new SOSOptionBoolean(this, conClassName + ".recursive", "This parameter specifies if files from subdirector", "false",
            "false", false);

    public SOSOptionBoolean IncludeSubdirectories = (SOSOptionBoolean) recursive.SetAlias("include_sub_directories");

    @Override
    public SOSOptionBoolean getrecursive() {
        return recursive;
    }

    @Override
    public void setrecursive(final SOSOptionBoolean p_recursive) {
        recursive = p_recursive;
    }

    public SOSOptionBoolean RecurseSubFolders = (SOSOptionBoolean) recursive.SetAlias(conClassName + ".RecurseSubFolders");

    @JSOptionDefinition(name = "remote_dir", description = "remote_dir Directory at the FTP/SFTP server from wh", key = "remote_dir",
            type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName remote_dir = new SOSOptionFolderName(this, conClassName + ".remote_dir", "remote_dir Directory at the FTP/SFTP server from wh",
            ".", ".", false);

    @Override
    public SOSOptionFolderName getremote_dir() {
        return remote_dir;
    }

    @Override
    public void setremote_dir(final SOSOptionFolderName p_remote_dir) {
        remote_dir = p_remote_dir;
    }

    @JSOptionDefinition(name = "remove_files", description = "This parameter specifies whether files on the FTP/", key = "remove_files",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean remove_files = new SOSOptionBoolean(this, conClassName + ".remove_files", "This parameter specifies whether files on the FTP/",
            "false", "false", false);

    public SOSOptionBoolean DeleteFilesAfterTransfer = (SOSOptionBoolean) remove_files.SetAlias(conClassName + ".DeleteFilesAfterTransfer");

    public SOSOptionBoolean DeleteFilesOnSource = (SOSOptionBoolean) remove_files.SetAlias(conClassName + ".DeleteFilesOnSource");

    @Override
    public SOSOptionBoolean getremove_files() {
        return remove_files;
    }

    @Override
    public void setremove_files(final SOSOptionBoolean p_remove_files) {
        remove_files = p_remove_files;
    }

    @JSOptionDefinition(name = "replacement", description = "String for replacement of matching character seque", key = "replacement",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString replacement = new SOSOptionString(this, conClassName + ".replacement", "String for replacement of matching character seque", null,
            null, false);

    @Override
    public SOSOptionString getreplacement() {
        return replacement;
    }

    @Override
    public void setreplacement(final SOSOptionString p_replacement) {
        replacement = p_replacement;
    }

    public SOSOptionString ReplaceWith = (SOSOptionString) replacement.SetAlias(conClassName + ".ReplaceWith");

    @JSOptionDefinition(name = "replacing", description = "Regular expression for filename replacement with", key = "replacing", type = "SOSOptionRegExp",
            mandatory = false)
    public SOSOptionRegExp replacing = new SOSOptionRegExp(this, conClassName + ".replacing", "Regular expression for filename replacement with", " ", " ",
            false);

    @Override
    public SOSOptionRegExp getreplacing() {
        return replacing;
    }

    @Override
    public void setreplacing(final SOSOptionRegExp p_replacing) {
        replacing = p_replacing;
    }

    public SOSOptionRegExp ReplaceWhat = (SOSOptionRegExp) replacing.SetAlias(conClassName + ".ReplaceWhat");

    @JSOptionDefinition(name = "root", description = "The parameter specifies the directory in which thi", key = "root", type = "SOSOptionFolderName",
            mandatory = false)
    public SOSOptionFolderName root = new SOSOptionFolderName(this, conClassName + ".root", "The parameter specifies the directory in which thi", "", "", false);

    @Override
    public SOSOptionFolderName getroot() {
        return root;
    }

    @Override
    public void setroot(final SOSOptionFolderName p_root) {
        root = p_root;
    }

    public SOSOptionFolderName TempFolderName = (SOSOptionFolderName) root.SetAlias(conClassName + ".TempFolderName");

    @JSOptionDefinition(name = "scheduler_host", description = "This parameter specifies the host name or IP addre", key = "scheduler_host",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionHostName scheduler_host = new SOSOptionHostName(this, conClassName + ".scheduler_host",
            "This parameter specifies the host name or IP addre", "", "", false);

    public SOSOptionHostName BackgroundServiceHost = (SOSOptionHostName) scheduler_host.SetAlias("Background_Service_Host");

    @Override
    public SOSOptionHostName getscheduler_host() {
        return scheduler_host;
    }

    @Override
    public void setscheduler_host(final SOSOptionHostName p_scheduler_host) {
        scheduler_host = p_scheduler_host;
    }

    @JSOptionDefinition(name = "scheduler_job_chain", description = "The name of a job chain for Managed File Transfer", key = "scheduler_job_chain",
            type = "JSJobChain", mandatory = false)
    public JSJobChain scheduler_job_chain = new JSJobChain(this, conClassName + ".scheduler_job_chain", "The name of a job chain for Managed File Transfer",
            "scheduler_sosftp_history", "scheduler_sosftp_history", false);

    public JSJobChain BackgroundServiceJobChainName = (JSJobChain) scheduler_job_chain.SetAlias("BackgroundService_Job_Chain_Name");

    @Override
    public JSJobChain getscheduler_job_chain() {
        return scheduler_job_chain;
    }

    @Override
    public void setscheduler_job_chain(final JSJobChain p_scheduler_job_chain) {
        scheduler_job_chain = p_scheduler_job_chain;
    }

    @JSOptionDefinition(name = "scheduler_port", description = "The port for which a Job Scheduler for Managed File Trans", key = "scheduler_port",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionPortNumber scheduler_port = new SOSOptionPortNumber(this, conClassName + ".scheduler_port",
            "The port for which a Job Scheduler for Managed File Trans", "0", "4444", false);

    public SOSOptionPortNumber BackgroundServicePort = (SOSOptionPortNumber) scheduler_port
            .SetAlias("Background_Service_Port", "Background_Service_PortNumber");

    @Override
    public SOSOptionPortNumber getscheduler_port() {
        return scheduler_port;
    }

    @Override
    public void setscheduler_port(final SOSOptionPortNumber p_scheduler_port) {
        scheduler_port = p_scheduler_port;
    }

    @JSOptionDefinition(name = "Restart", description = "Set Restart/Resume Mode for Transfer", key = "Restart", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean Restart = new SOSOptionBoolean(this, conClassName + ".Restart", "Set Restart/Resume Mode for Transfer", "false", "false", false);

    public SOSOptionBoolean ResumeTransfer = (SOSOptionBoolean) Restart.SetAlias(conClassName + "Resume", conClassName + "Resume_Transfer");

    public String getRestart() {
        return Restart.Value();
    }

    public SOSFtpOptionsSuperClass setRestart(final String pstrValue) {
        Restart.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "settings", description = "Name of INI-File which contains the transfer profiles to execute", key = "settings",
            type = "SOSOptionIniFileName", mandatory = false)
    public SOSOptionIniFileName settings = new SOSOptionIniFileName(this, conClassName + ".settings",
            "Name of INI-File which contains the transfer profiles to execute", "", "", false);

    public SOSOptionIniFileName ConfigurationFile = (SOSOptionIniFileName) settings.SetAlias("JADE_Configuration_File", "JADE_Config_File", "Configuration",
            "JADE_Configuration", "JADE_INI_FILE");

    public SOSOptionIniFileName SOSIniFileName = (SOSOptionIniFileName) settings.SetAlias(conClassName + ".SOSIniFileName");

    @Override
    public SOSOptionIniFileName getsettings() {
        return settings;
    }

    @Override
    public void setsettings(final SOSOptionIniFileName p_settings) {
        settings = p_settings;
    }

    @JSOptionDefinition(name = "skip_transfer", description = "If this Parameter is set to true then", key = "skip_transfer", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean skip_transfer = new SOSOptionBoolean(this, conClassName + ".skip_transfer", "If this Parameter is set to true then", "false",
            "false", false);

    @Override
    public SOSOptionBoolean getskip_transfer() {
        return skip_transfer;
    }

    @Override
    public void setskip_transfer(final SOSOptionBoolean p_skip_transfer) {
        skip_transfer = p_skip_transfer;
    }

    @JSOptionDefinition(name = "ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "ssh_auth_file",
            type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName ssh_auth_file = new SOSOptionInFileName(this, conClassName + ".ssh_auth_file",
            "This parameter specifies the path and name of a us", "", "", false);

    public SOSOptionInFileName auth_file = (SOSOptionInFileName) ssh_auth_file.SetAlias(conClassName + ".auth_file");

    @Override
    public SOSOptionInFileName getssh_auth_file() {
        return ssh_auth_file;
    }

    @Override
    public void setssh_auth_file(final SOSOptionInFileName p_ssh_auth_file) {
        ssh_auth_file = p_ssh_auth_file;
    }

    @JSOptionDefinition(name = "ssh_auth_method", description = "This parameter specifies the authentication method", key = "ssh_auth_method",
            type = "SOSOptionStringValueList", mandatory = false)
    public SOSOptionAuthenticationMethod ssh_auth_method = new SOSOptionAuthenticationMethod(this, conClassName + ".ssh_auth_method",
            "This parameter specifies the authentication method", "publickey", "publickey", false);

    public SOSOptionAuthenticationMethod auth_method = (SOSOptionAuthenticationMethod) ssh_auth_method.SetAlias(conClassName + ".auth_method");

    @Override
    public SOSOptionAuthenticationMethod getssh_auth_method() {
        return ssh_auth_method;
    }

    @Override
    public void setssh_auth_method(final SOSOptionAuthenticationMethod p_ssh_auth_method) {
        ssh_auth_method = p_ssh_auth_method;
    }

    @JSOptionDefinition(name = "ssh_proxy_host", description = "The value of this parameter is the host name or th", key = "ssh_proxy_host",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_proxy_host = new SOSOptionString(this, conClassName + ".ssh_proxy_host", "The value of this parameter is the host name or th",
            " ", " ", false);

    @Override
    public SOSOptionString getssh_proxy_host() {
        return ssh_proxy_host;
    }

    @Override
    public void setssh_proxy_host(final SOSOptionString p_ssh_proxy_host) {
        ssh_proxy_host = p_ssh_proxy_host;
    }

    @JSOptionDefinition(name = "ssh_proxy_password", description = "This parameter specifies the password for the prox", key = "ssh_proxy_password",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_proxy_password = new SOSOptionString(this, conClassName + ".ssh_proxy_password",
            "This parameter specifies the password for the prox", " ", " ", false);

    @Override
    public SOSOptionString getssh_proxy_password() {
        return ssh_proxy_password;
    }

    @Override
    public void setssh_proxy_password(final SOSOptionString p_ssh_proxy_password) {
        ssh_proxy_password = p_ssh_proxy_password;
    }

    @JSOptionDefinition(name = "ssh_proxy_port", description = "This parameter specifies the port number of the pr", key = "ssh_proxy_port",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_proxy_port = new SOSOptionString(this, conClassName + ".ssh_proxy_port", "This parameter specifies the port number of the pr",
            " ", " ", false);

    @Override
    public SOSOptionString getssh_proxy_port() {
        return ssh_proxy_port;
    }

    @Override
    public void setssh_proxy_port(final SOSOptionString p_ssh_proxy_port) {
        ssh_proxy_port = p_ssh_proxy_port;
    }

    @JSOptionDefinition(name = "ssh_proxy_user", description = "The value of this parameter specifies the user acc", key = "ssh_proxy_user",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_proxy_user = new SOSOptionString(this, conClassName + ".ssh_proxy_user", "The value of this parameter specifies the user acc",
            " ", " ", false);

    @Override
    public SOSOptionString getssh_proxy_user() {
        return ssh_proxy_user;
    }

    @Override
    public void setssh_proxy_user(final SOSOptionString p_ssh_proxy_user) {
        ssh_proxy_user = p_ssh_proxy_user;
    }

    @JSOptionDefinition(name = "transactional", description = "This parameter specifies if file transfers should", key = "transactional",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean transactional = new SOSOptionBoolean(this, conClassName + ".transactional", "This parameter specifies if file transfers should",
            "false", "false", false);

    @Override
    public SOSOptionBoolean gettransactional() {
        return transactional;
    }

    @Override
    public void settransactional(final SOSOptionBoolean p_transactional) {
        transactional = p_transactional;
    }

    public SOSOptionBoolean TransactionMode = (SOSOptionBoolean) transactional.SetAlias(conClassName + ".TransactionMode");

    @JSOptionDefinition(name = "transfer_mode", description = "Type of Character-Encoding Transfe", key = "transfer_mode", type = "SOSOptionTransferMode",
            mandatory = false)
    public SOSOptionTransferMode transfer_mode = new SOSOptionTransferMode(this, conClassName + ".transfer_mode", "Type of Character-Encoding Transfe",
            "binary", "binary", false);

    @Override
    public SOSOptionTransferMode gettransfer_mode() {
        return transfer_mode;
    }

    @Override
    public void settransfer_mode(final SOSOptionTransferMode p_transfer_mode) {
        transfer_mode = p_transfer_mode;
    }

    @JSOptionDefinition(name = "user", description = "UserID of user in charge User name", key = "user", type = "SOSOptionUserName", mandatory = true)
    public SOSOptionUserName user = new SOSOptionUserName(this, conClassName + ".user", "UserID of user in charge User name", "", "anonymous", false);

    @Override
    public SOSOptionUserName getuser() {
        return user;
    }

    @Override
    public void setuser(final SOSOptionUserName p_user) {
        user = p_user;
    }

    @JSOptionDefinition(name = "verbose", description = "The granuality of (Debug-)Messages The verbosit", key = "verbose", type = "SOSOptionInteger",
            mandatory = false)
    public SOSOptionInteger verbose = new SOSOptionInteger(this, conClassName + ".verbose", "The granuality of (Debug-)Messages The verbosit", "1", "10", false);

    @Override
    public SOSOptionInteger getverbose() {
        return verbose;
    }

    @Override
    public void setverbose(final SOSOptionInteger p_verbose) {
        verbose = p_verbose;
    }

    public SOSOptionInteger VerbosityLevel = (SOSOptionInteger) verbose.SetAlias(conClassName + ".VerbosityLevel");

    @JSOptionDefinition(name = "zero_byte_transfer", description = "This parameter specifies whether zero byte files", key = "zero_byte_transfer",
            type = "SOSOptionZeroByteTransfer", mandatory = false)
    public SOSOptionZeroByteTransfer zero_byte_transfer = new SOSOptionZeroByteTransfer(this, conClassName + ".zero_byte_transfer",
            "This parameter specifies whether zero byte files", "yes", "yes", false);

    public SOSOptionZeroByteTransfer TransferZeroByteFiles = (SOSOptionZeroByteTransfer) zero_byte_transfer.SetAlias("transfer_zero_byte_files");

    public SOSFtpOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public SOSFtpOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public SOSFtpOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    private String getAllOptionsAsString() {
        return conClassName + "\n" + this.toString();
    }

    public void setAllOptions(final Properties pobjProperties) {
        HashMap<String, String> map = new HashMap<String, String>((Map) pobjProperties);
        try {
            super.setAllOptions(map);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void CheckMandatory() throws com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(final String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

    @Override
    public SOSOptionHostName getHost() {
        return host;
    }

    @Override
    public SOSOptionPortNumber getPort() {
        return port;
    }

    @Override
    public SOSOptionString getProxy_host() {
        return null;
    }

    @Override
    public SOSOptionPassword getProxy_password() {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public SOSOptionPortNumber getProxy_port() {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public SOSOptionUserName getProxy_user() {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public void setHost(final SOSOptionHostName host) {
        this.sethost(host);
    }

    @Override
    public void setPort(final SOSOptionPortNumber port) {
        // TO DO Auto-generated method stub
    }

    @Override
    public void setProxy_host(final SOSOptionString proxyHost) {
        // TO DO Auto-generated method stub
    }

    @Override
    public void setProxy_password(final SOSOptionPassword proxyPassword) {
    }

    @Override
    public void setProxy_port(final SOSOptionPortNumber proxyPort) {
    }

    @Override
    public void setProxy_user(final SOSOptionUserName proxyUser) {
        // TO DO Auto-generated method stub
    }

    @Override
    public SOSOptionInFileName getAuth_file() {
        return ssh_auth_file;
    }

    @Override
    public SOSOptionAuthenticationMethod getAuth_method() {
        return ssh_auth_method;
    }

    @Override
    public SOSOptionPassword getPassword() {
        return password;
    }

    @Override
    public SOSOptionUserName getUser() {
        return user;
    }

    @Override
    public void setAuth_file(final SOSOptionInFileName authFile) {
        ssh_auth_file = authFile;
    }

    @Override
    public void setAuth_method(final SOSOptionAuthenticationMethod authMethod) {
        // TO DO Auto-generated method stub
    }

    @Override
    public void setPassword(final SOSOptionPassword password) {
        // TO DO Auto-generated method stub
    }

    @Override
    public void setUser(final SOSOptionUserName pobjUser) {
        user.Value(pobjUser.Value());
    }

    @Override
    public SOSOptionRegExp getfile_spec2() {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public void setfile_spec2(final SOSOptionRegExp p_file_spec2) {
        // TO DO Auto-generated method stub
    }

    @Override
    public SOSOptionFolderName SourceDir() {
        return SourceDir;
    }

    @Override
    public SOSOptionFolderName TargetDir() {
        return TargetDir;
    }

    @JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error",
            type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean raise_exception_on_error = new SOSOptionBoolean(this, conClassName + ".raise_exception_on_error",
            "Raise an Exception if an error occured", "true", "true", true);

    public SOSOptionBoolean getraise_exception_on_error() {
        return raise_exception_on_error;
    }

    public void setraise_exception_on_error(final SOSOptionBoolean raiseExceptionOnError) {
        this.raise_exception_on_error = raiseExceptionOnError;
    }

}