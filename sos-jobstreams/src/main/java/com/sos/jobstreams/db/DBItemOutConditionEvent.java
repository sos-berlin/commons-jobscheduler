package com.sos.jobstreams.db;

import java.util.Date;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import com.sos.jobstreams.classes.Constants;
import com.sos.jobstreams.classes.Constants.OutConditionEventCommand;

@Entity
@Table(name = Constants.OUT_CONDITION_EVENTS_TABLE)
@SequenceGenerator(name = Constants.OUT_CONDITION_EVENTS_TABLE_SEQUENCE, sequenceName = Constants.OUT_CONDITION_EVENTS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemOutConditionEvent {

    private Long id;
    private Long outConditionId;
    private String event;
    private String command;
    private Boolean globalEvent;
    private Date created;

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

    @Column(name = "[COMMAND]", nullable = false)
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Column(name = "[GLOBAL_EVENT]", nullable = false)
    @Type(type = "numeric_boolean")
    public Boolean getGlobalEvent() {
        return this.globalEvent;
    }

    public void setGlobalEvent(Boolean globalEvent) {
        this.globalEvent = globalEvent;
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
    public boolean isDelete() {
        return command != null && OutConditionEventCommand.delete.name().equalsIgnoreCase(command);
    }

    @Transient
    public boolean isCreate() {
        return command == null || OutConditionEventCommand.create.name().equalsIgnoreCase(command);
    }

}