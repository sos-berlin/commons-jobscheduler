package com.sos.tools.logback.db;

import com.sos.hibernate.classes.DbItem;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "LOGGING_EVENT")
public class LoggingEventDBItem extends DbItem {

    private Long eventId;
    private Long timeStmp;
    private String formattedMessage;
    private String loggerName;
    private String threadName;
    private String levelString;
    private Long referenceFlag;
    private String arg0;
    private String arg1;
    private String arg2;
    private String arg3;
    private String callerFilename;
    private String callerClass;
    private String callerMethod;
    private Integer callerLine;
    private List<LoggingEventPropertyDBItem> loggingEventProperties;
    private List<LoggingEventExceptionDBItem> loggingEventExceptions;

    public LoggingEventDBItem() {
        super();
    }

    @Id
    @Column(name = "EVENT_ID")
    public Long getEventId() {
        return eventId;
    }

    @Id
    @Column(name = "EVENT_ID")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @Column(name = "TIMESTMP")
    public Long getTimeStmp() {
        return timeStmp;
    }

    @Column(name = "TIMESTMP")
    public void setTimeStmp(Long timeStmp) {
        this.timeStmp = timeStmp;
    }

    @Column(name = "FORMATTED_MESSAGE")
    public String getFormattedMessage() {
        return formattedMessage;
    }

    @Column(name = "FORMATTED_MESSAGE")
    public void setFormattedMessage(String formattedMessae) {
        this.formattedMessage = formattedMessae;
    }

    @Column(name = "THREAD_NAME")
    public String getThreadName() {
        return threadName;
    }

    @Column(name = "THREAD_NAME")
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Column(name = "LOGGER_NAME")
    public String getLoggerName() {
        return loggerName;
    }

    @Column(name = "LOGGER_NAME")
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    @Column(name = "LEVEL_STRING")
    public String getLevelString() {
        return levelString;
    }

    @Column(name = "LEVEL_STRING")
    public void setLevelString(String levelString) {
        this.levelString = levelString;
    }

    @Column(name = "CALLER_LINE")
    public Integer getCallerLine() {
        return callerLine;
    }

    @Column(name = "CALLER_LINE")
    public void setCallerLine(Integer callerLine) {
        this.callerLine = callerLine;
    }

    @Column(name = "REFERENCE_FLAG")
    public Long getReferenceFlag() {
        return referenceFlag;
    }

    @Column(name = "REFERENCE_FLAG")
    public void setReferenceFlag(Long referenceFlag) {
        this.referenceFlag = referenceFlag;
    }

    @Column(name = "ARG0")
    public String getArg0() {
        return arg0;
    }

    @Column(name = "ARG0")
    public void setArg0(String arg0) {
        this.arg0 = arg0;
    }

    @Column(name = "ARG1")
    public String getArg1() {
        return arg1;
    }

    @Column(name = "ARG1")
    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    @Column(name = "ARG2")
    public String getArg2() {
        return arg2;
    }

    @Column(name = "ARG2")
    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    @Column(name = "ARG3")
    public String getArg3() {
        return arg3;
    }

    @Column(name = "ARG3")
    public void setArg3(String arg3) {
        this.arg3 = arg3;
    }

    @Column(name = "CALLER_FILENAME")
    public String getCallerFilename() {
        return callerFilename;
    }

    @Column(name = "CALLER_FILENAME")
    public void setCallerFilename(String callerFilename) {
        this.callerFilename = callerFilename;
    }

    @Column(name = "CALLER_CLASS")
    public String getCallerClass() {
        return callerClass;
    }

    @Column(name = "CALLER_CLASS")
    public void setCallerClass(String callerClass) {
        this.callerClass = callerClass;
    }

    @Column(name = "CALLER_METHOD")
    public String getCallerMethod() {
        return callerMethod;
    }

    @Column(name = "CALLER_METHOD")
    public void setCallerMethod(String callerMethod) {
        this.callerMethod = callerMethod;
    }

    @OneToMany
    @NotFound(action = NotFoundAction.IGNORE)
    @Cascade(CascadeType.ALL)
    @JoinColumn(name = "`EVENT_ID`", insertable = false, updatable = false)
    public List<LoggingEventPropertyDBItem> getLoggingEventProperties() {
        return loggingEventProperties;
    }

    public void setLoggingEventProperties(List<LoggingEventPropertyDBItem> loggingEventProperties) {
        this.loggingEventProperties = loggingEventProperties;
    }

    @OneToMany
    @NotFound(action = NotFoundAction.IGNORE)
    @Cascade(CascadeType.ALL)
    @JoinColumn(name = "`EVENT_ID`", insertable = false, updatable = false)
    public List<LoggingEventExceptionDBItem> getLoggingEventExceptions() {
        return loggingEventExceptions;
    }

    public void setLoggingEventExceptions(List<LoggingEventExceptionDBItem> loggingEventExceptions) {
        this.loggingEventExceptions = loggingEventExceptions;
    }

}
