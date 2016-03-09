package com.sos.dialog.comparators;

import com.sos.hibernate.classes.SosSortTableItem;

public class SortNumberComparator extends SortBaseComparator implements Comparable {

    int _intValue;

    public SortNumberComparator(SosSortTableItem tableItem, int rowNum, int colPos) {
        super(tableItem, rowNum, colPos);
        try {
            _intValue = Integer.parseInt(tableItem.getTextBuffer()[colPos]);
        } catch (NumberFormatException n) {
            _intValue = 0;
        }
    }

    public final int compareTo(Object o) {
        int _intValToCompare = ((SortNumberComparator) o)._intValue;
        int ret = _intValue < _intValToCompare ? -1 : (_intValue == _intValToCompare ? 0 : 1);
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