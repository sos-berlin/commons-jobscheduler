package com.sos.JSHelper.Basics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author KB */
public class JSJobUtilitiesClass<T> extends JSToolBox implements JSJobUtilities, IJSCommands {

    protected static final String conSettingEXIT_CODE = "exit_Code";
    protected static final String conSettingSQL_ERROR = "sql_error";
    protected static final String conSettingSTD_ERR_OUTPUT = "std_err_output";
    protected static final String conSettingSTD_OUT_OUTPUT = "std_out_output";
    protected JSJobUtilities objJSJobUtilities = this;
    protected IJSCommands objJSCommands = this;
    protected T objOptions = null;
    protected int exitCode = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSJobUtilitiesClass.class);

    @Deprecated
    public JSJobUtilitiesClass() {
        super();
    }

    public JSJobUtilitiesClass(final T pobjO) {
        super();
        objOptions = pobjO;
    }

    public T getOptions() {
        return objOptions;
    }

    public T getOptions(final T pobjOptions) {
        objOptions = pobjOptions;
        return objOptions;
    }

    public void setJSCommands(final IJSCommands pobjJSCommands) {
        if (pobjJSCommands == null) {
            objJSCommands = this;
        } else {
            objJSCommands = pobjJSCommands;
        }
        LOGGER.trace("pobjJSCommands = " + pobjJSCommands.getClass().getName());
    }

    @Override
    public String replaceSchedulerVars(final String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {
        //
    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuilder pstrValue) {
        //
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        LOGGER.trace("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    @Override
    public void setStateText(final String pstrStateText) {
        //
    }

    @Override
    public void setExitCode(final int code) {
        exitCode = code;
    }

    @Override
    public Object getSpoolerObject() {
        return null;
    }

    @Override
    public String executeXML(final String pstrJSXmlCommand) {
        return "";
    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
        //
    }

}