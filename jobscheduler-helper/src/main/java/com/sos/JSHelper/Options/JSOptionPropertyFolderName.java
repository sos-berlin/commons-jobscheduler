package com.sos.JSHelper.Options;

import java.io.File;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class JSOptionPropertyFolderName extends SOSOptionFolderName {

    private static final long serialVersionUID = -5962741104646578748L;
    private static final String CLASSNAME = "JSOptionPropertyFolderName";

    public JSOptionPropertyFolderName(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription,
            final String pPstrValue, final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, "Java-Property user.dir", pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    @Override
    public void setValue(String pstrFolderName) {
        final String methodName = CLASSNAME + "::Value";
        pstrFolderName = checkIsFileWritable(pstrFolderName, methodName);
        super.setValue(pstrFolderName);
        if (isNotEmpty(strValue)) {
            System.setProperty("user.dir", strValue);
        }
    }

    @Override
    public String getValue() {
        return System.getProperty("user.dir");
    }

    
    private String checkIsFileWritable(final String pstrFileName, final String pstrMethodName) {
        String strT = null;
        if (isNotEmpty(pstrFileName)) {
            try {
                final File fleF = new File(pstrFileName);
                if (!fleF.exists()) {
                    fleF.createNewFile();
                }
                if (!fleF.canWrite()) {
                    strT = String.format("%2$s: File '%1$s'. canWrite returns false. Check permissions.", pstrFileName, pstrMethodName);
                    strT += fleF.toString();
                }
            } catch (final Exception objException) {
                strT = String.format("%2$s: File '%1$s'. Exception thrown. Check permissions.", pstrFileName, pstrMethodName);
                final JobSchedulerException objJSEx = new JobSchedulerException(strT, objException);
                this.signalError(objJSEx, strT);
            }
            if (strT != null) {
                this.signalError(strT);
            }
        }
        return pstrFileName;
    }
}