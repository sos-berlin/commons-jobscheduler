package sos.net;


import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * @author ap
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SOSMailAuthenticator extends Authenticator { 

  	/** Attribut: user: Benutzername */
  	private String user 						= new String("");
  
  	/** Attribut: password: Kennwort */
  	private String password 					= new String("");
  


 	public SOSMailAuthenticator() {
 		super();
 	}


 	public SOSMailAuthenticator(final String user) {
 		super();
 		this.user = user;
 	}

 	public SOSMailAuthenticator( final String user, final String password) {
 		super();
 		this.user 		= user;
 		this.password 	= password;
 	}


	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		
		return new PasswordAuthentication(user, password);
	}


	/**
	  * setzt den Benutzernamen
	  * @param user Benutzername
	  */
	public void setUser(final String user) {
		this.user = user;
	}

	/**
	 * liefert den Benutzernamen 
	 */
	public String getUser() {
		return user;
	}


	/**
	  * setzt das Kennwort
	  * @param password Kennwort
	  */
	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * liefert das Kennwort 
	 */
	public String getPassword() {
		return password;
	}

}
