package com.sos.scheduler.model.commands;


import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.commands.JSCmdShowJob.enu4What;
import com.sos.scheduler.model.objects.Spooler;

import org.apache.log4j.Logger;
import org.junit.*;

@Ignore("Test set to Ignore for later examination")
public class JSCmd64Test {
	
	private static Logger logger = Logger.getLogger(JSCmd64Test.class);
	private static SchedulerObjectFactory	objSchedulerObjectFactory	= null;


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		objSchedulerObjectFactory = new SchedulerObjectFactory("homer.sos",4464);
		objSchedulerObjectFactory.initMarshaller(Spooler.class);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		objSchedulerObjectFactory.getSocket().doClose();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	@Test
	public final void testShowJob() {
		String jobName = "/test/javascriptAPI";
		//task_queue, job_params, job_orders, job_commands, description, log, run_time, task_history, source
		//enu4What[] what = JSCmdShowJob.enu4What.values();
		enu4What[] what = {JSCmdShowJob.enu4What.run_time};
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
