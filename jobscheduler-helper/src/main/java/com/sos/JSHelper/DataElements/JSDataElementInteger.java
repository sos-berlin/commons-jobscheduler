package com.sos.JSHelper.DataElements;


public class JSDataElementInteger extends JSDataElementNumeric {

	@SuppressWarnings("unused")
	private final int intMinSize = 0;  // minimum Size
	@SuppressWarnings("unused")
	private final int intMaxSize = 9999;  // maximum Size

	private int intValue = 0;

	public JSDataElementInteger() {
		super.TrimValue(true);		// hier immer die blanks abschneiden, falls nicht anders eingestellt.
	}

	public JSDataElementInteger(final String pstrValue) {
		super.Value(pstrValue);
		super.TrimValue(true);		// hier immer die blanks abschneiden, falls nicht anders eingestellt.
		getInt();
	}

	public JSDataElementInteger (final int pintValue) {
		intValue = pintValue;
		super.TrimValue(true);		// hier immer die blanks abschneiden, falls nicht anders eingestellt.
		this.Value(pintValue);
	}

	public JSDataElementInteger(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos, final String pPstrFormatString, final String pPstrColumnHeader,
			final String pPstrXMLTagName) {
		super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
	}


	@Override
	public void Value (final String pstrValue) {
		super.Value(pstrValue);
		getInt();
	}
	/**
	 *
	 * \brief Value
	 *
	 * \details

	 * \return void
	 *
	 * @param pintValue
	 */
	public void Value (final int pintValue) {
		intValue = pintValue;
		super.Value(new Integer(pintValue).toString());
	}
	/**
	 *
	 * \brief getInt
	 *
	 * \details

	 * \return int
	 *
	 */
	public int getInt () {
		// EQCPN-2009-03-06 Direkt aus dem Eigenschaftwert ermitteln, weil intValue nicht gesetzt
		// wird, wenn die Wertezuweisung mit Value(String pstrValue) erfolgt.
		try {
			intValue = 0;
			String strT = super.Value().trim();
			final int intLen = strT.length();
			if (intLen > 0) {
				if (strT.endsWith("-")) {
					strT = "-" + strT.substring(0, intLen-1);
				}
				else {
					if (strT.startsWith("+")) {
						strT = strT.substring(1, intLen);
					}
					else {
						if (strT.endsWith("+")) {
							strT = strT.substring(0, intLen-1);
						}
					}
				}

			intValue = Integer.parseInt(strT);
			}
		} catch(final NumberFormatException e) {
			intValue = 0;
		}
		return intValue;
	}
	/**
	 *
	 * \brief FormattedValue - Liefert den Wert des Elements formatiert
	 *
	 * \details
	 * das Format (die Edit-Maske) wird über die Eigenschaft FormatString
	 * definiert.
	 *
	 * Wenn kein Format-String definiert ist, so wird der Wert als String
	 * zurückgegeben.
	 *
	 * \return String
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public String FormattedValue ()  {
		final String strFormat = super.FormatString();

		if (isNotEmpty(strFormat.trim())) {
			String strFormatted = String.format("%1$" + strFormat, getInt());
			strFormatted = strFormatted.trim();
			if (super.MaxSize()!=0) {		// EQCPN-2009-04-22: führt bei 0 sonst zur leerer Ausgabe
				if (strFormatted.length() > super.MaxSize()) {
					strFormatted = "";
					for (int i = 0; i < super.MaxSize(); i++) {
						strFormatted += "*";
					}
				}
			}
			return strFormatted;
		}
		else {
			return super.Value();
		}
	}

	@Override
	public void doInit() {

		super.MaxSize(15);
		super.FormatString("-,15d");
		super.Description("Integer");
		super.ColumnHeader("Integer");
		super.XMLTagName("Integer");

	}
}
