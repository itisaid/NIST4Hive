set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE WORKWEEK(EMPNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0707/WORKWEEK.csv' OVERWRITE INTO TABLE WORKWEEK;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM WORKWEEK WHERE HOURS > 40;
