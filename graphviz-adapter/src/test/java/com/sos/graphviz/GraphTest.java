package com.sos.graphviz;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sos.graphviz.enums.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Class to demonstrate the usage of class Graph and its subsequent objects (subgraphs, nodes, edges).
 */
public class GraphTest {

    private final static String targetDirName = Resources.getResource("").getPath() + "/generated-svg/";
    private final static File targetDir = new File(targetDirName);


    @BeforeClass
    public static void setupBeforeClass() {
        createDirectory(targetDir);
    }

	@Test
	public void test() throws IOException, URISyntaxException {

		Graph g = new Graph();
		g.getGraphProperties().setDirection(RankDir.LR);
		g.getGraphProperties().setCompound(true);
        g.getGraphProperties().setSize("24,16");
        g.getGraphProperties().setRatio("auto");
		GlobalNodeProperties gn = g.getGlobalNodeProperties();
		gn.setFixedSize(true);
		gn.setHeight(0.4);
		gn.setWidth(0.75);


		Node n1 = g.newNode("START");
		n1.getSingleNodeProperties().setShape(Shape.diamond);
		
		Node n2 = g.newNode("B1");
		SingleNodeProperties p = n2.getSingleNodeProperties();
		p.setShape(Shape.circle);
		p.setLabel("");
		p.setHeight(0.2);
		p.setWidth(0.2);
		p.setUrl("http://www.sos-berlin.com");
		
		Node n3 = g.newNode("C");
		
		ClusterSubgraph c = createCluster(g, "dummy1", "C");
		c.getSubgraphProperties().setStyle(Style.solid);
		c.getSubgraphProperties().setBgcolor(SVGColor.cornsilk);
		g.newEdge( n3, new Node("C1") );

		c = createCluster(g, "dummy2", "D");
		c.getSubgraphProperties().setStyle(Style.dashed);
		g.newEdge( n3, new Node("D1") );
		
		ClusterSubgraph c1 = createCluster(c, "dummy3", "E");
		c1.getSubgraphProperties().setStyle(Style.dotted);
		c1.getSubgraphProperties().setBgcolor(SVGColor.chartreuse);
		Edge success = g.newEdge( g.newNode("D4"), g.newNode("E1") );
		success.getEdgeProperties().setLabel("success");
		
		c1 = createCluster(c, "dummy4", "F");
		c1.getSubgraphProperties().setStyle(Style.solid);
		c1.getSubgraphProperties().setBgcolor(SVGColor.gray);
		c1.getSubgraphProperties().setColor(SVGColor.red);
		Edge error = g.newEdge( g.newNode("D4"), g.newNode("F1") );
		error.getEdgeProperties().setHeadLabel("error");
		error.getEdgeProperties().setColor(SVGColor.red);
		
		Edge e = g.newEdge(n1, n2);
		e.getEdgeProperties().setLabel("start");
		e = g.newEdge(n2, n3);
		e.getEdgeProperties().setTailLabel("end");
		
		Node end = g.newNode("END");
		end.getSingleNodeProperties().setShape(Shape.diamond);
        Node c4 = g.newNode("C4");
        Node e4 = g.newNode("E4");
		g.newEdge( c4, end);
		g.newEdge( e4, end);
		g.newEdge( g.newNode("F4"), end);

        Edge e1 = g.newEdge(c4,e4);
        e1.getEdgeProperties().setLtail("cluster_dummy1");
        e1.getEdgeProperties().setLhead("cluster_dummy3");

        File tempDir = Files.createTempDir();
        GraphIO io = new GraphIO(g);
        io.setDotDir(tempDir.getAbsolutePath());
		io.writeGraphToFile(FileType.svg, new File(targetDir,"test.svg").getAbsolutePath() );
	}
	
	private ClusterSubgraph createCluster(Graph parent, String name, String base) {
		ClusterSubgraph s = parent.newClusterSubgraph(name);
		SubgraphProperties sp = s.getSubgraphProperties();
		if (parent instanceof ClusterSubgraph) sp.setLabel("cluster " + name);
		Node n1 = s.newNode(base + "1");
        n1.getSingleNodeProperties().setLabel("<b>test</b><br/><i>reihe2</i>");
		Node n2 = s.newNode(base + "2");
		Node n3 = s.newNode(base + "3");
		Node n4 = s.newNode(base + "4");
		s.newEdge( n1, n2 );
		s.newEdge( n2, n3 );
		s.newEdge( n3, n4 );
		return s;
	}

    private static void createDirectory(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdir())
                throw new RuntimeException("Error creating directory " + dir.getAbsolutePath());
        } else {
            if (!dir.isDirectory())
                throw new RuntimeException(dir.getAbsolutePath() + " already exists but is not a directory");
        }
    }

}
