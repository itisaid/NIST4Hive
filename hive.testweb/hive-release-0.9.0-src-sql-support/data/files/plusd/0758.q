set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE VSTAFF3(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0758/VSTAFF3.csv' OVERWRITE INTO TABLE VSTAFF3;
CREATE TABLE STAFF3(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0758/STAFF3.csv' OVERWRITE INTO TABLE STAFF3;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE STAFF66(SALARY DOUBLE,EMPNAME STRING,GRADE DOUBLE,EMPNUM STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0758/STAFF66.csv' OVERWRITE INTO TABLE STAFF66;
set hive.ql.mode=sql;
SELECT EMPNUM, GRADE, HU.STAFF3.EMPNAME, CITY, SALARY, STAFF66.EMPNAME FROM HU.STAFF3 LEFT JOIN STAFF66 USING (GRADE, EMPNUM) WHERE EMPNUM = 'E3';
