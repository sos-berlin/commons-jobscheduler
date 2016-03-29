package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;

public class JodaTools {

    /** \brief calculate a date for a specific weekday based on a given date
     * \detail
     * 
     * @param date - the base date
     * @param weekday - the weekday
     * @return */
    public static DateTime getNextWeekday(DateTime date, int weekday) {
        if (date.getDayOfWeek() > weekday) {
            date = date.plusWeeks(1);
        }
        return date.withDayOfWeek(weekday);
    }

    /** \brief calculate a date for a specific weekday based on a given date
     * \detail
     *
     * @param date
     * @param weekday
     * @return */
    public static DateTime getPreviousWeekday(DateTime date, int weekday) {
        if (date.getDayOfWeek() < weekday) {
            date = date.minusWeeks(1);
        }
        return date.withDayOfWeek(weekday);
    }

    /** \brief calculate a date for a specific weekday based on a given date
     * \detail
     * 
     * @param date - the base date
     * @param weekday - the weekday
     * @return */
    public static DateTime getWeekdayInMonth(DateTime date, int weekday, int which) {
        if (which < 0) {
            DateTime baseDate = getEndOfMonth(date);
            DateTime d = getPreviousWeekday(baseDate, weekday);
            int weeks = (which * -1) - 1;
            return d.minusWeeks(weeks);
        }
        DateTime baseDate = getStartOfMonth(date);
        DateTime d = getNextWeekday(baseDate, weekday);
        return d.plusWeeks(which - 1);
    }

    public static DateTime getWeekdayInIntervalOrNull(Interval interval, int weekday, int which) {
        DateTime currentDate = getStartOfMonth(interval.getStart());
        DateTime result = getWeekdayInMonth(currentDate, weekday, which);
        while (!interval.contains(result)) {
            currentDate = currentDate.plusMonths(1);
            result = getWeekdayInMonth(currentDate, weekday, which);
            if (!result.isBefore(interval.getEnd())) {
                return null;
            }
        }
        return result;
    }

    public static DateTime getDayInMonth(DateTime date, int day) {
        DateTime baseDate = (day > 1) ? date.minusMonths(1) : date;
        DateTime result = getEndOfMonth(baseDate).plusDays(day);
        return result.minusMillis(result.getMillisOfDay());
    }

    public static DateTime getDayInIntervalOrNull(Interval interval, int day) {
        DateTime currentDate = getStartOfMonth(interval.getStart());
        DateTime result = getDayInMonth(currentDate, day);
        while (!interval.contains(result)) {
            currentDate = currentDate.plusMonths(1);
            result = getDayInMonth(currentDate, day);
            if (!result.isBefore(interval.getEnd())) {
                return null;
            }
        }
        return result;
    }

    /** \brief converts a JobScheduler day String into a list of Joda Weekday
     * constants \detail
     *
     * @param jsDay
     * @return */
    public static List<Integer> getJodaWeekdays(String jsDay) {
        List<Integer> result = new ArrayList<Integer>();
        String[] arr = jsDay.split(" ");
        for (int i = 0; i < arr.length; i++) {
            result.add(getJodaWeekday(arr[i]));
        }
        return result;
    }

    /** \brief converts a JS weekday in a Joda weekday \detail
     *
     * @param jsWeekday
     * @return */
    public static int getJodaWeekday(String jsWeekday) {
        String d = jsWeekday.toLowerCase();
        if (d.length() > 2) {
            d = d.substring(0, 2);
        }
        if ("1".equals(d) || "mo".equals(d)) {
            return DateTimeConstants.MONDAY;
        } else if ("2".equals(d) || "tu".equals(d) || "di".equals(d)) {
            return DateTimeConstants.TUESDAY;
        } else if ("3".equals(d) || "we".equals(d) || "mi".equals(d)) {
            return DateTimeConstants.WEDNESDAY;
        } else if ("4".equals(d) || "th".equals(d) || "do".equals(d)) {
            return DateTimeConstants.THURSDAY;
        } else if ("5".equals(d) || "fr".equals(d)) {
            return DateTimeConstants.FRIDAY;
        } else if ("6".equals(d) || "sa".equals(d)) {
            return DateTimeConstants.SATURDAY;
        }
        return DateTimeConstants.SUNDAY;
    }

    public static DateTime getStartOfMonth(DateTime base) {
        return base.minusMillis(base.getMillisOfDay()).minusDays(base.getDayOfMonth() - 1);
    }

    public static DateTime getEndOfMonth(DateTime base) {
        return getStartOfMonth(base.plusMonths(1)).minusMillis(1);
    }

    public static DateTime getStartOfDay(DateTime base) {
        return base.minusMillis(base.getMillisOfDay());
    }

}