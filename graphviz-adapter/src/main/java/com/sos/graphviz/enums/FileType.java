package com.sos.graphviz.enums;

public enum FileType {
    gif,
    dot,
    fig,
    pdf,
    ps,
    svg,
    png,
    plain;
    
    public static FileType fromText(String fileType) throws Exception {
    	String searchFor = fileType.toLowerCase();
    	for(FileType f : FileType.values()) {
    		if (searchFor.equals(f.name())) return f;
    	}
    	throw new Exception("Filetype " + fileType + " is unknown for DOT.");
    }

    public String getExtension() {
        return "." + name().toLowerCase();
    }
}
