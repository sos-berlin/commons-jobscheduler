package sos.scheduler.job;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import sos.spooler.Job_impl;
import sos.util.SOSFile;
import sos.util.SOSGZip;

/** @author andreas pueschel */
public class JobSchedulerRotateLog extends Job_impl {

    public boolean spooler_process() {
        String filePath = spooler.log_dir();
        long fileAge = 14;
        long deleteFileAge = 0;
        String fileSpec = "^(scheduler)([0-9\\-]+)";
        if (spooler.id() != null && !spooler.id().isEmpty()) {
            fileSpec += "(\\." + spooler.id() + ")";
        }
        fileSpec += "(\\.log)$";
        String deleteFileSpec = "^(scheduler)([0-9\\-]+)";
        if (spooler.id() != null && !spooler.id().isEmpty()) {
            deleteFileSpec += "(\\." + spooler.id() + ")";
        }
        deleteFileSpec += "(\\.log)(\\.gz)?$";
        if (spooler_task.params().var("file_path") != null && !spooler_task.params().var("file_path").isEmpty()) {
            filePath = spooler_task.params().var("file_path");
            spooler_log.info(".. job parameter [file_path]: " + filePath);
        }
        if (spooler_task.params().var("file_specification") != null && !spooler_task.params().var("file_specification").isEmpty()) {
            fileSpec = spooler_task.params().var("file_specification");
            spooler_log.info(".. job parameter [file_specification]: " + fileSpec);
        }
        if (spooler_task.params().var("file_age") != null && !spooler_task.params().var("file_age").isEmpty()) {
            fileAge = Long.parseLong(spooler_task.params().var("file_age"));
            spooler_log.info(".. job parameter [file_age]: " + fileAge);
        }
        if (spooler_task.params().var("delete_file_age") != null && !spooler_task.params().var("delete_file_age").isEmpty()) {
            deleteFileAge = Long.parseLong(spooler_task.params().var("delete_file_age"));
            spooler_log.info(".. job parameter [delete_file_age]: " + deleteFileAge);
        }
        if (spooler_task.params().var("delete_file_specification") != null && !spooler_task.params().var("delete_file_specification").isEmpty()) {
            deleteFileSpec = spooler_task.params().var("delete_file_specification");
            spooler_log.info(".. job parameter [delete_file_specification]: " + deleteFileSpec);
        }
        try {
            int counter = 0;
            int deleteCounter = 0;
            if (!filePath.endsWith("/")) {
                filePath += "/";
            }
            if (deleteFileAge > 0) {
                Vector deleteFilelist = SOSFile.getFilelist(filePath, deleteFileSpec, 0);
                Iterator deleteIterator = deleteFilelist.iterator();
                while (deleteIterator.hasNext()) {
                    File tempFile = (File) deleteIterator.next();
                    long interval = System.currentTimeMillis() - tempFile.lastModified();
                    if (tempFile.canWrite() && interval > (deleteFileAge * 24 * 3600 * 1000)) {
                        try {
                            tempFile.delete();
                            spooler_log.debug1(".. log file [" + tempFile.getName() + "] deleted.");
                            deleteCounter++;
                        } catch (Exception e) {
                            throw new Exception("an error occurred compressing log file [" + tempFile.getPath() + "] to gzip file: " + e.getMessage(), e);
                        }
                    }
                }
            }
            Vector filelist = SOSFile.getFilelist(spooler.log_dir(), fileSpec, 0);
            Iterator iterator = filelist.iterator();
            while (iterator.hasNext()) {
                File tempFile = (File) iterator.next();
                long interval = System.currentTimeMillis() - tempFile.lastModified();
                if (tempFile.canWrite() && interval > (fileAge * 24 * 3600 * 1000)) {
                    try {
                        counter++;
                        String gzipFilename = filePath + tempFile.getName().concat(".gz");
                        File gzipFile = new File(gzipFilename);
                        SOSGZip.compressFile(tempFile, gzipFile);
                        gzipFile.setLastModified(tempFile.lastModified());
                        tempFile.delete();
                        spooler_log.debug1(".. log file [" + tempFile.getName() + "] compressed to: " + gzipFilename);
                    } catch (Exception e) {
                        throw new Exception("an error occurred compressing log file [" + tempFile.getPath() + "] to gzip file: " + e.getMessage(), e);
                    }
                }
            }
            if (counter > 0) {
                spooler_log.info(counter + " log files compressed");
            }
            if (deleteCounter > 0) {
                spooler_log.info(deleteCounter + " log files deleted");
            }
        } catch (Exception e) {
            spooler_log.warn("an error occurred cleaning up log files: " + e.getMessage());
        }
        try {
            spooler.log().start_new_file();
        } catch (Exception e) {
            spooler_log.warn("an error occurred rotating log file: " + e.getMessage());
            return false;
        }
        return false;
    }

}