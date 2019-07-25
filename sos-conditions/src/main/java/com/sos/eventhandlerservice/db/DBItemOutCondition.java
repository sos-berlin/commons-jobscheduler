package com.sos.eventhandlerservice.db;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.*;
import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.resolver.interfaces.IJSJobConditionKey;

@Entity
@Table(name = Constants.OUT_CONDITIONS_TABLE)
@SequenceGenerator(name = Constants.OUT_CONDITIONS_TABLE_SEQUENCE, sequenceName = Constants.OUT_CONDITIONS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemOutCondition implements IJSJobConditionKey {

    private Long id;
    private String schedulerId;
    private String job;
    private String expression;
    private String workflow;

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

    @Column(name = "[WORKFLOW]", nullable = true)
    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
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