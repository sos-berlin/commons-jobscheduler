package com.sos.scheduler.cmd;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.commands.JSCmdAddOrder;
import com.sos.scheduler.model.objects.JSObjParams;
import com.sos.scheduler.model.objects.Param;
import com.sos.scheduler.model.objects.Params;
import org.apache.log4j.Logger;

public class JSAddOrder extends JSCommand {
	
	private final Logger logger = Logger.getLogger(JSAddOrder.class);

	private final JSCmdAddOrder cmdAddOrder;

	public JSAddOrder(String host, Integer port, String orderId, String jobChain) {
        super(host, port);
		this.cmdAddOrder = setCommand(orderId, jobChain);
	}

	public JSAddOrder(SchedulerObjectFactory factory, String orderId, String jobChain) {
        super(factory);
		this.cmdAddOrder = setCommand(orderId, jobChain);
	}

    private JSCmdAddOrder setCommand(String orderId, String jobChain) {
        JSCmdAddOrder cmd = getFactory().createAddOrder();
        cmd.setJobChain(jobChain);
        cmd.setId(orderId);
        setCommand(cmd);
        logger.info("Starting order for jobchain " + jobChain + " with orderID " + orderId);
        return cmd;
    }

	public void addParam(String name, String value) {
        initializeParams();
		Param p = getFactory().createParam(name, value);
        getParams().getParamOrCopyParamsOrInclude().add(p);
	}

	private void initializeParams() {
		if (cmdAddOrder.getParams() == null) {
			Params params = getFactory().createParams();
			setParams(params);
		}
	}

    public void mergeParams(Params paramsToMerge) {
        if(paramsToMerge != null) {
            initializeParams();
            JSObjParams orderParams = new JSObjParams(getFactory());
            orderParams.setParamsFromString(getParams().toXMLString());
            orderParams.merge(paramsToMerge);
            cmdAddOrder.setParams(orderParams);
        }
    }

	public void setParams(Params params) {
		if (params != null) {
            cmdAddOrder.setParams(params);
            cmdAddOrder.getParams().setParent(getFactory());
        }
	}

    public Params getParams() {
        return cmdAddOrder.getParams();
    }
	
	public void setState(String state) {
		cmdAddOrder.setState(state);
	}
	
	public void setTitle(String title) {
		cmdAddOrder.setTitle(title);
	}
	
	public void setReplace(boolean replace) {
		cmdAddOrder.setReplace(cmdAddOrder.setYesOrNo(replace));
	}
	
	public void setAt(String at) {
		cmdAddOrder.setAt(at);
	}

}
