package sos.scheduler.job;

import java.io.File;

import sos.spooler.Monitor_impl;

/** @author andreas pueschel */
public class JobSchedulerExportMonitor extends Monitor_impl {

    public boolean spooler_process_after(boolean rc) {
        File exportFile = null;
        File moveFile = null;
        try {
            spooler_log.info("calling JobSchedulerExportMonitor.spooler_process_after");
            sos.spooler.Variable_set set = spooler_task.order().params();
            if (spooler_task.params() != null) {
                set.merge(spooler_task.params());
            }
            if (spooler_job.order_queue() != null) {
                set.merge(spooler_task.order().params());
            }
            String fname = set.value("filename");
            if (fname != null && !fname.isEmpty()) {
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
            if (iname != null && !iname.isEmpty()) {
                moveFile = new File(iname.endsWith("\\") || iname.endsWith("/") ? iname : iname.concat("/") + exportFile.getName());
                if (!exportFile.renameTo(moveFile)) {
                    spooler_log.warn(exportFile.getCanonicalPath() + " could not move to " + moveFile.getCanonicalPath());
                    return rc;
                }
                spooler_log.info(exportFile.getCanonicalPath() + " moved to " + moveFile.getCanonicalPath());
            }
            return rc;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_after(): " + e.getMessage());
            return false;
        }
    }

}