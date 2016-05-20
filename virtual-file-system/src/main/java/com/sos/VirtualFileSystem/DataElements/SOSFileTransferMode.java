package com.sos.VirtualFileSystem.DataElements;

import com.sos.JSHelper.DataElements.JSDataElement;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFileTransferMode extends JSDataElement {

    SOSFileTransferMode() {
        //
    }

    public SOSFileTransferMode(String pstrValue) {
        super(pstrValue);
    }

    public SOSFileTransferMode(String pstrValue, String pstrDescription) {
        super(pstrValue, pstrDescription);
    }

    public SOSFileTransferMode(String pstrValue, String pstrDescription, int pintSize, int pintPos, String pstrFormatString, String pstrColumnHeader,
            String pstrXMLTagName) {
        super(pstrValue, pstrDescription, pintSize, pintPos, pstrFormatString, pstrColumnHeader, pstrXMLTagName);
    }

    public boolean isAscii() {
        return ("ascii".equalsIgnoreCase(this.getValue()) || "text".equalsIgnoreCase(this.getValue()));
    }

    public boolean isBinary() {
        return "binary".equalsIgnoreCase(this.getValue());
    }

}