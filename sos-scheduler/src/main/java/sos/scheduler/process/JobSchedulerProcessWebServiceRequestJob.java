package sos.scheduler.process;

import sos.spooler.Job_impl;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.spooler.Web_service_operation;
import sos.spooler.Web_service_request;
import sos.spooler.Xslt_stylesheet;

import sos.xml.SOSXMLXPath;

public class JobSchedulerProcessWebServiceRequestJob extends Job_impl {

    public boolean spooler_process() {

        try {
            String xml_document = "";
            Order order = spooler_task.order();

            Web_service_operation operation = order.web_service_operation();
            if (operation == null)
                throw new Exception("no web service operation available");

            Web_service_request request = operation.request();
            if (request == null)
                throw new Exception("no web service request available");
            spooler_log.debug3("content of web service request:\n" + request.string_content());

            // should the request be previously transformed ...
            if (spooler_task.params().value("request_stylesheet") != null && spooler_task.params().value("request_stylesheet").length() > 0) {
                Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                stylesheet.load_file(spooler_task.params().value("request_stylesheet"));
                xml_document = stylesheet.apply_xml(request.string_content());
                spooler_log.debug3("content of request transformation:\n" + xml_document);
            } else {
                xml_document = request.string_content();
            }

            Variable_set params = spooler.create_variable_set();

            // add order parameters from request xml element /params
            SOSXMLXPath xpath = new sos.xml.SOSXMLXPath(new StringBuffer(xml_document));
            String xmlParams = xpath.selectDocumentText("//params");
            if (xmlParams != null && xmlParams.length() > 0) {
                params.set_xml(xmlParams);
            }

            order.set_params(params);

            // altenatively you could add any xml structure of the request to
            // the xml payload of this order
            // order.set_xml_payload(
            // xpath.selectDocumentText("//xml_payload/*") );

            spooler_log.info("web service request accepted for order \"" + order.id() + "\"");

            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred processing web service request: " + e.getMessage());
            return false;
        }
    }

}
