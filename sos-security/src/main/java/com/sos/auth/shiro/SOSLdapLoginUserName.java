package com.sos.auth.shiro;

public class SOSLdapLoginUserName {

    private String login;
    private String alternateLogin;
    private String userName;
    private String domain;

    public SOSLdapLoginUserName(String login) {
        super();
        this.login = login;
        normalizeUser();
    }

    private void normalizeUser() {
        userName = login;
        domain = "";
        String[] s = login.split("@");
        if (s.length > 1) {
            userName = s[0];
            domain = s[1];
            alternateLogin = domain + "\\" + userName;
        } else {

            s = login.split("\\\\");
            if (s.length > 1) {
                userName = s[1];
                domain = s[0];
                alternateLogin = userName + "@" + domain;
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getDomain() {
        return domain;
    }

    
    public String getLogin() {
        return login;
    }

    
    public String getAlternateLogin() {
        return alternateLogin;
    }

    
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
