package com.sos.JSHelper.Options;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.interfaces.IAutoCompleteProposal;
import com.sos.JSHelper.io.Files.JSXMLFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_JSHelper_Messages", defaultLocale = "en")
public class SOSOptionElement extends JSToolBox implements Serializable, ISOSOptions, IAutoCompleteProposal {
	private static final String					constPrefixForEnviromentVariables	= "env:";
	private static final long					serialVersionUID					= -7652466722187678671L;
	public final String							ControlType							= "text";
	private static final String					conClassName						= "JSOptionElement";
	protected static final Logger				logger								= Logger.getLogger(SOSOptionElement.class);
	public static boolean						gflgProcessHashMap					= true;
	public static boolean						gflgCreateShortXML					= false;									// item-based with name and value
																																// attribute
	private Stack<String>						objValueStack						= null;
	private ArrayList<IValueChangedListener>	lstValueChangedListeners			= null;
	public IValueChangedListener				objParentControl					= null;
	protected String							strKey								= "";
	protected String							strValue							= "";
	private String								strDefaultValue						= "";
	private boolean								flgIsDirty							= false;
	protected boolean							flgValue							= false;
	protected JSOptionsClass					objOptions							= null;
	protected Vector<String>					objAliase							= new Vector<String>();
	protected Vector<Object>					objObjectStore						= new Vector<Object>();
	protected static final int					isOptionTypeString					= 0;
	protected static final int					isOptionTypeBoolean					= 1;
	protected static final int					isOptionTypeFileName				= 2;
	protected static final int					isOptionTypeInteger					= 3;
	public static final int						isOptionTypeOptions					= 4;
	protected static final int					isOptionTypeFolder					= 5;
	protected int								intOptionType						= 0;										// 0 = String, 1
																																// = boolean, 2
																																// = File .....
	private boolean								flgIsMandatory						= false;
	private String								strDescription						= "";
	private int									intSize								= 0;										// Size in Bytes
	private String								strTitle							= "";
	private String								strColumnHeader						= "";
	private String								strXMLTagName						= "";
	private String								strFormatString						= "";										// gehürt in
																																// Integer,
																																// Double, etc
	protected JSOptionsClass					objParentClass						= null;
	protected static final String				conNullButMandatory					= "JSO-D-0011";							// "Setting %1$s (%2$s) is mandatory, must be not null.%n";
	protected static final String				conChangedMsg						= "JSO-D-0010";							// "changed from '%1$s' to '%2$s'.";
	protected boolean							flgHideValue						= false;
	protected boolean							flgHideOption						= false;
	protected boolean							isCData								= false;
	public static boolean						flgShowPasswords					= false;

	public void addValueChangedListener(final IValueChangedListener pobjValueChangedListener) {
		if (lstValueChangedListeners == null) {
			lstValueChangedListeners = new ArrayList<IValueChangedListener>();
		}
		lstValueChangedListeners.add(pobjValueChangedListener);
	}

	public void removeValueChangedListener(final IValueChangedListener pobjValueChangedListener) {
		if (lstValueChangedListeners == null) {
			lstValueChangedListeners = new ArrayList<IValueChangedListener>();
		}
		lstValueChangedListeners.remove(pobjValueChangedListener);
	}

	public SOSOptionElement addObject(final Object objO) {
		getObjectStore();
		// TODO check for and avoid  duplicate objects
		objObjectStore.add(objO);
		return this;
	}

	public Vector<Object> getObjectStore() {
		if (objObjectStore == null) {
			objObjectStore = new Vector<Object>();
		}
		return objObjectStore;
	}

	public void changeDefaults(final String pstrValue, final String pstrDefaultValue) {
		strDefaultValue = getValue(pstrDefaultValue);
		this.Value(getValue(pstrValue));
		flgIsDirty = false;
	}

	public void changeDefaults(final int pintValue, final int pintDefaultValue) {
		strDefaultValue = String.valueOf(pintDefaultValue);
		this.Value(String.valueOf(pintValue));
		flgIsDirty = false;
	}

	public SOSOptionElement(final String pstrOptionValue) {
		this(null, "", "", pstrOptionValue, pstrOptionValue, false);
	}

