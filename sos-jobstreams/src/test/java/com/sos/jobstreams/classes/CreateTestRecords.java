package com.sos.jobstreams.classes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateTestRecords {

	private List<String> listOfStatements;

	public CreateTestRecords() {
		super();
		listOfStatements = new ArrayList<String>();
	}

	public void out() throws IOException {
		for (String s : listOfStatements) {
			System.out.println(s);
		}

		FileWriter fw = new FileWriter("c:/temp/1.txt");
		BufferedWriter bw = new BufferedWriter(fw);

		for (String s : listOfStatements) {
			System.out.println(s);
			bw.write(s+"\n");
		}
		bw.close();
	}

	public void generateInConditions() {
		String stmt1 = "Insert into SCHEDULER.JSTREAM_IN_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION,MARK_EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_1','performance_%s','performance','1');";
		String stmt2 = "Insert into SCHEDULER.JSTREAM_IN_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION,MARK_EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_2','performance_%s','ev_job_w%s_1[today]  ','1');";
		String stmt3 = "Insert into SCHEDULER.JSTREAM_IN_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION,MARK_EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_3','performance_%s','ev_job_w%s_1[today]','1');";
		String stmt4 = "Insert into SCHEDULER.JSTREAM_IN_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION,MARK_EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_4','performance_%s','ev_job_w%s_2[today] and ev_job_w%s_3[today]','1');";
		String stmt5 = "Insert into SCHEDULER.JSTREAM_IN_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION,MARK_EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_5','performance_%s','ev_job_w%s_1[today] and ev_job_w%s_4[today]','1');";
		String stmt6 = "Insert into SCHEDULER.JSTREAM_IN_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION,MARK_EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_error','performance_%s','ev_job_w%s_4_error','1');";
		String stmt7 = "Insert into SCHEDULER.JSTREAM_IN_CONDITION_COMMANDS (CREATED,ID,IN_CONDITION_ID,COMMAND,COMMAND_PARAM) values (sysdate,'%s','%s','startjob','now');";
		String stmt8 = "Insert into SCHEDULER.JSTREAM_IN_CONDITION_COMMANDS (CREATED,ID,IN_CONDITION_ID,COMMAND,COMMAND_PARAM) values (sysdate,'%s','%s','writelog','finished');";

		int id = 600;
		int idCommand = 600;
		for (int stream = 1; stream < 1000; stream++) {
			String stmt = String.format(stmt1, id, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt2, id, stream, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt3, id, stream, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id);
			listOfStatements.add(stmt);
			idCommand++;
			stmt = String.format(stmt8, idCommand, id);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt4, id, stream, stream, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt5, id, stream, stream, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt6, id, stream, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

		}
	}

	public void generateOutConditions() {
		String stmt1 = "Insert into SCHEDULER.JSTREAM_OUT_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_1','performance_%s','returncode:0');";
		String stmt2 = "Insert into SCHEDULER.JSTREAM_OUT_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_2','performance_%s','returncode:0');";
		String stmt3 = "Insert into SCHEDULER.JSTREAM_OUT_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_3','performance_%s','returncode:0');";
		String stmt4a = "Insert into SCHEDULER.JSTREAM_OUT_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_4','performance_%s','returncode:0');";
		String stmt4b = "Insert into SCHEDULER.JSTREAM_OUT_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_4','performance_%s','returncode:1-');";
		String stmt5 = "Insert into SCHEDULER.JSTREAM_OUT_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_5','performance_%s','returncode:0');";
		String stmt6 = "Insert into SCHEDULER.JSTREAM_OUT_CONDITIONS (CREATED,ID,SCHEDULER_ID,JOB,JOBSTREAM,EXPRESSION) values (sysdate,'%s','scheduler_joc_cockpit','/job_streams/performance/w%s/job_error','performance_%s','returncode:0');";
		String stmt7 = "Insert into SCHEDULER.JSTREAM_OUT_CONDITION_EVENTS (CREATED,ID,OUT_CONDITION_ID,EVENT,COMMAND) values (sysdate,'%s','%s','ev_job_w%s_%s','create');";

		int id = 600;
		int idCommand = 600;

		for (int stream = 1; stream < 1000; stream++) {
			String stmt = String.format(stmt1, id, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id, stream, 1);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt2, id, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id, stream, 2);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt3, id, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id, stream, 3);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt4a, id, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id, stream, 4);
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt4b, id, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id, stream, "4_error");
			listOfStatements.add(stmt);
			id++;
			idCommand++;

			stmt = String.format(stmt5, id, stream, stream);
			listOfStatements.add(stmt);
			stmt = String.format(stmt7, idCommand, id, idCommand, 5);
			listOfStatements.add(stmt);
			id++;
			idCommand++;
		}

	}

	public static void main(String[] args) {

		CreateTestRecords create = new CreateTestRecords();
		create.generateInConditions();
		create.generateOutConditions();
		try {
			create.out();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
