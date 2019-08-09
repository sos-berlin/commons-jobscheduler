
package com.sos.auth.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.Realm;
import java.util.Collection;

public class SOSAtLeastOneSuccessfulGroupStrategy extends SOSAbstractAuthenticationStrategy {

	@Override
	public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token)
			throws AuthenticationException {
		setRealmGroups(realms);
		return null;
	}

	@Override
	public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo,
			AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {

		AuthenticationInfo aggregate = listOfInfos.get(getGroup(realm.getName()));

		if (aggregate == null) {
			aggregate = singleRealmInfo;
		} else {
			aggregate = this.merge(singleRealmInfo, aggregate);
		}

		listOfInfos.put(getGroup(realm.getName()), aggregate);
		return singleRealmInfo;
	}

	@Override
	public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregateInfo) {

		AuthenticationInfo info = null;

		for (String group : listOfGroups) {
			info = listOfInfos.get(group);
			if (info == null) {
				String msg = "Unable to acquire account data from one realm in group [" + group + "]  The ["
						+ getClass().getName()
						+ " implementation requires at least one configured realm per group to operate for a successful authentication.";
				throw new AuthenticationException(msg);
			}

		}

		return info;
	}

}
