package sos.scheduler.webservice;

import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.spooler.Web_service_operation;
import sos.spooler.Web_service_request;
import sos.spooler.Xslt_stylesheet;
import sos.xml.SOSXMLXPath;

public class JobSchedulerWebServiceShellJobRequest extends Job_impl {

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
            spooler_log.debug3("content of web service request:\n" + request.string_content());
            if (spooler_task.params().value("request_stylesheet") != null && !spooler_task.params().value("request_stylesheet").isEmpty()) {
                Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                stylesheet.load_file(spooler_task.params().value("request_stylesheet"));
                xml_document = stylesheet.apply_xml(request.string_content());
                spooler_log.debug3("content of request transformation:\n" + xml_document);
            } else {
                xml_document = request.string_content();
            }
            SOSXMLXPath xpath = new sos.xml.SOSXMLXPath(new StringBuffer(xml_document));
            Variable_set params = spooler.create_variable_set();
            params.set_xml(xpath.selectDocumentText("//params"));
            order.set_payload(params);
            spooler_log.info("web service request accepted for order \"" + order.id() + "\"");
            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred processing web service request: " + e.getMessage());
            return false;
        }
    }

}