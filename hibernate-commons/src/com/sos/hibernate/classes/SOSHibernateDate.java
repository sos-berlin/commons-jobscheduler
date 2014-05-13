package com.sos.hibernate.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
* \class SOSHibernateDate 
* 
* \brief SOSHibernateDate - 
* 
* \details
*
* \section SOSHibernateDate.java_intro_sec Introduction
*
* \section SOSHibernateDate.java_samples Some Samples
*
* \code
*   .... code goes here ...
* \endcode
*
* <p style="text-align:center">
* <br />---------------------------------------------------------------------------
* <br /> SOS GmbH - Berlin
* <br />---------------------------------------------------------------------------
* </p>
* \author Uwe Risse
* \version 26.09.2011
* \see reference
*
* Created on 26.09.2011 11:36:37
 */

public class SOSHibernateDate {

	@SuppressWarnings("unused")
	private final String	conClassName	= "SOSHibernateDate";
	private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
	private Date   date;
	private String isoDate;
	
		

	public SOSHibernateDate(String dateFormat_) {
		this.dateFormat = dateFormat_;
		//
	}

	
	private void setIsoDate() throws ParseException {
		String isoDateFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat formatter = new SimpleDateFormat(isoDateFormat);
		this.isoDate = formatter.format(date);
	}
	
	
	public void setDate(String date) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		if (date.equals("now")){
				this.date = new Date();
		}else {
				this.date = formatter.parse(date);
		}
		this.setIsoDate();
	}

	public Date getDate() {
		return date;
	}

	public String getIsoDate() {
		return isoDate;
	}

	public void setDate(Date date) {
		this.date = date;
		try {
		   this.setIsoDate();
		}catch (ParseException e) {
			System.out.println(conClassName+".setDate: Could not set Iso-Date");
		}
	}
	
	
}
