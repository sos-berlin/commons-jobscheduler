package com.sos.graphviz.main;

public class Dot {

    public static final String OS = System.getProperty("os.name");
    public static final boolean isWindows = OS.startsWith("Windows");
    public static final String Command = isWindows ? "dot.exe" : "dot";
}
