package com.sos.eventhandlerservice.db;

import javax.persistence.*;
import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.resolver.interfaces.IJSJobConditionKey;

@Entity
@Table(name = Constants.IN_CONDITIONS_TABLE)
@SequenceGenerator(name = Constants.IN_CONDITIONS_TABLE_SEQUENCE, sequenceName = Constants.IN_CONDITIONS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemInCondition implements IJSJobConditionKey{

    private Long id;
    private String masterId;
    private String job;
    private String expression;

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

    @Column(name = "[MASTER_ID]", nullable = false)
    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
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

    

}