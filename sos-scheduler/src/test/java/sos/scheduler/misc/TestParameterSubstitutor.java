package sos.scheduler.misc;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestParameterSubstitutor {

    @Test
    public void testReplaceInFile() throws IOException {
        ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();
        parameterSubstitutor.addKey("SCHEDULER_HOME", String.format("SCHEDULER_HOME=%s", "mySchedulerHome"));
        File in =
                new File(
                        "C:/development_110/products/jitl/jitl-jobs/src/test/java/com/sos/jitl/agentbatchinstaller/jobscheduler_universal_agent_batch_install/batch_install/jobscheduler_agent_instance_script.txt");
        File out = new File("C:/temp/1.txt");
        parameterSubstitutor.replaceInFile(in, out);
    }

}
