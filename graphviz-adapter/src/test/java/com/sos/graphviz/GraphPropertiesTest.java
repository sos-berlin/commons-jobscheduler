package com.sos.graphviz;

import com.sos.graphviz.enums.RankDir;
import com.sos.graphviz.enums.Splines;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GraphPropertiesTest {

	String expectedDefault = "graph [\n"
			+ "rankdir = \"LR\"\n"
			+ "]\n";

	String expectedChanged = "graph [\n"
            + "concentrate = \"true\"\n"
            + "compound = \"true\"\n"
			+ "fontname = \"Arial\"\n"
			+ "nodesep = \"5.0\"\n"
			+ "normalize = \"true\"\n"
			+ "id = \"id\"\n"
			+ "rankdir = \"BT\"\n"
			+ "sep = \"4.0\"\n"
			+ "splines = \"ortho\"\n"
			+ "]\n";

	@Test
	public void testDefault() {
		GraphProperties p = new GraphProperties();
		String s = p.getSource();
		assertEquals(expectedDefault,s);
	}

	@Test
	public void testChanged() {
		GraphProperties p = new GraphProperties();
        p.setConcentrate(true);
        p.setCompound(true);
		p.setDirection(RankDir.BT);
		p.setFontname("Arial");
		p.setNodesep(5);
		p.setId("id");
		p.setNormalize(true);
		p.setSep(4);
		p.setSplines(Splines.ortho);
		String s = p.getSource();
		assertEquals(expectedChanged,s);
	}

}
