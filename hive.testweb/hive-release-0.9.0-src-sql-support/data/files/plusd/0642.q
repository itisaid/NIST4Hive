set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE ICAST2(C1 DOUBLE,C2 FLOAT,C3 DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0642/ICAST2.csv' OVERWRITE INTO TABLE ICAST2;
set hive.ql.mode=sql;
SELECT C1, C2, C3 FROM ICAST2;
