package com.sos.VirtualFileSystem.Options;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.i18n.annotation.I18NResourceBundle;

@JSOptionClass(name = "SOSConnection2Options", description = "Options for a connection to an uri (server, site, e.g.)")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSConnection2Options extends SOSConnection2OptionsSuperClass {

    private static final long serialVersionUID = 6485361196241983182L;
    private final String strAlternativePrefix = "";
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate alternativeOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate sourceOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate targetOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate jumpOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate proxyOptions = null;

    public SOSConnection2Options() {
        super();
        initChildOptions();
    }

    public SOSConnection2Options(final String prefix) {
    }

    @Deprecated
    public SOSConnection2Options(final JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    private void initChildOptions() {
        if (alternativeOptions == null) {
            alternativeOptions = new SOSConnection2OptionsAlternate("");
        }
        if (sourceOptions == null) {
            sourceOptions = new SOSConnection2OptionsAlternate("");
        }
        sourceOptions.isSource = true;
        if (targetOptions == null) {
            targetOptions = new SOSConnection2OptionsAlternate("");
            targetOptions.isSource = false;
        }
        if (jumpOptions == null) {
            jumpOptions = new SOSConnection2OptionsAlternate("");
        }
        if (proxyOptions == null) {
            proxyOptions = new SOSConnection2OptionsAlternate("");
        }
    }

    public SOSConnection2Options(final HashMap<String, String> settings) throws Exception {
        super(settings);
        initChildOptions();
        setPrefixedValues(settings);
    }

    public void setPrefixedValues(final HashMap<String, String> settings) throws Exception {
        alternativeOptions.setAllOptions(settings, strAlternativePrefix + conParamNamePrefixALTERNATIVE);
        this.addProcessedOptions(alternativeOptions.getProcessedOptions());
        alternativeOptions.setAllOptions(settings);
        this.addProcessedOptions(alternativeOptions.getProcessedOptions());

        sourceOptions.setAllOptions(settings, conParamNamePrefixSOURCE);
        sourceOptions.Alternatives().setChildClasses(settings, conParamNamePrefixSOURCE);
        sourceOptions.setChildClasses(settings, conParamNamePrefixSOURCE);
        this.addProcessedOptions(sourceOptions.getProcessedOptions());

        targetOptions.setAllOptions(settings, conParamNamePrefixTARGET);
        targetOptions.Alternatives().setChildClasses(settings, conParamNamePrefixTARGET);
        targetOptions.setChildClasses(settings, conParamNamePrefixTARGET);
        this.addProcessedOptions(targetOptions.getProcessedOptions());

        jumpOptions.setAllOptions(settings, conParamNamePrefixJUMP);
        this.addProcessedOptions(jumpOptions.getProcessedOptions());
    }

    @Override
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    public SOSConnection2OptionsAlternate Alternatives() {
        return alternativeOptions;
    }

    public void Alternatives(final SOSConnection2OptionsAlternate val) {
        if (alternativeOptions == null) {
            alternativeOptions = new SOSConnection2OptionsAlternate(conParamNamePrefixALTERNATIVE);
        }
        alternativeOptions = val;
    }

    public SOSConnection2OptionsAlternate Source() {
        if (sourceOptions == null) {
            sourceOptions = new SOSConnection2OptionsAlternate(conParamNamePrefixSOURCE);
        }
        return sourceOptions;
    }

    public void Source(final SOSConnection2OptionsAlternate val) {
        sourceOptions = val;
    }

    public SOSConnection2OptionsAlternate JumpServer() {
        if (jumpOptions == null) {
            jumpOptions = new SOSConnection2OptionsAlternate("");
        }
        return jumpOptions;
    }

    public void JumpServer(final SOSConnection2OptionsAlternate val) {
        jumpOptions = val;
    }

    public SOSConnection2OptionsAlternate Target() {
        if (targetOptions == null) {
            targetOptions = new SOSConnection2OptionsAlternate("");
        }
        return targetOptions;
    }

    public void Target(final SOSConnection2OptionsAlternate val) {
        targetOptions = val;
    }
}
