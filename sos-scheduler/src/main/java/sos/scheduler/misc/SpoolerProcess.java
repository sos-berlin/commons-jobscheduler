package sos.scheduler.misc;

import sos.spooler.Order;

public class SpoolerProcess {

    private Order order = null;
    private boolean success = false;

    public SpoolerProcess(Order o) {
        order = o;
        success = order == null ? false : true;
    }

    public Order getOrder() {
        return order;
    }

    public boolean getSuccess() {
        return success;
    }
}
