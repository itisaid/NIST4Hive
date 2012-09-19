set hive.ql.mode=hql;
drop database IF EXISTS CUGINI CASCADE;
create database CUGINI;
use CUGINI;
CREATE TABLE FF(INTTEST DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0487/FF.csv' OVERWRITE INTO TABLE FF;
drop database IF EXISTS SCHANZLE CASCADE;
create database SCHANZLE;
use SCHANZLE;
set hive.ql.mode=sql;
SELECT INTTEST FROM CUGINI.FF;
