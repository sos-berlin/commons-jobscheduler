package com.sos.hibernate.classes;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UtcTimeHelper {

    public static String localTimeZoneString() {
        return TimeZone.getDefault().getID();
    }


    public static String convertTimeZonesToString(String dateFormat, String fromTimeZone, String toTimeZone, DateTime fromDateTime) {
        DateTimeZone fromZone = DateTimeZone.forID(fromTimeZone);
        DateTimeZone toZone = DateTimeZone.forID(toTimeZone);

        DateTime dateTime = new DateTime(fromDateTime);

        dateTime = dateTime.withZoneRetainFields(fromZone);

        DateTime toDateTime = new DateTime(dateTime).withZone(toZone);

        DateTimeFormatter oFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'H:mm:ss.SSSZ");
        DateTimeFormatter oFormatter2 = DateTimeFormat.forPattern(dateFormat);

        DateTime newDate = oFormatter.withOffsetParsed().parseDateTime(toDateTime.toString());

        return oFormatter2.withZone(toZone).print(newDate.getMillis());

    }

    public static Date convertTimeZonesToDate(String fromTimeZone, String toTimeZone, DateTime fromDateTime) {
        if (fromDateTime == null) {
            return null;
        }
        DateTimeZone fromZone = DateTimeZone.forID(fromTimeZone);
        DateTimeZone toZone = DateTimeZone.forID(toTimeZone);

        DateTime dateTime = new DateTime(fromDateTime);

        dateTime = dateTime.withZoneRetainFields(fromZone);

        DateTime toDateTime = new DateTime(dateTime).withZone(toZone);

        DateTimeFormatter oFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'H:mm:ss.SSSZ");
        DateTimeFormatter oFormatter2 = DateTimeFormat.forPattern("yyyy-MM-dd H:mm:ss.ss");

        DateTime newDate = oFormatter.withOffsetParsed().parseDateTime(toDateTime.toString());

        try {
            return new SimpleDateFormat("yyyy-MM-dd H:mm:ss.ss").parse(oFormatter2.withZone(toZone).print(newDate.getMillis()));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }

    public Date getNowUtc() {
        return convertTimeZonesToDate(DateTimeZone.getDefault().getID(), DateTimeZone.UTC.getID(), new DateTime());
    }

    public boolean isToday(DateTime compareWith) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
        String today = new DateTime().toString(fmt);
        return today.equals(compareWith.toString(fmt));
    }
}
