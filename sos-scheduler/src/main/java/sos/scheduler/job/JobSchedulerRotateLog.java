package sos.scheduler.job;
import static sos.scheduler.job.JobSchedulerConstants.JobSchedulerLogFileName;
import static sos.scheduler.job.JobSchedulerConstants.JobSchedulerLogFileNameExtension;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSFolder;

/**
 * compress and rotate scheduler log files
 * @author andreas.pueschel@sos-berlin.com 
 */
public class JobSchedulerRotateLog extends JobSchedulerJob {
	@SuppressWarnings("unused") private final String	conClassName	= this.getClass().getSimpleName();
	private final String								conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger	logger			= Logger.getLogger(this.getClass());
	private String										strSchedulerID;

	@Override public boolean spooler_process() {
		try {
			super.spooler_process();
			spooler_log.info(conSVNVersion);
		}
		catch (Exception e1) {
			throw new JobSchedulerException(e1);
		}
		/** give a path for files to remove */
		String filePath = spooler.log_dir();
		strSchedulerID = spooler.id();
		objParams = spooler_task.params();
		/** give the number of days, defaults to 14 days */
		long lngCompressFileAge = 14;
		/** number of days, for file age of files that should be deleted **/
		long deleteFileAge = 0;
		/** give a regular expression as file specification */
		String strRegExpr4LogFiles2Compress = "^(scheduler)([0-9\\-]+)" + getRegExp4SchedulerID() + "(\\.log)$";
		String strRegExpr4CompressedFiles2Delete = "^(scheduler)([0-9\\-]+)" + getRegExp4SchedulerID() + "(\\.log)(\\.gz)?$";
		//
		filePath = getParm("file_path", filePath);
		strRegExpr4LogFiles2Compress = getParm("file_specification", strRegExpr4LogFiles2Compress);
		lngCompressFileAge = getLongParm("file_age", lngCompressFileAge);
		deleteFileAge = getLongParm("delete_file_age", deleteFileAge);
		strRegExpr4CompressedFiles2Delete = getParm("delete_file_specification", strRegExpr4CompressedFiles2Delete);
		int intNoOfLogFilesDeleted = 0;
		int intNoOfLogFilesCompressed = 0;
		//
		try {
			JSFolder objLogDirectory = new JSFolder(filePath);
			JSFile fleSchedulerLog = objLogDirectory.newFile(JobSchedulerLogFileName);
			String strNewLogFileName = JobSchedulerLogFileName + "-" + fleSchedulerLog.getTimeStamp() + getRegExp4SchedulerID()
					+ JobSchedulerLogFileNameExtension;
			JSFile objN = objLogDirectory.newFile(strNewLogFileName);
			fleSchedulerLog.copy(objN);
			objN.createZipFile("");
			//
			long lngMillisPerDay = 24 * 3600 * 1000;
			if (deleteFileAge > 0) {
				objLogDirectory.IncludeOlderThan = (deleteFileAge * lngMillisPerDay);
				for (JSFile tempFile : objLogDirectory.getFilelist(strRegExpr4CompressedFiles2Delete, 0)) {
					tempFile.delete();
					intNoOfLogFilesDeleted++;
				}
				String deleteSchedulerLogFileSpec = "^(scheduler-log\\.)([0-9\\-]+)" + getRegExp4SchedulerID() + "(\\.log)(\\.gz)?$";
				for (JSFile fleT : objLogDirectory.getFilelist(deleteSchedulerLogFileSpec, 0)) {
					fleT.delete();
					intNoOfLogFilesDeleted++;
				}
			}
			if (lngCompressFileAge > 0) {
				objLogDirectory.IncludeOlderThan = (lngCompressFileAge * lngMillisPerDay);
				for (JSFile fleT : objLogDirectory.getFilelist(strRegExpr4LogFiles2Compress, 0)) {
					intNoOfLogFilesCompressed++;
					fleT.createZipFile(filePath);
					fleT.delete();
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
			spooler.log().start_new_file(); // this will start with a fresh log file
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			spooler_log.warn("an error occurred rotating log file: " + e.getMessage());
			return false;
		}
		return false;
	}

	private String getRegExp4SchedulerID() {
		String strR = "";
		if (strSchedulerID != null) {
			strR += "(\\." + strSchedulerID + ")";
		}
		return strR;
	}
}
