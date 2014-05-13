package sos.scheduler.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sos.scheduler.job.JobSchedulerConstants;
import sos.xml.SOSXMLXPath;


public class JobSchedulerEventXPath {

    public JobSchedulerEventXPath() throws Exception {
    }
    
    
      
    public static String getEventXMLAsString(final String eventXml) throws DOMException, Exception {
       
       
    	
       try {
    	  SOSXMLXPath sosxml = new SOSXMLXPath(new StringBuffer(eventXml));
     	  //NodeList params = sosxml.selectNodeList("/spooler/answer/params/param[@name='scheduler_event_service.events']");
     	  //JobScheduler 1.3.3038 has changed param name to JobSchedulerEventJob.events
     	  NodeList params = sosxml.selectNodeList("/spooler/answer/params/param[@name='"+JobSchedulerConstants.eventVariableName+"']");
          if (params.item(0) == null) {
               throw new Exception("no event parameters found in Job Scheduler answer");
           } 
           
           NamedNodeMap attrParam = params.item(0).getAttributes();
           String eventString = getText(attrParam.getNamedItem("value"));
           eventString = eventString.replaceAll(String.valueOf((char) 254), "<").replaceAll(
           String.valueOf((char) 255), ">");

           return eventString;
       } catch (Exception e) {
           throw new Exception("error occurred reading Job Scheduler answer: " + e.getMessage());
       }
       
   }
    
    
    
    public static String getText(final Node node) {
    
        if (node != null) {
            return node.getNodeValue();
        } else {
            return "";
        }
    }
    
    
    public static void main(final String[] args) {
        
        try { 

           if (args.length < 2) {
                throw new Exception("Usage: JobSchedulerEventXPath xmlString  xPathString");
            }

            String eventXml = args[0];
            String eventXPath = args[1];

         //   String eventXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><spooler><answer time=\"2009-09-04 10:07:53.041\"><params><param name=\"test\" value=\"rest\"></param><param name=\""+JobSchedulerConstants.eventVariableName+"\" value=\"&#254;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&#255;&#10;&#254;events current_date=&quot;2009-09-04 10:02:54&quot; expiration_date=&quot;2009-09-04 22:02:54&quot;&#255;&#254;event created=&quot;2009-09-04 09:29:15&quot; event_class=&quot;sperrliste&quot; event_id=&quot;bpc&quot; exit_code=&quot;0&quot; expires=&quot;2009-09-04 10:20:00&quot; job_chain=&quot;spr-bjob200&quot; job_name=&quot;spr-bjob200&quot; order_id=&quot;&quot; remote_scheduler_host=&quot;hp06b&quot; remote_scheduler_port=&quot;4441&quot; scheduler_id=&quot;scheduler@hp06b&quot;/&#255;&#254;event created=&quot;2009-09-04 09:29:16&quot; event_class=&quot;sperrliste2&quot; event_id=&quot;bpc&quot; exit_code=&quot;0&quot; expires=&quot;2009-09-04 21:29:19&quot; job_chain=&quot;spr-bjob200&quot; job_name=&quot;spr-bjob200&quot; order_id=&quot;&quot; remote_scheduler_host=&quot;hp06b&quot; remote_scheduler_port=&quot;4441&quot; scheduler_id=&quot;scheduler@hp06b&quot;/&#255;&#254;event created=&quot;2009-09-04 09:29:16&quot; event_class=&quot;example&quot; event_id=&quot;event3&quot; exit_code=&quot;0&quot; expires=&quot;2009-09-04 21:29:19&quot; job_chain=&quot;spr-bjob200&quot; job_name=&quot;spr-bjob200&quot; order_id=&quot;&quot; remote_scheduler_host=&quot;hp06b&quot; remote_scheduler_port=&quot;4441&quot; scheduler_id=&quot;scheduler@hp06b&quot;/&#255;&#254;event created=&quot;2009-09-04 09:29:16&quot; event_class=&quot;example&quot; event_id=&quot;event4&quot; exit_code=&quot;0&quot; expires=&quot;2009-09-04 21:29:19&quot; job_chain=&quot;spr-bjob200&quot; job_name=&quot;spr-bjob200&quot; order_id=&quot;&quot; remote_scheduler_host=&quot;hp06b&quot; remote_scheduler_port=&quot;4441&quot; scheduler_id=&quot;scheduler@hp06b&quot;/&#255;&#254;/events&#255;\"/></params></answer></spooler>";
         //   String eventXPath = "//events/event[@event_class='example']";

            if (!eventXml.startsWith("<?xml ")) {
                File xmlFile = new File(eventXml);
                if (!xmlFile.canRead()) {
                    throw new Exception("input file not found: " + xmlFile.getAbsolutePath());
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFile)));
                String eventContent = ""; 
                String line = "";
                while (line != null) {
                    eventContent += line;
                    line = in.readLine();
                }
                eventXml = eventContent;
            }
            
            eventXml = JobSchedulerEventXPath.getEventXMLAsString(eventXml);
            
            SOSXMLXPath xPath = new SOSXMLXPath(new StringBuffer(eventXml));
            NodeList nl = xPath.selectNodeList(eventXPath);
            if (nl != null) {
                System.out.println(nl.getLength());
            } else {
                System.out.println(0);
            }
            
        } catch (Exception e) {
            System.out.println(0);
            System.err.println("JobSchedulerEventXPath: " + e.getMessage());
            System.exit(1);
        }
    }
    
}
