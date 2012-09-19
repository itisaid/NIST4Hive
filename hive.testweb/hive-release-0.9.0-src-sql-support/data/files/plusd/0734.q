set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE INCOMPLETES(ITEMTEXT STRING,CONDTEXT STRING,COSTTEXT STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0734/INCOMPLETES.csv' OVERWRITE INTO TABLE INCOMPLETES;
CREATE TABLE COMPLETES(ITEMTEXT STRING,CONDTEXT STRING,COSTTEXT STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0734/COMPLETES.csv' OVERWRITE INTO TABLE COMPLETES;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM COMPLETES;
