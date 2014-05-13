package sos.scheduler.managed;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



import sos.connection.SOSMySQLConnection;
import sos.scheduler.managed.JobSchedulerManagedJob;
import sos.scheduler.managed.JobSchedulerManagedObject;
import sos.spooler.Job;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSDate;
import sos.util.SOSSchedulerLogger;


/**
 * get pending orders from user managed jobs and add them to the order queue
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2005-05-29 
 */
public class JobSchedulerManagedUserJob extends JobSchedulerManagedJob {

    /** Settings Attribut: maxOrderCount: max. Größe der Auftragswarteschlangen der Job-Kette */
    private int maxOrderCount      		= 1000;

    /** Attribut: workflowOrders: Liste der Aufträge und zugehöriger Modelle */
    private ArrayList orders      		= new ArrayList();

    /** Attribut: orderIterator: Iterator für Aufträge */
    private Iterator orderIterator 		= null;
    
    private String jobChainName			="user_database_statements";


    /**
     * Initialisierung
     */
    public boolean spooler_init() {
        
        if (!super.spooler_init()) return false;
        try{
        	if (!(getConnection() instanceof SOSMySQLConnection)){
        		getLogger().warn("This Job only works with MySQL databases.");
        		return false;
        	}
        	ArrayList hostPort = getConnection().getArray("SELECT \"NAME\", \"WERT\" FROM "+
        			JobSchedulerManagedObject.getTableManagedUserVariables() + " WHERE \"NAME\"='scheduler_managed_user_job.port' OR" +
        					" \"NAME\"='scheduler_managed_user_job.host'");
        	getConnection().commit();
        	boolean correctSettings = true;
        	if(hostPort.size()<2) correctSettings = false;
        	String schedUdp="";
        	String schedHost="";
        	Iterator it = hostPort.iterator();
        	while (it.hasNext()){
        		HashMap line = (HashMap) it.next();
        		String name = line.get("name").toString();
        		String wert = line.get("wert").toString();
        		if(name!=null && name.equals("scheduler_managed_user_job.port"))
        			schedUdp = wert;
        		if(name!=null && name.equals("scheduler_managed_user_job.host"))
        			schedHost = wert;
        	}
        	String udp = ""+spooler.udp_port();
        	if(!schedUdp.equals(udp) || !schedHost.equals(spooler.hostname())) correctSettings=false;
        	
        	if(!correctSettings){
        		getConnection().execute("DELETE FROM "+JobSchedulerManagedObject.getTableManagedUserVariables()+
        		" WHERE \"NAME\"='scheduler_managed_user_job.port'");
        		getConnection().execute("DELETE FROM "+JobSchedulerManagedObject.getTableManagedUserVariables()+
        		" WHERE \"NAME\"='scheduler_managed_user_job.host'");
        		getConnection().execute("INSERT INTO "+JobSchedulerManagedObject.getTableManagedUserVariables()+
        				" (\"NAME\", \"WERT\") VALUES ('scheduler_managed_user_job.port'," +
						"'"+udp+"')");
        		getConnection().execute("INSERT INTO "+JobSchedulerManagedObject.getTableManagedUserVariables()+
        				" (\"NAME\", \"WERT\") VALUES ('scheduler_managed_user_job.host'," +
						"'"+spooler.hostname()+"')");
        		getConnection().commit();
        	}
        	//spooler.set_var("scheduler_managed_user_job.port", udp);
        	//spooler.set_var("scheduler_managed_user_job.host", spooler.hostname());
        	//config.close();
        } catch(Exception e){
        	try{
        		spooler_log.warn("Could not register scheduler host and port in database. SCHEDULER_JOB_RUN" +
        				" will not succeed. "+e);
        	}catch (Exception f){}
        }
        try{
        	if(spooler.job_chain_exists(jobChainName)) return true;
        	boolean jobExists = false;
        	String jobName ="scheduler_managed_user_database_statement";
        	        
        	spooler_log.debug3("Creating jobchain user_database_statements.");
        	Job_chain jobChain = spooler.create_job_chain();
            jobChain.set_name(jobChainName);
            spooler_log.debug3("Adding job scheduler_managed_user_database_statement to" +
            		" job_chain.");
            jobChain.add_job("scheduler_managed_user_database_statement",
            		"0","100","1100");
            jobChain.add_end_state("100");
            jobChain.add_end_state("1100");
            spooler.add_job_chain(jobChain);
            
        } catch(Exception e){
        	try{ getLogger().error("Failed to create jobchain "+jobChainName); } 
        	  catch (Exception ex){}
        	return false;
        }

        return true;
    }

	
    /**
     * Selektion
     */
    public boolean spooler_open() {
        
       	SOSSchedulerLogger sosLogger = null;
       	
       	try {
           	// refresh logger in global objects
           	sosLogger = new SOSSchedulerLogger(this.spooler_log); 
           	
           	String query = new String("SELECT \"ID\", \"SPOOLER_ID\", \"JOB_CHAIN\", \"PRIORITY\", \"TITLE\""
                    + ", \"JOB_TYPE\", \"SCHEMA\", \"USER_NAME\", \"ACTION\", \"PARAMS\"" 
                    + ", \"RUN_TIME\", \"NEXT_START\", \"NEXT_TIME\", \"TIMEOUT\", \"DELETED\", \"SUSPENDED\""
                    + " FROM " + JobSchedulerManagedObject.getTableManagedUserJobs() 
                    + " WHERE (\"UPDATED\"=1 OR \"NEXT_TIME\"< %now )"
                    //+ "   AND \"SUSPENDED\"=0"
                    + "   AND (\"SPOOLER_ID\" IS NULL OR \"SPOOLER_ID\"='" + spooler.id() + "')"
                    + " ORDER BY \"NEXT_TIME\" ASC");

           	sosLogger.debug3(".. query: " + query.toString());
           	
       		this.setOrders(this.getConnection().getArray(query));
       		this.getConnection().rollback();
       		this.setOrderIterator(this.getOrders().iterator());
       		
       	} catch (Exception e) {
       	    spooler_log.error("spooler_open(): fatal error occurred: " + e.getMessage());
       	    return false;
       	} finally {
       	    if (this.getConnection() != null) { try { this.getConnection().rollback(); } catch (Exception ex) {} }
       	}

       	return !this.getOrders().isEmpty();
    }

	
    /**
     * Verarbeitung
     */
    public boolean spooler_process() {

        Order order = null;
        HashMap orderAttributes = new HashMap(); 
        boolean rc = false;
        
        try {
            this.setLogger(new SOSSchedulerLogger(spooler_log));
            
            if (!this.getOrderIterator().hasNext()) {
                this.getLogger().info("no more orders found in queue");
                return false;
            }
            
            orderAttributes = (HashMap) this.getOrderIterator().next();
            if (orderAttributes.isEmpty()) {
                this.getLogger().warn("no order attributes found in queue");
                return false;
            }
            if (orderAttributes.get("job_chain")==null || orderAttributes.get("job_chain").toString().length()==0)
            	orderAttributes.put("job_chain", jobChainName);
            
            
            // Job Chain ist evtl. nicht vorhanden
            if ( !this.spooler.job_chain_exists(orderAttributes.get("job_chain").toString())) {
                this.getLogger().warn("no job chain found for this order: " + orderAttributes.get("job_chain").toString());
            }

            boolean deleted=false;
            if(orderAttributes.get("deleted")!= null){
            	String sDeleted = orderAttributes.get("deleted").toString();
            	deleted = !(sDeleted.trim().equals("0"));
            }
            boolean suspended=false;
            if(orderAttributes.get("suspended")!= null){
            	String sSuspended = orderAttributes.get("suspended").toString();
            	suspended = !(sSuspended.trim().equals("0"));
            }
            if (deleted){
            	getLogger().debug6("deleted=1, deleting order...");
            	getConnection().execute("DELETE FROM "+JobSchedulerManagedObject.getTableManagedUserJobs()+
            			" WHERE \"ID\"="+orderAttributes.get("id").toString());
            	getConnection().commit();
            	
            	String answer = spooler.execute_xml("<remove_order job_chain=\""+orderAttributes.get("job_chain").toString()+
            			"\" order=\""+orderAttributes.get("id").toString()+"\" />");
            }else {
            if (suspended){
            	getLogger().debug6("suspended=1, deactivating order...");
            	String answer = spooler.execute_xml("<remove_order job_chain=\""+orderAttributes.get("job_chain").toString()+
            			"\" order=\""+orderAttributes.get("id").toString()+"\" />");
            	getConnection().executeUpdate("UPDATE " + JobSchedulerManagedObject.getTableManagedUserJobs() + 
            			" SET \"UPDATED\"=0 WHERE \"ID\"="+orderAttributes.get("id").toString());
                        getConnection().commit();
            	return orderIterator.hasNext();
            }
    		// do not add orders if job chain exceeds setting for order queue length
    		if ( this.getMaxOrderCount() > 0 ) {
    			if ( spooler.job_chain(orderAttributes.get("job_chain").toString()).order_count() >= this.getMaxOrderCount() ) {
    				this.getLogger().info(".. current order [" + orderAttributes.get("id").toString() +
    				              "] skipped: order queue length [" +
    				             spooler.job_chain(orderAttributes.get("job_chain").toString()).order_count() +
    				             "] exceeds maximum size [" + this.getMaxOrderCount() + "]");
    				return this.orderIterator.hasNext();
    			}
    		}
    		
            String command =orderAttributes.get("action").toString();
            String runTime = orderAttributes.get("run_time").toString();
            String hexCommand = JobSchedulerManagedObject.toHexString(command.getBytes("US-ASCII"));
            
            order = spooler.create_order(); 
            order.set_id(orderAttributes.get("id").toString());        		// Auftragsnummer
            
            order.set_state("0");           								// Reihenfolge in der Jobkette
            order.set_priority(Integer.parseInt(orderAttributes.get("priority").toString()));            
            
            if (orderAttributes.get("title") != null)
              order.set_title(orderAttributes.get("title").toString());   	// Auftragstitel 

            
            sos.spooler.Variable_set orderData = spooler.create_variable_set();
            orderData.set_var("command", 	hexCommand);
            orderData.set_var("scheduler_order_schema",  	  orderAttributes.get("schema").toString());
            orderData.set_var("scheduler_order_user_name",  orderAttributes.get("user_name").toString());
            orderData.set_var("scheduler_order_is_user_job","1");
            if(orderAttributes.get("params")!=null){
            	String paramsXml = orderAttributes.get("params").toString();
            	if (paramsXml.length()>0){
            		Variable_set paramsSet = spooler.create_variable_set();
            		paramsSet.set_xml(paramsXml);
            		orderData.merge(paramsSet);
            	}
            }
            
            order.set_payload(orderData);
            if (runTime!=null && runTime.length()>0){
            	if (isOver(runTime)){
            		try{
            			getLogger().debug3("Order "+order.id()+" was not executed at specified runtime. Calculating new runtime.");
            		} catch (Exception e){}
            		JobSchedulerManagedDatabaseJob.updateRunTime(order, getLogger(), getConnection());
            		runTime = getConnection().getSingleValue("SELECT \"RUN_TIME\" FROM "+
                			JobSchedulerManagedObject.getTableManagedUserJobs()+ " WHERE \"ID\"=" +
							order.id());
            		if (runTime==null || runTime.length()==0){
            			return orderIterator.hasNext();
            		}
            	}
            	getLogger().debug3("Setting order run_time:"+runTime);
            	order.run_time().set_xml(runTime);
            }
            
            rc = !(spooler_task.job().order_queue() == null);
            
            /*if (flgOperationWasSuccessful == false) {
                this.getLogger().warn("no order in queue");
                return flgOperationWasSuccessful;
            }*/
            
            
            try { // to add the order
            	//String answer = spooler.execute_xml("<remove_order job_chain=\""+orderAttributes.get("job_chain").toString()+
            		//	"\" order=\""+order.id()+"\" />");
            	//if(answer.toLowerCase().indexOf("error")>0) throw new Exception(answer);
                //spooler.job_chain(orderAttributes.get("job_chain").toString()).add_order(order);
            	spooler.job_chain(orderAttributes.get("job_chain").toString()).add_or_replace_order(order);
            }
            catch (Exception e) {
                // ignore this error, scheduler cares for repeated orders
                this.getLogger().debug6("an ignorable error occurred while removing and adding order: " +
                          e.getMessage());
                this.getLogger().debug6("will try to add order on next run.");
                return orderIterator.hasNext();
            }
            
            getConnection().executeUpdate("UPDATE " + JobSchedulerManagedObject.getTableManagedUserJobs() + 
			" SET \"UPDATED\"=0 WHERE \"ID\"="+orderAttributes.get("id").toString());
            getConnection().commit();            

            this.getLogger().info( "order [" + orderAttributes.get("id").toString() + 
                            "] added to job chain [" + orderAttributes.get("job_chain").toString() + "]: " +
                            order.title() );
            }
            
            return orderIterator.hasNext();
            
        }    
        catch (Exception e) {
    		spooler_log.warn("error occurred processing managed user job" + ((order != null) ? " [" + order.id() + "]" : "") + ": " + e.getMessage());
    		spooler_task.end();
    		return false;
        }
        finally {
            try { if (this.getConnection() !=  null) this.getConnection().rollback(); } catch (Exception ex) {} // ignore this errror
        }
	}


