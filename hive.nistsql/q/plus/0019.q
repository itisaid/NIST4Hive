set hive.ql.mode=hql;
drop database FLATER if exist;
create database FLATER;
use FLATER;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM U_SIG;