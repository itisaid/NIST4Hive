set hive.ql.mode=hql;
drop database  CUGINI if exist;
create database  CUGINI;
use  CUGINI;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM CUGINI.HH;