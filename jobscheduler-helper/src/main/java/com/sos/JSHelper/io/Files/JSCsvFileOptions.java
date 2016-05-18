package com.sos.JSHelper.io.Files;

import java.util.HashMap;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;

public class JSCsvFileOptions extends JSOptionsClass {

    private static final long serialVersionUID = -7057158679889287553L;
    private static final String CLASSNAME = "JSCsvFileOptions";
    private static final String DELIMITER_SETTINGS_KEY = CLASSNAME + ".Delimiter";
    private static final String CSV_COLUMN_DELIMITER_SETTINGS_KEY = CLASSNAME + ".CSVColumnDelimiter";
    private static final String SKIP_FIRST_LINE_SETTINGS_KEY = CLASSNAME + ".Skip_First_Line";
    private static final String IGNORE_VALUE_DELIMITER_SETTINGS_KEY = CLASSNAME + ".IgnoreValueDelimiter";
    private String strDelimiter = String.valueOf((char) 254);
    private boolean flgSkipFirstLine = true;
    private boolean flgIgnoreValueDelimiter = true;

    public JSCsvFileOptions() {
        objParentClass = this.getClass();
    }

    public JSCsvFileOptions(final JSListener pobjListener) {
        registerMessageListener(pobjListener);
    }

    public JSCsvFileOptions(final HashMap<String, String> JSSettings) throws Exception {
        setAllOptions(JSSettings);
    }

    @Override
    public void toOut() {
        System.out.println(getAllOptionsAsString());
    }

    @Override
    public String toString() {
        return getAllOptionsAsString();
    }

    private String getAllOptionsAsString() {
        String strT = CLASSNAME + "\n";
        strT += "Delimiter      Feld-Trennzeichen : " + this.Delimiter() + "\n";
        strT += "SkipFirstLine  Erste Zeile als Überschrift interpretieren und überlesen : " + this.SkipFirstLine() + "\n";
        strT += "IgnoreValueDelimiter Ignore Value Delimiter : " + this.IgnoreValueDelimiter() + "\n";
        return strT;
    }

    @Override
    public void setAllOptions(final HashMap<String, String> JSSettings) {
        try {
            objSettings = JSSettings;
            super.Settings(objSettings);

            final String strT = super.getItem(DELIMITER_SETTINGS_KEY);

            if (isNotEmpty(strT)) {
                this.Delimiter(strT);
            } else {
                this.Delimiter(super.getItem(CSV_COLUMN_DELIMITER_SETTINGS_KEY));
            }
            this.SkipFirstLine(super.getBoolItem(SKIP_FIRST_LINE_SETTINGS_KEY));
            this.IgnoreValueDelimiter(super.getBoolItem(IGNORE_VALUE_DELIMITER_SETTINGS_KEY));

        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    @Override
    public void checkMandatory() throws Exception {
        try {
            this.Delimiter(this.Delimiter());
        } catch (final Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    public String Delimiter() {
        return strDelimiter;
    }

    public JSCsvFileOptions Delimiter(final String pstrDelimiter) throws Exception {
        if (pstrDelimiter == null) {
            SignalError(CLASSNAME + ":Delimiter" + conNullButMandatory);
        } else {
            if (pstrDelimiter.matches("^[0-9]+$")) {
                strDelimiter = String.valueOf((char) Integer.parseInt(pstrDelimiter));
            } else {
                strDelimiter = pstrDelimiter.substring(0, 1);
            }
        }
        return this;
    }

    public boolean SkipFirstLine() {
        return flgSkipFirstLine;
    }

    public JSCsvFileOptions SkipFirstLine(final boolean pflgSkipFirstLine) throws Exception {
        flgSkipFirstLine = pflgSkipFirstLine;
        return this;
    }

    public boolean IgnoreValueDelimiter() {
        return flgIgnoreValueDelimiter;
    }

    public JSCsvFileOptions IgnoreValueDelimiter(final boolean pflgIgnoreValueDelimiter) throws Exception {
        flgIgnoreValueDelimiter = pflgIgnoreValueDelimiter;
        return this;
    }

}
