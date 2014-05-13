package sos.scheduler.file;

import java.io.File;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		JSFolderSync - Workerclass for "syncronize two folders"
 *
 * \brief AdapterClass of JSExistFile for the SOSJobScheduler
 *
 * This Class JSFolderSync is the worker-class.
 *

 *
 * see \see C:\Users\KB\Documents\xmltest\JSExistFile.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20110820120954 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JSFolderSync extends JSFileOperationBase {
	@SuppressWarnings("hiding")
	private final String	conClassName	= "JSFolderSync";
	@SuppressWarnings("hiding")
	private static Logger	logger			= Logger.getLogger(JSFolderSync.class);
	private final String	conSVNVersion	= "$Id$";

	private String JFO_I_0010 = "JFO_I_0010"; // "File '%1$s' on target found, isNewer = %1$s";
	private String JFO_I_0011 = "JFO_I_0011"; // "File '%1$s' is not on Target, will be copied ";
	private String JFO_I_0012 = "JFO_I_0012"; // "File '%1$s' is newer on Source";
	private String JFO_I_0013 = "JFO_I_0013"; // "%1$d files to copy";

	/**
	 * 
	 * \brief JSFolderSync
	 *
	 * \details
	 *
	 */
	public JSFolderSync() {
		super();
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of JSFolderSync
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see JSFolderSyncMain
	 * 
	 * \return JSFolderSync
	 *
	 * @return
	 */
	public boolean Execute() /* throws Exception */ {
		final String conMethodName = conClassName + "::Execute";

		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));

		try {
			logger.info(conSVNVersion);
			initialize();
			Options().file.CheckMandatory();
			Options().target.CheckMandatory();
			Options().file_spec.setRegExpFlags(Pattern.CASE_INSENSITIVE);

			flgOperationWasSuccessful = existsFile(Options().file, Options().file_spec, //
					Options().min_file_age, Options().max_file_age, Options().min_file_size, Options().max_file_size, //
					Options().skip_first_files, //
					Options().skip_last_files, -1, -1);

			Vector<File> vecSourceList = new Vector<File>(); 
			vecSourceList.addAll(lstResultList);
			lstResultList = new Vector<File>();
			
			flgOperationWasSuccessful = existsFile(Options().target, Options().file_spec, //
					Options().min_file_age, Options().max_file_age, Options().min_file_size, Options().max_file_size, //
					Options().skip_first_files, //
					Options().skip_last_files, -1, -1);

			Vector<File> vecTargetList = new Vector<File>();
			vecTargetList.addAll(lstResultList);
			Vector<File> vecSyncList = new Vector<File>(); 

			for (File objSourceFile : vecSourceList) {
				String strSourceFileName = objSourceFile.getName();
				long lngLastModifiedSource = objSourceFile.lastModified();
				boolean flgFileIsOnTarget = false;
				boolean flgIsNewerOnSource = false;
				
				for (File objTargetFile : vecTargetList) {
					if (strSourceFileName.equalsIgnoreCase(objTargetFile.getName())) {
						flgFileIsOnTarget = true;
						if (lngLastModifiedSource > objTargetFile.lastModified()) {
							flgIsNewerOnSource = true;
						}
						logger.info(Messages.getMsg(JFO_I_0010, objSourceFile.getAbsoluteFile(), flgIsNewerOnSource));
						break;
					}
				}
				
				if (flgFileIsOnTarget == false) {
					logger.debug(Messages.getMsg(JFO_I_0011, objSourceFile.getName()));
					vecSyncList.add(objSourceFile);
				}
				if (flgIsNewerOnSource == true) {
					logger.debug(Messages.getMsg(JFO_I_0012, objSourceFile.getName()));
					vecSyncList.add(objSourceFile);
				}
			}
			
			logger.info(Messages.getMsg(JFO_I_0013, vecSyncList.size()));
			
			for (File objFile2Copy : vecSyncList) {
				String strFileName = objFile2Copy.getAbsolutePath();
				JSFile objF = new JSFile(strFileName);
				String strTargetFileName = Options().target.Value() + objFile2Copy.getName();
				
//				objF.copy(strTargetFileName);
				JSFile objTarget = new JSFile(strTargetFileName);
				objTarget.setLastModified(objFile2Copy.lastModified());
			}
			
			if (vecSyncList.size() > 0) {
				flgOperationWasSuccessful = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			String strM = Messages.getMsg("JSJ-F-107", conMethodName); 
			logger.fatal(strM);
			throw new JobSchedulerException(strM);
		}
		finally {
		}

		logger.debug(Messages.getMsg("JSJ-I-111", conMethodName));
		return flgOperationWasSuccessful;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

} // class JSFolderSync