	public SOSOptionElement(final JSOptionsClass pobjParent, final String pstrKey, final String pstrDescription, final String pstrValue,
			final String pstrDefaultValue, final boolean pflgIsMandatory) {
		try {
			this.setMessageResource("com_sos_JSHelper_Messages");
			objParentClass = pobjParent;
			//			if (objParentClass != null) {
			//				this.registerMessageListener(objParentClass.Listener());
			//			}
			strKey = pstrKey;
			strDefaultValue = getValue(pstrDefaultValue);
			this.Description(pstrDescription);
			strTitle = pstrDescription;
			flgIsMandatory = pflgIsMandatory;
			strXMLTagName = pstrKey;
			strColumnHeader = pstrKey;
			this.Value(getValue(pstrValue));
			flgIsDirty = false;
		} // try
		catch (final Exception objException) {
			objException.printStackTrace();
		}
		finally {
			//
		} // finally
	}

	public String getToolTip() {
		String strT = Description();
		strT = strT + "\nKey=  " + getShortKey();
		if (objAliase.size() > 0) {
			strT += ", Alias ";
			for (String strAlias : objAliase) {
				strT += strAlias + ", ";
			}
		}
		if (flgIsMandatory == true) {
			strT += "\nValue is mandatory\n";
		}
		strT = strT + "\nType = " + getClass().getName();
		// TODO add Valuelist
		String[] strVL = getValueList();
		if (strVL.length > 0) {
			strT += "\nValues = " + getStringArrayAsTextLines(strVL);
		}
		return strT;
	}

	private String getStringArrayAsTextLines(final String[] pstrV) {
		String strT = "";
		for (String string : pstrV) {
			strT += "\n    " + string;
		}
		return strT;
	}

	public void CheckMandatory(final boolean pflgIsMandatory) {
		if (pflgIsMandatory) {
			flgIsMandatory = true;
			CheckMandatory();
		}
	}

	/**
		* \brief CheckMandatory - prüft ob eine Option tatsächlich gefüllt ist
		 *
		 * \details
		 *
		* @throws Exception - wird ausgeläst, wenn eine mandatory-Option keinen Wert hat
		 */
	public void CheckMandatory() {
		if (flgIsMandatory) {
			if (this.isEmpty(strValue)) {
				//					this.SignalError(Messages.getMsg("%1$s (%2$s) %3$s", this.strDescription, this.strKey, this.conNullButMandatory));
				String strT = Messages.getMsg(SOSOptionElement.conNullButMandatory, strDescription, strKey);
				logger.error(strT);
				throw new JSExceptionMandatoryOptionMissing(strT);
			}
		}
	} // public void CheckMandatory ()

	/**
	 *
	 * \brief ColumnHeader
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	public String ColumnHeader() {
		if (strColumnHeader == null) {
			strColumnHeader = "";
		}
		return strColumnHeader;
	}

	/**
	 *
	 * \brief ColumnHeader
	 *
	 * \details
	 *
	 * \return JSOptionClass
	 *
	 * @param pstrColumnHeader
	 * @return
	 */
	public SOSOptionElement ColumnHeader(final String pstrColumnHeader) {
		if (pstrColumnHeader != null) {
			strColumnHeader = pstrColumnHeader;
		}
		return this;
	}

	public String createShortXml() {
		String strT = "<item ";
		strT += " name='" + this.getShortXMLTagName() + "'";
		// if (this.isDirty() == true) {
		strT += " value='";
		strT += this.Value().trim();
		strT += "' ";
		// }
		strT += "/>";
		return strT;
	}

	/**
	 *
	 * \brief DefaultValue
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	public String DefaultValue() {
		return strDefaultValue;
	}

	public void DefaultValue(final String pstrValue) {
		strDefaultValue = pstrValue;
	}

	/**
	 * \brief Description - Beschreibung des Datenelements liefern
	 *
	 * \details
	 *
	 * @param pstrDescription
	 * @return String
	 */
	public String Description() {
		if (strDescription == null) {
			strDescription = "";
		}
		return strDescription;
	}

	/**
	 * \brief Description - Beschreibung des Datenelements festlegen
	 *
	 * \details
	 *
	 * @param pstrDescription
	 * @return
	 */
	public SOSOptionElement Description(final String pstrDescription) {
		if (pstrDescription != null) {
			strDescription = pstrDescription;
		}
		return this;
	}

