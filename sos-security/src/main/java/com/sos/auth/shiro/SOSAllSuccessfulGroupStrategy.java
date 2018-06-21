
package com.sos.auth.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.Realm;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SOSAllSuccessfulGroupStrategy extends SOSAbstractAuthenticationStrategy {
	private Map<String, Realm> listOfUnsatisfiedGroups;

	@Override
	public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token)
			throws AuthenticationException {
		listOfUnsatisfiedGroups = new HashMap<String, Realm>();
		setRealmGroups(realms);
		return null;
	}

	@Override
	public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo,
			AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
		String group = getGroup(realm.getName());

		if (singleRealmInfo == null) {
			listOfUnsatisfiedGroups.put(group, realm);
		}

		listOfInfos.put(getGroup(realm.getName()), singleRealmInfo);
		return singleRealmInfo;
	}

	private boolean groupIsSatified(String group) {
		return listOfUnsatisfiedGroups.get(group) == null && listOfInfos.get(group) != null;
	}

	@Override
	public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregateInfo) {

		AuthenticationInfo info = null;

		for (String group : listOfGroups) {
			if (groupIsSatified(group)) {
				 info = listOfInfos.get(group);
			}
		}
		
		for (Realm realm : listOfRealms) {
			if (!groupIsSatified(getGroup(realm.getName()))) {
				 realm.supports(null);
			}
		}
		
		if (info == null) {
			String msg = "Unable to acquire account data from all realms.  The ["
					+ getClass().getName()
					+ " implementation requires all configured realm in at least one group to operate for a successful authentication.";
			throw new AuthenticationException(msg);
		}
		return info;
	}

}
