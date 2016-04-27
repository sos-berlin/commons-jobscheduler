package sos.scheduler.xsl;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionOutFileName;

@JSOptionClass(name = "JobSchedulerXslTransformationOptionsSuperClass", description = "JobSchedulerXslTransformationOptionsSuperClass")
public class JobSchedulerXslTransformOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -4196969125579402246L;
    private static final String CLASSNAME = "JobSchedulerXslTransformationOptionsSuperClass";

    @JSOptionDefinition(name = "FileName", description = "", key = "FileName", type = "SOSOptionString", mandatory = true)
    public SOSOptionInFileName FileName = new SOSOptionInFileName(this, CLASSNAME + ".FileName", "", "", "", true);
    public SOSOptionInFileName XMLFileName = (SOSOptionInFileName) FileName.SetAlias("xml_file_name");

    public SOSOptionInFileName getFileName() {
        return FileName;
    }

    public void setFileName(final SOSOptionInFileName p_FileName) {
        FileName = p_FileName;
    }

    @JSOptionDefinition(name = "OutputFileName", description = "", key = "OutputFileName", type = "SOSOptionString", mandatory = true)
    public SOSOptionOutFileName OutputFileName = new SOSOptionOutFileName(this, CLASSNAME + ".OutputFileName", "", "", "", true);

    public SOSOptionOutFileName getOutputFileName() {
        return OutputFileName;
    }

    public void setOutputFileName(final SOSOptionOutFileName p_OutputFileName) {
        OutputFileName = p_OutputFileName;
    }

    @JSOptionDefinition(name = "XslFileName", description = "", key = "XslFileName", type = "SOSOptionString", mandatory = true)
    public SOSOptionInFileName XslFileName = new SOSOptionInFileName(this, CLASSNAME + ".XslFileName", "", "", "", true);

    public SOSOptionInFileName getXslFileName() {
        return XslFileName;
    }

    public void setXslFileName(final SOSOptionInFileName p_XslFileName) {
        XslFileName = p_XslFileName;
    }

    public JobSchedulerXslTransformOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerXslTransformOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerXslTransformOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
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
    public void CommandLineArgs(final String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}