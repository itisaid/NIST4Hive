set hive.ql.mode=hql;
drop database IF EXISTS FLATER CASCADE;
create database FLATER;
use FLATER;
CREATE TABLE NAMGRP2(EMPNUM DOUBLE,NAME STRING,GRP DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0647/NAMGRP2.csv' OVERWRITE INTO TABLE NAMGRP2;
set hive.ql.mode=sql;
SELECT EMPNUM FROM NAMGRP2 WHERE NAME = 'KERI' AND GRP = 10;
