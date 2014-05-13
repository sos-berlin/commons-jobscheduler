

// Constructor
function schedulerMailer(){
	
	var params;
	
	var debugParameter = function(paramName) {
		try{
			spooler_log.debug1(".. mail parameter ["+paramName+"]: "+params.value(paramName));
		} catch (e){} //No error handling
		
	}
	
	try{
  	this.sosMail = new sos.net.SOSMail(spooler_log.mail.smtp);
  	this.sosMail.setQueueDir(spooler_log.mail.queue_dir);
  	this.sosMail.setFrom(spooler_log.mail.from);
  	this.sosMail.addRecipient(spooler_log.mail.to);
  	this.sosMail.addCC(spooler_log.mail.cc);
  	this.sosMail.addBCC(spooler_log.mail.bcc);
  	var smtpSettings = new sos.settings.SOSProfileSettings(spooler.ini_path);
    var smtpProperties = smtpSettings.getSection("smtp");
    if (!smtpProperties.isEmpty()) {
              if (smtpProperties.getProperty("mail.smtp.user") != null && smtpProperties.getProperty("mail.smtp.user").length() > 0) {
                  this.sosMail.setUser(smtpProperties.getProperty("mail.smtp.user"));
              }
              if (smtpProperties.getProperty("mail.smtp.password") != null && smtpProperties.getProperty("mail.smtp.password").length() > 0) { 
                  this.sosMail.setPassword(smtpProperties.getProperty("mail.smtp.password"));
              }
    }
  } catch(e){
   throw "Error initializing sosMail: "+e;
  }
  
  try{
    params = spooler.create_variable_set();
    params.merge(spooler_task.params);
    if (spooler_job.order_queue!=null){
    	 params.merge(spooler_task.order.params);
    }
    
    // read params
    spooler_log.debug1("Setting mail parameters:");
		if (params.value("to") != null && params.value("to").length > 0) {
			  this.sosMail.clearRecipients();
    		this.sosMail.addRecipient(params.value("to"));    			
    		debugParameter("to");
    	}
		
		if (params.value("from") != null && params.value("from").length > 0) {
    		this.sosMail.setFrom(params.value("from"));
    		debugParameter("from");
    	}
		
		if (params.value("from_name") != null && params.value("from_name").length > 0) {
    		this.sosMail.setFromName(params.value("from_name"));
    		debugParameter("from_name");
    	}
		
		if (params.value("reply_to") != null && params.value("reply_to").length > 0) {
    		this.sosMail.setReplyTo(params.value("reply_to"));
    		debugParameter("reply_to");
    	}
    	
    	if (params.value("cc") != null && params.value("cc").length > 0) {
    		this.sosMail.addCC( params.value("cc") );
    		debugParameter("cc");
    	} 
    	
    	if (params.value("bcc") != null && params.value("bcc").length > 0) {
    		this.sosMail.addBCC(params.value("bcc"));
    		debugParameter( "bcc");
    	}
		
    	
    	if (params.value("subject") != null && params.value("subject").length > 0) {
    		this.sosMail.setSubject(params.value("subject"));
    		debugParameter( "subject");
    	} 
    	
    	
    	if (params.value("host") != null && params.value("host").length > 0) {
    		this.sosMail.setHost(params.value("host"));
    		debugParameter( "host");
    	}
    	
    	if (params.value("port") != null && params.value("port").length > 0) {    		
    		var port = params.value("port");
    		this.sosMail.setPort(""+port);
    		debugParameter( "port");    		
    	}
    	
    	if (params.value("smtp_user") != null && params.value("smtp_user").length > 0) {
    		this.sosMail.setUser(params.value("smtp_user"));
    		debugParameter( "smtp_user");
    	}
    	
    	if (params.value("smtp_password") != null && params.value("smtp_password").length > 0) {
    		this.sosMail.setPassword(params.value("smtp_password"));
    		debugParameter( "smtp_password");
    	}
    	
    	if (params.value("queue_directory") != null && params.value("queue_directory").length > 0) {
    		this.sosMail.setQueueDir(params.value("queue_directory"));
    		debugParameter( "queue_director");
    	}
    	
    	if (params.value("body") != null && params.value("body").length > 0) {
    		this.sosMail.setBody(params.value("body"));
    		debugParameter( "body");
    	}
    	
    	
    	if (params.value("content_type") != null && params.value("content_type").length > 0) {
    		this.sosMail.setContentType( params.value("content_type"));
    		debugParameter( "content_type");
    	}
    	
    	if (params.value("encoding") != null && params.value("encoding").length > 0) {
    		this.sosMail.setEncoding(params.value("encoding"));
    		debugParameter( "encoding");
    	}
    	
    	if (params.value("charset") != null && params.value("charset").length > 0) {
    		this.sosMail.setCharset(params.value("charset"));
    		debugParameter( "charset");
    	}
    	
    	if (params.value("attachment_charset") != null && params.value("attachment_charset").length > 0) {
    		this.sosMail.setAttachmentCharset(params.value("attachment_charset"));
    		debugParameter( "attachment_charset");
    	}
    	
    	if (params.value("attachment_content_type") != null && params.value("attachment_content_type").length > 0) {
    		this.sosMail.setAttachmentContentType(params.value("attachment_content_type"));
    		debugParameter( "attachment_content_type");
    	}
    	
    	if (params.value("attachment_encoding") != null && params.value("attachment_encoding").length > 0) {
    		this.sosMail.setAttachmentEncoding(params.value("attachment_encoding"));
    		debugParameter( "attachment_encoding");
    	}
    	
    	if (params.value("attachment") != null && params.value("attachment").length > 0) {
    		var attachments = params.value("attachment").split(";");
    		for (var i in attachments) {				  
				  spooler_log.debug1(".. mail attachment ["+i+"]: "+attachments[i]);
				  this.sosMail.addAttachment(attachments[i]);
			  }
    	}
    		
  } catch(e){
  	throw "Error reading parameters: "+e;
  }
  
 
}