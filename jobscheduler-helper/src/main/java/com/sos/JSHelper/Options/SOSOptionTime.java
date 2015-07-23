package com.sos.JSHelper.Options;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * @author KB
 *
 */
public class SOSOptionTime extends SOSOptionString {

	private static final long	serialVersionUID	= 6687670638160800096L;
	public final String			ControlType			= "timetext";
	public static String		dateTimeFormat		= new String("yyyy-MM-dd HH:mm:ss");
	private String				strDefaultUoM		= "";

	/**
	 * \brief SOSOptionTime
	 *
	 * \details
	 *
	 * @param pPobjParent
	 * @param pPstrKey
	 * @param pPstrDescription
	 * @param pPstrValue
	 * @param pPstrDefaultValue
	 * @param pPflgIsMandatory
	 */
	public SOSOptionTime(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
			final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
		super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
	}

	public SOSOptionTime(final String pPstrValue) {
		this(null, "", "", pPstrValue, pPstrValue, false);
	}

	public static String getCurrentTimeAsString() {
		String strT = "n.a.";
		try {
			strT = getCurrentTimeAsString(dateTimeFormat);
		}
		catch (Exception e) {
		}
		return strT;
	}

	public static String getCurrentTimeAsString(final String pstrDateTimeFormat) throws Exception {

		SimpleDateFormat formatter = new SimpleDateFormat(pstrDateTimeFormat);
		formatter.setLenient(true);
		Calendar now = Calendar.getInstance();
		return formatter.format(now.getTime());
	}

	public static String getCurrentDateAsString() throws Exception {
		return getCurrentDateAsString("yyyy-MM-dd");
	}

	public static String getCurrentDateAsString(final String pstrDateFormatMask) throws Exception {

		SimpleDateFormat formatter = new SimpleDateFormat(pstrDateFormatMask);
		formatter.setLenient(true);
		Calendar now = Calendar.getInstance();
		return formatter.format(now.getTime());
	}

	public void value(long plngValue) {
		lngValue = plngValue;
	}

	private long	lngValue	= 0;

	public void value(int pintValue) {
		lngValue = pintValue;
	}

	@Override
	public void Value(final String pstrValue) {
		super.Value(pstrValue);
		strValue = adjust2TimeFormat();
	}

	public int value() {
		return getTimeAsSeconds();
	}

	public long getTimeAsMilliSeconds() {
		return getTimeAsSeconds() * 1000L;
	}

	/**
	 *
	 * \brief getTimeAsSeconds
	 *
	 * \details
	 *
	 * \return int
	 *
	 * @return time as seconds
	 */

	public int getTimeAsSeconds() {
		int intSeconds = 0;
		if (lngValue != 0) {
			intSeconds = (int) lngValue;
		}
		else {
			int[] intM = { 1, 60, 3600, 3600 * 24 };

			String[] strT = strValue.split(":");

			int j = 0;
			for (int i = strT.length - 1; i >= 0; i--) {
				intSeconds += new Integer(strT[i]) * intM[j++];
			}
		}

		return intSeconds;
	} // public int getTimeAsSeconds()

	/**
	 *
	 * \brief calculateFileAge
	 *
	 * \details
	 *
	 * \return long
	 *
	 * @return age in milli-seconds
	 */
	public long calculateFileAge() {
		// TODO implement this method in JSFile
		long age = 0;
		if (isNotEmpty(strValue)) {
			if (strValue.indexOf(":") > -1) {
				String[] timeArray = strValue.split(":");
				long hours = Long.parseLong(timeArray[0]);
				long minutes = Long.parseLong(timeArray[1]);
				long seconds = 0;
				if (timeArray.length > 2) {
					seconds = Long.parseLong(timeArray[2]);
				}
				age = hours * 3600000 + minutes * 60000 + seconds * 1000;
			}
			else {
				age = Long.parseLong(strValue) * 1000;
			}
		}
		return age;
	}

	public String getTimeAsString(final long lngValue) {
		String strT = "";

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		strT = df.format(lngValue);

		return strT;
	}

	public String adjust2TimeFormat() {
		if (isNotEmpty(strValue)) {
			if (strValue.indexOf(":") > -1) {
			}
			else {
				if (isNotEmpty(strDefaultUoM)) {
					strValue = strValue + strDefaultUoM;
				}
			}
			int intL = strValue.length();
			if (strValue.equals("0") == false) {
				String strT = strValue.substring(intL - 1, intL).toLowerCase();
				String strN = strValue.substring(0, intL - 1);
				switch (strT) { // convert the UoM
					case "w": // weeks
						int intW = new Integer(strN);
						strValue = intW * 7 + ":00:00:00";
						break;
					case "d": // days
						strValue = strN + ":00:00:00";
						break;
					case "h": // hours
						strValue = strN + ":00:00";
						break;
					case "m": // minutes
						strValue = strN + ":00";
						break;
					case "s": // seconds
						strValue = strN;
						break;
					default: // is seconds
						strValue = strValue;
						break;
				}

			}
		}
		return strValue;
	}

	/**
	 * @return the defaultUoM
	 */
	public String getDefaultUoM() {
		return strDefaultUoM;
	}

	/**
	 * @param defaultUoM the defaultUoM to set
	 */
	public void setDefaultUoM(String defaultUoM) {
		strDefaultUoM = defaultUoM.toLowerCase();
		if (isNotEmpty(defaultUoM)) {
			adjust2TimeFormat();
		}
	}
}
