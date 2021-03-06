set hive.ql.mode=hql;
drop database IF EXISTS SCHANZLE CASCADE;
create database SCHANZLE;
use SCHANZLE;
CREATE TABLE CPBASE(KC DOUBLE,JUNK1 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0437/CPBASE.csv' OVERWRITE INTO TABLE CPBASE;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM CPBASE WHERE JUNK1 LIKE 'P%X%%' ESCAPE 'X';