	public String DirtyToString() {
		String strR = "";
		if (!flgHideOption && this.isDirty()) {
			String strV = strValue;
			if (flgHideValue == true) {
				strV = "*****";
			}
			strR = this.getShortKey() + " = " + strV;
		}
		return strR;
	}

	protected void doInit() {
		//
	}

	public boolean isNotEmpty() {

		return isNotEmpty(strValue);

	}

	/**
	 *
	 * \brief equalsIgnoreCase
	 *
	 * \details
	 *
	 * \return boolean
	 *
	 * @param strCompare
	 * @return
	 */
	public boolean equalsIgnoreCase(final String strCompare) {
		if (strValue == null) {
			return false;
		}
		return strValue.equalsIgnoreCase(strCompare);
	}

	public String getControlType() {
		return ControlType;
	}

	/**
	 *
	 * \brief FormatString - liefert den für die Option definierten FormatString
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	public String FormatString() {
		if (strFormatString == null) {
			strFormatString = "";
		}
		return strFormatString;
	}

	/**
	 *
	 * \brief FormatString
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pstrFormatString
	 */
	public void FormatString(final String pstrFormatString) {
		if (pstrFormatString == null) {
			strFormatString = "";
		}
		else {
			strFormatString = pstrFormatString;
		}
	}

	/**
	 *
	 * \brief FormattedValue - Liefert den Wert der Option formatiert
	 *
	 * \details
	 * das Format (die Edit-Maske) wird über die Eigenschaft FormatString
	 * definiert.
	 *
	 * Wenn kein Format-String definiert ist, so wird der Wert als String
	 * zurückgegeben.
	 *
	 * \return String
	 *
	 * @return
	 * @throws Exception
	 */
	public String FormattedValue() throws Exception {
		// nur überschreibbar
		return this.Value();
	}

	public byte[] getBytes() {
		return strValue.getBytes();
	}

	/**
	 *
	 * \brief getKey
	 *
	 * \details
	 *
	 * \return String
	 */
	public String getKey() {
		return strKey;
	}

	public String getShortKey() {
		String strT = strKey;
		int i = strT.indexOf(".");
		if (i > 0) {
			strT = strT.substring(i + 1);
		}
		if (objParentClass != null) {
			String strPrefix = objParentClass.getPrefix();
			if (isNotEmpty(strPrefix)) {
				if (strT.startsWith(strPrefix) == false) {
					if (strPrefix.endsWith("_") == false) {
						strPrefix = strPrefix + "_";
					}
					strT = strPrefix + strT;
				}
			}
		}
		return strT;
	}

	public String getShortXMLTagName() {
		String strT = this.XMLTagName();
		int i = strT.indexOf(".");
		if (i > 0) {
			strT = strT.substring(i + 1);
		}
		return strT;
	}

	public Stack<String> getStack() {
		if (objValueStack == null) {
			objValueStack = new Stack<String>();
		}
		return objValueStack;
	}

	private String getValue(final String pstrValue) {
		String strRet = null;
		if (pstrValue == null) {
			strRet = null;
		}
		else {
			if (pstrValue.toLowerCase().startsWith(constPrefixForEnviromentVariables) == true) {
				String strEnvVarName = pstrValue.substring(4);
				String strEnvVarValue = EnvironmentVariable(strEnvVarName);
				if (isEmpty(strEnvVarValue) == true) {
					strRet = strDefaultValue;
				}
				else {
					strRet = strEnvVarValue;
				}
			}
			else {
				if (pstrValue.contains("${")) {
					int iFrom = pstrValue.indexOf("${");
					int iTo = pstrValue.indexOf("}");
					if (iTo != -1) {
						String strEnvVarName = pstrValue.substring(iFrom + 2, iTo);
						String strEnvVarValue = EnvironmentVariable(strEnvVarName);
						if (strEnvVarValue == null) {
							strEnvVarValue = "";
						}
						strRet = "";
						if (iFrom > 0) {
							strRet = pstrValue.substring(0, iFrom);
						}
						strRet += strEnvVarValue;
						strRet += pstrValue.substring(iTo + 1);
					}
				}
				else {
					strRet = pstrValue;
				}
			}
		}
		return strRet;
	}

