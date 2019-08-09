package com.sos.hibernate.classes;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 
/** @author stefan schaedlich */
public class ClassList {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassList.class);

    private final ClassLoader classLoader;
    private final List<Class<?>> classes;

    public ClassList() {
        this.classes = new ArrayList<Class<?>>();
        this.classLoader = ClassLoader.getSystemClassLoader();
    }

    public void addClassIfExist(String className) {
        try {
            Class<?> c = classLoader.loadClass(className);
            add(c);
        } catch (ClassNotFoundException e) {
            LOGGER.warn(String.format("Class %s not found in the classpath",className));       
         }
    }

    public void add(Class<?> c) {
        if (!classes.contains(c)) {
            classes.add(c);
        }
    }

    public void merge(List<Class<?>> classesToMerge) {
        for (Class<?> c : classesToMerge) {
            add(c);
        }
    }

    public List<Class<?>> getClasses() {
        return classes;
    }

}