package com.sos.auth.shiro;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.UsernamePasswordToken;

@SuppressWarnings("serial")
public class SOSUsernameRequestToken extends UsernamePasswordToken {

    private HttpServletRequest request;

    public SOSUsernameRequestToken(String user, String pwd, HttpServletRequest httpServletRequest) {
        super(user,pwd);
        this.request = httpServletRequest;
    }
  
    public HttpServletRequest getRequest() {
        return request;
    }

 
   

}