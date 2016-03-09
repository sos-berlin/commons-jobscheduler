package com.sos.jobscheduler.tools.webservices;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import javax.ws.rs.QueryParam;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;

import sos.xml.SOSXMLXPath;

import com.sos.jobscheduler.tools.webservices.client.SOSCommandSecurityClient;
import com.sos.jobscheduler.tools.webservices.globals.MyWebserviceAnswer;
import com.sos.jobscheduler.tools.webservices.globals.SOSCommandSecurityWebserviceAnswer;

public class TestSOSSecurityWebservice {

    private static final String LOGIN_USER = "root";
    private static final String LOGIN_PWD = "root";
    private static final String SOS_SECURITY_SERVER = "http://8of9.sos:40002/";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testMyWebserviceAnswer() {
        SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
        String name = "myName";
        String email = "myEmail";

        MyWebserviceAnswer m = sosSecurityWebservice.test(name, email);

        assertEquals("testMyWebserviceAnswerEmail", m.getEmail(), email);
        assertEquals("testMyWebserviceAnswerName", m.getName(), name);
        assertEquals("testMyWebserviceAnswerTelephone", m.getTelephone(), "03086479034");
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testStartJob() throws Exception {

        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();
        sosCommandSecurityClient.addParam("myName", "myValue");

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/start_job?job=test/jobJunit&params=ping=33|host=localhost&session_id=" + session);
        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());

    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testModifyOrder() throws Exception {

        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();
        sosCommandSecurityClient.addParam("myName", "myValue");

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/modify_order?job_chain=test/job_chain_junit&order=test&suspended=yes&session_id=" + session);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/modify_order?job_chain=test/job_chain_junit&order=test&suspended=no&session_id=" + session);
        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());

    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testProcessClass() throws Exception {

        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();
        sosCommandSecurityClient.addParam("myName", "myValue");

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/process_class?name=myProcessclass&session_id=" + session);
        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());

    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testProcessClassRemove() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();
        sosCommandSecurityClient.addParam("myName", "myValue");

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/process_class?name=myProcessclass&session_id=" + session);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/process_class_remove?process_class=myProcessclass&session_id=" + session);

        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());

    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testJobChainRemove() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/add_order?job_chain=test/job_chain_prepare&session_id=" + session);
        java.lang.Thread.sleep(4000);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/remove_job_chain?job_chain=test&session_id=" + session);

        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testOrderRemove() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/add_order?job_chain=test/job_chain_junit&order=3&at=now%2B60&session_id=" + session);
        java.lang.Thread.sleep(4000);

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/remove_order?job_chain=test/job_chain_junit&order=3&session_id=" + session);

        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testModifyJob() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();
        sosCommandSecurityClient.addParam("myName", "myValue");

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/add_order?state=200&job_chain=test/job_chain_prepare&session_id=" + session);
        java.lang.Thread.sleep(4000);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/add_order?job_chain=test/job_chain_prepare&session_id=" + session);

        java.lang.Thread.sleep(4000);

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/modify_job?job=test&cmd=stop&action=stop&session_id=" + session);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/modify_job?job=test&cmd=unstop&action=stop&session_id=" + session);
        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();

        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testModifySpooler() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();
        sosCommandSecurityClient.addParam("myName", "myValue");

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/modify_spooler?cmd=pause&session_id=" + session);
        java.lang.Thread.sleep(8000);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/modify_spooler?cmd=continue&session_id=" + session);
        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();

        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testAddOrder() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/add_order?job_chain=test/job_chain_prepare&session_id=" + session);

        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());

    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testLockRemove() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/lock?name=mylock&session_id="
                + session);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/lock_remove?lock=mylock&session_id=" + session);

        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());

    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testKillTask() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();

        // create a task
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/start_job?job=test&session_id="
                + session);

        String answer = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer();

        // get id and job from scheduler answer
        SOSXMLXPath xPath;

        xPath = new SOSXMLXPath(new StringBuffer(answer));
        Node n = xPath.selectSingleNode("//task");
        String id = n.getAttributes().getNamedItem("id").getNodeValue();
        String job = n.getAttributes().getNamedItem("job").getNodeValue();

        // kill the task
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/kill_task?id=" + id + "&job="
                + job + "&immediately=yes&session_id=" + session);

        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testJobChainModify() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/add_order?job_chain=test/job_chain_prepare&session_id=" + session);
        java.lang.Thread.sleep(4000);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/job_chain_modify?job_chain=test&state=stopped&session_id=" + session);

        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testJobChainNodeModify() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/add_order?job_chain=test/job_chain_prepare&session_id=" + session);
        java.lang.Thread.sleep(4000);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/job_chain_modify?job_chain=test&state=running&session_id=" + session);
        sosCommandSecurityClient.uncheckedExecuteCommand(SOS_SECURITY_SERVER
                + "jobscheduler/engine/plugin/security/job_chain_node_modify?job_chain=test&state=100&action=stop&session_id=" + session);

        sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser();
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getJobSchedulerAnswer());

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testLogin() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());

    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testLogout() throws Exception {
        String myUser = LOGIN_USER;
        String myPwd = LOGIN_PWD;

        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();

        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=" + myUser + "&password="
                + myPwd);
        System.out.println(sosCommandSecurityClient.getAnswer());
        System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        String session = sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getSessionId();
        sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/logout?session_id=" + session);

        assertEquals("testMyWebserviceAnswerMessage", myUser, sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getUser());
    }

}
