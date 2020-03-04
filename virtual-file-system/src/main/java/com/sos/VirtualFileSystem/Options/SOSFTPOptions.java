package com.sos.VirtualFileSystem.Options;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.keepass.SOSKeePassPath;

import sos.configuration.SOSConfiguration;
import sos.net.mail.options.SOSSmtpMailOptions;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFTPOptions extends SOSFtpOptionsSuperClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFTPOptions.class);
    private static final long serialVersionUID = -8219289268940238015L;

    private static final String PREFIX_FILE_URI = "file://";
    private static final String PREFIX_SCHEDULER_ENV_VAR = "scheduler_param_";
    private static final String PREFIX_SOSFTP_ENV_VAR = "sosftp_";
    private static final String FILE_SEPARATOR = "file.separator";
    private static final String OPERATION_RECEIVE = "receive";
    private static final String OPERATION_SEND = "send";
    private SOSConnection2Options connectionOptions;
    private SOSSmtpMailOptions mailOptions;
    private Map<String, String> dmzOptions = new HashMap<String, String>();
    private Properties allEnvVars = null;
    private Properties envVars = null;
    private Properties schedulerParams = null;
    private boolean checkMandatoryDone = false;
    private boolean readSettingsFileIsActive = false;
    private boolean settingsFileProcessed = false;
    private boolean deleteSettingsFileOnExit = false;
    private boolean cumulativeTargetDeleted = false;
    private String originalSettingsFile = null;
    private String jobSchedulerId;
    private String jobChain;
    private String jobChainNodeName;
    private String job;
    private String orderId;
    private String taskId;
    private Long parentTransferId;

    private final Map<String, String> includeDirectives = new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;
        {
            put("include", "");
            put("source_include", "source_");
            put("target_include", "target_");
            put("jump_include", "jump_");
            put("alternate_source_include", "alternative_source_");
            put("alternate_target_include", "alternative_target_");
            put("source_alternate_include", "alternative_source_");
            put("target_alternate_include", "alternative_target_");
            put("alternative_source_include", "alternative_source_");
            put("alternative_target_include", "alternative_target_");
            put("source_alternative_include", "alternative_source_");
            put("target_alternative_include", "alternative_target_");
            // used by SOSFTP?
            put("alternate_include", "alternate_");
            put("alternatesource_include", "alternatesource_");
            put("alternatetarget_include", "alternatetarget_");
        }
    };

    public SOSFTPOptions(final TransferTypes type) {
        super();
        switch (type) {
        case webdav:
            authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            getSource().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            getTarget().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(TransferTypes.webdav.name(), TransferTypes.webdav.name());
            getSource().protocol.changeDefaults(TransferTypes.webdav.name(), TransferTypes.webdav.name());
            getTarget().protocol.changeDefaults(TransferTypes.webdav.name(), TransferTypes.webdav.name());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            getSource().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            getTarget().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        case http:
        case https:
            authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            getSource().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            getTarget().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(TransferTypes.http.name(), TransferTypes.http.name());
            getSource().protocol.changeDefaults(TransferTypes.http.name(), TransferTypes.http.name());
            getTarget().protocol.changeDefaults(TransferTypes.http.name(), TransferTypes.http.name());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            getSource().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            getTarget().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        default:
            break;
        }
    }

    public SOSFTPOptions(final TransferTypes source, final TransferTypes target) {
        super();
        switch (source) {
        case webdav:
            authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(TransferTypes.webdav.name(), TransferTypes.webdav.name());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        default:
            break;
        }
        changeDefaults(source, getSource());
        changeDefaults(target, getTarget());
    }

    public SOSFTPOptions() {
        super();
    }

    public SOSFTPOptions(final HashMap<String, String> settings) throws Exception {
        super(settings);
    }

    private void changeDefaults(final TransferTypes type, final SOSConnection2OptionsAlternate options) {
        switch (type) {
        case sftp:
            options.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            options.protocol.changeDefaults(TransferTypes.sftp.name(), TransferTypes.sftp.name());
            options.port.changeDefaults(SOSOptionPortNumber.conPort4SFTP, SOSOptionPortNumber.conPort4SFTP);
            break;
        case ssh:
            options.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            options.protocol.changeDefaults(TransferTypes.ssh.name(), TransferTypes.ssh.name());
            options.port.changeDefaults(SOSOptionPortNumber.conPort4SFTP, SOSOptionPortNumber.conPort4SFTP);
            break;
        case local:
            options.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            options.protocol.changeDefaults(TransferTypes.local.name(), TransferTypes.local.name());
            options.port.changeDefaults(0, 0);
            break;
        case ftp:
            options.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            options.protocol.changeDefaults(TransferTypes.ftp.name(), TransferTypes.ftp.name());
            options.port.changeDefaults(SOSOptionPortNumber.conPort4FTP, SOSOptionPortNumber.conPort4FTP);
            break;
        case ftps:
            options.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            options.protocol.changeDefaults(TransferTypes.ftps.name(), TransferTypes.ftps.name());
            options.port.changeDefaults(SOSOptionPortNumber.conPort4FTPS, SOSOptionPortNumber.conPort4FTPS);
            break;
        case webdav:
            options.authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            options.protocol.changeDefaults(TransferTypes.webdav.name(), TransferTypes.webdav.name());
            options.port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        default:
            break;
        }
    }

    public SOSSmtpMailOptions getMailOptions() {
        if (mailOptions == null) {
            mailOptions = new SOSSmtpMailOptions();
        }
        return mailOptions;
    }

    @SuppressWarnings("unchecked")
    public void setChildClasses(final Properties properties) {
        @SuppressWarnings("rawtypes")
        HashMap<String, String> map = new HashMap<String, String>((Map) properties);
        try {
            setChildClasses(map);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private void setChildClasses(final HashMap<String, String> settings) {
        try {
            if (connectionOptions == null) {
                connectionOptions = new SOSConnection2Options(settings);
            } else {
                connectionOptions.setPrefixedValues(settings);
            }
            if (mailOptions == null) {
                mailOptions = new SOSSmtpMailOptions(settings);
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    public void adjustDefaults() {
        if (operation.getValue().equalsIgnoreCase(enuJadeOperations.move.getText())) {
            removeFiles.value(true);
            removeFiles.setProtected(operation.isProtected());
        }
        if (transactionMode.isTrue() && !isAtomicTransfer()) {
            atomicSuffix.setValue("~");
        }
        if (operation.getValue().equalsIgnoreCase(enuJadeOperations.getlist.getText())) {
            removeFiles.value(false);
        }
        String localDir = this.localDir.getValue();
        if (isEmpty(localDir)) {
            this.localDir.set(sourceDir);
            localDir = this.localDir.getValue();
        }
        checkReplaceAndReplacing(getTarget());
        checkReplaceAndReplacing(getSource());
        if (replacing.isNotEmpty() && replacement.isNull()) {
            replacement.setValue("");
        }
        if (this.getTarget().protocol.isDirty() && !protocol.isDirty()) {
            protocol.set(getTarget().protocol);
        }
        setDefaultHostPort(protocol, port, host);
        setDefaultHostPort(getSource().protocol, getSource().port, getSource().host);
        setDefaultHostPort(getTarget().protocol, getTarget().port, getTarget().host);
        setDefaultHostPort(getSource().getAlternatives().protocol, getSource().getAlternatives().port, getSource().getAlternatives().host);
        setDefaultHostPort(getTarget().getAlternatives().protocol, getTarget().getAlternatives().port, getTarget().getAlternatives().host);
        getDataSourceType();
        getDataTargetType();
    }

    @Override
    public void checkMandatory() {
        if (checkMandatoryDone) {
            return;
        }
        operation.checkMandatory();
        if (operation.getValue().equalsIgnoreCase(enuJadeOperations.move.getText())) {
            removeFiles.value(true);
        }
        if (transactionMode.isTrue() && !isAtomicTransfer()) {
            atomicSuffix.setValue("~");
        }
        if (operation.getValue().equalsIgnoreCase(enuJadeOperations.getlist.getText())) {
            removeFiles.setFalse();
        }
        checkURLParameter(getConnectionOptions().getSource());
        checkURLParameter(getConnectionOptions().getTarget());
        String localDir = this.localDir.getValue();
        if (isEmpty(localDir)) {
            this.localDir.setValue(sourceDir.getValue());
            localDir = this.localDir.getValue();
            localDir += "";
        }
        if (getSource().url.isDirty() && sourceDir.isNotDirty()) {
            sourceDir.setValue(getSource().url.getFolderName());
        }
        if (getTarget().url.isDirty() && targetDir.isNotDirty()) {
            targetDir.setValue(getTarget().url.getFolderName());
        }
        checkCredentialStore(getSource());
        checkCredentialStore(getTarget());
        if (getSource().replacing.isNotEmpty() && getSource().replacement.isNotEmpty()) {
            removeFiles.setFalse();
        }
        super.checkMandatory();
        if (localDir.startsWith("\\\\")) {
            while (localDir.indexOf("\\") != -1) {
                localDir = localDir.replace('\\', '/');
            }
        }
        this.localDir.setValue(localDir);
        if (localDir.startsWith(PREFIX_FILE_URI) && !(new File(createURI(localDir)).exists())) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0010.params(localDir));
        }
        checkReplaceAndReplacing(getTarget());
        checkReplaceAndReplacing(getSource());
        if (replacing.isNotEmpty() && replacement.isNull()) {
            replacement.setValue("");
        }
        if (replacing.IsEmpty() && replacement.isNotEmpty()) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0020.params(replacement.getKey(), replacing.getKey()));
        }
        if (appendFiles.value()) {
            String appendFilesKey = appendFiles.getKey();
            if (isAtomicTransfer()) {
                String name = getOptionNamesAsString(new SOSOptionElement[] { atomicPrefix, atomicSuffix });
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0050.params(appendFilesKey, name));
            }
            if (compressFiles.value()) {
                String name = getOptionNamesAsString(new SOSOptionElement[] { compressFiles });
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0050.params(appendFilesKey, name));
            }
            if (!"ftp".equalsIgnoreCase(protocol.getValue())) {
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0040.params(appendFilesKey, protocol.getValue()));
            }
        }
        if (this.getTarget().protocol.isDirty() && !protocol.isDirty()) {
            protocol.setValue(getTarget().protocol.getValue());
        }
        setDefaultHostPort(protocol, port, host);
        setDefaultHostPort(getSource().protocol, getSource().port, getSource().host);
        setDefaultHostPort(getTarget().protocol, getTarget().port, getTarget().host);
        setDefaultHostPort(getSource().getAlternatives().protocol, getSource().getAlternatives().port, getSource().getAlternatives().host);
        setDefaultHostPort(getTarget().getAlternatives().protocol, getTarget().getAlternatives().port, getTarget().getAlternatives().host);
        setDefaultAuth(getSource().protocol, getSource());
        setDefaultAuth(getTarget().protocol, getTarget());
        setDefaultAuth(getSource().getAlternatives().protocol, getSource().getAlternatives());
        setDefaultAuth(getTarget().getAlternatives().protocol, getTarget().getAlternatives());
        if (filePath.isDirty() && fileSpec.isDirty()) {
            filePath.setValue("");
        }
        if (filePath.IsEmpty() && sourceDir.IsEmpty() && this.getSource().directory.IsEmpty() && fileListName.IsEmpty()) {
            throw new JobSchedulerException(String.format("SOSVfs-E-0000: one of these parameters must be specified: '%1$s', '%2$s', '%3$s'", filePath
                    .getShortKey(), "source_dir", fileListName.getShortKey()));
        }

        if (protocolCommandListener.isDirty()) {
            if (getSource().protocolCommandListener.isNotDirty()) {
                getSource().protocolCommandListener.value(protocolCommandListener.value());
            }
            if (getTarget().protocolCommandListener.isNotDirty()) {
                getTarget().protocolCommandListener.value(protocolCommandListener.value());
            }
        }

        getDataSourceType();
        getDataTargetType();
        if (checkNotProcessedOptions.value()) {
            checkNotProcessedOptions();
        }
        checkMandatoryDone = true;
    }

    private void checkReplaceAndReplacing(final SOSConnection2OptionsSuperClass options) {
        if (options.replacing.isNotEmpty() && options.replacement.isNull()) {
            options.replacement.setValue("");
        }
        if (options.replacing.IsEmpty() && options.replacement.isNotEmpty()) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0020.params(options.replacement.getKey(), options.replacing.getKey()));
        }
    }

    private void checkCredentialStore(final SOSConnection2OptionsAlternate options) {
        if (options.getCredentialStore() != null) {
            options.checkCredentialStoreOptions();
        }
    }

    private void checkURLParameter(final SOSConnection2OptionsAlternate options) {
        if (options.url.isDirty()) {
            options.url.getOptions(options);
        }
    }

    private void setDefaultAuth(final SOSOptionTransferType type, final SOSConnection2OptionsAlternate options) {
        TransferTypes transferType = type.getEnum();
        if ((type.isHTTP() || transferType.equals(TransferTypes.webdav)) && !options.authMethod.isDirty() && !options.sshAuthMethod.isDirty()) {
            options.authMethod.setValue(enuAuthenticationMethods.url);
            options.sshAuthMethod.setValue(enuAuthenticationMethods.url);
        }
        if (transferType.equals(TransferTypes.local)) {
            options.user.setValue(System.getProperty("user.name"));
            options.user.setNotDirty();
            user.setValue(System.getProperty("user.name"));
            user.setNotDirty();
        }
    }

    private void setDefaultHostPort(final SOSOptionTransferType type, final SOSOptionPortNumber port, final SOSOptionHostName host) {
        TransferTypes transferType = type.getEnum();
        switch (transferType) {
        case sftp:
            port.setDefaultValue("" + SOSOptionPortNumber.conPort4SFTP);
            break;
        case ftp:
            port.setDefaultValue("" + SOSOptionPortNumber.conPort4FTP);
            break;
        case zip:
        case local:
            port.setDefaultValue("0");
            if (host.isNotDirty() || "localhost".equalsIgnoreCase(host.getValue()) || "127.0.0.1".equalsIgnoreCase(host.getValue())) {
                host.setValue(SOSOptionHostName.getLocalHost());
                host.setNotDirty();
            }
            break;
        case ftps:
            port.setDefaultValue("" + SOSOptionPortNumber.conPort4FTPS);
            break;
        case webdav:
        case http:
        case https:
            if (host.getValue().toLowerCase().startsWith("https://")) {
                port.setDefaultValue("" + SOSOptionPortNumber.conPort4https);
            } else {
                port.setDefaultValue("" + SOSOptionPortNumber.conPort4http);
            }
            break;
        default:
            break;
        }
        if (port.isNotDirty()) {
            port.setValue(port.getDefaultValue());
            port.setNotDirty();
            port.setProtected(type.isProtected());
        }
    }

    private String getOptionNamesAsString(final SOSOptionElement[] elements) {
        StringBuilder sb = new StringBuilder();
        for (SOSOptionElement element : elements) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(element.getKey()).append("=").append(element.getValue());
        }
        return sb.toString();
    }

    @Override
    public boolean isAtomicTransfer() {
        return atomicPrefix.isNotEmpty() || atomicSuffix.isNotEmpty();
    }

    private Properties getEnvVars() throws Exception {
        try {
            envVars = new Properties();
            schedulerParams = new Properties();
            int len = PREFIX_SOSFTP_ENV_VAR.length();
            Map<String, String> systemEnvs = System.getenv();
            allEnvVars = new Properties();
            allEnvVars.putAll(systemEnvs);
            for (Object k : allEnvVars.keySet()) {
                String key = (String) k;
                String value = (String) allEnvVars.get(k);
                if (key.startsWith(PREFIX_SOSFTP_ENV_VAR)) {
                    key = key.substring(len);
                    envVars.setProperty(key, value);
                    continue;
                }
                if (key.startsWith("current_pid") || key.startsWith("ppid")) {
                    envVars.setProperty(key, value);
                    continue;
                }
                if (key.indexOf(PREFIX_SCHEDULER_ENV_VAR) > -1) {
                    schedulerParams.setProperty(key.substring(PREFIX_SCHEDULER_ENV_VAR.length()), value);
                    continue;
                }
            }
            return envVars;
        } catch (Exception e) {
            String msg = SOSVfsMessageCodes.SOSVfs_E_161.params("reading environment", e.toString());
            LOGGER.error(msg, e);
            throw new JobSchedulerException(msg, e);
        }
    }

    @Override
    public void commandLineArgs(final String[] args) {
        super.commandLineArgs(args);
        this.setAllOptions(super.objSettings);
        boolean found = false;
        for (int i = 0; i < args.length; i++) {
            String strParam = args[i];
            if (strParam.toLowerCase().startsWith("-settings")) {
                args[i] = "-ignored=ignored";
                found = true;
            }
        }
        if (found) {
            super.commandLineArgs(args);
        }
    }

    @Override
    public void commandLineArgs(final String args) {
        try {
            this.commandLineArgs(args.split(" "));
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_153.params("command lines args"), e);
        }
    }

    @Override
    public void setAllOptions(final HashMap<String, String> settings) {
        objSettings = settings;
        super.setSettings(objSettings);
        super.setAllOptions(settings);
        HashMap<String, String> map = settings;
        if (!settingsFileProcessed && !readSettingsFileIsActive && configurationFile.isNotEmpty()) {
            readSettingsFileIsActive = true;
            map = readSettingsFile();
            readSettingsFileIsActive = false;
            settingsFileProcessed = true;
        }
        setChildClasses(map);
    }

    private boolean isEmpty(HashMap<String, String> params, String key) {
        return ((params.get(key) == null) || params.get(key).length() == 0);
    }

    private boolean isEqualIgnoreCase(HashMap<String, String> params, String key, String value) {
        return ((params.get(key) != null) && params.get(key).equalsIgnoreCase(value));
    }

    private void handleFileOrderSource(HashMap<String, String> params) {
        boolean b = false;
        b = ((isEmpty(params, "file_path") || isEqualIgnoreCase(params, "file_path", "${scheduler_file_path}")) && isEmpty(params, "source_dir")
                && isEmpty(params, "local_dir") && isEmpty(params, "file_spec"));
        if (b && !isEmpty(params, "scheduler_file_path")) {
            LOGGER.debug(String.format("Using value from parameter SCHEDULER_FILE_PATH %s for the parameter file_path, as no file_path, local_dir, "
                    + "file_spec or source_dir has been specified", params.get("scheduler_file_path")));
            params.put("file_path", params.get("scheduler_file_path"));
        }

        b = (!isEmpty(params, "scheduler_file_path") && isEqualIgnoreCase(params, "file_path", "${scheduler_file_name}") && isEmpty(params,
                "file_spec"));
        if (b) {
            File f = new File(params.get("scheduler_file_path"));
            String path = params.get("source_dir");
            String basename = f.getName();
            LOGGER.debug(String.format(
                    "Using base filename %s from parameter SCHEDULER_FILE_PATH %s and path %s for the parameter file_path, as file_path=${scheduler_file_name} "
                            + "and no file_spec has been specified", basename, params.get("scheduler_file_path"), path));
            params.put("file_path", basename);
        }
    }

    public void setAllOptions2(HashMap<String, String> params) {
        Map<String, String> mapFromIniFile = new HashMap<String, String>();
        if (!settingsFileProcessed && !readSettingsFileIsActive && params.containsKey("settings") && params.containsKey("profile")) {
            settings.setValue(params.get("settings"));
            profile.setValue(params.get("profile"));
            readSettingsFileIsActive = true;
            mapFromIniFile = readSettingsFile(params);
            readSettingsFileIsActive = false;
            settingsFileProcessed = true;
        }
        params.putAll(mapFromIniFile);
        handleFileOrderSource(params);
        objSettings = params;
        super.setSettings(params);
        super.setAllOptions(params);
        setChildClasses(params);
    }

    public HashMap<String, String> readSettingsFile() {
        return readSettingsFile(null);
    }

    public HashMap<String, String> readSettingsFile(Map<String, String> beatParams) {
        settings.checkMandatory();
        profile.checkMandatory();
        HashMap<String, String> map = new HashMap<String, String>();
        SOSStandardLogger sosLogger = null;
        SOSConfiguration conf = null;
        Properties properties = new Properties();
        try {
            LOGGER.debug(String.format("readSettingsFile: settings=%s", settings.getValue()));
            sosLogger = new SOSStandardLogger(0);
            getEnvVars();
            conf = new SOSConfiguration(settings.getValue(), profile.getValue());
            Properties profileProps = conf.getParameterAsProperties();
            if (profileProps.isEmpty()) {
                String sf = originalSettingsFile == null ? settings.getValue() : originalSettingsFile;
                throw new JobSchedulerException(String.format("[%s]not found profile=%s", sf, profile.getValue()));
            }
            conf = new SOSConfiguration(settings.getValue(), "globals");
            Properties globalsProps = conf.getParameterAsProperties();
            globalsProps = resolveIncludes(globalsProps, sosLogger);
            properties.putAll(globalsProps);
            profileProps = resolveIncludes(profileProps, sosLogger);
            properties.putAll(profileProps);
            // Additional Variables
            properties.put("uuid", UUID.randomUUID().toString());
            properties.put("date", SOSOptionTime.getCurrentDateAsString());
            properties.put("time", SOSOptionTime.getCurrentTimeAsString("hh:mm:ss"));
            properties.put("local_user", System.getProperty("user.name"));
            Properties props4Substitute = new Properties();
            props4Substitute.put("profile", profile.getValue());
            props4Substitute.put("settings", settings.getValue());
            try {
                java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
                properties.put("localhost", localMachine.getHostName());
                properties.put("local_host_ip", localMachine.getHostAddress());
            } catch (Exception e) {
                LOGGER.debug(e.toString());
                properties.put("localhost", "localhost");
                properties.put("local_host_ip", "127.0.0.1");
            }
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                String key = (String) e.getKey();
                String value = (String) e.getValue();
                if (beatParams != null && beatParams.containsKey(key)) {
                    value = beatParams.get(key);
                }
                if (hasVariableToSubstitute(value) == true && gflgSubsituteVariables == true) {

                    LOGGER.trace("ReadSettingsFile() - key = " + key + ", value = " + value);
                    value = substituteVariables(value, properties);
                    value = substituteVariables(value, props4Substitute);
                    value = substituteVariables(value, envVars);
                    value = substituteVariables(value, allEnvVars);
                    value = substituteVariables(value, schedulerParams);
                    if (hasVariableToSubstitute(value)) {
                        switch (key) {
                        case "source_pre_command":
                        case "source_post_command":
                        case "source_tfn_post_command":

                        case "target_pre_command":
                        case "target_post_command":
                        case "target_tfn_post_command":

                        case "jump_post_transfer_commands_on_error":
                        case "jump_post_transfer_commands_final":
                        case "jump_post_transfer_commands_on_success":
                        case "jump_pre_transfer_commands":

                        case "file_path":
                            break;
                        default:
                            if (!SOSKeePassPath.hasKeePassVariables(value)) {
                                LOGGER.warn(SOSVfsMessageCodes.SOSVfs_W_0070.params(value, key));
                            }
                        }
                    }
                    value = unescape(value);
                }
                map.put(key, value);
            }
            super.setAllOptions(map);
            setChildClasses(map);
        } catch (JobSchedulerException e) {
            LOGGER.error("ReadSettingsFile(): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("ReadSettingsFile(): " + e.getMessage());
            throw new JobSchedulerException(e);
        }
        return map;
    }

    private Properties resolveIncludes(Properties props, SOSLogger logger) throws Exception {
        return resolveIncludes(props, "", logger);
    }

    private Properties resolveIncludes(Properties props, String prefix, SOSLogger logger) throws Exception {
        Properties allIncludedProps = new Properties();
        SOSConfiguration conf = null;
        if (prefix == null) {
            prefix = "";
        }
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            String key = (String) e.getKey();
            key = key.substring(prefix.length());
            String value = (String) e.getValue();
            if (isIncludeDirective(key)) {
                String[] includes = value.split("[;|,]");
                String includePrefix = getIncludePrefix(prefix + key);
                if (includePrefix == null) {
                    continue;
                }
                for (String include : includes) {
                    include = include.trim();
                    conf = new SOSConfiguration(settings.getValue(), include);
                    Properties includedProps = conf.getParameterAsProperties(includePrefix);
                    if (includedProps.isEmpty()) {
                        String strM = SOSVfsMessageCodes.SOSVfs_E_0000.params(include, settings.getValue());
                        throw new JobSchedulerException(strM);
                    }
                    includedProps = resolveIncludes(includedProps, includePrefix, logger);
                    allIncludedProps.putAll(includedProps);
                }
            }
        }
        for (Map.Entry<Object, Object> e : allIncludedProps.entrySet()) {
            if (!props.containsKey(e.getKey())) {
                props.put(e.getKey(), e.getValue());
            }
        }
        return props;
    }

    private boolean hasVariableToSubstitute(String value) {
        boolean result = false;
        value = " " + value.toLowerCase().replaceAll("(\\$|%)\\{(source|target)(transfer)?filename\\}", "").replaceAll(
                "%(source|target)(transfer)?filename%", "");
        if (value.matches("^.*[^\\\\](\\$|%)\\{[^/\\}\\\\]+\\}.*$") || value.matches("^.*[^\\\\]%[^/%\\\\]+%.*$")) {
            result = true;
        }
        return result;
    }

    private String unescape(String value) {
        return value.replaceAll("\\\\((?:\\$|%)\\{[^/\\}\\\\]+\\})", "$1").replaceAll("\\\\(%[^/%\\\\]+%)", "$1");
    }

    public boolean isIncludeDirective(final String includeDirective) {
        return includeDirectives.containsKey(includeDirective);
    }

    public String getIncludePrefix(final String includeDirective) {
        return includeDirectives.get(includeDirective);
    }

    public String substituteVariables(String txt, final Properties prop) {
        Map<String, String> startEndCharsForSubstitute = new HashMap<String, String>();
        startEndCharsForSubstitute.put("${", "}");
        startEndCharsForSubstitute.put("%{", "}");
        startEndCharsForSubstitute.put("%", "%");
        for (Map.Entry<String, String> e : startEndCharsForSubstitute.entrySet()) {
            txt = substituteVariables(txt, prop, e.getKey(), e.getValue());
        }
        return txt;
    }

    public String substituteVariables(String txt, final Properties prop, final String startPrefix, final String endPrefix) {
        try {
            for (Object k : prop.keySet()) {
                String key = (String) k;
                String value = (String) prop.get(key);
                String searchFor = startPrefix + key + endPrefix;
                int pos1 = -1;
                int pos2 = 0;
                while (true) {
                    pos1 = txt.indexOf(searchFor, pos2);
                    if (pos1 == -1) {
                        break;
                    }
                    int intEscaped = txt.indexOf("\\" + searchFor);
                    if (intEscaped > -1 && intEscaped == pos1 - 1) {
                        pos1 = -1;
                    }
                    pos2 = pos1 + searchFor.length();
                    if (pos1 > -1 && pos2 > pos1) {
                        txt = txt.substring(0, pos1) + value + txt.substring(pos2);
                    }
                }
            }
            return txt;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_197.params(txt), e);
        }
    }

    protected URI createURI(final String fileName) {
        URI uri = null;
        try {
            uri = new URI(fileName);
        } catch (Exception e) {
            try {
                File f = new File(fileName);
                String path = f.getCanonicalPath();
                if (fileName.startsWith("/")) {
                    path = fileName;
                }
                String fs = System.getProperty(FILE_SEPARATOR);
                if (fs.length() == 1) {
                    char sep = fs.charAt(0);
                    if (sep != '/') {
                        path = path.replace(sep, '/');
                    }
                    if (path.charAt(0) != '/') {
                        path = '/' + path;
                    }
                }
                if (!path.startsWith(PREFIX_FILE_URI)) {
                    path = PREFIX_FILE_URI + path;
                }
                uri = new URI(path);
            } catch (Exception ex) {
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_198.params(e.getMessage()));
            }
        }
        return uri;
    }

    public boolean oneOrMoreSingleFilesSpecified() {
        return filePath.isNotEmpty() || fileListName.isNotEmpty();
    }

    public String getDataTargetType() {
        String targetType = "";
        if (OPERATION_SEND.equalsIgnoreCase(operation.getValue())) {
            targetType = protocol.getValue();
            if (targetType.isEmpty()) {
                targetType = TransferTypes.local.name();
            }
            copyValue(sourceDir, localDir);
            copyValue(targetDir, remoteDir);
            copyValue(getSource().directory, localDir);
            copyValue(getTarget().directory, remoteDir);
            changeOptions(getConnectionOptions().getTarget());
        } else if (OPERATION_RECEIVE.equalsIgnoreCase(operation.getValue())) {
            targetType = TransferTypes.local.name();
            copyValue(sourceDir, remoteDir);
            copyValue(targetDir, localDir);
            copyValue(getSource().directory, remoteDir);
            copyValue(getTarget().directory, localDir);
            changeOptions2Local(getConnectionOptions().getTarget());
        } else {
            targetType = getConnectionOptions().getTarget().protocol.getValue();
            if (targetType.isEmpty()) {
                targetType = TransferTypes.local.name();
            }
            changeDirValues();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[target]%s", targetType));
        }
        return targetType;
    }

    private void changeOptions(final SOSConnection2OptionsAlternate options) {
        options.host.set(host);
        LOGGER.debug("prefix_host = " + options.host.getValue());
        options.user.setValue(user.getValue());
        options.password.set(password);
        options.port.set(port);
        options.protocol.set(protocol);
        options.passiveMode.set(passiveMode);
        options.transferMode.set(transferMode);
        options.sshAuthFile.setIfNotDirty(sshAuthFile);
        options.sshAuthMethod.setIfNotDirty(sshAuthMethod);

        SOSConnection2OptionsSuperClass alternate = options.getAlternatives();
        alternate.host.setValue(alternativeHost.getValue());
        alternate.port.value(alternativePort.value());
        alternate.protocol.setValue(protocol.getValue());
        alternate.passiveMode.setValue(alternativePassiveMode.getValue());
        alternate.transferMode.setValue(alternativeTransferMode.getValue());
    }

    private void changeOptions2Local(final SOSConnection2OptionsAlternate options) {
        options.host.setValue(SOSOptionHostName.getLocalHost());
        options.user.setValue("");
        options.password.setValue("");
        options.port.value(0);
        options.protocol.setValue("local");
        options.passiveMode.setValue("");
        options.transferMode.setValue("");

        SOSConnection2OptionsSuperClass alternate = options.getAlternatives();
        alternate.host.setValue(options.host.getValue());
        alternate.port.value(0);
        alternate.protocol.setValue("local");
        alternate.passiveMode.setValue("");
        alternate.transferMode.setValue("");
    }

    private void copyValue(final SOSOptionElement to, final SOSOptionElement from) {
        if (to.isNotDirty()) {
            to.setValue(from.getValue());
        }
    }

    public String getDataSourceType() {
        String sourceType = "";
        if (OPERATION_SEND.equalsIgnoreCase(operation.getValue())) {
            sourceType = TransferTypes.local.name();
            changeDirValues();
            SOSConnection2OptionsAlternate options = getConnectionOptions().getSource();
            options.host.setValue(SOSOptionHostName.getLocalHost());
            options.port.value(0);
            options.protocol.setValue(sourceType);
            options = getConnectionOptions().getTarget();
            options.host = host;
            options.port = port;
            options.protocol = protocol;
            options.user = user;
            options.password = password;
            options.sshAuthFile = sshAuthFile;
            options.sshAuthMethod = sshAuthMethod;
            options.passiveMode = passiveMode;

            SOSConnection2OptionsSuperClass alternate = options.getAlternatives();
            alternate.host.setValue(alternativeHost.getValue());
            alternate.port.value(alternativePort.value());
            alternate.protocol.setValue(protocol.getValue());
            alternate.passiveMode.setValue(alternativePassiveMode.getValue());
        } else if (OPERATION_RECEIVE.equalsIgnoreCase(operation.getValue())) {
            sourceType = protocol.getValue();
            if (sourceType.isEmpty()) {
                sourceType = TransferTypes.local.name();
            }
            changeDirValues4Receive();
            SOSConnection2OptionsAlternate options = getConnectionOptions().getSource();
            options.host.setValue(host.getValue());
            options.port.value(port.value());
            options.protocol.setValue(protocol.getValue());
            options.passiveMode.setValue(passiveMode.getValue());
            options.user = user;
            options.password = password;
            options.sshAuthFile = sshAuthFile;
            options.sshAuthMethod = sshAuthMethod;
            options = getConnectionOptions().getTarget();
            options.host.setValue(SOSOptionHostName.getLocalHost());
            options.port.value(0);
            options.protocol.setValue(TransferTypes.local.name());

            SOSConnection2OptionsSuperClass alternate = options.getAlternatives();
            alternate.host.setValue(alternativeHost.getValue());
            alternate.port.value(alternativePort.value());
            alternate.protocol.setValue(protocol.getValue());
            alternate.passiveMode.setValue(alternativePassiveMode.getValue());
        } else {
            sourceType = this.getConnectionOptions().getSource().protocol.getValue();
            if (sourceType.isEmpty()) {
                sourceType = TransferTypes.local.name();
            }
            changeDirValues();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[source]%s", sourceType));
        }
        return sourceType;
    }

    private void changeDirValues() {
        changeValue(sourceDir, localDir);
        changeValue(targetDir, remoteDir);
        changeValue(localDir, sourceDir);
        changeValue(remoteDir, targetDir);
        changeValue(getSource().folderName, sourceDir);
        changeValue(getTarget().folderName, targetDir);
    }

    private void changeDirValues4Receive() {
        changeValue(sourceDir, remoteDir);
        changeValue(targetDir, localDir);
    }

    private void changeValue(final SOSOptionElement target, final SOSOptionElement source) {
        if (target.IsEmpty() && !source.IsEmpty()) {
            if (source instanceof SOSOptionPassword) {
                LOGGER.trace(SOSVfsMessageCodes.SOSVfs_I_263.params(target.getKey(), "*****"));
            } else {
                LOGGER.trace(SOSVfsMessageCodes.SOSVfs_I_263.params(target.getKey(), source.getValue()));
            }
            target.set(source);
        }
    }

    public boolean isDoNotOverwrite() {
        return !overwriteFiles.value() && !appendFiles.value();
    }

    public SOSConnection2Options getConnectionOptions() {
        if (connectionOptions == null) {
            connectionOptions = new SOSConnection2Options();
        }
        return connectionOptions;
    }

    public void setConnectionOptions(final SOSConnection2Options val) {
        connectionOptions = val;
    }

    public boolean isReplaceReplacingInEffect() {
        return this.getReplacing().isNotEmpty();
    }

    public SOSConnection2OptionsAlternate getSource() {
        return getConnectionOptions().getSource();
    }

    public SOSConnection2OptionsAlternate getTarget() {
        return getConnectionOptions().getTarget();
    }

    public boolean isNeedTargetClient() {
        boolean result = true;
        switch (operation.value()) {
        case delete:
        case getlist:
        case rename:
            result = false;
            break;
        default:
            break;
        }
        return result;
    }

    public SOSFTPOptions getClone() {
        SOSFTPOptions options = new SOSFTPOptions();
        options.commandLineArgs(getOptionsAsCommandLine());
        return options;
    }

    public void clearJumpParameter() {
        String nullString = null;
        jumpUser.setValue(nullString);
        jumpPassword.setValue(nullString);
        jumpProtocol.setValue(nullString);
        jumpHost.setValue(nullString);
        jumpSshAuthMethod.setValue(nullString);
        jumpCommand.setValue(nullString);
        host.setValue(nullString);
    }

    public boolean isFilePollingEnabled() {
        boolean result = false;
        if ((pollTimeout.isDirty() || pollingDuration.isDirty()) && skipTransfer.isFalse()) {
            result = true;
        }
        return result;
    }

    public String dirtyString() {
        StringBuilder sb = new StringBuilder("\n").append(super.dirtyString());
        sb.append("\n").append(getSource().dirtyString());
        sb.append("\n").append(getTarget().dirtyString());
        return sb.toString();
    }

    @Override
    public SOSOptionRegExp getReplacing() {
        SOSOptionRegExp val = super.getReplacing();
        if (getTarget().getReplacing().isDirty()) {
            val = getTarget().getReplacing();
        }
        return val;
    }

    @Override
    public SOSOptionString getReplacement() {
        SOSOptionString val = super.getReplacement();
        if (getTarget().getReplacement().isDirty()) {
            val = getTarget().getReplacement();
        }
        return val;
    }

    @Override
    public SOSOptionBoolean getRaiseExceptionOnError() {
        return super.getRaiseExceptionOnError();
    }

    public Map<String, String> getDmzOptions() {
        return dmzOptions;
    }

    public void setDmzOption(String dmzOptionKey, String dmzOptionValue) {
        dmzOptions.put(dmzOptionKey, dmzOptionValue);
    }

    public String getDmzOption(String dmzOptionKey) {
        return dmzOptions.getOrDefault(dmzOptionKey, "");
    }

    public void setOriginalSettingsFile(String val) {
        this.originalSettingsFile = val;
    }

    public String getOriginalSettingsFile() {
        return this.originalSettingsFile;
    }

    public void setDeleteSettingsFileOnExit(boolean val) {
        this.deleteSettingsFileOnExit = val;
    }

    public boolean getDeleteSettingsFileOnExit() {
        return this.deleteSettingsFileOnExit;
    }

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    public void setJobSchedulerId(String val) {
        jobSchedulerId = val;
    }

    public String getJobChain() {
        return jobChain;
    }

    public void setJobChain(String val) {
        jobChain = val;
    }

    public String getJobChainNodeName() {
        return jobChainNodeName;
    }

    public void setJobChainNodeName(String val) {
        jobChainNodeName = val;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String val) {
        job = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String val) {
        taskId = val;
    }

    public Long getParentTransferId() {
        return parentTransferId;
    }

    public void setParentTransferId(Long val) {
        parentTransferId = val;
    }

    @Override
    public void setUseKeyAgent(SOSOptionBoolean val) {

    }

    @Override
    public SOSOptionBoolean isUseKeyAgent() {
        return null;
    }

    public boolean getCumulativeTargetDeleted() {
        return cumulativeTargetDeleted;
    }

    public void setCumulativeTargetDeleted(boolean val) {
        cumulativeTargetDeleted = val;
    }
}