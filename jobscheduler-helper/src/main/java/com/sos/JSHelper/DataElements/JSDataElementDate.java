package com.sos.JSHelper.DataElements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.FormatPatternException;

public class JSDataElementDate extends JSDataElement {

    private static final Logger LOGGER = Logger.getLogger(JSDataElementDate.class);
    private JSDateFormat objFormat = null;

    public JSDataElementDate() {
        super();
    }

    public JSDataElementDate(final String pstrDate, final String pstrTime) {
        super.Value(pstrDate + pstrTime);
    }

    public JSDataElementDate(final JSDataElement pelemDate, final JSDataElement pelemTime) {
        final String strD = pelemDate.Value();
        String strT = pelemTime.Value();
        if (strT.trim().length() <= 0) {
            strT = "000000";
        }
        super.Value(strD + strT);
    }

    public JSDataElementDate(final String pstrDate) {
        super.Value(pstrDate);
    }

    public JSDataElementDate(final String pstrDate, final JSDateFormat pobjFormat) {
        super.Value(pstrDate);
        objFormat = pobjFormat;
    }

    public JSDataElementDate(final Date pdteDate, final JSDateFormat pobjFormat) {
        this.Value(pdteDate);
        objFormat = pobjFormat;
    }

    public JSDataElementDate(final Date pdatDate) {
        JSDateFormat objFormat = JSDateFormat.dfTIMESTAMPS24;
        this.Value(objFormat.format(pdatDate));
    }

    public JSDataElementDate(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos,
            final String pPstrFormatString, final String pPstrColumnHeader, final String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    @Override
    public void Value(String pstrValue) {
        if (pstrValue == null || "00000000".equals(pstrValue)) {
            pstrValue = "";
        }
        super.Value(pstrValue);
    }

    public void Value(final Date pdatValue) {
        if (objFormat == null) {
            objFormat = JSDateFormat.dfDATE_N8;
        }
        super.Value(objFormat.format(pdatValue));
    }

    public Date getDateObject() {
        try {
            if (objFormat == null) {
                objFormat = JSDateFormat.dfDATE_N8;
            }
            Date objD = objFormat.parse(this.Value());
            return objD;
        } catch (final ParseException e) {
            message(e.getMessage());
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public void setParsePattern(final JSDateFormat pobjFormat) {
        objFormat = pobjFormat;
    }

    public JSDateFormat getParsePattern() {
        return objFormat;
    }

    public void setFormatPattern(final JSDateFormat pobjFormat) {
        super.FormatString(pobjFormat.toPattern());
    }

    @Override
    public void doInit() {
        super.FormatString(JSDateFormat.dfDATE_N8.toPattern());
        super.Description("Date");
        super.ColumnHeader("Date");
        super.XMLTagName("Date");
        final String strFormat = super.FormatString();
        if (isNotEmpty(strFormat)) {
            final JSDateFormat df = new JSDateFormat(strFormat);
            super.setFormatPattern(df.toPattern());
        }
    }

    @Override
    public String FormattedValue() {
        String strFormat = super.FormatString();
        if (strFormat.length() <= 0) {
            strFormat = JSDateFormat.dfDATE_N8.toPattern();
        }
        if (isNotEmpty(strFormat) && HasAValue()) {
            final JSDateFormat df = new JSDateFormat(strFormat);
            return df.format(getDateObject());
        } else {
            return this.Value();
        }
    }

    @Override
    public void checkFormatPattern() throws FormatPatternException {
        if (objFormat != null) {
            try {
                objFormat.parse(this.Value());
            } catch (final ParseException e) {
                throw new FormatPatternException("the value '" + this.Value() + "' does not correspond with the pattern " + objFormat.toPattern());
            }
        }
    }

    public boolean HasAValue() {
        return !this.isEmpty();
    }

    public boolean isEmpty() {
        if (super.Value().trim().length() <= 0 || "00000000".equals(super.Value().trim())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String SQLValue() {
        this.FormatString(JSDateFormat.dfTIMESTAMPS.toPattern());
        String strV = FormattedValue();
        strV = strV.substring(0, 14);
        final String strMask = "YYYYMMDDHH24MISS";
        return "to_date(" + strV + ", '" + strMask + "')";
    }

    public Date Now() {
        final java.util.Calendar now = java.util.Calendar.getInstance();
        return now.getTime();
    }

    public static String getCurrentTimeAsString(String dateTimeFormat) throws Exception {
        String strFormat = dateTimeFormat;
        if (dateTimeFormat == null || dateTimeFormat.length() <= 0) {
            strFormat = "yyyyMMddHHmmss";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
        Calendar now = Calendar.getInstance();
        return formatter.format(now.getTime());
    }

    public static String getCurrentTimeAsString() throws Exception {
        return getCurrentTimeAsString("yyyyMMddHHmmss");
    }

    public int getLastFridayInAMonth(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        int firstDay = cal.get(Calendar.DAY_OF_WEEK);
        int daysOfMonth = cal.getMaximum(Calendar.DAY_OF_MONTH);
        switch (firstDay) {
        case Calendar.SUNDAY:
            return 27;
        case Calendar.MONDAY:
            return 26;
        case Calendar.TUESDAY:
            return 25;
        case Calendar.WEDNESDAY:
            if (daysOfMonth == 31) {
                return 31;
            }
            return 24;
        case Calendar.THURSDAY:
            if (daysOfMonth >= 30) {
                return 30;
            }
            return 23;
        case Calendar.FRIDAY:
            if (daysOfMonth >= 29) {
                return 29;
            }
            return 22;
        case Calendar.SATURDAY:
            return 28;
        }
        throw new RuntimeException("what day of the month?");
    }

    public int getLastThursday(int month, int year) {
        return getLastFridayInAMonth(month, year) - 1;
    }
}
