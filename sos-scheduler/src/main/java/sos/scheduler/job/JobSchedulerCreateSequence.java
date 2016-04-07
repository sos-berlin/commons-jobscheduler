package sos.scheduler.job;

import sos.spooler.Order;
import sos.spooler.Variable_set;

public class JobSchedulerCreateSequence extends JobSchedulerJob {

    private String paramName;
    private String application = "scheduler";
    private String section = "counter";
    private String counter;

    public boolean spooler_process() throws Exception {
        if (getConnection() == null) {
            throw new Exception("Job needs database connection");
        }
        try {
            Variable_set params = spooler.create_variable_set();
            Order order = spooler_task.order();
            Variable_set orderParams = order.params();
            if (spooler_task.params() != null) {
                params.merge(spooler_task.params());
            }
            if (spooler_job.order_queue() != null) {
                params.merge(orderParams);
            }
            if (params.value("sequence_param_name") != null && !params.value("sequence_param_name").isEmpty()) {
                paramName = params.value("sequence_param_name");
                counter = paramName;
                spooler_log.info(".. parameter [sequence_param_name]: " + paramName);
            } else {
                throw new Exception("no parameter [sequence_param_name] was specified");
            }
            if (params.value("sequence_application") != null && !params.value("sequence_application").isEmpty()) {
                application = params.value("sequence_application");
                spooler_log.info(".. parameter [sequence_application]: " + application);
            } else {
                application = "scheduler";
            }
            if (params.value("sequence_section") != null && !params.value("sequence_section").isEmpty()) {
                section = params.value("sequence_section");
                spooler_log.info(".. parameter [sequence_section]: " + section);
            } else {
                section = "counter";
            }
            if (params.value("sequence_counter") != null && !params.value("sequence_counter").isEmpty()) {
                counter = params.value("sequence_counter");
                spooler_log.info(".. parameter [sequence_counter]: " + counter);
            }
            String seqNr = getConnectionSettings().getSequenceAsString(application, section, counter);
            getConnection().commit();
            spooler_log.info("Setting order paramter [" + paramName + "] to " + seqNr);
            orderParams.set_var(paramName, seqNr);
        } catch (Exception e) {
            throw new Exception("Error generating sequence number: " + e);
        }
        return true;
    }

}