package com.sos.eventhandlerservice.db;

import javax.persistence.*;
import com.sos.eventhandlerservice.classes.Constants;

@Entity
@Table(name = Constants.OUT_CONDITION_EVENTS_TABLE)
@SequenceGenerator(name = Constants.OUT_CONDITION_EVENTS_TABLE_SEQUENCE, sequenceName = Constants.OUT_CONDITION_EVENTS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemOutConditionEvent {

    private Long id;
    private Long outConditionId;
    private String event;

    public DBItemOutConditionEvent() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.OUT_CONDITION_EVENTS_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[OUT_CONDITION_ID]", nullable = false)
    public Long getOutConditionId() {
        return outConditionId;
    }

    public void setOutConditionId(Long outConditionId) {
        this.outConditionId = outConditionId;
    }

    @Column(name = "[EVENT]", nullable = false)
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

}