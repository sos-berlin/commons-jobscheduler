package com.sos.scheduler.model.objects.extendedchain;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.sos.resources.SOSProductionResource;
import com.sos.resources.SOSResourceFactory;
import com.sos.scheduler.model.objects.JobChain;
import com.sos.scheduler.model.objects.ParamsExtended;
import com.sos.scheduler.model.objects.Spooler;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** @author Stefan Schädlich */
public class JobChainExtendedTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(JobChainExtendedTest.class);
    private final static String PACKAGE_NAME = "com/sos/scheduler/model/objects/extendedchain/";
    private final static String XML_VALID = PACKAGE_NAME + "valid.job_chain.xml";
    private final static String XML_INVALID = PACKAGE_NAME + "invalid.job_chain.xml";
    private final static SimpleChainNode NODE1 = new SimpleChainNode("100", "JobChainStart", "200", null, "error");
    private final static SimpleChainNode NODE2 = new SimpleChainNode("200", "JobChainEnd", "success", "100", "error");
    private final static SimpleChainNode NODE3 = new SimpleChainNode("success");
    private final static SimpleChainNode NODE4 = new SimpleChainNode("error");
    private final static SimpleParam PARAM1 = new SimpleParam("param1", "value1");
    private final static SimpleParam PARAM2 = new SimpleParam("param2", "value2");
    private final static SimpleParam PARAM3 = new SimpleParam("param3", "value3");
    private final static ImmutableMap<String, SimpleChainNode> EXPECTED_NODES =
            new ImmutableMap.Builder<String, SimpleChainNode>().put("100", NODE1).put("200", NODE2).put("success", NODE3).put("error", NODE4).build();
    private final static ImmutableMap<String, SimpleParam> EXPECTED_PARAMS =
            new ImmutableMap.Builder<String, SimpleParam>().put("param1", PARAM1).put("param2", PARAM2).put("param3", PARAM3).build();
    private Schema schema;

    public JobChainExtendedTest() {
        schema = getSchema();
    }

    private static void validate(StreamSource xmlSource, Schema schema) throws IOException, SAXException {
        Validator validator = schema.newValidator();
        validator.validate(xmlSource);
    }

    private Schema getSchema() {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema =
                    schemaFactory.newSchema(new Source[] { SOSResourceFactory.asStreamSource(SOSProductionResource.SCHEDULER_XSD),
                            SOSResourceFactory.asStreamSource(SOSProductionResource.JOB_CHAIN_EXTENSIONS_XSD) });
        } catch (SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return schema;
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void marshallAndUnmarshallTest() {
        try {
            final URL jobChainUrl = Resources.getResource(XML_VALID);
            final String jobChainXml = Resources.toString(jobChainUrl, Charsets.UTF_8);
            JAXBContext jaxbContext = JAXBContext.newInstance(Spooler.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringReader xmlReader = new StringReader(jobChainXml);
            /** build an internal representation of the given xml */
            Object o = unmarshaller.unmarshal(xmlReader);
            JobChain jobChain = (JobChain) o;
            testAssertions(jobChain);
            /** we marshall and unmarshall the JAXB object again - the result
             * should be the same */
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            marshaller.marshal(o, os);
            StringReader xmlReader2 = new StringReader(os.toString("UTF-8"));
            Object o2 = unmarshaller.unmarshal(xmlReader2);
            JobChain jobChain2 = (JobChain) o2;
            testAssertions(jobChain2);
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    private void testAssertions(JobChain jobChain) {
        Iterator<Object> it = jobChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd().iterator();
        while (it.hasNext()) {
            JobChain.JobChainNode n = (JobChain.JobChainNode) it.next();
            SimpleChainNode expected = EXPECTED_NODES.get(n.getState());
            assertEquals(expected.state, n.getState());
            assertEquals(expected.previousState, n.getPreviousState());
            if ("200".equals(n.getState())) {
                assertEquals(EXPECTED_PARAMS.size(), n.getParams().getParam().size());
                for (ParamsExtended.Param p : n.getParams().getParam()) {
                    assertTrue(EXPECTED_PARAMS.containsKey(p.getName()));
                    SimpleParam ep = EXPECTED_PARAMS.get(p.getName());
                    assertEquals(ep.value, (p.getValue() != null) ? p.getValue() : p.getContent());
                }
            }
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void validateValid() throws IOException, SAXException {
        validate(SOSResourceFactory.asStreamSource(XML_VALID), schema);
    }

    @Test(expected = org.xml.sax.SAXParseException.class)
    @Ignore("Test set to Ignore for later examination")
    public void validateInvalid() throws IOException, SAXException {
        validate(SOSResourceFactory.asStreamSource(XML_INVALID), schema);
    }

}