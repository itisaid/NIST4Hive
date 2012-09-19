set hive.ql.mode=hql;
drop database IF EXISTS CTS1 CASCADE;
create database CTS1;
use CTS1;
CREATE TABLE STAFFD(EMPNUM STRING,GRADE DOUBLE,MGR STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0865/STAFFD.csv' OVERWRITE INTO TABLE STAFFD;
set hive.ql.mode=sql;
SELECT COUNT (*) FROM STAFFd WHERE GRADE = 24;