package com.sos.JSHelper.System;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class CPUTime {

    private long starttime;
    private long stoptime;
    private ThreadMXBean tb = null;

    public CPUTime() {
        tb = ManagementFactory.getThreadMXBean();
        startTimer();
    }

    public void startTimer() {
        starttime = tb.getCurrentThreadCpuTime();
    }

    public void stopTimer() {
        stoptime = tb.getCurrentThreadCpuTime();
    }

    public long getStopTime() {
        return stoptime;
    }

    public long timeUsed() {
        long lngTimeDiff = 0;
        long lngT = 0;
        lngT = tb.getCurrentThreadCpuTime();
        lngTimeDiff = lngT - starttime;
        return lngTimeDiff;
    }

    public String toString() {
        return String.format("CPU-time used %1$8.4f ms", (double) this.timeUsed() / 1000000);
    }

}