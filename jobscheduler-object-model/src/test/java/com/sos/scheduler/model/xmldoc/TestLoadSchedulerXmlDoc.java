package com.sos.scheduler.model.xmldoc;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/** @author KB */
public class TestLoadSchedulerXmlDoc implements Runnable {

    private final String testFilename = "./JobSchedulerCanWrite.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestLoadSchedulerXmlDoc.class);

    TestLoadSchedulerXmlDoc() {
    }

    public void run() {
        File documentationFile = new File(testFilename);
        try {
            LOGGER.debug("test start");
            try {
                JAXBContext context = JAXBContext.newInstance(Description.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                Description description = (Description) unmarshaller.unmarshal(documentationFile);
                LOGGER.info(description.getJob().getScript().getLanguage());
            } catch (JAXBException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (Exception je) {
            LOGGER.error(je.getMessage(), je);
        }
    }

}
