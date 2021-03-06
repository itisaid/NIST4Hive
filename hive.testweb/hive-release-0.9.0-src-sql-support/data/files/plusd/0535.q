set hive.ql.mode=hql;
drop database IF EXISTS SUN CASCADE;
create database SUN;
use SUN;
CREATE TABLE STAFF4(EMPNUM STRING,EMPNAME STRING,GRADE DOUBLE,CITY STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;
LOAD DATA LOCAL INPATH '_datapath_/plusd/0535/STAFF4.csv' OVERWRITE INTO TABLE STAFF4;
set hive.ql.mode=sql;
SELECT GRADE FROM STAFF4 WHERE (EMPNAME IS NULL) AND CITY = '               ';
