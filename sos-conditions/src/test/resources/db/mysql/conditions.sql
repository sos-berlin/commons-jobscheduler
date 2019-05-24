set SQL_MODE=ANSI_QUOTES;

  CREATE TABLE SOS_JS_CONSUMED_IN_CONDITIONS (	
	"ID" INT NOT NULL AUTO_INCREMENT, 
	"SESSION" VARCHAR(100)  CHARACTER SET latin1 NOT NULL, 
	"IN_CONDITION_ID" INT NOT NULL, 
	"CREATED" DATETIME NOT NULL,
	CONSTRAINT SOS_JS_CONSUMED_IN_CONDITIONS UNIQUE ("SESSION", "IN_CONDITION_ID"),
    PRIMARY KEY ("ID")
   )
   

  CREATE TABLE SOS_JS_EVENTS(	
	"ID" INT NOT NULL AUTO_INCREMENT, 
	"OUT_CONDITION_ID" INT NOT NULL, 
	"SESSION" VARCHAR(100) CHARACTER SET latin1 NOT NULL, 
	"EVENT" VARCHAR(255) CHARACTER SET latin1 NOT NULL, 
	"CREATED" DATETIME NOT NULL,
	CONSTRAINT SOS_JS_EVENTS UNIQUE ("SESSION", "EVENT"),
    PRIMARY KEY ("ID")
   )
   

  CREATE TABLE SOS_JS_IN_CONDITION_COMMANDS (	
	"ID" INT NOT NULL AUTO_INCREMENT, 
	"IN_CONDITION_ID" INT NOT NULL, 
	"COMMAND" VARCHAR(255) CHARACTER SET latin1 NOT NULL,  
	"COMMAND_PARAM" VARCHAR(255),
    PRIMARY KEY ("ID")
   )  
 

  CREATE TABLE SOS_JS_IN_CONDITIONS (	
	"ID" INT NOT NULL AUTO_INCREMENT, 
	"MASTER_ID" VARCHAR(100) CHARACTER SET latin1 NOT NULL, 
	"JOB" VARCHAR(255) CHARACTER SET latin1 NOT NULL, 
	"WORKFLOW" VARCHAR(255) CHARACTER SET latin1 NOT NULL, 
	"EXPRESSION" VARCHAR(255) NOT NULL
    PRIMARY KEY ("ID"),
   )
 
 
  CREATE TABLE SOS_JS_OUT_CONDITION_EVENTS (
	"ID" INT NOT NULL AUTO_INCREMENT, 
	"OUT_CONDITION_ID" INT NOT NULL, 
	"EVENT" VARCHAR(255) CHARACTER SET latin1 NOT NULL, 
    PRIMARY KEY ("ID")
   )

   

  CREATE TABLE SOS_JS_OUT_CONDITIONS(
	"ID" INT NOT NULL AUTO_INCREMENT, 
	"MASTER_ID" VARCHAR(100) CHARACTER SET latin1 NOT NULL, 
	"JOB" VARCHAR(255) CHARACTER SET latin1 NOT NULL, 
	"WORKFLOW" VARCHAR(255) CHARACTER SET latin1 NOT NULL, 
	"EXPRESSION" VARCHAR(255) CHARACTER SET latin1 NOT NULL,
    PRIMARY KEY ("ID")
   ) 
   
    