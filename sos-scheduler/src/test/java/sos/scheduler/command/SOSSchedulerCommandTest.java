package sos.scheduler.command;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSSchedulerCommandTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSchedulerCommandTest.class);

    @Ignore
    @Test
    public void test() {
        SOSSchedulerCommand cmd = new SOSSchedulerCommand("localhost", 4444, "http");
        try {
            String job = "/my_job";
            cmd.connect();
            cmd.sendRequest("<show_history job=\"" + job + "\" />");
            LOGGER.info(cmd.getResponse());
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        } finally {
            try {
                cmd.disconnect();
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }

    }

}
