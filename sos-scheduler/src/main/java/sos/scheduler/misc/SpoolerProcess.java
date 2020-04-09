package sos.scheduler.misc;

import sos.spooler.Job;
import sos.spooler.Order;

public class SpoolerProcess {

    private Order order = null;
    private String currentOrderState = null;
    private boolean isOrderJob = false;

    public SpoolerProcess(Job job) {
        isOrderJob = job.order_queue() == null ? false : true;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order o) {
        order = o;
        currentOrderState = null;
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
