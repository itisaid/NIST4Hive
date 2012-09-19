set hive.ql.mode=hql;
drop database HU if exist;
create database HU;
use HU;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM STAFF3;