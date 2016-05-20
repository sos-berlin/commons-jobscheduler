package com.sos.JSHelper.Options;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/** @author KB */
public class SOSOptionTime extends SOSOptionString {

    private static final long serialVersionUID = 6687670638160800096L;
    public final String ControlType = "timetext";
    public static String dateTimeFormat = new String("yyyy-MM-dd HH:mm:ss");
    private String strDefaultUoM = "";
    private long lngValue = 0;

    public SOSOptionTime(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public SOSOptionTime(final String pPstrValue) {
        this(null, "", "", pPstrValue, pPstrValue, false);
    }

    public static String getCurrentTimeAsString() {
        String strT = "n.a.";
        try {
            strT = getCurrentTimeAsString(dateTimeFormat);
        } catch (Exception e) {
        }
        return strT;
    }

    public static String getCurrentTimeAsString(final String pstrDateTimeFormat) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(pstrDateTimeFormat);
        formatter.setLenient(true);
        Calendar now = Calendar.getInstance();
        return formatter.format(now.getTime());
    }

    public static String getCurrentDateAsString() throws Exception {
        return getCurrentDateAsString("yyyy-MM-dd");
    }

    public static String getCurrentDateAsString(final String pstrDateFormatMask) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat(pstrDateFormatMask);
        formatter.setLenient(true);
        Calendar now = Calendar.getInstance();
        return formatter.format(now.getTime());
    }

    public void value(long plngValue) {
        lngValue = plngValue;
    }

    public void value(int pintValue) {
        lngValue = pintValue;
    }

    @Override
    public void setValue(final String pstrValue) {
        super.setValue(pstrValue);
        strValue = adjust2TimeFormat();
    }

    public int value() {
        return getTimeAsSeconds();
    }

    public long getTimeAsMilliSeconds() {
        return getTimeAsSeconds() * 1000L;
    }

    public int getTimeAsSeconds() {
        int intSeconds = 0;
        if (lngValue != 0) {
            intSeconds = (int) lngValue;
        } else {
            int[] intM = { 1, 60, 3600, 3600 * 24 };
            String[] strT = strValue.split(":");
            int j = 0;
            for (int i = strT.length - 1; i >= 0; i--) {
                intSeconds += new Integer(strT[i]) * intM[j++];
            }
        }
        return intSeconds;
    }

    public long calculateFileAge() {
        long age = 0;
        if (isNotEmpty(strValue)) {
            if (strValue.indexOf(":") > -1) {
                String[] timeArray = strValue.split(":");
                long hours = Long.parseLong(timeArray[0]);
                long minutes = Long.parseLong(timeArray[1]);
                long seconds = 0;
                if (timeArray.length > 2) {
                    seconds = Long.parseLong(timeArray[2]);
                }
                age = hours * 3600000 + minutes * 60000 + seconds * 1000;
            } else {
                age = Long.parseLong(strValue) * 1000;
            }
        }
        return age;
    }

    public String getTimeAsString(final long lngValue) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(lngValue);
    }

    public String adjust2TimeFormat() {
        if (isNotEmpty(strValue)) {
            if (strValue.indexOf(":") > -1) {
            } else {
                if (isNotEmpty(strDefaultUoM)) {
                    strValue = strValue + strDefaultUoM;
                }
            }
            int intL = strValue.length();
            if (!"0".equals(strValue)) {
                String strT = strValue.substring(intL - 1, intL).toLowerCase();
                String strN = strValue.substring(0, intL - 1);
                switch (strT) {
                case "w":
                    int intW = new Integer(strN);
                    strValue = intW * 7 + ":00:00:00";
                    break;
                case "d":
                    strValue = strN + ":00:00:00";
                    break;
                case "h":
                    strValue = strN + ":00:00";
                    break;
                case "m":
                    strValue = strN + ":00";
                    break;
                case "s":
                    strValue = strN;
                    break;
                default:
                    break;
                }
            }
        }
        return strValue;
    }

    public String getDefaultUoM() {
        return strDefaultUoM;
    }

    public void setDefaultUoM(String defaultUoM) {
        strDefaultUoM = defaultUoM.toLowerCase();
        if (isNotEmpty(defaultUoM)) {
            adjust2TimeFormat();
        }
    }

}