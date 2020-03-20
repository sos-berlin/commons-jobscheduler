package sos.scheduler.misc;

import sos.spooler.Job;
import sos.spooler.Order;
import sos.spooler.Task;

public class SpoolerProcess {

    private Order order = null;
    private String currentOrderState = null;
    private boolean isOrderJob = false;

    public SpoolerProcess(Task task, Job job) {
        order = task.order();
        isOrderJob = job.order_queue() == null ? false : true;
    }

    public Order getOrder() {
        return order;
    }

    public boolean isOrderJob() {
        return isOrderJob;
    }

    public void setCurrentOrderState(String val) {
        currentOrderState = val;
    }

    public String getCurrentOrderState() {
        return currentOrderState;
    }
}
