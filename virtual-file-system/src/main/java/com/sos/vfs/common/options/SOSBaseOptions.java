package com.sos.vfs.common.options;

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
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.keepass.SOSKeePassPath;
import com.sos.vfs.common.options.SOSTransfer;
import com.sos.vfs.common.SOSVFSMessageCodes;

import sos.net.mail.options.SOSSmtpMailOptions;
import sos.settings.SOSProfileSettings;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSBaseOptions extends SOSBaseOptionsSuperClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSBaseOptions.class);
    private static final long serialVersionUID = -8219289268940238015L;

    public static final String SETTINGS_KEY_ALTERNATIVE_SOURCE_INCLUDE = "alternative_source_include";
    public static final String SETTINGS_KEY_ALTERNATIVE_TARGET_INCLUDE = "alternative_target_include";
    public static final String SETTINGS_KEY_ALTERNATIVE_CREDENTIAL_STORE_AUTH_METHOD = "alternative_%scredentialstore_authenticationmethod";
    public static final String SETTINGS_KEY_USE_CREDENTIAL_STORE = "%suse_credential_store";
    public static final String SETTINGS_KEY_MAIL_SMTP = "mail_smtp";

    private static final String PREFIX_FILE_URI = "file://";
    private static final String PREFIX_SCHEDULER_ENV_VAR = "scheduler_param_";
    private static final String PREFIX_SOSFTP_ENV_VAR = "sosftp_";
    private static final String FILE_SEPARATOR = "file.separator";
    private SOSSmtpMailOptions mailOptions;
    private Map<String, String> dmzOptions = new HashMap<String, String>();
    private SOSTransfer transfer;// source/target options
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

    public SOSBaseOptions(final TransferTypes source, final TransferTypes target) throws Exception {
        super();
        switch (source) {
        case webdav:
            protocol.changeDefaults(TransferTypes.webdav.name(), TransferTypes.webdav.name());
            break;
        default:
            break;
        }
        changeDefaults(source, getSource());
        changeDefaults(target, getTarget());
    }

    public SOSBaseOptions() {
        super();
    }

    public SOSBaseOptions(final HashMap<String, String> settings) throws Exception {
        super(settings);
    }

    private void changeDefaults(final TransferTypes type, final SOSProviderOptions options) {
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

    private void setChildClasses(final HashMap<String, String> settings) {
        try {
            LOGGER.trace("[setChildClasses]map");

            if (transfer == null) {
                LOGGER.trace("[transfer]main");
                transfer = new SOSTransfer(settings);
            }
            if (mailOptions == null) {
                if (settings.containsKey(SETTINGS_KEY_MAIL_SMTP)) {
                    LOGGER.trace("[smtpmail options]");
                    mailOptions = new SOSSmtpMailOptions(settings);
                } else {
                    mailOptions = new SOSSmtpMailOptions();
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public void checkMandatory() {
        if (checkMandatoryDone) {
            return;
        }
        try {
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
                throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_0010.params(localDir));
            }
            checkReplaceAndReplacing(getTarget());
            checkReplaceAndReplacing(getSource());
            if (replacing.isNotEmpty() && replacement.isNull()) {
                replacement.setValue("");
            }
            if (replacing.IsEmpty() && replacement.isNotEmpty()) {
                throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_0020.params(replacement.getKey(), replacing.getKey()));
            }
            if (appendFiles.value()) {
                String appendFilesKey = appendFiles.getKey();
                if (isAtomicTransfer()) {
                    String name = getOptionNamesAsString(new SOSOptionElement[] { atomicPrefix, atomicSuffix });
                    throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_0050.params(appendFilesKey, name));
                }
                if (compressFiles.value()) {
                    String name = getOptionNamesAsString(new SOSOptionElement[] { compressFiles });
                    throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_0050.params(appendFilesKey, name));
                }
                if (!"ftp".equalsIgnoreCase(protocol.getValue())) {
                    throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_0040.params(appendFilesKey, protocol.getValue()));
                }
            }
            if (this.getTarget().protocol.isDirty() && !protocol.isDirty()) {
                protocol.setValue(getTarget().protocol.getValue());
            }
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
                throw new JobSchedulerException(String.format("SOSVfs-E-0000: one of these parameters must be specified: '%1$s', '%2$s', '%3$s'",
                        filePath.getShortKey(), "source_dir", fileListName.getShortKey()));
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
        } catch (JobSchedulerException e) {
            throw e;
        } catch (Throwable e) {
            throw new JobSchedulerException(e);
        }
    }

    private void checkReplaceAndReplacing(final SOSProviderOptionsSuperClass options) {
        if (options.replacing.isNotEmpty() && options.replacement.isNull()) {
            options.replacement.setValue("");
        }
        if (options.replacing.IsEmpty() && options.replacement.isNotEmpty()) {
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_0020.params(options.replacement.getKey(), options.replacing.getKey()));
        }
    }

    private void checkCredentialStore(final SOSProviderOptions options) {
        if (options.getCredentialStore() != null) {
            options.checkCredentialStoreOptions();
        }
    }

    private void setDefaultAuth(final SOSOptionTransferType type, final SOSProviderOptions options) {
        TransferTypes transferType = type.getEnum();
        if ((type.isHTTP() || transferType.equals(TransferTypes.webdav)) && !options.authMethod.isDirty() && !options.sshAuthMethod.isDirty()) {
            options.authMethod.setValue(enuAuthenticationMethods.url);
            options.sshAuthMethod.setValue(enuAuthenticationMethods.url);
        }
        if (transferType.equals(TransferTypes.local)) {
            options.user.setValue(System.getProperty("user.name"));
            options.user.setNotDirty();
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
            String msg = SOSVFSMessageCodes.SOSVfs_E_161.params("reading environment", e.toString());
            LOGGER.error(msg, e);
            throw new JobSchedulerException(msg, e);
        }
    }

    @Override
    public void commandLineArgs(final String[] args) {
        super.commandLineArgs(args);
        this.setAllOptions(super.getSettings());
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
            commandLineArgs(args.split(" "));
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_153.params("command lines args"), e);
        }
    }

    public void setOptions(HashMap<String, String> map) {
        LOGGER.trace("[set options]start");
        super.setAllOptions(map);
        setChildClasses(map);
        LOGGER.trace("[set options]end");
    }

    public void setAllOptionsOnJob(HashMap<String, String> params) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("settingsFileProcessed=%s, readSettingsFileIsActive=%s", settingsFileProcessed, readSettingsFileIsActive));
        }
        if (!settingsFileProcessed && !readSettingsFileIsActive && params.containsKey("settings") && params.containsKey("profile")) {
            settings.setValue(params.get("settings"));
            profile.setValue(params.get("profile"));
            readSettingsFileIsActive = true;
            params.putAll(readSettingsFile(params));
            readSettingsFileIsActive = false;
            settingsFileProcessed = true;
        }
        handleFileOrderSource(params);

        setOptions(params);
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

    public HashMap<String, String> readSettingsFile(Map<String, String> beatParams) {
        settings.checkMandatory();
        profile.checkMandatory();

        HashMap<String, String> result = new HashMap<String, String>();
        Properties properties = new Properties();
        try {
            LOGGER.debug(String.format("readSettingsFile: settings=%s, profile=%s", settings.getValue(), profile.getValue()));
            getEnvVars();

            SOSProfileSettings conf = new SOSProfileSettings(settings.getValue());
            Properties profileProps = conf.getSection(profile.getValue());
            if (profileProps.isEmpty()) {
                String sf = originalSettingsFile == null ? settings.getValue() : originalSettingsFile;
                throw new JobSchedulerException(String.format("[%s]not found profile=%s", sf, profile.getValue()));
            }
            Properties globalsProps = conf.getSection("globals");
            globalsProps = resolveIncludes(conf, globalsProps);
            properties.putAll(globalsProps);

            profileProps = resolveIncludes(conf, profileProps);
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
                if (hasVariableToSubstitute(value) == true) {

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
                                LOGGER.warn(SOSVFSMessageCodes.SOSVfs_W_0070.params(value, key));
                            }
                        }
                    }
                    value = unescape(value);
                }
                result.put(key, value);
            }
        } catch (JobSchedulerException e) {
            LOGGER.error("ReadSettingsFile(): " + e.toString(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("ReadSettingsFile(): " + e.toString(), e);
            throw new JobSchedulerException(e);
        }
        return result;
    }

    private Properties resolveIncludes(SOSProfileSettings conf, Properties props) throws Exception {
        return resolveIncludes(conf, props, "");
    }

    private Properties resolveIncludes(SOSProfileSettings conf, Properties props, String prefix) throws Exception {
        Properties allIncludedProps = new Properties();
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
                    Properties includedProps = conf.getSection(include, true, includePrefix);
                    if (includedProps.isEmpty()) {
                        throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_0000.params(include, settings.getValue()));
                    }
                    includedProps = resolveIncludes(conf, includedProps, includePrefix);
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

    private boolean isIncludeDirective(final String includeDirective) {
        return includeDirectives.containsKey(includeDirective);
    }

    private String getIncludePrefix(final String includeDirective) {
        return includeDirectives.get(includeDirective);
    }

    private String substituteVariables(String txt, final Properties prop) {
        Map<String, String> startEndCharsForSubstitute = new HashMap<String, String>();
        startEndCharsForSubstitute.put("${", "}");
        startEndCharsForSubstitute.put("%{", "}");
        startEndCharsForSubstitute.put("%", "%");
        for (Map.Entry<String, String> e : startEndCharsForSubstitute.entrySet()) {
            txt = substituteVariables(txt, prop, e.getKey(), e.getValue());
        }
        return txt;
    }

    private String substituteVariables(String txt, final Properties prop, final String startPrefix, final String endPrefix) {
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
            throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_197.params(txt), e);
        }
    }

    private URI createURI(final String fileName) {
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
                throw new JobSchedulerException(SOSVFSMessageCodes.SOSVfs_E_198.params(e.getMessage()));
            }
        }
        return uri;
    }

    public boolean oneOrMoreSingleFilesSpecified() {
        return filePath.isNotEmpty() || fileListName.isNotEmpty();
    }

    public String getDataTargetType() throws Exception {
        String targetType = getTransfer().getTarget().protocol.getValue();
        if (targetType.isEmpty()) {
            targetType = TransferTypes.local.name();
        }
        changeDirValues();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[target]%s", targetType));
        }
        return targetType;
    }

    public String getDataSourceType() throws Exception {
        String sourceType = getTransfer().getSource().protocol.getValue();
        if (sourceType.isEmpty()) {
            sourceType = TransferTypes.local.name();
        }
        changeDirValues();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[source]%s", sourceType));
        }
        return sourceType;
    }

    private void changeDirValues() throws Exception {
        changeValue(sourceDir, localDir);
        changeValue(targetDir, remoteDir);
        changeValue(localDir, sourceDir);
        changeValue(remoteDir, targetDir);
        changeValue(getSource().folderName, sourceDir);
        changeValue(getTarget().folderName, targetDir);
    }

    private void changeValue(final SOSOptionElement target, final SOSOptionElement source) {
        if (target.IsEmpty() && !source.IsEmpty()) {
            if (source instanceof SOSOptionPassword) {
                LOGGER.trace(SOSVFSMessageCodes.SOSVfs_I_263.params(target.getKey(), "*****"));
            } else {
                LOGGER.trace(SOSVFSMessageCodes.SOSVfs_I_263.params(target.getKey(), source.getValue()));
            }
            target.set(source);
        }
    }

    public boolean isDoNotOverwrite() {
        return !overwriteFiles.value() && !appendFiles.value();
    }

    public SOSTransfer getTransfer() throws Exception {
        if (transfer == null) {
            transfer = new SOSTransfer(new HashMap<String, String>());
        }
        return transfer;
    }

    public boolean isReplaceReplacingInEffect() {
        return this.replacing.isNotEmpty();
    }

    public SOSProviderOptions getSource() throws Exception {
        return getTransfer().getSource();
    }

    public SOSProviderOptions getTarget() throws Exception {
        return getTransfer().getTarget();
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

    public boolean isFilePollingEnabled() {
        boolean result = false;
        if ((pollTimeout.isDirty() || pollingDuration.isDirty()) && skipTransfer.isFalse()) {
            result = true;
        }
        return result;
    }

    public Map<String, String> getDmzOptions() {
        return dmzOptions;
    }

    public void setDmzOption(String key, String value) {
        dmzOptions.put(key, value);
    }

    public String getDmzOption(String key) {
        return dmzOptions.getOrDefault(key, "");
    }

    public void setOriginalSettingsFile(String val) {
        originalSettingsFile = val;
    }

    public String getOriginalSettingsFile() {
        return originalSettingsFile;
    }

    public void setDeleteSettingsFileOnExit(boolean val) {
        deleteSettingsFileOnExit = val;
    }

    public boolean getDeleteSettingsFileOnExit() {
        return deleteSettingsFileOnExit;
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

    public boolean getCumulativeTargetDeleted() {
        return cumulativeTargetDeleted;
    }

    public void setCumulativeTargetDeleted(boolean val) {
        cumulativeTargetDeleted = val;
    }
}