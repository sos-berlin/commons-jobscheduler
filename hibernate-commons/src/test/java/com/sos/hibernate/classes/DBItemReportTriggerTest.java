package com.sos.hibernate.classes;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import sos.util.SOSString;

@Entity
@Table(name = "REPORTING_TRIGGERS")
public class DBItemReportTriggerTest extends DbItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Others */
    private String schedulerId;
    private Long historyId;

    private String name;
    private String title;
    private String parentFolder;
    private String parentName;
    private String parentBasename;
    private String parentTitle;
    private String state;
    private String stateText;
    private Date startTime;
    private Date endTime;
    private boolean isRuntimeDefined;
    private boolean syncCompleted;
    private boolean resultsCompleted;
    private String resultStartCase;
    private Long resultSteps;
    private boolean resultError;
    private String resultErrorCode;
    private String resultErrorText;

    private Date created;
    private Date modified;

    public DBItemReportTriggerTest() {
    }

    /** Primary key */
    @Id
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Others */
    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    @Column(name = "`HISTORY_ID`", nullable = false)
    public void setHistoryId(Long val) {
        this.historyId = val;
    }

    @Column(name = "`HISTORY_ID`", nullable = false)
    public Long getHistoryId() {
        return this.historyId;
    }

    @Column(name = "`NAME`", nullable = false)
    public void setName(String val) {
        this.name = val;
    }

    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return this.name;
    }

    @Column(name = "`TITLE`", nullable = true)
    public void setTitle(String val) {
        if (val != null && val.trim().length() == 0) {
            val = null;
        }
        this.title = val;
    }

    @Column(name = "`TITLE`", nullable = true)
    public String getTitle() {
        return this.title;
    }

    @Column(name = "`PARENT_FOLDER`", nullable = false)
    public void setParentFolder(String val) {
        if (val == null) {
            val = ".";
        }
        this.parentFolder = normalizePath(val);
    }

    @Column(name = "`PARENT_FOLDER`", nullable = false)
    public String getParentFolder() {
        return this.parentFolder;
    }

    @Column(name = "`PARENT_NAME`", nullable = false)
    public void setParentName(String val) {
        this.parentName = normalizePath(val);
    }

    @Column(name = "`PARENT_NAME`", nullable = false)
    public String getParentName() {
        return this.parentName;
    }

    @Column(name = "`PARENT_BASENAME`", nullable = true)
    public void setParentBasename(String val) {
        this.parentBasename = val;
    }

    @Column(name = "`PARENT_BASENAME`", nullable = true)
    public String getParentBasename() {
        return this.parentBasename;
    }

    @Column(name = "`PARENT_TITLE`", nullable = true)
    public void setParentTitle(String val) {
        this.parentTitle = val;
    }

    @Column(name = "`PARENT_TITLE`", nullable = true)
    public String getParentTitle() {
        return this.parentTitle;
    }

    @Column(name = "`STATE`", nullable = true)
    public void setState(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        this.state = val;
    }

    @Column(name = "`STATE`", nullable = true)
    public String getState() {
        return this.state;
    }

    @Column(name = "`STATE_TEXT`", nullable = true)
    public void setStateText(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        this.stateText = val;
    }

    @Column(name = "`STATE_TEXT`", nullable = true)
    public String getStateText() {
        return this.stateText;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public void setStartTime(Date val) {
        this.startTime = val;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public Date getStartTime() {
        return this.startTime;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public void setEndTime(Date val) {
        this.endTime = val;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public Date getEndTime() {
        return this.endTime;
    }

    @Column(name = "`IS_RUNTIME_DEFINED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setIsRuntimeDefined(boolean val) {
        this.isRuntimeDefined = val;
    }

    @Column(name = "`IS_RUNTIME_DEFINED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getIsRuntimeDefined() {
        return this.isRuntimeDefined;
    }

    @Column(name = "`SYNC_COMPLETED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setSyncCompleted(boolean val) {
        this.syncCompleted = val;
    }

    @Column(name = "`SYNC_COMPLETED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getSyncCompleted() {
        return this.syncCompleted;
    }

    @Column(name = "`RESULTS_COMPLETED`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setResultsCompleted(boolean val) {
        this.resultsCompleted = val;
    }

    @Column(name = "`RESULTS_COMPLETED`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getResultsCompleted() {
        return this.resultsCompleted;
    }

    @Column(name = "`RESULT_START_CAUSE`", nullable = false)
    public void setResultStartCause(String val) {
        this.resultStartCase = val;
    }

    @Column(name = "`RESULT_START_CAUSE`", nullable = false)
    public String getResultStartCause() {
        return this.resultStartCase;
    }

    @Column(name = "`RESULT_STEPS`", nullable = false)
    public void setResultSteps(Long val) {
        this.resultSteps = val;
    }

    @Column(name = "`RESULT_STEPS`", nullable = false)
    public Long getResultSteps() {
        return this.resultSteps;
    }

    @Column(name = "`RESULT_ERROR`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setResultError(boolean val) {
        this.resultError = val;
    }

    @Column(name = "`RESULT_ERROR`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getResultError() {
        return this.resultError;
    }

    @Column(name = "`RESULT_ERROR_CODE`", nullable = true)
    public void setResultErrorCode(String val) {
        this.resultErrorCode = val;
    }

    @Column(name = "`RESULT_ERROR_CODE`", nullable = true)
    public String getResultErrorCode() {
        return this.resultErrorCode;
    }

    @Column(name = "`RESULT_ERROR_TEXT`", nullable = true)
    public void setResultErrorText(String val) {
        this.resultErrorText = val;
    }

    @Column(name = "`RESULT_ERROR_TEXT`", nullable = true)
    public String getResultErrorText() {
        return this.resultErrorText;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return this.modified;
    }

    @Transient
    public String getFullOrderQualifier() {
        return String.format("%s,%s", this.getParentName(), this.getName());
    }

}
