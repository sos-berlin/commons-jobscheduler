/**
 *
 */
package com.sos.scheduler.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sos.JSHelper.interfaces.ISOSComboItem;

/**
 * @author KB
 *
 */
public class LanguageDescriptorList {



	// TODO use a config file for the language definitions (monitor and Job)
	/**
	 *
	 */
	public static final String				conLanguageJAVAX_SCRIPT_RHINO	= "javax.script:rhino";
	public static final String				conLanguageJAVAX_Javascript		= "java:javascript";  // same as rhino, but with "beans"
	public static final String				conLanguagePL_SQL				= "plsql";
	public static final String				conLanguagePERL_SCRIPT			= "perlScript";
	public static final String				conLanguageVB_SCRIPT			= "VBScript";
	public static final String				conLanguageJAVASCRIPT			= "javascript";
	public static final String				conLanguageSHELL				= "shell";
	public static final String				conLanguageJAVA					= "java";
	public static final String				conLanguageSQL_PLUS				= "sql*plus";
	public static final String				conLanguageSSH					= "ssh";

//	private static final String[]			_languagesJob					= { conLanguageSHELL, conLanguageJAVA, conLanguageJAVASCRIPT, conLanguageVB_SCRIPT,
//			conLanguagePERL_SCRIPT, conLanguageJAVAX_SCRIPT_RHINO, conLanguagePL_SQL, conLanguageSQL_PLUS, conLanguageSSH, "" };
//
	// @formatter:off   // avoid formatting
	private static List<LanguageDescriptor>	lstLanguages					= Arrays.asList(       // avoid formatting
			new LanguageDescriptor(conLanguageSHELL, 			0, false, conLanguageSHELL, "", "", true, true),   // avoid formatting
			new LanguageDescriptor(conLanguageJAVA, 			1, false, conLanguageJAVA, "", "", true, true), // avoid formatting
			new LanguageDescriptor(conLanguageJAVASCRIPT, 		2, false, conLanguageJAVASCRIPT, "", "", true, true),   // avoid formatting
			new LanguageDescriptor(conLanguageVB_SCRIPT, 		3, false, conLanguageVB_SCRIPT, "", "", true, true),   // avoid formatting
			new LanguageDescriptor(conLanguagePERL_SCRIPT, 		4, false, conLanguagePERL_SCRIPT, "", "", true, true),    // avoid formatting
			new LanguageDescriptor(conLanguageJAVAX_SCRIPT_RHINO, 5, false, conLanguageJAVAX_SCRIPT_RHINO, "", "", true, true),    // avoid formatting
		/*	new LanguageDescriptor(conLanguagePL_SQL, 			6, true, conLanguageJAVA, "sos.scheduler.db.JobSchedulerPLSQLJobJSAdapterClass",
																							"jobdoc/JobSchedulerPLSQLJob.xml", true, false),
			new LanguageDescriptor(conLanguageSQL_PLUS, 		7, true, conLanguageJAVA, "sos.scheduler.db.SOSSQLPlusJobJSAdapterClass",
																							"jobdoc/SOSSQLPlusJob.xml", true, false),
			new LanguageDescriptor(conLanguageSSH, 				8, true, conLanguageJAVA, "sos.scheduler.job.SOSSSHJob2JSAdapter",
																							"jobdoc/SOSSSHJob2JSAdapter.xml", true, false),
        */																							
			new LanguageDescriptor(conLanguageJAVAX_Javascript, 9, false, conLanguageJAVAX_Javascript, "", "", true, true)    // avoid formatting
																			);
	// @formatter:on

	public LanguageDescriptorList() {
	}

	public static String[] getLanguages4APIJobs() {
		List<String> lstL = new ArrayList<String>();
		for (LanguageDescriptor objL : lstLanguages) {
			if (objL.isIsAPIL()) {
				lstL.add(objL.getLanguageName());
			}
		}
		String[] strR = lstL.toArray(new String[lstL.size()]);
		return strR;
	}

	public static String[] getLanguages4Monitor() {
		List<String> lstL = new ArrayList<String>();
		for (LanguageDescriptor objL : lstLanguages) {
			if (objL.isIsMonitorL()) {
				lstL.add(objL.getLanguageName());
			}
		}
		String[] strR = lstL.toArray(new String[lstL.size()]);
		return strR;
	}

	public static ArrayList<ISOSComboItem> getComboItems4APIJobs() {
		ArrayList<ISOSComboItem> lstL = new ArrayList<ISOSComboItem>();
		for (LanguageDescriptor objL : lstLanguages) {
			if (objL.isIsAPIL()) {
				lstL.add(objL);
			}
		}
		return lstL;
	}

	public static LanguageDescriptor getLanguageDescriptor(final int pintLang) {
		for (LanguageDescriptor objL : lstLanguages) {
			if (objL.getLanguageNumber() == pintLang) {
				return objL;
			}
		}
		return null;
	}

	public static LanguageDescriptor getLanguageDescriptor4Class(final String pstrClassName) {
		if (pstrClassName != null && pstrClassName.length() > 0) {
			for (LanguageDescriptor objL : lstLanguages) {
				if (objL.getClassName().equalsIgnoreCase(pstrClassName)) {
					return objL;
				}
			}
		}

		return null;
	}

	public static LanguageDescriptor getDefaultLanguage() {
		return lstLanguages.get(0); // Shell
	}

	public static LanguageDescriptor getDefaultLanguage4Monitor() {
		return lstLanguages.get(2); // JavaScript
	}

	public static LanguageDescriptor getLanguageDescriptor(final String pstrLanguage) {
		if (pstrLanguage != null && pstrLanguage.length() > 0) {
			for (LanguageDescriptor objL : lstLanguages) {
				if (objL.getLanguageName().equalsIgnoreCase(pstrLanguage.toLowerCase())) {
					return objL;
				}
			}
		}

		return null;
	}

	public static String getJavaClassName4HiddenJob(final int pintLang) {
		String strR = "";
		LanguageDescriptor objL = getLanguageDescriptor(pintLang);
		if (objL != null) {
			strR = objL.getClassName();
		}
		return strR;
	}

	/**
	 * static ist eigentlich nicht gewollt, geht aber aus Historischen Gründen nicht anders.
	*
	* \brief isHiddenJobLanguage
	*
	* \details
	*
	* \return boolean
	*
	 */
	public static boolean isHiddenJobLanguage(final int pintLang) {
		LanguageDescriptor objL = getLanguageDescriptor(pintLang);
		boolean flgR = objL.isHiddenL();
		return flgR;
	}
}
