package com.sos.graphviz;

import com.sos.graphviz.enums.ArrowType;
import com.sos.graphviz.enums.DirType;
import com.sos.graphviz.enums.PortPos;
import com.sos.graphviz.enums.SVGColor;
import com.sos.graphviz.properties.GraphvizEnumProperty;
import com.sos.graphviz.properties.GraphvizProperty;

/**
 * The properties of an edge.
 *
 * <p>
 *     The properties follows the valid attributes for Graphviz (@see <a href="http://www.graphviz.org/doc/info/attrs.html">http://www.graphviz.org/doc/info/attrs.html</a>)
 * </p>
 *
 * <p>To add an additinal attribute to this class:</p>
 * <ul>
 *     <li>Define a private class property.</li>
 *     <li>Expand the getContent() method for this property.</li>
 *     <li>Generate a getter an a setter method for this attribute.</li>
 * </ul>
 */
public class EdgeProperties extends GraphvizObject implements IGraphvizObject {

	private static final String constEpilog = "]";
	private static final String constProlog = "[";

	private GraphvizProperty arrowHead = new GraphvizEnumProperty("arrowhead");
	private GraphvizProperty arrowSize = new GraphvizProperty("arrowsize");
	private GraphvizProperty arrowTail = new GraphvizEnumProperty("arrowtail");
	private GraphvizProperty color = new GraphvizProperty("color");
	private GraphvizProperty comment = new GraphvizProperty("comment");
	private GraphvizProperty constraint = new GraphvizProperty("constraint");
	private GraphvizProperty decorate = new GraphvizProperty("decorate");
	private GraphvizProperty dir = new GraphvizEnumProperty("dir");
	private GraphvizProperty fontColor = new GraphvizEnumProperty("fontcolor");
	private GraphvizProperty fontName = new GraphvizProperty("fontname");
	private GraphvizProperty fontSize = new GraphvizProperty("fontsize");
	private GraphvizProperty headURL = new GraphvizProperty("headURL");
	private GraphvizProperty headClip = new GraphvizProperty("headclip");
	private GraphvizProperty headPort = new GraphvizProperty("headport");
	//TODO implements missing attributes followed after 'headport' - see http://www.graphviz.org/pub/scm/graphviz2/doc/info/attrs.html
	private GraphvizProperty headLabel = new GraphvizProperty("headlabel");
	private GraphvizProperty id = new GraphvizProperty("id");
	private GraphvizProperty label = new GraphvizProperty("label");
	private GraphvizProperty lhead = new GraphvizProperty("lhead");
	private GraphvizProperty ltail = new GraphvizProperty("ltail");
	private GraphvizProperty samehead = new GraphvizProperty("samehead");
	private GraphvizProperty sametail = new GraphvizProperty("sametail");
	private GraphvizProperty tailLabel = new GraphvizProperty("taillabel");
	private GraphvizProperty tailPort = new GraphvizProperty("tailport");
	private GraphvizProperty url = new GraphvizProperty("URL");
	private GraphvizProperty weight = new GraphvizProperty("weight");
	
	public EdgeProperties() {
		super(constProlog, constEpilog);
	}

