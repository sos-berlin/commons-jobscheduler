package com.sos.eventhandlerservice.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSReturnCodeResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSReturnCodeResolver.class);

    public JSReturnCodeResolver() {
        super();
    }

    private boolean isInInterval(int taskReturnCode, String interval) {
        interval = interval.trim();
        if (interval.startsWith("-")) {
            interval = "0" + interval;
        }
        if (interval.endsWith("-")) {
            interval = interval + "255";
        }
        String[] limits = interval.split("-");
        if (limits.length == 1) {
            try {
                int ret = Integer.parseInt(limits[0]);
                return ret == taskReturnCode;
            } catch (NumberFormatException e) {
                LOGGER.warn("Illegal interval for returncode: " + interval);
            }
        } else {
            if (limits.length == 2) {
                try {
                    int ret1 = Integer.parseInt(limits[0]);
                    int ret2 = Integer.parseInt(limits[1]);
                    return (ret2 >= taskReturnCode && ret1 <= taskReturnCode);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Illegal interval for returncode: " + interval);
                }
            } else {
                LOGGER.warn("Illegal interval for returncode: " + interval);
            }
        }

        return false;
    }

    public boolean resolve(int taskReturnCode, String returnCode) {
        String[] intervals = returnCode.split(",");
        for (int i = 0; i < intervals.length; i++) {
            if (isInInterval(taskReturnCode, intervals[i])) {
                return true;
            }
        }
        return false;
    }

}
