set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE PROJ_M(PNUM STRING,PNAME STRING,PTYPE STRING,BUDGET DOUBLE,CITY STRING,MGR STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0024/PROJ_M.csv' OVERWRITE INTO TABLE PROJ_M;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM PROJ_M;
