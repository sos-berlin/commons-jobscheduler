package com.sos.scheduler.model.xmldoc;

import com.sos.JSHelper.io.Files.JSXMLFile;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

/** @author Uwe Risse */
public class DescriptionTest {

    private static final String TEST_FILENAME = "junit/com/sos/scheduler/model/xmldoc/JobSchedulerCanWrite.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestLoadSchedulerXmlDoc.class);
    private File documentationFile = null;
    static JAXBContext context = null;
    static Unmarshaller unmarshaller = null;

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        LOGGER.debug("test ended");
    }

    @Before
    public void setUp() throws Exception {
        LOGGER.debug("test start");
        documentationFile = new File(TEST_FILENAME);
        context = JAXBContext.newInstance(Description.class);
        unmarshaller = context.createUnmarshaller();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetJob() throws JAXBException {
        Description description = (Description) unmarshaller.unmarshal(documentationFile);
        String str = description.getJob().getName();
        assertEquals("JobName must be JobSchedulerCanWrite", "JobSchedulerCanWrite", str);
        str = description.getJob().getOrder();
        assertEquals("Job.Order must be both", "both", str);
        str = description.getJob().getTasks();
        assertEquals("Job.Tasks must be unbounded", "unbounded", str);
        str = description.getJob().getTitle();
        assertEquals("Job.Title must be check for file is writable", "check for file is writable", str);
        str = description.getJob().getScript().getLanguage();
        assertEquals("Job.Script.Langauge must be java", "java", str);
        str = description.getJob().getScript().getComClass();
        assertNull("Job.Script.ComClass must be NULL", str);
        str = description.getJob().getScript().getJavaClass();
        assertEquals("Job.Script.JavaClass must be sos.scheduler.file.JobSchedulerCanWrite", "sos.scheduler.file.JobSchedulerCanWrite", str);
        str = description.getJob().getScript().getLanguage();
        assertEquals("Job.Script.Language must be java", "java", str);
        str = description.getJob().getScript().getResource();
        assertEquals("Job.Script.SOSResource must be 1", "1", str);
        str = description.getJob().getMonitor().getScript().getComClass();
        assertNull("Job.Monitor.Script.ComClass must be myComClass", str);
        str = description.getJob().getMonitor().getScript().getJavaClass();
        assertEquals("Job.Monitor.Script.JavaClass must be myJavaClass", "myJavaClass", str);
        str = description.getJob().getMonitor().getScript().getLanguage();
        assertEquals("Job.Monitor.Script must be java", "java", str);
        str = description.getJob().getMonitor().getScript().getResource();
        assertEquals("Job.Monitor.Script.SOSResource must be myResource", "myResource", str);
        Description.Job.Process proc = description.getJob().getProcess();
        assertNull("Job.Process.File must be NULL", proc);
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testSetJob() throws JAXBException {
        File output = new File("JobSchedulerCanWrite_temp.xml");
        output.delete();
        Description description = (Description) unmarshaller.unmarshal(documentationFile);
        ObjectFactory objFactory = new ObjectFactory();
        Description.Job.Monitor jobMonitor = objFactory.createDescriptionJobMonitor();
        Script script = objFactory.createScript();
        script.setComClass("myComClass");
        script.setJavaClass("myJavaClass");
        script.setLanguage("myJava");
        script.setResource("myResource");
        Description.Job.Process process = objFactory.createDescriptionJobProcess();
        Description.Job.Process.Environment environment = objFactory.createDescriptionJobProcessEnvironment();
        process.setEnvironment(environment);
        process.setFile("myFile");
        process.setLog("myLog");
        process.setParam("myParam");
        jobMonitor.setScript(script);
        description.getJob().setMonitor(jobMonitor);
        description.getJob().setName("myName");
        description.getJob().setOrder("myOrder");
        description.getJob().setProcess(process);
        description.getJob().setScript(script);
        description.getJob().setTasks("myTasks");
        description.getJob().setTitle("myTitle");
        JAXBContext context = JAXBContext.newInstance(Description.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(description, output);
        description = (Description) unmarshaller.unmarshal(output);
        String str = description.getJob().getName();
        assertEquals("Job.Name must be myName", "myName", str);
        str = description.getJob().getOrder();
        assertEquals("Job.Order must be myOrder", "myOrder", str);
        str = description.getJob().getTasks();
        assertEquals("Job.Tasks must be myTasks", "myTasks", str);
        str = description.getJob().getTitle();
        assertEquals("Job.Title must be myTitle", "myTitle", str);
        str = description.getJob().getScript().getLanguage();
        assertEquals("Job.Script.Langauge must be myJava", "myJava", str);
        str = description.getJob().getScript().getComClass();
        assertEquals("Job.Script.ComClass must be myComClass", "myComClass", str);
        str = description.getJob().getScript().getJavaClass();
        assertEquals("Job.Script.JavaClass must be myJavaClass", "myJavaClass", str);
        str = description.getJob().getScript().getLanguage();
        assertEquals("Job.Script.Language must be myJava", "myJava", str);
        str = description.getJob().getScript().getResource();
        assertEquals("Job.Script.SOSResource must be myResource", "myResource", str);
        str = description.getJob().getMonitor().getScript().getComClass();
        assertEquals("Job.Script.ComClass must be myComClass", "myComClass", str);
        str = description.getJob().getMonitor().getScript().getJavaClass();
        assertEquals("Job.Script.JavaClass must be myJavaClass", "myJavaClass", str);
        str = description.getJob().getMonitor().getScript().getLanguage();
        assertEquals("Job.Script.Language must be myJava", "myJava", str);
        str = description.getJob().getMonitor().getScript().getResource();
        assertEquals("Job.Script.SOSResource must be myResource", "myResource", str);
        str = description.getJob().getProcess().getFile();
        assertEquals("Job.Process.File must be myFile", "myFile", str);
        str = description.getJob().getProcess().getLog();
        assertEquals("Job.Process.Log must be myLog", "myLog", str);
        str = description.getJob().getProcess().getParam();
        assertEquals("Job.Process.Param must be myParam", "myParam", str);
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetReleases() throws JAXBException {
        String str = "";
        Description description = (Description) unmarshaller.unmarshal(documentationFile);
        Description.Releases releases = description.getReleases();
        ArrayList<Description.Releases.Release> list = (ArrayList<Description.Releases.Release>) releases.getRelease();
        for (int i = 0; i < list.size(); i++) {
            Description.Releases.Release r = list.get(i);
            str = r.getId();
            assertEquals("Releases.Release.id must be 1.3", "1.3", str);
            XMLGregorianCalendar cal = r.getCreated();
            str = cal.toString();
            assertEquals("Releases.Release.created must be 2009-03-18", "2009-03-18", str);
            cal = r.getModified();
            str = cal.toString();
            assertEquals("Releases.Release.modified must be 2009-06-15", "2009-06-15", str);
            str = r.getTitle();
            assertEquals("Releases.Release.title must be version 1.3", "version 1.3", str);
            ArrayList<Description.Releases.Release.Author> list_author = (ArrayList<Description.Releases.Release.Author>) r.getAuthor();
            for (int ii = 0; ii < list_author.size(); ii++) {
                Description.Releases.Release.Author a = list_author.get(ii);
                str = a.getEmail();
                assertEquals("Releases.Release.Author.email must be uwe.risse@sos-berlin.com", "uwe.risse@sos-berlin.com", str);
                str = a.getName();
                assertEquals("Releases.Release.Author.name must be Uwe Risse", "Uwe Risse", str);
            }
            ArrayList<Changes> list_changes = (ArrayList<Changes>) r.getChanges();
            for (int ii = 0; ii < list_changes.size(); ii++) {
                Changes c = list_changes.get(ii);
                str = c.getId();
                assertNull("Releases.Release.Changes.id must be NULL", str);
                str = c.getLanguage();
                assertEquals("Releases.Release.Changes.language must be de", "de", str);
            }
            ArrayList<Note> list_note = (ArrayList<Note>) r.getNote();
            for (int ii = 0; ii < list_note.size(); ii++) {
                Note n = list_note.get(ii);
                str = n.getLanguage();
                assertEquals("Releases.Release.Note.language must be de", "de", str);
                ArrayList<Object> list_content = (ArrayList<Object>) n.getContent();
                for (int iii = 0; iii < list_content.size(); iii++) {
                    list_content.get(1).getClass();
                    Object o = list_content.get(1);
                    str = o.toString();
                    assertEquals("Releases.Release.Note.Content must be [div: null]", "[div: null]", str);
                }
            }
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testSetReleases() throws JAXBException, DatatypeConfigurationException {
        String str = "";
        File output = new File("JobSchedulerCanWrite_temp.xml");
        output.delete();
        Description description = (Description) unmarshaller.unmarshal(documentationFile);
        Description.Releases existingReleases = description.getReleases();
        ArrayList<Description.Releases.Release> existingList = (ArrayList<Description.Releases.Release>) existingReleases.getRelease();
        Description.Releases.Release existingRelease = existingList.get(0);
        existingRelease.setId("1.4");
        ObjectFactory objFactory = new ObjectFactory();
        Description.Releases releases = objFactory.createDescriptionReleases();
        Description.Releases.Release release = objFactory.createDescriptionReleasesRelease();
        GregorianCalendar gcal = new GregorianCalendar();
        XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        cal.setDay(1);
        cal.setMonth(1);
        cal.setYear(2011);
        cal.setHour(0);
        cal.setMinute(0);
        cal.setMillisecond(0);
        release.setCreated(cal);
        release.setId("myId");
        cal.setDay(1);
        release.setModified(cal);
        release.setTitle("myTitle");
        Description.Releases.Release.Author author = objFactory.createDescriptionReleasesReleaseAuthor();
        author.setEmail("myEmail");
        author.setName("myName");
        Changes changes = objFactory.createChanges();
        changes.setId("myCId");
        changes.setLanguage("myCLanguage");
        Note note = objFactory.createNote();
        note.setLanguage("myNLanguage");
        release.getChanges().add(changes);
        release.getAuthor().add(author);
        release.getNote().add(note);
        releases.getRelease().add(release);
        JAXBContext context = JAXBContext.newInstance(Description.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(description, output);
        description = (Description) unmarshaller.unmarshal(output);
        releases = description.getReleases();
        ArrayList<Description.Releases.Release> list = (ArrayList<Description.Releases.Release>) releases.getRelease();
        Description.Releases.Release r = list.get(0);
        str = r.getId();
        assertEquals("Releases.Release.id must be 1.4", "1.4", str);
        cal = r.getCreated();
        str = cal.toString();
        assertEquals("Releases.Release.created must be 2009-03-18", "2009-03-18", str);
        for (int i = 1; i < list.size(); i++) {
            r = list.get(i);
            str = r.getId();
            assertEquals("Releases.Release.id must be myId", "myId", str);
            cal = r.getCreated();
            str = cal.toString();
            assertEquals("Releases.Release.created must be 2011-01-01", "2011-01-01", str);
            cal = r.getModified();
            str = cal.toString();
            assertEquals("Releases.Release.modified must be 2011-01-02", "2011-01-02", str);
            str = r.getTitle();
            assertEquals("Releases.Release.title must be myTitle", "myTitle", str);
            ArrayList<Description.Releases.Release.Author> list_author = (ArrayList<Description.Releases.Release.Author>) r.getAuthor();
            for (int ii = 0; ii < list_author.size(); ii++) {
                Description.Releases.Release.Author a = list_author.get(ii);
                str = a.getEmail();
                assertEquals("Releases.Release.Author.email must be myEmail", "myEmail", str);
                str = a.getName();
                assertEquals("Releases.Release.Author.name must be myName", "myName", str);
            }
            ArrayList<Changes> list_changes = (ArrayList<Changes>) r.getChanges();
            for (int ii = 0; ii < list_changes.size(); ii++) {
                Changes c = list_changes.get(ii);
                str = c.getId();
                assertEquals("Releases.Release.Changes.id must be myCId", "myCId", str);
                str = c.getLanguage();
                assertEquals("Releases.Release.Changes.language must be myCLanguage", "myCLanguage", str);
            }
            ArrayList<Note> list_note = (ArrayList<Note>) r.getNote();
            for (int ii = 0; ii < list_note.size(); ii++) {
                Note n = list_note.get(ii);
                str = n.getLanguage();
                assertEquals("Releases.Release.Note.language must be de", "de", str);
            }
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetResources() throws JAXBException {
        String str = "";
        Description description = (Description) unmarshaller.unmarshal(documentationFile);
        ArrayList<Object> resources = (ArrayList<Object>) description.getResources().getDatabaseAndMemoryAndSpace();
        Object o = resources.get(2);
        if ("com.sos.scheduler.model.xmldoc.Description$Resources$File".equals(o.getClass().getName())) {
            Description.Resources.File f = (Description.Resources.File) o;
            str = f.getFile();
            assertEquals("SOSResource.File.file must be sos.util.jar", "sos.util.jar", str);
            str = f.getId();
            assertEquals("SOSResource.File.Id must be 3", "3", str);
            str = f.getOs();
            assertEquals("SOSResource.File.os must be all", "all", str);
            str = f.getType();
            assertEquals("SOSResource.File.type must be java", "java", str);
            ArrayList<Note> list_note = (ArrayList<Note>) f.getNote();
            for (int ii = 1; ii < 2; ii++) {
                Note n = list_note.get(ii);
                str = n.getLanguage();
                assertEquals("Releases.Release.Note.language must be en", "en", str);
                ArrayList<Object> list_content = (ArrayList<Object>) n.getContent();
                for (int iii = 0; iii < list_content.size(); iii++) {
                    list_content.get(1).getClass();
                    o = list_content.get(1);
                    str = o.toString();
                    assertEquals("Releases.Release.Note.Content must be [div: null]", "[div: null]", str);
                }
            }
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testSetResources() throws JAXBException {
        String str = "";
        File output = new File("JobSchedulerCanWrite_temp.xml");
        output.delete();
        Description description = (Description) unmarshaller.unmarshal(documentationFile);
        ArrayList<Object> resources = (ArrayList<Object>) description.getResources().getDatabaseAndMemoryAndSpace();
        Object o = resources.get(1);
        if ("com.sos.scheduler.model.xmldoc.Description$Resources$File".equals(o.getClass().getName())) {
            Description.Resources.File f = (Description.Resources.File) o;
            f.setFile("myFile");
        }
        ObjectFactory objFactory = new ObjectFactory();
        Description.Resources.File resource = objFactory.createDescriptionResourcesFile();
        resource.setFile("myNewFile");
        resource.setId("myNewId");
        resource.setOs("myNewOs");
        resource.setType("myNewType");
        resources.add(resource);
        JAXBContext context = JAXBContext.newInstance(Description.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(description, output);
        description = (Description) unmarshaller.unmarshal(output);
        ArrayList<Object> resources_reread = (ArrayList<Object>) description.getResources().getDatabaseAndMemoryAndSpace();
        o = resources_reread.get(3);
        if ("com.sos.scheduler.model.xmldoc.Description$Resources$File".equals(o.getClass().getName())) {
            Description.Resources.File f = (Description.Resources.File) o;
            str = f.getFile();
            assertEquals("SOSResource.File.file must be myNewFile", "myNewFile", str);
            str = f.getId();
            assertEquals("SOSResource.File.Id must be myNewId", "myNewId", str);
            str = f.getOs();
            assertEquals("SOSResource.File.os must be myNewOs", "myNewOs", str);
            str = f.getType();
            assertEquals("SOSResource.File.type must be myNewType", "myNewType", str);
            ArrayList<Note> list_note = (ArrayList<Note>) f.getNote();
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetConfiguration() throws Exception {
        System.setProperty("user.dir", "R:/backup/sos/java/development/com.sos.scheduler/src/sos/scheduler/jobdoc");
        JSXMLFile fleFile = new JSXMLFile("R:/backup/sos/java/development/com.sos.scheduler/src/sos/scheduler/jobdoc/SOSSSHJob2JSAdapter.xml");
        File objTempFile = File.createTempFile("sosxmldoc", ".xml");
        objTempFile.deleteOnExit();
        String strTempFileName = objTempFile.getAbsolutePath();
        Description objDescr = (Description) unmarshaller.unmarshal(objTempFile);
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        LOGGER.info(objDescr.job.title);
        Description.Configuration.Params objParams = objDescr.getConfiguration().getParams();
        for (int i = 0; i < objParams.getParam().size(); i++) {
            Param objParam = objParams.getParam().get(i);
            LOGGER.info(objParam.name);
            for (int j = 0; j < objParam.getNote().size(); j++) {
                Note objNote = objParam.getNote().get(j);
                objNote.content.size();
                for (int k = 0; k < objNote.content.size(); k++) {
                    if ("en".equalsIgnoreCase(objNote.getLanguage())) {
                        Object objO = objNote.content.get(k);
                        if (objO instanceof org.apache.xerces.dom.ElementNSImpl) {
                            org.apache.xerces.dom.ElementNSImpl objElem = (org.apache.xerces.dom.ElementNSImpl) objO;
                            DOMSource source = new DOMSource(objElem);
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            StreamResult result = new StreamResult(bout);
                            transformer.transform(source, result);
                            String meineWahl = bout.toString();
                            LOGGER.info(meineWahl);
                        }
                    }
                }
            }
        }
    }

}