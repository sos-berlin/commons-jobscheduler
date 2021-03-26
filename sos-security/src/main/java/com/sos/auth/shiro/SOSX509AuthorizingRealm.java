package com.sos.auth.shiro;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.client.ClientCertificateHandler;

public class SOSX509AuthorizingRealm extends AuthorizingRealm {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSX509AuthorizingRealm.class);
    private ISOSAuthorizing authorizing;
    
    public boolean supports(AuthenticationToken token) {
        SOSSimpleAuthorizing authorizing = new SOSSimpleAuthorizing();
        setAuthorizing(authorizing);
        return true;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authzInfo = null;
        if (authorizing != null) {
            authzInfo = authorizing.setRoles(authzInfo, principalCollection);
            authzInfo = authorizing.setPermissions(authzInfo, principalCollection);
        }
        return authzInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        SOSUsernameRequestToken myAuthToken = (SOSUsernameRequestToken) authcToken;
        HttpServletRequest request = myAuthToken.getRequest();
        boolean success = false;

        if (request != null) {
            String clientCertCN = null;
            try {
                ClientCertificateHandler clientCertHandler = new ClientCertificateHandler(request);
                clientCertCN = clientCertHandler.getClientCN();
                if (clientCertCN != null) {
                    LOGGER.info("Login with certificate");
                    success = (myAuthToken.getUsername()==null) || myAuthToken.getUsername().isEmpty() || clientCertCN.equals(myAuthToken.getUsername());
                }else {
                    LOGGER.debug("clientCertCN could not read");
                }
                if (success && ((myAuthToken.getUsername()==null) || myAuthToken.getUsername().isEmpty())){
                    myAuthToken.setUsername(clientCertCN);
                }
                
            } catch (IOException e) {
                LOGGER.debug("AuthenticationToken does not have a client certificate.");
            }
        }

        if (success) {
            return new SimpleAuthenticationInfo(myAuthToken.getUsername(), myAuthToken.getPassword(), getName());
        } else {
            return null;
        }
    }

    public void setAuthorizing(ISOSAuthorizing authorizing) {
        this.authorizing = authorizing;
    }

}