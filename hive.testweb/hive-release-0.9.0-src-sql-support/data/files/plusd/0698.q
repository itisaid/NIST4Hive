set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE WEIRDPAD(NAAM STRING,SPONSOR STRING,PADCHAR STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0698/WEIRDPAD.csv' OVERWRITE INTO TABLE WEIRDPAD;
set hive.ql.mode=sql;
SELECT TRIM (LEADING 'X' FROM SPONSOR) FROM WEIRDPAD WHERE TRIM (TRAILING 'X' FROM SPONSOR) = 'XXXXKATE';