	/**
	 *
	*
	* \brief isDefault
	*
	* \details
	* return true, is the actual value of the option is equal to the default value for the option.
	*
	* \return boolean
	*
	 */
	public boolean isDefault() {
		return strValue.equalsIgnoreCase(strDefaultValue);
	}

	/**
	 *
	*
	* \brief isDirty
	*
	* \details
	* return true, if the value of this option was change during the program run
	*
	* \return boolean
	*
	 */
	public boolean isDirty() {
		return flgIsDirty;
	}

	/**
	 *
	 * \brief IsEmpty
	 *
	 * \details
	 * Ist Value-String leer, dann liefert die Methode true
	 *
	 * \return boolean
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean IsEmpty() throws RuntimeException {
		if (strValue != null) {
			return strValue.trim().length() == 0;
		}
		else {
			return true;
		}
	}

	/**
	 * \brief gethideOption
	 *
	 * \details
	 * getter
	 *
	 * @return the hideOption
	 */
	public boolean isHideOption() {
		return flgHideOption;
	}

	/**
	 * \brief gethideValue
	 *
	 * \details
	 * getter
	 *
	 * @return the hideValue
	 */
	public boolean isHideValue() {
		return flgHideValue;
	}

	public Integer ISize() {
		return new Integer(intSize);
	}
	private boolean	gflgProtected	= false;

	/**
	 * \brief Protected
	 *
	 * \details
	 * get Protected
	 *
	 * If an Option is protected, it is not possible to change the value of this option until it is set to unprotected.
	 *
	 * \return boolean
	 *
	 * protect
	 */
	public boolean isProtected() {
		boolean retVal = gflgProtected;
		return retVal;
	}

	/**
	 * \brief Protected
	 *
	 * \details
	 * set Protected
	 *
	 * \return boolean
	 *
	 * protect
	 */
	public SOSOptionElement setProtected(final boolean pflgProtected) {
		gflgProtected = pflgProtected;
		return this;
	}

	/**
	 *
	*
	* \brief isMandatory
	*
	* \details
	*  if an Option is declared as mandator it must have a value assigned, which is not an empty value.
	* \return boolean
	*
	 */
	public boolean isMandatory() {
		return flgIsMandatory;
	}

	/**
	 *
	 * \brief isMandatory
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pflgIsMandatory
	 */
	public void isMandatory(final boolean pflgIsMandatory) {
		flgIsMandatory = pflgIsMandatory;
	}

	public boolean isNotDirty() {
		return !flgIsDirty;
	}

	public boolean IsNotEmpty() throws RuntimeException {
		return !this.IsEmpty();
	}

	public boolean IsNull() {
		return strValue == null;
	}

	public void loadValues() {
		Preferences objP = objParentClass.getPreferenceStore();
		if (objP != null) {
			String strT = objP.get(getKey().toLowerCase(), strDefaultValue);
			Value(strT);
		}
	}

	public void storeValues() {
		Preferences objP = objParentClass.getPreferenceStore();
		if (objP != null) {
			objP.put(getKey().toLowerCase(), Value());
		}
	}

	/**
	 *
	 * \brief MapValue - Wert der Option aus der HashTable übernehmen
	 *
	 * \details
	 * Mit dieser Methode wird aus der HashTable für den für diese Option
	 * definierten Key der eingestellte Wert übernommen.
	 *
	 * Ist der gesuchte Key, und damit der Wert, nicht in der HashTable,
	 * so wird der Default-Wert zugewiesen.
	 * \return void
	 *
	 * @throws Exception
	 */
	public void MapValue() { 
		/*
		 * \todo Wenn abgeleitete Klassen in der Value()-Methode prüfungen auf gültige Werte haben
		 * und ein "leerer" String ein ungültiger Wert ist, dann wird dieser ungültige Wert nicht erkannt.
		 */
		if (this.isEmpty(strKey) == false && objParentClass != null) {
			String strV = objParentClass.getItem(strKey, null);
			if (strV == null) {
				for (String strAlias : objAliase) {
					strV = objParentClass.getItem(strAlias, null);
					if (strV != null) {
						break;
					}
				}
			}
			// TODO check for global prefix
			if (strV == null) {
				strV = strDefaultValue;
			}
			else { // warum auf default setzen wenn er nicht in der hashmap ist? Dann unveründert lassen
				this.Value(strV);
				this.setProtected(JSOptionsClass.flgIncludeProcessingInProgress);
			}
			/*
			 * TODO Werte-Trenner als Eigenschaft des JSOptionElements statt konstant
			 * \todo Werte-Trenner als Eigenschaft des JSOptionElements statt konstant
			 */
			if (intOptionType == isOptionTypeOptions) {
				this.Value(objParentClass.getIndexedItem(strKey, this.Description(), ";"));
			}
		}
	}

