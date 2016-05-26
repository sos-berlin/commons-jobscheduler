package sos.scheduler.job;

import sos.spooler.Order;
import sos.spooler.Variable_set;

/** @author Andreas Liebert */
public class JobSchedulerShowParams extends JobSchedulerJob {

    @Override
    public boolean spooler_process() {
        boolean rc = false;
        rc = !(spooler_task.job().order_queue() == null);
        Variable_set params = spooler_task.params();
        spooler_log.info("Params for task: " + spooler_task.id());
        if (params != null) {
            spooler_log.info("Job params: \n" + getCleanedParams(params).xml());
        }
        Order order = spooler_task.order();
        Variable_set payload = null;
        if (order != null) {
            Object oPayload = order.payload();
            if (oPayload != null) {
                payload = (Variable_set) oPayload;
            }
        }
        if (payload != null) {
            spooler_log.info("Order payload: \n" + payload.xml());
        } else {
            spooler_log.info("No order payload.");
        }
        return rc;
    }

    private Variable_set getCleanedParams(Variable_set originalParams) {
        Variable_set cleanedParams = spooler.create_variable_set();
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(originalParams.names(), ";");
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            if (name.contains("password")) {
                cleanedParams.set_value(name, "*****");
            } else {
                cleanedParams.set_value(name, originalParams.value(name));
            }
        }
        return cleanedParams;
    }

}