package com.sos.graphviz;

import com.sos.graphviz.enums.*;
import com.sos.graphviz.properties.GraphvizEnumProperty;
import com.sos.graphviz.properties.GraphvizProperty;

public class GraphProperties extends GraphvizObject implements IGraphvizObject {

	private static final String constProlog = "graph [";
	private static final String constEpilog = "]";
	private static final RankDir defaultDirection = RankDir.LR;

   private GraphvizProperty concentrate = new GraphvizProperty("concentrate");
   private GraphvizProperty compound = new GraphvizProperty("compound");
   private GraphvizProperty rankdir = new GraphvizEnumProperty("rankdir");
	private GraphvizProperty fontname = new GraphvizProperty("fontname");
	private GraphvizProperty nodesep = new GraphvizProperty("nodesep");
   private GraphvizProperty normalize = new GraphvizProperty("normalize");
   private GraphvizProperty ordering = new GraphvizEnumProperty("ordering");
   private GraphvizProperty id = new GraphvizProperty("id");
   private GraphvizProperty ranksep = new GraphvizProperty("ranksep");
   private GraphvizProperty sep = new GraphvizProperty("sep");
   private GraphvizProperty smoothing = new GraphvizEnumProperty("smoothing");
   private GraphvizProperty packmode = new GraphvizEnumProperty("packmode");
   private GraphvizProperty splines = new GraphvizEnumProperty("splines");
   private GraphvizProperty size = new GraphvizProperty("size");
   private GraphvizProperty ratio = new GraphvizProperty("ratio");

	public GraphProperties(RankDir direction) {
		super(constProlog, constEpilog);
		setDirection(direction);
	}

	public GraphProperties() {
		super(constProlog, constEpilog);
		setDirection(defaultDirection);
	}

    @Override
	public String getContent() {
		StringBuilder sb = new StringBuilder();
      sb.append( concentrate.getContent());
      sb.append( compound.getContent());
		sb.append( fontname.getContent());

		sb.append( nodesep.getContent());
		sb.append( normalize.getContent());
		sb.append( id.getContent());
		sb.append( packmode.getContent());
		sb.append( rankdir.getContent());
		sb.append( ranksep.getContent());
		sb.append( sep.getContent());
       sb.append( splines.getContent());
       sb.append( size.getContent());
       sb.append( ratio.getContent());
		return sb.toString();
	}

	public GraphvizObject getProperties() {
		return this;
	}

	public RankDir getDirection() {
		return (RankDir)rankdir.getValue();
	}

	public void setDirection(RankDir direction) {
		this.rankdir.setValue(direction);
	}

	public boolean isConcentrate() {
		return (Boolean)concentrate.getValue();
	}

	public void setConcentrate(boolean concentrate) {
		this.concentrate.setValue(concentrate);
	}

	public Splines getSplines() {
		return (Splines)splines.getValue();
	}

	public void setSplines(Splines splines) {
		this.splines.setValue(splines);
	}

	public double getNodesep() {
		return (Double)nodesep.getValue();
	}

	public void setNodesep(double nodesep) {
		this.nodesep.setValue(nodesep);
	}

	public boolean isNormalize() {
		return (Boolean)normalize.getValue();
	}

	public void setNormalize(boolean normalize) {
		this.normalize.setValue(normalize);
	}

	public double getSep() {
		return (Double)sep.getValue();
	}

	public void setSep(double sep) {
		this.sep.setValue(sep);
	}

	public String getFontname() {
		return (String)fontname.getValue();
	}

	public void setFontname(String fontname) {
		this.fontname.setValue(fontname);
	}

    public boolean getCompound() {
        return (Boolean)compound.getValue();
    }

    public void setCompound(boolean compound) {
        this.compound.setValue(compound);
    }

    public PackMode getPackmode() {
        return (PackMode)packmode.getValue();
    }

    public void setPackmode(PackMode packmode) {
        this.packmode.setValue(packmode);
    }

    public Ordering getOrdering() {
        return (Ordering)ordering.getValue();
    }

    public void setOrdering(Ordering ordering) {
        this.ordering.setValue(ordering);
    }

    public String getRanksep() {
        return (String)ranksep.getValue();
    }

    public void setRanksep(String ranksep) {
        this.ranksep.setValue(ranksep);
    }

    public SmoothType getSmoothing() {
        return (SmoothType)smoothing.getValue();
    }

    public void setSmoothing(SmoothType smoothing) {
        this.smoothing.setValue(smoothing);
    }

    public String getId() {
        return (String)id.getValue();
    }

    public void setId(String id) {
        this.id.setValue(id);
    }

    public String getSize() {
        return (String)size.getValue();
    }

    public void setSize(String size) {
        this.size.setValue(size);
    }

    public String getRatio() {
        return (String)ratio.getValue();
    }

    public void setRatio(String ratio) {
        this.ratio.setValue(ratio);
    }

}
