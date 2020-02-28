package com.sos.VirtualFileSystem.zip;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class JSNotImplementedException extends JobSchedulerException {

    private static final long serialVersionUID = -2550077407292808377L;
    @SuppressWarnings("unused")
    private final String conClassName = "JSNotImplementedException";

    public JSNotImplementedException() {
        //
    }
}
