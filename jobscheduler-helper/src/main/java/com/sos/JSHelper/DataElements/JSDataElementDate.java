package com.sos.JSHelper.DataElements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.sos.JSHelper.Exceptions.FormatPatternException;

/**
 * \file JSDataElementDate.java
 * \brief Datenelement vom Typ Datum
 *
 * \class JSDataElementDate
 * \brief Datenelement vom Typ Datum
 *
 * \details
 * Diese Klasse bildet ein Datenelement vom Typ Datum ab. Instanzen dieser Klasse k�nnen
 * - je nach vorliegendem Ausgangswert - wie folgt gebildet werden:
 *
 * 1. Ohne Wertzuweisung (diese mu� dann mit der Methode Value() erfolgen):
 * \code
 * JSDataElementDate objDate = new JSDataElementDate();
 * \endcode
 *
 * 2. Auf Basis einer Datumsvariablen:
 * \code
 * Date objDat = new Date(2009, 05, 12);
 * JSDataElementDate objDate = new JSDataElementDate(objDat);
 * \endcode
 *
 * 3. Auf Basis eines Strings (das Stringformat mu� dem Standardformat JSDateFormat.dfISO entsprechen):
 * \code
 * JSDataElementDate objDate = new JSDataElementDate("2005-04-02T18:13:25");
 * \endcode
 *
 * 4. Auf Basis eines Strings mit Vorgabe des Datumsformates (aus der Klasse JSDateFormat):
 * \code
 * JSDataElementDate objDate = new JSDataElementDate("12.05.2009",JSDateFormat.dfGERMAN_SHORT);
 * \endcode
 *
 * Wahlweise kann das Eingabeformat mit der Methode setParsePattern(JSDateFormat pobjFormat) gesetzt werden.
 * Die Formatierung der Ausgabe kann mit setFormatPattern(JSDateFormat pobjFormat) beeinflusst werden. Vorgabe ist
 * auch hier das ISO-Format (JSDateFormat.dfISO.toPattern()).
 *
 * \author EQCPN
* @version $Id$13.05.2009 10:36:25
 * <div class="sos_branding">
 *   <p>� 2009 APL/Software GmbH - Berlin - generated by ClaviusXPress (<a style="color:silver" href="http://www.sos-berlin.com" target="_blank">http://www.sos-berlin.com</a>)</p>
 * </div>
 */
public class JSDataElementDate extends JSDataElement {

	private final String	conClassName	= "JSDataElementDate";

	// private Date datValue = new Date();
	private JSDateFormat	objFormat		= null;				// JSDateFormat.dfISO;

	/**
	 * \brief Konstruktor ohne Initialisierung
	 *
	 * \details
	 * Stellt eine Instanz des Objektes ohne Wertezuweisung zur Verf�gung.
	 */
	public JSDataElementDate() {
		super();
	}

	public JSDataElementDate(final String pstrDate, final String pstrTime) {
		super.Value(pstrDate + pstrTime);
	}

	public JSDataElementDate(final JSDataElement pelemDate, final JSDataElement pelemTime) {
		final String strD = pelemDate.Value();
		String strT = pelemTime.Value();
		if (strT.trim().length() <= 0) {
			strT = "000000";
		}
		super.Value(strD + strT);
	}

	/**
	 * \brief Initialisierung mit Datumsstring im Standardormat
	 *
	 * \details
	 * Der �bergebene Datumsstring muss dem ISO-Format yyyy-MM-dd'T'HH:mm:ss (JSDateFormat.dfISO) entsprechen.
	 *
	 * @param pstrDate
	 */
	public JSDataElementDate(final String pstrDate) {

		super.Value(pstrDate);
	}

	/**
	 * \brief Initialisierung mit Datumsstring und vorgegebenen Format
	 *
	 * \details
	 * Der �bergebene Datumsstring muss dem mit setFormatPattern(JSDateFormat pobjFormat)
	 * gesetztem Format entsprechen (Standard ist JSDateFormat.dfISO).
	 *
	 * @param pstrDate
	 * @param pobjFormat
	 */
	public JSDataElementDate(final String pstrDate, final JSDateFormat pobjFormat) {
		super.Value(pstrDate);
		objFormat = pobjFormat;
	}

