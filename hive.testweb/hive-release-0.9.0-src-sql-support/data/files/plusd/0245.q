set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE TEMP_SS(EMPNUM STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0245/TEMP_SS.csv' OVERWRITE INTO TABLE TEMP_SS;
CREATE TABLE SS(NUMTEST DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0245/SS.csv' OVERWRITE INTO TABLE SS;
set hive.ql.mode=sql;
SELECT * FROM SS;
