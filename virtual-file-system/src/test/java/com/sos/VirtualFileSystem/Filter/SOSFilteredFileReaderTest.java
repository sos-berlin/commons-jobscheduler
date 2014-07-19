package com.sos.VirtualFileSystem.Filter;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
	@SuppressWarnings("unused")
	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	private final Logger		logger			= Logger.getLogger(this.getClass());

	@BeforeClass public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass public static void tearDownAfterClass() throws Exception {
	}

	@Before public void setUp() throws Exception {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		logger.setLevel(Level.DEBUG);
	}

	@After public void tearDown() throws Exception {
	}

	//	@Test
	public void testSetProcesshandler() {
		fail("Not yet implemented");
	}

	@Test public void testRun() {
		logger.info("start");
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
	SOSFilteredFileReader	objR	= null;

	@Test public void testRunMultipleFiles() {
		logger.info("start");
		JSFile objFile = new JSFile("R:/backup/projects/anubex/Events/");
		objR = new SOSFilteredFileReader();
		objR.setProcesshandler(this);
		SOSFilterOptions objFO = objR.Options();
		objFO.excludeLinesBefore.Value("^(\\$EVENT_START).*$");
		objFO.exclude_lines_after.Value("^\\$EVENT_END");
		objFO.excludeEmptyLines.value(true);
		objFO.excludeLines.Value("^(\\*.*)|^(JOB  ).*|(LIST ALL FOR).*$");
		objR.runMultipleFiles(objFile.getAbsolutePath());
		for (String strKey : mapXref.keySet()) {
			System.out.println(strKey + " --- " + mapXref.get(strKey));
		}
		String strHeader = "GROUP;EVENTNAME;DESCR;CSPD;STATUS;WHEN_PRIM;WHEN_QNAM;WHEN_OP;WHEN_TYPE;WHEN_QUAL;WHEN_CMRC;WHEN_MRC;ESUCC_EVENT;ESUCC_GROUP;ESUCC_MRC;ESUCC_CONDMRC;ESUCC_ABND;CMD;CSUCC_MRC;CSUCC_CMD;CSUCC_ABND;CSUCC_CONDMRC";
		JSFile objCSV = new JSFile("c:/temp/anubex.csv");
		String strLine = "";
		try {
			objCSV.WriteLine(strHeader);
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		for (String strKey : mapEvents.keySet()) {
			HashMap<String, String> objH = mapEvents.get(strKey);
			strLine = "";
			for (String strK : strHeader.split(";")) {
				String strL = objH.get(strK);
				if (strL != null) {
					strLine += strL;
				}
				strLine += ";";
			}
			try {
				objCSV.WriteLine(strLine);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			objCSV.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		createDiagramForSubTree("03P1I-TE");
	}

	private void createDiagramForSubTree(final String pstrSubTreeName) {
		Graph g = new Graph();
		g.getGraphProperties().setDirection(RankDir.TB);
		g.getGraphProperties().setCompound(true);
		g.getGraphProperties().setId(pstrSubTreeName);;
		//		g.getGraphProperties().setSize("24,16");
		//		g.getGraphProperties().setRatio("auto");
		GlobalNodeProperties gn = g.getGlobalNodeProperties();
		//		gn.setFixedSize(true);
		//		gn.setHeight(0.4);
		//		gn.setWidth(0.75);
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
//						p.setShape(Shape.circle);
						String strLabel = objH.get("DESCR");
						if (strLabel == null) {
							strLabel = "";
						}
						strLabel = "<" + strLabel + "<br/>" + strFromObject + ">";
						p.setLabel(strLabel);
						//						p.setHeight(0.2);
						//						p.setWidth(0.2);
						//						p.setUrl("http://www.sos-berlin.com");
						Node n2 = g.newNode(Quoted(strToObject));
						SingleNodeProperties p2 = n2.getSingleNodeProperties();
//						p2.setShape(Shape.circle);
//						p2.setLabel(strToObject);
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
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String Quoted(final String pstrV) {
		String strR = pstrV.trim();
		if (strR != null) {
			strR = "\"" + strR + "\"";
		}
		else {
			strR = " ";
		}
		return strR;
	}
	JSFile										objOutput		= new JSFile("c:/temp/anubex.txt");
	String										strLastTooken	= "";
	String										strLastRecord	= "";
	HashMap<String, Integer>					mapXref			= new HashMap<String, Integer>();
	HashMap<String, HashMap<String, String>>	mapEvents		= new HashMap();
	HashMap<String, String>						mapEvent		= new HashMap();

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

	@Override public void processRecord(final String pstrRecord) {
		//		System.out.println(pstrRecord);
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
							System.out.println("duplicate Event: " + strKey + ", FileName = " + objR.getCurrentFile().getName());
							strKey += "-dup";
						}
						mapEvents.put(strKey, mapEvent);
						mapEvent = new HashMap<String, String>();
					}
				}
			}
			else {
				if (pstrRecord.trim().endsWith(":")) {
					//
				}
				else {
					String strTooken = getTooken(pstrRecord);
					addTooken(strTooken);
					addEventAttribut(strTooken, getValue(pstrRecord));
					if (strLastTooken.equals(strTooken) == false) {
						if (strLastRecord.trim().length() > 0) {
							objOutput.WriteLine(strLastRecord);
						}
						strLastRecord = pstrRecord;
						strLastTooken = strTooken;
					}
					else {
						String[] strA = pstrRecord.split(":");
						if (strA.length > 1) {
							strLastRecord += "," + strA[1].trim();
						}
					}
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addEventAttribut(final String pstrTooken, final String pstrValue) {
		String strT = mapEvent.get(pstrTooken);
		if (strT == null) {
			mapEvent.put(pstrTooken, pstrValue);
		}
		else {
			strT = strT.trim();
			strT += "," + pstrValue;
			mapEvent.put(pstrTooken, strT);
		}
	}

	@Override public void atStartOfData() {
	}

	@Override public void atEndOfData() {
	}
}
