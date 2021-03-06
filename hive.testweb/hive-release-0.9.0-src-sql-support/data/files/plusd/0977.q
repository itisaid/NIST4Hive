set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE SIZ3_P5(F1 DOUBLE,F2 STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0977/SIZ3_P5.csv' OVERWRITE INTO TABLE SIZ3_P5;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM SIZ3_P5 WHERE F1 = 11;
