package com.sos.jobscheduler.tools.webservices.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.sos.jobscheduler.tools.webservices.globals.SOSCommandSecurityWebserviceAnswer;
import com.sos.jobscheduler.tools.webservices.globals.jaxb.SOSCommandSecurityWebserviceAnswer.ObjectFactory;
import com.sos.jobscheduler.tools.webservices.globals.jaxb.SOSCommandSecurityWebserviceAnswer.SosSecurityWebserviceAnswer;

/** @author ur */
public class SOSCommandSecurityClient {

    private static final Logger LOGGER = Logger.getLogger(SOSCommandSecurityClient.class);
    private Map<String, Object> params;
    private String contentType = "application/x-www-form-urlencoded";
    private String encoding = "UTF-8";
    private String answer;
    private SosSecurityWebserviceAnswer sosCommandSecurityWebserviceJaxbAnswer;
    private SOSCommandSecurityWebserviceAnswer sosCommandSecurityWebserviceAnswer;

    public SOSCommandSecurityClient() {
        params = new LinkedHashMap<>();
    }

    public void executeCommand(URL url) throws Exception {
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), encoding));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), encoding));
        }
        byte[] postDataBytes = postData.toString().getBytes(encoding);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
        String s = "";
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
        for (int c; (c = in.read()) >= 0; s = s + (char) c) {
            // fast and dirty
        }
        answer = s;
        ObjectFactory o = new ObjectFactory();
        sosCommandSecurityWebserviceJaxbAnswer = o.createSosCommandSecurityWebserviceJaxbAnswer();
        JAXBContext context = JAXBContext.newInstance(SosSecurityWebserviceAnswer.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = new StringReader(answer);
        sosCommandSecurityWebserviceJaxbAnswer = (SosSecurityWebserviceAnswer) unmarshaller.unmarshal(reader);
        sosCommandSecurityWebserviceAnswer = new SOSCommandSecurityWebserviceAnswer(sosCommandSecurityWebserviceJaxbAnswer);
    }

    public void executeCommand(String url) throws Exception {
        executeCommand(new URL(url));
    }

    public void uncheckedExecuteCommand(String url) {
        try {
            executeCommand(new URL(url));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void addParam(String name, String value) {
        params.put(name, value);
    }

    public String getAnswer() {
        return answer;
    }

    public SosSecurityWebserviceAnswer getSosCommandSecurityWebserviceJaxbAnswer() {
        return sosCommandSecurityWebserviceJaxbAnswer;
    }

    public SOSCommandSecurityWebserviceAnswer getSosCommandSecurityWebserviceAnswer() {
        return sosCommandSecurityWebserviceAnswer;
    }

}
