set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE SIZ2_F9(F1 DOUBLE,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0037/SIZ2_F9.csv' OVERWRITE INTO TABLE SIZ2_F9;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM SIZ2_F9;
