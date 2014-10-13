package com.sos.dialog.classes;

import java.io.File;

import org.apache.log4j.Logger;
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

public abstract class SOSTable extends Table implements ISOSTable{

	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(this.getClass());

    private final int                columnWidth=0;
    private TableColumn        lastColumn;
    private WindowsSaver       formPosSizeHandler   = null;
    private String             msg;

    Listener    ColumnMoveListener  = new Listener() {
        @Override
        public void handleEvent(final Event e) {
            formPosSizeHandler.TableColumnOrderSave(getThis());
        }


    };


    public SOSTable(final Composite parent, final int style) {
        super(parent, style);
        formPosSizeHandler = new WindowsSaver(this.getClass(), getShell(), 643, 600);
        this.setData("caption",this.getClass());

        addResizeListener(parent);
     }

    @Override
	abstract public void createTable();
    @Override
    public SOSTableColumn getSOSTableColumn(final int index) {
        return (SOSTableColumn)this.getColumn(index);
    }

    private SOSTable getThis() {
        return this;
    }


    public void setMoveableColums(final boolean moveable) {
       TableColumn[] columns = this.getColumns();

         for (TableColumn column : columns) {
             column.setMoveable(moveable);
             column.addListener(SWT.Move, ColumnMoveListener);
         }
         formPosSizeHandler.TableColumnOrderRestore(this);
    }




    private void calculateColumnWidth() {

        TableColumn[] columns = this.getColumns();
        lastColumn = columns[this.getColumnOrder ()[columns.length-1]];

        int columnWidth = 0;
        for (int i = 0; i < columns.length-1-1; i++) {
            columnWidth = columnWidth + columns[i].getWidth();
        }


    }

    private void addResizeListener(final Composite composite) {
        composite.addControlListener(new ControlAdapter() {
            @Override
			public void controlResized(final ControlEvent e) {
                Rectangle area = composite.getClientArea();
                Point size = computeSize(SWT.DEFAULT, SWT.DEFAULT);
                ScrollBar vBar = getVerticalBar();
                int width = area.width - computeTrim(0, 0, 0, 0).width + vBar.getSize().x;
                if (size.y > area.height + getHeaderHeight()) {
                    Point vBarSize = vBar.getSize();
                    if (vBar.isVisible()) {
                        width -= vBarSize.x;
                    }
                }
                Point oldSize = getSize();
                calculateColumnWidth();

                if (oldSize.x > area.width) {
                    lastColumn.setWidth(width - columnWidth);
                    setSize(area.width, area.height);
                }
                else {
                    setSize(area.width, area.height);
                    lastColumn.setWidth(width - columnWidth);
                }
            }
        });
    }
    
    private File getExcelFile() {
    
        FileDialog dlg = new FileDialog(this.getShell(),SWT.SAVE);
        String filename = dlg.open();
        if (filename != null) {
            return new File(filename);
        }else {
           return null;
}
    }
    
    public void createExcelFile() {
        SOSExcelFileWriter sosExcelFileWriter = new SOSExcelFileWriter();

        String[] header = new String[getColumnCount()];
        TableColumn[] columns = this.getColumns();
        for (int i = 0; i < columns.length; i++) {
            header[i]  = columns[i].getText();
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
          msg = String.format("File saved in:%s",f.getName());
        }else {
            msg = "File not saved";
        }
     }

    public String getMsg() {
        return msg;
    }
}
