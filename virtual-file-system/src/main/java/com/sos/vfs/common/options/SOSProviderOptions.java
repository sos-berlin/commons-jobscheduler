package com.sos.vfs.common.options;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.linguafranca.pwdb.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.sos.credentialstore.options.SOSCredentialStoreOptions;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionObject;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.keepass.SOSKeePassDatabase;
import com.sos.keepass.SOSKeePassPath;

import sos.util.ParameterSubstitutor;
import sos.util.SOSString;

@JSOptionClass(name = "SOSProviderOptions", description = "Options for a connection to an uri (server, site, e.g.)")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSProviderOptions extends SOSProviderOptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSProviderOptions.class);
    private static final String CLASSNAME = SOSProviderOptions.class.getSimpleName();

    private SOSProviderOptions alternative = null;
    private SOSCredentialStoreOptions credentialStore = null;
    private String prefix = null;
    private boolean isAlternative = false;
    private boolean isSource = false;

    public SOSProviderOptions() {
    }

    public SOSProviderOptions(boolean alternative, boolean source) {
        isAlternative = alternative;
        isSource = source;

        setPrefix();
    }

    @Override
    public void setAllOptions(final HashMap<String, String> params) {
        try {
            super.setAllOptions(params, prefix);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setChildClasses(final HashMap<String, String> settings) throws Exception {
        if (settings.containsKey(String.format(SOSBaseOptions.SETTINGS_KEY_USE_CREDENTIAL_STORE, prefix))) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[provider options][credential store]%s", prefix));
            }
            getCredentialStore().setAllOptions(settings, prefix);
        }
        if (alternateOptionsUsed.value()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("[provider options][alternative]%s", getAlternative().getPrefix()));
            }
            getAlternative().setAllOptions(settings);
            if (settings.containsKey(String.format(SOSBaseOptions.SETTINGS_KEY_ALTERNATIVE_CREDENTIAL_STORE_AUTH_METHOD, prefix))) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("[provider options][alternative][credential store]%s", getAlternative().getPrefix()));
                }
                getAlternative().getCredentialStore().setAllOptions(settings, getAlternative().getPrefix());
            }
        }
    }

    public void setCredentialStore(SOSCredentialStoreOptions opt) {
        credentialStore = opt;
    }

    public SOSCredentialStoreOptions getCredentialStore() {
        if (credentialStore == null) {
            credentialStore = new SOSCredentialStoreOptions();
        }
        return credentialStore;
    }

    public void checkCredentialStoreOptions() {
        if (credentialStore.useCredentialStore.isTrue()) {
            SOSKeePassDatabase kpd = null;
            if (keepass_database.value() == null) {
                Path databaseFile = Paths.get(credentialStore.credentialStoreFileName.getValue());

                LOGGER.debug(String.format("load KeePass from file %s", SOSKeePassDatabase.getFilePath(databaseFile)));
                credentialStore.credentialStoreFileName.checkMandatory(true);

                String keePassPassword = null;
                Path keePassKeyFile = null;
                if (credentialStore.credentialStorePassword.isDirty()) {
                    keePassPassword = credentialStore.credentialStorePassword.getValue();
                }

                try {
                    if (credentialStore.credentialStoreKeyFileName.isDirty()) {
                        keePassKeyFile = Paths.get(credentialStore.credentialStoreKeyFileName.getValue());
                        if (Files.notExists(keePassKeyFile)) {
                            throw new Exception(String.format("[%s]key file not found", SOSKeePassDatabase.getFilePath(keePassKeyFile)));
                        }
                    } else {
                        if ("privatekey".equals(credentialStore.credentialStoreAuthenticationMethod.getValue())) {
                            Path defaultKeyFile = SOSKeePassDatabase.getDefaultKeyFile(databaseFile);
                            if (Files.notExists(defaultKeyFile)) {
                                if (SOSString.isEmpty(keePassPassword)) {
                                    throw new Exception(String.format("[%s]key file not found. password is empty", SOSKeePassDatabase.getFilePath(
                                            defaultKeyFile)));
                                }
                            }
                            keePassKeyFile = defaultKeyFile;
                        }
                    }

                    kpd = new SOSKeePassDatabase(databaseFile);
                    if (keePassKeyFile == null) {
                        kpd.load(keePassPassword);
                    } else {
                        kpd.load(keePassPassword, keePassKeyFile);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                    throw new JobSchedulerException(e);
                }
            } else {
                kpd = (SOSKeePassDatabase) keepass_database.value();
                LOGGER.debug(String.format("use already loaded KeePass from %s", kpd.getFile().toString()));
            }
            try {
                setKeePassOptions4Provider(kpd, null, null);
                keePass2Options(kpd);
                resolveCommands(kpd);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
                throw new JobSchedulerException(e);
            }

        }
    }

    private void resolveCommands(final SOSKeePassDatabase kpd) throws Exception {
        Map<String, Entry<?, ?, ?, ?>> entries = resolveCommand(preTransferCommands, kpd, new HashMap<String, Entry<?, ?, ?, ?>>());
        entries = resolveCommand(preCommand, kpd, entries);
        entries = resolveCommand(postCommand, kpd, entries);
        entries = resolveCommand(postTransferCommands, kpd, entries);
        entries = resolveCommand(postTransferCommandsOnError, kpd, entries);
        entries = resolveCommand(postTransferCommandsFinal, kpd, entries);
        entries = resolveCommand(tfnPostCommand, kpd, entries);
    }

    private Map<String, Entry<?, ?, ?, ?>> resolveCommand(final SOSOptionElement el, final SOSKeePassDatabase kpd,
            final Map<String, Entry<?, ?, ?, ?>> lastEntries) throws Exception {
        String command = el.getValue();
        if (SOSKeePassPath.hasKeePassVariables(command)) {
            ParameterSubstitutor ps = new ParameterSubstitutor();
            List<String> varNames = ps.getParameterNameFromString(command);
            List<String> resolvedNames = new ArrayList<String>();
            List<String> skippedNames = new ArrayList<String>();
            for (String varName : varNames) {
                SOSKeePassPath path = new SOSKeePassPath(kpd.isKDBX(), varName);
                if (path.isValid()) {
                    String entryKey = path.getEntryPath().toLowerCase();
                    Entry<?, ?, ?, ?> entry = null;
                    if (lastEntries.containsKey(entryKey)) {
                        entry = lastEntries.get(entryKey);
                    } else {
                        entry = kpd.getEntryByPath(path.getEntryPath());
                        if (entry == null) {
                            throw new Exception(String.format("[%s][%s][%s]entry not found", el.getShortKey(), varName, path.toString()));
                        }
                        lastEntries.put(entryKey, entry);
                    }
                    String value = entry.getProperty(path.getPropertyName());
                    if (value == null) {
                        throw new Exception(String.format("[%s][%s][%s]value is null", el.getShortKey(), varName, path.toString()));
                    }
                    ps.addKey(varName, value);
                    resolvedNames.add("${" + varName + "}");
                } else {
                    skippedNames.add("${" + varName + "}");
                }
            }
            if (skippedNames.size() > 0) {
                LOGGER.debug(String.format("[%s][skip]%s", el.getShortKey(), Joiner.on(",").join(skippedNames)));
            }
            if (resolvedNames.size() > 0) {
                el.setValue(ps.replace(command));
                LOGGER.debug(String.format("[%s][resolved]%s", el.getShortKey(), Joiner.on(",").join(resolvedNames)));
            } else {
                LOGGER.debug(String.format("[%s]nothing to resolve", el.getShortKey()));
            }
        }
        return lastEntries;
    }

    private Entry<?, ?, ?, ?> keePass2OptionsByKeePassSyntax(final SOSKeePassDatabase kpd) throws Exception {
        Entry<?, ?, ?, ?> entry = keePass2OptionByKeePassSyntax(kpd, host, null);
        entry = keePass2OptionByKeePassSyntax(kpd, user, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, password, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, passphrase, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, domain, entry);

        entry = keePass2OptionByKeePassSyntax(kpd, proxyHost, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, proxyUser, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, proxyPassword, entry);

        return entry;
    }

    private void keePass2Options(final SOSKeePassDatabase kpd) throws Exception {
        Entry<?, ?, ?, ?> entry = keePass2OptionsByKeePassSyntax(kpd);
        if (sshAuthFile.isNotEmpty()) {
            String optionName = sshAuthFile.getShortKey();
            SOSKeePassPath keePassPath = new SOSKeePassPath(kpd.isKDBX(), sshAuthFile.getValue(), credentialStore.credentialStoreKeyPath.getValue());
            if (keePassPath.isValid()) {
                LOGGER.debug(String.format("[%s]set from %s", optionName, keePassPath.toString()));
                if (entry == null || !keePassPath.getEntryPath().equals(entry.getPath())) {
                    entry = getKeePassEntry(kpd, keePassPath.getEntry());
                }
                setKeePassOptions4Provider(kpd, entry, keePassPath.getPropertyName());

            } else {
                LOGGER.debug(String.format("[%s]skip", optionName));
            }
        }
    }

    private void setKeePassOptions4Provider(SOSKeePassDatabase kpd, Entry<?, ?, ?, ?> entry, String attachmentPropertyName) {
        LOGGER.debug(String.format("attachmentPropertyName=%s", attachmentPropertyName));
        keepass_database.value(kpd);
        keepass_database_entry.value(entry);
        keepass_attachment_property_name.setValue(attachmentPropertyName);
    }

    private Entry<?, ?, ?, ?> getKeePassEntry(final SOSKeePassDatabase kpd, final String entryPath) throws Exception {
        Entry<?, ?, ?, ?> entry = kpd.getEntryByPath(entryPath);
        if (entry == null) {
            throw new Exception(String.format("[%s][%s]entry not found", credentialStore.credentialStoreFileName.getValue(), entryPath));
        }
        if (entry.getExpires()) {
            throw new Exception(String.format("[%s][%s]entry is expired (%s)", credentialStore.credentialStoreFileName.getValue(), entryPath, entry
                    .getExpiryTime()));
        }
        return entry;
    }

    private Entry<?, ?, ?, ?> keePass2OptionByKeePassSyntax(final SOSKeePassDatabase kpd, final SOSOptionElement option, Entry<?, ?, ?, ?> lastEntry)
            throws Exception {
        SOSKeePassPath keePassPath = new SOSKeePassPath(kpd.isKDBX(), option.getValue(), credentialStore.credentialStoreKeyPath.getValue());
        Entry<?, ?, ?, ?> entry = null;
        String fileName = credentialStore.credentialStoreFileName.getValue();
        String optionName = option.getShortKey();
        if (keePassPath.isValid()) {
            if (lastEntry == null || !keePassPath.getEntryPath().equals(lastEntry.getPath())) {
                entry = getKeePassEntry(kpd, keePassPath.getEntry());
            } else {
                entry = lastEntry;
            }
            String value = entry.getProperty(keePassPath.getPropertyName());
            if (value == null) {
                throw new Exception(String.format("[%s][%s][%s]value is null", fileName, optionName, keePassPath.toString()));
            }
            boolean setMultipleValue = false;
            if (optionName.equals(host.getShortKey()) || optionName.equals(proxyHost.getShortKey())) {
                String[] arr = value.split(":");
                switch (arr.length) {
                case 1:
                    break;
                default:
                    setMultipleValue = true;
                    String portOptionName = null;
                    if (optionName.equals(host.getShortKey())) {
                        host.setValue(arr[0]);
                        port.setValue(arr[1]);
                        portOptionName = port.getShortKey();
                    } else {
                        proxyHost.setValue(arr[0]);
                        proxyPort.setValue(arr[1]);
                        portOptionName = proxyPort.getShortKey();
                    }
                    LOGGER.debug(String.format("[%s,%s]set from %s", optionName, portOptionName, keePassPath.toString()));
                }
            }
            if (!setMultipleValue) {
                LOGGER.debug(String.format("[%s]set from %s", optionName, keePassPath.toString()));
                option.setValue(value);
            }
        } else {
            LOGGER.debug(String.format("[%s]skip", optionName));
        }
        return entry == null ? lastEntry : entry;
    }

    protected void setIfNotDirty(final SOSOptionElement option, final String value) {
        if (option.isNotDirty() && isNotEmpty(value)) {
            if (option instanceof SOSOptionPassword) {
                option.setValue(value);
                LOGGER.debug(String.format("[%s]?", option.getShortKey()));
            } else {
                LOGGER.debug(String.format("[%s]%s", option.getShortKey(), value));
            }
            option.setValue(value);
        }
    }

    @Override
    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    public boolean optionsHaveMinRequirements() {
        if (alternateOptionsUsed.isTrue()) {
            return false;
        }
        if ("local".equalsIgnoreCase(protocol.getValue())) {
            return true;
        }
        if (host.isNotDirty() || host.IsEmpty()) {
            return false;
        }
        if (protocol.getValue().matches("https?")) {
            return true;
        }
        if (user.isNotDirty() || user.IsEmpty()) {
            return false;
        }
        return true;
    }

    public boolean isSource() {
        return isSource;
    }

    public void setIsSource(boolean val) {
        isSource = val;
        setPrefix();
    }

    public SOSProviderOptions getAlternative() {
        if (alternative == null) {
            alternative = new SOSProviderOptions(true, isSource);
        }
        return alternative;
    }

    private void setPrefix() {
        prefix = isSource ? "source_" : "target_";
        if (isAlternative) {
            prefix = "alternative_" + prefix;
        }
    }

    public String getPrefix() {
        return prefix;
    }

    @JSOptionDefinition(name = "PreTransferCommands", description = "FTP commands, which has to be executed before the transfer started.", key = "PreTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString preTransferCommands = new SOSOptionCommandString(this, CLASSNAME + ".pre_transfer_commands",
            "FTP commands, which has to be executed before the transfer started.", "", "", false);

    public SOSOptionCommandString preFtpCommands = (SOSOptionCommandString) preTransferCommands.setAlias("pre_transfer_commands");

    @JSOptionDefinition(name = "PostTransferCommands", description = "FTP commands, which has to be executed after the transfer ended.", key = "PostTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString postTransferCommands = new SOSOptionCommandString(this, CLASSNAME + ".post_transfer_Commands",
            "FTP commands, which has to be executed after the transfer ended.", "", "", false);

    public SOSOptionCommandString postFtpCommands = (SOSOptionCommandString) postTransferCommands.setAlias("post_Transfer_commands");

    @JSOptionDefinition(name = "post_transfer_commands_on_error", description = "Commands, which has to be executed after the transfer ended with errors.", key = "post_transfer_commands_on_error", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString postTransferCommandsOnError = new SOSOptionCommandString(this, CLASSNAME + ".post_transfer_commands_on_error",
            "Commands, which has to be executed after the transfer ended with errors.", "", "", false);

    @JSOptionDefinition(name = "post_transfer_commands_final", description = "Commands, which has to be executed always after the transfer ended independent of "
            + "the transfer status.", key = "post_transfer_commands_final", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString postTransferCommandsFinal = new SOSOptionCommandString(this, CLASSNAME + ".post_transfer_commands_final",
            "Commands, which has to be executed always after the transfer ended independent of the transfer status.", "", "", false);

    @JSOptionDefinition(name = "IgnoreCertificateError", description = "Ignore a SSL Certificate Error", key = "IgnoreCertificateError", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean ignoreCertificateError = new SOSOptionBoolean(this, CLASSNAME + ".IgnoreCertificateError",
            "Ignore a SSL Certificate Error", "true", "true", true);

    @JSOptionDefinition(name = "command_delimiter", description = "Command delimiter for pre and post commands", key = "command_delimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString commandDelimiter = new SOSOptionString(this, CLASSNAME + ".command_delimiter",
            "Command delimiter for pre and post commands", ";", ";", true);

    @JSOptionDefinition(name = "AlternateOptionsUsed", description = "Alternate Options used for connection and/or authentication", key = "AlternateOptionsUsed", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean alternateOptionsUsed = new SOSOptionBoolean(this, CLASSNAME + ".AlternateOptionsUsed",
            "Alternate Options used for connection and/or authentication", "false", "false", false);

    @JSOptionDefinition(name = "user_info", description = "User Info implementation", key = "user_info", type = "SOSOptionObject", mandatory = false)
    public SOSOptionObject user_info = new SOSOptionObject(this, CLASSNAME + ".user_info", "user_info", "", "", false);

    @JSOptionDefinition(name = "keepass_database", description = "Keepass database", key = "keepass_database", type = "SOSOptionObject", mandatory = false)
    public SOSOptionObject keepass_database = new SOSOptionObject(this, CLASSNAME + ".keepass_database", "Keepass database", "", "", false);

    @JSOptionDefinition(name = "keepass_database_entry", description = "Keepass entry", key = "keepass_database_entry", type = "SOSOptionObject", mandatory = false)
    public SOSOptionObject keepass_database_entry = new SOSOptionObject(this, CLASSNAME + ".keepass_database_entry", "Keepass entry", "", "", false);

    @JSOptionDefinition(name = "keepass_attachment_property_name", description = "Keepass attachment property name", key = "keepass_database_entry", type = "SOSOptionString", mandatory = false)
    public SOSOptionString keepass_attachment_property_name = new SOSOptionString(this, CLASSNAME + ".keepass_attachment_property_name",
            "Keepass attachment property name", "", "", false);

}