package sos.scheduler.process;


import sos.util.SOSSchedulerLogger;

/**
 * <p>ASKOrderMonitor implementiert ein Monitor-Script, das pro Auftrag vor bzw. nach dessen Verarbeitung gestartet wird.
 * Das Script wird für Standard Job-Klassen verwendet, an die Auftragsparameter aus der XML-Konfiguration übergeben werden.</p>
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-10-05 
 */

public class ProcessJobMonitor extends ProcessBaseMonitor {


    /**
     * Initialisierung vor Verarbeitung eines Jobs
     * @see sos.spooler.Monitor_impl#spooler_task_before()
     */
    public boolean spooler_task_before() {
        
        try { // to map order configuration to this job
            this.setLogger(new SOSSchedulerLogger(spooler_log));            
            
            if (spooler_task.params().value("configuration_path") != null && spooler_task.params().value("configuration_path").length() > 0)
                this.setConfigurationPath(spooler_task.params().value("configuration_path"));

            if (spooler_task.params().value("configuration_file") != null && spooler_task.params().value("configuration_file").length() > 0)
                this.setConfigurationFilename(spooler_task.params().value("configuration_file"));

            this.prepareConfiguration();

            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_before(): " + e.getMessage());
            return false;
        }
    }

    
    /**
     * Cleanup nach Verarbeitung eines Jobs
     * @see sos.spooler.Monitor_impl#spooler_task_after()
     */

    public boolean spooler_task_after(boolean rc) {
        
        try { // to map order configuration to this job
            this.setLogger(new SOSSchedulerLogger(spooler_log));            

            if (rc == false) spooler_task.order().setback();
            
            return rc;

        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_after(): " + e.getMessage());
            return false;
        } finally {
            try { this.cleanupConfiguration(); } catch (Exception e) {}
        }
    }

}
