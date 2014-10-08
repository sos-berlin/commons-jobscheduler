/**
 *
 */
package com.sos.VirtualFileSystem.Filter;

import com.sos.JSHelper.io.Files.JSFile;

/**
 * @author KB
 *
 */
public interface ISOSFilteredFileReader {

	public void processRecord (final String pstrRecord);
	public void atStartOfNewFile (final JSFile file);
	public void atStartOfData ();
	public void atEndOfData() ;
	
}
