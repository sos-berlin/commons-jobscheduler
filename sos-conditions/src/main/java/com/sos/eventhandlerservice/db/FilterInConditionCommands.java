package com.sos.eventhandlerservice.db;


public class FilterInConditionCommands {
    private Long inConditionId;
    private String command;
    private String commandParam;
    
    public Long getInConditionId() {
        return inConditionId;
    }
    
    public void setInConditionId(Long inConditionId) {
        this.inConditionId = inConditionId;
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public String getCommandParam() {
        return commandParam;
    }
    
    public void setCommandParam(String commandParam) {
        this.commandParam = commandParam;
    }
    
}
