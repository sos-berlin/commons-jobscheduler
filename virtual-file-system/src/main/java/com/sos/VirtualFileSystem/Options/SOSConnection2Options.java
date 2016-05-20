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
    private SOSConnection2OptionsAlternate objAlternativeOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate objSourceOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate objTargetOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate objJumpServerOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate")
    private SOSConnection2OptionsAlternate objProxyServerOptions = null;

    public SOSConnection2Options() {
        super();
        initChildOptions();
    }

    public SOSConnection2Options(final String strPrefix) {
        //
    }

    @Deprecated
    public SOSConnection2Options(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public SOSConnection2Options(final HashMap<String, String> pobjJSSettings) throws Exception {
        super(pobjJSSettings);
        initChildOptions();
        setPrefixedValues(pobjJSSettings);
    }

    private void initChildOptions() {
        if (objAlternativeOptions == null) {
            objAlternativeOptions = new SOSConnection2OptionsAlternate("");
        }
        if (objSourceOptions == null) {
            objSourceOptions = new SOSConnection2OptionsAlternate("");
        }
        objSourceOptions.isSource = true;
        if (objTargetOptions == null) {
            objTargetOptions = new SOSConnection2OptionsAlternate("");
            objTargetOptions.isSource = false;
        }
        if (objJumpServerOptions == null) {
            objJumpServerOptions = new SOSConnection2OptionsAlternate("");
        }
        if (objProxyServerOptions == null) {
            objProxyServerOptions = new SOSConnection2OptionsAlternate("");
        }
    }

    public void setPrefixedValues(final HashMap<String, String> pobjJSSettings) throws Exception {
        objAlternativeOptions.setAllOptions(pobjJSSettings, strAlternativePrefix + conParamNamePrefixALTERNATIVE);
        this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
        objAlternativeOptions.setAllOptions(pobjJSSettings);
        this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
        objSourceOptions.setAllOptions(pobjJSSettings, conParamNamePrefixSOURCE);
        objSourceOptions.getAlternatives().setChildClasses(pobjJSSettings, conParamNamePrefixSOURCE);
        objSourceOptions.setChildClasses(pobjJSSettings, conParamNamePrefixSOURCE);
        this.addProcessedOptions(objSourceOptions.getProcessedOptions());
        objTargetOptions.setAllOptions(pobjJSSettings, conParamNamePrefixTARGET);
        objTargetOptions.getAlternatives().setChildClasses(pobjJSSettings, conParamNamePrefixTARGET);
        objTargetOptions.setChildClasses(pobjJSSettings, conParamNamePrefixTARGET);
        this.addProcessedOptions(objTargetOptions.getProcessedOptions());
        objJumpServerOptions.setAllOptions(pobjJSSettings, conParamNamePrefixJUMP);
        this.addProcessedOptions(objJumpServerOptions.getProcessedOptions());
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
        return objAlternativeOptions;
    }

    public void setAlternatives(final SOSConnection2OptionsAlternate pobjAlternativeOptions) {
        if (objAlternativeOptions == null) {
            objAlternativeOptions = new SOSConnection2OptionsAlternate(conParamNamePrefixALTERNATIVE);
        }
        objAlternativeOptions = pobjAlternativeOptions;
    }

    public SOSConnection2OptionsAlternate getSource() {
        if (objSourceOptions == null) {
            objSourceOptions = new SOSConnection2OptionsAlternate(conParamNamePrefixSOURCE);
        }
        return objSourceOptions;
    }

    public void setSource(final SOSConnection2OptionsAlternate pobjSourceOptions) {
        objSourceOptions = pobjSourceOptions;
    }

    public SOSConnection2OptionsAlternate getJumpServer() {
        if (objJumpServerOptions == null) {
            objJumpServerOptions = new SOSConnection2OptionsAlternate("");
        }
        return objJumpServerOptions;
    }

    public void setJumpServer(final SOSConnection2OptionsAlternate pobjJumpServerOptions) {
        objJumpServerOptions = pobjJumpServerOptions;
    }

    public SOSConnection2OptionsAlternate getTarget() {
        if (objTargetOptions == null) {
            objTargetOptions = new SOSConnection2OptionsAlternate("");
        }
        return objTargetOptions;
    }

    public void setTarget(final SOSConnection2OptionsAlternate pobjTargetOptions) {
        objTargetOptions = pobjTargetOptions;
    }
    
}