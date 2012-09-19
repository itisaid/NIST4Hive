set hive.ql.mode=hql;
drop database SUN if exist;
create database SUN;
use SUN;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM SIZ3_P9;