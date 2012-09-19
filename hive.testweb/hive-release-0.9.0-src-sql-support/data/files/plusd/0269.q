set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE T4(STR110 STRING,NUM6 DOUBLE,COL3 STRING,COL4 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0269/T4.csv' OVERWRITE INTO TABLE T4;
set hive.ql.mode=sql;
SELECT STR110 FROM T4 WHERE STR110 = USER;
