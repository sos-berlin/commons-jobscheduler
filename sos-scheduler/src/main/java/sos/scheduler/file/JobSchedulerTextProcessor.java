package sos.scheduler.file;

import com.sos.JSHelper.Basics.VersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import sos.spooler.Job_impl;
import sos.spooler.Variable_set;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSStandardLogger;

import java.io.*;
import java.util.HashMap;

class SOSSchedulerTextProcessor {
	private final String	conSVNVersion	= "$Id$";
	private SOSLogger		logger;
	private String			command;
	private HashMap			commands;
	private File			file;
	private String			param			= "";

	public SOSSchedulerTextProcessor(SOSLogger logger_, File file_, String command_) throws Exception {
		logger = logger_;
		this.command = command_.trim().toLowerCase().replaceAll("\\s{2,}", " ");
		this.file = file_;
		param = command.replaceFirst("^[^\\s]+\\s*(.*)$", "$1");
		command = command.replaceFirst("^([^\\s]+)\\s*.*$", "$1");
		commands = new HashMap();
		commands.put("count", "1");
		commands.put("countCaseSensitive", "2");
		commands.put("add", "3");
		commands.put("read", "4");
		commands.put("insert", "5");
	}

	public String exexute() throws Exception {
		return go();
	}

	public String exexute(String command_) throws Exception {
		param = command_.replaceFirst("^[^\\s]+\\s*(.*)$", "$1");
		command = command_.replaceFirst("^([^\\s]+)\\s*.*$", "$1");
		return go();
	}

	public String exexute(String command_, String param_) throws Exception {
		this.command = command_;
		this.param = param_;
		return go();
	}

	private String count(boolean ignoreCase) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String rec = null;
		String s = param;
		if (ignoreCase) {
			s = s.toLowerCase();
		}
		int i = 0;
		while ((rec = reader.readLine()) != null) {
			if (ignoreCase) {
				rec = rec.toLowerCase();
			}
			while (rec.indexOf(s) >= 0) {
				i++;
				rec = rec.replaceFirst(s, "");
			}
		}
		reader.close();
		return String.valueOf(i);
	}

	private String add() throws IOException {
		FileOutputStream f = new FileOutputStream(file, true);
		String s = "\n" + param;
		f.write(s.getBytes(), 0, s.length());
		return param;
	}

	private String insert() throws Exception {
		String line = param.replaceFirst("^[^\\s]+\\s*(.*).*$", "$1");
		String c = line.replaceFirst("^.*\\{char:\\s*([0-9]+)\\s*\\}.*$", "$1");
		if (!c.equals(line)) {
			int intVal = 0;
			try {
				intVal = Integer.parseInt(c, 10);
			}
			catch (NumberFormatException e) {
				logger.warn(c + " is not a valid number. 0 assumed");
				intVal = 0;
			}
			char charVal = (char) intVal;
			String s = String.valueOf(charVal);
			line = line.replaceFirst("\\{char:\\s*" + c + "\\s*\\}", s);
		}
		line = line + "\n";
		param = param.replaceFirst("^([^\\s]+)\\s*.*$", "$1");
		if (param.equals("last")) {
			add();
		}
		else {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String rec = "";
			int i = 0;
			if (param.equals("first")) {
				i = 1;
			}
			else {
				try {
					i = Integer.parseInt(param);
				}
				catch (NumberFormatException e) {
					logger.error(param + " is not a valid line number: 0 assumed");
					i = 0;
				}
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			i--;
			while ((rec = reader.readLine()) != null && i > 0) {
				rec = rec + "\n";
				baos.write(rec.getBytes());
				i--;
			}
			baos.write(line.getBytes());
			if (rec != null) {
				rec = rec + "\n";
				baos.write(rec.getBytes());
			}
			while ((rec = reader.readLine()) != null) {
				rec = rec + "\n";
				baos.write(rec.getBytes());
			}
			reader.close();
			FileOutputStream f = new FileOutputStream(file, false);
			f.write(baos.toByteArray());
			f.close();
		}
		return param;
	}

	private String read() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String rec = "";
		String erg = "";
		int i = 0;
		if (param.equals("first")) {
			i = 1;
		}
		else {
			if (!param.equals("last")) {
				try {
					i = Integer.parseInt(param);
				}
				catch (NumberFormatException e) {
					logger.error(param + " is not a valid line number: 0 assumed");
					i = 0;
				}
			}
		}
		while ((rec = reader.readLine()) != null && (param.equals("last") || i > 0)) {
			erg = rec;
			i--;
		}
		if (!param.equals("last") && rec == null && i > 0)
			erg = "(eof)";
		reader.close();
		return erg;
	}

	private String go() throws Exception {
		String erg = "";
		if (param.equals("")) {
			throw new Exception("Param missing in: " + command);
		}
		int command_id = getCommandId();
		switch (command_id) {
			case 1: // count
				return count(true);
			case 2: // countCaseSensitive
				return count(false);
			case 3: // add
				return add();
			case 4: // read
				return read();
			case 5: // insert
				return insert();
		}
		return erg;
	}

	private int getCommandId() throws Exception {
		if (commands.get(command) == null)
			throw new Exception("Unknown command: (not in count, add, read) " + command);
		String s = commands.get(command).toString();
		int commandId = 0;
		if (s != null) {
			commandId = Integer.parseInt(s);
		}
		return commandId;
	}

	public String getCommand() {
		return command;
	}

	public String getParam() {
		return param;
	}
}

