package com.sos.JSHelper.interfaces;

import java.util.Map;


public interface IJobSchedulerEventHandler {
    
//    public void sendEvent(String key, Map<String, String> values);
    
    public void updateDb(Long id, String type, Map<String, String> values);
    
}
