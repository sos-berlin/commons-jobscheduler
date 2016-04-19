package com.sos.JSHelper.System;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class CPUTime {

    private long starttime;
    private long stoptime;
    private ThreadMXBean tb = null;

    public CPUTime() {
        tb = ManagementFactory.getThreadMXBean();
        StartTimer();
    }

    public void StartTimer() {
        starttime = tb.getCurrentThreadCpuTime();
    }

    public void StopTimer() {
        stoptime = tb.getCurrentThreadCpuTime();
    }

    public long getStopTime() {
        return stoptime;
    }

    public long TimeUsed() {
        long lngTimeDiff = 0;
        long lngT = 0;
        lngT = tb.getCurrentThreadCpuTime();
        lngTimeDiff = lngT - starttime;
        return lngTimeDiff;
    }

    public String toString() {
        return String.format("CPU-time used %1$8.4f ms", (double) this.TimeUsed() / 1000000);
    }

}