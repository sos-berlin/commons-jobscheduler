package com.sos.JSHelper.io.Files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
// import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/** @author KlausBuettner */
public class JSTextFile extends JSFile {

    private static final long serialVersionUID = 1L;

    public JSTextFile(String pstrFileName) {
        super(pstrFileName);
    }

    public JSTextFile(String pstrPathName, String pstrFileName) {
        super(pstrPathName, pstrFileName);
    }

    public void replaceString(String pstrRegEx, String pstrReplacement) throws IOException {
        String strFileN = this.getAbsolutePath();
        File tempFile = new File(strFileN + "~");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        File file = new File(strFileN);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            line = line.replaceAll(pstrRegEx, pstrReplacement);
            writer.write(line);
            writer.newLine();
        }
        writer.close();
        reader.close();
        file.delete();
        tempFile.renameTo(file);
    }

}