package com.sos.JSHelper.Exceptions;

public class JSExceptionFileNotReadable extends JobSchedulerException {

    private static final long serialVersionUID = 3028342712536869075L;

    public JSExceptionFileNotReadable(String msg) {
        super(msg);
    }

    public JSExceptionFileNotReadable() {
        super("File is not readable");
    }

}
