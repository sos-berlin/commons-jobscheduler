package com.sos.JSHelper.System;

public class JRuntimeWrapper {

    public static String getMemoryInfoString() {
        Runtime runtime = Runtime.getRuntime();
        return "Memory-Info=>Free:" + runtime.freeMemory() + ", Max:" + runtime.maxMemory() + ", Total:" + runtime.totalMemory();
    }

}
