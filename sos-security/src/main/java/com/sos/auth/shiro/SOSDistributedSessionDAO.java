package com.sos.auth.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.joc.db.JocConfigurationDbItem;
import com.sos.joc.Globals;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.exceptions.JocException;

import sos.util.SOSSerializerUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class SOSDistributedSessionDAO extends CachingSessionDAO {
	private static final String SHIRO_SESSION = "SHIRO_SESSION";
	private HashMap<String, String> serializedSessions;

	private void copySessionToDb(Serializable sessionId, String session) {
		try {
			SOSHibernateSession sosHibernateSession = Globals
					.createSosHibernateStatelessConnection("SOSDistributedSessionDAO");
			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);

			JocConfigurationDbItem jocConfigurationDbItem;
			JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
			jocConfigurationDBLayer.getFilter().setAccount(".");
			jocConfigurationDBLayer.getFilter().setName(sessionId.toString());
			jocConfigurationDBLayer.getFilter().setConfigurationType(SHIRO_SESSION);
			List<JocConfigurationDbItem> listOfConfigurtions = jocConfigurationDBLayer.getJocConfigurations(0);
			if (listOfConfigurtions.size() > 0) {
				jocConfigurationDbItem = listOfConfigurtions.get(0);
			} else {
				jocConfigurationDbItem = new JocConfigurationDbItem();
				jocConfigurationDbItem.setId(null);
				jocConfigurationDbItem.setAccount(".");
				jocConfigurationDbItem.setConfigurationType(SHIRO_SESSION);
				jocConfigurationDbItem.setName(sessionId.toString());
				jocConfigurationDbItem.setShared(true);
				jocConfigurationDbItem.setInstanceId(0L);
				jocConfigurationDbItem.setSchedulerId("");
			}

			jocConfigurationDbItem.setConfigurationItem(session);
			Long id = jocConfigurationDBLayer.saveOrUpdateConfiguration(jocConfigurationDbItem);
			if (jocConfigurationDbItem.getId() == null) {
				jocConfigurationDbItem.setId(id);
			}
			Globals.commit(sosHibernateSession);
			sosHibernateSession.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String readSessionFromDb(Serializable sessionId) {
		try {
			SOSHibernateSession sosHibernateSession = Globals
					.createSosHibernateStatelessConnection("SOSDistributedSessionDAO");

			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);

			JocConfigurationDbItem jocConfigurationDbItem;
			JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
			jocConfigurationDBLayer.getFilter().setAccount(".");
			jocConfigurationDBLayer.getFilter().setName(sessionId.toString());
			jocConfigurationDBLayer.getFilter().setConfigurationType(SHIRO_SESSION);
			List<JocConfigurationDbItem> listOfConfigurtions = jocConfigurationDBLayer.getJocConfigurations(0);
			Globals.commit(sosHibernateSession);
			sosHibernateSession.close();

			if (listOfConfigurtions.size() > 0) {
				jocConfigurationDbItem = listOfConfigurtions.get(0);
				return jocConfigurationDbItem.getConfigurationItem();
			} else {
				return "";
			}
		} catch (SOSHibernateException e) {
			throw new RuntimeException(e);

		} catch (JocException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Serializable doCreate(Session session) {
		Serializable sessionId = generateSessionId(session);
		assignSessionId(session, sessionId);
		String sessionString = SOSSerializerUtil.object2toString(session);
		serializedSessions.put(session.getId().toString(), sessionString);
		copySessionToDb(sessionId, sessionString);
		return session.getId();
	}

	@Override
	protected void doUpdate(Session session) {
		if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
			return;
		}
		String sessionString = SOSSerializerUtil.object2toString(session);
		serializedSessions.put(session.getId().toString(), sessionString);
		copySessionToDb(session.getId(), sessionString);
	}

	@Override
	protected void doDelete(Session session) {
		try {
			SOSHibernateSession sosHibernateSession = Globals
					.createSosHibernateStatelessConnection("SOSDistributedSessionDAO");
			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);
			JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
			jocConfigurationDBLayer.getFilter().setAccount(".");
			jocConfigurationDBLayer.getFilter().setName(session.getId().toString());
			jocConfigurationDBLayer.getFilter().setConfigurationType(SHIRO_SESSION);
			jocConfigurationDBLayer.delete();
			serializedSessions.put(session.getId().toString(), null);
			Globals.commit(sosHibernateSession);
			sosHibernateSession.close();
		} catch (SOSHibernateException e) {
			throw new RuntimeException(e);
		} catch (JocException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		if (serializedSessions == null) {
			serializedSessions = new HashMap<String, String>();
		}
		String session = "";
		if (serializedSessions.get(sessionId.toString()) != null) {
			session = serializedSessions.get(sessionId.toString());
		} else {
			session = readSessionFromDb(sessionId);
			serializedSessions.put(sessionId.toString(), session);
		}

		if (session.length() == 0)
			return null;
		return (Session) SOSSerializerUtil.fromString(session);
	}
}