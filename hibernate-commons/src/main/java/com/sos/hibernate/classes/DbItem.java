package com.sos.hibernate.classes;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import javax.persistence.Transient;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/** @author Uwe Risse */
public class DbItem {

    private DateTimeZone dateTimeZone4Getters = DateTimeZone.getDefault();

    public DbItem() {
        //
    }

    @Transient
    public void setDateTimeZone4Getters(DateTimeZone dateTimeZone4Getters) {
        this.dateTimeZone4Getters = dateTimeZone4Getters;
    }

    @Transient
    public void setDateTimeZone4Getters(String dateTimeZone4Getters) {
        this.dateTimeZone4Getters = DateTimeZone.forID(dateTimeZone4Getters);
    }

    @Transient
    public DateTimeZone getDateTimeZone4Getters() {
        return this.dateTimeZone4Getters;
    }


    private boolean isToday(Date d) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(d).equals(formatter.format(new Date()));
    }

    @Transient
    public String getDateFormatted(Date d) {
        if (d == null) {
            return "";
        }
        String fromTimeZoneString = "UTC";
        DateTime dateTimeInUtc = new DateTime(d);
        String toTimeZoneString = getDateTimeZone4Getters().getID();
        if (isToday(UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(d)))) {
            return UtcTimeHelper.convertTimeZonesToString("HH:mm:ss", fromTimeZoneString, toTimeZoneString, dateTimeInUtc);
        } else {
            return UtcTimeHelper.convertTimeZonesToString("yyyy-MM-dd H:mm:ss", fromTimeZoneString, toTimeZoneString, dateTimeInUtc);
        }
    }

    protected String null2Blank(String s) {
        if (".".equals(s) || s == null) {
            return "";
        } else {
            return s;
        }
    }

    @Transient
    public String getDateDiff(Date start, Date end) {
        if (start == null || end == null) {
            return "";
        } else {
            Calendar cal_1 = new GregorianCalendar();
            Calendar cal_2 = new GregorianCalendar();
            cal_1.setTime(start);
            cal_2.setTime(end);
            long time = cal_2.getTime().getTime() - cal_1.getTime().getTime();
            time /= 1000;
            long seconds = time % 60;
            time /= 60;
            long minutes = time % 60;
            time /= 60;
            long hours = time % 24;
            time /= 24;
            long days = time;
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, (int) hours);
            calendar.set(Calendar.MINUTE, (int) minutes);
            calendar.set(Calendar.SECOND, (int) seconds);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            String d = "";
            if (days > 0) {
                d = String.format("%sd " + formatter.format(calendar.getTime()), days);
            } else {
                d = formatter.format(calendar.getTime());
            }
            return d;
        }
    }

    @Transient
    public long getDateDiffSeconds(Date start, Date end) {
        if (start == null || end == null) {
            return 0;
        } else {
            Calendar cal_1 = new GregorianCalendar();
            Calendar cal_2 = new GregorianCalendar();
            cal_1.setTime(start);
            cal_2.setTime(end);
            long time = cal_2.getTime().getTime() - cal_1.getTime().getTime();
            time /= 1000;
            long seconds = time % 60;
            time /= 60;
            time /= 60;
            time /= 24;
            return seconds;
        }
    }
 
    
    @Transient
    public String getValueOrBlank(String c) {
        if (c == null) {
            return "";
        } else {
            return c;
        }
    }
    
    @Transient
    public String normalizePath(String s) {
        return ("/"+s).replaceAll("//+", "/");
    }

}