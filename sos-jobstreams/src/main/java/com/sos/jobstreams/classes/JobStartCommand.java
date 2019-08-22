package com.sos.jobstreams.classes;

public class JobStartCommand {
	private String job;
    private String commandParam;

	
	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = normalizePath(job);
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

    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        return ("/" + path.trim()).replaceAll("//+", "/").replaceFirst("/$", "");
    }

}
