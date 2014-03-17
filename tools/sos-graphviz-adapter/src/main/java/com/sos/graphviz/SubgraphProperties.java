package com.sos.graphviz;

import com.sos.graphviz.enums.*;
import com.sos.graphviz.properties.GraphvizEnumProperty;
import com.sos.graphviz.properties.GraphvizProperty;

public class SubgraphProperties extends GraphvizObject implements IGraphvizObject {

    private static final String constProlog = "";
	private static final String constEpilog = "";

    private GraphvizProperty fontColor = new GraphvizEnumProperty("fontcolor");
    private GraphvizProperty fontName = new GraphvizProperty("fontname");
    private GraphvizProperty fontSize = new GraphvizProperty("fontsize");
    private GraphvizProperty id = new GraphvizProperty("id");
	private GraphvizProperty rank = new GraphvizEnumProperty("rank");
	private GraphvizProperty style = new GraphvizEnumProperty("style");
    private GraphvizProperty label = new GraphvizProperty("label");
    private GraphvizProperty labeljust = new GraphvizEnumProperty("labeljust");
    private GraphvizProperty ordering = new GraphvizEnumProperty("ordering");
	private GraphvizProperty bgcolor = new GraphvizEnumProperty("bgcolor");
	private GraphvizProperty color = new GraphvizEnumProperty("color");

	public SubgraphProperties(RankType rankType) {
		super(constProlog, constEpilog);
		this.rank.setValue(rankType);
	}

	public SubgraphProperties() {
		super(constProlog, constEpilog);
	}

    @Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
        sb.append( bgcolor.getContent() );
        sb.append( color.getContent() );
        sb.append( fontColor.getContent() );
        sb.append( fontName.getContent() );
        sb.append( fontSize.getContent() );
        sb.append( id.getContent() );
		sb.append( label.getContent() );
		sb.append( labeljust.getContent() );
        sb.append(rank.getContent());
        sb.append( style.getContent() );
		return sb.toString();
	}

	public Style getStyle() {
		return (Style)style.getValue();
	}

	public void setStyle(Style style) {
		this.style.setValue(style);
	}

	public GraphvizObject getProperties() {
		return this;
	}

	public String getLabel() {
		return (String)label.getValue();
	}

	public void setLabel(String label) {
		this.label.setValue(label);
	}

	public SVGColor getBgcolor() {
		return (SVGColor)bgcolor.getValue();
	}

	public void setBgcolor(SVGColor bgcolor) {
		this.bgcolor.setValue(bgcolor);
	}

	public SVGColor getColor() {
		return (SVGColor)color.getValue();
	}

	public void setColor(SVGColor color) {
		this.color.setValue(color);
	}

	public void setRank(RankType rankType) {
		this.rank.setValue(rankType);
	}

	public RankType getRank() {
		return (RankType)this.rank.getValue();
	}

    public Justify getLabeljust() {
        return (Justify)labeljust.getValue();
    }

    public void setLabeljust(Justify labeljust) {
        this.labeljust.setValue(labeljust);
    }

    public Ordering getOrdering() {
        return (Ordering)ordering.getValue();
    }

    public void setOrdering(Ordering ordering) {
        this.ordering.setValue(ordering);
    }

    public SVGColor getFontColor() {
        return (SVGColor)fontColor.getValue();
    }

    public String getFontName() {
        return (String)fontName.getValue();
    }

    public double getFontSize() {
        return (Double)fontSize.getValue();
    }

    public void setFontColor(SVGColor fontColor) {
        this.fontColor.setValue(fontColor);
    }

    public void setFontName(String fontName) {
        this.fontName.setValue(fontName);
    }

    public void setFontSize(double fontSize) {
        this.fontSize.setValue(fontSize);
    }

    public String getId() {
        return (String)id.getValue();
    }

    public void setId(String id) {
        this.id.setValue(id);
    }

}
