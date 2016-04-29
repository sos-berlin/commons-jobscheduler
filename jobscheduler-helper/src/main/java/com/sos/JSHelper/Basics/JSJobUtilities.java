package com.sos.JSHelper.Basics;

/** @author KB */
public interface JSJobUtilities {

    public String replaceSchedulerVars(final String pstrString2Modify);

    public void setJSParam(final String pstrKey, final String pstrValue);

    public void setJSParam(final String pstrKey, final StringBuffer pstrValue);

    public String getCurrentNodeName();

    public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities);

    public void setStateText(final String pstrStateText);

    public void setCC(final int pintCC);

    public void setNextNodeState(final String pstrNodeName);

}