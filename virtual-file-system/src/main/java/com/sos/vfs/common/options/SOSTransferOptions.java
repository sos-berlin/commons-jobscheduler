package com.sos.vfs.common.options;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@JSOptionClass(name = "SOSTransferOptions", description = "Options for a connection to an uri (server, site, e.g.)")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSTransferOptions extends SOSDestinationOptionsSuperClass {

    private static final long serialVersionUID = 6485361196241983182L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JSOptionsClass.class);

    private static final String PREFIX_TARGET = "target_";
    private static final String PREFIX_SOURCE = "source_";

    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSDestinationOptions sourceOptions = null;

    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSDestinationOptions targetOptions = null;

    public SOSTransferOptions() {
        super();
        initChildOptions();
    }

    public SOSTransferOptions(final HashMap<String, String> settings) throws Exception {
        super(settings);
        initChildOptions(settings);
    }

    private void initChildOptions() {
        if (sourceOptions == null) {
            sourceOptions = new SOSDestinationOptions(true);
        }
        if (targetOptions == null) {
            targetOptions = new SOSDestinationOptions(false);
        }
    }

    private void initChildOptions(final HashMap<String, String> settings) throws Exception {
        initChildOptions();

        LOGGER.trace("[destination options]source");
        sourceOptions.setAllOptions(settings, PREFIX_SOURCE);
        if (settings.containsKey(SOSBaseOptions.SETTINGS_KEY_ALTERNATIVE_SOURCE_INCLUDE)) {
            sourceOptions.alternateOptionsUsed.value(true);
        }
        // sourceOptions.getAlternatives().setChildClasses(settings, PREFIX_SOURCE);
        sourceOptions.setChildClasses(settings, PREFIX_SOURCE);
        // this.addProcessedOptions(sourceOptions.getProcessedOptions());

        LOGGER.trace("[destination options]target");
        targetOptions.setAllOptions(settings, PREFIX_TARGET);
        if (settings.containsKey(SOSBaseOptions.SETTINGS_KEY_ALTERNATIVE_TARGET_INCLUDE)) {
            targetOptions.alternateOptionsUsed.value(true);
        }
        // targetOptions.getAlternatives().setChildClasses(settings, PREFIX_TARGET);
        targetOptions.setChildClasses(settings, PREFIX_TARGET);
        // this.addProcessedOptions(targetOptions.getProcessedOptions());
    }

    @Override
    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    public SOSDestinationOptions getSource() {
        if (sourceOptions == null) {
            sourceOptions = new SOSDestinationOptions(true);
        }
        return sourceOptions;
    }

    public void setSource(final SOSDestinationOptions val) {
        sourceOptions = val;
        if (sourceOptions != null) {
            sourceOptions.setIsSource(true);
        }
    }

    public SOSDestinationOptions getTarget() {
        if (targetOptions == null) {
            targetOptions = new SOSDestinationOptions(false);
        }
        return targetOptions;
    }

    public void setTarget(final SOSDestinationOptions val) {
        targetOptions = val;
        if (targetOptions != null) {
            targetOptions.setIsSource(false);
        }
    }

}