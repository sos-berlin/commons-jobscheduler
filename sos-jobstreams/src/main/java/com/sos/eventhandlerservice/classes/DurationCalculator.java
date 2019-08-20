package com.sos.eventhandlerservice.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DurationCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DurationCalculator.class);

    private long timeStart = 0;

    public DurationCalculator() {
        super();
        timeStart = System.currentTimeMillis();
    }

    public void end(String prompt) {
        final long timeEnd = System.currentTimeMillis();
        LOGGER.debug(prompt + (timeEnd - timeStart) + " ms.");
    }

}
