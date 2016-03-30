package sos.scheduler.process;

import java.io.File;

import sos.spooler.Order;
import sos.util.SOSSchedulerLogger;

/** <p>
 * ProcessOrderMonitor implementiert ein Monitor-Script, das pro Auftrag vor
 * bzw. nach dessen Verarbeitung gestartet wird. Das Script wird f�r Standard
 * Job-Klassen verwendet, an die Auftragsparameter aus der XML-Konfiguration
 * �bergeben werden.
 * </p>
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-10-05 */

public class ProcessOrderMonitor extends ProcessBaseMonitor {

    /** Initialisierung vor Verarbeitung eines Auftrags
     * 
     * @see sos.spooler.Monitor_impl#spooler_process_before() */
    public boolean spooler_process_before() {

        try { // to map order configuration to this job
            this.setLogger(new SOSSchedulerLogger(spooler_log));
            Order order = spooler_task.order();

            if (order.params().value("configuration_path") != null && order.params().value("configuration_path").length() > 0) {
                this.setConfigurationPath(order.params().value("configuration_path"));
            } else if (spooler_task.params().value("configuration_path") != null && spooler_task.params().value("configuration_path").length() > 0) {
                this.setConfigurationPath(spooler_task.params().value("configuration_path"));
            } else {
                this.setConfigurationPath(new File(spooler.ini_path()).getParent());
                this.getLogger().debug1(".. parameter [configuration_path]: " + this.getConfigurationPath());
            }

            if (order.params().value("configuration_file") != null && order.params().value("configuration_file").length() > 0) {
                this.setConfigurationFilename(order.params().value("configuration_file"));
            } else if (spooler_task.params().value("configuration_file") != null && spooler_task.params().value("configuration_file").length() > 0) {
                this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
            } else {
                if (spooler_job.order_queue() != null) {
                    // this.setConfigurationFilename("scheduler_" +
                    // spooler_task.order().job_chain().name() + "_" +
                    // order.id() + ".config.xml");
                    this.getLogger().debug1(".. parameter [configuration_file]: " + this.getConfigurationFilename());
                    this.setConfigurationFilename("scheduler_" + spooler_task.order().job_chain().name() + ".config.xml");
                    this.getLogger().debug1(".. parameter [configuration_file]: " + this.getConfigurationFilename());
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

    /** Cleanup nach Verarbeitung eines Auftrags
     * 
     * @throws Exception
     * @see sos.spooler.Monitor_impl#spooler_process_after() */

    public boolean spooler_process_after(boolean rc) throws Exception {

        try { // to map order configuration to this job
            this.setLogger(new SOSSchedulerLogger(spooler_log));

            Order order = spooler_task.order();

            if (rc == false) {
                if (order.params() != null
                        && order.params().value("setback") != null
                        && (order.params().value("setback").equalsIgnoreCase("false") || order.params().value("setback").equalsIgnoreCase("no") || order.params().value(
                                "setback").equals("0"))) {
                    // nop
                } else {
                    spooler_task.order().setback();
                }
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
