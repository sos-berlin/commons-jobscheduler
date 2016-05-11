package sos.scheduler.process;

import sos.util.SOSSchedulerLogger;

/** @author andreas. pueschel */
public class ProcessJobMonitor extends ProcessBaseMonitor {

    public boolean spooler_task_before() {
        try {
            this.setLogger(new SOSSchedulerLogger(spooler_log));
            if (spooler_task.params().value("configuration_path") != null && !spooler_task.params().value("configuration_path").isEmpty()) {
                this.setConfigurationPath(spooler_task.params().value("configuration_path"));
            }
            if (spooler_task.params().value("configuration_file") != null && !spooler_task.params().value("configuration_file").isEmpty()) {
                this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
            }
            this.prepareConfiguration();
            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_before(): " + e.getMessage());
            return false;
        }
    }

    public boolean spooler_task_after(boolean rc) {
        try {
            this.setLogger(new SOSSchedulerLogger(spooler_log));
            if (!rc) {
                spooler_task.order().setback();
            }
            return rc;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_after(): " + e.getMessage());
            return false;
        } finally {
            try {
                this.cleanupConfiguration();
            } catch (Exception e) {
                //
            }
        }
    }

}