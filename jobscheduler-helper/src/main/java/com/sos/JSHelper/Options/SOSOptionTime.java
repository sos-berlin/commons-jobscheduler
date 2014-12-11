package com.sos.JSHelper.Options;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
* \class SOSOptionTime
*
* \brief SOSOptionTime -
*
* \details
*
*         <note language="en">
          <div xmlns="http://www.w3.org/1999/xhtml">
Specifies the minimum age of one or multiple files.
If the files is newer then it is classified as non-existing.<br/>
<br/>
Possible values:<br/>
0 (disabled)<br/>
seconds<br/>
hours:minutes<br/>
hours:minutes:seconds
          </div>
        </note>

        <note language="de">
          <div xmlns="http://www.w3.org/1999/xhtml">
Vereinbart das Mindestalter einer oder mehrerer Dateien.
Ist eine Datei j�nger, dann gilt sie als nicht vorhanden.<br/>
<br/>
M�gliche Werte sind:<br/>
0 (parameter wird ignoriert)<br/>
Sekunden<br/>
Stunden:Minuten<br/>
Stunden:Minuten:Sekunden
					</div>
        </note>

* \section SOSOptionTime.java_intro_sec Introduction
*
* \section SOSOptionTime.java_samples Some Samples
*
* \code
*   .... code goes here ...
* \endcode
*
* <p style="text-align:center">
* <br />---------------------------------------------------------------------------
* <br /> APL/Software GmbH - Berlin
* <br />##### generated by ClaviusXPress (http://www.sos-berlin.com) #########
* <br />---------------------------------------------------------------------------
* </p>
* \author KB
* @version $Id$28.08.2010
* \see reference
*
* Created on 28.08.2010 22:36:43
 */

/**
 * @author KB
 *
 */
public class SOSOptionTime extends SOSOptionInteger {

	private static final long	serialVersionUID	= 6687670638160800096L;
	@SuppressWarnings("unused")
	private final String		conClassName		= "SOSOptionTime";
	@SuppressWarnings("hiding")
	public final String			ControlType			= "timetext";

	public static String		dateTimeFormat		= new String("yyyy-MM-dd HH:mm:ss");

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
		int[] intM = { 1, 60, 3600, 3600 * 24 };

		String[] strT = strValue.split(":");

		int j = 0;
		for (int i = strT.length - 1; i >= 0; i--) {
			intSeconds += new Integer(strT[i]) * intM[j++];
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
}
