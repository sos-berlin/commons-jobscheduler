package com.sos.JSHelper.Options;

import java.util.HashMap;

public class JSMailOptions extends JSOptionsClass {

    private static final long serialVersionUID = 7303809164979485903L;

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        flgSetAllOptions = true;
        flgSetAllOptions = false;
    }

}