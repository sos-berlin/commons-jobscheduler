/* Table for CONSUMED_IN_CONDITIONS */
DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_CONSUMED_IN_CONDITIONS'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE TABLE SOS_JS_CONSUMED_IN_CONDITIONS (	
	"ID"              NUMBER(19,0) NOT NULL, 
	"SESSION"         VARCHAR2(100 BYTE) NOT NULL, 
	"IN_CONDITION_ID" NUMBER(19,0) NOT NULL, 
	"CREATED"         DATE NOT NULL,
	CONSTRAINT SOS_JS_CONSUMED_IN_CONDITIONS UNIQUE ("SESSION", "IN_CONDITION_ID"),
    PRIMARY KEY ("ID")
   )
'; END IF; END;
/   

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_CONDITIONS_ID_SEQ'; IF c = 0 THEN EXECUTE IMMEDIATE '
   CREATE SEQUENCE  SOS_JS_CONDITIONS_ID_SEQ  MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 21 CACHE 20 NOORDER  CYCLE ;
'; END IF; END;
/


/* Table for SOS_JS_EVENTS */
DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_EVENTS'; IF c = 0 THEN EXECUTE IMMEDIATE '  
CREATE TABLE SOS_JS_EVENTS(	
	"ID"               NUMBER(19,0) NOT NULL, 
	"OUT_CONDITION_ID" NUMBER(19,0) NOT NULL,
	"SESSION"          VARCHAR2(100 BYTE) NOT NULL, 
	"WORKFLOW"         VARCHAR2(100 BYTE) NOT NULL, 
	"EVENT"            VARCHAR2(255 BYTE) NOT NULL, 
	"CREATED" DATE NOT NULL,

	CONSTRAINT SOS_JS_EVENTS UNIQUE ("SESSION", "EVENT", "WORKFLOW"),
    PRIMARY KEY ("ID")
   )  
'; END IF; END;
/   


DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_EVENTS_ID_SEQ'; IF c = 0 THEN EXECUTE IMMEDIATE '
   CREATE SEQUENCE  SOS_JS_EVENTS_ID_SEQ  MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 101 CACHE 20 NOORDER  CYCLE ;
'; END IF; END;
/


/* Table for SOS_JS_IN_CONDITION_COMMANDS */
DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_IN_CONDITION_COMMANDS'; IF c = 0 THEN EXECUTE IMMEDIATE '   
CREATE TABLE SOS_JS_IN_CONDITION_COMMANDS (	
	"ID"              NUMBER(19,0) NOT NULL, 
	"IN_CONDITION_ID" NUMBER(19,0) NOT NULL, 
	"COMMAND"         VARCHAR2(255 BYTE) NOT NULL,  
	"COMMAND_PARAM"   VARCHAR2(255 BYTE),
    PRIMARY KEY ("ID")
   )  
'; END IF; END;
/   
 
 DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_IN_CONDITION_CMD_ID_SEQ'; IF c = 0 THEN EXECUTE IMMEDIATE '
   CREATE SEQUENCE  SOS_JS_IN_CONDITION_CMD_ID_SEQ  MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 81 CACHE 20 NOORDER  CYCLE ;
'; END IF; END;
/


/* Table for SOS_JS_IN_CONDITION_COMMANDS */
DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_IN_CONDITIONS'; IF c = 0 THEN EXECUTE IMMEDIATE '   
  CREATE TABLE SOS_JS_IN_CONDITIONS (	
	"ID"         NUMBER(19,0) NOT NULL, 
	"MASTER_ID"  VARCHAR2(100 BYTE) NOT NULL, 
	"JOB"        VARCHAR2(255 BYTE) NOT NULL, 
	"WORKFLOW"   VARCHAR2(255 BYTE) NOT NULL, 
	"EXPRESSION" VARCHAR2(255 BYTE) NOT NULL
    PRIMARY KEY ("ID")
   )
'; END IF; END;
/    

 DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_IN_CONDITION_ID_SEQ'; IF c = 0 THEN EXECUTE IMMEDIATE '
   CREATE SEQUENCE  SOS_JS_IN_CONDITION_ID_SEQ  MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 61 CACHE 20 NOORDER  CYCLE ;
