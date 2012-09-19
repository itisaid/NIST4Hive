set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE SIZ1_P(S1 STRING,S2 STRING,S3 DOUBLE,S4 STRING,S5 DOUBLE,S6 STRING,R1 STRING,R2 STRING,R3 DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0967/SIZ1_P.csv' OVERWRITE INTO TABLE SIZ1_P;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM SIZ1_P WHERE S1 = 'E1' AND S2 = 'TTT' AND S3 = 1;
