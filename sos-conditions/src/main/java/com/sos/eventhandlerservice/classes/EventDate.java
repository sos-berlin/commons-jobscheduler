package com.sos.eventhandlerservice.classes;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class EventDate {

    public String getEventDate(String conditionEventDate) {

        Calendar calendar = Calendar.getInstance();
        String[] d_plus = conditionEventDate.split("\\+");
        String[] d_minus = conditionEventDate.split("-");
        int operand = 0;

        if (d_plus.length > 1) {
            conditionEventDate = d_plus[0];
            try {
                operand = Integer.valueOf(d_plus[1]);
            } catch (NumberFormatException e) {
                operand = 0;
            }
        }

        if (d_minus.length > 1) {
            conditionEventDate = d_minus[0];
            try {
                operand = 0 - Integer.valueOf(d_minus[1]);
            } catch (NumberFormatException e) {
                operand = 0;
            }
        }

        Date d = new Date();
        switch (conditionEventDate) {
        case "today":
            d = addCalendar(new Date(), operand, java.util.Calendar.DATE);
            calendar.setTime(d);
            break;
        case "*":
            return "*";
        case "prev":
            return "*";
        case "yesterday":
            d = addCalendar(new Date(), -1 + operand, java.util.Calendar.DATE);
            calendar.setTime(d);
            break;
        default:
            return conditionEventDate;
        }

        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        return String.valueOf(year) + "." + String.valueOf(dayOfYear);
    }

    private Date addCalendar(Date date, Integer add, Integer c) {
        java.util.Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(c, add);
        return calendar.getTime();
    }

    public boolean isPrev(String conditionEventDate) {
        return conditionEventDate.toLowerCase().trim().startsWith("prev");
     }

}
