package com.sos.hibernate.classes;

import java.text.SimpleDateFormat;
import java.util.Date;

// import com.sos.hibernate.SOSHibernateConstants;
import com.sos.hibernate.SOSHibernateConstants;
import com.sos.i18n.I18NBase;

public abstract class SOSHibernateFilter extends I18NBase {

    protected String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    protected Date endTime;
    protected String status = "";
    private String sortMode = "asc";
    private String orderCriteria;
    private int limit = 0;

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getSortMode() {
        if (orderCriteria == null || "".equals(orderCriteria) || sortMode == null) {
            return "";
        } else {
            return " " + sortMode;
        }
    }

    public void setSortMode(String sortMode) {
        this.sortMode = sortMode;
    }

    public String getOrderCriteria() {
        if (orderCriteria == null || "".equals(orderCriteria)) {
            return "";
        } else {
            return " order by " + orderCriteria;
        }
    }

    public void setOrderCriteria(String orderCriteria) {
        this.orderCriteria = orderCriteria;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setLimit(String limit) {
        try {
            this.limit = Integer.parseInt(limit);
        } catch (NumberFormatException e) {
            //
        }
    }

    public String date2Iso(Date d) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(d);
    }

    public SOSHibernateFilter() {
        super(SOSHibernateConstants.conPropertiesFileName);
    }

    public SOSHibernateFilter(final String i18NPropertyFileName) {
        super(i18NPropertyFileName);
    }

}