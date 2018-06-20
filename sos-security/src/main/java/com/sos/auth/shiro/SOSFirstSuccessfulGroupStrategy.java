
package com.sos.auth.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.AbstractAuthenticationStrategy;
import org.apache.shiro.realm.Realm;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SOSFirstSuccessfulGroupStrategy extends AbstractAuthenticationStrategy {
	private Map<String, AuthenticationInfo> listOfInfos;
	private List<String> listOfGroups;

	private String getGroup(String realmName) {
		String group = "";
		String[] realmParts = realmName.split("#");
		if (realmParts.length > 1) {
			group = realmParts[0];
		}
		return group;
	}

	private void setRealmGroups(Collection<? extends Realm> realms) {
		listOfGroups = new ArrayList<String>();
		listOfInfos = new HashMap<String, AuthenticationInfo>();
		for (Realm realm : realms) {
			String group = getGroup(realm.getName());
			if (!listOfGroups.contains(group)) {
				listOfGroups.add(group);
			}
			listOfInfos.put(group, null);
		}
	}

	private AuthenticationInfo getFirstInfo(String group) {
		return listOfInfos.get(group);
	}

	@Override
	public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token)
			throws AuthenticationException {
		setRealmGroups(realms);
		return null;
	}

	@Override
	public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo,
			AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {

		if (listOfInfos.get(getGroup(realm.getName())) == null) {
			listOfInfos.put(getGroup(realm.getName()), singleRealmInfo);
		}
		return singleRealmInfo;
	}

	@Override
	public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregateInfo) {
		AuthenticationInfo aggregate = null;

		for (String group : listOfGroups) {
			AuthenticationInfo info = getFirstInfo(group);
			if (info == null) {
				String msg = "Unable to acquire account data from one realm in group [" + group + "]  The ["
						+ getClass().getName()
						+ " implementation requires one configured realm per group to operate for a successful authentication.";
				throw new AuthenticationException(msg);
			}
			if (aggregate == null) {
				aggregate = info;
			} else {
				aggregate = this.merge(info, aggregate);
			}

		}

		return aggregate;
	}

}
