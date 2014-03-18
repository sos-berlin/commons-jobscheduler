package com.sos.testframework.h2;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>A list of resources given as resource String, File or URL</h1>
 * <p>
 *     A ResourceList is a list of physical files resides in a temporary folder of the file system. Depending on the type of object added to the list a copy of the object is placed in a temporary folder (URL or Resource) or a pointer to the real object (File) is stored in the list.
 * </p>
 * <p>
 *     It is important to call the release method at the end to ensure that the temporary folder and its content will be delete.
 * </p>
 */
public class ResourceList {

    private static Logger logger = LoggerFactory.getLogger(ResourceList.class);

    private final Map<String,File> resources = new HashMap<String,File>();
    private final File workingDirectory;

    /**
     * Using this constructor it is possible to define the members of the ResourceList via a Map. The Map can contain objects of type String, URL or File.
     * Other object types will result in a warning (not an exception).
     * It is possible to add new member with the different variants of the add() method.
     * @param resources
     */
    public ResourceList(Map<String,?> resources) {
        this.workingDirectory = Files.createTempDir();
        for(String className : resources.keySet()) {
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

    /**
     * A constructor to create an empty resource list. It is possible to add new member with the different variants of the add() method.
     */
    public ResourceList() {
        this.workingDirectory = Files.createTempDir();
    }

    /**
     * Add a resource to the list.
     * @param className
     * @param resource
     */
    public void add(String className, String resource) {
        resources.put(className, createFileFromResource(resource));
    }

    /**
     * Add an URL to the list.
     * @param className
     * @param resource
     */
    public void add(String className, URL resource) {
        resources.put(className, createFileFromURL(resource)) ;
    }

    /**
     * Add a file to the list.
     * @param className
     * @param resource
     */
    public void add(String className, File resource) {
        resources.put(resource.getAbsolutePath(), resource);
    }

    private File createFileFromResource(String resource) {
        URL url = Resources.getResource(resource);
        return  createFileFromURL(url);
    }

    /**
     * Creates a file from content of the given resource resides in the workingDirectory.
     * @param resource
     * @return
     */
    private File createFileFromURL(URL resource) {
        logger.info("Resource is " + resource.getPath());
        String[] arr = resource.getPath().split("/");
        String filenameWithoutPath = arr[arr.length-1];
        File configFile = null;
        try {
            String fileContent = Resources.toString(resource, Charset.defaultCharset());
            configFile = createFileFromString(fileContent, filenameWithoutPath);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return  configFile;
    }

    /**
     * Create a file with given fileContent in a file named filenameWithoutPath (resides in the workingDirectory).
     * @param fileContent
     * @param filenameWithoutPath
     * @return
     */
    private File createFileFromString(String fileContent, String filenameWithoutPath) {
        File configFile = null;
        logger.info("Copy resource to folder " + workingDirectory.getAbsolutePath());
        logger.info("Targetname is " + filenameWithoutPath);
        try {
            workingDirectory.mkdirs();
            logger.info("Create file from Resource String:\n" + fileContent);
            configFile = new File(workingDirectory, filenameWithoutPath);
            logger.info("Write file " + configFile.getAbsolutePath());
            Files.write(fileContent, configFile, Charset.defaultCharset());
        } catch (IOException e) {
            logger.error("Could not create File from resource String:\n" + fileContent);
            throw new RuntimeException(e);
        }
        return configFile;
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
        removeFolderRecursively(workingDirectory);
    }

    private static void removeFolderRecursively(File folder) {
        for(File f : folder.listFiles()) {
            if (f.isFile())
                deleteFile(f);
            else
                removeFolderRecursively(f);
        }
        deleteFile(folder);
    }

    private static void deleteFile(File f) {
        String type = (f.isDirectory()) ? "folder" : "file";
        if(f.delete())
            logger.info("Temporary " + type + " [" + f.getAbsolutePath() + "] removed succesfully.");
        else
            logger.warn("Could not delete Temporary " + type + " [" + f.getAbsolutePath() + "].");
    }

}
