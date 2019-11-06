package com.sos.jobstreams.classes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.jobstreams.db.DBItemCalendarWithUsages;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendar;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendarUsage;
import com.sos.jobstreams.db.DBLayerCalendarUsages;
import com.sos.jobstreams.db.FilterCalendarUsage;
import com.sos.joc.classes.calendars.CalendarResolver;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Dates;

public class JobStreamCalendar {

    public Set<LocalDate> getListOfDates(SOSHibernateSession sosHibernateSession, FilterCalendarUsage filterCalendarUsage) throws Exception {

        Dates dates = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Set<LocalDate> listOfDates = new HashSet<LocalDate>();

        DBLayerCalendarUsages dbLayer = new DBLayerCalendarUsages(sosHibernateSession);

        List<DBItemCalendarWithUsages> listOfCalendarUsages = dbLayer.getCalendarUsages(filterCalendarUsage, 0);
        CalendarResolver calendarResolver = new CalendarResolver();
        for (DBItemCalendarWithUsages item : listOfCalendarUsages) {
            CalendarDatesFilter calendarDatesFilter = new CalendarDatesFilter();
            Calendar calendar = new ObjectMapper().readValue(item.getDBItemInventoryClusterCalendarUsage().getConfiguration(), Calendar.class);
            calendar.setBasedOn(item.getDBItemInventoryClusterCalendar().getName());
            calendarDatesFilter.setCalendar(calendar);
            calendarDatesFilter.setJobschedulerId(filterCalendarUsage.getSchedulerId());
            try {
                dates = calendarResolver.getCalendarDates(sosHibernateSession, calendarDatesFilter);
                for (String d : dates.getDates()) {
                    listOfDates.add(LocalDate.parse(d, formatter));
                }
            } catch (DBMissingDataException e) {
            }
        }
        if (listOfDates.size() == 0) {
            return null;
        } else {
            return listOfDates;
        }
    }

}
