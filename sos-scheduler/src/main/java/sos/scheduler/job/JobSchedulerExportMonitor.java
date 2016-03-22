package sos.scheduler.job;

import java.io.File;

import sos.spooler.Monitor_impl;
import sos.spooler.Order;
import sos.util.SOSSchedulerLogger;

/** <p>
 * ProcessOrderMonitor implementiert ein Monitor-Script, das pro Auftrag vor
 * bzw. nach dessen Verarbeitung gestartet wird. Das Script wird für Standard
 * Job-Klassen verwendet, an die Auftragsparameter aus der XML-Konfiguration
 * übergeben werden.
 * </p>
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-10-05 */

public class JobSchedulerExportMonitor extends Monitor_impl {

    /** Initialisierung vor Verarbeitung eines Auftrags
     * 
     * @see sos.spooler.Monitor_impl#spooler_process_before() */
    /*
     * public boolean spooler_process_before() { try { return true; } catch
     * (Exception e) {
     * spooler_log.warn("error occurred in spooler_process_before(222): " +
     * e.getMessage()); return false; } }
     */

    /** Cleanup nach Verarbeitung eines Auftrags
     * 
     * @see sos.spooler.Monitor_impl#spooler_process_after() */

    public boolean spooler_process_after(boolean rc) {

        File exportFile = null;
        File moveFile = null;

        try {

            spooler_log.info("calling JobSchedulerExportMonitor.spooler_process_after");// to
                                                                                        // map
                                                                                        // order
                                                                                        // configuration
                                                                                        // to
                                                                                        // this
                                                                                        // job
            sos.spooler.Variable_set set = spooler_task.order().params();

            if (spooler_task.params() != null)
                set.merge(spooler_task.params());
            if (spooler_job.order_queue() != null)
                set.merge(spooler_task.order().params());

            String fname = set.value("filename");
            if (fname != null && fname.length() > 0) {
                exportFile = new File(fname);
                if (!exportFile.exists()) {
                    spooler_log.warn("missing [file=" + exportFile.getCanonicalPath());
                    return rc;
                }
            } else {
                spooler_log.warn("missing parameter [filename]");
                return rc;
            }

            String iname = set.value("move_path");

            if (iname != null && iname.length() > 0) {
                moveFile = new File(iname.endsWith("\\") || iname.endsWith("/") ? iname : iname.concat("/") + exportFile.getName());
                if (!exportFile.renameTo(moveFile)) {
                    spooler_log.warn(exportFile.getCanonicalPath() + " could not move to " + moveFile.getCanonicalPath());
                    return rc;
                }
                spooler_log.info(exportFile.getCanonicalPath() + " moved to " + moveFile.getCanonicalPath());
            }

            // System.out.println(exportFile.getCanonicalPath() + " moved to " +
            // importFile.getCanonicalPath());

            return rc;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_after(): " + e.getMessage());
            return false;
        } finally {
        }
    }

}
