package main;

use strict;
use warnings FATAL => "all";
use vars qw($spooler $spooler_log $spooler_job $spooler_task);


sub spooler_process() {

  my $rc = 1;

  eval {
    my $order     = $spooler_task->order;
    my $operation = $order->web_service_operation;
    my $request   = $operation->request;
    my $response  = $operation->response;

    if ($spooler_task->params->value("response_stylesheet") && length($spooler_task->params->value("response_stylesheet")) > 0) {
      # .. either transform the response from order parameters and payload
      my $stylesheet = $spooler->create_xslt_stylesheet();
      $stylesheet->load_file( $spooler_task->params->value("response_stylesheet") );
      my $xml_document = $stylesheet->apply_xml( $order->xml );
      $spooler_log->debug3( "output of response transformation:\n" . $xml_document );
      $response->LetProperty( 'string_content', $xml_document);
    } else {
      # .. or send an individual response (use $payload->xml or $xml_payload to access order data)
      $response->LetProperty( 'string_content', "<response state=\"success\">" . $spooler_task->order->payload->xml . "</response>" );
    }

    $response->send();
    $spooler_log->info( "web service response successfully processed for order \"" . $order->id . "\"" ); 
  };
  if ( $@ ) {
    chomp $@; 
    $spooler_log->warn( $@ ); 
    return 0;
  }
  
  return $rc;
}
