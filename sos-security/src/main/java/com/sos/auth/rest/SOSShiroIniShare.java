package com.sos.auth.rest;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.joc.db.JocConfigurationDbItem;
import com.sos.joc.Globals;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.exceptions.JocException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSShiroIniShare {
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSShiroIniShare.class);
	private String iniFileName;
	SOSHibernateSession sosHibernateSession;

	public SOSShiroIniShare(SOSHibernateSession sosHibernateSession) throws JocException {
		super();
		this.sosHibernateSession = sosHibernateSession;
	}

	public void provideIniFile() throws SOSHibernateException, JocException, UnsupportedEncodingException, IOException {
		iniFileName = Globals.getShiroIniInClassPath();
		if (!iniFileName.startsWith("file:")) {
			LOGGER.warn("can not provide shiro.ini file from filesystem");
		} else {
			iniFileName = iniFileName.replaceFirst("^file:", "");
		}

		checkForceFile();
		String inifileContent = getContentFromDatabase();
		if (inifileContent.isEmpty()) {
			copyFileToDb(new File(iniFileName));
		} else {
			createShiroIniFileFromDb(inifileContent);
		}
	}

	private void checkForceFile()
			throws SOSHibernateException, JocException, UnsupportedEncodingException, IOException {
		File forceFile = new File(iniFileName + ".import");

		if (forceFile.exists()) {
			copyFileToDb(forceFile);
			forceFile.delete();
			File iniFile = new File(iniFileName);
			File destinationFile = new File(iniFileName + ".backup");
			destinationFile.delete();
			iniFile.renameTo(destinationFile);
		}

	}

	public void copyFileToDb(File iniFile)
			throws SOSHibernateException, JocException, UnsupportedEncodingException, IOException {
		Globals.beginTransaction(sosHibernateSession);

		JocConfigurationDbItem jocConfigurationDbItem;
		JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
		jocConfigurationDBLayer.getFilter().setAccount(".");
		jocConfigurationDBLayer.getFilter().setConfigurationType("SHIRO");
		List<JocConfigurationDbItem> listOfConfigurtions = jocConfigurationDBLayer.getJocConfigurations(0);
		if (listOfConfigurtions.size() > 0) {
			jocConfigurationDbItem = listOfConfigurtions.get(0);
		} else {
			jocConfigurationDbItem = new JocConfigurationDbItem();
			jocConfigurationDbItem.setId(null);
			jocConfigurationDbItem = new JocConfigurationDbItem();
			jocConfigurationDbItem.setAccount(".");
			jocConfigurationDbItem.setConfigurationType("SHIRO");
			jocConfigurationDbItem.setName("shiro.ini");
			jocConfigurationDbItem.setShared(true);
			jocConfigurationDbItem.setInstanceId(0L);
			jocConfigurationDbItem.setSchedulerId("");
		}

		String content = new String(Files.readAllBytes(Paths.get(iniFile.getAbsolutePath())), "UTF-8");

		jocConfigurationDbItem.setConfigurationItem(content);
		Long id = jocConfigurationDBLayer.saveOrUpdateConfiguration(jocConfigurationDbItem);
		if (jocConfigurationDbItem.getId() == null) {
			jocConfigurationDbItem.setId(id);
		}
		Globals.commit(sosHibernateSession);;
	}

	private void createShiroIniFileFromDb(String inifileContent) throws IOException {
	    byte[] bytes = inifileContent.getBytes(StandardCharsets.UTF_8);
	    Files.write(Paths.get(iniFileName), bytes, StandardOpenOption.CREATE);
	}

 

	private String getContentFromDatabase() throws SOSHibernateException {
		Globals.beginTransaction(sosHibernateSession);

		JocConfigurationDbItem jocConfigurationDbItem;
		JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
		jocConfigurationDBLayer.getFilter().setAccount(".");
		jocConfigurationDBLayer.getFilter().setConfigurationType("SHIRO");
		List<JocConfigurationDbItem> listOfConfigurtions = jocConfigurationDBLayer.getJocConfigurations(0);
		Globals.commit(sosHibernateSession);

		if (listOfConfigurtions.size() > 0) {
			jocConfigurationDbItem = listOfConfigurtions.get(0);
			return jocConfigurationDbItem.getConfigurationItem();
		} else {
			return "";
		}

	}
}