	public JSDataElementDate(final Date pdteDate, final JSDateFormat pobjFormat) {
		this.Value(pdteDate);
		objFormat = pobjFormat;
	}

	/**
	 * \brief Initialisierung mit Datumswert.
	 * @param pdatDate
	 */
	public JSDataElementDate(final Date pdatDate) {
		JSDateFormat objFormat = JSDateFormat.dfTIMESTAMPS24;

		this.Value(objFormat.format(pdatDate));
	}

	public JSDataElementDate(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos, final String pPstrFormatString,
			final String pPstrColumnHeader, final String pPstrXMLTagName) {
		super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void Value(String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Value";

		if (pstrValue == null || pstrValue.equals("00000000")) {
			pstrValue = "";
		}

		super.Value(pstrValue);
	} // public void Value}

	/**
	 *
	 * \brief Value
	 *
	 * \details

	 * \return void
	 *
	 * @param pdblValue
	 */
	public void Value(final Date pdatValue) {
		if (objFormat == null) {
			objFormat = JSDateFormat.dfDATE_N8;
		}
		super.Value(objFormat.format(pdatValue));
		// this.datValue = pdatValue;
	}

	/**
	 *
	 * \brief Datumsobjekt liefern
	 *
	 * \details
	 * Normalerweise sollte hier die Methode getDate aus JSToolbox �berschrieben werden, die liefert aber
	 * einen String statt Date.
	 *
	 * \return Date
	 * @throws ParseException
	 */
	public Date getDateObject() {
		try {
			if (objFormat == null) {
				objFormat = JSDateFormat.dfDATE_N8;
			}
			Date objD = objFormat.parse(this.Value());
			return objD;
		}
		catch (final ParseException e) {
			message(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * \brief Setzen des Musters zur Datumserkennung
	 *
	 * \details
	 * Setzt das Muster, nach dem die (Text-)Datumsangaben peparst werden. Diese Muster
	 * ist NICHT identisch mit dem f�r die formatierte Ausgabe. Das kann �ber die Methode
	 * setFormatString() festgelegt werden.
	 *
	 * \see checkFormatPattern()
	 * \return void
	 *
	 * @param pobjFormat
	 */
	public void setParsePattern(final JSDateFormat pobjFormat) {
		objFormat = pobjFormat;
	}

	public JSDateFormat getParsePattern() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getParsePattern";

		return objFormat;
	} // public void getParsePattern}

	/**
	 * \brief Setzen des Musters zur Ausgabeformatierung
	 *
	 * \details
	 * Setzt das Muster, nach dem die Datumsangabe formatiert wird �ber ein JSDateFormat-Objekt.
	 * Wahlweise kann auch die Methode FormatString() verwendet werden.
	 *
	 * \return void
	 *
	 * @param pobjFormat
	 *
	 * \see FormattedValue ()
	 */
	public void setFormatPattern(final JSDateFormat pobjFormat) {
		super.FormatString(pobjFormat.toPattern());
	}

	/**
	 *
	 * \brief doInit
	 *
	 * \details

	 * \return void
	 *
	 */
	@Override
	public void doInit() {
		// super.FormatString(JSDateFormat.dfISO.toPattern());
		super.FormatString(JSDateFormat.dfDATE_N8.toPattern());
		super.Description("Date");
		super.ColumnHeader("Date");
		super.XMLTagName("Date");
		final String strFormat = super.FormatString();
		if (isNotEmpty(strFormat)) {
			final JSDateFormat df = new JSDateFormat(strFormat);
			super.setFormatPattern(df.toPattern());
		}
	}

	/**
	 * \brief FormattedValue - Liefert den Wert des Elements formatiert
	 *
	 * \details
	 * das Format (die Edit-Maske) wird �ber die Eigenschaft FormatString
	 * definiert.
	 *
	 * Wenn kein Format-String definiert ist, so wird der Wert als String
	 * zur�ckgegeben.
	 *
	 * \see setFormatPattern(JSDateFormat pobjFormat)
	 * \see JSDataElement::FormatString(String pstrFormatString)
	 * \return String
	 */
	@Override
	public String FormattedValue() {
		String strFormat = super.FormatString();
		if (strFormat.length() <= 0) {
			strFormat = JSDateFormat.dfDATE_N8.toPattern();
		}

		if (isNotEmpty(strFormat) && HasAValue() == true) {
			final JSDateFormat df = new JSDateFormat(strFormat);
			return df.format(getDateObject());
		}
		else {
			return this.Value();
		}
	}

	/**
	 * \brief checkFormatPattern
	 *
	 * \details
	 *
	 * \return
	 *
	 * @throws FormatPatternException
	 *
	 * \see setParsePattern(JSDateFormat pobjFormat)
	 */
	@Override
	public void checkFormatPattern() throws FormatPatternException {
		if (objFormat != null) {
			try {
				objFormat.parse(this.Value());
			}
			catch (final ParseException e) {
				throw new FormatPatternException("the value '" + this.Value() + "' does not correspond with the pattern " + objFormat.toPattern());
			}
		}
	}

	public boolean HasAValue() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::HasAValue";

		return !this.isEmpty();
	} // public boolean HasAValue}

	/**
	 *
	 * \brief ISEmpty
	 *
	 * \details
	 *
	 * \return boolean
	 *
	 * @return
	 */
	public boolean isEmpty() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::ISEmpty";

		if (super.Value().trim().length() <= 0 || super.Value().trim().equals("00000000")) {
			return true;
		}
		else {
			return false;
		}

	} // public boolean ISEmpty

	@Override
	public String SQLValue() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::SQLValue";
		this.FormatString(JSDateFormat.dfTIMESTAMPS.toPattern());
		String strV = FormattedValue();

		/**
		 * \todo die "richtige" Maske f�r das date-format in Oracle einbauen ...
		 */
		strV = strV.substring(0, 14);
		final String strMask = "YYYYMMDDHH24MISS"; // .substring(0, 8);
		return "to_date(" + strV + ", '" + strMask + "')";
	} // public String SQLValue}

