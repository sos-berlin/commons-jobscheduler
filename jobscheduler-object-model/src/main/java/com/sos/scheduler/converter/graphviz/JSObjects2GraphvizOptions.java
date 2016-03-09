package com.sos.scheduler.converter.graphviz;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

/** \class JSObjects2GraphvizOptions - JSObjects2Graphviz
 *
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 *
 *
 * 
 *
 * see \see
 * C:\Users\KB\AppData\Local\Temp\scheduler_editor-2781494595910967227.html for
 * (more) details.
 * 
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 20121108150924 \endverbatim */
@JSOptionClass(name = "JSObjects2GraphvizOptions", description = "JSObjects2Graphviz")
public class JSObjects2GraphvizOptions extends JSObjects2GraphvizOptionsSuperClass {

    /**
	 * 
	 */
    private static final long serialVersionUID = 6063266497954052063L;
    @SuppressWarnings("unused")
    private final String conClassName = "JSObjects2GraphvizOptions";  //$NON-NLS-1$
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(JSObjects2GraphvizOptions.class);

    /** constructors */

    public JSObjects2GraphvizOptions() {
    } // public JSObjects2GraphvizOptions

    public JSObjects2GraphvizOptions(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public JSObjects2GraphvizOptions

    //

    public JSObjects2GraphvizOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    } // public JSObjects2GraphvizOptions (HashMap JSSettings)

    /** \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
     *
     * \details
     * 
     * @throws Exception
     *
     * @throws Exception - wird ausgelöst, wenn eine mandatory-Option keinen
     *             Wert hat */
    @Override
    // JSObjects2GraphvizOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
