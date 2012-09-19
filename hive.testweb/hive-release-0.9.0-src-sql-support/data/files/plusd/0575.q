set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE STAFF3(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0575/STAFF3.csv' OVERWRITE INTO TABLE STAFF3;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM STAFF3 WHERE EMPNUM = 'E2';
