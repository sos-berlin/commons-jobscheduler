/*
 * JobSchedulerJavaObject.java
 * Created on 07.09.2007
 * 
 */
package sos.scheduler.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.codec.binary.Base64;

import sos.spooler.Variable_set;

/**
 * This class can be used to store Java Objects in Job Scheduler
 * Variables.<br>
 * The objects need to be serializable.<br>
 * Objects can be stored in task, order or global Scheduler variables. Note
 * that global Scheduler variables are not persistent.
 *
 * @author Andreas Liebert 
 */
public class SchedulerJavaObject {

	public static void putObject(Object obj, Variable_set set, String name) throws Exception{
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.close();
			String encoded = new String(Base64.encodeBase64(bos.toByteArray()));
			set.set_var(name, encoded);
		} catch(Exception e){
			throw new Exception("Error occured storing object in variale: "+e);
		}
	}
	
	public static Object getObject(Variable_set set, String name) throws Exception{
		try{
		Object schedulerObject;
		String encoded = set.value(name);
		if(encoded==null || encoded.length()==0) return null;
		byte[] serializedObject = Base64.decodeBase64(encoded.getBytes());
		ByteArrayInputStream bis = new ByteArrayInputStream(serializedObject);
		ObjectInputStream ois = new ObjectInputStream(bis);
		schedulerObject = ois.readObject();
		ois.close();
		return schedulerObject;
		} catch (Exception e){
			throw new Exception("Error occured reading object from variale: "+e);
		}
	}
}
