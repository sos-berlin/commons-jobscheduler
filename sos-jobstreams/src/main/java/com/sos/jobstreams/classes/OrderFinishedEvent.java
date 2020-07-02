package com.sos.jobstreams.classes;

import javax.json.JsonObject;

public class OrderFinishedEvent {
    // { "nodeId": "success", "TYPE": "OrderFinished", "key": "/job_chain1,start", "eventId": 1563800477875000 },

    private String nodeId;
    private String type;
    private String key;
    private String jobChain;
    private String orderId;
    private Integer eventId;

    public OrderFinishedEvent(JsonObject entry) {
        super();
        this.nodeId = entry.getString("nodeId");
        this.type = entry.getString("TYPE");

        String key = entry.getString("key");
        if (key != null) {
            String[] keys = key.split(",");
            this.jobChain = keys[0];
            if (keys.length > 0) {
                this.orderId = keys[1];
            }
        }
        this.eventId = entry.getInt("eventId");
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getJobChain() {
        return jobChain;
    }

    public String getOrderId() {
        return orderId;
    }

    public Integer getEventId() {
        return eventId;
    }

}
