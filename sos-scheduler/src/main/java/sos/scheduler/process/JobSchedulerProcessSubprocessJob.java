package sos.scheduler.process;

import sos.spooler.Order;
import sos.spooler.Subprocess;
import sos.util.SOSSchedulerLogger;

public class JobSchedulerProcessSubprocessJob extends ProcessOrderJob {

    public boolean spooler_process() {
        Order order = null;
        String orderId = "(none)";
        Subprocess subprocess = null;
        try {
            try {
                this.setLogger(new SOSSchedulerLogger(spooler_log));
                if (spooler_job.order_queue() != null) {
                    order = spooler_task.order();
                    orderId = order.id();
                    if (order.params().value("configuration_path") != null && !order.params().value("configuration_path").isEmpty()) {
                        this.setConfigurationPath(order.params().value("configuration_path"));
                    } else if (spooler_task.params().value("configuration_path") != null
                            && !spooler_task.params().value("configuration_path").isEmpty()) {
                        this.setConfigurationPath(spooler_task.params().value("configuration_path"));
                    }
                    if (order.params().value("configuration_file") != null && !order.params().value("configuration_file").isEmpty()) {
                        this.setConfigurationFilename(order.params().value("configuration_file"));
                    } else if (spooler_task.params().value("configuration_file") != null
                            && !spooler_task.params().value("configuration_file").isEmpty()) {
                        this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
                    }
                    this.initConfiguration();
                }
                this.prepare();
            } catch (Exception e) {
                throw new Exception("error occurred preparing order: " + e.getMessage());
            }
            try {
                if (this.getCommand() == null || this.getCommand().isEmpty()) {
                    throw new Exception("no command was specified to process order");
                }
                subprocess = this.executeSubprocess();
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
            return (spooler_task.job().order_queue() != null) ? true : false;
        } catch (Exception e) {
            spooler_log.warn("error occurred processing order [" + orderId + "]: " + e.getMessage());
            return false;
        } finally {
            try {
                this.cleanup();
            } catch (Exception e) {
            }
            if (subprocess != null) {
                try {
                    subprocess.close();
                } catch (Exception e) {
                }
            }
        }
    }

}