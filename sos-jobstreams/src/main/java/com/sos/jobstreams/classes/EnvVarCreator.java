package com.sos.jobstreams.classes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.sos.joc.Globals;
import com.sos.joc.model.common.NameValuePair;

class EnvVarCreator {

    public NameValuePair getEnvVar(JobStarterOptions inCondition, String envVarName) {
        String functionName = envVarName.toUpperCase();
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

        NameValuePair envVar = new NameValuePair();
        java.util.Calendar calendar = new GregorianCalendar();
        Date today = new Date();
        calendar.setTime(today);

        switch (functionName) {
        case "JS_JOBSTREAM":
            envVar.setName(envVarName);
            envVar.setValue(inCondition.getJobStream());
            return envVar;
        case "JS_TIME":
            outFormat = new SimpleDateFormat("HH:mm:ss");
            envVar.setName("JS_TIME");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;

        case "JS_CENTURY":
            outFormat = new SimpleDateFormat("yyyy");
            envVar.setName("JS_CENTURY");
            envVar.setValue(outFormat.format(calendar.getTime()).substring(0,2));
            return envVar;
        case "JS_DAY":
            outFormat = new SimpleDateFormat("dd");
            envVar.setName("JS_DAY");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "JS_YEAR":
        case "JS_YEAR_YYYY":
            outFormat = new SimpleDateFormat("yyyy");
            envVar.setName("JS_YEAR_YYYY");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "JS_YEAR_YY":
            outFormat = new SimpleDateFormat("yy");
            envVar.setName("JS_YEAR_YY");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "JS_MONTH":
            outFormat = new SimpleDateFormat("MM");
            envVar.setName("JS_MONTH");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "JS_MONTH_NAME":
            envVar.setName("JS_MONTH_NAME");
            envVar.setValue(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
            return envVar;
        case "JS_DATE_YY":
            outFormat = new SimpleDateFormat("yyMMdd");
            envVar.setName("JS_DATE_YY");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "JS_DATE":
        case "JS_DATE_YYYY":
            outFormat = new SimpleDateFormat("yyyyMMdd");
            envVar.setName("JS_DATE_YYYY");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "JS_FOLDER":
            envVar.setName(envVarName);
            envVar.setValue(Globals.getParent(inCondition.getJob()));
            return envVar;
        case "JS_JOBNAME":
            envVar.setName(envVarName);
            envVar.setValue(inCondition.getJob());
            return envVar;
        default:
            break;
        }
        return null;
    }
}
