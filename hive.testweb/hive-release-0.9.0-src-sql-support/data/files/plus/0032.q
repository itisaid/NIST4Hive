set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE SIZ2_F4(F1 STRING,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0032/SIZ2_F4.csv' OVERWRITE INTO TABLE SIZ2_F4;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM SIZ2_F4;