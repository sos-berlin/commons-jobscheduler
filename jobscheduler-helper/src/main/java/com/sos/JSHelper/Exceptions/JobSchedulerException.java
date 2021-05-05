package com.sos.JSHelper.Exceptions;

import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com.sos.JSHelper.messages", defaultLocale = "en")
public class JobSchedulerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public static String LastErrorMessage = "";

    private String message = null;
    private Throwable nestedException;

    public JobSchedulerException() {
        super("*** JobSchedulerException ***");
    }

    public JobSchedulerException(final String msg) {
        super(msg);
        setMessage(msg);
    }

    public JobSchedulerException(final String msg, final Throwable e) {
        super(msg, e);
        setMessage(msg + " (" + e.getMessage() + ")");
        nestedException = e;
    }

    public JobSchedulerException(final Throwable e) {
        super(e);
        setMessage(e.getMessage());
        nestedException = e;
    }

    public void setMessage(final String val) {
        message = val;
        LastErrorMessage += val + "\n";
    }

    public Throwable getNestedException() {
        return nestedException;
    }

    public String getMessage() {
        return message;
    }

    public JobSchedulerException message(final String val) {
        message = val;
        return this;
    }

    public String getExceptionText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Message :").append(message);
        sb.append("\n\n");
        if (nestedException != null) {
            sb.append("\n").append(nestedException.toString());
            sb.append("\nStackTrace of nested Exception:\n").append(stackTrace2String(nestedException)).append("\n");
        } else {
            sb.append("\nStackTrace :\n").append(stackTrace2String(this)).append("\n");
        }
        return sb.toString();
    }

    private String stackTrace2String(final Throwable e) {
        StringBuilder sb = new StringBuilder();
        final StackTraceElement stacks[] = e.getStackTrace();
        for (final StackTraceElement stack : stacks) {
            sb.append(stack.toString()).append("\n");
        }
        return sb.toString();
    }

}