	@Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		sb.append( arrowSize.getContent() );
		sb.append( arrowHead.getContent() );
		sb.append( arrowTail.getContent() );
		sb.append( color.getContent() );
		sb.append( comment.getContent() );
		sb.append( constraint.getContent() );
		sb.append( decorate.getContent() );
		sb.append( dir.getContent() );
		sb.append( fontColor.getContent() );
		sb.append( fontName.getContent() );
		sb.append( fontSize.getContent() );
		sb.append( headClip.getContent() );
		sb.append( headLabel.getContent() );
		sb.append( headPort.getContent() );
		sb.append( headURL.getContent() );
		sb.append( id.getContent() );
		sb.append( label.getContent() );
		sb.append( lhead.getContent() );
		sb.append( ltail.getContent() );
		sb.append( samehead.getContent() );
		sb.append( sametail.getContent() );
		sb.append( tailLabel.getContent() );
		sb.append( tailPort.getContent() );
		sb.append( url.getContent() );
		sb.append( weight.getContent() );
		return sb.toString();
	}

	public GraphvizObject getProperties() {
		return this;
	}
	
	public ArrowType getArrowHead() {
		return (ArrowType)arrowHead.getValue();
	}

	public double getArrowSize() {
		return (Double)arrowSize.getValue();
	}

	public ArrowType getArrowTail() {
		return (ArrowType)arrowTail.getValue();
	}

	public SVGColor getColor() {
		return (SVGColor)color.getValue();
	}

	public String getComment() {
		return (String)comment.getValue();
	}

	public boolean getConstraint() {
		return (Boolean)constraint.getValue();
	}

	public boolean getDecorate() {
		return (Boolean)decorate.getValue();
	}

	public DirType getDir() {
		return (DirType)dir.getValue();
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

	public String getHeadLabel() {
		return (String)headLabel.getValue();
	}

	public String getLabel() {
		return (String)label.getValue();
	}

	public String getTailLabel() {
		return (String)tailLabel.getValue();
	}

	public String getUrl() {
		return (String)url.getValue();
	}

	public void setArrowHead(ArrowType arrowHead) {
		this.arrowHead.setValue(arrowHead);
	}

	public void setArrowSize(double arrowSize) {
		this.arrowSize.setValue(arrowSize);
	}

	public void setArrowTail(ArrowType arrowTail) {
		this.arrowTail.setValue(arrowTail);
	}

	public void setColor(SVGColor color) {
		this.color.setValue(color);
	}

	public void setComment(String comment) {
		this.comment.setValue(comment);
	}

	public void setConstraint(boolean constraint) {
		this.constraint.setValue(constraint);
	}

	public void setDecorate(boolean decorate) {
		this.decorate.setValue(decorate);
	}

	public void setDir(DirType dir) {
		this.dir.setValue(dir);
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

	public void setHeadLabel(String headLabel) {
		this.headLabel.setValue(headLabel);
	}

	public void setLabel(String label) {
		this.label.setValue(label);
	}

	public void setTailLabel(String tailLabel) {
		this.tailLabel.setValue(tailLabel);
	}

	public void setUrl(String url) {
		this.url.setValue(url);
	}

	public String getHeadURL() {
		return (String)headURL.getValue();
	}

	public boolean getHeadClip() {
		return (Boolean)headClip.getValue();
	}

	public PortPos getHeadPort() {
		return (PortPos)headPort.getValue();
	}

	public PortPos getTailPort() {
		return (PortPos)tailPort.getValue();
	}

	public void setHeadURL(String headURL) {
		this.headURL.setValue(headURL);
	}

	public void setHeadClip(boolean headClip) {
		this.headClip.setValue(headClip);
	}

	public void setHeadPort(PortPos headPort) {
		this.headPort.setValue(headPort);
	}

	public void setTailPort(PortPos tailPort) {
		this.tailPort.setValue(tailPort);
	}

	public double getWeight() {
		return (Double)weight.getValue();
	}

	public void setWeight(double weight) {
		this.weight.setValue(weight);
	}

	public String getSamehead() {
		return (String)samehead.getValue();
	}

	public void setSamehead(String samehead) {
		this.samehead.setValue(samehead);
	}

	public String getSametail() {
		return (String)sametail.getValue();
	}

	public void setSametail(String sametail) {
		this.sametail.setValue(sametail);
	}

	public String getLhead() {
		return (String)lhead.getValue();
	}

	public void setLhead(String lhead) {
		this.lhead.setValue(lhead);
	}

	public String getLtail() {
		return (String)ltail.getValue();
	}

	public void setLtail(String ltail) {
		this.ltail.setValue(ltail);
	}

    public String getId() {
        return (String)id.getValue();
    }

    public void setId(String id) {
        this.id.setValue(id);
    }

}
