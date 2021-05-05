package com.sos.jobstreams.classes;

import java.util.Calendar;

import com.sos.jitl.jobstreams.Constants;

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

        calendar = Constants.getSessionCalendar();
         switch (conditionEventDate) {
        case "today":
            calendar.add(java.util.Calendar.DATE, operand);
            break;
        case "*":
            return "*";
        case "prev":
            return "*";
        case "yesterday":
            calendar.add(java.util.Calendar.DATE, -1 + operand);
            break;
        default:
            return conditionEventDate;
        }

        int month = calendar.get(Calendar.MONTH)+1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        return String.valueOf(month) + "." + String.valueOf(dayOfMonth);
    }
 

    public boolean isPrev(String conditionEventDate) {
        return conditionEventDate.toLowerCase().trim().startsWith("prev");
     }

}
