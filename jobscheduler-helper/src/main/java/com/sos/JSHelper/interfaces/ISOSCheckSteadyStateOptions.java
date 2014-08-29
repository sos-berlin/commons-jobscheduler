package com.sos.JSHelper.interfaces;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionTime;

public interface ISOSCheckSteadyStateOptions {
	/**
	 * @return the checkSteadyCount
	 */
	public abstract SOSOptionInteger getCheckSteadyCount();

	/**
	 * @return the check_steady_state_interval
	 */
	public abstract SOSOptionTime getCheck_steady_state_interval();

	/**
	 * @return the checkSteadyStateInterval
	 */
	public abstract SOSOptionTime getCheckSteadyStateInterval();

	/**
	 * @return the checkSteadyStateOfFiles
	 */
	public abstract SOSOptionBoolean getCheckSteadyStateOfFiles();
}