package sos.scheduler.file;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB generated */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerFolderTree extends JSToolBox implements JSJobUtilities {

    protected JobSchedulerFolderTreeOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerFolderTree.class);
    private JSJobUtilities objJSJobUtilities = this;
    private SOSXMLHelper objXML = null;
    String strDefaultFileName = "c:/temp/SOSFolderTree.xml";
    String strXSLTFileName = "http://www.sos-berlin.com/schema/SOSFolderTree.xsl";
    String strXMLFileName = "c:/kb/SOSFolderTreeTest.xml";
    String strOutFileName = "";

    public JobSchedulerFolderTree() {
        super("com_sos_scheduler_messages");
    }

    public JobSchedulerFolderTreeOptions Options() {
        if (objOptions == null) {
            objOptions = new JobSchedulerFolderTreeOptions();
        }
        return objOptions;
    }

    public JobSchedulerFolderTreeOptions Options(final JobSchedulerFolderTreeOptions pobjOptions) {
        objOptions = pobjOptions;
        return objOptions;
    }

    public JobSchedulerFolderTree Execute() throws Exception {
        final String methodName = "JobSchedulerFolderTree::Execute";
        LOGGER.debug(Messages.getMsg("JSJ-I-110", methodName));
        try {
            Options().CheckMandatory();
            LOGGER.debug(Options().toString());
            try {
                objXML = new SOSXMLHelper(strXMLFileName, strXSLTFileName);
                SOSFolderStatistic objStat = new SOSFolderStatistic(objXML);
                objXML.XMLTag("FolderTree");
                RecurseFolder(objOptions.file_path.Value(), objStat);
                objXML.XMLTagE("FolderTree");
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                objXML.close();
            }
            LOGGER.debug("***lines written: " + String.valueOf(objXML.NoOfLinesWritten()));
            LOGGER.debug("SOSFolderTree - normal end");
        } catch (Exception e) {
            String strM = Messages.getMsg("JSJ-I-107");
            LOGGER.error(Messages.getMsg(strM, methodName) + " " + e.getMessage(), e);
            throw new JobSchedulerException(strM);
        }
        LOGGER.debug(Messages.getMsg("JSJ-I-111", methodName));
        return this;
    }

    public void init() {
        doInitialize();
    }

    private void RecurseFolder(final String pstrFolderName, final SOSFolderStatistic pobjS) {
        SOSFolderStatistic lobjS = new SOSFolderStatistic(objXML);
        try {
            File flePathName = new File(pstrFolderName);
            if (!flePathName.exists()) {
                throw new Exception("Path does not exist (may be not valid): " + pstrFolderName);
            }
            String[] strFileNames = flePathName.list();
            objXML.XMLTag("Folder");
            objXML.XMLTagV("Name", flePathName.getCanonicalPath());
            for (String strFileName : strFileNames) {
                File fleFile = new File(flePathName.getPath(), strFileName);
                if (fleFile.isDirectory()) {
                    if (lobjS.getNoOfFolders() == 0) {
                        objXML.XMLTag("Folders");
                    }
                    lobjS.incrNoOfFolders();
                    SOSFolderStatistic lobjSt = new SOSFolderStatistic(objXML);
                    RecurseFolder(fleFile.getPath(), lobjSt);
                    pobjS.Cumulate(lobjSt);
                } else {
                    lobjS.incrNoOfFiles();
                    lobjS.incrSize(fleFile.length());
                }
            }
            if (lobjS.getNoOfFolders() > 0) {
                objXML.XMLTagE("Folders");
            }
            lobjS.toXML("FolderStatistic");
            pobjS.Cumulate(lobjS);
            pobjS.toXML("TreeStatistic");
            objXML.XMLTagE("Folder");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void doInitialize() {
        // doInitialize
    }

    @Override
    public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {
        String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
        return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
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