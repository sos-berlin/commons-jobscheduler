package com.sos.JSHelper.Basics;

import org.apache.log4j.Logger;

/** \class JSJobUtilitiesClass
 *
 * \brief JSJobUtilitiesClass -
 *
 * \details
 *
 * \code .... code goes here ... \endcode
 *
 * <p style="text-align:center">
 * <br />
 * --------------------------------------------------------------------------- <br />
 * APL/Software GmbH - Berlin <br />
 * ##### generated by ClaviusXPress (http://www.sos-berlin.com) ######### <br />
 * ---------------------------------------------------------------------------
 * </p>
 * \author KB
 * 
 * @version $Id$15.06.2010 \see reference
 *
 *          Created on 15.06.2010 16:15:36 */

/** @author KB */
public class JSJobUtilitiesClass<T> extends JSToolBox implements JSJobUtilities, IJSCommands {

    private final String conClassName = "JSJobUtilitiesClass";
    private static final Logger logger = Logger.getLogger(JSJobUtilitiesClass.class);

    protected JSJobUtilities objJSJobUtilities = this;
    // protected final String conMessageFilePath = "com_sos_scheduler_messages";
    protected IJSCommands objJSCommands = this;

    protected static final String conSettingEXIT_CODE = "exit_Code";
    protected static final String conSettingSQL_ERROR = "sql_error";
    protected static final String conSettingSTD_ERR_OUTPUT = "std_err_output";
    protected static final String conSettingSTD_OUT_OUTPUT = "std_out_output";

    protected T objOptions = null;

    @Deprecated
    public JSJobUtilitiesClass() {
        super();
        // Messages = new Messages(conMessageFilePath, Locale.getDefault());
    }

    public JSJobUtilitiesClass(final T pobjO) {
        super();
        objOptions = pobjO;
    }

    /** \brief Options - OptionClass
     *
     * \details The OptionClass is used as a Container for all Options
     * (Settings) which are needed.
     *
     * \return T Options */

    public T Options() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

        if (objOptions == null) {
            // objOptions = new class <T>();
        }
        return objOptions;
    }

    /** \brief Options - set the SOSSQLPlusJobOptionClass
     *
     * \details The SOSSQLPlusJobOptionClass is used as a Container for all
     * Options (Settings) which are needed.
     *
     * \return SOSSQLPlusJobOptions */

    public T Options(final T pobjOptions) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

        objOptions = pobjOptions;
        return objOptions;
    }

    /** \brief setJSCommands
     *
     * \details
     *
     * \return void
     *
     * @param pobjJSCommands */
    public void setJSCommands(final IJSCommands pobjJSCommands) {
        if (pobjJSCommands == null) {
            objJSCommands = this;
        } else {
            objJSCommands = pobjJSCommands;
        }
        logger.trace("pobjJSCommands = " + pobjJSCommands.getClass().getName());
    }


    /** \brief replaceSchedulerVars
     *
     * \details Dummy-Method to make sure, that there is always a valid Instance
     * for the JSJobUtilities. \return
     *
     * @param isWindows
     * @param pstrString2Modify
     * @return */
    @Override
    public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
        logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    /** \brief setJSParam
     *
     * \details Dummy-Method to make sure, that there is always a valid Instance
     * for the JSJobUtilities. \return
     *
     * @param pstrKey
     * @param pstrValue */
    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {
        // logger.trace(String.format("*mock* set param '%1$s' to value '%2$s'",
        // pstrKey, pstrValue));
    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
        // logger.trace(String.format("*mock* set param '%1$s' to value '%2$s'",
        // pstrKey, pstrValue));
    }

    /** \brief setJSJobUtilites
     *
     * \details The JobUtilities are a set of methods used by the SSH-Job or can
     * be used be other, similar, job- implementations.
     *
     * \return void
     *
     * @param pobjJSJobUtilities */
    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {

        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        logger.trace("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    @Override
    public String getCurrentNodeName() {
        return null;
    }

    @Override
    public void setStateText(final String pstrStateText) {
        // logger.trace(String.format("*mock* Set job chain state text to '%1$s'",
        // pstrStateText));
    }

    @Override
    public void setCC(final int pintCC) {
        intCC = pintCC;
        // logger.trace(String.format("*mock* Set Condition code/return code to '%1$d'",
        // pintCC ));
    }

    protected int intCC = 0;

    public int getCC() {
        return intCC;
    }

    @Override
    public Object getSpoolerObject() {
        return null;
    }

    @Override
    public String executeXML(final String pstrJSXmlCommand) {
        // logger.info("***mock***\n" + pstrJSXmlCommand);
        return "";
    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
        // logger.info("***mock***\n" + pstrNodeName);
    }

}
