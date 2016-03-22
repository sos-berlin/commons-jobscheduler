package com.sos.JSHelper.System;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class CPUTime {

    @SuppressWarnings("unused")
    private final String conClassName = "CPUTime";

    @SuppressWarnings("unused")
    private long starttime, stoptime, timeused;
    private ThreadMXBean tb = null;

    public CPUTime() {
        /** --------------------------------------------------------------------
         * ------- <method type="smcw" version="1.0"> <name></name>
         * <title>CPUTime</title> <description> <para> CPUTime </para>
         * </description> <params> </params> <keywords>
         * <keyword>CPUTime</keyword> </keywords> <categories>
         * <category>SystemManagement</category> </categories> </method>
         * --------
         * -------------------------------------------------------------------- */
        tb = ManagementFactory.getThreadMXBean();
        StartTimer();

    } // public CPUTime

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
        String strT;

        strT = String.format("CPU-time used %1$8.4f ms", (double) this.TimeUsed() / 1000000);
        return strT;
    }
} // public class CPUTime
