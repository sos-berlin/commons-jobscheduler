package com.sos.jobstreams.classes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.sos.jitl.checkhistory.HistoryHelper;

public class FunctionResolver {

    public String resolveFunctions(String paramValue) {
        String newValue = paramValue;
        String[] functions = paramValue.split("%%");
        for (int i = 0; i < functions.length; i++) {
            String f = functions[i];
            String functionName = HistoryHelper.getMethodName(f);
            String parameter = HistoryHelper.getParameter(f);
            f = f.replaceAll("\\)", "\\\\)");
            f = f.replaceAll("\\(", "\\\\(");
            String s = resolveFunction(functionName, parameter);
            if (!s.isEmpty()) {
                newValue = newValue.replaceAll("\\%\\%" + f + "\\%\\%", s);
            }
        }
        return newValue;
    }

    private String todayAsIso() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    private String calcDate(String dateString, String quantity) throws ParseException {
        java.util.Calendar calendar = new GregorianCalendar();
        Date date;
        SimpleDateFormat outFormat = null;
        if (dateString.length() == 8) {
            date = new SimpleDateFormat("yyyyMMdd").parse(dateString);
            outFormat = new SimpleDateFormat("yyyyMMdd");
        } else {
            date = new SimpleDateFormat("yyMMdd").parse(dateString);
            outFormat = new SimpleDateFormat("yyMMdd");
        }
        calendar.setTime(date);
        int add = Integer.parseInt(quantity);
        calendar.add(Calendar.DATE, add);
        return outFormat.format(calendar.getTime());
    }

    private String substring(String str, int from, int length) {
        if (from > str.length()) {
            return "";
        }

        return str.substring(from - 1, from - 1 + length);
    }

    private String resolveFunction(String functionName, String parameter) {
        String[] params = parameter.split(",");

        switch (functionName) {
        case "today":
            return todayAsIso();
        case "getenv":
            if (params.length < 1) {
                return functionName + "(" + parameter + ")" + " -> wrong number of parameters. Expected 1, found: " + params.length;
            }
            return System.getenv(params[0]);
        case "substring":
            if (params.length < 3) {
                return functionName + "(" + parameter + ")" + " -> wrong number of parameters. Expected 3, found: " + params.length;
            }
            try {
                int from = Integer.parseInt(params[1]);
                int length = Integer.parseInt(params[2]);
                return substring(params[0], from, length);
            } catch (NumberFormatException e) {
                return functionName + "(" + parameter + ")" + " -> wrong number format. Expected int";
            }

        case "calcdate":
            if (params.length < 2) {
                return functionName + "(" + parameter + ")" + " -> wrong number of parameters. Expected 2, found: " + params.length;
            }
            try {
                return calcDate(params[0], params[1]);
            } catch (ParseException e) {
                return functionName + "(" + parameter + ")" + " -> wrong dateformat. Expected yyyymmdd or yymmdd";
            } catch (

            NumberFormatException e) {
                return functionName + "(" + parameter + ")" + " -> wrong number for quantity. Expected int";
            }

        default:
            break;
        }
        return "";
    }
}