	public String OptionalQuotedValue() {
		String strR = strValue;
		if (strR.indexOf(" ") > -1) {
			strR = QuotedValue(strR);
		}
		return strR;
	}

	public int OptionType() {
		return intOptionType;
	}

	/**
	 *
	 * \brief OptionType
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pintOptionType
	 */
	public void OptionType(final int pintOptionType) {
		intOptionType = pintOptionType;
	}

	public void pop() {
		String strT = getStack().pop();
		this.Value(strT);
	}

	public void push() {
		getStack().push(strValue);
	}

	/**
	 *
	 * \brief QuotedValue
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	public String QuotedValue() {
		return this.QuotedValue(strValue);
	}

	/**
	 *
	 * \brief QuotedValue
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @param pstrValue
	 * @return
	 */
	public String QuotedValue(final String pstrValue) {
		String strRet = "\"" + pstrValue.replaceAll("\"", "\"\"") + "\"";
		return strRet;
	}

	private void raiseValueChangedListener() {
		if (lstValueChangedListeners != null) {
			for (IValueChangedListener objValueChangedListener : lstValueChangedListeners) {
				objValueChangedListener.ValueHasChanged(this);
			}
		}
	}

	public void Set(final SOSOptionElement pobjOption) {
		if (pobjOption.isDirty()) {
			this.Value(pobjOption.Value());
			this.setProtected(pobjOption.isProtected());
		}
	}

	/**
	 *
	*
	* \brief SetIfNotDirty
	*
	* \details
	* set the value of this option to the value of the given option, if this option is not dirty
	* (e.g. has its initial state)
	* \return void
	*
	 */
	public void SetIfNotDirty(final SOSOptionElement pobjOption) {
		if (this.isDirty() == false) {
			this.Set(pobjOption);
		}
	}

	public SOSOptionElement SetAlias(final String pstrAliasKey) {
		objAliase.add(pstrAliasKey);
		return this;
	}

	public SOSOptionElement SetAlias(final String... pstrAliasKey) {
		for (String string : pstrAliasKey) {
			objAliase.add(string);
		}
		return this;
	}

	public void setDirty() {
		flgIsDirty = true;
	}

	/**
	 * \brief sethideOption -
	 *
	 * \details
	 * setter
	 *
	 * @param hideOption the value for hideOption to set
	 */
	public void setHideOption(final boolean hideOption) {
		flgHideOption = hideOption;
	}

	/**
	 * \brief sethideValue -
	 *
	 * \details
	 * setter
	 *
	 * @param hideValue the value for hideValue to set
	 */
	public void setHideValue(final boolean hideValue) {
		flgHideValue = hideValue;
	}

	public void setNotDirty() {
		flgIsDirty = false;
	}

	public void setNull() {
		strValue = null;
	}

	public String setPrefix(final String strPrefix) {
		String strT = strKey;
		if (strT.contains(strPrefix) == false) {
			int i = strT.indexOf(".");
			if (i > 0) {
				strT = strT.replaceFirst("\\.", "." + strPrefix);
			}
			else {
				strT = strPrefix + strT;
			}
			strKey = strT;
		}
		return strT;
	}

	/**
	 * \brief Size - Grüüe des Datenelements liefern
	 *
	 * \details
	 *
	 * @param pintSize
	 * @return String
	 */
	public int Size() {
		return intSize;
	}

	/**
	 * \brief Size - Grüüe des Datenelements festlegen
	 *
	 * \details
	 *
	 * @param pintSize
	 * @return
	 */
	public SOSOptionElement Size(final int pintSize) {
		intSize = pintSize;
		return this;
	}

	public SOSOptionElement Size(final Integer pintSize) {
		intSize = pintSize;
		return this;
	}

