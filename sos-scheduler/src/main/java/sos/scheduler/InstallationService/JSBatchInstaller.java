package sos.scheduler.InstallationService;

import java.util.Locale;

import org.apache.log4j.Logger;

import sos.scheduler.InstallationService.batchInstallationModel.JSBatchInstallerExecuter;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;

@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JSBatchInstaller extends JSToolBox implements JSJobUtilities, IJSCommands {

    private static final Logger LOGGER = Logger.getLogger(JSBatchInstaller.class);
    protected JSBatchInstallerOptions objOptions = null;
    private JSJobUtilities objJSJobUtilities = this;
    private IJSCommands objJSCommands = this;

    public JSBatchInstaller() {
        super();
        Messages = new Messages("com_sos_scheduler_messages", Locale.getDefault());
    }

    public IJSCommands getJSCommands() {
        return objJSCommands;
    }

    public JSBatchInstallerOptions Options() {
        if (objOptions == null) {
            objOptions = new JSBatchInstallerOptions();
        }
        return objOptions;
    }

    public JSBatchInstallerOptions Options(final JSBatchInstallerOptions pobjOptions) {
        objOptions = pobjOptions;
        return objOptions;
    }

    public JSBatchInstaller Execute() throws Exception {
        final String conMethodName = "JSBatchInstaller::Execute";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        try {
            Options().CheckMandatory();
            LOGGER.debug(Options().toString());
            JSBatchInstallerExecuter jsBatchInstaller = new JSBatchInstallerExecuter();
            jsBatchInstaller.performInstallation(this);
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName), e);
        } finally {
            LOGGER.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName));
        }
        return this;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }

    @Override
    public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {
    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        LOGGER.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    @Override
    public String getCurrentNodeName() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setJSCommands(final IJSCommands pobjJSCommands) {
        if (pobjJSCommands == null) {
            objJSCommands = this;
        } else {
            objJSCommands = pobjJSCommands;
        }
        LOGGER.debug("pobjJSCommands = " + pobjJSCommands.getClass().getName());
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
    public void setStateText(final String pstrStateText) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCC(final int pintCC) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
        // TODO Auto-generated method stub

    }

}