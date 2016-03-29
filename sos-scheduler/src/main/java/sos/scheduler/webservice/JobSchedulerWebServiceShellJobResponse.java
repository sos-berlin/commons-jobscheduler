package sos.scheduler.webservice;

import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Web_service_operation;
import sos.spooler.Web_service_request;
import sos.spooler.Web_service_response;
import sos.spooler.Xslt_stylesheet;

public class JobSchedulerWebServiceShellJobResponse extends Job_impl {

    public boolean spooler_process() {

        try {
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
                // .. either transform the response from order parameters and
                // payload
                Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                stylesheet.load_file(spooler_task.params().value("response_stylesheet"));
                xml_document = stylesheet.apply_xml(order.xml());
                spooler_log.debug3("content of response transformation:\n" + xml_document);
                response.set_string_content(xml_document);
            } else {
                // .. or send an individual response (use order.params().xml()
                // or order.xml_payload() to access order data)
                response.set_string_content("<response state=\"success\">" + order.params().xml() + "</response>");
            }
            response.send();
            spooler_log.info("web service response successfully processed for order \"" + order.id() + "\"");
            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred processing web service response: " + e.getMessage());
            return false;
        }
    }

}