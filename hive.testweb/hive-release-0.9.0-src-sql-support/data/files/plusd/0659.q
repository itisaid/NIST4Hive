set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE NMGRP3(NAME STRING,GRP DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0659/NMGRP3.csv' OVERWRITE INTO TABLE NMGRP3;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM NMGRP3 WHERE NAME = 'MARY' AND GRP = 30;
