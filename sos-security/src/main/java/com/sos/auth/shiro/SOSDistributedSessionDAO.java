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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sos.util.SOSSerializerUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class SOSDistributedSessionDAO extends CachingSessionDAO {
	private static final String SHIRO_SESSION = "SHIRO_SESSION";
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSDistributedSessionDAO.class);
	private HashMap<String, String> serializedSessions;

	private void putSerializedSession(String sessionId, String sessionString) {
		if (serializedSessions == null) {
			serializedSessions = new HashMap<String, String>();
		}
		serializedSessions.put(sessionId, sessionString);
	}

	private String getSessionId(Session session) {
		if (session == null || session.getId() == null) {
			return "";
		} else {
			return session.getId().toString();
		}

	}

	private void copySessionToDb(Serializable sessionId, String session) {
		SOSHibernateSession sosHibernateSession = null;
		try {
			LOGGER.debug("SOSDistributedSessionDAO: copySessionToDb");

			sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSDistributedSessionDAO");
			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);

			JocConfigurationDbItem jocConfigurationDbItem;
			JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
			jocConfigurationDBLayer.getFilter().setAccount(".");
			jocConfigurationDBLayer.getFilter().setName(sessionId.toString());
			jocConfigurationDBLayer.getFilter().setConfigurationType(SHIRO_SESSION);
			List<JocConfigurationDbItem> listOfConfigurtions = jocConfigurationDBLayer.getJocConfigurationList(0);
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
		} catch (Exception e) {
			throw new RuntimeException(e);

		} finally {
			Globals.disconnect(sosHibernateSession);
			sosHibernateSession = null;
		}
	}

	private String readSessionFromDb(Serializable sessionId) {
		SOSHibernateSession sosHibernateSession = null;
		try {

			String sessionIdString = "";
			if (sessionId != null) {
				sessionIdString = sessionId.toString();
			}
			LOGGER.debug("SOSDistributedSessionDAO: readSessionFromDb: " + sessionIdString);

			sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSDistributedSessionDAO");

			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);

			JocConfigurationDbItem jocConfigurationDbItem;
			JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
			jocConfigurationDBLayer.getFilter().setAccount(".");
			jocConfigurationDBLayer.getFilter().setName(sessionId.toString());
			jocConfigurationDBLayer.getFilter().setConfigurationType(SHIRO_SESSION);
			List<JocConfigurationDbItem> listOfConfigurtions = jocConfigurationDBLayer.getJocConfigurationList(0);
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
		} finally {
			Globals.disconnect(sosHibernateSession);
		}
	}

	@Override
	protected Serializable doCreate(Session session) {
		LOGGER.debug("SOSDistributedSessionDAO: doCreate Session ->" + getSessionId(session));
		Serializable sessionId = generateSessionId(session);
		assignSessionId(session, sessionId);
		String sessionString = SOSSerializerUtil.object2toString(session);
		putSerializedSession(session.getId().toString(), sessionString);
		copySessionToDb(sessionId, sessionString);
		return session.getId();
	}

	@Override
	protected void doUpdate(Session session) {
		LOGGER.debug("SOSDistributedSessionDAO: doUpdate Session ->" + getSessionId(session));
		if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
			return;
		}
		LOGGER.debug("SOSDistributedSessionDAO: session is valid");
		String sessionString = SOSSerializerUtil.object2toString(session);
		putSerializedSession(session.getId().toString(), sessionString);
		copySessionToDb(session.getId(), sessionString);
	}

	@Override
	protected void doDelete(Session session) {
		SOSHibernateSession sosHibernateSession = null;
		try {
			LOGGER.debug("SOSDistributedSessionDAO: doDelete Session ->" + getSessionId(session));

			sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSDistributedSessionDAO");
			sosHibernateSession.setAutoCommit(false);
			Globals.beginTransaction(sosHibernateSession);
			JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
			jocConfigurationDBLayer.getFilter().setAccount(".");
			jocConfigurationDBLayer.getFilter().setName(session.getId().toString());
			jocConfigurationDBLayer.getFilter().setConfigurationType(SHIRO_SESSION);
			jocConfigurationDBLayer.delete();
			putSerializedSession(session.getId().toString(), null);
			Globals.commit(sosHibernateSession);
			sosHibernateSession.close();
		} catch (SOSHibernateException e) {
			throw new RuntimeException(e);
		} catch (JocException e) {
			throw new RuntimeException(e);
		} finally {
			Globals.disconnect(sosHibernateSession);

		}
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		if (serializedSessions == null) {
			LOGGER.debug("SOSDistributedSessionDAO: doReadSession: initialize serializedSessions");
			serializedSessions = new HashMap<String, String>();
		}
		String session = "";
		session = serializedSessions.get(sessionId.toString());
		if (session == null) {
			session = readSessionFromDb(sessionId);
			serializedSessions.put(sessionId.toString(), session);
		}

		if (session.length() == 0) {
			LOGGER.debug("SOSDistributedSessionDAO: session is empty");
			return null;
		}
		return (Session) SOSSerializerUtil.fromString(session);
	}
}