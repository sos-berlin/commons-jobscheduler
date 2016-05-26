package com.sos.scheduler.model.commands;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.commands.JSCmdShowJob.enu4What;
import com.sos.scheduler.model.objects.Spooler;

@Ignore("Test set to Ignore for later examination")
public class JSCmd64Test {

    private static SchedulerObjectFactory objSchedulerObjectFactory = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        objSchedulerObjectFactory = new SchedulerObjectFactory("homer.sos", 4464);
        objSchedulerObjectFactory.initMarshaller(Spooler.class);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        objSchedulerObjectFactory.getSocket().doClose();
    }

    @Test
    public final void testShowJob() {
        String jobName = "/test/javascriptAPI";
        enu4What[] what = { JSCmdShowJob.enu4What.run_time };
        JSCmdShowJob cmdShowJob = objSchedulerObjectFactory.createShowJob(jobName, what);
        cmdShowJob.run();
    }

    @Test
    public final void testShowJobChain() {
        String jobChainName = "/test/job_chain1";
        JSCmdShowJobChain cmdShowJobChain = objSchedulerObjectFactory.createShowJobChain();
        cmdShowJobChain.setJobChain(jobChainName);
        cmdShowJobChain.run();
    }

}