package com.sos.graphviz;

import com.sos.graphviz.enums.SVGColor;
import com.sos.graphviz.enums.Shape;
import com.sos.graphviz.properties.GraphvizEnumProperty;
import com.sos.graphviz.properties.GraphvizHtmlProperty;
import com.sos.graphviz.properties.GraphvizProperty;

abstract class NodeProperties extends GraphvizObject implements IGraphvizObject {

	
	private GraphvizProperty fontcolor = new GraphvizEnumProperty("fontcolor");
	private GraphvizProperty color = new GraphvizEnumProperty("color");
	private GraphvizProperty fillcolor = new GraphvizEnumProperty("fillcolor");
	private GraphvizProperty fixedSize = new GraphvizProperty("fixedsize");
	private GraphvizProperty group = new GraphvizProperty("group");
	private GraphvizProperty height = new GraphvizProperty("height");
	private GraphvizProperty id = new GraphvizProperty("id");
	private GraphvizProperty label = new GraphvizHtmlProperty("label");
	private GraphvizProperty pos = new GraphvizProperty("pos");
	private GraphvizProperty shape = new GraphvizEnumProperty("shape");
    private GraphvizProperty style = new GraphvizProperty("style");
    private GraphvizProperty tooltip = new GraphvizProperty("tooltip");
	private GraphvizProperty url = new GraphvizProperty("URL");
	private GraphvizProperty width = new GraphvizProperty("width");
	
	private static final String constProlog = " [";
	private static final String constEpilog = "]";
	
	public NodeProperties(String prefix, Shape shape) {
		super(prefix + " " + constProlog, constEpilog);
		this.shape.setValue(shape);
	}
	
	public NodeProperties(String prefix) {
		super( (prefix + " ").trim() + constProlog, constEpilog);
	}
	
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		sb.append( color.getContent() );
		sb.append( fixedSize.getContent() );
		sb.append( fillcolor.getContent() );
		sb.append( fontcolor.getContent() );
		sb.append( group.getContent() );
		sb.append( height.getContent() );
		sb.append( id.getContent() );
		sb.append( label.getContent() );
		sb.append( pos.getContent() );
        sb.append( shape.getContent() );
        sb.append( style.getContent() );
        sb.append( tooltip.getContent() );
		sb.append( url.getContent() );
		sb.append( width.getContent() );
		return sb.toString();
	}

	public GraphvizObject getProperties() {
		return this;
	}

	public void setShape(Shape shape) {
		this.shape.setValue(shape);
	}

	public void setWidth(double width) {
		this.width.setValue(width);
	}

	public void setLabel(String label) {
		this.label.setValue(label);
	}

	public Shape getShape() {
		return (Shape)shape.getValue();
	}

	public double getWidth() {
		return (Double)width.getValue();
	}

	public String getLabel() {
		return (String)label.getValue();
	}

	public double getHeight() {
		return (Double)height.getValue();
	}

	public void setHeight(double height) {
		this.height.setValue(height);
	}

	public boolean isFixedSize() {
		return (Boolean)fixedSize.getValue();
	}

	public void setFixedSize(boolean fixedSize) {
		this.fixedSize.setValue(fixedSize);
	}

	public String getUrl() {
		return (String)url.getValue();
	}

	public void setUrl(String url) {
		this.url.setValue(url);
	}

	public SVGColor getColor() {
		return (SVGColor)color.getValue();
	}

	public void setColor(SVGColor color) {
		this.color.setValue(color);
	}

	public SVGColor getFillcolor() {
		return (SVGColor)fillcolor.getValue();
	}

	public void setFillcolor(SVGColor fillcolor) {
		this.fillcolor.setValue(fillcolor);
	}

	public SVGColor getFontcolor() {
		return (SVGColor)fontcolor.getValue();
	}

	public void setFontcolor(SVGColor fontcolor) {
		this.fontcolor.setValue(fontcolor);
	}

	public double getPos() {
		return (Double)pos.getValue();
	}

	public void setPos(double pos) {
		this.pos.setValue(pos);
	}

	public String getGroup() {
		return (String)url.getValue();
	}

	public void setGroup(String group) {
		this.group.setValue(group);
	}

    public String getStyle() {
        return (String)style.getValue();
    }

    public void setStyle(String style) {
        this.style.setValue(style);
    }

    public String getTooltip() {
        return (String)tooltip.getValue();
    }

    public void setTooltip(String tooltip) {
        this.tooltip.setValue(tooltip);
    }

    public String getId() {
        return (String)id.getValue();
    }

    public void setId(String id) {
        this.id.setValue(id);
    }
}
