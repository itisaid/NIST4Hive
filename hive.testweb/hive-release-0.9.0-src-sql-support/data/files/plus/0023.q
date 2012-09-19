set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE STAFF_M(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING,PRI_WK STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0023/STAFF_M.csv' OVERWRITE INTO TABLE STAFF_M;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM STAFF_M;
