package com.sos.hibernate.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** @author Uwe Risse */
public class SOSHibernateDate {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateDate.class);
    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private Date date;
    private String isoDate;

    public SOSHibernateDate(String dateFormat_) {
        this.dateFormat = dateFormat_;
    }

    private void setIsoDate() throws ParseException {
        String isoDateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(isoDateFormat);
        this.isoDate = formatter.format(date);
    }

    public void setDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        if ("now".equals(date)) {
            this.date = new Date();
        } else {
            this.date = formatter.parse(date);
        }
        this.setIsoDate();
    }

    public Date getDate() {
        return date;
    }

    public String getIsoDate() {
        return isoDate;
    }

    public void setDate(Date date) {
        this.date = date;
        try {
            this.setIsoDate();
        } catch (ParseException e) {
            LOGGER.debug("SOSHibernateDate.setDate: Could not set Iso-Date");
        }
    }

}
