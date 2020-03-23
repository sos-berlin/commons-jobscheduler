package com.sos.JSHelper.Options;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.System.SOSCommandline;

/** @author KB */
public class SOSOptionPassword extends SOSOptionString {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOptionPassword.class);
    private static final long serialVersionUID = 1374430778591063177L;
    public static final String conBackTic = "`";
    private String strCachedPW = "";

    public SOSOptionPassword(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        this.setHideValue(true);
    }

    @Override
    public String getValue() {
        String strReturnPassword = strValue;
        if (strValue != null) {
            try {
                if (executeCommandToGetPassword()) {
                    if (strCachedPW.isEmpty()) {
                        String command = strValue.substring(1, strValue.length() - 1);
                        Vector returnValues = new SOSCommandline().execute(command);
                        LOGGER.debug(returnValues.toString());
                        Integer exitValue = (Integer) returnValues.elementAt(0);
                        if (exitValue.compareTo(new Integer(0)) == 0 && returnValues.elementAt(1) != null) {
                            strReturnPassword = (String) returnValues.elementAt(1);
                            strCachedPW = strReturnPassword;
                        }
                    } else {
                        strReturnPassword = strCachedPW;
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
        return strReturnPassword;
    }

    public boolean executeCommandToGetPassword() {
        boolean flgExecuteCommandToGetPassword = false;
        if (strValue.startsWith(conBackTic) && strValue.endsWith(conBackTic)) {
            flgExecuteCommandToGetPassword = true;
        }
        return flgExecuteCommandToGetPassword;
    }

}