package com.sos.jobstreams.db;

import java.util.Date;

import javax.persistence.*;
import com.sos.jobstreams.classes.Constants;

@Entity
@Table(name = Constants.IN_CONDITION_COMMANDS_TABLE)
@SequenceGenerator(name = Constants.IN_CONDITION_COMMANDS_TABLE_SEQUENCE, sequenceName = Constants.IN_CONDITION_COMMANDS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemInConditionCommand {

    private Long id;
    private Long inConditionId;
    private String command;
    private String commandParam;
    private Date created;

    public DBItemInConditionCommand() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = Constants.IN_CONDITION_COMMANDS_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[IN_CONDITION_ID]", nullable = false)
    public Long getInConditionId() {
        return inConditionId;
    }

    public void setInConditionId(Long inConditionId) {
        this.inConditionId = inConditionId;
    }

    @Column(name = "[COMMAND]", nullable = false)
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Column(name = "[COMMAND_PARAM]", nullable = false)
    public String getCommandParam() {
        return commandParam;
    }

    public void setCommandParam(String commandParam) {
        this.commandParam = commandParam;
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
}