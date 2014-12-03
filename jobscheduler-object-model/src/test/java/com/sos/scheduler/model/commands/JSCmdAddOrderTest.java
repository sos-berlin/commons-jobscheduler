package com.sos.scheduler.model.commands;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.answers.Answer;
import com.sos.scheduler.model.answers.ERROR;
import com.sos.scheduler.model.objects.Params;
import com.sos.scheduler.model.objects.Spooler;

@Ignore("Test set to Ignore for later examination")
public class JSCmdAddOrderTest {
	private final static Logger				logger	= Logger.getLogger(JSCmdAddOrderTest.class);
	private static SchedulerObjectFactory	factory	= null;

	@BeforeClass public static void setUpBeforeClass() throws Exception {
		factory = new SchedulerObjectFactory("localhost", 4112);
		factory.initMarshaller(Spooler.class);
	}

	@Test public final void testSetValidXmlContent() {
		JSCmdAddOrder cmdOrder = factory.createAddOrder();
		cmdOrder.setJobChain("/job_chain_multiple_inheritance_sample/job_chain_multiple_inheritance_sample");
		cmdOrder.setReplace("yes");
		cmdOrder.setId("A");
		cmdOrder.setTitle("JobNet: null");
		cmdOrder.setParams(new Params());
		List<Object> objL = cmdOrder.getParams().getParamOrCopyParamsOrInclude();
		objL.add(factory.createParam("successor", "B,C,D,"));
		objL.add(factory.createParam("predecessor", ""));
		objL.add(factory.createParam("script_to_execute", "echo Here is the bootstrap order"));
		objL.add(factory.createParam("uuid_jobnet_identifier", "989ac2ce-2538-4276-8003-3350a6224c97"));
		objL.add(factory.createParam("jobnet", "/job_chain_multiple_inheritance_sample/job_chain_multiple_inheritance_sample"));
		cmdOrder.setAt("2012-04-17 22:00");
		String xml = cmdOrder.toXMLString();
		cmdOrder.run();
		Answer answer = cmdOrder.getAnswer();
		ERROR jsError = answer.getERROR();
		if (jsError != null) {
			logger.debug(xml + "\n" + jsError.getText() + "\n" + factory.answerToXMLString(jsError));
			throw new JobSchedulerException("Error in execution of order");
		}
	}
}
