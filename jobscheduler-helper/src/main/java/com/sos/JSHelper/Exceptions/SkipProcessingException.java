package com.sos.JSHelper.Exceptions;

/** @author Ghassan Beydoun */
public class SkipProcessingException extends JobSchedulerException {

    private static final long serialVersionUID = 1L;

    public SkipProcessingException(String message) {
        super(message);
        this.Status(JobSchedulerException.SKIPPROCESSING);
        this.eMailSubject("Skip processing.");
    }

    public SkipProcessingException() {
        this("Processing skipped, current tasklevel is not equal input level.");
    }

}