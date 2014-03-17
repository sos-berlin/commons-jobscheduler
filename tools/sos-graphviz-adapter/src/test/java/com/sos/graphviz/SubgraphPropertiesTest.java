package com.sos.graphviz;

import com.sos.graphviz.enums.RankType;
import com.sos.graphviz.enums.SVGColor;
import com.sos.graphviz.enums.Style;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SubgraphPropertiesTest {

	String expectedDefault = "";
	
	String expectedChanged =
              "bgcolor = \"antiquewhite\"\n"
            + "color = \"red\"\n"
            + "id = \"id\"\n"
            + "label = \"test\"\n"
            +  "rank = \"sink\"\n"
			+ "style = \"solid\"\n";

	@Test
	public void testDefault() {
		SubgraphProperties p = new SubgraphProperties();
		String s = p.getSource();
		assertEquals(expectedDefault, s);
	}

	@Test
	public void testChanged() {
		SubgraphProperties p = new SubgraphProperties();
		p.setRank(RankType.sink);
		p.setBgcolor(SVGColor.antiquewhite);
		p.setColor(SVGColor.red);
		p.setLabel("test");
		p.setId("id");
		p.setStyle(Style.solid);
		String s = p.getSource();
		assertEquals(expectedChanged, s);
	}

}
