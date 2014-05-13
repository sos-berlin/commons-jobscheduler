package com.sos.hibernate.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 16.01.14 14:18
 * @uthor stefan.schaedlich@sos-berlin.com
 */
public class ClassList {

    private final ClassLoader classLoader;
    private final List<Class> classes;

    public ClassList() {
        this.classes = new ArrayList<Class>();
        this.classLoader = ClassLoader.getSystemClassLoader();
    }

    public void addClassIfExist(String className) {
        try {
            Class c = classLoader.loadClass(className);
            add(c);
        } catch (ClassNotFoundException e) {
        }
    }

    public void add(Class c) {
        if(!classes.contains(c))
            classes.add(c);
    }

    public void merge(List<Class> classesToMerge) {
        for(Class c : classesToMerge)
            add(c);
    }

    public List<Class> getClasses() {
        return classes;
    }

}
