set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE VSTAFF3(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0082/VSTAFF3.csv' OVERWRITE INTO TABLE VSTAFF3;
CREATE TABLE STAFF3(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0082/STAFF3.csv' OVERWRITE INTO TABLE STAFF3;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
set hive.ql.mode=sql;
SELECT EMPNUM, EMPNAME, GRADE, CITY FROM HU.STAFF3 WHERE EMPNUM = 'E1';
