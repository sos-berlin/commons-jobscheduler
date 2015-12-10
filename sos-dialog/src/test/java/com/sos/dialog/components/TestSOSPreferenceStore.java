package com.sos.dialog.components;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestSOSPreferenceStore {

	@Test
	public void 	testSOSPreferenceStore() {
		SOSPreferenceStore sosPreferenceStore = new SOSPreferenceStore("/1/2/3"); 
   	    assertEquals ("", "1_2_3",sosPreferenceStore.strKey);

		sosPreferenceStore = new SOSPreferenceStore("/1//2/3"); 
   	    assertEquals ("", "1__2_3",sosPreferenceStore.strKey);

   	    sosPreferenceStore = new SOSPreferenceStore("/1/2/3"); 
   	    assertEquals ("", "1_2_3",sosPreferenceStore.strKey);
     
   	    sosPreferenceStore = new SOSPreferenceStore("///1/2/3");
   	    assertEquals ("", "1_2_3",sosPreferenceStore.strKey);

	}

}
