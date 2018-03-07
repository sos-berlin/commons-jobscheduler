package com.sos.JSHelper.io;

import java.io.File;
import java.util.Comparator;

public class FileComparatorSize implements Comparator<File> {

	@Override
    public int compare(File f1, File f2) {
		long s1 = f1.length();
		long s2 = f2.length();
		
        if(s1 == s2){
            return 0;
        }
        if (s1 > s2) {
        	return 1;
        }
        return -1;
     }

}