/**
 * This job performs some action on textfiles
 * 
 * @author Uwe Risse <uwe.risse@sos-berlin.com>
 * @since  2009-05-25
*/
public class JobSchedulerTextProcessor extends Job_impl {
	private final String		conSVNVersion										= "$Id$";
	private static final String	conReturnParameterSCHEDULER_TEXTPROCESSOR_PARAM		= "scheduler_textprocessor_param";
	private static final String	conReturnParameterSCHEDULER_TEXTPROCESSOR_COMMAND	= "scheduler_textprocessor_command";
	private static final String	conReturnParameterSCHEDULER_TEXTPROCESSOR_RESULT	= "scheduler_textprocessor_result";
	private static final String	conReturnParameterSCHEDULER_TEXTPROCESSOR_FILENAME	= "scheduler_textprocessor_filename";
	private static final String	conParameterCommandPARAM							= "param";
	private static final String	conParameterCOMMAND									= "command";
	private static final String	conParameterFILENAME								= "filename";
	private SOSLogger			logger												= null;

	/**
	 * Implementierung für Spooler API.
	 * Initialisierungmethode für den Scheduler.
	 * @return boolean
	 */
	public boolean spooler_init() {
		try {
			try {
				this.logger = new SOSSchedulerLogger(this.spooler_log);
			}
			catch (Exception e) {
				throw new Exception("error occurred instantiating logger: " + e.getMessage());
			}
			return true;
		}
		catch (Exception e) {
			try {
				if (logger != null)
					logger.error("error occurred in spooler_init(): " + e.getMessage());
			}
			catch (Exception x) {
			}
			return false;
		}
	}

	private String getParam(Variable_set params, String name, boolean mandatory) throws Exception {
		String erg = "";
		if (params.var(name) != null && params.var(name).length() > 0)
			erg = params.var(name);
		else
			if (mandatory)
				throw new JobSchedulerException("job parameter is missing: [" + name + "]");
		logger.info(".. job parameter [" + name + "]: " + erg);
		return erg;
	}

