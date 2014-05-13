package com.sos.JSHelper.DataElements;




/**
 * \class JSDataElementDouble
 * @author eqbfd
 *
 */
public class JSDataElementDouble extends JSDataElementNumeric {

	@SuppressWarnings("unused")
	private final String	conClassName			= "JSDataElementDouble";

	double dblValue = 0;

	public JSDataElementDouble() {
		super();
	}

	public JSDataElementDouble(String pstrValue) {
		super.Value(pstrValue);
		dblValue = getDouble();
	}

	public JSDataElementDouble(String pPstrValue, String pstrDescription) {
		this(pPstrValue);
		this.Description(pstrDescription);
	}

	public JSDataElementDouble(String pPstrValue, String pPstrDescription, int pPintSize, int pPintPos, String pPstrFormatString, String pPstrColumnHeader,
			String pPstrXMLTagName) {
		super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
	}


	public JSDataElementDouble (double pdblValue) {
		this.Value(pdblValue);
		dblValue = pdblValue;
	}

	/**
	 *
	 * \brief Value
	 *
	 * \details

	 * \return void
	 *
	 * @param pdblValue
	 */
	public void Value (double pdblValue) {
		super.Value(new Double(pdblValue).toString());
		dblValue = pdblValue;
	}

	/**
	 *
	 * \brief getDouble
	 *
	 * \details

	 * \return double
	 *
	 * @return
	 */
	public double getDouble () {

		return dblValue;
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
	public void doInit () {
//		super.doInit();
//- <remark who='EQALS' when='Donnerstag, 11. Juni 2009' id='Korrektur' >
		/**
		 * \change Donnerstag, 11. Juni 2009 EQALS Korrektur
		 * Korrektur Initialisierung FormatString
		 * Keine 1000er Gruppierung
		 * 3 Nachkommstellen
		 */
//- <oldcode>
//		super.FormatString(",15.2f");  // minus weggelassen, weil steht für "left justi"
//- </oldcode>
//- <newcode>
		super.FormatString("15.3f");  // minus weggelassen, weil steht für "left justi"
//- </newcode>
//- </remark>      <!-- id=<Korrektur>  -->
		super.Description("Double");
		super.ColumnHeader("Double");
		super.XMLTagName("Double");
	}

	protected double toDouble() throws Exception {
		dblValue = toDouble(this.Value());
		return dblValue ;
	}
	/**
	 * \change Donnerstag, 5. März 2009 EQCPN FORM
	 * Konvertierung verschiedener Zahlenformate
	 */
//	protected void toDouble () {
//		String strT = this.Value();
//		if (strT.length() <= 0) {
//			dblValue = 0;
//		}
//		else {
//			//- <remark who='EQCPN' when='Donnerstag, 5. März 2009' id='FORM' >
//			//-   <para>
//			//-   Konvertierung verschiedener Zahlenformate
//			//-   </para>
//			//- <oldcode>
//			// dblValue = new Double(this.Value());
//			//- </oldcode>
//			//- <newcode>
//			// Zunächst wird versucht die Zahl nach deutschem Format zu interpretieren (das , ist
//			// Dezimaltrennzeichen, der . für die Tausender. Klappt das nicht versuchen wir es mit
//			// dem amerikanischen Format.
//		    try {
//		        Number number = NumberFormat.getNumberInstance(Locale.GERMAN).parse(strT);
//		        dblValue = (number instanceof Double) ? (Double) number : new Double(this.Value());
//		    } catch (ParseException e) {
//				try {
//					Number number = NumberFormat.getNumberInstance(Locale.US).parse(strT);
//			        dblValue = (number instanceof Double) ? (Double) number : new Double(this.Value());
//				}
//				catch (ParseException e1) {
//					message("could not convert " + strT + " to double");
//					e1.printStackTrace();
//				}
//		    }
//			//- </newcode>
//			//- </remark>      <!-- id=<FORM>  -->
//		}
//	}


	/**
	 *
	 * \brief FormattedValue - Liefert den Wert des Elements formatiert
	 *
	 * \details
	 * das Format (die Edit-Maske) wird über die Eigenschaft FormatString
	 * definiert.
	 * Die Ausrichtung ist linksbündig.
	 *
	 * Wenn kein Format-String definiert ist, so wird der Wert als String
	 * zurückgegeben.
	 *
	 * \return String
	 *
	 */
	@Override
	public String FormattedValue () {
		String strFormat = super.FormatString();

// so geht es ohne Format-String:
//		NumberFormat fmt = NumberFormat.getInstance(Locale.US);
//		fmt.setGroupingUsed(false);
//		fmt.setMaximumFractionDigits(Decimal());
//		System.out.println("JSDataElementDouble.FormattedValue()" + Decimal());
//
//			return fmt.format(dblValue);
//
//		System.out.println("JSDataElementDouble.FormattedValue " + strFormat);
		if (isNotEmpty(strFormat)) {
			String strFormatted = String.format("%1$" + strFormat, dblValue);
			strFormatted = strFormatted.replace(",", ".");
			//TODO aktueller und zu verwendender Dezimalpunkt nicht fest, sondern dynamisch. 1000er Trennzeichen nicht sinnvoll?
			return strFormatted.trim();
		}
		return super.Value();
	}

	/**
	 *
	 * \brief Value
	 *
	 * \details
	 * Hier werden evtl. vorhandene Tausenderpunkte/Kommas oder dergl. rausgefiltert
	 * und es wird eine einheitliche Zahl erzeugt.
	 * \return
	 *
	 * @param pstrValue
	 */
	@Override
	public void Value(final String pstrValue) {

		@SuppressWarnings("unused")
		final String	conMethodName	= conClassName + "::Value";

		try {
			dblValue = toDouble(pstrValue);
			super.Value(new Double(dblValue).toString());
		} // try
		catch (Exception objException) {
			// TODO: handle exception
		}
		finally {
			//
		} // finally

	} // public void Value}
}
