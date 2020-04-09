package sos.scheduler.process;

import sos.spooler.Order;
import sos.spooler.Web_service_operation;
import sos.spooler.Web_service_request;
import sos.spooler.Web_service_response;
import sos.spooler.Xslt_stylesheet;

/** @author andreas pueschel */
public class ProcessWebServiceResponseOrderMonitor extends ProcessOrderMonitor {

    public boolean spooler_process_after(boolean rc) {
        try {
            if (!rc) {
                spooler_task.order().setback();
            }
            String xml_document = "";
            Order order = spooler_task.order();
            Web_service_operation operation = order.web_service_operation();
            if (operation == null) {
                throw new Exception("no web service operation available");
            }
            Web_service_request request = operation.request();
            if (request == null) {
                throw new Exception("no web service request available");
            }
            Web_service_response response = operation.response();
            if (response == null) {
                throw new Exception("no web service response available");
            }
            if (spooler_task.params().value("response_stylesheet") != null && !spooler_task.params().value("response_stylesheet").isEmpty()) {
                Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                stylesheet.load_file(spooler_task.params().value("response_stylesheet"));
                xml_document = stylesheet.apply_xml(order.xml());
                spooler_log.debug3("content of response transformation:\n" + xml_document);
                response.set_string_content(xml_document);
            } else {
                response.set_string_content("<response state=\"success\">" + order.params().xml() + "</response>");
            }
            response.send();
            spooler_log.info("web service response successfully processed for order \"" + order.id() + "\"");
            if (!super.spooler_process_after(rc)) {
                return false;
            }
            return rc;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_after(): " + e.getMessage());
            return false;
        }
    }

}