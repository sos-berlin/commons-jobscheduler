package com.sos.JSHelper.Options;

import java.util.HashMap;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "com_sos_JSHelper_Messages", defaultLocale = "en")
public class SOSOptionRelOp extends SOSOptionString {

    private static final long serialVersionUID = 1935027172360607987L;
    private static final String JSH_T_0010 = "JSH_T_0010";
    private static final String JSJ_E_0017 = "JSJ_E_0017";
    private static HashMap<String, Integer> objRelOp = null;
 
    public SOSOptionRelOp(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
            boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        setRelOpTab();
    }

    private void setRelOpTab() {
        this.setMessageResource("com_sos_JSHelper_Messages");
        if (objRelOp == null) {
            objRelOp = new HashMap<String, Integer>();
        }
        objRelOp.put("eq", 1);
        objRelOp.put("equal", 1);
        objRelOp.put("==", 1);
        objRelOp.put("=", 1);
        objRelOp.put("ne", 2);
        objRelOp.put("not equal", 2);
        objRelOp.put("!=", 2);
        objRelOp.put("<>", 2);
        objRelOp.put("lt", 3);
        objRelOp.put("less than", 3);
        objRelOp.put("<", 3);
        objRelOp.put("le", 4);
        objRelOp.put("less or equal", 4);
        objRelOp.put("<=", 4);
        objRelOp.put("ge", 5);
        objRelOp.put(Messages.getMsg(JSH_T_0010), 5);
        objRelOp.put(">=", 5);
        objRelOp.put("gt", 6);
        objRelOp.put("greater than", 6);
        objRelOp.put(">", 6);
    }

    @Override
    public void setValue(final String pstrValue) {
        if (objRelOp == null) {
            setRelOpTab();
        }
        if (pstrValue.trim().isEmpty()) {
            strValue = pstrValue;
        } else {
            Integer iOp = objRelOp.get(pstrValue.toLowerCase());
            if (isNotNull(iOp)) {
                this.strValue = pstrValue;
            } else {
                throw new JobSchedulerException(Messages.getMsg(JSJ_E_0017, pstrValue));
            }
        }
    }

    public boolean compareIntValues(int pintValue1, int pintValue2) {
        boolean flgR = false;
        String strT1 = strValue;
        Integer iOp = objRelOp.get(strT1.toLowerCase());
        if (isNotNull(iOp)) {
            switch (iOp) {
            case 1:
                flgR = pintValue1 == pintValue2;
                break;
            case 2:
                flgR = pintValue1 != pintValue2;
                break;
            case 3:
                flgR = pintValue1 < pintValue2;
                break;
            case 4:
                flgR = pintValue1 <= pintValue2;
                break;
            case 5:
                flgR = pintValue1 >= pintValue2;
                break;
            case 6:
                flgR = pintValue1 > pintValue2;
                break;
            default:
                break;
            }
        } else {
            throw new JobSchedulerException(Messages.getMsg(JSJ_E_0017, strValue));
        }
        return flgR;
    }

}