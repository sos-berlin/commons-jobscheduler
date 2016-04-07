package sos.net.mail.options;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.interfaces.ISOSSmtpMailOptions;

@JSOptionClass(name = "SOSSmtpMailOptions", description = "Launch and observe any given job or job chain")
public class SOSSmtpMailOptions extends SOSSmtpMailOptionsSuperClass implements ISOSSmtpMailOptions {

    private static final long serialVersionUID = 6441074884525254517L;
    private final String conClassName = "SOSSmtpMailOptions";						//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(SOSSmtpMailOptions.class);
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    // TODO über Prefix OnError_, OnSuccess_, OnEmptyFiles_ adressieren
    @JSOptionClass(description = "", name = "SOSSmtpMailOptions")
    private SOSSmtpMailOptions objMailOnError = null;
    @JSOptionClass(description = "", name = "SOSSmtpMailOptions")
    private SOSSmtpMailOptions objMailOnSuccess = null;
    @JSOptionClass(description = "", name = "SOSSmtpMailOptions")
    private SOSSmtpMailOptions objMailOnEmptyFiles = null;
    private String strAlternativePrefix = "";

    public enum enuMailClasses {
        MailDefault, MailOnError, MailOnSuccess, MailOnEmptyFiles;
    }

    public SOSSmtpMailOptions() {
        super();
    } // public SOSSmtpMailOptions

    public SOSSmtpMailOptions(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    } // public SOSSmtpMailOptions

    public SOSSmtpMailOptions getOptions(final enuMailClasses penuMailClass) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getOptions";
        SOSSmtpMailOptions objO = objMailOnError;
        switch (penuMailClass) {
        case MailOnError:
            break;
        case MailOnEmptyFiles:
            objO = objMailOnEmptyFiles;
            break;
        case MailOnSuccess:
            objO = objMailOnSuccess;
            break;
        default:
            objO = this;
            break;
        }
        return objO;
    } // private SOSSmtpMailOptions getOptions

    private void initChildOptions() {
        if (objMailOnError == null) {
            objMailOnError = new SOSSmtpMailOptions();
            objMailOnSuccess = new SOSSmtpMailOptions();
            objMailOnEmptyFiles = new SOSSmtpMailOptions();
        }
    }

    public void setPrefixedValues(final HashMap<String, String> JSSettings) throws Exception {
        objMailOnError.setAllOptions(JSSettings, "MailOnError_");
        objMailOnSuccess.setAllOptions(JSSettings, "MailOnSuccess_");
        objMailOnEmptyFiles.setAllOptions(JSSettings, "MailOnEmptyFiles_");
    }

    public SOSSmtpMailOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
        initChildOptions();
        setPrefixedValues(JSSettings);
    } // public SOSSmtpMailOptions (HashMap JSSettings)

    public SOSSmtpMailOptions(final HashMap<String, String> JSSettings, final String pstrPrefix) throws Exception {
        strAlternativePrefix = pstrPrefix;
        setAllOptions(JSSettings, strAlternativePrefix);
        logger.trace(this.dirtyString());
    } // public SOSSmtpMailOptions (HashMap JSSettings)

 
    @Override
    // SOSSmtpMailOptionsSuperClass
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    } // public void CheckMandatory ()
}
