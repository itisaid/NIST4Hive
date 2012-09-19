set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE NAMGRP2(EMPNUM DOUBLE,NAME STRING,GRP DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0649/NAMGRP2.csv' OVERWRITE INTO TABLE NAMGRP2;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM NAMGRP2 WHERE NAME <> 'MARY' AND NAME <> 'KERI' OR GRP <> 20 AND GRP <> 10 OR EMPNUM <> 0 AND EMPNUM <> 1 OR NAME IS NULL OR GRP IS NULL OR EMPNUM IS NULL;
