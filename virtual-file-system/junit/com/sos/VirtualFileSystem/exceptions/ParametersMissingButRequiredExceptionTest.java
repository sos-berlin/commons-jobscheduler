package com.sos.VirtualFileSystem.exceptions;

import org.junit.Test;

import com.sos.JSHelper.Exceptions.ParametersMissingButRequiredException;

public class ParametersMissingButRequiredExceptionTest {
	
	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class) 
	public void testParametersMissingButRequiredException() {
		throw new ParametersMissingButRequiredException("JADE", "http://www.sos-berlin.com/doc/en/jade/JADE Parameter Reference.pdf");
	}
}
