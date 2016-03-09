package com.sos.scheduler.generics;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_impl;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class GenericAPIJobJSAdapterClass extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = Logger.getLogger(GenericAPIJobJSAdapterClass.class);
    private GenericAPIJobOptions objO = null;
    private ClassLoader classLoader = null;
    private HashMap<String, Job_impl> objLoadedClasses = null;

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }

    @Override
    public boolean spooler_init() {
        return super.spooler_init();
    }

    @Override
    public boolean spooler_process() throws Exception {
        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            throw e;
        }
        return signalSuccess();
    }

    @Override
    public void spooler_exit() {
        super.spooler_exit();
    }

    private void doProcessing() throws Exception {
        GenericAPIJob objR = new GenericAPIJob();
        objO = objR.Options();
        objO.CurrentNodeName(getCurrentNodeName());
        objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
        objO.CheckMandatory();
        if (objLoadedClasses == null) {
            objLoadedClasses = new HashMap<String, Job_impl>();
        }
        String strNameOfClass2Load = objO.javaClassName.Value();
        Job_impl objClass2Call = objLoadedClasses.get(strNameOfClass2Load);
        if (objClass2Call == null) {
            objClass2Call = getDynamicClassInstance(strNameOfClass2Load);
            if (objClass2Call == null && objO.javaClassPath.IsNotEmpty()) {
                String[] strJars = objO.javaClassPath.Value().split(";");
                for (String strJarFileName : strJars) {
                    File objF = new File(strJarFileName);
                    if (objF.isFile() && objF.canExecute()) {
                        addJarsToClassPath(Thread.currentThread().getContextClassLoader(), new File[] { objF });
                    } else {
                        throw new JobSchedulerException(String.format("ClasspathElement '%1$s' not found", strJarFileName));
                    }
                }
                objClass2Call = getDynamicClassInstance(strNameOfClass2Load);
            }
            if (objClass2Call == null) {
                throw new JobSchedulerException(strNameOfClass2Load);
            }
            objLoadedClasses.put(strNameOfClass2Load, objClass2Call);
            objClass2Call.spooler = spooler;
            objClass2Call.spooler_log = spooler_log;
            objClass2Call.spooler_task = spooler_task;
            objClass2Call.spooler_job = spooler_job;
            objClass2Call.spooler_init();
            objClass2Call.spooler_open();
        }
        objClass2Call.spooler_process();
    }

    @Override
    public void spooler_close() {

    }

    @Override
    public void spooler_on_error() {

    }

    @Override
    public void spooler_on_success() {

    }

    private void addJarsToClassPath(final ClassLoader classLoader1, final File[] jars) {
        if (classLoader1 instanceof URLClassLoader) {
            try {
                Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
                if (null != addUrlMethod) {
                    addUrlMethod.setAccessible(true);
                    for (File jar : jars) {
                        try {
                            addUrlMethod.invoke(classLoader1, jar.toURI().toURL());
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private Job_impl getDynamicClassInstance(final String pstrLoadClassNameDefault) {
        String strLoadClassName = pstrLoadClassNameDefault;
        Job_impl objC = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
            Class objA = classLoader.loadClass(strLoadClassName);
            objC = (Job_impl) objA.newInstance();
            if (objC instanceof Job_impl) {
                LOGGER.debug("Job_impl is part of class   ...  " + objA.toString());
            } else {
                LOGGER.error("Job_impl not part of class" + objA.toString());
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage() + " not found...", e);
            throw new JobSchedulerException(e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobSchedulerException(e.getLocalizedMessage());
        }
        return objC;
    }

}
