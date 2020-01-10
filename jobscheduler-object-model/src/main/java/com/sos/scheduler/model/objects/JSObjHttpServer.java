package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjHttpServer extends HttpServer {

    public JSObjHttpServer(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
