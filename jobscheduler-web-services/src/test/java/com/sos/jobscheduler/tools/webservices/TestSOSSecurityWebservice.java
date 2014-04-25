package com.sos.jobscheduler.tools.webservices;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

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
         SOSSecurityWebservice sosSecurityWebservice = new SOSSecurityWebservice();
         String name = "myName";
         String email = "myEmail";
          
         MyWebserviceAnswer m = sosSecurityWebservice.test(name, email);
         
        
        assertEquals("testMyWebserviceAnswerEmail",m.getEmail(),email);        
        assertEquals("testMyWebserviceAnswerName",m.getName(),name);        
        assertEquals("testMyWebserviceAnswerTelephone",m.getTelephone(),"03086479034");        
     }
    
     @Test
     public void testStartJob() throws MalformedURLException {
          SOSSecurityWebservice sosSecurityWebservice = new SOSSecurityWebservice();
          String job = "myJob";
          String user = "SOS01";
          String password = "sos01";
          String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
          sosSecurityWebservice.login(user, password,resource);
          SOSSecurityWebserviceAnswer m = sosSecurityWebservice.startJob(job);          
          
         assertEquals("testMyWebserviceAnswerMessage","User SOS01 is not permitted to excecute start_job for myJob. Missing permission: jobscheduler:joc:command:start:job:myJob",m.getMessage());        
         assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
         assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
      }
     
     @Test
     public void testStartOrder() throws MalformedURLException {
          SOSSecurityWebservice sosSecurityWebservice = new SOSSecurityWebservice();
          String jobChain = "myJobChain";
          String orderId = "myOrderId";
          String user = "SOS01";
          String password = "sos01";
          String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
          sosSecurityWebservice.login(user, password,resource);
          SOSSecurityWebserviceAnswer m = sosSecurityWebservice.startOrder(jobChain,orderId);          
          
         assertEquals("testMyWebserviceAnswerMessage","job chain myJobChain gestartet with order myOrderId",m.getMessage());        
         assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
         assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
      }
     
     @Test
     public void testLogin() throws MalformedURLException {
          SOSSecurityWebservice sosSecurityWebservice = new SOSSecurityWebservice();
          
          String user = "SOS01";
          String password = "sos01";
          String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
          SOSSecurityWebserviceAnswer m = sosSecurityWebservice.login(user,password,resource);          
          
         assertEquals("testMyWebserviceAnswerMessage","user: SOS01, password: sos01, resource: http://localhost:40040/jobscheduler/rest/sosPermission --> authenticated",m.getMessage());        
         assertEquals("testMyWebserviceAnswerResource",resource,m.getResource());        
         assertEquals("testMyWebserviceAnswerUser",user,m.getUser());        
      }

     @Test
     public void testLogout() throws MalformedURLException {
          SOSSecurityWebservice sosSecurityWebservice = new SOSSecurityWebservice();
          String user = "SOS01";
          String password = "sos01";
          String resource = "http://localhost:40040/jobscheduler/rest/sosPermission";
          SOSSecurityWebserviceAnswer m = sosSecurityWebservice.login(user,password,resource);          
       
          
           m = sosSecurityWebservice.logout();          
           assertEquals("testMyWebserviceAnswerMessage","SOS01 --> Abgemeldet",m.getMessage());        
          
           m = sosSecurityWebservice.logout();          
           assertEquals("testMyWebserviceAnswerMessage","null --> Abgemeldet",m.getMessage());        
           assertEquals("testMyWebserviceAnswerResource",null,m.getResource());        
       }
     
}
