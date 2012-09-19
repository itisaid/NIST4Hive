set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
set hive.ql.mode=sql;
SELECT NUM, COUNT(*) FROM "PStaff" GROUP BY NUM ORDER BY NUM;
