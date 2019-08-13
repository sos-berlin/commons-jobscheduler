package com.sos.eventhandlerservice.resolver;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.classes.JobStartCommand;
import com.sos.eventhandlerservice.db.DBItemConsumedInCondition;
import com.sos.eventhandlerservice.db.DBItemInCondition;
import com.sos.eventhandlerservice.db.DBItemInConditionCommand;
import com.sos.eventhandlerservice.db.DBLayerConsumedInConditions;
import com.sos.eventhandlerservice.resolver.interfaces.IJSCondition;
import com.sos.eventhandlerservice.resolver.interfaces.IJSJobConditionKey;
import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import com.sos.joc.model.conditions.InCondition;
import com.sos.joc.model.conditions.InConditionCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSInCondition implements IJSJobConditionKey, IJSCondition {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSInCondition.class);
	private DBItemInCondition itemInCondition;
	private List<JSInConditionCommand> listOfInConditionCommands;
	private boolean consumed;

	public JSInCondition() {
		super();
		this.listOfInConditionCommands = new ArrayList<JSInConditionCommand>();
	}

	public void setItemInCondition(DBItemInCondition itemInCondition) {
		this.itemInCondition = itemInCondition;
	}

	public Long getId() {
		return itemInCondition.getId();
	}

	public String getJobSchedulerId() {
		return itemInCondition.getSchedulerId();
	}

	public String getJob() {
		return itemInCondition.getJob();
	}

	public String getExpression() {
		return itemInCondition.getExpression().replaceAll("\\s*\\[", "[") + " ";
	}

	public String getJobStream() {
		return itemInCondition.getJobStream();
	}

	public boolean isMarkExpression() {
		return itemInCondition.getMarkExpression();
	}

	public void addCommand(JSInConditionCommand inConditionCommand) {
		listOfInConditionCommands.add(inConditionCommand);
	}

	public List<JSInConditionCommand> getListOfInConditionCommand() {
		return listOfInConditionCommands;
	}

	protected void markAsConsumed(SOSHibernateSession sosHibernateSession) throws SOSHibernateException {
		this.consumed = true;
		DBItemConsumedInCondition dbItemConsumedInCondition = new DBItemConsumedInCondition();
		dbItemConsumedInCondition.setCreated(new Date());
		dbItemConsumedInCondition.setInConditionId(this.getId());
		dbItemConsumedInCondition.setSession(Constants.getSession());
		try {
			DBLayerConsumedInConditions dbLayerConsumedInConditions = new DBLayerConsumedInConditions(
					sosHibernateSession);
			sosHibernateSession.beginTransaction();
			dbLayerConsumedInConditions.deleteInsert(dbItemConsumedInCondition);
			sosHibernateSession.commit();
		} catch (Exception e) {
			sosHibernateSession.rollback();
		}
	}

	public boolean isConsumed() {
		return consumed;
	}

	public void setConsumed(boolean consumed) {
		this.consumed = consumed;
	}

}
