package com.sos.auth.shiro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.pam.AbstractAuthenticationStrategy;
import org.apache.shiro.realm.Realm;

public class SOSAbstractAuthenticationStrategy extends AbstractAuthenticationStrategy{
	protected Map<String, AuthenticationInfo> listOfInfos;
	protected List<String> listOfGroups;
	protected List<Realm> listOfRealms;
	
	protected String getGroup(String realmName) {
		String group = "";
		String[] realmParts = realmName.split("#");
		if (realmParts.length > 1) {
			group = realmParts[0];
		}
		return group;
	}

	protected void setRealmGroups(Collection<? extends Realm> realms) {
		listOfGroups = new ArrayList<String>();
		listOfInfos = new HashMap<String, AuthenticationInfo>();
		listOfRealms = new ArrayList<Realm>();
		for (Realm realm : realms) {
			listOfRealms.add(realm);
			String group = getGroup(realm.getName());
			if (!listOfGroups.contains(group)) {
				listOfGroups.add(group);
			}
			listOfInfos.put(group, null);
		}
	}
}
