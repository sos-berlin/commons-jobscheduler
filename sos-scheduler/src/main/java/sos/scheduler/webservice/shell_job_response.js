function spooler_process() {

  try {
    var order = spooler_task.order;
    var operation;

    try {
      operation = order.web_service_operation;
    } catch (e) {
      spooler_log.warn( "error occurred processing web service operation: " + String(e) );
      throw String(e);
    }

    try {
      var request  = operation.request;
      var response = operation.response;

      if (spooler_task.params.value("response_stylesheet") != null && spooler_task.params.value("response_stylesheet").length > 0) {
        // .. either transform the response from order parameters and payload
        var stylesheet = spooler.create_xslt_stylesheet();
        stylesheet.load_file( spooler_task.params.value("response_stylesheet") );
        var xml_document = stylesheet.apply_xml( order.xml );
        spooler_log.debug3( "content of response transformation:\n" + xml_document );
        response.string_content = xml_document;
      } else {
        // .. or send an individual response (use payload.xml or xml_payload to access order data)
        response.string_content = "<response state=\"success\">" + spooler_task.order.payload.xml + "</response>";
      }

      response.send();
      spooler_log.info( "web service response successfully processed for order \"" + order.id() + "\"" ); 
    } catch (e) {
      spooler_log.warn( "error occurred processing web service response: " + String(e) );
      throw String(e);
    }

    return true;
  } catch (e) {
    spooler_log.warn( String(e) );
    return false;
  }
}
