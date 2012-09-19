set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE UPUNIQ(NUMKEY DOUBLE,COL2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0443/UPUNIQ.csv' OVERWRITE INTO TABLE UPUNIQ;
drop database IF EXISTS SCHANZLE CASCADE;
create database SCHANZLE;
use SCHANZLE;
set hive.ql.mode=sql;
SELECT COL2 FROM HU.UPUNIQ WHERE NUMKEY = 1;