'; END IF; END;
/
 
 /* Table for SOS_JS_OUT_CONDITION_EVENTS */
DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_OUT_CONDITION_EVENTS'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE TABLE SOS_JS_OUT_CONDITION_EVENTS (
	"ID"               NUMBER(19,0) NOT NULL, 
	"OUT_CONDITION_ID" NUMBER(19,0) NOT NULL, 
	"EVENT"            VARCHAR2(255 BYTE) NOT NULL, 
    PRIMARY KEY ("ID")
   )
'; END IF; END;
/
    
 DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_OUT_CONDITION_EV_ID_SEQ'; IF c = 0 THEN EXECUTE IMMEDIATE '
   CREATE SEQUENCE  SOS_JS_OUT_CONDITION_EV_ID_SEQ  MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 21 CACHE 20 NOORDER  CYCLE ;
'; END IF; END;
/
   
 /* Table for SOS_JS_OUT_CONDITIONS */
DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_TABLES WHERE "TABLE_NAME"='SOS_JS_OUT_CONDITIONS'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE TABLE SOS_JS_OUT_CONDITIONS(
	"ID"         NUMBER(19,0) NOT NULL, 
	"MASTER_ID"  VARCHAR2(100 BYTE) NOT NULL, 
	"JOB"        VARCHAR2(255 BYTE) NOT NULL, 
	"WORKFLOW"   VARCHAR2(255 BYTE) NOT NULL, 
	"EXPRESSION" VARCHAR2(255 BYTE) NOT NULL,
    PRIMARY KEY ("ID")
   ) 
'; END IF; END;

/   
 DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_SEQUENCES WHERE "SEQUENCE_NAME"='SOS_JS_OUT_CONDITION_ID_SEQ'; IF c = 0 THEN EXECUTE IMMEDIATE '  
   CREATE SEQUENCE  SOS_JS_OUT_CONDITION_ID_SEQ  MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 21 CACHE 20 NOORDER  CYCLE ;
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_CONSUMED_IN_CONDITION_ID'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_CONSUMED_IN_CONDITION_ID   ON SOS_JS_CONSUMED_IN_CONDITIONS ( "IN_CONDITION_ID" )
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_CONSUMED_IN_SESSION'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_CONSUMED_IN_SESSION   ON SOS_JS_CONSUMED_IN_CONDITIONS ( "OUT_CONDITION_ID" )
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_OUT_CONDITION_ID'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_OUT_CONDITION_ID   ON SOS_JS_OUT_CONDITION_EVENTS ( "OUT_CONDITION_ID" )
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_OUT_CONDITION_ID'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_EVENT   ON SOS_JS_EVENTS ( "EVENT,WORKFLOW,SESSION"  )
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_IN_CONDITION_MASTER_ID'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_IN_CONDITION_MASTER_ID   ON SOS_JS_IN_CONDITIONS ( "MASTER_ID"  )
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_IN_CONDITION_JOB'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_IN_CONDITION_JOB   ON SOS_JS_IN_CONDITIONS ( "JOB"  )
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_IN_CONDITION_WORKFLOW'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_IN_CONDITION_WORKFLOW   ON SOS_JS_IN_CONDITIONS ( "WORKFLOW"  )
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_OUT_CONDITIONS_OUT_MASTER_ID'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_OUT_CONDITIONS_OUT_MASTER_ID   ON SOS_JS_OUT_CONDITIONS ( "CONDITION_OUT_CONDITIONS_OUT_MASTER_ID"  )
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_OUT_CONDITIONS_OUT_JOB'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_OUT_CONDITIONS_OUT_JOB   ON SOS_JS_OUT_CONDITIONS ( "JOB"  )
'; END IF; END;
/

DECLARE c number; BEGIN SELECT COUNT(*) INTO c FROM USER_INDEXES WHERE "INDEX_NAME"='CONDITION_OUT_CONDITIONS_OUT_WORKFLOW'; IF c = 0 THEN EXECUTE IMMEDIATE '
  CREATE INDEX CONDITION_OUT_CONDITIONS_OUT_WORKFLOW   ON SOS_JS_OUT_CONDITIONS ( "WORKFLOW"  )
'; END IF; END;
/
 

   



   