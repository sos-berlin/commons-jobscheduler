package com.sos.JSHelper.Options;

import java.util.HashMap;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSOptionInteger extends SOSOptionElement {

    protected int intValue = 0;
    private static final long serialVersionUID = -7044542882191150064L;
    public final String ControlType = "inttext";
    public SOSOptionInteger(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        intOptionType = isOptionTypeInteger;
    }

    public void value(final int pintval) {
        try {
            intValue = pintval;
            this.setValue(new Integer(pintval).toString());
        } catch (final Exception e) {
            //
        }
    }

    public void value(final long pintVal) {
        this.value((int) pintVal);
    }

    public int value() {
        if (strValue.trim().isEmpty()) {
            return 0;
        }
        return new Integer(strValue);
    }

    public boolean compare(final String pstrComparator, final int pintValue1) {
        HashMap<String, Integer> objRelOp = new HashMap<String, Integer>();
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
        objRelOp.put("greater or equal", 5);
        objRelOp.put(">=", 5);
        objRelOp.put("gt", 6);
        objRelOp.put("greater than", 6);
        objRelOp.put(">", 6);
        int intThisValue = this.value();
        boolean flgR = false;
        String strT1 = pstrComparator;
        Integer iOp = objRelOp.get(strT1.toLowerCase());
        if (isNotNull(iOp)) {
            switch (iOp) {
            case 1:
                flgR = pintValue1 == intThisValue;
                break;
            case 2:
                flgR = pintValue1 != intThisValue;
                break;
            case 3:
                flgR = pintValue1 < intThisValue;
                break;
            case 4:
                flgR = pintValue1 <= intThisValue;
                break;
            case 5:
                flgR = pintValue1 >= intThisValue;
                break;
            case 6:
                flgR = pintValue1 > intThisValue;
                break;
            default:
                break;
            }
        } else {
            throw new JobSchedulerException(String.format("Compare operator unknown: '%1$s'", pstrComparator));
        }
        return flgR;
    }

}