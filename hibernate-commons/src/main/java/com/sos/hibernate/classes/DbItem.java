package com.sos.hibernate.classes;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import javax.persistence.Transient;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;



 
/**
* \class DbItem 
* 
* \brief DbItem - 
* 
* \details
*
* \section DbItem.java_intro_sec Introduction
*
* \section DbItem.java_samples Some Samples
*
* \code
*   .... code goes here ...
* \endcode
*
* <p style="text-align:center">
* <br />---------------------------------------------------------------------------
* <br /> APL/Software GmbH - Berlin
* <br />##### generated by ClaviusXPress (http://www.sos-berlin.com) #########
* <br />---------------------------------------------------------------------------
* </p>
* \author Uwe Risse
* \version 19.01.2012
* \see reference
*
* Created on 19.01.2012 12:57:02
 */
public  class DbItem {

	@SuppressWarnings("unused")
	private final String	conClassName	= "DbItem";
 
    private DateTimeZone dateTimeZone4Getters = DateTimeZone.getDefault();

	public DbItem() {
 		//
	}

    @Transient
    public void setDateTimeZone4Getters(DateTimeZone dateTimeZone4Getters) {
        this.dateTimeZone4Getters = dateTimeZone4Getters;
    }

    @Transient
    public void setDateTimeZone4Getters(String dateTimeZone4Getters) {
        this.dateTimeZone4Getters =  DateTimeZone.forID(dateTimeZone4Getters);
    }

    @Transient 
    public DateTimeZone getDateTimeZone4Getters() {
        return this.dateTimeZone4Getters;
    }

    @Transient
	public String getLogAsString() throws IOException {
		return "";
	}

    @Transient
	public Long getLogId() {
		return Long.valueOf(-1);
	}

    @Transient
 	public String getTitle() {
 		return "";
	}

    @Transient
 	public boolean isStandalone() {
 		return false;
 	}

    @Transient
 	public String getIdentifier() {
 		return "";
 	}

 	public String getSchedulerId() {
 		return "";
 	}

    @Transient
 	public String getJob() {
 		return "";
 	}

    @Transient
 	public String getJobChain() {
 		return "";
 	}

    @Transient
 	public String getOrderId() {
 		return "";
 	}

    @Transient
 	public void setOrderId(String orderId) {
  	}
 
 	private boolean isToday(Date d) {
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 		return formatter.format(d).equals(formatter.format(new Date()));
 	}

    @Transient
 	public String getDateFormatted(Date d) {
 	   if (d == null) {
 	       return "";
 	   }
 	   String fromTimeZoneString = "UTC";
 	    
       DateTime dateTimeInUtc = new DateTime(d);
 
       String toTimeZoneString = getDateTimeZone4Getters().getID();

       if (isToday(UtcTimeHelper.convertTimeZonesToDate(fromTimeZoneString, toTimeZoneString, new DateTime(d)))) {
           return UtcTimeHelper.convertTimeZonesToString("HH:mm:ss",fromTimeZoneString, toTimeZoneString, dateTimeInUtc);
       }else {
           return UtcTimeHelper.convertTimeZonesToString("yyyy-MM-dd H:mm:ss",fromTimeZoneString, toTimeZoneString, dateTimeInUtc);
       }
       
 	}
 
 	  protected String null2Blank(String s) {
 	    	if (s==null) {
 	    		return ""; 
 	    	}else {
 	    		return s; 
 	    	}

 	    }


    @Transient
 	public String getDateDiff(Date start,Date end){
 		if (start == null || end == null){
 			return "";
 		}else{
 		 Calendar cal_1 = new GregorianCalendar( );
 		 Calendar cal_2 = new GregorianCalendar();
 		 

 		 cal_1.setTime( start );                      
 		 cal_2.setTime( end );                  
 
 		 long time = cal_2.getTime().getTime() - cal_1.getTime().getTime(); 
 		 
 		long millis = time % 1000;
 		time/=1000;
 		long seconds = time % 60;
 		time/=60;
 		long minutes = time % 60;
 		time/=60;
 		long hours = time % 24;
 		time/=24;
 		long days = time;
 		 
 		 
 		
 	 	Calendar calendar = GregorianCalendar.getInstance(); 
        calendar.set(Calendar.HOUR_OF_DAY, (int) hours);  
 		calendar.set(Calendar.MINUTE, (int) minutes);  
 		calendar.set(Calendar.SECOND, (int) seconds);  

 		
	    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
	    String d = "";
	    
	    if (days > 0) {
 		   d = String.format("%sd " + formatter.format(calendar.getTime()),days);
	    }else {
	       d =  formatter.format(calendar.getTime()) ;
	    }

 		return d;
 		}
 	}
 	
 	public boolean haveError() {
 		return false;
 	}


    @Transient
 	public String getJobName() {
 		return "";
 	}

    @Transient
 	public String getStartTimeFormated() {
 		return "";
 	} 	

    @Transient
 	public String getDurationFormated() {
 		return "";
 	}

    @Transient
 	public String getEndTimeFormated() {
 		return "";
 	}

    @Transient
 	public String getExecResult() {
 		return "";
 	}

    @Transient
 	public String getSpoolerId() {
 		return "";
 	}

    @Transient
 	public Date getEndTime() {
 		return null;
 	}

    @Transient
 	public String getState() {
 		return "";
 	}

    @Transient
 	public String getJobOrJobchain() {
 		return "";
 	}

    @Transient
    public boolean isOrderJob(){
        return false;
    }

    @Transient
    public String getValueOrBlank(String c) {
        if (c == null) {
            return "";
        }else {
        return c;
        }
    }     
 	
       
 	 	
}
