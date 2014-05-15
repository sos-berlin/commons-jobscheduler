/**
 *
 */
package com.sos.DataExchange.Options;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.sos.DataExchange.jaxb.configuration.ConfigurationElement;
import com.sos.DataExchange.jaxb.configuration.JADEParam;
import com.sos.DataExchange.jaxb.configuration.JADEParamValues;
import com.sos.DataExchange.jaxb.configuration.JADEParams;
import com.sos.DataExchange.jaxb.configuration.JADEProfile;
import com.sos.DataExchange.jaxb.configuration.JADEProfileIncludes;
import com.sos.DataExchange.jaxb.configuration.JADEProfiles;
import com.sos.DataExchange.jaxb.configuration.Value;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * @author KB
 *
 */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class JADEOptions extends SOSFTPOptions {

	/**
	 *
	 */
	private static final long	serialVersionUID	= -5788970501747521212L;
	@SuppressWarnings("unused")
	private static final String	conSVNVersion					= "$Id$";

	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	private final Logger logger = Logger.getLogger(this.getClass());
	

	public JADEOptions() {
		super();
	}
	
	
	public JADEOptions(final HashMap<String, String> JSSettings) throws Exception {
		super(JSSettings);
	}
	
	public JADEOptions(final enuTransferTypes local, final enuTransferTypes enuTargetTransferType) {
		super(local, enuTargetTransferType);
	}

	private static final String	conFileNameExtensionJADEConfigFile	= ".jadeconf";
	
	@Override
	public HashMap<String, String> ReadSettingsFile() {
		Properties objP = new Properties();
		HashMap<String, String> map = new HashMap<>();

		String strSettingsFile = settings.Value();
		if (strSettingsFile.endsWith(conFileNameExtensionJADEConfigFile)) {
			try {
				JAXBContext jc = JAXBContext.newInstance(ConfigurationElement.class);
				Unmarshaller u = jc.createUnmarshaller();
				ConfigurationElement objJADEConfig = (ConfigurationElement) u.unmarshal(new FileInputStream(settings.Value()));
				Vector<Object> objProfileOrProfiles = (Vector<Object>) objJADEConfig.getIncludeOrProfileOrProfiles();
				searchXMLProfile(objP, objProfileOrProfiles, "globals");
				searchXMLProfile(objP, objProfileOrProfiles, profile.Value());
			}
			catch (JAXBException je) {
				je.printStackTrace();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		else {  // TODO any file extension is allowed for the ini-configuration file
			super.ReadSettingsFile();
		}
		return map;
	}
	
	private void processXMLProfile(final Properties objP, final JADEProfile objProfile) {
		for (Object object2 : objProfile.getIncludeOrIncludesOrParams()) {
			if (object2 instanceof JADEProfileIncludes) {
			}
			else {
				if (object2 instanceof JADEParam) {
					processXMLParam(objP, (JADEParam) object2);
				}
				else {
					if (object2 instanceof JADEParams) {
						processXMLParams(objP, (JADEParams) object2);
					}
				}
			}
		}
	}

	private void searchXMLProfile(final Properties objP, final Vector<Object> objProfileOrProfiles, final String pstrProfileName) {
		logger.debug("Profile = " + profile.Value());
		for (Object object : objProfileOrProfiles) {
			if (object instanceof JADEProfile) {
				JADEProfile objProfile = (JADEProfile) object;
				String strProfileName = objProfile.getName();
				logger.debug(" ... Profile Name = " + strProfileName);
				if (objProfile.getName().equalsIgnoreCase(pstrProfileName)) {
					processXMLProfile(objP, objProfile);
					break;
				}
			}
			else {
				if (object instanceof JADEProfiles) {
					Vector<Object> lstProfileOrProfiles = (Vector<Object>) ((JADEProfiles) object).getIncludeOrProfile();
					searchXMLProfile(objP, lstProfileOrProfiles, pstrProfileName);
					break;
				}
			}
		}
	}

	private void processXMLParams(final Properties objP, final JADEParams pobjParams) {
		for (Object object3 : pobjParams.getParamOrParams()) {
			if (object3 instanceof JADEParam) {
				processXMLParam(objP, (JADEParam) object3);
			}
			else {
				if (object3 instanceof JADEParams) {
					processXMLParams(objP, (JADEParams) object3);
				}
			}
		}
	}

	private void processXMLParam(final Properties objP, final JADEParam pobjParam) {
		JADEParam objParam = pobjParam;
		System.out.println(" ... Param name = " + objParam.getName());
		if (objParam.getValue() != null) {
			objP.put(objParam.getName(), objParam.getValue());
		}
		else {
			List<Object> objV = objParam.getIncludeOrValues();
			for (Object objO2 : objV) {
				if (objO2 instanceof JADEParamValues) {
					JADEParamValues objValues = (JADEParamValues) objO2;
					for (Object objV2 : objValues.getValue()) {
						Value objValue = (Value) objV2;
						System.out.println(String.format(" +++ value '%1$s' with prefix '%2$s'", objValue.getVal(), objValue.getPrefix()));
						String strV = objValue.getVal();
						String strPre = objValue.getPrefix();
						if (strPre != null) {
							strV = strPre + "_" + objParam.getName();
						}
						else {
							strPre = objParam.getName();
						}
						objP.put(strPre, strV);
						logger.debug("Put to Properties Param = " + strPre + ", Value = " + strV);
					}
				}
			}
		}
	}

	@SuppressWarnings("unused") private void iterateProfile(final Object objP) {
		if (objP instanceof JADEProfile) {
			JADEProfile objProfile = (JADEProfile) objP;
			System.out.println("--- Profile name = " + objProfile.getName());
			for (Object object2 : objProfile.getIncludeOrIncludesOrParams()) {
				if (object2 instanceof JADEProfileIncludes) {
				}
				else {
					if (object2 instanceof JADEParam) {
						JADEParam objParam = (JADEParam) object2;
						System.out.println(" ... Param name = " + objParam.getName());
						Object objV = objParam.getIncludeOrValues();
						if (objV instanceof JADEParamValues) {
							JADEParamValues objValues = (JADEParamValues) objV;
							for (Object objV2 : objValues.getValue()) {
								Value objValue = (Value) objV2;
								System.out.println(String.format(" +++ value '%1$2' with prefix '%2$s'", objValue.getVal(), objValue.getPrefix()));
							}
						}
					}
					else {
						if (object2 instanceof JADEParams) {
							JADEParams objParams = (JADEParams) object2;
							for (Object object3 : objParams.getParamOrParams()) {
								if (object3 instanceof JADEParam) {
									JADEParam objParam = (JADEParam) object3;
									System.out.println("Param name = " + objParam.getName());
									for (Object objV2 : objParam.getIncludeOrValues()) {
										if (objV2 instanceof JADEParamValues) {
											JADEParamValues objValues = (JADEParamValues) objV2;
											for (Object objV3 : objValues.getValue()) {
												Value objValue = (Value) objV3;
												System.out.println(String.format(" +++ value '%1$s' with prefix '%2$s'", objValue.getVal(),
														objValue.getPrefix()));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}


}
