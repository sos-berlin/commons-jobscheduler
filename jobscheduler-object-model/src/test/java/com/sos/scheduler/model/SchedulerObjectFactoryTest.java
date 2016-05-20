package com.sos.scheduler.model;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionJSTransferMethod.enuJSTransferModes;
import com.sos.scheduler.model.SchedulerObjectFactory.enu4What;
import com.sos.scheduler.model.answers.*;
import com.sos.scheduler.model.answers.Job;
import com.sos.scheduler.model.answers.JobChain;
import com.sos.scheduler.model.answers.JobChains;
import com.sos.scheduler.model.answers.Jobs;
import com.sos.scheduler.model.answers.Order;
import com.sos.scheduler.model.answers.Period;
import com.sos.scheduler.model.answers.ProcessClass;
import com.sos.scheduler.model.answers.ProcessClasses;
import com.sos.scheduler.model.commands.*;
import com.sos.scheduler.model.exceptions.JSCommandErrorException;
import com.sos.scheduler.model.exceptions.JSCommandOKException;
import com.sos.scheduler.model.objects.Include;
import com.sos.scheduler.model.objects.*;
import com.sos.scheduler.model.objects.Job.Description;
import com.sos.scheduler.model.objects.Spooler;

import org.apache.log4j.Logger;
import org.junit.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

/** @author KB */
public class SchedulerObjectFactoryTest extends JSToolBox {

    private static final Logger LOGGER = Logger.getLogger(SchedulerObjectFactoryTest.class);
    private static SchedulerObjectFactory objSchedulerObjectFactory = null;

    public SchedulerObjectFactoryTest() {
        super("com_sos_scheduler_model");
    }

    @Before
    public void setUp() throws Exception {
        LOGGER.debug("test start");
        objSchedulerObjectFactory = new SchedulerObjectFactory("8of9.sos", 4210);
        objSchedulerObjectFactory.initMarshaller(Spooler.class);
    }

    @After
    public void tearDown() throws Exception {
        objSchedulerObjectFactory.getSocket().doClose();
        LOGGER.debug("test ended");
    }

    @Test
    @Ignore
    public final void testSOAP() throws UnsupportedOperationException, SOAPException, MalformedURLException {
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = soapConnectionFactory.createConnection();
        SOAPFactory soapFactory = SOAPFactory.newInstance();
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        SOAPHeader header = message.getSOAPHeader();
        SOAPBody body = message.getSOAPBody();
        header.detachNode();
        Name bodyName = soapFactory.createName("GetLastTradePrice", "m", "http://wombats.ztrade.com");
        SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
        Name name = soapFactory.createName("symbol");
        SOAPElement symbol = bodyElement.addChildElement(name);
        symbol.addTextNode("SUNW");
        LOGGER.info(message.toString());
        URL endpoint = new URL("http://wombat.ztrade.com/quotes");
        SOAPMessage response = connection.call(message, endpoint);
        connection.close();
        SOAPBody soapBody = response.getSOAPBody();
        Iterator iterator = soapBody.getChildElements(bodyName);
        bodyElement = (SOAPBodyElement) iterator.next();
        String lastPrice = bodyElement.getValue();
        LOGGER.info("The last price for SUNW is " + lastPrice);
    }

    @Test
    public final void testCreateSubsystemShow() {
        JSCmdSubsystemShow objSubsystemShow = objSchedulerObjectFactory.createSubsystemShow();
        objSubsystemShow.setWhat(JSCmdSubsystemShow.enu4What.STATISTICS);
        LOGGER.info(objSubsystemShow.toXMLString());
        objSubsystemShow.run();
        Answer objAnswer = objSubsystemShow.getAnswer();
        String xmlStr = objSchedulerObjectFactory.toXMLString(objAnswer);
        LOGGER.info(xmlStr);
        ERROR objERROR = objAnswer.getERROR();
        if (objERROR != null) {
            throw new JSCommandErrorException(objERROR.getText());
        }
    }

