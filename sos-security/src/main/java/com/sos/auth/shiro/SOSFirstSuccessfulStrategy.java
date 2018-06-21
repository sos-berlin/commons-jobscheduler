
package com.sos.auth.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.AbstractAuthenticationStrategy;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.util.CollectionUtils;

import java.util.Collection;


public class SOSFirstSuccessfulStrategy extends AbstractAuthenticationStrategy {

 
    public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token) throws AuthenticationException {
        return null;
    }

    public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
    	  if (aggregateInfo != null && !CollectionUtils.isEmpty(aggregateInfo.getPrincipals())) {
    		  realm.supports(null);
              return aggregateInfo;
          }
          return singleRealmInfo != null ? singleRealmInfo : aggregateInfo;
    }
  
}
