package com.sos.VirtualFileSystem.DataElements;

import com.sos.JSHelper.DataElements.JSDataElement;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFolderName extends JSDataElement {

    SOSFolderName() {
        //
    }

    public SOSFolderName(String val) {
        super(val);
    }

    public SOSFolderName(String val, String description) {
        super(val, description);
    }

    public SOSFolderName(String value, String description, int size, int pos, String formatString, String columnHeader, String xmlTagName) {
        super(value, description, size, pos, formatString, columnHeader, xmlTagName);
    }
}
