package main;

use strict;
use warnings FATAL => "all";
use vars qw($spooler $spooler_log $spooler_job $spooler_task);

use XML::XPath;


sub spooler_process() {

  my $rc = 1;

  eval {
    my $xml_document;
    my $order     = $spooler_task->order;
    my $operation = $order->web_service_operation;
    my $request   = $operation->request;

    $spooler_log->debug3( "input of web service request:\n" . $request->string_content );

    #  should the request be previously transformed ...
    if ($spooler_task->params->value("request_stylesheet") && length($spooler_task->params->value("request_stylesheet")) > 0) {
      my $stylesheet = $spooler->create_xslt_stylesheet();
      $stylesheet->load_file( $spooler_task->params->value("request_stylesheet") );
      $xml_document = "".$stylesheet->apply_xml($request->string_content);
      $spooler_log->debug3( "output of request transformation:\n" . $xml_document );
    } else {
      $xml_document = $request->string_content;
    }
    
    #  add order parameters from request parameter elements
    my $xpath = XML::XPath->new($xml_document);
    my $params = $spooler->create_variable_set();
    $params->LetProperty( 'xml', $xpath->findnodes_as_string('//params') );
    $order->LetProperty( 'payload', $params );

    #  alternatively you could add any xml structure of the request to the xml payload of this order
    #  order->LetProperty( 'xml_payload',  $xpath->findnodes_as_string('//xml_payload/*'));
 
    $spooler_log->info( "web service request accepted for order \"" . $order->id . "\"" );
  };
  if ( $@ ) {
    chomp $@; 
    $spooler_log->warn( $@ ); 
    return 0;
  }
  
  return $rc;
}