	/**
	 *
	 * \brief String to Boolean
	 *
	 * \details
	 *
	 * \return boolean
	 *
	 * @param pstrS
	 */
	public boolean String2Bool() {
		boolean flgT = false;
		String pstrVal = strValue;
		if (isNotEmpty(pstrVal)) {
			if (pstrVal.equals("1") || pstrVal.equalsIgnoreCase("y") || pstrVal.equalsIgnoreCase("yes") || pstrVal.equalsIgnoreCase("j")
					|| pstrVal.equalsIgnoreCase("on") || pstrVal.equalsIgnoreCase("true") || pstrVal.equalsIgnoreCase("wahr")) {
				flgT = true;
			}
		}
		return flgT;
	}

	/**
	 * \brief Title - Titel des Datenelements liefern
	 *
	 * \details
	 *
	 * @param pstrTitle
	 * @return
	 */
	public String Title() {
		if (strTitle == null) {
			strTitle = "";
		}
		return strTitle;
	}

	/**
	 * \brief Title - Titel des Datenelements festlegen
	 *
	 * \details
	 *
	 * @param pstrTitle
	 * @return
	 */
	public SOSOptionElement Title(final String pstrTitle) {
		if (pstrTitle != null) {
			strTitle = pstrTitle;
		}
		return this;
	}
	/**
	 *
	*
	* \brief toKeyValuePair
	*
	* \details
	* Get, if dirty, as key=value pair, e.g. for ini file
	* \return String
	*
	 */
	private final boolean	flgSelecteDirtyOnly	= true;

	public String toKeyValuePair(final String pstrAlternatePrefix) {
		String strRet = "";
		if (flgSelecteDirtyOnly == false || isDirty() == true) {
			if (IsNotEmpty() /* && isDefault() == false */) {
				if (isNotEmpty(pstrAlternatePrefix)) {
					strRet = pstrAlternatePrefix + "_";
				}
				strRet += this.getShortKey() + "=" + strValue;
			}
		}
		return strRet;
	}

	public String toCommandLine() {
		String strRet = "";
		//oh 2014-10-29 add isMandatory(), otherwise https://change.sos-berlin.com/browse/SOSFTP-220
		if (IsNotEmpty() && isDirty() == true && (isMandatory() || isDefault() == false)) {
			strRet = "-" + this.getShortKey() + "=" + OptionalQuotedValue() + " ";
		}
		return strRet;
	}
	
	//https://change.sos-berlin.com/browse/JADE-238
	public String toQuotedCommandLine() {
		String strRet = "";
		if (IsNotEmpty() && isDirty() == true && (isMandatory() || isDefault() == false)) {
			strRet = "-" + this.getShortKey() + "=" + QuotedValue() + " ";
		}
		return strRet;
	}

	/**
	 *
	 * \brief toOut
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 * @throws Exception
	 */
	public String toOut() throws Exception {
		String strT = "";
		strT = String.format("%1$s %2$s: %3$s \n", strTitle, strDescription, this.Value());
		return strT;
	}


	/**
	 *
	 * \brief toString
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public String toString() {
		String strR = "";
		if (flgHideOption == false) {
			String strV = strValue;
			if (flgHideValue == true) {
				strV = "*****";
			}
			strR = strKey + " (" + strDescription + "): " + strV;
		}
		return strR;
	}

	/**
	 * \brief toXml - XML-Tag mit dem Wert der Option liefern
	 *
	 * \details
	 *
	 * @return String - den Wert der Option im XML-Tag
	 * @throws Exception
	 */
	public String toXml() throws Exception {
		String strT = "";
		if (gflgCreateShortXML == true) {
			strT = createShortXml();
		}
		else {
			strT = "<" + this.XMLTagName();
			strT += " mandatory=" + QuotedValue(boolean2String(flgIsMandatory));
			if (isNotEmpty(strDefaultValue)) {
				strT += " default=" + QuotedValue(strDefaultValue);
			}
			if (isNotEmpty(strTitle)) {
				strT += " title=" + QuotedValue(strTitle);
			}
			strT += ">";
			if (this.Value().length() > 0) {
				if (isCData) {
					strT += "<![CDATA[" + this.FormattedValue() + "]]>";
				}
				else {
					strT += this.Value();
				}
			}
			strT += "</" + this.XMLTagName() + ">";
		}
		return strT;
	}

