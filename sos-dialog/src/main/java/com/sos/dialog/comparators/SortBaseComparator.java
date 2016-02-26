package com.sos.dialog.comparators;

import com.sos.hibernate.classes.SosSortTableItem;

public class SortBaseComparator implements Comparable {

    public SosSortTableItem sosSortTableItem;
    public int _oldRowNum;
    public int _colPos;
    public boolean _sortFlag;

    public SortBaseComparator(SosSortTableItem tableItem, int oldRowNum, int colPos) {
        sosSortTableItem = tableItem;
        _oldRowNum = oldRowNum;
        _colPos = colPos;
    }

    public int compareTo(Object o) {
        return 0;
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
