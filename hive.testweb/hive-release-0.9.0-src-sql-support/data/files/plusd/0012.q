set hive.ql.mode=hql;
drop database IF EXISTS CUGINI CASCADE;
create database CUGINI;
use CUGINI;
CREATE TABLE GG(C1 DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0012/GG.csv' OVERWRITE INTO TABLE GG;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM CUGINI.GG;
