/*
 * Created on 13.10.2008 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sos.scheduler.consoleviews.events;

import java.util.Iterator;
import java.util.LinkedHashSet;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOSActions {

    protected String name = "";
    protected String logic = "";
    protected LinkedHashSet listOfEventGroups = null;
    protected LinkedHashSet listOfCommands = null;
    protected NodeList commandNodes = null;
    protected Node commands = null;

    public SOSActions(final String name) {
        super();
        this.name = name;
        listOfEventGroups = new LinkedHashSet();
        listOfCommands = new LinkedHashSet();
    }

    public boolean isActive(final LinkedHashSet listOfActiveEvents) {
        String tmp = logic;
        if (logic.isEmpty() || "or".equalsIgnoreCase(logic)) {
            logic = "";
            Iterator i = listOfEventGroups.iterator();
            while (i.hasNext()) {
                SOSEventGroups evg = (SOSEventGroups) i.next();
                logic += evg.group + " or ";
            }
            logic += " false";
        }
        if ("and".equalsIgnoreCase(logic)) {
            logic = "";
            Iterator i = listOfEventGroups.iterator();
            while (i.hasNext()) {
                SOSEventGroups evg = (SOSEventGroups) i.next();
                logic += evg.group + " and ";
            }
            logic += " true";
        }
        BooleanExp exp = new BooleanExp(logic);
        Iterator i = listOfEventGroups.iterator();
        while (i.hasNext()) {
            SOSEventGroups evg = (SOSEventGroups) i.next();
            exp.replace(evg.group, exp.trueFalse(evg.isActiv(listOfActiveEvents)));
        }
        logic = tmp;
        return exp.evaluateExpression();
    }

    public LinkedHashSet getListOfCommands() {
        return listOfCommands;
    }

    public String getName() {
        return name;
    }

    public String getLogic() {
        return logic;
    }

    public LinkedHashSet getListOfEventGroups() {
        return listOfEventGroups;
    }

    public Node getCommands() {
        return commands;
    }

    public NodeList getCommandNodes() {
        return commandNodes;
    }

}