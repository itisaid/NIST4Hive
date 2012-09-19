set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE STAFF_P(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0053/STAFF_P.csv' OVERWRITE INTO TABLE STAFF_P;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM STAFF_P;
