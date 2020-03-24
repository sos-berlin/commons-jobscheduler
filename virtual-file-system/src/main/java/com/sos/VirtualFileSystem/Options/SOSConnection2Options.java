package com.sos.VirtualFileSystem.Options;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.i18n.annotation.I18NResourceBundle;

@JSOptionClass(name = "SOSConnection2Options", description = "Options for a connection to an uri (server, site, e.g.)")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSConnection2Options extends SOSConnection2OptionsSuperClass {

    private static final long serialVersionUID = 6485361196241983182L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JSOptionsClass.class);

    private static final String PREFIX_TARGET = "target_";
    private static final String PREFIX_SOURCE = "source_";

    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate sourceOptions = null;

    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate targetOptions = null;

    public SOSConnection2Options() {
        super();
        initChildOptions();
    }

    public SOSConnection2Options(final HashMap<String, String> settings) throws Exception {
        super(settings);
        initChildOptions();
        setPrefixedValues(settings);
    }

    private void initChildOptions() {
        if (sourceOptions == null) {
            sourceOptions = new SOSConnection2OptionsAlternate("");
        }
        sourceOptions.isSource = true;

        if (targetOptions == null) {
            targetOptions = new SOSConnection2OptionsAlternate("");
            targetOptions.isSource = false;
        }
    }

    private void setPrefixedValues(final HashMap<String, String> settings) throws Exception {
        LOGGER.debug("setSource options");
        sourceOptions.setAllOptions(settings, PREFIX_SOURCE);
        if (settings.containsKey("alternative_source_include")) {
            sourceOptions.alternateOptionsUsed.value(true);
        }
        // sourceOptions.getAlternatives().setChildClasses(settings, PREFIX_SOURCE);
        sourceOptions.setChildClasses(settings, PREFIX_SOURCE);
        // this.addProcessedOptions(sourceOptions.getProcessedOptions());

        
        LOGGER.debug("setTarget options");
        targetOptions.setAllOptions(settings, PREFIX_TARGET);
        if (settings.containsKey("alternative_target_include")) {
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

    public SOSConnection2OptionsAlternate getSource() {
        if (sourceOptions == null) {
            sourceOptions = new SOSConnection2OptionsAlternate(PREFIX_SOURCE);
        }
        return sourceOptions;
    }

    public void setSource(final SOSConnection2OptionsAlternate val) {
        sourceOptions = val;
    }

    public SOSConnection2OptionsAlternate getTarget() {
        if (targetOptions == null) {
            targetOptions = new SOSConnection2OptionsAlternate("");
        }
        return targetOptions;
    }

    public void setTarget(final SOSConnection2OptionsAlternate val) {
        targetOptions = val;
    }

}