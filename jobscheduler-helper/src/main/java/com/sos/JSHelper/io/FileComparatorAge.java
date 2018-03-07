package com.sos.JSHelper.io;

import java.io.File;
import java.util.Comparator;

public class FileComparatorAge implements Comparator<File> {

	@Override
    public int compare(File f1, File f2) {
		long a1 = f1.lastModified();
		long a2 = f2.lastModified();
		
        if(a1 == a2){
            return 0;
        }
        if (a1 > a2) {
        	return 1;
        }
        return -1;
     }

}
