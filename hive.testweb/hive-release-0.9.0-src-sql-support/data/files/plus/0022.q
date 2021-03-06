set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE WORKS3(EMPNUM STRING,PNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0022/WORKS3.csv' OVERWRITE INTO TABLE WORKS3;
CREATE TABLE WORKS(EMPNUM STRING,PNUM STRING,HOURS DOUBLE) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0022/WORKS.csv' OVERWRITE INTO TABLE WORKS;
set hive.ql.mode=sql;
SELECT COUNT(*) FROM WORKS;