	/**
	 * Implementierung für Spooler API. Läuft bis return = false
	 * @return boolean
	 */
	public boolean spooler_process() {
/*         •	Lesen der n-ten Zeile aus einer Datei
	   •	Zeile an Datei anfügen
	   •	Zählen des Vorkommens einer bestimmten Zeichenfolge in einer Datei
*/
		try {
			logger.debug(VersionInfo.VERSION_STRING);
			logger.debug(conSVNVersion);
			// Job oder Order
			Variable_set params = spooler.create_variable_set();
			if (spooler_task.params() != null)
				params.merge(spooler_task.params());
			if (spooler_job.order_queue() != null && spooler_task.order().params() != null) {
				params.merge(spooler_task.order().params());
			}
			// mandatory parameters
			String fileName = getParam(params, conParameterFILENAME, true);
			String command = getParam(params, conParameterCOMMAND, true);
			String param = getParam(params, conParameterCommandPARAM, false);
			command = command + " " + param;
			String oldFile = fileName;
			// To make orderparams available for substitution in orderparam value
			while (fileName.matches("^.*%[^%]+%.*$")) {
				String p = fileName.replaceFirst("^.*%([^%]+)%.*$", "$1");
				String s = params.var(p);
				s = s.replace('\\', '/');
				fileName = fileName.replaceAll("%" + p + "%", s);
			}
			if (!fileName.equals(oldFile))
				logger.info(".. job parameter after substitution [file]: " + fileName);
			spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_FILENAME, fileName);
			//
			SOSSchedulerTextProcessor textProcessor = new SOSSchedulerTextProcessor(logger, new File(fileName), command);
			String result = textProcessor.exexute();
			//
			if (spooler_job.order_queue() != null) {
				spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_RESULT, result);
			}
			spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_COMMAND, textProcessor.getCommand());
			spooler_task.order().params().set_var(conReturnParameterSCHEDULER_TEXTPROCESSOR_PARAM, textProcessor.getParam());
			return (spooler_job.order_queue() != null);
		}
		catch (Exception e) {
			try {
				e.printStackTrace(System.err);
				logger.error("error occurred in JobSchedulerTextProcessor: " + e.getMessage());
			}
			catch (Exception x) {
			}
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		SOSStandardLogger logger = new SOSStandardLogger(9);
		String file = "C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/logs/task.test,myJob.log";
		String command = " count   test";
		SOSSchedulerTextProcessor textProcessor = new SOSSchedulerTextProcessor(logger, new File(file), command);
		try {
			System.out.println(command + " ->" + textProcessor.exexute());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("count (Missing Argument)" + " ->" + textProcessor.exexute("count"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("countxxx (Unknow command)" + " ->" + textProcessor.exexute("countxxx"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("count Diferencia en" + " ->" + textProcessor.exexute("count Diferencia en"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("count b" + " ->" + textProcessor.exexute("count", "b"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("count xtest" + " ->" + textProcessor.exexute("count", "xtest"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("insert  last letzte" + " ->" + textProcessor.exexute("insert  last letzte"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("insert  2 zweite" + " ->" + textProcessor.exexute("insert  2 zweite"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			System.out.println("insert  first erste" + " ->" + textProcessor.exexute("insert  first erste{char:27}test"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		/*    try {
				  System.out.println("read  2" + " ->" + textProcessor.exexute("read","3"));
		  	}catch (Exception e) {System.out.println(e.getMessage());
		  	}
		    try {
			  System.out.println("read  2223" + " ->" + textProcessor.exexute("read","2223"));
		  	}catch (Exception e) {System.out.println(e.getMessage());
			}
		    try {
		 	   System.out.println("read  first" + " ->" + textProcessor.exexute("read first"));
		     }catch (Exception e) {System.out.println(e.getMessage());
		   	}
		 
		     try {
		 		  System.out.println("add xxtestxxx" + " ->" + textProcessor.exexute("add xxxtestxxx"));
		   	    }catch (Exception e) {System.out.println(e.getMessage());
		   	    }	     
		     try {
		   		  System.out.println("insert 2 Zeile2 ist nun" + " ->" + textProcessor.exexute("insert 2 Zeile2 ist nun"));
			    }catch (Exception e) {System.out.println(e.getMessage());
			   	    }	     

		   	    try {
		  	   System.out.println("read  last" + " ->" + textProcessor.exexute("read","last"));
		    }catch (Exception e) {System.out.println(e.getMessage());
		   	}*/
	}
}
