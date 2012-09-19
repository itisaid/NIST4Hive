set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE STAFF_C(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING,MGR STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0584/STAFF_C.csv' OVERWRITE INTO TABLE STAFF_C;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM STAFF_C WHERE MGR = 'E9';