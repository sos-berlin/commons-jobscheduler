package com.sos.dialog.classes;
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class SOSTableItem extends TableItem {

    protected  Color [] colorsBackground;
    protected  Color [] colorsForeground;  
    private Table table;
    
    public SOSTableItem(Table parent, int style) {
        super(parent, style);
        table = parent;
    }
    
    
public void colorSave() {
    int columnCount = this.getParent().getColumns().length;
    colorsBackground = new Color[columnCount];
    colorsForeground = new Color[columnCount];
    
    for (int i=0;i<columnCount;i++) {
        Color background = this.getBackground(i);
        Color foreground = this.getForeground(i);
        colorsBackground[i] = background;
        colorsForeground[i] = foreground;
    }
}
    
public Color[] getBackgroundColumn() {
  return colorsBackground;
}

public Color[] getForegroundColumn() {
  return colorsForeground;
}

public Table  getTable () {
    return this.table;
}

}
