package com.sos.JSHelper.Options;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.interfaces.IAutoCompleteProposal;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_JSHelper_Messages", defaultLocale = "en")
public class SOSOptionElement extends JSToolBox implements Serializable, ISOSOptions, IAutoCompleteProposal {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOptionElement.class);

    public static boolean gflgProcessHashMap = true;
    public static boolean gflgCreateShortXML = false;
    public static final int isOptionTypeOptions = 4;
    public static boolean flgShowPasswords = false;
    public final String ControlType = "text";
    public IValueChangedListener objParentControl = null;

    protected static final int isOptionTypeString = 0;
    protected static final int isOptionTypeBoolean = 1;
    protected static final int isOptionTypeFileName = 2;
    protected static final int isOptionTypeInteger = 3;
    protected static final int isOptionTypeFolder = 5;
    protected JSOptionsClass objParentClass = null;
    protected JSOptionsClass objOptions = null;
    protected Vector<String> objAliase = new Vector<String>();
    protected Vector<Object> objObjectStore = new Vector<Object>();
    protected String strKey = "";
    protected String strValue = "";
    protected int intOptionType = 0;
    protected boolean flgValue = false;

    private static boolean flgIncludeProcessingInProgress = false;
    private static final String constPrefixForEnviromentVariables = "env:";
    private static final long serialVersionUID = -7652466722187678671L;
    private static final HashMap<String, String> defaultProposals = new HashMap<>();
    private static final String conNullButMandatory = "JSO-D-0011";
    private Stack<String> objValueStack = null;
    private ArrayList<IValueChangedListener> lstValueChangedListeners = null;
    private String strDefaultValue = "";
    private String strDescription = "";
    private String strTitle = "";
    private String strColumnHeader = "";
    private String strXMLTagName = "";
    private String strFormatString = "";

    private final boolean flgSelecteDirtyOnly = true;
    private boolean flgHideValue = false;
    private boolean flgHideOption = false;
    private boolean gflgProtected = false;
    private boolean flgIsDirty = false;
    private boolean flgIsMandatory = false;
    private int intSize = 0;

    public SOSOptionElement(final String pstrFolderName) {
        this.setMessageResource("com_sos_JSHelper_Messages");
        objParentClass = null;
        strKey = "";
        this.description("description");
        this.setValue(pstrFolderName);
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
            this.description(pstrDescription);
            strTitle = pstrDescription;
            flgIsMandatory = pflgIsMandatory;
            strXMLTagName = pstrKey;
            strColumnHeader = pstrKey;
            this.setValue(getValue(pstrValue));
            flgIsDirty = false;
        } catch (final Exception objException) {
            LOGGER.error(objException.toString(), objException);
        }
    }

    public SOSOptionElement addObject(final Object objO) {
        getObjectStore();
        objObjectStore.add(objO);
        return this;
    }

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

    public Vector<Object> getObjectStore() {
        if (objObjectStore == null) {
            objObjectStore = new Vector<Object>();
        }
        return objObjectStore;
    }

    public void changeDefaults(final String pstrValue, final String pstrDefaultValue) {
        strDefaultValue = getValue(pstrDefaultValue);
        this.setValue(getValue(pstrValue));
        flgIsDirty = false;
    }

    public void changeDefaults(final int pintValue, final int pintDefaultValue) {
        strDefaultValue = String.valueOf(pintDefaultValue);
        this.setValue(String.valueOf(pintValue));
        flgIsDirty = false;
    }

    public String getToolTip() {
        String strT = getDescription();
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

    public void checkMandatory(final boolean pflgIsMandatory) {
        if (pflgIsMandatory) {
            flgIsMandatory = true;
            checkMandatory();
        }
    }

    public void checkMandatory() {
        try {
            if (flgIsMandatory && this.isEmpty(strValue)) {
                this.signalError(Messages.getMsg(SOSOptionElement.conNullButMandatory, strDescription, strKey));
            }
        } catch (final Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    public String getColumnHeader() {
        if (strColumnHeader == null) {
            strColumnHeader = "";
        }
        return strColumnHeader;
    }

    public SOSOptionElement columnHeader(final String pstrColumnHeader) {
        if (pstrColumnHeader != null) {
            strColumnHeader = pstrColumnHeader;
        }
        return this;
    }

    public String createShortXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<item ").append(" name='").append(this.getShortXMLTagName()).append("'").append(" value='").append(this.getValue().trim()).append(
                "' ").append("/>");
        return sb.toString();
    }

    public String getDefaultValue() {
        return strDefaultValue;
    }

    public void setDefaultValue(final String pstrValue) {
        strDefaultValue = pstrValue;
    }

    public String getDescription() {
        if (strDescription == null) {
            strDescription = "";
        }
        return strDescription;
    }

    public SOSOptionElement description(final String pstrDescription) {
        if (pstrDescription != null) {
            strDescription = pstrDescription;
        }
        return this;
    }

    public String getDirtyToString() {
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
        return !this.IsEmpty();
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

    public String getFormatString() {
        if (strFormatString == null) {
            strFormatString = "";
        }
        return strFormatString;
    }

    public void setFormatString(final String pstrFormatString) {
        if (pstrFormatString == null) {
            strFormatString = "";
        } else {
            strFormatString = pstrFormatString;
        }
    }

    public String getFormattedValue() throws Exception {
        return this.getValue();
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
        String strT = this.getXMLTagName();
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

    public Integer getIntegerSize() {
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

    public boolean isNull() {
        return strValue == null;
    }

    public void loadValues() {
        Preferences objP = objParentClass.getPreferenceStore();
        if (objP != null) {
            setValue(objP.get(getKey().toLowerCase(), strDefaultValue));
        }
    }

    public void storeValues() {
        Preferences objP = objParentClass.getPreferenceStore();
        if (objP != null) {
            objP.put(getKey().toLowerCase(), getValue());
        }
    }

    public void mapValue() {
        if (!this.isEmpty(strKey)) {
            String value = objParentClass.getItem(strKey, null);
            if (value == null) {
                for (String alias : objAliase) {
                    value = objParentClass.getItem(alias, null);
                    if (value != null) {
                        break;
                    }
                }
            }
            if (value == null) {
                value = strDefaultValue;
            } else {
                this.setValue(value);
                this.setProtected(flgIncludeProcessingInProgress);
            }
            if (intOptionType == isOptionTypeOptions) {
                this.setValue(objParentClass.getIndexedItem(strKey, this.getDescription(), ";"));
            }
        }
    }

    public String getOptionalQuotedValue() {
        String strR = strValue;
        if (strR.indexOf(" ") > -1) {
            strR = getQuotedValue(strR);
        }
        return strR;
    }

    public int getOptionType() {
        return intOptionType;
    }

    public void setOptionType(final int pintOptionType) {
        intOptionType = pintOptionType;
    }

    public void pop() {
        this.setValue(getStack().pop());
    }

    public void push() {
        getStack().push(strValue);
    }

    public String getQuotedValue() {
        return this.getQuotedValue(strValue);
    }

    public String getQuotedValue(final String pstrValue) {
        return "\"" + pstrValue.replaceAll("\"", "\"\"") + "\"";
    }

    private void raiseValueChangedListener() {
        if (lstValueChangedListeners != null) {
            for (IValueChangedListener objValueChangedListener : lstValueChangedListeners) {
                objValueChangedListener.valueHasChanged(this);
            }
        }
    }

    public void set(final SOSOptionElement pobjOption) {
        if (pobjOption.isDirty()) {
            this.setValue(pobjOption.getValue());
            this.setProtected(pobjOption.isProtected());
        }
    }

    public void setIfNotDirty(final SOSOptionElement pobjOption) {
        if (!this.isDirty()) {
            this.set(pobjOption);
        }
    }

    public SOSOptionElement setAlias(final String pstrAliasKey) {
        objAliase.add(pstrAliasKey);
        return this;
    }

    public SOSOptionElement setAlias(final String... pstrAliasKey) {
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

    public String setPrefix(final String prefix) {
        String key = strKey;
        int i = key.indexOf(".");
        if (i > 0) {
            key = key.replaceFirst("\\.", "." + prefix);
        } else {
            key = prefix + key;
        }
        strKey = key;
        return strKey;
    }

    public int getIntSize() {
        return intSize;
    }

    public SOSOptionElement setIntSize(final int pintSize) {
        intSize = pintSize;
        return this;
    }

    public SOSOptionElement size(final Integer pintSize) {
        intSize = pintSize;
        return this;
    }

    public boolean string2Bool() {
        boolean flgT = false;
        String pstrVal = strValue;
        if (isNotEmpty(pstrVal) && ("1".equals(pstrVal) || "y".equalsIgnoreCase(pstrVal) || "yes".equalsIgnoreCase(pstrVal) || "j".equalsIgnoreCase(
                pstrVal) || "on".equalsIgnoreCase(pstrVal) || "true".equalsIgnoreCase(pstrVal) || "wahr".equalsIgnoreCase(pstrVal))) {
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

    public String getTitle() {
        if (strTitle == null) {
            strTitle = "";
        }
        return strTitle;
    }

    public String toKeyValuePair(final String pstrAlternatePrefix) {
        String strRet = "";
        if ((!flgSelecteDirtyOnly || isDirty()) && isNotEmpty()) {
            if (isNotEmpty(pstrAlternatePrefix)) {
                strRet = pstrAlternatePrefix + "_";
            }
            strRet += this.getShortKey() + "=" + strValue;
        }
        return strRet;
    }

    public String toCommandLine() {
        String strRet = "";
        if (isNotEmpty() && isDirty() && (isMandatory() || !isDefault())) {
            strRet = "-" + this.getShortKey() + "=" + getOptionalQuotedValue() + " ";
        }
        return strRet;
    }

    public String toQuotedCommandLine() {
        String strRet = "";
        if (isNotEmpty() && isDirty() && (isMandatory() || !isDefault())) {
            strRet = "-" + this.getShortKey() + "=" + getQuotedValue() + " ";
        }
        return strRet;
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

    public String getValue() throws RuntimeException {
        if (strValue == null) {
            strValue = "";
        }
        return strValue;
    }

    @Override
    public void setValue(final String pstrValue) {
        if (flgIsMandatory && pstrValue == null && gflgProcessHashMap) {
            return;
        }
        if (pstrValue != null) {
            if (objParentClass != null) {
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

    public String getXMLTagName() {
        if (strXMLTagName == null) {
            strXMLTagName = "";
        }
        return strXMLTagName;
    }

    private String[] getValueList() {
        return new String[] {};
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