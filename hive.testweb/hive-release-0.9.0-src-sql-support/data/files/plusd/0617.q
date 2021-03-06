set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE EXACT_DEF(BODY_TEMP DOUBLE,MAX_NUM DOUBLE,MIN_NUM DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0617/EXACT_DEF.csv' OVERWRITE INTO TABLE EXACT_DEF;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM EXACT_DEF WHERE BODY_TEMP = 99.0 AND MAX_NUM = -55555 AND MIN_NUM = .000001 OR BODY_TEMP = 98.6 AND MAX_NUM = 100 AND MIN_NUM = .2;
