set hive.ql.mode=hql;
drop database SULLIVAN if exist;
create database SULLIVAN;
use SULLIVAN;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM TTT;