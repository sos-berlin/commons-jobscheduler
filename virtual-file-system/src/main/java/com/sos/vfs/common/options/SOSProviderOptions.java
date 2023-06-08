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
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;
import com.sos.credentialstore.options.SOSCredentialStoreOptions;
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

    private static final String SETTINGS_KEY_USE_CREDENTIAL_STORE = "%suse_credential_store";
    private static final String SETTINGS_KEY_ALTERNATIVE_CREDENTIAL_STORE_AUTH_METHOD = "alternative_%scredentialstore_authenticationmethod";

    private SOSProviderOptions alternative = null;
    private SOSCredentialStoreOptions credentialStore = null;

    private String range;
    private String prefix = null;
    private boolean isAlternative = false;
    private boolean isSource = false;

    public SOSProviderOptions() {
    }

    public SOSProviderOptions(boolean alternative, boolean source) {
        isAlternative = alternative;
        setIsSource(source);
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
        if (settings.containsKey(String.format(SETTINGS_KEY_USE_CREDENTIAL_STORE, prefix))) {
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
            if (settings.containsKey(String.format(SETTINGS_KEY_ALTERNATIVE_CREDENTIAL_STORE_AUTH_METHOD, prefix))) {
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
                if (!SOSString.isEmpty(credentialStore.credentialStorePassword.getValue())) {
                    keePassPassword = credentialStore.credentialStorePassword.getValue();
                }

                try {
                    if (!SOSString.isEmpty(credentialStore.credentialStoreKeyFileName.getValue())) {
                        keePassKeyFile = Paths.get(credentialStore.credentialStoreKeyFileName.getValue());
                        if (Files.notExists(keePassKeyFile)) {
                            throw new Exception(String.format("[%s]key file not found", SOSKeePassDatabase.getFilePath(keePassKeyFile)));
                        }
                    } else {
                        if ("privatekey".equals(credentialStore.credentialStoreAuthenticationMethod.getValue())) {
                            keePassKeyFile = SOSKeePassDatabase.getDefaultKeyFile(databaseFile);
                            if (keePassKeyFile == null) {
                                if (SOSString.isEmpty(keePassPassword)) {
                                    throw new Exception(String.format("default key file not found. password is empty"));
                                }
                            }
                        }
                    }

                    kpd = new SOSKeePassDatabase(databaseFile, SOSKeePassDatabase.getModule(credentialStore.credentialStoreModule.getValue()));
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
                LOGGER.debug(String.format("use already loaded KeePass from %s", kpd.getHandler().getKeePassFile().toString()));
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
                SOSKeePassPath path = new SOSKeePassPath(kpd.getHandler().isKdbx(), varName);
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
            SOSKeePassPath keePassPath = new SOSKeePassPath(kpd.getHandler().isKdbx(), sshAuthFile.getValue(), credentialStore.credentialStoreKeyPath
                    .getValue());
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
        SOSKeePassPath keePassPath = new SOSKeePassPath(kpd.getHandler().isKdbx(), option.getValue(), credentialStore.credentialStoreKeyPath
                .getValue());
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
        setRange();
        setPrefix();
    }

    public SOSProviderOptions getAlternative() {
        if (alternative == null) {
            alternative = new SOSProviderOptions(true, isSource);
        }
        return alternative;
    }

    public String getProtocol() throws Exception {
        String p = protocol.getValue();
        if (p.isEmpty()) {
            p = TransferTypes.local.name();
        }
        return p;
    }

    private void setRange() {
        range = isSource ? "source" : "target";
    }

    public String getRange() {
        return range;
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

}