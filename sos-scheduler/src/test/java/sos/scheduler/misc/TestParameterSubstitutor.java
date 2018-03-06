package sos.scheduler.misc;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class TestParameterSubstitutor {

	@Test
	public void testReplaceInFile() throws IOException {
		ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();
		parameterSubstitutor.addKey("SCHEDULER_HOME", String.format("SCHEDULER_HOME=%s", "mySchedulerHome"));
		File in = new File(
				"C:/development_110/products/jitl/jitl-jobs/src/test/java/com/sos/jitl/agentbatchinstaller/jobscheduler_universal_agent_batch_install/batch_install/jobscheduler_agent_instance_script.txt");
		File out = new File("C:/temp/1.txt");
		parameterSubstitutor.replaceInFile(in, out);
	}

	@Test
	public void testReplaceWithEnv() {
		ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();

		String s = parameterSubstitutor.replaceEnvVars("${HOMEDRIVE}");
		assertEquals("testReplaceWithEnv", "C:", s);

	}

	@Test
	public void testGetParameterNameFromString() {
		ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();

		List<String> s = parameterSubstitutor.getParameterNameFromString("ab${param1}12${param2}xyz");
		assertEquals("testGetParameterNameFromString", "param1", s.get(0));
		assertEquals("testGetParameterNameFromString", "param2", s.get(1));
		
		for (String p: s) {
			parameterSubstitutor.addKey(p, "value of " + p);
		}
		String substituted = parameterSubstitutor.replace("this is ${param1} and ${param2}");
		assertEquals("testGetParameterNameFromString", "this is value of param1 and value of param2", substituted);
	}
}
