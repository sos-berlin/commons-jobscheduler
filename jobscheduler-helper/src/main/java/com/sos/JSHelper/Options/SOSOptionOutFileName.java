package com.sos.JSHelper.Options;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.sos.JSHelper.io.Files.JSFile;

public class SOSOptionOutFileName extends SOSOptionFileName {

    private static final long serialVersionUID = -8227806083390668082L;
    private static final Logger LOGGER = Logger.getLogger(SOSOptionOutFileName.class);
    private JSFile objFile = null;

    public SOSOptionOutFileName(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    @Override
    public void checkMandatory(final boolean pflgSetMandatory) {
        this.isMandatory(pflgSetMandatory);
        this.checkMandatory();
    }

    @Override
    public void checkMandatory() {
        final String conMethodName = "JSOptionOutFileName::CheckMandatory";
        if (this.isMandatory()) {
            this.getJSFile();
            if (objFile != null) {
                String lstrFileName = strValue;
                try {
                    if (isNotEmpty(lstrFileName)) {
                        lstrFileName = objFile.getCanonicalPath();
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                if (!objFile.canWrite()) {
                    throw new RuntimeException(String.format("%1$s: Has no write permissiong for file '%2$s'", conMethodName, lstrFileName));
                }
            }
        }
    }

    @Override
    public JSFile getJSFile() {
        if (objFile == null && isNotEmpty(strValue)) {
            objFile = new JSFile(strValue);
        }
        return objFile;
    }

}