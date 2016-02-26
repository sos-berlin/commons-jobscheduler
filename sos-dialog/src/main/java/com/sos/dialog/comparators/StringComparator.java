package com.sos.dialog.comparators;

import com.sos.hibernate.classes.SosSortTableItem;

public class StringComparator extends SortBaseComparator implements Comparable {

    public StringComparator(SosSortTableItem tableItem, int rowNum, int colPos) {
        super(tableItem, rowNum, colPos);
    }

    public final int compareTo(Object arg0) {
        if (sosSortTableItem.getTextBuffer()[_colPos] == null) {
            sosSortTableItem.getTextBuffer()[_colPos] = "";
        }
        SosSortTableItem compareItem = null;
        if (((StringComparator) arg0) == null) {
            compareItem = new SosSortTableItem();
            compareItem.setTextBuffer(sosSortTableItem.getTextBuffer());
        } else {
            compareItem = ((StringComparator) arg0).sosSortTableItem;
        }

        if (compareItem.getTextBuffer()[_colPos] == null) {
            compareItem.getTextBuffer()[_colPos] = "";
        }
        int ret = sosSortTableItem.getTextBuffer()[_colPos].compareTo(compareItem.getTextBuffer()[_colPos]);
        return _sortFlag ? ret : ret * -1;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}