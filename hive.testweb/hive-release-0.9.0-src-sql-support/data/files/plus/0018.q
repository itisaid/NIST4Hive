set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE UUSIG(U1 DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0018/UUSIG.csv' OVERWRITE INTO TABLE UUSIG;
CREATE TABLE UUUSIG(IRREVERENT DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0018/UUUSIG.csv' OVERWRITE INTO TABLE UUUSIG;
CREATE TABLE USIG(C1 DOUBLE,C_1 DOUBLE,COL3 DOUBLE,COL4 DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0018/USIG.csv' OVERWRITE INTO TABLE USIG;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM USIG;