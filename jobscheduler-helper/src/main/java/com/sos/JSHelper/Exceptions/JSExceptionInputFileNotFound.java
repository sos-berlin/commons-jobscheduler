package com.sos.JSHelper.Exceptions;

public class JSExceptionInputFileNotFound extends JobSchedulerException {

    private static final long serialVersionUID = -3429713020015418931L;

    public JSExceptionInputFileNotFound(String msg) {
        super(msg);
    }

    public JSExceptionInputFileNotFound() {
        super("Input File not found");
    }

}