    @Test
    public final void testCreateModifyJob() {
        String jobName = "/junitModel/testStandaloneJob";
        JSCmdModifyJob objModifyJob = objSchedulerObjectFactory.createModifyJob();
        objModifyJob.setJob(jobName);
        objModifyJob.setCmd(JSCmdModifyJob.enu4Cmd.STOP);
        objModifyJob.run();
        try {
            objModifyJob.getAnswerWithException();
        } catch (JSCommandOKException e) {
            JSCmdShowJob objShowJob = objSchedulerObjectFactory.createShowJob();
            objShowJob.setJob(jobName);
            objShowJob.setMaxOrders(BigInteger.valueOf(0));
            objShowJob.setMaxTaskHistory(BigInteger.valueOf(0));
            objShowJob.run();
            objModifyJob.setCmd(JSCmdModifyJob.enu4Cmd.UNSTOP);
            objModifyJob.run();
            try {
                objModifyJob.getAnswerWithException();
            } catch (JSCommandOKException ee) {
                objShowJob.run();
            }
        }
    }

    @Test
    public final void testCreateJobChainModify() {
        String jobChainName = "/junitModel/testJobChain";
        JSCmdJobChainModify objModifyJobChain = objSchedulerObjectFactory.createJobChainModify();
        objModifyJobChain.setJobChain(jobChainName);
        objModifyJobChain.setState(JSCmdJobChainModify.enu4State.STOPPED);
        objModifyJobChain.run();
        try {
            objModifyJobChain.getAnswerWithException();
        } catch (JSCommandOKException e) {
            JSCmdShowJobChain objShowJobChain = objSchedulerObjectFactory.createShowJobChain();
            objShowJobChain.setJobChain(jobChainName);
            objShowJobChain.setMaxOrders(BigInteger.valueOf(0));
            objShowJobChain.setMaxOrderHistory(BigInteger.valueOf(0));
            objShowJobChain.run();
            objModifyJobChain.setState(JSCmdJobChainModify.enu4State.RUNNING);
            objModifyJobChain.run();
            try {
                objModifyJobChain.getAnswerWithException();
            } catch (JSCommandOKException ee) {
                objShowJobChain.run();
            }
        }
    }

    @Test
    @Ignore
    public final void testCreateModifySpooler() {
        JSCmdModifySpooler objModifySpooler = objSchedulerObjectFactory.createModifySpooler();
    }

    @Test
    @Ignore
    public final void testCreateModifyOrder() {
        JSCmdModifyOrder objOrder = objSchedulerObjectFactory.createModifyOrder();
        objOrder.setAt("now");
        objOrder.setOrder("1");
        objOrder.setJobChain("BuildJars");
        objOrder.run();
        Answer objA = objOrder.getAnswer();
        ERROR objE = objA.getERROR();
        if (objE != null) {
            fail("order not started. " + objE.getText());
        }
    }

    @Test
    @Ignore
    public final void testStartOrder() {
        JSCmdModifyOrder objOrder = objSchedulerObjectFactory.startOrder("BuildJars", "1");
        try {
            objOrder.getAnswerWithException();
        } catch (JSCommandErrorException e) {
            LOGGER.error(e.getMessage(), e);
            fail(objOrder.getERROR().getText());
        } catch (JSCommandOKException e) {
            //
        }
    }

    @Test(expected = JSCommandOKException.class)
    @Ignore("Test set to Ignore for later examination")
    public final void testStartJob() {
        JSCmdStartJob objOrder = objSchedulerObjectFactory.startJob("/sos/dailyschedule/CheckDaysSchedule");

    }

