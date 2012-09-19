set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE JJ_20(FLOATTEST FLOAT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0357/JJ_20.csv' OVERWRITE INTO TABLE JJ_20;
CREATE TABLE JJ(FLOATTEST FLOAT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0357/JJ.csv' OVERWRITE INTO TABLE JJ;
set hive.ql.mode=sql;
SELECT FLOATTEST FROM JJ;
