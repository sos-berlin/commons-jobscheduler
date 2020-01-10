package com.sos.localization;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/** \class PropertyFactoryOptions - PropertyFactora - a Factoroy to maintain I18N
 * Files
 *
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 *
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-297718331111000308.html for
 * (more) details.
 *
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 20141009200110 \endverbatim */
@JSOptionClass(name = "PropertyFactoryOptions", description = "PropertyFactora - a Factoroy to maintain I18N Files")
public class PropertyFactoryOptions extends PropertyFactoryOptionsSuperClass {

    private static final long serialVersionUID = 1L;

    /** constructors */

    public PropertyFactoryOptions() {
    } // public PropertyFactoryOptions

    public PropertyFactoryOptions(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public PropertyFactoryOptions

    //

    public PropertyFactoryOptions(HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public PropertyFactoryOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // PropertyFactoryOptionsSuperClass
    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
