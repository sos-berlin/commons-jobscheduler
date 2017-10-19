package com.sos.JSHelper.interfaces;

import java.util.Map;


public interface IJobSchedulerEventHandler {
    
    public void sendEvent(Map<String, Map<String, String>> eventParameters);
    
    public void updateDb(Long id, String type, Map<String, String> values);
    
}
