/*
 * Created on 06.04.2011
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sos.dialog.comparators;

import com.sos.hibernate.classes.SosSortTableItem;


/**
 * 
 * @author Administrator
 * Eigener Comparator der das Vergleichen der einzelnen Tabellenzeilen vornimmt... 
 */

public class StringComparator extends SortBaseComparator implements Comparable {

	/**
	 * Konstruktor ...
	 * @param textBuffer : Der Text der Zeile 
	 * @param rowNum : Die Zeilennr der Tabellenzeile 
	 * @param sortFlag : Aufsteigend false, Absteigend true
	 * @param colPos : Die Spalte nach der Sortiert werden soll
	 */

	public StringComparator(SosSortTableItem tableItem, int rowNum, int colPos) {
		super(tableItem,rowNum,colPos);
	}

	public final int compareTo(Object arg0) {
		if (sosSortTableItem.getTextBuffer()[_colPos] == null) {
			sosSortTableItem.getTextBuffer()[_colPos] = "";
		}
		SosSortTableItem compareItem = null;
		if (((StringComparator) arg0) == null) {
			compareItem = new SosSortTableItem();
			compareItem.setTextBuffer(sosSortTableItem.getTextBuffer());
		}else {
			compareItem = ((StringComparator) arg0).sosSortTableItem;
		}
		
		if  ( compareItem.getTextBuffer()[_colPos] == null){
			 compareItem.getTextBuffer()[_colPos] = "";
		}
		int ret =
			sosSortTableItem.getTextBuffer()[_colPos].compareTo(
					compareItem.getTextBuffer()[_colPos]);
		return _sortFlag ? ret : ret * -1;
	}

}