package com.sos.hibernate.layer;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;

/** @author Uwe Risse */
public abstract class SOSHibernateIntervalDBLayer extends SOSHibernateDBLayer {

    private static final Logger LOGGER = Logger.getLogger(SOSHibernateIntervalDBLayer.class);

    public abstract SOSHibernateIntervalFilter getFilter();

    public abstract void onAfterDeleting(DbItem h);

    public abstract List<DbItem> getListOfItemsToDelete();

    public abstract long deleteInterval();

    public SOSHibernateIntervalDBLayer() {
        super();
    }

    public long deleteInterval(int interval, int limit) {
        long deleted = 0;
        if (limit == 0) {
            GregorianCalendar to = new GregorianCalendar();
            to.add(GregorianCalendar.DAY_OF_YEAR, -interval);
            this.getFilter().setIntervalFrom(null);
            this.getFilter().setIntervalTo(to.getTime());
            try {
                deleted = deleteInterval();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            if (connection == null) {
                initConnection(getConfigurationFileName());
            }
            if (interval > 0) {
                GregorianCalendar to = new GregorianCalendar();
                to.add(GregorianCalendar.DAY_OF_YEAR, -interval);
                this.getFilter().setLimit(limit);
                this.getFilter().setIntervalFrom(null);
                this.getFilter().setIntervalTo(to.getTime());
                List<DbItem> listOfDBItems = this.getListOfItemsToDelete();
                Iterator<DbItem> dbitemEntries = listOfDBItems.iterator();
                int i = 0;
                while (dbitemEntries.hasNext()) {
                    DbItem h = (DbItem) dbitemEntries.next();
                    try {
                        connection.connect();
                        connection.beginTransaction();
                        connection.delete(h);
                        connection.commit();
                    } catch (Exception e) {
                        LOGGER.error("Error occurred connecting to DB: ", e);
                    }
                    this.onAfterDeleting(h);
                    deleted = deleted + 1;
                    i = i + 1;
                    if (i == limit) {
                        i = 0;
                    }
                }
            }
        }
        return deleted;
    }

    public long deleteInterval(int interval) {
        return deleteInterval(interval, 300);
    }

}
