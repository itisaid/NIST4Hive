set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE HH(SMALLTEST DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0940/HH.csv' OVERWRITE INTO TABLE HH;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM HH WHERE SMALLTEST = 9999;
