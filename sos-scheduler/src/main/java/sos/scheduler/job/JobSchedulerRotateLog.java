package sos.scheduler.job;
import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;

/**
 * compress and rotate scheduler log files
 * @author andreas.pueschel@sos-berlin.com 
 */
public class JobSchedulerRotateLog extends JobSchedulerJob {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());

	@Override public boolean spooler_process() {
		try {
			super.spooler_process();
		}
		catch (Exception e1) {
			throw new JobSchedulerException(e1);
		}
		/** give a path for files to remove */
		String filePath = spooler.log_dir();
		/** give the number of days, defaults to 14 days */
		long lngCompressFileAge = 14;
		/** number of days, for file age of files that should be deleted **/
		long deleteFileAge = 0;
		/** give a regular expression as file specification */
		String fileSpec = "^(scheduler)([0-9\\-]+)";
		String strSchedulerID = spooler.id();
		if (strSchedulerID != null) {
			fileSpec += "(\\." + strSchedulerID + ")";
		}
		fileSpec += "(\\.log)$";
		String deleteFileSpec = "^(scheduler)([0-9\\-]+)";
		if (strSchedulerID != null) {
			deleteFileSpec += "(\\." + strSchedulerID + ")";
		}
		deleteFileSpec += "(\\.log)(\\.gz)?$";
		objParams = spooler_task.params();
		//
		filePath = getParm("file_path", filePath);
		fileSpec = getParm("file_specification", fileSpec);
		lngCompressFileAge = getLongParm("file_age", lngCompressFileAge);
		deleteFileAge = getLongParm("delete_file_age", deleteFileAge);
		fileSpec = getParm("delete_file_specification", deleteFileSpec);
		int intNoOfLogFilesDeleted = 0;
		int intNoOfLogFilesCompressed = 0;
		//
		try {
			if (!filePath.endsWith("/")) {
				filePath += "/";
			}
			JSFile fleSchedulerLog = new JSFile(filePath + "scheduler.log");
			String strNewLogFileName = filePath + "scheduler.log-" + fleSchedulerLog.getTimeStamp();
			if (strSchedulerID != null) {
				strNewLogFileName += "." + strSchedulerID;
			}
			strNewLogFileName += ".log";
			fleSchedulerLog.copy(strNewLogFileName);
			JSFile objN = new JSFile(strNewLogFileName);
			objN.createZipFile("");
			//
			long lngMillisPerDay = 24 * 3600 * 1000;
			if (deleteFileAge > 0) {
				long lngInterval = (deleteFileAge * lngMillisPerDay);
				for (JSFile tempFile : JSFile.getFilelist(filePath, deleteFileSpec, 0)) {
					if (tempFile.isOlderThan(lngInterval)) {
						tempFile.delete();
						intNoOfLogFilesDeleted++;
					}
					else {
						spooler_log.debug(String.format("File '%1$s' not deleted due to age", tempFile.getAbsolutePath()));
					}
				}
				String deleteSchedulerLogFileSpec = "^(scheduler-log\\.)([0-9\\-]+)";
				if (strSchedulerID != null) {
					deleteSchedulerLogFileSpec += "(\\." + strSchedulerID + ")";
				}
				deleteSchedulerLogFileSpec += "(\\.log)(\\.gz)?$";
				for (JSFile fleT : JSFile.getFilelist(filePath, deleteSchedulerLogFileSpec, 0)) {
					if (fleT.isOlderThan(lngInterval)) {
						fleT.delete();
						intNoOfLogFilesDeleted++;
					}
					else {
						spooler_log.debug(String.format("File '%1$s' not deleted due to age", fleT.getAbsolutePath()));
					}
				}
			}
			if (lngCompressFileAge > 0) {
				long lngInterval = (lngCompressFileAge * lngMillisPerDay);
				for (JSFile fleT : JSFile.getFilelist(spooler.log_dir(), fileSpec, 0)) {
					if (fleT.isOlderThan(lngInterval)) {
						intNoOfLogFilesCompressed++;
						fleT.createZipFile(filePath);
						fleT.delete();
					}
					else {
						spooler_log.debug(String.format("File '%1$s' not compressed due to age", fleT.getAbsolutePath()));
					}
				}
			}
		}
		catch (Exception e) {
			spooler_log.warn("an error occurred cleaning up log files: " + e.getMessage());
			throw new JobSchedulerException(e);
		}
		finally {
			spooler_log.info(intNoOfLogFilesCompressed + " log files compressed");
			spooler_log.info(intNoOfLogFilesDeleted + " compressed log files deleted");
		}
		try {
			spooler.log().start_new_file();
		}
		catch (Exception e) {
			spooler_log.warn("an error occurred rotating log file: " + e.getMessage());
			return false;
		}
		return false;
	}
}
