package com.sos.scheduler.generics;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "GenericAPIJobOptionsSuperClass", description = "GenericAPIJobOptionsSuperClass")
public class GenericAPIJobOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 7680682721378489041L;
    private final String conClassName = "GenericAPIJobOptionsSuperClass";

    @JSOptionDefinition(name = "javaClassName", description = "The Name of the Java Class (e.g. a JS Adapter Class) which has to be e", key = "javaClassName",
            type = "SOSOptionString", mandatory = true)
    public SOSOptionString javaClassName = new SOSOptionString(this, conClassName + ".javaClassName", 
            "The Name of the Java Class (e.g. a JS Adapter Class) which has to be e", "", "", true);

    public SOSOptionString getjavaClassName() {
        return javaClassName;
    }

    public void setjavaClassName(final SOSOptionString p_javaClassName) {
        javaClassName = p_javaClassName;
    }

    @JSOptionDefinition(name = "javaClassPath", description = "", key = "javaClassPath", type = "SOSOptionString", mandatory = false)
    public SOSOptionString javaClassPath = new SOSOptionString(this, conClassName + ".javaClassPath", "", " ", " ", false);

    public SOSOptionString getjavaClassPath() {
        return javaClassPath;
    }

    public void setjavaClassPath(final SOSOptionString p_javaClassPath) {
        javaClassPath = p_javaClassPath;
    }

    public GenericAPIJobOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public GenericAPIJobOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public GenericAPIJobOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
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