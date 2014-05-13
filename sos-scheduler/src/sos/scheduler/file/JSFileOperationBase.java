package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JFO_F_0100;
import static com.sos.scheduler.messages.JSMessages.JFO_F_0101;
import static com.sos.scheduler.messages.JSMessages.JFO_F_0102;
import static com.sos.scheduler.messages.JSMessages.JFO_F_0103;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0105;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0106;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0020;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0040;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0110;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0080;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0090;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.util.SOSFilelistFilter;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFileName;
import com.sos.JSHelper.Options.SOSOptionFileSize;
import com.sos.JSHelper.Options.SOSOptionGracious;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JSFileOperationBase extends JSToolBox implements JSJobUtilities {

	protected JSExistsFileOptions	objOptions									= null;
	private JSJobUtilities			objJSJobUtilities							= this;
	//
	protected static final String	conPropertyJAVA_IO_TMPDIR					= "java.io.tmpdir";

	/** give a path for files to remove */
	protected String				filePath									= System.getProperty(conPropertyJAVA_IO_TMPDIR);

	/** give the number of milliseconds, defaults to 24 hours */
	protected long					lngFileAge									= 86400000;

	/** number of files which causes a warning */
	protected int					warningFileLimit							= 0;

	protected static final String	conParameterWARNING_FILE_LIMIT				= "warning_file_limit";
	protected static final String	conParameterFILE_AGE						= "file_age";
	protected static final String	conParameterFILE_SPEC						= "file_spec";
	protected static final String	conParameterFILE_SPECIFICATION				= "file_specification";
	protected static final String	conParameterFILE_PATH						= "file_path";

	protected static final String	conParameterMAX_FILE_SIZE					= "max_file_size";
	protected static final String	conParameterMIN_FILE_SIZE					= "min_file_size";
	protected static final String	conParameterMAX_FILE_AGE					= "max_file_age";
	protected static final String	conParameterMIN_FILE_AGE					= "min_file_age";
	protected static final String	conParameterTARGET_FILE						= "target_file";
	protected static final String	conParameterFILE							= "file";
	protected static final String	conParameterSOURCE_FILE						= "source_file";
	protected static final String	conParameterON_EMPTY_RESULT_SET				= "on_empty_result_set";
	protected static final String	conParameterSKIP_LAST_FILES					= "skip_last_files";
	protected static final String	conParameterSKIP_FIRST_FILES				= "skip_first_files";
	protected static final String	conParameterOVERWRITE						= "overwrite";
	protected static final String	conParameterCOUNT_FILES						= "count_files";
	protected static final String	conParameterREPLACEMENT						= "replacement";
	protected static final String	conParameterREPLACING						= "replacing";
	protected static final String	conParameterRAISE_ERROR_IF_RESULT_SET_IS	= "Raise_Error_If_Result_Set_Is";
	protected static final String	conParameterEXPECTED_SIZE_OF_RESULT_SET		= "Expected_Size_Of_Result_Set";
	protected Logger				logger										= Logger.getLogger(JSFileOperationBase.class);
	protected static final String	conParameterRESULT_LIST_FILE				= "Result_List_File";
	protected static final String	conParameterRECURSIVE						= "recursive";
	protected static final String	conParameterCREATE_DIR						= "create_dir";
	protected static final String	conClassName								= "JobSchedulerFileOperationBase";
	protected static final String	conValueYES									= "yes";
	public static final String		conParameterGRACIOUS						= "gracious";
	@SuppressWarnings("unused")
	private final String			conSVNVersion								= "$Id$";
	protected boolean				flgOperationWasSuccessful					= false;
	protected String				name										= null;
	protected String				file										= null;
	private final String			strFileSpecDefault							= ".*";
	protected String				fileSpec									= strFileSpecDefault;
	protected String				minFileAge									= "0";
	protected String				maxFileAge									= "0";
	private final String			conFileSizeDefault							= "-1";
	protected String				minFileSize									= conFileSizeDefault;
	protected String				maxFileSize									= conFileSizeDefault;
	protected int					skipFirstFiles								= 0;
	protected int					skipLastFiles								= 0;
	// HashMap<String, String> params = null;
	protected String				strGracious									= "false";
	String							source										= null;
	String							target										= null;
	int								flags										= 0;
	String							replacing									= null;
	String							replacement									= null;
	boolean							count_files									= false;
	protected final int				isCaseInsensitive							= Pattern.CASE_INSENSITIVE;
	public int						intNoOfHitsInResultSet						= 0;
	protected String				strOnEmptyResultSet							= null;
	protected String				strResultList2File							= null;
	protected int					intExpectedSizeOfResultSet					= 0;
	// possible values equal, lt, le, eq, ge, gt, ne
	protected String				strRaiseErrorIfResultSetIs					= null;
	protected Vector<File>			lstResultList								= null;															// new
																																					// Vector<File>();
	protected boolean				flgCreateOrder								= false;
	protected boolean				flgCreateOrders4AllFiles					= false;
	protected String				strOrderJobChainName						= null;
	protected String				strNextState								= null;

	public JSFileOperationBase() {
		super("com_sos_scheduler_messages");
	}

	protected void initialize() {
		// logger.debug(Options().toString());
		lstResultList = new Vector<File>();

		// lngFileAge = calculateFileAge(getParamValue(conParameterFILE_AGE));
		// warningFileLimit = getParamInteger(conParameterWARNING_FILE_LIMIT, 0);

		intExpectedSizeOfResultSet = Options().expected_size_of_result_set.value(); // getParamInteger(conParameterEXPECTED_SIZE_OF_RESULT_SET,
																					// 0);
		strRaiseErrorIfResultSetIs = Options().raise_error_if_result_set_is.Value(); // getParamValue(conParameterRAISE_ERROR_IF_RESULT_SET_IS,
																						// "");
		strResultList2File = Options().result_list_file.Value(); // getParamValue(conParameterRESULT_LIST_FILE, "");
		strOnEmptyResultSet = Options().on_empty_result_set.Value(); // getParamValue(conParameterON_EMPTY_RESULT_SET, "");
		file = Options().file.Value();
		// source = file = filePath = getParamValue(conParameterSOURCE_FILE, conParameterFILE, conParameterFILE_PATH);
		fileSpec = strFileSpecDefault;
		// fileSpec = getParamValue(conParameterFILE_SPEC, conParameterFILE_SPECIFICATION);
		fileSpec = Options().file_spec.Value();
		// target = getParamValue(conParameterTARGET_FILE);
		minFileAge = Options().min_file_age.Value();
		maxFileAge = Options().max_file_age.Value();
		minFileSize = Options().min_file_size.Value();
		maxFileSize = Options().min_file_size.Value();
		strGracious = Options().gracious.Value();
		skipFirstFiles = Options().skip_first_files.value();
		skipLastFiles = Options().skip_last_files.value();

		flags = 0;
		// if (getParamBoolean(conParameterCREATE_DIR, false) == true) {
		// flags |= SOSFileOperations.CREATE_DIR;
		// }
		if (Options().gracious.value()) {
			flags |= SOSOptionGracious.GRACIOUS;
		}
		// if (Options().o == false) {
		// flags |= SOSFileOperations.NOT_OVERWRITE;
		// }
		// if (Options().recursive.value() == true) {
		// flags |= SOSFileOperations.RECURSIVE;
		// }
		// replacing = getParamValue(conParameterREPLACING, "");
		// replacement = getParamValue(conParameterREPLACEMENT, "");
		String strM = JSJ_E_0110.get(conParameterREPLACEMENT, conParameterREPLACING);
		if (isNotNull(replacing) && isNull(replacement)) {
			throw new JobSchedulerException(strM);
		}
		if (isNull(replacing) && isNotNull(replacement)) {
			throw new JobSchedulerException(strM);
		}
	}

	/**
	 *
	 * \brief Options - returns the JSExistsFileOptionClass
	 *
	 * \details
	 * The JSExistsFileOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *
	 * \return JSExistsFileOptions
	 *
	 */
	public JSExistsFileOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JSExistsFileOptions();
		}
		return objOptions;
	}

	/**
	 *
	 * \brief Options - set the JSExistsFileOptionClass
	 *
	 * \details
	 * The JSExistsFileOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *
	 * \return JSExistsFileOptions
	 *
	 */
	public JSExistsFileOptions Options(final JSExistsFileOptions pobjOptions) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		objOptions = pobjOptions;
		return objOptions;
	}

	public Vector<File> getResultList() {
		return lstResultList;
	}

	public boolean createResultListParam(final boolean pflgResult) {
		String strT = "";
		intNoOfHitsInResultSet = lstResultList.size();

		if (isNotNull(lstResultList) && lstResultList.size() > 0) {
			intNoOfHitsInResultSet = lstResultList.size();
			for (File objFile : lstResultList) {
				strT += objFile.getAbsolutePath() + ";";
			}
		}

		if (isNotEmpty(strResultList2File) && isNotEmpty(strT)) {
			JSTextFile objResultListFile = new JSTextFile(strResultList2File);
			try {
				if (objResultListFile.canWrite()) {
					objResultListFile.Write(strT);
					objResultListFile.close();
				}
				else {
					throw new JobSchedulerException(JSJ_F_0090.get(conParameterRESULT_LIST_FILE, strResultList2File));
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				JSJ_F_0080.toLog(strResultList2File, conParameterRESULT_LIST_FILE);
			}
		}

		if (isNotEmpty(strRaiseErrorIfResultSetIs)) {
			boolean flgR = Options().raise_error_if_result_set_is.compareIntValues(intNoOfHitsInResultSet, intExpectedSizeOfResultSet);
			if (flgR == true) {
				logger.info(JSJ_E_0040.get(intNoOfHitsInResultSet, strRaiseErrorIfResultSetIs, intExpectedSizeOfResultSet));
				return false;
			}
		}

		return pflgResult;
	}// private void createResultListParam

	public void CheckMandatoryFile() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CheckMandatoryFile";
		if (isNull(file)) {
			throw new JobSchedulerException(JSJ_E_0020.get(conParameterFILE));
		}
	} // private void CheckMandatoryFile

	public void CheckMandatorySource() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CheckMandatoryFile";
		if (isNull(source)) {
			throw new JobSchedulerException(JSJ_E_0020.get(conParameterSOURCE_FILE));
		}
	} // private void CheckMandatoryFile

	public void CheckMandatoryTarget() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CheckMandatoryFile";
		if (isNull(source)) {
			throw new JobSchedulerException(JSJ_E_0020.get(conParameterTARGET_FILE));
		}
	} // private void CheckMandatoryFile

	@Override
	public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {

		String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
		return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
	}

	/**
	 *
	 * \brief replaceSchedulerVars
	 *
	 * \details
	 * Dummy-Method to make sure, that there is always a valid Instance for the JSJobUtilities.
	 * \return
	 *
	 * @param isWindows
	 * @param pstrString2Modify
	 * @return
	 */
	@Override
	public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
		logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
		return pstrString2Modify;
	}

	/**
	 *
	 * \brief setJSParam
	 *
	 * \details
	 * Dummy-Method to make shure, that there is always a valid Instance for the JSJobUtilities.
	 * \return
	 *
	 * @param pstrKey
	 * @param pstrValue
	 */
	@Override
	public void setJSParam(final String pstrKey, final String pstrValue) {

	}

	@Override
	public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {

	}

	/**
	 *
	 * \brief setJSJobUtilites
	 *
	 * \details
	 * The JobUtilities are a set of methods used by the SSH-Job or can be used be other, similar, job-
	 * implementations.
	 *
	 * \return void
	 *
	 * @param pobjJSJobUtilities
	 */
	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {

		if (pobjJSJobUtilities == null) {
			objJSJobUtilities = this;
		}
		else {
			objJSJobUtilities = pobjJSJobUtilities;
		}
		logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
	}

	@Override
	public String getCurrentNodeName() {
		// TODO Auto-generated method stub
		return null;
	}

	// public String JSJ_T_0010 = "greater or equal";
	// public String JSJ_E_0017 = "Compare operator not known: '%1$s'";

	// public String JSJ_I_0017 = "Order created by %1$s";
	// public String JSJ_I_0018 = "Order '%1$s' created for JobChain '%2$s'.";
	// public String JSJ_I_0019 = "Next State is '%1$s'.";

	/**
	 * Checks for file existence.
	 * if file is a directory and <em>fileSpec</em> is not NULL
	 * <em>fileSpec</em> is applied for matching files in the directory.
	 * In this case true will only be returned if at least one file was matched.
	 *
	 * @param file
	 * 					file or directory
	 * @param fileSpec1
	 * 					Regular expression for file filtering if file is a directory
	 * @param fileSpecFlags
	 *          Pattern bitmask providing the regular expression <em>fileSpec</em>
	 * @param minFileAge1
	 * 					Filter for file age: files with a earlier modification date are considered as non existing.
	 * 					Possible values: sec, hh:mm, hh:mm:sec
	 * 					The Resulting set is sorted by file age in ascending order (most recent first)
	 * @param maxFileAge1
	 * 					Filter for file age: files with a later modification date are considered as non existing.
	 * 					Possible values: sec, hh:mm, hh:mm:sec
	 * 					The Resulting set is sorted by file age in ascending order (most recent first)
	 * @param minFileSize1
	 * 					Filter for file size: smaller files are considered as non existing.
	 * 					Possible values: number (bytes), numberKB, numberMB, numberGB (KB, MB, GB case insensitive)
	 * 					The Resulting set is sorted by file size in ascending order (smallest first).
	 * 					If the set is additionally filtered by file age the set is sorted by file age.
	 * @param maxFileSize1
	 * 					Filter for file size: greater files are considered as non existing.
	 * 					Possible values: number (bytes), numberKB, numberMB, numberGB (KB, MB, GB case insensitive)
	 * 					The Resulting set is sorted by file size in ascending order (smallest first).
	 *					If the set is additionally filtered by file age the set is sorted by file age.
	 * @param skipFirstFiles1
	 * 					Decreases the number of noticed files. Requires at least one filter of minFileAge, maxFilesAge,
	 * 					minFileSize or maxFileSize. The file sre skipped in respect of the sorting of the filtered set.
	 * 					The smallest or most recent files are skipped.
	 * @param skipLastFiles1
	 * 					Decreases the number of noticed files. Requires at least one filter of minFileAge, maxFilesAge,
	 * 					minFileSize or maxFileSize. The file sre skipped in respect of the sorting of the filtered set.
	 * 					The greatest or oldest files are skipped.
	 * @see
	 * 				<a href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
	 * @param logger
	 * 					SOSLogger
	 * @return true if file exists or false if not
	 * @throws IOException
	 * @throws Exception
	 */
	public boolean existsFile(final SOSOptionFileName objFile, final SOSOptionRegExp fileSpec1, //
			final SOSOptionTime minFileAge1, final SOSOptionTime maxFileAge1, //
			final SOSOptionFileSize minFileSize1, //
			final SOSOptionFileSize maxFileSize1, final SOSOptionInteger skipFirstFiles1, final SOSOptionInteger skipLastFiles1, //
			final int minNumOfFiles, final int maxNumOfFiles) throws IOException, Exception {

		long minAge = 0;
		long maxAge = 0;

		long minSize = -1;
		long maxSize = -1;

		// log_debug1("arguments for existsFile:");
		// log_debug1("argument file=" + file.toString());
		// log_debug1("argument fileSpec=" + fileSpec1);
		// log_debug1("argument fileSpecFlags=" + fileSpec1.getRegExpFlagsText());
		//
		// log_debug1("argument minFileAge=" + minFileAge1);
		// log_debug1("argument maxFileAge=" + maxFileAge1);
		minAge = minFileAge1.calculateFileAge();
		maxAge = maxFileAge1.calculateFileAge();

		// log_debug1("argument minFileSize=" + minFileSize1);
		// log_debug1("argument maxFileSize=" + maxFileSize1);
		minSize = minFileSize1.getFileSize();
		maxSize = maxFileSize1.getFileSize();

		// log_debug1("argument skipFirstFiles=" + skipFirstFiles1);
		// log_debug1("argument skipLastFiles=" + skipLastFiles1);
		//
		// log_debug1("argument minNumOfFiles=" + minNumOfFiles);
		// log_debug1("argument maxNumOfFiles=" + maxNumOfFiles);

		if (skipFirstFiles1.value() < 0) {
			throw new JobSchedulerException(JFO_F_0100.get(skipFirstFiles1.value(), "skipFirstFiles"));
		}
		if (skipLastFiles1.value() < 0) {
			throw new JobSchedulerException(JFO_F_0100.get(skipLastFiles1.value(), "skipLastFiles"));
		}

		if (skipFirstFiles1.value() > 0 && skipLastFiles1.value() > 0) {
			JFO_F_0101.toLog();
		}
		if (skipFirstFiles1.value() > 0 || skipLastFiles1.value() > 0) {
			if (minAge == 0 && maxAge == 0 && minSize == -1 && maxSize == -1) {
				JFO_F_0103.toLog();
			}
		}
		String filename = objFile.substituteAllDate();

		// should any opening and closing brackets be found in the file name, then this is an error
		Matcher m = Pattern.compile("\\[[^]]*\\]").matcher(filename);
		if (m.find()) {
			JFO_F_0102.toLog(m.group());
		}

		File fleFile = new File(filename);
		if (!fleFile.exists()) {
			logger.debug(JFO_I_0105.get(fleFile.getCanonicalPath()));
			return false;
		}
		else {
			// Es ist eine Datei und sie existiert
			if (!fleFile.isDirectory()) {
				JFO_I_0106.toLog(fleFile.getCanonicalPath());

				long currentTime = System.currentTimeMillis();

				if (minAge > 0) {
					long interval = currentTime - fleFile.lastModified();
					if (interval < 0)
						throw new Exception("Cannot filter by file age. File [" + fleFile.getCanonicalPath() + "] was modified in the future.");

					if (interval < minAge) {
						log("checking file age " + fleFile.lastModified() + ": minimum age required is " + minAge);
						return false;
					}
				}

				if (maxAge > 0) {
					long interval = currentTime - fleFile.lastModified();
					if (interval < 0)
						throw new JobSchedulerException("Cannot filter by file age. File [" + fleFile.getCanonicalPath() + "] was modified in the future.");

					if (interval > maxAge) {
						log("checking file age " + fleFile.lastModified() + ": maximum age required is " + maxAge);
						return false;
					}
				}

				if (minSize > -1 && minSize > fleFile.length()) {
					log("checking file size " + fleFile.length() + ": minimum size required is " + minFileSize1);
					return false;
				}

				if (maxSize > -1 && maxSize < fleFile.length()) {
					log("checking file size " + fleFile.length() + ": maximum size required is " + maxFileSize1);
					return false;
				}

				if (skipFirstFiles1.value() > 0 || skipLastFiles1.value() > 0) {
					log("file skipped");
					return false;
				}

				return true;
			}
			else {
				// wenn kein fileSpec angegeben wurde, reicht die simple Existenz aus
				if (fileSpec1.IsEmpty()) {
					log("checking file " + fleFile.getCanonicalPath() + ": directory exists");
					return true;
				}

				Vector<File> fileList = getFilelist(fleFile.getPath(), fileSpec1, false, minAge, maxAge, minSize, maxSize, skipFirstFiles1.value(),
						skipLastFiles1.value());

				if (fileList.isEmpty()) {
					log("checking file " + fleFile.getCanonicalPath() + ": directory contains no files matching " + fileSpec1);
					return false;

				}
				else {
					log("checking file " + fleFile.getCanonicalPath() + ": directory contains " + fileList.size() + " file(s) matching " + fileSpec1);
					for (int i = 0; i < fileList.size(); i++) {
						File checkFile = fileList.get(i);
						log("found " + checkFile.getCanonicalPath());
					}

					if (minNumOfFiles >= 0 && fileList.size() < minNumOfFiles) {
						log("found " + fileList.size() + " files, minimum expected " + minNumOfFiles + " files");
						return false;
					}

					if (maxNumOfFiles >= 0 && fileList.size() > maxNumOfFiles) {
						log("found " + fileList.size() + " files, maximum expected " + maxNumOfFiles + " files");
						return false;
					}

					lstResultList.addAll(fileList);
					return true;
				}
			}
		}
	}

	/**
	 * liefert Dateiliste des eingegebenen Verzeichnis zurueck.
	 * @return Vector Dateiliste
	 * @param regexp ein regul&auml;er Ausdruck
	 * @param flag ein Integer-Wert: CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, and CANON_EQ
	 * @param withSubFolder
	 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/api/constant-values.html#java.util.regex.Pattern.UNIX_LINES">Constant Field Values</a>
	 */
	private Vector<File> getFilelist(final String folder, final SOSOptionRegExp regexp, final boolean withSubFolder, final long minFileAge1,
			final long maxFileAge1, final long minFileSize1, final long maxFileSize1, final int skipFirstFiles1, final int skipLastFiles1) throws Exception {

		Vector<File> filelist = new Vector<File>();
		Vector<File> temp = new Vector<File>();

		File objFile = null;
		File[] subDir = null;

		objFile = new File(folder);
		subDir = objFile.listFiles();

		temp = this.getFilelist(folder, regexp);

		temp = filelistFilterAge(temp, minFileAge1, maxFileAge1);
		temp = filelistFilterSize(temp, minFileSize1, maxFileSize1);

		if ((minFileSize1 != -1 || minFileSize1 != -1) && minFileAge1 == 0 && maxFileAge1 == 0)
			temp = filelistSkipFiles(temp, skipFirstFiles1, skipLastFiles1, "sort_size");
		else
			if (minFileAge1 != 0 || maxFileAge1 != 0)
				temp = filelistSkipFiles(temp, skipFirstFiles1, skipLastFiles1, "sort_age");

		filelist.addAll(temp);

		if (withSubFolder) {
			for (File element : subDir) {
				if (element.isDirectory()) {
					filelist.addAll(getFilelist(element.getPath(), regexp, true, minFileAge1, maxFileAge1, minFileSize1, maxFileSize1, skipFirstFiles1,
							skipLastFiles1));
				}
			}
		}
		return filelist;
	}

	/**
	 * liefert Dateiliste eines Verzeichnis.
	 * @return Vector Dateiliste
	 * @param regexp ein regul&auml;er Ausdruck
	 * @param flag ein Integer-Wert: CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, and CANON_EQ
	 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/api/constant-values.html#java.util.regex.Pattern.UNIX_LINES">Constant Field Values</a>
	 */
	public Vector<File> getFilelist(final String folder, final SOSOptionRegExp regexp) throws Exception {

		Vector<File> filelist = new Vector<File>();

		if (folder == null || folder.length() == 0) {
			throw new JobSchedulerException("a null value for param 'directory' is not allowed");
		}

		File f = new File(folder);
		if (!f.exists()) {
			String strM = JFO_I_0105.get(folder);
			logger.fatal(strM);
			throw new JobSchedulerException(strM);
		}
		filelist = new Vector<File>();
		File[] files = f.listFiles(new SOSFilelistFilter(regexp.Value(), regexp.getRegExpFlags()));
		for (File file2 : files) {
			if (file2.isDirectory()) {
			}
			else
				if (file2.isFile()) {
					filelist.add(file2);
				}
				else {
					// unknown
				}
		}

		return filelist;
	}

	private Vector<File> filelistFilterAge(Vector<File> filelist, final long minAge, final long maxAge) throws Exception {

		long currentTime = System.currentTimeMillis();

		if (minAge != 0) {
			File file1;
			Vector<File> newlist = new Vector<File>();
			for (int i = 0; i < filelist.size(); i++) {
				file1 = filelist.get(i);
				long interval = currentTime - file1.lastModified();

				if (interval < 0) {
					throw new JobSchedulerException("Cannot filter by file age. File [" + file1.getCanonicalPath() + "] was modified in the future.");
				}
				if (interval >= minAge)
					newlist.add(file1);
			}
			filelist = newlist;
		}

		if (maxAge != 0) {
			File file1;
			Vector<File> newlist = new Vector<File>();
			for (int i = 0; i < filelist.size(); i++) {
				file1 = filelist.get(i);
				long interval = currentTime - file1.lastModified();

				if (interval < 0) {
					throw new JobSchedulerException("Cannot filter by file age. File [" + file1.getCanonicalPath() + "] was modified in the future.");
				}
				if (interval <= maxAge)
					newlist.add(file1);
			}
			filelist = newlist;
		}
		return filelist;
	}

	private Vector<File> filelistFilterSize(Vector<File> filelist, final long minSize, final long maxSize) throws Exception {

		if (minSize > -1) {
			File file1;
			Vector<File> newlist = new Vector<File>();
			for (int i = 0; i < filelist.size(); i++) {
				file1 = filelist.get(i);
				if (file1.length() >= minSize)
					newlist.add(file1);
			}
			filelist = newlist;
		}

		if (maxSize > -1) {
			File file1;
			Vector<File> newlist = new Vector<File>();
			for (int i = 0; i < filelist.size(); i++) {
				file1 = filelist.get(i);
				if (file1.length() <= maxSize)
					newlist.add(file1);
			}
			filelist = newlist;
		}

		return filelist;
	}

	private Vector<File> filelistSkipFiles(Vector<File> filelist, final int skipFirstFiles1, final int skipLastFiles1, final String sorting) throws Exception {

		Object[] oArr = filelist.toArray();

		class SizeComparator implements Comparator {
			@Override
			public int compare(final Object o1, final Object o2) {
				int ret = 0;
				long val1 = ((File) o1).length();
				long val2 = ((File) o2).length();

				if (val1 < val2)
					ret = -1;
				if (val1 == val2)
					ret = 0;
				if (val1 > val2)
					ret = 1;

				return ret;
			}
		}
		;

		class AgeComparator implements Comparator {
			@Override
			public int compare(final Object o1, final Object o2) {
				int ret = 0;
				long val1 = ((File) o1).lastModified();
				long val2 = ((File) o2).lastModified();

				if (val1 > val2)
					ret = -1;
				if (val1 == val2)
					ret = 0;
				if (val1 < val2)
					ret = 1;

				return ret;
			}
		}
		;

		// sortiert die Dateien im Array aufsteigend nach Größe der Datei, d.h. kleinere zuerst
		if (sorting.equals("sort_size"))
			Arrays.sort(oArr, new SizeComparator());
		else
			// sortiert die Dateien im Array aufsteigend nach Änderungsdatum der Datei, d.h. jüngere zuerst
			if (sorting.equals("sort_age"))
				Arrays.sort(oArr, new AgeComparator());

		// Skip files
		filelist = new Vector<File>();
		for (int i = 0 + skipFirstFiles1; i < oArr.length - skipLastFiles1; i++) {
			filelist.add((File) oArr[i]);
		}

		return filelist;
	}

	private void log(final String msg) {

		try {
			logger.info(msg);
		}
		catch (Exception e) {
		}
	}

	private void log_debug1(final String msg) {

		try {
			if (logger != null)
				logger.debug(msg);
		}
		catch (Exception e) {
		}
	}

	@Override
	public void setStateText(final String pstrStateText) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCC(final int pintCC) {
		// TODO Auto-generated method stub
		
	}

	@Override public void setNextNodeState(final String pstrNodeName) {
		// TODO Auto-generated method stub
		
	}

}
