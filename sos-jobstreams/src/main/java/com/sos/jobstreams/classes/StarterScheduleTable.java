package com.sos.jobstreams.classes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobstreams.resolver.JSJobStreamStarter;

public class StarterScheduleTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StarterScheduleTable.class);

    private Map<StarterScheduleKey, StarterScheduleTableItem> listOfScheduledStarters;

    public StarterScheduleTable() {
        super();
        listOfScheduledStarters = new ConcurrentHashMap<StarterScheduleKey, StarterScheduleTableItem>();
    }

    public void markAsStarted(JSJobStreamStarter starter) {
        if (starter.getNextStart() == null) {
            return;
        }
        StarterScheduleKey starterScheduleKey = new StarterScheduleKey();
        starterScheduleKey.scheduledFor = starter.getNextStart().getTime();
        starterScheduleKey.starterName = starter.getStarterName();
        StarterScheduleTableItem starterScheduleTableItem = listOfScheduledStarters.get(starterScheduleKey);
        if (starterScheduleTableItem == null) {
            starterScheduleTableItem = new StarterScheduleTableItem();
            starterScheduleTableItem.setStarter(starter);
            starterScheduleTableItem.setStartTime(starter.getNextStart().getTime());
        }
        starterScheduleTableItem.setStarted(true);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(starterScheduleKey.scheduledFor);

        LOGGER.debug(starterScheduleKey.starterName + " --> " + calendar + " marked as started");

        listOfScheduledStarters.put(starterScheduleKey, starterScheduleTableItem);
    }

    public boolean isMarkedAsStarted(JSJobStreamStarter starter) {
        StarterScheduleKey starterScheduleKey = new StarterScheduleKey();
        if (starter.getNextStart() == null) {
            return false;
        }
        starterScheduleKey.scheduledFor = starter.getNextStart().getTime();
        starterScheduleKey.starterName = starter.getStarterName();
        StarterScheduleTableItem starterScheduleTableItem = listOfScheduledStarters.get(starterScheduleKey);

        return (starterScheduleTableItem != null) && (starterScheduleTableItem.isStarted());
    }

    public void addStarter(JSJobStreamStarter starter) {
        Date now = new Date();
        if (starter.getJobStreamScheduler() != null && starter.getJobStreamScheduler().getListOfStartTimes() != null) {
            for (Long startTime : starter.getJobStreamScheduler().getListOfStartTimes()) {
                if (startTime > now.getTime()) {
                    StarterScheduleKey starterScheduleKey = new StarterScheduleKey();

                    starterScheduleKey.scheduledFor = startTime;
                    starterScheduleKey.starterName = starter.getStarterName();
                    StarterScheduleTableItem starterScheduleTableItem = new StarterScheduleTableItem();
                    starterScheduleTableItem.setStarted(false);
                    starterScheduleTableItem.setStarter(starter);
                    starterScheduleTableItem.setStartTime(starterScheduleKey.scheduledFor);
                    if (listOfScheduledStarters.get(starterScheduleKey) == null) {

                        listOfScheduledStarters.put(starterScheduleKey, starterScheduleTableItem);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(starterScheduleKey.scheduledFor);

                        LOGGER.trace(starterScheduleKey.starterName + " --> " + calendar.getTime());
                    }
                }
            }
        }
    }

    public JSJobStreamStarter getLateStarter(JSJobStreamStarter nextStarter) {
        Long now = new Date().getTime() - 2 * 60 * 120;
        StarterScheduleKey starterScheduleKey = new StarterScheduleKey();
        starterScheduleKey.scheduledFor = nextStarter.getNextStart().getTime();
        starterScheduleKey.starterName = nextStarter.getStarterName();

        for (Entry<StarterScheduleKey, StarterScheduleTableItem> entry : listOfScheduledStarters.entrySet()) {
            if (!starterScheduleKey.equals(entry.getKey()) && !entry.getValue().isStarted() && entry.getValue().getStartTime() < now) {
                return entry.getValue().getStarter();
            }
        }
        return null;
    }

    public List<JSJobStreamStarter> getLateStarters(JSJobStreamStarter nextStarter) {
        Date nowDate = new Date();
        Long now = nowDate.getTime() - 2 * 60 * 1000;

        StarterScheduleKey starterScheduleKey = new StarterScheduleKey();
        starterScheduleKey.scheduledFor = nextStarter.getNextStart().getTime();
        starterScheduleKey.starterName = nextStarter.getStarterName();

        List<JSJobStreamStarter> resultList = new ArrayList<JSJobStreamStarter>();

        for (Entry<StarterScheduleKey, StarterScheduleTableItem> entry : listOfScheduledStarters.entrySet()) {

            if (!starterScheduleKey.equals(entry.getKey()) && !entry.getValue().isStarted() && entry.getValue().getStartTime() < now) {
                resultList.add(entry.getValue().getStarter());
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(entry.getKey().scheduledFor);

                LOGGER.debug(entry.getValue().getStartTime() + "<" + now + " " + entry.getKey().starterName + " " + calendar.getTime()
                        + " is late and before " + nowDate + ". Will be started");
            }
        }
        return resultList;
    }

}
