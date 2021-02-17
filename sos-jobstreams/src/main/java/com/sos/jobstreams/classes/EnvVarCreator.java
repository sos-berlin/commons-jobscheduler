package com.sos.jobstreams.classes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.sos.joc.Globals;
import com.sos.joc.model.common.NameValuePair;

class EnvVarCreator {

    public NameValuePair getEnvVar(JobStarterOptions jobStarterOptions, String envVarName) {
        String functionName = envVarName.toUpperCase();
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

        NameValuePair envVar = new NameValuePair();
        java.util.Calendar calendar = new GregorianCalendar();
        Date today = new Date();
        calendar.setTime(today);

        envVar.setName(functionName);

        switch (functionName) {
        case "SCHEDULER_JOBSTREAM_INSTANCE_ID":
            envVar.setValue(jobStarterOptions.getInstanceId());
            return envVar;
        case "SCHEDULER_JOBSTREAM_JOBSTREAM":
            envVar.setValue(jobStarterOptions.getJobStream());
            return envVar;
        case "SCHEDULER_JOBSTREAM_TIME":
            outFormat = new SimpleDateFormat("HH:mm:ss");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;

        case "SCHEDULER_JOBSTREAM_CENTURY":
            outFormat = new SimpleDateFormat("yyyy");
            envVar.setValue(outFormat.format(calendar.getTime()).substring(0,2));
            return envVar;
        case "SCHEDULER_JOBSTREAM_DAY":
            outFormat = new SimpleDateFormat("dd");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "SCHEDULER_JOBSTREAM_YEAR":
        case "SCHEDULER_JOBSTREAM_YEAR_YYYY":
            outFormat = new SimpleDateFormat("yyyy");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "SCHEDULER_JOBSTREAM_YEAR_YY":
            outFormat = new SimpleDateFormat("yy");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "SCHEDULER_JOBSTREAM_MONTH":
            outFormat = new SimpleDateFormat("MM");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "SCHEDULER_JOBSTREAM_MONTH_NAME":
            envVar.setValue(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.UK));
            return envVar;
        case "SCHEDULER_JOBSTREAM_DATE_YY":
            outFormat = new SimpleDateFormat("yyMMdd");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "SCHEDULER_JOBSTREAM_DATE":
        case "SCHEDULER_JOBSTREAM_DATE_YYYY":
            outFormat = new SimpleDateFormat("yyyyMMdd");
            envVar.setValue(outFormat.format(calendar.getTime()));
            return envVar;
        case "SCHEDULER_JOBSTREAM_FOLDER":
            envVar.setValue(Globals.getParent(jobStarterOptions.getJob()));
            return envVar;
        case "SCHEDULER_JOBSTREAM_JOBNAME":
            envVar.setValue(jobStarterOptions.getJob());
            return envVar;
        default:
            break;
        }
        return null;
    }
}
