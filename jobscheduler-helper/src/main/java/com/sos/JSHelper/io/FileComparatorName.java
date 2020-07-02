package com.sos.JSHelper.io;

import java.io.File;
import java.util.Comparator;

public class FileComparatorName implements Comparator<File> {

	@Override
	public int compare(File f1, File f2) {
		String n1 = f1.getName();
		String n2 = f2.getName();

		return n1.compareTo(n2);
	}

}
