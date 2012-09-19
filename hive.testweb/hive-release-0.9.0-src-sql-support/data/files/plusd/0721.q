set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE STANDARD_INPUT(USER_NAME STRING,USER_INPUT DOUBLE,RECEIVABLE FLOAT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
CREATE TABLE USER_INPUT(USER_ID DOUBLE,USER_TYPED STRING,CASH_BALANCE DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0721/USER_INPUT.csv' OVERWRITE INTO TABLE USER_INPUT;
set hive.ql.mode=sql;
SELECT CAST (AVG (CAST (USER_TYPED AS INT)) AS INT) FROM USER_INPUT;
