/**
 * 
 */
package com.sos.localization;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

/**
 * @author KB
 *
 */
public class I18NObject {

	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(this.getClass());

	

	private String	strKey		= "";
	private String	strLanguage	= "";

	private String	F1			= "";	//  
	private String	F10			= "";	//  
	private String	label		= "";	//  
	private String	shorttext	= "";	//  
	private String	tooltip		= "";	//  
	private String	icon		= "";	//  
	private String	acc			= "";	//  

	/**
	 * 
	 */
	public I18NObject(final String pstrKey, final String pstrLanguage) {
		strKey = pstrKey;
		strLanguage = pstrLanguage;
	}

	/**
	 * @return the f1
	 */
	public String getF1() {
		return F1;
	}

	/**
	 * @param f1 the f1 to set
	 */
	public void setF1(String f1) {
		F1 = f1;
	}

	/**
	 * @return the f10
	 */
	public String getF10() {
		return F10;
	}

	/**
	 * @param f10 the f10 to set
	 */
	public void setF10(String f10) {
		F10 = f10;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the shorttext
	 */
	public String getShorttext() {
		return shorttext;
	}

	/**
	 * @param shorttext the shorttext to set
	 */
	public void setShorttext(String shorttext) {
		this.shorttext = shorttext;
	}

	/**
	 * @return the tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @param tooltip the tooltip to set
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @return the acc
	 */
	public String getAcc() {
		return acc;
	}

	/**
	 * @param acc the acc to set
	 */
	public void setAcc(String acc) {
		this.acc = acc;
	}

	public void setBeanProperty(final String pstrTooken, final String pstrValue) {
		logger.debug(strKey + ": " + pstrTooken + " = " + pstrValue);
		try {
			switch (pstrTooken) {
				case "F1":
					setF1(pstrValue);
					break;

				case "F10":
					setF10(pstrValue);
					break;

				default:
					PropertyUtils.setProperty(this, pstrTooken, pstrValue.trim());
					break;
			}
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			System.err.println(e.getLocalizedMessage());
			//			System.exit(9);
		}
	}

}
