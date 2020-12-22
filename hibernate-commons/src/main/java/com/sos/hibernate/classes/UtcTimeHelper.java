package com.sos.hibernate.classes;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

public class UtcTimeHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtcTimeHelper.class);

    public static String localTimeZoneString() {
        return TimeZone.getDefault().getID();
    }

    public static String convertTimeZoneToTimeZone(String dateFormat, String fromTimeZone, String toTimeZone, String fromDateTime) throws Exception {
        LOGGER.debug("dateFormat:" + dateFormat);
        LOGGER.debug("fromTimeZone:" + fromTimeZone);
        LOGGER.debug("toTimeZone:" + toTimeZone);
        LOGGER.debug("fromDateTime:" + fromDateTime);
        if (ZoneId.SHORT_IDS.get(fromTimeZone) != null) {
            fromTimeZone = ZoneId.SHORT_IDS.get(fromTimeZone);
        }

        if (ZoneId.SHORT_IDS.get(toTimeZone) != null) {
            toTimeZone = ZoneId.SHORT_IDS.get(toTimeZone);
        }

        if (!ZoneId.getAvailableZoneIds().contains(toTimeZone)) {
            throw new Exception("Wrong value for timezone:" + toTimeZone);
        }
        
        if (!ZoneId.getAvailableZoneIds().contains(fromTimeZone)) {
            throw new Exception("Wrong value for timezone:" + fromTimeZone);
        }

        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern(dateFormat);

        if (dateFormat.length() > 8) {
            LocalDateTime dateTime = LocalDateTime.parse(fromDateTime, dateTimeFormatter);
            ZonedDateTime toDateTime = ZonedDateTime.now(ZoneId.of(fromTimeZone)).with(dateTime).withZoneSameInstant(ZoneId.of(toTimeZone));
            return toDateTime.format(dateTimeFormatter);
        } else {
            LocalTime dateTime = LocalTime.parse(fromDateTime, dateTimeFormatter);
            ZonedDateTime toDateTime = ZonedDateTime.now(ZoneId.of(fromTimeZone)).with(dateTime).withZoneSameInstant(ZoneId.of(toTimeZone));
            return toDateTime.format(dateTimeFormatter);
        }

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
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public static Date getNowUtc() {
        return convertTimeZonesToDate(DateTimeZone.getDefault().getID(), DateTimeZone.UTC.getID(), new DateTime());
    }

    public boolean isToday(DateTime compareWith) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd");
        String today = new DateTime().toString(fmt);
        return today.equals(compareWith.toString(fmt));
    }
}
