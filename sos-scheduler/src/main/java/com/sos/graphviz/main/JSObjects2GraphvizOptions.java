package com.sos.graphviz.main;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

 
@JSOptionClass(name = "JSObjects2GraphvizOptions", description = "JSObjects2Graphviz")
public class JSObjects2GraphvizOptions extends JSObjects2GraphvizOptionsSuperClass {

 
    private static final long serialVersionUID = 6063266497954052063L;
    @SuppressWarnings("unused")
    private final String conClassName = "JSObjects2GraphvizOptions";  //$NON-NLS-1$

 
    public JSObjects2GraphvizOptions() {
    } // public JSObjects2GraphvizOptions

    public JSObjects2GraphvizOptions(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JSObjects2GraphvizOptions

 
    public JSObjects2GraphvizOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JSObjects2GraphvizOptions (HashMap JSSettings)
 
    @Override
    // JSObjects2GraphvizOptionsSuperClass
    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
