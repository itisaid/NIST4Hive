set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE VERBOSE_INV(ITEMTEXT STRING,CONDTEXT STRING,COSTTEXT STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0732/VERBOSE_INV.csv' OVERWRITE INTO TABLE VERBOSE_INV;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM VERBOSE_INV;
