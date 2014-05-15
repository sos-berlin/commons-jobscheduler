/**
 *
 */
package com.sos.VirtualFileSystem.Filter;

/**
 * @author KB
 *
 */
public interface ISOSFilteredFileReader {

	public void processRecord (final String pstrRecord);
	public void atStartOfData ();
	public void atEndOfData() ;
	
}
