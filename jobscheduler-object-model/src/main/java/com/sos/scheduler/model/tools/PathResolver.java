package com.sos.scheduler.model.tools;

import java.io.File;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class PathResolver {

    private final static Logger LOGGER = LoggerFactory.getLogger(PathResolver.class);

    private PathResolver() {
    }

    public static String getRelativePath(String rootDir, String baseDir, String relativeName) {
        String result = normalizePath(relativeName);
        if (!result.startsWith("/")) {
            result = getPath(baseDir, relativeName);
            String rootPath = resolvePath(rootDir);
            if (!result.startsWith(rootPath)) {
                throw new JobSchedulerException("the path " + result + " point outside the root " + rootPath);
            }
            result = result.replace(normalizePath(rootDir), "");
        }
        return result;
    }

    public static String getAbsolutePath(String rootDir, String baseDir, String relativeName) {
        return normalizePath(rootDir) + getRelativePath(rootDir, baseDir, relativeName);
    }

    public static boolean isAbsolutePath(String path) {
        return isAbsoluteWindowsPath(path) || isAbsoluteUnixPath(path);
    }

    public static boolean isAbsoluteWindowsPath(String path) {
        String normalizedPath = normalizePath(path);
        return ":/".equals(normalizedPath.substring(1, 3));
    }

    public static boolean isAbsoluteUnixPath(String path) {
        String normalizedPath = normalizePath(path);
        return "/".equals(normalizedPath.substring(0, 1));
    }

    private static String getPath(String basePath, String relativeName) {
        String result = "";
        try {
            result = getPath(basePath, relativeName, false);
        } catch (FileNotFoundException e) {
            //
        }
        return result;
    }

    private static String getPath(String basePath, String relativePath, boolean throwException) throws FileNotFoundException {
        basePath = normalizePath(basePath);
        testPath(basePath, throwException);
        relativePath = normalizePath(relativePath);
        if (relativePath.startsWith("/")) {
            return relativePath;
        }
        basePath += "/";
        String result = resolvePath(basePath + relativePath);
        testPath(result, throwException);
        return result;
    }

    private static String stripTrailingSlash(String text) {
        return text.endsWith("/") ? text.substring(0, text.length() - 1) : text;
    }

    private static String normalizeSlashes(String text) {
        return text.replace("://", ":::").replace("//", "/").replace(":::", "://");
    }

    private static void testPath(String path, boolean throwException) throws FileNotFoundException {
        File f = new File(path);
        if (!f.exists()) {
            String message = "the directory " + path + " does not exist.";
            if (throwException) {
                throw new FileNotFoundException(message);
            }
            LOGGER.warn(message);
        }
    }

    public static String normalizePath(String path) {
        return normalizeSlashes(stripTrailingSlash(path.replace("\\", "/")));
    }

    public static String resolvePath(String path) {
        String result = normalizePath(path);
        if (result.startsWith("./")) {
            result = result.substring(2);
        }
        if (result.endsWith("/.")) {
            result = result.substring(0, result.length() - 2);
        }
        String workingCopy = result.replace("/./", "/");
        do {
            result = workingCopy;
            workingCopy = parseRelative(result);
        } while (!workingCopy.equals(result));
        return result;
    }

    private static String parseRelative(String path) {
        String result = path;
        String[] arr = path.split("/");
        for (int i = 0; i < arr.length; i++) {
            if ("..".equals(arr[i])) {
                result = removePart(arr, i - 1, i);
                break;
            }
        }
        return result;
    }

    private static String removePart(String[] arr, int start, int end) {
        StringBuilder result = new StringBuilder();
        for (int j = 0; j < arr.length; j++) {
            if (j < start || j > end) {
                result.append(arr[j]);
                result.append("/");
            }
        }
        return stripTrailingSlash(result.toString());
    }

}