    @Test
    public final void testCreateAddJobs() {
        // java job
        JSObjJob objJavaJob = objSchedulerObjectFactory.createJob();
        objJavaJob.setName("checkIfWritable");
        objJavaJob.setOrder(false);
        Description objDesc = objSchedulerObjectFactory.createJobDescription();
        Include objDescInclude = objSchedulerObjectFactory.createInclude();
        objDescInclude.setFile("jobs/JobSchedulerCanWrite.xml");
        objDesc.getContent().add(objDescInclude);
        objJavaJob.setDescription(objDesc);
        JSObjScript objScript = objSchedulerObjectFactory.createScript();
        objScript.setLanguage("java");
        objScript.setJavaClass("sos.scheduler.file.JobSchedulerCanWrite");
        objJavaJob.setScript(objScript);
        Params objParams = objSchedulerObjectFactory.setParams(new String[] { "file", "jobs/JobSchedulerCanWrite.xml" });
        objJavaJob.setParams(objParams);
        // javascript job
        JSObjJob objJavaScriptJob = objSchedulerObjectFactory.createJob();
        objJavaScriptJob.setName("hello");
        objJavaScriptJob.setOrder(false);
        JSObjScript objJSScript = objSchedulerObjectFactory.createScript();
        objJSScript.setLanguage("javascript");
        objJSScript.getContent().add("spooler_log.info('hello');");
        objJavaScriptJob.setScript(objJSScript);
        JSCmdAddJobs objAJ = objSchedulerObjectFactory.createAddJobs();
        objAJ.getJob().add(objJavaJob);
        objAJ.getJob().add(objJavaScriptJob);
        LOGGER.debug(objJavaScriptJob.toXMLString());
        objAJ.run();
        try {
            objAJ.getAnswerWithException();
        } catch (JSCommandOKException e) {
            fail(e.getMessage());
        } catch (JSCommandErrorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public final void testCreateAddJobsShort() {
        // java job
        JSObjJob objJavaJob = objSchedulerObjectFactory.createStandAloneJob("checkIfWritable");
        Description objDesc = objSchedulerObjectFactory.createJobDescription();
        Include objDescInclude = objSchedulerObjectFactory.createInclude();
        objDescInclude.setFile("jobs/JobSchedulerCanWrite.xml");
        objDesc.getContent().add(objDescInclude);
        objJavaJob.setDescription(objDesc);
        Script objScript = objJavaJob.getScript();
        objScript.setLanguage("java");
        objScript.setJavaClass("sos.scheduler.file.JobSchedulerCanWrite");
        objJavaJob.setScript(objScript);
        Params objParams = objSchedulerObjectFactory.setParams(new String[] { "file", "jobs/JobSchedulerCanWrite.xml" });
        objJavaJob.setParams(objParams);
        // javascript job
        JSObjJob objJavaScriptJob = objSchedulerObjectFactory.createJob();
        objJavaScriptJob.setName("hello");
        objJavaScriptJob.setOrder(false);
        JSObjScript objJSScript = objSchedulerObjectFactory.createScript();
        objJSScript.setLanguage("javascript");
        objJSScript.getContent().add("spooler_log.info('hello');");
        objJavaScriptJob.setScript(objJSScript);
        JSCmdAddJobs objAJ = objSchedulerObjectFactory.createAddJobs();
        objAJ.add(objJavaJob);
        objAJ.add(objJavaScriptJob);
        objAJ.run();
        try {
            objAJ.getAnswerWithException();
        } catch (JSCommandOKException e) {
            fail(e.getMessage());
        } catch (JSCommandErrorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public final void testCreateStartJob() {
        JSCmdStartJob objStartJob = objSchedulerObjectFactory.createStartJob();
        objStartJob.setForce("yes");
        objStartJob.setJob("/junitModel/testStandaloneJob");
        objStartJob.setParams(new String[] { "Hallo", "Value of Hallo" });
        LOGGER.debug(objStartJob.toXMLString());
        try {
            objStartJob.run();
        } catch (JobSchedulerException e) {
            fail("Job not started");
        } catch (Exception e) {
            fail("Job start results into an exception");
        }
        Task objTask = objStartJob.getTask();
        LOGGER.info("task-id  = " + objTask.getId());
        LOGGER.info("enqueued = " + objTask.getEnqueued());
    }

    @Test(expected = JobSchedulerException.class)
    public final void testCreateStartJob1() {
        JSCmdStartJob objStartJob = objSchedulerObjectFactory.createStartJob();
        objStartJob.setJob("JoeSixpack");
        objStartJob.run();
    }

    @Test
    public final void testCreateShowCalendar() {
        JSCmdShowCalendar objSC = objSchedulerObjectFactory.createShowCalendar();
        objSC.setWhat("orders");
        objSC.setLimit(9999);
        objSC.setFrom("2011-09-23T00:00:00");
        objSC.setBefore("2011-09-23T23:00:00");
        objSC.run();
        Calendar objCalendar = objSC.getCalendar();
        for (Object objCalendarObject : objCalendar.getAtOrPeriod()) {
            if (objCalendarObject instanceof At) {
                At objAt = (At) objCalendarObject;
                LOGGER.debug(objSchedulerObjectFactory.answerToXMLString(objAt));
                LOGGER.debug("Start at :" + objAt.getAt());
                LOGGER.debug("Job Name :" + objAt.getJob());
                LOGGER.debug("Job-Chain Name :" + objAt.getJobChain());
                LOGGER.debug("Order Name :" + objAt.getOrder());
            } else if (objCalendarObject instanceof Period) {
                Period objPeriod = (Period) objCalendarObject;
                LOGGER.debug(objSchedulerObjectFactory.answerToXMLString(objPeriod));
                LOGGER.debug("Absolute Repeat Interval :" + objPeriod.getAbsoluteRepeat());
                LOGGER.debug("Timerange start :" + objPeriod.getBegin());
                LOGGER.debug("Timerange end :" + objPeriod.getEnd());
                LOGGER.debug("Job-Name :" + objPeriod.getJob());
            }
        }
    }

    @Test
    public final void testCreateShowState() {
        JSCmdShowState objShowState = objSchedulerObjectFactory.createShowState(enu4What.all);
        objShowState.run();
    }

    @Test
    public final void testCreateShowState3() {
        JSCmdShowState objShowState = objSchedulerObjectFactory.createShowState(new enu4What[] { enu4What.job_chain_jobs }); // see:
        objShowState.run();
    }

    @Test
    public final void testCreateShowState2() {
        JSCmdShowState objShowState = objSchedulerObjectFactory.createShowState(enu4What.remote_schedulers); // see:
        objShowState.run();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testShowJob() {
        boolean flgIsJobRunning = false;
        String strJobName = "GenericShellExecutor";
        String strJobChainName = "ShellExecutor";
        String strOrderName = "Execute1";
        int intLastHeartBeatCount = 0;
        String strRegExp = "Antwort von";
        int intTimeBetweenCheck = 11;
        int intMaxNoOfRestarts = 10;
        JSCmdBase.flgRaiseOKException = false;
        JSCmdBase.flgLogXML = false;
        JSCmdShowJob objShowJob = objSchedulerObjectFactory.isJobRunning(strJobName);
        if (objShowJob == null) {
            if (isNotEmpty(strOrderName)) {
                objSchedulerObjectFactory.startOrder(strJobChainName, strOrderName);
            } else {
                objSchedulerObjectFactory.startJob(strJobName);
            }
            LOGGER.info(String.format("Job '%1$s' was started.", strJobName));
            sleep(intTimeBetweenCheck * 1000);
        } else {
            LOGGER.info(String.format("Job '%1$s' is already running.", strJobName));
        }
        for (int intNoOfRestarts = 0; intNoOfRestarts < intMaxNoOfRestarts;) {
            objShowJob = objSchedulerObjectFactory.isJobRunning(strJobName);
            if (objShowJob == null) {
                flgIsJobRunning = false;
                JSCmdShowHistory objHist = objSchedulerObjectFactory.createShowHistory();
                objHist.setJob(strJobName);
                objHist.run();
                Answer objHistA = objHist.getAnswer();
                LOGGER.debug(objSchedulerObjectFactory.getLastAnswer());
                List<HistoryEntry> objEntries = objHistA.getHistory().getHistoryEntry();
                if (objEntries != null) {
                    HistoryEntry objEntry = objEntries.get(0);
                    if (objEntry != null) {
                        int intLastExitCode = objEntry.getExitCode().intValue();
                        if (intLastExitCode != 0) {
                            LOGGER.info(String.format("Exitcode of last Execution was '%1$d'", intLastExitCode));
                        }
                    }
                }
            } else {
                flgIsJobRunning = true;
                Answer objAnswer = objShowJob.getAnswer();
                Job objJobAnswer = objAnswer.getJob();
                int intNoOfTasks = objJobAnswer.getTasks().getCount().intValue();
                LOGGER.debug(String.format("Number of tasks currently running is '%1$d'", intNoOfTasks));
                if (intNoOfTasks > 0) {
                    Task objTask = objJobAnswer.getTasks().getTask().get(0);
                    if (objTask != null && isNotEmpty(strRegExp)) {
                        Log objTaskLog = objTask.getLog();
                        String strT = objTaskLog.getContent();
                        // count Heartbeats
                        if (isNotEmpty(strRegExp)) {
                            int intHeartBeatCount = grepCount(strT, strRegExp);
                            if (intHeartBeatCount <= intLastHeartBeatCount) {
                                JSCmdKillTask objKiller = objSchedulerObjectFactory.createKillTask();
                                objKiller.setId(objTask.getId());
                                objKiller.setImmediately("true");
                                objKiller.setJob(strJobName);
                                objKiller.run();
                                LOGGER.info(String.format(
                                        "Job '%1$s' killed due to missing heartbeats '%2$s'. Count is '%3$d', LastCount was '%4$s'", strJobName,
                                        strRegExp, intHeartBeatCount, intLastHeartBeatCount));
                            }
                            intLastHeartBeatCount = intHeartBeatCount;
                        }
                    }
                } else {
                    flgIsJobRunning = false;
                }
            }
            if (!flgIsJobRunning) {
                if (isNotEmpty(strOrderName)) {
                    objSchedulerObjectFactory.startOrder(strJobChainName, strOrderName);
                } else {
                    objSchedulerObjectFactory.startJob(strJobName);
                }
                intNoOfRestarts++;
                LOGGER.info(String.format("Job started again. Number of restarts is '%1$d'", intNoOfRestarts));
                sleep(intTimeBetweenCheck * 1000);
                flgIsJobRunning = true;
                intLastHeartBeatCount = 0;
            } else {
                LOGGER.debug(String.format("sleep for 5 seconds, number of restarts is '%1$d'", intNoOfRestarts));
                sleep(intTimeBetweenCheck * 1000);
            }
        }
    }

    private int grepCount(final String pstrText, final String pstrRegExp) {
        Pattern p = Pattern.compile(pstrRegExp);
        BufferedReader buff = new BufferedReader(new StringReader(pstrText));
        int intCount = 0;
        try {
            String a;
            for (a = buff.readLine(); a != null; a = buff.readLine()) {
                Matcher m = p.matcher(a);
                if (m.find()) {
                    intCount++;
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return intCount;
    }

    private void sleep(final int pintMillis) {
        try {
            Thread.sleep(pintMillis);
        } catch (InterruptedException e) {
            //
        }
    }

    @Test
    public final void testShowJobs() {
        JSCmdShowJobs objCmdShowJobs = objSchedulerObjectFactory.createShowJobs(enu4What.folders);
        objCmdShowJobs.setMaxTaskHistory(BigInteger.valueOf(100));
        objCmdShowJobs.run();
        Jobs objJobs = objCmdShowJobs.getJobs();
        for (Job objJob : objJobs.getJob()) {
            LOGGER.debug(String.format("%1$s - %2$s", objJob.getJob(), objJob.getName()));
        }
    }

    @Test
    public final void testShowJobsViaState() {
        JSCmdShowState objCmdShowState = objSchedulerObjectFactory.createShowState(new enu4What[] { enu4What.folders, enu4What.job_chains });
        objCmdShowState.setMaxTaskHistory(BigInteger.valueOf(100));
        objCmdShowState.run();
        State objState = objCmdShowState.getState();
        Folder objFolder = objState.getFolder();
        if (objFolder != null) {
            traverseFolders(objFolder);
        }
    }

    private void traverseFolders(final Folder pobjFolder) {
        for (Object objAnObject : pobjFolder.getFileBasedOrJobsOrFolders()) {
            if (objAnObject instanceof FileBased) {
                FileBased objFB = (FileBased) objAnObject;
                LOGGER.debug("file = " + objFB.getFile());
            } else if (objAnObject instanceof JobChains) {
                JobChains objJobChains = (JobChains) objAnObject;
                for (JobChain objJobChain : objJobChains.getJobChain()) {
                    LOGGER.debug(objJobChain.getName());
                }
            } else if (objAnObject instanceof Jobs) {
                Jobs objJobs = (Jobs) objAnObject;
                for (Job objJob : objJobs.getJob()) {
                    LOGGER.debug(objJob.getName());
                }
            } else if (objAnObject instanceof Folders) {
                Folders objFolders = (Folders) objAnObject;
                for (Folder objFolder : objFolders.getFolder()) {
                    traverseFolders(objFolder);
                }
            } else if (objAnObject instanceof Orders) {
                Orders objOrders = (Orders) objAnObject;
                for (Order objOrder : objOrders.getOrder()) {
                    LOGGER.debug(objOrder.getName());
                }
            } else if (objAnObject instanceof ProcessClasses) {
                ProcessClasses objProcessClasses = (ProcessClasses) objAnObject;
                for (ProcessClass objProcessClass : objProcessClasses.getProcessClass()) {
                    LOGGER.debug(objProcessClass.getName());
                }
            }
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testShowTask() {
        JSCmdShowTask objCmdShowTask = objSchedulerObjectFactory.createShowTask(enu4What.all);
        objCmdShowTask.setId(BigInteger.valueOf(1386758));
        objCmdShowTask.run();
    }

    @Test
    public final void testCreateShowHistory() {
        JSCmdShowHistory objCmdShowHistory = objSchedulerObjectFactory.createShowHistory();
        objCmdShowHistory.setJob("/junitModel/testStandaloneJob");
        objCmdShowHistory.setMaxOrders(BigInteger.valueOf(9999));
        objCmdShowHistory.run();

    }

    @Test
    public final void testCreateOrder() {
        JSCmdAddOrder objOrder = objSchedulerObjectFactory.createAddOrder();
        objOrder.setJobChain("/test/myJobChain");
        objOrder.setId("myOrder");
        Params objParams = objSchedulerObjectFactory.setParams(new String[] { "param1", "value1" });
        objOrder.setParams(objParams);
        objOrder.setAt("2012-01-01 01:00:00");
        LOGGER.info(objOrder.toXMLString());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testCreateXmlPayload() throws IOException, SAXException, ParserConfigurationException {
        XmlPayload pl = objSchedulerObjectFactory.createXmlPayload();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is =
                new InputSource(new StringReader(
                        "<Xml_payload><params><param name=\"p1\" value=\"V1\"/><param name=\"p2\" value=\"V2\"/></params></Xml_payload>"));
        Document d = builder.parse(is);
        System.out.println(d);
        pl.setAny(d.getDocumentElement());
        LOGGER.info(pl.toXMLString());
    }

    @Test
    public final void testCreateAddOrderUsingUDP() {
        JSCmdAddOrder objOrder = objSchedulerObjectFactory.createAddOrder();
        objOrder.setJobChain("scheduler_sosftp_history");
        objOrder.setTitle("Test for UDP communication method");
        Params objParams = objSchedulerObjectFactory.setParams(new String[] { "Test1", "Test1", "scheduler_job_chain", "scheduler_sosftp_history" });
        objOrder.setParams(objParams);
        objSchedulerObjectFactory.Options().TransferMethod.setValue(enuJSTransferModes.udp.description);
        LOGGER.info(objOrder.toXMLString());
        objOrder.run();
    }

}
