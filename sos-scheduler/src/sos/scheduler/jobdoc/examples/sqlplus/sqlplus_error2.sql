Set Echo on
WHENEVER SQLERROR EXIT SQL.SQLCODE
WHENEVER OSERROR EXIT FAILURE
-- ORAERROR 00942 expected
select &SELECT from dual1;
prompt fertig;
prompt db_user = &DB_USER;
prompt var_select = &SELECT;
prompt SET varname IS varWert;
exit;