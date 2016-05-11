package sos.scheduler.misc;

import java.io.File;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import org.apache.commons.lang3.text.StrSubstitutor;

public class ParameterSubstitutor {

    private HashMap<String, String> keylist;
    private StrSubstitutor strSubstitutorEnv;
    private StrSubstitutor strSubstitutor;
    private boolean caseSensitive = false;
    private String openTag = "${";

    public void setOpenTag(String openTag) {
        this.openTag = openTag;
    }

    public void setCloseTag(String closeTag) {
        this.closeTag = closeTag;
    }

    private String closeTag = "}";

    public ParameterSubstitutor() {
        super();
    }

    public ParameterSubstitutor(boolean caseSensitive_) {
        super();
        this.caseSensitive = caseSensitive_;
    }

    public ParameterSubstitutor(boolean caseSensitive_, String openTag_, String closeTag_) {
        super();
        this.openTag = openTag_;
        this.closeTag = closeTag_;
        this.caseSensitive = caseSensitive_;
    }

    public ParameterSubstitutor(String openTag_, String closeTag_) {
        super();
        this.openTag = openTag_;
        this.closeTag = closeTag_;
    }

    public void addKey(String k, String v) {
        if (keylist == null) {
            keylist = new HashMap<String, String>();
        }
        if (caseSensitive) {
            keylist.put(k, v);
        } else {
            keylist.put(k.toLowerCase(), v);
        }
    }

    public String replaceEnvVars(String source) {
        if (strSubstitutorEnv == null) {
            if (caseSensitive) {
                strSubstitutorEnv = new StrSubstitutor(System.getenv());
            } else {
                strSubstitutorEnv = new StrSubstitutor(new CaseInsensitivLookupForParameter<String>(System.getenv()));
            }
        }
        return strSubstitutorEnv.replace(source);
    }

    public String replaceSystemProperties(String source) {
        return StrSubstitutor.replaceSystemProperties(source);
    }

    public String replace(final String source) {
        if (strSubstitutor == null) {
            if (caseSensitive) {
                strSubstitutor = new StrSubstitutor(keylist);
            } else {
                strSubstitutor = new StrSubstitutor(new CaseInsensitivLookupForParameter<String>(keylist));
            }
        }
        strSubstitutor.setVariablePrefix(openTag);
        strSubstitutor.setVariableSuffix(closeTag);
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

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

}
