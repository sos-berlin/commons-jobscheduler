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
        string2Date(pstrValue);
    }

    public JSDataElementDateTime(final String pPstrValue, final String pPstrDescription) {
        super(pPstrValue, pPstrDescription);
    }

    private void string2Date(final String pstrDateTime) {
        DateFormat objDF = DateFormat.getDateTimeInstance();
        try {
            dteDateTime = objDF.parse(pstrDateTime);
        } catch (Exception objException) {
            super.setValue("");
            dteDateTime = null;
        }
    }

    @Override
    public void doInit() {
        super.setFormatString(strDateFormat);
        super.description("DateAndTime");
        super.columnHeader("DateTime");
        super.xmlTagName("DateTime");
    }

    @Override
    public void setValue(final String pstrDateTime) {
        string2Date(pstrDateTime);
        super.setValue(pstrDateTime);
    }

    public void setValue(final Date pdteDateTime) {
        dteDateTime = pdteDateTime;
        super.setValue(this.getFormattedValue());
    }

    public Date value() {
        return dteDateTime;
    }

    @Override
    public String getFormattedValue() {
        if (dteDateTime == null) {
            strTimestamp = "";
        } else {
            if (isEmpty(this.getFormatString())) {
                this.setFormatString(FULL_DATETIME_FORMAT);
            }
            dateFormatter = new SimpleDateFormat(this.getFormatString());
            strTimestamp = dateFormatter.format(dteDateTime);
        }
        return strTimestamp;
    }

    public Date getActualDateTime() {
        return Calendar.getInstance().getTime();
    }

}