	/**
	 * 
	 * \brief Now
	 * 
	 * \details
	 *
	 * \return 
	 *
	 * @return
	 */
	@Override
	public Date Now() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Now";

		final java.util.Calendar now = java.util.Calendar.getInstance();
		return now.getTime();
	} // public Date Now}

	public static String getCurrentTimeAsString(final String dateTimeFormat)  {

		String strFormat = dateTimeFormat;
		if (dateTimeFormat == null || dateTimeFormat.length() <= 0) {
			strFormat = "yyyyMMddHHmmss";
		}
		SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
		// formatter.setLenient(lenient);
		Calendar now = Calendar.getInstance();
		return formatter.format(now.getTime());
	}

	public static String getCurrentTimeAsString() throws Exception {

		return getCurrentTimeAsString("yyyyMMddHHmmss");
	}

	public int getLastFridayInAMonth(final int month, final int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 1, 0, 0, 0); // set to first day of the month
		cal.set(Calendar.MILLISECOND, 0);

		int firstDay = cal.get(Calendar.DAY_OF_WEEK);
		int daysOfMonth = cal.getMaximum(Calendar.DAY_OF_MONTH);

		switch (firstDay) {
			case Calendar.SUNDAY:
				return 27;
			case Calendar.MONDAY:
				return 26;
			case Calendar.TUESDAY:
				return 25;
			case Calendar.WEDNESDAY:
				if (daysOfMonth == 31)
					return 31;
				return 24;
			case Calendar.THURSDAY:
				if (daysOfMonth >= 30)
					return 30;
				return 23;
			case Calendar.FRIDAY:
				if (daysOfMonth >= 29)
					return 29;
				return 22;
			case Calendar.SATURDAY:
				return 28;
		}
		throw new RuntimeException("what day of the month?");
	}

	public int getLastThursday (final int month, final int year) {
		return getLastFridayInAMonth(month, year) - 1;
	}
}
