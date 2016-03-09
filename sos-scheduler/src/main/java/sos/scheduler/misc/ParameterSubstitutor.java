package sos.scheduler.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.poi.util.IOUtils;

public class ParameterSubstitutor {

    private HashMap<String, String> keylist;

    public void addKey(String k, String v) {
        if (keylist == null) {
            keylist = new HashMap<String, String>();
        }
        keylist.put(k, v);
    }

    public String replaceEnvVars(String source) {
        StrSubstitutor strSubstitutor = new StrSubstitutor(System.getenv());
        return strSubstitutor.replace(source);
    }

    public String replaceSystemProperties(String source) {
        return StrSubstitutor.replaceSystemProperties(source);
    }

    public String replace(final String source) {
        StrSubstitutor strSubstitutor = new StrSubstitutor(keylist);
        return strSubstitutor.replace(source);
    }

    public void replaceInFile(File in, File out) throws IOException {
        Path inPath = Paths.get(in.getAbsolutePath());
        Path outPath = Paths.get(out.getAbsolutePath());
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(inPath), charset);
        content = replace(content);
        Files.write(outPath, content.getBytes(charset));
    }
}
