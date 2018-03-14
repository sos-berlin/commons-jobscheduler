package com.sos.VirtualFileSystem.Options;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.linguafranca.pwdb.Entry;

import com.google.common.base.Joiner;
import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Listener.JSListener;
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

@JSOptionClass(name = "SOSConnection2OptionsAlternate", description = "Options for a connection to an uri (server, site, e.g.)")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSConnection2OptionsAlternate extends SOSConnection2OptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SOSConnection2OptionsAlternate.class);
    private static final String CLASSNAME = SOSConnection2OptionsAlternate.class.getSimpleName();
    private String strAlternativePrefix = "";
    public boolean isSource = false;

    public SOSConnection2OptionsAlternate() {
        //
    }

    public SOSConnection2OptionsAlternate(final String prefix) {
        strAlternativePrefix = prefix;
    }

    public SOSConnection2OptionsAlternate(final JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    public SOSConnection2OptionsAlternate(final HashMap<String, String> settings) throws Exception {
        super(settings);
        getAlternativeOptions().setAllOptions(settings, "alternative_" + strAlternativePrefix);
        this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
    }

    public SOSConnection2OptionsAlternate(final HashMap<String, String> settings, final String prefix) throws Exception {
        strAlternativePrefix = prefix;
        setAllOptions(settings, strAlternativePrefix);
        setChildClasses(settings, prefix);
    }

    @JSOptionDefinition(name = "PreTransferCommands", description = "FTP commands, which has to be executed before the transfer started.", key = "PreTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString preTransferCommands = new SOSOptionCommandString(this, CLASSNAME + ".pre_transfer_commands",
            "FTP commands, which has to be executed before the transfer started.", "", "", false);

    public SOSOptionCommandString preFtpCommands = (SOSOptionCommandString) preTransferCommands.setAlias("pre_transfer_commands");

    public String getPreTransferCommands() {
        return preTransferCommands.getValue();
    }

    public SOSConnection2OptionsAlternate setPreTransferCommands(final String val) {
        preTransferCommands.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "PostTransferCommands", description = "FTP commands, which has to be executed after the transfer ended.", key = "PostTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString postTransferCommands = new SOSOptionCommandString(this, CLASSNAME + ".post_transfer_Commands",
            "FTP commands, which has to be executed after the transfer ended.", "", "", false);

    public SOSOptionCommandString postFtpCommands = (SOSOptionCommandString) postTransferCommands.setAlias("post_Transfer_commands");

    public String getPostTransferCommands() {
        return postTransferCommands.getValue();
    }

    public SOSConnection2OptionsAlternate setPostTransferCommands(final String val) {
        postTransferCommands.setValue(val);
        return this;
    }

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

    public boolean getIgnoreCertificateError() {
        return ignoreCertificateError.value();
    }

    public SOSConnection2OptionsAlternate setIgnoreCertificateError(final boolean val) {
        ignoreCertificateError.value(val);
        return this;
    }

    @JSOptionDefinition(name = "command_delimiter", description = "Command delimiter for pre and post commands", key = "command_delimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString commandDelimiter = new SOSOptionString(this, CLASSNAME + ".command_delimiter",
            "Command delimiter for pre and post commands", ";", ";", true);

    @JSOptionDefinition(name = "AlternateOptionsUsed", description = "Alternate Options used for connection and/or authentication", key = "AlternateOptionsUsed", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean alternateOptionsUsed = new SOSOptionBoolean(this, CLASSNAME + ".AlternateOptionsUsed",
            "Alternate Options used for connection and/or authentication", "false", "false", false);

    public String getAlternateOptionsUsed() {
        return alternateOptionsUsed.getValue();
    }

    public SOSConnection2OptionsAlternate setAlternateOptionsUsed(final String val) {
        alternateOptionsUsed.setValue(val);
        return this;
    }

    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate", prefix = "alternate_")
    private SOSConnection2OptionsAlternate objAlternativeOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate", prefix = "proxy_")
    private SOSConnection2OptionsAlternate objProxyOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate", prefix = "jump_")
    private SOSConnection2OptionsAlternate objJumpServerOptions = null;
    @JSOptionClass(description = "", name = "SOSCredentialStoreOptions")
    private SOSCredentialStoreOptions objCredentialStoreOptions = null;

    public void setChildClasses(final HashMap<String, String> settings, final String prefix) throws Exception {
        strAlternativePrefix = prefix;
        getCredentialStore().setAllOptions(settings, strAlternativePrefix);
        getAlternativeOptions().setAllOptions(settings, "alternative_" + strAlternativePrefix);
        getAlternativeOptions().setAllOptions(settings, "alternate_" + strAlternativePrefix);
        getProxyOptions().setAllOptions(settings, "proxy_" + strAlternativePrefix);
        getJumpServerOptions().setAllOptions(settings, "jump_" + strAlternativePrefix);
        this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
    }

    public SOSConnection2OptionsAlternate getAlternativeOptions() {
        if (objAlternativeOptions == null) {
            objAlternativeOptions = new SOSConnection2OptionsAlternate();
        }
        return objAlternativeOptions;
    }

    public SOSConnection2OptionsAlternate getProxyOptions() {
        if (objProxyOptions == null) {
            objProxyOptions = new SOSConnection2OptionsAlternate();
        }
        return objProxyOptions;
    }

    public SOSConnection2OptionsAlternate getJumpServerOptions() {
        if (objJumpServerOptions == null) {
            objJumpServerOptions = new SOSConnection2OptionsAlternate();
        }
        return objJumpServerOptions;
    }

    public void setCredentialStore(SOSCredentialStoreOptions opt) {
        objCredentialStoreOptions = opt;
    }

    public SOSCredentialStoreOptions getCredentialStore() {
        if (objCredentialStoreOptions == null) {
            objCredentialStoreOptions = new SOSCredentialStoreOptions();
        }
        return objCredentialStoreOptions;
    }

    public void checkCredentialStoreOptions() {
        if (objCredentialStoreOptions.useCredentialStore.isTrue()) {
            SOSKeePassDatabase kpd = null;
            if (keepass_database.value() == null) {
                LOGGER.debug(String.format("load KeePass from file %s", objCredentialStoreOptions.credentialStoreFileName.getValue()));
                objCredentialStoreOptions.credentialStoreFileName.checkMandatory(true);

                String keePassPassword = null;
                String keePassKeyFile = null;
                if (objCredentialStoreOptions.credentialStorePassword.isDirty()) {
                    keePassPassword = objCredentialStoreOptions.credentialStorePassword.getValue();
                }
                if (objCredentialStoreOptions.credentialStoreKeyFileName.isDirty()) {
                    keePassKeyFile = objCredentialStoreOptions.credentialStoreKeyFileName.getValue();
                }
                // SOSKeePassDatabase kpd = null;
                try {
                    kpd = new SOSKeePassDatabase(Paths.get(objCredentialStoreOptions.credentialStoreFileName.getValue()));
                    if (SOSString.isEmpty(keePassKeyFile)) {
                        kpd.load(keePassPassword);
                    } else {
                        kpd.load(keePassPassword, Paths.get(keePassKeyFile));
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
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
                LOGGER.error(e.getMessage());
                throw new JobSchedulerException(e);
            }

        }
    }

    private void resolveCommands(final SOSKeePassDatabase kpd) throws Exception {
        Map<String, Entry<?, ?, ?, ?>> entries = resolveCommand(preTransferCommands, kpd, new HashMap<String, Entry<?, ?, ?, ?>>());
        entries = resolveCommand(preCommand, kpd, entries);
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

    private Entry<?, ?, ?, ?> keePass2OptionsByKeePassDefault(final SOSKeePassDatabase kpd) throws Exception {
        String keyPath = objCredentialStoreOptions.credentialStoreKeyPath.getValue();
        if (keyPath.trim().isEmpty()) {
            LOGGER.debug(String.format("skip keePass2OptionsByKeePassDefault, credentialStoreKeyPath is empty"));
            return null;
        }
        Entry<?, ?, ?, ?> entry = getKeePassEntry(kpd, keyPath);
        if (!entry.getUrl().isEmpty()) {
            try {
                LOGGER.debug(String.format("[%s,%s,%s,%s,%s]try to set from %s@Url", host.getShortKey(), port.getShortKey(), protocol.getShortKey(), user
                        .getShortKey(), password.getShortKey(), keyPath));
                // Possible Elements of an URL are:
                //
                // http://hans:geheim@www.example.org:80/demo/example.cgi?land=de&stadt=aa#geschichte
                // | | | | | | | |
                // | | | host | url-path searchpart fragment
                // | | password port
                // | user
                // protocol
                //
                // ftp://<user>:<password>@<host>:<port>/<url-path>;type=<typecode>
                // see
                // http://docs.oracle.com/javase/7/docs/api/java/net/URI.html
                URI uri = new URI(entry.getUrl());
                setIfNotDirty(host, uri.getHost());
                String uriPort = String.valueOf(uri.getPort());
                if (isEmpty(uriPort) || uriPort.equals("-1")) {
                    LOGGER.debug(String.format("[%s]can't evaluate port from %s@Url", port.getShortKey(), keyPath));
                } else {
                    setIfNotDirty(port, uriPort);
                }
                setIfNotDirty(protocol, uri.getScheme());
                if (SOSString.isEmpty(uri.getUserInfo())) {
                    LOGGER.debug(String.format("[%s,%s]can't evaluate UserInfo from %s@Url", user.getShortKey(), password.getShortKey(), keyPath));
                } else {
                    String[] ui = uri.getUserInfo().split(":");
                    setIfNotDirty(user, ui[0]);
                    if (ui.length > 1) {
                        setIfNotDirty(password, ui[1]);
                    }
                }
            } catch (Throwable e) {
                LOGGER.debug(String.format("skip set from %s@Url due an exception: %s", keyPath, e.toString()));
            }
        }
        boolean hideValue = false;
        if (isNotEmpty(entry.getUsername())) {
            LOGGER.debug(String.format("[%s]set from %s@Username", user.getShortKey(), keyPath));
            user.setValue(entry.getUsername());
            user.setHideValue(hideValue);
        }
        if (isNotEmpty(entry.getPassword())) {
            LOGGER.debug(String.format("[%s]set from %s@Password", password.getShortKey(), keyPath));
            password.setValue(entry.getPassword());
            password.setHideValue(hideValue);
        }
        if (isNotEmpty(entry.getUrl())) {
            setIfNotDirty(host, entry.getUrl());
            host.setHideValue(hideValue);
        }
        if (hostName.isNotDirty() && isNotEmpty(entry.getUrl())) {
            LOGGER.debug(String.format("[%s]set from %s@Url", hostName.getShortKey(), keyPath));
            hostName.setValue(entry.getUrl().toString());
        }
        return entry;
    }

    @JSOptionDefinition(name = "user_info", description = "User Info implementation", key = "user_info", type = "SOSOptionObject", mandatory = false)
    public SOSOptionObject user_info = new SOSOptionObject(this, CLASSNAME + ".user_info", "user_info", "", "", false);

    @JSOptionDefinition(name = "keepass_database", description = "Keepass database", key = "keepass_database", type = "SOSOptionObject", mandatory = false)
    public SOSOptionObject keepass_database = new SOSOptionObject(this, CLASSNAME + ".keepass_database", "Keepass database", "", "", false);

    @JSOptionDefinition(name = "keepass_database_entry", description = "Keepass entry", key = "keepass_database_entry", type = "SOSOptionObject", mandatory = false)
    public SOSOptionObject keepass_database_entry = new SOSOptionObject(this, CLASSNAME + ".keepass_database_entry", "Keepass entry", "", "", false);

    @JSOptionDefinition(name = "keepass_attachment_property_name", description = "Keepass attachment property name", key = "keepass_database_entry", type = "SOSOptionString", mandatory = false)
    public SOSOptionString keepass_attachment_property_name = new SOSOptionString(this, CLASSNAME + ".keepass_attachment_property_name",
            "Keepass attachment property name", "", "", false);

    private void keePass2Options(final SOSKeePassDatabase kpd) throws Exception {
        Entry<?, ?, ?, ?> entry = keePass2OptionsByKeePassSyntax(kpd);
        if (entry == null) {
            entry = keePass2OptionsByKeePassDefault(kpd);
        }
        if (sshAuthFile.isNotEmpty()) {
            String optionName = sshAuthFile.getShortKey();
            SOSKeePassPath keePassPath = new SOSKeePassPath(kpd.isKDBX(), sshAuthFile.getValue(), objCredentialStoreOptions.credentialStoreKeyPath
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
            throw new Exception(String.format("[%s][%s]entry not found", objCredentialStoreOptions.credentialStoreFileName.getValue(), entryPath));
        }
        if (entry.getExpires()) {
            throw new Exception(String.format("[%s][%s]entry is expired (%s)", objCredentialStoreOptions.credentialStoreFileName.getValue(),
                    entryPath, entry.getExpiryTime()));
        }
        return entry;
    }

    private Entry<?, ?, ?, ?> keePass2OptionByKeePassSyntax(final SOSKeePassDatabase kpd, final SOSOptionElement option, Entry<?, ?, ?, ?> lastEntry)
            throws Exception {
        SOSKeePassPath keePassPath = new SOSKeePassPath(kpd.isKDBX(), option.getValue(), objCredentialStoreOptions.credentialStoreKeyPath.getValue());
        Entry<?, ?, ?, ?> entry = null;
        String fileName = objCredentialStoreOptions.credentialStoreFileName.getValue();
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

    public SOSConnection2OptionsAlternate getAlternatives() {
        if (objAlternativeOptions == null) {
            objAlternativeOptions = new SOSConnection2OptionsAlternate("");
        }
        return objAlternativeOptions;
    }

    public void setAlternativeOptions(final SOSConnection2OptionsAlternate val) {
        objAlternativeOptions = val;
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

}