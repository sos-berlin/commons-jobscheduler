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

    protected static final String conNullButMandatory = "JSO-D-0011";
    protected static final String conChangedMsg = "JSO-D-0010";
    protected static final Logger logger = Logger.getLogger(SOSOptionElement.class);
    protected static final int isOptionTypeString = 0;
    protected static final int isOptionTypeBoolean = 1;
    protected static final int isOptionTypeFileName = 2;
    protected static final int isOptionTypeInteger = 3;
    protected static final int isOptionTypeFolder = 5;
    protected boolean flgHideValue = false;
    protected boolean flgHideOption = false;
    protected boolean isCData = false;
    protected JSOptionsClass objParentClass = null;
    protected String strKey = "";
    protected String strValue = "";
    protected boolean flgValue = false;
    protected JSOptionsClass objOptions = null;
    protected Vector<String> objAliase = new Vector<String>();
    protected Vector<Object> objObjectStore = new Vector<Object>();
    protected int intOptionType = 0;
    private static final String constPrefixForEnviromentVariables = "env:";
    private static final long serialVersionUID = -7652466722187678671L;
    private static final HashMap<String, String> defaultProposals = new HashMap<>();
    private final boolean flgSelecteDirtyOnly = true;
    private boolean gflgProtected = false;
    private Stack<String> objValueStack = null;
    private ArrayList<IValueChangedListener> lstValueChangedListeners = null;
    private String strDefaultValue = "";
    private boolean flgIsDirty = false;
    private boolean flgIsMandatory = false;
    private String strDescription = "";
    private int intSize = 0;
    private String strTitle = "";
    private String strColumnHeader = "";
    private String strXMLTagName = "";
    private String strFormatString = "";
    public final String ControlType = "text";
    public static boolean gflgProcessHashMap = true;
    public static boolean gflgCreateShortXML = false;
    public IValueChangedListener objParentControl = null;
    public static final int isOptionTypeOptions = 4;
    public static boolean flgShowPasswords = false;

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

    public SOSOptionElement(final String pstrFolderName) {
        this.setMessageResource("com_sos_JSHelper_Messages");
        objParentClass = null;
        strKey = "";
        this.Description("description");
        this.Value(pstrFolderName);
        flgIsMandatory = false;
        flgIsDirty = false;
    }

    public SOSOptionElement(final JSOptionsClass pobjParent, final String pstrKey, final String pstrDescription, final String pstrValue,
            final String pstrDefaultValue, final boolean pflgIsMandatory) {
        try {
            this.setMessageResource("com_sos_JSHelper_Messages");
            objParentClass = pobjParent;
            strKey = pstrKey;
            strDefaultValue = getValue(pstrDefaultValue);
            this.Description(pstrDescription);
            strTitle = pstrDescription;
            flgIsMandatory = pflgIsMandatory;
            strXMLTagName = pstrKey;
            strColumnHeader = pstrKey;
            this.Value(getValue(pstrValue));
            flgIsDirty = false;
        } catch (final Exception objException) {
            logger.error(objException.getMessage(), objException);
        }
    }

    public String getToolTip() {
        String strT = Description();
        strT = strT + "\nKey=  " + getShortKey();
        if (!objAliase.isEmpty()) {
            strT += ", Alias ";
            for (String strAlias : objAliase) {
                strT += strAlias + ", ";
            }
        }
        if (flgIsMandatory) {
            strT += "\nValue is mandatory\n";
        }
        strT = strT + "\nType = " + getClass().getName();
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

    public void CheckMandatory() {
        try {
            if (flgIsMandatory && this.isEmpty(strValue)) {
                this.SignalError(Messages.getMsg(SOSOptionElement.conNullButMandatory, strDescription, strKey));
            }
        } catch (final Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    public String ColumnHeader() {
        if (strColumnHeader == null) {
            strColumnHeader = "";
        }
        return strColumnHeader;
    }

    public SOSOptionElement ColumnHeader(final String pstrColumnHeader) {
        if (pstrColumnHeader != null) {
            strColumnHeader = pstrColumnHeader;
        }
        return this;
    }

    public String createShortXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<item ").append(" name='").append(this.getShortXMLTagName()).append("'").append(" value='").append(this.Value().trim()).append(
                "' ").append("/>");
        return sb.toString();
    }

    public String DefaultValue() {
        return strDefaultValue;
    }

    public void DefaultValue(final String pstrValue) {
        strDefaultValue = pstrValue;
    }

    public String Description() {
        if (strDescription == null) {
            strDescription = "";
        }
        return strDescription;
    }

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
            if (flgHideValue) {
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

    public boolean equalsIgnoreCase(final String strCompare) {
        if (strValue == null) {
            return false;
        }
        return strValue.equalsIgnoreCase(strCompare);
    }

    public String getControlType() {
        return ControlType;
    }

    public String FormatString() {
        if (strFormatString == null) {
            strFormatString = "";
        }
        return strFormatString;
    }

    public void FormatString(final String pstrFormatString) {
        if (pstrFormatString == null) {
            strFormatString = "";
        } else {
            strFormatString = pstrFormatString;
        }
    }

    public String FormattedValue() throws Exception {
        return this.Value();
    }

    public byte[] getBytes() {
        return strValue.getBytes();
    }

    public String getKey() {
        return strKey;
    }

    public String getShortKey() {
        String strT = strKey;
        int i = strT.indexOf(".");
        if (i > 0) {
            strT = strT.substring(i + 1);
            if (objParentClass != null) {
                String strPrefix = objParentClass.getPrefix();
                if (isNotEmpty(strPrefix) && !strT.startsWith(strPrefix)) {
                    strT = strPrefix + "_" + strT;
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
        } else {
            if (pstrValue.toLowerCase().startsWith(constPrefixForEnviromentVariables)) {
                String strEnvVarName = pstrValue.substring(4);
                String strEnvVarValue = environmentVariable(strEnvVarName);
                if (isEmpty(strEnvVarValue)) {
                    strRet = System.getProperty(strEnvVarName);
                    if (isEmpty(strRet)) {
                        strRet = strDefaultValue;
                    }
                } else {
                    strRet = strEnvVarValue;
                }
            } else {
                if (pstrValue.contains("${")) {
                    int iFrom = pstrValue.indexOf("${");
                    int iTo = pstrValue.indexOf("}");
                    if (iTo != -1) {
                        String strEnvVarName = pstrValue.substring(iFrom + 2, iTo);
                        String strEnvVarValue = environmentVariable(strEnvVarName);
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
                } else {
                    strRet = pstrValue;
                }
            }
        }
        return strRet;
    }

    public boolean isDefault() {
        return strValue.equalsIgnoreCase(strDefaultValue);
    }

    public boolean isDirty() {
        return flgIsDirty;
    }

    public boolean IsEmpty() throws RuntimeException {
        if (strValue != null) {
            return strValue.trim().isEmpty();
        } else {
            return true;
        }
    }

    public boolean isHideOption() {
        return flgHideOption;
    }

    public boolean isHideValue() {
        return flgHideValue;
    }

    public Integer ISize() {
        return new Integer(intSize);
    }

    public boolean isProtected() {
        return gflgProtected;
    }

    public SOSOptionElement setProtected(final boolean pflgProtected) {
        gflgProtected = pflgProtected;
        return this;
    }

    public boolean isMandatory() {
        return flgIsMandatory;
    }

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
            Value(objP.get(getKey().toLowerCase(), strDefaultValue));
        }
    }

    public void storeValues() {
        Preferences objP = objParentClass.getPreferenceStore();
        if (objP != null) {
            objP.put(getKey().toLowerCase(), Value());
        }
    }

    public void MapValue() {
        if (!this.isEmpty(strKey)) {
            String strV = objParentClass.getItem(strKey, null);
            if (strV == null) {
                for (String strAlias : objAliase) {
                    strV = objParentClass.getItem(strAlias, null);
                    if (strV != null) {
                        break;
                    }
                }
            }
            if (strV == null) {
                strV = strDefaultValue;
            } else {
                this.Value(strV);
                this.setProtected(JSOptionsClass.flgIncludeProcessingInProgress);
            }
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

    public void OptionType(final int pintOptionType) {
        intOptionType = pintOptionType;
    }

    public void pop() {
        this.Value(getStack().pop());
    }

    public void push() {
        getStack().push(strValue);
    }

    public String QuotedValue() {
        return this.QuotedValue(strValue);
    }

    public String QuotedValue(final String pstrValue) {
        return "\"" + pstrValue.replaceAll("\"", "\"\"") + "\"";
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

    public void SetIfNotDirty(final SOSOptionElement pobjOption) {
        if (!this.isDirty()) {
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

    public void setHideOption(final boolean hideOption) {
        flgHideOption = hideOption;
    }

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
        int i = strT.indexOf(".");
        if (i > 0) {
            strT = strT.replaceFirst("\\.", "." + strPrefix);
        } else {
            strT = strPrefix + strT;
        }
        strKey = strT;
        return strT;
    }

    public int Size() {
        return intSize;
    }

    public SOSOptionElement Size(final int pintSize) {
        intSize = pintSize;
        return this;
    }

    public SOSOptionElement Size(final Integer pintSize) {
        intSize = pintSize;
        return this;
    }

    public boolean String2Bool() {
        boolean flgT = false;
        String pstrVal = strValue;
        if (isNotEmpty(pstrVal)
                && ("1".equals(pstrVal) || "y".equalsIgnoreCase(pstrVal) || "yes".equalsIgnoreCase(pstrVal) || "j".equalsIgnoreCase(pstrVal)
                        || "on".equalsIgnoreCase(pstrVal) || "true".equalsIgnoreCase(pstrVal) || "wahr".equalsIgnoreCase(pstrVal))) {
            flgT = true;
        }
        return flgT;
    }

    public String stripQuotes(final String pstrS) {
        String strR = pstrS;
        if ("\"".equals(pstrS.substring(0, 1)) && "\"".equals(pstrS.substring(pstrS.length() - 1))) {
            strR = pstrS.substring(1, pstrS.length() - 1);
        }
        return strR;
    }

    public String Title() {
        if (strTitle == null) {
            strTitle = "";
        }
        return strTitle;
    }

    public SOSOptionElement Title(final String pstrTitle) {
        if (pstrTitle != null) {
            strTitle = pstrTitle;
        }
        return this;
    }

    public String toKeyValuePair(final String pstrAlternatePrefix) {
        String strRet = "";
        if ((!flgSelecteDirtyOnly || isDirty()) && IsNotEmpty()) {
            if (isNotEmpty(pstrAlternatePrefix)) {
                strRet = pstrAlternatePrefix + "_";
            }
            strRet += this.getShortKey() + "=" + strValue;
        }
        return strRet;
    }

    public String toCommandLine() {
        String strRet = "";
        if (IsNotEmpty() && isDirty() && (isMandatory() || !isDefault())) {
            strRet = "-" + this.getShortKey() + "=" + OptionalQuotedValue() + " ";
        }
        return strRet;
    }

    public String toQuotedCommandLine() {
        String strRet = "";
        if (IsNotEmpty() && isDirty() && (isMandatory() || !isDefault())) {
            strRet = "-" + this.getShortKey() + "=" + QuotedValue() + " ";
        }
        return strRet;
    }

    public String toOut() throws Exception {
        return String.format("%1$s %2$s: %3$s \n", strTitle, strDescription, this.Value());
    }

    @Override
    public String toString() {
        String strR = "";
        if (!flgHideOption) {
            String strV = strValue;
            if (flgHideValue) {
                strV = "*****";
            }
            strR = strKey + " (" + strDescription + "): " + strV;
        }
        return strR;
    }

    public String toXml() throws Exception {
        String strT = "";
        if (gflgCreateShortXML) {
            strT = createShortXml();
        } else {
            strT = "<" + this.XMLTagName();
            strT += " mandatory=" + QuotedValue(boolean2String(flgIsMandatory));
            if (isNotEmpty(strDefaultValue)) {
                strT += " default=" + QuotedValue(strDefaultValue);
            }
            if (isNotEmpty(strTitle)) {
                strT += " title=" + QuotedValue(strTitle);
            }
            strT += ">";
            if (!this.Value().isEmpty()) {
                if (isCData) {
                    strT += "<![CDATA[" + this.FormattedValue() + "]]>";
                } else {
                    strT += this.Value();
                }
            }
            strT += "</" + this.XMLTagName() + ">";
        }
        return strT;
    }

    public void toXml(final JSXMLFile pobjXMLFile) throws Exception {
        pobjXMLFile.WriteLine(this.toXml());
    }

    public String toXml(final String pstrTagName) throws Exception {
        String strRet = " ";
        String strT = this.XMLTagName();
        try {
            this.XMLTagName(pstrTagName);
            strRet = toXml();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            this.XMLTagName(strT);
        }
        return strRet;
    }

    public String Value() throws RuntimeException {
        if (strValue == null) {
            strValue = "";
        }
        return strValue;
    }

    @Override
    public void Value(final String pstrValue) {
        if (flgIsMandatory && pstrValue == null && gflgProcessHashMap) {
            return;
        }
        if (pstrValue != null) {
            if (objParentClass != null) {
                final String strTemp = objParentClass.SubstituteVariables(pstrValue);
                Properties objP = objParentClass.getTextProperties();
                objP.put(getShortKey(), pstrValue);
            }
            changeValue(pstrValue);
            addProposal(pstrValue);
        } else {
            changeValue("");
        }
    }

    private void changeValue(final String pstrValue) {
        strValue = pstrValue;
        setDirty();
        raiseValueChangedListener();
    }

    public String XMLTagName() {
        if (strXMLTagName == null) {
            strXMLTagName = "";
        }
        return strXMLTagName;
    }

    public SOSOptionElement XMLTagName(final String pstrXMLTagName) {
        if (pstrXMLTagName != null) {
            strXMLTagName = pstrXMLTagName;
        }
        return this;
    }

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

    @Override
    public void addProposal(final String pstrProposal) {
        if (pstrProposal != null && !pstrProposal.trim().isEmpty()) {
            defaultProposals.put(pstrProposal, pstrProposal);
        }
    }

    @Override
    public String[] getAllProposals(final String text) {
        String[] proposals = defaultProposals.keySet().toArray(new String[0]);
        return proposals;
    }

}