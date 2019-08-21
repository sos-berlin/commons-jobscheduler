package com.sos.jobstreams.classes;

public class JobStartCommand {
	private String job;

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getCommandParam() {
		return commandParam;
	}

	public void setCommandParam(String commandParam) {
		if (getCommandParam() == null || getCommandParam().isEmpty()) {
			this.commandParam = "now";
		} else {
			this.commandParam = commandParam;
		}
	}

	private String commandParam;

}
