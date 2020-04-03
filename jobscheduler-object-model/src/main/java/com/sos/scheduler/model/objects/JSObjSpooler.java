package com.sos.scheduler.model.objects;

import java.util.ArrayList;
import java.util.List;

import com.sos.vfs.common.interfaces.ISOSProviderFile;
import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjSpooler extends Spooler {

    private final String conClassName = "JSObjSpooler";

    public JSObjSpooler(final SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

    public JSObjSpooler(final SchedulerObjectFactory schedulerObjectFactory, final ISOSProviderFile pobjVirtualFile) {
        super();
        objFactory = schedulerObjectFactory;
        final Spooler objSpooler = (Spooler) unMarshal(pobjVirtualFile);
        setObjectFieldsFrom(objSpooler);
        setHotFolderSrc(pobjVirtualFile);
        afterUnmarshal();
    }

    private void afterUnmarshal() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::afterUnmarchal";
        Config objConfig = getConfig().get(0);
        if (objConfig.getSchedulerScript() != null) {
            for (SchedulerScript objSchedulerScript : objConfig.getSchedulerScript()) {
                if (objSchedulerScript.getScript() != null) {
                    removeEmptyContentsFrom(objSchedulerScript.getScript().getContent());
                }
            }
        }
        if (objConfig.getScript() != null) {
            removeEmptyContentsFrom(objConfig.getScript().getContent());
        }
    } // public afterUnmarchal

    /** \brief removeEmptyContentsFrom
     * 
     * \details Some objects contain cdata as well as other objects, so that
     * unmarshalling creates for every spaces around the other objects empty
     * cdata nodes. These empty cdata nodes are removed.
     *
     * \return void
     *
     * @param objList */
    private void removeEmptyContentsFrom(final List<Object> objList) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::removeEmptyContentsFrom";
        final List<String> emptyContents = new ArrayList<String>();
        for (final Object listItem : objList) {
            if (listItem instanceof String && ((String) listItem).trim().length() == 0) {
                emptyContents.add(((String) listItem));
            }
        }
        objList.removeAll(emptyContents);
    } // private removeEmptyContentsFrom
}
