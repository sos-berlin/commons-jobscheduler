package com.sos.JSHelper.Options;

/** @author KB */
public class SOSOptionFileSize extends SOSOptionString {

    private static final long serialVersionUID = 1449599231135132925L;

    public SOSOptionFileSize(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
            boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public long getFileSize() throws Exception {
        long lngSize;
        String strFilesize = this.strValue;
        if (strFilesize == null || strFilesize.trim().isEmpty()) {
            return -1;
        }
        if (strFilesize.matches("-1")) {
            return -1;
        }
        if (strFilesize.matches("[\\d]+")) {
            lngSize = Long.parseLong(strFilesize);
        } else if (strFilesize.matches("^[\\d]+[kK][bB]$")) {
            lngSize = Long.parseLong(strFilesize.substring(0, strFilesize.length() - 2)) * 1024;
        } else if (strFilesize.matches("^[\\d]+[mM][bB]$")) {
            lngSize = Long.parseLong(strFilesize.substring(0, strFilesize.length() - 2)) * 1024 * 1024;
        } else if (strFilesize.matches("^[\\d]+[gG][bB]$")) {
            lngSize = Long.parseLong(strFilesize.substring(0, strFilesize.length() - 2)) * 1024 * 1024 * 1024;
        } else {
            throw new Exception("The expression [" + strFilesize + "] is no valid file size definition");
        }
        return lngSize;
    }

}