    /**
     * Cleanup
     */
    public void spooler_exit() {
    	/*try{
    		getConnection().execute("DELETE FROM "+spooler.db_variables_table_name()+
			" WHERE \"NAME\"='scheduler_managed_user_job.port'");
    		getConnection().execute("DELETE FROM "+spooler.db_variables_table_name()+
    		" WHERE \"NAME\"='scheduler_managed_user_job.host'");
    		getConnection().commit();
    	} catch (Exception e) {}*/
        super.spooler_exit();
    }
    
    /**
     * @return Returns the orderIterator.
     */
    public Iterator getOrderIterator() {
        return orderIterator;
    }
    
    /**
     * @param orderIterator The orderIterator to set.
     */
    public void setOrderIterator(Iterator orderIterator) {
        this.orderIterator = orderIterator;
    }
    
    /**
     * @return Returns the orders.
     */
    public ArrayList getOrders() {
        return orders;
    }
    
    /**
     * @param orders The orders to set.
     */
    public void setOrders(ArrayList orders) {
        this.orders = orders;
    }
    
    /**
     * @return Returns the maxOrderCount.
     */
    public int getMaxOrderCount() {
        return maxOrderCount;
    }
    
    /**
     * @param maxOrderCount The maxOrderCount to set.
     */
    public void setMaxOrderCount(int maxOrderCount) {
        this.maxOrderCount = maxOrderCount;
    }
    
