package com.sos.scheduler.model;

import java.io.File;
import java.net.URL;

public class TestBase {

    public static String getResourceFolder() {
        StringBuffer sb = new StringBuffer();
        sb.append(System.getProperty("user.dir"));
        sb.append("/testdata/");
        return sb.toString().replace("\\", "/");
    }

    public File getLiveFolder() {
        return new File(getResource(""));
    }

    public String getResource(String schedulerObjectName) {
        URL url = this.getClass().getClassLoader().getResource("live/" + schedulerObjectName);
        return url.toExternalForm().replace("file:/", "");
    }

}
