set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE V_WORKS1(EMPNUM STRING,PNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0299/V_WORKS1.csv' OVERWRITE INTO TABLE V_WORKS1;
CREATE TABLE WORKS1(EMPNUM STRING,PNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0299/WORKS1.csv' OVERWRITE INTO TABLE WORKS1;
CREATE TABLE STAFF1(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0299/STAFF1.csv' OVERWRITE INTO TABLE STAFF1;
CREATE TABLE PROJ1(PNUM STRING,PNAME STRING,PTYPE STRING,BUDGET DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0299/PROJ1.csv' OVERWRITE INTO TABLE PROJ1;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM STAFF1,WORKS1,PROJ1 WHERE STAFF1.EMPNUM = 'E9' AND STAFF1.EMPNUM = WORKS1.EMPNUM AND PROJ1.PNUM = WORKS1.PNUM;
