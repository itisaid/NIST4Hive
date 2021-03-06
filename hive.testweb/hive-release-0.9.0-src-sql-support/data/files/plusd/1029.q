set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE AA(CHARTEST STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/1029/AA.csv' OVERWRITE INTO TABLE AA;
set hive.ql.mode=sql;
SELECT * FROM AA ORDER BY CHARTEST;
