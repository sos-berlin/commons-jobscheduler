function spooler_process() {

  try {
    var order = spooler_task.order;
    var operation;
    var request;

    try { // to retrieve the web service operation
      operation = order.web_service_operation;
    } catch (e) {
      spooler_log.warn( "error occurred processing web service operation: " + String(e) );
      throw String(e);
    }

    try { // to retrieve the web service request
      var xml_document;
      request = operation.request;
      spooler_log.debug3( "content of web service request:\n" + request.string_content );

      // should the request be previously transformed ...
      if (spooler_task.params.value("request_stylesheet") != null && spooler_task.params.value("request_stylesheet").length > 0) {
        var stylesheet = spooler.create_xslt_stylesheet();
        stylesheet.load_file(spooler_task.params.value("request_stylesheet"));
        xml_document = String(stylesheet.apply_xml(request.string_content));
        spooler_log.debug3( "content of request transformation:\n" + xml_document );
      } else {
        xml_document = request.string_content;
      }
    
      // Javascript has no XML support, so we use a helper class for XPath expressions
      var xpath = new sos.xml.SOSXMLXPath(new java.lang.StringBuffer(xml_document));
      // add order parameters from request xml element /params
      var params = spooler.create_variable_set();
      params.xml = String(xpath.selectDocumentText("//params"));
      order.payload = params;

      // altenatively you could add any xml structure of the request to the xml payload of this order
      // order.xml_payload = String(xpath.selectDocumentText("//xml_payload/*"));
 
      spooler_log.info( "web service request accepted for order \"" + order.id() + "\"" );
    } catch (e) {
      spooler_log.warn( "error occurred processing web service request: " + String(e) );
      throw String(e);
    }

    return true;
  } catch (e) {
    spooler_log.warn( String(e) );
    return false;
  }
}
