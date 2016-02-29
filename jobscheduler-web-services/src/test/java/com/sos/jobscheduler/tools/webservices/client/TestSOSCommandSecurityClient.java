package com.sos.jobscheduler.tools.webservices.client;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TestSOSCommandSecurityClient {

    private static final String SOS_SECURITY_SERVER = "http://8of9.sos:40002/";
    private static final Logger LOGGER = Logger.getLogger(TestSOSCommandSecurityClient.class);

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
            LOGGER.info(sosCommandSecurityClient.getAnswer());
            LOGGER.info(sosCommandSecurityClient.getSosCommandSecurityWebserviceAnswer().getMessage());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
