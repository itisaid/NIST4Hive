set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE VSTAFF3(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0089/VSTAFF3.csv' OVERWRITE INTO TABLE VSTAFF3;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM HU.VSTAFF3;
