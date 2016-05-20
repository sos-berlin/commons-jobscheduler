package com.sos.dialog.classes;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.sos.dialog.components.SOSPreferenceStore;

public class WindowsSaver extends SOSPreferenceStore {

    private static final String WIN_LOCATE_Y = "win:locateY:";
    private static final String WIN_LOCATE_X = "win:locateX:";
    private static final String WIN_SIZE_Y = "win:sizeY:";
    private static final String WIN_SIZE_X = "win:sizeX:";
    private static final Logger LOGGER = Logger.getLogger(WindowsSaver.class);
    private static final String WIDTH = "width";
    private final Point defaultSize;
    private final Point defaultLocation;
    private boolean flgClassIsActive = false;

    public WindowsSaver(final Class<?> c, final Shell s, final int x, final int y) {
        super(c);
        shell = s;
        strKey = className;
        defaultSize = new Point(x, y);
        defaultLocation = new Point(100, 100);
        if (shell != null) {
            shell.addDisposeListener(new DisposeListener() {

                @Override
                public void widgetDisposed(final DisposeEvent arg0) {
                    LOGGER.trace("disposed");
                    saveWindowPosAndSize();
                }
            });
            shell.addControlListener(new ControlAdapter() {

                @Override
                public void controlMoved(final ControlEvent e) {
                    if (!flgClassIsActive) {
                        //
                    }
                }

                @Override
                public void controlResized(final ControlEvent e) {
                    if (!flgClassIsActive) {
                        saveWindowPosAndSize();
                    }
                }
            });
        }
    }

    @Override
    public void setKey(final String pstrKey) {
        super.setKey(pstrKey);
        restoreWindow();
    }

    public WindowsSaver restoreWindow() {
        flgClassIsActive = true;
        restoreWindowSize();
        restoreWindowLocation();
        flgClassIsActive = false;
        return this;
    }

    public void saveSash(final SashForm objSashForm) {
        this.saveSash(objSashForm.getWeights());
    }

    public void saveSash(final int[] intWeights) {
        saveProperty("sash.layout", intWeights[0] + "," + intWeights[1]);
        LOGGER.debug("Save Sash");
    }

    public void loadSash(final SashForm sash) {
        try {
            String value = getProperty("sash.layout");
            if (value != null && !value.isEmpty()) {
                String[] values = value.split(",");
                int[] weights = { new Integer(values[0].trim()).intValue(), new Integer(values[1].trim()).intValue() };
                sash.setWeights(weights);
                LOGGER.debug("load sash");
            }
        } catch (Exception e) {
            //
        }
    }

    public void restoreWindowLocation() {
        if (shell != null) {
            if ("1".equals(getProperty("isMaximized"))) {
                shell.setMaximized(true);
            } else {
                int x = getInt(getProperty(WIN_LOCATE_X), defaultLocation.x);
                int y = getInt(getProperty(WIN_LOCATE_Y), defaultLocation.y);
                LOGGER.debug("restoreWindowLocation: x = " + x + ", y = " + y + ", key = " + className);
                shell.setLocation(x, y);
            }
        } else {
            LOGGER.debug("shell is null");
        }
    }

    public Point getWindowSize() {
        int x = getInt(getProperty(WIN_SIZE_X), defaultSize.x);
        int y = getInt(getProperty(WIN_SIZE_Y), defaultSize.y);
        LOGGER.debug("getWindowSize: x = " + x + ", y = " + y + ", key = " + className);
        return new Point(x, y);
    }

    public void restoreWindowSize() {
        if ("1".equals(getProperty("isMaximized"))) {
            shell.setMaximized(true);
        } else {
            shell.setSize(getWindowSize());
            LOGGER.debug(className + ": Windows size restored");
        }
    }

    public void saveWindowPosAndSize() {
        saveWindow();
    }

    public void saveWindow() {
        if (shell != null) {
            LOGGER.debug(className);
            int x = shell.getSize().x;
            int y = shell.getSize().y;
            saveProperty(WIN_SIZE_X, x + "");
            saveProperty(WIN_SIZE_Y, y + "");
            saveProperty("isMaximized", shell.getMaximized() ? "1" : "0");
            x = shell.getLocation().x;
            y = shell.getLocation().y;
            saveProperty(WIN_LOCATE_X, x + "");
            saveProperty(WIN_LOCATE_Y, y + "");
        } else {
            LOGGER.debug("shell is null");
        }
    }

    public void centerScreen() {
        flgClassIsActive = true;
        java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        shell.setBounds((screen.width - shell.getBounds().width) / 2, (screen.height - shell.getBounds().height) / 2, shell.getBounds().width,
                shell.getBounds().height);
        flgClassIsActive = false;
    }

    public void saveTableColumn(final String tableName, final TableColumn t) {
        String strCaption = (String) t.getData("caption");
        if (strCaption == null) {
            strCaption = t.getText();
        }
        String name = tableName + "/col/" + "_" + strCaption;
        LOGGER.debug("save column: " + name);
        prefs.node(name).put(WIDTH, String.valueOf(t.getWidth()));
    }

    public void tableColumnOrderRestore(final Table pobjTable) {
        String name = pobjTable.getData("caption") + "/colorder/default";
        String strNoOfColumn = prefs.node(name).get("NoOfColumns", "");
        if (!strNoOfColumn.isEmpty()) {
            int intNoOfColumn = new Integer(strNoOfColumn);
            if (intNoOfColumn == pobjTable.getColumnCount()) {
                int[] i = new int[intNoOfColumn];
                String strColOrder = prefs.node(name).get("columnorder", "");
                if (!strColOrder.isEmpty()) {
                    int iCol = 0;
                    for (String strIndex : strColOrder.split(";")) {
                        if (!strIndex.isEmpty()) {
                            i[iCol++] = new Integer(strIndex);
                        }
                    }
                    pobjTable.setColumnOrder(i);
                }
            }
        }
    }

    public void tableColumnOrderSave(final Table pobjTable) {
        String strOrder = "";
        int intSize = pobjTable.getColumnOrder().length;
        for (int i : pobjTable.getColumnOrder()) {
            strOrder += i + ";";
        }
        String name = pobjTable.getData("caption") + "/colorder/default";
        LOGGER.debug("save column order: " + name + ", " + strOrder);
        prefs.node(name).put("columnorder", strOrder);
        prefs.node(name).put("NoOfColumns", intSize + "");
    }

    public void restoreTableColumn(final String tableName, final TableColumn t, final int def) {
        String strCaption = (String) t.getData("caption");
        if (strCaption == null) {
            strCaption = t.getText();
        }
        String name = tableName + "/col/" + "_" + strCaption;
        try {
            String strVal = prefs.node(name).get(WIDTH, String.valueOf(def));
            LOGGER.debug(String.format("restore column '%1$s' with size '%2$s': ", name, strVal));
            t.setWidth(this.getInt(strVal, def));
        } catch (Exception e) {
            t.setWidth(def);
        }
    }

}