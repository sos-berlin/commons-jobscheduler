package com.sos.scheduler.model.objects;

import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.Job.Description;

/** @author oh */
public class JSObjDescription extends Description {

    private final String conClassName = "JSObjectDescription";

    public JSObjDescription(final SchedulerObjectFactory schedulerObjectFactory) {
        objFactory = schedulerObjectFactory;
    }

    public JSObjDescription(final SchedulerObjectFactory schedulerObjectFactory, final Job origOrder) {
        objFactory = schedulerObjectFactory;
        setObjectFieldsFrom(origOrder);
        afterUnmarshal();
    }

    public JSObjDescription(final SchedulerObjectFactory schedulerObjectFactory, final ISOSVirtualFile pobjVirtualFile) {
        objFactory = schedulerObjectFactory;
        final Job ObjDescription = (Job) unMarshal(pobjVirtualFile);
        setObjectFieldsFrom(ObjDescription);
        setHotFolderSrc(pobjVirtualFile);
        afterUnmarshal();
    }

    private void afterUnmarshal() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::afterUnmarchal";
    } // public afterUnmarshal

}
