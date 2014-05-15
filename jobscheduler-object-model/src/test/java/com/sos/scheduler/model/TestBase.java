package com.sos.scheduler.model;

import java.io.File;
import java.net.URL;

import com.sos.JSHelper.Logging.Log4JHelper;

public class TestBase {

	public TestBase() {
		new Log4JHelper("./log4j.properties");
	}
	
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
