package com.sos.jobstreams.classes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.jitl.jobstreams.db.DBItemCalendarWithUsages;
import com.sos.joc.classes.calendars.CalendarResolver;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Dates;

public class JobStreamCalendar {

    public Set<LocalDate> getListOfDates(List<DBItemCalendarWithUsages> listOfCalendarUsages) throws Exception {

        Dates dates = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Set<LocalDate> listOfDates = new HashSet<LocalDate>();

        for (DBItemCalendarWithUsages item : listOfCalendarUsages) {
            if (item != null && item.getRestrictionConfiguration() != null) {
                CalendarDatesFilter calendarDatesFilter = new CalendarDatesFilter();
                Calendar calendar = new ObjectMapper().readValue(item.getRestrictionConfiguration(), Calendar.class);
                calendar.setBasedOn(item.getName());
                calendarDatesFilter.setCalendar(calendar);
                dates = CalendarResolver.getCalendarDates(item.getCalendarConfiguration(),calendarDatesFilter);
                for (String d : dates.getDates()) {
                    listOfDates.add(LocalDate.parse(d, formatter));
                }
            }
        }
        if (listOfDates.size() == 0) {
            return null;
        } else {
            return listOfDates;
        }
    }

}
