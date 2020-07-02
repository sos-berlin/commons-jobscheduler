package com.sos.scheduler.model.answers;


import com.sos.scheduler.model.exceptions.JSCommandErrorException;
import com.sos.scheduler.model.exceptions.JSCommandOKException;
import com.sos.scheduler.model.objects.JSObjBase;

/** @author KB */
public class JSCmdBase extends JSObjBase {

    private final String conClassName = "JSCmdBase";

    protected Answer objAnswer = null;
    public static boolean flgRaiseOKException = true; // raise an exception if the command was successfull
    public static boolean flgLogXML = true;

    public JSCmdBase() {
        //
    }

    public void run() {
        objAnswer = objFactory.run(this);
    }

    public void getAnswerFromSpooler(sos.spooler.Spooler spooler) {
        objFactory.setSpooler(spooler);
        objAnswer = objFactory.getAnswerFromSpooler(this);
    }

    public Answer getAnswer() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getAnswer";

        if (objAnswer == null) {
            objAnswer = new ObjectFactory().createAnswer();
        }

        return objAnswer;
    } // public Answer getAnswer

    public Answer getAnswer(String pXMLStr) {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getAnswer";

        objAnswer = objFactory.getAnswer(pXMLStr);
        return objAnswer;
    } // public Answer getAnswer

    public boolean isEmpty(String value) {
        return (value == null || value.length() == 0);
    }

    public void getAnswerWithException() throws JSCommandErrorException, JSCommandOKException {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getAnswer";

        if (objAnswer == null) {
            objAnswer = new ObjectFactory().createAnswer();
        }
        if (objAnswer != null) {
            Ok objOK = objAnswer.getOk();
            ERROR objERROR = objAnswer.getERROR();
            String errMsg = "unknown error";
            if (objOK != null) {
                if (flgRaiseOKException == true) {
                    throw new JSCommandOKException();
                }
            } else {
                if (objERROR != null) {
                    errMsg = objERROR.getText();
                } else {
                    throw new JSCommandErrorException(errMsg);
                }
            }
        } else {
            throw new JSCommandErrorException("unable to get an instance of Class 'Answer'");
        }

    } // private Answer getAnswer

    /** \brief getERROR Returns an instance of the ERROR-Object if the last
     * command returned an error \details
     *
     * \return ERROR
     *
     * @return */
    public ERROR getERROR() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getERROR";

        ERROR objError = null;
        if (objAnswer != null) {
            objError = objAnswer.getERROR();
        }

        return objError;
    } // private ERROR getERROR

}
