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
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JobsVCallable;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.common.NameValuePair;
import com.sos.joc.model.job.JobFilter;
import com.sos.joc.model.job.JobV;
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

    private List<NameValuePair> substituteParameters(List<NameValuePair> parameters, List<NameValuePair> envVars) throws JAXBException {

        JobchainNodeConfiguration jobchainNodeConfiguration = new JobchainNodeConfiguration();
        Map<String, String> schedulerParameters = new HashMap<String, String>();
        Map<String, String> taskParameters = new HashMap<String, String>();
        FunctionResolver functionResolver = new FunctionResolver();

        if (parameters != null) {
            for (NameValuePair param : parameters) {
                taskParameters.put(param.getName(), param.getValue());
            }
            if (envVars != null) {
                for (NameValuePair envVar : envVars) {
                    if (envVar != null) {
                        schedulerParameters.put(envVar.getName(), envVar.getValue());
                    }
                }
            }
            jobchainNodeConfiguration.setListOfTaskParameters(taskParameters);
            jobchainNodeConfiguration.setListOfSchedulerParameters(schedulerParameters);
            jobchainNodeConfiguration.substituteTaskParamters();

            for (NameValuePair entry : parameters) {
                String paramName = entry.getName();
                String paramValue = jobchainNodeConfiguration.getListOfTaskParameters().get(paramName);

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
        }
        for (Entry<String, String> entry : taskParameters.entrySet()) {
            NameValuePair param = new NameValuePair();
            param.setName(entry.getKey());
            param.setValue(entry.getValue());
            parameters.add(param);
        }

        return parameters;
    }

    private List<NameValuePair> getDefaultEnvVars(JobStarterOptions inCondition) {

        EnvVarCreator envVarCreator = new EnvVarCreator();

        List<NameValuePair> envVars = new ArrayList<NameValuePair>();
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_JOBSTREAM"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_TIME"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_CENTURY"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_DAY"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_YEAR"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_YEAR_YY"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_YEAR_YYYY"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_MONTH"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_MONTH_NAME"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_DATE"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_DATE_YY"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_DATE_YYYY"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_FOLDER"));
        envVars.add(envVarCreator.getEnvVar(inCondition, "JS_JOBNAME"));

        return envVars;
    }

    public Map<String, String> testGetMapOfAttributes(String commandParam) {
        return getMapOfAttributes(commandParam);
    }

    public String buildJobStartXml(JobStarterOptions jobStartOptions, String commandParam) throws JocException, JAXBException {
        JobV jobV = this.getJob(jobStartOptions.getNormalizedJob());
        XMLBuilder xml = new XMLBuilder("start_job");
        xml.addAttribute("job", jobStartOptions.getNormalizedJob());

        Map<String, String> listOfAttributes = getMapOfAttributes(commandParam);
        if (listOfAttributes.get("force") == null) {
            listOfAttributes.put("force", "no");
        }
        listOfAttributes.forEach((name, value) -> xml.addAttribute(name, value));

        List<NameValuePair> envVars = getDefaultEnvVars(jobStartOptions);
        List<NameValuePair> params = substituteParameters(jobV.getParams(), envVars);
        if (jobStartOptions.getListOfParameters() != null) {
            if (params == null) {
                params = new ArrayList<NameValuePair>();
            }
            for (Entry<String, String> param : jobStartOptions.getListOfParameters().entrySet()) {
                NameValuePair nameValuePair = new NameValuePair();
                nameValuePair.setName(param.getKey());
                if (param.getValue() == null) {
                    nameValuePair.setValue("");
                }else {
                    nameValuePair.setValue(param.getValue());
                }
                params.add(nameValuePair);
            }
        }
        xml.add(getParams(params));
        xml.add(getEnv(envVars));
        String xmlString = xml.asXML();
        xmlString =  xmlString.replaceAll("[^\\x09\\x0A\\x0D\\x20-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]", "");
        return xmlString;
    }

    private Element getParams(List<NameValuePair> params) throws SessionNotExistException {
        Element paramsElem = XMLBuilder.create("params");
        if (params != null) {
            for (NameValuePair param : params) {
                if (param.getValue() != null) {
                    String value = param.getValue().replaceAll("[^\\x09\\x0A\\x0D\\x20-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]", "");
                    paramsElem.addElement("param").addAttribute("name", param.getName()).addAttribute("value", value);
                }
            }
        }

        return paramsElem;
    }

    private Element getEnv(List<NameValuePair> envVars) throws SessionNotExistException {
        Element paramsElem = XMLBuilder.create("environment");
        if (envVars != null) {
            for (NameValuePair envVar : envVars) {
                if (envVar.getValue() != null) {
                    String value = envVar.getValue().replaceAll("[^\\x09\\x0A\\x0D\\x20-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]", "");
                    try {
                        paramsElem.addElement("variable").addAttribute("name", envVar.getName()).addAttribute("value", value);
                    } catch (Exception e) {

                    }
                }
            }
        }
        return paramsElem;
    }

}
