/*
 * Created on 13.10.2008
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sos.scheduler.consoleviews.events;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class SOSEventGroups {

	protected String		logic			= "";
	protected String		group			= "";
	protected String		event_class		= "";
	protected LinkedHashSet	listOfEvents	= null;

	public SOSEventGroups(final String group) {
		super();
		this.group = group;
		listOfEvents = new LinkedHashSet();
	}

	public boolean isActiv(final LinkedHashSet listOfActiveEvents) {
		boolean erg = false;
		if (logic.length() == 0) {
			logic = "or";
		}
		Iterator i = listOfEvents.iterator();
		if (logic.equalsIgnoreCase("or")) {
			while (i.hasNext() && !erg) {
				SchedulerEvent e = (SchedulerEvent) i.next();
				erg = e.isIn(listOfActiveEvents);
			}
		}
		else {
			if (logic.equalsIgnoreCase("and")) {
				erg = true;
				while (i.hasNext() && erg) {
					SchedulerEvent e = (SchedulerEvent) i.next();
					erg = erg && e.isIn(listOfActiveEvents);
				}
			}
			else {
				BooleanExp exp = new BooleanExp(logic);
				while (i.hasNext()) {
					SchedulerEvent e = (SchedulerEvent) i.next();

					exp.replace(e.getEvent_name(), exp.trueFalse(e.isIn(listOfActiveEvents)));
				}
				erg = exp.evaluateExpression();
			}
		}

		return erg;
	}

	public String getGroup() {
		return group;
	}

	public LinkedHashSet getListOfEvents() {
		return listOfEvents;
	}

	public String getLogic() {
		return logic;
	}
}
