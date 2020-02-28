package com.sos.VirtualFileSystem.DataElements;

import com.sos.JSHelper.DataElements.JSDataElement;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFileTransferMode extends JSDataElement {

    SOSFileTransferMode() {
        //
    }

    public SOSFileTransferMode(String val) {
        super(val);
    }

    public SOSFileTransferMode(String val, String description) {
        super(val, description);
    }

    public SOSFileTransferMode(String val, String description, int size, int pos, String formatString, String columnHeader, String xmlTagName) {
        super(val, description, size, pos, formatString, columnHeader, xmlTagName);
    }

    public boolean isAscii() {
        return ("ascii".equalsIgnoreCase(this.getValue()) || "text".equalsIgnoreCase(this.getValue()));
    }

    public boolean isBinary() {
        return "binary".equalsIgnoreCase(this.getValue());
    }

}