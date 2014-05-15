/**
 *
 */
package com.sos.scheduler.model;

import org.apache.log4j.Logger;

import com.sos.JSHelper.interfaces.ISOSComboItem;

/**
 * @author KB
 *
 */
public class LanguageDescriptor implements ISOSComboItem  {
	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(this.getClass());
	/**
	 *
	 */
	private String			strLanguageName		= "";
	private int				intLanguageNumber	= -1;
	private boolean			flgIsHiddenL		= false;
	private String			strClassName		= "";
	private String			strDocuFileName		= "";
	private String			strExternalLanguage	= "";
	private boolean IsAPIL = true;
	private boolean IsMonitorL = false;

	public LanguageDescriptor(final String pstrLanguageName, final int pintNumber, final boolean pflgIsHiddenL, final String pstrExternalLanguage,
			final String pstrClassName, final String pstrDocuFileName, final boolean pflgIsAPIL, final boolean pflgIsMonitorL) {

		strLanguageName = pstrLanguageName;
		intLanguageNumber = pintNumber;
		flgIsHiddenL = pflgIsHiddenL;
		strClassName = pstrClassName;
		strDocuFileName = pstrDocuFileName;
		strExternalLanguage = pstrExternalLanguage;  // e.g. SSH is internal, Java is external for SSH
		IsAPIL = pflgIsAPIL;
		IsMonitorL = pflgIsMonitorL;
	}

	public String getExternalLanguage() {
		return strExternalLanguage;
	}

	public void setExternalLanguage(final String externalLanguage) {
		strExternalLanguage = externalLanguage;
	}

	public String getLanguageName() {
		return strLanguageName;
	}

	public void setLanguageName(final String languageName) {
		strLanguageName = languageName;
	}

	public int getLanguageNumber() {
		return intLanguageNumber;
	}

	public void setLanguageNumber(final int languageNumber) {
		intLanguageNumber = languageNumber;
	}

	public boolean isHiddenL() {
		return flgIsHiddenL;
	}

	public void setHiddenL(final boolean isHiddenL) {
		flgIsHiddenL = isHiddenL;
	}

	public String getClassName() {
		return strClassName;
	}

	public void setClassName(final String className) {
		strClassName = className;
	}

	public String getDocuFileName() {
		return strDocuFileName;
	}

	public void setDocuFileName(final String docuFileName) {
		strDocuFileName = docuFileName;
	}

	public boolean isIsAPIL() {
		return IsAPIL;
	}

	public void setIsAPIL(final boolean isAPIL) {
		IsAPIL = isAPIL;
	}

	public boolean isIsMonitorL() {
		return IsMonitorL;
	}

	public void setIsMonitorL(final boolean isMonitorL) {
		IsMonitorL = isMonitorL;
	}

	@Override
	public String getText() {
		return getLanguageName();
	}

	@Override
	public int getIndex() {
		return getLanguageNumber();
	}



}
