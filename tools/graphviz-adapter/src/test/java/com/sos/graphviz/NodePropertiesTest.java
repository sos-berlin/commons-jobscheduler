package com.sos.graphviz;

import com.sos.graphviz.enums.SVGColor;
import com.sos.graphviz.enums.Shape;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NodePropertiesTest {

	String expectedDefault = "";

	String expectedChanged = "node [\n" + 
			"color = \"azure\"\n" +
			"fixedsize = \"true\"\n" +
			"fillcolor = \"blanchedalmond\"\n" +
			"group = \"myGroup\"\n" +
			"height = \"1.0\"\n" +
			"id = \"id\"\n" +
			"label = <test>\n" +
			"pos = \"100.0\"\n" +
			"shape = \"ellipse\"\n" + 
			"URL = \"http://myUrl.com\"\n" +
			"width = \"1.0\"\n" +
			"]\n";

	@Test
	public void testDefault() {
		NodeProperties p = new GlobalNodeProperties();
		String s = p.getSource();
		assertEquals(expectedDefault,s);
	}

	@Test
	public void testChanged() {
		NodeProperties p = new GlobalNodeProperties();
		p.setColor(SVGColor.azure);
		p.setFixedSize(true);
		p.setFillcolor(SVGColor.blanchedalmond);
		p.setGroup("myGroup");
		p.setHeight(1.0);
		p.setLabel("test");
		p.setId("id");
		p.setPos(100);
		p.setShape(Shape.ellipse);
		p.setUrl("http://myUrl.com");
		p.setWidth(1.0);
		String s = p.getSource();
		assertEquals(expectedChanged,s);
	}

}
