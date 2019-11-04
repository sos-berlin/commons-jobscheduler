package com.sos.jobstreams.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobchainnodeparameter.JobchainNodeConfiguration;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jobstreams.resolver.JSInCondition;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JobsVCallable;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.NameValuePair;
import com.sos.joc.model.job.JobFilter;
import com.sos.joc.model.job.JobV;
import com.sos.scheduler.messages.JSMessages;
import com.sos.xml.XMLBuilder;

public class JobStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobStarter.class);
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    public JobStarter() {
        super();

    }

    private JobV getJob(String job) throws JocException {
        JOCJsonCommand command = new JOCJsonCommand();
        command.setUrl(Constants.baseUrl);
        command.setUriBuilderForJobs();
        command.addJobCompactQuery(false);
        JobFilter jobFilter = new JobFilter();
        jobFilter.setJob(job);
        JobsVCallable j = new JobsVCallable(jobFilter, command, "", false, null);
        return j.getJob();
    }

    private Map<String, String> getMapOfAttributes(String commandParam) {

        Map<String, String> listOfAttributes = new HashMap<String, String>();
        if (commandParam == null || commandParam.isEmpty()) {
            listOfAttributes.put("at", "now");
        } else {
            String name = "";
            String value = "";
            String[] attributes = commandParam.split(",");
            for (int i = 0; i < attributes.length; i++) {
                String attribute = attributes[i];
                String[] parts = attribute.split("=");
                if (parts.length == 1) {
                    value = parts[0];
                    name = "at";
                } else if (parts.length == 2) {
                    value = parts[1];
                    name = parts[0];
                }
                listOfAttributes.put(name.trim(), value);
            }
        }
        return listOfAttributes;
    }

    private List<NameValuePair> substituteParameters(List<NameValuePair> parameters) throws JAXBException {

        JobchainNodeConfiguration jobchainNodeConfiguration = new JobchainNodeConfiguration();
        Map<String, String> taskParameters = new HashMap<String, String>();
        FunctionResolver functionResolver = new FunctionResolver();

        for (NameValuePair param : parameters) {
            taskParameters.put(param.getName(), param.getValue());
        }

        jobchainNodeConfiguration.setListOfTaskParameters(taskParameters);
        jobchainNodeConfiguration.substituteTaskParamters();
        for (Entry<String, String> entry : jobchainNodeConfiguration.getListOfTaskParameters().entrySet()) {
            String paramName = entry.getKey();
            String paramValue = entry.getValue();
            paramValue = functionResolver.resolveFunctions(paramValue);
            if (paramValue != null) {
                if (isTraceEnabled) {
                    LOGGER.debug("Replace task parameter " + paramName + " old value=" + taskParameters.get(paramName) + " with new value="
                            + paramValue);
                }
                taskParameters.put(paramName, paramValue);
            }
        }
        parameters = new ArrayList<NameValuePair>();
        for (Entry<String, String> entry : taskParameters.entrySet()) {
            NameValuePair param = new NameValuePair();
            param.setName(entry.getKey());
            param.setValue(entry.getValue());
            parameters.add(param);
        }

        return parameters;
    }

    public Map<String, String> testGetMapOfAttributes(String commandParam) {
        return getMapOfAttributes(commandParam);
    }

    public String buildJobStartXml(JSInCondition inCondition, String commandParam) throws JocException, JAXBException {
        JobV jobV = this.getJob(inCondition.getNormalizedJob());
        XMLBuilder xml = new XMLBuilder("start_job");
        xml.addAttribute("job", inCondition.getNormalizedJob());

        Map<String, String> listOfAttributes = getMapOfAttributes(commandParam);
        if (listOfAttributes.get("force") == null) {
            listOfAttributes.put("force", "no");
        }
        listOfAttributes.forEach((name, value) -> xml.addAttribute(name, value));

        xml.add(getParams(substituteParameters(jobV.getParams())));
        return xml.asXML();
    }

    private Element getParams(List<NameValuePair> params) throws SessionNotExistException {
        Element paramsElem = XMLBuilder.create("params");
        if (params != null) {
            for (NameValuePair param : params) {
                paramsElem.addElement("param").addAttribute("name", param.getName()).addAttribute("value", param.getValue());
            }
        }

        return paramsElem;
    }

}
