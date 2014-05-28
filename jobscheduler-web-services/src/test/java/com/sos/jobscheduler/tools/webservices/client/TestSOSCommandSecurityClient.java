package com.sos.jobscheduler.tools.webservices.client;

import static org.junit.Assert.*;
 
 

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

 
public class TestSOSCommandSecurityClient {

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
    public void testSOSCommandSecurityClientJavaAddParam() {
        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();
        sosCommandSecurityClient.addParam("myName", "myValue");
        
    }
    
    @Test
    public void testSOSCommandSecurityClientJavaExecuteCommand() {
        SOSCommandSecurityClient sosCommandSecurityClient = new SOSCommandSecurityClient();
        sosCommandSecurityClient.addParam("myName", "myValue");
        try {
            sosCommandSecurityClient.executeCommand(SOS_SECURITY_SERVER + "jobscheduler/engine/plugin/security/login?user=SOS01&password=sos01");
            System.out.println(sosCommandSecurityClient.getAnswer());
            System.out.println(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
           
            
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }

}
