package com.sos.scheduler.model.objects.extendedchain;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.sos.scheduler.model.objects.JobChain;
import com.sos.scheduler.model.objects.ParamsExtended;
import com.sos.scheduler.model.objects.Spooler;
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
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This test demonstrates the marshalling / unmarshalling of xml with different namespaces.
 * The xml object classes will be generated my the maven-jaxb2-plugin (see pom.xml).
 *
 * It generates the Classes <i>JobChain</i> and <i>JobChainExtended</i> among others.
 * <i>JobChainExtended</i> should be a superclass of <i>JobChain</i>. All other classes should be extend the superclass <i>ASuperClass</i>.
 *
 * @version 1.0
 * @author Stefan Schädlich
 */
public class JobChainExtendedTest {

    private final static Logger logger = LoggerFactory.getLogger(JobChainExtendedTest.class);


    private final static String packageName = "/com/sos/scheduler/model/objects/extendedchain/";

    private final static String xmlValid = packageName + "valid.job_chain.xml";
    private final static String xmlInvalid = packageName + "invalid.job_chain.xml";
    private final static String xsdScheduler = packageName + "scheduler.xsd";
    private final static String xsdJobChainExtensions = packageName + "job-chain-extensions-v1.0.xsd";

    private final static SimpleChainNode node1 = new SimpleChainNode("100","JobChainStart","200",null,"error");
    private final static SimpleChainNode node2 = new SimpleChainNode("200","JobChainEnd","success","100","error");
    private final static SimpleChainNode node3 = new SimpleChainNode("success");
    private final static SimpleChainNode node4 = new SimpleChainNode("error");

    private final static SimpleParam param1 = new SimpleParam("param1","value1");
    private final static SimpleParam param2 = new SimpleParam("param2","value2");
    private final static SimpleParam param3 = new SimpleParam("param3","value3");

    private final static ImmutableMap<String,SimpleChainNode> expectedNodes = new ImmutableMap.Builder<String,SimpleChainNode>()
            .put("100",node1)
            .put("200", node2)
            .put("success", node3)
            .put("error", node4)
            .build();
    private final static ImmutableMap<String,SimpleParam> expectedParams = new ImmutableMap.Builder<String,SimpleParam>()
            .put("param1",param1)
            .put("param2",param2)
            .put("param3",param3)
            .build();
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
            InputStream is = this.getClass().getResourceAsStream(xsdScheduler);
            schema = schemaFactory.newSchema(new Source[]
                    {
                            new StreamSource(this.getClass().getResourceAsStream(xsdScheduler),"scheduler.xsd"),
                            new StreamSource(this.getClass().getResourceAsStream(xsdJobChainExtensions),"job-chain-extensions.xsd")
                    });
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return schema;
    }

    @Test
    public void marshallAndUnmarshallTest() {

        try {

            final URL jobChainUrl = getClass().getResource(xmlValid);
            final String jobChainXml = Resources.toString(jobChainUrl, Charsets.UTF_8);
            JAXBContext jaxbContext = JAXBContext.newInstance(Spooler.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringReader xmlReader = new StringReader(jobChainXml);

            /** build an internal representation of the given xml */
            Object o = unmarshaller.unmarshal(xmlReader);
            {
                JobChain jobChain = (JobChain) o;
                testAssertions(jobChain);
            }

            /** we marshall and unmarshall the JAXB object again - the result should be the same */
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            marshaller.marshal(o, os);
            StringReader xmlReader2 = new StringReader(os.toString("UTF-8"));
            Object o2 = unmarshaller.unmarshal( xmlReader2 );
            JobChain jobChain2 = (JobChain)o2;
            testAssertions(jobChain2);

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void testAssertions(JobChain jobChain) {
        Iterator<Object> it = jobChain.getJobChainNodeOrFileOrderSinkOrJobChainNodeEnd().iterator();
        while(it.hasNext()) {
            JobChain.JobChainNode n = (JobChain.JobChainNode) it.next();
            SimpleChainNode expected = expectedNodes.get(n.getState());
            assertEquals(expected.state,n.getState());
            assertEquals(expected.previousState, n.getPreviousState());
            // logger.info(n.getState() + "/" + n.getPreviousState());
            if(n.getState().equals("200")) {
                assertEquals(expectedParams.size(), n.getParams().getParam().size());
                for(ParamsExtended.Param p : n.getParams().getParam()) {
                    assertTrue(expectedParams.containsKey(p.getName()));
                    SimpleParam ep = expectedParams.get(p.getName());
                    assertEquals(ep.value, (p.getValue()!=null) ? p.getValue() : p.getContent() );
                }
            }
        }
    }

    @Test
    public void validateValid() throws IOException, SAXException {
        StreamSource source = new StreamSource(this.getClass().getResourceAsStream(xmlValid));
        validate(source,schema);
    }

    @Test( expected = org.xml.sax.SAXParseException.class)
    public void validateInvalid() throws IOException, SAXException {
        StreamSource source = new StreamSource(this.getClass().getResourceAsStream(xmlInvalid));
        validate(source, schema);
    }

}
