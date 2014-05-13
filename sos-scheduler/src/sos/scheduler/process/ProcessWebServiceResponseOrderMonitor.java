package sos.scheduler.process;


import sos.spooler.Order;
import sos.spooler.Web_service_operation;
import sos.spooler.Web_service_request;
import sos.spooler.Web_service_response;
import sos.spooler.Xslt_stylesheet;
import sos.util.SOSSchedulerLogger;

/**
 * <p>ProcessWebServiceResponseOrderMonitor implementiert ein Monitor-Script, das pro Auftrag vor bzw. nach dessen Verarbeitung gestartet wird.
 * Das Script wird für Standard Job-Klassen verwendet, an die Auftragsparameter per Web Service übergeben werden.</p>
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-10-05 
 */

public class ProcessWebServiceResponseOrderMonitor extends ProcessOrderMonitor {


    /**
     * Cleanup nach Verarbeitung eines Auftrags
     * @see sos.spooler.Monitor_impl#spooler_process_after()
     */

    public boolean spooler_process_after(boolean rc) {
        
        try { // to map order configuration to this job
            this.setLogger(new SOSSchedulerLogger(spooler_log));            

            if (rc == false) spooler_task.order().setback();
            
            String xml_document = "";
            Order order = spooler_task.order();
            Web_service_operation operation = order.web_service_operation();
            
            if (operation == null) throw  new Exception( "no web service operation available" );
            
            Web_service_request request = operation.request();
            if (request == null) throw new Exception( "no web service request available" );
            
            Web_service_response response = operation.response();
            if (response == null) throw new Exception( "no web service response available" );

            if (spooler_task.params().value("response_stylesheet") != null && spooler_task.params().value("response_stylesheet").length() > 0) {
                // .. either transform the response from order parameters and payload
                Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                stylesheet.load_file( spooler_task.params().value("response_stylesheet") );
                xml_document = stylesheet.apply_xml( order.xml() );
                spooler_log.debug3( "content of response transformation:\n" + xml_document );
                response.set_string_content( xml_document );
            } else {
                // .. or send an individual response (use order.params().xml() or order.xml_payload() to access order data)
                response.set_string_content( "<response state=\"success\">" + order.params().xml() + "</response>" );
            }

            response.send();
            spooler_log.info( "web service response successfully processed for order \"" + order.id() + "\"" ); 

            if (!super.spooler_process_after(rc)) return false;
            
            return rc;

        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_after(): " + e.getMessage());
            return false;
        }
    }

}
