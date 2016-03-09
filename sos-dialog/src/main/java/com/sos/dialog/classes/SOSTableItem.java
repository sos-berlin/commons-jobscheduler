package com.sos.dialog.classes;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.sos.hibernate.classes.UtcTimeHelper;

public class SOSTableItem extends TableItem {

    protected Color[] colorsBackground;
    protected Color[] colorsForeground;
    private Table table;
    private String timeZone = "";
    protected UtcTimeHelper utcTimeHelper;

    public SOSTableItem(Table parent, int style) {
        super(parent, style);
        utcTimeHelper = new UtcTimeHelper();
        table = parent;
    }

    public void colorSave() {
        int columnCount = this.getParent().getColumns().length;
        colorsBackground = new Color[columnCount];
        colorsForeground = new Color[columnCount];

        for (int i = 0; i < columnCount; i++) {
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

    public Table getTable() {
        return this.table;
    }

    public String getTimeZone() {
        if (timeZone.length() == 0) {
            this.timeZone = UtcTimeHelper.localTimeZoneString();
        }
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

}
