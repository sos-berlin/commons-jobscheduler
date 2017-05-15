package com.sos.hibernate.layer;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.hibernate.exceptions.SOSHibernateException;

/** @author Uwe Risse */
public abstract class SOSHibernateIntervalDBLayer extends SOSHibernateDBLayer {

    private static final Logger LOGGER = Logger.getLogger(SOSHibernateIntervalDBLayer.class);
    public abstract SOSHibernateIntervalFilter getFilter();
    public abstract void onAfterDeleting(DbItem h) throws SOSHibernateException ;
    public abstract List<DbItem> getListOfItemsToDelete() throws SOSHibernateException;
    public abstract long deleteInterval() throws SOSHibernateException;

    public SOSHibernateIntervalDBLayer() {
        super();
    }

    public long deleteInterval(int interval, int limit) throws SOSHibernateException  {
        long deleted = 0;
        if (limit == 0) {
            GregorianCalendar to = new GregorianCalendar();
            to.add(GregorianCalendar.DAY_OF_YEAR, -interval);
            this.getFilter().setIntervalFrom(null);
            this.getFilter().setIntervalTo(to.getTime());
            this.getSession().beginTransaction();
            deleted = deleteInterval();
            this.getSession().commit();

        } else {
            if (sosHibernateSession == null) {
                this.getSession().beginTransaction();
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
                    sosHibernateSession.delete(h);
                    this.onAfterDeleting(h);
                    deleted = deleted + 1;
                    i = i + 1;
                    if (i == limit) {
                        i = 0;
                    }
                }
                this.getSession().commit();
            }
        }
        return deleted;
    }

    public long deleteInterval(int interval) throws SOSHibernateException {
        return deleteInterval(interval, 300);
    }

}
