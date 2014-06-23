package com.sos.JSHelper.Basics;

public class VersionInfo {
    public final static String ENGINE_VERSION = "${sos.engine.version}";
    public final static String JAR_VERSION = "${project.version}";
    public final static String SVN_REVISION = "${svn.build.number}";
    public final static String TIME_STAMP = "${build.timestamp}";
    
    public final static String VERSION_STRING = JAR_VERSION + " (" + TIME_STAMP + ", revision " + SVN_REVISION + ") Copyright 2003-2014 SOS GmbH Berlin";
    public final static String ENGINE_STRING = "This jar was build with Version " + ENGINE_VERSION + " of JobScheduler engine.";
    
    private VersionInfo() {
    }
    
}
