package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_D_0044;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0017;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0020;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0040;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0041;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0042;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0110;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0120;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0130;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0015;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0016;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0080;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0090;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0017;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0018;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0019;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0040;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_0090;
import static com.sos.scheduler.messages.JSMessages.JSJ_T_0010;
import static com.sos.scheduler.messages.JSMessages.JSJ_W_0043;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSSchedulerLogger;

import com.sos.JSHelper.Basics.JSVersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFileAge;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.io.SOSFileSystemOperations;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerFileOperationBase extends JobSchedulerJobAdapter {

	private static final String			conOrderParameterSCHEDULER_FILE_PATH							= "scheduler_file_path";
	private static final String			conOrderParameterSCHEDULER_FILE_PARENT							= "scheduler_file_parent";
	private static final String			conOrderParameterSCHEDULER_FILE_NAME							= "scheduler_file_name";
	public static final String			conOrderParameterSCHEDULER_SOS_FILE_OPERATIONS_RESULT_SET		= "scheduler_SOSFileOperations_ResultSet";
	public static final String			conOrderParameterSCHEDULER_SOS_FILE_OPERATIONS_RESULT_SET_SIZE	= "scheduler_SOSFileOperations_ResultSetSize";
	public static final String			conOrderParameterSCHEDULER_SOS_FILE_OPERATIONS_FILE_COUNT		= "scheduler_SOSFileOperations_file_count";

	protected static final String		conPropertyJAVA_IO_TMPDIR										= "java.io.tmpdir";

	/** give a path for files to remove */
	protected String					filePath														= System.getProperty(conPropertyJAVA_IO_TMPDIR);

	/** give the number of milliseconds, defaults to 24 hours */
	protected long						lngFileAge														= 86400000;

	/** number of files which causes a warning */
	protected int						warningFileLimit												= 0;

	private static final String			conParameterCREATE_ORDERS_FOR_ALL_FILES							= "create_orders_for_all_files";
	private static final String			conParameterNEXT_STATE											= "next_state";
	private static final String			conParameterORDER_JOBCHAIN_NAME									= "order_jobchain_name";
	private static final String			conParameterCREATE_ORDER										= "create_order";
	private static final String			conParameterMERGE_ORDER_PARAMETER								= "merge_order_parameter";

	protected static final String		conParameterCHECK_STEADYSTATEOFFILE								= "check_steady_state_of_files";
	protected static final String		conParameterCHECK_STEADYSTATEINTERVAL							= "check_steady_state_interval";
	protected static final String		conParameterSTEADYSTATECOUNT									= "steady_state_count";

	protected static final String		conParameterWARNING_FILE_LIMIT									= "warning_file_limit";
	protected static final String		conParameterFILE_AGE											= "file_age";
	protected static final String		conParameterFILE_SPEC											= "file_spec";
	protected static final String		conParameterFILE_REGEX											= "file_regex";
	protected static final String		conParameterFILE_SPECIFICATION									= "file_specification";
	protected static final String		conParameterFILE_PATH											= "file_path";

	protected static final String		conParameterMAX_FILE_SIZE										= "max_file_size";
	protected static final String		conParameterMIN_FILE_SIZE										= "min_file_size";
	protected static final String		conParameterMAX_FILE_AGE										= "max_file_age";
	protected static final String		conParameterMIN_FILE_AGE										= "min_file_age";
	protected static final String		conParameterTARGET_FILE											= "target_file";
	protected static final String		conParameterFILE												= "file";
	protected static final String		conParameterSOURCE_FILE											= "source_file";
	protected static final String		conParameterON_EMPTY_RESULT_SET									= "on_empty_result_set";
	protected static final String		conParameterSKIP_LAST_FILES										= "skip_last_files";
	protected static final String		conParameterSKIP_FIRST_FILES									= "skip_first_files";
	protected static final String		conParameterOVERWRITE											= "overwrite";
	protected static final String		conParameterCOUNT_FILES											= "count_files";
	protected static final String		conALL															= "all";
	protected static final String		conTRUE															= "true";
	protected static final String		conParameterREPLACEMENT											= "replacement";
	protected static final String		conParameterREPLACING											= "replacing";
	protected static final String		conParameterRAISE_ERROR_IF_RESULT_SET_IS						= "Raise_Error_If_Result_Set_Is";
	protected static final String		conParameterEXPECTED_SIZE_OF_RESULT_SET							= "Expected_Size_Of_Result_Set";
	protected SOSSchedulerLogger		objSOSLogger													= null;
	@SuppressWarnings("hiding")
	protected final Logger					logger															= Logger.getLogger(JobSchedulerFileOperationBase.class);
	protected static final String		conParameterRESULT_LIST_FILE									= "Result_List_File";
	protected static final String		conParameterRECURSIVE											= "recursive";
	protected static final String		conParameterCREATE_DIR											= "create_dir";
	protected static final String		conParameterCREATE_FILE											= "create_file";
	protected static final String		conParameterCREATE_FILES										= "create_files";
	protected static final String		conClassName													= "JobSchedulerFileOperationBase";
	protected static final String		conValueYES														= "yes";
	public static final String			conParameterGRACIOUS											= "gracious";
	@SuppressWarnings("unused")
	private final String				conSVNVersion													= "$Id$";
	// protected SOSLogger logger = null;
	protected boolean					flgOperationWasSuccessful										= false;
	protected String					name															= null;
	protected String					file															= null;
	private final String				strFileSpecDefault												= ".*";
	protected String					fileSpec														= strFileSpecDefault;
	protected String					minFileAge														= "0";
	protected String					maxFileAge														= "0";
	private final String				conFileSizeDefault												= "-1";
	protected String					minFileSize														= conFileSizeDefault;
	protected String					maxFileSize														= conFileSizeDefault;
	protected int						skipFirstFiles													= 0;
	protected int						skipLastFiles													= 0;
	// Variable_set params = null;
	HashMap<String, String>				params															= null;
	protected String					strGracious														= "false";
	String								source															= null;
	String								target															= null;
	int									flags															= 0;
	String								replacing														= null;
	String								replacement														= null;
	boolean								count_files														= false;
	protected final int					isCaseInsensitive												= Pattern.CASE_INSENSITIVE;
	public int							intNoOfHitsInResultSet											= 0;
	protected String					strOnEmptyResultSet												= null;
	protected String					strResultList2File												= null;
	protected int						intExpectedSizeOfResultSet										= 0;
	// possible values equal, lt, le, eq, ge, gt, ne
	protected String					strRaiseErrorIfResultSetIs										= null;
	protected Vector<File>				lstResultList													= new Vector<File>();															// new
	// Vector<File>();
	protected boolean					flgCreateOrder													= false;
	protected boolean					flgMergeOrderParameter											= false;
	protected boolean					flgCreateOrders4AllFiles										= false;
	protected String					strOrderJobChainName											= null;
	protected String					strNextState													= null;

	protected boolean					flgCheckSteadyStateOfFiles										= false;
	protected long						lngSteadyCount													= 30;
	protected long						lngCheckSteadyStateInterval										= 1000;																		// millis
	protected boolean					flgUseNIOLock													= false;

	private SOSOptionFileAge			objOptionFileAge												= null;
	private SOSOptionTime				objOptionTime													= null;

	protected SOSFileSystemOperations	SOSFileOperations												= new SOSFileSystemOperations();

	public JobSchedulerFileOperationBase() {
		super();
	}

	/**
	*
	* \brief spooler_init
	*
	* \details
	*
	* \return
	*
	* @return
	*/

	@Override
	public boolean spooler_init() {
		final String conMethodName = conClassName + "::spooler_init";
		boolean flgReturn = super.spooler_init();
		try {
			try {
				objSOSLogger = new SOSSchedulerLogger(spooler_log);
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				throw new JobSchedulerException(JSJ_F_0015.params("SOSSchedulerLogger ", e.getMessage()));
			}
			return flgReturn;
		}
		catch (Exception e) {
			try {
				if (isNotNull(objSOSLogger)) {
					objSOSLogger.error(JSJ_F_0016.params(conMethodName, e.getMessage()));
				}
			}
			catch (Exception x) {
			}
			return false;
		}
		// return flgReturn;
	}

	private void ResetVariables() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::ResetVariables";

		flgOperationWasSuccessful = false;
		name = null;
		file = null;
		fileSpec = strFileSpecDefault;
		minFileAge = "0";
		maxFileAge = "0";
		minFileSize = conFileSizeDefault;
		maxFileSize = conFileSizeDefault;
		skipFirstFiles = 0;
		skipLastFiles = 0;
		strGracious = "false";
		source = null;
		target = null;
		flags = 0;
		replacing = null;
		replacement = EMPTY_STRING;
		count_files = false;
		intNoOfHitsInResultSet = 0;
		strOnEmptyResultSet = null;
		strResultList2File = null;
		intExpectedSizeOfResultSet = 0;
		// possible values equal, lt, le, eq, ge, gt, ne
		strRaiseErrorIfResultSetIs = null;
		lstResultList = null; // new Vector<File>();
		flgCreateOrder = false;
		flgMergeOrderParameter = false;
		flgCreateOrders4AllFiles = false;
		strOrderJobChainName = null;
		strNextState = null;

		objOptionFileAge = new SOSOptionFileAge(null, "file_age", "file_age", "0", "0", false);
		objOptionTime = new SOSOptionTime(null, conParameterCHECK_STEADYSTATEINTERVAL, conParameterCHECK_STEADYSTATEINTERVAL, "1", "1", false);

		flgCheckSteadyStateOfFiles = false;
		lngSteadyCount = 30;
		lngCheckSteadyStateInterval = 1000;

	} // private void ResetVariables

	protected boolean getParamBoolean(final String pstrParamName, final boolean pflgDefaultValue) {
		String strReturnValue = getParamValue(pstrParamName);
		boolean flgReturnValue = pflgDefaultValue;
		if (isNotNull(strReturnValue)) {
			try {
				flgReturnValue = SOSFileOperations.toBoolean(strReturnValue);
			}
			catch (Exception e) {
			}
		}
		return flgReturnValue;
	}

	protected long getParamLong(final String pstrParamName, final long plngDefaultValue) {
		long lngReturnValue = getParamInteger(pstrParamName);
		if (lngReturnValue == 0) {
			lngReturnValue = plngDefaultValue;
		}
		return lngReturnValue;
	}

	protected int getParamInteger(final String pstrParamName, final int pintDefaultValue) {
		int intReturnValue = getParamInteger(pstrParamName);
		if (intReturnValue == 0) {
			intReturnValue = pintDefaultValue;
		}
		return intReturnValue;
	}

	protected int getParamInteger(final String pstrParamName) {
		String strT = getParamValue(pstrParamName);
		int intRetVal = 0;
		try {
			if (isNotEmpty(strT)) {
				intRetVal = Integer.parseInt(strT);
			}
		}
		catch (Exception ex) {
			throw new JobSchedulerException(JSJ_E_0130.get(pstrParamName, ex.getMessage()), ex);
		}
		return intRetVal;
	}

	/**
	 *
	 * \brief getParamValue - with (alias) (multiple) Parameters
	 *
	 * \details
	 * the first notEmpty value-string is taken as the value of the parameter and will end the lookup
	 *
	 * \return String
	 *
	 * @param pstrKeys
	 * @return
	 */
	protected String getParamValue(final String[] pstrKeys, final String pstrDefaultValue) {
		String strT = pstrDefaultValue;
		for (String strKey : pstrKeys) {
			String strK = getParamValue(strKey);
			if (isNotEmpty(strK)) {
				strT = strK;
				break;
			}
		}
		return strT;
	}

	protected String getParamValue(final String pstrParamName, final String pstrDefaultValue) {
		String strT = getParamValue(pstrParamName);
		if (isNull(strT)) {
			strT = pstrDefaultValue;
		}
		return strT;
	}

	protected String getParamValue(final String pstrParamName) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getParamValue";
		/**
		 * space or empty is a valid value of a parameter
		 */
		name = pstrParamName;
		String strT = params.get(pstrParamName);
		if (isNull(strT)) { // not found, try without "underscore"
			strT = params.get(pstrParamName.replaceAll("_", EMPTY_STRING).toLowerCase());
		}
		if (isNotNull(strT)) { // && params.value(pstrParamName).length() > 0) {
			strT = strT.trim();
			if (strT.length() > 0) {
				logger.info(JSJ_I_0040.params(pstrParamName, strT));
			}
		}
		return strT;
	} // private String getParamValue

	public boolean initialize(final String pstrVersionInfo) {
		try {
			super.spooler_process();
			logger.info(JSVersionInfo.getVersionString());
			logger.info(pstrVersionInfo);
			params = null;
			params = getSchedulerParameterAsProperties(super.getJobOrOrderParameters());
			getParametersFromHashMap();
			SOSFileOperations = new SOSFileSystemOperations();
		}
		catch (Exception e) {
			throw new JobSchedulerException(JSJ_E_0042.get(e), e);
		}
		return true;
	}

	protected void getParametersFromHashMap() throws Exception {

		ResetVariables();

		flgCreateOrders4AllFiles = getParamBoolean(conParameterCREATE_ORDERS_FOR_ALL_FILES, false);
		flgCreateOrder = getParamBoolean(conParameterCREATE_ORDER, false) | flgCreateOrders4AllFiles;

		flgMergeOrderParameter = getParamBoolean(conParameterMERGE_ORDER_PARAMETER, false);
		if (flgCreateOrder == true) {
			strOrderJobChainName = getParamValue(conParameterORDER_JOBCHAIN_NAME);
			if (isNull(strOrderJobChainName)) {
				throw new JobSchedulerException(JSJ_E_0020.params(conParameterORDER_JOBCHAIN_NAME));
			}
			if (spooler.job_chain_exists(strOrderJobChainName) == false) {
				throw new JobSchedulerException(JSJ_E_0041.params(strOrderJobChainName));
			}
			strNextState = getParamValue(conParameterNEXT_STATE);
		}
		else {
			strOrderJobChainName = null;
			strNextState = null;
			flgCreateOrders4AllFiles = false;
			flgMergeOrderParameter = false;
		}

		lngFileAge = calculateFileAge(getParamValue(conParameterFILE_AGE, "0"));
		warningFileLimit = getParamInteger(conParameterWARNING_FILE_LIMIT, 0);

		intExpectedSizeOfResultSet = getParamInteger(conParameterEXPECTED_SIZE_OF_RESULT_SET, 0);
		strRaiseErrorIfResultSetIs = getParamValue(conParameterRAISE_ERROR_IF_RESULT_SET_IS, EMPTY_STRING);
		strResultList2File = getParamValue(conParameterRESULT_LIST_FILE, EMPTY_STRING);
		strOnEmptyResultSet = getParamValue(conParameterON_EMPTY_RESULT_SET, EMPTY_STRING);
		source = file = filePath = getParamValue(new String[] { conParameterSOURCE_FILE, conParameterFILE, conParameterFILE_PATH }, EMPTY_STRING);
		fileSpec = getParamValue(new String[] { conParameterFILE_SPEC, conParameterFILE_SPECIFICATION }, strFileSpecDefault);
		if (isNotEmpty(fileSpec) || isNotEmpty(source)) {

		}
		else { // can be started by a file order source. In this case the name of the file comes as the value of SCHEDULER_WITH_PATH
			source = file = filePath = getParamValue(new String[] { conOrderParameterSCHEDULER_FILE_PATH }, EMPTY_STRING);
		}
		target = getParamValue(conParameterTARGET_FILE, null);

		minFileAge = getParamValue(conParameterMIN_FILE_AGE, "0");
		objOptionFileAge.Value(minFileAge);
		minFileAge = String.valueOf(objOptionFileAge.getAgeAsSeconds());

		maxFileAge = getParamValue(conParameterMAX_FILE_AGE, "0");
		objOptionFileAge.Value(maxFileAge);
		maxFileAge = String.valueOf(objOptionFileAge.getAgeAsSeconds());

		minFileSize = getParamValue(conParameterMIN_FILE_SIZE, conFileSizeDefault);
		maxFileSize = getParamValue(conParameterMAX_FILE_SIZE, conFileSizeDefault);

		flgUseNIOLock = getParamBoolean("use_nio_lock", false);

		strGracious = getParamValue(conParameterGRACIOUS, "false");
		skipFirstFiles = getParamInteger(conParameterSKIP_FIRST_FILES, 0);
		skipLastFiles = getParamInteger(conParameterSKIP_LAST_FILES, 0);

		flags = 0;
		String strCreateWhat = getParamValue(new String[] { conParameterCREATE_DIR, conParameterCREATE_FILE, conParameterCREATE_FILES }, "false");

		if (SOSFileOperations.toBoolean(strCreateWhat) == true) {
			flags |= SOSFileSystemOperations.CREATE_DIR;
		}
		if (getParamBoolean(conParameterGRACIOUS, false) == true) {
			flags |= SOSFileSystemOperations.GRACIOUS;
		}
		if (getParamBoolean(conParameterOVERWRITE, true) == false) {
			flags |= SOSFileSystemOperations.NOT_OVERWRITE;
		}
		if (getParamBoolean(conParameterRECURSIVE, false) == true) {
			flags |= SOSFileSystemOperations.RECURSIVE;
		}
		count_files = getParamBoolean(conParameterCOUNT_FILES, false);
		if (count_files == true && isJobchain() == false) {
			JSJ_E_0120.toLog(conParameterCOUNT_FILES);
		}
		replacing = getParamValue(conParameterREPLACING, EMPTY_STRING);
		replacement = getParamValue(conParameterREPLACEMENT, EMPTY_STRING);
		String strM = JSJ_E_0110.get(conParameterREPLACEMENT, conParameterREPLACING);
		if (isNotNull(replacing) && isNull(replacement)) {
			replacement = EMPTY_STRING;
			// throw new JobSchedulerException(strM);
		}
		if (isNull(replacing) && isNotNull(replacement)) {
			throw new JobSchedulerException(strM);
		}

		flgCheckSteadyStateOfFiles = getParamBoolean(conParameterCHECK_STEADYSTATEOFFILE, false);
		lngSteadyCount = getParamLong(conParameterSTEADYSTATECOUNT, 30);
		objOptionTime.Value(getParamValue(conParameterCHECK_STEADYSTATEINTERVAL, "1"));
		lngCheckSteadyStateInterval = objOptionTime.getTimeAsSeconds() * 1000;
	}

	@Override
	public void spooler_exit() {
		try {
			super.spooler_exit();
		}
		catch (Exception e) {
			// no error processing at job level
		}
		finally {
		}
	}

	public boolean isGraciousAll() {
		return strGracious != null && strGracious.equalsIgnoreCase(conALL);
	}

	public boolean isGraciousTrue() {
		boolean flgResult = false;
		try {
			if (isNotEmpty(strGracious)) {
				flgResult = SOSFileOperations.toBoolean(strGracious);
			}
		}
		catch (Exception e) {
			logger.error(StackTrace2String(e));
		}
		return flgResult;
	}

	/**
	 *
	 * \brief saveResultList
	 *
	 * \details
	 *
	 * \return void
	 *
	 */
	public void saveResultList() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::saveResultList";

		if (isNull(lstResultList)) {
			lstResultList = new Vector<File>();
		}

		Vector<File> lstR = SOSFileOperations.lstResultList;
		if (isNotNull(lstR) && lstR.size() > 0) {
			lstResultList.addAll(lstR);
		}

	} // private void saveResultList

	public boolean createResultListParam(final boolean pflgOperationWasSuccessful) {
		String strFirstFile = EMPTY_STRING;
		String strResultSetFileList = EMPTY_STRING;
		if (isNull(lstResultList)) {
			saveResultList();
		}
		intNoOfHitsInResultSet = 0;

		if (isNotNull(lstResultList) && lstResultList.size() > 0) {
			intNoOfHitsInResultSet = lstResultList.size();
			strFirstFile = lstResultList.get(0).getAbsolutePath();
			for (File objFile : lstResultList) {
				strResultSetFileList += objFile.getAbsolutePath() + ";";
			}
		}

		if (isJobchain()) {
			if (count_files) {
				setOrderParameter(conOrderParameterSCHEDULER_SOS_FILE_OPERATIONS_FILE_COUNT, String.valueOf(intNoOfHitsInResultSet));
			}
			Variable_set objP = spooler_task.order().params();
			if (isNotNull(objP)) {
				setOrderParameter(conOrderParameterSCHEDULER_SOS_FILE_OPERATIONS_RESULT_SET, strResultSetFileList);
				setOrderParameter(conOrderParameterSCHEDULER_SOS_FILE_OPERATIONS_RESULT_SET_SIZE, String.valueOf(intNoOfHitsInResultSet));
			}

			if (isNotEmpty(strOnEmptyResultSet) && intNoOfHitsInResultSet <= 0) {
				JSJ_I_0090.toLog(strOnEmptyResultSet);
				setNextNodeState(strOnEmptyResultSet);
//				spooler_task.order().set_state(strOnEmptyResultSet);
			}
		}

		if (isNotEmpty(strResultList2File) && isNotEmpty(strResultSetFileList)) {
			JSTextFile objResultListFile = new JSTextFile(strResultList2File);
			try {
				if (objResultListFile.canWrite()) {
					objResultListFile.Write(strResultSetFileList);
					objResultListFile.close();
				}
				else {
					throw new JobSchedulerException(JSJ_F_0090.get(conParameterRESULT_LIST_FILE, strResultList2File));
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				JSJ_F_0080.toLog(strResultList2File, conParameterRESULT_LIST_FILE);
				throw new JobSchedulerException(e);
			}
		}

		if (isNotEmpty(strRaiseErrorIfResultSetIs)) {
			boolean flgR = compareIntValues(strRaiseErrorIfResultSetIs, intNoOfHitsInResultSet, intExpectedSizeOfResultSet);
			if (flgR == true) {
				logger.info(JSJ_E_0040.params(intNoOfHitsInResultSet, strRaiseErrorIfResultSetIs, intExpectedSizeOfResultSet));
				return false;
			}
		}

		//  http://www.sos-berlin.com/jira/browse/JITL-71
		if (intNoOfHitsInResultSet > 0 && flgCreateOrder == true && pflgOperationWasSuccessful == true) {
			if (flgCreateOrders4AllFiles == true) {
				for (File objFile : lstResultList) {
					createOrder(objFile.getAbsolutePath(), strOrderJobChainName);
				}
			}
			else {
				createOrder(strFirstFile, strOrderJobChainName);
			}
		}

		return pflgOperationWasSuccessful;
	}// private void createResultListParam

	public boolean compareIntValues(final String pstrComparator, final int pintValue1, final int pintValue2) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::compareIntValues";

		HashMap<String, Integer> objRelOp = new HashMap<String, Integer>();

		objRelOp.put("eq", 1);
		objRelOp.put("equal", 1);
		objRelOp.put("==", 1);
		objRelOp.put("=", 1);
		objRelOp.put("ne", 2);
		objRelOp.put("not equal", 2);
		objRelOp.put("!=", 2);
		objRelOp.put("<>", 2);
		objRelOp.put("lt", 3);
		objRelOp.put("less than", 3);
		objRelOp.put("<", 3);
		objRelOp.put("le", 4);
		objRelOp.put("less or equal", 4);
		objRelOp.put("<=", 4);
		objRelOp.put("ge", 5);
		objRelOp.put(JSJ_T_0010.get(), 5);
		objRelOp.put(">=", 5);
		objRelOp.put("gt", 6);
		objRelOp.put("greater than", 6);
		objRelOp.put(">", 6);

		boolean flgR = false;
		String strT1 = pstrComparator;
		Integer iOp = objRelOp.get(strT1.toLowerCase());
		if (isNotNull(iOp)) {
			switch (iOp) {
				case 1:
					flgR = pintValue1 == pintValue2;
					break;

				case 2:
					flgR = pintValue1 != pintValue2;
					break;

				case 3:
					flgR = pintValue1 < pintValue2;
					break;

				case 4:
					flgR = pintValue1 <= pintValue2;
					break;

				case 5:
					flgR = pintValue1 >= pintValue2;
					break;

				case 6:
					flgR = pintValue1 > pintValue2;
					break;

				default:
					break;
			}
		}
		else {
			throw new JobSchedulerException(JSJ_E_0017.get(pstrComparator));
		}

		return flgR;
	}// private boolean compareIntValues

	private void createOrder(final String pstrOrder4FileName, final String pstrOrderJobChainName) {

		final String conMethodName = conClassName + "::createOrder";

		Order objOrder = spooler.create_order();
		Variable_set objOrderParams = spooler.create_variable_set();

		// kb: merge actual parameters into created order params (2012-07-25)
		if (flgMergeOrderParameter == true && isOrderJob() == true) {
			objOrderParams.merge(getOrderParams());
		}

		objOrderParams.set_value(conOrderParameterSCHEDULER_FILE_PATH, pstrOrder4FileName);
		objOrderParams.set_value(conOrderParameterSCHEDULER_FILE_PARENT, new File(pstrOrder4FileName).getParent());
		objOrderParams.set_value(conOrderParameterSCHEDULER_FILE_NAME, new File(pstrOrder4FileName).getName());

		if (isNotEmpty(strNextState)) {
			objOrder.set_state(strNextState);
		}

		objOrder.set_params(objOrderParams);
		objOrder.set_id(pstrOrder4FileName);
		objOrder.set_title(JSJ_I_0017.get(conMethodName)); // "Order created by %1$s"

		Job_chain objJobchain = spooler.job_chain(pstrOrderJobChainName);
		objJobchain.add_order(objOrder);
		String strT = JSJ_I_0018.get(pstrOrder4FileName, pstrOrderJobChainName); // "Order '%1$s' created for JobChain '%2$s'."
		if (isNotEmpty(strNextState)) {
			strT += " " + JSJ_I_0019.get(strNextState); // "Next State is '%1$s'."
		}
		logger.info(strT);

	} // private void createOrder

	public String replaceVars4(String pstrReplaceIn) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::replaceVars";

		String strParamNameEnclosedInPercentSigns = "^.*%([^%]+)%.*$"; // "^.*%[^%]+%.*$";

		if (isNotNull(pstrReplaceIn)) {
			// To make orderparams available for substitution in orderparam value
			while (pstrReplaceIn.matches(strParamNameEnclosedInPercentSigns)) {
				String p = pstrReplaceIn.replaceFirst(strParamNameEnclosedInPercentSigns, "$1");
				String strPP = "%" + p + "%";
				String s = params.get(p);
				if (isNotNull(s)) {
					s = s.replace('\\', '/');
					pstrReplaceIn = pstrReplaceIn.replaceAll(strPP, s);
					JSJ_D_0044.toLog(name, strPP, s);
				}
				else {
					pstrReplaceIn = pstrReplaceIn.replaceAll(strPP, "?" + p + "?");
					JSJ_W_0043.toLog(p);
				}
			}
		}
		return pstrReplaceIn;
	} // private String replaceVars

	/**
	 *
	 * \brief setParams
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pobjparams
	 */
	public void setParams(final HashMap<String, String> pobjparams) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setParams";

		params = pobjparams;

	} // private void setParams

	/**
	 *
	 * \brief setReturnResult
	 *
	 * \details
	 *
	 * \return boolean
	 *
	 * @param pflgResult
	 * @return
	 */
	public boolean setReturnResult(final boolean pflgResult) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setReturnResult";
		boolean rc1 = pflgResult;

		// if (isJobchain()) {
		// rc1 = createResultListParam(pflgResult);
		// }
		// nicht nur für Jobchains rufen, weil hier auch die Order(s) erzeugt werden
		rc1 = createResultListParam(pflgResult);

		if (rc1 == false && isGraciousAll()) {
			return signalSuccess();
		}
		else {
			if (rc1 == false && isGraciousTrue()) {
				if (isJobchain()) {
					return signalFailure();
				}
				return conJobSuccess;
			}
			else {
				if (rc1 == true) {
					return signalSuccess();
				}
				else {
					return signalFailure();
				}
			}
		}
	} // private boolean setReturnResult

	public void CheckMandatoryFile() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CheckMandatoryFile";
		if (isNull(file)) {
			throw new JobSchedulerException(JSJ_E_0020.params(conParameterFILE));
		}
	} // private void CheckMandatoryFile

	public void CheckMandatorySource() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CheckMandatoryFile";
		if (isNull(source)) {
			throw new JobSchedulerException(JSJ_E_0020.params(conParameterSOURCE_FILE));
		}
	} // private void CheckMandatoryFile

	public void CheckMandatoryTarget() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CheckMandatoryFile";
		if (isNull(target)) {
			throw new JobSchedulerException(JSJ_E_0020.params(conParameterTARGET_FILE));
		}
	} // private void CheckMandatoryFile

	/**
	 *
	 * \brief calculateFileAge
	 *
	 * \details
	 *
	 * \return long
	 *
	 * @param hoursMinSec
	 * @return
	 */
	public long calculateFileAge(final String hoursMinSec) {
		// TODO implement this method in JSFile
		long age = 0;
		if (isNotEmpty(hoursMinSec)) {
			if (hoursMinSec.indexOf(":") > -1) {
				String[] timeArray = hoursMinSec.split(":");
				long hours = Long.parseLong(timeArray[0]);
				long minutes = Long.parseLong(timeArray[1]);
				long seconds = 0;
				if (timeArray.length > 2) {
					seconds = Long.parseLong(timeArray[2]);
				}
				age = hours * 3600000 + minutes * 60000 + seconds * 1000;
			}
			else {
				age = Long.parseLong(hoursMinSec);
			}
		}

		return age;
	}

	/**
	 * @return the params
	 */
	public Variable_set getParams() {
		final String conMethodName = conClassName + "::getParams";
		throw new JobSchedulerException(Messages.getMsg(JSJ_F_0110, conMethodName)); // not implemented
	}

	/**
	 * @param params1 the params to set
	 */
	public void setParams(final Variable_set params1) {
		final String conMethodName = conClassName + "::setParams";
		throw new JobSchedulerException(Messages.getMsg(JSJ_F_0110, conMethodName)); // notImplemented
		// this.params = params1;
	}
	private final String	JSJ_F_0110	= "JSJ_F_0110"; // "This function is not implemented: %1$s";

	class FileDescriptor {
		FileDescriptor() {

		}
		public long		lastModificationDate	= 0;
		public long		lastFileLength			= 0;
		public String	FileName				= "";
		public boolean	flgIsSteady				= false;
	}





	public boolean checkSteadyStateOfFiles() {

		@SuppressWarnings("unused")



		final String conMethodName = conClassName + "::checkSteadyStateOfFiles";
		if (isNull(lstResultList)) {
			saveResultList();
		}
		boolean flgAllFilesAreSteady = flgOperationWasSuccessful;

		if (flgOperationWasSuccessful == true && flgCheckSteadyStateOfFiles == true && lstResultList.size() > 0) {
			logger.debug("checking file(s) for steady state");
			Vector<FileDescriptor> lstFD = new Vector<FileDescriptor>();
			for (File objFile : lstResultList) {
				FileDescriptor objFD = new FileDescriptor();
				objFD.lastFileLength = objFile.length();
				objFD.lastModificationDate = objFile.lastModified();
				objFD.FileName = objFile.getAbsolutePath();

				logger.debug("filedescriptor is : " + objFD.lastModificationDate + ", "  + objFD.lastFileLength);

			    lstFD.add(objFD);
			}
			try {
				Thread.sleep(lngCheckSteadyStateInterval);
			}
			catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			for (int i = 0; i < lngSteadyCount; i++) {
				flgAllFilesAreSteady = true;
				for (FileDescriptor objFD : lstFD) {
					File objActFile = new File(objFD.FileName);
					logger.debug("result is : " + objActFile.lastModified() + ", " + objFD.lastModificationDate + ", " + objActFile.length() + ", "
							+ objFD.lastFileLength);
					if (flgUseNIOLock == true) {
						try {
							RandomAccessFile objRAFile = new RandomAccessFile(objActFile, "rw");
							FileChannel channel = objRAFile.getChannel();
							FileLock lock = channel.lock(); // Get an exclusive lock on the whole file
							try {
								lock = channel.tryLock();
								logger.debug(String.format("lock for file '%1$s' ok", objActFile.getAbsolutePath()));
								// Ok. You got the lock
								break;
							}
							catch (OverlappingFileLockException e) {
								flgAllFilesAreSteady = false;
								logger.info(String.format("File '%1$s' is open by someone else", objActFile.getAbsolutePath()));
								break;
							}
							finally {
								lock.release();
								logger.debug(String.format("release lock for '%1$s'", objActFile.getAbsolutePath()));
								if (objRAFile != null) {
									channel.close();
									objRAFile.close();
									objRAFile = null;
								}
							}
						}
						catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (objActFile.lastModified() != objFD.lastModificationDate || objActFile.length() != objFD.lastFileLength) {
						flgAllFilesAreSteady = false;
						objFD.lastModificationDate = objActFile.lastModified();
						objFD.lastFileLength = objActFile.length();
						objFD.flgIsSteady = false;
						logger.info(String.format("File '%1$s' changed during checking steady state", objActFile.getAbsolutePath()));
						break;
					}
					else {
						objFD.flgIsSteady = true;
					}
				}
				if (flgAllFilesAreSteady == false) {
					try {
						Thread.sleep(lngCheckSteadyStateInterval);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else {
					break;
				}
			}
			if (flgAllFilesAreSteady == false) {
				logger.error("not all files are steady");
				for (FileDescriptor objFD : lstFD) {
					if (objFD.flgIsSteady == false) {
						logger.info(String.format("File '%1$s' is not steady", objFD.FileName));
					}
				}
				throw new JobSchedulerException("not all files are steady");
			}
		}
		return flgAllFilesAreSteady;
	} // private boolean checkSteadyStateOfFiles

}
