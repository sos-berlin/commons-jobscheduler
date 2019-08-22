package com.sos.jobstreams.db;

import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import com.sos.jobstreams.classes.Constants;
import com.sos.jobstreams.resolver.interfaces.IJSJobConditionKey;

@Entity
@Table(name = Constants.IN_CONDITIONS_TABLE)
@SequenceGenerator(name = Constants.IN_CONDITIONS_TABLE_SEQUENCE, sequenceName = Constants.IN_CONDITIONS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemInCondition implements IJSJobConditionKey {

    private Long id;
    private String schedulerId;
    private String job;
    private String expression;
    private Boolean markExpression;
    private String jobStream;
    private Date created;

    public DBItemInCondition() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.IN_CONDITIONS_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    @Column(name = "[JOB]", nullable = false)
    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    @Column(name = "[MARK_EXPRESSION]", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getMarkExpression() {
        return this.markExpression;
    }

    public void setMarkExpression(Boolean markExpression) {
        this.markExpression = markExpression;
    }

    @Column(name = "[EXPRESSION]", nullable = false)
    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Column(name = "[JOBSTREAM]", nullable = true)
    public String getJobStream() {
        return jobStream;
    }

    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Column(name = "[CREATED]", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    @Transient
    public String getJobSchedulerId() {
        return getSchedulerId();
    }

}