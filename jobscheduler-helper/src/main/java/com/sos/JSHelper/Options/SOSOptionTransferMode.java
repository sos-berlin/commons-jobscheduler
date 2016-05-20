package com.sos.JSHelper.Options;

import com.sos.JSHelper.enums.enuTransferModes;

/** @author KB */
public class SOSOptionTransferMode extends SOSOptionStringValueList {

    private static final long serialVersionUID = -3336540655234080013L;

    public SOSOptionTransferMode(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        super.valueList(enuTransferModes.getArray());

    }

    public SOSOptionTransferMode(final JSOptionsClass pobjParent, final String pstrIndexedKey, final String pstrDescription,
            final String pstrDefaultValue, final boolean pflgIsMandatory) {
        super(pobjParent, pstrIndexedKey, pstrDescription, pstrDefaultValue, pflgIsMandatory);
    }

    public boolean isAscii() {
        return this.getValue().equalsIgnoreCase(enuTransferModes.ascii.description) || this.getValue().equalsIgnoreCase(enuTransferModes.text.description);
    }

    public boolean isBinary() {
        return this.getValue().equalsIgnoreCase(enuTransferModes.binary.description);
    }

    public boolean isText() {
        return this.getValue().equalsIgnoreCase(enuTransferModes.text.description);
    }

    public String getDescription() {
        String strT = "";
        if (this.isAscii()) {
            strT = enuTransferModes.ascii.description;
        }
        if (this.isText()) {
            strT = enuTransferModes.text.description;
        }
        if (this.isBinary()) {
            strT = enuTransferModes.binary.description;
        }
        return strT;
    }

}