	/**
	 *
	 * \brief toXml
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pobjXMLFile
	 * @throws Exception
	 */
	public void toXml(final JSXMLFile pobjXMLFile) throws Exception {
		pobjXMLFile.WriteLine(this.toXml());
	}

	public String toXml(final String pstrTagName) throws Exception {
		String strRet = " ";
		String strT = this.XMLTagName();
		try {
			this.XMLTagName(pstrTagName);
			strRet = toXml();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			this.XMLTagName(strT);
		}
		return strRet;
	}

	/**
	 * \brief Value - Wert der Option liefern
	 *
	 * \details
	 *
	 * @param pstrValue
	 * @return
	 */
	public String Value() throws RuntimeException {
		if (strValue == null) {
			strValue = "";
		}
		return strValue;
	}

	/**
	 * \brief Value - Wert des Datenelements festlegen
	 *
	 * \details
	 *
	 * @param pstrValue
	 * @return
	 * @throws Exception
	 */
	@Override
	public void Value(final String pstrValue) {
		if (flgIsMandatory) {
			if (pstrValue == null) {
				if (gflgProcessHashMap == false) {
					// this.SignalError(Messages.getMsg(SOSOptionElement.conNullButMandatory, this.strTitle, this.strKey));
					// logger.error(Messages.getMsg(SOSOptionElement.conNullButMandatory, this.strTitle, this.strKey));
				}
				else {
					return; // to avoid to overwrite a previous assigned value
				}
			}
		}
		if (pstrValue != null) {
			/*
			 * \todo über den regexp prüfen, ob die Werte syntaktisch korrekt sind
			 */
			if (objParentClass != null) {
				final String strTemp = objParentClass.SubstituteVariables(pstrValue);
				// if (strTemp != null && strValue != null && strTemp.trim().equals(strValue.trim()) == false) {
				// logger.debug(Messages.getMsg(this.conChangedMsg, this.strValue, strTemp, this.strTitle));
				// }
				// this.SignalDebug(String.format(this.conChangedMsg, this.strValue, strTemp, this.strTitle));
				Properties objP = objParentClass.getTextProperties();
				objP.put(getShortKey(), pstrValue);
			}
			changeValue(pstrValue);
			addProposal(pstrValue);
		}
		else {
			changeValue("");
		}
		// logger.debug(conClassName + ", key = " + strKey + ", value = " + strValue);
	}

	private void changeValue(final String pstrValue) {
		// Das "if" macht jede isDirty-Abfrage unsicher
		//if (pstrValue != null && pstrValue.equalsIgnoreCase(strValue) == false) {
			strValue = pstrValue;
			setDirty();
			raiseValueChangedListener();
		//}
	}

	/**
	 * \brief XMLTagName - XML-TagName der Option liefern
	 *
	 * \details
	 *
	 * @param pstrXMLTagName
	 * @return String
	 */
	public String XMLTagName() {
		if (strXMLTagName == null) {
			strXMLTagName = "";
		}
		return strXMLTagName;
	}

	/**
	 * \brief XMLTagName - XML-TagName des Datenelements festlegen
	 *
	 * \details
	 *
	 * @param pstrXMLTagName
	 * @return
	 */
	public SOSOptionElement XMLTagName(final String pstrXMLTagName) {
		if (pstrXMLTagName != null) {
			strXMLTagName = pstrXMLTagName;
		}
		return this;
	}

	/**
	 *
	*
	* \brief getValueList
	*
	* \details
	*
	* \return String[]
	*
	 */
	public String[] getValueList() {
		return new String[] {};
	}

	public String getUserDir() {
		String strT = System.getProperty("user.dir");
		if (objParentClass != null) {
			strT = objParentClass.BaseDirectory.Value();
		}
		return strT;
	}
	private static final HashMap<String, String>	defaultProposals	= new HashMap<>();

	@Override
	public void addProposal(final String pstrProposal) {
		if (pstrProposal != null && pstrProposal.trim().length() > 0) {
			defaultProposals.put(pstrProposal, pstrProposal);
		}
	}

	@Override
	public String[] getAllProposals(final String text) {
		String[] proposals = defaultProposals.keySet().toArray(new String[0]);
		return proposals;
	}

}
