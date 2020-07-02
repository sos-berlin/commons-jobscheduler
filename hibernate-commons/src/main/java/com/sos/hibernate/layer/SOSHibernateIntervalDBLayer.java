package com.sos.hibernate.layer;

import java.util.GregorianCalendar;
import java.util.List;

import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.hibernate.exceptions.SOSHibernateException;

/** @author Uwe Risse */
public abstract class SOSHibernateIntervalDBLayer<T> extends SOSHibernateDBLayer {

    public abstract SOSHibernateIntervalFilter getFilter();
    public abstract void onAfterDeleting(T h) throws SOSHibernateException ;
    public abstract List<T> getListOfItemsToDelete() throws SOSHibernateException;
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
                List<T> listOfDBItems = this.getListOfItemsToDelete();
                int i = 0;
                if (listOfDBItems != null) {
                    for (T h : listOfDBItems) {
                        sosHibernateSession.delete(h);
                        this.onAfterDeleting(h);
                        deleted = deleted + 1;
                        i = i + 1;
                        if (i == limit) {
                            i = 0;
                        }
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
