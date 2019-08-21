package com.sos.jobstreams.db;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sos.jobstreams.classes.Constants;

@Entity
@Table(name = Constants.CONSUMED_IN_CONDITIONS_TABLE)
@SequenceGenerator(name = Constants.CONSUMED_IN_CONDITIONS_TABLE_SEQUENCE, sequenceName = Constants.CONSUMED_IN_CONDITIONS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemConsumedInCondition {

    private Long id;
    private String session;
    private Long inConditionId;
    private Date created;

    public DBItemConsumedInCondition() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.CONSUMED_IN_CONDITIONS_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[SESSION]", nullable = false)
    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    @Column(name = "[IN_CONDITION_ID]", nullable = false)
    public Long getInConditionId() {
        return inConditionId;
    }

    public void setInConditionId(Long inConditionId) {
        this.inConditionId = inConditionId;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]",  nullable = false)
    public Date getCreated() {
        return created;
    }

    @Column(name = "[CREATED]",  nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }

 

}