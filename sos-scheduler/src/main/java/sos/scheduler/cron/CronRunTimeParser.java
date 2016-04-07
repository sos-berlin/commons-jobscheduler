package sos.scheduler.cron;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class CronRunTimeParser extends JSToolBox {

    protected Pattern cronRegExPattern;
    private static final Logger LOGGER = Logger.getLogger(CronRunTimeParser.class);
    private static final String CRON_REG_EX = "-?([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+(.+)$";
    private String strCronLine = "";
    private boolean flgUsedNewRunTime = false;
    private Matcher cronRegExMatcher = null;
    private Element eleRunTimeElement = null;
    private DocumentBuilder docBuilder = null;

    public CronRunTimeParser(final String pstrCronLine) {
        cronRegExPattern = Pattern.compile(CRON_REG_EX);
        setBuffer(pstrCronLine);
    }

    public void setBuffer(final String pstrCronLine) {
        strCronLine = pstrCronLine;
        cronRegExMatcher = cronRegExPattern.matcher(strCronLine);
        flgUsedNewRunTime = false;
    }

    public boolean getUseNewRunTime() {
        return flgUsedNewRunTime;
    }

    public String getRunTimeAsXML() {
        String strR = "";
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
            Document runTimeDocument = docBuilder.newDocument();
            runTimeDocument.appendChild(runTimeDocument.importNode(eleRunTimeElement, true));
            StringWriter out = new StringWriter();
            OutputFormat format = new OutputFormat(runTimeDocument);
            format.setIndenting(true);
            format.setIndent(2);
            format.setOmitXMLDeclaration(true);
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(runTimeDocument);
            strR = out.getBuffer().toString();
        } catch (DOMException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return strR;
    }

    public void createRunTime(final Element runTimeElement, final String pstrCronLine) {
        setBuffer(pstrCronLine);
        try {
            createRunTime(runTimeElement);
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    public void createRunTime(final Element runTimeElement) throws Exception {
        try {
            if (!cronRegExMatcher.matches()) {
                throw new JobSchedulerException("Fail to parse cron line \"" + strCronLine + "\", regexp is " + cronRegExMatcher.toString());
            }
            eleRunTimeElement = runTimeElement;
            String minutes = cronRegExMatcher.group(1);
            String hours = cronRegExMatcher.group(2);
            String days = cronRegExMatcher.group(3);
            String months = cronRegExMatcher.group(4);
            String weekdays = cronRegExMatcher.group(5);
            if ("@reboot".equals(minutes)) {
                runTimeElement.setAttribute("once", "yes");
                return;
            }
            Vector<Element> childElements = new Vector<Element>();
            Element periodElement = runTimeElement.getOwnerDocument().createElement("period");
            LOGGER.debug("processing hours [" + hours + "] and minutes [" + minutes + "]");
            if (minutes.startsWith("*")) {
                if ("*".equalsIgnoreCase(minutes)) {
                    periodElement.setAttribute("repeat", "60");
                } else {
                    String repeat = minutes.substring(2);
                    repeat = formatTwoDigits(repeat);
                    periodElement.setAttribute("repeat", "00:" + repeat);
                }
                if (hours.startsWith("*")) {
                    if (!"*".equalsIgnoreCase(hours)) {
                        throw new JobSchedulerException("Combination of minutes and hours not supported: " + minutes + " " + hours);
                    }
                    childElements.add(periodElement);
                } else {
                    LOGGER.debug("Found specific hours, creating periods with begin and end.");
                    String[] hourArray = hours.split(",");
                    for (int i = 0; i < hourArray.length; i++) {
                        String currentHour = hourArray[i];
                        if (currentHour.indexOf("/") != -1) {
                            String[] additionalHours = getArrayFromColumn(currentHour);
                            hourArray = combineArrays(hourArray, additionalHours);
                            continue;
                        }
                        String[] currentHourArray = currentHour.split("-");
                        Element currentPeriodElement = (Element) periodElement.cloneNode(true);
                        String beginHour = currentHourArray[0];
                        int iEndHour = (Integer.parseInt(beginHour) + 1) % 24;
                        if (iEndHour == 0) {
                            iEndHour = 24;
                        }
                        String endHour = "" + iEndHour;
                        if (currentHourArray.length > 1) {
                            endHour = currentHourArray[1];
                        }
                        beginHour = formatTwoDigits(beginHour);
                        endHour = formatTwoDigits(endHour);
                        currentPeriodElement.setAttribute("begin", beginHour + ":00");
                        currentPeriodElement.setAttribute("end", endHour + ":00");
                        childElements.add(currentPeriodElement);
                    }
                }
            } else {
                String[] minutesArray = getArrayFromColumn(minutes);
                for (String element : minutesArray) {
                    Element currentPeriodElement = (Element) periodElement.cloneNode(true);
                    String currentMinute = element;
                    currentMinute = formatTwoDigits(currentMinute);
                    if (hours.startsWith("*")) {
                        currentPeriodElement.setAttribute("absolute_repeat", "01:00");
                        flgUsedNewRunTime = true;
                        if (!"*".equalsIgnoreCase(hours)) {
                            String repeat = hours.substring(2);
                            repeat = formatTwoDigits(repeat);
                            currentPeriodElement.setAttribute("absolute_repeat", repeat + ":00");
                        }
                        currentPeriodElement.setAttribute("begin", "00:" + currentMinute);
                        childElements.add(currentPeriodElement);
                    } else {
                        String[] hourArray = hours.split(",");
                        for (String element2 : hourArray) {
                            currentPeriodElement = (Element) periodElement.cloneNode(true);
                            String currentHour = element2;
                            if (currentHour.indexOf("-") == -1) {
                                currentHour = formatTwoDigits(currentHour);
                                currentPeriodElement.setAttribute("single_start", currentHour + ":" + currentMinute);
                            } else {
                                String[] currentHourArray = currentHour.split("[-/]");
                                int beginHour = Integer.parseInt(currentHourArray[0]);
                                int endHour = Integer.parseInt(currentHourArray[1]);
                                int beginMinute = Integer.parseInt(currentMinute);
                                int endMinute = beginMinute + 1;
                                endMinute = beginMinute;
                                if (endMinute == 60) {
                                    endMinute = 0;
                                    endHour = endHour + 1;
                                }
                                endHour = endHour % 24;
                                if (endHour == 0) {
                                    endHour = 24;
                                }
                                String stepSize = "1";
                                if (currentHourArray.length == 3) {
                                    stepSize = formatTwoDigits(currentHourArray[2]);
                                }
                                currentPeriodElement.setAttribute("absolute_repeat", stepSize + ":00");
                                flgUsedNewRunTime = true;
                                currentPeriodElement.setAttribute("begin", formatTwoDigits(beginHour) + ":" + formatTwoDigits(beginMinute));
                                currentPeriodElement.setAttribute("end", formatTwoDigits(endHour) + ":" + formatTwoDigits(endMinute));
                            }
                            childElements.add(currentPeriodElement);
                        }
                    }
                }
            }
            LOGGER.debug("processing days [" + days + "]");
            boolean monthDaysSet = false;
            if (days.startsWith("*")) {
                if (!"*".equals(days)) {
                    Element monthDaysElement = runTimeElement.getOwnerDocument().createElement("monthdays");
                    String repeat = days.substring(2);
                    int iRepeat = Integer.parseInt(repeat);
                    for (int i = 1; i <= 30; i = i + iRepeat) {
                        String day = "" + i;
                        addDay(day, monthDaysElement, childElements);
                    }
                    childElements.clear();
                    childElements.add(monthDaysElement);
                    monthDaysSet = true;
                }
            } else {
                Element monthDaysElement = runTimeElement.getOwnerDocument().createElement("monthdays");
                String[] daysArray = getArrayFromColumn(days);
                for (String day : daysArray) {
                    addDay(day, monthDaysElement, childElements);
                }
                childElements.clear();
                childElements.add(monthDaysElement);
                monthDaysSet = true;
            }
            if (!"*".equals(weekdays) && monthDaysSet) {
                LOGGER.info("Weekdays will not be processed as days are already set in current line.");
            } else {
                LOGGER.debug("processing weekdays [" + weekdays + "]");
                weekdays = replaceDayNames(weekdays);
                if (weekdays.startsWith("*/")) {
                    throw new JobSchedulerException("Repeat intervals for the weekdays column [" + weekdays
                            + "] are not supported. Please use the days column.");
                }
                if (!"*".equals(weekdays)) {
                    Element weekDaysElement = runTimeElement.getOwnerDocument().createElement("weekdays");
                    String[] daysArray = getArrayFromColumn(weekdays);
                    for (String day : daysArray) {
                        addDay(day, weekDaysElement, childElements);
                    }
                    childElements.clear();
                    childElements.add(weekDaysElement);
                }
            }
            LOGGER.debug("processing months [" + months + "]");
            if (months.startsWith("*")) {
                if (!"*".equals(months)) {
                    months = replaceMonthNames(months);
                    Vector<Element> newChildElements = new Vector<Element>();
                    String repeat = months.substring(2);
                    int iRepeat = Integer.parseInt(repeat);
                    for (int i = 1; i <= 12; i = i + iRepeat) {
                        String month = "" + i;
                        Element monthElement = runTimeElement.getOwnerDocument().createElement("month");
                        flgUsedNewRunTime = true;
                        monthElement.setAttribute("month", month);
                        Iterator<Element> iter = childElements.iterator();
                        while (iter.hasNext()) {
                            Element child = iter.next();
                            monthElement.appendChild(child.cloneNode(true));
                        }
                        newChildElements.add(monthElement);
                    }
                    childElements = newChildElements;
                }
            } else {
                Vector<Element> newChildElements = new Vector<Element>();
                String[] monthArray = getArrayFromColumn(months);
                for (String month : monthArray) {
                    Element monthElement = runTimeElement.getOwnerDocument().createElement("month");
                    flgUsedNewRunTime = true;
                    monthElement.setAttribute("month", month);
                    Iterator<Element> iter = childElements.iterator();
                    while (iter.hasNext()) {
                        Element child = iter.next();
                        monthElement.appendChild(child.cloneNode(true));
                    }
                    newChildElements.add(monthElement);
                }
                childElements = newChildElements;
            }
            Iterator<Element> iter = childElements.iterator();
            while (iter.hasNext()) {
                Element someElement = iter.next();
                runTimeElement.appendChild(someElement);
            }
        } catch (Exception e) {
            throw new JobSchedulerException("Error creating run time: " + e, e);
        }
    }

    private String[] combineArrays(final String[] hourArray, final String[] additionalHours) {
        String[] newArray = new String[hourArray.length + additionalHours.length];
        for (int i = 0; i < hourArray.length; i++) {
            newArray[i] = hourArray[i];
        }
        for (int i = 0; i < additionalHours.length; i++) {
            newArray[i + hourArray.length] = additionalHours[i];
        }
        return newArray;
    }

    private void addDay(final String day, final Element parentDaysElement, final Vector<Element> childElements) throws Exception {
        LOGGER.debug("adding day: " + day);
        Element dayElement = parentDaysElement.getOwnerDocument().createElement("day");
        dayElement.setAttribute("day", day);
        Iterator<Element> iter = childElements.iterator();
        while (iter.hasNext()) {
            Element child = iter.next();
            dayElement.appendChild(child.cloneNode(true));
        }
        parentDaysElement.appendChild(dayElement);
    }

    private String[] getArrayFromColumn(final String column) {
        String[] elements = column.split(",");
        Vector<String> result = new Vector<String>();
        for (String element : elements) {
            if (element.indexOf("-") == -1) {
                result.add(element);
            } else {
                String[] range = element.split("[-/]");
                if (range.length < 2 || range.length > 3) {
                    try {
                        LOGGER.warn("unknown crontab synthax: " + element);
                    } catch (Exception e) {
                    }
                } else {
                    int from = Integer.parseInt(range[0]);
                    int to = Integer.parseInt(range[1]);
                    int stepSize = 1;
                    if (range.length == 3) {
                        stepSize = Integer.parseInt(range[2]);
                    }
                    for (int j = from; j <= to; j = j + stepSize) {
                        result.add("" + j);
                    }
                }
            }
        }
        String[] dummy = new String[1];
        return result.toArray(dummy);
    }

    private String replaceDayNames(String element) {
        element = element.replaceAll("(?i)mon", "1");
        element = element.replaceAll("(?i)tue", "2");
        element = element.replaceAll("(?i)wed", "3");
        element = element.replaceAll("(?i)thu", "4");
        element = element.replaceAll("(?i)fri", "5");
        element = element.replaceAll("(?i)sat", "6");
        element = element.replaceAll("(?i)sun", "7");
        return element;
    }

    private String replaceMonthNames(String element) {
        element = element.replaceAll("(?i)jan", "1");
        element = element.replaceAll("(?i)feb", "2");
        element = element.replaceAll("(?i)mar", "3");
        element = element.replaceAll("(?i)apr", "4");
        element = element.replaceAll("(?i)may", "5");
        element = element.replaceAll("(?i)jun", "6");
        element = element.replaceAll("(?i)jul", "7");
        element = element.replaceAll("(?i)aug", "8");
        element = element.replaceAll("(?i)sep", "9");
        element = element.replaceAll("(?i)oct", "10");
        element = element.replaceAll("(?i)nov", "11");
        element = element.replaceAll("(?i)dec", "12");
        return element;
    }

    private String formatTwoDigits(final String number) {
        if (number.length() == 1) {
            return "0" + number;
        }
        return number;
    }

    private String formatTwoDigits(final int number) {
        return formatTwoDigits("" + number);
    }

}