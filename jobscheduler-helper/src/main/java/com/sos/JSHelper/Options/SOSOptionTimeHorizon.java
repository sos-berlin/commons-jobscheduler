package com.sos.JSHelper.Options;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author oh */
public class SOSOptionTimeHorizon extends SOSOptionString {

    private static final long serialVersionUID = -5511394144386870461L;
    private static final String ISO_PATTERN = "(\\d{4})-(\\d{1,2})-(\\d{1,2})(?:[ T](\\d{1,2}):(\\d{1,2})(?::(\\d{1,2}))?)?";
    private static final String PERIOD_PATTERN = "([+-]?\\d+)(?::(\\d{1,2}):(\\d{1,2})(?::(\\d{1,2}))?)?";

    public SOSOptionTimeHorizon(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
            boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public Date getDateObject() {
        Calendar objCal = Calendar.getInstance();
        Matcher objIsoDateMatcher = Pattern.compile(ISO_PATTERN).matcher(strValue);
        Matcher objPeriodMatcher = Pattern.compile(PERIOD_PATTERN).matcher(strValue);
        int seconds = 0;
        if (objIsoDateMatcher.find()) {
            objCal.set(Integer.parseInt(objIsoDateMatcher.group(1)), Integer.parseInt(objIsoDateMatcher.group(2)) - 1,
                    Integer.parseInt(objIsoDateMatcher.group(3)));
            if (isNotEmpty(objIsoDateMatcher.group(4))) {
                objCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(objIsoDateMatcher.group(4)));
                objCal.set(Calendar.MINUTE, Integer.parseInt(objIsoDateMatcher.group(5)));
                if (isNotEmpty(objIsoDateMatcher.group(6))) {
                    seconds = Integer.parseInt(objIsoDateMatcher.group(6));
                }
                objCal.set(Calendar.SECOND, seconds);
                objCal.set(Calendar.MILLISECOND, 0);
            }
        } else if (objPeriodMatcher.find()) {
            objCal.setTimeInMillis(System.currentTimeMillis() + (Long.parseLong(objPeriodMatcher.group(1).replace("+", "")) * 24 * 60 * 60 * 1000));
            if (isNotEmpty(objPeriodMatcher.group(2))) {
                objCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(objPeriodMatcher.group(2)));
                objCal.set(Calendar.MINUTE, Integer.parseInt(objPeriodMatcher.group(3)));
                if (isNotEmpty(objPeriodMatcher.group(4))) {
                    seconds = Integer.parseInt(objPeriodMatcher.group(4));
                }
                objCal.set(Calendar.SECOND, seconds);
                objCal.set(Calendar.MILLISECOND, 0);
            }
        } else {
            throw new JobSchedulerException(String.format(
                    "%1$s must be in the format yyyy-MM-dd[ HH:mm[:ss]] or <+/-number of days from now>[:HH:mm[:ss]]", strValue));
        }
        return objCal.getTime();
    }

    public Date getEndFromNow() {
        Calendar result = Calendar.getInstance();
        Matcher objPeriodMatcher = Pattern.compile(PERIOD_PATTERN).matcher(strValue);
        int seconds = 0;
        if (objPeriodMatcher.find()) {
            if (isNotEmpty(objPeriodMatcher.group(1))) {
                result.add(Calendar.DAY_OF_YEAR, Integer.parseInt(objPeriodMatcher.group(1).replace("+", "")));
            }
            if (isNotEmpty(objPeriodMatcher.group(2))) {
                result.add(Calendar.HOUR, Integer.parseInt(objPeriodMatcher.group(2)));
                result.add(Calendar.MINUTE, Integer.parseInt(objPeriodMatcher.group(3)));
                if (isNotEmpty(objPeriodMatcher.group(4))) {
                    seconds = Integer.parseInt(objPeriodMatcher.group(4));
                }
                result.add(Calendar.SECOND, seconds);
            }
        } else {
            throw new JobSchedulerException(String.format("%1$s must be in the format <+/-number of days from now>[:HH:mm[:ss]]", strValue));
        }
        return result.getTime();
    }

}