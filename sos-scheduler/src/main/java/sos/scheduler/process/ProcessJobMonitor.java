package sos.scheduler.process;

import sos.spooler.Variable_set;

/** @author andreas. pueschel */
public class ProcessJobMonitor extends ProcessBaseMonitor {

    public boolean spooler_task_before() {
        try {
            Variable_set taskParams = spooler_task.params();
            String configurationPath = taskParams.value("configuration_path");
            if (configurationPath != null && !configurationPath.isEmpty()) {
                this.setConfigurationPath(configurationPath);
            }
            String configurationFile = taskParams.value("configuration_file");
            if (configurationFile != null && !configurationFile.isEmpty()) {
                this.setConfigurationFilename(configurationFile);
            }
            prepareConfiguration();
            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_before(): " + e.getMessage());
            return false;
        }
    }

    public boolean spooler_task_after(boolean rc) {
        try {
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
            }
        }
    }

}