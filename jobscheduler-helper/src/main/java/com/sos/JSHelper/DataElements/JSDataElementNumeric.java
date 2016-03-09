package com.sos.JSHelper.DataElements;

/** \class JSDataElementNumeric
 *
 * \brief Basisklasse für ein numerisches Datenelement
 *
 * @author eqbfd */
public class JSDataElementNumeric extends JSDataElement {

    private final String conClassName = "JSDataElementNumeric";

    @SuppressWarnings("unused")
    private final String strMinValue = "";
    @SuppressWarnings("unused")
    private final String strMaxValue = "";

    @SuppressWarnings("unused")
    private int intDecimal = 0;

    public JSDataElementNumeric() {
    }

    public JSDataElementNumeric(final String pstrValue) {
        super.Value(pstrValue);
    }

    public JSDataElementNumeric(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos,
            final String pPstrFormatString, final String pPstrColumnHeader, final String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    public int MinSize() {
        return 0;
    }

    public void MinSize(@SuppressWarnings("unused") final int pintMinSize) {

    }

    public int MaxSize() {
        return 0;
    }

    public void MaxSize(@SuppressWarnings("unused") final int pintMaxSize) {

    }

    /** \brief FormattedValue - Liefert den Wert des Elements formatiert
     *
     * \details das Format (die Edit-Maske) wird über die Eigenschaft
     * FormatString definiert. Die Ausrichtung ist linksbündig.
     *
     * Wenn kein Format-String definiert ist, so wird der Wert als String
     * zurückgegeben.
     *
     * \return String
     * 
     * @throws Exception */
    @Override
    public String FormattedValue() {
        final String strFormat = super.FormatString();

        // so geht es ohne Format-String:
        // NumberFormat fmt = NumberFormat.getInstance(Locale.US);
        // fmt.setGroupingUsed(false);
        // fmt.setMaximumFractionDigits(3);
        //
        // try {
        // RetVal = fmt.format(dblQuantity);
        // //
        // }

        if (isNotEmpty(strFormat)) {
            try {
                final String strFormatted = String.format("%1$" + strFormat, new Integer(this.Value()));
                return strFormatted.trim();

            } // try
            catch (final Exception numberFormException) {
                return super.Value();
            }
        } else {
            return super.Value();
        }
    }

    /** \brief Decimal
     *
     * \details Setzt die max. Anzahl Dezimalstellen. Wird für die Darstellung
     * im XML verwendet
     *
     * \return void
     *
     * @param pintDecimal */
    @SuppressWarnings("unused")
    public void Decimal(final int pintDecimal) {
        System.out.println("JSDataElementNumeric.Decimal()" + pintDecimal);
        intDecimal = pintDecimal;
    }

    /** \brief Decimal Liefert die max. Anzahl Dezimalstellen
     *
     * \details
     *
     * \return int
     *
     * @return */
    @SuppressWarnings("unused")
    public int Decimal() {
        return intDecimal;
    }

    public int IntValue() {
        return new Integer(super.Value()).intValue();
    }

    public JSDataElementNumeric Subtract(final JSDataElementNumeric pobjDataElementNumeric) {
        final Integer intValue = new Integer(IntValue() - pobjDataElementNumeric.IntValue());
        this.Value(intValue.toString());
        return this;
    }

    public JSDataElementNumeric Add(final JSDataElementNumeric pobjDataElementNumeric) {
        final Integer intValue = new Integer(IntValue() + pobjDataElementNumeric.IntValue());
        this.Value(intValue.toString());
        return this;
    }

    @Override
    public String SQLValue() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::SQLValue";

        if (isEmpty(this.Value())) {
            return "null";
        }

        return this.Value();
    } // public String SQLValue}
}
