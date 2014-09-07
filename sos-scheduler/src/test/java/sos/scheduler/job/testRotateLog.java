/**
 * 
 */
package sos.scheduler.job;

import static sos.scheduler.job.JobSchedulerConstants.JobSchedulerLogFileName;
import static sos.scheduler.job.JobSchedulerConstants.JobSchedulerLogFileNameExtension;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSFolder;

/**
 * @author KB
 *
 */
public class testRotateLog {

	/**
	 * 
	 */
	public testRotateLog() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testRotateLog objL = new testRotateLog();
		objL.test_process();
	}

	/**
	 * compress and rotate scheduler log files
	 * @author andreas.pueschel@sos-berlin.com 
	 */
	@SuppressWarnings("unused")
	private final String	conClassName	= this.getClass().getSimpleName();
	private final String	conSVNVersion	= "$Id: JobSchedulerRotateLog.java 26910 2014-08-25 08:51:57Z kb $";
	@SuppressWarnings("unused")
	private final Logger	logger			= Logger.getLogger(this.getClass());
	private String			strSchedulerID;

	public boolean test_process() {
		try {
			logger.info(conSVNVersion);
		}
		catch (Exception e1) {
			throw new JobSchedulerException(e1);
		}
		/** give a path for files to remove */
//		String filePath = spooler.log_dir();
		String filePath = "C:\\ProgramData\\sos-berlin.com\\jobscheduler\\kb-xps-laptop_4445\\logs\\";  // getParm("file_path", filePath);
		strSchedulerID = "KB-XPS-Laptop_4445";   //spooler.id();
//		objParams = spooler_task.params();
		/** give the number of days, defaults to 14 days */
		long lngCompressFileAge = 14;
		/** number of days, for file age of files that should be deleted **/
		long deleteFileAge = 0;
		String strCaseInsensitive = "(?i)";
		/** give a regular expression as file specification */
		String strRegExpr4LogFiles2Compress = strCaseInsensitive + "^(scheduler)([0-9\\-]+)" + getRegExp4SchedulerID() + "(\\.log)$";
		String strRegExpr4CompressedFiles2Delete = strCaseInsensitive + "^(scheduler)([0-9\\-]+)" + getRegExp4SchedulerID() + "(\\.log)(\\.gz)?$";
		//
//		strRegExpr4LogFiles2Compress = getParm("file_specification", strRegExpr4LogFiles2Compress);
		lngCompressFileAge = 5; //getLongParm("file_age", lngCompressFileAge);
		deleteFileAge = 5; // getLongParm("delete_file_age", deleteFileAge);
//		strRegExpr4CompressedFiles2Delete = getParm("delete_file_specification", strRegExpr4CompressedFiles2Delete);
		int intNoOfLogFilesDeleted = 0;
		int intNoOfLogFilesCompressed = 0;
		String deleteSchedulerLogFileSpec = "";
		//
		try {
			JSFolder objLogDirectory = new JSFolder(filePath);
			filePath = objLogDirectory.getFolderName();
			JSFile fleSchedulerLog = objLogDirectory.newFile(JobSchedulerLogFileName);
			String strNewLogFileName = JobSchedulerLogFileName + "-" + fleSchedulerLog.getTimeStamp() + "-" + strSchedulerID + JobSchedulerLogFileNameExtension;
			JSFile objNewLogFileName = objLogDirectory.newFile(strNewLogFileName);
			fleSchedulerLog.copy(objNewLogFileName);
			objNewLogFileName.createZipFile(filePath);
			objNewLogFileName.delete();
			//
			long lngMillisPerDay = 24 * 3600 * 1000;
			if (deleteFileAge > 0) {
				objLogDirectory.IncludeOlderThan = (deleteFileAge * lngMillisPerDay);
				for (JSFile tempFile : objLogDirectory.getFilelist(strRegExpr4CompressedFiles2Delete, 0)) {
					tempFile.delete();
					intNoOfLogFilesDeleted++;
				}
				deleteSchedulerLogFileSpec = strCaseInsensitive + "^(" + JobSchedulerLogFileName + "\\.)([0-9\\-]+)" + getRegExp4SchedulerID() + "(\\"
						+ JobSchedulerLogFileNameExtension + ")(\\.gz)?$";
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
			String strT = "an error occurred cleaning up log files: " + e.getMessage();
			logger.warn(strT);
			throw new JobSchedulerException(strT, e);
		}
		finally {
			logger.info(intNoOfLogFilesCompressed + " log files compressed for regexp: " + strRegExpr4LogFiles2Compress);
			logger.info(intNoOfLogFilesDeleted + " compressed log files deleted for regexp: " + deleteSchedulerLogFileSpec);
		}
		try {
			//				spooler.log().start_new_file(); // this will start with a fresh log file
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.warn("an error occurred rotating log file: " + e.getMessage());
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
