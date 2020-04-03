package com.sos.scheduler.model.objects;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.tools.PathResolver;

/** @author oh */
public class JSObjInclude extends Include {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSObjInclude.class);

    public JSObjInclude(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    @Override
    public void setHotFolderSrc(ISOSProviderFile pobjVirtualFile) {
        if (pobjVirtualFile != null) {
            if (getFile() != null) {
                String fileName = replaceEnvVariables(getFile());
                if (!PathResolver.isAbsolutePath(fileName)) {
                    throw new JobSchedulerException("only an absolute path for the attribute 'file' is allowed (path is '" + fileName + "')");
                }
                ISOSProviderFile vfInclude = objFactory.getFileHandleOrNull(fileName);
                super.setHotFolderSrc(vfInclude);
            } else {
                File f = new File(pobjVirtualFile.getName());
                String baseFolder = f.getAbsolutePath().replace(f.getName(), "");
                String liveFolder = baseFolder;
                if (objFactory.getLiveFolderOrNull() != null) {
                    liveFolder = objFactory.getLiveFolderOrNull().getHotFolderSrc().getName();
                    String fileName = PathResolver.getAbsolutePath(liveFolder, baseFolder, getLiveFileResolved(getLiveFile()));
                    ISOSProviderFile vfInclude = objFactory.getFileHandleOrNull(fileName);
                    super.setHotFolderSrc(vfInclude);
                }
            }
        }
    }

    private String getLiveFileResolved(String liveFile) {
        String result = PathResolver.normalizePath(replaceEnvVariables(liveFile));
        if (PathResolver.isAbsoluteWindowsPath(liveFile)) {
            throw new JobSchedulerException("an absolute path for the attribute 'live_file' is not allowed (path is '" + result + "')");
        }
        return result;
    }

    private String replaceEnvVariables(String rawName) {
        String result = rawName;
        for (String key : System.getenv().keySet()) {
            String searchFor = getRegExp("${" + key + "}");
            result = result.replaceAll(searchFor, getRegExp(System.getenv(key)));
        }
        LOGGER.info("after replacing: " + result);
        return result;
    }

    private String getRegExp(String rawString) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < rawString.length(); i++) {
            char ch = rawString.charAt(i);
            if ("\\.^$|?*+[]{}()".indexOf(ch) != -1) {
                b.append("\\").append(ch);
            } else {
                b.append(ch);
            }
        }
        return b.toString();
    }

    public String getContent() {
        return getHotFolderSrc().file2String();
    }

}