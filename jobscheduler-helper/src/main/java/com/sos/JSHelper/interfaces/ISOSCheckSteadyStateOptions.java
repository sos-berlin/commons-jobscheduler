package com.sos.JSHelper.interfaces;

import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionTime;

public interface ISOSCheckSteadyStateOptions {

    public abstract SOSOptionInteger getCheckSteadyCount();

    public abstract SOSOptionTime getCheckSteadyStateInterval();

    public abstract SOSOptionBoolean getCheckSteadyStateOfFiles();

}