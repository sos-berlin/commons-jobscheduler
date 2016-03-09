package sos.scheduler.process;

import sos.spooler.Order;

/** <p>
 * JobSchedulerPreparationJob implements the primer job of a job chain that
 * copies parameters from a configuration to the order payload.
 * </p>
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-10-05
 * @deprecated use
 *             sos.scheduler.managed.configuration.JobSchedulerConfigurationPreparationJob */

public class JobSchedulerProcessPreparationJob extends ProcessOrderJob {

    /** Verarbeitung
     * 
     * @see sos.spooler.Job_impl#spooler_process() */
    public boolean spooler_process() {

        Order order = null;
        String orderId = "(none)";

        try {

            try { // to assign a configuration to this order
                if (spooler_job.order_queue() != null) {
                    order = spooler_task.order();
                    orderId = order.id();

                    if (order.params().value("configuration_path") != null && order.params().value("configuration_path").length() > 0) {
                        this.setConfigurationPath(order.params().value("configuration_path"));
                    } else if (spooler_task.params().value("configuration_path") != null
                            && spooler_task.params().value("configuration_path").length() > 0) {
                        this.setConfigurationPath(spooler_task.params().value("configuration_path"));
                    }

                    if (order.params().value("configuration_file") != null && order.params().value("configuration_file").length() > 0) {
                        this.setConfigurationFilename(order.params().value("configuration_file"));
                    } else if (spooler_task.params().value("configuration_file") != null
                            && spooler_task.params().value("configuration_file").length() > 0) {
                        this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
                    }
                }

                // load and assign configuration
                this.initConfiguration();

                // prepare parameters and attributes
                this.prepare();

            } catch (Exception e) {
                throw new Exception("error occurred preparing order: " + e.getMessage());
            }

            return (spooler_task.job().order_queue() != null) ? true : false;

        } catch (Exception e) {
            spooler_log.warn("error occurred processing order [" + orderId + "]: " + e.getMessage());
            return false;
        }
    }

}
