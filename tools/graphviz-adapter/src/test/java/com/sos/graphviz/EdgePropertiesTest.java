package com.sos.graphviz;

import com.sos.graphviz.enums.ArrowType;
import com.sos.graphviz.enums.DirType;
import com.sos.graphviz.enums.PortPos;
import com.sos.graphviz.enums.SVGColor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EdgePropertiesTest {

	String expectedDefault = "";

	String expectedChanged = "[\n" +
			"arrowsize = \"2.0\"\n" +
			"arrowhead = \"crow\"\n" +
			"arrowtail = \"dot\"\n" +
			"color = \"bisque\"\n" +
			"comment = \"comment\"\n" +
			"constraint = \"true\"\n" +
			"decorate = \"true\"\n" +
			"dir = \"forward\"\n" +
			"fontcolor = \"darkgray\"\n" +
			"fontname = \"Arial\"\n" +
			"fontsize = \"12.0\"\n" +
			"headclip = \"true\"\n" + 
			"headlabel = \"head\"\n" +
			"headport = \"nw\"\n" + 
			"headURL = \"http://myurl.com\"\n" + 
			"id = \"id\"\n" +
			"label = \"test\"\n" +
			"lhead = \"node1\"\n" + 
			"ltail = \"node2\"\n" + 
			"samehead = \"samehead\"\n" + 
			"sametail = \"sametail\"\n" +
			"taillabel = \"tail\"\n" +
			"tailport = \"e\"\n" + 
			"URL = \"http://myurl.com\"\n" +
			"weight = \"2.0\"\n" + 
			"]\n";

	@Test
	public void testDefault() {
		EdgeProperties p = new EdgeProperties();
		String s = p.getSource();
		assertEquals(expectedDefault,s);
	}

	@Test
	public void testChanged() {
		EdgeProperties p = new EdgeProperties();
		p.setArrowHead(ArrowType.crow);
		p.setArrowSize(2);
		p.setArrowTail(ArrowType.dot);
		p.setColor(SVGColor.bisque);
		p.setComment("comment");
		p.setConstraint(true);
		p.setDecorate(true);
		p.setDir(DirType.forward);
		p.setFontColor(SVGColor.darkgray);
		p.setFontName("Arial");
		p.setFontSize(12);
		p.setHeadClip(true);
		p.setHeadLabel("head");
		p.setHeadPort(PortPos.nw);
		p.setHeadURL("http://myurl.com");
		p.setId("id");
		p.setLabel("test");
		p.setLhead("node1");
		p.setLtail("node2");
		p.setUrl("http://myurl.com");
		p.setTailLabel("tail");
		p.setTailPort(PortPos.e);
		p.setSamehead("samehead");
		p.setSametail("sametail");
		p.setWeight(2);
		String s = p.getSource();
		assertEquals(expectedChanged,s);
	}

}
