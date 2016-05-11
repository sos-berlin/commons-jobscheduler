package com.sos.VirtualFileSystem.Filter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.common.io.Files;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;
import com.sos.graphviz.GlobalNodeProperties;
import com.sos.graphviz.Graph;
import com.sos.graphviz.GraphIO;
import com.sos.graphviz.Node;
import com.sos.graphviz.SingleNodeProperties;
import com.sos.graphviz.enums.FileType;
import com.sos.graphviz.enums.RankDir;
import com.sos.graphviz.enums.Shape;

public class SOSFilteredFileReaderTest implements ISOSFilteredFileReader {

    private static final Logger LOGGER = Logger.getLogger(SOSFilteredFileReaderTest.class);
    private final SOSFilteredFileReader objR = null;
    JSFile objOutput = new JSFile("c:/temp/anubex.txt");
    String strLastTooken = "";
    String strLastRecord = "";
    HashMap<String, Integer> mapXref = new HashMap<String, Integer>();
    HashMap<String, HashMap<String, String>> mapEvents = new HashMap();
    HashMap<String, String> mapEvent = new HashMap();

    @Test
    public void testRun() {
        LOGGER.info("start");
        JSFile objFile = new JSFile("R:/backup/projects/anubex/Events/10P9I029.09249.000");
        SOSFilteredFileReader objR = new SOSFilteredFileReader(objFile);
        objR.setProcesshandler(this);
        SOSFilterOptions objFO = objR.Options();
        objFO.excludeLinesBefore.Value("^(\\$EVENT_START).*$");
        objFO.exclude_lines_after.Value("^\\$EVENT_END");
        objFO.excludeEmptyLines.value(true);
        objFO.excludeLines.Value("^(\\*.*)|^(JOB  ).*|.*(LIST ALL FOR).*$");
        objR.run();
    }

    private void createDiagramForSubTree(final String pstrSubTreeName) {
        Graph g = new Graph();
        g.getGraphProperties().setDirection(RankDir.TB);
        g.getGraphProperties().setCompound(true);
        g.getGraphProperties().setId(pstrSubTreeName);
        GlobalNodeProperties gn = g.getGlobalNodeProperties();
        gn.setShape(Shape.oval);
        for (String strKey : mapEvents.keySet()) {
            HashMap<String, String> objH = mapEvents.get(strKey);
            String strGroup = objH.get("GROUP");
            if (strGroup.equalsIgnoreCase(pstrSubTreeName)) {
                String strFromObject = objH.get("GROUP") + objH.get("EVENTNAME");
                String strT = objH.get("ESUCC_EVENT");
                if (strT != null) {
                    String[] strESUCC_EVENT = strT.split(",");
                    String[] strESUCC_GROUP = objH.get("ESUCC_GROUP").split(",");
                    for (int i = 0; i < strESUCC_EVENT.length; i++) {
                        String strToObject = strESUCC_GROUP[i] + strESUCC_EVENT[i];
                        Node n1 = g.newNode(Quoted(strFromObject));
                        SingleNodeProperties p = n1.getSingleNodeProperties();
                        String strLabel = objH.get("DESCR");
                        if (strLabel == null) {
                            strLabel = "";
                        }
                        strLabel = "<" + strLabel + "<br/>" + strFromObject + ">";
                        p.setLabel(strLabel);
                        Node n2 = g.newNode(Quoted(strToObject));
                        SingleNodeProperties p2 = n2.getSingleNodeProperties();
                        g.newEdge(n1, n2);
                    }
                }
                String strPrim = objH.get("WHEN_PRIM");
                if (strPrim != null) {
                    Node n1 = g.newNode(Quoted(objH.get("WHEN_QNAM") + objH.get("WHEN_PRIM")));
                    Node n2 = g.newNode(Quoted(strFromObject));
                    g.newEdge(n1, n2);
                }
            }
        }
        File tempDir = Files.createTempDir();
        GraphIO io = new GraphIO(g);
        io.setDotDir(tempDir.getAbsolutePath());
        String strTargetDir = "c:/temp/graphviz";
        try {
            io.writeGraphToFile(FileType.svg, new File(strTargetDir, "test.svg").getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String Quoted(final String pstrV) {
        String strR = pstrV.trim();
        if (strR != null) {
            strR = "\"" + strR + "\"";
        } else {
            strR = " ";
        }
        return strR;
    }

    private String getTooken(final String pstrValue) {
        String[] strA = pstrValue.split(":");
        return strA[0].trim();
    }

    private String getValue(final String pstrValue) {
        String[] strA = pstrValue.split(":");
        String strRet = strA[1].trim();
        if (strA.length > 2) {
            strRet += ":" + strA[2];
        }
        return strRet;
    }

    private void addTooken(final String pstrValue) {
        Integer intS = mapXref.get(pstrValue);
        if (intS == null) {
            intS = 0;
        }
        mapXref.put(pstrValue, ++intS);
    }

    @Override
    public void processRecord(final String pstrRecord) {
        try {
            if (pstrRecord.startsWith("$")) {
                if (strLastRecord.trim().length() > 0) {
                    objOutput.WriteLine(strLastRecord);
                    strLastRecord = "";
                    strLastTooken = "";
                }
                objOutput.WriteLine(pstrRecord);
                if (pstrRecord.startsWith("$GENERAL_PARM_START")) {
                    String strKey = mapEvent.get("EVENTNAME");
                    if (strKey != null) {
                        strKey = strKey.trim();
                        strKey = mapEvent.get("GROUP") + "." + strKey;
                        HashMap<String, String> strH = mapEvents.get(strKey);
                        if (strH != null) {
                            LOGGER.info("duplicate Event: " + strKey + ", FileName = " + objR.getCurrentFile().getName());
                            strKey += "-dup";
                        }
                        mapEvents.put(strKey, mapEvent);
                        mapEvent = new HashMap<String, String>();
                    }
                }
            } else {
                if (!pstrRecord.trim().endsWith(":")) {
                    String strTooken = getTooken(pstrRecord);
                    addTooken(strTooken);
                    addEventAttribut(strTooken, getValue(pstrRecord));
                    if (!strLastTooken.equals(strTooken)) {
                        if (!strLastRecord.trim().isEmpty()) {
                            objOutput.WriteLine(strLastRecord);
                        }
                        strLastRecord = pstrRecord;
                        strLastTooken = strTooken;
                    } else {
                        String[] strA = pstrRecord.split(":");
                        if (strA.length > 1) {
                            strLastRecord += "," + strA[1].trim();
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void addEventAttribut(final String pstrTooken, final String pstrValue) {
        String strT = mapEvent.get(pstrTooken);
        if (strT == null) {
            mapEvent.put(pstrTooken, pstrValue);
        } else {
            strT = strT.trim();
            strT += "," + pstrValue;
            mapEvent.put(pstrTooken, strT);
        }
    }

    @Override
    public void atStartOfData() {
        //
    }

    @Override
    public void atEndOfData() {
        //
    }

    @Override
    public void atStartOfNewFile(JSFile file) {
        //
    }

}