package com.sos.JSHelper.Basics;

public interface JSJobUtilities {

    public String replaceSchedulerVars(final String string2Modify);

    public void setJSParam(final String key, final String value);

    public void setJSParam(final String key, final StringBuffer value);

    public void setJSJobUtilites(JSJobUtilities val);

    public void setStateText(final String val);

    public void setCC(final int val);

    public void setNextNodeState(final String val);

}