package com.sos.JSHelper.Options;

import com.sos.JSHelper.io.Files.JSFile;

/** @author KB */
public class SOSOptionInFileName extends JSOptionInFileName {

    private static final long serialVersionUID = 5320294338809514909L;
    private JSFile objFile = null;

    public SOSOptionInFileName(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        intOptionType = isOptionTypeFileName;
    }

    public void CheckMandatory(final boolean pflgSetMandatory) {
        this.isMandatory(pflgSetMandatory);
        this.CheckMandatory();
    }

    @Override
    public void CheckMandatory() {
        final String conMethodName = "SOSOptionInFileName::CheckMandatory";
        if (this.isMandatory()) {
            this.JSFile();
            if (objFile != null) {
                String lstrFileName = objFile.getPath();
                if (!objFile.exists()) {
                    throw new RuntimeException(String.format("%1$s: file '%2$s' does not exists", conMethodName, lstrFileName));
                }
                if (!objFile.canRead()) {
                    throw new RuntimeException(String.format("%1$s: file '%2$s' is not readable", conMethodName, lstrFileName));
                }
            }
        }
    }

    public JSFile JSFile() {
        if (objFile == null && isNotEmpty(strValue)) {
            objFile = new JSFile(strValue);
        }
        return objFile;
    }

}