package com.sos.jobstreams.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.persistence.*;
import com.sos.jobstreams.classes.Constants;
import com.sos.jobstreams.resolver.interfaces.IJSJobConditionKey;

@Entity
@Table(name = Constants.OUT_CONDITIONS_TABLE)
@SequenceGenerator(name = Constants.OUT_CONDITIONS_TABLE_SEQUENCE, sequenceName = Constants.OUT_CONDITIONS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemOutCondition implements IJSJobConditionKey {

    private Long id;
    private String schedulerId;
    private String job;
    private String expression;
    private String jobStream;
    private Date created;

    public DBItemOutCondition() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.OUT_CONDITIONS_TABLE_SEQUENCE)
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

    @Transient
    public String getPath() {
        Path path = Paths.get(job);
        return path.getParent().toString().replace("\\", "/");
    }

    @Override
    @Transient
    public String getJobSchedulerId() {
        return getSchedulerId();
    }
}