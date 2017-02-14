package sos.scheduler.process;

import java.io.File;

import sos.spooler.Order;

/** @author andreas pueschel */
public class ProcessOrderMonitor extends ProcessBaseMonitor {

    public boolean spooler_process_before() {
        try {
            Order order = spooler_task.order();
            if (order.params().value("configuration_path") != null && !order.params().value("configuration_path").isEmpty()) {
                this.setConfigurationPath(order.params().value("configuration_path"));
            } else if (spooler_task.params().value("configuration_path") != null && !spooler_task.params().value("configuration_path").isEmpty()) {
                this.setConfigurationPath(spooler_task.params().value("configuration_path"));
            } else {
                this.setConfigurationPath(new File(spooler.ini_path()).getParent());
                spooler_log.debug1(".. parameter [configuration_path]: " + this.getConfigurationPath());
            }

            if (order.params().value("configuration_file") != null && !order.params().value("configuration_file").isEmpty()) {
                this.setConfigurationFilename(order.params().value("configuration_file"));
            } else if (spooler_task.params().value("configuration_file") != null && !spooler_task.params().value("configuration_file").isEmpty()) {
                this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
            } else {
                if (spooler_job.order_queue() != null) {
                    spooler_log.debug1(".. parameter [configuration_file]: " + this.getConfigurationFilename());
                    this.setConfigurationFilename("scheduler_" + spooler_task.order().job_chain().name() + ".config.xml");
                    spooler_log.debug1(".. parameter [configuration_file]: " + this.getConfigurationFilename());
                }
            }
            this.initConfiguration();
            this.prepareConfiguration();
            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_before(222): " + e.getMessage());
            return false;
        }
    }

    public boolean spooler_process_after(boolean rc) throws Exception {
        try {
            Order order = spooler_task.order();
            if (!rc && !(order.params() != null && order.params().value("setback") != null && ("false".equalsIgnoreCase(order.params().value("setback"))
                    || "no".equalsIgnoreCase(order.params().value("setback")) || "0".equals(order.params().value("setback"))))) {
                spooler_task.order().setback();
            }
            return rc;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_after(): " + e.getMessage());
            return false;
        } finally {
            try {
                this.cleanupConfiguration();
            } catch (Exception e) {}
        }
    }

}