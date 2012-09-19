set hive.ql.mode=hql;
drop database IF EXISTS HU CASCADE;
create database HU;
use HU;
CREATE TABLE GG(REALTEST FLOAT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0956/GG.csv' OVERWRITE INTO TABLE GG;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM GG WHERE REALTEST > -0.1048576 AND REALTEST < -0.1048574;
