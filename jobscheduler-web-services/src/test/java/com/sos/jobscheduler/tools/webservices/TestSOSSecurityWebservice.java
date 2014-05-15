package com.sos.jobscheduler.tools.webservices;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import javax.ws.rs.QueryParam;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



public class TestSOSSecurityWebservice {

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
         
        
        assertEquals("testMyWebserviceAnswerEmail",m.getEmail(),email);        
        assertEquals("testMyWebserviceAnswerName",m.getName(),name);        
        assertEquals("testMyWebserviceAnswerTelephone",m.getTelephone(),"03086479034");        
     }
    
     @Test
     public void testStartJob() throws MalformedURLException {
          SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
          String job;
          job = "events2/job_exercise2";
          String user = "SOS01";
          String password = "sos01";
          String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
          SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
          m = sosSecurityWebservice.startJob(m.getSessionId(),job,"","","","myParam=test|yourParam=fest");          
          
         assertEquals("testMyWebserviceAnswerMessage","User SOS01 is not permitted. Missing permission: jobscheduler:joc:command:start:job:myJob",m.getMessage());        
         assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
         assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
      }
     
     @Test
     public void testModifyOrder() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String jobChain = "myJobChain";
         String orderId = "myOrderId";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.modifyOrder (m.getSessionId(),jobChain,orderId,"","","","","","","","","","myParam=test|yourParam=fest");          
         
         
         
        assertEquals("testMyWebserviceAnswerMessage","job chain myJobChain gestartet with order myOrderId",m.getMessage());        
        assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
        assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }

     @Test
     public void testProcessClass() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String processClass = "myProcessClass";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.processClass(m.getSessionId(),"", processClass, "", "", "");          
         
        assertEquals("testMyWebserviceAnswerMessage","job chain myJobChain gestartet with order myOrderId",m.getMessage());        
        assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
        assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }

     @Test
     public void testProcessClassRemove() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String processClass = "myProcessClass";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.processClassRemove(m.getSessionId(),processClass);          
         
        assertEquals("testMyWebserviceAnswerMessage","job chain myJobChain gestartet with order myOrderId",m.getMessage());        
        assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
        assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }

     @Test
     public void testJobChainRemove() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String jobChain = "xxx";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.jobChainRemove(m.getSessionId(),jobChain);          
         
        assertEquals("testMyWebserviceAnswerMessage","job chain myJobChain gestartet with order myOrderId",m.getMessage());        
        assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
        assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }

     @Test
     public void testOrderRemove() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String jobChain = "xxx";
         String order = "2";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.orderRemove(m.getSessionId(),jobChain,order);          
         
        assertEquals("testMyWebserviceAnswerMessage","job chain myJobChain gestartet with order myOrderId",m.getMessage());        
        assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
        assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }

     @Test
     public void testModifyJob() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String job = "myJob";
         String cmd = "stop";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.modifyJob (m.getSessionId(),job,cmd);          
         
         
         
        assertEquals("testMyWebserviceAnswerMessage","job chain myJobChain gestartet with order myOrderId",m.getMessage());        
        assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
        assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }

     @Test
     public void testModifySpooler() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String cmd = "terminate";
         String timeout = "";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.modifySpooler (m.getSessionId(),cmd, timeout);          
         
         
         
        assertEquals("testMyWebserviceAnswerMessage","job chain myJobChain gestartet with order myOrderId",m.getMessage());        
        assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
        assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }

     @Test
     public void testAddOrder() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String jobChain = "myJobChain";
         String orderId = "myOrderId";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.addOrder(m.getSessionId(),jobChain,orderId,"","","","","","","","","");          
         
        assertEquals("testMyWebserviceAnswerMessage","job chain myJobChain gestartet with order myOrderId",m.getMessage());        
        assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
        assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }

     @Test
     public void testLockRemove() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String lock = "mylock";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.lockRemove(m.getSessionId()+"*",lock);          
         
         assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
        assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }
    
     @Test
     public void testKillTask() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String id = "1324";
         String immediately = "yes";
         String job = "myJob";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.killTask(m.getSessionId(),id, immediately, job);          
         
         assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
         assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }
    
     @Test
     public void testJobChainModify() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String jobChain = "myJobChain";
         String state="stopped";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.jobChainModify(m.getSessionId(),jobChain, state);          
         
         assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
         assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }
   
     @Test
     public void testJobChainNodeModify() throws MalformedURLException {
         SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
         String jobChain = "myJobChain";
         String state="100";
         String action = "stop";
         String user = "SOS01";
         String password = "sos01";
         String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
         SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user, password);
         m = sosSecurityWebservice.jobChainNodeModify(m.getSessionId(),jobChain, action, state);          
         
         assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
         assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
     }
   
     @Test
     public void testLogin() throws MalformedURLException {
          SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
          
          String user = "SOS01";
          String password = "sos01";
          String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
          SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user,password);          
          
         assertEquals("testMyWebserviceAnswerMessage","user: SOS01, password: sos01, resource: http://localhost:40040/jobscheduler/rest/sosPermission --> authenticated",m.getMessage());        
         assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
         assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
      }

     @Test
     public void testLogout() throws MalformedURLException {
          SOSCommandSecurityWebservice sosSecurityWebservice = new SOSCommandSecurityWebservice();
          String user = "SOS01";
          String password = "sos01";
          String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
          SOSCommandSecurityWebserviceAnswer m = sosSecurityWebservice.login(user,password);          
       
          
           m = sosSecurityWebservice.logout("test");          
           assertEquals("testMyWebserviceAnswerMessage","SOS01 --> Abgemeldet",m.getMessage());        
          
           m = sosSecurityWebservice.logout("test!");          
           assertEquals("testMyWebserviceAnswerMessage","null --> Abgemeldet",m.getMessage());        
           assertEquals("testMyWebserviceAnswerResource",null,m.getResource());        
       }
     
}
