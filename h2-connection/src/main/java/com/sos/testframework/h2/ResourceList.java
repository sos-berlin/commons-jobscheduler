package com.sos.testframework.h2;

import com.google.common.io.Resources;
import com.sos.resources.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** <h1>A list of resources given as resource String, File or URL</h1>
 * <p>
 * A ResourceList is a list of physical files resides in a temporary folder of
 * the file system. Depending on the type of object added to the list a copy of
 * the object is placed in a temporary folder (URL or Resource) or a pointer to
 * the real object (File) is stored in the list.
 * </p>
 * <p>
 * It is important to call the release method at the end to ensure that the
 * temporary folder and its content will be delete.
 * </p> */
public class ResourceList {

    private static Logger logger = LoggerFactory.getLogger(ResourceList.class);

    private final Map<String, File> resources = new HashMap<String, File>();
    private final File workingDirectory;

    /** Using this constructor it is possible to define the members of the
     * ResourceList via a Map. The Map can contain objects of type String, URL
     * or File. Other object types will result in a warning (not an exception).
     * It is possible to add new member with the different variants of the add()
     * method.
     * 
     * @param resources */
    public ResourceList(Map<String, ?> resources) {
        this.workingDirectory = ResourceHelper.getInstance().createWorkingDirectory();
        for (String className : resources.keySet()) {
            Object o = resources.get(className);
            if (o instanceof String) {
                add(className, (String) o);
                continue;
            }
            if (o instanceof URL) {
                add(className, (URL) o);
                continue;
            }
            if (o instanceof File) {
                add(className, (File) o);
                continue;
            }
            logger.warn("An object of class [" + o.getClass().getName() + "] is not valid as parameter for constructor of " + ResourceList.class.getName());
        }
    }

    /** A constructor to create an empty resource list. It is possible to add new
     * member with the different variants of the add() method. */
    public ResourceList() {
        this.workingDirectory = ResourceHelper.getInstance().createWorkingDirectory();
    }

    /** Add a resource to the list.
     * 
     * @param className
     * @param resource */
    public void add(String className, String resource) {
        resources.put(className, createFileFromResource(resource));
    }

    /** Add an URL to the list.
     * 
     * @param className
     * @param resource */
    public void add(String className, URL resource) {
        File resourceFile = ResourceHelper.getInstance().createFileFromURL(resource);
        resources.put(className, resourceFile);
    }

    /** Add a file to the list.
     * 
     * @param className
     * @param resource */
    public void add(String className, File resource) {
        resources.put(resource.getAbsolutePath(), resource);
    }

    private File createFileFromResource(String resource) {
        URL url = Resources.getResource(resource);
        File resourceFile = ResourceHelper.getInstance().createFileFromURL(url);
        return resourceFile;
    }

    public List<File> getFilelist() {
        List fileList = new ArrayList<File>();
        fileList.addAll(resources.values());
        return fileList;
    }

    public List<String> getClasslist() {
        List classList = new ArrayList<File>();
        classList.addAll(resources.keySet());
        return classList;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void release() {
        ResourceHelper.destroy();
    }

}
