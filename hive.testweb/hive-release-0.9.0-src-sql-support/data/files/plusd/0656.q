set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE NAMGRP3(EMPNUM DOUBLE,NAME STRING,GRP DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0656/NAMGRP3.csv' OVERWRITE INTO TABLE NAMGRP3;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM NAMGRP3 WHERE EMPNUM = 9 AND NAME = 'BARRY' AND GRP IS NULL;
