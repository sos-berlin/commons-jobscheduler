package com.sos.dialog.classes;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import sos.util.SOSExcelFileWriter;

import com.sos.dialog.components.SOSTableColumn;
import com.sos.dialog.interfaces.ISOSTable;

public abstract class SOSTable extends Table implements ISOSTable {

    private static final String EXCEL_FILE_EXTENSION = ".xls";
    private TableColumn lastColumn;
    private WindowsSaver formPosSizeHandler = null;
    private String msg;
    private boolean rightMouseclick;
    public static final int RIGHT_MOUSE_BUTTON = 3;

    Listener ColumnMoveListener = new Listener() {

        @Override
        public void handleEvent(final Event e) {
            formPosSizeHandler.tableColumnOrderSave(getThis());
        }
    };

    public SOSTable(final Composite parent, final int style) {
        super(parent, style);
        formPosSizeHandler = new WindowsSaver(this.getClass(), getShell(), 643, 600);
        this.setData("caption", this.getClass());
        addListeners(parent);
    }

    @Override
    abstract public void createTable();

    @Override
    public SOSTableColumn getSOSTableColumn(final int index) {
        return (SOSTableColumn) this.getColumn(index);
    }

    private void addListeners(Composite parent) {
        addResizeListener(parent);
        addMouselistener();
    }

    private void addMouselistener() {
        this.addListener(SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                if (event.button == RIGHT_MOUSE_BUTTON) {
                    setRightMausclick(true);
                } else {
                    setRightMausclick(false);
                }
            }
        });
    }

    private SOSTable getThis() {
        return this;
    }

    public boolean isRightMouseclick() {
        return rightMouseclick;
    }

    public void setRightMausclick(boolean b) {
        rightMouseclick = b;
    }

    public void setRightMausclick(Event event) {
        setRightMausclick((event.button == RIGHT_MOUSE_BUTTON));
    }

    public void setMoveableColums(final boolean moveable) {
        TableColumn[] columns = this.getColumns();
        for (TableColumn column : columns) {
            column.setMoveable(moveable);
            column.addListener(SWT.Move, ColumnMoveListener);
        }
        formPosSizeHandler.tableColumnOrderRestore(this);
    }

    private int calculateColumnWidth() {
        TableColumn[] columns = this.getColumns();
        lastColumn = columns[this.getColumnOrder()[columns.length - 1]];
        int columnWidth = 0;
        for (int i = 0; i < columns.length; i++) {
            columnWidth = columnWidth + columns[i].getWidth();
        }
        columnWidth = columnWidth - lastColumn.getWidth();
        return columnWidth;
    }

    private void addResizeListener(final Composite composite) {
        composite.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(final ControlEvent e) {
                Rectangle area = composite.getClientArea();
                Point size = computeSize(SWT.DEFAULT, SWT.DEFAULT);
                int colWidth = 0;
                colWidth = calculateColumnWidth();
                ScrollBar vBar = getVerticalBar();
                int width = area.width - computeTrim(0, 0, 0, 0).width;
                if (size.y > area.height + getHeaderHeight()) {
                    Point vBarSize = vBar.getSize();
                    if (vBar.isVisible()) {
                        width -= vBarSize.x;
                    }
                }
                Point oldSize = getSize();
                if (oldSize.x > area.width) {
                    lastColumn.setWidth(width - colWidth);
                    setSize(area.width, area.height);
                } else {
                    setSize(area.width, area.height);
                    lastColumn.setWidth(width - colWidth);
                }
            }
        });
    }

    private File getExcelFile() {
        FileDialog dlg = new FileDialog(this.getShell(), SWT.SAVE);
        String[] extensions = { EXCEL_FILE_EXTENSION };
        dlg.setFilterExtensions(extensions);
        String filename = dlg.open();
        if (filename != null) {
            return new File(filename);
        } else {
            return null;
        }
    }

    public void createExcelFile() {
        SOSExcelFileWriter sosExcelFileWriter = new SOSExcelFileWriter();
        String[] header = new String[getColumnCount()];
        TableColumn[] columns = this.getColumns();
        for (int i = 0; i < columns.length; i++) {
            header[i] = columns[i].getText();
        }
        sosExcelFileWriter.addHeader(header);
        for (int i = 0; i < getItemCount(); i++) {
            String[] row = new String[getColumnCount()];
            TableItem t = getItems()[i];
            for (int ii = 0; ii < getColumnCount(); ii++) {
                row[ii] = t.getText(ii);
            }
            sosExcelFileWriter.addRow(row);
        }
        File f = getExcelFile();
        if (f != null) {
            sosExcelFileWriter.createFile(f);
            msg = String.format("File saved in:%s", f.getName());
        } else {
            msg = "File not saved";
        }
    }

    public String getMsg() {
        return msg;
    }

}