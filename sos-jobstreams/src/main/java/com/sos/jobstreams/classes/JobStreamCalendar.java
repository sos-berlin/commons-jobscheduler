package com.sos.jobstreams.classes;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.exception.SOSInvalidDataException;
import com.sos.exception.SOSMissingDataException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendar;
import com.sos.jobstreams.db.DBLayerCalendars;
import com.sos.jobstreams.db.FilterCalendarUsage;
import com.sos.joc.classes.calendar.FrequencyResolver;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.CalendarDatesFilter;
import com.sos.joc.model.calendar.Dates;

public class JobStreamCalendar {

    public Set<LocalDate> getListOfDates(SOSHibernateSession sosHibernateSession, FilterCalendarUsage filterCalendarUsage)
            throws SOSHibernateException, JsonParseException, JsonMappingException, IOException, SOSMissingDataException, SOSInvalidDataException {

        Dates dates = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Set<LocalDate> listOfDates = new HashSet<LocalDate>();
        FrequencyResolver fr = new FrequencyResolver();

        DBLayerCalendars dbLayer = new DBLayerCalendars(sosHibernateSession);

        List<DBItemInventoryClusterCalendar> listOfCalendars = dbLayer.getCalendar(filterCalendarUsage, 0);
        for (DBItemInventoryClusterCalendar item : listOfCalendars) {
            CalendarDatesFilter calendarFilter = new CalendarDatesFilter();
            calendarFilter.setCalendar(new ObjectMapper().readValue(item.getConfiguration(), Calendar.class));
            dates = fr.resolve(calendarFilter);
            for (String d : dates.getDates())
                listOfDates.add(LocalDate.parse(d, formatter));
        }
        return listOfDates;

    }

}
