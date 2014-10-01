/**
 * 
 */
package com.sos.JSHelper.interfaces;

/**
 * @author KB
 *
 */
public interface IAutoCompleteProposal {
	
	public void addProposal (final String pstrProposal);
	
	public String[] getAllProposals(String text);

}
