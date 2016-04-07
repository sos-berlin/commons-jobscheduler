package com.sos.JSHelper.DataElements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JSDataElementDateTime extends JSDataElement {

    protected Date dteDateTime = new Date();
    private String strDateFormat = "yyyy.MM.dd HH:mm:ss";
    private String strTimestamp = null;
    private SimpleDateFormat dateFormatter = null;
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
    public static final String FULL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public JSDataElementDateTime() {
        //
    }

    public JSDataElementDateTime(final Date pdteDateTime) {
        dteDateTime = pdteDateTime;
    }

    public JSDataElementDateTime(final String pstrValue) {
        super(pstrValue);
        String2Date(pstrValue);
    }

    private void String2Date(final String pstrDateTime) {
        DateFormat objDF = DateFormat.getDateTimeInstance();
        try {
            dteDateTime = objDF.parse(pstrDateTime);
        } catch (Exception objException) {
            super.Value("");
            dteDateTime = null;
        }
    }

    public JSDataElementDateTime(final String pPstrValue, final String pPstrDescription) {
        super(pPstrValue, pPstrDescription);
    }

    @Override
    public void doInit() {
        super.FormatString(strDateFormat);
        super.Description("DateAndTime");
        super.ColumnHeader("DateTime");
        super.XMLTagName("DateTime");
    }

    @Override
    public void Value(final String pstrDateTime) {
        String2Date(pstrDateTime);
        super.Value(pstrDateTime);
    }

    public void Value(final Date pdteDateTime) {
        dteDateTime = pdteDateTime;
        super.Value(this.FormattedValue());
    }

    public Date value() {
        return dteDateTime;
    }

    @Override
    public String FormattedValue() {
        if (dteDateTime == null) {
            strTimestamp = "";
        } else {
            if (isEmpty(this.FormatString())) {
                this.FormatString(FULL_DATETIME_FORMAT);
            }
            dateFormatter = new SimpleDateFormat(this.FormatString());
            strTimestamp = dateFormatter.format(dteDateTime);
        }
        return strTimestamp;
    }

    public Date ActualDateTime() {
        Calendar now = Calendar.getInstance();
        return now.getTime();
    }

}