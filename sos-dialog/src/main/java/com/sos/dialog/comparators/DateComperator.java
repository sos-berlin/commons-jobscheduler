package com.sos.dialog.comparators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sos.hibernate.classes.SosSortTableItem;

public class DateComperator extends SortBaseComparator implements Comparable {

    private static final Logger LOGGER = Logger.getLogger(DateComperator.class);

    public DateComperator(SosSortTableItem tableItem, int rowNum, int colPos) {
        super(tableItem, rowNum, colPos);
    }

    public final int compareTo(Object arg0) {
        if (sosSortTableItem.getTextBuffer()[_colPos] == null) {
            sosSortTableItem.getTextBuffer()[_colPos] = "2000-01-01 00:00:00";
        }
        SosSortTableItem compareItem = null;
        if (((DateComperator) arg0) == null) {
            compareItem = new SosSortTableItem();
            compareItem.setTextBuffer(sosSortTableItem.getTextBuffer());
        } else {
            compareItem = ((DateComperator) arg0).sosSortTableItem;
        }
        if (compareItem.getTextBuffer()[_colPos] == null) {
            compareItem.getTextBuffer()[_colPos] = "2000-01-01 00:00:00";
        }
        String s1 = sosSortTableItem.getTextBuffer()[_colPos];
        if ("".equals(s1)) {
            s1 = "2001-01-01 00:00:00";
        }
        String s2 = compareItem.getTextBuffer()[_colPos];
        if ("".equals(s2)) {
            s2 = "2001-01-01 00:00:00";
        }
        String isoDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        String isoDateFormat = "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(isoDateTimeFormat);
        SimpleDateFormat formatterDate = new SimpleDateFormat(isoDateFormat);
        Calendar now = Calendar.getInstance();
        String today = formatterDate.format(now.getTime());
        Date d1 = new Date();
        try {
            d1 = formatter.parse(s1);
        } catch (ParseException e) {
            try {
                d1 = formatter.parse(today + " " + s1);
            } catch (ParseException e1) {
                try {
                    d1 = formatter.parse("2000:01:01 00:00:00");
                } catch (ParseException e2) {
                    LOGGER.error(e2.getMessage(), e2);
                }
            }
        }
        Date d2 = new Date();
        try {
            d2 = formatter.parse(s2);
        } catch (ParseException e) {
            try {
                d2 = formatter.parse(today + " " + s2);
            } catch (ParseException e1) {
                try {
                    d2 = formatter.parse("2000:01:01 00:00:00");
                } catch (ParseException e2) {
                    LOGGER.error(e2.getMessage(), e2);
                }
            }
        }
        int ret = 0;
        if (d1.before(d2)) {
            ret = -1;
        }
        if (d2.before(d1)) {
            ret = 1;
        }
        return _sortFlag ? ret : ret * -1;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}