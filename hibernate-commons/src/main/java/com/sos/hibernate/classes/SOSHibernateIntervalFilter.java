package com.sos.hibernate.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Uwe Risse */
public abstract class SOSHibernateIntervalFilter extends SOSHibernateFilter {

    private String timeZone;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateIntervalFilter.class);

    public abstract void setIntervalFromDate(Date d);

    public abstract void setIntervalToDate(Date d);

    public abstract void setIntervalFromDateIso(String s);

    public abstract void setIntervalToDateIso(String s);

    public SOSHibernateIntervalFilter() {
        super();
    }

    private Date convertTimeZone(Date d, String fromTimeZoneString, String toTimeZoneString) {
        DateTime dateTimeInUtc = new DateTime(d);
        return UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, dateTimeInUtc);
    }

    public Date convertFromTimeZoneToUtc(Date d) {
        String toTimeZoneString = "UTC";
        String fromTimeZoneString = this.getTimeZone();
        return convertTimeZone(d, fromTimeZoneString, toTimeZoneString);
    }

    public SOSHibernateIntervalFilter(String strI28NPropertyFileName) {
        super(strI28NPropertyFileName);
    }

    public void setIntervalFrom(final Date from) {
        if (from != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            String d = formatter.format(from);
            try {
                formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                this.setIntervalFromDate(convertFromTimeZoneToUtc(formatter.parse(d)));
            } catch (ParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.setIntervalFromDateIso(formatter.format(from));
            LOGGER.debug(String.format("Setting interval from: %s ", formatter.format(from)));
        } else {
            this.setIntervalFromDate(null);
        }
    }

    public void setIntervalTo(final Date to) {
        if (to != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
            String d = formatter.format(to);
            try {
                formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                this.setIntervalToDate(convertFromTimeZoneToUtc(formatter.parse(d)));
            } catch (ParseException e) {
                LOGGER.error(e.getMessage(), e);
            }
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.setIntervalToDateIso(formatter.format(to));
            LOGGER.debug(String.format("Setting interval to: %s ", formatter.format(to)));
        } else {
            this.setIntervalToDate(null);
        }
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

}
