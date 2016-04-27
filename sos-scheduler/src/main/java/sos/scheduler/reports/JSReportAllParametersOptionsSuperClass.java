package sos.scheduler.reports;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JSReportAllParametersOptionsSuperClass", description = "JSReportAllParametersOptionsSuperClass")
public class JSReportAllParametersOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = "JSReportAllParametersOptionsSuperClass";

    @JSOptionDefinition(name = "ReportFileName", description = "The Name of the Report-File. The names and values of all parameters a", key = "ReportFileName",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString ReportFileName = new SOSOptionString(this, CLASSNAME + ".ReportFileName",
            "The Name of the Report-File. The names and values of all parameters a", " ", " ", false);

    public SOSOptionString getReportFileName() {
        return ReportFileName;
    }

    public void setReportFileName(SOSOptionString p_ReportFileName) {
        this.ReportFileName = p_ReportFileName;
    }

    @JSOptionDefinition(name = "ReportFormat", description = "The Format of the report is specified with this parameter. possbile V", key = "ReportFormat",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString ReportFormat = new SOSOptionString(this, CLASSNAME + ".ReportFormat",
            "The Format of the report is specified with this parameter. possbile V", "text", "text", false);

    public SOSOptionString getReportFormat() {
        return ReportFormat;
    }

    public void setReportFormat(SOSOptionString p_ReportFormat) {
        this.ReportFormat = p_ReportFormat;
    }

    public JSReportAllParametersOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JSReportAllParametersOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JSReportAllParametersOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}