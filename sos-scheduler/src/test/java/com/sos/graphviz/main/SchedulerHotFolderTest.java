package com.sos.graphviz.main;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.DataElements.JSDateFormat;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSBaseOptions;
import com.sos.VirtualFileSystem.shell.CmdShell;
import com.sos.scheduler.model.SchedulerHotFolder;
import com.sos.scheduler.model.SchedulerHotFolderFileList;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjBase;
import com.sos.scheduler.model.objects.JSObjHolidays;
import com.sos.scheduler.model.objects.JSObjJob;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JSObjLock;
import com.sos.scheduler.model.objects.JSObjOrder;
import com.sos.scheduler.model.objects.JobChain.JobChainNode;
import com.sos.scheduler.model.objects.Spooler;

/** @author oh */
public class SchedulerHotFolderTest {

    private static final String LIVE_FOLDER_LOCATION = "/8of9_buildjars_4210/config/live/";
    private static final String LIVE_LOCAL_FOLDER_LOCATION = "Z:" + LIVE_FOLDER_LOCATION;
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerHotFolderTest.class);
    private static SchedulerObjectFactory objFactory = null;
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer objFileSystemHandler = null;
    private SOSBaseOptions objOptions = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        LOGGER.debug("test start");
        objFactory = new SchedulerObjectFactory("8of9.sos", 4210);
        objFactory.initMarshaller(Spooler.class);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        LOGGER.debug("test ended");
    }

    private final void prepareLocalVfs() {
        try {
            objVFS = VFSFactory.getHandler("local");
            objFileSystemHandler = (ISOSVfsFileTransfer) objVFS;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private final void prepareFtpVfs() {
        objOptions = new SOSBaseOptions();
        objOptions.host.setValue("8of9.sos");
        objOptions.user.setValue("sos");
        objOptions.password.setValue("sos");
        try {
            objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
            objVFS.connect(objOptions);
            objVFS.authenticate(objOptions);
            objFileSystemHandler = (ISOSVfsFileTransfer) objVFS;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public final SchedulerHotFolderFileList loadHotFolder(final String strTestHotFolder) {
        ISOSVirtualFile objHotFolder = objFileSystemHandler.getFileHandle(strTestHotFolder);
        SchedulerHotFolder objSchedulerHotFolder = objFactory.createSchedulerHotFolder(objHotFolder);
        LOGGER.info(String.format("... load %1$s", strTestHotFolder));
        SchedulerHotFolderFileList objSchedulerHotFolderFileList = objSchedulerHotFolder.load();
        objSchedulerHotFolderFileList.getFolderList();
        objSchedulerHotFolderFileList.getJobList();
        objSchedulerHotFolderFileList.getJobChainList();
        objSchedulerHotFolderFileList.getOrderList();
        objSchedulerHotFolderFileList.getProcessClassList();
        objSchedulerHotFolderFileList.getLockList();
        objSchedulerHotFolderFileList.getScheduleList();
        objSchedulerHotFolderFileList.getParamsList();
        for (JSObjBase obj : objSchedulerHotFolderFileList.getSortedFileList()) {
            LOGGER.info(String.format("%1$s is an instance of %2$s", obj.getHotFolderSrc().getName(), obj.getClass()));
            if (obj instanceof SchedulerHotFolder) {
                LOGGER.info(String.format("... load %1$s", obj.getHotFolderSrc().getName()));
            } else {
                LOGGER.info(obj.toXMLString());
            }
        }
        return objSchedulerHotFolderFileList;
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void createDOTFileFromChain() throws Exception {
        boolean flgCreateCluster = false;
        prepareLocalVfs();
        SchedulerHotFolderFileList objSchedulerHotFolderFileList = loadHotFolder(LIVE_LOCAL_FOLDER_LOCATION);
        for (JSObjBase obj : objSchedulerHotFolderFileList.getSortedFileList()) {
            LOGGER.info(String.format("%1$s is an instance of %2$s", obj.getHotFolderSrc().getName(), obj.getClass()));
            if (obj instanceof JSObjJobChain) {
                JSObjJobChain objChain = (JSObjJobChain) obj;
                String strName = objChain.getName();
                if (strName == null) {
                    strName = objChain.getObjectName();
                }
                String strFileName = "c:/temp/dottest/" + strName;
                JSTextFile objDotFile = new JSTextFile(strFileName + ".dot");
                Hashtable<String, JSObjOrder> tblOrders = new Hashtable<String, JSObjOrder>();
                for (JSObjBase objO : objSchedulerHotFolderFileList.getSortedFileList()) {
                    if (objO instanceof JSObjOrder) {
                        JSObjOrder objOrder = (JSObjOrder) objO;
                        String strOrderName = objOrder.getJobChainName();
                        if (strName.equalsIgnoreCase(strOrderName)) {
                            tblOrders.put(strOrderName, objOrder);
                        }
                    }
                }
                objDotFile.writeLine("digraph " + getQuoted(strName) + " {");
                objDotFile.writeLine("rankdir = TB;");
                objDotFile.writeLine("graph [");
                objDotFile.writeLine("label = " + getQuoted(objChain.getTitle()));
                objDotFile.writeLine("fontsize = 14");
                objDotFile.writeLine("];");
                objDotFile.writeLine("node [");
                objDotFile.writeLine("fontsize = 10");
                objDotFile.writeLine("shape = " + getQuoted("box"));
                objDotFile.writeLine("style = " + getQuoted("rounded"));
                objDotFile.writeLine("fontname = " + getQuoted("Arial"));
                objDotFile.writeLine("];");
                Hashtable<String, JobChainNode> tblNodes = new Hashtable<String, JobChainNode>();
                objDotFile.writeLine(getQuoted("start") + " [label = " + getQuoted("start" + ": " + strName) + ", shape = " + getQuoted("box")
                        + ", style = " + getQuoted("solid") + "];");
                objDotFile.writeLine(getQuoted("end") + " [label = " + getQuoted("end" + ": " + strName) + ", shape = " + getQuoted("box")
                        + ", style = " + getQuoted("solid") + "];");
                for (JSObjOrder objOrder : tblOrders.values()) {
                    objDotFile.writeLine(getQuoted(objOrder.getObjectName()) + " [label = " + getQuoted("Order - " + objOrder.getObjectName()) + "];");
                }
                for (Object objO : objChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
                    if (objO instanceof JobChainNode) {
                        JobChainNode objNode = (JobChainNode) objO;
                        String strState = objNode.getState();
                        if (tblNodes.get(strState) == null) {
                            tblNodes.put(strState, objNode);
                            String strJobName = objNode.getJob();
                            if (strJobName == null) {
                                strJobName = "endNode";
                            }
                            String strT = strState;
                            if (!strState.equalsIgnoreCase(strJobName)) {
                                strT = strState + ": " + strJobName;
                            }
                            objDotFile.writeLine(getQuoted(strState) + " [label = " + getQuoted(strT) + "];");
                            String strErrorState = objNode.getErrorState();
                            if (strErrorState != null && tblNodes.get(strErrorState) == null) {
                                tblNodes.put(strErrorState, objNode);
                                objDotFile.writeLine(getQuoted(strErrorState) + " [label = " + getQuoted(strErrorState)
                                        + ", color=\"red\", fillcolor=\"yellow\", style=\"filled\", fontcolor=\"blue\"];");
                            }
                        }
                    }
                }
                for (JSObjOrder objOrder : tblOrders.values()) {
                    objDotFile.writeLine(getQuoted(objOrder.getObjectName()) + " -> " + getQuoted("start"));
                }
                boolean flgStart = true;
                String strState = null;
                String strNextState = null;
                String strLastNextState = null;
                for (Object objO : objChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
                    if (objO instanceof JobChainNode) {
                        JobChainNode objNode = (JobChainNode) objO;
                        strState = objNode.getState();
                        if (flgStart) {
                            flgStart = false;
                            objDotFile.writeLine(getQuoted("start") + " -> " + strState);
                            if (flgCreateCluster) {
                                objDotFile.writeLine("subgraph cluster_0 {");
                                objDotFile.writeLine("    style=filled;");
                                objDotFile.writeLine("color=lightgrey;");
                                objDotFile.writeLine("node [style=filled,color=white];");
                            }
                        }
                        strNextState = objNode.getNextState();
                        if (strNextState != null) {
                            objDotFile.writeLine(getQuoted(strState) + " -> " + getQuoted(strNextState));
                            strLastNextState = strNextState;
                        }
                    }
                }
                objDotFile.writeLine(getQuoted(strLastNextState) + " -> " + "end");
                if (flgCreateCluster) {
                    objDotFile.writeLine("label = \"Process\";");
                    objDotFile.writeLine("}");
                }
                Hashtable<String, JobChainNode> tblErrNodes = new Hashtable<String, JobChainNode>();
                for (Object objO : objChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd()) {
                    if (objO instanceof JobChainNode) {
                        JobChainNode objNode = (JobChainNode) objO;
                        strState = objNode.getState();
                        String strErrorState = objNode.getErrorState();
                        if (strErrorState != null) {
                            objDotFile.writeLine(getQuoted(strState) + " -> " + getQuoted(strErrorState) + " [style=\"dotted\", constraint=false]");
                            tblErrNodes.put(strErrorState, objNode);
                        }
                    }
                }
                if (flgCreateCluster) {
                    objDotFile.writeLine("subgraph cluster_1 {");
                    objDotFile.writeLine("    style=filled;");
                    objDotFile.writeLine("color=lightgrey;");
                    objDotFile.writeLine("node [style=filled,color=white];");
                }
                String strLastErrNode = "";
                for (JobChainNode objErrNode : tblErrNodes.values()) {
                    String strErrNodeName = objErrNode.getErrorState();
                    if (flgCreateCluster) {
                        if (strLastErrNode.isEmpty()) {
                            strLastErrNode = strErrNodeName;
                        } else {
                            objDotFile.writeLine(getQuoted(strLastErrNode) + " -> " + getQuoted(strErrNodeName) + " [style=invis]");
                            strLastErrNode = strErrNodeName;
                        }
                    }
                    objDotFile.writeLine(getQuoted(strErrNodeName) + " -> " + getQuoted("end"));
                }
                if (flgCreateCluster) {
                    objDotFile.writeLine("label = \"Error\";");
                    objDotFile.writeLine("}");
                }
                objDotFile.writeLine("}");
                objDotFile.close();
                CmdShell objShell = new CmdShell();
                String strCommandString = String.format(Dot.Command + " -x -T%1$s %2$s.dot > %2$s.%1$s", "jpg", strFileName);
                objShell.setCommand(strCommandString);
                objShell.run();
            }
        }
    }

    private String getQuoted(final String pstrVal) {
        return "\"" + pstrVal + "\"";
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void loadHotFolderLocal() {
        prepareLocalVfs();
        loadHotFolder(LIVE_LOCAL_FOLDER_LOCATION);
    }

    @Test
    @Ignore("Test set to Ignore for later examination, fails in Jenkins build")
    public final void loadHotFolderFTP() {
        prepareFtpVfs();
        loadHotFolder(LIVE_FOLDER_LOCATION);
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void loadJobChainByVfs() {
        prepareLocalVfs();
        String strTestFilePath = LIVE_LOCAL_FOLDER_LOCATION + "BuildJars.job_chain.xml";
        ISOSVirtualFile objFile = objFileSystemHandler.getFileHandle(strTestFilePath);
        JSObjJobChain objJobChain = new JSObjJobChain(objFactory, objFile);
        objJobChain.setTitle("New Title");
        objJobChain.setOrdersRecoverable(false);
        String xmlStr = objJobChain.toXMLString();
        LOGGER.info(xmlStr);
    }

    @Test
    @Ignore
    public final void loadJobChainByFtpVfs() {
        prepareFtpVfs();
        String strTestFilePath = LIVE_FOLDER_LOCATION + "BuildJars.job_chain.xml";
        ISOSVirtualFile objFile = objFileSystemHandler.getFileHandle(strTestFilePath);
        JSObjJobChain objJobChain = objFactory.createJobChain(objFile);
        objJobChain.setTitle("New Title");
        objJobChain.setOrdersRecoverable(false);
        String xmlStr = objJobChain.toXMLString();
        LOGGER.info(xmlStr);
    }

    @Test
    @Ignore
    public final void loadJobByFtpVfs() {
        prepareFtpVfs();
        String strTestFilePath = LIVE_FOLDER_LOCATION + "junitModel/testOrderJob.job.xml";
        ISOSVirtualFile objFile = objFileSystemHandler.getFileHandle(strTestFilePath);
        JSObjJob objJob = objFactory.createJob(objFile);
        objJob.setTitle("New Title");
        objJob.getScript().getContent();
        String xmlStr = objJob.toXMLString();
        LOGGER.info(xmlStr);
    }

    @Test
    @Ignore
    public final void loadLockByFtpVfs() {
        prepareFtpVfs();
        String strTestFilePath = LIVE_FOLDER_LOCATION + "junitModel/testlock.lock.xml";
        ISOSVirtualFile objFile = objFileSystemHandler.getFileHandle(strTestFilePath);
        JSObjLock objLock = objFactory.createLock(objFile);
        objLock.setMaxNonExclusive(20);
        String xmlStr = objLock.toXMLString();
        LOGGER.info(xmlStr);
        objLock.toXMLFile();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void modifyOrderByVfs() {
        prepareLocalVfs();
        String strTestFilePath = LIVE_LOCAL_FOLDER_LOCATION + "junitModel/testJobChain,testOrder.order.xml";
        ISOSVirtualFile objFile = objFileSystemHandler.getFileHandle(strTestFilePath);
        JSObjOrder objOrder = objFactory.createOrder(objFile);
        objOrder.setTitle("New Title");
        String xmlStr = objOrder.toXMLString();
        LOGGER.info(xmlStr);
        objOrder.toXMLFile();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void loadOrderByVfs() {
        prepareLocalVfs();
        String strTestFilePath = LIVE_LOCAL_FOLDER_LOCATION + "junitModel/testJobChain,testOrder.order.xml";
        ISOSVirtualFile objFile = objFileSystemHandler.getFileHandle(strTestFilePath);
        JSObjOrder objOrder = objFactory.createOrder(objFile);
        String xmlStr = objOrder.toXMLString();
        LOGGER.info(xmlStr);
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void loadHolidaysByVfs() {
        prepareLocalVfs();
        String strTestFilePath = LIVE_LOCAL_FOLDER_LOCATION + "holidays.xml";
        ISOSVirtualFile objFile = objFileSystemHandler.getFileHandle(strTestFilePath);
        JSObjHolidays objHolidays = objFactory.createHolidays(objFile);
        String xmlStr = objHolidays.toXMLString();
        LOGGER.info(xmlStr);
        JSDataElementDate objD = new JSDataElementDate("2011-11-27", JSDateFormat.dfDATE_SHORT);
        LOGGER.info(objD.getValue());
        if (objHolidays.isAHoliday(objD.getDateObject())) {
            LOGGER.info("is a Holiday");
        }
    }

    @Test
    @Ignore
    public void testDateRange() {
        for (int i = 20; i < 60; i++) {
            LOGGER.info(addDays(new Date(), i) + " --- " + i);
        }
    }

    public Date addDays(final Date d, final int days) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d);
        c1.add(Calendar.DAY_OF_YEAR, -days);
        return c1.getTime();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testLastFridayAlgorithm() {
        prepareLocalVfs();
        String strTestFilePath = LIVE_LOCAL_FOLDER_LOCATION + "holidays.xml";
        ISOSVirtualFile objFile = objFileSystemHandler.getFileHandle(strTestFilePath);
        JSObjHolidays objHolidays = objFactory.createHolidays(objFile);
        String xmlStr = objHolidays.toXMLString();
        LOGGER.info(xmlStr);
        Calendar c1 = Calendar.getInstance();
        JSDataElementDate objD = new JSDataElementDate("2011-11-01", JSDateFormat.dfDATE_SHORT);
        int intYear = c1.get(Calendar.YEAR);
        for (int i = 9; i <= 12; i++) {
            int intMonth = i - 1;
            int intLastFridayInMOnth = objD.getLastFridayInAMonth(intMonth, intYear);
            c1.clear();
            c1.setTimeInMillis(new Date().getTime());
            c1.set(intYear, intMonth, intLastFridayInMOnth, 23, 30, 30);
            LOGGER.debug("lastfriday = " + c1.getTime());
            LOGGER.debug("" + c1.getTimeInMillis());
            if (objHolidays.isAHoliday(c1)) {
                LOGGER.info(c1.getTime() + " is a Holiday");
                Calendar c2 = (Calendar) c1.clone();
                while (objHolidays.isAHoliday(c2)) {
                    c2.add(Calendar.DAY_OF_MONTH, 1);
                }
                if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)) {
                    LOGGER.info("later date found on " + c2.getTime());
                }
                c2 = (Calendar) c1.clone();
                while (objHolidays.isAHoliday(c2)) {
                    c2.add(Calendar.DAY_OF_MONTH, -1);
                }
                if (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)) {
                    LOGGER.info("previous date found on " + c2.getTime());
                }
            } else {
                LOGGER.info("valid date found on " + c1.getTime());
            }
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void loadObject() {
        String strTestFilePath = LIVE_LOCAL_FOLDER_LOCATION + "junitModel/testJobChain,testOrder.order.xml";
        File strTestFile = new File(strTestFilePath);
        Object obj = objFactory.unMarshall(strTestFile);
        LOGGER.info("" + obj.getClass());
    }

    @Test
    @Ignore
    public final void loadOrderByFtpVfs() {
        prepareFtpVfs();
        String strTestFilePath = LIVE_FOLDER_LOCATION + "BuildJars,1.order.xml";
        ISOSVirtualFile objFile = objFileSystemHandler.getFileHandle(strTestFilePath);
        JSObjOrder objOrder = objFactory.createOrder(objFile);
        String xmlStr = objOrder.toXMLString();
        LOGGER.info(xmlStr);
    }

}