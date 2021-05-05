package com.sos.JSHelper.Options;

import java.io.File;
import java.util.HashMap;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFolder;

public class SOSOptionFolderName extends SOSOptionFileName {

    private static final long serialVersionUID = 1197392401084895147L;
    private static final HashMap<String, String> DEFAULT_PROPOSALS = new HashMap<>();

    public SOSOptionFolderName(final String name) {
        super(null, "", "description", name, "", false);
    }

    public SOSOptionFolderName(final JSOptionsClass parent, final String key, final String description, final String value, final String defaultValue,
            final boolean isMandatory) {
        super(parent, key, description, value, defaultValue, isMandatory);
        intOptionType = isOptionTypeFolder;
    }

    @Override
    public String getValue() {
        if (strValue == null) {
            strValue = "";
        }
        String value = super.getValue();
        if (isNotEmpty() && !(value.endsWith("/") || value.endsWith("\\") || isDotFolder())) {
            value = value + "/";
        }
        return value;
    }

    public boolean isDotFolder() {
        String val = super.getValue();
        return ".".equals(val) || "..".equals(val);
    }

    public File[] listFiles() {
        File[] list = this.getJSFile().listFiles();
        if (list == null) {
            throw new JobSchedulerException(String.format("No Files found for pathname '%1$s'", strValue));
        }
        return list;
    }

    public String[] getSubFolderArray() {
        String[] result = null;
        try {
            String path = strValue.trim().replaceAll("/(\\s*/)+", "/");
            String slash = "";
            int iStart = 0;
            if (path.startsWith("/")) {
                slash = "/";
                iStart = 1;
            }

            String[] arr = path.substring(iStart).split("/");
            result = new String[arr.length];
            int i = 0;
            StringBuilder sb = new StringBuilder();
            for (String subFolder : arr) {
                sb.append(slash).append(subFolder);
                slash = "/";
                result[i] = sb.toString();
                i++;
            }
        } catch (Exception e) {
            //
        }
        return result;
    }

    public String[] getSubFolderArrayReverse() {
        String[] result = null;
        try {
            String path = strValue.trim().replaceAll("/(\\s*/)+", "/");
            String slash = "";
            int iStart = 0;
            if (path.startsWith("/")) {
                slash = "/";
                iStart = 1;
            }
            String[] arr = path.substring(iStart).split("/");
            result = new String[arr.length];
            int i = arr.length - 1;
            StringBuilder sb = new StringBuilder();
            for (String subFolder : arr) {
                sb.append(slash).append(subFolder);
                slash = "/";
                result[i] = sb.toString();
                i--;
            }
        } catch (Exception e) {
            //
        }
        return result;
    }

    public JSFolder getFolder() {
        return new JSFolder(strValue);
    }

    @Override
    public void addProposal(final String proposal) {
        if (proposal != null && !proposal.trim().isEmpty()) {
            String p = proposal.trim();
            SOSOptionFolderName.DEFAULT_PROPOSALS.put(p, p);
        }
    }

    @Override
    public String[] getAllProposals(String text) {
        return SOSOptionFolderName.DEFAULT_PROPOSALS.keySet().toArray(new String[0]);
    }

}