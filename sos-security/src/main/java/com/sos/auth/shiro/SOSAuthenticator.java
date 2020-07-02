package com.sos.auth.shiro;

import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class SOSAuthenticator extends ModularRealmAuthenticator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthenticator.class);

	protected AuthenticationInfo doMultiRealmAuthentication(Collection<Realm> realms, AuthenticationToken token) {
		AuthenticationStrategy strategy = getAuthenticationStrategy();
		AuthenticationInfo aggregate = strategy.beforeAllAttempts(realms, token);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Iterating through {} realms for PAM authentication", realms.size());
		}

		for (Realm realm : realms) {

			aggregate = strategy.beforeAttempt(realm, token, aggregate);

			if (realm.supports(token)) {

				LOGGER.trace("Attempting to authenticate token [{}] using realm [{}]", token, realm);

				AuthenticationInfo info = null;
				Throwable t = null;
				try {
					info = realm.getAuthenticationInfo(token);
				} catch (Throwable throwable) {
					t = throwable;
					if (LOGGER.isDebugEnabled()) {
						String msg = "Realm [" + realm
								+ "] threw an exception during a multi-realm authentication attempt:";
						LOGGER.debug(msg, t);
					}
				}

				aggregate = strategy.afterAttempt(realm, token, info, aggregate, t);
				if (aggregate != null && isFirstSuccessfulStrategy(strategy)) {
					break;
				}

			} else {
				LOGGER.debug("Realm [{}] does not support token {}. Skipping realm.", realm, token);
			}
		}

		aggregate = strategy.afterAllAttempts(token, aggregate);

		return aggregate;

	}

	private boolean isFirstSuccessfulStrategy(AuthenticationStrategy strategy) {
		return strategy.getClass().equals(FirstSuccessfulStrategy.class);
	}

}