    private boolean isOver(String runTime){
    	try{
    		DocumentBuilderFactory docFactory 	= DocumentBuilderFactory.newInstance();                
    		DocumentBuilder docBuilder 			= docFactory.newDocumentBuilder();                
    		Document payloadDocument  			= docBuilder.parse(new ByteArrayInputStream(runTime.getBytes()));
    	
    		Node node = payloadDocument.getFirstChild();
    		while( node != null  &&  node.getNodeType() != Node.ELEMENT_NODE ) node = node.getNextSibling();
    		
    		if (node==null) return false;
    		Element runtimeElement = (Element)node;
    		if (!runtimeElement.getNodeName().equalsIgnoreCase("run_time")) return false;
    		node = runtimeElement.getFirstChild();
    		while( node != null  &&  node.getNodeType() != Node.ELEMENT_NODE ) node = node.getNextSibling();
    		Element dateElement = (Element)node;
    		if (!dateElement.getNodeName().equalsIgnoreCase("date")) return false;
    	
    		String date = dateElement.getAttribute("date");
    		node = dateElement.getFirstChild();
    		while( node != null  &&  node.getNodeType() != Node.ELEMENT_NODE ) node = node.getNextSibling();
    		
    		if (node==null) return false;
    		
    		Element periodElement = (Element)node;
    		String time = periodElement.getAttribute("single_start");
    		if (date==null || time==null) return false;
    		
    		Date scheduledRuntime=SOSDate.getTime(date+" "+time);
    		Date now = SOSDate.getTime();
    		return(now.after(scheduledRuntime));
    	} catch(Exception e){}
    	return false;
    }

}
